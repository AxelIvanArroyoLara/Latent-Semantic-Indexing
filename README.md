# LSI Document Base

````markdown
# Latent Semantic Indexing Document Base

Sistema de recuperación documental basado en **Latent Semantic Indexing (LSI)** para indexar, representar y consultar una base de documentos mediante técnicas de recuperación de información, reducción dimensional con **SVD**, y comparación por funciones de similitud y disimilitud.

Este proyecto fue diseñado para desarrollarse de forma **colaborativa entre 6 integrantes**, permitiendo que cada módulo se implemente de manera independiente y quede listo para su integración posterior.

---

## 1. Objetivo del proyecto

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

## 2. Alcance funcional

El sistema contempla los siguientes bloques principales:

1. **Carga documental**
2. **Preprocesamiento**
3. **Tratamiento semántico**
4. **Indexación clásica**
5. **Reducción LSI**
6. **Consultas y ranking**
7. **Persistencia SQL**
8. **Interfaz de uso**
9. **Pruebas e integración**

---

## 3. Estructura del proyecto

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
````

---

## 4. Descripción de carpetas y módulos

## `data/`

Contiene los insumos documentales del sistema.

* `raw/`: documentos originales sin procesar.
* `processed/`: documentos transformados después del pipeline de preprocesamiento.

## `docs/`

Documentación técnica, arquitectura, flujo de trabajo del equipo, ejemplos de consulta y notas de integración.

## `sql/`

Scripts SQL del proyecto.

* `schema.sql`: definición de tablas, llaves e índices.
* `seed.sql`: carga inicial de datos y recursos.
* `queries.sql`: consultas de apoyo para validación y pruebas.

## `src/main/java/com/lsi/`

Código fuente principal del sistema.

### `config/`

Configuraciones globales del proyecto.

### `model/`

Entidades y clases de dominio.

### `preprocessing/`

Normalización, tokenización, filtrado y stemming.

### `semantic/`

Expansión semántica mediante sinónimos y manejo básico de polisemia.

### `indexing/`

Construcción del vocabulario y de la matriz de frecuencias.

### `lsi/`

Aplicación de SVD y construcción de la representación reducida LSI.

### `query/`

Comparación entre documentos, procesamiento de consultas y ranking.

### `persistence/`

Acceso a base de datos, repositorios y persistencia de resultados.

### `ui/`

Interfaz de línea de comandos o capa básica de interacción con el usuario.

## `src/main/resources/`

Recursos externos de configuración y conocimiento lingüístico.

## `src/test/java/com/lsi/`

Pruebas unitarias e integración por módulo.

---

## 5. Arquitectura lógica del sistema

El sistema sigue un flujo modular:

1. **Entrada documental**
2. **Preprocesamiento textual**
3. **Tratamiento semántico**
4. **Construcción de vocabulario**
5. **Generación de FrecT**
6. **Persistencia en base de datos**
7. **Aplicación de SVD**
8. **Reducción a espacio LSI**
9. **Consultas por similitud/disimilitud**
10. **Ranking de resultados**

### Flujo general

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

## 6. Diseño de base de datos

La base de datos relacional fue pensada para soportar indexación, consulta y comparación entre representación clásica y representación reducida.

### Tablas principales

* `documents`
* `terms`
* `document_terms`
* `stop_words`
* `suffix_rules`
* `synonyms`
* `polysemy_rules`
* `lsi_dimensions`
* `document_vectors_lsi`
* `query_logs`

### Propósito general

* almacenar documentos y términos;
* representar la matriz FrecT de forma relacional;
* persistir recursos lingüísticos;
* guardar vectores reducidos LSI;
* registrar consultas y resultados de prueba.

---

## 7. Librerías y tecnologías previstas

## Base del proyecto

* **Java**
* **Maven**

## Persistencia

* **PostgreSQL** o **MySQL**
* **JDBC**

## Álgebra lineal / SVD

Se recomienda usar una librería que permita manejo de matrices y descomposición SVD. Algunas opciones viables:

* **Apache Commons Math**
* **EJML**
* **Smile**

## Testing

* **JUnit 5**

## Logging

* **SLF4J**
* **Logback**

---

## 8. Flujo de trabajo colaborativo

Este proyecto fue diseñado para que **cada integrante implemente su parte sin esperar a los resultados de los demás**.

La regla principal es:

> Cada desarrollador debe trabajar de forma independiente sobre su módulo, dejarlo funcional y documentado, y asumir contratos claros de entrada/salida para que la integración posterior sea directa.

### Principio de trabajo

* no bloquearse por módulos ajenos;
* no esperar lógica terminada de otros;
* usar mocks, datos de prueba o interfaces provisionales;
* respetar la estructura de paquetes y clases acordadas;
* dejar cada módulo listo para conectarse después.

---

## 9. Distribución sugerida para equipo de 6 integrantes

## Integrante 1 — Coordinación e integración

