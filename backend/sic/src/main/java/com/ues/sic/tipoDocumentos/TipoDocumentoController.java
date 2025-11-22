package com.ues.sic.tipoDocumentos;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tipos-documento")
public class TipoDocumentoController {

    private final TipoDocumentoService tipoDocumentoService;

    public TipoDocumentoController(TipoDocumentoService tipoDocumentoService) {
        this.tipoDocumentoService = tipoDocumentoService;
    }

    @PostMapping
    public ResponseEntity<TipoDocumentoModel> crearTipoDocumento(
            @Validated @RequestBody TipoDocumentoRequest request) {

        TipoDocumentoModel creado = tipoDocumentoService.crearTipoDocumento(request);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TipoDocumentoModel>> obtenerTodos() {
        return ResponseEntity.ok(tipoDocumentoService.obtenerTodos());
    }

}
