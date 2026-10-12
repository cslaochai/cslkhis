<template>
  <el-dialog v-model="visible" destroy-on-close title="交班单详情" width="680px">
    <div v-loading="cashierDetailLoading" class="space-y-4 text-sm">
      <div class="grid grid-cols-2 gap-3 rounded-lg border border-slate-200 p-4">
        <div><span class="text-slate-400">交班单号：</span>
          <span class="font-mono text-slate-700">{{ cashierDetail?.settlementNo || '—' }}</span>
        </div>
        <div><span class="text-slate-400">收费员：</span>
          <span class="text-slate-700">{{ cashierDetail?.cashierName || '—' }}</span>
        </div>
        <div><span class="text-slate-400">班次：</span>
          <span class="text-slate-700">{{ shiftTypeText(cashierDetail?.shiftType) }}</span>
        </div>
        <div><span class="text-slate-400">状态：</span>
          <span class="text-slate-700">{{ cashierStatusText(cashierDetail?.settleStatus) }}</span>
        </div>
        <div class="col-span-2"><span class="text-slate-400">统计区间：</span>
          <span class="font-mono text-slate-700">
            {{ cashierDetail?.periodBegin || '—' }} ~ {{ cashierDetail?.periodEnd || '—' }}
          </span>
        </div>
        <div><span class="text-slate-400">收费：</span>
          <span class="text-slate-700">
            ¥{{ formatMoney(cashierDetail?.chargeAmount) }}（{{ cashierDetail?.chargeCount ?? 0 }} 笔）
          </span>
        </div>
        <div><span class="text-slate-400">退费：</span>
          <span class="text-slate-700">
            ¥{{ formatMoney(cashierDetail?.refundAmount) }}（{{ cashierDetail?.refundCount ?? 0 }} 笔）
          </span>
        </div>
        <div><span class="text-slate-400">净额：</span>
          <span class="font-semibold text-slate-800">¥{{ formatMoney(cashierDetail?.netAmount) }}</span>
        </div>
        <div><span class="text-slate-400">票据(开/废)：</span>
          <span class="text-slate-700">
            {{ cashierDetail?.invoiceCount ?? 0 }} / {{ cashierDetail?.invoiceVoidCount ?? 0 }}
          </span>
        </div>
      </div>

      <div class="grid grid-cols-3 gap-3 rounded-lg border border-slate-200 p-4">
        <div><span class="text-slate-400">现金：</span>¥{{ formatMoney(cashierDetail?.cashAmount) }}</div>
        <div><span class="text-slate-400">微信：</span>¥{{ formatMoney(cashierDetail?.wechatAmount) }}</div>
        <div><span class="text-slate-400">支付宝：</span>¥{{ formatMoney(cashierDetail?.alipayAmount) }}</div>
        <div><span class="text-slate-400">医保：</span>¥{{ formatMoney(cashierDetail?.insuranceAmount) }}</div>
        <div><span class="text-slate-400">余额：</span>¥{{ formatMoney(cashierDetail?.balanceAmount) }}</div>
        <div>
          <span class="text-slate-400">支付方式未知：</span>
          <span :class="Number(cashierDetail?.unknownPayAmount || 0) === 0 ? 'text-slate-700' : 'text-rose-600'">
            ¥{{ formatMoney(cashierDetail?.unknownPayAmount) }}
          </span>
        </div>
      </div>

      <div class="rounded-lg border border-slate-200 p-4">
        <div class="flex items-center gap-6">
          <div><span class="text-slate-400">实交现金：</span>
            <span class="font-semibold text-slate-800">¥{{ formatMoney(cashierDetail?.handinCash) }}</span>
          </div>
          <div>
            <span class="text-slate-400">现金差异：</span>
            <span
                :class="Number(cashierDetail?.cashDiff || 0) === 0 ? 'text-emerald-600' : 'text-rose-600'"
                class="font-semibold"
            >
              {{ Number(cashierDetail?.cashDiff || 0) > 0 ? '+' : '' }}{{ formatMoney(cashierDetail?.cashDiff) }}
            </span>
          </div>
        </div>
        <div v-if="cashierDetail?.diffReason" class="mt-2 text-xs text-slate-600">
          差异说明：{{ cashierDetail.diffReason }}
        </div>
      </div>

      <div v-if="cashierDetail?.remark" class="rounded-lg border border-slate-200 p-3">
        <span class="text-slate-400">备注：</span><span class="text-slate-700">{{ cashierDetail.remark }}</span>
      </div>
      <div class="text-xs text-slate-400">
        制单：{{ cashierDetail?.createBy || '—' }} / {{ cashierDetail?.createTime || '—' }}
        <span v-if="cashierDetail?.auditBy" class="ml-3">审核：{{ cashierDetail.auditBy }} / {{
            cashierDetail.auditTime
          }}</span>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getCashierById} from '@/api/financeSettlement';
import {cashierStatusText, shiftTypeText} from '@/lib/financeSettlement';
import {formatMoney} from '@/lib/utils';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const cashierDetailLoading = ref(false);
const cashierDetail = ref(null);

watch(visible, async (open) => {
  if (!open) return;
  cashierDetailLoading.value = true;
  cashierDetail.value = props.row;
  try {
    const res = await getCashierById(props.id);
    if (res.data)
      cashierDetail.value = res.data;
  } catch (e) {
    ElMessage.warning(e?.message || '获取交班单详情失败，已展示列表数据');
  } finally {
    cashierDetailLoading.value = false;
  }
});
</script>
