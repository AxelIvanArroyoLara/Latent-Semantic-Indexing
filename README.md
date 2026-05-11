# Latent Semantic Indexing Document Base

Sistema de recuperación documental basado en **Latent Semantic Indexing (LSI)** para indexar, representar y consultar una base de documentos mediante técnicas de recuperación de información, reducción dimensional con **SVD**, y comparación por funciones de similitud y disimilitud.

El proyecto fue estructurado para desarrollarse de forma **colaborativa entre 6 integrantes**, permitiendo que cada módulo se implemente de manera independiente y quede listo para su integración posterior.

---

## Objetivo

Construir un sistema capaz de:

- trabajar con una base de al menos 10 documentos;
- preprocesar texto considerando listas de exclusión y reducción léxica;
- construir una **matriz de frecuencias término-documento (FrecT)**;
- almacenar la información en una **base de datos relacional**;
- aplicar **Latent Semantic Indexing (LSI)** mediante **Single Value Decomposition (SVD)**;
- permitir consultas de similitud entre documentos;
- permitir consultas textuales para recuperar los **n documentos más relevantes**;
- incorporar tratamiento semántico básico mediante **sinónimos** y **polisemia**.

---

## Alcance funcional

El sistema contempla los siguientes bloques principales:

1. Carga documental  
2. Preprocesamiento  
3. Tratamiento semántico  
4. Indexación clásica  
5. Reducción LSI  
6. Consultas y ranking  
7. Persistencia SQL  
8. Interfaz de uso  
9. Pruebas e integración  

---

## Estructura del proyecto

```text
lsi/
├── data/
│   ├── raw/
│   └── processed/
├── docs/
├── sql/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── lsi/
│   │   │           ├── App.java
│   │   │           ├── config/
│   │   │           ├── model/
│   │   │           ├── preprocessing/
│   │   │           ├── semantic/
│   │   │           ├── indexing/
│   │   │           ├── lsi/
│   │   │           ├── query/
│   │   │           ├── persistence/
│   │   │           └── ui/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── stopwords.txt
│   │       ├── suffixes.txt
│   │       ├── synonyms.csv
│   │       └── polysemy_rules.csv
│   └── test/
│       └── java/
│           └── com/
│               └── lsi/
│                   ├── preprocessing/
│                   ├── semantic/
│                   ├── indexing/
│                   ├── lsi/
│                   ├── query/
│                   └── persistence/
├── pom.xml
└── README.md
```

---

## Descripción de carpetas

### `data/`
Contiene los insumos documentales del sistema.

- `raw/`: documentos originales sin procesar.
- `processed/`: documentos resultantes del pipeline de preprocesamiento.

### `docs/`
Documentación técnica, arquitectura, flujo de trabajo del equipo y notas de integración.

### `sql/`
Scripts SQL del proyecto.

- `schema.sql`: definición de tablas, llaves e índices.
- `seed.sql`: carga inicial de datos y recursos.
- `queries.sql`: consultas auxiliares de validación y prueba.

### `src/main/java/com/lsi/`
Código fuente principal del sistema, organizado por módulos:

- `config/`: configuraciones globales.
- `model/`: entidades y clases de dominio.
- `preprocessing/`: normalización, tokenización, filtrado y stemming.
- `semantic/`: sinónimos y manejo básico de polisemia.
- `indexing/`: vocabulario y matriz de frecuencias.
- `lsi/`: SVD y representación reducida.
- `query/`: consultas, similitud, disimilitud y ranking.
- `persistence/`: repositorios y acceso a base de datos.
- `ui/`: interfaz de línea de comandos.

### `src/main/resources/`
Recursos de configuración y apoyo lingüístico:

- `application.properties`
- `stopwords.txt`
- `suffixes.txt`
- `synonyms.csv`
- `polysemy_rules.csv`

### `src/test/java/com/lsi/`
Pruebas unitarias y de integración por módulo.

---

## Arquitectura lógica

El sistema sigue este flujo general:

```text
Documentos
   ↓
Preprocesamiento
   ↓
Tratamiento semántico
   ↓
Vocabulario + FrecT
   ↓
Persistencia SQL
   ↓
SVD / LSI
   ↓
Consulta y ranking
   ↓
Resultados
```

---

## Diseño de base de datos

La base de datos relacional fue planteada para soportar indexación, consulta y comparación entre la representación clásica y la representación reducida por LSI.

### Tablas principales

- `documents`
- `terms`
- `document_terms`
- `stop_words`
- `suffix_rules`
- `synonyms`
- `polysemy_rules`
- `lsi_dimensions`
- `document_vectors_lsi`
- `query_logs`

### Propósito

- almacenar documentos y términos;
- representar la matriz FrecT de forma relacional;
- persistir recursos lingüísticos;
- guardar vectores reducidos LSI;
- registrar consultas y resultados de prueba.

---

## Tecnologías previstas

### Base del proyecto
- **Java**
- **Maven**

