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
      const params = new URLSearchParams({
      depart: this.stationDepart.trim(),
      arrivee: this.stationArrivee.trim()
    });

    const response = await fetch(`http://localhost:8081/api/itineraire?${params}`);

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

    // -------------------------------------------------------------
    // ACTION 2 : Déclenchée par la page "Voir l'ACPM"
    // -------------------------------------------------------------
    chargerACPM() {
      this.isAcpmLoading = true;
      console.log("Demande de l'ACPM au serveur Java en cours...");

      // MOCKING : On simule l'attente du calcul de Kruskal/Prim (Backend Java)
      setTimeout(() => {
        // Fausse structure de graphe compatible avec Cytoscape.js
        this.arbreACPM = [
          // Les Nœuds (Stations)
          { data: { id: 'A', label: 'Châtelet' } },
          { data: { id: 'B', label: 'Les Halles' } },
          { data: { id: 'C', label: 'Gare du Nord' } },
          { data: { id: 'D', label: 'Bastille' } },
          
          // Les Arêtes (Lignes et poids/temps)
          { data: { source: 'A', target: 'B', weight: 1 } },
          { data: { source: 'B', target: 'C', weight: 4 } },
          { data: { source: 'A', target: 'D', weight: 2 } }
        ];
        
        this.isAcpmLoading = false;
        console.log("Arbre ACPM chargé !");
      }, 1000);
    }
  }
})