package com.ues.sic.dtos;

import com.ues.sic.detalle_partida.DetallePartidaModel;
import com.ues.sic.partidas.PartidasModel;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para responder con una partida y sus detalles completos.
 * Usado para cargar datos en el formulario de edición.
 */
public class PartidaConDetallesResponseDTO {

    private Long id;
    private String descripcion;
    private String fecha;
    private String idPeriodo;
    private String idUsuario;
    private String estado;
    private String createdAt;
    private String updatedAt;
    private String createdBy;
    private String updatedBy;
    private List<DetalleResponseDTO> detalles = new ArrayList<>();

    // Constructor vacío
    public PartidaConDetallesResponseDTO() {}

    // Constructor desde modelo
    public PartidaConDetallesResponseDTO(PartidasModel partida, List<DetallePartidaModel> detalles) {
        this.id = partida.getId();
        this.descripcion = partida.getDescripcion();
        this.fecha = partida.getFecha();
        this.idPeriodo = partida.getIdPeriodo();
        this.idUsuario = partida.getIdUsuario();
        this.estado = partida.getEstado();
        this.createdAt = partida.getCreatedAt() != null ? partida.getCreatedAt().toString() : null;
        this.updatedAt = partida.getUpdatedAt() != null ? partida.getUpdatedAt().toString() : null;
        this.createdBy = partida.getCreatedBy();
        this.updatedBy = partida.getUpdatedBy();

        for (DetallePartidaModel detalle : detalles) {
            this.detalles.add(new DetalleResponseDTO(detalle));
        }
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getIdPeriodo() {
        return idPeriodo;
    }

    public void setIdPeriodo(String idPeriodo) {
        this.idPeriodo = idPeriodo;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public List<DetalleResponseDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleResponseDTO> detalles) {
        this.detalles = detalles;
    }

    /**
     * Clase interna para los detalles.
     */
    public static class DetalleResponseDTO {
        private Long id;
        private String idCuenta;
        private String descripcion;
        private Double debito;
        private Double credito;

        public DetalleResponseDTO() {}

        public DetalleResponseDTO(DetallePartidaModel detalle) {
            this.id = detalle.getId();
            this.idCuenta = detalle.getIdCuenta();
            this.descripcion = detalle.getDescripcion();
            this.debito = detalle.getDebito();
            this.credito = detalle.getCredito();
        }

        // Getters y Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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
}
