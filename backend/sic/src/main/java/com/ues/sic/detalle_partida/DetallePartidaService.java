package com.ues.sic.detalle_partida;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DetallePartidaService {
    
    @Autowired
    private DetallePartidaRepository detallePartidaRepository;
    
    public DetallePartidaModel save(DetallePartidaModel detalle) {
        return detallePartidaRepository.save(detalle);
    }
    
    public List<DetallePartidaModel> findByPartidaId(Long partidaId) {
        return detallePartidaRepository.findByPartidaId(partidaId);
    }
    
    public DetallePartidaModel findById(Long id) {
        return detallePartidaRepository.findById(id).orElse(null);
    }
    
    public void deleteById(Long id) {
        detallePartidaRepository.deleteById(id);
    }

    public List<DetallePartidaModel> findAll() {
        
        return detallePartidaRepository.findAll();
    }
}