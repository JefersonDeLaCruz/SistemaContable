package com.ues.sic.partidas;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/partidas")
public class PartidasController {
    
    @Autowired
    private PartidasRepository partidasRepository;
    
    // Obtener todas las partidas
    @GetMapping
    public List<PartidasModel> getAllPartidas() {
        return partidasRepository.findAll();
    }

    // Obtener partidas filtradas por período
    @GetMapping("/periodo")
    public List<PartidasModel> getPartidasByPeriodo(@RequestParam String idPeriodo) {
        return partidasRepository.findByIdPeriodo(idPeriodo);
    }

    /*
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

    //implementar el método para registrar una partida mediante POST usando desde forms de html
    
    @PostMapping("/registrar")
    public ResponseEntity<PartidasModel> registrarPartida(@ModelAttribute PartidasModel partida) {
        partida.setFecha(java.time.LocalDate.now().toString()); // Asignar la fecha actual en formato ISO
        PartidasModel nuevaPartida = partidasRepository.save(partida);
        return ResponseEntity.ok(nuevaPartida);
    }

}
