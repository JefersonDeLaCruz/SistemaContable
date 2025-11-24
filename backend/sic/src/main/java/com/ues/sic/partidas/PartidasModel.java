package com.ues.sic.partidas;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
/* import jakarta.validation.constraints.NotBlank; */
import jakarta.validation.constraints.NotEmpty;
/* import jakarta.validation.constraints.NotNull; */
import java.time.LocalDateTime;

@Entity
@Table(name = "partidas")
public class PartidasModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotEmpty
    @NotNull
    @Column(name = "descripcion")
    private String descripcion;

    @NotEmpty
    @NotNull
    @Column(name = "fecha")
    private String fecha;

    @NotEmpty
    @NotNull
    @Column(name = "idPeriodo")
    private String idPeriodo; //ojo que aqui puse el iD del periodo como un string

    @NotEmpty
    @NotNull
    @Column(name = "idUsuario")
    private String idUsuario;

    // Campos de auditoría
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "estado", length = 20)
    private String estado; // ACTIVA, EDITADA, ELIMINADA

    // Getters
    public Long getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public String getIdPeriodo() {
        return idPeriodo;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public String getEstado() {
        return estado;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setIdPeriodo(String idPeriodo) {
        this.idPeriodo = idPeriodo;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public void setFecha(String string) {
        this.fecha = string;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}
