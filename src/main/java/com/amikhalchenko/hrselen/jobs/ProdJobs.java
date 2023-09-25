package com.amikhalchenko.hrselen.jobs;


import com.amikhalchenko.hrselen.SeleniumProperties;
import com.amikhalchenko.hrselen.common.CandidateType;
import com.amikhalchenko.hrselen.entity.Candidate;
import com.amikhalchenko.hrselen.service.SeleniumService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Profile("prod")
@Component
@Slf4j
public class ProdJobs {


    SeleniumService seleniumService;

    SeleniumProperties seleniumProperties;

    public ProdJobs(SeleniumService seleniumService, SeleniumProperties seleniumProperties) {
        this.seleniumProperties = seleniumProperties;
        this.seleniumService = seleniumService;
    }

    @Scheduled(fixedDelayString = "${com.amikhalchenko.hrselen.group-check-interval}")
    public void monitorLeadsInGroup() {
        if(!seleniumProperties.getTelegramGroupCheckEnabled()) return;
        seleniumService.monitorLeads();
    }

    @Scheduled(cron = "${com.amikhalchenko.hrselen.superjob-parse-cron}")
    public void parseSuperjob() {
        if (!seleniumProperties.getSuperjobParseEnabled()) return;
        List<Candidate> parsedList = new ArrayList<>();
        parsedList.addAll(seleniumService.parseSuperjob(CandidateType.BICYCLE));
        parsedList.addAll(seleniumService.parseSuperjob(CandidateType.PACKING));
        log.info("Finished parsing superjob at: " + LocalDateTime.now() +". Sending");
        seleniumService.sendWaMessage(parsedList);
    }
}
