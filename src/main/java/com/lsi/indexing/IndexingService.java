package com.lsi.indexing;

import com.lsi.config.AppConfig;
import com.lsi.lsi.LsiReducer;
import com.lsi.model.Document;
import com.lsi.model.FrequencyMatrix;
import com.lsi.model.LatentSpaceModel;
import com.lsi.model.SemanticDocument;
import com.lsi.preprocessing.PreprocessingPipeline;
import com.lsi.semantic.SemanticPipeline;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Coordinates preprocessing, semantic normalization, frequency matrix creation,
 * and LSI reduction for an in-memory corpus.
 */
public class IndexingService {

    private final PreprocessingPipeline preprocessingPipeline;
    private final SemanticPipeline semanticPipeline;
    private final FrequencyMatrixBuilder frequencyMatrixBuilder;
    private final LsiReducer lsiReducer;

    public IndexingService() {
        this(
                new PreprocessingPipeline(),
                new SemanticPipeline(),
                new FrequencyMatrixBuilder(),
                new LsiReducer()
        );
    }

    public IndexingService(
            PreprocessingPipeline preprocessingPipeline,
            SemanticPipeline semanticPipeline,
            FrequencyMatrixBuilder frequencyMatrixBuilder,
            LsiReducer lsiReducer
    ) {
        this.preprocessingPipeline = preprocessingPipeline;
        this.semanticPipeline = semanticPipeline;
        this.frequencyMatrixBuilder = frequencyMatrixBuilder;
        this.lsiReducer = lsiReducer;
    }

    public IndexedCorpus index(List<Document> documents) {
        return index(documents, AppConfig.DEFAULT_LSI_DIMENSIONS, true);
    }

    public IndexedCorpus index(
            List<Document> documents,
            int dimensions,
            boolean useTfIdf
    ) {
        if (documents == null || documents.isEmpty()) {
            throw new IllegalArgumentException("Document list cannot be null or empty");
        }

        List<SemanticDocument> semanticDocuments = new ArrayList<>();
        Map<String, String> titlesByCode = new LinkedHashMap<>();

        for (Document document : documents) {
            List<String> tokens = preprocessingPipeline.process(document.textForProcessing());
            List<String> canonicalTerms = semanticPipeline.process(tokens);

            semanticDocuments.add(new SemanticDocument(
                    document.code(),
                    canonicalTerms,
                    Map.of("title", document.title(), "source", document.source())
            ));
            titlesByCode.put(document.code(), document.title());
        }

        FrequencyMatrix frequencyMatrix = frequencyMatrixBuilder.build(semanticDocuments, useTfIdf);
        LatentSpaceModel model = lsiReducer.reduce(frequencyMatrix, dimensions);

        return new IndexedCorpus(semanticDocuments, frequencyMatrix, model, titlesByCode);
    }

    public IndexedCorpus indexQueryFixtures() {
        return indexQueryFixtures(AppConfig.QUERY_FIXTURE_DIR, AppConfig.DEFAULT_LSI_DIMENSIONS);
    }

    public IndexedCorpus indexQueryFixtures(Path fixtureDir, int dimensions) {
        return index(loadDocumentsFromQueryFixture(fixtureDir), dimensions, true);
    }

    public List<Document> loadDocumentsFromQueryFixture(Path fixtureDir) {
        Path path = fixtureDir.resolve("document_terms.csv");

        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            List<Document> documents = new ArrayList<>();

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();

                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(",", 3);

                if (parts.length < 3) {
                    continue;
                }

                documents.add(new Document(
                        parts[0].trim(),
                        parts[1].trim(),
                        path.toString(),
                        parts[2].trim(),
                        parts[2].trim()
                ));
            }

            return documents;
        } catch (IOException e) {
            throw new IllegalStateException("Could not load query fixture documents from " + path, e);
        }
    }

    public record IndexedCorpus(
            List<SemanticDocument> semanticDocuments,
            FrequencyMatrix frequencyMatrix,
            LatentSpaceModel latentSpaceModel,
            Map<String, String> titlesByCode
    ) {
        public IndexedCorpus {
            semanticDocuments = List.copyOf(semanticDocuments);
            titlesByCode = Map.copyOf(titlesByCode);
        }

        public Map<String, Set<String>> termsByDocumentCode() {
            Map<String, Set<String>> result = new LinkedHashMap<>();

            for (SemanticDocument document : semanticDocuments) {
                result.put(
                        document.code(),
                        new LinkedHashSet<>(document.canonicalTerms())
                );
            }

            return result;
        }
    }
}

