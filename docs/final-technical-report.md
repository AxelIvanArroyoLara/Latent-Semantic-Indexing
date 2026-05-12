# LSI Document Base - Step-by-Step Technical Report

## 1. Report Purpose

This report explains everything that happens from the moment `main` is executed until the results appear in the console. It also clarifies where each data item comes from, which files are read, which classes participate, what is calculated internally, and why the output demonstrates that the system works.

The system does not print isolated data without processing. The demonstration document base does come from a controlled CSV corpus, but the terms shown in the console are produced by a full pipeline: preprocessing, semantic normalization, FrecT construction, SVD/LSI reduction, and similarity/dissimilarity queries.

Main command:

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App"
```

Equivalent explicit command:

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=final-demo --k 3 --terms 10 --top 5"
```

## 2. Direct Answer About Hardcoding

There are two separate levels:

1. Demonstration corpus: the 10 base documents come from `data/fixtures/query/document_terms.csv`. This is intentional so the execution is stable and reproducible for the professor.
2. Term processing: the system does perform filtering, stemming, synonym normalization, and polysemy handling by itself. That part is not printed directly from the CSV.

In this version, the normal execution path loads linguistic resources from:

- `src/main/resources/stopwords.txt`
- `src/main/resources/suffixes.txt`
- `src/main/resources/synonyms.csv`
- `src/main/resources/polysemy_rules.csv`

The classes keep internal fallback values only to prevent the program from failing if a resource is unavailable. In the normal Maven execution, those files are copied to `target/classes` and read from the classpath.

Real example:

```text
D1 in document_terms.csv:
academic stress student university pressure

Canonical console output:
[academic_stress, student, university, stress]
```

That result is not a literal copy of the CSV. The system detects `academic + stress` as a semantic phrase/polysemy rule and maps `pressure` to `stress` through the synonym resource.

## 3. Second 0 - Program Startup

Entry file:

- `src/main/java/com/lsi/App.java`

Flow:

1. The JVM starts the `com.lsi.App` class.
2. `App.main(String[] args)` is executed.
3. `App.main` creates a `CommandLineInterface` instance.
4. `App.main` delegates control to `CommandLineInterface.run(args)`.
5. If the exit code is different from zero, `System.exit(exitCode)` is invoked.

Console coordinator:

- `src/main/java/com/lsi/ui/CommandLineInterface.java`

Because the user normally runs `main` without arguments, `CommandLineInterface.run(args)` detects `args.length == 0` and automatically calls:

```text
runFinalDemo(Map.of())
```

This means the default mode is no longer only the help screen; it now runs the full technical demonstration.

## 4. Initial Demo Configuration

Inside `runFinalDemo`, the main parameters are prepared:

- `k`: number of latent dimensions for LSI. Default value: `3`.
- `terms`: number of significant terms to print. Default value: `10`.
- `top`: number of documents retrieved per query. Default value: `5`.
- `matrix`: FrecT printing mode. By default it prints a preview; with `--matrix full` it prints the full matrix.

These values are read from arguments when provided. Otherwise, defaults are used.

Example:

```text
final-demo --k 3 --terms 10 --top 5 --matrix preview
```

## 5. Loading the Document Base

Data file:

- `data/fixtures/query/document_terms.csv`

Columns:

- `code`: document identifier, for example `D1`.
- `title`: human-readable document title.
- `terms`: base text representing the document content used for the demo.

Class that loads the file:

- `src/main/java/com/lsi/indexing/IndexingService.java`

Method:

```text
loadDocumentsFromQueryFixture(Path fixtureDir)
```

Process:

1. The path `data/fixtures/query/document_terms.csv` is resolved.
2. All file lines are read with UTF-8.
3. The first line is skipped because it is the CSV header.
4. Each row is split into three parts: code, title, and terms.
5. Each row is converted into a `Document` object.
6. The `Document` object stores:
   - code;
   - title;
   - source path;
   - original text;
   - text for processing.

Input example:

```text
D8,University psychological support services,university support counseling therapy student
```

That row is converted into a `Document` with code `D8`, title `University psychological support services`, and processable text `university support counseling therapy student`.

## 6. Corpus Indexing

Main method:

```text
IndexingService.index(List<Document> documents, int dimensions, boolean useTfIdf)
```

This method coordinates the full document process:

