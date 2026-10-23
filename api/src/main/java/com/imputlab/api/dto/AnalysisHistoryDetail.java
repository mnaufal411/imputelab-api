package com.imputlab.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AnalysisHistoryDetail(
        Long id,
        String fileName,
        String analysisType,
        LocalDateTime createdAt,
        Long rowCount,
        Integer columnCount,
        Double missingRate,
        Integer repetitions,
        Long seed,
        Double constantValue,
        List<ColumnHistoryItem> columns,
        List<ProfileColumnHistoryItem> profileColumns
) {
}
