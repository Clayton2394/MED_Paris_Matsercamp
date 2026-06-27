package com.itineraire.backend_api.models;

import java.util.List;

public class ItineraireResultat {
    private final List<String> chemin;
    private final int nombreEtapes;
    private final double dureeTotaleSecondes;

    public ItineraireResultat(List<String> chemin, double dureeTotaleSecondes) {
        this.chemin = chemin;
        this.nombreEtapes = chemin.size();
        this.dureeTotaleSecondes = dureeTotaleSecondes;
    }

    public List<String> getChemin() { return chemin; }
    public int getNombreEtapes() { return nombreEtapes; }
    public double getDureeTotaleSecondes() { return dureeTotaleSecondes; }
}
