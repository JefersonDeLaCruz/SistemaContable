package com.ues.sic.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ues.sic.usuarios.UsuariosModel;
import com.ues.sic.usuarios.UsuariosRepository;
import com.ues.sic.partidas.PartidasModel;
import com.ues.sic.partidas.PartidasService;
import com.ues.sic.detalle_partida.DetallePartidaModel;
import com.ues.sic.detalle_partida.DetallePartidaService;
import com.ues.sic.cuentas.CuentaModel;
import com.ues.sic.dashboard.DashboardService;
import com.ues.sic.cuentas.CuentaRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class AuthController {

    @Autowired
    private UsuariosRepository usuariosRepository;
    
    @Autowired
    private PartidasService partidasService;
    
    @Autowired
    private DetallePartidaService detallePartidaService;
    
    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Obtener el usuario autenticado desde Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        long nUsuarios = dashboardService.contarUsuariosActivos();
        long partidasMes = dashboardService.contarPartidasMesActual();
        UsuariosModel user = usuariosRepository.findByUsername(username);
        long totalSemana = dashboardService.totalPartidasSemanaActual();
        Double mayorMovHoy = dashboardService.mayorMovimientoHoy();

        Map<String, Integer> semana = dashboardService.contarPartidasSemanaActual();
        int semanaMax = dashboardService.maxPartidasSemanaActual(semana);
        if (user != null) {
            model.addAttribute("usuario", user);
            model.addAttribute("titulo", "Dashboard");
            
            // Obtener todas las partidas y detalles para debug
            List<PartidasModel> todasLasPartidas = partidasService.findAll();
            List<DetallePartidaModel> todosLosDetalles = detallePartidaService.findAll();
            
            // Obtener todas las cuentas y crear un mapa ID -> Cuenta
            List<CuentaModel> todasLasCuentas = cuentaRepository.findAll();
            Map<String, CuentaModel> mapaCuentas = todasLasCuentas.stream()
                .collect(Collectors.toMap(
                    cuenta -> cuenta.getIdCuenta().toString(),
                    cuenta -> cuenta
                ));
            
            model.addAttribute("partidas", todasLasPartidas);
            model.addAttribute("detalles", todosLosDetalles);
            model.addAttribute("mapaCuentas", mapaCuentas);
            model.addAttribute("usuariosActivos", nUsuarios);
            model.addAttribute("partidasMesActual", partidasMes);
            model.addAttribute("semana", semana);
            model.addAttribute("semanaMax", semanaMax);
            model.addAttribute("totalSemana", totalSemana);
            model.addAttribute("mayorMovimientoHoy", mayorMovHoy);
            
            model.addAttribute("movimientosRecientes", dashboardService.ultimosMovimientos(7));
                return "dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        
        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrecta");
        }
        
        if (logout != null) {
            model.addAttribute("message", "Ha cerrado sesión exitosamente");
        }
        
        return "login";
    }
}
