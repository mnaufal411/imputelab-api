package com.imputlab.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.imputlab.api.entity.ColumnResult;

public interface ColumnResultRepository
        extends JpaRepository<ColumnResult, Long> {

    List<ColumnResult> findAllByAnalysisRun_IdOrderByIdAsc(
            Long analysisRunId
    );
}
