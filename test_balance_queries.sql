-- ===============================================
-- SCRIPTS DE PRUEBA - BALANCE GENERAL Y ESTADO DE RESULTADOS
-- Sistema Contable
-- Fecha: 2025-11-22
-- ===============================================

-- ===============================================
-- TEST 1: Verificar si hay movimientos en cuentas de GRUPO
-- Resultado esperado: 0 filas (NO debería haber movimientos en cuentas de grupo)
-- ===============================================

SELECT
  '🚨 MOVIMIENTOS EN CUENTAS DE GRUPO' AS alerta,
  p.id AS partida_id,
  p.fecha,
  p.descripcion,
  d.id_cuenta,
  c.codigo,
  c.nombre,
  c.tipo,
  d.debito,
  d.credito
FROM detalle_partida d
JOIN partidas p ON p.id = d.id_partida
JOIN cuentas c ON c.id_cuenta = CAST(d.id_cuenta AS INTEGER)
WHERE c.codigo IN (
    '1',    -- ACTIVO (grupo principal)
    '1.1',  -- ACTIVO CORRIENTE (subgrupo)
    '1.2',  -- ACTIVO NO CORRIENTE (subgrupo)
    '2',    -- PASIVO (grupo principal)
    '2.1',  -- PASIVO CORRIENTE (subgrupo)
    '2.2',  -- PASIVO NO CORRIENTE (subgrupo)
    '3',    -- CAPITAL CONTABLE (grupo principal)
    '4',    -- INGRESOS (grupo principal)
    '5',    -- COSTO DE VENTAS (grupo principal)
    '6',    -- GASTOS DE OPERACIÓN (grupo principal)
    '6.1',  -- GASTOS DE ADMINISTRACIÓN (subgrupo)
    '6.2',  -- GASTOS DE VENTAS (subgrupo)
    '7'     -- GASTOS NO OPERATIVOS (grupo principal)
)
ORDER BY p.fecha DESC, d.id;

-- Si esta query retorna filas, HAY UN PROBLEMA CRÍTICO
-- Las cuentas de grupo NO deberían tener movimientos directos

-- ===============================================
-- TEST 2: Verificar Balance General cuadra
-- Resultado esperado: diferencia = 0
-- ===============================================

WITH balance AS (
    SELECT
        -- ACTIVO
        (
            SELECT COALESCE(SUM(
                CASE
                    WHEN c.saldo_normal = 'DEUDOR' THEN (d.debito - d.credito)
                    ELSE (d.credito - d.debito)
                END
            ), 0)
            FROM cuentas c
            LEFT JOIN detalle_partida d ON CAST(d.id_cuenta AS INTEGER) = c.id_cuenta
            LEFT JOIN partidas p ON p.id = d.id_partida
            WHERE c.tipo = 'ACTIVO'
              AND (p.fecha IS NULL OR CAST(p.fecha AS DATE) <= CURRENT_DATE)
        ) AS total_activo,

        -- PASIVO
        (
            SELECT COALESCE(SUM(
                CASE
                    WHEN c.saldo_normal = 'DEUDOR' THEN (d.debito - d.credito)
                    ELSE (d.credito - d.debito)
                END
            ), 0)
            FROM cuentas c
            LEFT JOIN detalle_partida d ON CAST(d.id_cuenta AS INTEGER) = c.id_cuenta
            LEFT JOIN partidas p ON p.id = d.id_partida
            WHERE c.tipo = 'PASIVO'
              AND (p.fecha IS NULL OR CAST(p.fecha AS DATE) <= CURRENT_DATE)
        ) AS total_pasivo,

        -- CAPITAL CONTABLE
        (
            SELECT COALESCE(SUM(
                CASE
                    WHEN c.saldo_normal = 'DEUDOR' THEN (d.debito - d.credito)
                    ELSE (d.credito - d.debito)
                END
            ), 0)
            FROM cuentas c
            LEFT JOIN detalle_partida d ON CAST(d.id_cuenta AS INTEGER) = c.id_cuenta
            LEFT JOIN partidas p ON p.id = d.id_partida
            WHERE c.tipo = 'CAPITAL CONTABLE'
              AND (p.fecha IS NULL OR CAST(p.fecha AS DATE) <= CURRENT_DATE)
        ) AS total_capital,

        -- INGRESOS
        (
            SELECT COALESCE(SUM(d.credito - d.debito), 0)
            FROM detalle_partida d
            JOIN partidas p ON p.id = d.id_partida
            LEFT JOIN cuentas c ON c.id_cuenta = CAST(d.id_cuenta AS INTEGER)
            WHERE (c.tipo = 'INGRESOS' OR c.codigo LIKE '4%')
              AND CAST(p.fecha AS DATE) <= CURRENT_DATE
        ) AS total_ingresos,

        -- GASTOS
        (
            SELECT COALESCE(SUM(d.debito - d.credito), 0)
            FROM detalle_partida d
            JOIN partidas p ON p.id = d.id_partida
            LEFT JOIN cuentas c ON c.id_cuenta = CAST(d.id_cuenta AS INTEGER)
            WHERE (c.tipo = 'GASTOS' OR c.codigo LIKE '5%' OR c.codigo LIKE '6%' OR c.codigo LIKE '7%')
              AND CAST(p.fecha AS DATE) <= CURRENT_DATE
        ) AS total_gastos
)
SELECT
    '📊 VERIFICACIÓN DE BALANCE GENERAL' AS reporte,
    total_activo,
    total_pasivo,
    total_capital,
    (total_ingresos - total_gastos) AS resultado_ejercicio,
    (total_pasivo + total_capital + (total_ingresos - total_gastos)) AS pasivo_mas_capital_mas_resultado,
    (total_activo - (total_pasivo + total_capital + (total_ingresos - total_gastos))) AS diferencia,
    CASE
        WHEN ABS(total_activo - (total_pasivo + total_capital + (total_ingresos - total_gastos))) < 0.01 THEN '✅ CUADRA'
        ELSE '❌ NO CUADRA'
    END AS estado
