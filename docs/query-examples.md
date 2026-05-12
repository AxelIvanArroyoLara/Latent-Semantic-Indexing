# Query Examples

## Run the Complete Demonstration

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App"
```

The default run prints the complete final-project flow: document base, preprocessing resources, FrecT, SVD/LSI, document similarity, top-n retrieval, dissimilarity ranking, and SQL persistence references.

## Select Expert Indexing Terms

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=select-terms --terms depression,anxiety --k 3"
```

Expected behavior:

- loads the same extracted PDF corpus used by `main`;
- ranks terms by LSI significance;
- keeps the explicitly requested expert terms if they exist in the vocabulary;
- persists the selected terms in `selected_index_terms` when PostgreSQL is available.

## Compare Two Documents

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=compare-docs --d1 D1 --d2 D3"
```

Expected behavior:

- loads the indexed corpus;
- retrieves D1 and D3;
- calculates cosine similarity over latent vectors;
- calculates Jaccard similarity over canonical terms;
- calculates Euclidean distance over latent vectors;
- prints all three values.

## Top-N Retrieval With Cosine

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=query --text \"academic stress anxiety\" --top 5 --metric cosine"
```

Expected behavior:

- preprocesses the query text;
- applies synonym and polysemy rules;
- builds a latent query vector;
- ranks documents from highest to lowest cosine score;
- persists the query run and ranked results when PostgreSQL is available.

## Top-N Retrieval With Jaccard

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=query --text \"sleep wellbeing\" --top 5 --metric jaccard"
```

Expected behavior:

- converts the query into canonical terms;
- compares the query term set against each document term set;
- ranks documents by intersection over union;
- persists the query run and ranked results when PostgreSQL is available.

## Top-N Retrieval With Euclidean Distance

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=query --text \"academic stress\" --top 5 --metric euclidean"
```

Expected behavior:

- builds the query vector;
- calculates Euclidean distance against every document vector;
- sorts from lowest distance to highest distance;
- prints a note that lower score means closer;
- persists the query run and ranked results when PostgreSQL is available.

## SQL Verification

After running `main` with PostgreSQL available, run:

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/11_compare_documents_similarity.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/12_rank_query_topn_cosine.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/13_rank_query_topn_jaccard.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/14_rank_query_topn_euclidean.sql
```
