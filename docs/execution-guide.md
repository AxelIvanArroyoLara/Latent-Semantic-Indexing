# Execution Guide — LSI Document Base

This guide explains how to run the **LSI Document Base** project from zero using PostgreSQL persistence.

The recommended execution path is:

```text
PDF files in data/raw/
    ↓
import-pdfs-db
    ↓
PostgreSQL documents table
    ↓
final-demo --source db --persist true
    ↓
FrecT + LSI + selected terms persisted
    ↓
query --source db --persist true
    ↓
query_runs + query_results persisted
```

The file `sql/demo/insert_demo_data.sql` is kept only for legacy/manual SQL experiments. The recommended final execution path uses the real PDFs under `data/raw/`.

---

## 1. Requirements

Install:

- Java 21
- Maven
- PostgreSQL 16 or newer
- Git
- A terminal such as PowerShell or Git Bash

Check versions:

```powershell
java -version
javac -version
mvn -version
psql --version
```

Java should report version 21.

---

## 2. Clone the repository

```bash
git clone https://github.com/AxelIvanArroyoLara/Latent-Semantic-Indexing.git
cd Latent-Semantic-Indexing
```

If using a specific branch:

```bash
git checkout develop
```

or the branch provided by the team.

---

## 3. Create the PostgreSQL database

Open PostgreSQL:

```powershell
psql -U postgres -h localhost -p 5433
```

If your installation uses the default PostgreSQL port, use `5432` instead:

```powershell
psql -U postgres -h localhost -p 5432
```

Create the database:

```sql
CREATE DATABASE lsi_documentbase;
```

Exit:

```sql
\q
```

---

## 4. Configure database credentials

The project reads database configuration from:

```text
src/main/resources/application.properties
```

Example for local development:

```properties
db.url=jdbc:postgresql://localhost:5433/lsi_documentbase
db.user=postgres
db.password=your_password_here
```

If PostgreSQL uses port `5432`, use:

```properties
db.url=jdbc:postgresql://localhost:5432/lsi_documentbase
db.user=postgres
db.password=your_password_here
```

The project also supports environment variables:

```powershell
$env:LSI_DB_URL="jdbc:postgresql://localhost:5433/lsi_documentbase"
$env:LSI_DB_USER="postgres"
$env:LSI_DB_PASSWORD="your_password_here"
```

Use the password configured in your local PostgreSQL installation.

---

## 5. Run Flyway migrations

Flyway creates the relational schema and inserts official linguistic seed data.

```powershell
mvn flyway:migrate "-Dflyway.password=your_password_here"
```

If the password is already configured in `application.properties` or environment variables, this may also work:

```powershell
mvn flyway:migrate
```

Check migration status:

```powershell
mvn flyway:info "-Dflyway.password=your_password_here"
```

Expected result: migrations should be applied successfully.

---

## 6. Compile the project

```powershell
mvn clean compile
```

Expected result:

```text
BUILD SUCCESS
```

---

## 7. Import real PDFs into PostgreSQL

The real document corpus is stored under:

```text
data/raw/
```

Import those PDFs into the `documents` table:

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=import-pdfs-db --reset true"
```

Expected output:

```text
Imported PDF documents into PostgreSQL: <number>
Source directory: data/raw
Reset existing documents: true
```

This command resets the previously stored documents and imports the current PDFs from `data/raw`.

---

## 8. Run the final demo using PostgreSQL

After importing PDFs, run the full database-backed demo:

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=final-demo --source db --persist true"
```

This command:

- loads documents from PostgreSQL;
- applies preprocessing and semantic normalization;
- builds FrecT;
- applies SVD/LSI;
- selects important indexing terms;
- persists terms, frequencies, LSI model, latent vectors, and selected terms;
- prints the guided technical demonstration.

Expected output should include:

```text
Source: PostgreSQL database
Indexed documents: <number>
FrecT dimensions: <terms> terms x <documents> documents
BUILD SUCCESS
```

---

## 9. Persist query results

For queries with spaces in the `--text` argument, **Git Bash is recommended** because PowerShell may have quoting issues.

### Cosine similarity query

