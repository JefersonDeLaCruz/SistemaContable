package com.ues.sic.auditor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ues.sic.usuarios.UsuariosModel;
import com.ues.sic.usuarios.UsuariosRepository;

@Controller
@RequestMapping("/auditor")
public class AuditorController {

    @Autowired
    private UsuariosRepository usuariosRepository;

    private UsuariosModel getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return usuariosRepository.findByUsername(username);
    }

    private boolean hasRequiredRole(UsuariosModel user, String requiredRole) {
        return user != null && requiredRole.equals(user.getRole());
    }

    @GetMapping("/explorador-partidas")
    public String exploradorPartidas(Model model) {
        UsuariosModel user = getAuthenticatedUser();
        
        if (!hasRequiredRole(user, "AUDITOR")) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol AUDITOR");
        }
        
        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Explorador de Partidas");
        return "auditor/explorador-partidas";
    }

    @GetMapping("/libro-diario")
    public String libroDiario(Model model) {
        UsuariosModel user = getAuthenticatedUser();
        
        if (!hasRequiredRole(user, "AUDITOR")) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol AUDITOR");
        }
        
        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Libro Diario");
        return "auditor/libro-diario";
    }

    @GetMapping("/libro-mayor")
    public String libroMayor(Model model) {
        UsuariosModel user = getAuthenticatedUser();
        
        if (!hasRequiredRole(user, "AUDITOR")) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol AUDITOR");
        }
        
        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Libro Mayor");
        return "auditor/libro-mayor";
    }

    @GetMapping("/ver-pdfs")
    public String verPdfs(Model model) {
        UsuariosModel user = getAuthenticatedUser();
        
        if (!hasRequiredRole(user, "AUDITOR")) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol AUDITOR");
        }
        
        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Ver PDFs Generados");
        return "auditor/ver-pdfs";
    }

    @GetMapping("/bitacora")
    public String bitacora(Model model) {
        UsuariosModel user = getAuthenticatedUser();
        
        if (!hasRequiredRole(user, "AUDITOR")) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol AUDITOR");
        }
        
        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Bitácora");
        return "auditor/bitacora";
    }

    @GetMapping("/balances")
    public String balances(Model model) {
        UsuariosModel user = getAuthenticatedUser();
        
        // Permitir acceso a AUDITOR y ADMIN
        if (!hasRequiredRole(user, "AUDITOR") && !hasRequiredRole(user, "ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol AUDITOR o ADMIN");
        }

        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Balances");
        return "auditor/balances";
    }

       

    @GetMapping("/configuracion")
    public String configuracion(Model model) {
        UsuariosModel user = getAuthenticatedUser();
        
        if (!hasRequiredRole(user, "AUDITOR")) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol AUDITOR");
        }
        
        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Configuración");
        return "auditor/configuracion";
    }
}
