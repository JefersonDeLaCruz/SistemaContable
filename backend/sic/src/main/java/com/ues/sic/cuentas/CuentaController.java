package com.ues.sic.cuentas;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    @Autowired
    private CuentaRepository cuentaRepository;

    // Obtener todas las cuentas
    @GetMapping
    public List<CuentaModel> getAllCuentas() {
        return cuentaRepository.findAll();
    }

    // Obtener una cuenta por ID
    @GetMapping("/{id}")
    public ResponseEntity<CuentaModel> getCuentaById(@PathVariable Integer id) {
        return cuentaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Inserta solo una cuenta 
    @PostMapping("/insertar")
    public CuentaModel insertarCuenta(@RequestBody CuentaModel cuenta) {
        return CuentaModel.insertarCuenta(cuentaRepository, cuenta.getCodigo(), cuenta.getNombre(),
                cuenta.getTipo(), cuenta.getSaldoNormal(), cuenta.getIdPadre());
    }

    // Inserta un conjunto de cuentas que vienen en un json
    @PostMapping("/insertar-masivo")
    public List<CuentaModel> insertarCuentas(@RequestBody List<CuentaModel> cuentas) {
        return cuentas.stream()
                .map(c -> CuentaModel.insertarCuenta(cuentaRepository,
                        c.getCodigo(), c.getNombre(), c.getTipo(),
                        c.getSaldoNormal(), c.getIdPadre()))
                .toList();
    }

    // Actualizar una cuenta existente
    @PutMapping("/{id}")
    public ResponseEntity<CuentaModel> actualizarCuenta(
            @PathVariable Integer id, 
            @RequestBody CuentaModel cuentaActualizada) {
        
        return cuentaRepository.findById(id)
                .map(cuenta -> {
                    cuenta.setCodigo(cuentaActualizada.getCodigo());
                    cuenta.setNombre(cuentaActualizada.getNombre());
                    cuenta.setTipo(cuentaActualizada.getTipo());
                    cuenta.setSaldoNormal(cuentaActualizada.getSaldoNormal());
                    cuenta.setIdPadre(cuentaActualizada.getIdPadre());
                    
                    CuentaModel cuentaGuardada = cuentaRepository.save(cuenta);
                    return ResponseEntity.ok(cuentaGuardada);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar una cuenta
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCuenta(@PathVariable Integer id) {
        return cuentaRepository.findById(id)
                .map(cuenta -> {
                    cuentaRepository.delete(cuenta);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
