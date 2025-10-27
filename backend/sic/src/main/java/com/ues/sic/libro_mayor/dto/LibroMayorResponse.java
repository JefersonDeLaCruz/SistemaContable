package com.ues.sic.libro_mayor.dto;

import java.math.BigDecimal;
import java.util.List;

public class LibroMayorResponse {
    private Integer periodoId;
    private List<CuentaMayorDTO> cuentas;
    private BigDecimal totalDebitosPeriodo;
    private BigDecimal totalCreditosPeriodo;

    public LibroMayorResponse(Integer periodoId, List<CuentaMayorDTO> cuentas,
                              BigDecimal totalDebitosPeriodo, BigDecimal totalCreditosPeriodo) {
        this.periodoId = periodoId;
        this.cuentas = cuentas;
        this.totalDebitosPeriodo = totalDebitosPeriodo;
        this.totalCreditosPeriodo = totalCreditosPeriodo;
    }

    public Integer getPeriodoId() { return periodoId; }
    public List<CuentaMayorDTO> getCuentas() { return cuentas; }
    public BigDecimal getTotalDebitosPeriodo() { return totalDebitosPeriodo; }
    public BigDecimal getTotalCreditosPeriodo() { return totalCreditosPeriodo; }
}
