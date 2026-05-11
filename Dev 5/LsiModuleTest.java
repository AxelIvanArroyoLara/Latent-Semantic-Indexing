package com.equipo.documentbase.indexing;

import com.equipo.documentbase.domain.FrequencyMatrix;
import com.equipo.documentbase.domain.LatentSpaceModel;
import com.equipo.documentbase.domain.SemanticDocument;

import com.equipo.documentbase.lsi.LsiInspector;
import com.equipo.documentbase.lsi.LsiReducer;

import com.equipo.documentbase.persistence.FileLatentModelRepository;
import com.equipo.documentbase.persistence.LatentModelRepository;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LsiModuleTest {

    // =====================================================
    // Frequency Matrix
    // =====================================================

    @Test
    void testFrequencyMatrixConstruction() {

        var docs = FixtureSemanticDocuments.build();

        FrequencyMatrixBuilder builder =
                new FrequencyMatrixBuilder();

        FrequencyMatrix fm =
                builder.build(docs);

        assertNotNull(fm);

        assertNotNull(fm.terms());
        assertFalse(fm.terms().isEmpty());

        assertNotNull(fm.documentCodes());

        assertEquals(
                docs.size(),
                fm.documentCodes().size()
        );

        assertEquals(
                fm.terms().size(),
                fm.values().length
        );

        for (double[] row : fm.values()) {

            assertEquals(
                    docs.size(),
                    row.length
            );
        }
    }

    @Test
    void testFrequencyMatrixRawFrequencies() {

        var docs = FixtureSemanticDocuments.build();

        FrequencyMatrix fm =
                new FrequencyMatrixBuilder()
                        .build(docs, false);

        boolean foundInteger = false;

        for (double[] row : fm.values()) {

            for (double value : row) {

                if (value > 0 &&
                        value == Math.floor(value)) {

                    foundInteger = true;
                    break;
                }
            }
        }

        assertTrue(
                foundInteger,
                "Expected integer frequencies"
        );
    }

    @Test
    void testTfIdfProducesWeightedValues() {

        var docs = FixtureSemanticDocuments.build();

        FrequencyMatrix fm =
                new FrequencyMatrixBuilder()
                        .build(docs, true);

        boolean foundDecimal = false;

        for (double[] row : fm.values()) {

            for (double value : row) {

                if (value > 0 &&
                        value != Math.floor(value)) {

                    foundDecimal = true;
                    break;
                }
            }
        }

        assertTrue(
                foundDecimal,
                "Expected TF-IDF decimal weights"
        );
    }

    @Test
    void testEmptyDocumentListShouldFail() {

        FrequencyMatrixBuilder builder =
                new FrequencyMatrixBuilder();

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.build(List.of())
        );
    }

    // =====================================================
    // LSI / SVD
    // =====================================================

    @Test
    void testLsiSvdAndModel() {

        var docs = FixtureSemanticDocuments.build();

        FrequencyMatrix fm =
                new FrequencyMatrixBuilder()
                        .build(docs);

        LsiReducer reducer =
                new LsiReducer();

        LatentSpaceModel model =
                reducer.reduce(fm, 3);

        assertNotNull(model);

        assertEquals(3, model.k());

        // docs x k
        assertEquals(
                docs.size(),
                model.documentVectors().length
        );

        // terms x k
        assertEquals(
                fm.terms().size(),
                model.termVectors().length
        );

        assertEquals(
                3,
                model.singularValues().length
        );

        // Document vector dimensionality
        for (double[] vec : model.documentVectors()) {

            assertEquals(
                    model.k(),
                    vec.length
            );
        }

        // Term vector dimensionality
        for (double[] vec : model.termVectors()) {

            assertEquals(
                    model.k(),
                    vec.length
            );
        }

        // Metadata
        assertEquals(
                fm.documentCodes(),
                model.documentCodes()
        );

        assertEquals(
                fm.terms(),
                model.terms()
        );
    }

    @Test
    void testSingularValuesAreDescending() {

        var docs = FixtureSemanticDocuments.build();

        FrequencyMatrix fm =
                new FrequencyMatrixBuilder()
                        .build(docs);

        LatentSpaceModel model =
                new LsiReducer()
                        .reduce(fm, 4);

        double[] sv =
                model.singularValues();

        for (int i = 0;
             i < sv.length - 1;
             i++) {

            assertTrue(
                    sv[i] >= sv[i + 1],
                    "Singular values should be descending"
            );
        }
    }

    @Test
    void testInvalidKShouldFail() {

        var docs = FixtureSemanticDocuments.build();

        FrequencyMatrix fm =
                new FrequencyMatrixBuilder()
                        .build(docs);

        LsiReducer reducer =
                new LsiReducer();

        assertThrows(
                IllegalArgumentException.class,
                () -> reducer.reduce(fm, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> reducer.reduce(fm, -1)
        );
    }

    @Test
    void testNullFrequencyMatrixShouldFail() {

        LsiReducer reducer =
                new LsiReducer();

        assertThrows(
                IllegalArgumentException.class,
                () -> reducer.reduce(null, 2)
        );
    }

    @Test
    void testRequestedKGreaterThanRank() {

        var docs = FixtureSemanticDocuments.build();

        FrequencyMatrix fm =
                new FrequencyMatrixBuilder()
                        .build(docs);

        LatentSpaceModel model =
                new LsiReducer()
                        .reduce(fm, 999);

        assertTrue(model.k() > 0);

        assertTrue(model.k() <= 999);
    }

    @Test
    void testSingleDocumentMatrix() {

        SemanticDocument doc =
                new SemanticDocument(
                        "D1",
                        List.of(
                                "estres",
                                "estres",
                                "ansiedad"
                        ),
                        Map.of()
                );

        FrequencyMatrix fm =
                new FrequencyMatrixBuilder()
                        .build(List.of(doc));

        assertEquals(
                1,
                fm.documentCodes().size()
        );

        LatentSpaceModel model =
                new LsiReducer()
                        .reduce(fm, 1);

        assertEquals(1, model.k());

        assertEquals(
                1,
                model.documentVectors().length
        );
    }

    // =====================================================
    // Inspector
    // =====================================================

    @Test
    void testLsiInspector() {

        var docs = FixtureSemanticDocuments.build();

        FrequencyMatrix fm =
                new FrequencyMatrixBuilder()
                        .build(docs);

        LatentSpaceModel model =
                new LsiReducer()
                        .reduce(fm, 2);

        String inspection =
                LsiInspector.inspect(model);

        assertNotNull(inspection);

        assertTrue(
                inspection.contains("Valores singulares")
        );

        assertTrue(
                inspection.contains("Componente")
        );
    }

    // =====================================================
    // Persistence
    // =====================================================

    @Test
    void testModelPersistence() {

        var docs = FixtureSemanticDocuments.build();

        FrequencyMatrix fm =
                new FrequencyMatrixBuilder()
                        .build(docs);

        LatentSpaceModel original =
                new LsiReducer()
                        .reduce(fm, 2);

        String file =
                "test-lsi-model.bin";

        LatentModelRepository repo = new FileLatentModelRepository(file);

        repo.save(original);

        var loadedOpt = repo.loadLatest();

        assertTrue(loadedOpt.isPresent(), "Expected model to be present after save");

        LatentSpaceModel loaded = loadedOpt.get();

        assertEquals(original.k(), loaded.k());

        assertEquals(
                original.documentVectors().length,
                loaded.documentVectors().length
        );

        assertEquals(
                original.termVectors().length,
                loaded.termVectors().length
        );

        assertEquals(
                original.singularValues().length,
                loaded.singularValues().length
        );

        // Cleanup
        new File(file).delete();
    }
}