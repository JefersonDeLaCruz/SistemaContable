package com.ues.sic.libro_mayor;

import com.ues.sic.partidas.PartidasModel; // <- entidad @Entity de 'partidas'
import com.ues.sic.libro_mayor.projections.AsientoMayorRow;
import com.ues.sic.libro_mayor.projections.PeriodoAbiertoRow;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LibroMayorRepository extends Repository<PartidasModel, Integer> {

 @Query(value = """
    SELECT
        p.id                    AS idPartida,
        p.fecha                 AS fecha,
        p.descripcion           AS descripcionPartida,
        d.id_cuenta             AS idCuenta,
        c.nombre                AS nombreCuenta,
        COALESCE(d.debito,  0)  AS debito,
        COALESCE(d.credito, 0)  AS credito
    FROM public.partidas p
    JOIN public.detalle_partida d
        ON d.id_partida::integer = p.id
    JOIN public.cuentas c
        ON c.id_cuenta = d.id_cuenta::integer
    JOIN public.periodos_contables pc
        ON pc.id_periodo = :periodoId
    WHERE
          -- 1) Partidas que YA están ligadas a algún período (mes, trim, año)
          --    cuyo rango de fechas está contenido en el período seleccionado
          p.id_periodo::integer IN (
              SELECT pc2.id_periodo
              FROM public.periodos_contables pc2
              WHERE pc2.fecha_inicio >= pc.fecha_inicio
                AND pc2.fecha_fin    <= pc.fecha_fin
          )

          -- 2) (opcional) Partidas antiguas sin id_periodo,
          --    se filtran por el rango de fechas del período
          OR (
              p.id_periodo::integer IS NULL
              AND p.fecha::date BETWEEN pc.fecha_inicio AND pc.fecha_fin
          )
    ORDER BY d.id_cuenta, p.fecha, p.id
    """, nativeQuery = true)
List<AsientoMayorRow> findAsientosByPeriodo(@Param("periodoId") Integer periodoId);


  @Query(value = """
      SELECT pc.id_periodo AS idPeriodo
      FROM public.periodos_contables pc
      WHERE pc.cerrado = false
      ORDER BY pc.fecha_inicio DESC
      LIMIT 1
      """, nativeQuery = true)
  PeriodoAbiertoRow findUltimoPeriodoAbierto();

}
