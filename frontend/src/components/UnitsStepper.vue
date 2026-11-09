<script setup lang="ts">
// Selector de unidades (− N +). Se usa con v-model: <UnitsStepper v-model="units" />
const units = defineModel<number>({ required: true })

const props = withDefaults(defineProps<{ label?: string; max?: number }>(), {
  label: 'Unidades',
  max: Infinity,
})
</script>

<template>
  <div class="flex items-center justify-between rounded-xl bg-white px-4 py-3 shadow-sm">
    <span class="font-medium text-navy-900">{{ props.label }}</span>
    <div class="flex items-center gap-3">
      <button
        type="button"
        aria-label="Quitar una unidad"
        :disabled="units <= 1"
        class="flex size-10 items-center justify-center rounded-full bg-navy-100 font-display text-xl font-bold text-navy-700 transition active:scale-95 disabled:opacity-40"
        @click="units--"
      >
        −
      </button>
      <span class="w-8 text-center font-display text-2xl font-bold text-navy-700">
        {{ units }}
      </span>
      <button
        type="button"
        aria-label="Añadir una unidad"
        :disabled="units >= props.max"
        class="flex size-10 items-center justify-center rounded-full bg-navy-100 font-display text-xl font-bold text-navy-700 transition active:scale-95 disabled:opacity-40"
        @click="units++"
      >
        +
      </button>
    </div>
  </div>
</template>
