package com.ues.sic.partidas;
/* 
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody; */
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/partidas")
public class PartidasController {
    
   /*  @Autowired
    private PartidasRepository partidasRepository;

    // Obtener todas las partidas
    @GetMapping
    public List<PartidasModel> getAllPartidas() {
        return partidasRepository.findAll();
    }

    // Obtener partida por ID
    @GetMapping("/{id}")
    public ResponseEntity<PartidasModel> getPartidaById(@PathVariable Long id) {
        Optional<PartidasModel> partida = partidasRepository.findById(id);
        if (partida.isPresent()) {
            return ResponseEntity.ok(partida.get());
        }
        return ResponseEntity.notFound().build();
    }

    // Crear nueva partida
    @PostMapping
    public PartidasModel createPartida(@RequestBody PartidasModel partida) {
        return partidasRepository.save(partida);
    }

    // Actualizar partida existente
    @PutMapping("/{id}")
    public ResponseEntity<PartidasModel> updatePartida(@PathVariable Long id, @RequestBody PartidasModel partidaActualizada) {
        Optional<PartidasModel> partidaExistente = partidasRepository.findById(id);
        
        if (partidaExistente.isPresent()) {
            PartidasModel partida = partidaExistente.get();
            partida.setDescripcion(partidaActualizada.getDescripcion());
            partida.setIdPeriodo(partidaActualizada.getIdPeriodo());
            partida.setIdUsuario(partidaActualizada.getIdUsuario());
            
            PartidasModel partidaGuardada = partidasRepository.save(partida);
            return ResponseEntity.ok(partidaGuardada);
        }
        
        return ResponseEntity.notFound().build();
    }

    // Eliminar partida
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePartida(@PathVariable Long id) {
        Optional<PartidasModel> partida = partidasRepository.findById(id);
        
        if (partida.isPresent()) {
            partidasRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.notFound().build();
    } */
}
