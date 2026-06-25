package com.itineraire.backend_api.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itineraire.backend_api.dtos.StationDTO;
import com.itineraire.backend_api.dtos.TrajetDTO;
import com.itineraire.backend_api.models.Station;
import com.itineraire.backend_api.services.NetworkGraphService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173") // Autorise le Frontend Vue.js
public class ApiController {

    private final NetworkGraphService graphService;

    // Spring Boot injecte automatiquement ton service ici
    public ApiController(NetworkGraphService graphService) {
        this.graphService = graphService;
    }

    // 🚪 PORTE 1 : Envoyer la liste des stations (pour les menus déroulants du front)
    // URL : http://localhost:8080/api/stations
    @GetMapping("/stations")
    public List<StationDTO> getToutesLesStations() {
        // On utilise un Set (HashSet) basé sur le NOM pour éviter d'envoyer 
        // 5 fois "Châtelet" au frontend (vu qu'il y a plusieurs quais)
        Set<String> nomsVus = new HashSet<>();
        List<StationDTO> listeEpurée = new ArrayList<>();

        for (Station s : graphService.getNetworkGraph().vertexSet()) {
            if (!nomsVus.contains(s.getNom())) {
                nomsVus.add(s.getNom());
                // On envoie l'ID du premier quai trouvé et le nom
                listeEpurée.add(new StationDTO(s.getId(), s.getNom()));
            }
        }
        
        // On trie par ordre alphabétique pour que ce soit joli dans le menu déroulant !
        listeEpurée.sort(Comparator.comparing(StationDTO::getNom));
        return listeEpurée;
    }

    // 🚪 PORTE 2 : Calculer et envoyer l'itinéraire
    // URL : http://localhost:8080/api/itineraire?departId=IDFM:71497&arriveeId=IDFM:71502
    @GetMapping("/itineraire")
    public TrajetDTO getItineraire(@RequestParam String departId, @RequestParam String arriveeId) {
        
        // ⚠️ TODO pour le Membre 2 : 
        // C'est ICI qu'il faudra appeler le RoutingService avec l'algorithme de Dijkstra !
        
        // En attendant que le Membre 2 ait fini son code, on renvoie de fausses données 
        // pour que le Membre 4 (Frontend) puisse quand même avancer sur le design !
        List<String> faussesStations = Arrays.asList("Station Départ Factice", "Station Intermédiaire", "Station Arrivée Factice");
        return new TrajetDTO(faussesStations, 360.0);
    }
}