Responsable de:

* revisar consistencia global;
* validar estructura;
* coordinar integración entre módulos;
* consolidar entregables finales.

## Integrante 2 — Base de datos y persistencia

Responsable de:

* diseño relacional;
* scripts SQL;
* repositorios;
* conexión con DBMS.

## Integrante 3 — Preprocesamiento

Responsable de:

* normalización;
* tokenización;
* eliminación de stop words;
* stemming y suffix handling.

## Integrante 4 — Semántica

Responsable de:

* sinónimos;
* reglas de polisemia;
* expansión o normalización semántica.

## Integrante 5 — Indexación y LSI

Responsable de:

* vocabulario;
* FrecT;
* SVD;
* representación reducida.

## Integrante 6 — Consultas e interfaz

Responsable de:

* similitud entre documentos;
* ranking top-n;
* funciones de comparación;
* CLI o interfaz de demostración.

---

## 10. Reglas de implementación

Cada integrante debe seguir estas reglas:

1. implementar únicamente su módulo;
2. no modificar arbitrariamente módulos ajenos;
3. respetar el package `com.lsi`;
4. documentar supuestos y contratos;
5. dejar comentarios claros donde falte integración;
6. usar datos simulados si aún no existe conexión con otro módulo;
7. dejar métodos, clases y archivos listos para conexión posterior.

---

## 11. Contratos de integración

Para reducir dependencia entre compañeros:

* `preprocessing` debe producir texto limpio o tokens listos;
* `semantic` debe recibir tokens/texto normalizado y devolver representación enriquecida;
* `indexing` debe trabajar con tokens finales y producir vocabulario + frecuencias;
* `lsi` debe recibir la matriz de frecuencias;
* `query` debe consumir vectores clásicos o reducidos;
* `persistence` debe poder guardar y recuperar entidades del dominio;
* `ui` debe llamar servicios ya definidos, no implementar lógica del negocio.

---

## 12. Funcionalidades mínimas esperadas

El sistema debe permitir:

* registrar y cargar documentos;
* procesar documentos con pipeline de texto;
* construir la matriz FrecT;
* guardar resultados en DB;
* aplicar LSI sobre la representación documental;
* comparar dos documentos dados;
* procesar una consulta textual;
* devolver los `n` documentos más relevantes;
* usar al menos dos funciones de similitud;
* usar al menos una función de disimilitud.

---

## 13. Métricas de comparación sugeridas

### Similitud

* Coseno
* Jaccard o Dice

### Disimilitud

* Distancia Euclidiana o Manhattan

---

## 14. Ejecución esperada del sistema

De manera general, el flujo de uso esperado será:

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

## 15. Archivos de recursos

## `stopwords.txt`

Lista de palabras vacías que serán excluidas del análisis.

## `suffixes.txt`

Reglas de sufijos para normalización o stemming simplificado.

## `synonyms.csv`

Relaciones de sinonimia del dominio seleccionado.

## `polysemy_rules.csv`

Reglas contextuales básicas para interpretación de términos ambiguos.

## `application.properties`

Parámetros generales del proyecto:

* conexión a base de datos;
* rutas de archivos;
* parámetros de ejecución;
* valores por defecto del sistema.

---

## 16. Estado actual del repositorio

La estructura base del proyecto ya fue definida para permitir implementación modular.
A partir de este punto, cada integrante puede comenzar directamente su parte.

### Pendiente por implementar

* lógica interna de clases;
* scripts SQL completos;
* recursos lingüísticos definitivos;
* integración entre módulos;
* pruebas funcionales completas;
* dataset final de documentos.

---

## 17. Recomendaciones de desarrollo

* mantener commits pequeños y claros;
* trabajar por ramas;
* no mezclar lógica de distintos módulos;
* probar localmente antes de integrar;
* dejar comentarios técnicos en clases base;
* documentar decisiones importantes en `docs/`.

---

## 18. Posibles nombres del proyecto

Nombre formal sugerido:

**Latent Semantic Indexing Document Base**

Nombre descriptivo alternativo:

**Sistema de Recuperación Documental con LSI**

---

## 19. Autores y equipo

Proyecto colaborativo desarrollado para la materia de bases de datos avanzadas.

Equipo de desarrollo organizado por módulos independientes:

* configuración e integración
* persistencia
* preprocesamiento
* semántica
* indexación/LSI
* consultas/interfaz

---

## 20. Nota final para el equipo

Cada integrante debe avanzar **sin esperar a que los demás terminen**.
La prioridad es dejar el módulo propio:

* estructurado,
* documentado,
* funcional en su alcance,
* y listo para integrarse cuando llegue el momento.

La integración final debe ser un proceso de conexión entre módulos ya preparados, no una etapa donde todavía se empiece a construir la lógica principal.


Si quieres, también te puedo dar una **versión más corta y más profesional** para que el README no quede tan largo en GitHub.
```
