# Sistema Contable

Sistema de gestión contable empresarial desarrollado con Spring Boot y PostgreSQL, diseñado para el registro, administración y análisis de operaciones contables según estándares NIIF.

## Características Principales

- **Libro Diario y Mayor**: Registro completo de partidas contables
- **Balance General**: Estado de situación financiera en tiempo real
- **Estado de Resultados**: Análisis de ingresos, costos y gastos
- **Flujos de Efectivo**: Método indirecto según NIIF
- **Cambios en el Patrimonio**: Tracking de movimientos de capital
- **Balance de Comprobación**: Verificación de saldos
- **Multi-usuario**: Sistema de roles (Admin, Contador, Auditor)
- **Períodos Contables**: Gestión por períodos (mensual, trimestral, anual)
- **Seguridad**: Autenticación JWT y control de acceso basado en roles

## Inicio Rápido (Producción)

### Requisitos Previos

- Docker Desktop (Windows/Mac) o Docker Engine (Linux)
- Al menos 2GB de RAM disponible
- Puertos 8080 y 5432 libres

### Despliegue en 1 Paso

```powershell
# Windows PowerShell
.\deploy.ps1
```

```bash
# Linux/Mac o manual
docker-compose build
docker-compose up -d
```

La aplicación estará disponible en: `http://localhost:8080`

### Ver logs en tiempo real

```bash
docker-compose logs -f java_app
```

## Desarrollo Local

Si prefieres trabajar en modo desarrollo con hot-reload:

```bash
# Usar el compose de desarrollo
docker-compose -f docker-compose-dev.yml up

# La app correrá en el puerto 8080
# PostgreSQL en el puerto 5433
# Tailwind CSS con watch mode activo
```

## Usuarios por Defecto

El sistema crea automáticamente tres usuarios al iniciar:

| Rol       | Usuario    | Contraseña    | Email               | Permisos                          |
|-----------|------------|---------------|---------------------|-----------------------------------|
| Admin     | admin      | admin123      | admin@sic.com       | Gestión total + usuarios          |
| Contador  | contador   | contador123   | contador@sic.com    | Registrar partidas + reportes     |
| Auditor   | auditor    | auditor123    | auditor@sic.com     | Solo lectura de reportes          |

**Importante:** Cambiar estas contraseñas en producción.

## Datos Iniciales

El sistema incluye datos de demostración:

- **Catálogo de Cuentas**: 30 cuentas (código 1.X a 7.X)
- **Períodos Contables**: Enero-Marzo 2025 + Q1 2025 + Año Fiscal 2025
- **Partidas de Ejemplo**: Operaciones de inicio, ventas, compras, pagos

## Arquitectura

### Stack Tecnológico

**Backend:**
- Spring Boot 3.4.0
- Spring Security + JWT
- Spring Data JPA + Hibernate
- PostgreSQL 12

**Frontend:**
- HTML5 + Thymeleaf
- Tailwind CSS + DaisyUI
- Vanilla JavaScript (ES6+)

**DevOps:**
- Docker Multi-stage builds
- Docker Compose
- Maven

### Estructura del Proyecto

```
SistemaContable/
├── backend/sic/
│   ├── src/main/
│   │   ├── java/com/ues/sic/
│   │   │   ├── auth/              # JWT + Security
│   │   │   ├── balances/          # Estados financieros
│   │   │   ├── catalogo/          # Plan de cuentas
│   │   │   ├── partidas/          # Libro diario
│   │   │   ├── periodos/          # Períodos contables
│   │   │   └── usuarios/          # Gestión de usuarios
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── data/              # Seeders JSON
│   │       ├── static/            # CSS, JS, imágenes
│   │       └── templates/         # Vistas HTML
│   └── pom.xml
├── Dockerfile                      # Multi-stage build
├── docker-compose.yml              # Producción
├── docker-compose-dev.yml          # Desarrollo
├── tailwind.config.js              # Config Tailwind
├── deploy.ps1                      # Script de deploy
├── DEPLOY.md                       # Guía de despliegue
└── README.md                       # Este archivo
```

## Configuración

### Variables de Entorno (docker-compose.yml)

```yaml
environment:
  - SPRING_DATASOURCE_URL=jdbc:postgresql://java_db:5432/sistema_contable
  - SPRING_DATASOURCE_USERNAME=postgres
  - SPRING_DATASOURCE_PASSWORD=postgres
  - SPRING_JPA_HIBERNATE_DDL_AUTO=update
  - SERVER_PORT=8080
```

### Desarrollo Local (application.properties)

```properties
server.port=8080
spring.datasource.url=jdbc:postgresql://localhost:5432/sistema_contable
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
```

## Documentación

- [Guía de Despliegue](DEPLOY.md) - Instrucciones detalladas de producción
- [Flujos de Efectivo](DOCUMENTACION_FLUJOS_EFECTIVO.md) - Documentación técnica EFE

## Testing

```bash
# Ejecutar tests
cd backend/sic
./mvnw test

# Con cobertura
./mvnw test jacoco:report
```

## Troubleshooting

### Puerto ya en uso

```bash
# Cambiar puerto en docker-compose.yml
ports:
  - "8081:8080"  # Usar 8081 en vez de 8080
```

### Reconstruir contenedores

```bash
docker-compose down -v
docker-compose build --no-cache
docker-compose up -d
```

### Ver logs de errores

```bash
docker-compose logs java_app | grep ERROR
```

## Comandos Útiles

```bash
# Detener servicios
docker-compose down

# Reiniciar un servicio
docker-compose restart java_app

# Ver estado
docker-compose ps

# Acceder a la base de datos
docker-compose exec java_db psql -U postgres -d sistema_contable

# Ver uso de recursos
docker stats
```

## Contribuir

1. Fork el proyecto
2. Crear rama feature (`git checkout -b feature/NuevaCaracteristica`)
3. Commit cambios (`git commit -m 'Add: nueva característica'`)
4. Push a la rama (`git push origin feature/NuevaCaracteristica`)
5. Abrir Pull Request

## Licencia

Proyecto académico - Universidad de El Salvador (UES)

---

**Desarrollado para Sistemas Contables**
