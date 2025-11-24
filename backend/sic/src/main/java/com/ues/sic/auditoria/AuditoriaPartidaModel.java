package com.ues.sic.auditoria;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Modelo de auditoría para registrar todos los cambios realizados en partidas contables.
 * Cumple con requisitos de trazabilidad contable y auditoría.
 */
@Entity
@Table(name = "auditoria_partidas")
public class AuditoriaPartidaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "partida_id", nullable = false)
    private Long partidaId;

    @NotNull
    @Column(name = "operacion", nullable = false, length = 50)
    private String operacion; // CREATE, UPDATE, DELETE

    @NotNull
    @Column(name = "usuario_id", nullable = false)
    private String usuarioId;

    @NotNull
    @Column(name = "fecha_cambio", nullable = false)
    private LocalDateTime fechaCambio;

    @Column(name = "valores_anteriores", columnDefinition = "TEXT")
    private String valoresAnteriores; // JSON con los valores antes del cambio

    @Column(name = "valores_nuevos", columnDefinition = "TEXT")
    private String valoresNuevos; // JSON con los valores después del cambio

    @Column(name = "razon_cambio", length = 500)
    private String razonCambio;

    @Column(name = "ip_origen", length = 50)
    private String ipOrigen;

    // Constructores
    public AuditoriaPartidaModel() {
        this.fechaCambio = LocalDateTime.now();
    }

    public AuditoriaPartidaModel(Long partidaId, String operacion, String usuarioId) {
        this.partidaId = partidaId;
        this.operacion = operacion;
        this.usuarioId = usuarioId;
        this.fechaCambio = LocalDateTime.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getPartidaId() {
        return partidaId;
    }

    public String getOperacion() {
        return operacion;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public LocalDateTime getFechaCambio() {
        return fechaCambio;
    }

    public String getValoresAnteriores() {
        return valoresAnteriores;
    }

    public String getValoresNuevos() {
        return valoresNuevos;
    }

    public String getRazonCambio() {
        return razonCambio;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setPartidaId(Long partidaId) {
        this.partidaId = partidaId;
    }

    public void setOperacion(String operacion) {
        this.operacion = operacion;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public void setFechaCambio(LocalDateTime fechaCambio) {
        this.fechaCambio = fechaCambio;
    }

    public void setValoresAnteriores(String valoresAnteriores) {
        this.valoresAnteriores = valoresAnteriores;
    }

    public void setValoresNuevos(String valoresNuevos) {
        this.valoresNuevos = valoresNuevos;
    }

    public void setRazonCambio(String razonCambio) {
        this.razonCambio = razonCambio;
    }

    public void setIpOrigen(String ipOrigen) {
        this.ipOrigen = ipOrigen;
    }
}
