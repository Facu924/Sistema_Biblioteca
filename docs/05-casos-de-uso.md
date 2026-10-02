### Módulo 1: Autenticación y Gestión de Usuarios

#### HU-01: Alta de Usuario

**Como** Administrador,

**Quiero** registrar nuevos usuarios en el sistema especificando su rol de acceso (ADMIN, BIBLIOTECARIO, USUARIO_GENERAL) y su perfil institucional (carrera o categoría "DOCENTE"),

**Para que** cada miembro de la institución tenga las credenciales y permisos correctos según su función.

*Criterios de aceptación:* La contraseña debe guardarse cifrada con BCrypt. Si es alumno, se le asigna su carrera; si es profesor, la categoría "DOCENTE"; si es personal administrativo, queda libre.

#### HU-02: Baja Lógica de Usuario

**Como** Administrador,

**Quiero** inactivar o dar de baja de forma lógica a un usuario,

**Para que** pierda el acceso al sistema sin perder la integridad histórica de sus préstamos pasados.

*Criterios de aceptación:* La baja debe ser lógica (cambio de estado activo/inactivo), manteniendo intactas las relaciones con los préstamos anteriores.

#### HU-03: Desbloqueo y Blanqueo de Cuentas

**Como** Administrador,

**Quiero** restablecer el estado de aquellas cuentas bloqueadas por seguridad y blanquear sus credenciales,

**Para que** el usuario pueda recuperar el acceso al sistema de forma segura.

*Criterios de aceptación:* El sistema debe permitir limpiar el contador de intentos fallidos, reactivar el acceso y opcionalmente generar una nueva contraseña temporal.

### Módulo 2: Gestión Operativa de Préstamos

#### HU-04: Registro de Préstamo Rápido (Lector de Códigos de Barras)

**Como** Bibliotecaria,

**Quiero** escanear el número de inventario de un ejemplar físico y asociarlo al usuario mediante su legajo,

**Para que** el préstamo presencial quede registrado en segundos sin necesidad de usar el mouse.

*Criterios de aceptación:* El sistema debe calcular automáticamente la fecha de devolución según el tipo de préstamo (Aula, Sala o Domicilio).

#### HU-05: Validación de Penalizaciones y Bloqueos en Préstamos

**Como** Bibliotecaria,

**Quiero** que el sistema verifique de forma obligatoria que el usuario no posea penalizaciones vigentes ni cuentas bloqueadas antes de confirmar un préstamo,

**Para que** no se entregue material bibliográfico a usuarios inhabilitados.

*Criterios de aceptación:* Si el usuario tiene mora, penalización o bloqueo, el sistema debe denegar la transacción y mostrar una advertencia en pantalla. También se deniega si el lector tiene un préstamo vencido sin devolver.

#### HU-06: Procesamiento de Devoluciones y Cambios de Estado

**Como** Bibliotecaria,

**Quiero** registrar la devolución de un material, actualizar el stock y cambiar el estado del ejemplar a Extraviado o Dañado si corresponde,

**Para que** el inventario refleje la realidad física de la biblioteca y se gestione la reposición.

*Criterios de aceptación:* La devolución debe impactar de forma inmediata en el stock disponible y permitir registrar observaciones físicas del ejemplar devuelto.

### Módulo 3: Autogestión y Consulta

#### HU-07: Búsqueda y Consulta en el Catálogo

**Como** Alumno o Profesor,

**Quiero** buscar libros y tesis en el catálogo de forma insensible a mayúsculas, minúsculas y acentos,

**Para que** pueda encontrar rápidamente el material de estudio sin importar cómo escriba el término.

*Criterios de aceptación:* El motor de búsqueda debe retornar resultados precisos ignorando diferencias de acentuación y capitalización.

#### HU-08: Visualización y Descarga de Material Digital

**Como** Alumno o Profesor,

**Quiero** buscar material digitalizado o tesis y poder abrirlas directamente en el navegador (para visualizarlas en las tablets de la institución) o descargarlas,

**Para que** pueda acceder al contenido académico sin restricciones de stock físico.

*Criterios de aceptación:* Los archivos en formato PDF o EPUB deben renderizarse correctamente en el navegador o permitir su descarga directa.

#### HU-09: Solicitud de Reserva y Préstamo Condicionada

**Como** Alumno o Profesor,

