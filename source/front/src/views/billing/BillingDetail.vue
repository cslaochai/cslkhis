<template>
  <el-dialog v-model="visible" :title="`费用明细 - ${record?.patientName}`" destroy-on-close width="600px">
    <template v-if="record">
      <div class="space-y-4 py-2">
        <div class="grid grid-cols-2 gap-3 rounded-lg border border-slate-200 p-4 text-sm">
          <div><span class="text-slate-400">账单号：</span><span
              class="font-mono text-slate-700">{{ record.billNo }}</span></div>
          <div><span class="text-slate-400">患者：</span><span class="text-slate-700">{{ record.patientName }}</span>
          </div>
          <div><span class="text-slate-400">总费用：</span><span
              class="text-slate-700">¥{{ record.totalAmount?.toFixed(2) }}</span></div>
          <div><span class="text-slate-400">实收：</span><span
              class="text-slate-700">¥{{ record.paidAmount?.toFixed(2) }}</span></div>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * L2 结算账单费用明细 —— 从 views/billing/BillingView.vue 拆出
 *
 * 明细取数照旧走 getBillDetailById，接口没给数据时退回列表行（原逻辑如此，不补字段）。
 */
import {computed, ref, watch} from 'vue';
import {getBillDetailById} from '@/api/settlementBill';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 账单 id */
  id: {type: [String, Number], default: null},
  /** 列表当前行（详情未回前的兜底展示） */
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const record = ref(null);

async function load() {
  try {
    const res = await getBillDetailById(props.id);
    record.value = res.data || props.row;
  } catch (error) {
    record.value = props.row;
  }
}

watch(() => props.modelValue, (v) => {
  if (v) {
    record.value = props.row || null;
    if (props.id) load();
  }
});

defineExpose({reload: load});
</script>
