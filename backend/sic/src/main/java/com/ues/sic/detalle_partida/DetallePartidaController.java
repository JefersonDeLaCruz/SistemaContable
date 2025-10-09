package com.ues.sic.detalle_partida;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/detalle-partida")
public class DetallePartidaController {
    

    @Autowired
    private DetallePartidaService detallePartidaService;

    @GetMapping
    public List<DetallePartidaModel> getAllDetalles() {
        return detallePartidaService.findAll();
    }

}
