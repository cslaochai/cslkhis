<template>
  <!-- noCollapse：医嘱开单卡常驻展开，不走 el-collapse（无折叠箭头、header 不可点、无折叠交互） -->
  <div v-if="noCollapse" class="order-panel no-collapse" :class="{ fill }">
    <div class="order-panel__header">
      <div class="flex w-full items-center gap-2 pr-2">
        <span class="text-sm font-bold text-slate-700">{{ title }}</span>
        <!-- 标题右侧操作区（如「使用模板」按钮）：紧跟标题，不靠右 -->
        <div class="flex shrink-0 items-center gap-2">
          <slot name="header-extra"/>
        </div>
        <span v-if="countText" class="ml-auto text-xs font-normal text-slate-400">{{ countText }}</span>
      </div>
    </div>
    <div class="order-panel__body">
      <slot/>
    </div>
  </div>
  <el-collapse v-else :model-value="defaultOpen ? ['1'] : []" class="order-panel" :class="{ fill }">
    <el-collapse-item name="1">
      <template #title>
        <div class="flex w-full items-center gap-2 pr-2">
          <span class="text-sm font-bold text-slate-700">{{ title }}</span>
          <!-- 标题右侧操作区（如「使用模板」按钮）：紧跟标题，不靠右 -->
          <div class="flex shrink-0 items-center gap-2">
            <slot name="header-extra"/>
          </div>
          <span v-if="countText" class="ml-auto text-xs font-normal text-slate-400">{{ countText }}</span>
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
  defaultOpen: {type: Boolean, required: false, default: true},
  // noCollapse：医嘱开单卡常驻不可折叠，隐藏折叠箭头（标题右侧不留折叠按钮）
  noCollapse: {type: Boolean, required: false, default: false},
  // fill：撑满父容器高度（医嘱栏三卡均分场景），卡内容区不滚动、滚动只在表格内部；
  // 不传则维持内容自适应高度（原行为）
  fill: {type: Boolean, required: false, default: false}
});
</script>

<style scoped>
.order-panel {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

.order-panel__header {
  min-height: 37px;
  display: flex;
  align-items: center;
  padding: 0 12px;
  border-bottom: 1px solid var(--his-card-border, #e3ecf5);
  background: #f8fafc;
}

.order-panel__body {
  padding: 8px 12px 12px;
  min-height: 0;
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

/* fill 模式：卡占满父容器（医嘱栏内 flex 均分高度），标题固定、内容区不滚动（滚动只在表格内部） */
.order-panel.fill {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.order-panel.fill :deep(.el-collapse-item) {
  display: flex;
  flex-direction: column;
  min-height: 0;
  flex: 1;
}

.order-panel.fill :deep(.el-collapse-item__wrap) {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.order-panel.fill :deep(.el-collapse-item__content) {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.order-panel.fill .order-panel__body {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
</style>
