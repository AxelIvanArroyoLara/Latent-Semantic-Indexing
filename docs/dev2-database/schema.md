# Esquema de Base de Datos — Dev 2

Este documento describe el esquema relacional implementado para el módulo de base de datos y persistencia del proyecto **LSI Document Base**.

El esquema está pensado para soportar la carga documental, los recursos lingüísticos, la matriz de frecuencias **FrecT**, la persistencia de modelos LSI y el historial de consultas del sistema.

---

## Organización general del esquema

Las tablas están organizadas en cinco grupos principales:

1. **Documentos**
2. **Recursos lingüísticos y semánticos**
3. **Vocabulario y matriz FrecT**
4. **Persistencia LSI**
5. **Historial de consultas**

Las migraciones que crean estas tablas se encuentran en:

```text
sql/migrations/
```

---

## Migraciones principales

| Migración | Propósito |
|---|---|
| `V1__create_documents.sql` | Crea la tabla principal de documentos. |
| `V2__create_linguistic_tables.sql` | Crea tablas para stop words, suffix rules, sinónimos y polisemia. |
| `V3__create_terms_and_frequencies.sql` | Crea tablas de términos y frecuencias documento-término. |
| `V4__create_latent_space_tables.sql` | Crea tablas para modelos LSI y vectores latentes. |
| `V5__create_query_logging_tables.sql` | Crea tablas para ejecuciones de consulta y resultados rankeados. |
| `V6__seed_stop_words.sql` | Inserta stop words iniciales. |
| `V7__seed_suffix_rules.sql` | Inserta reglas de sufijos iniciales. |
| `V8__seed_synonyms_and_polysemy.sql` | Inserta sinónimos y reglas de polisemia iniciales. |

---

## 1. Documentos

### `documents`

Esta tabla almacena los documentos fuente y su versión normalizada.

| Campo | Tipo | Descripción |
|---|---|---|
| `document_id` | `BIGSERIAL PRIMARY KEY` | Identificador único del documento. |
| `code` | `VARCHAR(20) UNIQUE NOT NULL` | Código corto del documento, por ejemplo `D1`, `D2`, `D3`. |
| `title` | `VARCHAR(255) NOT NULL` | Título del documento. |
| `source` | `TEXT` | Ruta, URL o referencia de origen del documento. |
| `raw_text` | `TEXT` | Texto original del documento. |
| `normalized_text` | `TEXT` | Texto normalizado por el pipeline. |
| `language_code` | `VARCHAR(10)` | Idioma del documento. Para este proyecto se usa principalmente `en`. |
| `created_at` | `TIMESTAMP` | Fecha y hora de inserción. |

### Propósito dentro del proyecto

La tabla `documents` permite persistir la colección documental usada por el sistema. Esta colección será consumida por los módulos de preprocesamiento, indexación, LSI y consulta.

El módulo de persistencia no lee archivos `.txt` directamente; solo guarda y consulta los documentos ya recibidos por contrato.

---

## 2. Recursos lingüísticos y semánticos

Estas tablas permiten guardar recursos usados por los módulos de preprocesamiento y semántica. Se separan de la lógica Java para que sean trazables, verificables y modificables de forma controlada.

---

### `stop_words`

Almacena palabras vacías que deben eliminarse durante el preprocesamiento.

| Campo | Tipo | Descripción |
|---|---|---|
| `stop_word_id` | `BIGSERIAL PRIMARY KEY` | Identificador de la stop word. |
| `language_code` | `VARCHAR(10) NOT NULL` | Idioma de la palabra. |
| `word` | `VARCHAR(100) NOT NULL` | Palabra vacía. |

Restricción principal:

```sql
UNIQUE (language_code, word)
```

Esto evita repetir la misma stop word para el mismo idioma.

---

### `suffix_rules`

Almacena reglas para recorte o reemplazo de sufijos.

| Campo | Tipo | Descripción |
|---|---|---|
| `suffix_rule_id` | `BIGSERIAL PRIMARY KEY` | Identificador de la regla. |
| `suffix` | `VARCHAR(50) NOT NULL` | Sufijo a detectar. |
| `replacement` | `VARCHAR(50) NOT NULL` | Reemplazo que se aplicará. Puede ser vacío. |
| `priority` | `INTEGER NOT NULL` | Prioridad de aplicación. Mayor valor significa mayor prioridad. |
| `enabled` | `BOOLEAN NOT NULL` | Indica si la regla está activa. |

### Propósito de `priority`

La prioridad permite aplicar primero reglas más específicas. Por ejemplo, conviene evaluar `ies -> y` antes que simplemente eliminar `s`.

---

### `term_synonyms`

Almacena sinónimos controlados para canonicalización semántica.

| Campo | Tipo | Descripción |
|---|---|---|
| `term_synonym_id` | `BIGSERIAL PRIMARY KEY` | Identificador del sinónimo. |
| `canonical_term` | `VARCHAR(150) NOT NULL` | Término canónico. |
| `synonym_term` | `VARCHAR(150) NOT NULL` | Término considerado sinónimo. |
| `direction_type` | `VARCHAR(20) NOT NULL` | Dirección de la relación: `one_way` o `bidirectional`. |

