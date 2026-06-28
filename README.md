# MED Paris - Moteur de recherche d'itinéraire éco-responsable 🍃

**MED Paris** (Mastercamp Éco-Développement) est un moteur de recherche d'itinéraires innovant conçu pour le réseau de transport francilien. L'objectif principal de cette solution est de sensibiliser et de contribuer à la réduction de l'empreinte carbone étudiante, en incitant à l'usage optimisé des transports en commun plutôt que des véhicules individuels.

Ce projet associe des structures de graphes complexes à un tableau de bord environnemental permettant de visualiser l'impact positif de ses trajets.

---

## 🛠️ Choix techniques et Algorithmiques

Afin de garantir d'excellentes performances tout en minimisant l'empreinte énergétique du serveur, les choix technologiques et algorithmiques suivants ont été actés :

- **Backend Java Spring Boot & JGraphT** : Le backend est construit en Java avec Spring Boot pour sa robustesse. La gestion du graphe s'appuie sur la bibliothèque `JGraphT`. L'approche "en mémoire" d'une structure de graphe pure plutôt que de multiples requêtes de base de données garantit une grande sobriété en ressources serveur, une faible allocation mémoire et une rapidité de réponse exceptionnelle.
- **Algorithme de Dijkstra** : La recherche du trajet optimal s'effectue via l'algorithme de Dijkstra. C'est l'approche la plus stricte et garantie pour trouver le plus court chemin pondéré (ici par le **temps de parcours en secondes**). 
- **Arbre Couvrant de Poids Minimum (ACPM) avec Kruskal** : L'algorithme de Kruskal est utilisé pour générer l'ACPM du réseau. Cette modélisation permet d'analyser la redondance et la topologie globale du métro parisien de façon optimale en éliminant les boucles cycliques.
- **Test de Connexité** : Un `ConnectivityInspector` permet de valider automatiquement la structure du graphe afin de s'assurer qu'aucune station (ou aucun ensemble de stations) ne se trouve isolée du reste du réseau lors des mises à jour des données.

---

## 📊 Données, Sources et Limites

- **Sources des données** : Les données cartographiques et horaires proviennent initialement de l'Open Data RATP / Île-de-France Mobilités.
- **Préparation** : Les données brutes (stations, arrêts, liaisons) sont filtrées, expurgées des doublons et formatées spécifiquement via le script de nettoyage `clean_data.py`. Le backend ingère ce format épuré.
- **Limites actuelles** : L'application repose actuellement sur un instantané statique du réseau (vitesses théoriques, liaisons fixes). Les incidents en temps réel (trafic, pannes de rames) ou les fluctuations d'horaires spécifiques ne sont pas encore pris en compte.

---

## 🌍 Performances et Impact Écologique

Au cœur du projet réside le **Tableau de bord écologique et technique** développé côté frontend (Vue.js) :

- **Performance brute** : Le backend trace et expose avec précision ses performances algorithmiques à chaque requête (chronométrage exact en millisecondes `ms`, et la RAM consommée via la supervision fine de la JVM en `Ko`).
- **Sensibilisation (Éco-Score)** : L'interface expose directement la **distance estimée** du parcours et calcule le **CO2 évité** par rapport au même trajet effectué en voiture individuelle thermique. Un retour direct et impactant pour les utilisateurs.

---

## 🚀 Prérequis et Guide de lancement

Le projet est divisé en deux briques principales : une API REST Java et une interface web moderne Vue.js.

### 1. Préparation des données (Python)
Avant de lancer le serveur, assurez-vous de préparer et nettoyer les fichiers de données d'entrée. 
*(Exécutez le script `clean_data.py` situé dans le dossier des données pour générer `stops_clean.txt` et `liaisons_clean.txt`)*.

### 2. Lancement de l'API (Backend)
Le backend requiert Java 17+ et Maven.
```bash
cd backend-api
# Compilation et démarrage (Windows/Linux/Mac)
./mvnw spring-boot:run
# ou avec l'installation Maven globale :
mvn spring-boot:run
```
Le serveur écoutera sur le port `8080`.

### 3. Lancement de l'Interface Web (Frontend)
Le frontend requiert Node.js.
```bash
cd app-itineraire
# Installation des dépendances
npm install
# Démarrage du serveur de développement avec Hot-Reload
npm run dev
```
L'interface sera accessible, généralement sur `http://localhost:5173`. Vous pouvez maintenant tracer votre itinéraire éco-responsable !