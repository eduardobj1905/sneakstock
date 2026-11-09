<script setup lang="ts">
import { onMounted, ref, useTemplateRef } from 'vue'
import { findTransfer, receiveTransfer, type Transfer } from '@/api/stock'
import { describeVariant } from '@/api/catalog'
import { currentStore } from '@/currentStore'
import ProductCard from '@/components/ProductCard.vue'
import UnitsStepper from '@/components/UnitsStepper.vue'

const transferNumber = ref('')
const transfer = ref<Transfer | null>(null)
const units = ref(1)
const loading = ref(false)
const error = ref('')
const success = ref('')

const transferInput = useTemplateRef<HTMLInputElement>('transferInput')

onMounted(() => transferInput.value?.focus())

// Paso 1: ver qué trae el traspaso, de dónde viene y si es para mi tienda
async function lookup() {
  const id = Number(transferNumber.value.trim())
  if (!Number.isInteger(id) || id <= 0) {
    error.value = 'Escribe un número de traspaso válido'
    return
  }

  loading.value = true
  error.value = ''
  success.value = ''
  try {
    const found = await findTransfer(id)
    if (found.received) {
      error.value = `El traspaso nº ${id} ya se recibió`
    } else if (found.destinationStoreNumber !== currentStore.number) {
      error.value = `El traspaso nº ${id} va dirigido a ${found.destinationStoreName} (${found.destinationStoreNumber}), no a esta tienda`
    } else {
      transfer.value = found
      units.value = found.sentUnits
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Error inesperado'
  } finally {
    loading.value = false
  }
}

// Paso 2: confirmar los pares que han llegado de verdad
async function receive() {
  if (!transfer.value) return

  loading.value = true
  error.value = ''
  try {
    const received = transfer.value
    await receiveTransfer(received.transferId, currentStore.number, units.value)
    const missing = received.sentUnits - units.value
    success.value =
      `Traspaso nº ${received.transferId} recibido: ${units.value} ${units.value === 1 ? 'par' : 'pares'} de ${describeVariant(received.variant)}.` +
      (missing > 0 ? ` Queda anotado que faltan ${missing}.` : '')
    reset()
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Error inesperado'
  } finally {
    loading.value = false
  }
}

function reset() {
  transferNumber.value = ''
  transfer.value = null
  transferInput.value?.focus()
}
</script>

<template>
  <div class="space-y-4">
    <form v-if="!transfer" class="space-y-4" @submit.prevent="lookup">
      <input
        ref="transferInput"
        v-model="transferNumber"
        type="text"
        inputmode="numeric"
        placeholder="Escanea o escribe el nº de traspaso de la caja"
        class="w-full rounded-xl border-2 border-navy-100 bg-white px-4 py-3.5 text-base text-navy-900 placeholder:text-slate-400 transition focus:border-navy-500 focus:ring-4 focus:ring-navy-100 focus:outline-none"
      />
      <button
        type="submit"
        :disabled="loading"
        class="flex w-full items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-navy-500 to-navy-700 px-4 py-3.5 font-display text-lg font-semibold text-white shadow-md shadow-navy-700/30 transition hover:from-navy-600 hover:to-navy-800 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-60"
      >
        {{ loading ? 'Un momento…' : 'Buscar traspaso' }}
      </button>
    </form>

    <template v-else>
      <div class="flex items-center justify-between rounded-xl bg-navy-100 px-4 py-3">
        <div>
          <p class="text-xs font-medium tracking-wide text-navy-600 uppercase">
            Traspaso nº {{ transfer.transferId }}
          </p>
          <p class="font-display text-lg font-bold text-navy-900">
            Desde {{ transfer.originStoreName }} ({{ transfer.originStoreNumber }})
          </p>
        </div>
        <button
          type="button"
          class="rounded-lg px-3 py-1.5 text-sm font-semibold text-navy-700 hover:bg-navy-200"
          @click="reset"
        >
          Cancelar
        </button>
      </div>

      <ProductCard :variant="transfer.variant" />

      <p class="px-1 text-sm text-slate-600">
        Enviados: <span class="font-semibold text-navy-900">{{ transfer.sentUnits }}</span
        >. Si ha llegado alguno menos, bájalo antes de confirmar.
      </p>

      <UnitsStepper v-model="units" label="Recibidos" :max="transfer.sentUnits" />

      <button
        type="button"
        :disabled="loading"
        class="flex w-full items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-navy-500 to-navy-700 px-4 py-3.5 font-display text-lg font-semibold text-white shadow-md shadow-navy-700/30 transition hover:from-navy-600 hover:to-navy-800 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-60"
        @click="receive"
      >
        {{ loading ? 'Un momento…' : 'Recibir traspaso' }}
      </button>
    </template>

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
  </div>
</template>
