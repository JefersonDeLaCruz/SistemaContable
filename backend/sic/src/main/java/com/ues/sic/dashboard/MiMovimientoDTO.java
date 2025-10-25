package com.ues.sic.dashboard;

public class MiMovimientoDTO {
    public String fecha;
    public Long partidaId;
    public String descripcion;
    public String cuenta;
    public Double debito;
    public Double credito;
    public String estado;

    public MiMovimientoDTO(String fecha, Long partidaId, String descripcion, String cuenta,
                           Double debito, Double credito, String estado) {
        this.fecha = fecha;
        this.partidaId = partidaId;
        this.descripcion = descripcion;
        this.cuenta = cuenta;
        this.debito = debito;
        this.credito = credito;
        this.estado = estado;
    }
}

