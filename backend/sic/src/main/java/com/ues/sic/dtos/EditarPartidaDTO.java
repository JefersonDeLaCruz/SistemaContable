package com.ues.sic.dtos;

import com.ues.sic.detalle_partida.DetallePartidaModel;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para editar una partida contable existente.
 * Incluye campos de auditoría (razón del cambio).
 */
public class EditarPartidaDTO {

    @NotNull
    private Long id;

    @NotEmpty
    @NotNull
    private String descripcion;

    @NotEmpty
    @NotNull
    private String fecha;

    @NotEmpty
    @NotNull
    private String idPeriodo;

    @NotEmpty
    @NotNull
    private String razonCambio; // Campo obligatorio para auditoría

    private List<DetalleEditarDTO> detalles = new ArrayList<>();

    // Constructor vacío
    public EditarPartidaDTO() {}

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

    public String getRazonCambio() {
        return razonCambio;
    }

    public void setRazonCambio(String razonCambio) {
        this.razonCambio = razonCambio;
    }

    public List<DetalleEditarDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleEditarDTO> detalles) {
        this.detalles = detalles;
    }

    /**
     * Convierte los detalles del DTO a modelos de DetallePartida.
     */
    public List<DetallePartidaModel> getDetallesAsModel() {
        List<DetallePartidaModel> detallesList = new ArrayList<>();
        for (DetalleEditarDTO detalleDTO : this.detalles) {
            DetallePartidaModel detalle = new DetallePartidaModel();
            detalle.setIdCuenta(detalleDTO.getIdCuenta());
            detalle.setDescripcion(detalleDTO.getDescripcion());
            detalle.setDebito(detalleDTO.getDebito());
            detalle.setCredito(detalleDTO.getCredito());
            detallesList.add(detalle);
        }
        return detallesList;
    }

    /**
     * Clase interna para los detalles de la partida editada.
     */
    public static class DetalleEditarDTO {

        @NotEmpty
        @NotNull
        private String idCuenta;

        @NotEmpty
        @NotNull
        private String descripcion;

        @NotNull
        private Double debito = 0.0;

        @NotNull
        private Double credito = 0.0;

        public DetalleEditarDTO() {}

        // Getters y Setters
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
            return debito != null ? debito : 0.0;
        }

        public void setDebito(Double debito) {
            this.debito = debito != null ? debito : 0.0;
        }

        public Double getCredito() {
            return credito != null ? credito : 0.0;
        }

        public void setCredito(Double credito) {
            this.credito = credito != null ? credito : 0.0;
        }
    }
}
