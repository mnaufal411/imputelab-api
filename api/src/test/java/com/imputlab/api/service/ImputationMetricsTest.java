package com.imputlab.api.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class ImputationMetricsTest {

    @Test
    void shouldCalculateMaeCorrectly() {
        double mae = ImputationMetrics.calculateMae(
                List.of(10.0, 20.0),
                15.0
        );

        assertEquals(5.0, mae, 1e-9);
    }

    @Test
    void shouldCalculateRmseCorrectly() {
        double rmse = ImputationMetrics.calculateRmse(
                List.of(10.0, 20.0),
                15.0
        );

        assertEquals(5.0, rmse, 1e-9);
    }

    @Test
    void shouldDistinguishMaeFromRmseForUnequalErrors() {
        List<Double> actual = List.of(0.0, 10.0);

        double mae = ImputationMetrics.calculateMae(actual, 0.0);
        double rmse = ImputationMetrics.calculateRmse(actual, 0.0);

        assertEquals(5.0, mae, 1e-9);
        assertEquals(Math.sqrt(50.0), rmse, 1e-9);
    }

    @Test
    void shouldRejectEmptyActualValues() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ImputationMetrics.calculateMae(List.of(), 0.0)
        );
    }

    @Test
    void shouldRejectNonFinitePrediction() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ImputationMetrics.calculateRmse(
                        List.of(10.0),
                        Double.NaN
                )
        );
    }
}
