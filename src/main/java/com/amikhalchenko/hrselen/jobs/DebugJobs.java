package com.amikhalchenko.hrselen.jobs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;

import org.springframework.stereotype.Component;

@Component
@Profile("debug")
@Slf4j
public class DebugJobs {

}
