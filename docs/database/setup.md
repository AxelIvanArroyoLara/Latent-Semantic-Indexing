# Setup — Módulo de Base de Datos y Persistencia

Este documento explica cómo configurar localmente el módulo de base de datos y persistencia del proyecto **LSI Document Base**.

La finalidad es que cualquier integrante del equipo pueda levantar PostgreSQL, crear la base local, ejecutar las migraciones Flyway, importar los PDFs reales del proyecto, ejecutar el flujo persistente y correr las pruebas automatizadas sin depender de configuraciones manuales ocultas.

> Para una guía corta de ejecución completa del proyecto, consultar también:
>
> ```text
> docs/execution-guide.md
> ```

---

## 1. Requisitos previos

Antes de ejecutar este módulo, se necesita tener instalado:

- **Java 21**
- **Maven**
- **PostgreSQL 16 o superior**
- **Git**
- Un IDE compatible con Maven, como IntelliJ IDEA Community

Para verificar Java y Maven:

```powershell
java -version
javac -version
mvn -version
```

La salida esperada debe indicar que Java y Maven están usando **Java 21**.

Para verificar PostgreSQL:

```powershell
psql --version
```

---

## 2. Base de datos local

Este módulo espera conectarse a una base de datos llamada:

```text
lsi_documentbase
```

La base debe existir antes de ejecutar Flyway. Flyway crea las tablas dentro de la base, pero normalmente no crea la base de datos desde cero.

Para crearla manualmente, entra a PostgreSQL:

```powershell
psql -U postgres -h localhost -p 5433
```

Dentro de `psql`, ejecuta:

```sql
CREATE DATABASE lsi_documentbase;
```

Luego sal con:

```sql
\q
```

Si tu instalación de PostgreSQL usa el puerto por defecto, cambia `5433` por `5432`.

---

## 3. Configuración de conexión

La configuración local se encuentra en:

```text
src/main/resources/application.properties
```

Ejemplo de configuración usada durante el desarrollo:

```properties
db.url=jdbc:postgresql://localhost:5433/lsi_documentbase
db.user=postgres
db.password=your_password_here
```

Cada integrante debe ajustar estos valores según su instalación local.

Por ejemplo, si PostgreSQL está en el puerto por defecto:

```properties
db.url=jdbc:postgresql://localhost:5432/lsi_documentbase
db.user=postgres
db.password=your_password_here
```

---

## 4. Nota sobre credenciales

La aplicación puede leer credenciales desde `application.properties` y también permite usar variables de entorno.

Variables de entorno en PowerShell:

```powershell
$env:LSI_DB_URL="jdbc:postgresql://localhost:5433/lsi_documentbase"
$env:LSI_DB_USER="postgres"
$env:LSI_DB_PASSWORD="your_password_here"
```

Si PostgreSQL usa el puerto por defecto:

```powershell
$env:LSI_DB_URL="jdbc:postgresql://localhost:5432/lsi_documentbase"
```

Para ejecutar migraciones, también se puede pasar la contraseña directamente a Maven:

```powershell
mvn flyway:migrate "-Dflyway.password=your_password_here"
```

Para un proyecto productivo, lo ideal sería usar un archivo `application.example.properties`, ignorar `application.properties` local con Git y depender de variables de entorno. Para este proyecto académico, basta con asegurar que cada máquina tenga los valores correctos antes de ejecutar.

---

## 5. Ejecución de migraciones Flyway

Las migraciones activas están en:

```text
sql/migrations/
```

Flyway solo ejecuta los archivos ubicados en esa carpeta.

Para ejecutar las migraciones:

```powershell
mvn flyway:migrate "-Dflyway.password=your_password_here"
```

Si la contraseña ya está configurada en `application.properties` o mediante variables de entorno, también puede funcionar:

```powershell
mvn flyway:migrate
```

Si todo está correcto, Flyway debe crear las tablas y cargar los datos semilla definidos en las migraciones.

Para revisar el estado de las migraciones:

```powershell
mvn flyway:info "-Dflyway.password=your_password_here"
```

---

## 6. Qué archivos ejecuta Flyway

Flyway está configurado para usar:

```text
sql/migrations/
```

Por lo tanto, ejecuta archivos como:

```text
V1__create_documents.sql
V2__create_linguistic_tables.sql
V3__create_terms_and_frequencies.sql
V4__create_latent_space_tables.sql
V5__create_query_logging_tables.sql
V6__seed_stop_words.sql
V7__seed_suffix_rules.sql
V8__seed_synonyms_and_polysemy.sql
V9__create_selected_index_terms.sql
```

Flyway **no** ejecuta automáticamente:

```text
sql/queries/
sql/demo/
```

Esas carpetas contienen consultas manuales y datos demo que deben ejecutarse por separado cuando se necesiten.

---

## 7. Reiniciar el esquema en desarrollo

Durante desarrollo local, puede ser útil borrar todo el esquema y volver a correr Flyway desde cero.

Conéctate a la base:

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase
```

Luego ejecuta:

```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;
```

Después sal:

```sql
\q
```

Y vuelve a ejecutar:

```powershell
mvn flyway:migrate "-Dflyway.password=your_password_here"
```

Esto recreará todas las tablas y seeds desde las migraciones.

---

## 8. Flujo real recomendado con PDFs

El flujo final recomendado **no** depende de `sql/demo/insert_demo_data.sql`.

El flujo real usa los PDFs ubicados en:

```text
data/raw/
```

Primero importa esos PDFs a PostgreSQL:

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=import-pdfs-db --reset true"
```

