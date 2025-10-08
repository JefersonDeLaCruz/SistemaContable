package com.ues.sic.dtos;

import com.ues.sic.partidas.PartidasModel;
import com.ues.sic.detalle_partida.DetallePartidaModel;
import java.util.List;
import java.util.ArrayList;

public class PartidaConDetallesDTO {
    
    // Campos de la partida principal
    private String descripcion;
    private String fecha;
    private String idPeriodo;
    private String idUsuario;
    
    // Lista de detalles
    private List<DetallePartidaDTO> detalles = new ArrayList<>();
    
    // Constructor vacío
    public PartidaConDetallesDTO() {}
    
    // Método para convertir a PartidasModel
    public PartidasModel getPartida() {
        PartidasModel partida = new PartidasModel();
        partida.setDescripcion(this.descripcion);
        partida.setFecha(this.fecha);
        partida.setIdPeriodo(this.idPeriodo);
        partida.setIdUsuario(this.idUsuario);
        return partida;
    }
    
    // Método para convertir detalles a DetallePartidaModel
    public List<DetallePartidaModel> getDetallesAsModel() {
        List<DetallePartidaModel> detallesList = new ArrayList<>();
        for (DetallePartidaDTO detalleDTO : this.detalles) {
            DetallePartidaModel detalle = new DetallePartidaModel();
            detalle.setIdCuenta(detalleDTO.getIdCuenta());
            detalle.setDescripcion(detalleDTO.getDescripcion());
            detalle.setDebito(detalleDTO.getDebito());
            detalle.setCredito(detalleDTO.getCredito());
            detallesList.add(detalle);
        }
        return detallesList;
    }
    
    // Getters y Setters
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
    
    public List<DetallePartidaDTO> getDetalles() {
        return detalles;
    }
    
    public void setDetalles(List<DetallePartidaDTO> detalles) {
        this.detalles = detalles;
    }
    
    // Método auxiliar para mantener compatibilidad
    public List<DetallePartidaDTO> getDetallesDTO() {
        return this.detalles;
    }
    
    // Clase interna para los detalles
    public static class DetallePartidaDTO {
        private String idCuenta;
        private String descripcion;
        private Double debito = 0.0;
        private Double credito = 0.0;
        
        public DetallePartidaDTO() {}
        
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
