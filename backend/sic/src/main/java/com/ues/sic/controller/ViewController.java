package com.ues.sic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ues.sic.usuarios.UsuariosModel;
import com.ues.sic.usuarios.UsuariosRepository;
import com.ues.sic.dtos.PartidaConDetallesDTO;
import com.ues.sic.partidas.PartidasModel;
import com.ues.sic.partidas.PartidasService;
import com.ues.sic.detalle_partida.DetallePartidaModel;
import com.ues.sic.detalle_partida.DetallePartidaService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Controller
public class ViewController {

    @Autowired
    private UsuariosRepository usuariosRepository;
    
    @Autowired
    private PartidasService partidasService;
    
    @Autowired
    private DetallePartidaService detallePartidaService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("/")
    public String home(){
        return "redirect:/login";
    }

    @GetMapping("/error")
    public String error(Model model){
        model.addAttribute("status", 404);
        model.addAttribute("error", "Not Found");
        return "error";
    }


    @GetMapping("/registrar-partida")
    public String registrarPartida(Model model){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        UsuariosModel user = usuariosRepository.findByUsername(username);

        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Registrar Partida");
        return "registrar-partida";
    }

    @GetMapping("/libro-mayor")
    public String verLibroMayor(Model model){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        UsuariosModel user = usuariosRepository.findByUsername(username);

        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Libro Mayor");
        return "libro-mayor";
    }


    @GetMapping("/configuracion")
    public String configuracion(Model model){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        UsuariosModel user = usuariosRepository.findByUsername(username);

        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Configuración");
        return "configuracion";
    }

    @PostMapping("/configuracion/actualizar")
    public String actualizarConfiguracion(
            @RequestParam("username") String newUsername,
            @RequestParam("email") String newEmail,
            @RequestParam(value = "role", required = false) String newRole,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Obtener usuario actual
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = auth.getName();
            UsuariosModel user = usuariosRepository.findByUsername(currentUsername);
            
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
                return "redirect:/configuracion";
            }
            
            boolean usernameChanged = false;
            
            // Validar username único (excluyendo el usuario actual)
            if (!user.getUsername().equalsIgnoreCase(newUsername)) {
                if (usuariosRepository.existsByUsernameIgnoreCaseAndIdNot(newUsername, user.getId())) {
                    redirectAttributes.addFlashAttribute("error", "El nombre de usuario ya está en uso");
                    return "redirect:/configuracion";
                }
                usernameChanged = true;
            }
            
            // Validar email único (excluyendo el usuario actual)
            if (!user.getEmail().equalsIgnoreCase(newEmail)) {
                if (usuariosRepository.existsByEmailIgnoreCaseAndIdNot(newEmail, user.getId())) {
                    redirectAttributes.addFlashAttribute("error", "El correo electrónico ya está en uso");
                    return "redirect:/configuracion";
                }
            }
            
            // Actualizar campos
            user.setUsername(newUsername);
            user.setEmail(newEmail);
            
            // Solo permitir cambio de rol si el usuario es ADMIN
            if (user.getRole().equals("ADMIN") && newRole != null && !newRole.isEmpty()) {
                user.setRole(newRole);
            }
            
            // Actualizar timestamp
            user.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            // Guardar cambios
            usuariosRepository.save(user);
            
            // Si cambió el username, forzar logout y re-login
            if (usernameChanged) {
                redirectAttributes.addFlashAttribute("info", "Usuario actualizado. Por favor, inicia sesión nuevamente con tu nuevo nombre de usuario.");
                SecurityContextHolder.clearContext();
                return "redirect:/login?username_changed=true";
            }
            
            redirectAttributes.addFlashAttribute("success", "Configuración actualizada exitosamente");
            return "redirect:/configuracion";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al actualizar la configuración: " + e.getMessage());
            return "redirect:/configuracion";
        }
    }

    @PostMapping("/configuracion/cambiar-password")
    public String cambiarPassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Obtener usuario actual
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            UsuariosModel user = usuariosRepository.findByUsername(username);
            
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
                return "redirect:/configuracion";
            }
            
            // Verificar contraseña actual
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "La contraseña actual es incorrecta");
                return "redirect:/configuracion";
            }
            
            // Validar longitud mínima de nueva contraseña
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "La nueva contraseña debe tener al menos 6 caracteres");
                return "redirect:/configuracion";
            }
            
            // Actualizar contraseña
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            // Guardar cambios
            usuariosRepository.save(user);
            
            redirectAttributes.addFlashAttribute("success", "Contraseña actualizada exitosamente");
            return "redirect:/configuracion";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al cambiar la contraseña: " + e.getMessage());
            return "redirect:/configuracion";
        }
    }


    @GetMapping("/subir-documento")
    public String subirDocumento(Model model){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        UsuariosModel user = usuariosRepository.findByUsername(username);

        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Subir Documento");
        return "subir-documento";
    }


       
    




    @GetMapping("/libro-diario")
    public String libroDiario(Model model){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        UsuariosModel user = usuariosRepository.findByUsername(username);

        model.addAttribute("usuario", user);
        model.addAttribute("titulo", "Libro Diario");
        return "libro-diario";
    }

    @PostMapping("/partida")
    public String crearPartida(@ModelAttribute PartidaConDetallesDTO dto) {
        try {
            System.out.println("=== DEBUG: Datos recibidos ===");
            System.out.println("Descripción: " + dto.getDescripcion());
            System.out.println("Período: " + dto.getIdPeriodo());
            System.out.println("Número de detalles: " + dto.getDetalles().size());
            
            // Debug adicional: mostrar cada detalle
            for (int i = 0; i < dto.getDetalles().size(); i++) {
                var detalle = dto.getDetalles().get(i);
                System.out.println("Detalle " + i + ":");
                System.out.println("  - ID Cuenta: " + detalle.getIdCuenta());
                System.out.println("  - Débito: " + detalle.getDebito());
                System.out.println("  - Crédito: " + detalle.getCredito());
            }
            
            // Obtener usuario actual
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            UsuariosModel user = usuariosRepository.findByUsername(username);
            
            // Preparar partida
            PartidasModel partida = dto.getPartida();
            partida.setIdUsuario(user.getId().toString());
            partida.setFecha(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            
            // Guardar partida y generar ID
            PartidasModel partidaGuardada = partidasService.save(partida);
            System.out.println("Partida guardada con ID: " + partidaGuardada.getId());
            
            // Guardar detalles
            for (DetallePartidaModel detalle : dto.getDetallesAsModel()) {
                detalle.setPartida(partidaGuardada);
                // Si no hay descripción en el detalle, usar la descripción de la partida
                if (detalle.getDescripcion() == null || detalle.getDescripcion().isEmpty()) {
                    detalle.setDescripcion(partida.getDescripcion());
                }
                detallePartidaService.save(detalle);
                System.out.println("Detalle guardado: " + detalle.getIdCuenta() + " - Débito: " + detalle.getDebito() + " - Crédito: " + detalle.getCredito());
            }
            
            return "redirect:/subir-documento?partidaId=" + partidaGuardada.getId();
            
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/registrar-partida";
        }
    }
    
    // Método movido a AuthController para manejar parámetros de error/logout
    // @GetMapping("/login")
    // public String login(Model model){
    //     return "login";
    // }
}
