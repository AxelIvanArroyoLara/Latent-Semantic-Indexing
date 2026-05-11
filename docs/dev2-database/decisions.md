# Decisiones Técnicas — Dev 2

Este documento registra las decisiones principales tomadas durante la implementación del módulo de base de datos y persistencia del proyecto **LSI Document Base**.

El objetivo es dejar claro por qué se tomaron ciertas decisiones y cómo afectan la integración con el resto del equipo.

---

## 1. Uso de PostgreSQL como DBMS principal

Se decidió usar **PostgreSQL** como base de datos relacional del proyecto.

### Motivo

PostgreSQL permite representar de forma clara:

- Documentos.
- Términos.
- Frecuencias documento-término.
- Recursos lingüísticos.
- Modelos LSI.
- Vectores latentes.
- Resultados de consultas.

Además, cumple con la necesidad del proyecto de usar un DBMS relacional para persistir la base documental, la matriz de frecuencias y las estructuras necesarias para consulta.

---

## 2. Uso de Flyway para migraciones

Se decidió usar **Flyway** para crear y versionar el esquema de base de datos.

Las migraciones se encuentran en:

```text
sql/migrations/
```

### Motivo

Flyway permite que cualquier integrante pueda crear el esquema desde cero ejecutando:

```powershell
mvn flyway:migrate
```

Esto evita depender de scripts manuales sueltos como `schema.sql` o `seed.sql`.

### Regla importante

Una vez que una migración se comparte con el equipo, no debería editarse directamente.  
Si se requiere modificar el esquema, se debe crear una nueva migración:

```text
V9__example_change.sql
V10__another_change.sql
```

---

## 3. Separación entre migraciones, consultas y datos demo

Se separaron los archivos SQL en tres carpetas:

```text
sql/migrations/
sql/queries/
sql/demo/
```

### Propósito de cada carpeta

| Carpeta | Propósito | ¿La ejecuta Flyway? |
|---|---|---|
| `sql/migrations/` | Crear tablas y cargar seeds oficiales. | Sí |
| `sql/queries/` | Consultas manuales de verificación. | No |
| `sql/demo/` | Datos de ejemplo para pruebas manuales. | No |

### Motivo

Esta separación evita confusión entre:

- Esquema oficial.
- Consultas de revisión.
- Datos temporales de demostración.

Flyway solo debe controlar el esquema y los seeds base, no los datos demo.

---

## 4. Uso de HikariCP para conexiones

Se decidió usar **HikariCP** como pool de conexiones.

### Motivo

HikariCP permite reutilizar conexiones a PostgreSQL en lugar de abrir una conexión nueva para cada operación.

El flujo general es:

```text
Repositorio Java
    ↓
DatabaseConfig
    ↓
HikariCP DataSource
    ↓
PostgreSQL
```

Esto centraliza la configuración y mejora la organización del acceso a la base.

---

## 5. Configuración en `application.properties`

La conexión se configuró inicialmente en:

```text
src/main/resources/application.properties
```

Ejemplo local:

```properties
db.url=jdbc:postgresql://localhost:5433/lsi_documentbase
db.user=postgres
db.password=Miami123
```

### Decisión temporal

Por simplicidad durante el desarrollo inicial, las credenciales quedaron directamente en `application.properties`.

### Recomendación futura

Para integración más limpia, se recomienda crear un archivo de ejemplo:

```text
application.example.properties
```

y evitar subir contraseñas personales al repositorio.

Cada integrante debería configurar su propio `application.properties` local según:

- Puerto local de PostgreSQL.
- Usuario.
- Contraseña.
- Nombre de base de datos.

---

## 6. Puerto local de PostgreSQL

Durante el desarrollo se usó PostgreSQL en:

```text
localhost:5433
```

### Motivo

La instalación local de PostgreSQL quedó configurada en el puerto `5433`.

### Consideración de integración

Otros integrantes pueden tener PostgreSQL en el puerto default:

```text
localhost:5432
```

Por eso, el puerto no debe asumirse como universal. Cada persona debe revisar su instalación y ajustar `application.properties`.

---

## 7. Representación de FrecT como tabla relacional dispersa

La matriz de frecuencias **FrecT** se representa mediante la tabla:

```text
document_terms
```

En lugar de crear una matriz física con columnas dinámicas, se usa una fila por cada relación documento-término:

```text
document_id | term_id | raw_frequency | weighted_frequency
```

### Motivo

Este diseño es más flexible porque:

- Permite cualquier número de documentos.
- Permite cualquier número de términos.
- Evita alterar el esquema cada vez que cambia el vocabulario.
- Facilita consultas SQL.
- Se parece a una representación dispersa de matriz.

---

## 8. Separación entre `raw_frequency` y `weighted_frequency`

En `document_terms` se guardan dos valores:

```text
raw_frequency
weighted_frequency
```

### Motivo

El proyecto requiere construir la matriz de frecuencias, pero también puede requerir ponderaciones posteriores.

Por eso:

- `raw_frequency` guarda la frecuencia cruda.
- `weighted_frequency` permite guardar una frecuencia transformada o ponderada.

Esto deja la base lista para futuras decisiones del módulo de indexación.

---

## 9. Recursos lingüísticos en tablas propias

Se decidió guardar los recursos lingüísticos en tablas separadas:

```text
stop_words
suffix_rules
term_synonyms
polysemy_rules
```

### Motivo

Esto permite que las reglas sean:

- Persistentes.
- Auditables.
- Consultables.
- Modificables sin cambiar código Java.
- Reutilizables por otros módulos.

