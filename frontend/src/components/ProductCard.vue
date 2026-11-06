<script setup lang="ts">
import type { Variant } from '@/api/catalog'

defineProps<{ variant: Variant }>()
</script>

<template>
  <div class="rounded-xl border-2 border-navy-200 bg-white px-4 py-3 shadow-sm">
    <p class="font-display text-lg font-bold text-navy-900">
      {{ variant.brand }} {{ variant.model }}
    </p>
    <p class="text-sm text-slate-500">{{ variant.color }}</p>
    <div class="mt-2 flex items-center justify-between">
      <span class="rounded-full bg-navy-100 px-3 py-1 text-sm font-semibold text-navy-700">
        US {{ variant.sizeUs }} · EU {{ variant.sizeEu }}
      </span>
      <span class="font-mono text-sm text-slate-500">{{ variant.barcode }}</span>
    </div>
  </div>
</template>
