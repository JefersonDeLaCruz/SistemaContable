package com.ues.sic.partidas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PartidasService {
    
    @Autowired
    private PartidasRepository partidasRepository;
    
    public PartidasModel save(PartidasModel partida) {
        return partidasRepository.save(partida);
    }
    public PartidasModel findById(Long id) {
        return partidasRepository.findById(id).orElse(null);
    }
    
    public void deleteById(Long id) {
        partidasRepository.deleteById(id);
    }
    
    public List<PartidasModel> findAll() {
        return partidasRepository.findAll();
    }
}
