# Decisiones de diseño

*Última actualización: octubre de 2026.*

Este documento registra las decisiones que se tomaron durante el desarrollo, con su motivo, para que quien continúe el proyecto entienda por qué el sistema se comporta como lo hace.

## Seguridad

**Sesión con cookie, no JWT.** Los clientes se servirán desde el mismo servidor que la API, por lo que la cookie de sesión funciona sin configuración extra y es lo más simple de desplegar. La autenticación es un formulario en `POST /api/login` con los campos `mail` y `clave`.

**Roles definitivos.** `ROLE_ADMIN`, `ROLE_BIBLIOTECARIA` y `ROLE_LECTOR`, con el prefijo que espera Spring Security. Los nombres funcionales de los documentos originales (`ADMIN`, `BIBLIOTECARIO`, `USUARIO_GENERAL`) equivalen a estos.

**El Administrador solo lee el catálogo y la circulación.** Las altas, bajas y modificaciones de libros, ejemplares, préstamos, devoluciones y penalizaciones son de la bibliotecaria. El Administrador gestiona usuarios y consulta todo lo demás (`RF-ADM-06`).

**Bloqueo de cuenta.** A los 5 intentos fallidos (configurable con `app.security.max-intentos-fallidos`) la cuenta se bloquea y **sigue bloqueada hasta que el Administrador la desbloquee**. Se eligió así porque el caso de uso `HU-11` lo pide, aunque `RF-GEN-02` hablaba de un bloqueo temporal. Un ingreso correcto pone el contador en cero. El login responde 401 (credenciales inválidas), 423 (cuenta bloqueada) o 403 (usuario dado de baja).

**Administrador inicial por variables de entorno.** Si no existe ningún administrador, se crea al arrancar con `ADMIN_MAIL` y `ADMIN_PASSWORD` (mínimo 8 caracteres). Si las variables faltan, no se crea nada y queda un aviso en la consola.

**Contraseñas de 8 a 72 caracteres.** BCrypt ignora lo que pasa de 72, así que se rechaza.

**CSRF desactivado.** Es lo que usaba el proyecto anterior y es habitual en APIs REST. Con sesión por cookie tiene un riesgo real si alguna vez los clientes se sirven desde otro sitio; en una red interna, con ambos clientes en el mismo servidor, el riesgo es bajo. Conviene revisarlo si cambia el despliegue.

**Correos en minúsculas.** Se normalizan al guardar. Las búsquedas por correo ignoran mayúsculas por la collation de la base.

## Préstamos

**Plazos.** Aula y Sala se devuelven el mismo día; Domicilio, a los 7 días corridos. El plazo (`app.prestamos.plazo-domicilio-dias`) y el límite de materiales (`app.prestamos.limite-por-lector`) se cambian por configuración.

**Verificaciones, en este orden:** usuario activo y sin bloqueo, sin penalizaciones vigentes, sin préstamos vencidos, límite de 2 materiales, ejemplar existente y disponible, y stock mínimo si es a domicilio.

**Stock mínimo.** Un préstamo a domicilio solo se permite si hay más de un ejemplar `DISPONIBLE`, de modo que siempre queda uno para consulta en sala. Los ejemplares dañados, extraviados o dados de baja no cuentan como disponibles.

**La mora se calcula por fecha.** Un préstamo abierto con fecha límite anterior a hoy bloquea nuevos préstamos al lector aunque la tarea diaria todavía no lo haya marcado como `VENCIDO`. Los préstamos vencidos siguen contando para el límite de 2.

**Un préstamo en Aula o Sala vence al terminar el día.** Si no se devuelve, desde el día siguiente cuenta como mora.

**Concurrencia.** El préstamo y la devolución bloquean la fila del libro y la del ejemplar (`SELECT ... FOR UPDATE`) y usan aislamiento `READ_COMMITTED`, para que dos operaciones simultáneas no rompan el stock mínimo ni presten o devuelvan dos veces el mismo ejemplar. Los bloqueos se escribieron en SQL directo porque la sintaxis que genera Hibernate (`FOR UPDATE OF`) no la acepta MariaDB, que es la base que trae XAMPP. No se bloquea al lector, por lo que dos préstamos exactamente simultáneos al mismo lector desde dos mostradores podrían superar el límite; con una sola bibliotecaria no es un riesgo real.

