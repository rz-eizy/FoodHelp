# Deuda técnica — FoodHelp

## Cuentas
- Registro con verificación de correo (hoy se puede registrar un correo inexistente).
- Recuperación de contraseña (requiere proveedor de correo; sin verificación de correo, los usuarios con correos inexistentes no podrían recuperarla). Alternativa sin correo: códigos de recuperación de un solo uso.
- Cambio de contraseña autenticado (`PATCH /api/usuarios/me/password`, con token nuevo y cierre de las demás sesiones).
- Refresh tokens (hoy el JWT dura 7 días sin refresh).
- Límite de intentos del registro; los límites actuales viven en memoria (por instancia).

## Notificaciones push (FCM)
- Diseño previsto: tabla `dispositivos_fcm` (usuario, token único, plataforma, fechas), `PUT /api/usuarios/me/dispositivos` (idempotente, máx. 10 por usuario) y `POST /api/usuarios/me/dispositivos/baja`; interfaz `NotificadorPush` con implementación real (`firebase-admin`) y otra vacía si `app.fcm.enabled=false`; una notificación diaria resumen por hogar ("N por vencer, M vencidos") mientras existan artículos con alerta; los tokens inválidos se purgan; un fallo de FCM nunca aborta el monitoreo; logout y eliminación de cuenta borran los dispositivos. Credenciales de Firebase fuera del repo y de la imagen. Con más de una instancia hace falta ShedLock para no duplicar envíos.

## Inventario y recetas
- Campo opcional `nombreIngles` en artículos; el frontend aporta la tabla de traducción español→inglés (Spoonacular solo entiende inglés).
- RF-04 filtros de inventario (categoría y búsqueda), RF-07 autocompletado de nombres, RF-12 detalle de receta (ingredientes y pasos).

## Plataforma y operación
- ShedLock para el cron con varias instancias.
- `application.yml` conserva el default de desarrollo `POSTGRES_PASSWORD:123postgresql`: eliminarlo y exigir `.env`.
- `all-open` para las entidades (hoy son `final` y los `LAZY` no generan proxies).
- Pruebas contra PostgreSQL con Testcontainers (hoy los tests usan H2 y las migraciones solo las valida la CI).
- Digest fijo de las imágenes Docker; TLS en el proxy inverso; CD (publicar imagen).
