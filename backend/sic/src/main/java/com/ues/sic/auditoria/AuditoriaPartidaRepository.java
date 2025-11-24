package com.ues.sic.auditoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para gestionar el historial de auditoría de partidas contables.
 */
@Repository
public interface AuditoriaPartidaRepository extends JpaRepository<AuditoriaPartidaModel, Long> {

    /**
     * Encuentra todas las auditorías de una partida específica ordenadas por fecha.
     */
    List<AuditoriaPartidaModel> findByPartidaIdOrderByFechaCambioDesc(Long partidaId);

    /**
     * Encuentra auditorías por usuario.
     */
    List<AuditoriaPartidaModel> findByUsuarioIdOrderByFechaCambioDesc(String usuarioId);

    /**
     * Encuentra auditorías por tipo de operación.
     */
    List<AuditoriaPartidaModel> findByOperacionOrderByFechaCambioDesc(String operacion);

    /**
     * Encuentra auditorías en un rango de fechas.
     */
    @Query("SELECT a FROM AuditoriaPartidaModel a WHERE a.fechaCambio BETWEEN :inicio AND :fin ORDER BY a.fechaCambio DESC")
    List<AuditoriaPartidaModel> findByFechaCambioBetween(
        @Param("inicio") LocalDateTime inicio,
        @Param("fin") LocalDateTime fin
    );

    /**
     * Cuenta auditorías por partida.
     */
    Long countByPartidaId(Long partidaId);

    /**
     * Encuentra todas las auditorías ordenadas por fecha (para reporte completo).
     */
    List<AuditoriaPartidaModel> findAllByOrderByFechaCambioDesc();

    /**
     * Encuentra auditorías de hoy.
     */
    @Query("SELECT a FROM AuditoriaPartidaModel a WHERE CAST(a.fechaCambio AS date) = CAST(:fecha AS date) ORDER BY a.fechaCambio DESC")
    List<AuditoriaPartidaModel> findAuditoriasDelDia(@Param("fecha") LocalDateTime fecha);
}
