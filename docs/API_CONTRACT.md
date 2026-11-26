# Contrato de API — Backend FoodHelp (cliente Android nativo)

Este documento detalla exhaustivamente el contrato HTTP y las especificaciones de integración de la API REST del backend de FoodHelp para el equipo de desarrollo de la aplicación móvil Android nativa (Kotlin y Jetpack Compose).

---

## 1. Arquitectura y Reglas Generales

- **Base URL (Desarrollo):** `http://localhost:8080` (en emulador Android usar `http://10.0.2.2:8080`)
- **Protocolo y Formato:** HTTP/1.1 o HTTP/2, `Content-Type: application/json; charset=UTF-8`
- **Móvil First (Sin CORS):** El backend interactúa exclusivamente con el cliente móvil Android nativo. No hay soporte ni cabeceras CORS habilitadas.
- **Formato de Fechas:** `YYYY-MM-DD` (ISO-8601 estándar, ej. `2026-12-31`).
- **Identificadores (IDs):** UUID versión 4 en minúsculas con guiones (ej. `3fa85f64-5717-4562-b3fc-2c963f66afa6`), salvo IDs de recetas externas de Spoonacular que son enteros (`Int`).
- **Código de Hogar:** Cadena de exactamente 10 caracteres alfanuméricos en mayúsculas generada a partir de un alfabeto seguro de 32 símbolos sin caracteres visualmente ambiguos (`ABCDEFGHJKLMNPQRSTUVWXYZ23456789`, excluye `0`, `O`, `1`, `I`).

---

## 2. Flujo de Autenticación y Ciclo de Vida del Token

### 2.1. Tokens JWT y Versionado (`tokenVersion`)
- El backend emite tokens JWT (JSON Web Tokens) firmados con algoritmo **HMAC-SHA256**.
- Cada token contiene los siguientes claims relevantes:
  - `sub`: Correo electrónico del usuario (normalizado en minúsculas y sin espacios).
  - `tv`: Versión actual del token (`tokenVersion`, entero).
  - `iat`: Timestamp de emisión.
  - `exp`: Timestamp de expiración (7 días = 604.800.000 ms).
- **Cabecera requerida en endpoints protegidos:**
  ```http
  Authorization: Bearer <token>
  ```

### 2.2. Registro y Login
1. El usuario se registra vía `POST /api/auth/registro` o inicia sesión vía `POST /api/auth/login`.
2. Ambos endpoints retornan `{"token": "<jwt>", "tipo": "Bearer"}`.

### 2.3. Cierre de Sesión (Logout) y Revocación
- Para cerrar sesión se debe invocar `POST /api/usuarios/me/logout` con la cabecera `Authorization`.
- El backend incrementa de forma atómica el campo `token_version` del usuario en la base de datos y responde HTTP `204 No Content`.
- Cualquier petición posterior realizada con ese mismo token (o tokens emitidos anteriormente) será rechazada inmediatamente con HTTP `401 Unauthorized` porque el claim `tv` del token ya no coincidirá con el `tokenVersion` actual del usuario.

### 2.4. Qué debe hacer el cliente Android ante un HTTP 401
Cuando el cliente móvil reciba un código `401 Unauthorized`:
1. Borrar inmediatamente el token JWT del almacenamiento cifrado (Jetpack Security / Keystore).
2. Limpiar la sesión en memoria (estado de los ViewModels).
3. Redirigir al usuario de forma automática a la pantalla de Login mostrando un mensaje explicativo (ej. "Tu sesión ha expirado o ha sido cerrada en otro dispositivo. Por favor ingresa nuevamente").

El 401 incluye cuerpo JSON: `{"error": "No autenticado", "codigo": "NO_AUTENTICADO"}`.

Un `403 Forbidden` **no** equivale a un 401: no se debe cerrar la sesión (p. ej. una contraseña incorrecta en una operación sensible devuelve 403). El `codigo` del 403 distingue `SIN_HOGAR`, `HOGAR_AJENO`, `NO_PROPIETARIO`, `EXPULSADO_DEL_HOGAR`, `CONTRASENA_INCORRECTA` y `ACCESO_DENEGADO`, sin necesidad de leer el texto. Un `429 Too Many Requests` se muestra como "demasiados intentos", sin borrar la sesión.

---

## 3. Esquema Estándar de Errores

Todos los errores producidos en la aplicación —incluidos los de seguridad (401/403) y los generados por Spring (400/404/405/415/500)— siguen una estructura JSON homogénea con `error` (texto en español para la persona usuaria) y `codigo` (identificador estable para el cliente). El campo `campos` solo aparece en errores de validación.

