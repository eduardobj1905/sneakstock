<script setup lang="ts">
import { onMounted, ref, useTemplateRef } from 'vue'
import { findAvailableStock, type StoreStock } from '@/api/stock'
import { currentStore } from '@/currentStore'

const barcode = ref('')
const lastBarcode = ref('')
const stores = ref<StoreStock[]>([])
const searched = ref(false)
const loading = ref(false)
const error = ref('')

const barcodeInput = useTemplateRef<HTMLInputElement>('barcodeInput')

// Al abrir la pantalla, el cursor ya está en el campo: se puede escanear sin tocar nada
onMounted(() => barcodeInput.value?.focus())

async function search() {
  const code = barcode.value.trim()
  if (!code) return

  loading.value = true
  error.value = ''
  try {
    stores.value = await findAvailableStock(code)
    lastBarcode.value = code
    searched.value = true
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Error inesperado'
  } finally {
    loading.value = false
    // Campo vacío y con el cursor dentro, listo para la siguiente caja
    barcode.value = ''
    barcodeInput.value?.focus()
  }
}

function isCurrentStore(store: StoreStock): boolean {
  return store.storeNumber === currentStore.number
}
</script>

<template>
  <section class="space-y-5">
    <h2 class="font-display text-xl font-bold text-navy-900">Consultar stock</h2>

    <form class="space-y-3" @submit.prevent="search">
      <input
        ref="barcodeInput"
        v-model="barcode"
        type="text"
        inputmode="numeric"
        placeholder="Escanea o escribe el código de barras"
        class="w-full rounded-xl border-2 border-navy-100 bg-white px-4 py-3.5 text-base text-navy-900 placeholder:text-slate-400 transition focus:border-navy-500 focus:ring-4 focus:ring-navy-100 focus:outline-none"
      />
      <button
        type="submit"
        :disabled="loading"
        class="flex w-full items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-navy-500 to-navy-700 px-4 py-3.5 font-display text-lg font-semibold text-white shadow-md shadow-navy-700/30 transition hover:from-navy-600 hover:to-navy-800 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-60"
      >
        <svg
          class="size-5"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2.5"
          stroke-linecap="round"
          stroke-linejoin="round"
          aria-hidden="true"
        >
          <circle cx="11" cy="11" r="7" />
          <path d="m20 20-3.5-3.5" />
        </svg>
        {{ loading ? 'Buscando…' : 'Buscar' }}
      </button>
    </form>

    <p
      v-if="error"
      class="rounded-xl border border-red-200 bg-red-50 px-4 py-3 font-medium text-red-700"
    >
      {{ error }}
    </p>

    <template v-else-if="searched">
      <p class="text-sm text-slate-500">
        Resultados para <span class="font-semibold text-navy-700">{{ lastBarcode }}</span>
      </p>

      <p
        v-if="stores.length === 0"
        class="rounded-xl bg-white px-4 py-6 text-center text-slate-500 shadow-sm"
      >
        No hay pares de esta talla en ninguna tienda abierta.
      </p>

      <ul v-else class="space-y-3">
        <li
          v-for="store in stores"
          :key="store.storeNumber"
          class="flex items-center justify-between rounded-xl px-4 py-4 shadow-sm"
          :class="isCurrentStore(store) ? 'bg-navy-100 ring-2 ring-navy-500' : 'bg-white'"
        >
          <div>
            <p class="flex items-center gap-2 font-semibold text-navy-900">
              {{ store.storeName }}
              <span
                v-if="isCurrentStore(store)"
                class="rounded-full bg-navy-600 px-2 py-0.5 text-xs font-semibold text-white"
              >
                Tu tienda
              </span>
            </p>
            <p class="text-sm text-slate-500">Nº {{ store.storeNumber }} · {{ store.phone }}</p>
          </div>
          <div class="text-right">
            <p class="font-display text-3xl font-bold text-navy-700">{{ store.quantity }}</p>
            <p class="text-xs text-slate-500">{{ store.quantity === 1 ? 'par' : 'pares' }}</p>
          </div>
        </li>
      </ul>
    </template>
  </section>
</template>
