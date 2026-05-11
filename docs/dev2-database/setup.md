# Setup — Módulo de Base de Datos y Persistencia

Este documento explica cómo configurar localmente el módulo de base de datos y persistencia del proyecto **LSI Document Base**.

La finalidad es que cualquier integrante del equipo pueda levantar PostgreSQL, crear la base local, ejecutar las migraciones Flyway y correr las pruebas automatizadas sin depender de configuraciones manuales ocultas.

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
db.password=Miami123
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

Por simplicidad inicial, las credenciales se configuraron directamente en `application.properties`.

Esta decisión facilita el desarrollo local, pero no es la opción más segura para un sistema real.

Para una versión más limpia, se recomienda usar:

```text
application.example.properties
application.properties local ignorado por Git
variables de entorno
```

Ejemplo de archivo de referencia:

```properties
db.url=jdbc:postgresql://localhost:5432/lsi_documentbase
db.user=postgres
db.password=your_password_here
```

---

## 5. Ejecución de migraciones Flyway

Las migraciones activas están en:

```text
sql/migrations/
```

Flyway solo ejecuta los archivos ubicados en esa carpeta.

Para ejecutar las migraciones:

```powershell
mvn flyway:migrate
```

Si todo está correcto, Flyway debe crear las tablas y cargar los datos semilla definidos en `V6`, `V7` y `V8`.

Para revisar el estado de las migraciones:

```powershell
mvn flyway:info
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
mvn flyway:migrate
```

Esto recreará todas las tablas y seeds desde las migraciones.

---

## 8. Ejecutar pruebas automatizadas

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

---

## 9. Cargar datos demo

Los datos demo están en:

```text
sql/demo/insert_demo_data.sql
```

Este archivo no es una migración Flyway. Se ejecuta manualmente.

Comando:

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/demo/insert_demo_data.sql
```

Los datos demo sirven para llenar la base con documentos, términos, frecuencias, vectores LSI y resultados de consulta de ejemplo.

---

## 10. Ejecutar consultas de verificación

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

## 11. Problemas comunes

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

Para llenar la base con datos consistentes de ejemplo, ejecuta:

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/demo/insert_demo_data.sql
```

---

## 12. Resumen rápido de comandos

Crear base de datos:

```sql
CREATE DATABASE lsi_documentbase;
```

Ejecutar migraciones:

```powershell
mvn flyway:migrate
```

Ver estado de migraciones:

```powershell
mvn flyway:info
```

Ejecutar pruebas:

```powershell
mvn test
```

Cargar datos demo:

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/demo/insert_demo_data.sql
```

Ejecutar consulta de verificación:

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/01_count_documents.sql
```

---

## 13. Estado esperado

Después de completar este setup, el módulo debe permitir:

- Conectarse a PostgreSQL desde Java.
- Crear el esquema mediante Flyway.
- Insertar seeds lingüísticos y semánticos.
- Ejecutar pruebas automatizadas.
- Cargar datos demo manuales.
- Consultar la base mediante archivos SQL de verificación.
