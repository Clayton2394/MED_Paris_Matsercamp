package com.itineraire.backend_api.dtos;

public class StationDTO {
    private String id;
    private String nom;

    public StationDTO(String id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    public String getId() { return id; }
    public String getNom() { return nom; }
}