package com.ues.sic.libro_mayor.dto;

import java.math.BigDecimal;
import java.util.List;

public class CuentaMayorDTO {
    private Integer idCuenta;
    private String nombreCuenta; // opcional (null si no hay join a cuentas)
    private List<AsientoMayorDTO> movimientos;
    private BigDecimal totalDebito;
    private BigDecimal totalCredito;
    private BigDecimal saldoFinal;

    public CuentaMayorDTO(Integer idCuenta, String nombreCuenta, List<AsientoMayorDTO> movimientos,
                          BigDecimal totalDebito, BigDecimal totalCredito, BigDecimal saldoFinal) {
        this.idCuenta = idCuenta;
        this.nombreCuenta = nombreCuenta;
        this.movimientos = movimientos;
        this.totalDebito = totalDebito;
        this.totalCredito = totalCredito;
        this.saldoFinal = saldoFinal;
    }

    // getters
    public Integer getIdCuenta() { return idCuenta; }
    public String getNombreCuenta() { return nombreCuenta; }
    public List<AsientoMayorDTO> getMovimientos() { return movimientos; }
    public BigDecimal getTotalDebito() { return totalDebito; }
    public BigDecimal getTotalCredito() { return totalCredito; }
    public BigDecimal getSaldoFinal() { return saldoFinal; }
}

