<script setup lang="ts">
withDefaults(defineProps<{ align?: 'center' | 'start' }>(), {align: 'center'})
</script>

<template>
  <div
      class="flex h-[var(--his-actionbar-h)] shrink-0 items-center gap-3 rounded-lg border border-slate-200 bg-white px-4 shadow-sm"
      :class="align === 'start' ? 'justify-start' : 'justify-center'">
    <slot/>
  </div>
</template>
