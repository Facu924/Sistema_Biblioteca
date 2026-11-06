# Referencia de la API

*Última actualización: octubre de 2026.*

Todas las rutas empiezan con `/api`. Salvo el login, **requieren sesión iniciada**. Las respuestas son JSON.

**Quién puede llamar a cada ruta:**

| Abreviatura | Significa |
|---|---|
| Público | Cualquiera, sin iniciar sesión |
| Todos | Cualquier usuario con sesión |
| Lector | `ROLE_LECTOR` |
| Biblio | `ROLE_BIBLIOTECARIA` |
| Personal | `ROLE_BIBLIOTECARIA` o `ROLE_ADMIN` (el Administrador solo lee) |
| Admin | `ROLE_ADMIN` |

**Códigos de respuesta comunes:** `200` correcto, `201` creado, `204` sin contenido, `400` datos inválidos (indica los campos), `401` sin sesión o credenciales inválidas, `403` sin permiso o usuario inactivo, `404` no encontrado, `409` regla de negocio violada o conflicto, `413` archivo demasiado grande y `423` cuenta bloqueada.

Los listados paginados reciben `pagina` (desde 0) y `tamanio` (máximo 100) y devuelven `contenido`, `pagina`, `tamanio`, `totalElementos` y `totalPaginas`.

## Sesión

| Método y ruta | Quién | Descripción |
|---|---|---|
| `POST /login` | Público | Formulario con `mail` y `clave`. `200`, `401`, `403` o `423`. |
| `POST /logout` | Todos | Cierra la sesión. |
| `GET /me` | Todos | Devuelve el correo y el rol de quien tiene la sesión. |
| `GET /publico/resumen` | Público | Cantidad de libros, ejemplares disponibles y libros digitales. Se muestra en la pantalla de ingreso. |

## Usuarios

| Método y ruta | Quién | Descripción |
|---|---|---|
| `POST /usuarios` | Admin | Alta de usuario, con persona y, si es lector, su perfil. |
| `GET /usuarios` | Admin | Listado. Filtros: `texto`, `rol`, `activo`, `bloqueado`. |
| `GET /usuarios/{id}` | Admin | Detalle. |
| `PUT /usuarios/{id}` | Admin | Edición de datos, rol, perfil y contraseña opcional. |
| `POST /usuarios/{id}/baja` | Admin | Baja lógica. |
| `POST /usuarios/{id}/reactivar` | Admin | Reactivación. |
| `POST /usuarios/{id}/desbloquear` | Admin | Desbloqueo. Con `?claveTemporal=true` blanquea la contraseña y la devuelve una sola vez. |
| `GET /perfil` | Todos | Datos propios. |
| `PUT /perfil/telefono` | Todos | Cambia el teléfono propio. |
| `PUT /perfil/contrasena` | Todos | Cambia la contraseña. Exige la actual. |
| `PUT /perfil/mail` | Todos | Cambia el correo. Exige la contraseña actual y cierra la sesión. |

Ejemplo de alta de un alumno:

```json
{
  "nombre": "Juan",
  "apellido": "Perez",
  "legajo": "A-1001",
  "mail": "juan@cent35.edu.ar",
  "contrasena": "UnaClaveDe8OMas",
  "tipoUsuario": "ROLE_LECTOR",
  "tipoLector": "ALUMNO",
  "perfilInstitucional": "Tecnicatura en Programacion"
}
```

`tipoLector` y `perfilInstitucional` solo se envían para `ROLE_LECTOR`. Un alumno necesita la carrera; un profesor se guarda como `DOCENTE`; el personal administrativo no lleva perfil.

## Catálogo

Autores (`/autores`), editoriales (`/editoriales`) y materias CDU (`/materias`) funcionan igual:

| Método y ruta | Quién | Descripción |
|---|---|---|
| `GET /autores` | Todos | Listado paginado. Filtro: `texto`. |
| `GET /autores/{id}` | Todos | Detalle. |
| `POST /autores` | Biblio | Alta. Cuerpo: `{"nombreAutor": "Borges, Jorge Luis"}`. |
| `PUT /autores/{id}` | Biblio | Modificación. |
| `DELETE /autores/{id}` | Biblio | Baja. `409` si tiene libros. |

`GET /materias` (el listado) también es público. Los cuerpos son `{"nombreEditorial": "..."}` para editoriales y `{"codigoCdu": "821.134", "nombreTema": "..."}` para materias.

## Libros y ejemplares

| Método y ruta | Quién | Descripción |
|---|---|---|
| `GET /libros` | Público | Búsqueda. Filtros: `texto`, `idAutor`, `idEditorial`, `idMateria`, `soloDigitales`. Cada libro informa `ejemplaresTotal`, `ejemplaresDisponibles` y `prestamoDomicilioHabilitado`. |
| `GET /libros/{id}` | Público | Detalle con stock. |
| `POST /libros` | Biblio | Alta. |
| `PUT /libros/{id}` | Biblio | Modificación. |
| `DELETE /libros/{id}` | Biblio | Baja. `409` si tiene ejemplares. |
| `GET /libros/{id}/ejemplares` | Personal | Ejemplares del libro. |
| `POST /libros/{id}/ejemplares` | Biblio | Alta de un ejemplar. Cuerpo: `numeroInventario`, `ubicacionEstante`. |
| `GET /ejemplares/inventario/{numero}` | Personal | Busca un ejemplar por el número del código de barras. |
| `PUT /ejemplares/{id}` | Biblio | Cambia inventario y ubicación. |
| `POST /ejemplares/{id}/estado` | Biblio | Cambia el estado: `DISPONIBLE`, `DANADO`, `EXTRAVIADO` o `BAJA_LOGICA`. Exige `observaciones` salvo para `DISPONIBLE`. |

