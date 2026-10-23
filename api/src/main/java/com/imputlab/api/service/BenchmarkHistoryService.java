package com.imputlab.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imputlab.api.dto.ColumnBenchmarkResult;
import com.imputlab.api.dto.ImputationBenchmarkResponse;
import com.imputlab.api.dto.StrategyBenchmarkResult;
import com.imputlab.api.entity.AnalysisRun;
import com.imputlab.api.entity.ColumnResult;
import com.imputlab.api.entity.StrategyResult;
import com.imputlab.api.repository.AnalysisRunRepository;
import com.imputlab.api.repository.ColumnResultRepository;
import com.imputlab.api.repository.StrategyResultRepository;

@Service
public class BenchmarkHistoryService {

    private final AnalysisRunRepository analysisRunRepository;
    private final ColumnResultRepository columnResultRepository;
    private final StrategyResultRepository strategyResultRepository;

    public BenchmarkHistoryService(
            AnalysisRunRepository analysisRunRepository,
            ColumnResultRepository columnResultRepository,
            StrategyResultRepository strategyResultRepository
    ) {
        this.analysisRunRepository = analysisRunRepository;
        this.columnResultRepository = columnResultRepository;
        this.strategyResultRepository = strategyResultRepository;
    }

    @Transactional
    public AnalysisRun saveBenchmark(
            ImputationBenchmarkResponse response
    ) {
        int columnCount = response.columns().size()
                + response.skippedColumns().size();

        AnalysisRun run = new AnalysisRun(
                response.fileName(),
                "BENCHMARK",
                response.rowCount(),
                columnCount
        );

        run.setBenchmarkParameters(
                response.missingRate(),
                response.repetitions(),
                response.seed(),
                response.constantValue()
        );

        run = analysisRunRepository.save(run);

        for (ColumnBenchmarkResult column : response.columns()) {
            ColumnResult columnResult = new ColumnResult(
                    run,
                    column.columnName(),
                    column.observedCount()
            );

            columnResult
                    = columnResultRepository.save(columnResult);

            for (StrategyBenchmarkResult strategy
                    : column.strategies()) {

                StrategyResult strategyResult = new StrategyResult(
                        columnResult,
                        strategy.strategy(),
                        strategy.evaluatedCount(),
                        strategy.meanMae(),
                        strategy.meanRmse(),
                        strategy.rmseStdDev()
                );

                strategyResultRepository.save(strategyResult);
            }
        }

        return run;
    }
}
