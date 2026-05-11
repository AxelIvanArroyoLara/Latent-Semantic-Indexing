package com.lsi.semantic;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Orchestrates semantic normalization before indexing and query ranking.
 */
public class SemanticPipeline {

    private final PolysemyResolver polysemyResolver;
    private final SynonymExpander synonymExpander;

    public SemanticPipeline() {
        this(new PolysemyResolver(), new SynonymExpander());
    }

    public SemanticPipeline(
            PolysemyResolver polysemyResolver,
            SynonymExpander synonymExpander
    ) {
        this.polysemyResolver = polysemyResolver;
        this.synonymExpander = synonymExpander;
    }

    public List<String> process(List<String> tokens) {
        List<String> resolvedTerms = polysemyResolver.resolve(tokens);
        List<String> canonicalTerms = synonymExpander.normalizeAll(resolvedTerms);

        List<String> result = new ArrayList<>();

        for (String term : canonicalTerms) {
            if (term != null && !term.isBlank()) {
                result.add(term);
            }
        }

        return result;
    }

    public List<String> processDistinct(List<String> tokens) {
        return new ArrayList<>(new LinkedHashSet<>(process(tokens)));
    }
}

