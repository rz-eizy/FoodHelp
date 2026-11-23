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

Un `403 Forbidden` **no** equivale a un 401: no se debe cerrar la sesión (p. ej. una contraseña incorrecta en una operación sensible devuelve 403). Un `429 Too Many Requests` se muestra como "demasiados intentos", sin borrar la sesión.

---

## 3. Esquema Estándar de Errores

Todos los errores producidos en la aplicación siguen una estructura JSON homogénea.

### 3.1. Error General
```json
{
  "error": "Descripción legible en español del problema"
}
```

### 3.2. Error de Validación de Campos (HTTP 400)
Producido cuando los campos del cuerpo no cumplen las validaciones Jakarta Validation (`@field:NotBlank`, `@field:Size`, etc.):
```json
{
  "error": "Datos de entrada inválidos",
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
| **204 No Content** | Sin contenido | Operación completada sin cuerpo de retorno (logout, eliminar artículo, salir del hogar). |
| **400 Bad Request** | Solicitud errónea | JSON malformado, validación de campos fallida, cantidad menor a 1, intentar actualizar artículo sin campos válidos, o inventario vacío al pedir recetas. |
| **401 Unauthorized** | No autenticado | Token ausente, expirado, inválido, claim `tv` desactualizado, o credenciales erróneas en login. |
| **403 Forbidden** | Prohibido | Usuario sin hogar asignado o peticiones a rutas con `{hogarId}` de un hogar al que no pertenece. |
| **404 Not Found** | No encontrado | Recurso inexistente o ajeno: artículo/alerta de otro hogar y `GET /api/hogares/{codigo}` con código inexistente o ajeno (respuesta uniforme por privacidad). |
| **409 Conflict** | Conflicto | Correo ya registrado, usuario ya pertenece a un hogar, o usuario intenta salir sin pertenecer a un hogar. |
| **429 Too Many Requests** | Límite excedido | Bloqueo por fuerza bruta (más de 10 intentos fallidos en login o unirse a hogar). |
| **500 Internal Server Error** | Error del servidor | Error inesperado en el servidor backend. |

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

---

### 6.3. Módulo de Hogares (`/api/hogares`)

#### `POST /api/hogares`
- **Autenticación:** Requerida (`Bearer`).
- **Descripción:** Crea un nuevo hogar asignando al usuario creador como miembro. El usuario no debe pertenecer a otro hogar previamente.
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
      "usuarios": [
        {
          "id": "c1f7b82e-9d21-4f76-88a1-5f053e1ad944",
          "nombre": "Ana Gómez"
        }
      ]
    }
    ```
  - `400 Bad Request`: Nombre vacío o mayor a 100 caracteres.
  - `401 Unauthorized`: Token ausente o inválido.
  - `409 Conflict`: El usuario ya pertenece a un hogar (`UsuarioYaEnHogarException`).

#### `POST /api/hogares/unirse`
- **Autenticación:** Requerida (`Bearer`).
- **Descripción:** Une al usuario a un hogar existente mediante su código de invitación (10 caracteres). Sujeto a rate limiting (10 fallos / 10 min).
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
      "usuarios": [
        {
          "id": "c1f7b82e-9d21-4f76-88a1-5f053e1ad944",
          "nombre": "Ana Gómez"
        },
        {
          "id": "9f8432a1-55bb-4770-bc10-1845bbcd4123",
          "nombre": "Carlos López"
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
- **Descripción:** Desvincula al usuario autenticado de su hogar actual. Si el usuario era el último integrante restante, el hogar y su inventario se eliminan automáticamente.
- **Respuestas:**
  - `204 No Content`: Salida exitosa.
  - `401 Unauthorized`: Token inválido.
  - `409 Conflict`: El usuario no pertenece a ningún hogar (`UsuarioSinHogarException`).

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
- **Query Params:**
  - `page`: Entero, default `0`.
  - `size`: Entero, default `50` (máx `100`).
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
        "fechaCreacion": "2026-11-15"
      }
    ]
    ```
  - `401 Unauthorized`: Token inválido.
  - `403 Forbidden`: Usuario sin hogar.

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
