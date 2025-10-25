package com.ues.sic.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.ues.sic.partidas.PartidasRepository;
import com.ues.sic.usuarios.UsuariosRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.DayOfWeek;

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

    // Si quieres porcentaje: calcula max y también devuélvelo
    public int maxPartidasSemanaActual(Map<String, Integer> semana) {
        return semana.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    }
}
