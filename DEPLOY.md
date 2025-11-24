# Despliegue en Producción - Sistema Contable

## Requisitos Previos

- Docker Desktop instalado y en ejecución
- Al menos 2GB de RAM disponible
- Puertos 8080 y 5432 disponibles

## Estructura de Docker

El proyecto utiliza un **Dockerfile multi-stage** que:

1. **Stage 1 (Tailwind Builder)**: Compila los estilos CSS con Tailwind y DaisyUI
2. **Stage 2 (Maven Builder)**: Compila el proyecto Spring Boot con Maven
3. **Stage 3 (Runtime)**: Imagen final optimizada con solo el JRE y el JAR

## Despliegue Automático

### Opción 1: Script PowerShell (Recomendado para Windows)

```powershell
.\deploy.ps1
```

Este script:
- Limpia contenedores anteriores
- Construye las imágenes desde cero
- Inicia los servicios
- Muestra el estado y logs

### Opción 2: Comandos Manuales

```bash
# Construir las imágenes
docker-compose build

# Iniciar los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f java_app
```

## Desarrollo vs Producción

### Desarrollo (docker-compose-dev.yml)
- Base de datos en puerto 5433 (no conflicta con instalaciones locales)
- Servicio Tailwind con hot-reload
- Volúmenes montados para desarrollo en vivo

```bash
docker-compose -f docker-compose-dev.yml up
```

### Producción (docker-compose.yml)
- Todo se compila dentro del contenedor
- Imágenes optimizadas
- Health checks configurados
- Restart automático

```bash
docker-compose up -d
```

## Acceso a la Aplicación

Una vez desplegado:

- **Aplicación Web**: http://localhost:8080
- **Login**: Usar las credenciales configuradas en el sistema
- **Base de Datos**: 
  - Host: localhost
  - Puerto: 5432
  - Database: sistema_contable
  - Usuario: postgres
  - Password: postgres

## Comandos Útiles

### Ver estado de contenedores
```bash
docker-compose ps
```

### Ver logs en tiempo real
```bash
docker-compose logs -f java_app
docker-compose logs -f java_db
```

### Reiniciar un servicio
```bash
docker-compose restart java_app
```

### Detener todo
```bash
docker-compose down
```

### Detener y eliminar volúmenes (¡Cuidado! Borra la BD)
```bash
docker-compose down -v
```

### Reconstruir después de cambios
```bash
docker-compose build --no-cache
docker-compose up -d
```

## Troubleshooting

### Error: Puerto ya en uso
```bash
# Verificar qué está usando el puerto 8080
netstat -ano | findstr :8080

# Cambiar el puerto en docker-compose.yml
ports:
  - "8081:8080"  # Mapear a puerto 8081 en el host
```

### Error: No se puede conectar a la base de datos
```bash
# Verificar que PostgreSQL esté saludable
docker-compose exec java_db pg_isready -U postgres

# Ver logs de la base de datos
docker-compose logs java_db
```

### Error de compilación
```bash
# Limpiar caché de Docker
docker system prune -a

# Reconstruir sin caché
docker-compose build --no-cache
```

## Archivos de Configuración

- `Dockerfile`: Construcción multi-stage de la aplicación
- `docker-compose.yml`: Orquestación de producción
- `docker-compose-dev.yml`: Configuración para desarrollo
- `.dockerignore`: Archivos excluidos del build
- `deploy.ps1`: Script automatizado de despliegue

## Variables de Entorno

Puedes personalizar las variables en `docker-compose.yml`:

```yaml
environment:
  - SPRING_DATASOURCE_URL=jdbc:postgresql://java_db:5432/sistema_contable
  - SPRING_DATASOURCE_USERNAME=postgres
  - SPRING_DATASOURCE_PASSWORD=postgres
  - SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

## Notas Importantes

1. **Primera ejecución**: Puede tardar 5-10 minutos en compilar todo
2. **Persistencia**: Los datos se guardan en el volumen `pgdata`
3. **Health checks**: La app tarda ~60s en estar lista después del inicio
4. **Hot reload**: No disponible en producción, requiere rebuild para cambios

## Checklist de Despliegue

- [ ] Docker Desktop está corriendo
- [ ] Puertos 8080 y 5432 están libres
- [ ] Código compilado sin errores
- [ ] Variables de entorno configuradas
- [ ] Ejecutar `deploy.ps1` o `docker-compose up`
- [ ] Verificar logs con `docker-compose logs -f`
- [ ] Acceder a http://localhost:8080
- [ ] Probar funcionalidades clave

---

**Sistema listo para producción.**
