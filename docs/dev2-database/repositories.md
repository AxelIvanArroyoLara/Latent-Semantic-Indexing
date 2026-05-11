# Repositorios Java — Dev 2

Este documento describe los repositorios Java implementados para el módulo de base de datos y persistencia del proyecto **LSI Document Base**.

Los repositorios se encuentran en:

```text
src/main/java/com/lsi/persistence/
```

Su objetivo es encapsular el acceso SQL a PostgreSQL para que los demás módulos del sistema no tengan que escribir consultas SQL directamente.

---

## Propósito de los repositorios

Los repositorios funcionan como una capa intermedia entre la lógica del sistema y la base de datos.

En lugar de que otros módulos hagan consultas SQL manuales, pueden usar métodos Java como:

```java
documentRepository.findByCode("D1");
termRepository.findByNormalizedTerm("stress");
frequencyRepository.saveFrequency(documentId, termId, 3.0, 0.90);
```

Internamente, cada repositorio:

1. Solicita una conexión a la base de datos mediante `DatabaseConfig`.
2. Usa `PreparedStatement` para ejecutar consultas SQL de forma segura.
3. Convierte los resultados de PostgreSQL en objetos Java simples.
4. Devuelve resultados usando `Optional`, `List` o valores simples según el caso.

---

## Relación con `DatabaseConfig`

Todos los repositorios usan la clase:

```text
src/main/java/com/lsi/config/DatabaseConfig.java
```

Esta clase crea un `DataSource` basado en HikariCP.

El flujo general es:

```text
Repositorio Java
    ↓
DatabaseConfig.getDataSource()
    ↓
HikariCP
    ↓
PostgreSQL
```

Esto permite reutilizar conexiones y mantener la configuración centralizada.

---

## Convención general

Los repositorios siguen estas ideas:

- No implementan lógica de preprocesamiento.
- No calculan frecuencias.
- No ejecutan SVD.
- No calculan similitudes ni rankings.
- Solo guardan, consultan, actualizan o eliminan datos.
- Lanzan `RepositoryException` cuando ocurre un error SQL.
- Usan `PreparedStatement` para evitar concatenar SQL manualmente.
- Usan `record` internos para representar filas recuperadas de la base.

---

## `DocumentRepository`

Archivo:

```text
src/main/java/com/lsi/persistence/DocumentRepository.java
```

Tabla principal:

```text
documents
```

### Responsabilidad

Este repositorio se encarga de guardar y consultar documentos dentro de la base documental.

No lee archivos `.txt` directamente. El módulo de ingesta debe encargarse de leer archivos y después pasar la información al repositorio.

### Métodos principales

| Método | Propósito |
|---|---|
| `save(...)` | Inserta un documento y devuelve su `document_id`. |
| `findById(long documentId)` | Busca un documento por identificador. |
| `findByCode(String code)` | Busca un documento por código, por ejemplo `D1`. |
| `findAll()` | Lista todos los documentos almacenados. |
| `deleteAll()` | Elimina todos los documentos. Se usa principalmente en pruebas. |

### Datos que guarda

El método `save(...)` recibe:

```java
String code,
String title,
String source,
String rawText,
String normalizedText,
String languageCode
```

Ejemplo conceptual:

```java
long documentId = documentRepository.save(
    "D1",
    "Academic Stress in University Students",
    "data/raw/D1.txt",
    "Academic stress affects university students.",
    "academic stress affects university students",
    "en"
);
```

### Tipo de retorno principal

El repositorio define el record:

```java
DocumentRow
```

Este representa una fila de la tabla `documents`.

Campos principales:

```text
documentId
code
title
source
rawText
normalizedText
languageCode
createdAt
```

---

## `TermRepository`

Archivo:

```text
src/main/java/com/lsi/persistence/TermRepository.java
```

Tabla principal:

```text
terms
```

### Responsabilidad

Este repositorio se encarga de guardar y consultar términos del vocabulario maestro.

No aplica stemming, no resuelve sinónimos y no decide polisemia. Solo persiste el resultado que otros módulos produzcan.

### Métodos principales

| Método | Propósito |
|---|---|
| `save(...)` | Inserta un término y devuelve su `term_id`. |
| `findById(long termId)` | Busca un término por identificador. |
| `findByNormalizedTerm(String normalizedTerm)` | Busca un término por su forma normalizada. |
| `findByNormalizedTermAndSense(String normalizedTerm, String senseLabel)` | Busca un término considerando también su sentido semántico. |
| `findAll()` | Lista todos los términos. |
| `deleteAll()` | Elimina todos los términos. Se usa principalmente en pruebas. |

### Datos que guarda

El método `save(...)` recibe:

```java
String normalizedTerm,
String canonicalTerm,
String stem,
String senseLabel
```

Ejemplo conceptual:

