package com.ues.sic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.ues.sic.usuarios.UsuariosModel;
import com.ues.sic.usuarios.UsuariosRepository;
import com.ues.sic.dtos.PartidaConDetallesDTO;
import com.ues.sic.partidas.PartidasModel;
import com.ues.sic.partidas.PartidasService;
import com.ues.sic.detalle_partida.DetallePartidaModel;
import com.ues.sic.detalle_partida.DetallePartidaService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@Controller
public class ViewController {

    @Autowired
    private UsuariosRepository usuariosRepository;
    
    @Autowired
    private PartidasService partidasService;
    
    @Autowired
    private DetallePartidaService detallePartidaService;


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
    public ResponseEntity<?> crearPartida(@ModelAttribute PartidaConDetallesDTO dto) {
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
            
            return ResponseEntity.ok("Partida creada exitosamente con ID: " + partidaGuardada.getId());
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al crear la partida: " + e.getMessage());
        }
    }
    
    // Método movido a AuthController para manejar parámetros de error/logout
    // @GetMapping("/login")
    // public String login(Model model){
    //     return "login";
    // }
}
