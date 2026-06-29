package com.itineraire.backend_api.dtos;

public class StationDTO {
    private String id;
    private String nom;
    private double lon; // Ajout de la longitude
    private double lat; // Ajout de la latitude

    public StationDTO(String id, String nom, double lon, double lat) {
        this.id = id;
        this.nom = nom;
        this.lon = lon;
        this.lat = lat;
    }

    public String getId() { return id; }
    public String getNom() { return nom; }
    public double getLon() { return lon; }
    public double getLat() { return lat; }
}