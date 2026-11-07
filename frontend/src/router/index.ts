import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'lobby', component: () => import('@/views/LobbyView.vue') },
    { path: '/room/:roomId', name: 'room', component: () => import('@/views/RoomView.vue') },
    { path: '/table/:roomId', name: 'table', component: () => import('@/views/TableView.vue') },
    { path: '/stats', name: 'stats', component: () => import('@/views/StatsView.vue') }
  ]
})

export default router