```java
long termId = termRepository.save(
    "performance",
    "performance",
    "perform",
    "academic_performance"
);
```

### Sobre `senseLabel`

`senseLabel` puede ser `null` cuando el término no requiere desambiguación semántica.

Ejemplo:

```java
termRepository.save("stress", "stress", "stress", null);
```

También puede contener una etiqueta cuando aplica polisemia:

```java
termRepository.save("support", "support", "support", "institutional_support");
```

### Tipo de retorno principal

El repositorio define el record:

```java
TermRow
```

Campos principales:

```text
termId
normalizedTerm
canonicalTerm
stem
senseLabel
```

---

## `FrequencyRepository`

Archivo:

```text
src/main/java/com/lsi/persistence/FrequencyRepository.java
```

Tabla principal:

```text
document_terms
```

### Responsabilidad

Este repositorio se encarga de persistir la relación entre documentos y términos.

Esta tabla representa la matriz de frecuencias **FrecT** en formato relacional.

No calcula frecuencias; solo guarda los valores calculados por el módulo de indexación.

### Métodos principales

| Método | Propósito |
|---|---|
| `saveFrequency(...)` | Guarda o actualiza una frecuencia documento-término. |
| `findByDocumentIdAndTermId(long documentId, long termId)` | Busca una frecuencia específica. |
| `findByDocumentId(long documentId)` | Lista todas las frecuencias asociadas a un documento. |
| `findByTermId(long termId)` | Lista todas las frecuencias asociadas a un término. |
| `findAll()` | Lista todas las frecuencias almacenadas. |
| `deleteAll()` | Elimina todas las frecuencias. Se usa principalmente en pruebas. |

### Datos que guarda

El método `saveFrequency(...)` recibe:

```java
long documentId,
long termId,
double rawFrequency,
double weightedFrequency
```

Ejemplo conceptual:

```java
frequencyRepository.saveFrequency(
    documentId,
    termId,
    3.0,
    0.90
);
```

### Comportamiento ante duplicados

La tabla `document_terms` tiene una llave primaria compuesta:

```sql
PRIMARY KEY (document_id, term_id)
```

Por eso, si se intenta guardar otra frecuencia para el mismo documento y término, el repositorio actualiza los valores mediante:

```sql
ON CONFLICT (document_id, term_id)
DO UPDATE
```

Esto es útil porque el módulo de indexación puede recalcular frecuencias y guardar nuevamente el resultado.

### Tipo de retorno principal

El repositorio define el record:

```java
FrequencyRow
```

Campos principales:

```text
documentId
termId
rawFrequency
weightedFrequency
```

---

## `LsiRepository`

Archivo:

```text
src/main/java/com/lsi/persistence/LsiRepository.java
```

Tablas principales:

```text
latent_models
latent_document_vectors
latent_query_vectors
```

### Responsabilidad

Este repositorio se encarga de guardar y consultar modelos LSI y vectores latentes.

No ejecuta SVD ni calcula proyecciones. El módulo LSI debe calcular los vectores y después usar este repositorio para persistirlos.

### Métodos para modelos LSI

| Método | Propósito |
|---|---|
| `saveLatentModel(...)` | Guarda metadatos de un modelo LSI y devuelve su `latent_model_id`. |
| `findLatentModelById(long latentModelId)` | Busca un modelo LSI por identificador. |
| `findAllLatentModels()` | Lista todos los modelos LSI guardados. |

Datos esperados:

```java
int kValue,
String sourceMatrixType,
String notes
```

Ejemplo conceptual:

```java
long modelId = lsiRepository.saveLatentModel(
    2,
    "raw_frequency",
    "Demo LSI model with k=2"
);
```

### Métodos para vectores de documentos

| Método | Propósito |
|---|---|
| `saveDocumentVectorComponent(...)` | Guarda o actualiza un componente del vector latente de un documento. |
| `findDocumentVector(long latentModelId, long documentId)` | Recupera el vector completo de un documento para un modelo. |
| `findDocumentVectorsByModel(long latentModelId)` | Recupera todos los vectores documentales de un modelo. |

Datos esperados para un componente:

```java
long latentModelId,
long documentId,
int componentIndex,
double componentValue
```

Ejemplo conceptual:

```java
lsiRepository.saveDocumentVectorComponent(
    modelId,
    documentId,
    0,
    0.82
);
```

### Métodos para vectores de consulta

| Método | Propósito |
|---|---|
| `saveQueryVectorComponent(...)` | Guarda un componente del vector latente de una consulta. |
| `findQueryVectorsByModel(long latentModelId)` | Recupera vectores de consulta asociados a un modelo. |

Datos esperados:

```java
long latentModelId,
String queryText,
int componentIndex,
double componentValue
```

### Método de limpieza

| Método | Propósito |
|---|---|
| `deleteAll()` | Elimina vectores y modelos LSI. Se usa principalmente en pruebas. |

