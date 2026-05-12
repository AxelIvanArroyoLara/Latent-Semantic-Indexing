# Final SQL Verification Queries

These queries close the database-side requirements from the final project.

## Required SQL query types

| File | Purpose |
|---|---|
| `sql/queries/11_compare_documents_similarity.sql` | Compares two documents with cosine similarity, Jaccard similarity, and Euclidean distance over FrecT. |
| `sql/queries/12_rank_query_topn_cosine.sql` | Retrieves top-n documents for query Q using cosine similarity. |
| `sql/queries/13_rank_query_topn_jaccard.sql` | Retrieves top-n documents for query Q using Jaccard similarity. |
| `sql/queries/14_rank_query_topn_euclidean.sql` | Retrieves top-n documents for query Q using Euclidean distance. |

## Execution

Run after Flyway migrations and demo data:

```powershell
Run `mvn exec:java "-Dexec.mainClass=com.lsi.App"` with PostgreSQL available so the extracted PDF corpus is persisted.
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/11_compare_documents_similarity.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/12_rank_query_topn_cosine.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/13_rank_query_topn_jaccard.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/14_rank_query_topn_euclidean.sql
```
