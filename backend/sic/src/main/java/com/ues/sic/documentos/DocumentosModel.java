package com.ues.sic.documentos;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documentos", schema = "public")
public class DocumentosModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento")
    private Integer idDocumento;

    @Column(name = "id_tipo", nullable = false)
    private Integer idTipo;

    @Column(name = "id_partida", nullable = false)
    private Integer idPartida;

    // Aquí puedes guardar la ruta del archivo o el contenido (Base64, etc.)
    @Column(name = "archivo", nullable = false, columnDefinition = "text")
    private String archivo;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "tamano")
    private Integer tamano;

    @Column(name = "fecha_subida", nullable = false)
    private LocalDateTime fechaSubida;

    @Column(name = "subido_por", nullable = false)
    private Integer subidoPor;

    // ====== Getters y Setters ======

    public Integer getIdDocumento() {
        return idDocumento;
    }

    public void setIdDocumento(Integer idDocumento) {
        this.idDocumento = idDocumento;
    }

    public Integer getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(Integer idTipo) {
        this.idTipo = idTipo;
    }

    public Integer getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(Integer idPartida) {
        this.idPartida = idPartida;
    }

    public String getArchivo() {
        return archivo;
    }

    public void setArchivo(String archivo) {
        this.archivo = archivo;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Integer getTamano() {
        return tamano;
    }

    public void setTamano(Integer tamano) {
        this.tamano = tamano;
    }

    public LocalDateTime getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(LocalDateTime fechaSubida) {
        this.fechaSubida = fechaSubida;
    }

    public Integer getSubidoPor() {
        return subidoPor;
    }

    public void setSubidoPor(Integer subidoPor) {
        this.subidoPor = subidoPor;
    }
}
