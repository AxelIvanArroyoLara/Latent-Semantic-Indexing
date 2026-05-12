# Architecture Overview

## Purpose

The project implements a document base that extracts, indexes, represents, and queries real PDFs using Latent Semantic Indexing. The implementation is organized as a pipeline so each requirement from the final project can be traced to a concrete module.

## Runtime Flow

1. `com.lsi.App` starts the application.
2. `CommandLineInterface` selects the command. With no arguments, it runs the guided final demo.
3. `IndexingService` extracts text from the PDFs in `data/raw/`.
4. `PreprocessingPipeline` normalizes text, tokenizes it, removes stop words, removes numeric/table noise, and applies suffix-based stemming.
5. `SemanticPipeline` resolves domain phrases/polysemy and normalizes synonyms.
6. `FrequencyMatrixBuilder` builds the initial FrecT with terms as rows and documents as columns.
7. `LsiReducer` applies SVD and creates the initial latent representation.
8. `TermSelectionService` ranks terms by LSI significance; the expert-selected vocabulary is then used to rebuild FrecT/LSI as the active filtered index. The demo shows 10 terms for review and keeps 150 selected terms by default for the active representation.
9. `QueryProcessor` executes document comparison and top-n retrieval against the active filtered index.
10. `CorpusPersistenceService` persists documents, terms, FrecT rows, LSI document vectors, and expert-selected indexing terms when PostgreSQL is available.
11. `QueryResultPersistenceService` persists query runs and ranked results when PostgreSQL is available.
12. The CLI prints the technical evidence expected by the final delivery.

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

Manual SQL verification scripts live in `sql/queries/`. The Java final demo now obtains its document base from extracted PDFs instead of CSV fixtures.

## Design Decision

The main demo extracts the real PDF corpus and connects PostgreSQL persistence to that real execution path. If the DBMS is available and the migrations have been applied, the extracted documents, filtered FrecT rows, LSI model evidence, selected terms, query runs, and ranked results are saved through repositories. If PostgreSQL is unavailable, the CLI reports that persistence was not completed and still prints the algorithmic evidence.
