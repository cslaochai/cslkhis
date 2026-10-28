<template>
  <div
      :class="align === 'start' ? 'justify-start' : 'justify-center'"
      class="flex h-[var(--his-actionbar-h)] shrink-0 items-center gap-3 rounded-lg border border-slate-200 bg-white px-4 py-2.5 shadow-sm">
    <slot/>
  </div>
</template>

<script setup>
defineProps({
  align: {type: String, required: false, default: 'center'}
});
</script>
