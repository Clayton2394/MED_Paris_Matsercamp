package com.itineraire.backend_api.services;

import java.text.Normalizer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.springframework.stereotype.Service;

import com.itineraire.backend_api.models.Liaison;
import com.itineraire.backend_api.models.Station;

@Service
public class ConnectivityService {

    private final NetworkGraphService networkGraphService;

    public ConnectivityService(NetworkGraphService networkGraphService) {
        this.networkGraphService = networkGraphService;
    }

    public List<String> bfs(String stationId) {
        Graph<Station, Liaison> graph = networkGraphService.getNetworkGraph();
        Station depart = trouverStation(graph, stationId);
        if (depart == null) {
            throw new IllegalArgumentException("Station inconnue : " + stationId);
        }

        List<String> ordreVisite = new ArrayList<>();
        Set<Station> visitees = new HashSet<>();
        Deque<Station> file = new ArrayDeque<>();

        file.add(depart);
        visitees.add(depart);

        while (!file.isEmpty()) {
            Station courante = file.poll();
            ordreVisite.add(courante.getNom());

            for (Liaison liaison : graph.edgesOf(courante)) {
                Station voisine = Graphs.getOppositeVertex(graph, liaison, courante);
                if (!visitees.contains(voisine)) {
                    visitees.add(voisine);
                    file.add(voisine);
                }
            }
        }

        return ordreVisite;
    }

    public List<String> dfs(String stationId) {
        Graph<Station, Liaison> graph = networkGraphService.getNetworkGraph();
        Station depart = trouverStation(graph, stationId);
        if (depart == null) {
            throw new IllegalArgumentException("Station inconnue : " + stationId);
        }

        List<String> ordreVisite = new ArrayList<>();
        Set<Station> visitees = new HashSet<>();
        Deque<Station> pile = new ArrayDeque<>();

        pile.push(depart);

        while (!pile.isEmpty()) {
            Station courante = pile.pop();
            if (visitees.contains(courante)) {
                continue;
            }
            visitees.add(courante);
            ordreVisite.add(courante.getNom());

            for (Liaison liaison : graph.edgesOf(courante)) {
                Station voisine = Graphs.getOppositeVertex(graph, liaison, courante);
                if (!visitees.contains(voisine)) {
                    pile.push(voisine);
                }
            }
        }

        return ordreVisite;
    }

    public List<List<String>> composantesConnexes() {
        Graph<Station, Liaison> graph = networkGraphService.getNetworkGraph();
        Set<Station> visitees = new HashSet<>();
        List<List<String>> composantes = new ArrayList<>();

        for (Station depart : graph.vertexSet()) {
            if (visitees.contains(depart)) {
                continue;
            }

            List<String> composante = new ArrayList<>();
            Deque<Station> file = new ArrayDeque<>();
            file.add(depart);
            visitees.add(depart);

            while (!file.isEmpty()) {
                Station courante = file.poll();
                composante.add(courante.getNom());

                for (Liaison liaison : graph.edgesOf(courante)) {
                    Station voisine = Graphs.getOppositeVertex(graph, liaison, courante);
                    if (!visitees.contains(voisine)) {
                        visitees.add(voisine);
                        file.add(voisine);
                    }
                }
            }

            composantes.add(composante);
        }

        return composantes;
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
