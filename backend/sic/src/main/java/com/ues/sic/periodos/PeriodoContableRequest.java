package com.ues.sic.periodos;

import java.time.LocalDate;

public record PeriodoContableRequest(
        String nombre,
        String frecuencia,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Boolean cerrado
) {}
