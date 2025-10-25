package com.ues.sic.contador;

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
@RequestMapping("/contador")
public class ContadorController {

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

    @GetMapping("/registrar-partida")
    public String registrarPartida(Model model) {
        UsuariosModel user = getAuthenticatedUser();
        
        if (!hasRequiredRole(user, "CONTADOR")) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol CONTADOR");
        }
        
        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Registrar Partida");
        return "contador/registrar-partida";
    }

    @GetMapping("/libro-diario")
    public String libroDiario(Model model) {
        UsuariosModel user = getAuthenticatedUser();
        
        if (!hasRequiredRole(user, "CONTADOR")) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol CONTADOR");
        }
        
        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Libro Diario");
        return "contador/libro-diario";
    }

    @GetMapping("/libro-mayor")
    public String libroMayor(Model model) {
        UsuariosModel user = getAuthenticatedUser();
        
        if (!hasRequiredRole(user, "CONTADOR")) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol CONTADOR");
        }
        
        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Libro Mayor");
        return "contador/libro-mayor";
    }

    @GetMapping("/balances")
    public String balances(Model model) {
        UsuariosModel user = getAuthenticatedUser();
        
        if (!hasRequiredRole(user, "CONTADOR")) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol CONTADOR");
        }
        
        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Balances");
        return "contador/balances";
    }

    @GetMapping("/subir-documento")
    public String subirDocumento(Model model) {
        UsuariosModel user = getAuthenticatedUser();
        
        if (!hasRequiredRole(user, "CONTADOR")) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol CONTADOR");
        }
        
        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Subir PDF");
        return "contador/subir-pdf";
    }

    @GetMapping("/configuracion")
    public String configuracion(Model model) {
        UsuariosModel user = getAuthenticatedUser();
        
        if (!hasRequiredRole(user, "CONTADOR")) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol CONTADOR");
        }
        
        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Configuración");
        return "contador/configuracion";
    }
}
