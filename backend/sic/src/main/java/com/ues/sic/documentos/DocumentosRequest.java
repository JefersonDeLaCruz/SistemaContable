package com.ues.sic.documentos;

public class DocumentosRequest {

    private Integer idTipo;
    private Integer idPartida;
    private String archivo;
    private String mimeType;
    private Integer tamano;
    private Integer subidoPor;

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

    public Integer getSubidoPor() {
        return subidoPor;
    }

    public void setSubidoPor(Integer subidoPor) {
        this.subidoPor = subidoPor;
    }
}
