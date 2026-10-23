# ADR 0001: Definición del Stack Tecnológico y Arquitectura Base

**Fecha:** 2026-10-23
**Estado:** Aceptado

## Contexto
El desarrollo de FoodHelp requiere la construcción de un Producto Mínimo Viable (MVP) enfocado en mitigar el desperdicio de alimentos. El sistema exige un cliente móvil para la interacción de baja fricción del usuario y un motor centralizado que maneje la concurrencia de datos en entornos compartidos ("Hogares"), tareas programadas para la verificación de fechas de caducidad y la orquestación de integraciones con APIs externas de recetas. Es imperativo optimizar el tiempo de desarrollo y mantener un código limpio que facilite el debugging y la escalabilidad de la lógica de negocio.

## Decisión
Se adoptará una arquitectura de cliente ligero y servidor robusto bajo una estructura de **Monorepo dividido por dominios**, estandarizando el uso de **Kotlin** en todo el stack tecnológico.

1. **Estructura del Repositorio:**
   - `/backend`: Ecosistema Spring Boot gestionado de forma independiente con **Maven**.
   - `/frontend`: Ecosistema Android nativo gestionado de forma independiente con **Gradle**.
2. **Backend (Motor Central):** Se utilizará Spring Boot 3+ (con JDK 21). La persistencia se manejará mediante Spring Data JPA, la seguridad con Spring Security y JWT (stateless), y los procesos asíncronos de monitoreo a través de tareas programadas (`@Scheduled`).
3. **Base de Datos:** PostgreSQL. Seleccionado por su capacidad superior en el manejo de integridad referencial estricta, consultas transaccionales concurrentes y soporte nativo para almacenamiento de estructuras complejas (JSONB para pasos de recetas).
4. **Frontend (Cliente Móvil):** Aplicación nativa Android implementada en Jetpack Compose. Seguirá el patrón MVVM, delegando todo cálculo complejo al servidor. Se utilizará Dagger Hilt para inyección de dependencias y Retrofit para el consumo de la API REST.

## Consecuencias
* **Positivas:** 
  - La adopción transversal de Kotlin unifica el modelo mental, permitiendo que los Modelos de Dominio y los *Data Transfer Objects* (DTOs) compartan la misma estructura y sintaxis tanto en el cliente como en el servidor.
  - El uso estricto de Kotlin en Spring Boot elimina la necesidad de librerías de generación de código como Lombok, reduciendo el *boilerplate* mediante el uso nativo de *data classes*.
  - Desacoplar Maven y Gradle previene problemas de configuración interdependientes y facilita la creación de pipelines de CI/CD aislados en el futuro.
* **Negativas / Riesgos Aceptados:**
  - Mantener dos sistemas de construcción distintos exige ejecutar comandos de terminal separados dependiendo del directorio de trabajo, un compromiso justificado frente a la complejidad y lentitud de forzar un proyecto Spring Boot y uno de Android nativo en un único script de Gradle.