FROM balance;

-- Si diferencia != 0, hay un problema en los cálculos o en los datos

-- ===============================================
-- TEST 3: Listar todas las cuentas con movimientos
-- Para verificar que solo las cuentas de DETALLE tienen movimientos
-- ===============================================

SELECT
    '📋 CUENTAS CON MOVIMIENTOS' AS reporte,
    c.codigo,
    c.nombre,
    c.tipo,
    c.saldo_normal,
    c.id_padre,
    COUNT(d.id) AS cantidad_movimientos,
    COALESCE(SUM(d.debito), 0) AS total_debito,
    COALESCE(SUM(d.credito), 0) AS total_credito,
    CASE
        WHEN c.saldo_normal = 'DEUDOR' THEN COALESCE(SUM(d.debito - d.credito), 0)
        ELSE COALESCE(SUM(d.credito - d.debito), 0)
    END AS saldo,
    CASE
        WHEN c.id_padre IS NULL THEN '🚨 GRUPO PRINCIPAL'
        WHEN LENGTH(c.codigo) - LENGTH(REPLACE(c.codigo, '.', '')) = 1 AND c.codigo NOT LIKE '%.%.%' THEN '⚠️ SUBGRUPO'
        ELSE '✅ CUENTA DETALLE'
    END AS clasificacion
FROM cuentas c
LEFT JOIN detalle_partida d ON CAST(d.id_cuenta AS INTEGER) = c.id_cuenta
GROUP BY c.id_cuenta, c.codigo, c.nombre, c.tipo, c.saldo_normal, c.id_padre
HAVING COUNT(d.id) > 0
ORDER BY c.codigo;

-- Las filas con 🚨 o ⚠️ indican posibles problemas

-- ===============================================
-- TEST 4: Verificar Estado de Resultados del mes actual
-- ===============================================

SELECT
    '📊 ESTADO DE RESULTADOS - MES ACTUAL' AS reporte,
    YEAR(CURRENT_DATE) AS anio,
    MONTH(CURRENT_DATE) AS mes,

    -- INGRESOS
    (
        SELECT COALESCE(SUM(d.credito - d.debito), 0)
        FROM detalle_partida d
        JOIN partidas p ON p.id = d.id_partida
        JOIN cuentas c ON c.id_cuenta = CAST(d.id_cuenta AS INTEGER)
        WHERE (c.tipo = 'INGRESOS' OR c.codigo LIKE '4%')
          AND MONTH(CAST(p.fecha AS DATE)) = MONTH(CURRENT_DATE)
          AND YEAR(CAST(p.fecha AS DATE)) = YEAR(CURRENT_DATE)
    ) AS total_ingresos,

    -- GASTOS
    (
        SELECT COALESCE(SUM(d.debito - d.credito), 0)
        FROM detalle_partida d
        JOIN partidas p ON p.id = d.id_partida
        JOIN cuentas c ON c.id_cuenta = CAST(d.id_cuenta AS INTEGER)
        WHERE (c.tipo = 'GASTOS' OR c.codigo LIKE '5%' OR c.codigo LIKE '6%' OR c.codigo LIKE '7%')
          AND MONTH(CAST(p.fecha AS DATE)) = MONTH(CURRENT_DATE)
          AND YEAR(CAST(p.fecha AS DATE)) = YEAR(CURRENT_DATE)
    ) AS total_gastos,

    -- UTILIDAD NETA
    (
        SELECT COALESCE(SUM(d.credito - d.debito), 0)
        FROM detalle_partida d
        JOIN partidas p ON p.id = d.id_partida
        JOIN cuentas c ON c.id_cuenta = CAST(d.id_cuenta AS INTEGER)
        WHERE (c.tipo = 'INGRESOS' OR c.codigo LIKE '4%')
          AND MONTH(CAST(p.fecha AS DATE)) = MONTH(CURRENT_DATE)
          AND YEAR(CAST(p.fecha AS DATE)) = YEAR(CURRENT_DATE)
    ) - (
        SELECT COALESCE(SUM(d.debito - d.credito), 0)
        FROM detalle_partida d
        JOIN partidas p ON p.id = d.id_partida
        JOIN cuentas c ON c.id_cuenta = CAST(d.id_cuenta AS INTEGER)
        WHERE (c.tipo = 'GASTOS' OR c.codigo LIKE '5%' OR c.codigo LIKE '6%' OR c.codigo LIKE '7%')
          AND MONTH(CAST(p.fecha AS DATE)) = MONTH(CURRENT_DATE)
          AND YEAR(CAST(p.fecha AS DATE)) = YEAR(CURRENT_DATE)
    ) AS utilidad_neta;

