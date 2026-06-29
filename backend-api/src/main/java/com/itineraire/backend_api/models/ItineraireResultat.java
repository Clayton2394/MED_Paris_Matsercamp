package com.itineraire.backend_api.models;

import java.util.List;

public class ItineraireResultat {
    private final List<StationDTO> chemin;
    private final int nombreEtapes;
    private final double dureeTotaleSecondes;
    private long tempsCalculMs;
    private long memoireUtiliseeBytes;

    public ItineraireResultat(List<StationDTO> chemin, double dureeTotaleSecondes) {
        this.chemin = chemin;
        this.nombreEtapes = chemin.size();
        this.dureeTotaleSecondes = dureeTotaleSecondes;
    }

    public List<StationDTO> getChemin() { return chemin; }
    public int getNombreEtapes() { return nombreEtapes; }
    public double getDureeTotaleSecondes() { return dureeTotaleSecondes; }

    public long getTempsCalculMs() { return tempsCalculMs; }
    public void setTempsCalculMs(long tempsCalculMs) { this.tempsCalculMs = tempsCalculMs; }

    public long getMemoireUtiliseeBytes() { return memoireUtiliseeBytes; }
    public void setMemoireUtiliseeBytes(long memoireUtiliseeBytes) { this.memoireUtiliseeBytes = memoireUtiliseeBytes; }
}