### 3.1. Error General
```json
{
  "error": "Descripción legible en español del problema",
  "codigo": "SOLICITUD_INVALIDA"
}
```

### 3.2. Error de Validación de Campos (HTTP 400)
Producido cuando los campos del cuerpo no cumplen las validaciones Jakarta Validation (`@field:NotBlank`, `@field:Size`, etc.):
```json
{
  "error": "Datos de entrada inválidos",
  "codigo": "VALIDACION",
  "campos": {
    "correo": "El correo no es válido",
    "contrasena": "La contraseña debe tener entre 8 y 72 caracteres"
  }
}
```

### 3.3. Catálogo de Códigos HTTP

| Código | Significado | Causa común en la API |
| :--- | :--- | :--- |
| **200 OK** | Éxito | Operación de lectura o actualización exitosa. |
| **201 Created** | Creado | Recurso creado exitosamente (registro, nuevo hogar, nuevo artículo). |
| **204 No Content** | Sin contenido | Operación completada sin cuerpo de retorno (logout, eliminar artículo, salir del hogar, eliminar cuenta). |
| **400 Bad Request** | Solicitud errónea | JSON malformado, validación de campos fallida, cantidad menor a 1, intentar actualizar artículo sin campos válidos, o inventario vacío al pedir recetas. |
| **401 Unauthorized** | No autenticado | Token ausente, expirado, inválido, claim `tv` desactualizado, o credenciales erróneas en login. |
| **403 Forbidden** | Prohibido | Usuario sin hogar asignado, peticiones a rutas con `{hogarId}` de un hogar al que no pertenece, o contraseña incorrecta al eliminar la cuenta. |
| **404 Not Found** | No encontrado | Recurso inexistente o ajeno: artículo/alerta de otro hogar y `GET /api/hogares/{codigo}` con código inexistente o ajeno (respuesta uniforme por privacidad). |
| **409 Conflict** | Conflicto | Correo ya registrado, usuario ya pertenece a un hogar, o usuario intenta salir sin pertenecer a un hogar. |
| **429 Too Many Requests** | Límite excedido | Bloqueo por fuerza bruta (más de 10 intentos fallidos en login, unirse a hogar o eliminar cuenta). |
| **500 Internal Server Error** | Error del servidor | Error inesperado en el servidor backend. |

### 3.4. Catálogo de códigos `codigo`

El cliente debe decidir por `codigo`, nunca por el texto de `error` (que puede cambiar). Los textos y los status HTTP no cambian respecto a versiones anteriores salvo en los casos que antes caían en el JSON por defecto de Spring.

| `codigo` | HTTP | Cuándo |
| :--- | :--- | :--- |
| `NO_AUTENTICADO` | 401 | Token ausente, inválido, expirado o revocado (`tv` desactualizado). |
| `CREDENCIALES_INVALIDAS` | 401 | Login con correo o contraseña incorrectos (mensaje idéntico para ambos). |
| `ACCESO_DENEGADO` | 403 | Cualquier 403 sin código más específico (incluye `AccessDeniedException` de Spring Security). |
| `SIN_HOGAR` | 403 / 409 | El usuario no tiene hogar: 403 en inventario, alertas y recetas; 409 en salir y expulsar. |
| `HOGAR_AJENO` | 403 | Ruta con `{hogarId}` de un hogar al que no pertenece. |
| `NO_PROPIETARIO` | 403 | Expulsar sin ser el propietario. |
| `EXPULSADO_DEL_HOGAR` | 403 | Unirse a un hogar del que fue expulsado. |
| `CONTRASENA_INCORRECTA` | 403 | Eliminar cuenta con contraseña incorrecta. |
| `VALIDACION` | 400 | Campos del cuerpo inválidos (lleva `campos`). |
| `SOLICITUD_INVALIDA` | 400 | Regla de negocio genérica (sin campos válidos, expulsarse a sí mismo, `cantidad < 1`…). |
| `FECHA_VENCIMIENTO_PASADA` | 400 | Fecha de vencimiento anterior a hoy. |
| `CANTIDAD_INSUFICIENTE` | 400 | Restar más de lo disponible. |
| `INVENTARIO_VACIO` | 400 | Pedir sugerencias de recetas con el inventario vacío. |
| `JSON_MALFORMADO` | 400 | Cuerpo ilegible. |
| `PARAMETRO_INVALIDO` | 400 | UUID, número o enum inválido en ruta o query, o parámetro requerido ausente. |
| `ARTICULO_NO_ENCONTRADO` | 404 | Artículo inexistente o de otro hogar (misma respuesta). |
| `ALERTA_NO_ENCONTRADA` | 404 | Alerta inexistente o de otro hogar. |
| `HOGAR_NO_ENCONTRADO` | 404 | Código de hogar inexistente o ajeno. |
| `MIEMBRO_NO_ENCONTRADO` | 404 | Objetivo de expulsión inexistente o de otro hogar. |
| `RUTA_NO_ENCONTRADA` | 404 | La ruta no existe (con token válido). |
| `METODO_NO_PERMITIDO` | 405 | Método HTTP no soportado en esa ruta. |
| `TIPO_CONTENIDO_NO_SOPORTADO` | 415 | `Content-Type` no soportado. |
| `CORREO_YA_REGISTRADO` | 409 | Registro con correo existente. |
| `USUARIO_YA_EN_HOGAR` | 409 | Crear o unirse a un hogar teniendo ya uno. |
| `CONFLICTO` | 409 | Fallo de bloqueo pesimista o colisión de restricción UNIQUE. |
| `DEMASIADOS_INTENTOS` | 429 | Límite de intentos fallidos excedido. |
| `ERROR_INTERNO` | 500 | Error inesperado; el detalle nunca se expone al cliente. |

