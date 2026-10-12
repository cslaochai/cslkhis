<template>
  <el-dialog v-model="visible" title="记账行详情" width="760px">
    <div v-loading="loading">
      <template v-if="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="记账流水号">{{ detail.feeNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ feeStatusText(detail.feeStatus) }}</el-descriptions-item>
          <el-descriptions-item label="患者">{{ detail.patientName }}（{{
              detail.patientNo || '—'
            }}）
          </el-descriptions-item>
          <el-descriptions-item label="项目">{{ detail.itemName }}</el-descriptions-item>
          <el-descriptions-item label="单价×数量">¥{{ formatMoney(detail.price) }} × {{
              detail.quantity
            }}
          </el-descriptions-item>
          <el-descriptions-item label="金额">¥{{ formatMoney(detail.amount) }}</el-descriptions-item>
          <el-descriptions-item label="已红冲金额">¥{{ formatMoney(detail.reversedAmount) }}</el-descriptions-item>
          <el-descriptions-item label="剩余金额">¥{{ formatMoney(detail.remainingAmount) }}</el-descriptions-item>
          <el-descriptions-item label="记账时间">{{ detail.bookTime }}</el-descriptions-item>
          <el-descriptions-item label="记账人">{{ detail.bookByName || '—' }}</el-descriptions-item>
        </el-descriptions>
        <template v-if="detail.reverseRows?.length">
          <div class="mt-3 mb-1 text-sm font-medium">红冲链（负数为红冲行）</div>
          <el-table :data="detail.reverseRows" border size="small">
            <el-table-column class-name="font-mono text-xs" label="红冲行号" min-width="150" prop="feeNo"/>
            <el-table-column align="right" label="数量" min-width="70" prop="quantity"/>
            <el-table-column align="right" label="金额" min-width="90" prop="amount">
              <template #default="{ row }">¥{{ formatMoney(row.amount) }}</template>
            </el-table-column>
            <el-table-column class-name="font-mono text-xs" label="红冲时间" min-width="150" prop="bookTime"/>
          </el-table>
        </template>
      </template>
    </div>
  </el-dialog>
</template>

<script setup>
/**
 * L1 记账行详情（本行 + 红冲链）—— 从 views/fee-record/FeeRecordView.vue 拆出
 *
 * 记账行不可修改，本弹框只读：红冲行是负数行、指向被冲原行，状态由后端流转。
 * 打开时按 id 自取详情；父页面另传当前行，只用于「打开瞬间不闪白」。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getFeeRecordDetail} from '@/api/feeRecord';
import {formatMoney} from '@/lib/utils';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 当前记账行 id */
  id: {type: [String, Number], default: null},
  /** 列表当前行（详情未回前的兜底展示） */
  row: {type: Object, default: null},
  /** 记账状态字典翻译（列表列同源，由页面加载后传下） */
  feeStatusText: {type: Function, default: (v) => v},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const loading = ref(false);
const detail = ref(null);

async function load() {
  loading.value = true;
  try {
    const res = await getFeeRecordDetail(props.id);
    detail.value = res.data || null;
  } catch (e) {
    detail.value = null;
    ElMessage.error(e.message || '加载详情失败');
  } finally {
    loading.value = false;
  }
}

watch(() => props.modelValue, (v) => {
  if (v) {
    detail.value = props.row || null;
    if (props.id) load();
  }
});

defineExpose({reload: load});
</script>
