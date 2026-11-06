<script setup lang="ts">
import { ref } from 'vue'
import { findAvailableStock, type StoreStock } from '@/api/stock'

const barcode = ref('')
const stores = ref<StoreStock[]>([])
const searched = ref(false)
const loading = ref(false)
const error = ref('')

async function search() {
  if (!barcode.value.trim()) return

  loading.value = true
  error.value = ''
  try {
    stores.value = await findAvailableStock(barcode.value.trim())
    searched.value = true
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Error inesperado'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="space-y-4">
    <h2 class="text-lg font-semibold text-gray-900">Consultar stock</h2>

    <form class="flex gap-2" @submit.prevent="search">
      <input
        v-model="barcode"
        type="text"
        inputmode="numeric"
        placeholder="Escanea o escribe el código de barras"
        class="flex-1 rounded-lg border border-gray-300 bg-white px-3 py-3 text-base focus:border-gray-900 focus:outline-none"
      />
      <button
        type="submit"
        :disabled="loading"
        class="rounded-lg bg-gray-900 px-4 py-3 font-semibold text-white disabled:opacity-50"
      >
        {{ loading ? 'Buscando…' : 'Buscar' }}
      </button>
    </form>

    <p v-if="error" class="rounded-lg bg-red-100 px-3 py-2 text-red-800">{{ error }}</p>

    <p v-else-if="searched && stores.length === 0" class="text-gray-600">
      No hay pares de esta talla en ninguna tienda abierta.
    </p>

    <ul v-else class="space-y-2">
      <li
        v-for="store in stores"
        :key="store.storeNumber"
        class="flex items-center justify-between rounded-lg bg-white px-4 py-3 shadow-sm"
      >
        <div>
          <p class="font-semibold text-gray-900">{{ store.storeName }}</p>
          <p class="text-sm text-gray-500">Tienda {{ store.storeNumber }} · {{ store.phone }}</p>
        </div>
        <span class="text-2xl font-bold text-gray-900">{{ store.quantity }}</span>
      </li>
    </ul>
  </section>
</template>
