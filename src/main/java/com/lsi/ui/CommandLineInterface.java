package com.lsi.ui;

import com.lsi.config.AppConfig;
import com.lsi.indexing.IndexingService;
import com.lsi.model.FrequencyMatrix;
import com.lsi.model.LatentSpaceModel;
import com.lsi.persistence.FileLatentModelRepository;
import com.lsi.query.DocumentSimilarityService;
import com.lsi.query.QueryProcessor;
import com.lsi.query.RankingService;

import java.util.LinkedHashMap;
import java.util.Map;

public class CommandLineInterface {

    private final QueryProcessor queryProcessor;
    private final IndexingService indexingService;

    public CommandLineInterface() {
        this(new IndexingService());
    }

    public CommandLineInterface(QueryProcessor queryProcessor) {
        this.queryProcessor = queryProcessor;
        this.indexingService = new IndexingService();
    }

    public CommandLineInterface(IndexingService indexingService) {
        this.indexingService = indexingService;
        this.queryProcessor = new QueryProcessor(indexingService.indexQueryFixtures());
    }

    public static void main(String[] args) {
        int exitCode = new CommandLineInterface().run(args);

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    public int run(String[] args) {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
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

        DocumentSimilarityService.ComparisonResult result = queryProcessor.compareDocuments(d1, d2);

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

        if (Boolean.parseBoolean(options.getOrDefault("save-model", "false"))) {
            System.out.println("Model saved to: " + AppConfig.DEFAULT_MODEL_FILE);
        }

        return 0;
    }

    private int runQuery(Map<String, String> options) {
        String text = requireOption(options, "text");
        int top = Integer.parseInt(options.getOrDefault("top", "5"));
        String metric = options.getOrDefault("metric", "cosine");

        QueryProcessor.QueryRun run = queryProcessor.query(text, top, metric);

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

    private void printHelp() {
        System.out.println("Latent Semantic Indexing demo CLI");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  compare-docs --d1 D1 --d2 D3");
        System.out.println("  demo-pipeline --k 3 --save-model true");
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

