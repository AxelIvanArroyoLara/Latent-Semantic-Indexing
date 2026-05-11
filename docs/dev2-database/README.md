# Dev 2 — Módulo de Base de Datos y Persistencia

Esta carpeta documenta el módulo de base de datos y persistencia desarrollado para el proyecto **LSI Document Base**.

El objetivo de este módulo es proporcionar una capa de persistencia relacional confiable para el sistema. Esta capa define el esquema en PostgreSQL, administra la evolución de la base de datos mediante migraciones Flyway, expone repositorios Java para acceder a la información y deja pruebas, consultas de verificación y datos demo para facilitar la integración con el resto del equipo.

---

## Propósito del módulo

El módulo de persistencia se encarga de almacenar y recuperar los principales artefactos requeridos por el sistema de base documental con LSI:

- Documentos fuente y su versión normalizada.
- Recursos lingüísticos como stop words, reglas de sufijos, sinónimos y reglas de polisemia.
- Términos del vocabulario y frecuencias documento-término.
- Representación relacional de la matriz de frecuencias FrecT.
- Metadatos de modelos LSI y vectores latentes.
- Ejecuciones de consulta y resultados rankeados.

Este módulo permite que el resto del sistema interactúe con PostgreSQL mediante repositorios Java, sin que cada módulo tenga que escribir consultas SQL directamente.

---

## Qué hace este módulo

Este módulo proporciona:

- Creación del esquema PostgreSQL mediante migraciones Flyway.
- Datos semilla para recursos lingüísticos y semánticos.
- Configuración reutilizable de conexión a base de datos con HikariCP.
- Clases repositorio dentro de `com.lsi.persistence`.
- Pruebas automatizadas para la conexión y los repositorios.
- Consultas SQL manuales de verificación.
- Datos demo para validar la capa de persistencia de extremo a extremo.

---

## Qué no hace este módulo

Este módulo solo se encarga de la persistencia. No implementa la lógica interna de los demás módulos del proyecto.

No se encarga de:

- Leer archivos `.txt` desde `data/raw`.
- Tokenizar documentos.
- Eliminar stop words.
- Aplicar stemming.
- Resolver sinónimos o polisemia.
- Calcular frecuencias documento-término.
- Ejecutar SVD.
- Calcular similitud coseno, Jaccard o distancia euclidiana.
- Rankear resultados de consulta.

Estas responsabilidades pertenecen a los módulos de ingesta, preprocesamiento, semántica, indexación, LSI y consulta.

---

## Estructura principal de documentación

```text
docs/dev2-database/
  README.md
  setup.md
  schema.md
  repositories.md
  testing.md
  demo-data.md
  verification-queries.md
  decisions.md
```

---

## Archivos principales implementados

```text
src/main/resources/application.properties

src/main/java/com/lsi/config/
  DatabaseConfig.java

src/main/java/com/lsi/persistence/
  DocumentRepository.java
  TermRepository.java
  FrequencyRepository.java
  LsiRepository.java
  QueryLogRepository.java

src/test/java/com/lsi/config/
  DatabaseConfigTest.java

src/test/java/com/lsi/persistence/
  DocumentRepositoryTest.java
  TermRepositoryTest.java
  FrequencyRepositoryTest.java
  LsiRepositoryTest.java
  QueryLogRepositoryTest.java

sql/migrations/
  V1__create_documents.sql
  V2__create_linguistic_tables.sql
  V3__create_terms_and_frequencies.sql
  V4__create_latent_space_tables.sql
  V5__create_query_logging_tables.sql
  V6__seed_stop_words.sql
  V7__seed_suffix_rules.sql
  V8__seed_synonyms_and_polysemy.sql

sql/queries/
  01_count_documents.sql
  02_list_documents.sql
  03_list_terms.sql
  04_document_frequency_matrix.sql
  05_lsi_models.sql
  06_lsi_document_vectors.sql
  07_lsi_query_vectors.sql
  08_query_runs_and_results.sql
  09_linguistic_resources_summary.sql
  10_linguistic_resources_detail.sql

sql/demo/
  insert_demo_data.sql
```

---

## Índice de documentación

| Archivo | Propósito |
|---|---|
| [`setup.md`](setup.md) | Explica la configuración local de PostgreSQL, `application.properties`, ejecución de Flyway y comandos para reiniciar el esquema en desarrollo. |
| [`schema.md`](schema.md) | Describe las tablas de la base de datos y cómo soportan documentos, recursos lingüísticos, FrecT, LSI y logs de consulta. |
| [`repositories.md`](repositories.md) | Documenta las clases repositorio de Java y sus métodos principales. |
| [`testing.md`](testing.md) | Explica cómo ejecutar las pruebas y qué valida cada clase de test. |
| [`demo-data.md`](demo-data.md) | Explica cómo cargar datos demo manualmente y qué registros inserta. |
| [`verification-queries.md`](verification-queries.md) | Lista las consultas SQL manuales disponibles en `sql/queries/`. |
| [`decisions.md`](decisions.md) | Registra las decisiones técnicas principales tomadas para este módulo. |

---

## Orden recomendado para levantar el módulo

Para usar este módulo localmente, se recomienda seguir este orden:

1. Instalar PostgreSQL.
2. Crear la base local `lsi_documentbase`.
3. Configurar `src/main/resources/application.properties`.
4. Ejecutar las migraciones Flyway.
5. Ejecutar las pruebas automatizadas.
6. Cargar datos demo, si se desea validar la base con información de ejemplo.
7. Ejecutar las consultas SQL de verificación.

Las instrucciones detalladas estarán en [`setup.md`](setup.md).

---

## Comandos rápidos

Ejecutar migraciones Flyway:

```powershell
mvn flyway:migrate
```

Consultar el estado de Flyway:

```powershell
mvn flyway:info
```

Ejecutar todas las pruebas:

```powershell
mvn test
```

Cargar datos demo manualmente:

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/demo/insert_demo_data.sql
```

Ejecutar una consulta de verificación:

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/01_count_documents.sql
```

El puerto puede cambiar según la instalación local de PostgreSQL. Algunas máquinas pueden usar `5432` en lugar de `5433`.

---

## Notas de integración

Los demás módulos deben usar los repositorios ubicados en:

```text
com.lsi.persistence
```

en lugar de acceder directamente a PostgreSQL.

Puntos de integración sugeridos:

| Módulo | Repositorio recomendado |
|---|---|
| Ingesta | `DocumentRepository` |
| Indexación | `TermRepository`, `FrequencyRepository` |
| LSI / SVD | `LsiRepository` |
| Motor de consulta | `QueryLogRepository` |

Esto mantiene el acceso SQL centralizado y hace que el sistema sea más fácil de probar, mantener e integrar.
