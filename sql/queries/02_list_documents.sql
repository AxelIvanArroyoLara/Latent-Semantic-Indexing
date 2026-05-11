-- Lists the documents loaded into the document base.

SELECT
    document_id,
    code,
    title,
    source,
    language_code,
    created_at
FROM documents
ORDER BY document_id;