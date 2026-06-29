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

        <div v-if="horairesTrajet" class="resume-trajet">
          <strong>Départ :</strong> {{ horairesTrajet.depart }} <br/>
          <strong>Arrivée :</strong> {{ horairesTrajet.arrivee }} <br/>
          <strong>Durée :</strong> {{ horairesTrajet.duree }} min
        </div>

        <ul class="liste-stations">
          <li v-for="(station, index) in store.resultatItineraire.chemin" :key="index">
            <div class="station-ligne-info" v-if="index > 0 && station.ligne && station.ligne !== store.resultatItineraire.chemin[index-1].ligne">
              <span v-if="station.ligne === 'Correspondance'" class="badge badge-correspondance">
                🚶 Correspondance ({{ Math.round(station.dureeDepuisPrecedent / 60) }} min)
              </span>
              <span v-else class="badge badge-ligne" :style="{ backgroundColor: getCouleurLigne(station.ligne, station.couleur) }">
                {{ station.ligne.includes(' ') ? station.ligne : 'Ligne ' + station.ligne }}
              </span>
            </div>
            
            <div class="station-nom">
              <span v-if="index === 0 && store.resultatItineraire.chemin.length > 1 && store.resultatItineraire.chemin[1].ligne && store.resultatItineraire.chemin[1].ligne !== 'Correspondance'" class="badge badge-ligne" :style="{ backgroundColor: getCouleurLigne(store.resultatItineraire.chemin[1].ligne, store.resultatItineraire.chemin[1].couleur) }">
                Ligne {{ store.resultatItineraire.chemin[1].ligne }}
              </span>
              📍 {{ station.nom }}
            </div>
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

// Dictionnaire des couleurs de lignes (demandé par l'utilisateur)
const COULEURS_LIGNES = {
  '1': '#FFCE00',
  '2': '#0064B0',
  '3': '#9F9825',
  '3bis': '#98D4E2',
  '4': '#C04191',
  '5': '#F28E42',
  '6': '#83C491',
  '7': '#F3A4BA',
  '7bis': '#83C491',
  '8': '#CEADD2',
  '9': '#D5C900',
  '10': '#E3B32A',
  '11': '#8D5E2A',
  '12': '#00814F',
  '13': '#98D4E2',
  '14': '#662483',
  '15': '#B90845',
  '16': '#F3A4BA',
  '17': '#D5C900',
  '18': '#00A88F',
  'Correspondance': '#808080'
};

const getCouleurLigne = (ligne, couleurBackend) => {
  if (!ligne) return '#000000';
  if (ligne === 'Correspondance') return '#808080';
  const numLigne = ligne.replace('Ligne ', '').trim();
  if (COULEURS_LIGNES[numLigne]) return COULEURS_LIGNES[numLigne];
  if (couleurBackend) return '#' + couleurBackend;
  return '#1a73e8';
};

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

// Calcul des horaires de trajet
const horairesTrajet = computed(() => {
  if (!store.resultatItineraire || !store.heureTrajet) return null;
  const dureeSec = store.resultatItineraire.dureeTotaleSecondes || 0;
  const dureeMin = Math.round(dureeSec / 60);
  
  const [h, m] = store.heureTrajet.split(':').map(Number);
  const dateBase = new Date(store.dateTrajet || new Date());
  dateBase.setHours(h || 0, m || 0, 0);

  const dateArrivee = new Date(dateBase.getTime() + dureeSec * 1000);
  
  const formatter = new Intl.DateTimeFormat('fr-FR', { hour: '2-digit', minute: '2-digit' });
  
  if (store.typeRecherche === 'arrivee') {
    const dateDepart = new Date(dateBase.getTime() - dureeSec * 1000);
    return {
      depart: formatter.format(dateDepart),
      arrivee: formatter.format(dateBase),
      duree: dureeMin
    };
  } else {
    return {
      depart: formatter.format(dateBase),
      arrivee: formatter.format(dateArrivee),
      duree: dureeMin
    };
  }
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
    const chemin = nouveauResultat.chemin;
    const coordonneesReelles = chemin.map(etape => [etape.longitude, etape.latitude]);

    const features = [];
    for (let i = 0; i < chemin.length - 1; i++) {
      const stationA = chemin[i];
      const stationB = chemin[i + 1];
      const ligneNom = stationB.ligne || 'Correspondance';
      const segmentColor = getCouleurLigne(ligneNom, stationB.couleur);

      features.push({
        type: 'Feature',
        properties: { color: segmentColor },
        geometry: {
          type: 'LineString',
          coordinates: [
            [stationA.longitude, stationA.latitude],
            [stationB.longitude, stationB.latitude]
          ]
        }
      });
    }

    const geojsonData = {
      type: 'FeatureCollection',
      features: features
    };

    if (mapInstance.getSource('tracet-itineraire')) {
      mapInstance.getSource('tracet-itineraire').setData(geojsonData);
    } else {
      mapInstance.addSource('tracet-itineraire', {
        type: 'geojson',
        data: geojsonData
      });

      mapInstance.addLayer({
        id: 'calque-itineraire',
        type: 'line',
        source: 'tracet-itineraire',
        layout: { 'line-join': 'round', 'line-cap': 'round' },
        paint: { 'line-color': ['get', 'color'], 'line-width': 5 }
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
  height: 100%;
  overflow: hidden;
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
  top: 0;
  left: 0;
  width: 380px;
  height: 100%;
  background-color: white;
  padding: 30px 25px;
  box-shadow: 4px 0 20px rgba(0,0,0,0.15);
  z-index: 2;
  overflow-y: auto;
  box-sizing: border-box;
}

h1 {
  font-size: 1.8rem;
  margin-top: 0;
  margin-bottom: 25px;
  text-align: left;
  color: #1a73e8;
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

/* Nouveaux styles pour les horaires et badges */
.resume-trajet {
  background-color: #f8f9fa;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 15px;
  border-left: 4px solid #1a73e8;
  font-size: 0.95rem;
  line-height: 1.5;
}

.liste-stations {
  padding-left: 0;
}

.station-ligne-info {
  margin: 8px 0;
  padding-left: 20px;
  border-left: 2px dashed #ccc;
  margin-left: 10px;
}

.station-nom {
  display: flex;
  align-items: center;
}

.badge {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 12px;
  color: white;
  font-size: 0.8rem;
  font-weight: bold;
  margin-right: 8px;
}

.badge-correspondance {
  background-color: #808080;
}

.badge-ligne {
  color: white;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.5);
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