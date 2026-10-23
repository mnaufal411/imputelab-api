package com.imputlab.api.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.imputlab.api.dto.ColumnBenchmarkResult;
import com.imputlab.api.dto.ImputationBenchmarkResponse;
import com.imputlab.api.dto.StrategyBenchmarkResult;
import com.imputlab.api.validation.CsvFileValidator;

@Service
public class ImputationBenchmarkService {

    private static final List<String> STRATEGIES
            = List.of("MEAN", "MEDIAN", "CONSTANT");
    private final CsvFileValidator csvFileValidator;

    public ImputationBenchmarkService(CsvFileValidator csvFileValidator) {
        this.csvFileValidator = csvFileValidator;
    }

    public ImputationBenchmarkResponse benchmark(
            MultipartFile file,
            double missingRate,
            int repetitions,
            long seed,
            double constantValue
    ) throws IOException {

        validateParameters(
                file, missingRate, repetitions, constantValue
        );

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .get();

        List<ColumnBenchmarkResult> results = new ArrayList<>();
        List<String> skippedColumns = new ArrayList<>();
        long rowCount = 0;

        try (
                Reader reader = new InputStreamReader(
                        file.getInputStream(),
                        StandardCharsets.UTF_8
                ); CSVParser parser = format.parse(reader)) {
            List<String> headers = parser.getHeaderNames();

            validateHeaders(headers);

            Map<String, List<Double>> numericValues
                    = new LinkedHashMap<>();

            Map<String, Boolean> numericColumns
                    = new LinkedHashMap<>();

            for (String header : headers) {
                numericValues.put(header, new ArrayList<>());
                numericColumns.put(header, true);
            }

            for (CSVRecord record : parser) {
                rowCount++;
                if (record.size() != headers.size()) {
                    throw new IllegalArgumentException(
                            "CSV row " + record.getRecordNumber()
                            + " has an inconsistent column count."
                    );
                }

                for (int i = 0; i < headers.size(); i++) {
                    String header = headers.get(i);
                    String rawValue = record.get(i).trim();

                    // Empty cells are treated as missing values.
                    if (rawValue.isEmpty()) {
                        continue;
                    }

                    try {
                        double value = Double.parseDouble(rawValue);

                        if (!Double.isFinite(value)) {
                            numericColumns.put(header, false);
                            continue;
                        }

                        numericValues.get(header).add(value);

                    } catch (NumberFormatException exception) {
                        // The entire column will be skipped.
                        numericColumns.put(header, false);
                    }
                }
            }

            for (String header : headers) {
                if (!numericColumns.get(header)) {
                    skippedColumns.add(
                            header
                            + ": contains non-numeric or "
                            + "non-finite values."
                    );
                    continue;
                }

                List<Double> values = numericValues.get(header);

                if (values.size() < 2) {
                    skippedColumns.add(
                            header
                            + ": at least two observed "
                            + "numeric values are required."
                    );
                    continue;
                }

                ColumnBenchmarkResult result = benchmarkColumn(
                        header,
                        values,
                        missingRate,
                        repetitions,
                        seed,
                        constantValue
                );

                results.add(result);
            }
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            fileName = "uploaded.csv";
        }

        return new ImputationBenchmarkResponse(
                fileName,
                rowCount,
                missingRate,
                repetitions,
                seed,
                constantValue,
                results,
                skippedColumns
        );
    }

