package com.ues.sic.detalle_partida;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface DetallePartidaRepository extends JpaRepository<DetallePartidaModel, Long> {
    List<DetallePartidaModel> findByPartida_Id(Long partidaId);
    @Query(value = """
            SELECT
              CAST(p.fecha AS DATE)                AS fecha,
              p.id                                 AS partida_id,
              COALESCE(c.codigo || ' - ' || c.nombre, d.id_cuenta::text) AS cuenta,
              d.debito                             AS debito,
              d.credito                            AS credito,
              p.id_usuario                         AS usuario
            FROM detalle_partida d
            JOIN partidas p ON p.id = d.id_partida
            LEFT JOIN cuentas c ON c.id_cuenta = CAST(d.id_cuenta AS INTEGER)
            ORDER BY CAST(p.fecha AS DATE) DESC, p.id DESC, d.id DESC
            LIMIT :limite
            """,
           nativeQuery = true)
    List<Object[]> ultimosMovimientos(@Param("limite") int limite);

    @Query(value = "SELECT MAX(GREATEST(d.debito,d.credito)) FROM detalle_partida d JOIN partidas p ON p.id = d.id_partida WHERE CAST(p.fecha AS DATE) = CAST(:hoy AS DATE)", nativeQuery = true)
    Double maxMovimientoHoy(@Param("hoy")String hoy);

    @Query(value = """
            SELECT COALESCE(SUM(d.credito - d.debito), 0)
            FROM detalle_partida d
            JOIN partidas p ON p.id = d.id_partida
            LEFT JOIN cuentas c ON c.id_cuenta = CAST(d.id_cuenta AS INTEGER)
            WHERE CAST(p.fecha AS DATE) = CAST(:hoy AS DATE)
              AND (c.tipo = 'INGRESOS' OR c.codigo LIKE '4%')
            """,
           nativeQuery = true)
    Double ingresoDiario(@Param("hoy") String hoy);

    @Query(value = """
            SELECT COALESCE(SUM(d.debito - d.credito), 0)
            FROM detalle_partida d
            JOIN partidas p ON p.id = d.id_partida
            LEFT JOIN cuentas c ON c.id_cuenta = CAST(d.id_cuenta AS INTEGER)
            WHERE CAST(p.fecha AS DATE) = CAST(:hoy AS DATE)
              AND (c.tipo = 'GASTOS' OR c.codigo LIKE '5%')
            """,
           nativeQuery = true)
    Double gastoDiario(@Param("hoy") String hoy);

    @Query(value = """
            SELECT
              CAST(p.fecha AS DATE)                AS fecha,
              p.id                                 AS partida_id,
              d.descripcion                        AS descripcion,
              COALESCE(c.codigo || ' - ' || c.nombre, d.id_cuenta::text) AS cuenta,
              d.debito                             AS debito,
              d.credito                            AS credito
            FROM detalle_partida d
            JOIN partidas p ON p.id = d.id_partida
            LEFT JOIN cuentas c ON c.id_cuenta = CAST(d.id_cuenta AS INTEGER)
            WHERE p.id_usuario = :usuario
            ORDER BY CAST(p.fecha AS DATE) DESC, p.id DESC, d.id DESC
            LIMIT :limite
            """,
           nativeQuery = true)
    List<Object[]> misMovimientosRecientes(@Param("usuario") String usuario, @Param("limite") int limite);

    @Query(value = """
            SELECT
              CAST(p.fecha AS DATE)                AS fecha,
              p.id                                 AS partida_id,
              d.descripcion                        AS descripcion,
              COALESCE(c.codigo || ' - ' || c.nombre, d.id_cuenta::text) AS cuenta,
              d.debito                             AS debito,
              d.credito                            AS credito
            FROM detalle_partida d
            JOIN partidas p ON p.id = d.id_partida
            LEFT JOIN cuentas c ON c.id_cuenta = CAST(d.id_cuenta AS INTEGER)
            WHERE p.id_usuario = :username OR p.id_usuario = :userId
            ORDER BY CAST(p.fecha AS DATE) DESC, p.id DESC, d.id DESC
            LIMIT :limite
            """,
           nativeQuery = true)
    List<Object[]> misMovimientosRecientesDe(
        @Param("username") String username,
        @Param("userId") String userId,
        @Param("limite") int limite
    );
    
    // Balance general acumulado hasta una fecha de corte
    @Query(value = """
            SELECT
              c.tipo AS tipo,
              c.id_cuenta AS id_cuenta,
              c.codigo AS codigo,
              c.nombre AS nombre,
              c.saldo_normal AS saldo_normal,
              COALESCE(SUM(d.debito), 0) AS total_debito,
              COALESCE(SUM(d.credito), 0) AS total_credito
            FROM cuentas c
            LEFT JOIN detalle_partida d ON CAST(d.id_cuenta AS INTEGER) = c.id_cuenta
            LEFT JOIN partidas p ON p.id = d.id_partida
            WHERE c.tipo IN ('ACTIVO','PASIVO','CAPITAL CONTABLE')
              AND (p.fecha IS NULL OR CAST(p.fecha AS DATE) <= CAST(:fechaCorte AS DATE))
            GROUP BY c.tipo, c.id_cuenta, c.codigo, c.nombre, c.saldo_normal
            ORDER BY c.codigo
            """,
           nativeQuery = true)
    List<Object[]> balanceGeneralHasta(@Param("fechaCorte") String fechaCorte);

    // Ingresos acumulados hasta fechaCorte (crédito - débito)
    @Query(value = """
            SELECT COALESCE(SUM(d.credito - d.debito), 0)
            FROM detalle_partida d
            JOIN partidas p ON p.id = d.id_partida
            LEFT JOIN cuentas c ON c.id_cuenta = CAST(d.id_cuenta AS INTEGER)
            WHERE CAST(p.fecha AS DATE) <= CAST(:fechaCorte AS DATE)
              AND (c.tipo = 'INGRESOS' OR c.codigo LIKE '4%')
            """,
           nativeQuery = true)
    Double ingresoAcumulado(@Param("fechaCorte") String fechaCorte);

    // Gastos acumulados hasta fechaCorte (débito - crédito)
    @Query(value = """
            SELECT COALESCE(SUM(d.debito - d.credito), 0)
            FROM detalle_partida d
            JOIN partidas p ON p.id = d.id_partida
            LEFT JOIN cuentas c ON c.id_cuenta = CAST(d.id_cuenta AS INTEGER)
            WHERE CAST(p.fecha AS DATE) <= CAST(:fechaCorte AS DATE)
              AND (c.tipo = 'GASTOS' OR c.codigo LIKE '5%')
            """,
           nativeQuery = true)
    Double gastoAcumulado(@Param("fechaCorte") String fechaCorte);
    
  

    // Estado de Resultados por periodo (rango de fechas)
    @Query(value = """
            SELECT
              c.tipo AS tipo,
              c.id_cuenta AS id_cuenta,
              c.codigo AS codigo,
              c.nombre AS nombre,
              c.saldo_normal AS saldo_normal,
              COALESCE(SUM(d.debito), 0) AS total_debito,
              COALESCE(SUM(d.credito), 0) AS total_credito
            FROM cuentas c
            LEFT JOIN detalle_partida d ON CAST(d.id_cuenta AS INTEGER) = c.id_cuenta
            LEFT JOIN partidas p ON p.id = d.id_partida
            WHERE (c.tipo = 'INGRESOS' OR c.codigo LIKE '4%' OR c.tipo = 'GASTOS' OR c.codigo LIKE '5%')
              AND (p.fecha IS NULL OR (CAST(p.fecha AS DATE) BETWEEN CAST(:inicio AS DATE) AND CAST(:fin AS DATE)))
            GROUP BY c.tipo, c.id_cuenta, c.codigo, c.nombre, c.saldo_normal
            ORDER BY c.codigo
            """,
           nativeQuery = true)
    List<Object[]> estadoResultadosEntre(@Param("inicio") String inicio, @Param("fin") String fin);

    @Query(value = """
            SELECT COALESCE(SUM(d.credito - d.debito), 0)
            FROM detalle_partida d
            JOIN partidas p ON p.id = d.id_partida
            LEFT JOIN cuentas c ON c.id_cuenta = CAST(d.id_cuenta AS INTEGER)
            WHERE CAST(p.fecha AS DATE) BETWEEN CAST(:inicio AS DATE) AND CAST(:fin AS DATE)
              AND (c.tipo = 'INGRESOS' OR c.codigo LIKE '4%')
            """,
           nativeQuery = true)
    Double ingresoEntre(@Param("inicio") String inicio, @Param("fin") String fin);

    @Query(value = """
            SELECT COALESCE(SUM(d.debito - d.credito), 0)
            FROM detalle_partida d
            JOIN partidas p ON p.id = d.id_partida
            LEFT JOIN cuentas c ON c.id_cuenta = CAST(d.id_cuenta AS INTEGER)
            WHERE CAST(p.fecha AS DATE) BETWEEN CAST(:inicio AS DATE) AND CAST(:fin AS DATE)
              AND (c.tipo = 'GASTOS' OR c.codigo LIKE '5%')
            """,
           nativeQuery = true)
    Double gastoEntre(@Param("inicio") String inicio, @Param("fin") String fin);

    // Saldos acumulados hasta fecha (todas las cuentas)
    @Query(value = """
            SELECT
              c.id_cuenta AS id_cuenta,
              c.codigo AS codigo,
              c.nombre AS nombre,
              c.saldo_normal AS saldo_normal,
              COALESCE(SUM(d.debito), 0) AS total_debito,
              COALESCE(SUM(d.credito), 0) AS total_credito
            FROM cuentas c
            LEFT JOIN detalle_partida d ON CAST(d.id_cuenta AS INTEGER) = c.id_cuenta
            LEFT JOIN partidas p ON p.id = d.id_partida
            WHERE (p.fecha IS NULL OR CAST(p.fecha AS DATE) <= CAST(:fecha AS DATE))
            GROUP BY c.id_cuenta, c.codigo, c.nombre, c.saldo_normal
            ORDER BY c.codigo
            """,
           nativeQuery = true)
    List<Object[]> saldosHastaTodos(@Param("fecha") String fecha);

    // Movimientos del periodo (todas las cuentas)
    @Query(value = """
            SELECT
              c.id_cuenta AS id_cuenta,
              c.codigo AS codigo,
              c.nombre AS nombre,
              COALESCE(SUM(d.debito), 0) AS debito,
              COALESCE(SUM(d.credito), 0) AS credito
            FROM cuentas c
            LEFT JOIN detalle_partida d ON CAST(d.id_cuenta AS INTEGER) = c.id_cuenta
            LEFT JOIN partidas p ON p.id = d.id_partida
            WHERE p.fecha IS NOT NULL
              AND CAST(p.fecha AS DATE) BETWEEN CAST(:inicio AS DATE) AND CAST(:fin AS DATE)
            GROUP BY c.id_cuenta, c.codigo, c.nombre
            ORDER BY c.codigo
            """,
           nativeQuery = true)
    List<Object[]> movimientosEntreTodos(@Param("inicio") String inicio, @Param("fin") String fin);
}