package com.ues.sic.documentos;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentosService {

    // Ruta RELATIVA configurada en application.properties
    // Ejemplo recomendado:
    // file.upload-dir=uploads
    @Value("${file.upload-dir}")
    private String uploadDir;

    private final DocumentosRepository documentosRepository;

    public DocumentosService(DocumentosRepository documentosRepository) {
        this.documentosRepository = documentosRepository;
    }

    // ================================
    // MÉTODOS EXISTENTES
    // ================================

    public List<DocumentosModel> obtenerTodos() {
        return documentosRepository.findAll();
    }

    public Optional<DocumentosModel> obtenerPorId(Integer id) {
        return documentosRepository.findById(id);
    }

    /**
     * Elimina el documento de la BD y también el archivo físico.
     */
    public void eliminarDocumento(Integer id) {

        Optional<DocumentosModel> optDoc = documentosRepository.findById(id);
        if (optDoc.isEmpty()) {
            return;
        }

        DocumentosModel doc = optDoc.get();
        String nombreArchivo = doc.getArchivo(); // ahora siempre será solo el nombre

        if (nombreArchivo != null && !nombreArchivo.isBlank()) {
            try {
                // Construir ruta absoluta usando uploadDir relativo
                Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
                Path archivoPath = uploadPath.resolve(nombreArchivo).normalize();

                Files.deleteIfExists(archivoPath);

            } catch (IOException e) {
                System.err.println("⚠️ No se pudo eliminar el archivo físico: " + e.getMessage());
            }
        }

        documentosRepository.deleteById(id);
    }

    // ============================================
    // MÉTODO PARA CREAR DOCUMENTO + GUARDAR ARCHIVO
    // ============================================

    public DocumentosModel crearDocumento(DocumentosRequest request, MultipartFile archivoFile)
            throws IOException {

        // Crear carpeta uploads si no existe
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Nombre seguro y único
        String originalName = archivoFile.getOriginalFilename();
        if (originalName == null) {
            originalName = "archivo.pdf";
        }

        String fileName = System.currentTimeMillis() + "_" + originalName;

        Path destino = uploadPath.resolve(fileName);

        // Guardar archivo físico dentro de /uploads
        Files.copy(archivoFile.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        // Crear el modelo
        DocumentosModel doc = new DocumentosModel();
        doc.setIdTipo(request.getIdTipo());
        doc.setIdPartida(request.getIdPartida());
        doc.setMimeType(archivoFile.getContentType());
        doc.setTamano((int) archivoFile.getSize());
        doc.setSubidoPor(request.getSubidoPor());
        doc.setFechaSubida(LocalDateTime.now());

        // ✔️ Guardar SOLO el nombre en la BD
        doc.setArchivo(fileName);

        return documentosRepository.save(doc);
    }
}
