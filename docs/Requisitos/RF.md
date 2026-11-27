# Requisitos Funcionales (RF)

**RF-01: Crear Hogar.** El sistema debe permitir a un usuario registrado crear un entorno virtual ("Hogar") y generar un código de invitación.
**RF-02: Unirse a Hogar.** El sistema debe permitir a un usuario unirse a un "Hogar" existente ingresando un código de invitación válido.
**RF-03: Sincronización Concurrente.** El sistema debe reflejar los cambios realizados en el inventario por un miembro del hogar al resto de los miembros en tiempo real (o al refrescar la vista).
**RF-04: Visualizar Inventario.** El sistema debe listar los productos del hogar, mediante filtros generador por sistema (fecha, categoría, etc).
**RF-05: Ingreso Rápido de Producto.** El sistema debe permitir ingresar un producto indicando obligatoriamente: Nombre, Cantidad y Fecha de Caducidad.
**RF-06: Edición y Eliminación.** El sistema debe permitir modificar la cantidad o eliminar un producto existente del inventario.
**RF-07: Autocompletado Básico.** El sistema debe sugerir nombres de productos basados en ingresos anteriores del mismo hogar para agilizar la entrada de datos.
**RF-08: Detección Diaria.** El servidor debe evaluar diariamente el inventario de todos los hogares para identificar productos cuya fecha de caducidad esté dentro de un rango de advertencia (ej. 10 días).
**RF-09: Generación de Alertas.** El sistema debe generar alertas internas cuando un producto es marcado como "próximo a caducar".
**RF-10: Sugerencia por Caducidad.** El sistema debe presentar opciones de recetas que incluyen, como ingrediente principal, los productos que el motor de monitoreo identificó como próximos a caducar.
**RF-11: Consulta Externa (Backend).** El sistema(Spring boot) debe consultar una API externa de recetas utilizando los ingredientes críticos, y retornar las coincidencias.
**RF-12: Visualización de Receta.** El sistema debe mostrar el título, imagen, ingredientes y pasos básicos de una receta.
