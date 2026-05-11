# Query and CLI module example

## Purpose

This document explains the Dev 6 module: query engine and demo CLI.

The module works even if persistence, indexing, and LSI are not finished yet. For that reason, it uses fixtures stored in data/fixtures/query.

## Responsibilities

The module covers:

1. Comparing two documents with cosine similarity, Jaccard similarity, and Euclidean distance.
2. Processing a query text.
3. Creating a query vector from term fixtures.
4. Ranking the top-n most relevant documents.
5. Printing clear CLI output for screenshots and final report evidence.

## Compare documents

Command:

    java -jar target/lsi-1.0-SNAPSHOT.jar compare-docs --d1 D1 --d2 D3

Output style:

    === Document comparison ===
    D1: D1 - Academic stress in university students
    D2: D3 - Student burnout

    Cosine similarity: 0.9582
    Jaccard similarity: 0.3333
    Euclidean distance: 0.4717

## Query top-n

Command:

    java -jar target/lsi-1.0-SNAPSHOT.jar query --text "academic stress anxiety" --top 5 --metric cosine

Output style:

    === Query ranking ===
    Query: academic stress anxiety
    Metric: cosine
    Top N: 5
    Terms used: [academic, stress, anxiety]

    1. D2 - Anxiety and school performance - score: 0.9942
    2. D1 - Academic stress in university students - score: 0.9920
    3. D3 - Student burnout - score: 0.9354

## Design decision

The module uses controlled fixtures instead of waiting for other modules.

This follows the team rule: if a dependency is not ready, the developer must work with contracts, mocks, and fixtures.

## Out of scope

This module does not preprocess raw documents, apply stemming, resolve synonyms or polysemy, build FrecT, run real SVD, or write to PostgreSQL.
