# Latent Semantic Indexing Document Base

Document retrieval system based on Latent Semantic Indexing (LSI). It indexes a controlled document base, applies linguistic preprocessing and semantic normalization, builds a term-document frequency matrix (FrecT), reduces the representation with SVD, executes similarity/dissimilarity queries, and persists the corpus, FrecT rows, LSI evidence, selected indexing terms, and query results in PostgreSQL when the database is available.

## Objective

The project satisfies the final-project objective: create and manipulate a document base of at least 10 documents using indexing and querying techniques, with semantic handling through stop words, suffix/stemming rules, synonyms, and polysemy rules.

The demonstrated corpus focuses on mental health and wellbeing among university students, a public-health and welfare domain.

## Quick Start / Full Execution Guide

To run the project from zero with PostgreSQL persistence, follow:

[docs/execution-guide.md](docs/execution-guide.md)

This guide explains how to configure PostgreSQL, run Flyway migrations, import the real PDFs from `data/raw`, execute the final demo with `--source db --persist true`, persist query results, and validate the database with SQL scripts.

This is the recommended path for evaluating the final integrated version of the project.

## Main Runtime

The recommended database-backed execution uses PostgreSQL as the document source and persists the generated artifacts.

First import the real PDFs from `data/raw` into PostgreSQL:

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=import-pdfs-db --reset true"
```

Then run the final demo using the database:

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=final-demo --source db --persist true"
```

This execution path loads documents from PostgreSQL, builds FrecT, applies SVD/LSI, persists the generated terms, frequencies, LSI model, document vectors, selected indexing terms, and prints the guided final-project demonstration.

Running without arguments is still supported:

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App"
```

However, for the final reproducible database integration, use the explicit `--source db --persist true` commands above.

### Query examples with persistence

For query commands with spaces in the `--text` argument, Git Bash is recommended.

```bash
mvn exec:java -Dexec.mainClass=com.lsi.App -Dexec.args="query --source db --text \"academic stress anxiety\" --top 5 --metric cosine --persist true"
mvn exec:java -Dexec.mainClass=com.lsi.App -Dexec.args="query --source db --text \"sleep wellbeing\" --top 5 --metric jaccard --persist true"
mvn exec:java -Dexec.mainClass=com.lsi.App -Dexec.args="query --source db --text \"academic stress\" --top 5 --metric euclidean --persist true"
```

These commands persist query executions in `query_runs` and ranked results in `query_results`.

### Other useful commands

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=final-demo --source pdf"
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=demo-pipeline --source db --k 3 --terms 8"
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=inspect-lsi --source db --k 3 --terms 10"
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=compare-docs --d1 D1 --d2 D3"
```

## Functional Coverage

The system provides:

- a real PDF corpus loaded from `data/raw/*.pdf`;
- preprocessing with normalization, tokenization, stop-word filtering, and suffix-based stemming;
- semantic normalization with synonyms and polysemy/domain phrase rules;
- FrecT construction with TF-IDF weighting;
- SVD/LSI reduction using EJML;
- expert-oriented significant term selection with a broader active vocabulary, 150 terms by default;
- document-document similarity with cosine, Jaccard, and Euclidean distance;
- top-n retrieval for text queries with cosine and Jaccard similarity;
- top-n retrieval with Euclidean distance as a dissimilarity function;
- PostgreSQL schema, migrations, repositories, and SQL verification scripts;
- corpus, FrecT, LSI model, expert-selected terms, query-run, and ranked-result persistence when PostgreSQL is available.

## Project Structure

```text
lsi/
├── data/
│   ├── fixtures/
│   │   ├── query/
│   │   └── semantics/
│   ├── processed/
│   └── raw/
├── docs/
│   ├── execution-guide.md
│   ├── architecture-overview.md
│   ├── query-examples.md
│   ├── final-sql-verification.md
│   └── final-technical-report.docx
├── sql/
│   ├── migrations/
│   ├── queries/
│   └── demo/
├── src/
│   ├── main/
│   │   ├── java/com/lsi/
│   │   │   ├── config/
│   │   │   ├── indexing/
│   │   │   ├── lsi/
│   │   │   ├── model/
│   │   │   ├── persistence/
│   │   │   ├── preprocessing/
│   │   │   ├── query/
│   │   │   ├── semantic/
│   │   │   └── ui/
│   │   └── resources/
│   └── test/
└── pom.xml
```

## Modules

