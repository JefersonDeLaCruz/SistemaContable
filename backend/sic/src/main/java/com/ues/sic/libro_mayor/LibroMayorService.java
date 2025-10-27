package com.ues.sic.libro_mayor;

import com.ues.sic.libro_mayor.dto.AsientoMayorDTO;
import com.ues.sic.libro_mayor.dto.CuentaMayorDTO;
import com.ues.sic.libro_mayor.dto.LibroMayorResponse;
import com.ues.sic.libro_mayor.projections.AsientoMayorRow;
import com.ues.sic.libro_mayor.projections.PeriodoAbiertoRow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LibroMayorService {

    private final LibroMayorRepository repo;

    public LibroMayorService(LibroMayorRepository repo) {
        this.repo = repo;
    }

    public LibroMayorResponse buildLibroMayor(Integer periodoIdNullable) {
        Integer periodoId = periodoIdNullable;

        if (periodoId == null) {
            // período abierto más reciente
            PeriodoAbiertoRow pa = repo.findUltimoPeriodoAbierto();
            if (pa == null) {
                throw new IllegalStateException("No existe período abierto. Envía ?periodoId=...");
            }
            periodoId = pa.getIdPeriodo();
        }

        // Traer todas las filas (partida + detalle) del período
        List<AsientoMayorRow> rows = repo.findAsientosByPeriodo(periodoId);

        // Agrupar por cuenta
        Map<Integer, List<AsientoMayorRow>> porCuenta = rows.stream()
                .collect(Collectors.groupingBy(AsientoMayorRow::getIdCuenta, LinkedHashMap::new, Collectors.toList()));

        List<CuentaMayorDTO> cuentas = new ArrayList<>();

        for (Map.Entry<Integer, List<AsientoMayorRow>> e : porCuenta.entrySet()) {
            Integer idCuenta = e.getKey();
            List<AsientoMayorRow> movimientos = e.getValue();

            // ordenar por fecha, luego id_partida por si acaso
            movimientos.sort(Comparator
                    .comparing(AsientoMayorRow::getFecha)
                    .thenComparing(AsientoMayorRow::getIdPartida));

             String nombreCuenta = movimientos.get(0).getNombreCuenta(); // <- nuevo

            BigDecimal saldo = BigDecimal.ZERO;
            BigDecimal totalDeb = BigDecimal.ZERO;
            BigDecimal totalCred = BigDecimal.ZERO;

            List<AsientoMayorDTO> asientos = new ArrayList<>();
            for (AsientoMayorRow r : movimientos) {
                BigDecimal deb = Optional.ofNullable(r.getDebito()).orElse(BigDecimal.ZERO);
                BigDecimal cred = Optional.ofNullable(r.getCredito()).orElse(BigDecimal.ZERO);
                totalDeb = totalDeb.add(deb);
                totalCred = totalCred.add(cred);

                // Convención: saldo = acumulado (debitos - creditos)
                // saldo = saldo.add(deb).subtract(cred);

                BigDecimal saldoTotal;

if (totalDeb.compareTo(totalCred) > 0) {
    // Deudor
    saldoTotal = totalDeb.subtract(totalCred);
} else {
    // Acreedor
    saldoTotal = totalCred.subtract(totalDeb);
}

                asientos.add(new AsientoMayorDTO(
                        r.getIdPartida(),
                        r.getFecha(),
                        r.getDescripcionPartida(),
                        deb,
                        cred,
                        saldoTotal
                ));
            }

           BigDecimal saldoTotal;

if (totalDeb.compareTo(totalCred) > 0) {
    // Deudor
    saldoTotal = totalDeb.subtract(totalCred);
} else {
    // Acreedor
    saldoTotal = totalCred.subtract(totalDeb);
}

cuentas.add(new CuentaMayorDTO(
        idCuenta,
        nombreCuenta,
        asientos,
        totalDeb,
        totalCred,
        saldoTotal
));

        }

        // Resumen global del período
        BigDecimal sumaDeb = cuentas.stream()
                .map(CuentaMayorDTO::getTotalDebito)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumaCred = cuentas.stream()
                .map(CuentaMayorDTO::getTotalCredito)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new LibroMayorResponse(periodoId, cuentas, sumaDeb, sumaCred);
    }
}
