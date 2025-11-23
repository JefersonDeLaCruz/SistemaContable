# 🔍 INSTRUCCIONES DE VERIFICACIÓN - BALANCE GENERAL Y ESTADO DE RESULTADOS

**Fecha:** 2025-11-22
**Sistema:** SistemaContable

---

## 📋 DOCUMENTOS GENERADOS

He creado **3 documentos** para ayudarte:

1. **[ANALISIS_PROBLEMAS_BALANCE.md](./ANALISIS_PROBLEMAS_BALANCE.md)** - Análisis detallado de todos los problemas encontrados
2. **[test_balance_queries.sql](./test_balance_queries.sql)** - Scripts SQL para testear el sistema
3. **[VERIFICACION_ESTADO_RESULTADOS.md](./VERIFICACION_ESTADO_RESULTADOS.md)** - Verificación de capturaes de ingresos/gastos

---

## ⚡ ACCIÓN INMEDIATA REQUERIDA

### PASO 1: Ejecutar Tests SQL (10 minutos)

Abre tu cliente PostgreSQL y ejecuta:

```bash
psql -U postgres -d sistema_contable -f test_balance_queries.sql
```

O desde tu IDE de base de datos, ejecuta cada test manualmente:

1. **TEST 1** - Movimientos en cuentas de grupo (CRÍTICO)
2. **TEST 2** - Balance General cuadra
3. **TEST 3** - Cuentas con movimientos
4. **TEST 4** - Estado de Resultados
5. **TEST 5** - Jerarquía de cuentas
6. **TEST 6** - Partidas desbalanceadas
7. **TEST 7** - Resumen por tipo

---

### PASO 2: Analizar Resultados

#### ✅ Si TEST 1 retorna 0 filas:

**EXCELENTE** - No hay movimientos en cuentas de grupo. El sistema está funcionando correctamente.

**Acción:** No se requiere corrección inmediata, pero implementa las mejoras preventivas (ver PASO 3).

#### ❌ Si TEST 1 retorna filas:

**CRÍTICO** - Hay movimientos en cuentas de grupo.

**Acción inmediata:**
1. Revisar cada movimiento listado
2. Reclasificar a cuentas de detalle
3. Crear script de corrección

**Ejemplo de script de corrección:**

```sql
-- Reemplazar movimientos de cuenta 1.1 (grupo) a 1.1.01 (detalle)
UPDATE detalle_partida
SET id_cuenta = (SELECT id_cuenta FROM cuentas WHERE codigo = '1.1.01')
WHERE id_cuenta = (SELECT id_cuenta FROM cuentas WHERE codigo = '1.1')
  AND -- agregar condiciones específicas
```

#### 🔍 Si TEST 2 muestra diferencia != 0:

**GRAVE** - El Balance General no cuadra.

**Posibles causas:**
1. Partidas desbalanceadas (ejecutar TEST 6)
2. Movimientos en cuentas de grupo (TEST 1)
3. Error en cálculo de resultado del ejercicio

**Acción:**
1. Ejecutar TEST 6 para detectar partidas desbalanceadas
2. Corregir partidas con débito != crédito
3. Volver a ejecutar TEST 2

#### ⚠️ Si TEST 6 retorna filas:

**ERROR GRAVE** - Hay partidas con débito != crédito.

**Todas las partidas deben cumplir:** `TOTAL DÉBITOS = TOTAL CRÉDITOS`

**Acción:**
1. Revisar cada partida listada
2. Completar los asientos faltantes
3. Eliminar partidas incorrectas si es necesario

---

### PASO 3: Implementar Mejoras Preventivas

Una vez verificado el sistema, implementa estas mejoras:

#### Mejora 1: Agregar validación en partidas

Edita: [DetallePartidaService.java](backend/sic/src/main/java/com/ues/sic/detalle_partida/DetallePartidaService.java)

```java
private static final Set<String> CUENTAS_GRUPO = Set.of(
    "1", "1.1", "1.2",
    "2", "2.1", "2.2",
    "3",
    "4",
    "5",
    "6", "6.1", "6.2",
    "7"
);

public void validarCuenta(String codigoCuenta) throws IllegalArgumentException {
    if (CUENTAS_GRUPO.contains(codigoCuenta)) {
        throw new IllegalArgumentException(
            "La cuenta " + codigoCuenta + " es una cuenta de grupo " +
            "y no acepta movimientos directos. Use una cuenta de detalle."
        );
    }
}

// Llamar esta validación antes de guardar un detalle de partida
@Transactional
public DetallePartidaModel guardar(DetallePartidaModel detalle) {
    // Obtener el código de cuenta
    CuentaModel cuenta = cuentaRepo.findById(detalle.getIdCuenta())
        .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));

    validarCuenta(cuenta.getCodigo());

    return detalleRepo.save(detalle);
}
```

