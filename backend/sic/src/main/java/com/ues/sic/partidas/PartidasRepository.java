package com.ues.sic.partidas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
/* 
import com.ues.sic.usuarios.UsuariosModel;
 */
public interface PartidasRepository extends  JpaRepository<PartidasModel, Long>{
    PartidasModel findByDescripcion(String id);
    List<PartidasModel> findByIdPeriodo(String idPeriodo);
    
    // Buscar partidas cuya fecha esté dentro del rango de fechas especificado
    // Usando CAST para asegurar comparación correcta de fechas
    @Query(value = "SELECT * FROM partidas p WHERE CAST(p.fecha AS DATE) >= CAST(:fechaInicio AS DATE) AND CAST(p.fecha AS DATE) <= CAST(:fechaFin AS DATE) ORDER BY CAST(p.fecha AS DATE) ASC, p.id ASC", nativeQuery = true)
    List<PartidasModel> findByFechaBetween(@Param("fechaInicio") String fechaInicio, @Param("fechaFin") String fechaFin);

    @Query(value = "SELECT CAST(p.fecha AS DATE) AS dia, COUNT(*) AS cantidad " +
                   "FROM partidas p " +
                   "WHERE CAST(p.fecha AS DATE) BETWEEN CAST(:inicio AS DATE) AND CAST(:fin AS DATE) " +
                   "GROUP BY CAST(p.fecha AS DATE) " +
                   "ORDER BY CAST(p.fecha AS DATE) ASC",
           nativeQuery = true)
    List<Object[]> countPorDiaEntre(@Param("inicio") String inicio, @Param("fin") String fin);

}
