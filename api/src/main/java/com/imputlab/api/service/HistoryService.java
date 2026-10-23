package com.imputlab.api.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.imputlab.api.dto.AnalysisHistoryDetail;
import com.imputlab.api.dto.AnalysisHistoryItem;
import com.imputlab.api.dto.ColumnHistoryItem;
import com.imputlab.api.dto.ProfileColumnHistoryItem;
import com.imputlab.api.dto.StrategyHistoryItem;
import com.imputlab.api.entity.AnalysisRun;
import com.imputlab.api.repository.AnalysisRunRepository;
import com.imputlab.api.repository.ColumnResultRepository;
import com.imputlab.api.repository.ProfileColumnResultRepository;
import com.imputlab.api.repository.StrategyResultRepository;

@Service
public class HistoryService {

    private final AnalysisRunRepository analysisRunRepository;
    private final ColumnResultRepository columnResultRepository;
    private final StrategyResultRepository strategyResultRepository;
    private final ProfileColumnResultRepository profileColumnResultRepository;

    public HistoryService(
            AnalysisRunRepository analysisRunRepository,
            ColumnResultRepository columnResultRepository,
            StrategyResultRepository strategyResultRepository,
            ProfileColumnResultRepository profileColumnResultRepository
    ) {
        this.analysisRunRepository = analysisRunRepository;
        this.columnResultRepository = columnResultRepository;
        this.strategyResultRepository = strategyResultRepository;
        this.profileColumnResultRepository = profileColumnResultRepository;
    }

    @Transactional(readOnly = true)
    public List<AnalysisHistoryItem> getAllHistory() {
        return analysisRunRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(run -> new AnalysisHistoryItem(
                        run.getId(),
                        run.getFileName(),
                        run.getAnalysisType(),
                        run.getCreatedAt(),
                        run.getRowCount(),
                        run.getColumnCount(),
                        run.getMissingRate(),
                        run.getRepetitions(),
                        run.getSeed(),
                        run.getConstantValue()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public AnalysisHistoryDetail getHistoryById(Long id) {
        AnalysisRun run = analysisRunRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Analysis history not found: " + id
                ));

        List<ColumnHistoryItem> columns
                = columnResultRepository
                        .findAllByAnalysisRun_IdOrderByIdAsc(id)
                        .stream()
                        .map(column -> {
                            List<StrategyHistoryItem> strategies
                                    = strategyResultRepository
                                            .findAllByColumnResult_IdOrderByIdAsc(
                                                    column.getId()
                                            )
                                            .stream()
                                            .map(strategy -> new StrategyHistoryItem(
                                                    strategy.getStrategy(),
                                                    strategy.getEvaluatedCount(),
                                                    strategy.getMeanMae(),
                                                    strategy.getMeanRmse(),
                                                    strategy.getRmseStdDev()
                                            ))
                                            .toList();

                            return new ColumnHistoryItem(
                                    column.getColumnName(),
                                    column.getObservedCount(),
                                    strategies
                            );
                        })
                        .toList();

        List<ProfileColumnHistoryItem> profileColumns
                = profileColumnResultRepository
                        .findAllByAnalysisRun_IdOrderByIdAsc(id)
                        .stream()
                        .map(column -> new ProfileColumnHistoryItem(
                                column.getColumnName(),
                                column.getMissingCount(),
                                column.getNonMissingCount(),
                                column.getMissingPercentage()
                        ))
                        .toList();

        return new AnalysisHistoryDetail(
                run.getId(),
                run.getFileName(),
                run.getAnalysisType(),
                run.getCreatedAt(),
                run.getRowCount(),
                run.getColumnCount(),
                run.getMissingRate(),
                run.getRepetitions(),
                run.getSeed(),
                run.getConstantValue(),
                columns,
                profileColumns
        );
    }

    @Transactional
    public void deleteHistory(Long id) {
        AnalysisRun run = analysisRunRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Analysis history not found: " + id
                ));

        var columns
                = columnResultRepository.findAllByAnalysisRun_IdOrderByIdAsc(id);

        for (var column : columns) {
            strategyResultRepository
                    .deleteAll(
                            strategyResultRepository
                                    .findAllByColumnResult_IdOrderByIdAsc(
                                            column.getId()
                                    )
                    );
        }

        columnResultRepository.deleteAll(columns);

        profileColumnResultRepository.deleteAll(
                profileColumnResultRepository
                        .findAllByAnalysisRun_IdOrderByIdAsc(id)
        );

        analysisRunRepository.delete(run);
    }
}
