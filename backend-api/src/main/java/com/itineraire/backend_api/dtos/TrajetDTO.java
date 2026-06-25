package com.itineraire.backend_api.dtos;

import java.util.List;

public class TrajetDTO {
    private List<String> stations; // Ex: ["Gare du Nord", "Les Halles", "Châtelet"]
    private double tempsSecondes;  // Ex: 450.0

    public TrajetDTO(List<String> stations, double tempsSecondes) {
        this.stations = stations;
        this.tempsSecondes = tempsSecondes;
    }

    public List<String> getStations() { return stations; }
    public double getTempsSecondes() { return tempsSecondes; }
}