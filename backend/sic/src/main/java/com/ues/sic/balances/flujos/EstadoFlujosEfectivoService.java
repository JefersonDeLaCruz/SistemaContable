package com.ues.sic.balances.flujos;

import com.ues.sic.balances.flujos.EstadoFlujosEfectivoDTO.*;
import com.ues.sic.cuentas.CuentaModel;
import com.ues.sic.cuentas.CuentaRepository;
import com.ues.sic.detalle_partida.DetallePartidaRepository;
import com.ues.sic.periodos.PeriodoContableModel;
import com.ues.sic.periodos.PeriodoContableRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * Servicio para calcular el Estado de Flujos de Efectivo (EFE)
 * Implementa el Método Indirecto según NIIF/US GAAP
 */
@Service
public class EstadoFlujosEfectivoService {

    @Autowired
    private DetallePartidaRepository detalleRepo;

    @Autowired
    private CuentaRepository cuentaRepo;

    @Autowired
    private PeriodoContableRepository periodoRepo;

    @Autowired
    private ClasificadorCuentasEFE clasificador;

    /**
     * Calcula el Estado de Flujos de Efectivo para un período específico
     * 
     * @param periodoId ID del período contable
     * @return DTO con el estado de flujos de efectivo completo
     */
    public EstadoFlujosEfectivoDTO calcularEFE(Integer periodoId) {
        // Obtener período
        Optional<PeriodoContableModel> periodoOpt = periodoRepo.findById(periodoId);
        if (periodoOpt.isEmpty()) {
            throw new IllegalArgumentException("Período no encontrado: " + periodoId);
        }

        PeriodoContableModel periodo = periodoOpt.get();
        
        EstadoFlujosEfectivoDTO efe = new EstadoFlujosEfectivoDTO();
        
        String sInicio = periodo.getFechaInicio().toString();
        String sFin = periodo.getFechaFin().toString();

        System.out.println("=== DEBUG EFE ===");
        System.out.println("Período: " + periodo.getNombre() + " (" + sInicio + " a " + sFin + ")");
        System.out.println("Período ID: " + periodoId);

        // Configurar período
        PeriodoDTO periodoDTO = new PeriodoDTO(periodo.getNombre(), sInicio, sFin);
        efe.setPeriodo(periodoDTO);

        // PASO 1: Calcular saldo inicial y final de efectivo basado en períodos anteriores
        calcularSaldosEfectivoPorPeriodo(efe, periodoId);
        System.out.println("Saldo Inicial Efectivo: " + efe.getSaldoInicial());
        System.out.println("Saldo Final Efectivo: " + efe.getSaldoFinal());

        // PASO 2: Calcular utilidad neta del período
        double utilidadNeta = calcularUtilidadNetaPorPeriodo(periodoId);
        efe.getOperacion().setUtilidadNeta(round2(utilidadNeta));
        System.out.println("Utilidad Neta: " + utilidadNeta);

        // PASO 3: Obtener todas las cuentas y movimientos del período
        System.out.println("Obteniendo movimientos del período ID: " + periodoId);
        List<Object[]> movimientos = detalleRepo.movimientosPorPeriodo(periodoId);
        List<Object[]> saldosIniciales = detalleRepo.saldosHastaPeriodo(periodoId);
        System.out.println("Total movimientos encontrados: " + movimientos.size());
        System.out.println("Total saldos iniciales: " + saldosIniciales.size());

        // Crear mapas de cuentas por categoría
        Map<Integer, CuentaModel> cuentasMap = obtenerMapaCuentas();
        System.out.println("Total cuentas en catálogo: " + cuentasMap.size());
        
        Map<CategoriaEFE, List<MovimientoCuenta>> movimientosPorCategoria = 
            clasificarMovimientos(movimientos, saldosIniciales, cuentasMap);

        // Mostrar clasificación
        for (Map.Entry<CategoriaEFE, List<MovimientoCuenta>> entry : movimientosPorCategoria.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue().size() + " movimientos");
        }

        // PASO 4: Procesar actividades de operación
        procesarActividadesOperacion(efe, movimientosPorCategoria, cuentasMap);

        // PASO 5: Procesar actividades de inversión
        procesarActividadesInversion(efe, movimientosPorCategoria);

