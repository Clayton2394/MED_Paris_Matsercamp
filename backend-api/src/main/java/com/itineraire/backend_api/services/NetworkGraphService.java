package com.itineraire.backend_api.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.springframework.stereotype.Service;

import com.itineraire.backend_api.models.Liaison;
import com.itineraire.backend_api.models.Station;

import jakarta.annotation.PostConstruct;

@Service
public class NetworkGraphService {

    // Notre Graphe principal (Undirected = on peut voyager dans les deux sens)
    private final Graph<Station, Liaison> networkGraph = new SimpleWeightedGraph<>(Liaison.class);
    
    // Un dictionnaire pour retrouver vite une station grâce à son ID
    private final Map<String, Station> stationIndex = new HashMap<>();

    // Les chemins vers les fichiers générés par ton script Python
    private final String STOPS_FILE = "Data_clean/stops_clean.txt";
    private final String STOP_TIMES_FILE = "Data_clean/stop_times_clean.txt";

    @PostConstruct
    public void initGraph() {
        System.out.println("Début de la construction du graphe...");
        
        loadStations();
        loadEdges();
        createTransfers(); // Ajout des correspondances intra-stations
        
        System.out.println("Graphe terminé ! Noeuds: " + networkGraph.vertexSet().size() + " | Arêtes: " + networkGraph.edgeSet().size());
    }

    private void loadStations() {
        try (BufferedReader br = new BufferedReader(new FileReader(STOPS_FILE))) {
            String line;
            br.readLine(); // Passer la première ligne (les en-têtes)
            
            while ((line = br.readLine()) != null) {
                // On ignore les lignes totalement vides
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] cols = line.split(",");
                if (cols.length >= 4) {
                    String id = cols[0].trim();
                    String nom = cols[1].trim();
                    String lonStr = cols[2].trim();
                    String latStr = cols[3].trim();

                    try {
                        // Sécurité : si la case est vide, on met 0.0, sinon on convertit en Double
                        double lon = lonStr.isEmpty() ? 0.0 : Double.parseDouble(lonStr);
                        double lat = latStr.isEmpty() ? 0.0 : Double.parseDouble(latStr);

                        Station station = new Station(id, nom, lon, lat);
                        networkGraph.addVertex(station);
                        stationIndex.put(id, station);
                        
                    } catch (NumberFormatException e) {
                        System.err.println("Coordonnées invalides ignorées pour la station : " + nom);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Impossible de trouver le fichier stops_clean.txt : " + e.getMessage());
        }
    }

    private void loadEdges() {
        try (BufferedReader br = new BufferedReader(new FileReader(STOP_TIMES_FILE))) {
            String line;
            String currentTripId = "";
            Station previousStation = null;

            while ((line = br.readLine()) != null) {
                String[] cols = line.split(",");
                if (cols.length >= 3) {
                    String tripId = cols[0].trim();
                    String stopId = cols[1].trim();
                    
                    Station currentStation = stationIndex.get(stopId);

                    if (currentStation != null) {
                        // Si on est dans le même trajet qu'à la ligne précédente, on les relie !
                        if (tripId.equals(currentTripId) && previousStation != null && !previousStation.equals(currentStation)) {
                            
                            // On vérifie que la liaison n'existe pas déjà
                            if (!networkGraph.containsEdge(previousStation, currentStation)) {
                                Liaison liaison = new Liaison();
                                networkGraph.addEdge(previousStation, currentStation, liaison);
                                // Poids par défaut = 1.0 (1 arrêt)
                                networkGraph.setEdgeWeight(liaison, 1.0);
                            }
                        }
                        currentTripId = tripId;
                        previousStation = currentStation;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture des liaisons: " + e.getMessage());
        }
    }

    private void createTransfers() {
        System.out.println("Création des correspondances à l'intérieur des stations...");
        
        // 1. On regroupe tous les quais (Stations) qui ont exactement le même nom
        Map<String, java.util.List<Station>> stationsParNom = new HashMap<>();
        for (Station s : networkGraph.vertexSet()) {
            stationsParNom.computeIfAbsent(s.getNom(), k -> new java.util.ArrayList<>()).add(s);
        }

        // 2. On crée des liaisons entre tous les quais d'un même groupe
        int correspondancesCrees = 0;
        for (java.util.List<Station> quais : stationsParNom.values()) {
            // S'il y a plus d'un quai pour ce nom de station
            if (quais.size() > 1) {
                // On relie tous les quais entre eux
                for (int i = 0; i < quais.size(); i++) {
                    for (int j = i + 1; j < quais.size(); j++) {
                        Station quaiA = quais.get(i);
                        Station quaiB = quais.get(j);

                        if (!networkGraph.containsEdge(quaiA, quaiB)) {
                            Liaison correspondance = new Liaison();
                            networkGraph.addEdge(quaiA, quaiB, correspondance);
                            // On donne un "poids" de 3.0 pour pénaliser légèrement les changements de ligne
                            networkGraph.setEdgeWeight(correspondance, 3.0); 
                            correspondancesCrees++;
                        }
                    }
                }
            }
        }
        System.out.println(correspondancesCrees + " liaisons de correspondances ont été ajoutées !");
    }

    // Getter pour exposer le graphe aux autres services
    public Graph<Station, Liaison> getNetworkGraph() {
        return networkGraph;
    }
}