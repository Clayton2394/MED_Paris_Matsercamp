package com.itineraire.backend_api.services;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.springframework.stereotype.Service;

import com.itineraire.backend_api.models.ItineraireResultat;
import com.itineraire.backend_api.models.Liaison;
import com.itineraire.backend_api.models.Station;

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

        if (stationDepart == null || stationArrivee == null) {
            return null;
        }

        DijkstraShortestPath<Station, Liaison> dijkstra = new DijkstraShortestPath<>(graph);
        GraphPath<Station, Liaison> path = dijkstra.getPath(stationDepart, stationArrivee);

        if (path == null) {
            return null;
        }

        // Dédupliquer les noms consécutifs identiques (correspondances = même station, plusieurs quais)
        List<String> chemin = new ArrayList<>();
        String dernierNom = null;
        for (Station s : path.getVertexList()) {
            if (!s.getNom().equals(dernierNom)) {
                chemin.add(s.getNom());
                dernierNom = s.getNom();
            }
        }

        return new ItineraireResultat(chemin, path.getWeight());
    }

    private Station trouverStation(Graph<Station, Liaison> graph, String nom) {
        for (Station s : graph.vertexSet()) {
            if (s.getNom().equalsIgnoreCase(nom.trim())) {
                return s;
            }
        }
        return null;
    }
}