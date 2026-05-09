package com.lsi.preprocessing;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the preprocessing pipeline.
 */
public class PreprocessingPipelineTest {

    @Test
    void shouldProcessEnglishDocumentText() {
        PreprocessingPipeline pipeline = new PreprocessingPipeline();

        List<String> result = pipeline.process(
                "College students are experiencing anxiety and stress."
        );

        assertEquals(
                List.of("college", "student", "experienc", "anxiety", "stress"),
                result
        );
    }

    @Test
    void shouldReturnEmptyListForBlankInput() {
        PreprocessingPipeline pipeline = new PreprocessingPipeline();

        List<String> result = pipeline.process("   ");

        assertEquals(List.of(), result);
    }
}
