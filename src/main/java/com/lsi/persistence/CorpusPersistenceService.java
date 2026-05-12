package com.lsi.persistence;

import com.lsi.indexing.IndexingService;
import com.lsi.lsi.TermSelectionService;
import com.lsi.model.FrequencyMatrix;
import com.lsi.model.LatentSpaceModel;
import com.lsi.model.SemanticDocument;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CorpusPersistenceService {

    private final DocumentRepository documentRepository;
    private final TermRepository termRepository;
    private final FrequencyRepository frequencyRepository;
    private final LsiRepository lsiRepository;
    private final SelectedTermRepository selectedTermRepository;
    private final TermSelectionService termSelectionService;

    public CorpusPersistenceService() {
        this(
                new DocumentRepository(),
                new TermRepository(),
                new FrequencyRepository(),
                new LsiRepository(),
                new SelectedTermRepository(),
                new TermSelectionService()
        );
    }

    public CorpusPersistenceService(
            DocumentRepository documentRepository,
            TermRepository termRepository,
            FrequencyRepository frequencyRepository,
            LsiRepository lsiRepository,
            SelectedTermRepository selectedTermRepository,
            TermSelectionService termSelectionService
    ) {
        this.documentRepository = documentRepository;
        this.termRepository = termRepository;
        this.frequencyRepository = frequencyRepository;
        this.lsiRepository = lsiRepository;
        this.selectedTermRepository = selectedTermRepository;
        this.termSelectionService = termSelectionService;
    }

    public PersistenceSummary persist(
            IndexingService.IndexedCorpus corpus,
            List<TermSelectionService.SelectedTerm> expertTerms
    ) {
        Map<String, Long> documentIds = persistDocuments(corpus);
        Map<String, Long> termIds = persistTerms(corpus.frequencyMatrix());
        int frequencyRows = persistFrequencies(corpus.frequencyMatrix(), documentIds, termIds);
        long latentModelId = persistLsiModel(corpus.latentSpaceModel(), documentIds);
        int selectedRows = persistSelectedTerms(latentModelId, termIds, expertTerms);

        return new PersistenceSummary(
                documentIds.size(),
                termIds.size(),
                frequencyRows,
                latentModelId,
                selectedRows
        );
    }

    private Map<String, Long> persistDocuments(IndexingService.IndexedCorpus corpus) {
        Map<String, Long> ids = new LinkedHashMap<>();

        for (SemanticDocument document : corpus.semanticDocuments()) {
            String title = corpus.titlesByCode().getOrDefault(document.code(), document.code());
            String rawText = String.join(" ", document.canonicalTerms());
            String source = String.valueOf(document.metadata()
                    .getOrDefault("source", "data/fixtures/query/document_terms.csv"));

            DocumentRepository.DocumentRow existing = documentRepository.findByCode(document.code())
                    .orElse(null);

            long id = existing == null
                    ? documentRepository.save(document.code(), title, source, rawText, rawText, "en")
                    : existing.documentId();

            ids.put(document.code(), id);
        }

        return ids;
    }

    private Map<String, Long> persistTerms(FrequencyMatrix matrix) {
        Map<String, Long> ids = new LinkedHashMap<>();

        for (String term : matrix.terms()) {
            TermRepository.TermRow existing = termRepository.findByNormalizedTerm(term)
                    .orElse(null);

            long id = existing == null
                    ? termRepository.save(term, term, term, null)
                    : existing.termId();

            ids.put(term, id);
        }

        return ids;
    }

    private int persistFrequencies(
            FrequencyMatrix matrix,
            Map<String, Long> documentIds,
            Map<String, Long> termIds
    ) {
        int rows = 0;

        for (int termIndex = 0; termIndex < matrix.terms().size(); termIndex++) {
            String term = matrix.terms().get(termIndex);
            Long termId = termIds.get(term);

            for (int docIndex = 0; docIndex < matrix.documentCodes().size(); docIndex++) {
                double weight = matrix.values()[termIndex][docIndex];

                if (weight <= 0) {
                    continue;
                }

                Long documentId = documentIds.get(matrix.documentCodes().get(docIndex));
                frequencyRepository.saveFrequency(documentId, termId, weight, weight);
                rows++;
            }
        }

        return rows;
    }

    private long persistLsiModel(
            LatentSpaceModel model,
            Map<String, Long> documentIds
    ) {
        long latentModelId = lsiRepository.saveLatentModel(
                model.k(),
                "weighted_frequency",
                "Persisted from main final-demo execution"
        );

        for (int docIndex = 0; docIndex < model.documentCodes().size(); docIndex++) {
            Long documentId = documentIds.get(model.documentCodes().get(docIndex));
            double[] vector = model.documentVectors()[docIndex];

            for (int component = 0; component < vector.length; component++) {
                lsiRepository.saveDocumentVectorComponent(
                        latentModelId,
                        documentId,
                        component,
                        vector[component]
                );
            }
        }

        return latentModelId;
    }

    private int persistSelectedTerms(
            long latentModelId,
            Map<String, Long> termIds,
            List<TermSelectionService.SelectedTerm> expertTerms
    ) {
        int rows = 0;

        for (TermSelectionService.SelectedTerm selectedTerm : expertTerms) {
            selectedTermRepository.saveSelectedTerm(
                    latentModelId,
                    termIds.get(selectedTerm.term()),
                    selectedTerm.term(),
                    selectedTerm.significance(),
                    "expert",
                    "cli"
            );
            rows++;
        }

        return rows;
    }

    public List<TermSelectionService.SelectedTerm> defaultExpertTerms(
            LatentSpaceModel model,
            int topTerms
    ) {
        return termSelectionService.selectTopTerms(model, topTerms);
    }

    public record PersistenceSummary(
            int documents,
            int terms,
            int frequencyRows,
            long latentModelId,
            int selectedTerms
    ) {
    }
}
