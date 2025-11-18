package com.ues.sic.libro_mayor.projections;

public interface AsientoMayorRow {
    Integer getIdPartida();
    java.time.LocalDate getFecha();
    String getDescripcionPartida();
    Integer getIdCuenta();
    String getNombreCuenta();           // <- nuevo
    java.math.BigDecimal getDebito();
    java.math.BigDecimal getCredito();
}
