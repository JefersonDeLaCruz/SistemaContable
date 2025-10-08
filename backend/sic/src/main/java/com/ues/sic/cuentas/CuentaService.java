package com.ues.sic.cuentas;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CuentaService {

    private final CuentaRepository repo;

    public CuentaService(CuentaRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public List<CuentaModel> crearCatalogo(List<CuentaRequest> requests) {
        Map<String, CuentaModel> porCodigo = new HashMap<>();
        List<CuentaModel> nuevas = new ArrayList<>();

        for (CuentaRequest r : requests) {
            CuentaModel c = new CuentaModel();
            c.setCodigo(r.codigo());
            c.setNombre(r.nombre());
            c.setTipo(r.tipo());
            c.setSaldoNormal(r.saldoNormal());
            c.setIdPadre(r.codigoPadre()); // NOTA: EL ID DEL PADRE TIENE QUE VENIR EN LA REQUEST
            nuevas.add(c);
            porCodigo.put(r.codigo(), c);
        }

        // Guardado masivo en una transacción
        return repo.saveAll(nuevas);
    }
}
