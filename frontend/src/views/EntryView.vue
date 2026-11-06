<script setup lang="ts">
import { computed, nextTick, onMounted, ref, useTemplateRef } from 'vue'
import { receiveDelivery } from '@/api/stock'
import { findVariant, type Variant } from '@/api/catalog'
import { currentStore } from '@/currentStore'

interface ScannedLine {
  variant: Variant
  units: number
}

const deliveryNote = ref('')
const started = ref(false)
const barcode = ref('')
const lines = ref<ScannedLine[]>([])
const loading = ref(false)
const error = ref('')
const success = ref('')

const totalUnits = computed(() => lines.value.reduce((total, line) => total + line.units, 0))

const deliveryNoteInput = useTemplateRef<HTMLInputElement>('deliveryNoteInput')
const barcodeInput = useTemplateRef<HTMLInputElement>('barcodeInput')

onMounted(() => deliveryNoteInput.value?.focus())

// Paso 1: escanear el número de albarán
async function start() {
  if (!deliveryNote.value.trim()) return

  error.value = ''
  success.value = ''
  started.value = true
  await nextTick()
  barcodeInput.value?.focus()
}

// Paso 2: escanear cada zapatilla. Si ya está en la lista, suma una unidad
async function scan() {
  const code = barcode.value.trim()
  barcode.value = ''
  if (!code) return

  error.value = ''
  const existing = lines.value.find((line) => line.variant.barcode === code)
  if (existing) {
    existing.units++
    return
  }

  loading.value = true
  try {
    const variant = await findVariant(code)
    lines.value.unshift({ variant, units: 1 })
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Error inesperado'
  } finally {
    loading.value = false
    barcodeInput.value?.focus()
  }
}

function decrease(line: ScannedLine) {
  if (line.units > 1) line.units--
}

function remove(line: ScannedLine) {
  lines.value = lines.value.filter((other) => other !== line)
}

// Paso 3: cuando la lista cuadra con el albarán, se registra todo de una vez
async function register() {
  if (lines.value.length === 0) return

  loading.value = true
  error.value = ''
  try {
    await receiveDelivery(
      deliveryNote.value.trim(),
      currentStore.number,
      lines.value.map((line) => ({ barcode: line.variant.barcode, units: line.units })),
    )
    success.value = `Albarán ${deliveryNote.value.trim()} registrado: ${totalUnits.value} unidades de ${lines.value.length} ${lines.value.length === 1 ? 'referencia' : 'referencias'}`
    reset()
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Error inesperado'
  } finally {
    loading.value = false
  }
}

function reset() {
  deliveryNote.value = ''
  lines.value = []
  started.value = false
  nextTick(() => deliveryNoteInput.value?.focus())
}

function cancel() {
  error.value = ''
  success.value = ''
  reset()
}
</script>

