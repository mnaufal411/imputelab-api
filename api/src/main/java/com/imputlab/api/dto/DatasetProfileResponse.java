package com.imputlab.api.dto;

import java.util.List;

public record DatasetProfileResponse(
        String fileName,
        long rowCount,
        int columnCount,
        List<ColumnProfile> columns
) {
}