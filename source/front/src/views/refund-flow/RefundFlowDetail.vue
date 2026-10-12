<template>
  <el-dialog v-model="visible" data-testid="refund-flow-detail-dialog" destroy-on-close title="退费流水明细"
             width="760px">
    <div v-loading="detailLoading" class="space-y-4 text-sm">
      <div class="grid grid-cols-2 gap-3 rounded-lg border border-slate-200 p-4">
        <div><span class="text-slate-400">退费单号：</span><span
            class="font-mono text-slate-700">{{ detail?.refundNo || '—' }}</span></div>
        <div><span class="text-slate-400">原收费单号：</span><span
            class="font-mono text-slate-700">{{ detail?.chargeNo || '—' }}</span></div>
        <div><span class="text-slate-400">患者：</span><span class="text-slate-700">{{
            detail?.patientName || '—'
          }}</span></div>
        <div><span class="text-slate-400">患者号：</span><span class="text-slate-700">{{
            detail?.patientNo || '—'
          }}</span></div>
        <div><span class="text-slate-400">实退金额：</span><span
            class="font-semibold text-slate-800">¥{{ formatMoney(detail?.totalAmount) }}</span></div>
        <div><span class="text-slate-400">退费类型：</span><span
            class="text-slate-700">{{ dictLabelText(refundTypeOptions, detail?.refundType) }}</span></div>
        <div><span class="text-slate-400">流水来源：</span><span
            class="text-slate-700">{{ dictLabelText(flowSourceOptions, detail?.flowSource) }}</span></div>
        <div><span class="text-slate-400">来源申请号：</span><span
            class="font-mono text-slate-700">{{ detail?.applyNo || '—' }}</span></div>
        <div><span class="text-slate-400">退费方式：</span><span
            class="text-slate-700">{{ dictLabelText(refundMethodOptions, detail?.refundMethod) }}</span></div>
        <div><span class="text-slate-400">原支付方式：</span><span
            class="text-slate-700">{{ dictLabelText(payMethodOptions, detail?.payMethod) }}</span></div>
        <div><span class="text-slate-400">渠道退费流水号：</span><span
            class="font-mono text-slate-700">{{ detail?.channelRefundNo || '—' }}</span></div>
        <div><span class="text-slate-400">医保报盘：</span><span
            class="text-slate-700">{{ detail?.insuranceCancelled === 1 ? '已随本次退费撤销（2305）' : '不涉及' }}</span>
        </div>
        <div><span class="text-slate-400">退费人：</span><span class="text-slate-700">{{
            detail?.refundBy || '—'
          }}</span></div>
        <div><span class="text-slate-400">退费时间：</span><span class="text-slate-700">{{
            detail?.refundTime || '—'
          }}</span></div>
      </div>
      <div class="rounded-lg border border-slate-200 p-3">
        <span class="text-slate-400">退费原因：</span><span class="text-slate-700">{{
          detail?.refundReason || '—'
        }}</span>
      </div>
      <div class="rounded-lg border border-slate-200 p-3">
        <div class="mb-2 text-slate-400">冲正的收费明细</div>
        <el-table :data="detail?.details || []" border data-testid="refund-flow-detail-table" size="small">
          <el-table-column label="项目名称" min-width="180" prop="itemName" show-overflow-tooltip/>
          <el-table-column label="规格" min-width="110" prop="specification">
            <template #default="{ row }">{{ row.specification || '—' }}</template>
          </el-table-column>
          <el-table-column align="right" label="退费数量" prop="refundQuantity" width="100"/>
          <el-table-column label="单位" prop="unit" width="70">
            <template #default="{ row }">{{ row.unit || '—' }}</template>
          </el-table-column>
          <el-table-column align="right" label="单价" width="90">
            <template #default="{ row }">¥{{ formatMoney(row.price) }}</template>
          </el-table-column>
          <el-table-column align="right" label="退费金额" width="100">
            <template #default="{ row }">¥{{ formatMoney(row.refundAmount) }}</template>
          </el-table-column>
          <el-table-column class-name="font-mono text-xs" label="来源单号" min-width="150" prop="sourceNo">
            <template #default="{ row }">{{ row.sourceNo || '—' }}</template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getRefundFlowDetail} from '@/api/refundFlow';
import {dictLabelText, formatMoney} from '@/lib/utils';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
  // 列表行先垫进明细（防空白闪烁），再按 id 拉完整明细覆盖
  row: {type: Object, default: null},
  flowSourceOptions: {type: Array, default: () => []},
  refundMethodOptions: {type: Array, default: () => []},
  payMethodOptions: {type: Array, default: () => []},
  refundTypeOptions: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const detailLoading = ref(false);
const detail = ref(null);

watch(visible, async (open) => {
  if (!open) return;
  detailLoading.value = true;
  detail.value = props.row;
  try {
    const res = await getRefundFlowDetail(props.id);
    if (res.data)
      detail.value = res.data;
  } catch (e) {
    ElMessage.warning(e?.message || '获取退费流水明细失败，已展示列表数据');
  } finally {
    detailLoading.value = false;
  }
});
</script>
