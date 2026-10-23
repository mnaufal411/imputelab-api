package com.imputlab.api.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "analysis_runs")
public class AnalysisRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "analysis_type", nullable = false, length = 20)
    private String analysisType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "row_count")
    private Long rowCount;

    @Column(name = "column_count")
    private Integer columnCount;

    @Column(name = "missing_rate")
    private Double missingRate;

    @Column(name = "repetitions")
    private Integer repetitions;

    @Column(name = "seed")
    private Long seed;

    @Column(name = "constant_value")
    private Double constantValue;

    protected AnalysisRun() {
        // Required by JPA
    }

    public AnalysisRun(
            String fileName,
            String analysisType,
            Long rowCount,
            Integer columnCount
    ) {
        this.fileName = fileName;
        this.analysisType = analysisType;
        this.createdAt = LocalDateTime.now();
        this.rowCount = rowCount;
        this.columnCount = columnCount;
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getAnalysisType() {
        return analysisType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getRowCount() {
        return rowCount;
    }

    public Integer getColumnCount() {
        return columnCount;
    }

    public Double getMissingRate() {
        return missingRate;
    }

    public Integer getRepetitions() {
        return repetitions;
    }

    public Long getSeed() {
        return seed;
    }

    public Double getConstantValue() {
        return constantValue;
    }

    public void setBenchmarkParameters(
            double missingRate,
            int repetitions,
            long seed,
            double constantValue
    ) {
        this.missingRate = missingRate;
        this.repetitions = repetitions;
        this.seed = seed;
        this.constantValue = constantValue;
    }
}
