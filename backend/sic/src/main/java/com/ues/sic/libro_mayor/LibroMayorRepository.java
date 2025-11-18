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
                p.id                   AS idPartida,
                p.fecha                AS fecha,
                p.descripcion          AS descripcionPartida,
                d.id_cuenta            AS idCuenta,
                c.nombre               AS nombreCuenta,          
                COALESCE(d.debito,0)   AS debito,
                COALESCE(d.credito,0)  AS credito
            FROM public.partidas p
            JOIN public.detalle_partida d
              ON (d.id_partida)::integer = p.id
            JOIN public.cuentas c                                 
              ON c.id_cuenta = d.id_cuenta::integer                           
            WHERE (p.id_periodo)::integer = :periodoId
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
