package com.lsi.indexing;

import com.lsi.model.FrequencyMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class FrequencyMatrixBuilderTest {

    @Test
    void buildsFrequencyMatrixFromSemanticDocuments() {
        FrequencyMatrix matrix = new FrequencyMatrixBuilder()
                .build(FixtureSemanticDocuments.build(), false);

        assertFalse(matrix.terms().isEmpty());
        assertEquals(
                FixtureSemanticDocuments.build().size(),
                matrix.documentCodes().size()
        );
        assertEquals(matrix.terms().size(), matrix.values().length);
    }
}
