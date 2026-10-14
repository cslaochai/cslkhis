<template>
  <div class="his-card p-4 flex items-center gap-4">
    <div class="w-12 h-12 rounded-lg flex items-center justify-center text-white" :style="{ background: color }">
      <el-icon :size="24"><component :is="icon" /></el-icon>
    </div>
    <div class="min-w-0">
      <div class="text-[12px] text-gray-500">{{ label }}</div>
      <div class="amount-strong leading-tight">{{ value }}</div>
      <div v-if="sub" class="text-[11px] text-gray-400 mt-0.5">{{ sub }}</div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  label: String,
  value: [String, Number],
  sub: String,
  icon: { type: [String, Object], default: 'Tickets' },
  color: { type: String, default: '#1269B5' },
})
</script>
