package com.itineraire.backend_api.controllers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itineraire.backend_api.dtos.StationDTO;
import com.itineraire.backend_api.models.ItineraireResultat;
import com.itineraire.backend_api.models.Station;
import com.itineraire.backend_api.services.NetworkGraphService;
import com.itineraire.backend_api.services.RoutageService;
import com.itineraire.backend_api.services.AcpmService;
import com.itineraire.backend_api.services.ConnectivityService;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class ApiController {

    private final NetworkGraphService graphService;
    private final RoutageService routageService;
    private final AcpmService acpmService;
    private final ConnectivityService connectivityService;

    public ApiController(NetworkGraphService graphService, RoutageService routageService, AcpmService acpmService, ConnectivityService connectivityService) {
        this.graphService = graphService;
        this.routageService = routageService;
        this.acpmService = acpmService;
        this.connectivityService = connectivityService;
    }

    @GetMapping("/stations")
    public List<String> getToutesLesStations() {
        return graphService.getToutesLesStations();
    }

    @GetMapping("/itineraire")
    public ResponseEntity<?> getItineraire(
            @RequestParam String depart, 
            @RequestParam String arrivee,
            @RequestParam(required = false) String date) {
        try {
            ItineraireResultat resultat = routageService.calculerItineraire(depart, arrivee, date);

            if (resultat == null) {
                return ResponseEntity.status(404)
                    .body("Aucun chemin trouvé entre « " + depart + " » et « " + arrivee + " ».");
            }

            return ResponseEntity.ok(resultat);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/acpm")
    public ResponseEntity<List<java.util.Map<String, Object>>> getAcpm() {
        return ResponseEntity.ok(acpmService.calculerACPM());
    }

    @GetMapping("/connexite")
    public ResponseEntity<java.util.Map<String, Object>> getConnexite() {
        return ResponseEntity.ok(graphService.verifierConnexite());
    }

    @GetMapping("/bfs")
    public ResponseEntity<?> getBfs(@RequestParam String depart) {
        try {
            List<String> ordreVisite = connectivityService.bfs(depart);
            return ResponseEntity.ok(ordreVisite);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/dfs")
    public ResponseEntity<?> getDfs(@RequestParam String depart) {
        try {
            List<String> ordreVisite = connectivityService.dfs(depart);
            return ResponseEntity.ok(ordreVisite);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}