---

## 4. Bloqueo de Fuerza Bruta (Rate Limiting)

El backend implementa limitación de tasa en memoria basada en Caffeine para mitigar ataques de fuerza bruta:

1. **Login (`POST /api/auth/login`):**
   - **Clave:** Correo electrónico normalizado.
   - **Límite:** 10 intentos fallidos consecutivos en una ventana de 5 minutos.
   - **Efecto:** El 11º intento responderá HTTP `429 Too Many Requests` con el cuerpo:
     ```json
     {
       "error": "Demasiados intentos. Inténtalo de nuevo más tarde"
     }
     ```
   - **Reinicio:** Un login exitoso resetea el contador de fallos inmediatamente.

2. **Unirse a Hogar (`POST /api/hogares/unirse`):**
   - **Clave:** Correo del usuario autenticado.
   - **Límite:** 10 intentos fallidos con códigos de hogar inexistentes en una ventana de 10 minutos.
   - **Efecto:** El 11º intento responderá HTTP `429 Too Many Requests` con el mismo mensaje.
   - **Reinicio:** Unirse exitosamente a un hogar resetea el contador.

---

## 5. Catálogo de Enums

### 5.1. `CategoriaEnum`
Utilizado para clasificar los artículos del inventario de despensa:
- `LACTEOS`
- `CARNES`
- `VEGETALES`
- `FRUTAS`
- `BEBIDAS`
- `PANADERIA`
- `DESPENSA`
- `CONGELADOS`
- `OTROS`

---

## 6. Endpoints Exhaustivos

### 6.1. Módulo de Autenticación (`/api/auth`)

#### `POST /api/auth/registro`
- **Autenticación:** No requerida.
- **Descripción:** Registra una nueva cuenta de usuario y retorna el JWT inicial.
- **Request Body:**
  ```json
  {
    "nombre": "Ana Gómez",
    "correo": "ana@ejemplo.com",
    "contrasena": "MiPasswordSeguro123"
  }
  ```
  - `nombre`: String, obligatorio, máx 100 caracteres.
  - `correo`: String, obligatorio, formato email válido, máx 254 caracteres.
  - `contrasena`: String, obligatorio, entre 8 y 72 caracteres.
- **Respuestas:**
  - `201 Created`:
    ```json
    {
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "tipo": "Bearer"
    }
    ```
  - `400 Bad Request`: Datos de validación inválidos.
  - `409 Conflict`: Si el correo ya se encuentra registrado.

#### `POST /api/auth/login`
- **Autenticación:** No requerida.
- **Descripción:** Autentica credenciales y emite un JWT. Sujeto a rate limiting (10 fallos / 5 min).
- **Request Body:**
  ```json
  {
    "correo": "ana@ejemplo.com",
    "contrasena": "MiPasswordSeguro123"
  }
  ```
  - `correo`: String, obligatorio, formato email válido.
  - `contrasena`: String, obligatorio, máx 72 caracteres.
- **Respuestas:**
  - `200 OK`:
    ```json
    {
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "tipo": "Bearer"
    }
    ```
  - `400 Bad Request`: Formato de correo o contraseña vacío.
  - `401 Unauthorized`: Credenciales inválidas.
  - `429 Too Many Requests`: Más de 10 intentos fallidos acumulados.

