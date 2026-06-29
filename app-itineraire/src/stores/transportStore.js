import { defineStore } from 'pinia'

export const useTransportStore = defineStore('transport', {
  state: () => ({
    // ==========================================
    // 1. RECHERCHE D'ITINÉRAIRE (V1 & V2)
    // ==========================================
    stationDepart: '',
    stationArrivee: '',
    resultatItineraire: null,
    isLoading: false,
    erreurMessage: '',
    stationsDisponibles: [],

    // ==========================================
    // 2. OPTIONS AVANCÉES (V3)
    // ==========================================
    typeRecherche: 'depart', // 'depart' (par défaut) ou 'arrivee'
    dateTrajet: '',          // Date (ex: 2026-06-14)
    heureTrajet: '',         // Heure (ex: 08:30)
    pmr: false,              // Accessibilité fauteuil roulant (booléen)

    // ==========================================
    // 3. ARBORESCENCE ACPM (Membre 5)
    // ==========================================
    arbreACPM: null,
    isAcpmLoading: false,
  }),
  
  actions: {
    // -------------------------------------------------------------
    // ACTION 1 : Déclenchée par le bouton "Rechercher"
    // -------------------------------------------------------------
    async rechercher() {
      this.erreurMessage = '';
      this.resultatItineraire = null;

    if (!this.stationDepart.trim() || !this.stationArrivee.trim()) {
      this.erreurMessage = "Veuillez renseigner la station de départ et d'arrivée.";
    return;
    }

    this.isLoading = true;

    try {
      let url = `http://localhost:8081/api/itineraire?depart=${encodeURIComponent(this.stationDepart)}&arrivee=${encodeURIComponent(this.stationArrivee)}`;
      if (this.dateTrajet) {
          url += `&date=${encodeURIComponent(this.dateTrajet)}`;
      }
      const response = await fetch(url);

    if (!response.ok) {
      this.erreurMessage = await response.text();
      return;
    }

    this.resultatItineraire = await response.json();
  
  } catch (e) {
    this.erreurMessage = "Impossible de contacter le serveur. Le backend Java est-il lancé ?";
  } finally {
    this.isLoading = false;
  }
},

    async chargerStations() {
      try {
        const response = await fetch('http://localhost:8081/api/stations');
        if (response.ok) {
          const data = await response.json();
          this.stationsDisponibles = data;
        }
      } catch (error) {
        // Géré silencieusement
      }
    },

    // -------------------------------------------------------------
    // ACTION 2 : Déclenchée par la page "Voir l'ACPM"
    // -------------------------------------------------------------
    async chargerACPM() {
      this.isAcpmLoading = true;
      console.log("Demande de l'ACPM au serveur Java en cours...");

      try {
        const response = await fetch('http://localhost:8081/api/acpm');
        
        if (!response.ok) {
          throw new Error(`Erreur réseau (statut: ${response.status})`);
        }
        
        const data = await response.json();
        this.arbreACPM = data;
        console.log("Arbre ACPM chargé avec succès !");
      } catch (error) {
        console.error("Erreur lors de la récupération de l'ACPM :", error);
      } finally {
        this.isAcpmLoading = false;
      }
    }
  }
})