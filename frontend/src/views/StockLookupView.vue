<script setup lang="ts">
import { onMounted, ref, useTemplateRef } from 'vue'
import { findAvailableStock, findStockInStore, type StoreStock } from '@/api/stock'
import { findVariant, type Variant } from '@/api/catalog'
import { currentStore } from '@/currentStore'
import ProductCard from '@/components/ProductCard.vue'

const barcode = ref('')
const variant = ref<Variant | null>(null)
const myStock = ref<StoreStock | null>(null)
// null = todavía no se han pedido; solo se consultan si el dependiente pulsa el botón
const otherStores = ref<StoreStock[] | null>(null)
const loading = ref(false)
const loadingOthers = ref(false)
const error = ref('')

const barcodeInput = useTemplateRef<HTMLInputElement>('barcodeInput')

// Al abrir la pantalla, el cursor ya está en el campo: se puede escanear sin tocar nada
onMounted(() => barcodeInput.value?.focus())

async function search() {
  const code = barcode.value.trim()
  if (!code) return

  loading.value = true
  error.value = ''
  otherStores.value = null
  try {
    // Qué zapatilla es y cuántas hay en mi tienda. Si el código no existe, findVariant falla.
    const [foundVariant, foundStock] = await Promise.all([
      findVariant(code),
      findStockInStore(code, currentStore.number),
    ])
    variant.value = foundVariant
    myStock.value = foundStock
  } catch (e) {
    variant.value = null
    myStock.value = null
    error.value = e instanceof Error ? e.message : 'Error inesperado'
  } finally {
    loading.value = false
    // Campo vacío y con el cursor dentro, listo para la siguiente caja
    barcode.value = ''
    barcodeInput.value?.focus()
  }
}

// Carga bajo demanda: las demás tiendas solo cuando el cliente pregunta por ellas
async function showOtherStores() {
  if (!variant.value) return

  loadingOthers.value = true
  error.value = ''
  try {
    const stores = await findAvailableStock(variant.value.barcode)
    otherStores.value = stores.filter((store) => store.storeNumber !== currentStore.number)
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Error inesperado'
  } finally {
    loadingOthers.value = false
  }
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

    <template v-if="variant && myStock">
      <ProductCard :variant="variant" />

      <!-- Mi tienda: lo primero que necesita saber el dependiente -->
      <div
        class="flex items-center justify-between rounded-xl px-4 py-4 shadow-sm"
        :class="
          myStock.quantity > 0
            ? 'bg-emerald-50 ring-2 ring-emerald-500'
            : 'bg-amber-50 ring-2 ring-amber-400'
        "
      >
        <div>
          <p class="text-xs font-semibold tracking-widest text-slate-500 uppercase">En tu tienda</p>
          <p class="font-semibold text-navy-900">{{ myStock.storeName }}</p>
          <p v-if="myStock.quantity === 0" class="text-sm font-medium text-amber-700">
            No te queda ningún par de esta talla
          </p>
        </div>
        <div class="text-right">
          <p
            class="font-display text-4xl font-bold"
            :class="myStock.quantity > 0 ? 'text-emerald-700' : 'text-amber-600'"
          >
            {{ myStock.quantity }}
          </p>
          <p class="text-xs text-slate-500">{{ myStock.quantity === 1 ? 'par' : 'pares' }}</p>
        </div>
      </div>

      <!-- Otras tiendas: solo bajo demanda -->
      <button
        v-if="otherStores === null"
        type="button"
        :disabled="loadingOthers"
        class="flex w-full items-center justify-center gap-2 rounded-xl border-2 border-navy-500 bg-white px-4 py-3 font-display font-semibold text-navy-700 transition hover:bg-navy-50 active:scale-[0.98] disabled:opacity-60"
        @click="showOtherStores"
      >
        <svg
          class="size-5"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2.2"
          stroke-linecap="round"
          stroke-linejoin="round"
          aria-hidden="true"
        >
          <path d="M3 9.5 12 4l9 5.5M5 10v9h14v-9M9 19v-5h6v5" />
        </svg>
        {{ loadingOthers ? 'Consultando…' : 'Ver stock en otras tiendas' }}
      </button>

      <template v-else>
        <h3 class="flex items-center gap-2 pt-1 font-display text-lg font-bold text-navy-900">
          Otras tiendas
          <span
            v-if="otherStores.length > 0"
            class="rounded-full bg-navy-100 px-2 py-0.5 text-xs font-semibold text-navy-700"
          >
            {{ otherStores.length }}
          </span>
        </h3>

        <p
          v-if="otherStores.length === 0"
          class="rounded-xl bg-white px-4 py-6 text-center text-slate-500 shadow-sm"
        >
          Ninguna otra tienda abierta tiene pares de esta talla.
        </p>

        <ul v-else class="space-y-3">
          <li
            v-for="store in otherStores"
            :key="store.storeNumber"
            class="flex items-center justify-between rounded-xl bg-white px-4 py-4 shadow-sm"
          >
            <div>
              <p class="font-semibold text-navy-900">{{ store.storeName }}</p>
              <p class="text-sm text-slate-500">Nº {{ store.storeNumber }} · {{ store.phone }}</p>
            </div>
            <div class="text-right">
              <p class="font-display text-3xl font-bold text-navy-700">{{ store.quantity }}</p>
              <p class="text-xs text-slate-500">{{ store.quantity === 1 ? 'par' : 'pares' }}</p>
            </div>
          </li>
        </ul>
      </template>
    </template>
  </section>
</template>