| Module | Responsibility |
| --- | --- |
| `config` | Application and PostgreSQL configuration. |
| `model` | Domain objects for documents, FrecT, LSI, and query results. |
| `preprocessing` | Text normalization, tokenization, stop words, and stemming. |
| `semantic` | Synonym normalization and polysemy/domain phrase resolution. |
| `indexing` | Document loading, semantic document creation, vocabulary, and FrecT. |
| `lsi` | SVD reduction, latent vectors, and significant term inspection. |
| `query` | Similarity, dissimilarity, top-n ranking, and query processing. |
| `persistence` | PostgreSQL repositories and query result persistence. |
| `ui` | CLI orchestration and final demonstration output. |

## SQL and PostgreSQL

The project uses Flyway migrations and separated SQL scripts. It does not use consolidated `schema.sql`, `seed.sql`, or `queries.sql` files.

| Path | Purpose |
| --- | --- |
| `sql/migrations/` | Flyway migrations that create schema and seed official linguistic resources. |
| `sql/demo/insert_demo_data.sql` | Legacy manual seed script kept for SQL-only experiments; it is not the source used by `main`. |
| `sql/queries/` | Manual verification queries for document counts, FrecT, LSI vectors, query logs, and final project query requirements after runtime persistence. |

Core tables:

- `documents`
- `terms`
- `document_terms`
- `stop_words`
- `suffix_rules`
- `term_synonyms`
- `polysemy_rules`
- `latent_models`
- `latent_document_vectors`
- `latent_query_vectors`
- `selected_index_terms`
- `query_runs`
- `query_results`

Load migrations:

```powershell
mvn flyway:migrate "-Dflyway.password=your_password_here"
```

If the password is already configured through `application.properties` or environment variables, `mvn flyway:migrate` may also work.

Run final SQL verification:

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/11_compare_documents_similarity.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/12_rank_query_topn_cosine.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/13_rank_query_topn_jaccard.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/14_rank_query_topn_euclidean.sql
```

## Database Configuration

Runtime database values live in `src/main/resources/application.properties` and can be overridden without editing tracked files:

```powershell
$env:LSI_DB_URL = "jdbc:postgresql://localhost:5433/lsi_documentbase"
$env:LSI_DB_USER = "postgres"
$env:LSI_DB_PASSWORD = "your_password_here"
```

Flyway can be overridden with Maven properties:

```powershell
mvn flyway:migrate "-Dflyway.password=your_password_here"
```

## Persistence Behavior

The CLI builds the corpus and query results first, then attempts to persist them through `CorpusPersistenceService` and `QueryResultPersistenceService`.

If PostgreSQL is available and `main` or `final-demo` is executed, the flow stores:

- the extracted PDF corpus in `documents`;
- vocabulary rows in `terms`;
- FrecT rows in `document_terms`;
- LSI document vector components in `latent_document_vectors`;
- expert-selected indexing terms in `selected_index_terms`;
- one row in `query_runs` per query execution;
- one row in `query_results` per ranked document result.

If PostgreSQL is unavailable, the CLI prints a clear persistence warning and continues showing the algorithmic results. This keeps the classroom demo runnable while still connecting the real execution path to the DBMS requirement.

## Corpus Scope

The executable final demo extracts text directly from the PDFs under `data/raw/`. CSV fixtures remain only as legacy/unit-test support; they are not the document base used by `main`, `final-demo`, `demo-pipeline`, `inspect-lsi`, `compare-docs`, `query`, or `select-terms`.

The final demo first builds an initial LSI model from the full extracted PDF corpus, ranks significant terms, applies the expert-selected terms, rebuilds FrecT/LSI with a broader selected vocabulary, 150 terms by default, and executes the query examples against that filtered active index. The console shows a short expert-review list while the active index keeps enough terms to preserve latent semantic richness.

## Documentation

- `docs/execution-guide.md`: step-by-step guide to install, configure PostgreSQL, import PDFs, run the database-backed demo, persist queries, and validate SQL results.
- `docs/architecture-overview.md`: module architecture and runtime flow.
- `docs/query-examples.md`: CLI and SQL query examples.
- `docs/final-sql-verification.md`: final SQL verification checklist.
- `docs/final-technical-report.docx`: final technical report.

## Verification

Compile:

```powershell
mvn -DskipTests compile
```

Run tests:

```powershell
mvn test
```

Some persistence tests require a reachable local PostgreSQL instance configured in `src/main/resources/application.properties`; when the database is unavailable, those tests are skipped by assumption.

## Project Name

**Latent Semantic Indexing Document Base**
