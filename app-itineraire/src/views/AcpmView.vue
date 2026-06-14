<template>
  <div class="acpm-container">
    <div class="header">
      <h1>Structure du Réseau (ACPM)</h1>
      <p>Visualisation de l'Arbre Couvrant de Poids Minimum calculé par le backend.</p>
      
      <button @click="store.chargerACPM" :disabled="store.isAcpmLoading">
        <span v-if="store.isAcpmLoading">Chargement du graphe...</span>
        <span v-else>🔄 Rafraîchir l'Arbre</span>
      </button>
    </div>

    <div ref="cyContainer" class="cy-container"></div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useTransportStore } from '../stores/transportStore'
import cytoscape from 'cytoscape'

const store = useTransportStore()
const cyContainer = ref(null)

// Au chargement de la page, on demande les données
onMounted(() => {
  store.chargerACPM()
})

// On surveille l'arrivée des données dans le store
watch(() => store.arbreACPM, (nouvelArbre) => {
  if (nouvelArbre && cyContainer.value) {
    
    // Initialisation de Cytoscape
    cytoscape({
      container: cyContainer.value,
      elements: nouvelArbre, // On injecte nos fausses données
      
      // Le style visuel (pour le Membre 5)
      style: [
        {
          selector: 'node',
          style: {
            'background-color': '#1a73e8',
            'label': 'data(label)',
            'color': '#333',
            'font-size': '14px',
            'text-valign': 'top',
            'text-halign': 'center',
            'margin': '10px'
          }
        },
        {
          selector: 'edge',
          style: {
            'width': 3,
            'line-color': '#ccc',
            'target-arrow-color': '#ccc',
            'target-arrow-shape': 'triangle',
            'curve-style': 'bezier',
            'label': 'data(weight)', // Affiche le poids du lien
            'text-background-color': '#fff',
            'text-background-opacity': 1
          }
        }
      ],
      
      // L'algorithme de disposition automatique
      layout: {
        name: 'cose', // algorithme de physique pour écarter les nœuds
        padding: 50
      }
    });
  }
})
</script>

<style scoped>
.acpm-container {
  display: flex;
  flex-direction: column;
  height: 80vh;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  padding: 20px;
  box-sizing: border-box;
}

.header {
  text-align: center;
  margin-bottom: 20px;
}

h1 {
  margin: 0 0 10px 0;
  color: #2c3e50;
}

button {
  padding: 10px 20px;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-weight: bold;
}

button:disabled {
  background-color: #a0d8bf;
}

/* La zone de dessin du graphe */
.cy-container {
  flex-grow: 1; /* Prend tout l'espace restant */
  border: 2px dashed #eee;
  border-radius: 8px;
  background-color: #fafafa;
}
</style>