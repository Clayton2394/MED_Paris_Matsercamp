package com.itineraire.backend_api.services;

import org.jgrapht.Graph;
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm.SpanningTree;
import org.springframework.stereotype.Service;

import com.itineraire.backend_api.models.Liaison;
import com.itineraire.backend_api.models.Station;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AcpmService {

    private final NetworkGraphService networkGraphService;

    public AcpmService(NetworkGraphService networkGraphService) {
        this.networkGraphService = networkGraphService;
    }

    public List<Map<String, Object>> calculerACPM() {
        Graph<Station, Liaison> graph = networkGraphService.getNetworkGraph();

        KruskalMinimumSpanningTree<Station, Liaison> kruskal = new KruskalMinimumSpanningTree<>(graph);
        SpanningTree<Liaison> spanningTree = kruskal.getSpanningTree();

        List<Map<String, Object>> elementsCytoscape = new ArrayList<>();
        Set<Station> stationsInTree = new HashSet<>();
        Set<String> nomsVus = new HashSet<>();

        // On parcourt les arêtes de l'arbre
        for (Liaison edge : spanningTree.getEdges()) {
            Station source = graph.getEdgeSource(edge);
            Station target = graph.getEdgeTarget(edge);
            
            stationsInTree.add(source);
            stationsInTree.add(target);

            // S'il s'agit d'une correspondance au sein de la même station, cela créera une boucle sur soi-même 
            // dans Cytoscape car on utilise le nom comme identifiant de nœud.
            if (source.getNom().equals(target.getNom())) {
                continue; 
            }

            Map<String, Object> edgeData = new HashMap<>();
            // On s'assure que l'ID de l'arête soit unique en utilisant les ID des stations d'origine
            edgeData.put("id", source.getNom() + "-" + target.getNom() + "-" + source.getId() + "-" + target.getId());
            edgeData.put("source", source.getNom());
            edgeData.put("target", target.getNom());
            edgeData.put("weight", graph.getEdgeWeight(edge));

            Map<String, Object> edgeWrapper = new HashMap<>();
            edgeWrapper.put("data", edgeData);
            elementsCytoscape.add(edgeWrapper);
        }

        // On ajoute les nœuds de manière unique basés sur le nom de la station
        for (Station s : stationsInTree) {
            if (!nomsVus.contains(s.getNom())) {
                nomsVus.add(s.getNom());
                
                Map<String, Object> nodeData = new HashMap<>();
                nodeData.put("id", s.getNom());
                nodeData.put("label", s.getNom());
                
                Map<String, Object> nodeWrapper = new HashMap<>();
                nodeWrapper.put("data", nodeData);
                elementsCytoscape.add(nodeWrapper);
            }
        }

        return elementsCytoscape;
    }
}
