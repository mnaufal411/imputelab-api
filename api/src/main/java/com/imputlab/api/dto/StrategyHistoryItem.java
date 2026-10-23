package com.imputlab.api.dto;

public record StrategyHistoryItem(
        String strategy,
        Integer evaluatedCount,
        Double meanMae,
        Double meanRmse,
        Double rmseStdDev
        ) {

}
