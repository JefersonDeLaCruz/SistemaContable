package com.ues.sic.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ues.sic.usuarios.UsuariosModel;
import com.ues.sic.usuarios.UsuariosRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Autowired
    private UsuariosRepository usuariosRepository;



    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        UsuariosModel user = (UsuariosModel) session.getAttribute("usuarioActivo");
        if (user != null) {
            model.addAttribute("usuario", user);
            return "dashboard";
        }
        return "redirect:/login";
    }

    // login
    // requestbody es para recibir un json(usar fetch)
    // requestparam es para recibir datos de un formulario (html)
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model,
            HttpSession session) {

        // String username = data.get("username");
        // String password = data.get("password");

        UsuariosModel user = usuariosRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            // return user;
            session.setAttribute("usuarioActivo", user);
            System.out.println("user logged in: " + user.getUsername());
            return "redirect:/dashboard"; // redireccionar a dashboard
        }
        model.addAttribute("error", "usuario o contraseña incorrecta");
        return "/login"; // redireccionar a login
    }
}
