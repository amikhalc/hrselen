package com.amikhalchenko.hrselen.service;

import com.amikhalchenko.hrselen.SeleniumProperties;
import com.amikhalchenko.hrselen.common.CandidateState;
import com.amikhalchenko.hrselen.common.CandidateType;
import com.amikhalchenko.hrselen.common.WebDriverScope;
import com.amikhalchenko.hrselen.dao.CandidateDao;
import com.amikhalchenko.hrselen.entity.Candidate;
import com.amikhalchenko.hrselen.factory.WebDriverFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@EnableScheduling
@Service
@Slf4j
@Setter
@Getter
public class SeleniumService {

    CandidateDao candidateDao;
    WebDriverFactory webDriverFactory;
    SeleniumProperties seleniumProperties;
    WebDriver telegramDriver;

    @Autowired
    public SeleniumService(CandidateDao candidateDao, SeleniumProperties seleniumProperties, WebDriverFactory webDriverFactory) {
        this.candidateDao = candidateDao;
        this.webDriverFactory = webDriverFactory;
        this.seleniumProperties = seleniumProperties;
        this.telegramDriver = webDriverFactory.getDriver(WebDriverScope.TELEGRAM);
        telegramDriver.get(seleniumProperties.getTelegramGroupUrl());
        log.info("Telegram checking started");
    }

