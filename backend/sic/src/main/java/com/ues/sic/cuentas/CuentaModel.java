package com.ues.sic.cuentas;

import jakarta.persistence.*;

@Entity
@Table(name = "cuentas", schema = "public")
public class CuentaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuenta")
    private Integer idCuenta;

    @Column(nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String tipo;

    @Column(name = "saldo_normal", nullable = false)
    private String saldoNormal;

    @Column(name = "id_padre")
    private Integer idPadre; // Id de la cuenta padre

    // Constructor vacío
    public CuentaModel() {}

    // Constructor con parámetros
    public CuentaModel(String codigo, String nombre, String tipo, String saldoNormal, Integer idPadre) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.saldoNormal = saldoNormal;
        this.idPadre = idPadre;
    }

    // Getters y Setters
    public Integer getIdCuenta() {
        return idCuenta;
    }

    public void setIdCuenta(Integer idCuenta) {
        this.idCuenta = idCuenta;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getSaldoNormal() {
        return saldoNormal;
    }

    public void setSaldoNormal(String saldoNormal) {
        this.saldoNormal = saldoNormal;
    }

    public Integer getIdPadre() {
        return idPadre;
    }

    public void setIdPadre(Integer idPadre) {
        this.idPadre = idPadre;
    }

    // Método para insertar (usando el repositorio)
    public static CuentaModel insertar(CuentaRepository repository, String codigo, String nombre, String tipo, String saldoNormal, Integer idPadre) {
        CuentaModel cuenta = new CuentaModel(codigo, nombre, tipo, saldoNormal, idPadre);
        return repository.save(cuenta);
    }
}
