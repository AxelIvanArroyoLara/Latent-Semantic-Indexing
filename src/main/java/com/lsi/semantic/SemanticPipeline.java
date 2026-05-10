package com.lsi.semantic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Main semantic pipeline for Dev 4.
 *
 * The order is:
 * 1. Synonym canonicalization.
 * 2. Controlled polysemy resolution.
 *
 * This module receives already preprocessed tokens.
 * It does not read raw documents, does not write to database,
 * and does not calculate frequencies or SVD.
 */
public class SemanticPipeline {

    private final SynonymExpander synonymExpander;
    private final PolysemyResolver polysemyResolver;

    public SemanticPipeline() {
        this(new SynonymExpander(), new PolysemyResolver());
    }

    public SemanticPipeline(SynonymExpander synonymExpander, PolysemyResolver polysemyResolver) {
        this.synonymExpander = Objects.requireNonNull(synonymExpander, "synonymExpander cannot be null");
        this.polysemyResolver = Objects.requireNonNull(polysemyResolver, "polysemyResolver cannot be null");
    }

    public List<String> process(List<String> tokens) {
        Objects.requireNonNull(tokens, "tokens cannot be null");

        List<String> canonicalTerms = synonymExpander.expand(tokens);
        return polysemyResolver.resolve(canonicalTerms);
    }

    public List<String> processTextToTerms(String text) {
        return process(tokenizeText(text));
    }

    public String processText(String text) {
        return String.join(" ", processTextToTerms(text));
    }

    private List<String> tokenizeText(String text) {
        List<String> tokens = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return tokens;
        }

        String[] parts = text.trim().split("\\s+");

        for (String part : parts) {
            if (!part.isBlank()) {
                tokens.add(part);
            }
        }

        return tokens;
    }
}
