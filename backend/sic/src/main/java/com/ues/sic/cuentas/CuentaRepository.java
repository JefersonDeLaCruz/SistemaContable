package com.ues.sic.cuentas;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CuentaRepository extends JpaRepository<CuentaModel, Integer> {
    Optional<CuentaModel> findByCodigo(String codigo);

     @Query("SELECT c.codigo FROM CuentaModel c WHERE c.idCuenta = :idCuenta")
    String findCodigoByIdCuenta(@Param("idCuenta") Integer idCuenta);
}
