package com.itineraire.backend_api.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
// @CrossOrigin autorise le Frontend (qui tourne souvent sur le port 5173 avec Vite) à appeler ton API
@CrossOrigin(origins = "http://localhost:5173") 
public class StatusController {

    @GetMapping("/status")
    public Map<String, String> getStatus() {
        Map<String, String> response = new HashMap<>();
        // On utilise .put() pour ajouter des éléments dans une Map
        response.put("status", "OK");
        response.put("message", "Le serveur Java est prêt et configuré !");
        return response;
    }
}