
package com.ues.sic.detalle_partida;

import com.ues.sic.partidas.PartidasModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;



@Entity
@Table(name = "detalle_partida")
public class DetallePartidaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_partida", nullable = false)
    private PartidasModel partida;

    @NotNull
    @NotBlank
    @NotEmpty
    @Column(name = "idCuenta")
    private String idCuenta;//de momento como string despues se integra con el code de david

    @NotNull
    @NotBlank
    @NotEmpty
    @Column(name = "descripcion")
    private String descripcion;

    @NotNull
    @Column(name = "debito")
    private Double debito;

    @NotNull
    @Column(name = "credito")
    private Double credito;


    // Getters
    public Long getId() {
        return this.id;
    }

    public PartidasModel getPartida() {
        return this.partida;
    }

    public String getIdCuenta() {
        return this.idCuenta;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public Double getDebito() {
        return this.debito;
    }

    public Double getCredito() {
        return this.credito;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setPartida(PartidasModel partida) {
        this.partida = partida;
    }

    public void setIdCuenta(String idCuenta) {
        this.idCuenta = idCuenta;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setDebito(Double debito) {
        this.debito = debito;
    }

    public void setCredito(Double credito) {
        this.credito = credito;
    }
}