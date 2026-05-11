# Consultas de Verificación — Dev 2

Las consultas de verificación están en:

```text
sql/queries/
```

Estas consultas sirven para revisar manualmente el estado de la base de datos y generar evidencia para la integración o el reporte.

No son migraciones Flyway. Se ejecutan manualmente con `psql`.

---

## Cómo ejecutar una consulta

Ejemplo:

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/01_count_documents.sql
```

Si tu PostgreSQL usa otro puerto, ajusta `5433`.

---

## Consultas disponibles

| Archivo | Propósito |
|---|---|
| `01_count_documents.sql` | Cuenta cuántos documentos hay cargados. |
| `02_list_documents.sql` | Lista documentos con código, título, fuente, idioma y fecha. |
| `03_list_terms.sql` | Lista los términos del vocabulario. |
| `04_document_frequency_matrix.sql` | Muestra FrecT en formato relacional. |
| `05_lsi_models.sql` | Lista los modelos LSI guardados. |
| `06_lsi_document_vectors.sql` | Muestra vectores latentes por documento. |
| `07_lsi_query_vectors.sql` | Muestra vectores latentes de consultas. |
| `08_query_runs_and_results.sql` | Muestra consultas ejecutadas y resultados rankeados. |
| `09_linguistic_resources_summary.sql` | Resume conteos de recursos lingüísticos. |
| `10_linguistic_resources_detail.sql` | Lista recursos lingüísticos y semánticos en detalle. |

---

## Orden recomendado para revisión manual

Después de correr Flyway y cargar demo data, ejecutar en este orden:

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/01_count_documents.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/02_list_documents.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/03_list_terms.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/04_document_frequency_matrix.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/05_lsi_models.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/06_lsi_document_vectors.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/07_lsi_query_vectors.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/08_query_runs_and_results.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/09_linguistic_resources_summary.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/10_linguistic_resources_detail.sql
```

---

## Resultados esperados con datos demo

Si se ejecutó:

```text
sql/demo/insert_demo_data.sql
```

entonces se espera ver:

| Consulta | Resultado esperado |
|---|---|
| `01_count_documents.sql` | 3 documentos. |
| `02_list_documents.sql` | D1, D2 y D3. |
| `03_list_terms.sql` | 6 términos. |
| `04_document_frequency_matrix.sql` | 9 relaciones documento-término. |
| `05_lsi_models.sql` | 1 modelo LSI. |
| `06_lsi_document_vectors.sql` | 6 componentes vectoriales. |
| `07_lsi_query_vectors.sql` | 2 componentes de query. |
| `08_query_runs_and_results.sql` | Ranking top-3. |
| `09_linguistic_resources_summary.sql` | Conteos de seeds lingüísticos. |
| `10_linguistic_resources_detail.sql` | Recursos lingüísticos detallados. |

---

## Nota

Si algunas consultas aparecen vacías, puede deberse a que:

- No se ha cargado `sql/demo/insert_demo_data.sql`.
- Las pruebas automatizadas limpiaron algunas tablas.
- La base fue reiniciada y solo tiene migraciones/seeds.
- Se está consultando otra base o puerto.
