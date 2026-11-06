<script setup lang="ts">
import { onMounted, ref, useTemplateRef } from 'vue'
import { sell } from '@/api/stock'
import { currentStore } from '@/currentStore'

const barcode = ref('')
const units = ref(1)
const loading = ref(false)
const error = ref('')
const success = ref('')

const barcodeInput = useTemplateRef<HTMLInputElement>('barcodeInput')

onMounted(() => barcodeInput.value?.focus())

function decrease() {
  if (units.value > 1) units.value--
}

function increase() {
  units.value++
}

async function submitSale() {
  const code = barcode.value.trim()
  if (!code) return

  loading.value = true
  error.value = ''
  success.value = ''
  try {
    await sell(code, currentStore.number, units.value)
    success.value = `Venta registrada: ${units.value} ${units.value === 1 ? 'par' : 'pares'} de ${code}`
    // Venta hecha: formulario limpio para el siguiente cliente
    barcode.value = ''
    units.value = 1
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Error inesperado'
  } finally {
    loading.value = false
    barcodeInput.value?.focus()
  }
}
</script>

<template>
  <section class="space-y-5">
    <h2 class="font-display text-xl font-bold text-navy-900">Vender</h2>

    <form class="space-y-4" @submit.prevent="submitSale">
      <input
        ref="barcodeInput"
        v-model="barcode"
        type="text"
        inputmode="numeric"
        placeholder="Escanea o escribe el código de barras"
        class="w-full rounded-xl border-2 border-navy-100 bg-white px-4 py-3.5 text-base text-navy-900 placeholder:text-slate-400 transition focus:border-navy-500 focus:ring-4 focus:ring-navy-100 focus:outline-none"
      />

      <div class="flex items-center justify-between rounded-xl bg-white px-4 py-3 shadow-sm">
        <span class="font-medium text-navy-900">Pares</span>
        <div class="flex items-center gap-3">
          <button
            type="button"
            aria-label="Quitar un par"
            :disabled="units <= 1"
            class="flex size-10 items-center justify-center rounded-full bg-navy-100 font-display text-xl font-bold text-navy-700 transition active:scale-95 disabled:opacity-40"
            @click="decrease"
          >
            −
          </button>
          <span class="w-8 text-center font-display text-2xl font-bold text-navy-700">
            {{ units }}
          </span>
          <button
            type="button"
            aria-label="Añadir un par"
            class="flex size-10 items-center justify-center rounded-full bg-navy-100 font-display text-xl font-bold text-navy-700 transition active:scale-95"
            @click="increase"
          >
            +
          </button>
        </div>
      </div>

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
          <circle cx="9" cy="20" r="1.5" />
          <circle cx="18" cy="20" r="1.5" />
          <path d="M2 3h3l2.5 12h11L21 7H6.2" />
        </svg>
        {{ loading ? 'Vendiendo…' : 'Vender' }}
      </button>
    </form>

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
  </section>
</template>
