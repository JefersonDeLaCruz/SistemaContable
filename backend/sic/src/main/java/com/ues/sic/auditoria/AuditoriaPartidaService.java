package com.ues.sic.auditoria;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ues.sic.detalle_partida.DetallePartidaModel;
import com.ues.sic.partidas.PartidasModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para gestionar la auditoría de partidas contables.
 * Registra todos los cambios realizados en partidas para cumplir con normativa contable.
 */
@Service
public class AuditoriaPartidaService {

    @Autowired
    private AuditoriaPartidaRepository auditoriaRepository;

    private final ObjectMapper objectMapper;

    public AuditoriaPartidaService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Registra una creación de partida.
     */
    public void registrarCreacion(PartidasModel partida, String usuarioId, String ipOrigen) {
        AuditoriaPartidaModel auditoria = new AuditoriaPartidaModel();
        auditoria.setPartidaId(partida.getId());
        auditoria.setOperacion("CREATE");
        auditoria.setUsuarioId(usuarioId);
        auditoria.setFechaCambio(LocalDateTime.now());
        auditoria.setValoresNuevos(convertirPartidaAJSON(partida, null));
        auditoria.setIpOrigen(ipOrigen);
        auditoria.setRazonCambio("Creación inicial de partida");

        auditoriaRepository.save(auditoria);
    }

    /**
     * Registra una actualización de partida.
     */
    public void registrarActualizacion(
        PartidasModel partidaAntigua,
        PartidasModel partidaNueva,
        List<DetallePartidaModel> detallesAntiguos,
        List<DetallePartidaModel> detallesNuevos,
        String usuarioId,
        String razonCambio,
        String ipOrigen
    ) {
        AuditoriaPartidaModel auditoria = new AuditoriaPartidaModel();
        auditoria.setPartidaId(partidaNueva.getId());
        auditoria.setOperacion("UPDATE");
        auditoria.setUsuarioId(usuarioId);
        auditoria.setFechaCambio(LocalDateTime.now());
        auditoria.setValoresAnteriores(convertirPartidaAJSON(partidaAntigua, detallesAntiguos));
        auditoria.setValoresNuevos(convertirPartidaAJSON(partidaNueva, detallesNuevos));
        auditoria.setRazonCambio(razonCambio);
        auditoria.setIpOrigen(ipOrigen);

        auditoriaRepository.save(auditoria);
    }

    /**
     * Registra una eliminación de partida.
     */
    public void registrarEliminacion(
        PartidasModel partida,
        List<DetallePartidaModel> detalles,
        String usuarioId,
        String razonCambio,
        String ipOrigen
    ) {
        AuditoriaPartidaModel auditoria = new AuditoriaPartidaModel();
        auditoria.setPartidaId(partida.getId());
        auditoria.setOperacion("DELETE");
        auditoria.setUsuarioId(usuarioId);
        auditoria.setFechaCambio(LocalDateTime.now());
        auditoria.setValoresAnteriores(convertirPartidaAJSON(partida, detalles));
        auditoria.setRazonCambio(razonCambio);
        auditoria.setIpOrigen(ipOrigen);

        auditoriaRepository.save(auditoria);
    }

    /**
     * Obtiene el historial de cambios de una partida.
     */
    public List<AuditoriaPartidaModel> obtenerHistorial(Long partidaId) {
        return auditoriaRepository.findByPartidaIdOrderByFechaCambioDesc(partidaId);
    }

    /**
     * Obtiene todos los cambios realizados por un usuario.
     */
    public List<AuditoriaPartidaModel> obtenerCambiosPorUsuario(String usuarioId) {
        return auditoriaRepository.findByUsuarioIdOrderByFechaCambioDesc(usuarioId);
    }

    /**
     * Obtiene el reporte completo de auditoría.
     */
    public List<AuditoriaPartidaModel> obtenerReporteCompleto() {
        return auditoriaRepository.findAllByOrderByFechaCambioDesc();
    }

    /**
     * Obtiene auditorías de hoy.
     */
    public List<AuditoriaPartidaModel> obtenerAuditoriasDeHoy() {
        return auditoriaRepository.findAuditoriasDelDia(LocalDateTime.now());
    }

    /**
     * Convierte una partida y sus detalles a JSON para almacenamiento.
     */
    private String convertirPartidaAJSON(PartidasModel partida, List<DetallePartidaModel> detalles) {
        try {
            Map<String, Object> datos = new HashMap<>();
            datos.put("id", partida.getId());
            datos.put("descripcion", partida.getDescripcion());
            datos.put("fecha", partida.getFecha());
            datos.put("idPeriodo", partida.getIdPeriodo());
            datos.put("idUsuario", partida.getIdUsuario());
            datos.put("estado", partida.getEstado());
            datos.put("createdAt", partida.getCreatedAt());
            datos.put("updatedAt", partida.getUpdatedAt());
            datos.put("createdBy", partida.getCreatedBy());
            datos.put("updatedBy", partida.getUpdatedBy());

            if (detalles != null && !detalles.isEmpty()) {
                List<Map<String, Object>> detallesMap = new java.util.ArrayList<>();
                for (DetallePartidaModel detalle : detalles) {
                    Map<String, Object> detalleMap = new HashMap<>();
                    detalleMap.put("id", detalle.getId());
                    detalleMap.put("idCuenta", detalle.getIdCuenta());
                    detalleMap.put("descripcion", detalle.getDescripcion());
                    detalleMap.put("debito", detalle.getDebito());
                    detalleMap.put("credito", detalle.getCredito());
                    detallesMap.add(detalleMap);
                }
                datos.put("detalles", detallesMap);
            }

            return objectMapper.writeValueAsString(datos);
        } catch (Exception e) {
            return "{\"error\": \"No se pudo serializar la partida\"}";
        }
    }
}