Restricción principal:

```sql
CHECK (direction_type IN ('one_way', 'bidirectional'))
```

### Ejemplo conceptual

```text
canonical_term = anxiety
synonym_term   = worry
direction_type = bidirectional
```

Esto indica que `worry` puede mapearse al término canónico `anxiety`.

---

### `polysemy_rules`

Almacena reglas simples para resolver polisemia de forma controlada por contexto.

| Campo | Tipo | Descripción |
|---|---|---|
| `polysemy_rule_id` | `BIGSERIAL PRIMARY KEY` | Identificador de la regla. |
| `ambiguous_term` | `VARCHAR(150) NOT NULL` | Término ambiguo. |
| `context_token` | `VARCHAR(150) NOT NULL` | Token de contexto que ayuda a resolver el sentido. |
| `assigned_sense` | `VARCHAR(150) NOT NULL` | Sentido asignado. |
| `priority` | `INTEGER NOT NULL` | Prioridad de aplicación. |

### Ejemplo conceptual

```text
ambiguous_term = support
context_token  = university
assigned_sense = institutional_support
```

Esto permite distinguir entre soporte institucional, soporte emocional u otros sentidos del mismo término.

---

## 3. Vocabulario y matriz FrecT

Este grupo representa los términos normalizados y sus frecuencias dentro de los documentos.

---

### `terms`

Almacena el vocabulario maestro del sistema.

| Campo | Tipo | Descripción |
|---|---|---|
| `term_id` | `BIGSERIAL PRIMARY KEY` | Identificador único del término. |
| `normalized_term` | `VARCHAR(150) NOT NULL` | Término normalizado. |
| `canonical_term` | `VARCHAR(150) NOT NULL` | Término canónico después de tratamiento semántico. |
| `stem` | `VARCHAR(150) NOT NULL` | Raíz o stem del término. |
| `sense_label` | `VARCHAR(150)` | Etiqueta de sentido cuando aplica polisemia. Puede ser `NULL`. |

Restricción principal:

```sql
UNIQUE (normalized_term, sense_label)
```

### Sobre `sense_label`

No todos los términos tienen polisemia. Por eso `sense_label` puede ser nulo.

Ejemplos:

```text
stress  → sense_label NULL
support → institutional_support
support → emotional_support
```

---

### `document_terms`

Representa la matriz de frecuencias **FrecT** en forma relacional dispersa.

| Campo | Tipo | Descripción |
|---|---|---|
| `document_id` | `BIGINT NOT NULL` | Documento asociado. |
| `term_id` | `BIGINT NOT NULL` | Término asociado. |
| `raw_frequency` | `DOUBLE PRECISION NOT NULL` | Frecuencia cruda del término en el documento. |
| `weighted_frequency` | `DOUBLE PRECISION NOT NULL` | Frecuencia ponderada, si el sistema la calcula. |

Llave primaria compuesta:

```sql
PRIMARY KEY (document_id, term_id)
```

Llaves foráneas:

```sql
document_id → documents(document_id)
term_id     → terms(term_id)
```

Restricciones:

```sql
CHECK (raw_frequency >= 0)
CHECK (weighted_frequency >= 0)
```

### Propósito dentro del proyecto

Esta tabla es una de las más importantes del módulo. Permite guardar la matriz FrecT sin crear columnas dinámicas para cada documento o término.

En vez de guardar una matriz física con muchas columnas, se guarda una fila por cada relación documento-término:

```text
document_id | term_id | raw_frequency | weighted_frequency
```

Esto facilita consultas SQL, persistencia y escalabilidad.

---

## 4. Persistencia LSI

Este grupo almacena los resultados producidos por el módulo de LSI/SVD. La base de datos no calcula SVD; solamente persiste los resultados recibidos.

---

### `latent_models`

Guarda metadatos de cada corrida LSI.

| Campo | Tipo | Descripción |
|---|---|---|
| `latent_model_id` | `BIGSERIAL PRIMARY KEY` | Identificador del modelo LSI. |
| `k_value` | `INTEGER NOT NULL` | Número de dimensiones latentes conservadas. |
| `source_matrix_type` | `VARCHAR(50) NOT NULL` | Tipo de matriz usada como entrada. |
| `created_at` | `TIMESTAMP` | Fecha de creación del modelo. |
| `notes` | `TEXT` | Notas descriptivas del modelo. |

Restricciones:

```sql
CHECK (k_value > 0)
CHECK (source_matrix_type IN ('raw_frequency', 'weighted_frequency'))
```

---

### `latent_document_vectors`

Guarda los vectores latentes asociados a documentos.

