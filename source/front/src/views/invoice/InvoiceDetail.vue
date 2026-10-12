<template>
  <el-dialog v-model="visible" destroy-on-close title="发票详情" width="640px">
    <div v-loading="loading" class="space-y-4 text-sm">
      <div class="grid grid-cols-2 gap-3 rounded-lg border border-slate-200 p-4">
        <div><span class="text-slate-400">发票号：</span><span
            class="font-mono text-slate-700">{{ detail?.invoiceNo || '—' }}</span></div>
        <div><span class="text-slate-400">发票类型：</span><span
            class="text-slate-700">{{ invoiceTypeText(detail?.invoiceType) }}</span></div>
        <div><span class="text-slate-400">患者：</span><span class="text-slate-700">{{
            detail?.patientName || '—'
          }}</span></div>
        <div><span class="text-slate-400">患者号：</span><span class="text-slate-700">{{
            detail?.patientNo || '—'
          }}</span></div>
        <div><span class="text-slate-400">收费单号：</span><span
            class="font-mono text-slate-700">{{ detail?.chargeNo || '—' }}</span></div>
        <div><span class="text-slate-400">金额：</span><span
            class="font-semibold text-slate-800">¥{{ formatMoney(detail?.totalAmount) }}</span></div>
        <div><span class="text-slate-400">状态：</span><span
            class="text-slate-700">{{ invoiceStatusText(detail?.invoiceStatus) }}</span></div>
        <div><span class="text-slate-400">开票时间：</span><span class="text-slate-700">{{
            detail?.invoiceTime || '—'
          }}</span></div>
        <div><span class="text-slate-400">打印时间：</span><span class="text-slate-700">{{
            detail?.printTime || '—'
          }}</span></div>
        <div><span class="text-slate-400">作废时间：</span><span class="text-slate-700">{{
            detail?.voidTime || '—'
          }}</span></div>
      </div>
      <div v-if="detail?.voidReason" class="rounded-lg border border-rose-200 bg-rose-50 p-3 text-rose-700">
        作废原因：{{ detail.voidReason }}
      </div>
      <div v-if="detail?.electronicUrl" class="rounded-lg border border-slate-200 p-3">
        电子发票地址：<a :href="detail.electronicUrl" class="text-[#1269B5] hover:underline"
                        target="_blank">{{ detail.electronicUrl }}</a>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
/**
 * L4 票据：发票详情 —— 从 views/invoice/InvoiceView.vue 拆出
 *
 * 打开瞬间先显示列表行，详情接口回来再替换；详情失败保留列表行并明确告知（原口径照搬）。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getInvoiceDetail} from '@/api/invoice';
import {invoiceStatusText, invoiceTypeText} from '@/lib/invoiceStatus';
import {formatMoney} from '@/lib/utils';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 发票 id */
  id: {type: [String, Number], default: null},
  /** 列表当前行（详情未回前的兜底展示） */
  row: {type: Object, default: null},
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
  detail.value = props.row;
  try {
    const res = await getInvoiceDetail(props.id);
    if (res.data)
      detail.value = res.data;
  } catch (e) {
    // 详情失败保留列表行，但要明确告知，不能静默
    ElMessage.warning(e?.message || '获取发票详情失败，已展示列表数据');
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
