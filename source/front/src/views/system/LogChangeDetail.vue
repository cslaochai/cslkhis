<template>
  <!-- 字段变更：同批次明细（一次保存改了哪些字段） -->
  <el-dialog v-model="visible" data-testid="field-batch-dialog" title="本次修改明细" width="720px">
    <el-descriptions :column="2" border class="batch-meta" size="small">
      <el-descriptions-item label="时间">{{ batchMeta.changeTime }}</el-descriptions-item>
      <el-descriptions-item label="操作人">{{ batchMeta.operatorName }}</el-descriptions-item>
      <el-descriptions-item label="对象">{{ batchMeta.bizTypeText }} ·
        {{ batchMeta.bizName || batchMeta.bizNo || batchMeta.bizId }}
      </el-descriptions-item>
      <el-descriptions-item label="批次号">{{ batchMeta.batchNo }}</el-descriptions-item>
    </el-descriptions>
    <el-table :data="batchRows" border class="batch-table" data-testid="field-batch-table" size="small" stripe>
      <el-table-column label="字段" prop="fieldLabel" width="140"/>
      <el-table-column label="变更前" min-width="200">
        <template #default="{ row }">{{ row.oldValue || '（空）' }}</template>
      </el-table-column>
      <el-table-column label="变更后" min-width="200">
        <template #default="{ row }">{{ row.newValue || '（空）' }}</template>
      </el-table-column>
      <template #empty>
        <span data-testid="field-batch-empty">本批次没有明细</span>
      </template>
    </el-table>
  </el-dialog>
</template>

<script lang="js" setup>
/**
 * 一次保存改 5 个字段会落 5 行，列表里散着看不出是"同一个人同一分钟改的"。
 * 点行按 batchNo 拉回整批，一次性看完这一刀动了什么 —— 这才是审计要问的。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getFieldChangeBatch} from '@/api/log';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 批次号：明细按它自取数
  batchNo: {type: [String, Number], default: null},
  // 列表当前行：批次抬头（时间/操作人/对象）本就取自这一行，顺带「打开瞬间不闪白」
  row: {type: Object, default: null},
})
const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const batchRows = ref([]);
const batchMeta = ref({});

watch(visible, async (open) => {
  if (!open) return;
  batchMeta.value = props.row || {};
  batchRows.value = [];
  if (!props.batchNo) return;
  try {
    const res = await getFieldChangeBatch(props.batchNo);
    batchRows.value = res?.data || [];
  } catch (e) {
    ElMessage.error(e?.message || '加载变更明细失败');
  }
});
</script>

<style scoped>
.batch-meta {
  margin-bottom: 12px;
}
</style>
