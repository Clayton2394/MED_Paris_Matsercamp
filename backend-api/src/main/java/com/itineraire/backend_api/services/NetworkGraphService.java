package com.itineraire.backend_api.services;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
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

    // Dictionnaire pour les routes (route_id -> {nomLigne, couleur})
    private final Map<String, String[]> routesInfo = new HashMap<>();

    // --- NOUVELLES DONNEES TEMPORELLES ---
    private final Map<String, java.util.List<String>> routeToServices = new HashMap<>();
    
    public static class ServiceCalendar {
        boolean[] days;
        int startDate;
        int endDate;
    }
    private final Map<String, ServiceCalendar> services = new HashMap<>();
    private final Map<String, Map<Integer, Integer>> calendarDates = new HashMap<>();

    // Les nouveaux chemins vers les fichiers
    private final String STOPS_FILE = "../Data_clean/stops_clean.txt";
    private final String LIAISONS_FILE = "../Data_clean/liaisons_clean.txt";
    private final String ROUTES_FILE = "../Data_clean/routes_clean.txt";
    private final String CALENDAR_FILE = "../Data_clean/calendar_clean.txt";
    private final String CALENDAR_DATES_FILE = "../Data_clean/calendar_dates_clean.txt";
    private final String TRIPS_FILE = "../Data_clean/trips_clean.txt";

    @PostConstruct
    public void initGraph() {
        System.out.println("Début de la construction du graphe...");

        loadRoutes();
        loadCalendar();
        loadCalendarDates();
        loadTrips();
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

    private void loadRoutes() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ROUTES_FILE), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] cols = line.split(",");
                if (cols.length >= 5) {
                    String routeId = cols[0].trim();
                    String nomLigne = cols[2].trim();
                    String couleur = cols[4].trim();
                    routesInfo.put(routeId, new String[]{nomLigne, couleur});
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement routes_clean.txt: " + e.getMessage());
        }
    }

    private void loadCalendar() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(CALENDAR_FILE), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] cols = line.split(",");
                if (cols.length >= 10) {
                    ServiceCalendar sc = new ServiceCalendar();
                    sc.days = new boolean[7];
                    for (int i=0; i<7; i++) {
                        sc.days[i] = cols[i+1].trim().equals("1");
                    }
                    sc.startDate = Integer.parseInt(cols[8].trim());
                    sc.endDate = Integer.parseInt(cols[9].trim());
                    services.put(cols[0].trim(), sc);
                }
            }
        } catch (Exception e) { System.err.println("Erreur loadCalendar: " + e.getMessage()); }
    }

    private void loadCalendarDates() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(CALENDAR_DATES_FILE), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] cols = line.split(",");
                if (cols.length >= 3) {
                    String serviceId = cols[0].trim();
                    int date = Integer.parseInt(cols[1].trim());
                    int exceptionType = Integer.parseInt(cols[2].trim());
                    calendarDates.computeIfAbsent(serviceId, k -> new HashMap<>()).put(date, exceptionType);
                }
            }
        } catch (Exception e) { System.err.println("Erreur loadCalendarDates: " + e.getMessage()); }
    }

    private void loadTrips() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(TRIPS_FILE), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] cols = line.split(",");
                if (cols.length >= 2) {
                    String routeId = cols[0].trim();
                    String serviceId = cols[1].trim();
                    routeToServices.computeIfAbsent(routeId, k -> new java.util.ArrayList<>()).add(serviceId);
                }
            }
        } catch (Exception e) { System.err.println("Erreur loadTrips: " + e.getMessage()); }
    }

    private void loadStations() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(STOPS_FILE), StandardCharsets.UTF_8))) {
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
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(LIAISONS_FILE), StandardCharsets.UTF_8))) {
            String line;
            br.readLine(); // Passer l'en-tête (stop_depart,stop_arrivee,duree_secondes,route_id)

            while ((line = br.readLine()) != null) {
                String[] cols = line.split(",");
                if (cols.length >= 3) {
                    String stopDepartId = cols[0].trim();
                    String stopArriveeId = cols[1].trim();
                    String routeId = cols.length >= 4 ? cols[3].trim() : "INCONNU";
                    
                    try {
                        double dureeSecondes = Double.parseDouble(cols[2].trim());
                        
                        Station stationDepart = stationIndex.get(stopDepartId);
                        Station stationArrivee = stationIndex.get(stopArriveeId);

                        if (stationDepart != null && stationArrivee != null && !stationDepart.equals(stationArrivee)) {
                            if (!networkGraph.containsEdge(stationDepart, stationArrivee)) {
                                String nomLigne = "Ligne";
                                String couleur = "000000";
                                if (routesInfo.containsKey(routeId)) {
                                    nomLigne = routesInfo.get(routeId)[0];
                                    couleur = routesInfo.get(routeId)[1];
                                }
                                Liaison liaison = new Liaison(routeId, nomLigne, couleur);
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
                            
                            // Distance physique réelle entre les quais en mètres
                            double distanceMeters = calculateHaversineDistance(quaiA.getLat(), quaiA.getLon(), quaiB.getLat(), quaiB.getLon());
                            
                            // Logique réaliste: vitesse de marche d'environ 1.2 m/s
                            // + Un temps forfaitaire (ex: 2 min = 120s) pour traverser les portiques, descendre/monter les escaliers
                            double tempsMarche = distanceMeters / 1.2;
                            double tempsTotal = tempsMarche + 120.0;
                            
                            // Plafond de sécurité (maximum 15 minutes) au cas où deux gares ont le même nom mais sont très éloignées géographiquement
                            if (tempsTotal > 900.0) tempsTotal = 900.0;
                            
                            networkGraph.setEdgeWeight(correspondance, tempsTotal); 
                            correspondancesCrees++;
                        }
                    }
                }
            }
        }
        System.out.println(correspondancesCrees + " liaisons de correspondances ont été ajoutées !");
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Rayon de la Terre en km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // Retour en mètres
    }

    public Graph<Station, Liaison> getNetworkGraph() {
        return networkGraph;
    }

    public boolean isRouteActive(String routeId, String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return true; // Si aucune date spécifiée, on garde tout
        if ("INCONNU".equals(routeId) || routeId == null) return true; // Les correspondances à pied sont toujours ouvertes
        
        int dateInt;
        int dayOfWeek;
        try {
            dateInt = Integer.parseInt(dateStr.replace("-", ""));
            java.time.LocalDate localDate = java.time.LocalDate.parse(dateStr);
            dayOfWeek = localDate.getDayOfWeek().getValue() - 1; // 0=Lundi, 6=Dimanche
        } catch (Exception e) {
            return true; // En cas de date invalide, on garde tout par défaut
        }
        
        List<String> sIds = routeToServices.get(routeId);
        if (sIds == null || sIds.isEmpty()) return true; // Si on a pas les données de service pour cette route, on l'autorise par défaut
        
        for (String sId : sIds) {
            boolean active = false;
            
            // On vérifie le calendrier normal
            ServiceCalendar sc = services.get(sId);
            if (sc != null && dateInt >= sc.startDate && dateInt <= sc.endDate) {
                if (sc.days[dayOfWeek]) {
                    active = true;
                }
            }
            
            // On vérifie les exceptions (jours fériés / travaux)
            Map<Integer, Integer> exceptions = calendarDates.get(sId);
            if (exceptions != null && exceptions.containsKey(dateInt)) {
                int type = exceptions.get(dateInt);
                if (type == 1) active = true;   // Exceptionnellement ouvert
                if (type == 2) active = false;  // Exceptionnellement fermé
            }
            
            // Si au moins un service de cette route roule ce jour-là, la ligne est considérée comme active
            if (active) return true; 
        }
        
        return false;
    }

    public Map<String, Object> verifierConnexite() {
        ConnectivityInspector<Station, Liaison> inspector = new ConnectivityInspector<>(networkGraph);
        boolean isConnected = inspector.isConnected();

        Map<String, Object> resultat = new HashMap<>();
        resultat.put("estConnexe", isConnected);
        
        if (isConnected) {
            resultat.put("message", "Le réseau est entièrement connecté.");
        } else {
            resultat.put("message", "Attention, certaines stations sont isolées du reste du réseau.");
        }

        return resultat;
    }

    public List<String> getToutesLesStations() {
        java.util.Set<String> nomsVus = new java.util.HashSet<>();
        List<String> listeEpurée = new java.util.ArrayList<>();

        for (Station s : networkGraph.vertexSet()) {
            if (!nomsVus.contains(s.getNom())) {
                nomsVus.add(s.getNom());
                listeEpurée.add(s.getNom());
            }
        }

        listeEpurée.sort(String::compareTo);
        return listeEpurée;
    }
}