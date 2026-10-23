package com.imputlab.api.service;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.imputlab.api.dto.ImputationBenchmarkResponse;
import com.imputlab.api.dto.StrategyBenchmarkResult;
import com.imputlab.api.validation.CsvFileValidator;

class ImputationBenchmarkServiceTest {

    private final ImputationBenchmarkService service
            = new ImputationBenchmarkService(new CsvFileValidator());

    ;

    private MockMultipartFile createCsv(String content) {
        return new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Test
    void shouldReturnZeroErrorWhenAllValuesAreIdenticalAndConstantMatches() throws Exception {
        String csv = """
            value
            10
            10
            10
            10
            10
            """;

        ImputationBenchmarkResponse result = service.benchmark(
                createCsv(csv),
                0.4,
                3,
                42L,
                10.0
        );

        var column = result.columns().get(0);
        var constantStrategy = column.strategies().stream()
                .filter(strategy -> strategy.strategy().equals("CONSTANT"))
                .findFirst()
                .orElseThrow();

        assertEquals(0.0, constantStrategy.meanMae(), 1e-9);
        assertEquals(0.0, constantStrategy.meanRmse(), 1e-9);
        assertEquals(0.0, constantStrategy.rmseStdDev(), 1e-9);
    }

    @Test
    void shouldProducePositiveErrorWhenConstantDoesNotMatchActualValues() throws Exception {
        String csv = """
            value
            10
            10
            10
            10
            10
            """;

        ImputationBenchmarkResponse result = service.benchmark(
                createCsv(csv),
                0.4,
                3,
                42L,
                0.0
        );

        var column = result.columns().get(0);
        var constantStrategy = column.strategies().stream()
                .filter(strategy -> strategy.strategy().equals("CONSTANT"))
                .findFirst()
                .orElseThrow();

        assertEquals(10.0, constantStrategy.meanMae(), 1e-9);
        assertEquals(10.0, constantStrategy.meanRmse(), 1e-9);
        assertEquals(0.0, constantStrategy.rmseStdDev(), 1e-9);
    }

    private String sampleCsv() {
        return """
                temperature,humidity
                20,60
                22,65
                24,70
                26,75
                28,80
                30,85
                32,90
                34,95
                36,100
                38,105
                """;
    }

    @Test
    void shouldBenchmarkAllNumericColumns() throws Exception {
        MockMultipartFile file = createCsv(sampleCsv());

        ImputationBenchmarkResponse result = service.benchmark(
                file, 0.2, 3, 42L, 0.0
        );

        assertEquals(2, result.columns().size());
        assertTrue(result.skippedColumns().isEmpty());

        for (var column : result.columns()) {
            assertEquals(10, column.observedCount());
            assertEquals(3, column.strategies().size());

            List<String> expectedStrategies
                    = List.of("MEAN", "MEDIAN", "CONSTANT");

            assertEquals(
                    expectedStrategies,
                    column.strategies().stream()
                            .map(StrategyBenchmarkResult::strategy)
                            .toList()
            );

            for (var strategy : column.strategies()) {
                assertEquals(6, strategy.evaluatedCount());

                assertTrue(Double.isFinite(strategy.meanMae()));
                assertTrue(Double.isFinite(strategy.meanRmse()));
                assertTrue(Double.isFinite(strategy.rmseStdDev()));

                assertTrue(strategy.meanMae() >= 0);
                assertTrue(strategy.meanRmse() >= 0);
                assertTrue(strategy.rmseStdDev() >= 0);
            }
        }
    }

    @Test
    void shouldProduceSameResultsForSameSeed() throws Exception {
        ImputationBenchmarkResponse first = service.benchmark(
                createCsv(sampleCsv()), 0.2, 5, 42L, 0.0
        );

        ImputationBenchmarkResponse second = service.benchmark(
                createCsv(sampleCsv()), 0.2, 5, 42L, 0.0
        );

        assertEquals(first, second);
    }

    @Test
    void shouldRejectInvalidMissingRate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.benchmark(
                        createCsv(sampleCsv()),
                        1.0,
                        3,
                        42L,
                        0.0
                )
        );
    }

    @Test
    void shouldSkipCategoricalColumns() throws Exception {
        String csv = """
                temperature,label
                20,low
                22,medium
                24,high
                26,low
                """;

        ImputationBenchmarkResponse result = service.benchmark(
                createCsv(csv), 0.25, 3, 42L, 0.0
        );

        assertEquals(1, result.columns().size());
        assertEquals("temperature", result.columns().get(0).columnName());

        assertEquals(1, result.skippedColumns().size());
        assertTrue(result.skippedColumns().get(0).startsWith("label:"));
    }

    @Test
    void shouldReturnZeroErrorForMeanAndMedianWhenAllValuesAreIdentical()
            throws Exception {

        String csv = """
            value
            10
            10
            10
            10
            10
            """;

        ImputationBenchmarkResponse result = service.benchmark(
                createCsv(csv),
                0.4,
                3,
                42L,
                0.0
        );

        var column = result.columns().get(0);

        var meanStrategy = column.strategies().stream()
                .filter(strategy -> strategy.strategy().equals("MEAN"))
                .findFirst()
                .orElseThrow();

        var medianStrategy = column.strategies().stream()
                .filter(strategy -> strategy.strategy().equals("MEDIAN"))
                .findFirst()
                .orElseThrow();

        assertEquals(0.0, meanStrategy.meanMae(), 1e-9);
        assertEquals(0.0, meanStrategy.meanRmse(), 1e-9);

        assertEquals(0.0, medianStrategy.meanMae(), 1e-9);
        assertEquals(0.0, medianStrategy.meanRmse(), 1e-9);
    }
}