1. It validates that the document list is not null or empty.
2. It creates a list of `SemanticDocument`.
3. It creates a `titlesByCode` map to preserve each document title.
4. It processes each document with `PreprocessingPipeline`.
5. It processes the resulting tokens with `SemanticPipeline`.
6. It converts each original document into a semantic document.
7. It builds the FrecT matrix.
8. It applies SVD/LSI.
9. It returns an `IndexedCorpus` containing semantic documents, the matrix, the latent model, and document titles.

The final `IndexedCorpus` object is what the console and the query processor use.

## 7. Linguistic Preprocessing

Coordinator class:

- `src/main/java/com/lsi/preprocessing/PreprocessingPipeline.java`

Method:

```text
process(String rawText)
```

Preprocessing happens in four stages.

### 7.1 Normalization

Class:

- `src/main/java/com/lsi/preprocessing/TextNormalizer.java`

Process:

1. Converts the text to lowercase.
2. Removes accents using `Normalizer`.
3. Replaces punctuation and non-alphanumeric characters with spaces.
4. Collapses repeated spaces.
5. Returns a normalized string.

Example:

```text
"Anxiety and school performance"
-> "anxiety and school performance"
```

### 7.2 Tokenization

Class:

- `src/main/java/com/lsi/preprocessing/Tokenizer.java`

Process:

1. Receives the normalized text.
2. Splits it by spaces.
3. Removes empty tokens.
4. Returns a list of words.

Example:

```text
"anxiety and school performance"
-> [anxiety, and, school, performance]
```

### 7.3 Stop List

Class:

- `src/main/java/com/lsi/preprocessing/StopWordFilter.java`

File used:

- `src/main/resources/stopwords.txt`

Process:

1. When `StopWordFilter` is created, its constructor tries to load `/stopwords.txt` from the classpath.
2. Maven copies that file from `src/main/resources` to `target/classes`.
3. Each non-empty line in the file is registered as a stop word.
4. During filtering, tokens present in the stop list are removed.

Example:

```text
[anxiety, and, school, performance]
-> [anxiety, school, performance]
```

### 7.4 Suffix List and Stemming

Class:

- `src/main/java/com/lsi/preprocessing/Stemmer.java`

File used:

- `src/main/resources/suffixes.txt`

Process:

1. When `Stemmer` is created, its constructor tries to load `/suffixes.txt`.
2. Each line is interpreted as `suffix=replacement`.
3. For each token, the configured suffixes are checked.
4. If the token ends with a suffix and has enough length, that suffix is replaced.

Observable examples:

```text
breathing -> breath
emotional -> emotion
focus -> focu
wellbeing -> wellbe
```

This explains why some printed terms look like simplified stems. They are not printing errors; they are the result of rule-based stemming.

## 8. Semantic Normalization

Coordinator class:

- `src/main/java/com/lsi/semantic/SemanticPipeline.java`

Method:

```text
process(List<String> tokens)
```

The semantic flow happens after preprocessing.

### 8.1 Polysemy and Domain Phrase Resolution

Class:

- `src/main/java/com/lsi/semantic/PolysemyResolver.java`

File used:

- `src/main/resources/polysemy_rules.csv`

Process:

1. When `PolysemyResolver` is created, `/polysemy_rules.csv` is loaded.
2. Each rule contains:
   - ambiguous term;
   - context token;
   - assigned sense;
   - priority.
3. The resolver checks consecutive token pairs.
4. If a rule is found, the pair is replaced by a canonical term.
5. The rule is registered symmetrically so both token orders can be recognized.

Examples:

```text
academic + stress -> academic_stress
mental + health -> mental_health
university + support -> institutional_support
social + media -> social_media
```

### 8.2 Synonyms

Class:

- `src/main/java/com/lsi/semantic/SynonymExpander.java`

File used:

- `src/main/resources/synonyms.csv`

Process:

1. When `SynonymExpander` is created, `/synonyms.csv` is loaded.
2. Each row connects a canonical term with a synonym.
3. The system normalizes each term to the canonical form when a rule exists.

Examples:

```text
worry -> anxiety
therapy -> counseling
pressure -> stress
rest -> sleep
network -> social_media
```

## 9. Semantic Document Creation

Model class:

- `src/main/java/com/lsi/model/SemanticDocument.java`

Each original document becomes a semantic document with:

- code;
- canonical term list;
- metadata such as title and source.

Real example:

```text
D8 input:
university support counseling therapy student

After preprocessing and semantics:
[institutional_support, counsel, counseling, student]
```

Explanation:

1. `university support` activates a polysemy rule and becomes `institutional_support`.
2. `therapy` is normalized to `counseling`.
3. `counseling` goes through stemming and may also appear as `counsel`.
4. `student` remains as a document term.

