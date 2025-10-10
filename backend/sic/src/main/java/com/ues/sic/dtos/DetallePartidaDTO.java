package com.ues.sic.dtos;

public class DetallePartidaDTO {
    private Long id;
    private Long partidaId;
    private String idCuenta;
    private String descripcion;
    private Double debito;
    private Double credito;

    // Constructor vacío
    public DetallePartidaDTO() {}

    // Constructor con todos los campos
    public DetallePartidaDTO(Long id, Long partidaId, String idCuenta, String descripcion, Double debito, Double credito) {
        this.id = id;
        this.partidaId = partidaId;
        this.idCuenta = idCuenta;
        this.descripcion = descripcion;
        this.debito = debito;
        this.credito = credito;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPartidaId() {
        return partidaId;
    }

    public void setPartidaId(Long partidaId) {
        this.partidaId = partidaId;
    }

    public String getIdCuenta() {
        return idCuenta;
    }

    public void setIdCuenta(String idCuenta) {
        this.idCuenta = idCuenta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getDebito() {
        return debito;
    }

    public void setDebito(Double debito) {
        this.debito = debito;
    }

    public Double getCredito() {
        return credito;
    }

    public void setCredito(Double credito) {
        this.credito = credito;
    }
}
