package com.itineraire.backend_api.models;

import java.util.Objects;

public class Station {
    private final String id;
    private final String nom;
    private final double lon;
    private final double lat;

    public Station(String id, String nom, double lon, double lat) {
        this.id = id;
        this.nom = nom;
        this.lon = lon;
        this.lat = lat;
    }

    // --- Getters et Setters ---
    public String getId() { return id; }
    public String getNom() { return nom; }
    public double getLon() { return lon; }
    public double getLat() { return lat; }

    // --- Méthodes vitales pour JGraphT ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return Objects.equals(id, station.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nom; // Pratique pour le débuggage dans la console
    }
}