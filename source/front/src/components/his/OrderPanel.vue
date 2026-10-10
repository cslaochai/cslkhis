<template>
  <el-collapse :model-value="defaultOpen ? ['1'] : []" class="order-panel">
    <el-collapse-item name="1">
      <template #title>
        <div class="flex w-full items-center justify-between pr-2">
          <span class="text-sm font-bold text-slate-700">{{ title }}</span>
          <span v-if="countText" class="text-xs font-normal text-slate-400">{{ countText }}</span>
        </div>
      </template>
      <div class="pt-1">
        <slot/>
      </div>
    </el-collapse-item>
  </el-collapse>
</template>

<script setup>
defineProps({
  title: {type: String, required: true},
  countText: {type: String, required: false, default: ''},
  defaultOpen: {type: Boolean, required: false, default: true}
});
</script>

<style scoped>
.order-panel {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

:deep(.el-collapse-item__header) {
  height: auto;
  min-height: 37px;
  padding: 0 12px;
  border-bottom: 1px solid var(--his-card-border, #e3ecf5);
  background: #f8fafc;
}

:deep(.el-collapse-item__wrap) {
  border-bottom: none;
}

:deep(.el-collapse-item__content) {
  padding: 8px 12px 12px;
}
</style>