---

### 6.2. Módulo de Usuarios (`/api/usuarios`)

#### `GET /api/usuarios/me`
- **Autenticación:** Requerida (`Bearer`).
- **Descripción:** Obtiene los datos del perfil del usuario autenticado y su hogar asignado (o `null` si no pertenece a ninguno).
- **Respuestas:**
  - `200 OK`:
    ```json
    {
      "id": "c1f7b82e-9d21-4f76-88a1-5f053e1ad944",
      "nombre": "Ana Gómez",
      "correo": "ana@ejemplo.com",
      "hogar": {
        "id": "e4b9d032-124b-4a5f-9562-b9e7bdfa1832",
        "nombre": "Familia Gómez",
        "codigoHogar": "ABCDEF2345"
      }
    }
    ```
    *(Si el usuario aún no tiene hogar, el campo `"hogar"` es `null`)*.
  - `401 Unauthorized`: Token ausente, revocado o inválido.

#### `POST /api/usuarios/me/logout`
- **Autenticación:** Requerida (`Bearer`).
- **Descripción:** Cierra la sesión revocando todos los tokens JWT emitidos previamente para el usuario.
- **Respuestas:**
  - `204 No Content`: Sesión cerrada exitosamente.
  - `401 Unauthorized`: Token ausente o inválido.

#### `POST /api/usuarios/me/eliminar`
- **Autenticación:** Requerida (`Bearer`).
- **Descripción:** Elimina la cuenta de forma irreversible mediante soft delete: anonimiza el nombre, el correo y la contraseña, registra `eliminadoEn` e incrementa `tokenVersion`, de modo que los tokens anteriores dejan de ser válidos y el correo original queda libre para un nuevo registro. Si el usuario pertenecía a un hogar, se aplica la misma regla que al salir: la propiedad pasa al integrante más antiguo y, si era el último integrante, se eliminan el hogar, su inventario, sus alertas y sus expulsados. Comparte con el login el limitador de 10 fallos por correo.
- **Advertencia para el cliente:** pedir confirmación explícita antes de invocarlo; la operación no se puede deshacer y debe cerrar la sesión local tras el `204`.
- **Request Body:**
  ```json
  {
    "contrasena": "Password123"
  }
  ```
- **Respuestas:**
  - `204 No Content`: cuenta eliminada.
  - `400 Bad Request`: contraseña ausente o de más de 72 caracteres.
  - `401 Unauthorized`: token ausente, inválido o revocado.
  - `403 Forbidden`: contraseña incorrecta. **No equivale a 401**: muestra el error y permite reintentar sin cerrar la sesión.
  - `429 Too Many Requests`: más de 10 contraseñas incorrectas.

---

### 6.3. Módulo de Hogares (`/api/hogares`)

#### `POST /api/hogares`
- **Autenticación:** Requerida (`Bearer`).
- **Descripción:** Crea un nuevo hogar asignando al usuario creador como propietario (`esPropietario: true`). El usuario no debe pertenecer a otro hogar previamente.
- **Request Body:**
  ```json
  {
    "nombre": "Casa de Campo"
  }
  ```
  - `nombre`: String, obligatorio, máx 100 caracteres.
- **Respuestas:**
  - `201 Created`:
    ```json
    {
      "id": "e4b9d032-124b-4a5f-9562-b9e7bdfa1832",
      "nombre": "Casa de Campo",
      "codigoHogar": "K7W9MNP2QR",
      "esPropietario": true,
      "usuarios": [
        {
          "id": "c1f7b82e-9d21-4f76-88a1-5f053e1ad944",
          "nombre": "Ana Gómez",
          "esPropietario": true
        }
      ]
    }
    ```
  - `400 Bad Request`: Nombre vacío o mayor a 100 caracteres.
  - `401 Unauthorized`: Token ausente o inválido.
  - `409 Conflict`: El usuario ya pertenece a un hogar (`UsuarioYaEnHogarException`).

#### `POST /api/hogares/unirse`
- **Autenticación:** Requerida (`Bearer`).
- **Descripción:** Une al usuario a un hogar existente mediante su código de invitación (10 caracteres). Registra la fecha de ingreso y, si el hogar no tiene propietario, el nuevo integrante pasa a serlo. Un usuario expulsado de ese hogar no puede volver a unirse (`403 Forbidden`, sin contar para el limitador). Sujeto a rate limiting (10 fallos / 10 min).
- **Request Body:**
  ```json
  {
    "codigoHogar": "K7W9MNP2QR"
  }
  ```
  - `codigoHogar`: String, obligatorio, máx 32 caracteres (alfanumérico).
