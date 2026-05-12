# Pruebas — Dev 2

Este documento describe las pruebas automatizadas implementadas para el módulo de base de datos y persistencia del proyecto **LSI Document Base**.

Las pruebas validan que la conexión a PostgreSQL funcione correctamente y que los repositorios Java puedan insertar, consultar, actualizar y limpiar datos reales en la base.

---

## Ubicación de las pruebas

Las pruebas se encuentran en:

```text
src/test/java/
```

Estructura principal:

```text
src/test/java/com/lsi/config/
  DatabaseConfigTest.java

src/test/java/com/lsi/persistence/
  DocumentRepositoryTest.java
  TermRepositoryTest.java
  FrequencyRepositoryTest.java
  LsiRepositoryTest.java
  QueryLogRepositoryTest.java
```

---

## Comando para ejecutar todas las pruebas

Desde la raíz del proyecto, ejecutar:

```powershell
mvn test
```

Si todo está funcionando correctamente, Maven debe terminar con:

```text
BUILD SUCCESS
```

También debe mostrar un resumen similar a:

```text
Tests run: ..., Failures: 0, Errors: 0, Skipped: 0
```

El número exacto de pruebas puede cambiar conforme se agreguen nuevos casos.

---

## Requisitos antes de correr las pruebas

Antes de ejecutar `mvn test`, se necesita:

1. Tener PostgreSQL instalado y corriendo.
2. Tener creada la base de datos `lsi_documentbase`.
3. Tener configurado `src/main/resources/application.properties`.
4. Haber ejecutado las migraciones Flyway.

---

## Configuración esperada de `application.properties`

Ejemplo local usado durante el desarrollo:

```properties
db.url=jdbc:postgresql://localhost:5433/lsi_documentbase
db.user=postgres
db.password=
```

Importante: estos valores dependen de cada máquina.

Algunos integrantes pueden usar el puerto default de PostgreSQL:

```properties
db.url=jdbc:postgresql://localhost:5432/lsi_documentbase
```

Por eso, cada integrante debe ajustar este archivo según su instalación local.

---

## Ejecutar migraciones antes de probar

Las pruebas esperan que las tablas ya existan. Para crear el esquema y cargar los seeds iniciales, ejecutar:

```powershell
mvn flyway:migrate
```

Para revisar el estado de las migraciones:

```powershell
mvn flyway:info
```

---

## Reiniciar el esquema durante desarrollo

Si se quiere limpiar completamente la base y volver a ejecutar las migraciones desde cero, entrar a PostgreSQL:

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase
```

Luego ejecutar:

```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;
```

Después salir con:

```sql
\q
```

Y volver a ejecutar:

```powershell
mvn flyway:migrate
```

---

## `DatabaseConfigTest`

Archivo:

```text
src/test/java/com/lsi/config/DatabaseConfigTest.java
```

### Propósito

Verifica que Java pueda abrir una conexión real a PostgreSQL usando `DatabaseConfig`.

### Qué valida

- Que `DatabaseConfig.getConnection()` devuelva una conexión no nula.
- Que la conexión esté abierta.
- Que `application.properties` sea leído correctamente.
- Que HikariCP pueda crear el `DataSource`.

### Importancia

Esta prueba confirma que la infraestructura base funciona:

```text
application.properties
    ↓
DatabaseConfig
    ↓
HikariCP
    ↓
