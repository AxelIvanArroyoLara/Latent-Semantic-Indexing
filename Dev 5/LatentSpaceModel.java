package com.equipo.documentbase.domain;

import java.io.Serializable;
import java.util.List;

public record LatentSpaceModel(

        int k,

        // [documentIndex][latentDimension]
        double[][] documentVectors,

        // [termIndex][latentDimension]
        double[][] termVectors,

        double[] singularValues,

        // Metadata útil para inspección y consultas
        List<String> documentCodes,

        List<String> terms

) implements Serializable {
                private static final long serialVersionUID = 1L;

        public LatentSpaceModel {

                if (k <= 0) {
                        throw new IllegalArgumentException("k must be positive");
                }

                if (documentVectors == null) {
                        throw new IllegalArgumentException("documentVectors cannot be null");
                }

                if (termVectors == null) {
                        throw new IllegalArgumentException("termVectors cannot be null");
                }

                if (singularValues == null) {
                        throw new IllegalArgumentException("singularValues cannot be null");
                }

                if (documentCodes == null) {
                        throw new IllegalArgumentException("documentCodes cannot be null");
                }

                if (terms == null) {
                        throw new IllegalArgumentException("terms cannot be null");
                }

                if (singularValues.length != k) {
                        throw new IllegalArgumentException(
                                        "singularValues length must equal k"
                        );
                }

                // Validate documentVectors: docs x k
                int docCount = documentVectors.length;

                if (docCount != documentCodes.size()) {
                        throw new IllegalArgumentException(
                                        "documentVectors rows must match documentCodes size"
                        );
                }

                for (int i = 0; i < documentVectors.length; i++) {
                        double[] row = documentVectors[i];

                        if (row == null) {
                                throw new IllegalArgumentException(
                                                "documentVectors row " + i + " is null"
                                );
                        }

                        if (row.length != k) {
                                throw new IllegalArgumentException(
                                                "Each document vector must have length k"
                                );
                        }
                }

                // Validate termVectors: terms x k
                int termCount = termVectors.length;

                if (termCount != terms.size()) {
                        throw new IllegalArgumentException(
                                        "termVectors rows must match terms size"
                        );
                }

                for (int i = 0; i < termVectors.length; i++) {
                        double[] row = termVectors[i];

                        if (row == null) {
                                throw new IllegalArgumentException(
                                                "termVectors row " + i + " is null"
                                );
                        }

                        if (row.length != k) {
                                throw new IllegalArgumentException(
                                                "Each term vector must have length k"
                                );
                        }
                }

                // Defensive copies
                documentVectors = deepCopyMatrix(documentVectors);
                termVectors = deepCopyMatrix(termVectors);
                singularValues = singularValues.clone();
                documentCodes = List.copyOf(documentCodes);
                terms = List.copyOf(terms);
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