    private ColumnBenchmarkResult benchmarkColumn(
            String columnName,
            List<Double> values,
            double missingRate,
            int repetitions,
            long seed,
            double constantValue
    ) {
        int observedCount = values.size();

        int maskingCount = Math.max(
                1,
                (int) Math.round(observedCount * missingRate)
        );

        // Keep at least one observed value for imputation.
        maskingCount = Math.min(maskingCount, observedCount - 1);

        List<List<Double>> maeResults = new ArrayList<>();
        List<List<Double>> rmseResults = new ArrayList<>();

        for (int i = 0; i < STRATEGIES.size(); i++) {
            maeResults.add(new ArrayList<>());
            rmseResults.add(new ArrayList<>());
        }

        for (int repetition = 0;
                repetition < repetitions;
                repetition++) {

            List<Integer> indices = new ArrayList<>();

            for (int i = 0; i < observedCount; i++) {
                indices.add(i);
            }

            long splitSeed = seed
                    + 31L * columnName.hashCode()
                    + 1_000_003L * repetition;

            Collections.shuffle(indices, new Random(splitSeed));

            Set<Integer> hiddenIndices = new HashSet<>(
                    indices.subList(0, maskingCount)
            );

            List<Double> trainingValues = new ArrayList<>();

            for (int i = 0; i < observedCount; i++) {
                if (!hiddenIndices.contains(i)) {
                    trainingValues.add(values.get(i));
                }
            }

            // Calculate statistics only from visible values.
            double mean = calculateMean(trainingValues);
            double median = calculateMedian(trainingValues);

            double[] predictions = {
                mean,
                median,
                constantValue
            };

            List<Double> hiddenActualValues = new ArrayList<>();

            for (int i = 0; i < maskingCount; i++) {
                int hiddenIndex = indices.get(i);
                hiddenActualValues.add(values.get(hiddenIndex));
            }

            for (int strategy = 0;
                    strategy < STRATEGIES.size();
                    strategy++) {

                double prediction = predictions[strategy];

                double mae = ImputationMetrics.calculateMae(
                        hiddenActualValues,
                        prediction
                );

                double rmse = ImputationMetrics.calculateRmse(
                        hiddenActualValues,
                        prediction
                );

                maeResults.get(strategy).add(mae);
                rmseResults.get(strategy).add(rmse);
            }

        }

        List<StrategyBenchmarkResult> strategies
                = new ArrayList<>();

        for (int i = 0; i < STRATEGIES.size(); i++) {
            List<Double> maes = maeResults.get(i);
            List<Double> rmses = rmseResults.get(i);

            double meanMae = calculateMean(maes);
            double meanRmse = calculateMean(rmses);
            double rmseStdDev = calculatePopulationStdDev(
                    rmses, meanRmse
            );

            strategies.add(
                    new StrategyBenchmarkResult(
                            STRATEGIES.get(i),
                            maskingCount * repetitions,
                            meanMae,
                            meanRmse,
                            rmseStdDev
                    )
            );
        }

        return new ColumnBenchmarkResult(
                columnName,
                observedCount,
                strategies
        );
    }

    private double calculateMean(List<Double> values) {
        double sum = 0.0;

        for (double value : values) {
            sum += value;
        }

        return sum / values.size();
    }

    private double calculateMedian(List<Double> values) {
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);

        int middle = sorted.size() / 2;

        if (sorted.size() % 2 == 1) {
            return sorted.get(middle);
        }

        return sorted.get(middle - 1) / 2.0
                + sorted.get(middle) / 2.0;
    }

    private double calculatePopulationStdDev(
            List<Double> values,
            double mean
    ) {
        double squaredDifferences = 0.0;

        for (double value : values) {
            double difference = value - mean;
            squaredDifferences += difference * difference;
        }

        return Math.sqrt(squaredDifferences / values.size());
    }

    private void validateParameters(
            MultipartFile file,
            double missingRate,
            int repetitions,
            double constantValue
    ) {
        csvFileValidator.validate(file);

        if (!Double.isFinite(missingRate)
                || missingRate <= 0.0
                || missingRate >= 1.0) {
            throw new IllegalArgumentException(
                    "missingRate must be greater than 0 and less than 1."
            );
        }

        if (repetitions < 2 || repetitions > 20) {
            throw new IllegalArgumentException(
                    "repetitions must be between 2 and 20."
            );
        }

        if (!Double.isFinite(constantValue)) {
            throw new IllegalArgumentException(
                    "constantValue must be finite."
            );
        }
    }

    private void validateHeaders(List<String> headers) {
        if (headers.isEmpty()
                || headers.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException(
                    "CSV must contain non-empty column names."
            );
        }

        if (new HashSet<>(headers).size() != headers.size()) {
            throw new IllegalArgumentException(
                    "CSV contains duplicate column names."
            );
        }
    }
}
