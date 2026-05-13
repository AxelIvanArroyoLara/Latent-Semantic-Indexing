package com.lsi.ui;

import java.util.LinkedHashMap;
import java.util.Map;

import com.lsi.config.AppConfig;
import com.lsi.indexing.IndexingService;
import com.lsi.lsi.LsiInspector;
import com.lsi.lsi.TermSelectionService;
import com.lsi.model.FrequencyMatrix;
import com.lsi.model.LatentSpaceModel;
import com.lsi.model.SemanticDocument;
import com.lsi.persistence.FileLatentModelRepository;
import com.lsi.query.DocumentSimilarityService;
import com.lsi.query.QueryProcessor;
import com.lsi.query.RankingService;
import com.lsi.persistence.DocumentRepository;
import com.lsi.persistence.QueryLogRepository;

import com.lsi.persistence.CorpusPersistenceService;

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

            if ("import-pdfs-db".equalsIgnoreCase(command)) {
                return runImportPdfsToDatabase(options);
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
        String source = options.getOrDefault("source", "pdf");
        boolean useDatabase = "db".equalsIgnoreCase(source) || "database".equalsIgnoreCase(source);

        IndexingService.IndexedCorpus corpus = useDatabase
                ? indexingService.indexFromDatabase(dimensions)
                : indexingService.indexQueryFixtures(dimensions);
        QueryProcessor processor = new QueryProcessor(corpus);
        FrequencyMatrix matrix = corpus.frequencyMatrix();
        LatentSpaceModel model = corpus.latentSpaceModel();

        boolean persist = Boolean.parseBoolean(options.getOrDefault("persist", "true"));

        if (useDatabase && persist) {
            CorpusPersistenceService persistenceService = new CorpusPersistenceService();

            CorpusPersistenceService.PersistenceSummary summary =
                    persistenceService.persist(
                            corpus,
                            termSelectionService.selectTopTerms(model, topTerms)
                    );

            System.out.println("PostgreSQL persistence completed:");
            System.out.println("  documents: " + summary.documents());
            System.out.println("  terms: " + summary.terms());
            System.out.println("  frequency rows: " + summary.frequencyRows());
            System.out.println("  latent model id: " + summary.latentModelId());
            System.out.println("  selected terms: " + summary.selectedTerms());
        }

        printSection("FINAL PROJECT TECHNICAL DEMONSTRATION");
        System.out.println("Demonstrated objective: document base with LSI to index, represent, and query documents.");
        System.out.println("Corpus domain: mental health and wellbeing among university students.");
        System.out.println("Flow: documents -> preprocessing -> semantics -> FrecT -> SVD/LSI -> queries.");

        printSection("STEP 1. DOCUMENT BASE");
        System.out.println("Requirement: work with a base of at least 10 documents.");
        System.out.println("Source: " + (useDatabase ? "PostgreSQL database" : "processing 15 PDF documents from data/raw directory"));
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

        printSection("STEP 5. QUERY 1 - SIMILARITY BETWEEN TWO DOCUMENTS");
        System.out.println("Requested question: given D1 and D2, evaluate their degree of similarity.");
        System.out.println("Executed query: compare D1 against D3.");
        printComparison(processor.compareDocuments("D1", "D3"));

        printSection("STEP 6. QUERY 2 - RETRIEVE TOP-N WITH SIMILARITY FUNCTIONS");
        System.out.println("Requested question: given a query Q, retrieve the n most relevant documents.");
        System.out.println("Similarity function 1: cosine.");
        printQueryRun(processor.query("academic stress anxiety", topDocuments, "cosine"));
        System.out.println();
        System.out.println("Similarity function 2: Jaccard.");
        printQueryRun(processor.query("sleep wellbeing", topDocuments, "jaccard"));

        printSection("STEP 7. QUERY 3 - RETRIEVE TOP-N WITH A DISSIMILARITY FUNCTION");
        System.out.println("Dissimilarity function: Euclidean distance.");
        printQueryRun(processor.query("academic stress", topDocuments, "euclidean"));

        printSection("STEP 8. SQL VALIDATION AND PERSISTENCE");
        System.out.println("The PostgreSQL SQL version is available in these files:");
        System.out.println("  sql/queries/11_compare_documents_similarity.sql");
        System.out.println("  sql/queries/12_rank_query_topn_cosine.sql");
        System.out.println("  sql/queries/13_rank_query_topn_jaccard.sql");
        System.out.println("  sql/queries/14_rank_query_topn_euclidean.sql");
        System.out.println("Final report: docs/final-technical-report.docx");

        return 0;
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
        String source = options.getOrDefault("source", "pdf");
        boolean useDatabase = "db".equalsIgnoreCase(source) || "database".equalsIgnoreCase(source);

        IndexingService.IndexedCorpus corpus = useDatabase
                ? indexingService.indexFromDatabase(dimensions)
                : indexingService.indexQueryFixtures(dimensions);
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
        String source = options.getOrDefault("source", "pdf");
        boolean useDatabase = "db".equalsIgnoreCase(source) || "database".equalsIgnoreCase(source);

        IndexingService.IndexedCorpus corpus = useDatabase
                ? indexingService.indexFromDatabase(dimensions)
                : indexingService.indexQueryFixtures(dimensions);

        LatentSpaceModel model = corpus.latentSpaceModel();

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

        String source = options.getOrDefault("source", "pdf");
        boolean useDatabase = "db".equalsIgnoreCase(source) || "database".equalsIgnoreCase(source);

        IndexingService.IndexedCorpus corpus = useDatabase
                ? indexingService.indexFromDatabase(AppConfig.DEFAULT_LSI_DIMENSIONS)
                : indexingService.indexQueryFixtures();

        QueryProcessor processor = new QueryProcessor(corpus);
        QueryProcessor.QueryRun run = processor.query(text, top, metric);

        boolean persist = Boolean.parseBoolean(options.getOrDefault("persist", "false"));

        if (useDatabase && persist) {
            persistQueryRun(run);
        }

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

        return 0;
    }
    // Integración final de importación de pdfs para poder pasarlo a la base de datos
    private int runImportPdfsToDatabase(Map<String, String> options) {
        boolean reset = Boolean.parseBoolean(options.getOrDefault("reset", "true"));

        int count = indexingService.importRawPdfsToDatabase(reset);

        System.out.println("Imported PDF documents into PostgreSQL: " + count);
        System.out.println("Source directory: " + AppConfig.RAW_DOCUMENT_DIR);
        System.out.println("Reset existing documents: " + reset);

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

    private void persistQueryRun(QueryProcessor.QueryRun run) {
        QueryLogRepository queryLogRepository = new QueryLogRepository();
        DocumentRepository documentRepository = new DocumentRepository();

        long queryRunId = queryLogRepository.saveQueryRun(
                run.queryText(),
                run.metric(),
                run.topN()
        );

        int rank = 1;

        for (RankingService.RankedDocument document : run.results()) {
            long documentId = documentRepository.findByCode(document.code())
                    .orElseThrow(() -> new IllegalStateException(
                            "Document not found while persisting query result: " + document.code()
                    ))
                    .documentId();

            queryLogRepository.saveQueryResult(
                    queryRunId,
                    rank,
                    documentId,
                    document.score()
            );

            rank++;
        }

        System.out.println("Query persistence completed:");
        System.out.println("  query_run_id: " + queryRunId);
        System.out.println("  results: " + run.results().size());
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
        System.out.println("  inspect-lsi --k 3 --terms 10");
        System.out.println("  query --text \"academic stress anxiety\" --top 5 --metric cosine");
        System.out.println("  query --text \"sleep wellbeing\" --top 3 --metric jaccard");
        System.out.println("  query --text \"academic stress\" --top 3 --metric euclidean");
        System.out.println("  import-pdfs-db --reset true");
        System.out.println();
        System.out.println("Optional flags for pipeline commands:");
        System.out.println("  --source db       Use the PostgreSQL document base instead of the local PDF corpus");
        System.out.println("  --source pdf      Use the local PDF corpus (default)");
        System.out.println();
        System.out.println("Supported metrics:");
        System.out.println("  cosine");
        System.out.println("  jaccard");
        System.out.println("  euclidean");
    }
}

