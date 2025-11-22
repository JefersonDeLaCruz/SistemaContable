package com.ues.sic.libro_mayor.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AsientoMayorDTO {
    private Integer idPartida;
    private LocalDate fecha;
    private String descripcion;
    private BigDecimal debito;
    private BigDecimal credito;
    private BigDecimal saldoAcumulado;

    public AsientoMayorDTO(Integer idPartida, LocalDate fecha, String descripcion,
                           BigDecimal debito, BigDecimal credito, BigDecimal saldoAcumulado) {
        this.idPartida = idPartida;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.debito = debito;
        this.credito = credito;
        this.saldoAcumulado = saldoAcumulado;
    }

    // getters & setters (o usa Lombok @Data si prefieres)
    public Integer getIdPartida() { return idPartida; }
    public LocalDate getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getDebito() { return debito; }
    public BigDecimal getCredito() { return credito; }
    public BigDecimal getSaldoAcumulado() { return saldoAcumulado; }
}
