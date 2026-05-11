package com.lsi.semantic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SemanticPipelineTest {

    @Test
    void shouldNormalizeSynonymsAndResolveContextTerms() {
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

        assertEquals(
                List.of(
                        "student",
                        "anxiety",
                        "academic_pressure",
                        "emotional_support",
                        "institutional_support"
                ),
                result
        );
    }
}
