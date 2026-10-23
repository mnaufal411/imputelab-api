package com.imputlab.api.dto;

public record ColumnProfile(
    String columnName,
    long missingCount,
    long nonMissingCount,
    double missingPercentage
) {}