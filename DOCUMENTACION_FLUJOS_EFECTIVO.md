# Estado de Flujos de Efectivo (EFE) - Documentación

## 📋 Descripción General

Se ha implementado un módulo completo para el cálculo del **Estado de Flujos de Efectivo (EFE)** siguiendo el **Método Indirecto** según las normas NIIF/US GAAP. El sistema calcula automáticamente los flujos de efectivo clasificados en tres categorías principales:

1. **Actividades de Operación**
2. **Actividades de Inversión**
3. **Actividades de Financiamiento**

## 🏗️ Arquitectura del Sistema

### Componentes Backend

#### 1. **CategoriaEFE.java** (Enum)
Ubicación: `com.ues.sic.balances.flujos.CategoriaEFE`

Define las categorías de clasificación:
- `EFECTIVO`: Cuentas de efectivo y equivalentes (Caja, Bancos)
- `OPERACION`: Actividades operacionales
- `INVERSION`: Actividades de inversión (activos fijos)
- `FINANCIAMIENTO`: Préstamos, capital, dividendos
- `NO_FLUJO`: Partidas que no generan flujo (depreciación, provisiones)
- `INDETERMINADA`: Cuentas sin clasificación

#### 2. **EstadoFlujosEfectivoDTO.java** (DTO)
Ubicación: `com.ues.sic.balances.flujos.EstadoFlujosEfectivoDTO`

Estructura de datos para el EFE con las siguientes secciones:
- **PeriodoDTO**: Información del período
- **SeccionOperacionDTO**: Flujos de operación
  - Utilidad neta
  - Ajustes por partidas que no afectan efectivo
  - Cambios en capital de trabajo
- **SeccionInversionDTO**: Flujos de inversión
  - Adquisiciones de activos
  - Ventas de activos
- **SeccionFinanciamientoDTO**: Flujos de financiamiento
  - Entradas (préstamos, aportes de capital)
  - Salidas (pago de préstamos, dividendos)

#### 3. **ClasificadorCuentasEFE.java** (Servicio)
Ubicación: `com.ues.sic.balances.flujos.ClasificadorCuentasEFE`

**Responsabilidad**: Clasifica automáticamente cada cuenta del catálogo contable según su categoría en el EFE.

**Lógica de Clasificación**:

##### Efectivo
- Códigos: `1.1.01` a `1.1.05` (Caja, Bancos)
- Nombres que contengan: "CAJA", "BANCO", "EFECTIVO", "CHEQUE"

##### Inversión
- Activos no corrientes (`1.2.XX`)
- Excluyendo depreciación acumulada
- Nombres: "TERRENO", "EDIFICIO", "MOBILIARIO", "EQUIPO", "VEHÍCULO"

##### Financiamiento
- Todo el Capital Contable (excepto resultado del ejercicio)
- Pasivos a largo plazo (`2.2.XX`)
- Nombres: "PRÉSTAMO", "CAPITAL SOCIAL", "APORTE", "DIVIDENDO"

##### No Flujo
- Partidas que no generan movimiento de efectivo
- Nombres: "DEPRECIACIÓN", "AMORTIZACIÓN", "PROVISIÓN"

##### Operación
- Activo corriente (excepto efectivo)
- Pasivo corriente
- Todas las cuentas de Ingresos y Gastos

#### 4. **EstadoFlujosEfectivoService.java** (Servicio Principal)
Ubicación: `com.ues.sic.balances.flujos.EstadoFlujosEfectivoService`

**Algoritmo de Cálculo** (paso a paso):

