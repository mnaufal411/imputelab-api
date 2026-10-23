package com.imputlab.api.service;

import java.util.List;

final class ImputationMetrics {

    private ImputationMetrics() {
    }

    static double calculateMae(
            List<Double> actualValues,
            double prediction
    ) {
        validate(actualValues, prediction);

        double absoluteErrorSum = 0.0;

        for (double actual : actualValues) {
            absoluteErrorSum += Math.abs(actual - prediction);
        }

        return absoluteErrorSum / actualValues.size();
    }

    static double calculateRmse(
            List<Double> actualValues,
            double prediction
    ) {
        validate(actualValues, prediction);

        double squaredErrorSum = 0.0;

        for (double actual : actualValues) {
            double error = actual - prediction;
            squaredErrorSum += error * error;
        }

        return Math.sqrt(squaredErrorSum / actualValues.size());
    }

    private static void validate(
            List<Double> actualValues,
            double prediction
    ) {
        if (actualValues == null || actualValues.isEmpty()) {
            throw new IllegalArgumentException(
                    "Actual values must not be empty."
            );
        }

        if (!Double.isFinite(prediction)) {
            throw new IllegalArgumentException(
                    "Prediction must be finite."
            );
        }

        for (Double actual : actualValues) {
            if (actual == null || !Double.isFinite(actual)) {
                throw new IllegalArgumentException(
                        "Actual values must be finite."
                );
            }
        }
    }
}
