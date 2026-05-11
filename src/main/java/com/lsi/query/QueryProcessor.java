package com.lsi.query;

import com.lsi.indexing.IndexingService;
import com.lsi.model.LatentSpaceModel;
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class QueryProcessor {

    private static final Path DEFAULT_FIXTURE_DIR = Path.of("data", "fixtures", "query");

    private final RankingService rankingService;
    private final DocumentSimilarityService documentSimilarityService;
    private final Path fixtureDir;
    private final FixtureData indexedData;
    private final PreprocessingPipeline preprocessingPipeline;
    private final SemanticPipeline semanticPipeline;

    public QueryProcessor() {
        this(DEFAULT_FIXTURE_DIR);
    }

    public QueryProcessor(Path fixtureDir) {
        this.fixtureDir = fixtureDir;
        this.rankingService = new RankingService();
        this.documentSimilarityService = new DocumentSimilarityService();
        this.indexedData = null;
        this.preprocessingPipeline = new PreprocessingPipeline();
        this.semanticPipeline = new SemanticPipeline();
    }

    public QueryProcessor(IndexingService.IndexedCorpus indexedCorpus) {
        this.fixtureDir = DEFAULT_FIXTURE_DIR;
        this.rankingService = new RankingService();
        this.documentSimilarityService = new DocumentSimilarityService();
        this.indexedData = fromIndexedCorpus(indexedCorpus);
        this.preprocessingPipeline = new PreprocessingPipeline();
        this.semanticPipeline = new SemanticPipeline();
    }

    public DocumentSimilarityService.ComparisonResult compareDocuments(String firstCode, String secondCode) {
        FixtureData data = loadFixtureData();

        DocumentSimilarityService.DocumentProfile first = data.documentProfile(firstCode);
        DocumentSimilarityService.DocumentProfile second = data.documentProfile(secondCode);

        if (first == null) {
            throw new IllegalArgumentException("Unknown document code: " + firstCode);
        }

        if (second == null) {
            throw new IllegalArgumentException("Unknown document code: " + secondCode);
        }

        return documentSimilarityService.compare(first, second);
    }

    public QueryRun query(String queryText, int topN, String metric) {
        FixtureData data = loadFixtureData();

        Set<String> queryTerms = tokenize(queryText);
        List<RankingService.RankedDocument> rankedDocuments;
        double[] queryVector = buildQueryVector(queryTerms, data.termVectors());

        if ("jaccard".equalsIgnoreCase(metric)) {
            rankedDocuments = rankingService.rankByTerms(
                    queryTerms,
                    data.documentTerms(),
                    topN
            );
        } else {
            rankedDocuments = rankingService.rankByVector(
                    queryVector,
                    data.documentVectors(),
                    metric,
                    topN
            );
        }

        return new QueryRun(
                queryText,
                metric.toLowerCase(Locale.ROOT),
                topN,
                queryTerms,
                queryVector,
                rankedDocuments
        );
    }

    private FixtureData loadFixtureData() {
        if (indexedData != null) {
            return indexedData;
        }

        try {
            Map<String, RankingService.DocumentVector> documentVectors = loadDocumentVectors();
            Map<String, RankingService.DocumentTerms> documentTerms = loadDocumentTerms();
            Map<String, double[]> termVectors = loadTermVectors();

            return new FixtureData(documentVectors, documentTerms, termVectors);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load query fixtures from " + fixtureDir, e);
        }
    }

    private FixtureData fromIndexedCorpus(IndexingService.IndexedCorpus indexedCorpus) {
        if (indexedCorpus == null) {
            throw new IllegalArgumentException("Indexed corpus cannot be null");
        }

        LatentSpaceModel model = indexedCorpus.latentSpaceModel();
        Map<String, RankingService.DocumentVector> documentVectors = new LinkedHashMap<>();
        Map<String, RankingService.DocumentTerms> documentTerms = new LinkedHashMap<>();
        Map<String, double[]> termVectors = new LinkedHashMap<>();
        Map<String, Set<String>> termsByDocumentCode = indexedCorpus.termsByDocumentCode();

        for (int i = 0; i < model.documentCodes().size(); i++) {
            String code = model.documentCodes().get(i);
            String title = indexedCorpus.titlesByCode().getOrDefault(code, code);

            documentVectors.put(
                    code,
                    new RankingService.DocumentVector(code, title, model.documentVectors()[i])
            );
            documentTerms.put(
                    code,
                    new RankingService.DocumentTerms(
                            code,
                            title,
                            termsByDocumentCode.getOrDefault(code, Set.of())
                    )
            );
        }

        for (int i = 0; i < model.terms().size(); i++) {
            termVectors.put(model.terms().get(i), model.termVectors()[i]);
        }

        return new FixtureData(documentVectors, documentTerms, termVectors);
    }

    private Map<String, RankingService.DocumentVector> loadDocumentVectors() throws IOException {
        Path path = fixtureDir.resolve("document_vectors.csv");
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

        Map<String, RankingService.DocumentVector> result = new LinkedHashMap<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }

            String[] parts = line.split(",", -1);

            if (parts.length < 3) {
                continue;
            }

            String code = parts[0].trim();
            String title = parts[1].trim();
            double[] vector = parseVector(parts, 2);

            result.put(code, new RankingService.DocumentVector(code, title, vector));
        }

        return result;
    }

    private Map<String, RankingService.DocumentTerms> loadDocumentTerms() throws IOException {
        Path path = fixtureDir.resolve("document_terms.csv");
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

        Map<String, RankingService.DocumentTerms> result = new LinkedHashMap<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }

            String[] parts = line.split(",", -1);

            if (parts.length < 3) {
                continue;
            }

            String code = parts[0].trim();
            String title = parts[1].trim();
            Set<String> terms = tokenize(parts[2]);

            result.put(code, new RankingService.DocumentTerms(code, title, terms));
        }

        return result;
    }

    private Map<String, double[]> loadTermVectors() throws IOException {
        Path path = fixtureDir.resolve("term_vectors.csv");
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

        Map<String, double[]> result = new LinkedHashMap<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }

            String[] parts = line.split(",", -1);

            if (parts.length < 2) {
                continue;
            }

            String term = normalize(parts[0]);
            double[] vector = parseVector(parts, 1);

            result.put(term, vector);
        }

        return result;
    }

    private double[] buildQueryVector(Set<String> queryTerms, Map<String, double[]> termVectors) {
        int dimension = inferDimension(termVectors);

        double[] result = new double[dimension];
        int matchedTerms = 0;

        for (String term : queryTerms) {
            double[] vector = termVectors.get(term);

            if (vector == null) {
                continue;
            }

            for (int i = 0; i < dimension; i++) {
                result[i] += vector[i];
            }

            matchedTerms++;
        }

        if (matchedTerms == 0) {
            return result;
        }

        for (int i = 0; i < dimension; i++) {
            result[i] = result[i] / matchedTerms;
        }

        return result;
    }

    private int inferDimension(Map<String, double[]> termVectors) {
        if (termVectors.isEmpty()) {
            return 0;
        }

        return termVectors.values().iterator().next().length;
    }

    private double[] parseVector(String[] parts, int startIndex) {
        List<Double> values = new ArrayList<>();

        for (int i = startIndex; i < parts.length; i++) {
            values.add(Double.parseDouble(parts[i].trim()));
        }

        double[] vector = new double[values.size()];

        for (int i = 0; i < values.size(); i++) {
            vector[i] = values.get(i);
        }

        return vector;
    }

    private Set<String> tokenize(String text) {
        Set<String> terms = new LinkedHashSet<>();

        if (text == null || text.isBlank()) {
            return terms;
        }

        List<String> tokens = preprocessingPipeline.process(text);
        List<String> canonicalTerms = semanticPipeline.process(tokens);

        for (String part : canonicalTerms) {
            String term = normalize(part);
            if (!term.isBlank()) {
                terms.add(term);
            }
        }

        return terms;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "_");
    }

    private record FixtureData(
            Map<String, RankingService.DocumentVector> documentVectorsByCode,
            Map<String, RankingService.DocumentTerms> documentTermsByCode,
            Map<String, double[]> termVectors
    ) {
        List<RankingService.DocumentVector> documentVectors() {
            return new ArrayList<>(documentVectorsByCode.values());
        }

        List<RankingService.DocumentTerms> documentTerms() {
            return new ArrayList<>(documentTermsByCode.values());
        }

        DocumentSimilarityService.DocumentProfile documentProfile(String code) {
            RankingService.DocumentVector vector = documentVectorsByCode.get(code);
            RankingService.DocumentTerms terms = documentTermsByCode.get(code);

            if (vector == null || terms == null) {
                return null;
            }

            return new DocumentSimilarityService.DocumentProfile(
                    vector.code(),
                    vector.title(),
                    vector.vector(),
                    terms.terms()
            );
        }
    }

    public record QueryRun(
            String queryText,
            String metric,
            int topN,
            Set<String> queryTerms,
            double[] queryVector,
            List<RankingService.RankedDocument> results
    ) {
    }
}