Además, la consigna solicita considerar stop list, suffix list, stems, sinónimos y polisemia.

---

## 10. Vectores LSI guardados por componente

Los vectores latentes se guardan en tablas como:

```text
latent_document_vectors
latent_query_vectors
```

Cada fila representa un componente del vector:

```text
latent_model_id | document_id | component_index | component_value
```

### Motivo

El valor de `k` puede cambiar. Si se guardaran columnas como `component_1`, `component_2`, etc., habría que modificar el esquema cada vez que cambiara la dimensión del modelo.

Guardar por componente permite soportar cualquier valor de `k`.

---

## 11. Separación entre modelos LSI y vectores

Se creó una tabla para metadatos del modelo:

```text
latent_models
```

y tablas separadas para vectores:

```text
latent_document_vectors
latent_query_vectors
```

### Motivo

Un mismo sistema puede generar distintos modelos LSI con diferentes valores de `k` o diferentes matrices fuente.

Separar modelos y vectores permite saber a qué corrida pertenece cada vector.

---

## 12. Historial de consultas separado de resultados

Se separaron las consultas en:

```text
query_runs
query_results
```

### Motivo

Una ejecución de consulta puede tener varios resultados.

Por eso:

- `query_runs` guarda la consulta ejecutada.
- `query_results` guarda los documentos rankeados para esa consulta.

Esto permite representar resultados top-n de forma ordenada.

---

## 13. Uso de repositorios Java

Se decidió encapsular el acceso SQL en repositorios dentro de:

```text
com.lsi.persistence
```

Repositorios implementados:

```text
DocumentRepository
TermRepository
FrequencyRepository
LsiRepository
QueryLogRepository
```

### Motivo

Los demás módulos no deberían escribir SQL directamente.

En lugar de eso, deben usar métodos Java como:

```java
documentRepository.findByCode("D1");
frequencyRepository.saveFrequency(documentId, termId, 3.0, 0.90);
queryLogRepository.saveQueryResult(queryRunId, 1, documentId, 0.95);
```

Esto centraliza el acceso a la base y facilita pruebas e integración.

---

## 14. Uso de `PreparedStatement`

Los repositorios usan `PreparedStatement` para ejecutar SQL con parámetros.

### Motivo

Esto evita construir consultas concatenando strings manualmente.

También mejora:

- Seguridad.
- Legibilidad.
- Manejo de parámetros.
- Mantenimiento del código.

---

## 15. Uso de `Optional`

Los métodos que pueden devolver cero o un resultado usan:

```java
Optional<T>
```

Ejemplo:

```java
Optional<DocumentRow> result = documentRepository.findByCode("D1");
```

### Motivo

Esto evita regresar `null` y obliga a manejar explícitamente dos casos:

- El dato existe.
- El dato no existe.

---

## 16. Uso de tests con PostgreSQL real

Las pruebas se conectan a PostgreSQL real en lugar de usar mocks.

### Motivo

Esto valida la integración completa:

```text
Java
    ↓
HikariCP
    ↓
PostgreSQL JDBC Driver
    ↓
PostgreSQL
```

También permite probar:

- Llaves primarias.
- Llaves foráneas.
- Constraints.
- Inserts.
- Selects.
- Deletes.
- `ON CONFLICT`.

### Consideración

Las pruebas requieren que PostgreSQL esté instalado, corriendo y configurado localmente.

---

## 17. Uso de datos demo fuera de Flyway

Los datos demo se colocaron en:

```text
sql/demo/insert_demo_data.sql
```

### Motivo

Los datos demo no forman parte obligatoria del esquema.

Deben poder cargarse manualmente cuando se necesiten para:

- Capturas.
- Verificación.
- Pruebas manuales.
- Revisión de queries.

No deben ejecutarse automáticamente como migraciones.

---

## 18. Eliminación de `schema.sql`, `seed.sql` y `queries.sql` vacíos

Los archivos antiguos:

```text
sql/schema.sql
sql/seed.sql
sql/queries.sql
```

estaban vacíos o solo contenían comentarios.

### Decisión

Se decidió eliminarlos para evitar confusión.

### Motivo

El flujo oficial quedó dividido en:

```text
sql/migrations/
sql/queries/
sql/demo/
```

Dejar archivos vacíos en `sql/` podía hacer que alguien pensara que debía ejecutarlos manualmente.

---

## 19. Uso de Java 21

Se decidió estandarizar el proyecto en **Java 21**.

### Motivo

Aunque el `pom.xml` inicial indicaba Java 17, el equipo confirmó que se usará Java 21.

La configuración Maven quedó orientada a compilar con release 21.

---

## 20. Decisiones pendientes o futuras

Algunas decisiones pueden revisarse más adelante:

- Mover credenciales fuera de `application.properties`.
- Usar variables de entorno.
- Agregar `application.example.properties`.
- Agregar Docker o Docker Compose para PostgreSQL.
- Usar Testcontainers para pruebas más aisladas.
- Ampliar seeds lingüísticos cuando el equipo defina la colección final.
- Agregar nuevas migraciones si el esquema requiere cambios.

---

## Resumen

Las decisiones tomadas buscan que el módulo sea:

- Claro.
- Reproducible.
- Integrable.
- Fácil de probar.
- Separado de la lógica de otros módulos.
- Compatible con el flujo de trabajo paralelo del equipo.

El resultado es una capa de persistencia que puede ser usada por los módulos de ingesta, indexación, LSI y consulta sin que tengan que acceder directamente a PostgreSQL.
