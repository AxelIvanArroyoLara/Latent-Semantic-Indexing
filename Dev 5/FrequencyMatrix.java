package com.equipo.documentbase.domain;

import java.util.List;

public record FrequencyMatrix(
                List<String> terms,
                List<String> documentCodes,
                double[][] values
) {

        public FrequencyMatrix {

                if (terms == null) {
                        throw new IllegalArgumentException("terms cannot be null");
                }

                if (documentCodes == null) {
                        throw new IllegalArgumentException("documentCodes cannot be null");
                }

                if (values == null) {
                        throw new IllegalArgumentException("values cannot be null");
                }

                int termCount = values.length;

                if (termCount != terms.size()) {
                        throw new IllegalArgumentException(
                                        "Number of rows in values must match terms size"
                        );
                }

                int docCount = documentCodes.size();

                for (int i = 0; i < values.length; i++) {

                        double[] row = values[i];

                        if (row == null) {
                                throw new IllegalArgumentException(
                                                "values row " + i + " cannot be null"
                                );
                        }

                        if (row.length != docCount) {
                                throw new IllegalArgumentException(
                                                "Each row in values must have length equal to documentCodes size"
                                );
                        }
                }

                // Defensive copies
                values = deepCopyMatrix(values);
                terms = List.copyOf(terms);
                documentCodes = List.copyOf(documentCodes);
        }

        private static double[][] deepCopyMatrix(double[][] src) {
                double[][] copy = new double[src.length][];

                for (int i = 0; i < src.length; i++) {
                        double[] row = src[i];

                        if (row == null) {
                                copy[i] = null;
                        } else {
                                copy[i] = row.clone();
                        }
                }

                return copy;
        }
}