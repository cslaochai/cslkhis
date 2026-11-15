<template>
  <div class="wf-node" :class="[`wf-node-${nodeType}`, { picked: selected }]">
    <div class="wf-node-title">{{ data.label || '节点' }}</div>
    <div v-if="nodeType === 1 && data.permissionFlag" class="wf-node-perm">{{ data.permissionFlag }}</div>
    <div v-if="nodeType === 3 || nodeType === 4" class="wf-node-ai">{{ aiText }}</div>
    <div v-if="nodeType === 1" class="wf-node-tag" :class="tagClass">{{ tagText }}</div>
    <Handle v-if="nodeType !== 0" :position="Position.Left" type="target"/>
    <Handle v-if="nodeType !== 2" :position="Position.Right" type="source"/>
  </div>
</template>

<script lang="js" setup>
import {computed} from 'vue'
import {Handle, Position, useNode} from '@vue-flow/core'

// Vue Flow 会把这些 NodeProps 当 props 传入；不声明就会作为 fallthrough 挂到根元素，
// 拖拽时响应式代理与 DOM 属性冲突报 "Unhandled error during native event handler"
defineProps({
  id: {type: [String, Number], default: ''},
  type: {type: String, default: 'default'},
  selected: {type: Boolean, default: false},
  dragging: {type: Boolean, default: false},
  connectable: {type: Boolean, default: true},
  draggable: {type: Boolean, default: true},
  resizing: {type: Boolean, default: false},
  zIndex: {type: Number, default: 0},
  data: {type: Object, default: () => ({})},
  dimensions: {type: Object, default: () => ({width: 0, height: 0})},
  handleBounds: {type: String, default: '0'},
  sourcePosition: {type: String, default: 'right'},
  targetPosition: {type: String, default: 'left'},
  parentNodeId: {type: String, default: null},
  z: {type: Number, default: 0},
  dragHandle: {type: String, default: null},
})

const {node} = useNode()

const nodeType = computed(() => Number(node.value?.data?.nodeType ?? 1))
const ratio = computed(() => node.value?.data?.nodeRatio ?? '0')
const tagText = computed(() => {
  const r = Number(ratio.value)
  if (!r) return '或签'
  if (r >= 100) return '会签'
  return `票签 ${r}%`
})
const tagClass = computed(() => {
  const r = Number(ratio.value)
  if (!r) return 'tag-or'
  if (r >= 100) return 'tag-all'
  return 'tag-vote'
})
const aiText = computed(() => {
  const action = node.value?.data?.aiAction
  if (!action) return '未配 AI 动作'
  return action === 'ROUTE' ? '意图识别' : (node.value?.data?.aiTemplateName || '能力调用')
})
</script>

<style scoped>
.wf-node {
  background: #fff;
  border: 2px solid #1269B5;
  border-radius: 10px;
  padding: 10px 14px;
  min-width: 150px;
  box-shadow: 0 2px 6px rgba(15, 23, 42, .08);
  cursor: grab;
}

.wf-node:active {
  cursor: grabbing;
}

.wf-node:hover {
  box-shadow: 0 4px 14px rgba(18, 105, 181, .25);
}

.wf-node.picked {
  border-color: #B45309;
  box-shadow: 0 0 0 3px rgba(180, 83, 9, .18);
}

.wf-node-start {
  border-color: #0E9488;
  background: #ecfdf8;
  border-radius: 50%;
  width: 78px;
  height: 78px;
  min-width: 0;
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.wf-node-end {
  border-color: #64748b;
  background: #f8fafc;
  border-radius: 50%;
  width: 78px;
  height: 78px;
  min-width: 0;
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.wf-node-auto {
  border-color: #0E9488;
  background: #f0fdfa;
}

.wf-node-gateway {
  border-color: #B45309;
  background: #fffbf2;
}

.wf-node-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  word-break: break-all;
}

.wf-node-start .wf-node-title,
.wf-node-end .wf-node-title {
  font-size: 12px;
  line-height: 1.3;
}

.wf-node-perm,
.wf-node-ai {
  font-size: 11px;
  color: #64748b;
  margin-top: 4px;
  font-family: ui-monospace, monospace;
}

.wf-node-ai {
  color: #0E9488;
}

.wf-node-tag {
  display: inline-block;
  margin-top: 6px;
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 4px;
}

.tag-or {
  background: #e0f2fe;
  color: #075985;
}

.tag-all {
  background: #fee2e2;
  color: #b91c1c;
}

.tag-vote {
  background: #fef3c7;
  color: #a16207;
}

:deep(.vue-flow__handle) {
  width: 10px;
  height: 10px;
  background: #1269B5;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px #1269B5;
}
</style>
