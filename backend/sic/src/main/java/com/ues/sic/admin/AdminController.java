package com.ues.sic.usuarios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuariosRepository usuariosRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

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
}