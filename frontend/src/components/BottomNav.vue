<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()

// Opciones de la barra: para añadir una pantalla nueva basta con añadir un elemento aquí
const items = [
  {
    to: '/',
    label: 'Consultar',
    icon: 'M11 4a7 7 0 1 0 0 14 7 7 0 0 0 0-14Zm9 16-3.5-3.5',
  },
  {
    to: '/vender',
    label: 'Vender',
    icon: 'M2 3h3l2.5 12h11L21 7H6.2M9 20h.01M18 20h.01',
  },
  {
    to: '/entradas',
    label: 'Entradas',
    icon: 'M3 7.5 12 3l9 4.5v9L12 21l-9-4.5v-9Zm0 0 9 4.5 9-4.5M12 12v9',
  },
  {
    to: '/traspasos',
    label: 'Traspasos',
    icon: 'M7 4 3 8l4 4M3 8h14M17 20l4-4-4-4M21 16H7',
  },
]
</script>

<template>
  <nav
    class="fixed inset-x-0 bottom-0 border-t border-navy-100 bg-white shadow-[0_-4px_12px_rgba(0,0,0,0.05)]"
  >
    <ul class="mx-auto flex max-w-xl">
      <li v-for="item in items" :key="item.to" class="flex-1">
        <RouterLink
          :to="item.to"
          class="flex flex-col items-center gap-1 py-2.5 text-xs font-medium transition"
          :class="route.path === item.to ? 'text-navy-600' : 'text-slate-400'"
        >
          <svg
            class="size-6"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2.2"
            stroke-linecap="round"
            stroke-linejoin="round"
            aria-hidden="true"
          >
            <path :d="item.icon" />
          </svg>
          {{ item.label }}
        </RouterLink>
      </li>
    </ul>
  </nav>
</template>
