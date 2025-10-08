package com.ues.sic.detalle_partida;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DetallePartidaRepository extends JpaRepository<DetallePartidaModel, Long> {
    List<DetallePartidaModel> findByPartidaId(Long partidaId);

    
}