El orden de limpieza es importante:

```text
latent_document_vectors
latent_query_vectors
latent_models
```

Los vectores deben borrarse antes que los modelos porque dependen de `latent_models`.

### Records principales

```java
LatentModelRow
LatentDocumentVectorRow
LatentQueryVectorRow
```

---

## `QueryLogRepository`

Archivo:

```text
src/main/java/com/lsi/persistence/QueryLogRepository.java
```

Tablas principales:

```text
query_runs
query_results
```

### Responsabilidad

Este repositorio se encarga de guardar el historial de consultas ejecutadas y sus resultados ordenados.

No calcula similitudes, distancias ni rankings. El motor de consulta debe calcular esos valores y después persistirlos.

### Métodos para ejecuciones de consulta

| Método | Propósito |
|---|---|
| `saveQueryRun(...)` | Guarda una ejecución de consulta y devuelve su `query_run_id`. |
| `findQueryRunById(long queryRunId)` | Busca una ejecución por identificador. |
| `findAllQueryRuns()` | Lista todas las ejecuciones de consulta. |

Datos esperados:

```java
String queryText,
String similarityMetric,
int nValue
```

Ejemplo conceptual:

```java
long queryRunId = queryLogRepository.saveQueryRun(
    "academic stress anxiety",
    "cosine",
    3
);
```

### Métodos para resultados rankeados

| Método | Propósito |
|---|---|
| `saveQueryResult(...)` | Guarda o actualiza un resultado rankeado. |
| `findResultsByQueryRunId(long queryRunId)` | Lista resultados de una consulta específica. |
| `findAllQueryResults()` | Lista todos los resultados almacenados. |

Datos esperados:

```java
long queryRunId,
int rankPosition,
long documentId,
double score
```

Ejemplo conceptual:

```java
queryLogRepository.saveQueryResult(
    queryRunId,
    1,
    documentId,
    0.95
);
```

### Comportamiento ante duplicados

La tabla `query_results` tiene llave primaria compuesta:

```sql
PRIMARY KEY (query_run_id, rank_position)
```

Si se guarda de nuevo un resultado para la misma posición dentro de la misma consulta, se actualiza el documento y el puntaje.

### Método de limpieza

| Método | Propósito |
|---|---|
| `deleteAll()` | Elimina resultados y ejecuciones de consulta. Se usa principalmente en pruebas. |

El orden de limpieza es:

```text
query_results
query_runs
```

---

## Uso de `Optional`

Algunos métodos devuelven:

```java
Optional<T>
```

Esto significa que el resultado puede existir o no existir.

Ejemplo:

```java
Optional<DocumentRepository.DocumentRow> result =
    documentRepository.findByCode("D1");
```

Casos posibles:

```text
Optional.of(document) → sí se encontró el documento.
Optional.empty()      → no existe un documento con ese código.
```

Esto evita regresar `null` y obliga a manejar explícitamente el caso en que no haya resultado.

---

## Uso de `List`

Los métodos que pueden devolver varios resultados usan:

```java
List<T>
```

Ejemplo:

```java
List<DocumentRepository.DocumentRow> documents =
    documentRepository.findAll();
```

Si no hay resultados, se devuelve una lista vacía.

---

## Uso de `PreparedStatement`

Los repositorios usan `PreparedStatement` para enviar parámetros a SQL de forma segura.

Ejemplo conceptual:

```java
String sql = "SELECT * FROM documents WHERE code = ?";
PreparedStatement statement = connection.prepareStatement(sql);
statement.setString(1, code);
```

Esto evita concatenar SQL manualmente y reduce errores.

---

## Manejo de errores

Cada repositorio define una excepción interna:

```java
RepositoryException
```

Esta excepción envuelve errores SQL y agrega contexto sobre la operación que falló.

Ejemplo conceptual:

```java
throw new RepositoryException("Could not save document with code: " + code, e);
```

Esto ayuda a depurar errores de persistencia sin exponer directamente detalles de bajo nivel en otros módulos.

---

## Recomendaciones de integración

Los demás módulos deben usar estos repositorios en lugar de escribir SQL directamente.

| Módulo | Repositorios recomendados |
|---|---|
| Ingesta | `DocumentRepository` |
| Indexación | `TermRepository`, `FrequencyRepository` |
| LSI / SVD | `LsiRepository` |
| Motor de consulta | `QueryLogRepository` |

Esto mantiene el acceso a la base de datos centralizado en `com.lsi.persistence`.

---

## Estado actual

Los repositorios implementados ya cuentan con pruebas automatizadas y fueron validados con `mvn test`.

Repositorios implementados:

- `DocumentRepository`
- `TermRepository`
- `FrequencyRepository`
- `LsiRepository`
- `QueryLogRepository`

Cada uno tiene pruebas asociadas en:

```text
src/test/java/com/lsi/persistence/
```
