package com.lsi.lsi;

import com.lsi.indexing.FixtureSemanticDocuments;
import com.lsi.indexing.FrequencyMatrixBuilder;
import com.lsi.model.FrequencyMatrix;
import com.lsi.model.LatentSpaceModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LsiReducerTest {

    @Test
    void shouldSelectTopTermsForExpertReview() {
        FrequencyMatrix matrix = new FrequencyMatrixBuilder()
                .build(FixtureSemanticDocuments.build());
        LatentSpaceModel model = new SvdDecomposer().decompose(matrix, 3);

        var selectedTerms = new TermSelectionService().selectTopTerms(model, 5);

        assertEquals(5, selectedTerms.size());
        assertFalse(selectedTerms.get(0).term().isBlank());
        assertNotNull(selectedTerms.get(0).significance());
    }
}
