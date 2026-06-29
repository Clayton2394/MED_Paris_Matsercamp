<template>
  <div class="app-container">
    <main>
      <router-view></router-view>
    </main>

    <nav class="view-switch">
      <router-link to="/" class="switch-btn" exact-active-class="active">🗺️ Carte</router-link>
      <router-link to="/acpm" class="switch-btn" exact-active-class="active">🌳 ACPM</router-link>
    </nav>
  </div>
</template>

<style scoped>
.app-container {
  width: 100vw;
  height: 100vh;
  position: relative;
  overflow: hidden;
}

main {
  width: 100%;
  height: 100%;
}

.view-switch {
  position: absolute;
  bottom: 30px;
  right: 30px;
  background-color: white;
  padding: 5px;
  border-radius: 30px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  display: flex;
  gap: 5px;
  z-index: 1000;
}

.switch-btn {
  padding: 10px 20px;
  font-weight: bold;
  color: #333;
  text-decoration: none;
  border-radius: 25px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.switch-btn:hover {
  background-color: #f0f0f0;
}

.switch-btn.active {
  background-color: #1a73e8;
  color: white;
}
</style>