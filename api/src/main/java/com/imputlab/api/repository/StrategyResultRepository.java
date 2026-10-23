package com.imputlab.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.imputlab.api.entity.StrategyResult;

public interface StrategyResultRepository
        extends JpaRepository<StrategyResult, Long> {

    List<StrategyResult> findAllByColumnResult_IdOrderByIdAsc(
            Long columnResultId
    );
}
