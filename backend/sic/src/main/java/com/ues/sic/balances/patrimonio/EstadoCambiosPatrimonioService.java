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
 */
@Service
public class EstadoCambiosPatrimonioService {

    @Autowired
    private DetallePartidaRepository detalleRepo;

    @Autowired
    private PeriodoContableRepository periodoRepo;

    /**
     * Calcula el Estado de Cambios en el Patrimonio para un período específico
     * 
     * @param periodoId ID del período contable
     * @return DTO con el estado de cambios en el patrimonio
     */
    public EstadoCambiosPatrimonioDTO calcularCambiosPatrimonio(Integer periodoId) {
        // Obtener período
        Optional<PeriodoContableModel> periodoOpt = periodoRepo.findById(periodoId);
        if (periodoOpt.isEmpty()) {
            throw new IllegalArgumentException("Período no encontrado: " + periodoId);
        }

        PeriodoContableModel periodo = periodoOpt.get();
        EstadoCambiosPatrimonioDTO resultado = new EstadoCambiosPatrimonioDTO();
        
        String sInicio = periodo.getFechaInicio().toString();
        String sFin = periodo.getFechaFin().toString();
        String sDiaAntes = periodo.getFechaInicio().minusDays(1).toString();

        System.out.println("=== DEBUG PATRIMONIO ===");
        System.out.println("Período: " + periodo.getNombre() + " (" + sInicio + " a " + sFin + ")");

        // Configurar período
        PeriodoDTO periodoDTO = new PeriodoDTO(periodo.getNombre(), sInicio, sFin);
        resultado.setPeriodo(periodoDTO);

        // 1. Obtener saldos iniciales de capital contable
        List<Object[]> saldosIniciales = detalleRepo.saldosHastaTodos(sDiaAntes);
        Map<String, CuentaPatrimonioDTO> cuentasMap = new HashMap<>();

        for (Object[] r : saldosIniciales) {
            String codigo = (String) r[1];
            String nombre = (String) r[2];
            String saldoNormal = (String) r[3];
            double tDeb = r[4] != null ? ((Number) r[4]).doubleValue() : 0.0;
            double tCre = r[5] != null ? ((Number) r[5]).doubleValue() : 0.0;
            
            // Solo cuentas de capital contable (código 3.X)
            if (!codigo.startsWith("3")) {
                continue;
            }
            
            double saldoInicial = "DEUDOR".equalsIgnoreCase(saldoNormal) 
                ? (tDeb - tCre) 
                : (tCre - tDeb);
            
            if (Math.abs(saldoInicial) >= 0.01) {
                CuentaPatrimonioDTO cuenta = new CuentaPatrimonioDTO(codigo, nombre);
                cuenta.setSaldoInicial(round2(saldoInicial));
                cuentasMap.put(codigo, cuenta);
            }
        }

        // 2. Obtener movimientos del período en capital contable
        List<Object[]> movimientos = detalleRepo.movimientosPorPeriodo(periodoId);
        
        for (Object[] r : movimientos) {
            String codigo = (String) r[1];
            String nombre = (String) r[2];
            double debito = r[3] != null ? ((Number) r[3]).doubleValue() : 0.0;
            double credito = r[4] != null ? ((Number) r[4]).doubleValue() : 0.0;
            
            // Solo cuentas de capital contable
            if (!codigo.startsWith("3")) {
                continue;
            }
            
            CuentaPatrimonioDTO cuenta = cuentasMap.computeIfAbsent(codigo, 
                k -> new CuentaPatrimonioDTO(codigo, nombre));
            
            // Aumentos = créditos (aumentan patrimonio)
            // Disminuciones = débitos (reducen patrimonio)
            cuenta.setAumentos(round2(credito));
            cuenta.setDisminuciones(round2(debito));
        }

        // 3. Calcular utilidad del período
        Double ingresos = detalleRepo.ingresoEntre(sInicio, sFin);
        Double gastos = detalleRepo.gastoEntre(sInicio, sFin);
        double utilidadNeta = (ingresos != null ? ingresos : 0.0) - (gastos != null ? gastos : 0.0);
        
        System.out.println("Utilidad del período: " + utilidadNeta);

        // 4. Agregar resultado del ejercicio como movimiento del patrimonio
        if (Math.abs(utilidadNeta) >= 0.01) {
            CuentaPatrimonioDTO resultadoEjercicio = cuentasMap.computeIfAbsent("3.99", 
                k -> new CuentaPatrimonioDTO("3.99", utilidadNeta >= 0 ? "UTILIDAD DEL EJERCICIO" : "PÉRDIDA DEL EJERCICIO"));
            resultadoEjercicio.setUtilidadPeriodo(round2(utilidadNeta));
        }

        // 5. Calcular saldos finales
        TotalesPatrimonioDTO totales = new TotalesPatrimonioDTO();
        double totalInicial = 0.0;
        double totalAumentos = 0.0;
        double totalDisminuciones = 0.0;
        
        List<CuentaPatrimonioDTO> listaCuentas = new ArrayList<>(cuentasMap.values());
        listaCuentas.sort(Comparator.comparing(CuentaPatrimonioDTO::getCodigo));
        
        for (CuentaPatrimonioDTO cuenta : listaCuentas) {
            double saldoFinal = cuenta.getSaldoInicial() 
                              + cuenta.getAumentos() 
                              - cuenta.getDisminuciones()
                              + cuenta.getUtilidadPeriodo();
            cuenta.setSaldoFinal(round2(saldoFinal));
            
            totalInicial += cuenta.getSaldoInicial();
            totalAumentos += cuenta.getAumentos();
            totalDisminuciones += cuenta.getDisminuciones();
        }
        
        totales.setSaldoInicial(round2(totalInicial));
        totales.setAumentos(round2(totalAumentos));
        totales.setDisminuciones(round2(totalDisminuciones));
        totales.setUtilidadPeriodo(round2(utilidadNeta));
        totales.setSaldoFinal(round2(totalInicial + totalAumentos - totalDisminuciones + utilidadNeta));
        
        resultado.setCuentas(listaCuentas);
        resultado.setTotales(totales);

        System.out.println("Total cuentas patrimonio: " + listaCuentas.size());
        System.out.println("Patrimonio inicial: " + totales.getSaldoInicial());
        System.out.println("Patrimonio final: " + totales.getSaldoFinal());
        System.out.println("=== FIN DEBUG PATRIMONIO ===");

        return resultado;
    }

    /**
     * Redondea a 2 decimales
     */
    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