- **Respuestas:**
  - `200 OK`:
    ```json
    {
      "id": "e4b9d032-124b-4a5f-9562-b9e7bdfa1832",
      "nombre": "Casa de Campo",
      "codigoHogar": "K7W9MNP2QR",
      "esPropietario": true,
      "usuarios": [
        {
          "id": "c1f7b82e-9d21-4f76-88a1-5f053e1ad944",
          "nombre": "Ana Gómez",
          "esPropietario": true
        },
        {
          "id": "9f8432a1-55bb-4770-bc10-1845bbcd4123",
          "nombre": "Carlos López",
          "esPropietario": false
        }
      ]
    }
    ```
  - `400 Bad Request`: Código vacío.
  - `401 Unauthorized`: Token inválido.
  - `404 Not Found`: Código de hogar inexistente.
  - `409 Conflict`: El usuario ya pertenece a un hogar.
  - `429 Too Many Requests`: Más de 10 intentos fallidos acumulados.

#### `GET /api/hogares/{codigoHogar}`
- **Autenticación:** Requerida (`Bearer`).
- **Descripción:** Obtiene los datos del hogar por su código. Solo los usuarios miembros del hogar pueden consultar este endpoint. Si el código no existe o el usuario no es miembro, responde 404 uniforme.
- **Parámetros de ruta:**
  - `codigoHogar`: String (10 caracteres).
- **Respuestas:**
  - `200 OK`: `HogarResponse` (mismo formato que unirse).
  - `401 Unauthorized`: Token inválido.
  - `404 Not Found`: Hogar no encontrado o usuario no es miembro.

#### `POST /api/hogares/salir`
- **Autenticación:** Requerida (`Bearer`).
- **Descripción:** Desvincula al usuario autenticado de su hogar actual. Si el usuario era el propietario, la propiedad se traspasa al integrante más antiguo. Si era el último integrante, se eliminan el hogar, su inventario, sus alertas y su registro de expulsados.
- **Respuestas:**
  - `204 No Content`: Salida exitosa.
  - `401 Unauthorized`: Token inválido.
  - `409 Conflict`: El usuario no pertenece a ningún hogar (`UsuarioSinHogarException`) o hubo conflicto de bloqueo.

#### `DELETE /api/hogares/miembros/{usuarioId}`
- **Autenticación:** Requerida (`Bearer`).
- **Descripción:** Expulsa a otro integrante del hogar del usuario autenticado. Solo puede hacerlo el propietario; el afectado queda registrado en `hogar_expulsados` y no podrá volver a unirse. El JWT del expulsado sigue válido, pero ya no pertenece al hogar.
- **Parámetros de ruta:** `usuarioId` (UUID).
- **Respuestas:**
  - `204 No Content`: Integrante expulsado.
  - `400 Bad Request`: El usuario intenta expulsarse a sí mismo; debe usar `POST /api/hogares/salir`.
  - `401 Unauthorized`: Token inválido.
  - `403 Forbidden`: El actor no es el propietario (`Solo el propietario del hogar puede expulsar integrantes`).
  - `404 Not Found`: El objetivo no existe o no pertenece al hogar (`MiembroNotFoundException`, misma respuesta para ambos).
  - `409 Conflict`: El actor no pertenece a ningún hogar o hubo conflicto de bloqueo.

---

### 6.4. Módulo de Inventario (`/api/inventario`)

#### `GET /api/inventario`
- **Autenticación:** Requerida (`Bearer`).
- **Descripción:** Lista los artículos del inventario del hogar del usuario autenticado, ordenados por fecha de vencimiento ascendente.
- **Query Params:**
  - `page`: Entero, opcional, default `0`. Mínimo `0`.
  - `size`: Entero, opcional, default `50`. Mínimo `1`, máximo `100`.
- **Respuestas:**
  - `200 OK`:
    ```json
    [
      {
        "id": "1a2b3c4d-0000-0000-0000-000000000001",
        "nombre": "Leche Descremada",
        "fechaVencimiento": "2026-12-20",
        "cantidad": 2,
        "categoria": "LACTEOS",
        "hogarId": "e4b9d032-124b-4a5f-9562-b9e7bdfa1832"
      }
    ]
    ```
  - `401 Unauthorized`: Token inválido.
  - `403 Forbidden`: Usuario sin hogar asignado.

