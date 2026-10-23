package com.imputlab.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imputlab.api.dto.DatasetProfileResponse;
import com.imputlab.api.entity.AnalysisRun;
import com.imputlab.api.entity.ProfileColumnResult;
import com.imputlab.api.repository.AnalysisRunRepository;
import com.imputlab.api.repository.ProfileColumnResultRepository;

@Service
public class AnalysisHistoryService {

    private final AnalysisRunRepository repository;
    private final ProfileColumnResultRepository profileColumnResultRepository;

    public AnalysisHistoryService(
            AnalysisRunRepository repository,
            ProfileColumnResultRepository profileColumnResultRepository
    ) {
        this.repository = repository;
        this.profileColumnResultRepository = profileColumnResultRepository;
    }

    @Transactional
    public AnalysisRun saveProfile(
            String fileName,
            DatasetProfileResponse response
    ) {
        AnalysisRun run = new AnalysisRun(
                fileName,
                "PROFILE",
                response.rowCount(),
                response.columnCount()
        );

        AnalysisRun savedRun = repository.save(run);

        List<ProfileColumnResult> profileColumns = response.columns()
                .stream()
                .map(column -> new ProfileColumnResult(
                        savedRun,
                        column.columnName(),
                        column.missingCount(),
                        column.nonMissingCount(),
                        column.missingPercentage()
                ))
                .toList();

        profileColumnResultRepository.saveAll(profileColumns);

        return savedRun;
    }
}
