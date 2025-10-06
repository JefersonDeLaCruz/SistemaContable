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

@Controller
public class AuthController {

    @Autowired
    private UsuariosRepository usuariosRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Obtener el usuario autenticado desde Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        UsuariosModel user = usuariosRepository.findByUsername(username);
        if (user != null) {
            model.addAttribute("usuario", user);
            model.addAttribute("titulo", "Dashboard");
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
