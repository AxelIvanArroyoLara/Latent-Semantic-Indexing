package com.lsi.semantic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class SemanticPipelineTest {

    @Test
    void shouldCanonicalizeSynonyms() {
        SynonymExpander expander = new SynonymExpander();

        List<String> result = expander.expand(List.of(
                "worry",
                "nervousness",
                "tension",
                "workout"
        ));

        assertEquals(List.of(
                "anxiety",
                "anxiety",
                "stress",
                "physical_activity"
        ), result);
    }

    @Test
    void shouldResolvePolysemyUsingContextBeforeAmbiguousTerm() {
        PolysemyResolver resolver = new PolysemyResolver();

        List<String> result = resolver.resolve(List.of(
                "academic",
                "pressure",
                "emotional",
                "support",
                "university",
                "support"
        ));

        assertEquals(List.of(
                "academic_pressure",
                "emotional_support",
                "institutional_support"
        ), result);
    }

    @Test
    void shouldPreserveUnknownTerms() {
        SemanticPipeline pipeline = new SemanticPipeline();

        List<String> result = pipeline.process(List.of(
                "student",
                "motivation",
                "focus"
        ));

        assertEquals(List.of(
                "student",
                "motivation",
                "focus"
        ), result);
    }

    @Test
    void shouldRunFullSemanticPipelineWithSynonymsAndPolysemy() {
        SemanticPipeline pipeline = new SemanticPipeline();

        List<String> result = pipeline.process(List.of(
                "student",
                "worry",
                "academic",
                "pressure",
                "emotional",
                "support",
                "university",
                "support"
        ));

        assertEquals(List.of(
                "student",
                "anxiety",
                "academic_pressure",
                "emotional_support",
                "institutional_support"
        ), result);
    }

    @Test
    void shouldMatchFixtureExpectedOutput() throws Exception {
        SemanticPipeline pipeline = new SemanticPipeline();

        String input = java.nio.file.Files.readString(
                java.nio.file.Path.of("data/fixtures/semantics/input_tokens.txt")
        ).trim();

        String expected = java.nio.file.Files.readString(
                java.nio.file.Path.of("data/fixtures/semantics/expected_semantic_terms.txt")
        ).trim();

        String result = pipeline.processText(input);

        assertEquals(expected, result);
        }

}
