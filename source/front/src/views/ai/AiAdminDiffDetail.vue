<template>
  <el-dialog
      v-model="visible"
      :title="`草稿留痕 · ${row?.patientName || ''}（${row?.deptName || ''}）`"
      width="720px"
  >
    <p class="diff-meta">
      医生 {{ row?.doctorName || '—' }} · {{ (row?.createTime || '').replace('T', ' ').slice(0, 19) }}
    </p>
    <div class="diff-view">
      <template v-for="(seg, i) in diffSegments" :key="i">
        <span v-if="seg.type === 1" class="diff-del">{{ seg.text }}</span>
        <span v-else-if="seg.type === 2" class="diff-add">{{ seg.text }}</span>
        <span v-else>{{ seg.text }}</span>
      </template>
      <span v-if="!diffSegments.length">{{ row?.finalText }}</span>
    </div>
    <p class="diff-legend">
      <span class="diff-del">删除</span> / <span class="diff-add">新增</span> ——
      医生对 AI 草稿的改动；文本过长时尾部可能被截断
    </p>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 草稿留痕详情弹框（自 AiAdminView 拆出，只读，红删绿增渲染用模板插值，禁止 v-html）
 *
 * 留痕没有单条查询接口，数据源就是列表行快照（row.diffJson 为分段数组），打开时解析一次。
 */
import {computed, ref, watch} from 'vue';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 留痕行快照（diffJson 即分段数据源）
  row: {type: Object, default: null},
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const diffSegments = ref([]);

// 打开时按行快照解析（与拆分前 showDiff 的取数口径一致）
watch(() => props.modelValue, (v) => {
  if (!v) return;
  diffSegments.value = [];
  try {
    // diffJson 是留痕快照，坏数据只退化为展示终稿原文，不许整页报错
    const parsed = JSON.parse(props.row?.diffJson || '[]');
    if (Array.isArray(parsed)) {
      diffSegments.value = parsed;
    }
  } catch {
    /* 保持空数组 */
  }
});
</script>

<style scoped>
.diff-meta {
  margin: 0 0 8px;
  font-size: 12px;
  color: #9ca3af;
}

.diff-view {
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.8;
  white-space: pre-wrap;
  max-height: 360px;
  overflow-y: auto;
}

.diff-del {
  color: #b91c1c;
  background: #fee2e2;
  text-decoration: line-through;
}

.diff-add {
  color: #047857;
  background: #d1fae5;
}

.diff-legend {
  margin: 8px 0 0;
  font-size: 12px;
  color: #9ca3af;
}
</style>
