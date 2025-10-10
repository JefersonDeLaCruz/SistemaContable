package com.ues.sic.periodos;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "periodos_contables", schema = "public")
public class PeriodoContableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_periodo")
    private Integer idPeriodo;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "frecuencia", nullable = false)
    private String frecuencia;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "cerrado", nullable = false)
    private Boolean cerrado;

    // Constructor vacío
    public PeriodoContableModel() {}

    // Constructor con parámetros
    public PeriodoContableModel(String nombre, String frecuencia, LocalDate fechaInicio, LocalDate fechaFin, Boolean cerrado) {
        this.nombre = nombre;
        this.frecuencia = frecuencia;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.cerrado = cerrado;
    }

    // Getters y Setters
    public Integer getIdPeriodo() {
        return idPeriodo;
    }

    public void setIdPeriodo(Integer idPeriodo) {
        this.idPeriodo = idPeriodo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(String frecuencia) {
        this.frecuencia = frecuencia;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Boolean getCerrado() {
        return cerrado;
    }

    public void setCerrado(Boolean cerrado) {
        this.cerrado = cerrado;
    }

    // Método para insertar un nuevo periodo contable
    public static PeriodoContableModel insertarPeriodo(PeriodoContableRepository repository, String nombre, String frecuencia, LocalDate fechaInicio, LocalDate fechaFin, Boolean cerrado) {
        PeriodoContableModel periodo = new PeriodoContableModel(nombre, frecuencia, fechaInicio, fechaFin, cerrado);
        return repository.save(periodo);
    }
}
