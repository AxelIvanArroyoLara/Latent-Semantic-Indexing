# LSI Document Base - Final Technical Report

## 1. Project Title

Latent Semantic Indexing Document Base.

## 2. Brief Description

This project implements a document retrieval system for a PDF document base about mental health and wellbeing among university students. The executable flow imports real PDFs into PostgreSQL, loads the document base from the `documents` table, applies linguistic preprocessing, resolves synonyms and polysemy/domain phrases, constructs the FrecT term-document matrix, applies SVD/LSI, lets expert-selected terms define the active filtered index, executes similarity and dissimilarity queries, and persists the corpus, selected terms, LSI evidence, query runs, and ranked results in PostgreSQL when the database-backed execution path is used.

The PDFs under `data/raw/` are the real source document base. CSV fixtures remain only for legacy/unit-test support. The recommended final execution path is:

```text
data/raw/*.pdf
→ import-pdfs-db
→ PostgreSQL
→ final-demo --source db --persist true
→ query --source db --persist true
```

## 3. Feasibility Analysis

The project is feasible because the corpus is bounded to 10 documents, Java 21 and Maven provide a reproducible platform, EJML provides SVD, and PostgreSQL stores the relational evidence. The main risk is local DBMS availability; the CLI therefore prints the complete algorithmic evidence even when PostgreSQL is down, while still connecting the real runtime path to persistence through repositories.

## 4. Objectives, Goals, and Hypothesis

Objective: create and manipulate a document base using LSI, FrecT, SQL-supported persistence, and query functions.

- Use at least 10 documents.
- Build FrecT from the document base.
- Apply stop-word filtering, suffix-based stemming, synonym normalization, and polysemy resolution.
- Apply SVD/LSI and expose significant indexing terms for expert selection.
- Compare two documents using similarity and distance.
- Retrieve top-n documents for a text query with two similarity functions and one dissimilarity function.
- Persist the corpus, selected terms, query runs, and ranked results in a DBMS.

Hypothesis: semantic normalization plus LSI produces a compact representation that supports meaningful document comparison and top-n retrieval over the student wellbeing corpus.

## 5. Technical Specifications

- Corpus: PDF files extracted from `data/raw/`.
- Stop list: `src/main/resources/stopwords.txt` through `StopWordFilter`.
- Suffix list and stems: `src/main/resources/suffixes.txt` through `Stemmer`.
- Synonyms: `src/main/resources/synonyms.csv` through `SynonymExpander`.
- Polysemy/domain phrases: `src/main/resources/polysemy_rules.csv` through `PolysemyResolver`.
- FrecT: `FrequencyMatrixBuilder` creates a TF-IDF terms x documents matrix.
- LSI: `LsiReducer` applies SVD and returns `LatentSpaceModel`.
- Expert term selection: `TermSelectionService`, `final-demo --expert-terms ...`, `--index-terms 150`, and `select-terms --terms ...`.
- Similarity: cosine and Jaccard.
- Dissimilarity: Euclidean distance.
- Persistence: PostgreSQL repositories, `CorpusPersistenceService`, `SelectedTermRepository`, and `QueryResultPersistenceService`.
- CLI: `CommandLineInterface`, launched from `com.lsi.App`.

## 6. Database Design

The project uses PostgreSQL with Flyway migrations in `sql/migrations/`. It does not depend on obsolete consolidated `schema.sql`, `seed.sql`, or `queries.sql` files.

Core tables: `documents`, `terms`, `document_terms`, `stop_words`, `suffix_rules`, `term_synonyms`, `polysemy_rules`, `latent_models`, `latent_document_vectors`, `latent_query_vectors`, `selected_index_terms`, `query_runs`, and `query_results`.

The relational schema is created with:

```powershell
mvn flyway:migrate "-Dflyway.password=your_password_here"
```

Runtime credentials are not hardcoded. `DatabaseConfig` reads `application.properties` and supports `LSI_DB_URL`, `LSI_DB_USER`, and `LSI_DB_PASSWORD`.

