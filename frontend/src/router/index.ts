import { createRouter, createWebHistory } from 'vue-router'
import StockLookupView from '@/views/StockLookupView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'stock-lookup',
      component: StockLookupView,
    },
  ],
})

export default router
