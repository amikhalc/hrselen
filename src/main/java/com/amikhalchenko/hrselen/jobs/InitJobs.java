package com.amikhalchenko.hrselen.jobs;

import com.amikhalchenko.hrselen.service.SeleniumService;
import org.springframework.context.annotation.Profile;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Profile("init")
@Component
public class InitJobs {

    SeleniumService seleniumService;

    public InitJobs(SeleniumService seleniumService) {
        this.seleniumService = seleniumService;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void initResources() {
        seleniumService.initResources();
    }

}
