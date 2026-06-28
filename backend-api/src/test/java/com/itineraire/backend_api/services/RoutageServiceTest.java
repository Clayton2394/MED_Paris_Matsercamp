package com.itineraire.backend_api.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itineraire.backend_api.models.ItineraireResultat;
import com.itineraire.backend_api.models.Liaison;
import com.itineraire.backend_api.models.Station;

@ExtendWith(MockitoExtension.class)
public class RoutageServiceTest {

    @Mock
    private NetworkGraphService networkGraphService;

    @InjectMocks
    private RoutageService routageService;

    private Graph<Station, Liaison> miniGraphe;

    @BeforeEach
    public void setUp() {
        miniGraphe = new SimpleWeightedGraph<>(Liaison.class);

        Station stationA = new Station("A", "Station A", 2.0, 48.0);
        Station stationB = new Station("B", "Station B", 2.1, 48.1);
        Station stationC = new Station("C", "Station C", 2.2, 48.2);

        miniGraphe.addVertex(stationA);
        miniGraphe.addVertex(stationB);
        miniGraphe.addVertex(stationC);

        Liaison ab = new Liaison();
        miniGraphe.addEdge(stationA, stationB, ab);
        miniGraphe.setEdgeWeight(ab, 60.0);

        Liaison bc = new Liaison();
        miniGraphe.addEdge(stationB, stationC, bc);
        miniGraphe.setEdgeWeight(bc, 60.0);

        when(networkGraphService.getNetworkGraph()).thenReturn(miniGraphe);
    }

    @Test
    public void testTrajetValide_RetourneLeBonChemin() {
        ItineraireResultat resultat = routageService.calculerItineraire("Station A", "Station C");

        assertNotNull(resultat);
        assertEquals(3, resultat.getChemin().size());
        assertEquals("Station A", resultat.getChemin().get(0).getNom());
        assertEquals("Station B", resultat.getChemin().get(1).getNom());
        assertEquals("Station C", resultat.getChemin().get(2).getNom());
        assertEquals(120.0, resultat.getDureeTotaleSecondes());
        assertTrue(resultat.getTempsCalculMs() >= 0);
    }

    @Test
    public void testTrajetMemeStation_RetourneUnCheminVideOuUneEtape() {
        ItineraireResultat resultat = routageService.calculerItineraire("Station A", "Station A");

        assertNotNull(resultat);
        assertEquals(1, resultat.getChemin().size());
        assertEquals("Station A", resultat.getChemin().get(0).getNom());
        assertEquals(0.0, resultat.getDureeTotaleSecondes());
    }

    @Test
    public void testStationInconnue_LeveException() {
        assertThrows(IllegalArgumentException.class, () -> {
            routageService.calculerItineraire("Station A", "Station Fantôme");
        });
    }
}