```
1. OBTENER PERÍODO CONTABLE
   - Recuperar fechas de inicio y fin del período

2. CALCULAR SALDOS DE EFECTIVO
   - Saldo inicial: sumar cuentas de efectivo hasta día anterior al inicio
   - Saldo final: sumar cuentas de efectivo hasta fecha fin del período

3. CALCULAR UTILIDAD NETA
   - Ingresos del período - Gastos del período

4. OBTENER MOVIMIENTOS DEL PERÍODO
   - Consultar todos los débitos y créditos entre fechas
   - Calcular saldos iniciales (día anterior al inicio)

5. CLASIFICAR MOVIMIENTOS POR CATEGORÍA
   - Usar ClasificadorCuentasEFE para cada cuenta
   - Agrupar por: EFECTIVO, OPERACION, INVERSION, FINANCIAMIENTO, NO_FLUJO

6. PROCESAR ACTIVIDADES DE OPERACIÓN (Método Indirecto)
   a. Partir de la utilidad neta
   b. Ajustes por partidas que no generan flujo:
      - Sumar depreciación (gasto que no es salida de efectivo)
      - Sumar amortización
      - Sumar provisiones
   c. Cambios en capital de trabajo:
      - Activos corrientes:
        * Aumento = uso de efectivo (negativo)
        * Disminución = fuente de efectivo (positivo)
      - Pasivos corrientes:
        * Aumento = fuente de efectivo (positivo)
        * Disminución = uso de efectivo (negativo)
   d. Flujo neto = utilidad neta + ajustes + cambios

7. PROCESAR ACTIVIDADES DE INVERSIÓN
   a. Aumentos en activos fijos = adquisiciones (salida de efectivo)
   b. Disminuciones en activos fijos = ventas (entrada de efectivo)
   c. Flujo neto = ventas - adquisiciones

8. PROCESAR ACTIVIDADES DE FINANCIAMIENTO
   a. Aumentos en pasivos/capital = entradas de efectivo
      - Préstamos recibidos
      - Aportes de capital
   b. Disminuciones en pasivos/capital = salidas de efectivo
      - Pago de préstamos
      - Pago de dividendos
   c. Flujo neto = entradas - salidas

9. CALCULAR TOTALES FINALES
   - Aumento neto = flujo operación + flujo inversión + flujo financiamiento
   - Verificar: saldo inicial + aumento neto = saldo final
```

#### 5. **BalanceController.java** (Actualizado)
Ubicación: `com.ues.sic.balances.BalanceController`

**Nuevo Endpoint**:
```java
GET /api/balances/flujos-efectivo?periodo={idPeriodo}
```

**Respuesta JSON**:
```json
{
  "periodo": {
    "nombre": "2025",
    "inicio": "2025-01-01",
    "fin": "2025-12-31"
  },
  "saldoInicial": 10000.00,
  "operacion": {
    "utilidadNeta": 5000.00,
    "ajustesNoEfectivo": [
      {"codigo": "1.2.05", "nombre": "DEPRECIACIÓN", "monto": 1000.00}
    ],
    "totalAjustesNoEfectivo": 1000.00,
    "cambiosCapitalTrabajo": [
      {"codigo": "1.1.06", "nombre": "Disminución en CLIENTES", "monto": 500.00}
    ],
    "totalCambiosCapitalTrabajo": 500.00,
    "flujoNetoOperacion": 6500.00
  },
  "inversion": {
    "adquisiciones": [
      {"codigo": "1.2.03", "nombre": "Adquisición de MOBILIARIO", "monto": 2000.00}
    ],
    "ventas": [],
    "totalAdquisiciones": 2000.00,
    "totalVentas": 0.00,
    "flujoNetoInversion": -2000.00
  },
  "financiamiento": {
    "entradas": [
      {"codigo": "3.1", "nombre": "Aporte de CAPITAL SOCIAL", "monto": 3000.00}
    ],
    "salidas": [],
    "totalEntradas": 3000.00,
    "totalSalidas": 0.00,
    "flujoNetoFinanciamiento": 3000.00
  },
  "aumentoNetoEfectivo": 7500.00,
  "saldoFinal": 17500.00,
  "cuadra": true
}
```

### Componentes Frontend

#### 1. **balances.js** (Actualizado)
Ubicación: `static/js/balances.js`

**Nueva Función**: `renderFlujosEfectivo(data)`

Renderiza el EFE con las siguientes secciones:
- Header con información del período
- Resumen de estadísticas (cards)
- Tabla de actividades de operación
- Tabla de actividades de inversión
- Tabla de actividades de financiamiento
- Tabla resumen final

**Características**:
- Colores diferenciados por sección
- Formateo de moneda en USD
- Mensajes cuando no hay movimientos
- Indicador de cuadratura

