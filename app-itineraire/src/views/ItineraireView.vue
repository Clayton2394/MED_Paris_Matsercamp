<template>
  <div class="itineraire-layout">
    <div ref="mapContainer" class="map-container"></div>

    <div class="panneau-flottant">
      <h1>Calcul d'itinéraire</h1>
      
      <div class="formulaire">
        <input 
          v-model="store.stationDepart" 
          type="text" 
          placeholder="Départ (ex: Châtelet)" 
          :disabled="store.isLoading" 
        />
        <input 
          v-model="store.stationArrivee" 
          type="text" 
          placeholder="Arrivée (ex: Gare du Nord)" 
          :disabled="store.isLoading" 
        />
        
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
            📍 {{ station }}
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useTransportStore } from '../stores/transportStore'
import maplibregl from 'maplibre-gl'

const store = useTransportStore()
const mapContainer = ref(null)
let mapInstance = null

// Initialisation de la carte au chargement de la page
onMounted(() => {
  mapInstance = new maplibregl.Map({
    container: mapContainer.value,
    style: 'https://basemaps.cartocdn.com/gl/positron-gl-style/style.json',
    center: [2.349014, 48.864716], // Coordonnées de Paris
    zoom: 12
  });
})

// Surveillance des résultats pour dessiner la ligne sur la carte
watch(() => store.resultatItineraire, (nouveauResultat) => {
  if (!mapInstance) return;

  if (nouveauResultat) {
    // Liste de coordonnées fictives pour le test
    const faussesCoordonnees = [
      [2.347059, 48.858689], // Châtelet
      [2.346534, 48.862489], // Les Halles
      [2.355304, 48.880948]  // Gare du Nord
    ];

    if (mapInstance.getSource('tracet-itineraire')) {
      // Mise à jour de la ligne si elle existe déjà
      mapInstance.getSource('tracet-itineraire').setData({
        type: 'Feature',
        properties: {},
        geometry: { type: 'LineString', coordinates: faussesCoordonnees }
      });
    } else {
      // Création de la ligne bleue si elle n'existe pas encore
      mapInstance.addSource('tracet-itineraire', {
        type: 'geojson',
        data: {
          type: 'Feature',
          properties: {},
          geometry: { type: 'LineString', coordinates: faussesCoordonnees }
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

input[type="text"], input[type="date"], input[type="time"] {
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
</style>