For the final integrated flow, the database is not only a secondary output. It is also used as the source of the runtime corpus after importing PDFs with:

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=import-pdfs-db --reset true"
```

The legacy script `sql/demo/insert_demo_data.sql` remains available for SQL-only manual experiments, but it is not the recommended final execution path.

## 7. System Functions and Platform

Platform: Java 21, Maven, PostgreSQL, Flyway, EJML, and JUnit.

Runtime flow for the database-backed execution:

1. `App.main` delegates to `CommandLineInterface`.
2. `import-pdfs-db --reset true` imports real PDFs from `data/raw/` into PostgreSQL.
3. `final-demo --source db --persist true` loads documents from the PostgreSQL `documents` table.
4. `PreprocessingPipeline` normalizes, tokenizes, filters stop words, removes numeric/table noise, and stems terms.
5. `SemanticPipeline` applies polysemy/domain phrase rules and synonym normalization.
6. `FrequencyMatrixBuilder` creates FrecT.
7. `LsiReducer` applies SVD/LSI.
8. `TermSelectionService` ranks terms for expert selection.
9. The system rebuilds FrecT/LSI using a broader expert-selected indexing vocabulary, 150 terms by default, while showing only the top terms needed for review.
10. `CorpusPersistenceService` saves documents, terms, FrecT rows, LSI vectors, and selected terms.
11. `QueryProcessor` executes document comparison and top-n retrieval on the active filtered index.
12. `query --source db --persist true` saves query runs and ranked results in `query_runs` and `query_results`.
13. The CLI prints technical evidence for review.

The project can still execute a guided demo without explicit database flags, but the recommended reproducible path for the final integrated version is the database-backed path described above.

## 8. Standards and Regulations

The project follows ISO/IEC 12207:2008 principles at course-project scale: requirements are mapped to modules, implementation is modularized by lifecycle concern, verification is performed with tests and SQL scripts, and documentation is maintained in `docs/`.

## 9. Methodology

The implementation followed incremental integration: define the corpus, build preprocessing, add semantic resources, construct FrecT, apply SVD/LSI, add significant-term selection, implement similarity and dissimilarity queries, add PostgreSQL migrations and repositories, connect the CLI to `main`, and validate with Maven tests plus main execution.

The final integration step connected the runtime pipeline to PostgreSQL in both directions: real PDFs are imported into `documents`, the LSI pipeline can use PostgreSQL as the document source, and the generated artifacts are persisted back into relational tables for SQL verification.

## 10. Results and Execution Evidence

The current database-backed execution prints the complete guided demonstration:

- PDF documents are imported from `data/raw/` into PostgreSQL.
- The final demo loads the document base from PostgreSQL using `--source db`.
- Stop list, suffix list, synonyms, and polysemy resources are named.
- FrecT dimensions are printed as terms x documents.
- SVD singular values and top indexing terms are printed.
- Expert-selected terms are printed for review; a broader selected vocabulary is used to rebuild the active index and is persisted when PostgreSQL is available.
- D1 vs D3 is compared with cosine, Jaccard, and Euclidean distance.
- `academic stress anxiety` is retrieved with cosine.
- `sleep wellbeing` is retrieved with Jaccard.
- `academic stress` is retrieved with Euclidean distance, where lower score means closer.
- Query executions and ranked results are persisted when `query --source db --persist true` is used.

The validated runtime evidence included execution over a real imported PDF corpus. In one successful run, the system built an LSI matrix with thousands of terms over the imported PDF documents and persisted ranked query results for cosine, Jaccard, and Euclidean executions.

![Final demo document base evidence](docs/assets/final-demo-document-base.png)

![Final demo LSI and query evidence](docs/assets/final-demo-lsi-queries.png)

## 11. SQL Query Evidence

The required SQL verification files are:

- `sql/queries/11_compare_documents_similarity.sql`: compares two documents with cosine, Jaccard, and Euclidean distance.
- `sql/queries/12_rank_query_topn_cosine.sql`: top-n retrieval using cosine similarity.
- `sql/queries/13_rank_query_topn_jaccard.sql`: top-n retrieval using Jaccard similarity.
- `sql/queries/14_rank_query_topn_euclidean.sql`: top-n retrieval using Euclidean distance.

Additional SQL verification files confirm that the database-backed execution persisted the generated artifacts:

- `sql/queries/02_list_documents.sql`: verifies documents imported from `data/raw/`.
- `sql/queries/03_list_terms.sql`: verifies vocabulary terms generated by the pipeline.
- `sql/queries/04_document_frequency_matrix.sql`: verifies FrecT rows in relational form.
- `sql/queries/05_lsi_models.sql`: verifies persisted LSI model metadata.
- `sql/queries/06_lsi_document_vectors.sql`: verifies latent document vectors.
- `sql/queries/08_query_runs_and_results.sql`: verifies persisted query executions and ranked results.

When PostgreSQL is available, the Java runtime persists the extracted PDF corpus and the active expert-filtered index through `CorpusPersistenceService`, so the relational evidence comes from the same PDF-based execution path used by the final demo.

## 12. Verification

Automated verification:

```text
mvn test
Tests run: 55, Failures: 0, Errors: 0
BUILD SUCCESS
```

Some PostgreSQL-related tests may be skipped or may depend on local DBMS availability depending on the environment. Since tests can clean and insert temporary data, the final demo state should be rebuilt after running tests.

Recommended execution verification:

```powershell
mvn flyway:migrate "-Dflyway.password=your_password_here"
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=import-pdfs-db --reset true"
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=final-demo --source db --persist true"
```

Recommended query persistence verification from Git Bash:

```bash
mvn exec:java -Dexec.mainClass=com.lsi.App -Dexec.args="query --source db --text \"academic stress anxiety\" --top 5 --metric cosine --persist true"
mvn exec:java -Dexec.mainClass=com.lsi.App -Dexec.args="query --source db --text \"sleep wellbeing\" --top 5 --metric jaccard --persist true"
mvn exec:java -Dexec.mainClass=com.lsi.App -Dexec.args="query --source db --text \"academic stress\" --top 5 --metric euclidean --persist true"
```

SQL verification:

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/08_query_runs_and_results.sql
```

The successful run confirms that the project imports real PDFs, stores them in PostgreSQL, loads them as the runtime corpus, builds FrecT, applies SVD/LSI, performs the required queries, and persists the generated artifacts and ranked results.

## 13. Conclusions

The system now satisfies the requested delivery points: real PDF document base, FrecT, stop list, suffix list, stems, synonyms, polysemy, SVD/LSI, expert indexing-term selection that controls the active index, SQL query files, two similarity functions, one dissimilarity function, DBMS-backed persistence path, console evidence, and a report structured around the project requirements.

The final integrated version does not only demonstrate the algorithmic LSI pipeline. It also provides a functional PostgreSQL-backed execution path where real PDFs are imported, indexed, transformed into FrecT/LSI artifacts, and persisted with query history and ranked results.

Operational condition: PostgreSQL must be running, migrated, and configured for persistence to complete at runtime. Without the DBMS, the CLI can still demonstrate part of the algorithmic flow, but the recommended final verification requires the full database-backed path.

## 14. References

- ISO/IEC 12207:2008, Systems and software engineering - Software life cycle processes.
- Deerwester et al., Indexing by Latent Semantic Analysis.
- Manning, Raghavan, and Schutze, Introduction to Information Retrieval.
- PostgreSQL documentation.
- EJML documentation.
