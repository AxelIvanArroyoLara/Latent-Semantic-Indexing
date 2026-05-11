package com.lsi.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LsiRepositoryTest {

    private DocumentRepository documentRepository;
    private LsiRepository lsiRepository;

    @BeforeEach
    void setUp() {
        documentRepository = new DocumentRepository();
        lsiRepository = new LsiRepository();

        lsiRepository.deleteAll();
        documentRepository.deleteAll();
    }

    @Test
    void shouldSaveAndFindLatentModelById() {
        long modelId = lsiRepository.saveLatentModel(
                3,
                "raw_frequency",
                "Test LSI model with k=3"
        );

        Optional<LsiRepository.LatentModelRow> result =
                lsiRepository.findLatentModelById(modelId);

        assertTrue(result.isPresent(), "Latent model should exist");
        assertEquals(modelId, result.get().latentModelId());
        assertEquals(3, result.get().kValue());
        assertEquals("raw_frequency", result.get().sourceMatrixType());
        assertEquals("Test LSI model with k=3", result.get().notes());
        assertNotNull(result.get().createdAt());
    }

    @Test
    void shouldListAllLatentModels() {
        lsiRepository.saveLatentModel(2, "raw_frequency", "Model k=2");
        lsiRepository.saveLatentModel(3, "weighted_frequency", "Model k=3");

        List<LsiRepository.LatentModelRow> models =
                lsiRepository.findAllLatentModels();

        assertEquals(2, models.size());
        assertEquals(2, models.get(0).kValue());
        assertEquals(3, models.get(1).kValue());
    }

    @Test
    void shouldSaveAndFindDocumentVector() {
        long documentId = documentRepository.save(
                "D1",
                "Academic Stress in University Students",
                "data/raw/D1.txt",
                "Academic stress affects university students.",
                "academic stress affects university students",
                "en"
        );

        long modelId = lsiRepository.saveLatentModel(
                3,
                "raw_frequency",
                "Test model"
        );

        lsiRepository.saveDocumentVectorComponent(modelId, documentId, 0, 0.25);
        lsiRepository.saveDocumentVectorComponent(modelId, documentId, 1, 0.50);
        lsiRepository.saveDocumentVectorComponent(modelId, documentId, 2, 0.75);

        List<LsiRepository.LatentDocumentVectorRow> vector =
                lsiRepository.findDocumentVector(modelId, documentId);

        assertEquals(3, vector.size());

        assertEquals(0, vector.get(0).componentIndex());
        assertEquals(0.25, vector.get(0).componentValue());

        assertEquals(1, vector.get(1).componentIndex());
        assertEquals(0.50, vector.get(1).componentValue());

        assertEquals(2, vector.get(2).componentIndex());
        assertEquals(0.75, vector.get(2).componentValue());
    }

    @Test
    void shouldUpdateDocumentVectorComponentWhenItAlreadyExists() {
        long documentId = documentRepository.save(
                "D1",
                "Academic Stress in University Students",
                "data/raw/D1.txt",
                "Academic stress affects university students.",
                "academic stress affects university students",
                "en"
        );

        long modelId = lsiRepository.saveLatentModel(
                2,
                "raw_frequency",
                "Test model"
        );

        lsiRepository.saveDocumentVectorComponent(modelId, documentId, 0, 0.25);
        lsiRepository.saveDocumentVectorComponent(modelId, documentId, 0, 0.90);

        List<LsiRepository.LatentDocumentVectorRow> vector =
                lsiRepository.findDocumentVector(modelId, documentId);

        assertEquals(1, vector.size());
        assertEquals(0, vector.get(0).componentIndex());
        assertEquals(0.90, vector.get(0).componentValue());
    }

    @Test
    void shouldFindDocumentVectorsByModel() {
        long documentOneId = documentRepository.save(
                "D1",
                "Academic Stress in University Students",
                "data/raw/D1.txt",
                "Academic stress affects university students.",
                "academic stress affects university students",
                "en"
        );

        long documentTwoId = documentRepository.save(
                "D2",
                "Anxiety and School Performance",
                "data/raw/D2.txt",
                "Anxiety can affect school performance.",
                "anxiety can affect school performance",
                "en"
        );

        long modelId = lsiRepository.saveLatentModel(
                2,
                "raw_frequency",
                "Test model"
        );

        lsiRepository.saveDocumentVectorComponent(modelId, documentOneId, 0, 0.10);
        lsiRepository.saveDocumentVectorComponent(modelId, documentOneId, 1, 0.20);
        lsiRepository.saveDocumentVectorComponent(modelId, documentTwoId, 0, 0.30);
        lsiRepository.saveDocumentVectorComponent(modelId, documentTwoId, 1, 0.40);

        List<LsiRepository.LatentDocumentVectorRow> vectors =
                lsiRepository.findDocumentVectorsByModel(modelId);

        assertEquals(4, vectors.size());

        assertEquals(documentOneId, vectors.get(0).documentId());
        assertEquals(0, vectors.get(0).componentIndex());

        assertEquals(documentOneId, vectors.get(1).documentId());
        assertEquals(1, vectors.get(1).componentIndex());

        assertEquals(documentTwoId, vectors.get(2).documentId());
        assertEquals(0, vectors.get(2).componentIndex());

        assertEquals(documentTwoId, vectors.get(3).documentId());
        assertEquals(1, vectors.get(3).componentIndex());
    }

    @Test
    void shouldSaveAndFindQueryVectorByModel() {
        long modelId = lsiRepository.saveLatentModel(
                2,
                "raw_frequency",
                "Test model"
        );

        lsiRepository.saveQueryVectorComponent(
                modelId,
                "academic stress anxiety",
                0,
                0.35
        );

        lsiRepository.saveQueryVectorComponent(
                modelId,
                "academic stress anxiety",
                1,
                0.65
        );

        List<LsiRepository.LatentQueryVectorRow> queryVectors =
                lsiRepository.findQueryVectorsByModel(modelId);

        assertEquals(2, queryVectors.size());

        assertEquals(modelId, queryVectors.get(0).latentModelId());
        assertEquals("academic stress anxiety", queryVectors.get(0).queryText());
        assertEquals(0, queryVectors.get(0).componentIndex());
        assertEquals(0.35, queryVectors.get(0).componentValue());

        assertEquals(1, queryVectors.get(1).componentIndex());
        assertEquals(0.65, queryVectors.get(1).componentValue());
    }

    @Test
    void shouldReturnEmptyWhenLatentModelDoesNotExist() {
        Optional<LsiRepository.LatentModelRow> result =
                lsiRepository.findLatentModelById(999L);

        assertTrue(result.isEmpty(), "Latent model should not exist");
    }
}