#### Mejora 2: Agregar filtro en queries (opcional)

Si prefieres filtrar en la base de datos:

```sql
-- Modificar query del Balance General
WHERE c.tipo IN ('ACTIVO','PASIVO','CAPITAL CONTABLE')
  AND c.codigo NOT IN ('1', '1.1', '1.2', '2', '2.1', '2.2', '3')  -- Excluir grupos
  AND (p.fecha IS NULL OR CAST(p.fecha AS DATE) <= CAST(:fechaCorte AS DATE))
```

#### Mejora 3: Agregar campo al modelo (más robusto)

1. Agregar campos a `CuentaModel.java`:

```java
@Column(name = "es_grupo")
private Boolean esGrupo = false;

@Column(name = "acepta_movimientos")
private Boolean aceptaMovimientos = true;
```

2. Actualizar base de datos:

```sql
ALTER TABLE cuentas
ADD COLUMN es_grupo BOOLEAN DEFAULT FALSE,
ADD COLUMN acepta_movimientos BOOLEAN DEFAULT TRUE;

-- Marcar cuentas de grupo
UPDATE cuentas SET es_grupo = TRUE, acepta_movimientos = FALSE
WHERE codigo IN ('1', '1.1', '1.2', '2', '2.1', '2.2', '3', '4', '5', '6', '6.1', '6.2', '7');
```

3. Modificar queries:

```sql
WHERE c.tipo IN ('ACTIVO','PASIVO','CAPITAL CONTABLE')
  AND (c.acepta_movimientos = TRUE OR c.acepta_movimientos IS NULL)
  AND (p.fecha IS NULL OR CAST(p.fecha AS DATE) <= CAST(:fechaCorte AS DATE))
```

---

## 📊 RESUMEN DE VERIFICACIÓN

### ✅ Lo que está BIEN:

1. **Nombres de cuentas** - Correctos y siguiendo normas contables
2. **Clasificación de tipos** - INGRESOS y GASTOS unificados (plural)
3. **Queries SQL** - Capturan todos los códigos necesarios (4, 5, 6, 7)
4. **Cálculos en controlador** - Lógica correcta para saldos y utilidades
5. **Frontend** - Renderiza correctamente con métricas clave
6. **Estructura del catálogo** - Jerarquía lógica y completa

### ⚠️ Lo que PUEDE fallar:

1. **Movimientos en cuentas de grupo** - Si se permite, duplica saldos
2. **Falta validación** - No hay prevención de errores de captura
3. **Partidas desbalanceadas** - Si existen, el balance no cuadrará

---

## 🎯 PLAN DE ACCIÓN RECOMENDADO

| Paso | Acción | Tiempo | Prioridad |
|------|--------|--------|-----------|
| 1 | Ejecutar TEST 1-7 | 10 min | 🔴 ALTA |
| 2 | Analizar resultados | 15 min | 🔴 ALTA |
| 3 | Corregir datos si es necesario | Variable | 🔴 ALTA |
| 4 | Implementar validación básica | 30 min | 🟠 MEDIA |
| 5 | Agregar campos al modelo | 1-2 horas | 🟡 BAJA |
| 6 | Crear UI mejorada | 2-3 horas | 🟡 BAJA |

---

## 📞 SOPORTE

Si necesitas ayuda para:
- Interpretar los resultados de los tests
- Corregir datos incorrectos
- Implementar las mejoras

Revisa el archivo **[ANALISIS_PROBLEMAS_BALANCE.md](./ANALISIS_PROBLEMAS_BALANCE.md)** que tiene ejemplos detallados.

---

## ✅ VERIFICACIÓN COMPLETADA

**Estado del sistema:**
- ✅ Queries SQL: CORRECTAS (capturan todos los datos necesarios)
- ✅ Controladores: CORRECTOS (cálculos según normas contables)
- ✅ Frontend: CORRECTO (renderiza estructura completa)
- ⚠️ Validaciones: FALTANTES (implementar para prevenir errores)
- ⚠️ Datos: REQUIERE VERIFICACIÓN (ejecutar tests SQL)

**Próximos pasos:**
1. Ejecutar tests SQL
2. Verificar que no hay movimientos en cuentas de grupo
3. Implementar validaciones recomendadas

---

**Generado:** 2025-11-22
**Sistema:** SistemaContable
**Verificado por:** Claude Code
