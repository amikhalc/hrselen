package com.amikhalchenko.hrselen.service;

import com.amikhalchenko.hrselen.dao.CandidateDao;
import com.amikhalchenko.hrselen.entity.Candidate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandidateService {

    CandidateDao candidateDao;

    public CandidateService(CandidateDao candidateDao) {
        this.candidateDao = candidateDao;
    }


    public ResponseEntity<List<Candidate>> getAllCustomers() {
        return new ResponseEntity<>(candidateDao.findAll(), HttpStatus.OK);
    }
}
