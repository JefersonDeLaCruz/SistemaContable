package com.ues.sic.periodos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/periodos")
public class PeriodoContableController {

    @Autowired
    private PeriodoContableRepository periodoRepository;

    // Obtener todos los periodos contables
    @GetMapping
    public List<PeriodoContableModel> getAllPeriodos() {
        return periodoRepository.findAll();
    }

    // Insertar un solo periodo contable
    @PostMapping("/insertar")
    public PeriodoContableModel insertarPeriodo(@RequestBody PeriodoContableModel periodo) {
        return PeriodoContableModel.insertarPeriodo(
                periodoRepository,
                periodo.getNombre(),
                periodo.getFrecuencia(),
                periodo.getFechaInicio(),
                periodo.getFechaFin(),
                periodo.getCerrado()
        );
    }

    // Inserción masiva (JSON con varios periodos)
    @PostMapping("/insertar-masivo")
    public List<PeriodoContableModel> insertarPeriodos(@RequestBody List<PeriodoContableModel> periodos) {
        return periodos.stream()
                .map(p -> PeriodoContableModel.insertarPeriodo(
                        periodoRepository,
                        p.getNombre(),
                        p.getFrecuencia(),
                        p.getFechaInicio(),
                        p.getFechaFin(),
                        p.getCerrado()
                ))
                .toList();
    }
}
