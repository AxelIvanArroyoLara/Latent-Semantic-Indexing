# Datos Demo — Dev 2

Este archivo explica cómo cargar datos de ejemplo para validar rápidamente la capa de persistencia.

Los datos demo están en:

```text
sql/demo/insert_demo_data.sql
```

Este archivo **no es una migración Flyway**. Se ejecuta manualmente cuando se quiere poblar la base con datos de prueba.

---

## Cómo ejecutar la demo

Desde la raíz del proyecto:

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/demo/insert_demo_data.sql
```

Si tu PostgreSQL usa el puerto default, cambia `5433` por `5432`.

---

## Qué inserta la demo

El script inserta un conjunto pequeño pero completo:

| Elemento | Cantidad | Descripción |
|---|---:|---|
| Documentos | 3 | `D1`, `D2`, `D3` sobre estrés, ansiedad y sueño. |
| Términos | 6 | `stress`, `anxiety`, `sleep`, `performance`, `wellbeing`, `support`. |
| Frecuencias | 9 | Relaciones documento-término para representar FrecT. |
| Modelo LSI | 1 | Modelo demo con `k = 2`. |
| Vectores de documentos | 6 | Dos componentes latentes por documento. |
| Vector de consulta | 2 | Dos componentes para la consulta `academic stress anxiety`. |
| Query run | 1 | Ejecución de consulta con métrica `cosine`. |
| Query results | 3 | Ranking de D1, D2 y D3. |

---

## Qué limpia antes de insertar

El script limpia datos transaccionales/demo antes de insertar nuevos registros:

```sql
DELETE FROM query_results;
DELETE FROM query_runs;
DELETE FROM latent_query_vectors;
DELETE FROM latent_document_vectors;
DELETE FROM latent_models;
DELETE FROM document_terms;
DELETE FROM documents;
DELETE FROM terms;
```

No elimina los recursos lingüísticos creados por seeds:

```text
stop_words
suffix_rules
term_synonyms
polysemy_rules
```

---

## Cuándo usarlo

Úsalo cuando quieras:

- Validar que las consultas de `sql/queries/` muestran información.
- Tomar capturas para el reporte.
- Probar que documentos, términos, FrecT, LSI y query logs se conectan correctamente.
- Dejar una base local con datos consistentes después de correr tests.

---

## Consultas recomendadas después de cargar demo

Después de ejecutar la demo, se recomienda correr:

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/01_count_documents.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/04_document_frequency_matrix.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/06_lsi_document_vectors.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/08_query_runs_and_results.sql
```

---

## Nota importante

Este archivo es únicamente para demostración y verificación manual.  
No debe colocarse dentro de `sql/migrations/`, porque Flyway no debe cargar datos demo como parte obligatoria del esquema.
