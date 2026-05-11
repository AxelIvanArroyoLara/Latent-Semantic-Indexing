package com.lsi.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TermRepositoryTest {

    private TermRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TermRepository();
        repository.deleteAll();
    }

    @Test
    void shouldSaveAndFindTermById() {
        long termId = repository.save(
                "stress",
                "stress",
                "stress",
                null
        );

        Optional<TermRepository.TermRow> result = repository.findById(termId);

        assertTrue(result.isPresent(), "Term should exist");
        assertEquals("stress", result.get().normalizedTerm());
        assertEquals("stress", result.get().canonicalTerm());
        assertEquals("stress", result.get().stem());
        assertNull(result.get().senseLabel());
    }

    @Test
    void shouldFindTermByNormalizedTerm() {
        repository.save(
                "anxiety",
                "anxiety",
                "anxieti",
                null
        );

        Optional<TermRepository.TermRow> result = repository.findByNormalizedTerm("anxiety");

        assertTrue(result.isPresent(), "Term should exist");
        assertEquals("anxiety", result.get().normalizedTerm());
        assertEquals("anxiety", result.get().canonicalTerm());
        assertEquals("anxieti", result.get().stem());
    }

    @Test
    void shouldFindTermByNormalizedTermAndNullSense() {
        repository.save(
                "support",
                "support",
                "support",
                null
        );

        Optional<TermRepository.TermRow> result =
                repository.findByNormalizedTermAndSense("support", null);

        assertTrue(result.isPresent(), "Term with null sense should exist");
        assertEquals("support", result.get().normalizedTerm());
        assertNull(result.get().senseLabel());
    }

    @Test
    void shouldFindTermByNormalizedTermAndSpecificSense() {
        repository.save(
                "support",
                "support",
                "support",
                "institutional_support"
        );

        Optional<TermRepository.TermRow> result =
                repository.findByNormalizedTermAndSense("support", "institutional_support");

        assertTrue(result.isPresent(), "Term with specific sense should exist");
        assertEquals("support", result.get().normalizedTerm());
        assertEquals("institutional_support", result.get().senseLabel());
    }

    @Test
    void shouldListAllTerms() {
        repository.save(
                "stress",
                "stress",
                "stress",
                null
        );

        repository.save(
                "anxiety",
                "anxiety",
                "anxieti",
                null
        );

        List<TermRepository.TermRow> terms = repository.findAll();

        assertEquals(2, terms.size());
        assertEquals("stress", terms.get(0).normalizedTerm());
        assertEquals("anxiety", terms.get(1).normalizedTerm());
    }

    @Test
    void shouldReturnEmptyWhenTermDoesNotExist() {
        Optional<TermRepository.TermRow> result =
                repository.findByNormalizedTerm("nonexistent");

        assertTrue(result.isEmpty(), "Term should not exist");
    }
}