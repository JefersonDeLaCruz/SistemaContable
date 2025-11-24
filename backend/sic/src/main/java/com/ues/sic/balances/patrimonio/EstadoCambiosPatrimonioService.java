package com.ues.sic.balances.patrimonio;

import com.ues.sic.balances.patrimonio.EstadoCambiosPatrimonioDTO.*;
import com.ues.sic.detalle_partida.DetallePartidaRepository;
import com.ues.sic.periodos.PeriodoContableModel;
import com.ues.sic.periodos.PeriodoContableRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Servicio para calcular el Estado de Cambios en el Patrimonio Neto
 * Muestra los movimientos en las cuentas de capital durante un período
 * 
 * Componentes:
 * - Saldo inicial del patrimonio (períodos anteriores)
 * - Aumentos del patrimonio (aportes, reservas)
 * - Disminuciones del patrimonio (retiros, dividendos)
 * - Resultado del período (utilidad/pérdida del Estado de Resultados)
 * - Saldo final del patrimonio
 */
@Service
public class EstadoCambiosPatrimonioService {

    @Autowired
    private DetallePartidaRepository detalleRepo;

    @Autowired
    private PeriodoContableRepository periodoRepo;

    /**
     * Calcula el Estado de Cambios en el Patrimonio para un período específico
     * Delegado a la versión con múltiples períodos
     * 
     * @param periodoId ID del período contable
     * @return DTO con el estado de cambios en el patrimonio
     */
    public EstadoCambiosPatrimonioDTO calcularCambiosPatrimonio(Integer periodoId) {
        return calcularCambiosPatrimonioConPeriodos(periodoId, Arrays.asList(periodoId));
    }

    /**
     * Calcula el Estado de Cambios en el Patrimonio para múltiples períodos
     * Útil cuando se selecciona un trimestre o año fiscal que agrupa varios períodos
     * 
     * @param periodoId ID del período contable seleccionado
     * @param periodosIncluidos Lista de IDs de períodos a incluir en el cálculo
     * @return DTO con el estado de cambios en el patrimonio
     */
    public EstadoCambiosPatrimonioDTO calcularCambiosPatrimonioConPeriodos(Integer periodoId, List<Integer> periodosIncluidos) {
        // Obtener período principal
        Optional<PeriodoContableModel> periodoOpt = periodoRepo.findById(periodoId);
        if (periodoOpt.isEmpty()) {
            throw new IllegalArgumentException("Período no encontrado: " + periodoId);
        }

        PeriodoContableModel periodo = periodoOpt.get();
        EstadoCambiosPatrimonioDTO resultado = new EstadoCambiosPatrimonioDTO();
        
        String sInicio = periodo.getFechaInicio().toString();
        String sFin = periodo.getFechaFin().toString();

        System.out.println("=== DEBUG PATRIMONIO ===");
        System.out.println("Período: " + periodo.getNombre() + " (" + sInicio + " a " + sFin + ")");
        System.out.println("Períodos incluidos: " + periodosIncluidos);

        // Configurar período
        PeriodoDTO periodoDTO = new PeriodoDTO(periodo.getNombre(), sInicio, sFin);
        resultado.setPeriodo(periodoDTO);

        // PASO 1: Calcular utilidad neta del período (Estado de Resultados)
        double utilidadNeta = calcularUtilidadNetaPorPeriodosMultiples(periodosIncluidos);
        System.out.println("Utilidad del período calculada: " + utilidadNeta);

        // PASO 2: Obtener saldos iniciales de patrimonio (períodos anteriores)
        Map<String, CuentaPatrimonioDTO> cuentasMap = obtenerSaldosInicialesPatrimonio(periodoId);
        System.out.println("Cuentas de patrimonio encontradas: " + cuentasMap.size());

        // PASO 3: Obtener movimientos del período en cuentas de patrimonio (solo 3.X)
        procesarMovimientosPatrimonioPorPeriodos(periodosIncluidos, cuentasMap);

        // PASO 4: Agregar resultado del ejercicio
        if (Math.abs(utilidadNeta) >= 0.01) {
            CuentaPatrimonioDTO resultadoEjercicio = cuentasMap.computeIfAbsent("3.999", 
                k -> new CuentaPatrimonioDTO("3.999", utilidadNeta >= 0 ? "UTILIDAD DEL EJERCICIO" : "PÉRDIDA DEL EJERCICIO"));
            resultadoEjercicio.setUtilidadPeriodo(round2(utilidadNeta));
        }

        // PASO 5: Calcular saldos finales y totales
        TotalesPatrimonioDTO totales = calcularTotales(cuentasMap);
        
        // PASO 6: Ordenar y asignar al resultado
        List<CuentaPatrimonioDTO> listaCuentas = new ArrayList<>(cuentasMap.values());
        listaCuentas.sort(Comparator.comparing(CuentaPatrimonioDTO::getCodigo));
        
        resultado.setCuentas(listaCuentas);
        resultado.setTotales(totales);

        System.out.println("Total cuentas patrimonio: " + listaCuentas.size());
        System.out.println("Patrimonio inicial: " + totales.getSaldoInicial());
        System.out.println("Patrimonio final: " + totales.getSaldoFinal());
        System.out.println("=== FIN DEBUG PATRIMONIO ===");

        return resultado;
    }

