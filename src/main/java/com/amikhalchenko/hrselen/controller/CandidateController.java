package com.amikhalchenko.hrselen.controller;

import com.amikhalchenko.hrselen.service.CandidateService;
import com.amikhalchenko.hrselen.service.SeleniumService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("candidate")
public class CandidateController {

    CandidateService candidateService;

    SeleniumService seleniumService;


    public CandidateController(CandidateService candidateService, SeleniumService seleniumService) {
        this.candidateService = candidateService;
        this.seleniumService = seleniumService;
    }

    @GetMapping("reachnewcandidates")
    public ResponseEntity<String> reachNewCandidates() {
        return seleniumService.reachNewCandidates();
    }

}
