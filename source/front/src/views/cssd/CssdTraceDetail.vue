<template>
  <el-dialog v-model="visible" :title="`追溯链 — ${detail?.packNo || ''}`" width="640px">
    <template v-if="detail">
      <div class="text-sm text-gray-500 mb-3">
        {{ detail.packName }} · {{ methodText(detail.sterilizeMethod) }} ·
        当前状态
        <el-tag :type="cssdStatusTag(detail.status)">{{ statusText(detail.status) }}</el-tag>
      </div>
      <el-timeline data-testid="cssd-trace-timeline">
        <el-timeline-item v-for="t in detail.traces || []" :key="t.id"
                          :timestamp="`${fmtTime(t.nodeTime)} · ${t.operatorName || '—'}`"
                          :type="t.result === 2 ? 'danger' : 'primary'">
          <span class="font-bold">{{ t.nodeTypeText }}</span>
          <el-tag v-if="t.result === 2" class="ml-1" size="small" type="danger">不合格</el-tag>
          <span v-if="t.sterilizerNo" class="text-xs text-gray-500 ml-2">锅次 {{ t.sterilizerNo }} / 批次 {{
              t.batchNo
            }}</span>
          <div v-if="t.remark" class="text-xs text-gray-500">{{ t.remark }}</div>
        </el-timeline-item>
      </el-timeline>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getCssdDetail} from '@/api/cssd';
import {dictLabelText} from '@/lib/utils';
import {cssdStatusTag, fmtTime} from '@/lib/cssd';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 器械包 id：打开时自拉追溯链全量数据
  id: {type: [Number, String], default: null},
  // 字典由父页一次加载后传入（与列表列共用同一份，避免两处各拉一次）
  statusDict: {type: Array, default: () => []},
  methodDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const detail = ref(null);

watch(visible, async (open) => {
  if (!open || props.id == null) return;
  detail.value = null;
  try {
    const res = await getCssdDetail(props.id);
    if (res.code === 200)
      detail.value = res.data;
    else
      ElMessage.error(res.message || '查询失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  }
});

const statusText = (v) => dictLabelText(props.statusDict, v);
const methodText = (v) => dictLabelText(props.methodDict, v);
</script>
