package com.lsi.query;

import java.util.Objects;

public class DissimilarityCalculator {

    public double euclidean(double[] left, double[] right) {
        validateSameLength(left, right);

        double sum = 0.0;

        for (int i = 0; i < left.length; i++) {
            double difference = left[i] - right[i];
            sum += difference * difference;
        }

        return Math.sqrt(sum);
    }

    private void validateSameLength(double[] left, double[] right) {
        Objects.requireNonNull(left, "left vector cannot be null");
        Objects.requireNonNull(right, "right vector cannot be null");

        if (left.length != right.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }
    }
}

