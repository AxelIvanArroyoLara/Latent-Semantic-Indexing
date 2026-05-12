package com.lsi.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FrequencyRepositoryTest {

    private DocumentRepository documentRepository;
    private TermRepository termRepository;
    private FrequencyRepository frequencyRepository;

    @BeforeEach
    void setUp() {
        DatabaseTestSupport.assumeDatabaseAvailable();
        documentRepository = new DocumentRepository();
        termRepository = new TermRepository();
        frequencyRepository = new FrequencyRepository();

        frequencyRepository.deleteAll();
        documentRepository.deleteAll();
        termRepository.deleteAll();
    }

    @Test
    void shouldSaveAndFindFrequencyByDocumentAndTerm() {
        long documentId = documentRepository.save(
                "D1",
                "Academic Stress in University Students",
                "data/raw/D1.txt",
                "Academic stress affects university students.",
                "academic stress affects university students",
                "en"
        );

        long termId = termRepository.save(
                "stress",
                "stress",
                "stress",
                null
        );

        frequencyRepository.saveFrequency(documentId, termId, 3.0, 0.75);

        Optional<FrequencyRepository.FrequencyRow> result =
                frequencyRepository.findByDocumentIdAndTermId(documentId, termId);

        assertTrue(result.isPresent(), "Frequency should exist");
        assertEquals(documentId, result.get().documentId());
        assertEquals(termId, result.get().termId());
        assertEquals(3.0, result.get().rawFrequency());
        assertEquals(0.75, result.get().weightedFrequency());
    }

    @Test
    void shouldUpdateFrequencyWhenDocumentAndTermAlreadyExist() {
        long documentId = documentRepository.save(
                "D1",
                "Academic Stress in University Students",
                "data/raw/D1.txt",
                "Academic stress affects university students.",
                "academic stress affects university students",
                "en"
        );

        long termId = termRepository.save(
                "stress",
                "stress",
                "stress",
                null
        );

        frequencyRepository.saveFrequency(documentId, termId, 2.0, 0.50);
        frequencyRepository.saveFrequency(documentId, termId, 5.0, 1.25);

        Optional<FrequencyRepository.FrequencyRow> result =
                frequencyRepository.findByDocumentIdAndTermId(documentId, termId);

        assertTrue(result.isPresent(), "Frequency should exist");
        assertEquals(5.0, result.get().rawFrequency());
        assertEquals(1.25, result.get().weightedFrequency());
    }

    @Test
    void shouldFindFrequenciesByDocumentId() {
        long documentId = documentRepository.save(
                "D1",
                "Academic Stress in University Students",
                "data/raw/D1.txt",
                "Academic stress affects university students.",
                "academic stress affects university students",
                "en"
        );

        long stressTermId = termRepository.save("stress", "stress", "stress", null);
        long anxietyTermId = termRepository.save("anxiety", "anxiety", "anxieti", null);

        frequencyRepository.saveFrequency(documentId, stressTermId, 3.0, 0.75);
        frequencyRepository.saveFrequency(documentId, anxietyTermId, 1.0, 0.25);

        List<FrequencyRepository.FrequencyRow> frequencies =
                frequencyRepository.findByDocumentId(documentId);

        assertEquals(2, frequencies.size());
        assertEquals(stressTermId, frequencies.get(0).termId());
        assertEquals(anxietyTermId, frequencies.get(1).termId());
    }

    @Test
    void shouldFindFrequenciesByTermId() {
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

        long termId = termRepository.save("stress", "stress", "stress", null);

        frequencyRepository.saveFrequency(documentOneId, termId, 3.0, 0.75);
        frequencyRepository.saveFrequency(documentTwoId, termId, 1.0, 0.25);

        List<FrequencyRepository.FrequencyRow> frequencies =
                frequencyRepository.findByTermId(termId);

        assertEquals(2, frequencies.size());
        assertEquals(documentOneId, frequencies.get(0).documentId());
        assertEquals(documentTwoId, frequencies.get(1).documentId());
    }

    @Test
    void shouldListAllFrequencies() {
        long documentId = documentRepository.save(
                "D1",
                "Academic Stress in University Students",
                "data/raw/D1.txt",
                "Academic stress affects university students.",
                "academic stress affects university students",
                "en"
        );

        long termId = termRepository.save("stress", "stress", "stress", null);

        frequencyRepository.saveFrequency(documentId, termId, 3.0, 0.75);

        List<FrequencyRepository.FrequencyRow> frequencies = frequencyRepository.findAll();

        assertEquals(1, frequencies.size());
        assertEquals(documentId, frequencies.get(0).documentId());
        assertEquals(termId, frequencies.get(0).termId());
    }

    @Test
    void shouldReturnEmptyWhenFrequencyDoesNotExist() {
        Optional<FrequencyRepository.FrequencyRow> result =
                frequencyRepository.findByDocumentIdAndTermId(999L, 999L);

        assertTrue(result.isEmpty(), "Frequency should not exist");
    }
}
