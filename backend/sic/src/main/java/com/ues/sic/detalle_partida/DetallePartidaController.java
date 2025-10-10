package com.ues.sic.detalle_partida;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ues.sic.dtos.DetallePartidaDTO;

@RestController
@RequestMapping("/api/detalle-partida")
public class DetallePartidaController {
    

    @Autowired
    private DetallePartidaService detallePartidaService;

    @GetMapping
    public List<DetallePartidaModel> getAllDetalles() {
        return detallePartidaService.findAll();
    }

    // Endpoint para obtener detalles por ID de partida
    @GetMapping("/partida")
    public List<DetallePartidaDTO> getDetallesByPartidaId(@RequestParam Long partidaId) {
        List<DetallePartidaModel> detalles = detallePartidaService.findByPartidaId(partidaId);
        
        // Convertir a DTO para evitar problemas de serialización con relaciones LAZY
        return detalles.stream()
            .map(detalle -> new DetallePartidaDTO(
                detalle.getId(),
                detalle.getPartida() != null ? detalle.getPartida().getId() : null,
                detalle.getIdCuenta(),
                detalle.getDescripcion(),
                detalle.getDebito(),
                detalle.getCredito()
            ))
            .collect(Collectors.toList());
    }

}
