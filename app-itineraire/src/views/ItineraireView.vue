<template>
  <div class="itineraire-layout">
    <div ref="mapContainer" class="map-container"></div>

    <div class="panneau-flottant">
      <h1>Calcul d'itinéraire</h1>
      
      <div class="formulaire">
        <div class="autocomplete-container">
          <input 
            v-model="store.stationDepart" 
            type="text" 
            placeholder="Départ (ex: Châtelet)" 
            :disabled="store.isLoading"
            @focus="showDepartDropdown = true"
            @blur="hideDepartDropdown"
          />
          <ul v-if="showDepartDropdown && filteredDepart.length" class="suggestions-list">
            <li v-for="station in filteredDepart" :key="station" @mousedown.prevent="selectDepart(station)">
              {{ station }}
            </li>
          </ul>
        </div>

        <div class="autocomplete-container">
          <input 
            v-model="store.stationArrivee" 
            type="text" 
            placeholder="Arrivée (ex: Gare du Nord)" 
            :disabled="store.isLoading" 
            @focus="showArriveeDropdown = true"
            @blur="hideArriveeDropdown"
          />
          <ul v-if="showArriveeDropdown && filteredArrivee.length" class="suggestions-list">
            <li v-for="station in filteredArrivee" :key="station" @mousedown.prevent="selectArrivee(station)">
              {{ station }}
            </li>
          </ul>
        </div>
        
        <div class="options-v3">
          <div class="radio-group">
            <label>
              <input type="radio" v-model="store.typeRecherche" value="depart" :disabled="store.isLoading"> Partir à
            </label>
            <label>
              <input type="radio" v-model="store.typeRecherche" value="arrivee" :disabled="store.isLoading"> Arriver à
            </label>
          </div>

          <div class="datetime-group">
            <input type="date" v-model="store.dateTrajet" :disabled="store.isLoading" />
            <input type="time" v-model="store.heureTrajet" :disabled="store.isLoading" />
            <button @click="partirMaintenant" :disabled="store.isLoading" class="btn-maintenant" type="button">
              🕒 Partir maintenant
            </button>
          </div>

          <div class="checkbox-group">
            <label>
              <input type="checkbox" v-model="store.pmr" :disabled="store.isLoading">
              ♿ Accessible en fauteuil roulant
            </label>
          </div>
        </div>

        <p v-if="store.erreurMessage" class="message-erreur">⚠️ {{ store.erreurMessage }}</p>

        <button @click="store.rechercher" :disabled="store.isLoading">
          <span v-if="store.isLoading" class="spinner"></span>
          <span v-else>Rechercher</span>
        </button>
      </div>

      <div v-if="store.resultatItineraire" class="resultats">
        <h3>Trajet trouvé :</h3>
        <ul>
          <li v-for="(station, index) in store.resultatItineraire.chemin" :key="index">
            📍 {{ station.nom }}
          </li>
        </ul>

        <div class="dashboard-eco">
          <div class="dashboard-section">
            <h4>Performance Algorithmique</h4>
            <p>⏱️ Temps de calcul : {{ store.resultatItineraire.tempsCalculMs }} ms</p>
            <p>💾 Mémoire utilisée : {{ (store.resultatItineraire.memoireUtiliseeBytes / 1024).toFixed(2) }} Ko</p>
          </div>
          <div class="dashboard-section">
            <h4>Impact Environnemental</h4>
            <p>🍃 Distance estimée : {{ impactCarbone.distance }} km</p>
            <p>🌍 CO2 évité (vs Voiture) : {{ impactCarbone.co2Evite }} kg</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useTransportStore } from '../stores/transportStore'
import maplibregl from 'maplibre-gl'

const store = useTransportStore()
const mapContainer = ref(null)
let mapInstance = null

// Calcul de l'impact carbone
const impactCarbone = computed(() => {
  if (!store.resultatItineraire || !store.resultatItineraire.chemin) return { distance: 0, co2Evite: 0 };
  
  const nbStations = store.resultatItineraire.chemin.length;
  if (nbStations < 2) return { distance: 0, co2Evite: 0 };
  
  const distanceTotal = (nbStations - 1) * 1.2; // en km
  const co2Voiture = distanceTotal * 193; // en g
  const co2Metro = distanceTotal * 4; // en g
  
  const co2EviteKg = (co2Voiture - co2Metro) / 1000;
  
  return {
    distance: distanceTotal.toFixed(1),
    co2Evite: co2EviteKg.toFixed(2)
  };
});

// Variables pour l'autocomplétion personnalisée
const showDepartDropdown = ref(false);
const showArriveeDropdown = ref(false);

const filteredDepart = computed(() => {
  if (!store.stationDepart) return store.stationsDisponibles;
  const search = store.stationDepart.toLowerCase();
  return store.stationsDisponibles.filter(s => s.toLowerCase().includes(search));
});

const filteredArrivee = computed(() => {
  if (!store.stationArrivee) return store.stationsDisponibles;
  const search = store.stationArrivee.toLowerCase();
  return store.stationsDisponibles.filter(s => s.toLowerCase().includes(search));
});

const selectDepart = (station) => {
  store.stationDepart = station;
  showDepartDropdown.value = false;
};

const selectArrivee = (station) => {
  store.stationArrivee = station;
  showArriveeDropdown.value = false;
};

const hideDepartDropdown = () => {
  setTimeout(() => showDepartDropdown.value = false, 150);
};

const hideArriveeDropdown = () => {
  setTimeout(() => showArriveeDropdown.value = false, 150);
};

// Fonction pour remplir l'heure et la date actuelles
const partirMaintenant = () => {
  const now = new Date();
  store.dateTrajet = now.toISOString().split('T')[0];
  store.heureTrajet = now.toTimeString().substring(0, 5);
};

