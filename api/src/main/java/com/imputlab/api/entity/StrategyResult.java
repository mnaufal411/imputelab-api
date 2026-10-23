package com.imputlab.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "strategy_results")
public class StrategyResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "column_result_id", nullable = false)
    private ColumnResult columnResult;

    @Column(name = "strategy", nullable = false, length = 30)
    private String strategy;

    @Column(name = "evaluated_count", nullable = false)
    private Integer evaluatedCount;

    @Column(name = "mean_mae", nullable = false)
    private Double meanMae;

    @Column(name = "mean_rmse", nullable = false)
    private Double meanRmse;

    @Column(name = "rmse_std_dev", nullable = false)
    private Double rmseStdDev;

    protected StrategyResult() {
        // Required by JPA
    }

    public StrategyResult(
            ColumnResult columnResult,
            String strategy,
            Integer evaluatedCount,
            Double meanMae,
            Double meanRmse,
            Double rmseStdDev
    ) {
        this.columnResult = columnResult;
        this.strategy = strategy;
        this.evaluatedCount = evaluatedCount;
        this.meanMae = meanMae;
        this.meanRmse = meanRmse;
        this.rmseStdDev = rmseStdDev;
    }

    public Long getId() {
        return id;
    }

    public ColumnResult getColumnResult() {
        return columnResult;
    }

    public String getStrategy() {
        return strategy;
    }

    public Integer getEvaluatedCount() {
        return evaluatedCount;
    }

    public Double getMeanMae() {
        return meanMae;
    }

    public Double getMeanRmse() {
        return meanRmse;
    }

    public Double getRmseStdDev() {
        return rmseStdDev;
    }
}
