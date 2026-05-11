package com.lsi.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DocumentRepositoryTest {

    private DocumentRepository repository;

    @BeforeEach
    void setUp() {
        repository = new DocumentRepository();
        repository.deleteAll();
    }

    @Test
    void shouldSaveAndFindDocumentById() {
        long documentId = repository.save(
                "D1",
                "Academic Stress in University Students",
                "data/raw/D1.txt",
                "Academic stress affects university students.",
                "academic stress affects university students",
                "en"
        );

        Optional<DocumentRepository.DocumentRow> result = repository.findById(documentId);

        assertTrue(result.isPresent(), "Document should exist");
        assertEquals("D1", result.get().code());
        assertEquals("Academic Stress in University Students", result.get().title());
        assertEquals("en", result.get().languageCode());
    }

    @Test
    void shouldFindDocumentByCode() {
        repository.save(
                "D2",
                "Anxiety and School Performance",
                "data/raw/D2.txt",
                "Anxiety can affect school performance.",
                "anxiety can affect school performance",
                "en"
        );

        Optional<DocumentRepository.DocumentRow> result = repository.findByCode("D2");

        assertTrue(result.isPresent(), "Document should exist");
        assertEquals("D2", result.get().code());
        assertEquals("Anxiety and School Performance", result.get().title());
    }

    @Test
    void shouldListAllDocuments() {
        repository.save(
                "D1",
                "Academic Stress in University Students",
                "data/raw/D1.txt",
                "Academic stress affects university students.",
                "academic stress affects university students",
                "en"
        );

        repository.save(
                "D2",
                "Anxiety and School Performance",
                "data/raw/D2.txt",
                "Anxiety can affect school performance.",
                "anxiety can affect school performance",
                "en"
        );

        List<DocumentRepository.DocumentRow> documents = repository.findAll();

        assertEquals(2, documents.size());
        assertEquals("D1", documents.get(0).code());
        assertEquals("D2", documents.get(1).code());
    }

    @Test
    void shouldReturnEmptyWhenDocumentDoesNotExist() {
        Optional<DocumentRepository.DocumentRow> result = repository.findByCode("D999");

        assertTrue(result.isEmpty(), "Document should not exist");
    }
}