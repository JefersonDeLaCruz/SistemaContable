package com.ues.sic.cuentas;

public record CuentaRequest(
        String codigo,
        String nombre,
        String tipo,
        String saldoNormal,
        Integer codigoPadre // opcional: referenciamos al padre por su código
) {}
