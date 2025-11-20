package com.ues.sic.tipoDocumentos;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TipoDocumentoService {

    private final TipoDocumentoRepository tipoDocumentoRepository;

    public TipoDocumentoService(TipoDocumentoRepository tipoDocumentoRepository) {
        this.tipoDocumentoRepository = tipoDocumentoRepository;
    }

    @Transactional
    public TipoDocumentoModel crearTipoDocumento(TipoDocumentoRequest request) {
        TipoDocumentoModel tipo = new TipoDocumentoModel();
        tipo.setNombre(request.getNombre());
        tipo.setCreadoPor(request.getCreadoPor());
        return tipoDocumentoRepository.save(tipo);
    }

    public List<TipoDocumentoModel> obtenerTodos() {
    return tipoDocumentoRepository.findAll();
}
}