**El préstamo no se borra al devolver.** Pasa a `DEVUELTO` y conserva la fecha de devolución, y además se crea un registro en el historial con el estado de entrega y las observaciones. Así el lector ve todos sus préstamos (activos, devueltos y vencidos) con una sola consulta.

**El estado `PRESTADO` lo maneja el sistema.** La bibliotecaria no puede asignarlo ni modificarlo a mano.

**No hay renovaciones.** Los documentos originales no las mencionan.

## Penalizaciones

Se definieron con la bibliotecaria, que explicó que las sanciones no siempre se tratan igual.

- **Atraso:** el sistema sugiere **un día de penalización por cada día de atraso**, pero la bibliotecaria decide al devolver: aplicar lo sugerido, indicar otra cantidad de días o anularla con `0`.
- **Daño o pérdida:** la penalización queda vigente **hasta que la bibliotecaria la levante**, salvo que indique una cantidad de días. Si hay daño y atraso a la vez, se crea una sola penalización con ambos motivos.
- **Sanciones manuales:** se pueden cargar y levantar en cualquier momento.
- **Vigencia:** una penalización de N días impide préstamos hasta N días después de la devolución, inclusive. La tarea diaria desactiva las cumplidas.

## Reservas

- Solo se puede reservar si **no hay ejemplares disponibles** y **hay al menos uno prestado**. Si hay disponibles, el lector retira el material en el mostrador.
- No se puede repetir una reserva pendiente del mismo libro.
- Se atienden **por orden de llegada**. El sistema no impide prestar a otra persona mientras hay reservas pendientes: queda a criterio de la bibliotecaria, a quien la devolución le informa cuántas reservas esperan ese título.
- Al prestarle un ejemplar a quien lo reservó, la reserva pasa a `COMPLETADA`.
- El lector cancela sus propias reservas; la bibliotecaria puede cancelar cualquiera. No hay un tope de reservas por lector.

## Catálogo y archivos digitales

**Los archivos se guardan en el disco del servidor**, en `app.storage.path` (por defecto `./storage`), y la base conserva solo el nombre. Esa carpeta **hay que incluirla en los respaldos**.

**Los archivos se validan por su contenido.** Se acepta un PDF si empieza con la firma `%PDF-`, y un EPUB si es un archivo comprimido con extensión `.epub`. El nombre guardado lo genera el sistema, nunca el del cliente, para evitar rutas maliciosas. Límite: 50 MB.

**El ISBN se valida por formato** (10 o 13 dígitos, sin guiones), no por dígito de control, y es opcional.

**Un libro con ejemplares no se puede eliminar.** Los ejemplares se dan de baja lógica. Un autor, editorial o materia con libros tampoco se elimina.

**Búsqueda.** Se hace con `LIKE` sobre título, autor, editorial e ISBN. Ignora mayúsculas y acentos porque la base se crea con collation `utf8mb4_unicode_ci`.

## Datos y errores

- **Paginación:** máximo 100 resultados por página.
- **Códigos de error:** 400 datos inválidos (indica los campos), 401 sin sesión, 403 sin permiso, 404 no encontrado, 409 regla de negocio o conflicto, 413 archivo demasiado grande y 423 cuenta bloqueada.
- **Fechas:** se calculan con la hora del servidor, que tiene que estar en hora argentina.
- **Mantenimiento diario:** a las 00:05 y al arrancar la aplicación.

## Limitaciones conocidas

1. **Una baja no corta una sesión ya abierta.** Si dan de baja a un usuario que tiene la sesión iniciada, sigue entrando hasta que expire (`server.servlet.session.timeout`, 30 minutos sugeridos) o cierre sesión.
2. **El EPUB necesita un lector en el cliente.** Los navegadores muestran PDF por sí solos, pero no EPUB.
3. **Incrustar un PDF en una página propia** puede estar bloqueado por la cabecera de seguridad `X-Frame-Options` que Spring activa por defecto. Se ajusta cuando se construyan los clientes.
4. **La contraseña temporal** generada al desbloquear no obliga a cambiarla al primer ingreso, porque el modelo no tiene ese campo.
5. **Los mensajes de login** distinguen entre credenciales inválidas, cuenta bloqueada y usuario inactivo, lo que permite saber si una cuenta existe. Es aceptable en una red interna.
6. **No hay límite de reservas** por lector.
7. **No hay pruebas automatizadas** ni mediciones de rendimiento.
