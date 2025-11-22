package com.ues.sic.balances.flujos;

import com.ues.sic.cuentas.CuentaModel;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para clasificar cuentas contables según su categoría en el Estado de Flujos de Efectivo
 * Implementa la lógica de clasificación basada en NIIF/US GAAP
 */
@Service
public class ClasificadorCuentasEFE {

    // Cache para evitar clasificar la misma cuenta múltiples veces
    private final Map<String, CategoriaEFE> cacheClasificacion = new HashMap<>();

    /**
     * Clasifica una cuenta según su categoría en el EFE
     * 
     * @param cuenta Objeto CuentaModel a clasificar
     * @return Categoría EFE correspondiente
     */
    public CategoriaEFE clasificarCuenta(CuentaModel cuenta) {
        if (cuenta == null) {
            return CategoriaEFE.INDETERMINADA;
        }

        String codigo = cuenta.getCodigo();
        
        // Verificar cache
        if (cacheClasificacion.containsKey(codigo)) {
            return cacheClasificacion.get(codigo);
        }

        CategoriaEFE categoria = determinarCategoria(cuenta);
        cacheClasificacion.put(codigo, categoria);
        
        return categoria;
    }

    /**
     * Determina la categoría de una cuenta según su código y tipo
     */
    private CategoriaEFE determinarCategoria(CuentaModel cuenta) {
        String codigo = cuenta.getCodigo();
        String tipo = cuenta.getTipo();
        String nombre = cuenta.getNombre().toUpperCase();

        // 1. EFECTIVO Y EQUIVALENTES
        if (esEfectivo(codigo, nombre)) {
            return CategoriaEFE.EFECTIVO;
        }

        // 2. ACTIVIDADES DE INVERSIÓN (Activos no corrientes)
        if (esInversion(codigo, tipo, nombre)) {
            return CategoriaEFE.INVERSION;
        }

        // 3. ACTIVIDADES DE FINANCIAMIENTO
        if (esFinanciamiento(codigo, tipo, nombre)) {
            return CategoriaEFE.FINANCIAMIENTO;
        }

        // 4. PARTIDAS QUE NO GENERAN FLUJO (Depreciación, amortización, provisiones)
        if (esNoFlujo(codigo, nombre)) {
            return CategoriaEFE.NO_FLUJO;
        }

        // 5. ACTIVIDADES DE OPERACIÓN (por defecto: activo/pasivo corriente, ingresos, gastos)
        if (esOperacion(codigo, tipo)) {
            return CategoriaEFE.OPERACION;
        }

        return CategoriaEFE.INDETERMINADA;
    }

    /**
     * Verifica si la cuenta es efectivo o equivalente
     */
    private boolean esEfectivo(String codigo, String nombre) {
        // Códigos típicos: 1.1.01 (Caja), 1.1.02 (Caja Chica), 1.1.03 (Bancos), etc.
        if (codigo.matches("1\\.1\\.(01|02|03|04|05)")) {
            return true;
        }

        // Por nombre
        return nombre.contains("CAJA") || 
               nombre.contains("BANCO") || 
               nombre.contains("EFECTIVO") ||
               nombre.contains("CHEQUE");
    }

    /**
     * Verifica si la cuenta pertenece a actividades de inversión
     */
    private boolean esInversion(String codigo, String tipo, String nombre) {
        // Activos fijos y no corrientes (excepto depreciación acumulada)
        if ("ACTIVO".equals(tipo)) {
            // Código 1.2.XX = Activo No Corriente
            if (codigo.startsWith("1.2.")) {
                // Excluir depreciación acumulada (es NO_FLUJO)
                if (nombre.contains("DEPRECIACION") || nombre.contains("DEPRECIACIÓN")) {
                    return false;
                }
                return true;
            }

            // Verificar por nombre
            if (nombre.contains("TERRENO") || 
                nombre.contains("EDIFICIO") || 
                nombre.contains("MOBILIARIO") || 
                nombre.contains("EQUIPO") ||
                nombre.contains("VEHICULO") || 
                nombre.contains("VEHÍCULO") ||
                nombre.contains("MAQUINARIA") ||
                nombre.contains("INVERSION A LARGO PLAZO") ||
                nombre.contains("INVERSIÓN A LARGO PLAZO")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Verifica si la cuenta pertenece a actividades de financiamiento
     */
    private boolean esFinanciamiento(String codigo, String tipo, String nombre) {
        // Capital y préstamos
        if ("CAPITAL CONTABLE".equals(tipo)) {
            // Todo el capital excepto resultado del ejercicio (que es de operación)
            if (!nombre.contains("RESULTADO")) {
                return true;
            }
        }

        // Pasivos a largo plazo (2.2.XX)
        if ("PASIVO".equals(tipo) && codigo.startsWith("2.2.")) {
            return true;
        }

        // Verificar por nombre - incluye explícitamente RETIROS
        if (nombre.contains("PRESTAMO") || 
            nombre.contains("PRÉSTAMO") ||
            nombre.contains("FINANCIAMIENTO") ||
            nombre.contains("CAPITAL SOCIAL") ||
            nombre.contains("APORTE") ||
            nombre.contains("RETIRO") ||  // AGREGADO: captura retiros del propietario
            nombre.contains("DIVIDENDO")) {
            return true;
        }

        return false;
    }

    /**
     * Verifica si la cuenta NO genera flujo de efectivo
     */
    private boolean esNoFlujo(String codigo, String nombre) {
        // Depreciación, amortización, provisiones
        return nombre.contains("DEPRECIACION") || 
               nombre.contains("DEPRECIACIÓN") ||
               nombre.contains("AMORTIZACION") || 
               nombre.contains("AMORTIZACIÓN") ||
               nombre.contains("PROVISION") || 
               nombre.contains("PROVISIÓN");
    }

    /**
     * Verifica si la cuenta pertenece a actividades de operación
     */
    private boolean esOperacion(String codigo, String tipo) {
        // Activo corriente (excepto efectivo, ya clasificado antes)
        if ("ACTIVO".equals(tipo) && codigo.startsWith("1.1.")) {
            return true;
        }

        // Pasivo corriente (excepto los de financiamiento ya clasificados)
        if ("PASIVO".equals(tipo) && codigo.startsWith("2.1.")) {
            return true;
        }

        // Ingresos (código 4.XX)
        if ("INGRESO".equals(tipo) || codigo.startsWith("4")) {
            return true;
        }

        // Gastos (código 5.XX, 6.XX, 7.XX)
        if ("GASTO".equals(tipo) || codigo.startsWith("5") || 
            codigo.startsWith("6") || codigo.startsWith("7")) {
            return true;
        }

        return false;
    }

    /**
     * Limpia el cache de clasificación
     */
    public void limpiarCache() {
        cacheClasificacion.clear();
    }

    /**
     * Obtiene información de diagnóstico sobre la clasificación de una cuenta
     */
    public String obtenerInfoClasificacion(CuentaModel cuenta) {
        CategoriaEFE categoria = clasificarCuenta(cuenta);
        return String.format("Cuenta: %s - %s | Tipo: %s | Categoría EFE: %s", 
            cuenta.getCodigo(), 
            cuenta.getNombre(), 
            cuenta.getTipo(), 
            categoria);
    }
}
