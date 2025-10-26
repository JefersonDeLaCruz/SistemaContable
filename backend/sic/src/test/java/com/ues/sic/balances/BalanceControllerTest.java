package com.ues.sic.balances;

import com.ues.sic.detalle_partida.DetallePartidaRepository;
import com.ues.sic.periodos.PeriodoContableModel;
import com.ues.sic.periodos.PeriodoContableRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BalanceController.class)
@AutoConfigureMockMvc(addFilters = false)
class BalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DetallePartidaRepository detalleRepo;

    @MockBean
    private PeriodoContableRepository periodoRepo;

    private PeriodoContableModel periodo(LocalDate ini, LocalDate fin) {
        PeriodoContableModel p = new PeriodoContableModel();
        p.setIdPeriodo(1);
        p.setNombre("Periodo Test");
        p.setFrecuencia("MENSUAL");
        p.setFechaInicio(ini);
        p.setFechaFin(fin);
        p.setCerrado(false);
        return p;
    }

    @Test
    void balanceGeneral_ok() throws Exception {
        var ini = LocalDate.of(2025, 1, 1);
        var fin = LocalDate.of(2025, 1, 31);
        given(periodoRepo.findById(eq(1))).willReturn(Optional.of(periodo(ini, fin)));

        // tipo, id_cuenta, codigo, nombre, saldo_normal, total_debito, total_credito
        List<Object[]> filas = List.of(
                new Object[]{"ACTIVO", 1, "1.1.01", "CAJA", "DEUDOR", 1000.0, 200.0},
                new Object[]{"PASIVO", 2, "2.1.01", "PROVEEDORES", "ACREEDOR", 100.0, 300.0},
                new Object[]{"CAPITAL CONTABLE", 3, "3.1", "CAPITAL SOCIAL", "ACREEDOR", 0.0, 500.0}
        );
        given(detalleRepo.balanceGeneralHasta(any(String.class))).willReturn(filas);
        given(detalleRepo.ingresoAcumulado(any(String.class))).willReturn(4000.0);
        given(detalleRepo.gastoAcumulado(any(String.class))).willReturn(3000.0);

        mockMvc.perform(get("/api/balances/general").param("periodo", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaCorte").exists())
                .andExpect(jsonPath("$.activo").exists())
                .andExpect(jsonPath("$.pasivo").exists())
                .andExpect(jsonPath("$.capital").exists());
    }

    @Test
    void estadoResultados_ok() throws Exception {
        var ini = LocalDate.of(2025, 1, 1);
        var fin = LocalDate.of(2025, 1, 31);
        given(periodoRepo.findById(eq(1))).willReturn(Optional.of(periodo(ini, fin)));

        List<Object[]> filas = List.of(
                new Object[]{"INGRESOS", 10, "4.1", "VENTAS", "ACREEDOR", 0.0, 1000.0},
                new Object[]{"GASTOS", 20, "5.1", "GASTO ADM", "DEUDOR", 300.0, 0.0}
        );
        given(detalleRepo.estadoResultadosEntre(any(String.class), any(String.class))).willReturn(filas);
        given(detalleRepo.ingresoEntre(any(String.class), any(String.class))).willReturn(1000.0);
        given(detalleRepo.gastoEntre(any(String.class), any(String.class))).willReturn(300.0);

        mockMvc.perform(get("/api/balances/estado").param("periodo", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodo.inicio").value("2025-01-01"))
                .andExpect(jsonPath("$.periodo.fin").value("2025-01-31"))
                .andExpect(jsonPath("$.ingresos.total").exists())
                .andExpect(jsonPath("$.gastos.total").exists())
                .andExpect(jsonPath("$.utilidadNeta").exists());
    }

    @Test
    void balanceComprobacion_ok() throws Exception {
        var ini = LocalDate.of(2025, 1, 1);
        var fin = LocalDate.of(2025, 1, 31);
        given(periodoRepo.findById(eq(1))).willReturn(Optional.of(periodo(ini, fin)));

        // id_cuenta, codigo, nombre, saldo_normal, total_debito, total_credito
        List<Object[]> saldosHasta = List.of(
                new Object[]{1, "1.1.01", "CAJA", "DEUDOR", 5000.0, 0.0},
                new Object[]{2, "2.1.01", "PROVEEDORES", "ACREEDOR", 0.0, 4000.0}
        );
        given(detalleRepo.saldosHastaTodos(any(String.class))).willReturn(saldosHasta);

        // id_cuenta, codigo, nombre, debito, credito
        List<Object[]> movimientos = List.of(
                new Object[]{1, "1.1.01", "CAJA", 3000.0, 0.0},
                new Object[]{2, "2.1.01", "PROVEEDORES", 1000.0, 1500.0}
        );
        given(detalleRepo.movimientosEntreTodos(any(String.class), any(String.class))).willReturn(movimientos);

        mockMvc.perform(get("/api/balances/comprobacion").param("periodo", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodo.inicio").value("2025-01-01"))
                .andExpect(jsonPath("$.periodo.fin").value("2025-01-31"))
                .andExpect(jsonPath("$.cuentas").isArray())
                .andExpect(jsonPath("$.totales.saldoInicial").exists())
                .andExpect(jsonPath("$.totales.debitos").exists())
                .andExpect(jsonPath("$.totales.creditos").exists())
                .andExpect(jsonPath("$.totales.saldoFinal").exists());
    }
}
