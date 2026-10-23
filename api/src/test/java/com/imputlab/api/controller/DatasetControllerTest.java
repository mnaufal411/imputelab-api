package com.imputlab.api.controller;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.imputlab.api.service.AnalysisHistoryService;
import com.imputlab.api.service.BenchmarkHistoryService;
import com.imputlab.api.service.CsvProfilingService;
import com.imputlab.api.service.ImputationBenchmarkService;

@WebMvcTest(DatasetController.class)
class DatasetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CsvProfilingService csvProfilingService;

    @MockitoBean
    private ImputationBenchmarkService benchmarkService;

    @MockitoBean
    private AnalysisHistoryService analysisHistoryService;

    @MockitoBean
    private BenchmarkHistoryService benchmarkHistoryService;

    @Test
    void shouldRejectMissingRateAboveOne() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.csv",
                "text/csv",
                "age\n20\n25\n30\n".getBytes()
        );

        doThrow(new IllegalArgumentException(
                "missingRate must be greater than 0 and less than 1."
        )).when(benchmarkService).benchmark(
                any(),
                eq(1.5),
                anyInt(),
                anyLong(),
                anyDouble()
        );

        mockMvc.perform(multipart("/api/datasets/benchmark")
                .file(file)
                .param("missingRate", "1.5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectZeroRepetitions() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.csv",
                "text/csv",
                "age\n20\n25\n30\n".getBytes()
        );

        doThrow(new IllegalArgumentException(
                "repetitions must be between 2 and 20."
        )).when(benchmarkService).benchmark(
                any(),
                anyDouble(),
                eq(0),
                anyLong(),
                anyDouble()
        );

        mockMvc.perform(multipart("/api/datasets/benchmark")
                .file(file)
                .param("repetitions", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectNonNumericMissingRate() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.csv",
                "text/csv",
                "age\n20\n25\n30\n".getBytes()
        );

        mockMvc.perform(multipart("/api/datasets/benchmark")
                .file(file)
                .param("missingRate", "abc"))
                .andExpect(status().isBadRequest());
    }
}
