package com.amikhalchenko.hrselen;

import com.amikhalchenko.hrselen.common.CandidateType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Getter
@Setter
@ConfigurationProperties("com.amikhalchenko.hrselen")
public class SeleniumProperties {

    String telegramGroupUrl;
    String superjobDebugUrl;
    String regionRegexp;
    String avoidRegexp;
    String groupCheckInterval;
    Boolean telegramGroupCheckEnabled;
    Boolean superjobParseEnabled;
    String superjobParseCron;
    Integer seleniumImplicitWait;
    Map<CandidateType, String> messages;
    Map<CandidateType, String> superjobSearchUrls;

    public SeleniumProperties() {
        messages = new HashMap<>();
        superjobSearchUrls = new HashMap<>();
        messages.put(CandidateType.BICYCLE, "bycicle_message");
        messages.put(CandidateType.PACKING, "packing_message");
        superjobSearchUrls.put(CandidateType.BICYCLE, "bicycle_url");
        superjobSearchUrls.put(CandidateType.PACKING, "packing_url");
    }
}