Después ejecuta el demo final usando la base como fuente y persistiendo los artefactos generados:

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=final-demo --source db --persist true"
```

Este flujo persiste:

- Documentos importados desde PDFs en `documents`.
- Términos generados por el pipeline en `terms`.
- Filas de FrecT en `document_terms`.
- Modelos LSI en `latent_models`.
- Vectores latentes de documentos en `latent_document_vectors`.
- Términos seleccionados en `selected_index_terms`.

Para persistir consultas y resultados rankeados, se recomienda ejecutar los comandos de query desde **Git Bash**, especialmente cuando el texto de la consulta contiene espacios:

```bash
mvn exec:java -Dexec.mainClass=com.lsi.App -Dexec.args="query --source db --text \"academic stress anxiety\" --top 5 --metric cosine --persist true"
mvn exec:java -Dexec.mainClass=com.lsi.App -Dexec.args="query --source db --text \"sleep wellbeing\" --top 5 --metric jaccard --persist true"
mvn exec:java -Dexec.mainClass=com.lsi.App -Dexec.args="query --source db --text \"academic stress\" --top 5 --metric euclidean --persist true"
```

Estos comandos persisten información en:

```text
query_runs
query_results
```

---

## 9. Ejecutar pruebas automatizadas

Una vez configurada la base y aplicadas las migraciones, ejecuta:

```powershell
mvn test
```

Las pruebas validan:

- Conexión Java con PostgreSQL.
- Inserción y lectura de documentos.
- Inserción y lectura de términos.
- Persistencia de frecuencias documento-término.
- Persistencia de modelos y vectores LSI.
- Persistencia de consultas y resultados rankeados.

Si todo funciona, la salida debe terminar con:

```text
BUILD SUCCESS
```

Importante: algunas pruebas limpian tablas e insertan datos temporales. Si se ejecuta `mvn test` antes de presentar el demo final, vuelve a correr:

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=import-pdfs-db --reset true"
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=final-demo --source db --persist true"
```

---

## 10. Datos demo legacy

Los datos demo están en:

```text
sql/demo/insert_demo_data.sql
```

Este archivo no es una migración Flyway. Se ejecuta manualmente.

Comando:

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/demo/insert_demo_data.sql
```

Los datos demo sirven para pruebas manuales o experimentos SQL aislados, pero **no son el flujo recomendado para la entrega final**.

Para la ejecución final del proyecto, usar:

```text
data/raw/*.pdf
→ import-pdfs-db
→ PostgreSQL
→ final-demo --source db --persist true
```

---

## 11. Ejecutar consultas de verificación

Las consultas manuales están en:

```text
sql/queries/
```

Ejemplo:

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/01_count_documents.sql
```

Estas consultas permiten revisar:

- Documentos cargados.
- Términos registrados.
- Matriz FrecT en formato relacional.
- Modelos LSI.
- Vectores latentes.
- Resultados de consulta.
- Recursos lingüísticos y semánticos.

---

## 12. Problemas comunes

### PostgreSQL no responde

Verifica el puerto:

```powershell
pg_isready -h localhost -p 5433
```

Si no responde, prueba con:

```powershell
pg_isready -h localhost -p 5432
```

También revisa si el servicio de PostgreSQL está activo:

```powershell
Get-Service *postgres*
```

---

### Error de contraseña

Si `psql` marca error de autenticación, revisa que `application.properties` tenga la misma contraseña que tu instalación local de PostgreSQL.

Si Flyway indica que la contraseña está vacía, ejecuta:

```powershell
mvn flyway:migrate "-Dflyway.password=your_password_here"
```

---

### PowerShell marca error con queries que tienen espacios

PowerShell puede fallar con comandos como `--text "academic stress anxiety"`.

Para esos casos, usar Git Bash:

```bash
mvn exec:java -Dexec.mainClass=com.lsi.App -Dexec.args="query --source db --text \"academic stress anxiety\" --top 5 --metric cosine --persist true"
```

---

### Flyway no encuentra PostgreSQL

Si aparece un error como:

```text
No database found to handle jdbc:postgresql://...
```

verifica que el `pom.xml` incluya el soporte de PostgreSQL para Flyway y el driver JDBC correspondiente.

---

### Las consultas de verificación no muestran datos

Esto puede pasar si solo se ejecutaron pruebas automatizadas, ya que algunas pruebas limpian tablas antes de correr.

Para llenar la base con datos reales del proyecto, ejecuta:

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=import-pdfs-db --reset true"
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=final-demo --source db --persist true"
```

Si solo se desea probar SQL con datos manuales, se puede usar el script legacy:

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/demo/insert_demo_data.sql
```

---

## 13. Resumen rápido de comandos

Crear base de datos:

```sql
CREATE DATABASE lsi_documentbase;
```

Ejecutar migraciones:

```powershell
mvn flyway:migrate "-Dflyway.password=your_password_here"
```

Ver estado de migraciones:

```powershell
mvn flyway:info "-Dflyway.password=your_password_here"
```

Compilar:

```powershell
mvn clean compile
```

Ejecutar flujo real con PDFs y PostgreSQL:

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=import-pdfs-db --reset true"
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=final-demo --source db --persist true"
```

Ejecutar pruebas:

```powershell
mvn test
```

Ejecutar consulta de verificación:

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/08_query_runs_and_results.sql
```

---

## 14. Estado esperado

Después de completar este setup, el módulo debe permitir:

- Conectarse a PostgreSQL desde Java.
- Crear el esquema mediante Flyway.
- Insertar seeds lingüísticos y semánticos.
- Importar PDFs reales desde `data/raw`.
- Persistir documentos, términos, FrecT, modelos LSI, vectores y términos seleccionados.
- Persistir consultas y resultados rankeados.
- Ejecutar pruebas automatizadas.
- Consultar la base mediante archivos SQL de verificación.