    public void sendWaMessage(List<Candidate> candidates) {
        if (candidates.size() == 0) return;
        WebDriver waDriver = webDriverFactory.getDriver(WebDriverScope.WHATSAPP);
        for (Candidate candidate : candidates) {
            log.info("Sending message to: " + candidate.getMobile());
            waDriver.get("https://web.whatsapp.com/send/?phone=" + candidate.getMobile() + "&text=" + seleniumProperties.getMessages().get(candidate.getRole()));
            try {
                waDriver.findElement(By.cssSelector("span[data-icon='send']")).click();
            } catch (Exception e) {
                log.warn("Phone not registered in wa: " + candidate.getMobile());
                candidate.setStatus(CandidateState.NOWA);
                candidateDao.save(candidate);
                continue;
            }
            candidate.setStatus(CandidateState.REACHED);
            candidateDao.save(candidate);
            waDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
            waDriver.findElements(By.cssSelector("span[aria-label=' Sent ']"));
            waDriver.findElements(By.cssSelector("span[aria-label=' Delivered ']"));
            waDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(seleniumProperties.getSeleniumImplicitWait()));
        }
        waDriver.quit();
    }

    public void initResources() {
        WebDriver superjobDriver = webDriverFactory.getDriver(WebDriverScope.SUPERJOB);
        WebDriver waDriver = webDriverFactory.getDriver(WebDriverScope.WHATSAPP);
        waDriver.get("https://web.whatsapp.com");
        superjobDriver.get("https://www.superjob.ru/");
        superjobDriver.switchTo().newWindow(WindowType.TAB);
        superjobDriver.get("https://www.hh.ru/");
    }

    public void monitorLeads() {
        List<WebElement> messages = telegramDriver.findElements(By.cssSelector("div.message.spoilers-container"));
        WebElement input = telegramDriver.findElement(By.cssSelector("div[data-placeholder='Message']"));
        WebElement sendButton = telegramDriver.findElement(By.cssSelector("div.btn-send-container"));
        String lastMessage = messages.get(messages.size() - 1).getText().split("\n")[0];
        if (lastMessage.matches(seleniumProperties.getAvoidRegexp())) return;
        if (lastMessage.matches(seleniumProperties.getRegionRegexp())) {
            log.info("Replied for lead at: " + LocalDateTime.now() + ". Message: " + lastMessage);
            input.sendKeys("+");
            sendButton.click();
        }
    }

    public List<Candidate> parseSuperjob(CandidateType type) {
        log.info("Started parsing superjob at: " + LocalDateTime.now());
        List<Candidate> parsedCandidates = new ArrayList<>();
        WebDriver superjobDriver = webDriverFactory.getDriver(WebDriverScope.SUPERJOB);
        superjobDriver.get(seleniumProperties.getSuperjobSearchUrls().get(type));
        expandAllContacts(superjobDriver);
        List<WebElement> profiles = superjobDriver.findElements(By.cssSelector("div.f-test-search-result-item"));
        for (WebElement profile : profiles) {
            Candidate parsedCandidate = parseCandidateFrame(profile);
            if (parsedCandidate == null) continue;
            parsedCandidate.setRole(type);
            Candidate savedCandidate;
            try {
                savedCandidate = candidateDao.save(parsedCandidate);
            } catch (Exception e) {
                savedCandidate = candidateDao.findByMobile(parsedCandidate.getMobile()).get();
                log.info("this mobile is already used");
            }
            //retrying 5 times to pass Stale element exception
            for (int i = 0; i < 5; i++) {
                try {
                    profile.findElement(By.xpath("./div[@class='f-test-search-result-item']/div/div/div/div/div/div/div[3]/div/div[4]/div/div[2]/div")).click();
                    break;
                } catch (Exception e) {
                    log.info("Stale exception on comments");
                }
            }
            superjobDriver.findElement(By.cssSelector("textarea[name='comment']")).sendKeys("+",Keys.RETURN);
            //retrying 5 times to pass Stale element exception
            for (int i = 0; i < 5; i++) {
                try {
                    profile.findElement(By.xpath("./div[@class='f-test-search-result-item']/div/div/div/div/div/div/div[3]/div/div[4]/div/div[2]/div")).click();
                    break;
                } catch (Exception e) {
                    log.info("Stale exception on comments");
                }
            }
            parsedCandidates.add(savedCandidate);
        }
        superjobDriver.quit();
        log.info("Saved " + parsedCandidates.size() + " candidates of " + type + " type." );
        return parsedCandidates;
    }

    public void expandAllContacts(WebDriver driver) {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
        int buttons = driver.findElements(By.cssSelector("button[class*='f-test-button-Pokazat_kontakty']")).size();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(seleniumProperties.getSeleniumImplicitWait()));
        if (buttons == 0) return;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10), Duration.ofMillis(500));
        for (int i = 0; i < buttons; i++) { // amount of buttons
            for (int j = 0; j < 5; j++) { // amount of retries
                try {
                    log.debug("buttons left: " + driver.findElements(By.cssSelector("button[class*='f-test-button-Pokazat_kontakty']")).size());
                    log.debug("clicking");
                    WebElement button = driver.findElement(By.cssSelector("button[class*='f-test-button-Pokazat_kontakty']"));
                    button.click();
                    log.debug("success");
                    wait.until(ExpectedConditions.stalenessOf(button));
                    break;
                } catch (StaleElementReferenceException exception) {
                    log.warn("Stale element during opening contacts");
                }
            }
        }
    }

    public Candidate parseCandidateFrame(WebElement profile) {
        List<WebElement> subElements = profile.findElements(By.xpath("./div/div/div/div/div/div/div"));
        // not a person frame
        if (subElements.size() != 3) return null;
        String number = subElements.get(2).findElement(By.xpath("./div/div[2]/div/div/div/div[2]/div/div[1]/div")).getText().split("\n")[0].replaceAll("[+ ]", "");
        //missing number
        if (number.equalsIgnoreCase("неуказано")) return null;
        WebElement comments = subElements.get(2).findElement(By.xpath("./div/div[4]/div/div[2]/div/div[1]"));
        //already commented
        if (!comments.getText().equalsIgnoreCase("добавить комментарий")) {
            return null;
        }
        String[] fullName = subElements.get(1).findElement(By.xpath("./div/div[2]/div[2]/span[1]")).getText().split(",")[0].split(" ");
        String firstName = fullName[0];
        String lastName = fullName.length > 1 ? fullName[1].replaceAll(",", "") : "Unknown";
        Candidate candidate = new Candidate();
        candidate.setFirstName(firstName);
        candidate.setLastName(lastName);
        candidate.setMobile(number);
        candidate.setStatus(CandidateState.NEW);
        candidate.setSource("superjob");
        log.info(candidate.toString());
        return candidate;
    }

    public ResponseEntity<String> reachNewCandidates() {
        List<Candidate> candidates = candidateDao.findAllByStatus(CandidateState.NEW).get();
        sendWaMessage(candidates);
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }
}
