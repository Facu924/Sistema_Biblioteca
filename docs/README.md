# Sistema de Gestión Bibliotecaria — CENT 35

Plataforma integral desarrollada para modernizar, automatizar y optimizar la gestión de la biblioteca del CENT 35. El sistema reemplaza los procesos manuales y planillas descentralizadas por una solución centralizada, robusta y escalable.

## Características Principales

- **Gestión Operativa y Préstamos:** Control de préstamos físicos (Aula, Sala y Domicilio) con validación obligatoria de stock mínimo, límites por usuario y verificación estricta de penalizaciones o bloqueos activos.
- **Seguridad y Control de Accesos:** Autenticación robusta y control de accesos basado en roles (**RBAC**) con Spring Security y encriptación **BCrypt**. Incluye control automático de intentos fallidos de inicio de sesión con bloqueo temporal de cuentas y un módulo de desbloqueo administrativo.
- **Integración de Hardware:** Optimizado para la lectura rápida mediante lectores de códigos de barras (emulación de teclado) basados en números de inventario y diseño adaptado para la visualización en las tablets institucionales.
- **Catálogo Mixto y Digital:** Administración de libros físicos (con proceso de importación masiva compatible con el estándar *Aguapey*), tesis académicas y recursos digitales (PDF/EPUB) descargables o visualizables directamente en el navegador.
- **Arquitectura Robusta:** Backend desarrollado en Java con Spring Boot estructurado en capas (Controladores, Servicios y Repositorios) utilizando transacciones ACID y manejo seguro de archivos pesados en el servidor.

## Stack Tecnológico

- **Backend:** Java, Spring Boot, Spring Data JPA / Hibernate
- **Seguridad:** Spring Security (RBAC, Hash BCrypt, manejo de variables de entorno)
- **Base de Datos:** MySQL (con *collation* `utf8mb4_unicode_ci` para búsquedas optimizadas insensibles a acentos y mayúsculas)
- **Infraestructura y Almacenamiento:** Sistema de archivos del servidor para recursos digitales (PDFs), manteniendo únicamente las rutas referenciales en la base de datos.