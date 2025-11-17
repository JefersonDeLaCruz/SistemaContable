package com.ues.sic.balances.patrimonio;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para el Estado de Cambios en el Patrimonio Neto
 * Muestra los movimientos del capital contable durante un período
 */
public class EstadoCambiosPatrimonioDTO {
    
    private PeriodoDTO periodo;
    private List<CuentaPatrimonioDTO> cuentas;
    private TotalesPatrimonioDTO totales;
    
    public EstadoCambiosPatrimonioDTO() {
        this.cuentas = new ArrayList<>();
        this.totales = new TotalesPatrimonioDTO();
    }
    
    // Getters y Setters
    public PeriodoDTO getPeriodo() {
        return periodo;
    }
    
    public void setPeriodo(PeriodoDTO periodo) {
        this.periodo = periodo;
    }
    
    public List<CuentaPatrimonioDTO> getCuentas() {
        return cuentas;
    }
    
    public void setCuentas(List<CuentaPatrimonioDTO> cuentas) {
        this.cuentas = cuentas;
    }
    
    public TotalesPatrimonioDTO getTotales() {
        return totales;
    }
    
    public void setTotales(TotalesPatrimonioDTO totales) {
        this.totales = totales;
    }
    
    /**
     * Información del período
     */
    public static class PeriodoDTO {
        private String nombre;
        private String inicio;
        private String fin;
        
        public PeriodoDTO() {}
        
        public PeriodoDTO(String nombre, String inicio, String fin) {
            this.nombre = nombre;
            this.inicio = inicio;
            this.fin = fin;
        }
        
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getInicio() { return inicio; }
        public void setInicio(String inicio) { this.inicio = inicio; }
        public String getFin() { return fin; }
        public void setFin(String fin) { this.fin = fin; }
    }
    
    /**
     * Movimientos de una cuenta de patrimonio
     */
    public static class CuentaPatrimonioDTO {
        private String codigo;
        private String nombre;
        private double saldoInicial;
        private double aumentos;
        private double disminuciones;
        private double utilidadPeriodo; // Solo para resultado del ejercicio
        private double saldoFinal;
        
        public CuentaPatrimonioDTO() {}
        
        public CuentaPatrimonioDTO(String codigo, String nombre) {
            this.codigo = codigo;
            this.nombre = nombre;
        }
        
        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public double getSaldoInicial() { return saldoInicial; }
        public void setSaldoInicial(double saldoInicial) { this.saldoInicial = saldoInicial; }
        public double getAumentos() { return aumentos; }
        public void setAumentos(double aumentos) { this.aumentos = aumentos; }
        public double getDisminuciones() { return disminuciones; }
        public void setDisminuciones(double disminuciones) { this.disminuciones = disminuciones; }
        public double getUtilidadPeriodo() { return utilidadPeriodo; }
        public void setUtilidadPeriodo(double utilidadPeriodo) { this.utilidadPeriodo = utilidadPeriodo; }
        public double getSaldoFinal() { return saldoFinal; }
        public void setSaldoFinal(double saldoFinal) { this.saldoFinal = saldoFinal; }
    }
    
    /**
     * Totales del patrimonio
     */
    public static class TotalesPatrimonioDTO {
        private double saldoInicial;
        private double aumentos;
        private double disminuciones;
        private double utilidadPeriodo;
        private double saldoFinal;
        
        public double getSaldoInicial() { return saldoInicial; }
        public void setSaldoInicial(double saldoInicial) { this.saldoInicial = saldoInicial; }
        public double getAumentos() { return aumentos; }
        public void setAumentos(double aumentos) { this.aumentos = aumentos; }
        public double getDisminuciones() { return disminuciones; }
        public void setDisminuciones(double disminuciones) { this.disminuciones = disminuciones; }
        public double getUtilidadPeriodo() { return utilidadPeriodo; }
        public void setUtilidadPeriodo(double utilidadPeriodo) { this.utilidadPeriodo = utilidadPeriodo; }
        public double getSaldoFinal() { return saldoFinal; }
        public void setSaldoFinal(double saldoFinal) { this.saldoFinal = saldoFinal; }
    }
}
