<script setup lang="ts">
import { onMounted, ref, useTemplateRef, watch } from 'vue'
import { sendTransfer } from '@/api/stock'
import { describeVariant, findVariant, type Variant } from '@/api/catalog'
import { findOpenStores, type StoreSummary } from '@/api/stores'
import { currentStore } from '@/currentStore'
import ProductCard from '@/components/ProductCard.vue'
import UnitsStepper from '@/components/UnitsStepper.vue'

const barcode = ref('')
const variant = ref<Variant | null>(null)
const units = ref(1)
const destinations = ref<StoreSummary[]>([])
const destination = ref('')
const loading = ref(false)
const error = ref('')
const success = ref('')

const barcodeInput = useTemplateRef<HTMLInputElement>('barcodeInput')

onMounted(async () => {
  barcodeInput.value?.focus()
  try {
    // A mi propia tienda no puedo enviarme un traspaso
    const stores = await findOpenStores()
    destinations.value = stores.filter((store) => store.storeNumber !== currentStore.number)
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Error inesperado'
  }
})

// Si se escanea otro código, la zapatilla que se estaba mostrando ya no vale
watch(barcode, (value) => {
  if (variant.value && value.trim() !== variant.value.barcode) {
    variant.value = null
  }
})

// Paso 1: comprobar qué zapatilla es. Paso 2: enviarla a la tienda elegida.
async function submit() {
  const code = barcode.value.trim()
  if (!code) return

  error.value = ''
  success.value = ''

  if (variant.value && !destination.value) {
    error.value = 'Elige la tienda a la que envías el traspaso'
    return
  }

  loading.value = true
  try {
    if (!variant.value) {
      variant.value = await findVariant(code)
      return
    }

    const transferId = await sendTransfer(
      variant.value.barcode,
      currentStore.number,
      destination.value,
      units.value,
    )
    const destinationName = destinations.value.find(
      (s) => s.storeNumber === destination.value,
    )?.name
    success.value = `Traspaso nº ${transferId} enviado a ${destinationName}: ${units.value} ${units.value === 1 ? 'par' : 'pares'} de ${describeVariant(variant.value)}. Apunta el nº ${transferId} en la caja.`
    barcode.value = ''
    variant.value = null
    units.value = 1
    destination.value = ''
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Error inesperado'
  } finally {
    loading.value = false
    barcodeInput.value?.focus()
  }
}
</script>

<template>
  <form class="space-y-4" @submit.prevent="submit">
    <input
      ref="barcodeInput"
      v-model="barcode"
      type="text"
      inputmode="numeric"
      placeholder="Escanea la zapatilla que envías"
      class="w-full rounded-xl border-2 border-navy-100 bg-white px-4 py-3.5 text-base text-navy-900 placeholder:text-slate-400 transition focus:border-navy-500 focus:ring-4 focus:ring-navy-100 focus:outline-none"
    />

    <template v-if="variant">
      <ProductCard :variant="variant" />

      <UnitsStepper v-model="units" />

      <label class="block rounded-xl bg-white px-4 py-3 shadow-sm">
        <span class="mb-1 block font-medium text-navy-900">Tienda destino</span>
        <select
          v-model="destination"
          class="w-full rounded-lg border-2 border-navy-100 bg-white px-3 py-2.5 text-base text-navy-900 focus:border-navy-500 focus:outline-none"
        >
          <option value="" disabled>Elige una tienda</option>
          <option v-for="store in destinations" :key="store.storeNumber" :value="store.storeNumber">
            {{ store.name }} ({{ store.storeNumber }})
          </option>
        </select>
      </label>
    </template>

    <button
      type="submit"
      :disabled="loading"
      class="flex w-full items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-navy-500 to-navy-700 px-4 py-3.5 font-display text-lg font-semibold text-white shadow-md shadow-navy-700/30 transition hover:from-navy-600 hover:to-navy-800 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-60"
    >
      <template v-if="loading">Un momento…</template>
      <template v-else-if="variant">Enviar traspaso</template>
      <template v-else>Comprobar código</template>
    </button>

    <p
      v-if="error"
      class="rounded-xl border border-red-200 bg-red-50 px-4 py-3 font-medium text-red-700"
    >
      {{ error }}
    </p>

    <p
      v-if="success"
      class="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 font-medium text-emerald-700"
    >
      {{ success }}
    </p>
  </form>
</template>