    /**
     * Calcula la utilidad neta del período basándose en Estado de Resultados
     * Ingresos (4.x) - Costos (5.x) - Gastos (6.x, 7.x)
     * @deprecated Use calcularUtilidadNetaPorPeriodosMultiples instead
     */
    @Deprecated
    private double calcularUtilidadNetaPeriodo(Integer periodoId) {
        return calcularUtilidadNetaPorPeriodosMultiples(Arrays.asList(periodoId));
    }

    /**
     * Calcula la utilidad neta agregando múltiples períodos
     * Ingresos (4.x) - Costos (5.x) - Gastos (6.x, 7.x)
     */
    private double calcularUtilidadNetaPorPeriodosMultiples(List<Integer> periodosIncluidos) {
        List<Object[]> movimientos = detalleRepo.movimientosPorPeriodos(periodosIncluidos);
        
        double ingresos = 0.0;
        double costosYGastos = 0.0;
        
        for (Object[] r : movimientos) {
            String codigo = (String) r[1];
            double debito = r[3] != null ? ((Number) r[3]).doubleValue() : 0.0;
            double credito = r[4] != null ? ((Number) r[4]).doubleValue() : 0.0;
            
            // Ingresos (4.x): saldo acreedor, por lo tanto crédito - débito
            if (codigo.startsWith("4")) {
                ingresos += (credito - debito);
            }
            // Costos y Gastos (5.x, 6.x, 7.x): saldo deudor, por lo tanto débito - crédito
            else if (codigo.startsWith("5") || codigo.startsWith("6") || codigo.startsWith("7")) {
                costosYGastos += (debito - credito);
            }
        }
        
        double utilidadNeta = ingresos - costosYGastos;
        
        System.out.println("  Ingresos totales: " + ingresos);
        System.out.println("  Costos y gastos totales: " + costosYGastos);
        System.out.println("  Utilidad neta: " + utilidadNeta);
        
        return utilidadNeta;
    }

    /**
     * Obtiene saldos iniciales de cuentas de patrimonio (períodos anteriores)
     * Solo procesa cuentas con código 3.X
     */
    private Map<String, CuentaPatrimonioDTO> obtenerSaldosInicialesPatrimonio(Integer periodoId) {
        List<Object[]> saldosIniciales = detalleRepo.saldosHastaPeriodo(periodoId);
        Map<String, CuentaPatrimonioDTO> cuentasMap = new HashMap<>();

        for (Object[] r : saldosIniciales) {
            String codigo = (String) r[1];
            String nombre = (String) r[2];
            String saldoNormal = (String) r[3];
            double tDeb = r[4] != null ? ((Number) r[4]).doubleValue() : 0.0;
            double tCre = r[5] != null ? ((Number) r[5]).doubleValue() : 0.0;
            
            // SOLO cuentas de patrimonio (código 3.X)
            if (!codigo.startsWith("3")) {
                continue;
            }
            
            // Patrimonio normalmente es acreedor: crédito - débito
            double saldoInicial = "DEUDOR".equalsIgnoreCase(saldoNormal) 
                ? (tDeb - tCre) 
                : (tCre - tDeb);
            
            // Solo agregar si tiene saldo inicial
            if (Math.abs(saldoInicial) >= 0.01) {
                CuentaPatrimonioDTO cuenta = new CuentaPatrimonioDTO(codigo, nombre);
                cuenta.setSaldoInicial(round2(saldoInicial));
                cuentasMap.put(codigo, cuenta);
                System.out.println("  Saldo inicial: " + codigo + " - " + nombre + " = " + saldoInicial);
            }
        }

        return cuentasMap;
    }

