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
@Table(name = "profile_column_results")
public class ProfileColumnResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_run_id", nullable = false)
    private AnalysisRun analysisRun;

    @Column(name = "column_name", nullable = false, length = 255)
    private String columnName;

    @Column(name = "missing_count", nullable = false)
    private Long missingCount;

    @Column(name = "non_missing_count", nullable = false)
    private Long nonMissingCount;

    @Column(name = "missing_percentage", nullable = false)
    private Double missingPercentage;

    protected ProfileColumnResult() {
        // Required by JPA
    }

    public ProfileColumnResult(
            AnalysisRun analysisRun,
            String columnName,
            long missingCount,
            long nonMissingCount,
            double missingPercentage
    ) {
        this.analysisRun = analysisRun;
        this.columnName = columnName;
        this.missingCount = missingCount;
        this.nonMissingCount = nonMissingCount;
        this.missingPercentage = missingPercentage;
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

    public Long getMissingCount() {
        return missingCount;
    }

    public Long getNonMissingCount() {
        return nonMissingCount;
    }

    public Double getMissingPercentage() {
        return missingPercentage;
    }
}
