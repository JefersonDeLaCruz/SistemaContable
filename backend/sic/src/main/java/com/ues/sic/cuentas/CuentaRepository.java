package com.ues.sic.cuentas;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CuentaRepository extends JpaRepository<CuentaModel, Integer> {
    Optional<CuentaModel> findByCodigo(String codigo);
}
