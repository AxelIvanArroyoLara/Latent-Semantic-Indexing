package com.lsi.config;

import java.nio.file.Path;

/**
 * Central application defaults used by the CLI integration flow.
 */
public final class AppConfig {

    public static final int DEFAULT_LSI_DIMENSIONS = 3;
    public static final int DEFAULT_TOP_N = 5;
    public static final String DEFAULT_METRIC = "cosine";

    public static final Path QUERY_FIXTURE_DIR = Path.of("data", "fixtures", "query");
    public static final Path DEFAULT_MODEL_FILE = Path.of("target", "lsi-model.bin");

    private AppConfig() {
        // Utility class.
    }
}

