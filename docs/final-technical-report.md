# LSI Document Base for College Student Mental Health

## 1. Title of the project

**LSI Document Base for College Student Mental Health and Wellbeing**

## 2. Brief description

This project implements a document base that indexes and queries a corpus about college student mental health, wellbeing, academic stress, sleep quality, counseling, and institutional support. The system applies preprocessing, semantic normalization, frequency matrix construction, Singular Value Decomposition (SVD), Latent Semantic Indexing (LSI), and SQL/Java query operations to retrieve relevant documents and compare document similarity.

## 3. Feasibility analysis

The solution is technically feasible because it uses Java, Maven, PostgreSQL, JDBC/HikariCP, Flyway migrations, and EJML for SVD. The corpus is available as local PDF sources and controlled fixtures. PostgreSQL stores the relational representation of documents, terms, frequencies, LSI vectors, and query logs. The main operational constraint is that repository integration tests require a running PostgreSQL instance at the configured host and port.

## 4. Objectives, goals, and hypotheses

The objective is to create a document base that can represent and consult at least ten documents using LSI. The goals are to reduce lexical noise, capture semantic equivalences, construct FrecT, persist the document base in a DBMS, reduce the representation with SVD, compare documents, and retrieve the top-n documents for a query. The hypothesis is that LSI plus semantic normalization improves retrieval by grouping related concepts such as `worry -> anxiety`, `therapy -> counseling`, and contextual terms such as `university support -> institutional_support`.

## 5. Technical specifications

- Language: Java 21.
- Build tool: Maven.
- DBMS: PostgreSQL.
- Persistence: JDBC, HikariCP, Flyway.
- Linear algebra: EJML.
- Testing: JUnit 5.
- Data model: documents, terms, document-term frequencies, LSI models, latent vectors, query logs, and query results.
- Query functions: cosine similarity, Jaccard similarity, and Euclidean distance.

## 6. Database design

The database schema is defined in `sql/migrations`. It contains tables for `documents`, linguistic resources, `terms`, `document_terms`, `latent_models`, `latent_document_vectors`, `latent_query_vectors`, `query_runs`, and `query_results`. FrecT is represented as a sparse relational matrix in `document_terms`, avoiding dynamic matrix columns and allowing SQL verification queries.

## 7. System functions and platform

The system runs from `com.lsi.App`. Main commands:

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=demo-pipeline --k 3 --terms 8"
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=inspect-lsi --k 3 --terms 10"
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=compare-docs --d1 D1 --d2 D3"
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=query --text academic --top 3 --metric cosine"
```

The integrated pipeline performs preprocessing, semantic normalization, FrecT construction, SVD/LSI reduction, term selection, and ranking.

## 8. Standards and regulations

The project follows ISO/IEC 12207:2008 concepts by separating the software life cycle into requirements, design, implementation, verification, integration, and maintenance artifacts. The repository includes modular source packages, migration scripts, tests, verification queries, architecture notes, and a repeatable Maven build.

## 9. Methodology

1. Define the domain and corpus.
2. Design the relational schema and migrations.
3. Implement preprocessing and semantic normalization.
4. Build FrecT from semantic terms.
5. Reduce FrecT with SVD to obtain LSI vectors.
6. Implement document comparison and query ranking.
7. Add SQL verification queries for similarity and dissimilarity.
8. Validate with unit tests and CLI execution.

## 10. Results obtained

The project indexes ten controlled documents for demonstration and includes more than ten raw source documents. The integrated pipeline produces a FrecT matrix, a reduced LSI model, document vectors, and top indexing terms for expert review. The query engine supports cosine, Jaccard, and Euclidean metrics. SQL files `11` to `14` demonstrate the required database-side query types.

## 11. Program examples and window evidence

Example integrated pipeline output:

```text
=== Integrated LSI pipeline ===
Documents indexed: 10
Vocabulary terms: 32
Frequency matrix: 32x10
LSI dimensions: 3
Document vectors: 10
Top indexing terms for expert review: ...
```

Example document comparison:

```text
compare-docs --d1 D1 --d2 D3
=== Document comparison ===
D1: D1 - Academic stress in university students
D2: D3 - Student burnout

Cosine similarity: 1.0000
Jaccard similarity: 0.3333
Euclidean distance: 0.0000
```

Example top-n query:

```text
query --text sleep --top 3 --metric jaccard

=== Query ranking ===
Query: sleep
Metric: jaccard
Top N: 3
Terms used: [sleep]

1. D5 - Sleep quality and mental health - score: 0.2500
2. D1 - Academic stress in university students - score: 0.0000
3. D10 - Breathing techniques and self regulation - score: 0.0000
```

These command outputs serve as terminal-window evidence for the report. Additional screenshots can be captured directly from the terminal when preparing the final submission PDF.

## 12. Conclusions

The project satisfies the core technical requirements of the document base: document representation, semantic treatment, FrecT construction, LSI reduction, database schema design, similarity and dissimilarity functions, SQL verification queries, and CLI execution from `main`. Repository tests are integration tests and are skipped when PostgreSQL is not available; they execute when the configured DBMS is running.

## 13. References and bibliography

- ISO/IEC 12207:2008, Systems and software engineering - Software life cycle processes.
- Manning, Raghavan, and Schutze, *Introduction to Information Retrieval*.
- Deerwester et al., "Indexing by Latent Semantic Analysis".
- EJML documentation for matrix decomposition and SVD.
- PostgreSQL documentation for relational schema design and SQL queries.
