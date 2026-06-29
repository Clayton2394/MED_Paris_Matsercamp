package com.itineraire.backend_api.models;

import org.jgrapht.graph.DefaultWeightedEdge;

public class Liaison extends DefaultWeightedEdge {
    private String routeId;
    private String ligneNom;
    private String couleur;

    public Liaison() {
        this.ligneNom = "Correspondance";
        this.couleur = "808080"; // Gris par défaut pour les correspondances
    }

    public Liaison(String routeId, String ligneNom, String couleur) {
        this.routeId = routeId;
        this.ligneNom = ligneNom;
        this.couleur = couleur;
    }

    public String getRouteId() { return routeId; }
    public String getLigneNom() { return ligneNom; }
    public String getCouleur() { return couleur; }

    public double getPoids() {
        return getWeight();
    }
}