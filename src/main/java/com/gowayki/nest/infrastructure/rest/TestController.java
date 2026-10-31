package com.gowayki.nest.infrastructure.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/estado")
    public String estado() {
        return "GoWayki Node Backend - ¡Infraestructura lista para recibir coordenadas!";
    }
}
