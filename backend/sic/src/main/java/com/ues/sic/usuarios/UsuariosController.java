package com.ues.sic.usuarios;

import java.util.List;
/* import java.util.Map;
 */
import org.springframework.beans.factory.annotation.Autowired;
/* import org.springframework.ui.Model; */
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
/* import org.springframework.web.bind.annotation.RequestParam; */
import org.springframework.web.bind.annotation.RestController;

/* import jakarta.servlet.http.HttpSession; */

@RestController
@RequestMapping("/api/usuarios")
public class UsuariosController {

    @Autowired
    private UsuariosRepository usuariosRepository;

    // get all usuarios
    @GetMapping
    public List<UsuariosModel> getAllUsuarios() {
        return usuariosRepository.findAll();
    }

    // get usuario by id
    @GetMapping("/{id}")
    public UsuariosModel getUsuarioById(@PathVariable Long id) {
        return usuariosRepository.findById(id).orElse(null);
    }

    // get usuario by username
    @GetMapping("/username/{username}")
    public UsuariosModel getUsuarioByUsername(@PathVariable String username) {
        return usuariosRepository.findByUsername(username);
    }

    // create usuario
    @PostMapping
    public UsuariosModel createUsuario(@RequestBody UsuariosModel usuario) {
        return usuariosRepository.save(usuario);
    }

    
   

}
