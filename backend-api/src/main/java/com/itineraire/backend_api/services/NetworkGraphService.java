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

    // Notre Graphe principal pondéré (le poids sera en SECONDES)
    private final Graph<Station, Liaison> networkGraph = new SimpleWeightedGraph<>(Liaison.class);
    
    // Un dictionnaire pour retrouver vite une station grâce à son ID
    private final Map<String, Station> stationIndex = new HashMap<>();

    // Les nouveaux chemins vers les fichiers
    private final String STOPS_FILE = "Data_clean/stops_clean.txt";
    private final String LIAISONS_FILE = "Data_clean/liaisons_clean.txt";

    @PostConstruct
    public void initGraph() {
        System.out.println("Début de la construction du graphe...");
        
        loadStations();
        loadEdges();
        createTransfers(); 
        
        System.out.println("Graphe terminé ! Nœuds: " + networkGraph.vertexSet().size() + " | Arêtes: " + networkGraph.edgeSet().size());
    }

    private void loadStations() {
        try (BufferedReader br = new BufferedReader(new FileReader(STOPS_FILE))) {
            String line;
            br.readLine(); // Passer les en-têtes
            
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] cols = line.split(",");
                if (cols.length >= 4) {
                    String id = cols[0].trim();
                    String nom = cols[1].trim();
                    String lonStr = cols[2].trim();
                    String latStr = cols[3].trim();

                    try {
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
        try (BufferedReader br = new BufferedReader(new FileReader(LIAISONS_FILE))) {
            String line;
            br.readLine(); // Passer l'en-tête (stop_depart,stop_arrivee,duree_secondes,route_id)

            while ((line = br.readLine()) != null) {
                String[] cols = line.split(",");
                if (cols.length >= 3) {
                    String stopDepartId = cols[0].trim();
                    String stopArriveeId = cols[1].trim();
                    
                    try {
                        double dureeSecondes = Double.parseDouble(cols[2].trim());
                        
                        Station stationDepart = stationIndex.get(stopDepartId);
                        Station stationArrivee = stationIndex.get(stopArriveeId);

                        if (stationDepart != null && stationArrivee != null && !stationDepart.equals(stationArrivee)) {
                            if (!networkGraph.containsEdge(stationDepart, stationArrivee)) {
                                Liaison liaison = new Liaison();
                                networkGraph.addEdge(stationDepart, stationArrivee, liaison);
                                
                                // On applique le VRAI temps de parcours en secondes !
                                networkGraph.setEdgeWeight(liaison, dureeSecondes);
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Durée invalide ignorée entre " + stopDepartId + " et " + stopArriveeId);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture des liaisons: " + e.getMessage());
        }
    }

    private void createTransfers() {
        System.out.println("Création des correspondances à l'intérieur des stations...");
        
        Map<String, java.util.List<Station>> stationsParNom = new HashMap<>();
        for (Station s : networkGraph.vertexSet()) {
            stationsParNom.computeIfAbsent(s.getNom(), k -> new java.util.ArrayList<>()).add(s);
        }

        int correspondancesCrees = 0;
        for (java.util.List<Station> quais : stationsParNom.values()) {
            if (quais.size() > 1) {
                for (int i = 0; i < quais.size(); i++) {
                    for (int j = i + 1; j < quais.size(); j++) {
                        Station quaiA = quais.get(i);
                        Station quaiB = quais.get(j);

                        if (!networkGraph.containsEdge(quaiA, quaiB)) {
                            Liaison correspondance = new Liaison();
                            networkGraph.addEdge(quaiA, quaiB, correspondance);
                            
                            // Un changement de ligne prend environ 4 minutes (240 secondes)
                            networkGraph.setEdgeWeight(correspondance, 240.0); 
                            correspondancesCrees++;
                        }
                    }
                }
            }
        }
        System.out.println(correspondancesCrees + " liaisons de correspondances ont été ajoutées !");
    }

    public Graph<Station, Liaison> getNetworkGraph() {
        return networkGraph;
    }
}