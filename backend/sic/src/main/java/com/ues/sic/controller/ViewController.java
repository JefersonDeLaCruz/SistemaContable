package com.ues.sic.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class ViewController {
    

    @GetMapping("/")
    public String home(Model model){
        model.addAttribute("message", "Welcome to the SIC API");
        return "home";
    }

    @GetMapping("/error")
    public String error(Model model){
        model.addAttribute("status", 404);
        model.addAttribute("error", "Not Found");
        return "error";
    }

    // Método movido a AuthController para manejar parámetros de error/logout
    // @GetMapping("/login")
    // public String login(Model model){
    //     return "login";
    // }
}
