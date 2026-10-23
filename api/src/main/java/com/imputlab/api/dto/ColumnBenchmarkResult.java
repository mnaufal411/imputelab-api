package com.imputlab.api.dto;

import java.util.List;

public record ColumnBenchmarkResult(
        String columnName,
        int observedCount,
        List<StrategyBenchmarkResult> strategies
) {
}