| Campo | Tipo | Descripción |
|---|---|---|
| `latent_model_id` | `BIGINT NOT NULL` | Modelo LSI asociado. |
| `document_id` | `BIGINT NOT NULL` | Documento asociado. |
| `component_index` | `INTEGER NOT NULL` | Índice de la dimensión latente. |
| `component_value` | `DOUBLE PRECISION NOT NULL` | Valor numérico del componente. |

Llave primaria compuesta:

```sql
PRIMARY KEY (latent_model_id, document_id, component_index)
```

### Motivo de almacenar por componente

Los vectores se guardan por componente para evitar columnas dinámicas como `component_1`, `component_2`, etc. Esto permite usar cualquier valor de `k`.

---

### `latent_query_vectors`

Guarda componentes de vectores de consulta proyectados al espacio latente.

| Campo | Tipo | Descripción |
|---|---|---|
| `latent_query_vector_id` | `BIGSERIAL PRIMARY KEY` | Identificador del componente almacenado. |
| `latent_model_id` | `BIGINT NOT NULL` | Modelo LSI asociado. |
| `query_text` | `TEXT NOT NULL` | Texto original de la consulta. |
| `component_index` | `INTEGER NOT NULL` | Índice de la dimensión latente. |
| `component_value` | `DOUBLE PRECISION NOT NULL` | Valor del componente. |
| `created_at` | `TIMESTAMP` | Fecha de inserción. |

---

## 5. Historial de consultas

Este grupo permite registrar consultas ejecutadas y los documentos recuperados como resultado.

---

### `query_runs`

Guarda cada ejecución de consulta.

| Campo | Tipo | Descripción |
|---|---|---|
| `query_run_id` | `BIGSERIAL PRIMARY KEY` | Identificador de la ejecución. |
| `query_text` | `TEXT NOT NULL` | Texto de la consulta. |
| `similarity_metric` | `VARCHAR(50) NOT NULL` | Métrica usada. |
| `n_value` | `INTEGER NOT NULL` | Número de resultados solicitados. |
| `created_at` | `TIMESTAMP` | Fecha de ejecución. |

Restricciones:

```sql
CHECK (similarity_metric IN ('cosine', 'jaccard', 'euclidean'))
CHECK (n_value > 0)
```

---

### `query_results`

Guarda los resultados rankeados de una consulta.

| Campo | Tipo | Descripción |
|---|---|---|
| `query_run_id` | `BIGINT NOT NULL` | Ejecución de consulta asociada. |
| `rank_position` | `INTEGER NOT NULL` | Posición del documento en el ranking. |
| `document_id` | `BIGINT NOT NULL` | Documento recuperado. |
| `score` | `DOUBLE PRECISION NOT NULL` | Puntaje obtenido por el documento. |

Llave primaria compuesta:

```sql
PRIMARY KEY (query_run_id, rank_position)
```

Restricción adicional:

```sql
UNIQUE (query_run_id, document_id)
```

### Propósito

Esta estructura permite guardar resultados como:

```text
query_run_id | rank_position | document_id | score
```

Ejemplo:

```text
8 | 1 | D1 | 0.95
8 | 2 | D2 | 0.88
8 | 3 | D3 | 0.41
```

---

## Relaciones principales

Resumen de relaciones clave:

```text
documents 1 ── n document_terms n ── 1 terms

latent_models 1 ── n latent_document_vectors n ── 1 documents

latent_models 1 ── n latent_query_vectors

query_runs 1 ── n query_results n ── 1 documents
```

---

## Consideraciones importantes

### `BIGSERIAL` vs `BIGINT`

Las tablas principales usan `BIGSERIAL` para IDs autoincrementales.

Ejemplo:

```sql
document_id BIGSERIAL PRIMARY KEY
```

En las tablas hijas se usa `BIGINT` para referenciar esos IDs.

Ejemplo:

```sql
document_id BIGINT NOT NULL
```

Esto es correcto porque `BIGSERIAL` en PostgreSQL es un atajo para un `BIGINT` con autoincremento.

---

### Uso de `ON DELETE CASCADE`

Varias relaciones usan `ON DELETE CASCADE` para que, si se borra un documento o modelo, también se borren sus registros dependientes.

Ejemplo:

```text
Si se borra un documento, también se eliminan sus frecuencias en document_terms.
```

Esto facilita la limpieza durante pruebas y evita datos huérfanos.

---

### Separación entre migraciones, queries y demo

El esquema oficial vive en:

```text
sql/migrations/
```

Las consultas manuales de verificación viven en:

```text
sql/queries/
```

Los datos demo viven en:

```text
sql/demo/
```

Flyway solo ejecuta las migraciones configuradas en `sql/migrations/`. Las consultas y los datos demo se ejecutan manualmente.

---

## Estado del esquema

El esquema actual permite:

- Guardar documentos.
- Guardar recursos lingüísticos.
- Guardar términos.
- Representar FrecT de forma relacional.
- Guardar modelos LSI.
- Guardar vectores latentes de documentos.
- Guardar vectores latentes de consultas.
- Guardar ejecuciones de consulta.
- Guardar resultados rankeados.
- Verificar todo mediante consultas SQL manuales.
