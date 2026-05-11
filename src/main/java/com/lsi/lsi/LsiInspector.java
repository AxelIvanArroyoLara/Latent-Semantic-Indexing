package com.lsi.lsi;

import com.lsi.model.LatentSpaceModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LsiInspector {

    private static final int TOP_TERMS = 5;

    public static String inspect(LatentSpaceModel model) {

        validate(model);

        StringBuilder sb = new StringBuilder();

        sb.append("=========== LSI MODEL ===========\n");

        sb.append("k = ")
                .append(model.k())
                .append("\n\n");

        // =========================================
        // Singular values
        // =========================================

        sb.append("Valores singulares:\n");

        double[] sv = model.singularValues();

        double retainedVariance = 0.0;

        for (int i = 0; i < sv.length; i++) {

            sb.append(String.format(
                    "  Ïƒ%d = %.4f%n",
                    i + 1,
                    sv[i]
            ));

            retainedVariance += sv[i] * sv[i];
        }

        sb.append(String.format(
                "%nVarianza retenida: %.4f%n",
                retainedVariance
        ));

        // =========================================
        // Matrix dimensions
        // =========================================

        sb.append("\nRepresentaciones:\n");

        sb.append("  Documentos: ")
                .append(model.documentVectors().length)
                .append(" x ")
                .append(model.k())
                .append("\n");

        sb.append("  TÃ©rminos: ")
                .append(model.termVectors().length)
                .append(" x ")
                .append(model.k())
                .append("\n");

        // =========================================
        // Semantic interpretation
        // =========================================

        sb.append("\n=========== COMPONENTES ===========\n");

        for (int component = 0;
             component < model.k();
             component++) {

            sb.append("\nComponente ")
                    .append(component + 1)
                    .append(":\n");

            List<TermWeight> weights = new ArrayList<>();

            for (int termIndex = 0;
                 termIndex < model.terms().size();
                 termIndex++) {

                String term = model.terms().get(termIndex);

                double weight = model.termVectors()[termIndex][component];

                weights.add(new TermWeight(term, weight));
            }

            weights.sort(
                    Comparator.comparingDouble(
                            (TermWeight tw) -> Math.abs(tw.weight())
                    ).reversed()
            );

            int limit = Math.min(TOP_TERMS, weights.size());

            for (int i = 0; i < limit; i++) {

                TermWeight tw = weights.get(i);

                sb.append(String.format(
                        "  - %-20s % .4f (abs % .4f)%n",
                        tw.term(),
                        tw.weight(),
                        Math.abs(tw.weight())
                ));
            }
        }

        return sb.toString();
    }

    // =============================================
    // Validation
    // =============================================

    private static void validate(
            LatentSpaceModel model
    ) {

        if (model == null) {
            throw new IllegalArgumentException(
                    "Model cannot be null"
            );
        }

        if (model.k() <= 0) {
            throw new IllegalArgumentException(
                    "Invalid k"
            );
        }

        if (model.singularValues() == null) {
            throw new IllegalArgumentException(
                    "Singular values missing"
            );
        }

        if (model.termVectors() == null) {
            throw new IllegalArgumentException(
                    "Term vectors missing"
            );
        }

        if (model.terms() == null) {
            throw new IllegalArgumentException(
                    "Terms metadata missing"
            );
        }
    }

    // =============================================
    // Helper record
    // =============================================

    private record TermWeight(
            String term,
            double weight
    ) {}
}

