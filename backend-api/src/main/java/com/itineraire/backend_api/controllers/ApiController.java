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

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class ApiController {

    private final NetworkGraphService graphService;
    private final RoutageService routageService;

    public ApiController(NetworkGraphService graphService, RoutageService routageService) {
        this.graphService = graphService;
        this.routageService = routageService;
    }

    @GetMapping("/stations")
    public List<StationDTO> getToutesLesStations() {
        Set<String> nomsVus = new HashSet<>();
        List<StationDTO> listeEpurée = new ArrayList<>();

        for (Station s : graphService.getNetworkGraph().vertexSet()) {
            if (!nomsVus.contains(s.getNom())) {
                nomsVus.add(s.getNom());
                listeEpurée.add(new StationDTO(s.getId(), s.getNom()));
            }
        }

        listeEpurée.sort(Comparator.comparing(StationDTO::getNom));
        return listeEpurée;
    }

    @GetMapping("/itineraire")
    public ResponseEntity<?> getItineraire(@RequestParam String depart, @RequestParam String arrivee) {
        ItineraireResultat resultat = routageService.calculerItineraire(depart, arrivee);

        if (resultat == null) {
            return ResponseEntity.status(404)
                .body("Aucun chemin trouvé entre « " + depart + " » et « " + arrivee + " ».");
        }

        return ResponseEntity.ok(resultat);
    }
}
