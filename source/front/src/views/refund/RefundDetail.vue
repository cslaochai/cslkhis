<template>
  <el-dialog v-model="visible" data-testid="refund-detail-dialog" destroy-on-close title="退费申请详情"
             width="640px">
    <div v-loading="detailLoading" class="space-y-4 text-sm">
      <div class="grid grid-cols-2 gap-3 rounded-lg border border-slate-200 p-4">
        <div><span class="text-slate-400">申请号：</span><span
            class="font-mono text-slate-700">{{ detail?.refundApplyNo || '—' }}</span></div>
        <div><span class="text-slate-400">状态：</span><span
            class="text-slate-700">{{ dictLabelText(applyStatusOptions, detail?.applyStatus) }}</span></div>
        <div><span class="text-slate-400">患者：</span><span class="text-slate-700">{{
            detail?.patientName || '—'
          }}</span></div>
        <div><span class="text-slate-400">患者号：</span><span class="text-slate-700">{{
            detail?.patientNo || '—'
          }}</span></div>
        <div><span class="text-slate-400">原账单号：</span><span
            class="font-mono text-slate-700">{{ detail?.billNo || '—' }}</span></div>
        <div><span class="text-slate-400">退费类型：</span><span
            class="text-slate-700">{{ dictLabelText(refundTypeOptions, detail?.refundType) }}</span></div>
        <div><span class="text-slate-400">退费金额：</span><span
            class="font-semibold text-slate-800">¥{{ formatMoney(detail?.refundAmount) }}</span></div>
        <div><span class="text-slate-400">申请人：</span><span class="text-slate-700">{{
            detail?.applyBy || '—'
          }}</span></div>
        <div><span class="text-slate-400">申请时间：</span><span class="text-slate-700">{{
            detail?.applyTime || '—'
          }}</span></div>
        <div><span class="text-slate-400">审核人：</span><span class="text-slate-700">{{
            detail?.auditorName || '—'
          }}</span></div>
        <div><span class="text-slate-400">审核时间：</span><span class="text-slate-700">{{
            detail?.auditTime || '—'
          }}</span></div>
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
      <div v-if="detail?.auditRemark" class="rounded-lg border border-slate-200 p-3">
        <span class="text-slate-400">审核意见：</span><span class="text-slate-700">{{ detail.auditRemark }}</span>
      </div>
      <div v-if="detail?.applyStatus === 5" class="rounded-lg border border-slate-200 p-3">
        <div class="text-slate-400">作废留痕</div>
        <div class="mt-1"><span class="text-slate-400">作废人：</span><span
            class="text-slate-700">{{ detail?.cancelBy || '—' }}</span></div>
        <div><span class="text-slate-400">作废时间：</span><span class="text-slate-700">{{
            detail?.cancelTime || '—'
          }}</span></div>
        <div><span class="text-slate-400">作废原因：</span><span class="text-slate-700">{{
            detail?.cancelReason || '—'
          }}</span></div>
      </div>
      <!-- 执行过才非空：钱到底走哪个渠道退出去的，日结对账就查这几列 -->
      <div v-if="detail?.flowRefundNo" class="rounded-lg border border-slate-200 p-3" data-testid="refund-flow-block">
        <div class="text-slate-400">退费流水</div>
        <div class="mt-1 grid grid-cols-2 gap-3">
          <div><span class="text-slate-400">退费单号：</span><span
              class="font-mono text-slate-700">{{ detail.flowRefundNo }}</span></div>
          <div><span class="text-slate-400">退费方式：</span><span
              class="text-slate-700">{{ dictLabelText(refundMethodOptions, detail.flowRefundMethod) }}</span></div>
          <div><span class="text-slate-400">原支付方式：</span><span
              class="text-slate-700">{{ dictLabelText(payMethodOptions, detail.flowPayMethod) }}</span></div>
          <div><span class="text-slate-400">渠道退费流水号：</span><span
              class="font-mono text-slate-700">{{ detail.flowChannelRefundNo || '—' }}</span></div>
        </div>
        <div v-if="detail.flowInsuranceCancelled === 1" class="mt-1 text-amber-700">
          医保结算清单已随本次退费撤销报盘（2305）
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getRefundApplyDetail} from '@/api/refund';
import {dictLabelText, formatMoney} from '@/lib/utils';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
  // 列表行先垫进详情（防空白闪烁），再按 id 拉完整详情覆盖
  row: {type: Object, default: null},
  applyStatusOptions: {type: Array, default: () => []},
  refundTypeOptions: {type: Array, default: () => []},
  refundMethodOptions: {type: Array, default: () => []},
  payMethodOptions: {type: Array, default: () => []},
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
    const res = await getRefundApplyDetail(props.id);
    if (res.data)
      detail.value = res.data;
  } catch (e) {
    ElMessage.warning(e?.message || '获取退费详情失败，已展示列表数据');
  } finally {
    detailLoading.value = false;
  }
});
</script>
