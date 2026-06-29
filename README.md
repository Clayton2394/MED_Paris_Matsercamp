<div align="center">
  <h1>🍃 MED Paris - Moteur de recherche d'itinéraire éco-responsable</h1>
  <p><strong>Mastercamp Éco-Développement - Réseau de transport francilien</strong></p>
  
  [![Vue.js](https://img.shields.io/badge/Vue%203-4FC08D?style=for-the-badge&logo=vuedotjs&logoColor=white)](https://vuejs.org/)
  [![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
  [![Java](https://img.shields.io/badge/Java_17-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
  [![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)](https://www.python.org/)
</div>

<br/>

**MED Paris** est un moteur de recherche d'itinéraires innovant conçu pour le réseau de transport francilien (Île-de-France Mobilités). L'objectif principal de cette solution est de sensibiliser et de contribuer à la réduction de l'empreinte carbone, en incitant à l'usage optimisé des transports en commun plutôt que des véhicules individuels.

Ce projet associe des structures de graphes complexes à un tableau de bord environnemental permettant de visualiser l'impact positif de ses trajets.

---

## ✨ Fonctionnalités Principales

- 🗺️ **Cartographie interactive** : Visualisation du réseau et des itinéraires grâce à **MapLibre GL**.
- 🕸️ **Visualisation de graphe** : Représentation topologique experte du réseau de transport via **Cytoscape**.
- ⏱️ **Calculs ultra-rapides** : Temps de parcours optimisés grâce aux algorithmes sur graphes tournant en mémoire.
- 🌱 **Éco-Score** : Évaluation directe du CO2 évité par rapport à un trajet thermique équivalent.

---

## 🛠️ Stack Technique & Algorithmique

### Backend (Java Spring Boot)
Afin de garantir d'excellentes performances tout en minimisant l'empreinte énergétique du serveur :
- **JGraphT** : Modélisation en mémoire (sans requêtes répétées en base de données) pour une sobriété en ressources et une faible allocation mémoire.
- **Algorithme de Dijkstra** : Recherche du trajet optimal pondéré par le **temps de parcours en secondes**.
- **Algorithme de Kruskal** : Génération de l'Arbre Couvrant de Poids Minimum (ACPM) pour analyser la redondance et la topologie du réseau (élimination des boucles cycliques).
- **Test de Connexité** : Utilisation d'un `ConnectivityInspector` garantissant qu'aucune station n'est isolée.

### Frontend (Vue 3 & Vite)
- **Vue 3 (Composition API) & Pinia** : Architecture moderne et réactive pour la gestion de l'état.
- **MapLibre GL** : Rendu cartographique vectoriel performant.
- **Cytoscape.js** : Moteur de visualisation de réseaux complexe.

### Traitement des Données (Python)
- Le script `clean_data.py` se charge de lire les données GTFS brutes (routes, trips, stops) de l'Open Data RATP / IDFM, d'en extraire le temps de trajet en secondes, et de générer les fichiers épurés `liaisons_clean.txt` et `stops_clean.txt`.

---

## 📊 Données, Sources et Limites

- **Sources des données** : Open Data RATP / Île-de-France Mobilités.
- **Préparation** : Les données brutes sont filtrées, expurgées des doublons et formatées via un pipeline Python.
- **Limites actuelles** : L'application repose actuellement sur un instantané statique du réseau. Les incidents en temps réel (trafic, pannes) ou les fluctuations d'horaires spécifiques ne sont pas encore pris en compte.

---

## 🚀 Prérequis et Guide de Lancement

Le projet est divisé en deux briques principales : une API REST Java et une interface web moderne Vue.js. Vous pouvez lancer le projet facilement via le script de lancement fourni.

### ⚡ Lancement Rapide (Windows)
Un script `run.bat` est à votre disposition à la racine du projet pour lancer simultanément le backend et le frontend.
```cmd
run.bat
```

### 🔧 Lancement Manuel

#### 1. Préparation des données (Python)
*(Si les données ne sont pas déjà générées dans `Data_clean`)*
Exécutez le script situé à la racine pour parser les données brutes :
```bash
python clean_data.py
```

#### 2. Lancement de l'API (Backend)
Le backend requiert **Java 17+** et **Maven**.
```bash
cd backend-api
./mvnw spring-boot:run
# Le serveur écoutera sur le port 8080.
```

#### 3. Lancement de l'Interface Web (Frontend)
Le frontend requiert **Node.js** (v16+).
```bash
cd app-itineraire
npm install
npm run dev
# L'interface sera accessible sur http://localhost:5173
```

---

<div align="center">
  <i>Contribuez à un monde plus vert, privilégiez les transports en commun ! 🚇🌱</i>
</div>