PostgreSQL
```

Si esta prueba falla, los repositorios también fallarán porque todos dependen de la misma configuración de conexión.

---

## `DocumentRepositoryTest`

Archivo:

```text
src/test/java/com/lsi/persistence/DocumentRepositoryTest.java
```

### Propósito

Verifica las operaciones principales sobre la tabla:

```text
documents
```

### Qué valida

- Insertar un documento.
- Buscar un documento por `document_id`.
- Buscar un documento por `code`.
- Listar todos los documentos.
- Confirmar que se devuelva vacío cuando un documento no existe.

### Métodos probados

```text
save(...)
findById(...)
findByCode(...)
findAll()
deleteAll()
```

### Nota

Antes de cada prueba se limpia la tabla `documents` mediante `deleteAll()` para mantener los casos aislados.

---

## `TermRepositoryTest`

Archivo:

```text
src/test/java/com/lsi/persistence/TermRepositoryTest.java
```

### Propósito

Verifica las operaciones principales sobre la tabla:

```text
terms
```

### Qué valida

- Insertar términos.
- Buscar términos por `term_id`.
- Buscar términos por `normalized_term`.
- Buscar términos por `normalized_term` y `sense_label`.
- Manejar términos sin polisemia, donde `sense_label` es `NULL`.
- Manejar términos con una etiqueta de sentido específica.
- Listar todos los términos.
- Confirmar que se devuelva vacío cuando un término no existe.

### Métodos probados

```text
save(...)
findById(...)
findByNormalizedTerm(...)
findByNormalizedTermAndSense(...)
findAll()
deleteAll()
```

### Importancia

Esta prueba valida que el vocabulario pueda persistirse correctamente, incluyendo casos con y sin polisemia.

---

## `FrequencyRepositoryTest`

Archivo:

```text
src/test/java/com/lsi/persistence/FrequencyRepositoryTest.java
```

### Propósito

Verifica la persistencia de la relación documento-término en:

```text
document_terms
```

Esta tabla representa la matriz de frecuencias **FrecT** en forma relacional.

### Qué valida

- Insertar una frecuencia documento-término.
- Buscar una frecuencia específica por `document_id` y `term_id`.
- Actualizar una frecuencia existente usando `ON CONFLICT`.
- Listar frecuencias por documento.
- Listar frecuencias por término.
- Listar todas las frecuencias.
- Confirmar que se devuelva vacío cuando una frecuencia no existe.

### Métodos probados

```text
saveFrequency(...)
findByDocumentIdAndTermId(...)
findByDocumentId(...)
findByTermId(...)
findAll()
deleteAll()
```

### Orden de limpieza

Esta prueba usa varias tablas relacionadas. Por eso, el orden de limpieza es importante:

```text
document_terms
documents
terms
```

Primero se elimina la tabla hija `document_terms`, porque depende de `documents` y `terms`.

---

## `LsiRepositoryTest`

Archivo:

```text
src/test/java/com/lsi/persistence/LsiRepositoryTest.java
```

### Propósito

Verifica la persistencia de modelos LSI y vectores latentes.

Tablas relacionadas:

```text
latent_models
latent_document_vectors
latent_query_vectors
```

### Qué valida

- Insertar un modelo LSI.
- Buscar un modelo LSI por `latent_model_id`.
- Listar modelos LSI.
- Guardar componentes de vectores latentes de documentos.
- Actualizar componentes de vectores documentales existentes.
- Recuperar el vector completo de un documento.
- Recuperar todos los vectores documentales de un modelo.
- Guardar componentes de vectores de consulta.
- Recuperar vectores de consulta por modelo.
- Confirmar que se devuelva vacío cuando un modelo no existe.

### Métodos probados

```text
saveLatentModel(...)
findLatentModelById(...)
findAllLatentModels()
saveDocumentVectorComponent(...)
findDocumentVector(...)
findDocumentVectorsByModel(...)
saveQueryVectorComponent(...)
findQueryVectorsByModel(...)
deleteAll()
```

### Orden de limpieza

Los vectores dependen de los modelos. Por eso, el repositorio limpia en este orden:

```text
latent_document_vectors
latent_query_vectors
latent_models
```

---

## `QueryLogRepositoryTest`

Archivo:

```text
src/test/java/com/lsi/persistence/QueryLogRepositoryTest.java
```

### Propósito

Verifica la persistencia del historial de consultas y resultados rankeados.

Tablas relacionadas:

```text
query_runs
query_results
```

### Qué valida

- Insertar una ejecución de consulta.
- Buscar una ejecución por `query_run_id`.
- Listar ejecuciones de consulta.
- Guardar resultados rankeados.
- Actualizar un resultado cuando ya existe la misma posición de ranking.
- Listar resultados por consulta.
- Listar todos los resultados.
- Confirmar que se devuelva vacío cuando una consulta no existe.
- Confirmar que una consulta sin resultados devuelva una lista vacía.

### Métodos probados

```text
saveQueryRun(...)
findQueryRunById(...)
findAllQueryRuns()
saveQueryResult(...)
findResultsByQueryRunId(...)
findAllQueryResults()
deleteAll()
```

### Orden de limpieza

`query_results` depende de `query_runs`. Por eso se limpia en este orden:

```text
query_results
query_runs
```

Si también se usan documentos de prueba, estos se eliminan después de limpiar los resultados.

---

## Sobre el uso de datos reales en pruebas

Estas pruebas no usan mocks para la base de datos. Se conectan a PostgreSQL real mediante `DatabaseConfig`.

Esto permite validar la integración completa:

```text
Java repository
    ↓
