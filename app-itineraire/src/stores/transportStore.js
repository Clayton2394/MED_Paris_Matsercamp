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
    rechercher() {
      // On réinitialise les erreurs et les résultats précédents
      this.erreurMessage = '';
      this.resultatItineraire = null;

      // Validation basique : vérifier que les champs ne sont pas vides
      if (!this.stationDepart.trim() || !this.stationArrivee.trim()) {
        this.erreurMessage = "Veuillez renseigner la station de départ et d'arrivée.";
        return; // Stoppe l'exécution
      }

      this.isLoading = true;
      
      // Logs console pour le débug (vérifier que les options V3 sont bien prises en compte)
      console.log(`Recherche demandée : ${this.stationDepart} -> ${this.stationArrivee}`);
      console.log(`Mode : ${this.typeRecherche} le ${this.dateTrajet} à ${this.heureTrajet}`);
      console.log(`PMR activé : ${this.pmr}`);

      // MOCKING : On simule l'attente du calcul de Dijkstra (Backend Java)
      // À remplacer plus tard par : const response = await fetch('http://localhost:8080/api/itineraire...')
      setTimeout(() => {
        this.resultatItineraire = {
          chemin: [this.stationDepart, "Station Centrale (Fictive)", this.stationArrivee],
          distance: 2
        };
        this.isLoading = false;
        console.log("Résultat de l'itinéraire chargé !");
      }, 1500);
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