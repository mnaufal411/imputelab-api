package com.imputlab.api.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.imputlab.api.dto.DatasetProfileResponse;
import com.imputlab.api.dto.ImputationBenchmarkResponse;
import com.imputlab.api.service.AnalysisHistoryService;
import com.imputlab.api.service.BenchmarkHistoryService;
import com.imputlab.api.service.CsvProfilingService;
import com.imputlab.api.service.ImputationBenchmarkService;

@RestController
@RequestMapping("/api/datasets")
public class DatasetController {

    private final CsvProfilingService csvProfilingService;
    private final ImputationBenchmarkService benchmarkService;
    private final AnalysisHistoryService analysisHistoryService;
    private final BenchmarkHistoryService benchmarkHistoryService;

    public DatasetController(
            AnalysisHistoryService analysisHistoryService,
            CsvProfilingService csvProfilingService,
            ImputationBenchmarkService benchmarkService,
            BenchmarkHistoryService benchmarkHistoryService
    ) {
        this.csvProfilingService = csvProfilingService;
        this.benchmarkService = benchmarkService;
        this.analysisHistoryService = analysisHistoryService;
        this.benchmarkHistoryService = benchmarkHistoryService;
    }

    @PostMapping(
            value = "/profile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public DatasetProfileResponse profile(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        DatasetProfileResponse response
                = csvProfilingService.profile(file);

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            fileName = "uploaded.csv";
        }

        analysisHistoryService.saveProfile(fileName, response);

        return response;
    }

    @PostMapping(
            value = "/benchmark",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ImputationBenchmarkResponse benchmark(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "0.15") double missingRate,
            @RequestParam(defaultValue = "5") int repetitions,
            @RequestParam(defaultValue = "42") long seed,
            @RequestParam(defaultValue = "0.0") double constantValue
    ) throws IOException {

        ImputationBenchmarkResponse response = benchmarkService.benchmark(
                file,
                missingRate,
                repetitions,
                seed,
                constantValue
        );

        benchmarkHistoryService.saveBenchmark(response);

        return response;
    }
}