This shows that what is printed is not literal CSV text; it comes from the pipeline.

## 10. FrecT Construction

Class:

- `src/main/java/com/lsi/indexing/FrequencyMatrixBuilder.java`

Model:

- `src/main/java/com/lsi/model/FrequencyMatrix.java`

Process:

1. All `SemanticDocument` instances are received.
2. A global vocabulary is built from all canonical terms.
3. The vocabulary is sorted so the matrix is reproducible.
4. A matrix is created with terms as rows and documents as columns.
5. For each term-document pair, frequency is counted.
6. If `useTfIdf = true`, TF-IDF weighting is applied.
7. A `FrequencyMatrix` is returned.

FrecT interpretation:

- Each row represents a term.
- Each column represents a document.
- Each cell represents the term weight inside the document.

Observable console example:

```text
FrecT dimensions: 33 terms x 10 documents.
term                            D1        D2        ...
academic_stress              2.705     0.000        ...
anxiety                      0.000     2.299        ...
```

To see the full matrix:

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=final-demo --matrix full"
```

## 11. SVD / LSI Reduction

Classes:

- `src/main/java/com/lsi/lsi/LsiReducer.java`
- `src/main/java/com/lsi/lsi/SvdDecomposer.java`
- `src/main/java/com/lsi/model/LatentSpaceModel.java`

Process:

1. `LsiReducer.reduce(FrequencyMatrix, k)` receives FrecT.
2. FrecT is transformed into a numeric matrix `A` of terms by documents.
3. `SvdDecomposer` uses EJML to calculate SVD.
4. The matrix is decomposed as:

```text
A = U * S * V^T
```

5. Only the first `k` latent dimensions are kept.
6. Latent term vectors are calculated.
7. Latent document vectors are calculated.
8. A `LatentSpaceModel` is created.

The latent model contains:

- `k`;
- document codes;
- vocabulary;
- singular values;
- term vectors;
- document vectors.

Observable output:

```text
Requested k: 3
Actual k used by LSI: 3
Singular values:
  sigma_1 = 7.7209
  sigma_2 = 7.0984
  sigma_3 = 6.8823
```

The values can vary slightly if the corpus or linguistic resources change.

## 12. Significant Term Selection

Class:

- `src/main/java/com/lsi/lsi/TermSelectionService.java`

Process:

1. Term latent vectors are taken from `LatentSpaceModel`.
2. For each term, a significance score is calculated using the vector norm.
3. Terms are sorted from highest to lowest significance.
4. The first `terms` items are printed.

Observable output:

```text
Top indexing terms for expert review:
1. depression - significance: 5.2738
2. anxiety - significance: 4.9526
3. social_media - significance: 4.7357
...
```

This supports expert review of relevant indexing terms after applying LSI.

## 13. Query Processor Creation

Class:

- `src/main/java/com/lsi/query/QueryProcessor.java`

After indexing the corpus, `runFinalDemo` creates:

```text
QueryProcessor processor = new QueryProcessor(corpus)
```

This processor receives:

- semantic documents;
- FrecT matrix;
- LSI model;
- titles by code.

With that data, it can solve:

- document-document comparison;
- text-document queries using cosine similarity;
- text-document queries using Jaccard similarity;
- text-document queries using Euclidean distance.

## 14. Initial Demo Printout

First, an orientation section is printed:

```text
FINAL PROJECT TECHNICAL DEMONSTRATION
Demonstrated objective: document base with LSI to index, represent, and query documents.
Corpus domain: mental health and wellbeing among university students.
Flow: documents -> preprocessing -> semantics -> FrecT -> SVD/LSI -> queries.
```

This part comes from:

- `CommandLineInterface.runFinalDemo`

It is not a mathematical calculation; it is guide text so the professor can understand the flow before reviewing the results.

## 15. Printed Step 1 - Document Base

Output:

```text
STEP 1. DOCUMENT BASE
Requirement: work with a base of at least 10 documents.
Indexed documents: 10
D1 - Academic stress in university students
    Canonical terms: [academic_stress, student, university, stress]
