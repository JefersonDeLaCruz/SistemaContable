package com.ues.sic.partidas;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ues.sic.periodos.PeriodoContableModel;
import com.ues.sic.periodos.PeriodoContableRepository;


@RestController
@RequestMapping("/api/partidas")
public class PartidasController {
    
    @Autowired
    private PartidasRepository partidasRepository;
    
    @Autowired
    private PeriodoContableRepository periodoRepository;
    
    // Obtener todas las partidas
    @GetMapping
    public List<PartidasModel> getAllPartidas() {
        return partidasRepository.findAll();
    }

    // Obtener partidas filtradas por período (ahora considera rangos de fechas)
    @GetMapping("/periodo")
    public ResponseEntity<List<PartidasModel>> getPartidasByPeriodo(@RequestParam String idPeriodo) {
        try {
            // Convertir idPeriodo a Integer
            Integer periodoId = Integer.parseInt(idPeriodo);
            
            // Buscar el período seleccionado
            Optional<PeriodoContableModel> periodoOpt = periodoRepository.findById(periodoId);
            
            if (periodoOpt.isEmpty()) {
                System.out.println("Periodo no encontrado: " + periodoId);
                return ResponseEntity.notFound().build();
            }
            
            PeriodoContableModel periodoSeleccionado = periodoOpt.get();
            
            System.out.println("Buscando partidas para período: " + periodoSeleccionado.getNombre());
            System.out.println("Tipo: " + periodoSeleccionado.getFrecuencia());
            
            // Obtener TODOS los períodos
            List<PeriodoContableModel> todosPeriodos = periodoRepository.findAll();
            
            // Encontrar todos los períodos que están dentro del rango del período seleccionado
            List<String> idsPeridosIncluidos = new java.util.ArrayList<>();
            
            for (PeriodoContableModel periodo : todosPeriodos) {
                // Verificar si el período está completamente dentro del rango del período seleccionado
                boolean inicioEnRango = !periodo.getFechaInicio().isBefore(periodoSeleccionado.getFechaInicio()) 
                                        && !periodo.getFechaInicio().isAfter(periodoSeleccionado.getFechaFin());
                boolean finEnRango = !periodo.getFechaFin().isBefore(periodoSeleccionado.getFechaInicio()) 
                                     && !periodo.getFechaFin().isAfter(periodoSeleccionado.getFechaFin());
                
                if (inicioEnRango && finEnRango) {
                    idsPeridosIncluidos.add(String.valueOf(periodo.getIdPeriodo()));
                    System.out.println(" Incluye período: " + periodo.getNombre() + " (ID: " + periodo.getIdPeriodo() + ")");
                }
            }
            
            // Buscar partidas que tengan idPeriodo en la lista de períodos incluidos
            List<PartidasModel> partidas = new java.util.ArrayList<>();
            for (String idPer : idsPeridosIncluidos) {
                List<PartidasModel> partidasPorPeriodo = partidasRepository.findByIdPeriodo(idPer);
                partidas.addAll(partidasPorPeriodo);
                System.out.println("Período " + idPer + ": " + partidasPorPeriodo.size() + " partida(s)");
            }
            
            // Ordenar partidas por fecha
            partidas.sort((p1, p2) -> p1.getFecha().compareTo(p2.getFecha()));
            
            System.out.println("Total de partidas encontradas: " + partidas.size());
            
            return ResponseEntity.ok(partidas);
            
        } catch (NumberFormatException e) {
            System.out.println("Error: idPeriodo no es un número válido: " + idPeriodo);
            return ResponseEntity.badRequest().build();
        }
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