### Persistencia
- **PostgreSQL** o **MySQL**
- **JDBC**

### Álgebra lineal / SVD
Se recomienda usar una librería con soporte para matrices y descomposición SVD. Opciones viables:

- **Apache Commons Math**
- **EJML**
- **Smile**

### Testing
- **JUnit 5**

### Logging
- **SLF4J**
- **Logback**

---

## Flujo de trabajo colaborativo

El proyecto fue diseñado para que **cada integrante implemente su parte sin esperar a los resultados de los demás**.

### Regla principal
Cada desarrollador debe trabajar de forma independiente sobre su módulo, dejarlo funcional y documentado, y asumir contratos claros de entrada y salida para que la integración posterior sea directa.

### Principios de trabajo
- no bloquearse por módulos ajenos;
- no esperar lógica terminada de otros;
- usar mocks, datos de prueba o interfaces provisionales;
- respetar la estructura de paquetes y clases acordadas;
- dejar cada módulo listo para conectarse después.

---

## Distribución sugerida del equipo

### Integrante 1 — Coordinación e integración
- revisar consistencia global;
- validar estructura;
- coordinar integración entre módulos;
- consolidar entregables finales.

### Integrante 2 — Base de datos y persistencia
- diseño relacional;
- scripts SQL;
- repositorios;
- conexión con DBMS.

### Integrante 3 — Preprocesamiento
- normalización;
- tokenización;
- eliminación de stop words;
- stemming y manejo de sufijos.

### Integrante 4 — Semántica
- sinónimos;
- reglas de polisemia;
- expansión o normalización semántica.

### Integrante 5 — Indexación y LSI
- vocabulario;
- FrecT;
- SVD;
- representación reducida.

### Integrante 6 — Consultas e interfaz
- similitud entre documentos;
- ranking top-n;
- funciones de comparación;
- CLI o interfaz de demostración.

---

## Reglas de implementación

Cada integrante debe:

1. implementar únicamente su módulo;
2. no modificar arbitrariamente módulos ajenos;
3. respetar el package `com.lsi`;
4. documentar supuestos y contratos;
5. dejar comentarios claros donde falte integración;
6. usar datos simulados si aún no existe conexión con otro módulo;
7. dejar métodos, clases y archivos listos para conexión posterior.

---

## Contratos de integración

- `preprocessing` debe producir texto limpio o tokens listos.
- `semantic` debe recibir tokens o texto normalizado y devolver representación enriquecida.
- `indexing` debe trabajar con tokens finales y producir vocabulario y frecuencias.
- `lsi` debe recibir la matriz de frecuencias.
- `query` debe consumir vectores clásicos o reducidos.
- `persistence` debe poder guardar y recuperar entidades del dominio.
- `ui` debe llamar servicios ya definidos, no implementar lógica de negocio.

---

## Funcionalidades mínimas esperadas

El sistema debe permitir:

- registrar y cargar documentos;
- procesar documentos con pipeline de texto;
- construir la matriz FrecT;
- guardar resultados en base de datos;
- aplicar LSI sobre la representación documental;
- comparar dos documentos dados;
- procesar una consulta textual;
- devolver los `n` documentos más relevantes;
- usar al menos dos funciones de similitud;
- usar al menos una función de disimilitud.

---

## Métricas sugeridas

### Similitud
- Coseno
- Jaccard o Dice

### Disimilitud
- Distancia Euclidiana o Manhattan

---

## Flujo esperado de uso

1. cargar documentos;  
2. ejecutar preprocesamiento;  
3. ejecutar tratamiento semántico;  
4. construir vocabulario;  
5. generar FrecT;  
6. guardar en base de datos;  
7. calcular SVD y representación LSI;  
8. realizar consultas;  
9. comparar resultados y mostrar ranking.  

---

## Estado actual

La estructura base del proyecto ya fue definida para permitir implementación modular.  
A partir de este punto, cada integrante puede comenzar directamente su parte.

### Pendiente por implementar
- lógica interna de clases;
- scripts SQL completos;
- recursos lingüísticos definitivos;
- integración entre módulos;
- pruebas funcionales completas;
- dataset final de documentos.

---

## Recomendaciones de desarrollo

- mantener commits pequeños y claros;
- trabajar por ramas;
- no mezclar lógica de distintos módulos;
- probar localmente antes de integrar;
- dejar comentarios técnicos en clases base;
- documentar decisiones importantes en `docs/`.

---

## Nombre del proyecto

**Latent Semantic Indexing Document Base**

Nombre descriptivo alternativo:

**Sistema de Recuperación Documental con LSI**

---

## Nota final para el equipo

Cada integrante debe avanzar **sin esperar a que los demás terminen**. La prioridad es dejar el módulo propio:

- estructurado;
- documentado;
- funcional en su alcance;
- listo para integrarse cuando llegue el momento.

La integración final debe ser un proceso de conexión entre módulos ya preparados, no una etapa donde todavía se empiece a construir la lógica principal.