#### `GET /api/inventario/hogar/{hogarId}`
- **Autenticación:** Requerida (`Bearer`).
- **Descripción:** Lista los artículos de un hogar específico por ID. El usuario debe pertenecer a dicho hogar.
- **Parámetros de ruta:** `hogarId` (UUID).
- **Query Params:** `page` (default 0), `size` (default 50, máx 100).
- **Respuestas:**
  - `200 OK`: Lista paginada de `ArticuloDTO`.
  - `401 Unauthorized`: Token inválido.
  - `403 Forbidden`: El usuario no pertenece a ese hogar.

#### `POST /api/inventario`
- **Autenticación:** Requerida (`Bearer`).
- **Descripción:** Registra un nuevo artículo en el inventario del hogar actual.
- **Request Body:**
  ```json
  {
    "nombre": "Arroz Integral 1kg",
    "fechaVencimiento": "2026-11-30",
    "cantidad": 3,
    "categoria": "DESPENSA"
  }
  ```
  - `nombre`: String, obligatorio, máx 120 caracteres.
  - `fechaVencimiento`: String fecha (`YYYY-MM-DD`), obligatorio. Debe ser hoy o futura; se valida en el servicio con el `Clock` inyectado (no con `@FutureOrPresent`), por lo que "hoy" es válido en cualquier zona horaria.
  - `cantidad`: Entero, obligatorio, mínimo 1.
  - `categoria`: Enum `CategoriaEnum`, opcional, default `"OTROS"`.
- **Respuestas:**
  - `201 Created`: `ArticuloDTO` creado.
  - `400 Bad Request`: Campos inválidos o fecha de vencimiento anterior a hoy (`SolicitudInvalidaException`: `{"error": "La fecha de vencimiento no puede ser anterior a hoy"}`).
  - `401 Unauthorized`: Token inválido.
  - `403 Forbidden`: Usuario sin hogar.

#### `PATCH /api/inventario/{articuloId}/restar`
- **Autenticación:** Requerida (`Bearer`).
- **Descripción:** Disminuye la cantidad de un artículo mediante un `UPDATE` atómico condicional. Si la cantidad restante llega a 0, el artículo y sus alertas se eliminan, pero la respuesta sigue siendo `200 OK` con `cantidad: 0`.
- **Parámetros:**
  - Ruta: `articuloId` (UUID).
  - Cantidad a restar: se puede enviar vía Query Param `?cantidad=N` o en el Body `{"cantidad": N}`. Si no se envía ninguno, descuenta `1` por defecto. Mínimo 1.
- **Request Body (Opcional):**
  ```json
  {
    "cantidad": 1
  }
  ```
- **Respuestas:**
  - `200 OK`: `ArticuloDTO` con la cantidad actualizada (si llegó a 0, el artículo ya no existe pero se devuelve con `cantidad: 0`).
  - `400 Bad Request`: Si la cantidad a restar es mayor a la cantidad actual disponible (`CantidadInsuficienteException`) o si `cantidad < 1`.
  - `401 Unauthorized`: Token inválido.
  - `404 Not Found`: Artículo inexistente o perteneciente a otro hogar.
  - `404 Not Found`: Artículo inexistente.

#### `PATCH /api/inventario/{articuloId}`
- **Autenticación:** Requerida (`Bearer`).
- **Descripción:** Actualiza parcialmente los datos de un artículo del hogar. Solo se modifican los campos incluidos en el cuerpo.
- **Parámetros de ruta:** `articuloId` (UUID).
- **Request Body:**
  ```json
  {
    "nombre": "Leche Entera",
    "fechaVencimiento": "2026-12-25",
    "cantidad": 4,
    "categoria": "LACTEOS"
  }
  ```
  *Todos los campos son opcionales, pero al menos uno debe ser provisto y con valor válido:*
  - `nombre`: String opcional, máx 120 caracteres, no puede ser vacío ni espacios en blanco.
  - `fechaVencimiento`: String fecha (`YYYY-MM-DD`) opcional, hoy o futura (fecha pasada → 400). Al actualizar la fecha, cualquier alerta asociada se limpia automáticamente.
  - `cantidad`: Entero opcional, mínimo 0. Si se envía `0`, el artículo y sus alertas se eliminan, se responde `200 OK` con `cantidad: 0` y no se aplican los demás campos del cuerpo.
  - `categoria`: Enum `CategoriaEnum` opcional.
