# Modelo de amenazas — FoodHelp (backend Spring Boot / Kotlin)

## Autorización por hogar (multi-inquilino)
- Todo recurso (artículo, alerta, miembro, dispositivo) pertenece a un hogar. Cualquier endpoint o consulta que reciba un id debe verificar que el recurso pertenece al hogar del usuario autenticado.
- Si el recurso es de otro hogar, la respuesta debe ser 404 uniforme (nunca 403) para no revelar qué ids existen. Las rutas con `{hogarId}` usan `exigirPertenenciaAHogar`.
- Nunca confiar en un `hogarId` o `usuarioId` enviado por el cliente sin compararlo con el usuario autenticado.

## Autenticación y sesiones
- El claim `tv` del JWT debe coincidir con `Usuario.tokenVersion`; no saltarse esa verificación en el filtro.
- Las operaciones sensibles (eliminar cuenta, cambiar contraseña) responden 403, no 401, si la contraseña es incorrecta, y cuentan intentos en el limitador.
- El mensaje de error de login debe ser idéntico para correo inexistente y contraseña errónea.

## Secretos y datos sensibles
- No registrar en logs: tokens JWT, tokens FCM, contraseñas, claves de API (Spoonacular, Firebase) ni URLs con claves.
- Las claves de API viajan en cabeceras, no en la URL. Las credenciales de Firebase no se escriben en el repositorio ni en la imagen Docker.
- Las respuestas de la API nunca incluyen `contrasena` ni `tokenVersion`.

## Consistencia y robustez
- Las modificaciones de stock usan el UPDATE atómico condicional; no leer-modificar-escribir.
- Fechas de negocio: usar el `Clock` inyectado, nunca `LocalDate.now()` sin argumento.
- Entradas: todo `@RequestBody` lleva `@Valid`; toda lista expuesta por la API está paginada.
- Migraciones Flyway ya aplicadas (V1 en adelante) no se editan: se crea una nueva.
