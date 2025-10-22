package com.example.taller1seguridadinformatica.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
public class TestController {

    @GetMapping("/api/protected")
    public String protectedEndpoint(Principal principal) {
        return "Hola " + (principal != null ? principal.getName() : "anon");
    }
}
