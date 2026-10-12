<template>
  <el-dialog v-model="visible" destroy-on-close title="发药明细详情" width="620px">
    <template v-if="detailData">
      <div v-loading="loading" class="space-y-4 py-2">
        <div class="grid grid-cols-2 gap-4 rounded-lg border border-slate-200 p-4">
          <div><p class="text-xs text-slate-400">患者</p>
            <p class="text-sm font-medium text-slate-700">{{ detailData.patientName }}（{{ detailData.patientNo }}）</p>
          </div>
          <div><p class="text-xs text-slate-400">处方号</p>
            <p class="font-mono text-sm font-medium text-slate-700">{{ detailData.prescriptionNo }}</p></div>
          <div><p class="text-xs text-slate-400">药品</p>
            <p class="text-sm font-medium text-slate-700">{{ detailData.drugName }}</p></div>
          <div><p class="text-xs text-slate-400">管制分类</p>
            <p class="text-sm font-medium">
              <el-tag
                  v-if="isControlledFlag(detailData.specialFlag)"
                  :type="rowFlagTag(detailData.specialFlag)"
                  effect="dark"
                  size="small"
              >{{ rowFlagText(detailData.specialFlag) }}
              </el-tag>
              <span v-else class="text-slate-500">普通药品</span>
            </p>
          </div>
          <div><p class="text-xs text-slate-400">规格</p>
            <p class="text-sm font-medium text-slate-700">{{ detailData.specification || '—' }}</p></div>
          <div><p class="text-xs text-slate-400">数量</p>
            <p class="text-sm font-medium text-slate-700">{{ detailData.quantity }}{{ detailData.unit || '' }}</p>
          </div>
          <div><p class="text-xs text-slate-400">金额</p>
            <p class="text-sm font-bold text-red-600">¥{{ detailData.amount ?? 0 }}</p></div>
          <div><p class="text-xs text-slate-400">状态</p>
              <span
                  :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', statusColor(detailData.dispensingStatus)]">
                {{ statusLabel(detailData.dispensingStatus) }}
              </span>
          </div>
          <div><p class="text-xs text-slate-400">发药单号</p>
            <p class="font-mono text-sm font-medium text-slate-700">{{ detailData.dispensingNo }}</p></div>
          <div><p class="text-xs text-slate-400">发药药师</p>
            <p class="text-sm font-medium text-slate-700">{{ detailData.pharmacistName || '—' }}</p></div>
          <div><p class="text-xs text-slate-400">发药时间</p>
            <p class="text-sm font-medium text-slate-700">{{ detailData.dispensingTime || '—' }}</p></div>
          <div><p class="text-xs text-slate-400">发药前库存（该药合计）</p>
            <p class="text-sm font-medium text-slate-700">{{ detailData.stockBefore ?? '—' }}</p></div>
          <div><p class="text-xs text-slate-400">发药后库存（该药合计）</p>
            <p class="text-sm font-medium text-slate-700">{{ detailData.stockAfter ?? '—' }}</p></div>
          <div v-if="detailData.remark" class="col-span-2"><p class="text-xs text-slate-400">备注/退药原因</p>
            <p class="text-sm text-slate-700">{{ detailData.remark }}</p></div>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 发药明细详情（含发药前后库存）—— 从 views/pharmacy-window/PharmacyWindowView.vue 拆出
 *
 * 只读：发药记录与库存事实由后端发药动作写入，本页不改一行；
 * 管制分类文案与列表列同源（页面加载字典后按 prop 传下），状态口径走 @/lib/pharmacyWindow。
 */
import {computed, ref, watch} from 'vue';
import {getDispensingDetail} from '@/api/dispensing';
import {isControlledFlag} from '@/lib/drugSpecialFlag';
import {dispensingStatusColor as statusColor, dispensingStatusLabel as statusLabel} from '@/lib/pharmacyWindow';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 发药记录 id */
  id: {type: [String, Number], default: null},
  /** 管制分类文案/配色（列表列同源） */
  rowFlagText: {type: Function, required: true},
  rowFlagTag: {type: Function, required: true},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const loading = ref(false);
const detailData = ref(null);

async function load() {
  loading.value = true;
  try {
    const res = await getDispensingDetail(props.id);
    detailData.value = res.data;
  } catch (error) {
    console.error('加载详情失败:', error);
  } finally {
    loading.value = false;
  }
}

watch(() => props.modelValue, (v) => {
  if (v) {
    detailData.value = null;
    if (props.id) load();
  }
});

defineExpose({reload: load});
</script>
