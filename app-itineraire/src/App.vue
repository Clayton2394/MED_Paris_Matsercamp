<template>
  <div class="app-container">
    <nav>
      <router-link to="/">Rechercher un Itinéraire</router-link> |
      <router-link to="/acpm">Voir l'ACPM</router-link>
    </nav>

    <main>
      <router-view></router-view>
    </main>
  </div>
</template>

<style scoped>
nav {
  padding: 20px;
  background-color: #2c3e50;
  text-align: center;
  margin-bottom: 30px;
  border-radius: 8px;
}

nav a {
  font-weight: bold;
  color: white;
  text-decoration: none;
  margin: 0 15px;
  font-size: 1.2rem;
}

/* Vue.js ajoute automatiquement cette classe quand on est sur la page active */
nav a.router-link-exact-active {
  color: #42b983; /* Vert Vue.js */
}
</style>