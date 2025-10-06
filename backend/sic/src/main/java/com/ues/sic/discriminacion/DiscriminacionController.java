package com.ues.sic.discriminacion;

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
@RequestMapping("/discriminacion")
public class DiscriminacionController {

    @Autowired
    private UsuariosRepository usuariosRepository;

    @GetMapping("/dashboard")
    public String dashboardDiscriminado(Model model) {
        // Obtener el usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        UsuariosModel user = usuariosRepository.findByUsername(username);
        if (user != null) {
            model.addAttribute("usuario", user);
            return "discriminacion/dashboard-discriminado";
        }
        return "redirect:/login";
    }

    @GetMapping("/admin")
    public String funcionesAdmin(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        System.out.println("DEBUG: Usuario intentando acceder a /admin: " + username);
        
        UsuariosModel user = usuariosRepository.findByUsername(username);
        if (user != null) {
            System.out.println("DEBUG: Rol del usuario: " + user.getRole());
            // Verificar que el usuario tenga rol ADMIN
            if (!"ADMIN".equals(user.getRole())) {
                System.out.println("DEBUG: Acceso denegado - Usuario no es ADMIN");
                throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol ADMIN");
            }
            model.addAttribute("usuario", user);
            return "discriminacion/funciones-admin";
        }
        return "redirect:/login";
    }

    @GetMapping("/contador")
    public String funcionesContador(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        UsuariosModel user = usuariosRepository.findByUsername(username);
        if (user != null) {
            // Verificar que el usuario tenga rol CONTADOR
            if (!"CONTADOR".equals(user.getRole())) {
                throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol CONTADOR");
            }
            model.addAttribute("usuario", user);
            return "discriminacion/funciones-contador";
        }
        return "redirect:/login";
    }

    @GetMapping("/auditor")
    public String funcionesAuditor(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        UsuariosModel user = usuariosRepository.findByUsername(username);
        if (user != null) {
            // Verificar que el usuario tenga rol AUDITOR
            if (!"AUDITOR".equals(user.getRole())) {
                throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol AUDITOR");
            }
            model.addAttribute("usuario", user);
            return "discriminacion/funciones-auditor";
        }
        return "redirect:/login";
    }
}