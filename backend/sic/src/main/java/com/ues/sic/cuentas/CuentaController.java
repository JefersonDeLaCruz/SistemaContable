package com.ues.sic.cuentas;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    @Autowired
    private CuentaRepository cuentaRepository;

    @PostMapping("/insertar")
    public CuentaModel insertarCuenta(@RequestBody CuentaModel cuenta) {
        // Puedes usar el método estático o directamente el repositorio
        return CuentaModel.insertar(cuentaRepository, cuenta.getCodigo(), cuenta.getNombre(),
                cuenta.getTipo(), cuenta.getSaldoNormal(), cuenta.getIdPadre());
    }

    @PostMapping("/insertar-masivo")
    public List<CuentaModel> insertarCuentas(@RequestBody List<CuentaModel> cuentas) {
        return cuentas.stream()
                .map(c -> CuentaModel.insertar(cuentaRepository,
                        c.getCodigo(), c.getNombre(), c.getTipo(),
                        c.getSaldoNormal(), c.getIdPadre()))
                .toList();
    }
}
