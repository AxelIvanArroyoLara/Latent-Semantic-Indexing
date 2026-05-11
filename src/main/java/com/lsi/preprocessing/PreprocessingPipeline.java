package com.lsi.preprocessing;

import java.util.List;

/**
 * Orchestrates the full preprocessing flow:
 * normalization, tokenization, stop word filtering, and stemming.
 */
public class PreprocessingPipeline {

    private final TextNormalizer normalizer;
    private final Tokenizer tokenizer;
    private final StopWordFilter stopWordFilter;
    private final Stemmer stemmer;

    public PreprocessingPipeline() {
        this.normalizer = new TextNormalizer();
        this.tokenizer = new Tokenizer();
        this.stopWordFilter = new StopWordFilter();
        this.stemmer = new Stemmer();
    }

    public PreprocessingPipeline(
            TextNormalizer normalizer,
            Tokenizer tokenizer,
            StopWordFilter stopWordFilter,
            Stemmer stemmer
    ) {
        this.normalizer = normalizer;
        this.tokenizer = tokenizer;
        this.stopWordFilter = stopWordFilter;
        this.stemmer = stemmer;
    }

    public List<String> process(String rawText) {
        String normalizedText = normalizer.normalize(rawText);
        List<String> tokens = tokenizer.tokenize(normalizedText);
        List<String> filteredTokens = stopWordFilter.filter(tokens);

        return stemmer.stemAll(filteredTokens);
    }
}
