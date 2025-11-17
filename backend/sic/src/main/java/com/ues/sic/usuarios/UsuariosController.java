package com.ues.sic.usuarios;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuariosController {

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String nowIso() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private UsuariosModel getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        return usuariosRepository.findByUsername(auth.getName());
    }

    // Listado de usuarios
    @GetMapping
    public List<UsuariosModel> getAllUsuarios() {
        return usuariosRepository.findAll();
    }

    // Obtener usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUsuarioById(@PathVariable Long id) {
        return usuariosRepository.findById(id)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado"));
    }

    // Obtener usuario por username
    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUsuarioByUsername(@PathVariable String username) {
        UsuariosModel u = usuariosRepository.findByUsername(username);
        if (u == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        return ResponseEntity.ok(u);
    }

    // Crear usuario (API)
    @PostMapping
    public ResponseEntity<?> createUsuario(@RequestBody UsuariosModel usuario) {
        try {
            if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
                return ResponseEntity.badRequest().body("El nombre de usuario es obligatorio");
            }
            if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
                return ResponseEntity.badRequest().body("El email es obligatorio");
            }
            if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
                return ResponseEntity.badRequest().body("La contraseña es obligatoria");
            }
            if (usuariosRepository.existsByUsernameIgnoreCase(usuario.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("El nombre de usuario ya existe");
            }
            if (usuariosRepository.existsByEmailIgnoreCase(usuario.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("El email ya existe");
            }

            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            if (usuario.getActive() == null) usuario.setActive(true);
            String ts = nowIso();
            usuario.setCreatedAt(ts);
            usuario.setUpdatedAt(ts);

            UsuariosModel creado = usuariosRepository.save(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear usuario: " + e.getMessage());
        }
    }

    // Actualizar usuario (maneja cambio de rol/estado propio)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUsuario(@PathVariable Long id, @RequestBody UsuariosModel body) {
        var opt = usuariosRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");

        var existente = opt.get();

        // Validaciones de unicidad
        if (body.getUsername() != null && !body.getUsername().equalsIgnoreCase(existente.getUsername())) {
            if (usuariosRepository.existsByUsernameIgnoreCaseAndIdNot(body.getUsername(), id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("El nombre de usuario ya existe");
            }
            existente.setUsername(body.getUsername());
        }
        if (body.getEmail() != null && !body.getEmail().equalsIgnoreCase(existente.getEmail())) {
            if (usuariosRepository.existsByEmailIgnoreCaseAndIdNot(body.getEmail(), id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("El email ya existe");
            }
            existente.setEmail(body.getEmail());
        }

        // Rol y estado con restricciones
        String rolAnterior = existente.getRole();
        boolean requestedDeactivate = body.getActive() != null && Boolean.FALSE.equals(body.getActive());
        UsuariosModel authUser = getAuthenticatedUser();

        if (requestedDeactivate) {
            boolean isSelf = (authUser != null && authUser.getId().equals(existente.getId()));
            boolean targetIsAdmin = "ADMIN".equalsIgnoreCase(rolAnterior);
            boolean authIsAdmin = authUser != null && "ADMIN".equalsIgnoreCase(authUser.getRole());

            // No permitir que el admin activo se desactive a sí mismo ni desactive a otros admins
            if ((isSelf && authIsAdmin) || targetIsAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("No está permitido desactivar usuarios con rol ADMIN ni desactivarse a sí mismo.");
            }
        }

        if (body.getRole() != null && !body.getRole().isBlank()) {
            existente.setRole(body.getRole());
        }
        if (body.getActive() != null) {
            existente.setActive(body.getActive());
        }

        // Contraseña: si viene vacía o null, se mantiene
        if (body.getPassword() != null && !body.getPassword().isBlank()) {
            existente.setPassword(passwordEncoder.encode(body.getPassword()));
        }

        existente.setUpdatedAt(nowIso());
        usuariosRepository.save(existente);

        // Detectar si el usuario autenticado se degradó o desactivó
        boolean mustLogout = false;
        if (authUser != null && authUser.getId().equals(existente.getId())) {
            if (!"ADMIN".equalsIgnoreCase(existente.getRole()) || Boolean.FALSE.equals(existente.getActive())) {
                mustLogout = true;
            }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("usuario", existente);
        resp.put("mustLogout", mustLogout);
        return ResponseEntity.ok(resp);
    }

    // Eliminar usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUsuario(@PathVariable Long id) {
        var opt = usuariosRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");

        UsuariosModel aEliminar = opt.get();
        UsuariosModel authUser = getAuthenticatedUser();
        boolean mustLogout = authUser != null && authUser.getId().equals(aEliminar.getId());

        usuariosRepository.deleteById(id);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("mustLogout", mustLogout);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/activos/count")
    public long getActiveCount(){
        return usuariosRepository.countByActive(true);
    }
}