**Quiero** solicitar la reserva de un material bajo colas de espera o pedir préstamos, sujeto a que mi cuenta no esté penalizada ni bloqueada,

**Para que** pueda gestionar mis solicitudes cumpliendo con las normativas de la biblioteca.

*Criterios de aceptación:* El sistema debe validar el estado del usuario antes de registrar la solicitud y rechazarla en caso de registrar sanciones vigentes.

#### HU-10: Consulta de Historial Personal

**Como** Alumno o Profesor,

**Quiero** ver el registro de mis préstamos activos, devueltos y vencidos,

**Para que** pueda hacer un seguimiento personal de los plazos de mis materiales retirados.

*Criterios de aceptación:* El usuario solo debe visualizar sus propios registros, bloqueando totalmente el acceso a los datos de otros perfiles.

### Módulo 4: Procesos Generales y de Sistema (Categoría General)

#### HU-11: Bloqueo de Cuenta por Intentos Fallidos

**Como** sistema,

**Quiero** registrar automáticamente los intentos fallidos de inicio de sesión de cualquier usuario y bloquear temporalmente el acceso tras superar el límite establecido,

**Para que** el sistema esté protegido contra accesos no autorizados o ataques de fuerza bruta.

*Criterios de aceptación:* Al tercer o quinto intento fallido (según configuración), la cuenta debe bloquearse de forma automática e impedir nuevas autenticaciones hasta la intervención del administrador.

#### HU-12: Validación de Stock Mínimo

**Como** sistema,

**Quiero** bloquear el préstamo a "Domicilio" si el libro físico seleccionado tiene un único ejemplar disponible,

**Para que** siempre se garantice al menos un ejemplar físico dentro de la biblioteca para consulta en sala.

*Criterios de aceptación:* Si el stock disponible es igual a 1, la opción de préstamo domiciliario se deshabilitará automáticamente, permitiendo únicamente el uso interno. Si se intenta igual, el sistema rechaza el préstamo con un mensaje claro, y el catálogo informa `prestamoDomicilioHabilitado: false` para que el cliente oculte la opción.

#### HU-13: Control de Límite de Préstamos

**Como** sistema,

**Quiero** verificar que el usuario no supere los 2 materiales bibliográficos físicos en préstamo simultáneo,

**Para que** se cumplan las reglas de préstamo de la institución antes de confirmar la transacción.

*Criterios de aceptación:* Si el usuario ya cuenta con 2 préstamos activos, el sistema impedirá registrar un nuevo préstamo físico de manera automática.

### Módulo 5: Catálogo, Inventario y Sanciones (incorporado durante el desarrollo)

#### HU-14: Gestión del Catálogo

**Como** Bibliotecaria,

**Quiero** dar de alta, modificar y eliminar autores, editoriales, materias CDU y libros, y buscar libros por título, autor, editorial o ISBN,

**Para que** el catálogo esté siempre actualizado y los lectores encuentren el material.

*Criterios de aceptación:* No se pueden repetir autores, editoriales ni códigos CDU. Un autor, editorial o materia con libros asociados no se puede eliminar. El ISBN es opcional y, si se informa, no puede repetirse. Cada libro informa cuántos ejemplares tiene y cuántos están disponibles.

#### HU-15: Gestión de Ejemplares y Estados

**Como** Bibliotecaria,

**Quiero** registrar los ejemplares físicos de un libro con su número de inventario, buscarlos con el lector de códigos de barras y cambiar su estado,

**Para que** el inventario refleje lo que hay en la estantería.

*Criterios de aceptación:* El número de inventario es único. El estado puede pasar a `DISPONIBLE`, `DANADO`, `EXTRAVIADO` o `BAJA_LOGICA` (expurgo), con motivo obligatorio salvo para volver a `DISPONIBLE`. El estado `PRESTADO` lo asigna solo el sistema, y un ejemplar prestado no puede cambiar de estado hasta devolverse. Un libro con ejemplares no se puede eliminar.

#### HU-16: Gestión de Material Digital

**Como** Bibliotecaria,

**Quiero** subir, reemplazar y quitar el archivo PDF o EPUB de un libro o tesis,

**Para que** alumnos y profesores accedan al contenido digital.

*Criterios de aceptación:* Solo se aceptan PDF y EPUB válidos, verificados por su contenido y no solo por la extensión, de hasta 50 MB. El archivo se guarda en el servidor con un nombre generado por el sistema, y en la base queda solo ese nombre. Cualquier usuario autenticado puede visualizarlo en línea o descargarlo.

