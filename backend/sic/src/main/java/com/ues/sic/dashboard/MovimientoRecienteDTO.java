package com.ues.sic.dashboard;

public class MovimientoRecienteDTO {
    public String fecha;
    public Long partidaId;
    public String cuenta;
    public Double debito;
    public Double credito;
    public String usuario;

    public MovimientoRecienteDTO(String fecha, Long partidaId, String cuenta,
                                 Double debito, Double credito, String usuario) {
        this.fecha = fecha;
        this.partidaId = partidaId;
        this.cuenta = cuenta;
        this.debito = debito;
        this.credito = credito;
        this.usuario = usuario;
    }
}

