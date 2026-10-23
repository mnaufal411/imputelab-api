package com.imputlab.api.dto;

public record StrategyBenchmarkResult(
        String strategy,
        int evaluatedCount,
        double meanMae,
        double meanRmse,
        double rmseStdDev
) {
}