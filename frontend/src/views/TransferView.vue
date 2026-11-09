<script setup lang="ts">
import { ref } from 'vue'
import TransferSend from '@/components/TransferSend.vue'
import TransferReceive from '@/components/TransferReceive.vue'

const tab = ref<'send' | 'receive'>('send')
</script>

<template>
  <section class="space-y-5">
    <h2 class="font-display text-xl font-bold text-navy-900">Traspasos</h2>

    <div class="grid grid-cols-2 gap-1 rounded-xl bg-navy-100 p-1" role="tablist">
      <button
        type="button"
        role="tab"
        :aria-selected="tab === 'send'"
        class="rounded-lg py-2.5 font-display font-semibold transition"
        :class="tab === 'send' ? 'bg-white text-navy-800 shadow-sm' : 'text-navy-600'"
        @click="tab = 'send'"
      >
        Enviar
      </button>
      <button
        type="button"
        role="tab"
        :aria-selected="tab === 'receive'"
        class="rounded-lg py-2.5 font-display font-semibold transition"
        :class="tab === 'receive' ? 'bg-white text-navy-800 shadow-sm' : 'text-navy-600'"
        @click="tab = 'receive'"
      >
        Recibir
      </button>
    </div>

    <TransferSend v-if="tab === 'send'" />
    <TransferReceive v-else />
  </section>
</template>