- **Respuestas:**
  - `200 OK`: `ArticuloDTO` actualizado.
  - `400 Bad Request`: Cuerpo sin cambios válidos, fecha de vencimiento anterior a hoy o validación fallida (nombre en blanco, cantidad negativa); todas son `SolicitudInvalidaException` o errores de validación con `$.error`.
  - `401 Unauthorized`: Token inválido.
  - `404 Not Found`: Artículo inexistente o perteneciente a otro hogar.
  - `404 Not Found`: Artículo no encontrado.

#### `DELETE /api/inventario/{articuloId}`
- **Autenticación:** Requerida (`Bearer`).
- **Descripción:** Elimina un artículo del inventario y cualquier alerta vinculada.
- **Parámetros de ruta:** `articuloId` (UUID).
- **Respuestas:**
  - `204 No Content`: Eliminado exitosamente.
  - `401 Unauthorized`: Token inválido.
  - `404 Not Found`: Artículo inexistente o perteneciente a otro hogar.
  - `404 Not Found`: Artículo no encontrado.

---

### 6.5. Módulo de Alertas (`/api/alertas`)

#### `GET /api/alertas`
- **Autenticación:** Requerida (`Bearer`).
- **Descripción:** Lista las alertas de vencimiento del hogar del usuario autenticado, ordenadas por fecha de creación descendente.
- **Ciclo de vida:** mientras exista un artículo con `cantidad > 0` cuya fecha de vencimiento esté dentro de los próximos 10 días (o ya haya pasado), existe su alerta. El monitor actualiza `diasRestantes` en cada ejecución; si el producto vence (`diasRestantes < 0`), el campo `vencido` es `true` y la alerta vuelve a `leida: false` aunque hubiera sido marcada como leída. La alerta desaparece al eliminar el artículo, al agotar su stock (`cantidad = 0`) o al mover la fecha a más de 10 días. La creación y edición de artículos sincronizan la alerta de inmediato, sin esperar al monitor. El monitor ya no consulta Spoonacular: las sugerencias se obtienen solo desde `GET /api/recetas/sugerencias`.
- **Query Params:**
  - `page`: Entero, default `0`.
  - `size`: Entero, default `50` (máx `100`).
  - `leida`: Booleano opcional. Si se envía (`true` o `false`), filtra las alertas por su estado de lectura; si se omite, devuelve todas.
- **Respuestas:**
  - `200 OK`:
    ```json
    [
      {
        "id": "8b5d0c73-4567-4890-abcd-112233445566",
        "articuloId": "1a2b3c4d-0000-0000-0000-000000000001",
        "nombreProducto": "Yogurt Natural",
        "diasRestantes": 2,
        "leida": false,
        "fechaCreacion": "2026-11-15",
        "vencido": false
      }
    ]
    ```
  - `401 Unauthorized`: Token inválido.
  - `403 Forbidden`: Usuario sin hogar.

   - `diasRestantes`: entero; negativo indica que la fecha de vencimiento ya pasó (por ejemplo, `-2` = venció hace 2 días). `vencido` es `true` exactamente cuando `diasRestantes < 0`.

#### `GET /api/alertas/hogar/{hogarId}`
- **Autenticación:** Requerida (`Bearer`).
- **Descripción:** Lista las alertas de un hogar específico por ID. El usuario debe pertenecer al hogar.
- **Parámetros de ruta:** `hogarId` (UUID).
- **Query Params:** `page` (default 0), `size` (default 50, máx 100).
- **Respuestas:**
  - `200 OK`: Lista paginada de `AlertaResponse`.
  - `401 Unauthorized`: Token inválido.
  - `403 Forbidden`: El usuario no pertenece al hogar.

#### `PATCH /api/alertas/{id}/leida`
- **Autenticación:** Requerida (`Bearer`).
- **Descripción:** Marca una alerta específica como leída (`leida: true`).
- **Parámetros de ruta:** `id` (UUID de la alerta).
- **Respuestas:**
  - `200 OK`: `AlertaResponse` con `leida: true`.
  - `401 Unauthorized`: Token inválido.
  - `404 Not Found`: Alerta inexistente o perteneciente a otro hogar.
  - `404 Not Found`: Alerta no encontrada.

#### `GET /api/alertas/no-leidas/contador`
- **Autenticación:** Requerida (`Bearer`).
- **Descripción:** Devuelve el número de alertas no leídas del hogar del usuario autenticado. No incluye alertas de otros hogares.
- **Respuestas:**
  - `200 OK`:
    ```json
    {"noLeidas": 3}
    ```
  - `401 Unauthorized`: Token inválido.
  - `403 Forbidden`: Usuario sin hogar.

