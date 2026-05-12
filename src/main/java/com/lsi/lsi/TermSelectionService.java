package com.lsi.lsi;

import com.lsi.model.LatentSpaceModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Helps an expert user identify the most significant terms in the LSI space.
 */
public class TermSelectionService {

    public List<SelectedTerm> selectTopTerms(LatentSpaceModel model, int topN) {
        validate(model, topN);

        List<SelectedTerm> selectedTerms = new ArrayList<>();

        for (int termIndex = 0; termIndex < model.terms().size(); termIndex++) {
            double significance = vectorNorm(model.termVectors()[termIndex]);

            selectedTerms.add(new SelectedTerm(
                    model.terms().get(termIndex),
                    termIndex,
                    significance
            ));
        }

        selectedTerms.sort(
                Comparator.comparingDouble(SelectedTerm::significance)
                        .reversed()
                        .thenComparing(SelectedTerm::term)
        );

        return selectedTerms.stream()
                .limit(topN)
                .toList();
    }

    public List<ComponentTerm> selectTopTermsByComponent(
            LatentSpaceModel model,
            int componentIndex,
            int topN
    ) {
        validate(model, topN);

        if (componentIndex < 0 || componentIndex >= model.k()) {
            throw new IllegalArgumentException("Component index out of range: " + componentIndex);
        }

        List<ComponentTerm> selectedTerms = new ArrayList<>();

        for (int termIndex = 0; termIndex < model.terms().size(); termIndex++) {
            double weight = model.termVectors()[termIndex][componentIndex];

            selectedTerms.add(new ComponentTerm(
                    model.terms().get(termIndex),
                    termIndex,
                    componentIndex,
                    weight,
                    Math.abs(weight)
            ));
        }

        selectedTerms.sort(
                Comparator.comparingDouble(ComponentTerm::absoluteWeight)
                        .reversed()
                        .thenComparing(ComponentTerm::term)
        );

        return selectedTerms.stream()
                .limit(topN)
                .toList();
    }

    private double vectorNorm(double[] vector) {
        double sum = 0.0;

        for (double value : vector) {
            sum += value * value;
        }

        return Math.sqrt(sum);
    }

    private void validate(LatentSpaceModel model, int topN) {
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }

        if (topN <= 0) {
            throw new IllegalArgumentException("topN must be positive");
        }
    }

    public record SelectedTerm(
            String term,
            int termIndex,
            double significance
    ) {
    }

    public record ComponentTerm(
            String term,
            int termIndex,
            int componentIndex,
            double weight,
            double absoluteWeight
    ) {
    }
}
