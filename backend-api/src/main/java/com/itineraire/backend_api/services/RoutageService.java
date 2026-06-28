package com.itineraire.backend_api.services;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.springframework.stereotype.Service;

import com.itineraire.backend_api.models.ItineraireResultat;
import com.itineraire.backend_api.models.Liaison;
import com.itineraire.backend_api.models.Station;
import com.itineraire.backend_api.models.StationDTO;

@Service
public class RoutageService {

    private final NetworkGraphService networkGraphService;

    public RoutageService(NetworkGraphService networkGraphService) {
        this.networkGraphService = networkGraphService;
    }

    public ItineraireResultat calculerItineraire(String nomDepart, String nomArrivee) {
        Graph<Station, Liaison> graph = networkGraphService.getNetworkGraph();

        Station stationDepart = trouverStation(graph, nomDepart);
        Station stationArrivee = trouverStation(graph, nomArrivee);

        if (stationDepart == null) {
            throw new IllegalArgumentException("Station de départ inconnue : " + nomDepart);
        }
        if (stationArrivee == null) {
            throw new IllegalArgumentException("Station d'arrivée inconnue : " + nomArrivee);
        }

        long startTime = System.currentTimeMillis();
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long startMemory = runtime.totalMemory() - runtime.freeMemory();

        DijkstraShortestPath<Station, Liaison> dijkstra = new DijkstraShortestPath<>(graph);
        GraphPath<Station, Liaison> path = dijkstra.getPath(stationDepart, stationArrivee);

        long endTime = System.currentTimeMillis();
        long endMemory = runtime.totalMemory() - runtime.freeMemory();

        long duree = endTime - startTime;
        long memoireUtilisee = Math.max(0, endMemory - startMemory);

        if (path == null) {
            return null;
        }

        List<StationDTO> chemin = new ArrayList<>();
        String dernierNom = null;
        for (Station s : path.getVertexList()) {
            if (!s.getNom().equals(dernierNom)) {
                chemin.add(new StationDTO(s.getNom(), s.getLat(), s.getLon()));
                dernierNom = s.getNom();
            }
        }

        ItineraireResultat resultat = new ItineraireResultat(chemin, path.getWeight());
        resultat.setTempsCalculMs(duree);
        resultat.setMemoireUtiliseeBytes(memoireUtilisee);
        
        return resultat;
    }

    // --- NOUVELLE MÉTHODE DE NORMALISATION ---
    private String normaliser(String texte) {
        if (texte == null) return "";
        return Normalizer.normalize(texte, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .trim().toLowerCase();
    }

    private Station trouverStation(Graph<Station, Liaison> graph, String nom) {
        String nomRecherche = normaliser(nom);
        for (Station s : graph.vertexSet()) {
            if (normaliser(s.getNom()).equals(nomRecherche)) {
                return s; // On a trouvé la station exacte, peu importe les accents ou la casse !
            }
        }
        return null;
    }
}