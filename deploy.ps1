# ============================================
# Script de Despliegue - Sistema Contable
# ============================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Sistema Contable - Build Producción" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Paso 1: Limpiar contenedores y volúmenes anteriores (opcional)
Write-Host "[1/4] Limpiando contenedores anteriores..." -ForegroundColor Yellow
docker-compose down -v 2>$null
Write-Host "✓ Limpieza completada" -ForegroundColor Green
Write-Host ""

# Paso 2: Construir las imágenes
Write-Host "[2/4] Construyendo imágenes Docker..." -ForegroundColor Yellow
Write-Host "Esto puede tardar varios minutos..." -ForegroundColor Gray
docker-compose build --no-cache

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Build completado exitosamente" -ForegroundColor Green
} else {
    Write-Host "✗ Error en el build" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Paso 3: Iniciar los servicios
Write-Host "[3/4] Iniciando servicios..." -ForegroundColor Yellow
docker-compose up -d

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Servicios iniciados" -ForegroundColor Green
} else {
    Write-Host "✗ Error al iniciar servicios" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Paso 4: Mostrar estado
Write-Host "[4/4] Estado de los servicios:" -ForegroundColor Yellow
Start-Sleep -Seconds 5
docker-compose ps
Write-Host ""

# Mostrar logs
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Información de Acceso" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Aplicación: http://localhost:8080" -ForegroundColor Green
Write-Host "Base de datos: localhost:5432" -ForegroundColor Green
Write-Host ""
Write-Host "Para ver logs en tiempo real:" -ForegroundColor Yellow
Write-Host "  docker-compose logs -f java_app" -ForegroundColor Gray
Write-Host ""
Write-Host "Para detener los servicios:" -ForegroundColor Yellow
Write-Host "  docker-compose down" -ForegroundColor Gray
Write-Host ""
Write-Host "✓ Despliegue completado!" -ForegroundColor Green