#### HU-17: Penalización Flexible en la Devolución

**Como** Bibliotecaria,

**Quiero** que el sistema me sugiera la penalización al devolver un material con atraso, daño o pérdida, y poder aplicarla, ajustarla o anularla,

**Para que** las sanciones se traten según cada caso, como se hace en la práctica.

*Criterios de aceptación:* Ante un atraso, la sugerencia es un día de penalización por cada día de atraso. La bibliotecaria puede indicar otra cantidad de días o cero para no penalizar. Ante un daño o pérdida, la penalización queda vigente hasta que se levante, salvo que se indique una cantidad de días. Una penalización de N días impide nuevos préstamos hasta N días después de la devolución, inclusive. Es obligatorio indicar observaciones si hubo daño o pérdida.

#### HU-18: Gestión Manual de Penalizaciones

**Como** Bibliotecaria,

**Quiero** cargar una sanción a un lector por su legajo y levantar cualquier sanción vigente,

**Para que** pueda resolver situaciones que no pasan por una devolución.

*Criterios de aceptación:* Una sanción manual puede tener duración en días o quedar sin fecha de fin. Al levantarla, se registra la fecha. Ambas acciones quedan auditadas.

#### HU-19: Consulta de Mora con Datos de Contacto

**Como** Bibliotecaria,

**Quiero** ver los préstamos vencidos junto con el teléfono y el correo de cada lector,

**Para que** pueda comunicarme con ellos, ya que el sistema no envía notificaciones automáticas.

*Criterios de aceptación:* El listado se ordena del vencimiento más antiguo al más reciente e informa los días de atraso. La mora se calcula por fecha, aunque la tarea diaria todavía no haya marcado el préstamo como vencido.

#### HU-20: Cola de Reservas y Cancelación

**Como** Bibliotecaria,

**Quiero** ver la cola de espera de un libro en el orden en que debe atenderse y cancelar reservas,

**Para que** el material devuelto llegue primero a quien lo pidió antes.

*Criterios de aceptación:* La cola se ordena por fecha de solicitud. Al devolver un material, el sistema informa cuántas reservas pendientes tiene el título. Al prestarle el ejemplar a quien lo reservó, la reserva queda completada. El lector solo puede cancelar sus propias reservas pendientes; la bibliotecaria puede cancelar cualquiera.

#### HU-21: Gestión del Perfil Propio

**Como** usuario del sistema,

**Quiero** cambiar mi teléfono, mi correo y mi contraseña,

**Para que** mis datos estén al día sin depender del Administrador.

*Criterios de aceptación:* Cambiar el correo o la contraseña exige ingresar la contraseña actual. Al cambiar el correo se cierra la sesión y hay que volver a ingresar con el nuevo. Ambos cambios quedan auditados.

#### HU-22: Consulta de Penalizaciones y Reservas Propias

**Como** Alumno o Profesor,

**Quiero** ver mis penalizaciones y mis reservas, con mi posición en la cola de espera,

**Para que** sepa por qué no puedo retirar material y cuándo me toca.

*Criterios de aceptación:* El lector solo ve sus propios registros. Cada penalización indica si sigue vigente y hasta cuándo.

#### HU-23: Auditoría de Eventos e Historial

**Como** Administrador o Bibliotecaria,

**Quiero** consultar el historial de devoluciones, las penalizaciones y los préstamos, filtrando por legajo,

**Para que** pueda auditar la actividad de la biblioteca.

*Criterios de aceptación:* El Administrador accede en modo de solo lectura. Los eventos críticos quedan registrados en la tabla de auditoría con el usuario, la acción, el detalle y la IP.

#### HU-24: Listado y Edición de Usuarios

**Como** Administrador,

**Quiero** listar usuarios con filtros y editar los datos, el rol, el perfil y la contraseña de cualquiera,

**Para que** pueda mantener las cuentas de la institución.

*Criterios de aceptación:* El listado filtra por texto (nombre, apellido, legajo o correo), rol, estado activo y estado de bloqueo, con paginación. El Administrador no puede darse de baja a sí mismo, cambiar su propio rol ni cambiar su propio correo por esta vía. No se puede cambiar el rol de un lector que ya tiene historial.