-- ===============================================
-- TEST 5: Verificar jerarquía de cuentas
-- Mostrar estructura del catálogo de cuentas
-- ===============================================

SELECT
    '🌳 JERARQUÍA DE CUENTAS' AS reporte,
    CASE
        WHEN c.codigo NOT LIKE '%.%' THEN CONCAT('📁 ', c.codigo, ' - ', c.nombre)
        WHEN c.codigo LIKE '%.%.%' THEN CONCAT('  📄 ', c.codigo, ' - ', c.nombre)
        ELSE CONCAT('📂 ', c.codigo, ' - ', c.nombre)
    END AS cuenta_jerarquia,
    c.tipo,
    c.saldo_normal,
    c.id_padre,
    (SELECT COUNT(*) FROM cuentas c2 WHERE c2.id_padre = c.codigo) AS cantidad_hijos,
    CASE
        WHEN (SELECT COUNT(*) FROM cuentas c2 WHERE c2.id_padre = c.codigo) > 0 THEN 'GRUPO'
        ELSE 'DETALLE'
    END AS clasificacion
FROM cuentas c
ORDER BY c.codigo;

-- ===============================================
-- TEST 6: Detectar partidas desbalanceadas
-- Todas las partidas deben tener débito = crédito
-- ===============================================

SELECT
    '⚖️ PARTIDAS DESBALANCEADAS' AS alerta,
    p.id AS partida_id,
    p.fecha,
    p.descripcion,
    COALESCE(SUM(d.debito), 0) AS total_debitos,
    COALESCE(SUM(d.credito), 0) AS total_creditos,
    COALESCE(SUM(d.debito), 0) - COALESCE(SUM(d.credito), 0) AS diferencia,
    CASE
        WHEN ABS(COALESCE(SUM(d.debito), 0) - COALESCE(SUM(d.credito), 0)) < 0.01 THEN '✅ BALANCEADA'
        ELSE '❌ DESBALANCEADA'
    END AS estado
FROM partidas p
LEFT JOIN detalle_partida d ON d.id_partida = p.id
GROUP BY p.id, p.fecha, p.descripcion
HAVING ABS(COALESCE(SUM(d.debito), 0) - COALESCE(SUM(d.credito), 0)) >= 0.01
ORDER BY p.fecha DESC;

-- Si esta query retorna filas, hay partidas con errores

-- ===============================================
-- TEST 7: Resumen de cuentas por tipo
-- ===============================================

SELECT
    '📊 RESUMEN POR TIPO DE CUENTA' AS reporte,
    c.tipo,
    COUNT(*) AS cantidad_cuentas,
    SUM(CASE WHEN (SELECT COUNT(*) FROM cuentas c2 WHERE c2.id_padre = c.codigo) > 0 THEN 1 ELSE 0 END) AS cuentas_grupo,
    SUM(CASE WHEN (SELECT COUNT(*) FROM cuentas c2 WHERE c2.id_padre = c.codigo) = 0 THEN 1 ELSE 0 END) AS cuentas_detalle,
    COUNT(DISTINCT d.id) AS movimientos,
    COALESCE(SUM(d.debito), 0) AS total_debitos,
    COALESCE(SUM(d.credito), 0) AS total_creditos
FROM cuentas c
LEFT JOIN detalle_partida d ON CAST(d.id_cuenta AS INTEGER) = c.id_cuenta
GROUP BY c.tipo
ORDER BY c.tipo;

-- ===============================================
-- INSTRUCCIONES DE USO:
-- ===============================================
-- 1. Ejecutar cada test en orden
-- 2. TEST 1: Si retorna filas, hay movimientos en cuentas de grupo (CRÍTICO)
-- 3. TEST 2: Verificar que diferencia = 0 (balance cuadra)
-- 4. TEST 3: Revisar clasificación de cuentas con movimientos
-- 5. TEST 4: Verificar ingresos y gastos del mes
-- 6. TEST 5: Revisar estructura del catálogo
-- 7. TEST 6: Si retorna filas, hay partidas desbalanceadas (ERROR GRAVE)
-- 8. TEST 7: Resumen general del catálogo
--
-- Si algún test falla, revisar el archivo ANALISIS_PROBLEMAS_BALANCE.md
-- ===============================================
