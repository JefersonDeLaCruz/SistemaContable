package com.ues.sic.documentos;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documentos")
@CrossOrigin(origins = "*")
public class DocumentosController {

    private final DocumentosService documentosService;

    public DocumentosController(DocumentosService documentosService) {
        this.documentosService = documentosService;
    }

    // ================== OBTENER TODOS ==================
    @GetMapping
    public ResponseEntity<List<DocumentosModel>> getTodos() {
        return ResponseEntity.ok(documentosService.obtenerTodos());
    }

    // ================== OBTENER POR ID (opcional) ==================
    @GetMapping("/{id}")
    public ResponseEntity<DocumentosModel> getPorId(@PathVariable Integer id) {
        return documentosService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ================== CREAR DOCUMENTO CON ARCHIVO ==================
    // Espera un form-data con:
    // - data    -> JSON de DocumentosRequest
    // - archivo -> archivo físico (PDF, etc.)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentosModel> crear(
            @RequestPart("data") DocumentosRequest request,
            @RequestPart("archivo") MultipartFile archivo
    ) throws IOException {

        DocumentosModel creado = documentosService.crearDocumento(request, archivo);
        return ResponseEntity.ok(creado);
    }

    // ================== ELIMINAR ==================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        documentosService.eliminarDocumento(id);
        return ResponseEntity.noContent().build();
    }
}
