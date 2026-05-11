package com.lsi.query;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SimilarityCalculator {

    public double cosine(double[] left, double[] right) {
        validateSameLength(left, right);

        double dot = 0.0;
        double leftNorm = 0.0;
        double rightNorm = 0.0;

        for (int i = 0; i < left.length; i++) {
            dot += left[i] * right[i];
            leftNorm += left[i] * left[i];
            rightNorm += right[i] * right[i];
        }

        if (leftNorm == 0.0 || rightNorm == 0.0) {
            return 0.0;
        }

        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }

    public double jaccard(Collection<String> leftTerms, Collection<String> rightTerms) {
        Objects.requireNonNull(leftTerms, "leftTerms cannot be null");
        Objects.requireNonNull(rightTerms, "rightTerms cannot be null");

        Set<String> left = new HashSet<>(leftTerms);
        Set<String> right = new HashSet<>(rightTerms);

        if (left.isEmpty() && right.isEmpty()) {
            return 1.0;
        }

        Set<String> intersection = new HashSet<>(left);
        intersection.retainAll(right);

        Set<String> union = new HashSet<>(left);
        union.addAll(right);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }

    private void validateSameLength(double[] left, double[] right) {
        Objects.requireNonNull(left, "left vector cannot be null");
        Objects.requireNonNull(right, "right vector cannot be null");

        if (left.length != right.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }
    }
}
