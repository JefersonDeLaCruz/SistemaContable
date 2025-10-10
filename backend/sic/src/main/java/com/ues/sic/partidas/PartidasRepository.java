package com.ues.sic.partidas;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
/* 
import com.ues.sic.usuarios.UsuariosModel;
 */
public interface PartidasRepository extends  JpaRepository<PartidasModel, Long>{
    PartidasModel findByDescripcion(String id);
    List<PartidasModel> findByIdPeriodo(String idPeriodo);
}
