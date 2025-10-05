package com.ues.sic.partidas;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

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
    @Column(name = "idPeriodo")
    private String idPeriodo; //ojo que aqui puse el iD del periodo como un string

    @NotEmpty
    @NotNull
    @Column(name = "idUsuario")
    private String idUsuario;

    // Getters
    public Long getId() {
        return id;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public String getIdPeriodo() {
        return idPeriodo;
    }
    
    public String getIdUsuario() {
        return idUsuario;
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

}