<template>
  <section class="space-y-5">
    <h2 class="font-display text-xl font-bold text-navy-900">Entrada de mercancía</h2>

    <!-- Paso 1: número de albarán -->
    <form v-if="!started" class="space-y-4" @submit.prevent="start">
      <input
        ref="deliveryNoteInput"
        v-model="deliveryNote"
        type="text"
        maxlength="30"
        placeholder="Escanea o escribe el número de albarán"
        class="w-full rounded-xl border-2 border-navy-100 bg-white px-4 py-3.5 text-base text-navy-900 placeholder:text-slate-400 transition focus:border-navy-500 focus:ring-4 focus:ring-navy-100 focus:outline-none"
      />
      <button
        type="submit"
        class="flex w-full items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-navy-500 to-navy-700 px-4 py-3.5 font-display text-lg font-semibold text-white shadow-md shadow-navy-700/30 transition hover:from-navy-600 hover:to-navy-800 active:scale-[0.98]"
      >
        Empezar recepción
      </button>
    </form>

    <!-- Pasos 2 y 3: escanear zapatillas y registrar -->
    <template v-else>
      <div class="flex items-center justify-between rounded-xl bg-navy-100 px-4 py-3">
        <div>
          <p class="text-xs font-medium tracking-wide text-navy-600 uppercase">Albarán</p>
          <p class="font-display text-lg font-bold text-navy-900">{{ deliveryNote }}</p>
        </div>
        <button
          type="button"
          class="rounded-lg px-3 py-1.5 text-sm font-semibold text-navy-700 hover:bg-navy-200"
          @click="cancel"
        >
          Cancelar
        </button>
      </div>

      <form @submit.prevent="scan">
        <input
          ref="barcodeInput"
          v-model="barcode"
          type="text"
          inputmode="numeric"
          :disabled="loading"
          placeholder="Escanea cada zapatilla del envío"
          class="w-full rounded-xl border-2 border-navy-100 bg-white px-4 py-3.5 text-base text-navy-900 placeholder:text-slate-400 transition focus:border-navy-500 focus:ring-4 focus:ring-navy-100 focus:outline-none"
        />
      </form>

      <p
        v-if="lines.length === 0"
        class="rounded-xl bg-white px-4 py-6 text-center text-slate-500 shadow-sm"
      >
        Todavía no has escaneado ninguna zapatilla.
      </p>

      <ul v-else class="space-y-2">
        <li
          v-for="line in lines"
          :key="line.variant.barcode"
          class="flex items-center justify-between gap-3 rounded-xl bg-white px-4 py-3 shadow-sm"
        >
          <div class="min-w-0">
            <p class="truncate font-semibold text-navy-900">
              {{ line.variant.brand }} {{ line.variant.model }}
            </p>
            <p class="text-sm text-slate-500">
              US {{ line.variant.sizeUs }} · EU {{ line.variant.sizeEu }}
              <span class="font-mono">· {{ line.variant.barcode }}</span>
            </p>
          </div>
          <div class="flex shrink-0 items-center gap-2">
            <button
              type="button"
              :aria-label="`Quitar una unidad de ${line.variant.barcode}`"
              :disabled="line.units <= 1"
              class="flex size-8 items-center justify-center rounded-full bg-navy-100 font-bold text-navy-700 disabled:opacity-40"
              @click="decrease(line)"
            >
              −
            </button>
            <span class="w-6 text-center font-display text-xl font-bold text-navy-700">
              {{ line.units }}
            </span>
            <button
              type="button"
              :aria-label="`Añadir una unidad de ${line.variant.barcode}`"
              class="flex size-8 items-center justify-center rounded-full bg-navy-100 font-bold text-navy-700"
              @click="line.units++"
            >
              +
            </button>
            <button
              type="button"
              :aria-label="`Eliminar ${line.variant.barcode} de la lista`"
              class="ml-1 flex size-8 items-center justify-center rounded-full text-slate-400 hover:bg-red-50 hover:text-red-600"
              @click="remove(line)"
            >
              ✕
            </button>
          </div>
        </li>
      </ul>

      <div class="flex items-center justify-between px-1">
        <span class="font-medium text-navy-900">Total</span>
        <span class="font-display text-xl font-bold text-navy-700">
          {{ totalUnits }} {{ totalUnits === 1 ? 'unidad' : 'unidades' }}
        </span>
      </div>

      <button
        type="button"
        :disabled="loading || lines.length === 0"
        class="flex w-full items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-navy-500 to-navy-700 px-4 py-3.5 font-display text-lg font-semibold text-white shadow-md shadow-navy-700/30 transition hover:from-navy-600 hover:to-navy-800 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-60"
        @click="register"
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
          <path d="M3 7.5 12 3l9 4.5v9L12 21l-9-4.5v-9Z" />
          <path d="m3 7.5 9 4.5 9-4.5M12 12v9" />
        </svg>
        {{ loading ? 'Un momento…' : `Registrar entrada (${totalUnits})` }}
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
  </section>
</template>
