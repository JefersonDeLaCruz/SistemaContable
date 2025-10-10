package com.ues.sic.periodos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PeriodoContableRepository extends JpaRepository<PeriodoContableModel, Integer> {
    
    // Buscar un periodo por su nombre
    Optional<PeriodoContableModel> findByNombre(String nombre);

    // Buscar periodos por su estado (abierto o cerrado)
    Optional<PeriodoContableModel> findByCerrado(Boolean cerrado);
}
