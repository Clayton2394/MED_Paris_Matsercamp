package com.itineraire.backend_api.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
    private final String STOPS_FILE = "./Data_clean/stops_clean.txt";
    private final String LIAISONS_FILE = "./Data_clean/liaisons_clean.txt";

    @PostConstruct
public void initGraph() {
    System.out.println("Début de la construction du graphe...");

    loadStations();
    loadEdges();
    createTransfers();

    // Si les fichiers CSV sont absents, on injecte un mini-graphe de test
    if (networkGraph.vertexSet().isEmpty()) {
        System.out.println("Fichiers introuvables — chargement du graphe de test (10 stations).");
        chargerGrapheDeTest();
    }

    System.out.println("Graphe terminé ! Noeuds: " + networkGraph.vertexSet().size() + " | Arêtes: " + networkGraph.edgeSet().size());
}

private void chargerGrapheDeTest() {
    Station chatelet     = new Station("T1", "Châtelet",          2.3470, 48.8587);
    Station lesHalles    = new Station("T2", "Les Halles",         2.3465, 48.8625);
    Station gareNord     = new Station("T3", "Gare du Nord",       2.3553, 48.8809);
    Station republique   = new Station("T4", "République",         2.3632, 48.8674);
    Station bastille     = new Station("T5", "Bastille",           2.3693, 48.8533);
    Station nation       = new Station("T6", "Nation",             2.3960, 48.8484);
    Station gareEst      = new Station("T7", "Gare de l'Est",      2.3581, 48.8767);
    Station operaParis   = new Station("T8", "Opéra",              2.3316, 48.8720);
    Station saint_lazare = new Station("T9", "Saint-Lazare",       2.3249, 48.8757);
    Station montparnasse = new Station("T10","Montparnasse",        2.3213, 48.8438);

    for (Station s : List.of(chatelet, lesHalles, gareNord, republique, bastille,
                              nation, gareEst, operaParis, saint_lazare, montparnasse)) {
        networkGraph.addVertex(s);
        stationIndex.put(s.getId(), s);
    }

    ajouterLiaison(chatelet,     lesHalles,    60);
    ajouterLiaison(chatelet,     republique,  120);
    ajouterLiaison(chatelet,     operaParis,  180);
    ajouterLiaison(lesHalles,    gareNord,    240);
    ajouterLiaison(republique,   bastille,    120);
    ajouterLiaison(republique,   gareEst,     180);
    ajouterLiaison(bastille,     nation,      240);
    ajouterLiaison(gareNord,     gareEst,      60);
    ajouterLiaison(operaParis,   saint_lazare, 90);
    ajouterLiaison(saint_lazare, gareNord,    300);
    ajouterLiaison(montparnasse, chatelet,    360);
}

private void ajouterLiaison(Station a, Station b, double secondes) {
    Liaison l = new Liaison();
    networkGraph.addEdge(a, b, l);
    networkGraph.setEdgeWeight(l, secondes);
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