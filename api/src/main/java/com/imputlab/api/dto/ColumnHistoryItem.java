package com.imputlab.api.dto;

import java.util.List;

public record ColumnHistoryItem(
        String columnName,
        Integer observedCount,
        List<StrategyHistoryItem> strategies
        ) {

}
