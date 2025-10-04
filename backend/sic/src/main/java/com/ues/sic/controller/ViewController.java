package com.ues.sic.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ViewController {
    

    @GetMapping("/")
    public String home(){
        return "Welcome to the Sistema contable API";
    }
}
