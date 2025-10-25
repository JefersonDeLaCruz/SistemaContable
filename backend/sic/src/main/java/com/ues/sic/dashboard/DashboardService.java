package com.ues.sic.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

import com.ues.sic.partidas.PartidasRepository;
import com.ues.sic.usuarios.UsuariosRepository;
import com.ues.sic.detalle_partida.DetallePartidaRepository;
import java.time.LocalDate;
import java.time.YearMonth;


@Service
public class DashboardService {
    @Autowired
    private UsuariosRepository usuariosRepository;

    public long contarUsuariosActivos() {
        return usuariosRepository.countByActive(true);
    }

    @Autowired
    private PartidasRepository partidasRepository;

    public long contarPartidasMesActual() {
        YearMonth ym = YearMonth.now();
        LocalDate inicio = ym.atDay(1);
        LocalDate fin = ym.atEndOfMonth();
        return partidasRepository.findByFechaBetween(inicio.toString(), fin.toString()).size();
    }

    public Map<String, Integer> contarPartidasSemanaActual() {
        LocalDate hoy = LocalDate.now();
        LocalDate lunes = hoy.with(java.time.DayOfWeek.MONDAY);
        LocalDate domingo = hoy.with(java.time.DayOfWeek.SUNDAY);
        List<Object[]> filas = partidasRepository.countPorDiaEntre(lunes.toString(), domingo.toString());

        Map<LocalDate, Integer> porFecha = new HashMap<>();
        for (Object[] row : filas) {
            LocalDate dia = ((java.sql.Date) row[0]).toLocalDate();
            int cantidad = ((Number) row[1]).intValue();
            porFecha.put(dia, cantidad);
        }

        // Mapa ordenado Lunes a Domingo
        LinkedHashMap<String, Integer> semana = new LinkedHashMap<>();
        LocalDate cursor = lunes;
        while (!cursor.isAfter(domingo)) {
            String nombre = switch (cursor.getDayOfWeek()) {
                case MONDAY -> "Lunes";
                case TUESDAY -> "Martes";
                case WEDNESDAY -> "Miércoles";
                case THURSDAY -> "Jueves";
                case FRIDAY -> "Viernes";
                case SATURDAY -> "Sábado";
                case SUNDAY -> "Domingo";
            };
            semana.put(nombre, porFecha.getOrDefault(cursor, 0));
            cursor = cursor.plusDays(1);
        }

        // Normalizar claves sin acentos para evitar problemas de codificación en la vista
        if (semana.containsKey("MiǸrcoles")) {
            semana.put("Miercoles", semana.get("MiǸrcoles"));
        }
        if (semana.containsKey("Sǭbado")) {
            semana.put("Sabado", semana.get("Sǭbado"));
        }
        return semana;
    }

    public int maxPartidasSemanaActual(Map<String, Integer> semana) {
        return semana.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    //sacar el total
    public long totalPartidasSemanaActual(){
        Map<String,Integer> semana = contarPartidasSemanaActual();
        long total = 0;
        for(Integer conteo: semana.values()){
            total +=conteo;
        }    
        return total;
    }

    @Autowired
    private DetallePartidaRepository detallePartidaRepository;

    public List<MovimientoRecienteDTO> ultimosMovimientos(int limite) {
        List<Object[]> filas = detallePartidaRepository.ultimosMovimientos(limite);
        List<MovimientoRecienteDTO> out = new ArrayList<>();
        for (Object[] r : filas) {
            String fecha = ((java.sql.Date) r[0]).toLocalDate().toString();
            Long partidaId = ((Number) r[1]).longValue();
            String cuenta = (String) r[2];
            Double debito = r[3] != null ? ((Number) r[3]).doubleValue() : 0.0;
            Double credito = r[4] != null ? ((Number) r[4]).doubleValue() : 0.0;
            String usuario = (String) r[5];
            out.add(new MovimientoRecienteDTO(fecha, partidaId, cuenta, debito, credito, usuario));
        }
        return out;
    }

    public List<MiMovimientoDTO> misMovimientosRecientes(String usuario, int limite) {
        List<Object[]> filas = detallePartidaRepository.misMovimientosRecientes(usuario, limite);
        List<MiMovimientoDTO> out = new ArrayList<>();
        for (Object[] r : filas) {
            String fecha = ((java.sql.Date) r[0]).toLocalDate().toString();
            Long partidaId = ((Number) r[1]).longValue();
            String descripcion = (String) r[2];
            String cuenta = (String) r[3];
            Double debito = r[4] != null ? ((Number) r[4]).doubleValue() : 0.0;
            Double credito = r[5] != null ? ((Number) r[5]).doubleValue() : 0.0;
            String estado = debito > 0 ? "Debito" : (credito > 0 ? "Credito" : "-");
            out.add(new MiMovimientoDTO(fecha, partidaId, descripcion, cuenta, debito, credito, estado));
        }
        return out;
    }

    public List<MiMovimientoDTO> misMovimientosRecientesPara(String username, String userId, int limite) {
        List<Object[]> filas = detallePartidaRepository.misMovimientosRecientesDe(username, userId, limite);
        List<MiMovimientoDTO> out = new ArrayList<>();
        for (Object[] r : filas) {
            String fecha = ((java.sql.Date) r[0]).toLocalDate().toString();
            Long partidaId = ((Number) r[1]).longValue();
            String descripcion = (String) r[2];
            String cuenta = (String) r[3];
            Double debito = r[4] != null ? ((Number) r[4]).doubleValue() : 0.0;
            Double credito = r[5] != null ? ((Number) r[5]).doubleValue() : 0.0;
            String estado = debito > 0 ? "Debito" : (credito > 0 ? "Credito" : "-");
            out.add(new MiMovimientoDTO(fecha, partidaId, descripcion, cuenta, debito, credito, estado));
        }
        return out;
    }

    public double mayorMovimientoHoy(){
        String hoy = java.time.LocalDate.now().toString();
        Double max = detallePartidaRepository.maxMovimientoHoy(hoy);
        return (max != null) ? max : 0.0;
    }

    public double ingresoDiario(){
        String hoy = java.time.LocalDate.now().toString();
        Double total = detallePartidaRepository.ingresoDiario(hoy);
        return (total != null) ? total : 0.0;
    }

    public double gastoDiario(){
        String hoy = java.time.LocalDate.now().toString();
        Double total = detallePartidaRepository.gastoDiario(hoy);
        return (total != null) ? total : 0.0;
    }

    public long partidasHoyTotal(){
        String hoy = java.time.LocalDate.now().toString();
        return partidasRepository.countHoy(hoy);
    }

    public long partidasHoyDe(String usuario){
        String hoy = java.time.LocalDate.now().toString();
        return partidasRepository.countHoyPorUsuario(hoy, usuario);
    }


}
