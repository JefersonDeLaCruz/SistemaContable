package com.ues.sic.partidas;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.ues.sic.auditoria.AuditoriaPartidaModel;
import com.ues.sic.auditoria.AuditoriaPartidaService;
import com.ues.sic.detalle_partida.DetallePartidaModel;
import com.ues.sic.detalle_partida.DetallePartidaService;
import com.ues.sic.dtos.EditarPartidaDTO;
import com.ues.sic.dtos.PartidaConDetallesResponseDTO;
import com.ues.sic.periodos.PeriodoContableModel;
import com.ues.sic.periodos.PeriodoContableRepository;
import com.ues.sic.usuarios.UsuariosModel;
import com.ues.sic.usuarios.UsuariosService;


@RestController
@RequestMapping("/api/partidas")
public class PartidasController {

    @Autowired
    private PartidasRepository partidasRepository;

    @Autowired
    private PeriodoContableRepository periodoRepository;

    @Autowired
    private PartidasService partidasService;

    @Autowired
    private DetallePartidaService detallePartidaService;

    @Autowired
    private AuditoriaPartidaService auditoriaService;

    @Autowired
    private UsuariosService usuariosService;


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

    /**
     * Obtiene una partida específica con todos sus detalles.
     * Usado para cargar datos en el formulario de edición.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPartidaPorId(@PathVariable Long id) {
        try {
            PartidasModel partida = partidasService.findById(id);
            if (partida == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Partida no encontrada con ID: " + id);
            }

            List<DetallePartidaModel> detalles = detallePartidaService.findByPartidaId(id);

            PartidaConDetallesResponseDTO response = new PartidaConDetallesResponseDTO(partida, detalles);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener la partida: " + e.getMessage());
        }
    }

    /**
     * Edita una partida existente con validaciones y auditoría completa.
     * Requiere autenticación y permisos adecuados.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> editarPartida(
            @PathVariable Long id,
            @RequestBody EditarPartidaDTO editarDTO,
            HttpServletRequest request
    ) {
        try {
            // 1. Obtener usuario autenticado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            // 2. Obtener información del usuario
            UsuariosModel usuario = usuariosService.findByUsername(username);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Usuario no autenticado");
            }

            // 3. Verificar que la partida existe
            PartidasModel partida = partidasService.findById(id);
            if (partida == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Partida no encontrada con ID: " + id);
            }

            // 4. Validar permisos de edición
            if (!partidasService.puedeEditarPartida(partida, username, usuario.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("No tienes permisos para editar esta partida");
            }

            // 5. Validar que se proporcionó una razón de cambio
            if (editarDTO.getRazonCambio() == null || editarDTO.getRazonCambio().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Debe proporcionar una razón para el cambio (auditoría)");
            }

            // 6. Obtener IP del cliente
            String ipOrigen = obtenerIPCliente(request);

            // 7. Llamar al servicio de edición
            PartidasModel partidaActualizada = partidasService.editarPartida(
                    id,
                    editarDTO.getDescripcion(),
                    editarDTO.getFecha(),
                    editarDTO.getIdPeriodo(),
                    editarDTO.getDetallesAsModel(),
                    username,
                    editarDTO.getRazonCambio(),
                    ipOrigen
            );

            return ResponseEntity.ok(partidaActualizada);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al editar la partida: " + e.getMessage());
        }
    }

    /**
     * Obtiene el historial completo de cambios de una partida.
     * Cumple con requisitos de auditoría contable.
     */
    @GetMapping("/historial/{id}")
    public ResponseEntity<?> obtenerHistorialPartida(@PathVariable Long id) {
        try {
            // Verificar que la partida existe
            PartidasModel partida = partidasService.findById(id);
            if (partida == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Partida no encontrada con ID: " + id);
            }

            // Obtener historial de auditoría
            List<AuditoriaPartidaModel> historial = auditoriaService.obtenerHistorial(id);

            return ResponseEntity.ok(historial);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener el historial: " + e.getMessage());
        }
    }

    /**
     * Obtiene el reporte completo de auditoría de todas las partidas.
     * Solo accesible por ADMIN y AUDITOR.
     */
    @GetMapping("/auditoria/reporte")
    public ResponseEntity<?> obtenerReporteAuditoria() {
        try {
            // Verificar rol del usuario
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            UsuariosModel usuario = usuariosService.findByUsername(username);

            if (usuario == null || (!usuario.getRole().equals("ADMIN") && !usuario.getRole().equals("AUDITOR"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Solo ADMIN y AUDITOR pueden acceder al reporte de auditoría");
            }

            List<AuditoriaPartidaModel> reporteCompleto = auditoriaService.obtenerReporteCompleto();

            return ResponseEntity.ok(reporteCompleto);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al generar el reporte: " + e.getMessage());
        }
    }

    /**
     * Método auxiliar para obtener la IP del cliente.
     */
    private String obtenerIPCliente(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}
