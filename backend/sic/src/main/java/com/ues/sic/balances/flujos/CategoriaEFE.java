package com.ues.sic.balances.flujos;

/**
 * Categorías para clasificar cuentas en el Estado de Flujos de Efectivo
 */
public enum CategoriaEFE {
    
    /**
     * Cuentas de efectivo y equivalentes (Caja, Bancos)
     */
    EFECTIVO,
    
    /**
     * Cuentas que afectan actividades de operación
     * - Activo corriente (excepto efectivo): Inventarios, Cuentas por cobrar, etc.
     * - Pasivo corriente: Proveedores, Cuentas por pagar, etc.
     * - Ingresos y Gastos operativos
     */
    OPERACION,
    
    /**
     * Cuentas que afectan actividades de inversión
     * - Activos fijos: Terrenos, Edificios, Equipos, Vehículos
     * - Inversiones a largo plazo
     * - Depreciación acumulada
     */
    INVERSION,
    
    /**
     * Cuentas que afectan actividades de financiamiento
     * - Préstamos (recibidos/pagados)
     * - Capital social y aportes
     * - Dividendos
     * - Pasivos a largo plazo
     */
    FINANCIAMIENTO,
    
    /**
     * Cuentas que no generan flujo de efectivo
     * - Depreciación
     * - Amortización
     * - Provisiones
     */
    NO_FLUJO,
    
    /**
     * Categoría no determinada o desconocida
     */
    INDETERMINADA
}
