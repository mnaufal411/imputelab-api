package com.imputlab.api.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.imputlab.api.dto.ColumnProfile;
import com.imputlab.api.dto.DatasetProfileResponse;
import com.imputlab.api.validation.CsvFileValidator;

@Service
public class CsvProfilingService {

    private final CsvFileValidator csvFileValidator;

    public CsvProfilingService(CsvFileValidator csvFileValidator) {
        this.csvFileValidator = csvFileValidator;
    }

    public DatasetProfileResponse profile(MultipartFile file)
            throws IOException {

        // Validasi file kosong dan ekstensi CSV.
        csvFileValidator.validate(file);

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .get();

        try (
                Reader reader = new InputStreamReader(
                        file.getInputStream(),
                        StandardCharsets.UTF_8
                ); CSVParser parser = format.parse(reader)) {
            List<String> headers = parser.getHeaderNames();

            if (headers.isEmpty()
                    || headers.stream().anyMatch(String::isBlank)) {
                throw new IllegalArgumentException(
                        "CSV must contain non-empty column names."
                );
            }

            Set<String> uniqueHeaders = new HashSet<>(headers);

            if (uniqueHeaders.size() != headers.size()) {
                throw new IllegalArgumentException(
                        "CSV contains duplicate column names."
                );
            }

            Map<String, Long> missingCounts = new LinkedHashMap<>();

            for (String header : headers) {
                missingCounts.put(header, 0L);
            }

            long rowCount = 0;

            for (CSVRecord record : parser) {
                rowCount++;

                for (String header : headers) {
                    boolean missing = !record.isSet(header)
                            || record.get(header).isBlank();

                    if (missing) {
                        missingCounts.merge(header, 1L, Long::sum);
                    }
                }
            }

            List<ColumnProfile> profiles = new ArrayList<>();

            for (String header : headers) {
                long missing = missingCounts.get(header);
                long nonMissing = rowCount - missing;

                double percentage = rowCount == 0
                        ? 0.0
                        : Math.round(
                                (missing * 10000.0) / rowCount
                        ) / 100.0;

                profiles.add(
                        new ColumnProfile(
                                header,
                                missing,
                                nonMissing,
                                percentage
                        )
                );
            }

            String fileName = file.getOriginalFilename();

            if (fileName == null || fileName.isBlank()) {
                fileName = "uploaded.csv";
            }

            return new DatasetProfileResponse(
                    fileName,
                    rowCount,
                    headers.size(),
                    profiles
            );
        }
    }
}
