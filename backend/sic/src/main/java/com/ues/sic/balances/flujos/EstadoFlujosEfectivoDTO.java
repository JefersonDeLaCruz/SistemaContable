package com.ues.sic.balances.flujos;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para el Estado de Flujos de Efectivo
 * Estructura completa según NIIF/US GAAP - Método Indirecto
 */
public class EstadoFlujosEfectivoDTO {
    
    private PeriodoDTO periodo;
    private double saldoInicial;
    private SeccionOperacionDTO operacion;
    private SeccionInversionDTO inversion;
    private SeccionFinanciamientoDTO financiamiento;
    private double aumentoNetoEfectivo;
    private double saldoFinal;
    private boolean cuadra;

    // Constructores
    public EstadoFlujosEfectivoDTO() {
        this.operacion = new SeccionOperacionDTO();
        this.inversion = new SeccionInversionDTO();
        this.financiamiento = new SeccionFinanciamientoDTO();
    }

    // DTO para el período
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

        // Getters y Setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getInicio() { return inicio; }
        public void setInicio(String inicio) { this.inicio = inicio; }
        public String getFin() { return fin; }
        public void setFin(String fin) { this.fin = fin; }
    }

    // DTO para detalle de movimiento
    public static class DetalleMovimientoDTO {
        private String codigo;
        private String nombre;
        private double monto;

        public DetalleMovimientoDTO() {}

        public DetalleMovimientoDTO(String codigo, String nombre, double monto) {
            this.codigo = codigo;
            this.nombre = nombre;
            this.monto = monto;
        }

        // Getters y Setters
        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public double getMonto() { return monto; }
        public void setMonto(double monto) { this.monto = monto; }
    }

    /**
     * Sección de Actividades de Operación (Método Indirecto)
     */
    public static class SeccionOperacionDTO {
        // Resultado del período como base
        private double utilidadNeta;
        
        // Ajustes por partidas que no generan flujo
        private List<DetalleMovimientoDTO> ajustesNoEfectivo;
        private double totalAjustesNoEfectivo;
        
        // Cambios en capital de trabajo
        private List<DetalleMovimientoDTO> cambiosCapitalTrabajo;
        private double totalCambiosCapitalTrabajo;
        
        // Flujo neto de operación
        private double flujoNetoOperacion;

        public SeccionOperacionDTO() {
            this.ajustesNoEfectivo = new ArrayList<>();
            this.cambiosCapitalTrabajo = new ArrayList<>();
        }

        // Getters y Setters
        public double getUtilidadNeta() { return utilidadNeta; }
        public void setUtilidadNeta(double utilidadNeta) { this.utilidadNeta = utilidadNeta; }
        public List<DetalleMovimientoDTO> getAjustesNoEfectivo() { return ajustesNoEfectivo; }
        public void setAjustesNoEfectivo(List<DetalleMovimientoDTO> ajustesNoEfectivo) { this.ajustesNoEfectivo = ajustesNoEfectivo; }
        public double getTotalAjustesNoEfectivo() { return totalAjustesNoEfectivo; }
        public void setTotalAjustesNoEfectivo(double totalAjustesNoEfectivo) { this.totalAjustesNoEfectivo = totalAjustesNoEfectivo; }
        public List<DetalleMovimientoDTO> getCambiosCapitalTrabajo() { return cambiosCapitalTrabajo; }
        public void setCambiosCapitalTrabajo(List<DetalleMovimientoDTO> cambiosCapitalTrabajo) { this.cambiosCapitalTrabajo = cambiosCapitalTrabajo; }
        public double getTotalCambiosCapitalTrabajo() { return totalCambiosCapitalTrabajo; }
        public void setTotalCambiosCapitalTrabajo(double totalCambiosCapitalTrabajo) { this.totalCambiosCapitalTrabajo = totalCambiosCapitalTrabajo; }
        public double getFlujoNetoOperacion() { return flujoNetoOperacion; }
        public void setFlujoNetoOperacion(double flujoNetoOperacion) { this.flujoNetoOperacion = flujoNetoOperacion; }
    }

    /**
     * Sección de Actividades de Inversión
     */
    public static class SeccionInversionDTO {
        private List<DetalleMovimientoDTO> adquisiciones;
        private List<DetalleMovimientoDTO> ventas;
        private double totalAdquisiciones;
        private double totalVentas;
        private double flujoNetoInversion;

        public SeccionInversionDTO() {
            this.adquisiciones = new ArrayList<>();
            this.ventas = new ArrayList<>();
        }

        // Getters y Setters
        public List<DetalleMovimientoDTO> getAdquisiciones() { return adquisiciones; }
        public void setAdquisiciones(List<DetalleMovimientoDTO> adquisiciones) { this.adquisiciones = adquisiciones; }
        public List<DetalleMovimientoDTO> getVentas() { return ventas; }
        public void setVentas(List<DetalleMovimientoDTO> ventas) { this.ventas = ventas; }
        public double getTotalAdquisiciones() { return totalAdquisiciones; }
        public void setTotalAdquisiciones(double totalAdquisiciones) { this.totalAdquisiciones = totalAdquisiciones; }
        public double getTotalVentas() { return totalVentas; }
        public void setTotalVentas(double totalVentas) { this.totalVentas = totalVentas; }
        public double getFlujoNetoInversion() { return flujoNetoInversion; }
        public void setFlujoNetoInversion(double flujoNetoInversion) { this.flujoNetoInversion = flujoNetoInversion; }
    }

    /**
     * Sección de Actividades de Financiamiento
     */
    public static class SeccionFinanciamientoDTO {
        private List<DetalleMovimientoDTO> entradas;
        private List<DetalleMovimientoDTO> salidas;
        private double totalEntradas;
        private double totalSalidas;
        private double flujoNetoFinanciamiento;

        public SeccionFinanciamientoDTO() {
            this.entradas = new ArrayList<>();
            this.salidas = new ArrayList<>();
        }

        // Getters y Setters
        public List<DetalleMovimientoDTO> getEntradas() { return entradas; }
        public void setEntradas(List<DetalleMovimientoDTO> entradas) { this.entradas = entradas; }
        public List<DetalleMovimientoDTO> getSalidas() { return salidas; }
        public void setSalidas(List<DetalleMovimientoDTO> salidas) { this.salidas = salidas; }
        public double getTotalEntradas() { return totalEntradas; }
        public void setTotalEntradas(double totalEntradas) { this.totalEntradas = totalEntradas; }
        public double getTotalSalidas() { return totalSalidas; }
        public void setTotalSalidas(double totalSalidas) { this.totalSalidas = totalSalidas; }
        public double getFlujoNetoFinanciamiento() { return flujoNetoFinanciamiento; }
        public void setFlujoNetoFinanciamiento(double flujoNetoFinanciamiento) { this.flujoNetoFinanciamiento = flujoNetoFinanciamiento; }
    }

    // Getters y Setters principales
    public PeriodoDTO getPeriodo() { return periodo; }
    public void setPeriodo(PeriodoDTO periodo) { this.periodo = periodo; }
    public double getSaldoInicial() { return saldoInicial; }
    public void setSaldoInicial(double saldoInicial) { this.saldoInicial = saldoInicial; }
    public SeccionOperacionDTO getOperacion() { return operacion; }
    public void setOperacion(SeccionOperacionDTO operacion) { this.operacion = operacion; }
    public SeccionInversionDTO getInversion() { return inversion; }
    public void setInversion(SeccionInversionDTO inversion) { this.inversion = inversion; }
    public SeccionFinanciamientoDTO getFinanciamiento() { return financiamiento; }
    public void setFinanciamiento(SeccionFinanciamientoDTO financiamiento) { this.financiamiento = financiamiento; }
    public double getAumentoNetoEfectivo() { return aumentoNetoEfectivo; }
    public void setAumentoNetoEfectivo(double aumentoNetoEfectivo) { this.aumentoNetoEfectivo = aumentoNetoEfectivo; }
    public double getSaldoFinal() { return saldoFinal; }
    public void setSaldoFinal(double saldoFinal) { this.saldoFinal = saldoFinal; }
    public boolean isCuadra() { return cuadra; }
    public void setCuadra(boolean cuadra) { this.cuadra = cuadra; }
}
