#### GENERALES

- **RF-GEN-01 (Gestión de Perfil):** El sistema debe permitir a cualquier usuario autenticado modificar sus propios datos personales de registro (como contraseña o correo electrónico).
- **RF-GEN-02: Manejo de Seguridad y Bloqueo de Cuenta:** El sistema debe registrar automáticamente los intentos fallidos de inicio de sesión de cualquier usuario. Tras superar el límite configurado (ej. 3 o 5 intentos), el sistema debe bloquear temporalmente el acceso a la cuenta por seguridad.

#### Rol: Administrador (Módulo de Control Total)

- **RF-ADM-01 (Alta de Usuarios):** El sistema debe permitir al Administrador registrar nuevos usuarios definiendo su rol de acceso al sistema (`ADMIN`, `BIBLIOTECARIO`, `USUARIO_GENERAL`) y su perfil institucional (asignando una carrera específica para alumnos, o la categoría institucional **"DOCENTE"** para los profesores, dejando este campo libre para el personal administrativo).
- **RF-ADM-02 (Baja de Usuarios):** El sistema debe permitir al Administrador dar de baja o inactivar de forma lógica a cualquier usuario del sistema.
- **RF-ADM-03 (Visualización Global):** El sistema debe proveer una interfaz para listar, filtrar y observar a todos los usuarios registrados.
- **RF-ADM-04 (Modificación Externa):** El sistema debe facultar al Administrador para editar los datos de cualquier usuario (reajuste de roles, credenciales o perfiles).
- **RF-ADM-05 (Gestión de Desbloqueo de Cuentas):** El sistema debe permitir al Administrador blanquear credenciales y restablecer el estado de aquellas cuentas de usuario que hayan sido bloqueadas automáticamente por superar el límite de intentos fallidos de inicio de sesión.
- **RF-ADM-06 (Auditoría Total):** El Administrador debe tener acceso de lectura a todos los módulos de gestión (catálogo, préstamos e historiales) sin restricciones.

#### Rol: Bibliotecaria/o (Módulo de Gestión Operativa)

- **RF-BIB-01 (Gestión del Catálogo y Migración):** El sistema debe permitir el CRUD de libros, autores y editoriales, incluyendo un proceso de importación masiva compatible con el estándar de Aguapey.
- **RF-BIB-02 (Búsqueda Optimizada):** El sistema debe permitir buscar y filtrar libros mediante un motor insensible a mayúsculas, minúsculas y acentos (*case-insensitive*), además de soportar la entrada rápida mediante lector de código de barras por número de inventario.
- **RF-BIB-03 (Registro de Préstamos y Validaciones):** El sistema debe permitir asentar un préstamo presencial asociando un usuario con un ejemplar físico, **verificando obligatoriamente que el usuario no posea penalizaciones vigentes ni cuentas bloqueadas**, calculando automáticamente la fecha de devolución según el tipo de préstamo (Aula, Sala o Domicilio).
- **RF-BIB-04 (Restricción de Stock Mínimo):** El sistema debe impedir que el stock disponible para préstamo a domicilio de un libro físico llegue a cero, bloqueando el préstamo externo si solo queda un ejemplar (reservándolo para uso en sala).
- **RF-BIB-05 (Control de Límite):** El sistema debe validar que un usuario no supere el límite máximo de 2 materiales bibliográficos físicos simultáneos.
- **RF-BIB-06 (Devoluciones y Bajas):** El sistema debe procesar el retorno de materiales actualizando stock, permitiendo marcar estados especiales (*Extraviado, Dañado*) y gestionar bajas lógicas por expurgo (donación o reciclaje).
- **RF-BIB-07 (Auditoría y Contacto):** El sistema debe permitir auditar historiales y visualizar datos de contacto (teléfono y correo) para facilitar la comunicación ante moras o reposiciones.

#### Rol: Alumno / Profesor (Módulo de Autogestión y Consulta)

- **RF-ALU-01 (Catálogo y Material Digital):** El sistema debe permitir buscar en el catálogo (físico y digital), verificar stock en tiempo real, visualizar archivos PDF/EPUB directamente en el navegador (para uso en las 10 tablets de la institución) o descargarlos.
- **RF-ALU-02 (Solicitud de Reserva y Préstamo):** El sistema debe permitir solicitar la reserva de un material bajo ciertas condiciones o colas de espera, **condicionando las solicitudes a que el usuario no se encuentre penalizado ni con su cuenta bloqueada**.
- **RF-ALU-03 (Historial Personal):** El sistema debe permitir consultar exclusivamente el registro de préstamos propios (activos, devueltos y vencidos).