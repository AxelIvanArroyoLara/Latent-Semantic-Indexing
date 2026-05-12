# LSI Document Base - Final Technical Report

## 1. Project Title

Latent Semantic Indexing Document Base.

## 2. Brief Description

This project implements a document retrieval system for a controlled document base about mental health and wellbeing among university students. The executable flow builds a 10-document corpus, applies linguistic preprocessing, resolves synonyms and polysemy/domain phrases, constructs the FrecT term-document matrix, applies SVD/LSI, executes similarity and dissimilarity queries, and attempts to persist the corpus, selected terms, LSI evidence, query runs, and ranked results in PostgreSQL.

The raw PDFs under `data/raw/` are reference source material. The final executable demo intentionally uses `data/fixtures/query/document_terms.csv` as a controlled corpus so every evaluator sees the same FrecT, SVD/LSI, and query output.

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

- Corpus: `data/fixtures/query/document_terms.csv` with D1-D10.
- Stop list: `src/main/resources/stopwords.txt` through `StopWordFilter`.
- Suffix list and stems: `src/main/resources/suffixes.txt` through `Stemmer`.
- Synonyms: `src/main/resources/synonyms.csv` through `SynonymExpander`.
- Polysemy/domain phrases: `src/main/resources/polysemy_rules.csv` through `PolysemyResolver`.
- FrecT: `FrequencyMatrixBuilder` creates a TF-IDF terms x documents matrix.
- LSI: `LsiReducer` applies SVD and returns `LatentSpaceModel`.
- Expert term selection: `TermSelectionService`, `final-demo --expert-terms ...`, and `select-terms --terms ...`.
- Similarity: cosine and Jaccard.
- Dissimilarity: Euclidean distance.
- Persistence: PostgreSQL repositories, `CorpusPersistenceService`, `SelectedTermRepository`, and `QueryResultPersistenceService`.
- CLI: `CommandLineInterface`, launched from `com.lsi.App`.

## 6. Database Design

The project uses PostgreSQL with Flyway migrations in `sql/migrations/`. It does not depend on obsolete consolidated `schema.sql`, `seed.sql`, or `queries.sql` files.

Core tables: `documents`, `terms`, `document_terms`, `stop_words`, `suffix_rules`, `term_synonyms`, `polysemy_rules`, `latent_models`, `latent_document_vectors`, `latent_query_vectors`, `selected_index_terms`, `query_runs`, and `query_results`.

The relational demo data is loaded with:

```powershell
mvn flyway:migrate "-Dflyway.password=your_password_here"
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/demo/insert_demo_data.sql
```

Runtime credentials are not hardcoded. `DatabaseConfig` reads `application.properties` and supports `LSI_DB_URL`, `LSI_DB_USER`, and `LSI_DB_PASSWORD`.

## 7. System Functions and Platform

Platform: Java 21, Maven, PostgreSQL, Flyway, EJML, and JUnit.

Runtime flow from `main`:

1. `App.main` delegates to `CommandLineInterface`.
2. No arguments starts `final-demo`.
3. `IndexingService` loads the controlled 10-document corpus.
4. `PreprocessingPipeline` normalizes, tokenizes, filters stop words, and stems terms.
5. `SemanticPipeline` applies polysemy/domain phrase rules and synonym normalization.
6. `FrequencyMatrixBuilder` creates FrecT.
7. `LsiReducer` applies SVD/LSI.
8. `TermSelectionService` ranks terms for expert selection.
9. `CorpusPersistenceService` attempts to save documents, terms, FrecT rows, LSI vectors, and selected terms.
10. `QueryProcessor` executes document comparison and top-n retrieval.
11. `QueryResultPersistenceService` attempts to save query runs and ranked results.
12. The CLI prints technical evidence for review.

## 8. Standards and Regulations

The project follows ISO/IEC 12207:2008 principles at course-project scale: requirements are mapped to modules, implementation is modularized by lifecycle concern, verification is performed with tests and SQL scripts, and documentation is maintained in `docs/`.

## 9. Methodology

The implementation followed incremental integration: define the corpus, build preprocessing, add semantic resources, construct FrecT, apply SVD/LSI, add significant-term selection, implement similarity and dissimilarity queries, add PostgreSQL migrations and repositories, connect the CLI to `main`, and validate with Maven tests plus main execution.

## 10. Results and Execution Evidence

The current `main` execution prints the complete guided demonstration:

- D1-D10 are loaded from the controlled corpus.
- Stop list, suffix list, synonyms, and polysemy resources are named.
- FrecT dimensions are printed as terms x documents.
- SVD singular values and top indexing terms are printed.
- Expert-selected terms are printed and persisted when PostgreSQL is available.
- D1 vs D3 is compared with cosine, Jaccard, and Euclidean distance.
- `academic stress anxiety` is retrieved with cosine.
- `sleep wellbeing` is retrieved with Jaccard.
- `academic stress` is retrieved with Euclidean distance, where lower score means closer.

![Final demo document base evidence](docs/assets/final-demo-document-base.png)

![Final demo LSI and query evidence](docs/assets/final-demo-lsi-queries.png)

![SQL D1-D10 evidence](docs/assets/sql-demo-d1-d10.png)

## 11. SQL Query Evidence

The required SQL verification files are:

- `sql/queries/11_compare_documents_similarity.sql`: compares two documents with cosine, Jaccard, and Euclidean distance.
- `sql/queries/12_rank_query_topn_cosine.sql`: top-n retrieval using cosine similarity.
- `sql/queries/13_rank_query_topn_jaccard.sql`: top-n retrieval using Jaccard similarity.
- `sql/queries/14_rank_query_topn_euclidean.sql`: top-n retrieval using Euclidean distance.

`sql/demo/insert_demo_data.sql` now aligns with the Java corpus by loading D1-D10, not only D1-D3.

## 12. Verification

Automated verification:

```text
mvn test
Tests run: 55, Failures: 0, Errors: 0, Skipped: 31
BUILD SUCCESS
```

The skipped tests are PostgreSQL integration tests guarded by JUnit assumptions when the local DBMS is unavailable. Main execution verification:

```text
mvn exec:java "-Dexec.mainClass=com.lsi.App"
BUILD SUCCESS
```

The successful run confirms that the project starts from `main`, loads the corpus, builds FrecT, applies SVD/LSI, performs the required queries, and attempts database persistence.

## 13. Conclusions

The system now satisfies the requested delivery points: controlled 10-document base, FrecT, stop list, suffix list, stems, synonyms, polysemy, SVD/LSI, expert indexing-term selection, SQL query files, two similarity functions, one dissimilarity function, DBMS-backed persistence path, console evidence, and a report structured around the project requirements.

Operational condition: PostgreSQL must be running, migrated, and seeded for persistence to complete at runtime. Without the DBMS, the CLI still demonstrates the algorithmic flow and reports that persistence could not be completed.

## 14. References

- ISO/IEC 12207:2008, Systems and software engineering - Software life cycle processes.
- Deerwester et al., Indexing by Latent Semantic Analysis.
- Manning, Raghavan, and Schutze, Introduction to Information Retrieval.
- PostgreSQL documentation.
- EJML documentation.
