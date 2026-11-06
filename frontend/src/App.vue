<script setup lang="ts">
import BottomNav from '@/components/BottomNav.vue'
import { currentStore } from '@/currentStore'
</script>

<template>
  <div class="min-h-screen bg-navy-50 font-sans text-slate-800">
    <header
      class="bg-gradient-to-br from-navy-600 to-navy-800 px-4 pt-6 pb-5 text-center text-white shadow-lg"
    >
      <h1 class="font-display text-3xl font-extrabold tracking-tight">
        Sneak<span class="text-navy-200">Stock</span>
      </h1>
      <p class="mt-2 text-base font-semibold">{{ currentStore.name }}</p>
      <p class="text-sm text-navy-200">Nº de tienda: {{ currentStore.number }}</p>
    </header>

    <!-- pb-24: deja hueco al final para que la barra de abajo no tape el contenido -->
    <main class="mx-auto max-w-xl p-4 pb-24">
      <RouterView />
    </main>

    <BottomNav />
  </div>
</template>
