package com.imputlab.api.dto;

import java.time.LocalDateTime;

public record AnalysisHistoryItem(
        Long id,
        String fileName,
        String analysisType,
        LocalDateTime createdAt,
        Long rowCount,
        Integer columnCount,
        Double missingRate,
        Integer repetitions,
        Long seed,
        Double constantValue
        ) {

}