#### `POST /api/alertas/marcar-todas-leidas`
- **Autenticación:** Requerida (`Bearer`).
- **Descripción:** Marca como leídas todas las alertas no leídas del hogar del usuario autenticado en una sola actualización. Las alertas de otros hogares no se modifican.
- **Respuestas:**
  - `200 OK`:
    ```json
    {"actualizadas": 3}
    ```
    El número es cuántas alertas cambiaron de `leida: false` a `leida: true`.
  - `401 Unauthorized`: Token inválido.
  - `403 Forbidden`: Usuario sin hogar.

---

### 6.6. Módulo de Recetas (`/api/recetas`)

#### `GET /api/recetas/sugerencias`
- **Autenticación:** Requerida (`Bearer`).
- **Descripción:** Obtiene sugerencias de recetas basadas en los ingredientes del inventario del hogar que están más próximos a caducar (toma hasta un máximo de 3 artículos). Utiliza la API de Spoonacular con caché en memoria de 6 horas.
- **Respuestas:**
  - `200 OK`:
    ```json
    [
      {
        "id": 716429,
        "title": "Pasta with Garlic, Scallions, Cauliflower & Breadcrumbs",
        "image": "https://img.spoonacular.com/recipes/716429-312x231.jpg",
        "missedIngredientCount": 1
      }
    ]
    ```
  - `400 Bad Request`: Si el inventario del hogar está vacío:
    ```json
    {
      "error": "El inventario del hogar está vacío"
    }
    ```
  - `401 Unauthorized`: Token inválido.
  - `403 Forbidden`: Usuario sin hogar.

---

### 6.7. Módulo de Monitoreo (`/actuator`)

#### `GET /actuator/health`
- **Autenticación:** No requerida.
- **Descripción:** Health check del backend para comprobar que el servicio y la base de datos se encuentran operativos.
- **Respuestas:**
  - `200 OK`:
    ```json
    {
      "status": "UP"
    }
    ```

---

## 7. Recomendaciones Técnicas para el Cliente Android Nativo

### 7.1. Almacenamiento Seguro del Token
- Almacenar el JWT emitido por `/api/auth/login` o `/api/auth/registro` con Jetpack Security (`EncryptedSharedPreferences`), cuyas claves viven en el Android Keystore.
- **Nunca almacenar el JWT en `SharedPreferences` sin cifrar**, ni en archivos o logs: queda expuesto en dispositivos con acceso root o backups.

### 7.2. Interceptor HTTP Centralizado (OkHttp)
Se recomienda un único cliente Retrofit/OkHttp con un interceptor de petición que añada la cabecera a todas las llamadas:
  `Authorization: Bearer <token>`
- **Interceptor de respuesta:**
  - Si el código es `401`: borrar el token cifrado, limpiar el estado de los ViewModels y navegar a Login.
  - Si el código es `403`: **no** cerrar la sesión; mostrar el error (p. ej. contraseña incorrecta en una operación sensible).
  - Si el código es `429`: mostrar "demasiados intentos" y esperar antes de reintentar.
  - Si el código es `400` y el JSON contiene `"campos"`: mapear las claves a los errores de los `TextField` correspondientes.
   - Leer siempre el campo `codigo` (catálogo en §3.4), nunca el texto de `error`: `NO_AUTENTICADO` cierra la sesión; `CREDENCIALES_INVALIDAS` se muestra en Login; `CONFLICTO` permite reintentar la operación; `VALIDACION` rellena los `TextField` desde `campos`.

### 7.3. Manejo de Código de Invitación de Hogar
- El código de hogar generado por el backend tiene exactamente 10 caracteres alfanuméricos.
- En la UI con Jetpack Compose, usar un `TextField`/`OutlinedTextField` con:
  - `KeyboardOptions(capitalization = KeyboardCapitalization.Characters)`
  - filtro de caracteres alfanuméricos y `maxLines = 1` con límite de 10 caracteres.
  - Al enviar la petición, aplicar siempre `.trim().uppercase()`.

### 7.4. Debouncing y Prevención de Doble Envío
- Para evitar disparar el limitador de tasa de fuerza bruta (429) por pulsaciones repetidas o accidentales:
  - Deshabilitar los botones de "Iniciar Sesión" y "Unirse al Hogar" justo después del primer toque mediante un estado `isLoading`.
  - Aplicar un debounce de al menos 500 ms en los eventos de submit de formularios.
