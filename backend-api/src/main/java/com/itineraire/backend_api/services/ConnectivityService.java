package com.itineraire.backend_api.services;

import java.text.Normalizer;

import org.jgrapht.Graph;
import org.springframework.stereotype.Service;

import com.itineraire.backend_api.models.Liaison;
import com.itineraire.backend_api.models.Station;

@Service
public class ConnectivityService {

    private final NetworkGraphService networkGraphService;

    public ConnectivityService(NetworkGraphService networkGraphService) {
        this.networkGraphService = networkGraphService;
    }

    private String normaliser(String texte) {
        if (texte == null) return "";
        return Normalizer.normalize(texte, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .trim().toLowerCase();
    }

    // Convertit un identifiant (ID de station comme dans stationIndex, ou nom comme dans /itineraire) en Station
    private Station trouverStation(Graph<Station, Liaison> graph, String identifiant) {
        if (identifiant == null) return null;

        for (Station s : graph.vertexSet()) {
            if (s.getId().equals(identifiant)) {
                return s;
            }
        }

        String identifiantNormalise = normaliser(identifiant);
        for (Station s : graph.vertexSet()) {
            if (normaliser(s.getNom()).equals(identifiantNormalise)) {
                return s;
            }
        }

        return null;
    }
}
