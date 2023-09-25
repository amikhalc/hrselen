package com.amikhalchenko.hrselen.dao;

import com.amikhalchenko.hrselen.common.CandidateState;
import com.amikhalchenko.hrselen.entity.Candidate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateDao extends JpaRepository<Candidate, Integer> {

    Optional<Candidate> findByMobile(String mobile);

    Optional<List<Candidate>> findAllByStatus(CandidateState state);
}
