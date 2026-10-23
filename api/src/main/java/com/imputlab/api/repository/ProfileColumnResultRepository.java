package com.imputlab.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.imputlab.api.entity.ProfileColumnResult;

public interface ProfileColumnResultRepository
        extends JpaRepository<ProfileColumnResult, Long> {

    List<ProfileColumnResult> findAllByAnalysisRun_IdOrderByIdAsc(Long analysisRunId);
}
