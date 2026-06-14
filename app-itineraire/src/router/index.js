import { createRouter, createWebHistory } from 'vue-router'

const routes = [

   { path: '/', component: () => import('../views/ItineraireView.vue') },
   { path: '/acpm', component: () => import('../views/AcpmView.vue') }
]

export const router = createRouter({
  history: createWebHistory(),
  routes
})