```bash
mvn exec:java -Dexec.mainClass=com.lsi.App -Dexec.args="query --source db --text \"academic stress anxiety\" --top 5 --metric cosine --persist true"
```

### Jaccard similarity query

```bash
mvn exec:java -Dexec.mainClass=com.lsi.App -Dexec.args="query --source db --text \"sleep wellbeing\" --top 5 --metric jaccard --persist true"
```

### Euclidean distance query

```bash
mvn exec:java -Dexec.mainClass=com.lsi.App -Dexec.args="query --source db --text \"academic stress\" --top 5 --metric euclidean --persist true"
```

Expected output:

```text
Query persistence completed:
  query_run_id: <id>
  results: 5
BUILD SUCCESS
```

These commands persist:

```text
query_runs
query_results
```

---

## 10. Validate persistence with SQL

Use the SQL verification files under:

```text
sql/queries/
```

### Check imported documents

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/02_list_documents.sql
```

### Check persisted terms

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/03_list_terms.sql
```

### Check FrecT rows

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/04_document_frequency_matrix.sql
```

### Check LSI models

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/05_lsi_models.sql
```

### Check document vectors

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/06_lsi_document_vectors.sql
```

### Check query runs and ranked results

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/08_query_runs_and_results.sql
```

If your PostgreSQL port is `5432`, replace `5433` with `5432`.

---

## 11. Run tests

```powershell
mvn clean test
```

Expected result:

```text
BUILD SUCCESS
```

Some database-related tests may depend on having PostgreSQL configured locally.

Important: some tests clean and insert temporary records in the database. If you run `mvn clean test` before presenting the final demo, run the import and demo commands again:

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=import-pdfs-db --reset true"
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=final-demo --source db --persist true"
```

---

## 12. Recommended full execution sequence

Use this sequence for a complete run from an already cloned repository.

```powershell
mvn flyway:migrate "-Dflyway.password=your_password_here"
mvn clean compile
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=import-pdfs-db --reset true"
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=final-demo --source db --persist true"
```

Then, in Git Bash, run:

```bash
mvn exec:java -Dexec.mainClass=com.lsi.App -Dexec.args="query --source db --text \"academic stress anxiety\" --top 5 --metric cosine --persist true"
mvn exec:java -Dexec.mainClass=com.lsi.App -Dexec.args="query --source db --text \"sleep wellbeing\" --top 5 --metric jaccard --persist true"
mvn exec:java -Dexec.mainClass=com.lsi.App -Dexec.args="query --source db --text \"academic stress\" --top 5 --metric euclidean --persist true"
```

Finally validate with:

```powershell
psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/queries/08_query_runs_and_results.sql
```

---

## 13. Common problems

### PowerShell says: "La sintaxis del comando no es correcta"

This usually happens with `exec.args` when the query text contains spaces.

Use Git Bash for query commands:

```bash
mvn exec:java -Dexec.mainClass=com.lsi.App -Dexec.args="query --source db --text \"academic stress anxiety\" --top 5 --metric cosine --persist true"
```

### Flyway says the password is empty

Pass the password explicitly:

```powershell
mvn flyway:migrate "-Dflyway.password=your_password_here"
```

Or configure:

```properties
db.password=your_password_here
```

in `src/main/resources/application.properties`.

### PostgreSQL does not connect

Check if PostgreSQL is running:

```powershell
pg_isready -h localhost -p 5433
```

Try port `5432` if `5433` fails:

```powershell
pg_isready -h localhost -p 5432
```

### Database does not exist

Create it:

```sql
CREATE DATABASE lsi_documentbase;
```

### Queries show no data

Run the full persistence flow again:

```powershell
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=import-pdfs-db --reset true"
mvn exec:java "-Dexec.mainClass=com.lsi.App" "-Dexec.args=final-demo --source db --persist true"
```

Then run the SQL verification query again.

---

## 14. Notes about demo SQL

The file:

```text
sql/demo/insert_demo_data.sql
```

is not the recommended execution path for the final project.

It is kept for legacy SQL-only checks or manual experiments. The real final execution path uses:

```text
data/raw/*.pdf
→ import-pdfs-db
→ PostgreSQL
→ final-demo --source db --persist true
```
