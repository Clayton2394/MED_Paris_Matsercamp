package com.itineraire.backend_api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itineraire.backend_api.models.ItineraireResultat;
import com.itineraire.backend_api.services.RoutageService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class ItineraireController {

    private final RoutageService routageService;

    public ItineraireController(RoutageService routageService) {
        this.routageService = routageService;
    }

    @GetMapping("/itineraire")
    public ResponseEntity<?> getItineraire(
            @RequestParam String depart,
            @RequestParam String arrivee) {

        ItineraireResultat resultat = routageService.calculerItineraire(depart, arrivee);

        if (resultat == null) {
            return ResponseEntity.status(404)
                .body("Aucun chemin trouvé entre « " + depart + " » et « " + arrivee + " ».");
        }

        return ResponseEntity.ok(resultat);
    }
}