# ADR 0002: Estrategia de Ramificación y Estándares de Integración (Git Workflow)

**Fecha:** 2026-10-23
**Estado:** Aceptado

## Contexto
La adopción de un monorepo que alberga tanto el ecosistema Android como el backend de Spring Boot exige un control estricto sobre cómo se introduce el código. Dado que hemos implementado validaciones automatizadas (Lefthook) para el formato del código y mensajes de commit basados en *Conventional Commits*, es necesario definir una topología de ramas y reglas de integración que prevengan colisiones, mantengan un historial lineal y preparen el terreno para futuros pipelines de CI/CD.

## Decisión
Se adoptará un modelo basado en **Trunk-Based Development modificado (con rama de integración)**, aplicando las siguientes normativas:

### 1. Topología de Ramas
* **`main`**: Rama de producción. Refleja el código estable y desplegable.
* **`develop`**: Rama base de integración. Todo el desarrollo diario converge aquí antes de pasar a producción.
* **Ramas Efímeras (Features/Bugfix):** Se crean a partir de `develop` y se eliminan tras ser integradas.

### 2. Nomenclatura Estricta
Las ramas efímeras deben reflejar el tipo de trabajo y el módulo afectado, alineándose con nuestro archivo `lefthook.yml`.
* **Formato:** `tipo/modulo-descripcion-corta`
* **Ejemplos Válidos:** 
  - `feat/backend/crud-inventario`
  - `fix/backend/conexion-db`
  - `docs/adr-002`

### 3. Reglas de Integración (Pull Requests)
* **Prohibición de Push Directo:** Queda estrictamente prohibido realizar *commits* directos o *merges* manuales hacia `main` y `develop`.
* **Uso Obligatorio de Pull Requests (PR):** Toda rama efímera debe integrarse a `develop` mediante un PR. Esto garantiza un espacio para revisión asíncrona y la ejecución de *checks* automatizados.
* **Estrategia "Squash and Merge":** Al aceptar un PR hacia `develop`, los *commits* de la rama efímera deben comprimirse en uno solo (*squash*). Esto mantiene el historial de `develop` limpio, lineal y fácil de revertir en caso de errores, evitando la contaminación con *commits* de trabajo (ej. "wip", "fix typo").

## Consecuencias
* **Positivas:** 
  - El historial de Git se mantiene semántico y lineal, facilitando la generación automática de *Changelogs*.
  - El aislamiento del código mediante PRs protege las ramas estables de rupturas accidentales.
  - La nomenclatura estandarizada permite identificar instantáneamente qué módulo del monorepo está siendo modificado.
* **Negativas / Riesgos Aceptados:**
  - Requiere mayor disciplina operativa y configuración de protección de ramas en el servidor remoto (GitHub/GitLab), añadiendo una ligera fricción al desarrollo diario frente a un modelo de *commit* directo.