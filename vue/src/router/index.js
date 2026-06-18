import { createWebHistory, createRouter } from 'vue-router'
import IndexPage from '@/components/pages/IndexPage.vue'
import MapPage from '@/components/pages/MapPage.vue'

export const ROUTES = {
  INDEX: 'INDEX',
  MAP: 'MAP'
}

const routes = [
  {
    name: ROUTES.INDEX,
    path: '/',
    component: IndexPage
  },
  {
    name: ROUTES.MAP,
    path: '/map',
    component: MapPage
  }
]

export const router = createRouter({
  history: createWebHistory('/'),
  routes
})
