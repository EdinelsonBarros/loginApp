package org.example.loginapp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RegistrationPageController {

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
}