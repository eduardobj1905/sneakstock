<script setup lang="ts">
import type { Variant } from '@/api/catalog'

defineProps<{ variant: Variant }>()
</script>

<template>
  <div
    class="rounded-2xl bg-gradient-to-br from-navy-700 to-navy-900 px-5 py-4 text-white shadow-lg shadow-navy-900/20"
  >
    <div class="flex items-start gap-3">
      <div class="flex size-11 shrink-0 items-center justify-center rounded-xl bg-white/15">
        <svg
          class="size-6"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
          aria-hidden="true"
        >
          <path d="M2 17h20v2H2zM2 17l1-6 5 1 3-4 3 1c1 3 4 4 7 4l1 4" />
          <path d="M9 12l1 1M11 10.5l1 1" />
        </svg>
      </div>
      <div class="min-w-0">
        <p class="text-xs font-semibold tracking-widest text-navy-200 uppercase">Artículo</p>
        <p class="font-display text-xl leading-tight font-bold">
          {{ variant.brand }} {{ variant.model }}
        </p>
        <p class="text-sm text-navy-200">{{ variant.color }}</p>
      </div>
    </div>

    <div class="mt-3 flex items-center justify-between border-t border-white/15 pt-3">
      <span class="rounded-full bg-white px-3 py-1 text-sm font-bold text-navy-800">
        US {{ variant.sizeUs }} · EU {{ variant.sizeEu }}
      </span>
      <span class="font-mono text-sm text-navy-200">{{ variant.barcode }}</span>
    </div>
  </div>
</template>
