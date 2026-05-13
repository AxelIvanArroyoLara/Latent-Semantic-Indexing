# Consultas de Verificación — Base de Datos

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

| Archivo                                 | Propósito                                                      |
| --------------------------------------- | --------------------------------------------------------------- |
| `01_count_documents.sql`              | Cuenta cuántos documentos hay cargados.                        |
| `02_list_documents.sql`               | Lista documentos con código, título, fuente, idioma y fecha.  |
| `03_list_terms.sql`                   | Lista los términos del vocabulario.                            |
| `04_document_frequency_matrix.sql`    | Muestra FrecT en formato relacional.                            |
| `05_lsi_models.sql`                   | Lista los modelos LSI guardados.                                |
| `06_lsi_document_vectors.sql`         | Muestra vectores latentes por documento.                        |
| `07_lsi_query_vectors.sql`            | Muestra vectores latentes de consultas.                         |
| `08_query_runs_and_results.sql`       | Muestra consultas ejecutadas y resultados rankeados.            |
| `09_linguistic_resources_summary.sql` | Resume conteos de recursos lingüísticos.                      |
| `10_linguistic_resources_detail.sql`  | Lista recursos lingüísticos y semánticos en detalle.         |
| `11_compare_documents_similarity.sql` | Verifica comparación entre documentos con similitud/distancia. |
| `12_rank_query_topn_cosine.sql`       | Verifica ranking top-n usando coseno.                           |
| `13_rank_query_topn_jaccard.sql`      | Verifica ranking top-n usando Jaccard.                          |
| `14_rank_query_topn_euclidean.sql`    | Verifica ranking top-n usando distancia euclidiana.             |

---

## Flujo recomendado antes de verificar

Para validar el estado real del proyecto, primero ejecutar el flujo con PDFs reales:

```powershell
mvn flyway:migrate "-Dflyway.password=your_password_here"
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=import-pdfs-db --reset true"
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=final-demo --source db --persist true"
```

Después, ejecutar queries persistentes desde Git Bash:

```bash
mvn exec:java -Dexec.mainClass=com.lsi.App -Dexec.args="query --source db --text \"academic stress anxiety\" --top 5 --metric cosine --persist true"
mvn exec:java -Dexec.mainClass=com.lsi.App -Dexec.args="query --source db --text \"sleep wellbeing\" --top 5 --metric jaccard --persist true"
mvn exec:java -Dexec.mainClass=com.lsi.App -Dexec.args="query --source db --text \"academic stress\" --top 5 --metric euclidean --persist true"
```

---

## Orden recomendado para revisión manual del flujo real

Después de correr el flujo real, ejecutar en este orden:

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/01_count_documents.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/02_list_documents.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/03_list_terms.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/04_document_frequency_matrix.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/05_lsi_models.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/06_lsi_document_vectors.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/08_query_runs_and_results.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/09_linguistic_resources_summary.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/10_linguistic_resources_detail.sql
```

Si tu PostgreSQL usa el puerto `5432`, reemplaza `5433` por `5432`.

---

## Resultados esperados con el flujo real

Después de ejecutar:

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=import-pdfs-db --reset true"
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=final-demo --source db --persist true"
```

y después de persistir queries con `query --source db --persist true`, se espera ver:

| Consulta                                | Resultado esperado                                           |
| --------------------------------------- | ------------------------------------------------------------ |
| `01_count_documents.sql`              | Conteo de documentos importados desde `data/raw`.          |
| `02_list_documents.sql`               | Documentos reales con código, título y fuente de PDF.      |
| `03_list_terms.sql`                   | Términos generados por el pipeline real.                    |
| `04_document_frequency_matrix.sql`    | Filas de FrecT persistidas desde el corpus real.             |
| `05_lsi_models.sql`                   | Al menos un modelo LSI generado por la ejecución real.      |
| `06_lsi_document_vectors.sql`         | Componentes vectoriales para documentos reales.              |
| `08_query_runs_and_results.sql`       | Consultas reales persistidas con ranking, documento y score. |
| `09_linguistic_resources_summary.sql` | Conteos de recursos lingüísticos cargados por migraciones. |
| `10_linguistic_resources_detail.sql`  | Recursos lingüísticos y semánticos detallados.            |

---

## Consultas finales del proyecto

Después de correr `final-demo --source db --persist true`, los archivos finales de verificación SQL pueden ejecutarse así:

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/11_compare_documents_similarity.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/12_rank_query_topn_cosine.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/13_rank_query_topn_jaccard.sql
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/14_rank_query_topn_euclidean.sql
```

Estas consultas sirven como evidencia SQL de las operaciones requeridas por el proyecto: comparación de documentos, ranking por similitud y ranking por disimilitud.

---

## Datos demo legacy

El archivo:

```text
sql/demo/insert_demo_data.sql
```

sigue existiendo para pruebas manuales o experimentos SQL aislados.

No es el flujo recomendado para la entrega final. Para la entrega final, se debe usar:

```text
data/raw/*.pdf
→ import-pdfs-db
→ PostgreSQL
→ final-demo --source db --persist true
```

Si se usa `sql/demo/insert_demo_data.sql`, los resultados esperados serán pequeños y no representarán el corpus real de PDFs.

---

## Nota

Si algunas consultas aparecen vacías, puede deberse a que:

- No se ha ejecutado `import-pdfs-db --reset true`.
- No se ha ejecutado `final-demo --source db --persist true`.
- No se han ejecutado queries con `--persist true`.
- Las pruebas automatizadas limpiaron algunas tablas.
- La base fue reiniciada y solo tiene migraciones/seeds.
- Se está consultando otra base o puerto.

Para reconstruir el estado real de demostración, ejecutar:

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=import-pdfs-db --reset true"
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=final-demo --source db --persist true"
```

Y después, desde Git Bash, persistir las queries:

```bash
mvn exec:java -Dexec.mainClass=com.lsi.App -Dexec.args="query --source db --text \"academic stress anxiety\" --top 5 --metric cosine --persist true"
mvn exec:java -Dexec.mainClass=com.lsi.App -Dexec.args="query --source db --text \"sleep wellbeing\" --top 5 --metric jaccard --persist true"
mvn exec:java -Dexec.mainClass=com.lsi.App -Dexec.args="query --source db --text \"academic stress\" --top 5 --metric euclidean --persist true"
```