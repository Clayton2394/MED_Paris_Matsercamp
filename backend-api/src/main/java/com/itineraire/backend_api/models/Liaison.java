package com.itineraire.backend_api.models;

import org.jgrapht.graph.DefaultWeightedEdge;

public class Liaison extends DefaultWeightedEdge {
    // On pourra rajouter le temps de trajet ici plus tard pour la V3
    
    public double getPoids() {
        return getWeight();
    }
}