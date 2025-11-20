package com.ues.sic.tipoDocumentos;

import jakarta.persistence.*;

@Entity
@Table(name = "tipos_documento", schema = "public")
public class TipoDocumentoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo")
    private Integer idTipo;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "creado_por", nullable = false)
    private Integer creadoPor; // id del usuario que lo creó

    // ==== Getters y Setters ====
    public Integer getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(Integer idTipo) {
        this.idTipo = idTipo;
    }

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

