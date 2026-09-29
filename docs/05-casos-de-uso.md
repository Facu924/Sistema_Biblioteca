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

*Criterios de aceptación:* Si el usuario tiene mora, penalización o bloqueo, el sistema debe denegar la transacción y mostrar una advertencia en pantalla.

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

*Criterios de aceptación:* Si el stock disponible es igual a 1, la opción de préstamo domiciliario se deshabilitará automáticamente, permitiendo únicamente el uso interno.

#### HU-13: Control de Límite de Préstamos

**Como** sistema,

**Quiero** verificar que el usuario no supere los 2 materiales bibliográficos físicos en préstamo simultáneo,

**Para que** se cumplan las reglas de préstamo de la institución antes de confirmar la transacción.

*Criterios de aceptación:* Si el usuario ya cuenta con 2 préstamos activos, el sistema impedirá registrar un nuevo préstamo físico de manera automática.