Ejemplo de alta de un libro:

```json
{
  "isbn": "978-950-03-9748-3",
  "titulo": "Ficciones",
  "idAutor": 1,
  "idEditorial": 1,
  "idMateria": 1,
  "anioPublicacion": 1944,
  "numeroPaginas": 200,
  "edicion": "1ra",
  "resumen": "Cuentos."
}
```

### Material digital

| Método y ruta | Quién | Descripción |
|---|---|---|
| `POST /libros/{id}/archivo` | Biblio | Sube o reemplaza el PDF o EPUB. Formulario `multipart` con el campo `archivo`. |
| `GET /libros/{id}/archivo` | Todos | Entrega el archivo para verlo en el navegador. Con `?descargar=true` lo baja. |
| `DELETE /libros/{id}/archivo` | Biblio | Quita el archivo. |

## Circulación

| Método y ruta | Quién | Descripción |
|---|---|---|
| `POST /prestamos` | Biblio | Registra un préstamo. |
| `GET /prestamos` | Personal | Listado. Filtros: `estado` (`ACTIVO`, `VENCIDO`, `DEVUELTO`) y `texto`, que busca por nombre del lector, legajo, título, ISBN o número de inventario. |
| `GET /prestamos/resumen` | Personal | Cantidad de préstamos activos, devueltos y vencidos. |
| `GET /lectores?legajo=` | Personal | Ficha del lector con su estado: si hoy se le puede prestar y, si no, el motivo. Se usa en el mostrador antes de prestar. |
| `GET /prestamos/vencidos` | Personal | Préstamos en mora, con teléfono y correo del lector. |
| `GET /prestamos/inventario/{numero}` | Personal | Préstamo abierto de un ejemplar, con los días de atraso. |
| `GET /prestamos/mios` | Lector | Préstamos propios, activos, devueltos y vencidos. |
| `POST /devoluciones` | Biblio | Registra una devolución. |
| `GET /historial` | Personal | Devoluciones cerradas. Filtros: `legajo` e `inventario`. |

Registrar un préstamo (el lector se identifica por legajo y el ejemplar por el número de inventario):

```json
{ "legajo": "A-1001", "numeroInventario": "INV-0001", "tipoPrestamo": "DOMICILIO" }
```

`tipoPrestamo` es `AULA`, `SALA` o `DOMICILIO`.

Registrar una devolución:

```json
{
  "numeroInventario": "INV-0001",
  "estadoEntrega": "DEVUELTO_OK",
  "observaciones": "",
  "diasPenalizacion": null
}
```

- `estadoEntrega`: `DEVUELTO_OK`, `DEVUELTO_CON_DANO` o `PERDIDO`. Los dos últimos exigen `observaciones`.
- `diasPenalizacion` es opcional. Sin valor: un día por cada día de atraso, y sin fecha de fin si hay daño o pérdida. Con `0` no se penaliza. Con un número, esa cantidad de días.
- La respuesta incluye los días de atraso, la penalización creada (si hubo) y `reservasPendientesDelLibro`.

## Penalizaciones

| Método y ruta | Quién | Descripción |
|---|---|---|
| `GET /penalizaciones` | Personal | Listado. Filtros: `legajo` y `soloVigentes`. |
| `GET /penalizaciones/mias` | Lector | Sanciones propias. |
| `POST /penalizaciones` | Biblio | Sanción manual. Cuerpo: `legajo`, `motivo` y `dias` (opcional; sin él queda sin fecha de fin). |
| `POST /penalizaciones/{id}/levantar` | Biblio | Levanta la sanción. |

## Reservas

| Método y ruta | Quién | Descripción |
|---|---|---|
| `POST /reservas` | Lector | Reserva un libro. Cuerpo: `{"idLibro": 1}`. |
| `GET /reservas` | Personal | Listado general, en orden de llegada. Filtro: `estado` (`PENDIENTE`, `COMPLETADA`, `CANCELADA`). |
| `GET /reservas/mias` | Lector | Reservas propias, con la posición en la cola. |
| `GET /reservas/libro/{idLibro}` | Personal | Cola de espera del libro, en orden de atención. |
| `POST /reservas/{id}/cancelar` | Lector o Biblio | Cancela una reserva pendiente. El lector solo las propias. |

## Panel principal

| Método y ruta | Quién | Descripción |
|---|---|---|
| `GET /dashboard` | Personal | Indicadores y datos de los gráficos: ejemplares y libros, préstamos abiertos, de hoy y vencidos, tasa de devolución a tiempo, lectores morosos, cuentas bloqueadas, reservas pendientes, penalizaciones vigentes, préstamos y devoluciones de los últimos 6 meses, los 5 libros más prestados de los últimos 30 días, títulos por categoría y los últimos 8 movimientos. |
