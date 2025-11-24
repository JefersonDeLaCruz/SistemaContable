package com.ues.sic.partidas;

import com.ues.sic.auditoria.AuditoriaPartidaService;
import com.ues.sic.detalle_partida.DetallePartidaModel;
import com.ues.sic.detalle_partida.DetallePartidaRepository;
import com.ues.sic.periodos.PeriodoContableModel;
import com.ues.sic.periodos.PeriodoContableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PartidasService {

    @Autowired
    private PartidasRepository partidasRepository;

    @Autowired
    private DetallePartidaRepository detallePartidaRepository;

    @Autowired
    private PeriodoContableRepository periodoRepository;

    @Autowired
    private AuditoriaPartidaService auditoriaService;

    public PartidasModel save(PartidasModel partida) {
        return partidasRepository.save(partida);
    }

    public PartidasModel findById(Long id) {
        return partidasRepository.findById(id).orElse(null);
    }

    public void deleteById(Long id) {
        partidasRepository.deleteById(id);
    }

    public List<PartidasModel> findAll() {
        return partidasRepository.findAll();
    }

    /**
     * Edita una partida existente con validaciones contables y auditoría completa.
     * CRÍTICO: Cumple con principios de trazabilidad contable.
     */
    @Transactional
    public PartidasModel editarPartida(
        Long partidaId,
        String nuevaDescripcion,
        String nuevaFecha,
        String nuevoIdPeriodo,
        List<DetallePartidaModel> nuevosDetalles,
        String usuarioEditor,
        String razonCambio,
        String ipOrigen
    ) throws Exception {

        // 1. Verificar que la partida existe
        Optional<PartidasModel> partidaOpt = partidasRepository.findById(partidaId);
        if (partidaOpt.isEmpty()) {
            throw new Exception("Partida no encontrada con ID: " + partidaId);
        }

        PartidasModel partidaOriginal = partidaOpt.get();

        // 2. Crear una copia de los valores originales para auditoría
        PartidasModel partidaAntigua = copiarPartida(partidaOriginal);
        List<DetallePartidaModel> detallesAntiguos = detallePartidaRepository.findByPartida_Id(partidaId);

        // 3. Validar que el período no esté cerrado
        if (nuevoIdPeriodo != null && !nuevoIdPeriodo.isEmpty()) {
            Integer periodoId = Integer.parseInt(nuevoIdPeriodo);
            Optional<PeriodoContableModel> periodoOpt = periodoRepository.findById(periodoId);
            if (periodoOpt.isPresent() && periodoOpt.get().getCerrado()) {
                throw new Exception("No se puede editar una partida en un período cerrado");
            }
        }

        // 4. Validar ecuación contable (Débito = Crédito)
        double totalDebito = 0.0;
        double totalCredito = 0.0;
        for (DetallePartidaModel detalle : nuevosDetalles) {
            totalDebito += detalle.getDebito();
            totalCredito += detalle.getCredito();
        }

        if (Math.abs(totalDebito - totalCredito) > 0.01) {
            throw new Exception("La partida no está balanceada. Débito: " + totalDebito + ", Crédito: " + totalCredito);
        }

        // 5. Validar mínimo 2 líneas de movimiento
        if (nuevosDetalles.size() < 2) {
            throw new Exception("Una partida debe tener al menos 2 líneas de movimiento (partida doble)");
        }

        // 6. Actualizar la partida principal
        partidaOriginal.setDescripcion(nuevaDescripcion);
        partidaOriginal.setFecha(nuevaFecha);
        partidaOriginal.setIdPeriodo(nuevoIdPeriodo);
        partidaOriginal.setUpdatedAt(LocalDateTime.now());
        partidaOriginal.setUpdatedBy(usuarioEditor);
        partidaOriginal.setEstado("EDITADA");

        PartidasModel partidaActualizada = partidasRepository.save(partidaOriginal);

        // 7. Eliminar detalles antiguos
        for (DetallePartidaModel detalleAntiguo : detallesAntiguos) {
            detallePartidaRepository.deleteById(detalleAntiguo.getId());
        }

        // 8. Guardar nuevos detalles
        for (DetallePartidaModel nuevoDetalle : nuevosDetalles) {
            nuevoDetalle.setPartida(partidaActualizada);
            detallePartidaRepository.save(nuevoDetalle);
        }

        // 9. Registrar en auditoría
        auditoriaService.registrarActualizacion(
            partidaAntigua,
            partidaActualizada,
            detallesAntiguos,
            nuevosDetalles,
            usuarioEditor,
            razonCambio,
            ipOrigen
        );

        return partidaActualizada;
    }

    /**
     * Crea una copia independiente de una partida para auditoría.
     */
    private PartidasModel copiarPartida(PartidasModel original) {
        PartidasModel copia = new PartidasModel();
        copia.setId(original.getId());
        copia.setDescripcion(original.getDescripcion());
        copia.setFecha(original.getFecha());
        copia.setIdPeriodo(original.getIdPeriodo());
        copia.setIdUsuario(original.getIdUsuario());
        copia.setCreatedAt(original.getCreatedAt());
        copia.setUpdatedAt(original.getUpdatedAt());
        copia.setCreatedBy(original.getCreatedBy());
        copia.setUpdatedBy(original.getUpdatedBy());
        copia.setEstado(original.getEstado());
        return copia;
    }

    /**
     * Valida que un usuario pueda editar una partida.
     * Solo el creador, un ADMIN o un CONTADOR pueden editar.
     */
    public boolean puedeEditarPartida(PartidasModel partida, String usuarioEditor, String rolUsuario) {
        // ADMIN siempre puede editar
        if ("ADMIN".equals(rolUsuario)) {
            return true;
        }

        // CONTADOR puede editar
        if ("CONTADOR".equals(rolUsuario)) {
            return true;
        }

        // El creador original puede editar su propia partida
        if (partida.getIdUsuario().equals(usuarioEditor)) {
            return true;
        }

        // AUDITOR NO puede editar
        return false;
    }
}
