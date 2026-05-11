package com.equipo.documentbase.indexing;

import com.equipo.documentbase.domain.FrequencyMatrix;
import com.equipo.documentbase.domain.SemanticDocument;

import java.util.*;

public class FrequencyMatrixBuilder {

    public FrequencyMatrix build(List<SemanticDocument> docs) {
        return build(docs, true);
    }

    public FrequencyMatrix build(
            List<SemanticDocument> docs,
            boolean useTfIdf
    ) {

        validateDocuments(docs);

        // =========================================
        // 1. Construir vocabulario global
        // =========================================

        Set<String> vocabSet = new LinkedHashSet<>();

        for (SemanticDocument doc : docs) {

            vocabSet.addAll(doc.canonicalTerms());
        }

        List<String> terms =
                new ArrayList<>(vocabSet);

        Collections.sort(terms);

        // =========================================
        // 2. Códigos de documentos
        // =========================================

        List<String> documentCodes =
                new ArrayList<>();

        for (SemanticDocument doc : docs) {

            documentCodes.add(doc.code());
        }

        // =========================================
        // 3. Matriz término-documento
        // rows = terms
        // cols = documents
        // =========================================

        int termCount = terms.size();
        int docCount = docs.size();

        double[][] values =
                new double[termCount][docCount];

        // =========================================
        // 4. Índice término -> fila
        // =========================================

        Map<String, Integer> termIndex =
                new HashMap<>();

        for (int i = 0; i < terms.size(); i++) {

            termIndex.put(
                    terms.get(i),
                    i
            );
        }

        // =========================================
        // 5. Frecuencias brutas (TF)
        // =========================================

        for (int docIndex = 0;
             docIndex < docs.size();
             docIndex++) {

            SemanticDocument doc =
                    docs.get(docIndex);

            Map<String, Integer> counts =
                    new HashMap<>();

            for (String term : doc.canonicalTerms()) {

                counts.put(
                        term,
                        counts.getOrDefault(term, 0) + 1
                );
            }

            for (Map.Entry<String, Integer> entry
                    : counts.entrySet()) {

                Integer row =
                        termIndex.get(entry.getKey());

                if (row != null) {

                    values[row][docIndex] =
                            entry.getValue();
                }
            }
        }

        // =========================================
        // 6. TF-IDF
        // =========================================

        if (useTfIdf) {

            applyTfIdf(
                    values,
                    termCount,
                    docCount
            );
        }

        // =========================================
        // 7. Construir record
        // =========================================

        return new FrequencyMatrix(
                terms,
                documentCodes,
                values
        );
    }

    // =====================================================
    // TF-IDF
    // =====================================================

    private void applyTfIdf(
            double[][] values,
            int termCount,
            int docCount
    ) {

        int[] documentFrequency =
                new int[termCount];

        // =========================================
        // DF(t)
        // Número de documentos que contienen t
        // =========================================

        for (int term = 0;
             term < termCount;
             term++) {

            for (int doc = 0;
                 doc < docCount;
                 doc++) {

                if (values[term][doc] > 0) {

                    documentFrequency[term]++;
                }
            }
        }

        // =========================================
        // TF-IDF
        //
        // tfidf = tf * idf
        //
        // idf = log((N + 1)/(df + 1)) + 1
        //
        // Smoothed IDF
        // =========================================

        double N = docCount;

        for (int term = 0;
             term < termCount;
             term++) {

            double idf =
                    Math.log(
                            (N + 1.0)
                                    /
                                    (documentFrequency[term] + 1.0)
                    ) + 1.0;

            for (int doc = 0;
                 doc < docCount;
                 doc++) {

                double tf =
                        values[term][doc];

                values[term][doc] =
                        tf * idf;
            }
        }
    }

    // =====================================================
    // Validation
    // =====================================================

    private void validateDocuments(
            List<SemanticDocument> docs
    ) {

        if (docs == null || docs.isEmpty()) {

            throw new IllegalArgumentException(
                    "Document list cannot be null or empty"
            );
        }

        for (SemanticDocument doc : docs) {

            if (doc == null) {

                throw new IllegalArgumentException(
                        "Document cannot be null"
                );
            }

            if (doc.canonicalTerms() == null) {

                throw new IllegalArgumentException(
                        "Document terms cannot be null"
                );
            }
        }
    }
}