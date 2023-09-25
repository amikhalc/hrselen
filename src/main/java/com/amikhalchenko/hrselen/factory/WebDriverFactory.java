package com.amikhalchenko.hrselen.factory;

import com.amikhalchenko.hrselen.SeleniumProperties;
import com.amikhalchenko.hrselen.common.WebDriverScope;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class WebDriverFactory {

    SeleniumProperties seleniumProperties;
    private final List<WebDriver> drivers;

    public WebDriverFactory(SeleniumProperties seleniumProperties) {
        this.seleniumProperties = seleniumProperties;
        this.drivers = new ArrayList<>();
    }

    public WebDriver getDriver(WebDriverScope webDriverScope){
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--allow-profiles-outside-user-dir");
        options.addArguments("--enable-profile-shortcut-manager");
        switch (webDriverScope) {
            case TELEGRAM -> {
                options.addArguments("user-data-dir=telegram");
                options.addArguments("--remote-debugging-port=9223");
                options.addArguments("--headless=new");
            }
            case SUPERJOB -> {
                options.addArguments("user-data-dir=selenium");
                options.addArguments("--remote-debugging-port=9222");
            } case WHATSAPP -> {
                options.addArguments("user-data-dir=whatsapp");
                options.addArguments("--remote-debugging-port=9224");
            }
        }
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(seleniumProperties.getSeleniumImplicitWait()));
        drivers.add(driver);
        return driver;
    }

    @EventListener(ContextClosedEvent.class)
    public void quitAllDrivers() {
        for (WebDriver driver: drivers) {
            driver.quit();
        }
    }
}

