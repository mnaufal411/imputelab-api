package com.imputlab.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.imputlab.api.entity.AnalysisRun;

public interface AnalysisRunRepository
        extends JpaRepository<AnalysisRun, Long> {

    List<AnalysisRun> findAllByOrderByCreatedAtDesc();
}
