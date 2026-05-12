# Architecture Overview

## Purpose

The project implements a document base that indexes, represents, and queries a controlled corpus using Latent Semantic Indexing. The implementation is organized as a pipeline so each requirement from the final project can be traced to a concrete module.

## Runtime Flow

1. `com.lsi.App` starts the application.
2. `CommandLineInterface` selects the command. With no arguments, it runs the guided final demo.
3. `IndexingService` loads the 10-document corpus from `data/fixtures/query/document_terms.csv`.
4. `PreprocessingPipeline` normalizes text, tokenizes it, removes stop words, and applies suffix-based stemming.
5. `SemanticPipeline` resolves domain phrases/polysemy and normalizes synonyms.
6. `FrequencyMatrixBuilder` builds FrecT with terms as rows and documents as columns.
7. `LsiReducer` applies SVD and creates the reduced latent representation.
8. `QueryProcessor` executes document comparison and top-n retrieval.
9. `CorpusPersistenceService` persists documents, terms, FrecT rows, LSI document vectors, and expert-selected indexing terms when PostgreSQL is available.
10. `QueryResultPersistenceService` persists query runs and ranked results when PostgreSQL is available.
11. The CLI prints the technical evidence expected by the final delivery.

## Module Responsibilities

| Module | Responsibility |
| --- | --- |
| `config` | Loads application and PostgreSQL configuration. |
| `model` | Defines documents, semantic documents, FrecT, and LSI model data. |
| `preprocessing` | Normalization, tokenization, stop-word filtering, and stemming. |
| `semantic` | Synonym normalization and polysemy/domain phrase resolution. |
| `indexing` | Corpus indexing and FrecT construction. |
| `lsi` | SVD reduction, latent vectors, and significant-term inspection. |
| `query` | Similarity, dissimilarity, top-n ranking, and query processing. |
| `persistence` | PostgreSQL repositories and query result persistence. |
| `ui` | CLI orchestration and final-project demonstration output. |

## Database Architecture

The database is PostgreSQL and is managed through Flyway migrations in `sql/migrations/`.

| Area | Tables |
| --- | --- |
| Document base | `documents` |
| Linguistic resources | `stop_words`, `suffix_rules`, `term_synonyms`, `polysemy_rules` |
| FrecT | `terms`, `document_terms` |
| LSI representation | `latent_models`, `latent_document_vectors`, `latent_query_vectors` |
| Expert selection | `selected_index_terms` |
| Query history | `query_runs`, `query_results` |

Manual SQL verification scripts live in `sql/queries/`. Demo seed data lives in `sql/demo/insert_demo_data.sql` and loads the same 10 document identifiers used by the Java final demo.

## Design Decision

The main demo remains reproducible by using a controlled CSV corpus, while PostgreSQL persistence is connected to the real execution path. If the DBMS is available and the migrations have been applied, the corpus, FrecT rows, LSI model evidence, selected terms, query runs, and ranked results are saved through repositories. If PostgreSQL is unavailable, the CLI reports that persistence was not completed and still prints the algorithmic evidence.
