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
}
