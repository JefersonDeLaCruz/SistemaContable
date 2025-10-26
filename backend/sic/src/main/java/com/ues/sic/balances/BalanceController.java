package com.ues.sic.balances;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.ues.sic.detalle_partida.DetallePartidaRepository;
import com.ues.sic.periodos.PeriodoContableModel;
import com.ues.sic.periodos.PeriodoContableRepository;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/balances")
public class BalanceController {

    @Autowired
    private DetallePartidaRepository detalleRepo;

    @Autowired
    private PeriodoContableRepository periodoRepo;

    @GetMapping("/general")
    public Map<String, Object> balanceGeneral(
            @RequestParam(value = "fecha", required = false) String fecha,
            @RequestParam(value = "periodo", required = false) Integer periodoId) {

        LocalDate fechaCorte;
        if (periodoId != null) {
            Optional<PeriodoContableModel> p = periodoRepo.findById(periodoId);
            fechaCorte = p.map(PeriodoContableModel::getFechaFin).orElse(LocalDate.now());
        } else if (fecha != null && !fecha.isBlank()) {
            fechaCorte = LocalDate.parse(fecha);
        } else {
            fechaCorte = LocalDate.now();
        }
        String corte = fechaCorte.toString();

        List<Object[]> filas = detalleRepo.balanceGeneralHasta(corte);

        List<Map<String, Object>> activo = new ArrayList<>();
        List<Map<String, Object>> pasivo = new ArrayList<>();
        List<Map<String, Object>> capital = new ArrayList<>();

        double totalActivo = 0.0;
        double totalPasivo = 0.0;
        double totalCapital = 0.0;

        for (Object[] r : filas) {
            String tipo = (String) r[0];
            // Integer idCuenta = ((Number) r[1]).intValue();
            String codigo = (String) r[2];
            String nombre = (String) r[3];
            String saldoNormal = (String) r[4];
            double totalDebito = r[5] != null ? ((Number) r[5]).doubleValue() : 0.0;
            double totalCredito = r[6] != null ? ((Number) r[6]).doubleValue() : 0.0;

            double saldo = "DEUDOR".equalsIgnoreCase(saldoNormal)
                    ? (totalDebito - totalCredito)
                    : (totalCredito - totalDebito);

            // Ocultar saldos cero para simplificar la vista
            if (Math.abs(saldo) < 0.005) {
                continue;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("codigo", codigo);
            item.put("nombre", nombre);
            item.put("saldo", round2(Math.abs(saldo)));

            if ("ACTIVO".equalsIgnoreCase(tipo)) {
                activo.add(item);
                totalActivo += saldo; // saldo ya estÃ¡ con signo correcto
            } else if ("PASIVO".equalsIgnoreCase(tipo)) {
                pasivo.add(item);
                totalPasivo += saldo;
            } else if ("CAPITAL CONTABLE".equalsIgnoreCase(tipo)) {
                capital.add(item);
                totalCapital += saldo;
            }
        }

        // Resultado del ejercicio
        Double ingreso = detalleRepo.ingresoAcumulado(corte);
        Double gasto = detalleRepo.gastoAcumulado(corte);
        double resultado = (ingreso != null ? ingreso : 0.0) - (gasto != null ? gasto : 0.0);
        if (Math.abs(resultado) >= 0.005) {
            Map<String, Object> resItem = new LinkedHashMap<>();
            resItem.put("codigo", "RESULTADO");
            resItem.put("nombre", resultado >= 0 ? "Resultado del ejercicio (Utilidad)" : "Resultado del ejercicio (PÃ©rdida)");
            resItem.put("saldo", round2(Math.abs(resultado)));
            capital.add(resItem);
            totalCapital += resultado; // utilidad aumenta capital; perdida lo reduce
        }

        double totalPasivoMasCapital = totalPasivo + totalCapital;
        boolean cuadra = round2(totalActivo) == round2(totalPasivoMasCapital);
        double diferencia = round2(totalActivo - totalPasivoMasCapital);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("fechaCorte", corte);
        resp.put("activo", Map.of(
                "cuentas", activo,
                "total", round2(totalActivo)
        ));
        resp.put("pasivo", Map.of(
                "cuentas", pasivo,
                "total", round2(totalPasivo)
        ));
        resp.put("capital", Map.of(
                "cuentas", capital,
                "total", round2(totalCapital)
        ));
        resp.put("cuadra", cuadra);
        resp.put("diferencia", round2(diferencia));

        return resp;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }


    @GetMapping("/estado")
    public Map<String, Object> estadoResultados(
            @RequestParam(value = "periodo", required = false) Integer periodoId,
            @RequestParam(value = "inicio", required = false) String inicio,
            @RequestParam(value = "fin", required = false) String fin) {

        LocalDate fi;
        LocalDate ff;
        if (periodoId != null) {
            Optional<PeriodoContableModel> p = periodoRepo.findById(periodoId);
            fi = p.map(PeriodoContableModel::getFechaInicio).orElse(LocalDate.now().withDayOfMonth(1));
            ff = p.map(PeriodoContableModel::getFechaFin).orElse(LocalDate.now());
        } else if (inicio != null && fin != null && !inicio.isBlank() && !fin.isBlank()) {
            fi = LocalDate.parse(inicio);
            ff = LocalDate.parse(fin);
        } else {
            // Por defecto: mes actual
            java.time.YearMonth ym = java.time.YearMonth.now();
            fi = ym.atDay(1);
            ff = ym.atEndOfMonth();
        }

        String sFi = fi.toString();
        String sFf = ff.toString();

        List<Object[]> filas = detalleRepo.estadoResultadosEntre(sFi, sFf);

        List<Map<String, Object>> ingresos = new ArrayList<>();
        List<Map<String, Object>> gastos = new ArrayList<>();
        double totalIngresos = 0.0;
        double totalGastos = 0.0;

        for (Object[] r : filas) {
            String tipo = (String) r[0];
            String codigo = (String) r[2];
            String nombre = (String) r[3];
            String saldoNormal = (String) r[4];
            double totalDebito = r[5] != null ? ((Number) r[5]).doubleValue() : 0.0;
            double totalCredito = r[6] != null ? ((Number) r[6]).doubleValue() : 0.0;

            double monto;
            if ("INGRESOS".equalsIgnoreCase(tipo) || codigo.startsWith("4")) {
                monto = totalCredito - totalDebito;
                if (Math.abs(monto) < 0.005) continue;
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("codigo", codigo);
                item.put("nombre", nombre);
                item.put("monto", round2(Math.abs(monto)));
                ingresos.add(item);
                totalIngresos += monto;
            } else if ("GASTOS".equalsIgnoreCase(tipo) || codigo.startsWith("5")) {
                monto = totalDebito - totalCredito;
                if (Math.abs(monto) < 0.005) continue;
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("codigo", codigo);
                item.put("nombre", nombre);
                item.put("monto", round2(Math.abs(monto)));
                gastos.add(item);
                totalGastos += monto;
            }
        }

        Double ingresoCalc = detalleRepo.ingresoEntre(sFi, sFf);
        Double gastoCalc = detalleRepo.gastoEntre(sFi, sFf);
        double totalIng = ingresoCalc != null ? ingresoCalc : totalIngresos;
        double totalGas = gastoCalc != null ? gastoCalc : totalGastos;
        double utilidad = totalIng - totalGas;

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("periodo", Map.of("inicio", sFi, "fin", sFf));
        resp.put("ingresos", Map.of("cuentas", ingresos, "total", round2(totalIng)));
        resp.put("gastos", Map.of("cuentas", gastos, "total", round2(totalGas)));
        resp.put("utilidadNeta", round2(utilidad));
        resp.put("resultado", utilidad >= 0 ? "Utilidad" : "PÃ©rdida");
        return resp;
    }

    @GetMapping("/comprobacion")
    public Map<String, Object> balanceComprobacion(
            @RequestParam(value = "periodo", required = false) Integer periodoId,
            @RequestParam(value = "inicio", required = false) String inicio,
            @RequestParam(value = "fin", required = false) String fin) {

        LocalDate fi;
        LocalDate ff;
        if (periodoId != null) {
            Optional<PeriodoContableModel> p = periodoRepo.findById(periodoId);
            fi = p.map(PeriodoContableModel::getFechaInicio).orElse(LocalDate.now().withDayOfMonth(1));
            ff = p.map(PeriodoContableModel::getFechaFin).orElse(LocalDate.now());
        } else if (inicio != null && fin != null && !inicio.isBlank() && !fin.isBlank()) {
            fi = LocalDate.parse(inicio);
            ff = LocalDate.parse(fin);
        } else {
            java.time.YearMonth ym = java.time.YearMonth.now();
            fi = ym.atDay(1);
            ff = ym.atEndOfMonth();
        }

        String sIni = fi.toString();
        String sFin = ff.toString();
        String sDiaAntes = fi.minusDays(1).toString();

        // Saldo inicial acumulado hasta el dÃ­a anterior del inicio
        List<Object[]> iniRows = detalleRepo.saldosHastaTodos(sDiaAntes);
        Map<String, Double> inicialPorCodigo = new HashMap<>();
        Map<String, String> saldoNormalPorCodigo = new HashMap<>();
        Map<String, String> nombrePorCodigo = new HashMap<>();
        for (Object[] r : iniRows) {
            String codigo = (String) r[1];
            String nombre = (String) r[2];
            String saldoNormal = (String) r[3];
            double tDeb = r[4] != null ? ((Number) r[4]).doubleValue() : 0.0;
            double tCre = r[5] != null ? ((Number) r[5]).doubleValue() : 0.0;
            double saldoInicial = "DEUDOR".equalsIgnoreCase(saldoNormal) ? (tDeb - tCre) : (tCre - tDeb);
            if (Math.abs(saldoInicial) >= 0.005) {
                inicialPorCodigo.put(codigo, saldoInicial);
                nombrePorCodigo.put(codigo, nombre);
                saldoNormalPorCodigo.put(codigo, saldoNormal);
            }
        }

        // Movimientos del periodo
        List<Object[]> movRows = detalleRepo.movimientosEntreTodos(sIni, sFin);
        Map<String, Map<String, Object>> cuentas = new TreeMap<>();

        // Incluir cuentas que tienen movimientos
        for (Object[] r : movRows) {
            String codigo = (String) r[1];
            String nombre = (String) r[2];
            double deb = r[3] != null ? ((Number) r[3]).doubleValue() : 0.0;
            double cre = r[4] != null ? ((Number) r[4]).doubleValue() : 0.0;
            Map<String, Object> m = cuentas.computeIfAbsent(codigo, k -> new LinkedHashMap<>());
            m.put("codigo", codigo);
            m.put("nombre", nombre);
            m.put("debitos", deb);
            m.put("creditos", cre);
        }

        // Incluir cuentas con saldo inicial pero sin movimientos
        for (Map.Entry<String, Double> e : inicialPorCodigo.entrySet()) {
            cuentas.computeIfAbsent(e.getKey(), k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("codigo", k);
                m.put("nombre", nombrePorCodigo.getOrDefault(k, k));
                m.put("debitos", 0.0);
                m.put("creditos", 0.0);
                return m;
            });
        }

        // Calcular saldos
        double totIni = 0, totDeb = 0, totCre = 0, totFin = 0;
        List<Map<String, Object>> salida = new ArrayList<>();
        for (Map<String, Object> m : cuentas.values()) {
            String codigo = (String) m.get("codigo");
            String saldoNormal = saldoNormalPorCodigo.getOrDefault(codigo, "DEUDOR");
            double sInicial = inicialPorCodigo.getOrDefault(codigo, 0.0);
            double deb = ((Number) m.get("debitos")).doubleValue();
            double cre = ((Number) m.get("creditos")).doubleValue();

            double saldoFinal = "DEUDOR".equalsIgnoreCase(saldoNormal)
                    ? (sInicial + (deb - cre))
                    : (sInicial + (cre - deb));

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("codigo", codigo);
            row.put("nombre", (String) m.get("nombre"));
            row.put("saldoInicial", round2(Math.abs(sInicial)));
            row.put("debitos", round2(deb));
            row.put("creditos", round2(cre));
            row.put("saldoFinal", round2(Math.abs(saldoFinal)));
            salida.add(row);

            totIni += sInicial;
            totDeb += deb;
            totCre += cre;
            totFin += saldoFinal;
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("periodo", Map.of("inicio", sIni, "fin", sFin));
        resp.put("cuentas", salida);
        resp.put("totales", Map.of(
                "saldoInicial", round2(Math.abs(totIni)),
                "debitos", round2(totDeb),
                "creditos", round2(totCre),
                "saldoFinal", round2(Math.abs(totFin))
        ));
        return resp;
    }
}
