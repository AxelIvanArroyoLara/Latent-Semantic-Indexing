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
import java.util.stream.Collectors;

public class CommandLineInterface {

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
        this.queryProcessor = new QueryProcessor(indexingService.indexQueryFixtures());
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
        int topTerms = Integer.parseInt(options.getOrDefault("terms", "10"));
        int topDocuments = Integer.parseInt(options.getOrDefault("top", "5"));
        boolean fullMatrix = "full".equalsIgnoreCase(options.getOrDefault("matrix", "preview"));

        IndexingService.IndexedCorpus corpus = indexingService.indexQueryFixtures(
                AppConfig.QUERY_FIXTURE_DIR,
                dimensions
        );
        QueryProcessor processor = new QueryProcessor(corpus);
        FrequencyMatrix matrix = corpus.frequencyMatrix();
        LatentSpaceModel model = corpus.latentSpaceModel();
        List<TermSelectionService.SelectedTerm> expertTerms =
                resolveExpertTerms(model, topTerms, options.get("expert-terms"));

        printSection("FINAL PROJECT TECHNICAL DEMONSTRATION");
        System.out.println("Demonstrated objective: document base with LSI to index, represent, and query documents.");
        System.out.println("Corpus domain: mental health and wellbeing among university students.");
        System.out.println("Flow: documents -> preprocessing -> semantics -> FrecT -> SVD/LSI -> queries.");

        printSection("STEP 1. DOCUMENT BASE");
        System.out.println("Requirement: work with a base of at least 10 documents.");
        System.out.println("Indexed documents: " + corpus.semanticDocuments().size());
        for (SemanticDocument document : corpus.semanticDocuments()) {
            String title = corpus.titlesByCode().getOrDefault(document.code(), document.code());
            System.out.printf(
                    "%s - %s%n    Canonical terms: %s%n",
                    document.code(),
                    title,
                    document.canonicalTerms()
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
        System.out.println("FrecT dimensions: " + matrix.values().length + " terms x " + matrix.documentCodes().size() + " documents.");
        if (fullMatrix) {
            printFrequencyMatrix(matrix);
        } else {
            printFrequencyMatrixPreview(matrix, 12);
            System.out.println("Note: to print the full FrecT matrix use: final-demo --matrix full");
        }

        printSection("STEP 4. SVD / LSI REDUCTION");
        System.out.println("Requirement: apply SVD and allow significant terms to be selected.");
        System.out.println("Requested k: " + dimensions);
        System.out.println("Actual k used by LSI: " + model.k());
        System.out.println("Singular values:");
        for (int i = 0; i < model.singularValues().length; i++) {
            System.out.printf("  sigma_%d = %.4f%n", i + 1, model.singularValues()[i]);
        }
        printSelectedTerms(model, topTerms);
        printExpertTerms(expertTerms);
        persistIndexedCorpus(corpus, expertTerms);

        printSection("STEP 5. QUERY 1 - SIMILARITY BETWEEN TWO DOCUMENTS");
        System.out.println("Requested question: given D1 and D2, evaluate their degree of similarity.");
        System.out.println("Executed query: compare D1 against D3.");
        printComparison(processor.compareDocuments("D1", "D3"));

        printSection("STEP 6. QUERY 2 - RETRIEVE TOP-N WITH SIMILARITY FUNCTIONS");
        System.out.println("Requested question: given a query Q, retrieve the n most relevant documents.");
        System.out.println("Similarity function 1: cosine.");
        QueryProcessor.QueryRun cosineRun = processor.query("academic stress anxiety", topDocuments, "cosine");
        printQueryRun(cosineRun);
        persistQueryRun(cosineRun);
        System.out.println();
        System.out.println("Similarity function 2: Jaccard.");
        QueryProcessor.QueryRun jaccardRun = processor.query("sleep wellbeing", topDocuments, "jaccard");
        printQueryRun(jaccardRun);
        persistQueryRun(jaccardRun);

        printSection("STEP 7. QUERY 3 - RETRIEVE TOP-N WITH A DISSIMILARITY FUNCTION");
        System.out.println("Dissimilarity function: Euclidean distance.");
        QueryProcessor.QueryRun euclideanRun = processor.query("academic stress", topDocuments, "euclidean");
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
        int topTerms = Integer.parseInt(options.getOrDefault("top", "10"));
        String explicitTerms = options.get("terms");

        IndexingService.IndexedCorpus corpus = indexingService.indexQueryFixtures(
                AppConfig.QUERY_FIXTURE_DIR,
                dimensions
        );
        List<TermSelectionService.SelectedTerm> expertTerms =
                resolveExpertTerms(corpus.latentSpaceModel(), topTerms, explicitTerms);

        printSection("EXPERT INDEXING TERM SELECTION");
        printExpertTerms(expertTerms);
        persistIndexedCorpus(corpus, expertTerms);

        return 0;
    }

    private List<TermSelectionService.SelectedTerm> resolveExpertTerms(
            LatentSpaceModel model,
            int topTerms,
            String explicitTerms
    ) {
        List<TermSelectionService.SelectedTerm> rankedTerms =
                termSelectionService.selectTopTerms(model, Math.max(topTerms, model.terms().size()));

        if (explicitTerms == null || explicitTerms.isBlank()) {
            return rankedTerms.stream()
                    .limit(topTerms)
                    .toList();
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

    private void printExpertTerms(List<TermSelectionService.SelectedTerm> expertTerms) {
        System.out.println();
        System.out.println("Expert-selected indexing terms to persist:");

        int position = 1;
        for (TermSelectionService.SelectedTerm term : expertTerms) {
            System.out.printf(
                    "%d. %s - significance: %.4f%n",
                    position,
                    term.term(),
                    term.significance()
            );
            position++;
        }
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
                            + "Run Flyway and sql/demo/insert_demo_data.sql, then execute again."
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
                    "Persistence not completed: PostgreSQL is unavailable or the demo data is not loaded. "
                            + "Run Flyway and sql/demo/insert_demo_data.sql, then execute again."
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

    private void printFrequencyMatrixPreview(FrequencyMatrix matrix, int maxTerms) {
        System.out.println("FrecT preview with the first " + maxTerms + " terms.");
        System.out.println("Columns: " + matrix.documentCodes());
        System.out.printf("%-24s", "term");

        for (String code : matrix.documentCodes()) {
            System.out.printf("%10s", code);
        }

        System.out.println();

        int limit = Math.min(maxTerms, matrix.terms().size());

        for (int row = 0; row < limit; row++) {
            System.out.printf("%-24s", matrix.terms().get(row));

            for (int col = 0; col < matrix.documentCodes().size(); col++) {
                System.out.printf("%10.3f", matrix.values()[row][col]);
            }

            System.out.println();
        }
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

        IndexingService.IndexedCorpus corpus = indexingService.indexQueryFixtures(
                AppConfig.QUERY_FIXTURE_DIR,
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

        LatentSpaceModel model = indexingService.indexQueryFixtures(
                AppConfig.QUERY_FIXTURE_DIR,
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
                : termSelectionService.selectTopTerms(model, topTerms)) {
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
            queryProcessor = new QueryProcessor(indexingService.indexQueryFixtures());
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
        System.out.println("  final-demo --k 3 --terms 10 --top 5 --matrix preview");
        System.out.println("  final-demo --k 3 --terms 10 --top 5 --matrix full");
        System.out.println("  final-demo --expert-terms depression,anxiety,social_media");
        System.out.println("  inspect-lsi --k 3 --terms 10");
        System.out.println("  select-terms --terms depression,anxiety --k 3");
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

