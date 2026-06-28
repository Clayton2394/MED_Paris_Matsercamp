package com.itineraire.backend_api.models;

public class StationDTO {
    private String nom;
    private double latitude;
    private double longitude;

    public StationDTO() {
    }

    public StationDTO(String nom, double latitude, double longitude) {
        this.nom = nom;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
