package com.lsi.indexing;

import com.lsi.model.SemanticDocument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds a sorted global vocabulary from semantic documents.
 */
public class VocabularyBuilder {

    public List<String> build(List<SemanticDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return List.of();
        }

        Set<String> vocabulary = new LinkedHashSet<>();

        for (SemanticDocument document : documents) {
            if (document != null && document.canonicalTerms() != null) {
                vocabulary.addAll(document.canonicalTerms());
            }
        }

        List<String> terms = new ArrayList<>(vocabulary);
        Collections.sort(terms);

        return terms;
    }
}

