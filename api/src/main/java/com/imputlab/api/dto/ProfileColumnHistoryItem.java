package com.imputlab.api.dto;

public record ProfileColumnHistoryItem(
        String columnName,
        Long missingCount,
        Long nonMissingCount,
        Double missingPercentage
) {
}
