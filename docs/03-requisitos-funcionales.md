> **Nomenclatura de roles.** En el código y en la base de datos los roles se llaman `ROLE_ADMIN`, `ROLE_BIBLIOTECARIA` y `ROLE_LECTOR`. En este documento y en los casos de uso se usan los nombres funcionales, con esta equivalencia:
>
> | Nombre funcional | Rol en el sistema |
> |---|---|
> | `ADMIN` | `ROLE_ADMIN` |
> | `BIBLIOTECARIO` | `ROLE_BIBLIOTECARIA` |
> | `USUARIO_GENERAL` | `ROLE_LECTOR` |

#### GENERALES

- **RF-GEN-01 (Gestión de Perfil):** El sistema debe permitir a cualquier usuario autenticado modificar sus propios datos personales de registro (como contraseña o correo electrónico).
- **RF-GEN-02: Manejo de Seguridad y Bloqueo de Cuenta:** El sistema debe registrar automáticamente los intentos fallidos de inicio de sesión de cualquier usuario. Tras superar el límite configurado (ej. 3 o 5 intentos), el sistema debe bloquear el acceso a la cuenta por seguridad hasta que el Administrador la desbloquee (límite por defecto: 5 intentos, configurable).

#### Rol: Administrador (Módulo de Control Total)

- **RF-ADM-01 (Alta de Usuarios):** El sistema debe permitir al Administrador registrar nuevos usuarios definiendo su rol de acceso al sistema (`ADMIN`, `BIBLIOTECARIO`, `USUARIO_GENERAL`) y su perfil institucional (asignando una carrera específica para alumnos, o la categoría institucional **"DOCENTE"** para los profesores, dejando este campo libre para el personal administrativo). Los usuarios con rol `USUARIO_GENERAL` se registran además con su tipo de lector (`ALUMNO`, `PROFESOR` o `PERSONAL_ADMINISTRATIVO`).
- **RF-ADM-02 (Baja de Usuarios):** El sistema debe permitir al Administrador dar de baja o inactivar de forma lógica a cualquier usuario del sistema.
- **RF-ADM-03 (Visualización Global):** El sistema debe proveer una interfaz para listar, filtrar y observar a todos los usuarios registrados.
- **RF-ADM-04 (Modificación Externa):** El sistema debe facultar al Administrador para editar los datos de cualquier usuario (reajuste de roles, credenciales o perfiles).
- **RF-ADM-05 (Gestión de Desbloqueo de Cuentas):** El sistema debe permitir al Administrador blanquear credenciales y restablecer el estado de aquellas cuentas de usuario que hayan sido bloqueadas automáticamente por superar el límite de intentos fallidos de inicio de sesión.
- **RF-ADM-06 (Auditoría Total):** El Administrador debe tener acceso de lectura a todos los módulos de gestión (catálogo, préstamos e historiales) sin restricciones. El acceso es de **solo lectura**: el Administrador consulta catálogo, préstamos, mora, penalizaciones, reservas e historial, pero no los modifica.

#### Rol: Bibliotecaria/o (Módulo de Gestión Operativa)

- **RF-BIB-01 (Gestión del Catálogo y Migración):** El sistema debe permitir el CRUD de libros, autores y editoriales, incluyendo un proceso de importación masiva compatible con el estándar de Aguapey.
- **RF-BIB-02 (Búsqueda Optimizada):** El sistema debe permitir buscar y filtrar libros mediante un motor insensible a mayúsculas, minúsculas y acentos (*case-insensitive*), además de soportar la entrada rápida mediante lector de código de barras por número de inventario.
- **RF-BIB-03 (Registro de Préstamos y Validaciones):** El sistema debe permitir asentar un préstamo presencial asociando un usuario con un ejemplar físico, **verificando obligatoriamente que el usuario no posea penalizaciones vigentes ni cuentas bloqueadas**, calculando automáticamente la fecha de devolución según el tipo de préstamo: el mismo día para Aula y Sala, y 7 días corridos para Domicilio (configurable). También se deniega el préstamo si el usuario tiene un préstamo vencido sin devolver.
- **RF-BIB-04 (Restricción de Stock Mínimo):** El sistema debe impedir que el stock disponible para préstamo a domicilio de un libro físico llegue a cero, bloqueando el préstamo externo si solo queda un ejemplar (reservándolo para uso en sala). Se evalúa sobre los ejemplares en estado `DISPONIBLE`.
- **RF-BIB-05 (Control de Límite):** El sistema debe validar que un usuario no supere el límite máximo de 2 materiales bibliográficos físicos simultáneos. Cuentan los préstamos `ACTIVO` y `VENCIDO` de cualquier tipo.
- **RF-BIB-06 (Devoluciones y Bajas):** El sistema debe procesar el retorno de materiales actualizando stock, permitiendo marcar estados especiales (*Extraviado, Dañado*) y gestionar bajas lógicas por expurgo (donación o reciclaje). La devolución registra el estado de entrega (`DEVUELTO_OK`, `DEVUELTO_CON_DANO`, `PERDIDO`) y las observaciones, y puede generar una penalización (ver `RF-BIB-08`).
- **RF-BIB-07 (Auditoría y Contacto):** El sistema debe permitir auditar historiales y visualizar datos de contacto (teléfono y correo) para facilitar la comunicación ante moras o reposiciones. El listado de préstamos vencidos incluye el teléfono y el correo del lector.