// Initialisation de la carte au chargement de la page
onMounted(() => {
  store.chargerStations();
  mapInstance = new maplibregl.Map({
    container: mapContainer.value,
    style: 'https://basemaps.cartocdn.com/gl/voyager-gl-style/style.json',
    center: [2.349014, 48.864716], // Coordonnées de Paris
    zoom: 12
  });
})

// Surveillance des résultats pour dessiner la ligne sur la carte
watch(() => store.resultatItineraire, (nouveauResultat) => {
  if (!mapInstance) return;

  if (nouveauResultat && nouveauResultat.chemin) {
    // Récupération des véritables coordonnées [longitude, latitude]
    const coordonneesReelles = nouveauResultat.chemin.map(etape => [etape.longitude, etape.latitude]);

    if (mapInstance.getSource('tracet-itineraire')) {
      // Mise à jour de la ligne si elle existe déjà
      mapInstance.getSource('tracet-itineraire').setData({
        type: 'Feature',
        properties: {},
        geometry: { type: 'LineString', coordinates: coordonneesReelles }
      });
    } else {
      // Création de la ligne bleue si elle n'existe pas encore
      mapInstance.addSource('tracet-itineraire', {
        type: 'geojson',
        data: {
          type: 'Feature',
          properties: {},
          geometry: { type: 'LineString', coordinates: coordonneesReelles }
        }
      });

      mapInstance.addLayer({
        id: 'calque-itineraire',
        type: 'line',
        source: 'tracet-itineraire',
        layout: { 'line-join': 'round', 'line-cap': 'round' },
        paint: { 'line-color': '#1a73e8', 'line-width': 5 }
      });
    }

    // Zoom automatique sur le trajet dessiné
    if (coordonneesReelles.length > 0) {
      const bounds = new maplibregl.LngLatBounds(coordonneesReelles[0], coordonneesReelles[0]);
      for (const coord of coordonneesReelles) {
        bounds.extend(coord);
      }
      mapInstance.fitBounds(bounds, { padding: 50, duration: 1000 });
    }
  } else {
    // Nettoyage de la carte si on efface les résultats
    if (mapInstance.getLayer('calque-itineraire')) {
      mapInstance.removeLayer('calque-itineraire');
      mapInstance.removeSource('tracet-itineraire');
    }
  }
})
</script>

<style scoped>
/* Disposition générale */
.itineraire-layout {
  position: relative;
  width: 100%;
  height: 80vh;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}

.map-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.panneau-flottant {
  position: absolute;
  top: 20px;
  left: 20px;
  width: 340px;
  background-color: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
  z-index: 2;
  max-height: 90%;
  overflow-y: auto;
}

h1 {
  font-size: 1.5rem;
  margin-top: 0;
  margin-bottom: 15px;
  text-align: left;
}

/* Styles du formulaire */
.formulaire {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.autocomplete-container {
  position: relative;
  width: 100%;
}

.suggestions-list {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  margin: 0;
  padding: 0;
  list-style: none;
  max-height: 200px;
  overflow-y: auto;
  z-index: 1000;
}

.suggestions-list li {
  padding: 10px;
  cursor: pointer;
  border-bottom: 1px solid #eee;
  color: #333;
}

.suggestions-list li:last-child {
  border-bottom: none;
}

.suggestions-list li:hover {
  background-color: #f0f0f0;
}

input[type="text"], input[type="date"], input[type="time"] {
  width: 100%;
  box-sizing: border-box;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 5px;
  width: 100%;
  box-sizing: border-box;
}

input:disabled {
  background-color: #e9ecef;
}

/* Zone Options V3 */
.options-v3 {
  background-color: #f4f6f8;
  padding: 12px;
  border-radius: 8px;
  font-size: 0.9rem;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.radio-group, .checkbox-group {
  display: flex;
  gap: 15px;
  align-items: center;
  color: #333;
  cursor: pointer;
}

.datetime-group {
  display: flex;
  gap: 10px;
  align-items: center;
}

.btn-maintenant {
  margin-top: 0;
  padding: 10px;
  background-color: #f1f3f4;
  color: #333;
  border: 1px solid #ccc;
  min-height: auto;
}

.btn-maintenant:hover:not(:disabled) {
  background-color: #e8eaed;
}

/* Bouton principal */
button {
  padding: 12px;
  background-color: #1a73e8;
  color: white;
  border: none;
  border-radius: 5px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 45px;
  margin-top: 5px;
}

button:hover:not(:disabled) {
  background-color: #1557b0;
}

button:disabled {
  background-color: #8ab4f8;
}

/* Messages et Résultats */
.message-erreur {
  color: #d9534f;
  font-size: 0.9rem;
  margin: 0;
}

.resultats {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #eee;
  text-align: left;
}

ul {
  padding-left: 0;
  list-style: none;
  margin: 0;
}

li {
  padding: 5px 0;
  font-size: 0.95rem;
}

/* Animation de chargement */
.spinner {
  width: 20px;
  height: 20px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Dashboard Écologique et Technique */
.dashboard-eco {
  margin-top: 20px;
  background-color: #e8f5e9; /* Vert pastel clair */
  border-radius: 10px;
  padding: 15px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 15px;
  border: 1px solid #c8e6c9;
}

.dashboard-section h4 {
  margin: 0 0 8px 0;
  font-size: 0.95rem;
  color: #2e7d32;
  border-bottom: 1px solid #c8e6c9;
  padding-bottom: 5px;
}

.dashboard-section p {
  margin: 5px 0;
  font-size: 0.85rem;
  color: #333;
}
</style>