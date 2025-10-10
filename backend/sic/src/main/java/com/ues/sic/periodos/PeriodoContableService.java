package com.ues.sic.periodos;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PeriodoContableService {

    private final PeriodoContableRepository repo;

    public PeriodoContableService(PeriodoContableRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public List<PeriodoContableModel> crearPeriodos(List<PeriodoContableRequest> requests) {
        List<PeriodoContableModel> nuevos = new ArrayList<>();

        for (PeriodoContableRequest r : requests) {
            PeriodoContableModel p = new PeriodoContableModel();
            p.setNombre(r.nombre());
            p.setFrecuencia(r.frecuencia());
            p.setFechaInicio(r.fechaInicio());
            p.setFechaFin(r.fechaFin());
            p.setCerrado(r.cerrado());
            nuevos.add(p);
        }

        // Guardado masivo en una transacción
        return repo.saveAll(nuevos);
    }

    @Transactional(readOnly = true)
    public List<PeriodoContableModel> listarTodos() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<PeriodoContableModel> buscarPorNombre(String nombre) {
        return repo.findByNombre(nombre);
    }

    @Transactional
    public void eliminarPorId(Integer idPeriodo) {
        repo.deleteById(idPeriodo);
    }
}
