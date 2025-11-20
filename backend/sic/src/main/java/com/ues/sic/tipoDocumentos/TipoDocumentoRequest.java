package com.ues.sic.tipoDocumentos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TipoDocumentoRequest {

    @NotBlank
    private String nombre;

    @NotNull
    private Integer creadoPor; // id del usuario

    // ==== Getters y Setters ====
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(Integer creadoPor) {
        this.creadoPor = creadoPor;
    }
}