...
```

Where it comes from:

- Codes and titles: `data/fixtures/query/document_terms.csv`.
- Canonical terms: result of `PreprocessingPipeline` + `SemanticPipeline`.
- Document count: `corpus.semanticDocuments().size()`.

Class that prints it:

- `CommandLineInterface.runFinalDemo`.

## 16. Printed Step 2 - Linguistic Resources

Output:

```text
STEP 2. PREPROCESSING AND SEMANTICS
Stop list: src/main/resources/stopwords.txt
Suffix list / stemming: src/main/resources/suffixes.txt
Synonyms: src/main/resources/synonyms.csv
Polysemy: src/main/resources/polysemy_rules.csv
```

Where it comes from:

- These paths are the real resource files.
- Maven copies them to `target/classes`.
- The classes load them with `getResourceAsStream`.

Classes:

- `StopWordFilter`
- `Stemmer`
- `SynonymExpander`
- `PolysemyResolver`

The console also prints examples to connect resource and effect:

```text
worry -> anxiety
therapy -> counseling
university + support -> institutional_support
mental + health -> mental_health
```

## 17. Printed Step 3 - FrecT

Output:

```text
STEP 3. FREQUENCY MATRIX FrecT
FrecT dimensions: 33 terms x 10 documents.
FrecT preview with the first 12 terms.
```

Where it comes from:

- `matrix.values().length`: number of terms.
- `matrix.documentCodes().size()`: number of documents.
- `matrix.terms()`: rows.
- `matrix.documentCodes()`: columns.
- `matrix.values()`: TF-IDF weights.

Class that calculates it:

- `FrequencyMatrixBuilder`.

Class that prints it:

- `CommandLineInterface.printFrequencyMatrixPreview`.

## 18. Printed Step 4 - SVD / LSI

Output:

```text
STEP 4. SVD / LSI REDUCTION
Requested k: 3
Actual k used by LSI: 3
Singular values:
...
Top indexing terms for expert review:
...
```

Where it comes from:

- `Requested k`: CLI parameter.
- `Actual k`: `model.k()`.
- Singular values: `model.singularValues()`.
- Significant terms: `TermSelectionService.selectTopTerms(model, topTerms)`.

Classes that calculate it:

- `LsiReducer`
- `SvdDecomposer`
- `TermSelectionService`

Class that prints it:

- `CommandLineInterface.printSelectedTerms`.

## 19. Printed Step 5 - Similarity Between Two Documents

Output:

```text
STEP 5. QUERY 1 - SIMILARITY BETWEEN TWO DOCUMENTS
Executed query: compare D1 against D3.
Cosine similarity: ...
Jaccard similarity: ...
Euclidean distance: ...
```

Process:

1. `QueryProcessor.compareDocuments("D1", "D3")` finds both documents.
2. It retrieves their latent vectors.
3. It retrieves their canonical term sets.
4. `DocumentSimilarityService` calculates:
   - cosine between vectors;
   - Jaccard between sets;
   - Euclidean distance between vectors.
5. The console prints the three results.

Classes:

- `QueryProcessor`
- `DocumentSimilarityService`
- `SimilarityCalculator`
- `DissimilarityCalculator`

## 20. Printed Step 6 - Top-N Retrieval by Similarity

Output:

```text
STEP 6. QUERY 2 - RETRIEVE TOP-N WITH SIMILARITY FUNCTIONS
Similarity function 1: cosine.
Query: academic stress anxiety
Terms used: [academic_stress, anxiety]
...
Similarity function 2: Jaccard.
Query: sleep wellbeing
Terms used: [sleep, wellbe]
...
```

Cosine process:

1. The text query is normalized like the documents.
2. It is tokenized.
3. It is filtered with stopwords.
4. Stemming is applied.
5. Semantic rules are applied.
6. A query vector is built using the latent vectors of the found terms.
7. `RankingService.rankByVector` compares the query vector against each document.
8. Results are sorted from highest to lowest cosine score.
9. The first `top` results are printed.

Jaccard process:

1. The query is converted into a set of canonical terms.
2. Each document already has its own set of canonical terms.
3. Intersection over union is calculated.
4. Results are sorted from highest to lowest Jaccard score.
5. The first `top` results are printed.

Classes:

- `QueryProcessor`
- `RankingService`
- `SimilarityCalculator`

## 21. Printed Step 7 - Top-N Retrieval by Dissimilarity

Output:

```text
STEP 7. QUERY 3 - RETRIEVE TOP-N WITH A DISSIMILARITY FUNCTION
Dissimilarity function: Euclidean distance.
Query: academic stress
Terms used: [academic_stress]
For Euclidean distance, lower score means closer.
```

Process:

1. The query is processed with the same pipeline.
2. A latent query vector is built.
3. Euclidean distance is calculated against each document.
4. A smaller distance means greater closeness.
5. Results are sorted from lowest to highest distance.
6. The first `top` results are printed.

Classes:

- `QueryProcessor`
- `RankingService`
- `DissimilarityCalculator`

## 22. Printed Step 8 - SQL and Persistence

Output:

```text
STEP 8. SQL VALIDATION AND PERSISTENCE
sql/queries/11_compare_documents_similarity.sql
sql/queries/12_rank_query_topn_cosine.sql
sql/queries/13_rank_query_topn_jaccard.sql
sql/queries/14_rank_query_topn_euclidean.sql
Final report: docs/final-technical-report.docx
```

Related files:

- `sql/migrations/V1__create_documents.sql`
- `sql/migrations/V2__create_linguistic_tables.sql`
- `sql/migrations/V3__create_terms_and_frequencies.sql`
- `sql/migrations/V4__create_latent_space_tables.sql`
- `sql/migrations/V5__create_query_logging_tables.sql`
- `sql/demo/insert_demo_data.sql`
- `sql/queries/11_compare_documents_similarity.sql`
- `sql/queries/12_rank_query_topn_cosine.sql`
- `sql/queries/13_rank_query_topn_jaccard.sql`
- `sql/queries/14_rank_query_topn_euclidean.sql`

This demonstrates that the project does not depend only on local in-memory execution. It also includes a relational PostgreSQL representation and equivalent SQL queries to validate similarity, cosine ranking, Jaccard ranking, and Euclidean distance ranking.

## 23. Output Traceability Summary

Documents and titles:

- Source: `data/fixtures/query/document_terms.csv`
- Processed by: `IndexingService`
- Printed by: `CommandLineInterface`

Canonical terms:

- Source: `document_terms.csv` plus linguistic resources
- Processed by: `PreprocessingPipeline` and `SemanticPipeline`
- Printed by: `CommandLineInterface`

Stopwords:

- Source: `src/main/resources/stopwords.txt`
- Processed by: `StopWordFilter`
- Effect: removes stop words before indexing

Suffixes and stems:

- Source: `src/main/resources/suffixes.txt`
- Processed by: `Stemmer`
- Effect: reduces words to stems by rules

Synonyms:

- Source: `src/main/resources/synonyms.csv`
- Processed by: `SynonymExpander`
- Effect: normalizes synonyms to canonical terms

Polysemy and phrases:

- Source: `src/main/resources/polysemy_rules.csv`
- Processed by: `PolysemyResolver`
- Effect: converts contextual pairs into canonical concepts

FrecT:

- Source: semantic documents
- Processed by: `FrequencyMatrixBuilder`
- Printed by: `CommandLineInterface`

SVD/LSI:

- Source: `FrequencyMatrix`
- Processed by: `LsiReducer` and `SvdDecomposer`
- Printed by: `CommandLineInterface`

Significant terms:

- Source: latent term vectors
- Processed by: `TermSelectionService`
- Printed by: `CommandLineInterface`

D1-D3 comparison:

- Source: document vectors and terms
- Processed by: `DocumentSimilarityService`
- Printed by: `CommandLineInterface`

Top-n rankings:

- Source: processed query and corpus document vectors/terms
- Processed by: `QueryProcessor` and `RankingService`
- Printed by: `CommandLineInterface`

SQL:

- Source: `sql/queries/*.sql`
- Processed by: PostgreSQL
- Printed by: `psql`

## 24. Executed Verification

Compilation:

```powershell
mvn -DskipTests compile
```

Result:

```text
BUILD SUCCESS
```

Main execution:

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App"
```

Result:

```text
FINAL PROJECT TECHNICAL DEMONSTRATION
Indexed documents: 10
FrecT dimensions: 33 terms x 10 documents.
Actual k used by LSI: 3
Top indexing terms for expert review:
...
BUILD SUCCESS
```

The execution confirms that the system starts from `main`, loads the base, applies the pipeline, builds FrecT, reduces it with SVD/LSI, and executes the queries.

## 25. Conclusions

The system uses a controlled corpus so the demonstration remains stable and reproducible, but term filtering and transformation are not simply hardcoded output. Documents go through normalization, tokenization, stop list filtering, stemming, polysemy resolution, synonym normalization, FrecT construction, SVD/LSI reduction, and rankings.

The console shows high-level results for the professor, and this report documents the technical origin of each section. This makes it possible to defend that the output comes from a complete functional flow rather than manually written text meant to simulate results.

## 26. References

- ISO/IEC 12207:2008, Systems and software engineering - Software life cycle processes.
- Manning, Raghavan, and Schutze, Introduction to Information Retrieval.
- Deerwester et al., Indexing by Latent Semantic Analysis.
- PostgreSQL documentation.
- EJML documentation.
