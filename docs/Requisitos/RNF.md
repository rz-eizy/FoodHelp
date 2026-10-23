# Requisitos No Funcionales (RNF)

**RNF-01: Eficiencia de Acceso.** La vista principal del inventario y la lista de productos críticos deben ser accesibles desde la pantalla de inicio en un máximo de 2 clics.
**RNF-02: Fricción de Ingreso.** El formulario de ingreso manual de un nuevo producto no debe exigir más de tres campos obligatorios.
**RNF-03: Alertas Push.** El sistema debe utilizar notificaciones Push (ej. vía Firebase Cloud Messaging integrado con Spring Boot) para enviar las alertas de caducidad de manera asíncrona, sin que la app esté en primer plano.
**RNF-04: Lógica Centralizada.** Toda la comunicación con APIs de terceros (recetas) debe realizarse exclusivamente a través del backend (Spring Boot), prohibiendo llamadas directas desde la aplicación Android.
**RNF-05: Plataforma.** El cliente móvil debe ser compatible y presentar disponibilidad completa para dispositivos con sistema operativo Android (versión mínima a definir, ej. API 24).
