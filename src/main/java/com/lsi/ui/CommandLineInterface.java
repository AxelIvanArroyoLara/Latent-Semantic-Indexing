package com.lsi.ui;

import com.lsi.config.AppConfig;
import com.lsi.indexing.IndexingService;
import com.lsi.lsi.LsiInspector;
import com.lsi.lsi.TermSelectionService;
import com.lsi.model.FrequencyMatrix;
import com.lsi.model.LatentSpaceModel;
import com.lsi.model.SemanticDocument;
import com.lsi.persistence.CorpusPersistenceService;
import com.lsi.persistence.FileLatentModelRepository;
import com.lsi.persistence.QueryResultPersistenceService;
import com.lsi.query.DocumentSimilarityService;
import com.lsi.query.QueryProcessor;
import com.lsi.query.RankingService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandLineInterface {

    private static final List<String> DEFAULT_EXPERT_TERM_PREFERENCES = List.of(
            "student",
            "mental_health",
            "anxiety",
            "depression",
            "stress",
            "well",
            "wellbe",
            "academic",
            "support",
            "counseling",
            "sleep",
            "social_media",
            "health",
            "campu",
            "college"
    );
    private static final int DEFAULT_DISPLAY_TERMS = 10;
    private static final int DEFAULT_INDEX_TERMS = 150;
    private static final Set<String> DEFAULT_EXPERT_TERM_SET = Set.copyOf(DEFAULT_EXPERT_TERM_PREFERENCES);
    private static final Set<String> NON_INDEXING_TERMS = Set.of(
            "about",
            "above",
            "access",
            "address",
            "administration",
            "achf",
            "also",
            "american",
            "association",
            "author",
            "avoid",
            "avoidance",
            "bas",
            "belong",
            "below",
            "block",
            "break",
            "built",
            "cis",
            "content",
            "copyright",
            "eprint",
            "fig",
            "figure",
            "ijw",
            "license",
            "manuscript",
            "phda",
            "space",
            "spac",
            "table",
            "uthor",
            "whiterose"
    );

    private QueryProcessor queryProcessor;
    private final IndexingService indexingService;
    private final TermSelectionService termSelectionService;

    public CommandLineInterface() {
        this.indexingService = new IndexingService();
        this.termSelectionService = new TermSelectionService();
    }

    public CommandLineInterface(QueryProcessor queryProcessor) {
        this.queryProcessor = queryProcessor;
        this.indexingService = new IndexingService();
        this.termSelectionService = new TermSelectionService();
    }

    public CommandLineInterface(IndexingService indexingService) {
        this.indexingService = indexingService;
        this.queryProcessor = new QueryProcessor(indexingService.indexRawPdfDocuments(AppConfig.RAW_DOCUMENT_DIR, AppConfig.DEFAULT_LSI_DIMENSIONS));
        this.termSelectionService = new TermSelectionService();
    }

    public static void main(String[] args) {
        int exitCode = new CommandLineInterface().run(args);

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    public int run(String[] args) {
        if (args.length == 0) {
            return runFinalDemo(Map.of());
        }

        if ("help".equalsIgnoreCase(args[0])) {
            printHelp();
            return 0;
        }

        String command = args[0];
        Map<String, String> options = parseOptions(args);

        try {
            if ("compare-docs".equalsIgnoreCase(command)) {
                return runCompareDocs(options);
            }

            if ("query".equalsIgnoreCase(command)) {
                return runQuery(options);
            }

            if ("demo-pipeline".equalsIgnoreCase(command)) {
                return runDemoPipeline(options);
            }

            if ("inspect-lsi".equalsIgnoreCase(command)) {
                return runInspectLsi(options);
            }

            if ("final-demo".equalsIgnoreCase(command)) {
                return runFinalDemo(options);
            }

            if ("select-terms".equalsIgnoreCase(command)) {
                return runSelectTerms(options);
            }

            System.out.println("Unknown command: " + command);
            printHelp();
            return 1;
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
            return 1;
        }
    }

    private int runCompareDocs(Map<String, String> options) {
        String d1 = requireOption(options, "d1");
        String d2 = requireOption(options, "d2");

        DocumentSimilarityService.ComparisonResult result = queryProcessor().compareDocuments(d1, d2);

        System.out.println("=== Document comparison ===");
        System.out.println("D1: " + result.firstCode() + " - " + result.firstTitle());
        System.out.println("D2: " + result.secondCode() + " - " + result.secondTitle());
        System.out.println();
        System.out.printf("Cosine similarity: %.4f%n", result.cosine());
        System.out.printf("Jaccard similarity: %.4f%n", result.jaccard());
        System.out.printf("Euclidean distance: %.4f%n", result.euclidean());
        System.out.println();
        System.out.println("Note: higher cosine/Jaccard means more similar; lower Euclidean means closer.");

        return 0;
    }

    private int runFinalDemo(Map<String, String> options) {
        int dimensions = Integer.parseInt(
                options.getOrDefault("k", String.valueOf(AppConfig.DEFAULT_LSI_DIMENSIONS))
        );
        int displayTerms = Integer.parseInt(options.getOrDefault("terms", String.valueOf(DEFAULT_DISPLAY_TERMS)));
        int indexTerms = Integer.parseInt(options.getOrDefault("index-terms", String.valueOf(DEFAULT_INDEX_TERMS)));
        int topDocuments = Integer.parseInt(options.getOrDefault("top", "5"));
        boolean fullMatrix = "full".equalsIgnoreCase(options.getOrDefault("matrix", "preview"));

        IndexingService.IndexedCorpus sourceCorpus = indexingService.indexRawPdfDocuments(
                AppConfig.RAW_DOCUMENT_DIR,
                dimensions
        );
        LatentSpaceModel sourceModel = sourceCorpus.latentSpaceModel();
        Map<String, TermStats> termStats = buildTermStats(sourceCorpus);
        List<TermSelectionService.SelectedTerm> expertTerms =
                resolveExpertTerms(sourceModel, indexTerms, options.get("expert-terms"), termStats);
        IndexingService.IndexedCorpus corpus = indexingService.filterToSelectedTerms(
                sourceCorpus,
                expertTerms.stream().map(TermSelectionService.SelectedTerm::term).toList(),
                dimensions
        );
        QueryProcessor processor = new QueryProcessor(corpus);
        FrequencyMatrix matrix = corpus.frequencyMatrix();
        LatentSpaceModel model = corpus.latentSpaceModel();
        Set<String> activeIndexTerms = expertTerms.stream()
                .map(TermSelectionService.SelectedTerm::term)
                .collect(Collectors.toSet());

        printSection("FINAL PROJECT TECHNICAL DEMONSTRATION");
        System.out.println("Demonstrated objective: document base with LSI to index, represent, and query documents.");
        System.out.println("Corpus domain: mental health and wellbeing among university students.");
        System.out.println("Flow: PDFs -> text extraction -> preprocessing -> semantics -> SVD term selection -> filtered FrecT/LSI -> queries.");

        printSection("STEP 1. DOCUMENT BASE");
        System.out.println("Requirement: work with a base of at least 10 documents.");
        System.out.println("Source directory: " + AppConfig.RAW_DOCUMENT_DIR);
        System.out.println("Extracted PDF documents: " + sourceCorpus.semanticDocuments().size());
        for (SemanticDocument document : sourceCorpus.semanticDocuments()) {
            String title = sourceCorpus.titlesByCode().getOrDefault(document.code(), document.code());
            String source = String.valueOf(document.metadata().getOrDefault("source", ""));
            List<String> previewTerms = document.canonicalTerms()
                    .stream()
                    .filter(activeIndexTerms::contains)
                    .distinct()
                    .limit(14)
                    .toList();
            System.out.printf(
                    "%s - %s%n    Source PDF: %s%n    Active indexing term preview: %s%n",
                    document.code(),
                    title,
                    source,
                    previewTerms
            );
        }

        printSection("STEP 2. PREPROCESSING AND SEMANTICS");
        System.out.println("Requirement: consider stop list, suffix list, stems, synonyms, and polysemy.");
        System.out.println("Stop list: src/main/resources/stopwords.txt");
        System.out.println("Suffix list / stemming: src/main/resources/suffixes.txt");
        System.out.println("Synonyms: src/main/resources/synonyms.csv");
        System.out.println("Polysemy: src/main/resources/polysemy_rules.csv");
        System.out.println("Applied examples:");
        System.out.println("  worry -> anxiety");
        System.out.println("  therapy -> counseling");
        System.out.println("  university + support -> institutional_support");
        System.out.println("  mental + health -> mental_health");

        printSection("STEP 3. FREQUENCY MATRIX FrecT");
        System.out.println("Requirement: build the FrecT matrix generated from the document base.");
        System.out.println("This FrecT is rebuilt after expert term selection, so the active index keeps a broader selected vocabulary.");
        System.out.println("FrecT dimensions: " + matrix.values().length + " terms x " + matrix.documentCodes().size() + " documents.");
        if (fullMatrix) {
            printFrequencyMatrix(matrix);
        } else {
            printFrequencyMatrixPreview(matrix, expertTerms, 12);
            System.out.println("Note: to print the full FrecT matrix use: final-demo --matrix full");
        }

        printSection("STEP 4. SVD / LSI REDUCTION");
        System.out.println("Requirement: apply SVD and allow significant terms to be selected.");
        System.out.println("Requested k: " + dimensions);
        System.out.println("Initial PDF-corpus k used before term filtering: " + sourceModel.k());
        System.out.println("Active filtered-index k used after expert selection: " + model.k());
        System.out.println("Initial singular values used to rank significant terms:");
        for (int i = 0; i < sourceModel.singularValues().length; i++) {
            System.out.printf("  sigma_%d = %.4f%n", i + 1, sourceModel.singularValues()[i]);
        }
        printSelectedTerms(sourceModel, displayTerms);
        printExpertTerms(expertTerms, displayTerms);
        System.out.println("Active expert-filtered indexing vocabulary size: " + model.terms().size());
        System.out.println("Active indexing vocabulary preview: " + expertTerms.stream()
                .map(TermSelectionService.SelectedTerm::term)
                .limit(displayTerms)
                .toList());
        persistIndexedCorpus(corpus, expertTerms);

        printSection("STEP 5. QUERY 1 - SIMILARITY BETWEEN TWO DOCUMENTS");
        System.out.println("Requested question: given D1 and D2, evaluate their degree of similarity.");
        System.out.println("Executed query: compare D1 against D3.");
        printComparison(processor.compareDocuments("D1", "D3"));

        printSection("STEP 6. QUERY 2 - RETRIEVE TOP-N WITH SIMILARITY FUNCTIONS");
        System.out.println("Requested question: given a query Q, retrieve the n most relevant documents.");
        System.out.println("Similarity function 1: cosine.");
        String cosineQuery = queryTextFromExpertTerms(expertTerms, 0, 3);
        QueryProcessor.QueryRun cosineRun = processor.query(cosineQuery, topDocuments, "cosine");
        printQueryRun(cosineRun);
        persistQueryRun(cosineRun);
        System.out.println();
        System.out.println("Similarity function 2: Jaccard.");
        String jaccardQuery = queryTextFromExpertTerms(expertTerms, 3, 3);
        QueryProcessor.QueryRun jaccardRun = processor.query(jaccardQuery, topDocuments, "jaccard");
        printQueryRun(jaccardRun);
        persistQueryRun(jaccardRun);

        printSection("STEP 7. QUERY 3 - RETRIEVE TOP-N WITH A DISSIMILARITY FUNCTION");
        System.out.println("Dissimilarity function: Euclidean distance.");
        String euclideanQuery = queryTextFromExpertTerms(expertTerms, 0, 2);
        QueryProcessor.QueryRun euclideanRun = processor.query(euclideanQuery, topDocuments, "euclidean");
        printQueryRun(euclideanRun);
        persistQueryRun(euclideanRun);

        printSection("STEP 8. SQL VALIDATION AND PERSISTENCE");
        System.out.println("The PostgreSQL SQL version is available in these files:");
        System.out.println("  sql/queries/11_compare_documents_similarity.sql");
        System.out.println("  sql/queries/12_rank_query_topn_cosine.sql");
        System.out.println("  sql/queries/13_rank_query_topn_jaccard.sql");
        System.out.println("  sql/queries/14_rank_query_topn_euclidean.sql");
        System.out.println("Final report: docs/final-technical-report.docx");

        return 0;
    }

    private int runSelectTerms(Map<String, String> options) {
        int dimensions = Integer.parseInt(
                options.getOrDefault("k", String.valueOf(AppConfig.DEFAULT_LSI_DIMENSIONS))
        );
        int displayTerms = Integer.parseInt(options.getOrDefault("top", String.valueOf(DEFAULT_DISPLAY_TERMS)));
        int indexTerms = Integer.parseInt(options.getOrDefault("index-terms", String.valueOf(DEFAULT_INDEX_TERMS)));
        String explicitTerms = options.get("terms");

        IndexingService.IndexedCorpus sourceCorpus = indexingService.indexRawPdfDocuments(
                AppConfig.RAW_DOCUMENT_DIR,
                dimensions
        );
        Map<String, TermStats> termStats = buildTermStats(sourceCorpus);
        List<TermSelectionService.SelectedTerm> expertTerms =
                resolveExpertTerms(sourceCorpus.latentSpaceModel(), indexTerms, explicitTerms, termStats);
        IndexingService.IndexedCorpus corpus = indexingService.filterToSelectedTerms(
                sourceCorpus,
                expertTerms.stream().map(TermSelectionService.SelectedTerm::term).toList(),
                dimensions
        );

        printSection("EXPERT INDEXING TERM SELECTION");
        printExpertTerms(expertTerms, displayTerms);
        System.out.println("Active expert-filtered indexing vocabulary size: " + corpus.latentSpaceModel().terms().size());
        System.out.println("Active indexing vocabulary preview: " + expertTerms.stream()
                .map(TermSelectionService.SelectedTerm::term)
                .limit(displayTerms)
                .toList());
        persistIndexedCorpus(corpus, expertTerms);

        return 0;
    }

    private String queryTextFromExpertTerms(
            List<TermSelectionService.SelectedTerm> expertTerms,
            int start,
            int count
    ) {
        if (expertTerms.isEmpty()) {
            return "";
        }

        int safeStart = Math.min(start, Math.max(0, expertTerms.size() - 1));
        int safeEnd = Math.min(expertTerms.size(), safeStart + count);

        if (safeStart >= safeEnd) {
            safeStart = 0;
            safeEnd = Math.min(expertTerms.size(), count);
        }

        return expertTerms.subList(safeStart, safeEnd)
                .stream()
                .map(TermSelectionService.SelectedTerm::term)
                .map(term -> term.replace('_', ' '))
                .collect(Collectors.joining(" "));
    }

    private List<TermSelectionService.SelectedTerm> resolveExpertTerms(
            LatentSpaceModel model,
            int topTerms,
            String explicitTerms,
            Map<String, TermStats> termStats
    ) {
        List<TermSelectionService.SelectedTerm> rankedTerms =
                termSelectionService.selectTopTerms(model, Math.max(topTerms, model.terms().size()));

        if (explicitTerms == null || explicitTerms.isBlank()) {
            List<TermSelectionService.SelectedTerm> selectedTerms = new ArrayList<>();

            for (String preferredTerm : DEFAULT_EXPERT_TERM_PREFERENCES) {
                rankedTerms.stream()
                        .filter(term -> term.term().equalsIgnoreCase(preferredTerm))
                        .findFirst()
                        .filter(term -> !selectedTerms.contains(term))
                        .ifPresent(selectedTerms::add);

                if (selectedTerms.size() == topTerms) {
                    return selectedTerms;
                }
            }

            for (TermSelectionService.SelectedTerm rankedTerm : rankedTerms) {
                if (isUsableIndexTerm(rankedTerm.term(), termStats) && !selectedTerms.contains(rankedTerm)) {
                    selectedTerms.add(rankedTerm);
                }

                if (selectedTerms.size() == topTerms) {
                    break;
                }
            }

            return selectedTerms;
        }

        Set<String> requestedTerms = List.of(explicitTerms.split(","))
                .stream()
                .map(String::trim)
                .filter(term -> !term.isBlank())
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

        List<TermSelectionService.SelectedTerm> selectedTerms = new ArrayList<>();

        for (String requested : requestedTerms) {
            rankedTerms.stream()
                    .filter(term -> term.term().equalsIgnoreCase(requested))
                    .findFirst()
                    .ifPresent(selectedTerms::add);
        }

        if (selectedTerms.isEmpty()) {
            throw new IllegalArgumentException("None of the requested expert terms exist in the LSI vocabulary");
        }

        return selectedTerms;
    }

    private boolean isUsableIndexTerm(String term) {
        return isUsableIndexTerm(term, Map.of());
    }

    private boolean isUsableIndexTerm(String term, Map<String, TermStats> termStats) {
        if (term == null || term.isBlank()) {
            return false;
        }

        String normalized = term.toLowerCase();

        if (DEFAULT_EXPERT_TERM_SET.contains(normalized)) {
            return true;
        }

        if (NON_INDEXING_TERMS.contains(normalized)) {
            return false;
        }

        if (normalized.length() < 4) {
            return false;
        }

        if (normalized.matches(".*\\d.*")) {
            return false;
        }

        if (!normalized.matches(".*[aeiou].*")) {
            return false;
        }

        TermStats stats = termStats.get(normalized);

        if (stats == null) {
            return true;
        }

        if (stats.documentFrequency() < 2 || stats.totalFrequency() < 4) {
            return false;
        }

        if (stats.maxDocumentShare() > 0.80) {
            return false;
        }

        return !stats.acronymLike() && !stats.properNameLike();
    }

    private Map<String, TermStats> buildTermStats(IndexingService.IndexedCorpus corpus) {
        Map<String, Integer> totalFrequencies = new LinkedHashMap<>();
        Map<String, Integer> documentFrequencies = new LinkedHashMap<>();
        Map<String, Integer> maxDocumentFrequencies = new LinkedHashMap<>();
        Map<String, Boolean> acronymLike = new LinkedHashMap<>();
        Map<String, Boolean> properNameLike = new LinkedHashMap<>();

        for (SemanticDocument document : corpus.semanticDocuments()) {
            Map<String, Long> counts = document.canonicalTerms()
                    .stream()
                    .collect(Collectors.groupingBy(term -> term, LinkedHashMap::new, Collectors.counting()));
            String rawText = String.valueOf(document.metadata().getOrDefault("rawText", ""));

            for (Map.Entry<String, Long> entry : counts.entrySet()) {
                String term = entry.getKey();
                int count = entry.getValue().intValue();

                totalFrequencies.merge(term, count, Integer::sum);
                documentFrequencies.merge(term, 1, Integer::sum);
                maxDocumentFrequencies.merge(term, count, Math::max);
                acronymLike.merge(term, isUppercaseAcronymInRawText(term, rawText), Boolean::logicalOr);
                properNameLike.merge(term, isProperNameInRawText(term, rawText), Boolean::logicalOr);
            }
        }

        Map<String, TermStats> result = new LinkedHashMap<>();

        for (String term : totalFrequencies.keySet()) {
            int total = totalFrequencies.get(term);
            int max = maxDocumentFrequencies.getOrDefault(term, 0);
            double maxShare = total == 0 ? 0.0 : (double) max / total;

            result.put(term, new TermStats(
                    documentFrequencies.getOrDefault(term, 0),
                    total,
                    maxShare,
                    acronymLike.getOrDefault(term, false),
                    properNameLike.getOrDefault(term, false)
            ));
        }

        return result;
    }

    private boolean isUppercaseAcronymInRawText(String term, String rawText) {
        if (term.length() < 3 || term.length() > 6 || term.contains("_")) {
            return false;
        }

        String uppercase = Pattern.quote(term.toUpperCase());
        String lowercase = Pattern.quote(term.toLowerCase());

        return Pattern.compile("\\b" + uppercase + "\\b").matcher(rawText).find()
                && !Pattern.compile("\\b" + lowercase + "\\b").matcher(rawText).find();
    }

    private boolean isProperNameInRawText(String term, String rawText) {
        if (term.length() < 4 || term.contains("_")) {
            return false;
        }

        String titleCase = Pattern.quote(Character.toUpperCase(term.charAt(0)) + term.substring(1));
        String lowercase = Pattern.quote(term.toLowerCase());

        return Pattern.compile("\\b" + titleCase + "\\b").matcher(rawText).find()
                && !Pattern.compile("\\b" + lowercase + "\\b").matcher(rawText).find();
    }

    private void printExpertTerms(List<TermSelectionService.SelectedTerm> expertTerms, int displayTerms) {
        System.out.println();
        System.out.println("Expert-selected indexing terms shown for review:");

        int position = 1;
        for (TermSelectionService.SelectedTerm term : expertTerms.stream().limit(displayTerms).toList()) {
            System.out.printf(
                    "%d. %s - significance: %.4f%n",
                    position,
                    term.term(),
                    term.significance()
            );
            position++;
        }

        System.out.println("Total expert-selected terms used by the active index: " + expertTerms.size());
    }

    private void persistIndexedCorpus(
            IndexingService.IndexedCorpus corpus,
            List<TermSelectionService.SelectedTerm> expertTerms
    ) {
        try {
            CorpusPersistenceService.PersistenceSummary summary =
                    new CorpusPersistenceService().persist(corpus, expertTerms);

            System.out.printf(
                    "Persisted corpus: %d documents, %d terms, %d FrecT rows, LSI model %d, %d expert terms.%n",
                    summary.documents(),
                    summary.terms(),
                    summary.frequencyRows(),
                    summary.latentModelId(),
                    summary.selectedTerms()
            );
        } catch (RuntimeException e) {
            System.out.println(
                    "Corpus persistence not completed: PostgreSQL is unavailable or migrations are not applied. "
                            + "Run Flyway, make sure PostgreSQL is available, then execute the PDF demo again."
            );
        }
    }

    private void persistQueryRun(QueryProcessor.QueryRun run) {
        try {
            QueryResultPersistenceService.PersistenceSummary summary =
                    new QueryResultPersistenceService().persist(run);

            System.out.printf(
                    "Persisted query run %d with %d ranked results.%n",
                    summary.queryRunId(),
                    summary.persistedResults()
            );
        } catch (RuntimeException e) {
            System.out.println(
                    "Persistence not completed: PostgreSQL is unavailable or migrations are not applied. "
                            + "Run Flyway, make sure PostgreSQL is available, then execute the PDF demo again."
            );
        }
    }

    private void printSection(String title) {
        System.out.println();
        System.out.println("============================================================");
        System.out.println(title);
        System.out.println("============================================================");
    }

    private void printFrequencyMatrix(FrequencyMatrix matrix) {
        System.out.println("Rows: terms. Columns: documents.");
        System.out.println("Documents: " + matrix.documentCodes());
        System.out.printf("%-24s", "term");

        for (String code : matrix.documentCodes()) {
            System.out.printf("%10s", code);
        }

        System.out.println();

        for (int row = 0; row < matrix.terms().size(); row++) {
            System.out.printf("%-24s", matrix.terms().get(row));

            for (int col = 0; col < matrix.documentCodes().size(); col++) {
                System.out.printf("%10.3f", matrix.values()[row][col]);
            }

            System.out.println();
        }
    }

    private void printFrequencyMatrixPreview(
            FrequencyMatrix matrix,
            List<TermSelectionService.SelectedTerm> selectedTerms,
            int maxTerms
    ) {
        System.out.println("FrecT preview with " + maxTerms + " expert-selected terms.");
        System.out.println("Columns: " + matrix.documentCodes());
        System.out.printf("%-24s", "term");

        for (String code : matrix.documentCodes()) {
            System.out.printf("%10s", code);
        }

        System.out.println();

        Map<String, Integer> rowByTerm = new LinkedHashMap<>();

        for (int row = 0; row < matrix.terms().size(); row++) {
            rowByTerm.put(matrix.terms().get(row), row);
        }

        List<String> previewTerms = selectedTerms.stream()
                .map(TermSelectionService.SelectedTerm::term)
                .filter(rowByTerm::containsKey)
                .limit(maxTerms)
                .toList();

        for (String term : previewTerms) {
            int row = rowByTerm.get(term);
            System.out.printf("%-24s", term);

            for (int col = 0; col < matrix.documentCodes().size(); col++) {
                System.out.printf("%10.3f", matrix.values()[row][col]);
            }

            System.out.println();
        }
    }

    private record TermStats(
            int documentFrequency,
            int totalFrequency,
            double maxDocumentShare,
            boolean acronymLike,
            boolean properNameLike
    ) {
    }

    private void printComparison(DocumentSimilarityService.ComparisonResult result) {
        System.out.println("Given two documents D1 and D2, evaluate their degree of similarity.");
        System.out.println("D1: " + result.firstCode() + " - " + result.firstTitle());
        System.out.println("D2: " + result.secondCode() + " - " + result.secondTitle());
        System.out.printf("Cosine similarity: %.4f%n", result.cosine());
        System.out.printf("Jaccard similarity: %.4f%n", result.jaccard());
        System.out.printf("Euclidean distance: %.4f%n", result.euclidean());
    }

    private void printQueryRun(QueryProcessor.QueryRun run) {
        System.out.println("Query: " + run.queryText());
        System.out.println("Metric: " + run.metric());
        System.out.println("Top N: " + run.topN());
        System.out.println("Terms used: " + run.queryTerms());

        int position = 1;
        for (RankingService.RankedDocument document : run.results()) {
            System.out.printf(
                    "%d. %s - %s - score: %.4f%n",
                    position,
                    document.code(),
                    document.title(),
                    document.score()
            );
            position++;
        }

        if ("euclidean".equalsIgnoreCase(run.metric())) {
            System.out.println("For Euclidean distance, lower score means closer.");
        }
    }

    private int runDemoPipeline(Map<String, String> options) {
        int dimensions = Integer.parseInt(
                options.getOrDefault("k", String.valueOf(AppConfig.DEFAULT_LSI_DIMENSIONS))
        );

        IndexingService.IndexedCorpus corpus = indexingService.indexRawPdfDocuments(
                AppConfig.RAW_DOCUMENT_DIR,
                dimensions
        );
        FrequencyMatrix matrix = corpus.frequencyMatrix();
        LatentSpaceModel model = corpus.latentSpaceModel();

        if (Boolean.parseBoolean(options.getOrDefault("save-model", "false"))) {
            new FileLatentModelRepository(AppConfig.DEFAULT_MODEL_FILE.toString())
                    .save(model);
        }

        System.out.println("=== Integrated LSI pipeline ===");
        System.out.println("Documents indexed: " + corpus.semanticDocuments().size());
        System.out.println("Vocabulary terms: " + matrix.terms().size());
        System.out.println("Frequency matrix: " + matrix.values().length + "x" + matrix.documentCodes().size());
        System.out.println("LSI dimensions: " + model.k());
        System.out.println("Document vectors: " + model.documentVectors().length);
        printSelectedTerms(model, Integer.parseInt(options.getOrDefault("terms", "8")));

        if (Boolean.parseBoolean(options.getOrDefault("save-model", "false"))) {
            System.out.println("Model saved to: " + AppConfig.DEFAULT_MODEL_FILE);
        }

        return 0;
    }

    private int runInspectLsi(Map<String, String> options) {
        int dimensions = Integer.parseInt(
                options.getOrDefault("k", String.valueOf(AppConfig.DEFAULT_LSI_DIMENSIONS))
        );
        int topTerms = Integer.parseInt(options.getOrDefault("terms", "10"));

        LatentSpaceModel model = indexingService.indexRawPdfDocuments(
                AppConfig.RAW_DOCUMENT_DIR,
                dimensions
        ).latentSpaceModel();

        System.out.println(LsiInspector.inspect(model));
        printSelectedTerms(model, topTerms);

        return 0;
    }

    private void printSelectedTerms(LatentSpaceModel model, int topTerms) {
        System.out.println();
        System.out.println("Top indexing terms for expert review:");

        int position = 1;

        for (TermSelectionService.SelectedTerm term
                : termSelectionService.selectTopTerms(model, Math.max(topTerms, model.terms().size()))
                .stream()
                .filter(selectedTerm -> isUsableIndexTerm(selectedTerm.term()))
                .limit(topTerms)
                .toList()) {
            System.out.printf(
                    "%d. %s - significance: %.4f%n",
                    position,
                    term.term(),
                    term.significance()
            );
            position++;
        }
    }

    private int runQuery(Map<String, String> options) {
        String text = requireOption(options, "text");
        int top = Integer.parseInt(options.getOrDefault("top", "5"));
        String metric = options.getOrDefault("metric", "cosine");

        QueryProcessor.QueryRun run = queryProcessor().query(text, top, metric);

        System.out.println("=== Query ranking ===");
        System.out.println("Query: " + run.queryText());
        System.out.println("Metric: " + run.metric());
        System.out.println("Top N: " + run.topN());
        System.out.println("Terms used: " + run.queryTerms());
        System.out.println();

        int position = 1;

        for (RankingService.RankedDocument document : run.results()) {
            System.out.printf(
                    "%d. %s - %s - score: %.4f%n",
                    position,
                    document.code(),
                    document.title(),
                    document.score()
            );
            position++;
        }

        if ("euclidean".equalsIgnoreCase(run.metric())) {
            System.out.println();
            System.out.println("Note: for Euclidean distance, lower score means closer.");
        }

        System.out.println();
        persistQueryRun(run);

        return 0;
    }

    private Map<String, String> parseOptions(String[] args) {
        Map<String, String> options = new LinkedHashMap<>();

        for (int i = 1; i < args.length; i++) {
            String current = args[i];

            if (!current.startsWith("--")) {
                continue;
            }

            String key = current.substring(2);

            if (i + 1 >= args.length || args[i + 1].startsWith("--")) {
                options.put(key, "true");
            } else {
                options.put(key, args[i + 1]);
                i++;
            }
        }

        return options;
    }

    private String requireOption(Map<String, String> options, String key) {
        String value = options.get(key);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required option --" + key);
        }

        return value;
    }

    private QueryProcessor queryProcessor() {
        if (queryProcessor == null) {
            queryProcessor = new QueryProcessor(indexingService.indexRawPdfDocuments(
                    AppConfig.RAW_DOCUMENT_DIR,
                    AppConfig.DEFAULT_LSI_DIMENSIONS
            ));
        }

        return queryProcessor;
    }

    private void printHelp() {
        System.out.println("Latent Semantic Indexing demo CLI");
        System.out.println("Running without arguments prints the guided final project demo.");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  compare-docs --d1 D1 --d2 D3");
        System.out.println("  demo-pipeline --k 3 --terms 8 --save-model true");
        System.out.println("  final-demo --k 3 --terms 10 --index-terms 150 --top 5 --matrix preview");
        System.out.println("  final-demo --k 3 --terms 10 --index-terms 150 --top 5 --matrix full");
        System.out.println("  final-demo --expert-terms depression,anxiety,social_media");
        System.out.println("  inspect-lsi --k 3 --terms 10");
        System.out.println("  select-terms --terms depression,anxiety --k 3");
        System.out.println("  select-terms --top 10 --index-terms 150 --k 3");
        System.out.println("  query --text \"academic stress anxiety\" --top 5 --metric cosine");
        System.out.println("  query --text \"sleep wellbeing\" --top 3 --metric jaccard");
        System.out.println("  query --text \"academic stress\" --top 3 --metric euclidean");
        System.out.println();
        System.out.println("Supported metrics:");
        System.out.println("  cosine");
        System.out.println("  jaccard");
        System.out.println("  euclidean");
    }
}

