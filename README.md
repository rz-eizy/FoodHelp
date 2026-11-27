# FoodHelp

Gestor inteligente de despensa diseñado para hogares colaborativos. FoodHelp es una aplicación móvil nativa para Android respaldada por un backend en Spring Boot, cuyo objetivo principal es mitigar el desperdicio de alimentos mediante alertas proactivas de caducidad y sugerencias de recetas basadas en el inventario crítico.

## Características Principales
* **Entornos Colaborativos:** Gestión de inventario compartida en tiempo real mediante la creación de "Hogares" con códigos de invitación.
* **Monitoreo Proactivo:** Evaluaciones automáticas diarias en el servidor que detectan productos próximos a vencer y notifican a los usuarios.
* **Inventario Ágil:** Registro de productos optimizado con enfoque en la fecha de caducidad como atributo crítico de control.
* **Orquestación de Recetas:** Integración con APIs externas para sugerir preparaciones utilizando estrictamente los alimentos detectados en estado crítico.

## Stack Tecnológico
**Frontend (Android)**
* **Lenguaje:** Kotlin
* **Interfaz:** Jetpack Compose (Material 3)
* **Arquitectura:** MVVM (Model-View-ViewModel)
* **Herramientas:** Dagger Hilt (Inyección de dependencias), Retrofit (Cliente HTTP), Coil (Carga asíncrona de imágenes).

**Backend (Spring Boot)**
* **Lenguaje:** Kotlin (JDK 21)
* **Framework Base:** Spring Boot
* **Persistencia:** PostgreSQL, Spring Data JPA
* **Seguridad:** Spring Security con JSON Web Tokens (JWT)
* **Automatización:** Tareas programadas nativas (`@Scheduled`)

## Estructura del Monorepo
El proyecto adopta una estructura de monorepo separada por ecosistemas para aislar responsabilidades y mantener configuraciones de construcción independientes.

* **`/backend`**: Ecosistema Spring Boot gestionado con Maven. Contiene la lógica de negocio, manejo de transacciones concurrentes, control de base de datos y orquestación de APIs de terceros.
* **`/frontend`**: Ecosistema Android nativo gestionado con Gradle. Actúa como un cliente ligero enfocado exclusivamente en la renderización del estado de la UI y la experiencia de usuario.

## Decisiones Técnicas Fundamentales
1. **Delegación de Carga al Servidor (Cliente Ligero):** La aplicación móvil no realiza cálculos de caducidad, cruce de datos complejos ni llamadas directas a las APIs de recetas. Toda la lógica pesada reside en el backend para optimizar el rendimiento, asegurar la consistencia y reducir el consumo de recursos en el dispositivo físico.
2. **Desacoplamiento de Herramientas de Construcción:** Se utiliza Maven para el backend y Gradle para el frontend. Esto evita la fricción de unificar ecosistemas con paradigmas distintos, respetando los estándares industriales de cada plataforma.
3. **Integridad Relacional del Inventario:** A nivel de base de datos, los artículos físicos se vinculan estrictamente a la entidad `Hogar` (composición estructural) y no al `Usuario`. Esto garantiza que los datos permanezcan consistentes y accesibles frente a la entrada o salida de miembros en el grupo colaborativo.
4. **Homologación Lingüística en Kotlin:** La adopción transversal de Kotlin elimina el código repetitivo en las entidades de Spring Boot y establece un modelo mental uniforme. Los *Data Transfer Objects* (DTOs) diseñados en el servidor comparten la misma estructura sintáctica que los modelos consumidos por la arquitectura MVVM en Android.