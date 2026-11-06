import { createRouter, createWebHistory } from 'vue-router'
import StockLookupView from '@/views/StockLookupView.vue'
import SaleView from '@/views/SaleView.vue'
import EntryView from '@/views/EntryView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'stock-lookup',
      component: StockLookupView,
    },
    {
      path: '/vender',
      name: 'sale',
      component: SaleView,
    },
    {
      path: '/entradas',
      name: 'entry',
      component: EntryView,
    },
  ],
})

export default router
