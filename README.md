# Sistema Contable

Sistema de gestión contable desarrollado con Spring Boot y PostgreSQL, diseñado para el registro y administración de operaciones contables.

## Requisitos Previos

- Java 17 o superior
- Docker y Docker Compose
- Maven 3.6 o superior
- Git

## Instalación y Configuración

### 1. Clonar el Repositorio

```bash
git clone <url-del-repositorio>
cd SistemaContable
```

### 2. Configurar la Base de Datos

Desde la raíz del proyecto, construir la imagen de Node.js:

```bash
docker build -f dockerfile.node -t node-img-test .
```

Iniciar los contenedores de Docker (PostgreSQL y otros servicios):

```bash
docker compose up --build
```

Esto iniciará una instancia de PostgreSQL en el puerto 5433 con las siguientes credenciales:

- **Host:** localhost
- **Puerto:** 5433
- **Base de datos:** sistema_contable
- **Usuario:** postgres
- **Contraseña:** postgres

### 3. Ejecutar la Aplicación

Ejecutar el proyecto Java desde la clase principal:

```
backend/sic/src/main/java/com/ues/sic/SicApplication.java
```

La aplicación estará disponible en: `http://localhost:8082`

## Usuarios por Defecto

El sistema crea automáticamente tres usuarios con diferentes roles al iniciar por primera vez:

| Rol       | Usuario    | Contraseña    | Email               |
|-----------|------------|---------------|---------------------|
| Admin     | admin      | admin123      | admin@sic.com       |
| Contador  | contador   | contador123   | contador@sic.com    |
| Auditor   | auditor    | auditor123    | auditor@sic.com     |

**Nota de Seguridad:** Se recomienda cambiar estas contraseñas en un entorno de producción.

## Datos Iniciales

Al iniciar la aplicación, el sistema carga automáticamente:

- **Catálogo de Cuentas:** 30 cuentas predefinidas (Activos, Pasivos, Capital Contable)
- **Periodos Contables:** 5 periodos de ejemplo para el año 2025

Estos datos se cargan desde los archivos JSON ubicados en `backend/sic/src/main/resources/data/`

## Estructura del Proyecto

```
SistemaContable/
├── backend/
│   └── sic/                    # Aplicación Spring Boot
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/       # Código fuente Java
│       │   │   └── resources/  # Archivos de configuración y datos
│       │   └── test/           # Pruebas unitarias
│       └── pom.xml             # Dependencias Maven
├── frontend/                   # Archivos del frontend
├── docker-compose.yml          # Configuración de servicios Docker
└── README.md
```

## Tecnologías Utilizadas

- **Backend:** Spring Boot 3.x, Spring Security, Spring Data JPA
- **Base de Datos:** PostgreSQL 15
- **Autenticación:** JWT (JSON Web Tokens)
- **Containerización:** Docker, Docker Compose
- **Build Tool:** Maven

## Configuración

La configuración principal se encuentra en:

```
backend/sic/src/main/resources/application.properties
```

Puerto del servidor: **8082**

## Contribuir

Para contribuir al proyecto, crear una rama desde `main` y enviar un Pull Request con los cambios propuestos.

## Licencia

Este proyecto es de uso académico.
