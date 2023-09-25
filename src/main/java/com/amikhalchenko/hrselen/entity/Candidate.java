package com.amikhalchenko.hrselen.entity;

import com.amikhalchenko.hrselen.common.CandidateState;
import com.amikhalchenko.hrselen.common.CandidateType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    @Enumerated(EnumType.STRING)
    private CandidateType role;
    @Enumerated(EnumType.STRING)
    private CandidateState status;
    private String source;
    private String mobile;
    private String comment;
    private LocalDateTime commentDate;
    private LocalDateTime dbCreateTime;
    private LocalDateTime dbUpdateTime;


}
