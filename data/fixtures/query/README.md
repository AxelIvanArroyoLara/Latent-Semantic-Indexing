# Query fixtures

These files allow Dev 6 to work without waiting for the database, indexing module, or LSI/SVD module.

## Files

- document_vectors.csv: simulated latent vectors for documents.
- document_terms.csv: terms associated with each document.
- term_vectors.csv: simulated vectors for query terms.

## Purpose

The query module can compare documents and rank query results using this controlled data.

## Supported metrics

- Cosine similarity
- Jaccard similarity
- Euclidean distance

## Example commands

    java -jar target/lsi-1.0-SNAPSHOT.jar compare-docs --d1 D1 --d2 D3
    java -jar target/lsi-1.0-SNAPSHOT.jar query --text "academic stress anxiety" --top 5 --metric cosine
    java -jar target/lsi-1.0-SNAPSHOT.jar query --text "sleep wellbeing" --top 3 --metric jaccard
    java -jar target/lsi-1.0-SNAPSHOT.jar query --text "academic stress" --top 3 --metric euclidean
