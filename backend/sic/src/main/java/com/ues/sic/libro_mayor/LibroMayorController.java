package com.ues.sic.libro_mayor;

import com.ues.sic.libro_mayor.dto.LibroMayorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/libromayor")
public class LibroMayorController {

    private final LibroMayorService service;

    public LibroMayorController(LibroMayorService service) {
        this.service = service;
    }

    // GET /api/libro-mayor            -> período abierto más reciente
    // GET /api/libro-mayor?periodoId= -> período específico
    @GetMapping
    public ResponseEntity<LibroMayorResponse> getLibroMayor(
            @RequestParam(name = "periodoId") Optional<Integer> periodoId) {

        LibroMayorResponse resp = service.buildLibroMayor(periodoId.orElse(null));
        return ResponseEntity.ok(resp);
    }
}