    /**
     * Procesa movimientos del período en cuentas de patrimonio
     * Crédito = Aumento del patrimonio
     * Débito = Disminución del patrimonio
     * @deprecated Use procesarMovimientosPatrimonioPorPeriodos instead
     */
    @Deprecated
    private void procesarMovimientosPatrimonio(Integer periodoId, Map<String, CuentaPatrimonioDTO> cuentasMap) {
        procesarMovimientosPatrimonioPorPeriodos(Arrays.asList(periodoId), cuentasMap);
    }

    /**
     * Procesa movimientos de múltiples períodos en cuentas de patrimonio
     * Crédito = Aumento del patrimonio
     * Débito = Disminución del patrimonio
     */
    private void procesarMovimientosPatrimonioPorPeriodos(List<Integer> periodosIncluidos, Map<String, CuentaPatrimonioDTO> cuentasMap) {
        List<Object[]> movimientos = detalleRepo.movimientosPorPeriodos(periodosIncluidos);
        
        for (Object[] r : movimientos) {
            String codigo = (String) r[1];
            String nombre = (String) r[2];
            double debito = r[3] != null ? ((Number) r[3]).doubleValue() : 0.0;
            double credito = r[4] != null ? ((Number) r[4]).doubleValue() : 0.0;
            
            // SOLO cuentas de patrimonio (código 3.X)
            if (!codigo.startsWith("3")) {
                continue;
            }
            
            // Obtener o crear cuenta
            CuentaPatrimonioDTO cuenta = cuentasMap.computeIfAbsent(codigo, 
                k -> new CuentaPatrimonioDTO(codigo, nombre));
            
            // Regla contable para patrimonio:
            // CRÉDITO = AUMENTO del patrimonio (ej: aportes de capital)
            // DÉBITO = DISMINUCIÓN del patrimonio (ej: retiros, dividendos)
            cuenta.setAumentos(round2(cuenta.getAumentos() + credito));
            cuenta.setDisminuciones(round2(cuenta.getDisminuciones() + debito));
            
            System.out.println("  Movimiento: " + codigo + " - Aumentos: +" + credito + ", Disminuciones: -" + debito);
        }
    }

    /**
     * Calcula los totales del estado de cambios en el patrimonio
     */
    private TotalesPatrimonioDTO calcularTotales(Map<String, CuentaPatrimonioDTO> cuentasMap) {
        TotalesPatrimonioDTO totales = new TotalesPatrimonioDTO();
        
        double totalInicial = 0.0;
        double totalAumentos = 0.0;
        double totalDisminuciones = 0.0;
        double totalUtilidad = 0.0;
        
        for (CuentaPatrimonioDTO cuenta : cuentasMap.values()) {
            // Calcular saldo final de cada cuenta
            // Saldo final = Inicial + Aumentos - Disminuciones + Utilidad
            double saldoFinal = cuenta.getSaldoInicial() 
                              + cuenta.getAumentos() 
                              - cuenta.getDisminuciones()
                              + cuenta.getUtilidadPeriodo();
            cuenta.setSaldoFinal(round2(saldoFinal));
            
            // Acumular totales
            totalInicial += cuenta.getSaldoInicial();
            totalAumentos += cuenta.getAumentos();
            totalDisminuciones += cuenta.getDisminuciones();
            totalUtilidad += cuenta.getUtilidadPeriodo();
        }
        
        totales.setSaldoInicial(round2(totalInicial));
        totales.setAumentos(round2(totalAumentos));
        totales.setDisminuciones(round2(totalDisminuciones));
        totales.setUtilidadPeriodo(round2(totalUtilidad));
        totales.setSaldoFinal(round2(totalInicial + totalAumentos - totalDisminuciones + totalUtilidad));
        
        return totales;
    }

    /**
     * Redondea a 2 decimales
     */
    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