#### Rol: Alumno / Profesor (Módulo de Autogestión y Consulta)

- **RF-ALU-01 (Catálogo y Material Digital):** El sistema debe permitir buscar en el catálogo (físico y digital), verificar stock en tiempo real, visualizar archivos PDF/EPUB directamente en el navegador (para uso en las 10 tablets de la institución) o descargarlos. El servidor entrega el archivo para visualización en línea o descarga; mostrar un EPUB en el navegador requiere un lector en la aplicación cliente.
- **RF-ALU-02 (Solicitud de Reserva y Préstamo):** El sistema debe permitir solicitar la reserva de un material bajo ciertas condiciones o colas de espera, **condicionando las solicitudes a que el usuario no se encuentre penalizado ni con su cuenta bloqueada**. Solo se acepta la reserva cuando no hay ejemplares disponibles y hay al menos uno prestado. Se atienden por orden de llegada y quedan completadas al prestarse el material al lector, quien puede cancelar sus reservas pendientes.
- **RF-ALU-03 (Historial Personal):** El sistema debe permitir consultar exclusivamente el registro de préstamos propios (activos, devueltos y vencidos).

---

### Requisitos incorporados durante el desarrollo

#### Rol: Bibliotecaria/o (ampliación)

- **RF-BIB-08 (Gestión de Penalizaciones):** El sistema debe sugerir, ante una devolución con atraso, un día de penalización por cada día de atraso, y permitir a la bibliotecaria aplicarla, ajustar la cantidad de días o anularla. La devolución con daño o pérdida genera una penalización vigente hasta que la bibliotecaria la levante o por la cantidad de días que ella indique. También debe poder cargar sanciones manuales y levantar cualquier sanción.
- **RF-BIB-09 (Gestión de Reservas):** El sistema debe permitir ver la cola de espera de un libro en el orden en que debe atenderse, y cancelar reservas pendientes. Al devolver un material, debe informar cuántas reservas pendientes tiene ese título.
- **RF-BIB-10 (Estados de Ejemplares):** El sistema debe permitir cambiar manualmente el estado de un ejemplar a `DISPONIBLE`, `DANADO`, `EXTRAVIADO` o `BAJA_LOGICA`, exigiendo un motivo salvo para volver a `DISPONIBLE`. El estado `PRESTADO` solo lo asigna el sistema al registrar un préstamo, y un ejemplar prestado no puede cambiar de estado hasta su devolución.

#### Rol: Alumno / Profesor (ampliación)

- **RF-ALU-04 (Consulta de Sanciones y Reservas Propias):** El sistema debe permitir al lector consultar sus penalizaciones y sus reservas, indicando para cada reserva pendiente su posición en la cola.

#### GENERALES (ampliación)

- **RF-GEN-03 (Auditoría de Eventos):** El sistema debe registrar los eventos críticos (altas, ediciones y bajas de usuarios, bloqueos y desbloqueos, cambios de correo o contraseña, bajas de libros, cambios de estado de ejemplares, creación y levantamiento de penalizaciones) con el usuario responsable, la fecha y hora y la IP de origen. Los eventos automáticos del sistema quedan sin usuario asociado.

#### Sistema

- **RF-SIS-01 (Mantenimiento Diario):** El sistema debe marcar como `VENCIDO` los préstamos activos con fecha límite anterior a la actual y desactivar las penalizaciones cuya fecha de fin ya pasó. Se ejecuta todos los días a las 00:05 y cada vez que la aplicación arranca.
