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
@Table(name = "column_results")
public class ColumnResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_run_id", nullable = false)
    private AnalysisRun analysisRun;

    @Column(name = "column_name", nullable = false, length = 255)
    private String columnName;

    @Column(name = "observed_count", nullable = false)
    private Integer observedCount;

    protected ColumnResult() {
        // Required by JPA
    }

    public ColumnResult(
            AnalysisRun analysisRun,
            String columnName,
            Integer observedCount
    ) {
        this.analysisRun = analysisRun;
        this.columnName = columnName;
        this.observedCount = observedCount;
    }

    public Long getId() {
        return id;
    }

    public AnalysisRun getAnalysisRun() {
        return analysisRun;
    }

    public String getColumnName() {
        return columnName;
    }

    public Integer getObservedCount() {
        return observedCount;
    }
}
