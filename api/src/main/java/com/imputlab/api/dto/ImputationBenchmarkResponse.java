package com.imputlab.api.dto;

import java.util.List;

public record ImputationBenchmarkResponse(
        String fileName,
        long rowCount,
        double missingRate,
        int repetitions,
        long seed,
        double constantValue,
        List<ColumnBenchmarkResult> columns,
        List<String> skippedColumns
) {
}