#### 2. **balances.html** (Actualizado)
Ubicación: `templates/auditor/balances.html`

**Cambio**: Agregada opción "Flujos de Efectivo" al selector de tipo de balance.

```html
<select id="selectTipo" class="select">
  <option value="general">Balance General</option>
  <option value="estado">Estado de Resultados</option>
  <option value="comprobacion">Balance de Comprobación</option>
  <option value="flujos">Flujos de Efectivo</option> <!-- NUEVO -->
</select>
```

## 🔄 Flujo de Trabajo del Usuario

1. Usuario navega a **Balances** (auditor o contador)
2. Selecciona tipo: **"Flujos de Efectivo"**
3. Selecciona período contable del dropdown
4. Hace clic en **"Calcular"**
5. El sistema:
   - Consulta todas las partidas del período
   - Clasifica automáticamente cada cuenta
   - Calcula flujos por categoría
   - Genera el EFE completo
6. Se muestra el reporte con:
   - Saldo inicial y final de efectivo
   - Desglose de flujos de operación
   - Desglose de flujos de inversión
   - Desglose de flujos de financiamiento
   - Resumen final

## 📊 Reglas Contables Implementadas

### Método Indirecto - Actividades de Operación

**Base**: Utilidad Neta del período

**Ajustes (+)**:
- Depreciación (gasto no efectivo)
- Amortización
- Provisiones

**Cambios en Capital de Trabajo**:
- ↑ Inventarios = -efectivo
- ↓ Inventarios = +efectivo
- ↑ Cuentas por cobrar = -efectivo
- ↓ Cuentas por cobrar = +efectivo
- ↑ Proveedores = +efectivo
- ↓ Proveedores = -efectivo

### Actividades de Inversión

**Salidas (-)**:
- Compra de terrenos
- Compra de edificios
- Compra de equipos
- Compra de vehículos

**Entradas (+)**:
- Venta de activos fijos

### Actividades de Financiamiento

**Entradas (+)**:
- Préstamos recibidos
- Aportes de capital
- Emisión de acciones

**Salidas (-)**:
- Pago de préstamos
- Pago de dividendos
- Retiro de capital

## 🎯 Ventajas del Sistema

1. **Automatización Completa**: No requiere intervención manual
2. **Clasificación Inteligente**: Detecta automáticamente la categoría de cada cuenta
3. **Flexible**: Funciona con cualquier período contable
4. **Trazable**: Muestra el detalle de cada movimiento
5. **Verificable**: Incluye cuadratura automática
6. **Normativo**: Cumple con NIIF/US GAAP

## 🔧 Extensibilidad

Para agregar nuevas reglas de clasificación:

1. Modificar `ClasificadorCuentasEFE.java`
2. Actualizar los métodos `esXXX()` según necesidad
3. El sistema aplicará las nuevas reglas automáticamente

## ⚠️ Consideraciones

- Las cuentas de efectivo deben tener códigos `1.1.01` a `1.1.05`
- Los activos fijos deben estar en el rango `1.2.XX`
- Las cuentas sin código reconocido se marcan como `INDETERMINADA`
- El sistema usa el saldo normal de cada cuenta para determinar el signo

## 🧪 Cómo Probar

1. Asegurarse de tener períodos contables creados
2. Registrar partidas contables en el sistema
3. Navegar a Balances → Tipo: Flujos de Efectivo
4. Seleccionar un período
5. Verificar que el reporte muestre:
   - Saldo inicial de caja/bancos
   - Flujos de operación correctos
   - Movimientos de inversión (si hay compras de activos)
   - Movimientos de financiamiento (si hay préstamos/aportes)
   - Saldo final que cuadre

## 📝 Notas Técnicas

- **Cache**: El clasificador usa cache interno para mejorar performance
- **Redondeo**: Todos los montos se redondean a 2 decimales
- **Umbral**: Se ignoran movimientos menores a $0.01
- **Queries**: Reutiliza las consultas existentes del repositorio

---

**Autor**: Sistema Contable SIC  
**Fecha**: Noviembre 2025  
**Versión**: 1.0
