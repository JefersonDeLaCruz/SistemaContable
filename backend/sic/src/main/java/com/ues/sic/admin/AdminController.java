package com.ues.sic.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ues.sic.usuarios.UsuariosModel;
import com.ues.sic.usuarios.UsuariosRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuariosRepository usuariosRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    private UsuariosModel getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return usuariosRepository.findByUsername(username);
    }

    private boolean hasRequiredRole(UsuariosModel user, String requiredRole) {
        return user != null && requiredRole.equals(user.getRole());
    }

    @GetMapping("/crear-usuario")
    public String mostrarFormularioCrearUsuario(Model model) {
        return "admin/crear-usuario";
    }

    @PostMapping("/crear-usuario")
    public String crearUsuario(@RequestParam String username, 
                              @RequestParam String email,
                              @RequestParam String password,
                              @RequestParam String role,
                              Model model) {
        
        try {
            // Verificar si el usuario ya existe
            if (usuariosRepository.existsByUsernameIgnoreCase(username)) {
                model.addAttribute("error", "El nombre de usuario ya existe");
                return "admin/crear-usuario";
            }
            
            if (usuariosRepository.existsByEmailIgnoreCase(email)) {
                model.addAttribute("error", "El email ya existe");
                return "admin/crear-usuario";
            }
            
            // Crear nuevo usuario
            UsuariosModel nuevoUsuario = new UsuariosModel();
            nuevoUsuario.setUsername(username);
            nuevoUsuario.setEmail(email);
            nuevoUsuario.setPassword(passwordEncoder.encode(password));
            nuevoUsuario.setRole(role);
            nuevoUsuario.setActive(true);
            
            // Establecer timestamps
            String currentTimestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            nuevoUsuario.setCreatedAt(currentTimestamp);
            nuevoUsuario.setUpdatedAt(currentTimestamp);
            
            usuariosRepository.save(nuevoUsuario);
            
            model.addAttribute("success", "Usuario creado exitosamente");
            System.out.println("Nuevo usuario creado: " + username + " con rol: " + role);
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al crear usuario: " + e.getMessage());
            System.err.println("Error creando usuario: " + e.getMessage());
        }
        
        return "admin/crear-usuario";
    }
    
    @GetMapping("/listar-usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuariosRepository.findAll());
        return "admin/listar-usuarios";
    }
    

    // ===== FUNCIONES ADMINISTRATIVAS CONSOLIDADAS =====

    @GetMapping("/explorador-partidas")
    public String exploradorPartidas(Model model) {
        UsuariosModel user = getAuthenticatedUser();
        
        if (!hasRequiredRole(user, "ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol ADMIN");
        }
        
        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Explorador de Partidas");
        return "admin/explorador-partidas";
    }

    @GetMapping("/libro-diario")
    public String libroDiario(Model model) {
        UsuariosModel user = getAuthenticatedUser();
        
        if (!hasRequiredRole(user, "ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol ADMIN");
        }
        
        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Libro Diario");
        return "admin/libro-diario";
    }

    @GetMapping("/libro-mayor")
    public String libroMayor(Model model) {
        UsuariosModel user = getAuthenticatedUser();
        
        if (!hasRequiredRole(user, "ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol ADMIN");
        }
        
        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Libro Mayor");
        return "admin/libro-mayor";
    }

    @GetMapping("/ver-pdfs")
    public String verPdfs(Model model) {
        UsuariosModel user = getAuthenticatedUser();
        
        if (!hasRequiredRole(user, "ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol ADMIN");
        }
        
        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Ver PDFs Generados");
        return "admin/ver-pdfs";
    }
    
    @GetMapping("/balance-comprobacion")
    public String explorarPartidas(Model model) {
        UsuariosModel user = getAuthenticatedUser();
        
        if (!hasRequiredRole(user, "ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol ADMIN");
        }

        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Explorar Partidas");
        return "admin/balance-comprobacion";
    }

    @GetMapping("/balance-general")
    public String balanceGeneral(Model model) {
        UsuariosModel user = getAuthenticatedUser();
        
        if (!hasRequiredRole(user, "ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol ADMIN");
        }

        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Balance General");
        return "admin/balance-general";
    }

    @GetMapping("/bitacora")
    public String bitacora(Model model) {
        UsuariosModel user = getAuthenticatedUser();
        
        if (!hasRequiredRole(user, "ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol ADMIN");
        }
        
        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Bitácora");
        return "admin/bitacora";
    }

    @GetMapping("/configuracion")
    public String configuracion(Model model) {
        UsuariosModel user = getAuthenticatedUser();
        
        if (!hasRequiredRole(user, "ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol ADMIN");
        }
        
        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Configuración");
        return "admin/configuracion";
    }

    @GetMapping("/usuarios")
    public String gestionUsuarios(Model model) {
        UsuariosModel user = getAuthenticatedUser();
        
        if (!hasRequiredRole(user, "ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: Se requiere rol ADMIN");
        }
        
        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Gestión de Usuarios");
        model.addAttribute("usuarios", usuariosRepository.findAll());
        return "admin/usuarios";
    }
}