        // PASO 6: Procesar actividades de financiamiento
        procesarActividadesFinanciamiento(efe, movimientosPorCategoria);

        // PASO 7: Calcular totales finales
        calcularTotales(efe);

        System.out.println("Flujo Neto Operación: " + efe.getOperacion().getFlujoNetoOperacion());
        System.out.println("Flujo Neto Inversión: " + efe.getInversion().getFlujoNetoInversion());
        System.out.println("Flujo Neto Financiamiento: " + efe.getFinanciamiento().getFlujoNetoFinanciamiento());
        System.out.println("=== FIN DEBUG EFE ===");

        return efe;
    }

    /**
     * Calcula los saldos inicial y final de efectivo basándose en períodos
     */
    private void calcularSaldosEfectivoPorPeriodo(EstadoFlujosEfectivoDTO efe, Integer periodoId) {
        List<Object[]> saldosIniciales = detalleRepo.saldosHastaPeriodo(periodoId);
        
        // Para el saldo final, necesitamos incluir el período actual
        // Podemos calcular: saldo inicial + movimientos del período
        List<Object[]> movimientosPeriodo = detalleRepo.movimientosPorPeriodo(periodoId);
        
        double saldoInicial = 0.0;
        double movimientosEfectivo = 0.0;

        Map<Integer, CuentaModel> cuentasMap = obtenerMapaCuentas();

        // Calcular saldo inicial (períodos anteriores)
        for (Object[] r : saldosIniciales) {
            Integer idCuenta = ((Number) r[0]).intValue();
            CuentaModel cuenta = cuentasMap.get(idCuenta);
            
            if (cuenta != null && clasificador.clasificarCuenta(cuenta) == CategoriaEFE.EFECTIVO) {
                String saldoNormal = (String) r[3];
                double tDeb = r[4] != null ? ((Number) r[4]).doubleValue() : 0.0;
                double tCre = r[5] != null ? ((Number) r[5]).doubleValue() : 0.0;
                double saldo = "DEUDOR".equalsIgnoreCase(saldoNormal) ? (tDeb - tCre) : (tCre - tDeb);
                saldoInicial += saldo;
            }
        }

        // Calcular movimientos de efectivo del período actual
        for (Object[] r : movimientosPeriodo) {
            Integer idCuenta = ((Number) r[0]).intValue();
            CuentaModel cuenta = cuentasMap.get(idCuenta);
            
            if (cuenta != null && clasificador.clasificarCuenta(cuenta) == CategoriaEFE.EFECTIVO) {
                String saldoNormal = cuenta.getSaldoNormal();
                double debito = r[3] != null ? ((Number) r[3]).doubleValue() : 0.0;
                double credito = r[4] != null ? ((Number) r[4]).doubleValue() : 0.0;
                double variacion = "DEUDOR".equalsIgnoreCase(saldoNormal) ? (debito - credito) : (credito - debito);
                movimientosEfectivo += variacion;
            }
        }

        efe.setSaldoInicial(round2(saldoInicial));
        efe.setSaldoFinal(round2(saldoInicial + movimientosEfectivo));
    }

    /**
     * Calcula la utilidad neta del período basándose en el ID del período
     */
    private double calcularUtilidadNetaPorPeriodo(Integer periodoId) {
        // Obtener todas las cuentas de ingresos y gastos del período
        List<Object[]> movimientos = detalleRepo.movimientosPorPeriodo(periodoId);
        Map<Integer, CuentaModel> cuentasMap = obtenerMapaCuentas();
        
        double ingresos = 0.0;
        double gastos = 0.0;
        
        for (Object[] r : movimientos) {
            Integer idCuenta = ((Number) r[0]).intValue();
            CuentaModel cuenta = cuentasMap.get(idCuenta);
            
            if (cuenta != null) {
                String tipo = cuenta.getTipo();
                String codigo = cuenta.getCodigo();
                double debito = r[3] != null ? ((Number) r[3]).doubleValue() : 0.0;
                double credito = r[4] != null ? ((Number) r[4]).doubleValue() : 0.0;
                
                // Ingresos (normalmente acreedor): crédito - débito
                if ("INGRESOS".equalsIgnoreCase(tipo) || codigo.startsWith("4")) {
                    ingresos += (credito - debito);
                }
                // Gastos (normalmente deudor): débito - crédito
                else if ("GASTOS".equalsIgnoreCase(tipo) || codigo.startsWith("5")) {
                    gastos += (debito - credito);
                }
            }
        }
        
        return ingresos - gastos;
    }

    /**
     * Calcula la utilidad neta del período (DEPRECATED - usar calcularUtilidadNetaPorPeriodo)
     */
    @Deprecated
    private double calcularUtilidadNeta(String inicio, String fin) {
        Double ingreso = detalleRepo.ingresoEntre(inicio, fin);
        Double gasto = detalleRepo.gastoEntre(inicio, fin);
        return (ingreso != null ? ingreso : 0.0) - (gasto != null ? gasto : 0.0);
    }

    /**
     * Obtiene un mapa de todas las cuentas indexado por ID
     */
    private Map<Integer, CuentaModel> obtenerMapaCuentas() {
        List<CuentaModel> cuentas = cuentaRepo.findAll();
        Map<Integer, CuentaModel> mapa = new HashMap<>();
        for (CuentaModel cuenta : cuentas) {
            mapa.put(cuenta.getIdCuenta(), cuenta);
        }
        return mapa;
    }

    /**
     * Clasifica los movimientos según su categoría EFE
     */
    private Map<CategoriaEFE, List<MovimientoCuenta>> clasificarMovimientos(
            List<Object[]> movimientos, 
            List<Object[]> saldosIniciales,
            Map<Integer, CuentaModel> cuentasMap) {

        Map<CategoriaEFE, List<MovimientoCuenta>> resultado = new EnumMap<>(CategoriaEFE.class);
        
        // Inicializar listas
        for (CategoriaEFE cat : CategoriaEFE.values()) {
            resultado.put(cat, new ArrayList<>());
        }

        // Crear mapa de saldos iniciales
        Map<Integer, Double> saldosInicialesMap = new HashMap<>();
        for (Object[] r : saldosIniciales) {
            Integer idCuenta = ((Number) r[0]).intValue();
            String saldoNormal = (String) r[3];
            double tDeb = r[4] != null ? ((Number) r[4]).doubleValue() : 0.0;
            double tCre = r[5] != null ? ((Number) r[5]).doubleValue() : 0.0;
            double saldo = "DEUDOR".equalsIgnoreCase(saldoNormal) ? (tDeb - tCre) : (tCre - tDeb);
            saldosInicialesMap.put(idCuenta, saldo);
        }

        // Clasificar movimientos
        for (Object[] r : movimientos) {
            Integer idCuenta = ((Number) r[0]).intValue();
            String codigo = (String) r[1];
            String nombre = (String) r[2];
            double debito = r[3] != null ? ((Number) r[3]).doubleValue() : 0.0;
            double credito = r[4] != null ? ((Number) r[4]).doubleValue() : 0.0;

            CuentaModel cuenta = cuentasMap.get(idCuenta);
            if (cuenta == null) {
                System.out.println("WARNING: Cuenta no encontrada en catálogo - ID: " + idCuenta + ", Código: " + codigo + ", Nombre: " + nombre);
                continue;
            }

            CategoriaEFE categoria = clasificador.clasificarCuenta(cuenta);
            double saldoInicial = saldosInicialesMap.getOrDefault(idCuenta, 0.0);
            
            String saldoNormal = cuenta.getSaldoNormal();
            double variacion = "DEUDOR".equalsIgnoreCase(saldoNormal) 
                ? (debito - credito) 
                : (credito - debito);

            System.out.println("Clasificando: " + codigo + " - " + nombre + " | Categoría: " + categoria + 
                             " | Débito: " + debito + " | Crédito: " + credito + " | Variación: " + variacion);

            MovimientoCuenta mov = new MovimientoCuenta(
                idCuenta, codigo, nombre, debito, credito, 
                saldoInicial, variacion, saldoNormal, categoria
            );

            resultado.get(categoria).add(mov);
        }

        return resultado;
    }

    /**
     * Procesa las actividades de operación (Método Indirecto)
     */
    private void procesarActividadesOperacion(
            EstadoFlujosEfectivoDTO efe,
            Map<CategoriaEFE, List<MovimientoCuenta>> movimientos,
            Map<Integer, CuentaModel> cuentasMap) {

        SeccionOperacionDTO operacion = efe.getOperacion();
        double totalAjustes = 0.0;
        double totalCambiosCapital = 0.0;

        // 1. Ajustes por partidas que no generan flujo (depreciación, etc.)
        List<MovimientoCuenta> noFlujo = movimientos.get(CategoriaEFE.NO_FLUJO);
        for (MovimientoCuenta mov : noFlujo) {
            // La depreciación reduce utilidad pero no es salida de efectivo
            // Por lo tanto, se suma de vuelta
            double ajuste = mov.debito - mov.credito; // Normalmente débito > 0
            if (Math.abs(ajuste) >= 0.01) {
                operacion.getAjustesNoEfectivo().add(
                    new DetalleMovimientoDTO(mov.codigo, mov.nombre, round2(Math.abs(ajuste)))
                );
                totalAjustes += ajuste;
            }
        }
        operacion.setTotalAjustesNoEfectivo(round2(totalAjustes));

        // 2. Cambios en capital de trabajo (activos y pasivos corrientes)
        List<MovimientoCuenta> operativos = movimientos.get(CategoriaEFE.OPERACION);
        for (MovimientoCuenta mov : operativos) {
            CuentaModel cuenta = cuentasMap.get(mov.idCuenta);
            if (cuenta == null) continue;

            String tipo = cuenta.getTipo();
            
            // Saltar cuentas de resultado (ingresos/gastos) ya están en utilidad neta
            if ("INGRESO".equals(tipo) || "GASTO".equals(tipo)) {
                continue;
            }

            // Cambios en activo corriente y pasivo corriente
            double cambio = calcularCambioCapitalTrabajo(mov, tipo);
            
            if (Math.abs(cambio) >= 0.01) {
                String descripcion = cambio > 0 
                    ? "Disminución en " + mov.nombre 
                    : "Aumento en " + mov.nombre;
                
                operacion.getCambiosCapitalTrabajo().add(
                    new DetalleMovimientoDTO(mov.codigo, descripcion, round2(Math.abs(cambio)))
                );
                totalCambiosCapital += cambio;
            }
        }
        operacion.setTotalCambiosCapitalTrabajo(round2(totalCambiosCapital));

        // 3. Calcular flujo neto de operación
        double flujoNeto = operacion.getUtilidadNeta() + totalAjustes + totalCambiosCapital;
        operacion.setFlujoNetoOperacion(round2(flujoNeto));
    }

    /**
     * Calcula el cambio en capital de trabajo
     * Aumentos en activos corrientes = uso de efectivo (negativo)
     * Disminuciones en activos corrientes = fuente de efectivo (positivo)
     * Aumentos en pasivos corrientes = fuente de efectivo (positivo)
     * Disminuciones en pasivos corrientes = uso de efectivo (negativo)
     */
    private double calcularCambioCapitalTrabajo(MovimientoCuenta mov, String tipo) {
        double variacion = mov.variacion;
        
        if ("ACTIVO".equals(tipo)) {
            // Aumento en activo = uso de efectivo (negativo para EFE)
            return -variacion;
        } else if ("PASIVO".equals(tipo)) {
            // Aumento en pasivo = fuente de efectivo (positivo para EFE)
            return variacion;
        }
        
        return 0.0;
    }

    /**
     * Procesa las actividades de inversión
     */
    private void procesarActividadesInversion(
            EstadoFlujosEfectivoDTO efe,
            Map<CategoriaEFE, List<MovimientoCuenta>> movimientos) {

        SeccionInversionDTO inversion = efe.getInversion();
        double totalAdquisiciones = 0.0;
        double totalVentas = 0.0;

        List<MovimientoCuenta> inversionMov = movimientos.get(CategoriaEFE.INVERSION);
        
        for (MovimientoCuenta mov : inversionMov) {
            double variacion = mov.variacion;
            
            if (variacion > 0.01) {
                // Aumento en activos fijos = adquisición (uso de efectivo)
                inversion.getAdquisiciones().add(
                    new DetalleMovimientoDTO(mov.codigo, "Adquisición de " + mov.nombre, round2(variacion))
                );
                totalAdquisiciones += variacion;
            } else if (variacion < -0.01) {
                // Disminución en activos fijos = venta (fuente de efectivo)
                inversion.getVentas().add(
                    new DetalleMovimientoDTO(mov.codigo, "Venta de " + mov.nombre, round2(Math.abs(variacion)))
                );
                totalVentas += Math.abs(variacion);
            }
        }

        inversion.setTotalAdquisiciones(round2(totalAdquisiciones));
        inversion.setTotalVentas(round2(totalVentas));
        inversion.setFlujoNetoInversion(round2(totalVentas - totalAdquisiciones));
    }

    /**
     * Procesa las actividades de financiamiento
     */
    private void procesarActividadesFinanciamiento(
            EstadoFlujosEfectivoDTO efe,
            Map<CategoriaEFE, List<MovimientoCuenta>> movimientos) {

        SeccionFinanciamientoDTO financiamiento = efe.getFinanciamiento();
        double totalEntradas = 0.0;
        double totalSalidas = 0.0;

        List<MovimientoCuenta> financMov = movimientos.get(CategoriaEFE.FINANCIAMIENTO);
        
        for (MovimientoCuenta mov : financMov) {
            double variacion = mov.variacion;
            String nombre = mov.nombre;
            
            if (variacion > 0.01) {
                // Aumento en pasivos/capital = entrada de efectivo
                String descripcion = nombre.contains("CAPITAL") || nombre.contains("APORTE")
                    ? "Aporte de " + nombre
                    : "Préstamo recibido - " + nombre;
                    
                financiamiento.getEntradas().add(
                    new DetalleMovimientoDTO(mov.codigo, descripcion, round2(variacion))
                );
                totalEntradas += variacion;
            } else if (variacion < -0.01) {
                // Disminución en pasivos/capital = salida de efectivo
                String descripcion = nombre.contains("DIVIDENDO")
                    ? "Pago de dividendos"
                    : "Pago de préstamo - " + nombre;
                    
                financiamiento.getSalidas().add(
                    new DetalleMovimientoDTO(mov.codigo, descripcion, round2(Math.abs(variacion)))
                );
                totalSalidas += Math.abs(variacion);
            }
        }

        financiamiento.setTotalEntradas(round2(totalEntradas));
        financiamiento.setTotalSalidas(round2(totalSalidas));
        financiamiento.setFlujoNetoFinanciamiento(round2(totalEntradas - totalSalidas));
    }

    /**
     * Calcula los totales finales y verifica cuadratura
     */
    private void calcularTotales(EstadoFlujosEfectivoDTO efe) {
        double aumentoNeto = efe.getOperacion().getFlujoNetoOperacion() +
                           efe.getInversion().getFlujoNetoInversion() +
                           efe.getFinanciamiento().getFlujoNetoFinanciamiento();
        
        efe.setAumentoNetoEfectivo(round2(aumentoNeto));
        
        double saldoCalculado = efe.getSaldoInicial() + aumentoNeto;
        double diferencia = Math.abs(saldoCalculado - efe.getSaldoFinal());
        
        efe.setCuadra(diferencia < 0.01);
    }

    /**
     * Redondea a 2 decimales
     */
    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    /**
     * Clase interna para representar un movimiento de cuenta
     */
    private static class MovimientoCuenta {
        Integer idCuenta;
        String codigo;
        String nombre;
        double debito;
        double credito;
        double saldoInicial;
        double variacion;
        String saldoNormal;
        CategoriaEFE categoria;

        MovimientoCuenta(Integer idCuenta, String codigo, String nombre, 
                        double debito, double credito, double saldoInicial,
                        double variacion, String saldoNormal, CategoriaEFE categoria) {
            this.idCuenta = idCuenta;
            this.codigo = codigo;
            this.nombre = nombre;
            this.debito = debito;
            this.credito = credito;
            this.saldoInicial = saldoInicial;
            this.variacion = variacion;
            this.saldoNormal = saldoNormal;
            this.categoria = categoria;
        }
    }
}