HikariCP DataSource
    ↓
PostgreSQL JDBC Driver
    ↓
PostgreSQL
```

La ventaja es que se prueba el comportamiento real de las tablas, constraints, llaves foráneas y consultas SQL.

La desventaja es que las pruebas requieren una base local correctamente configurada.

---

## Sobre la limpieza de datos

Cada prueba usa `@BeforeEach` para limpiar las tablas relevantes antes de ejecutarse.

Esto ayuda a que las pruebas sean independientes entre sí.

Sin embargo, como las pruebas insertan datos reales, la base puede quedar con registros residuales del último test ejecutado. Esto es normal durante desarrollo.

Para tener una base con datos consistentes para revisión manual, se recomienda ejecutar después:

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/demo/insert_demo_data.sql
```

---

## Sobre `Optional` en las pruebas

Varios métodos de búsqueda devuelven `Optional`.

Ejemplo:

```java
Optional<DocumentRepository.DocumentRow> result =
    repository.findByCode("D1");
```

En las pruebas se valida con:

```java
assertTrue(result.isPresent());
```

cuando se espera que el registro exista.

También se valida con:

```java
assertTrue(result.isEmpty());
```

cuando se espera que no haya resultados.

Esto evita depender de `null` y hace explícito el caso en que una búsqueda no encuentra datos.

---

## Sobre precisión de valores `double`

Algunas pruebas comparan valores `double`, por ejemplo:

```java
assertEquals(0.95, result.get().score());
```

Para los valores simples usados en las pruebas actuales, esto es suficiente.

Si en el futuro se comparan resultados numéricos derivados de cálculos más complejos, se recomienda usar una tolerancia:

```java
assertEquals(0.95, result.get().score(), 0.0001);
```

---

## Advertencia sobre `mvn clean compile` vs `mvn test`

`mvn clean compile` solo valida que el código principal compile.

```powershell
mvn clean compile
```

`mvn test` compila el código principal, compila las pruebas y ejecuta los tests.

```powershell
mvn test
```

Por eso, un proyecto puede compilar correctamente pero fallar en pruebas si hay problemas de JUnit, conexión o datos.

---

## Problemas comunes

### 1. JUnit no se reconoce

Síntomas:

```text
Cannot resolve symbol 'Test'
package org.junit.jupiter.api does not exist
```

Causa común:

El archivo de prueba fue creado por error en:

```text
src/main/java/
```

Debe estar en:

```text
src/test/java/
```

---

### 2. PostgreSQL no responde

Síntomas:

```text
Connection refused
The connection attempt failed
```

Posibles causas:

- PostgreSQL no está corriendo.
- El puerto en `application.properties` es incorrecto.
- La base `lsi_documentbase` no existe.
- Usuario o contraseña incorrectos.

---

### 3. Tablas no existen

Síntomas:

```text
relation "documents" does not exist
```

Causa común:

No se han ejecutado las migraciones Flyway.

Solución:

```powershell
mvn flyway:migrate
```

---

### 4. Fallan deletes por llaves foráneas

Causa común:

Se intenta borrar una tabla padre antes que una tabla hija.

Ejemplo incorrecto:

```text
documents
document_terms
```

Orden correcto:

```text
document_terms
documents
```

---

## Estado actual de pruebas

El módulo cuenta con pruebas para:

- Conexión a base de datos.
- Persistencia de documentos.
- Persistencia de términos.
- Persistencia de FrecT mediante `document_terms`.
- Persistencia de modelos y vectores LSI.
- Persistencia de consultas y resultados rankeados.

El estado esperado al finalizar es:

```text
BUILD SUCCESS
Failures: 0
Errors: 0
```
