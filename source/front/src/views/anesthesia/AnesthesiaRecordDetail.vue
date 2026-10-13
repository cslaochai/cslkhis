<template>
  <el-drawer v-model="visible" data-testid="g15-detail-drawer" size="60%" title="麻醉记录详情">
    <div v-if="detail" class="space-y-4">
      <div class="flex items-center gap-3">
        <el-icon class="text-slate-400">
          <Aim/>
        </el-icon>
        <span class="text-base font-semibold text-slate-900">{{ text(detail.recordNo) }}</span>
        <el-tag :type="statusTagType(detail.recordStatus)" size="small">{{ text(detail.recordStatusText) }}</el-tag>
        <el-tag :type="chargeTagType(detail.chargeStatus)" effect="plain" size="small">
          {{ text(detail.chargeStatusText) }}
        </el-tag>
      </div>

      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="患者">{{ text(detail.patientName) }}</el-descriptions-item>
        <el-descriptions-item label="入院号">{{ text(detail.admissionNo) }}</el-descriptions-item>
        <el-descriptions-item label="术式">{{ text(detail.plannedOperationName) }}</el-descriptions-item>
        <el-descriptions-item label="实际术式">{{ text(detail.actualOperationName) }}</el-descriptions-item>
        <el-descriptions-item label="麻醉方式">{{ text(detail.anesthesiaTypeText) }}</el-descriptions-item>
        <el-descriptions-item label="ASA">{{ text(detail.asaText) }}</el-descriptions-item>
        <el-descriptions-item label="麻醉医师">{{ text(detail.anesthetistName) }}</el-descriptions-item>
        <el-descriptions-item label="手术间">{{ text(detail.operationRoom) }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="麻醉时段">{{ fmt(detail.anesthesiaStartTime) }} ~
          {{ fmt(detail.anesthesiaEndTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="麻醉时长">{{ text(detail.anesthesiaDurationText) }}（计费
          {{ text(detail.billHours) }} 小时）
        </el-descriptions-item>
        <el-descriptions-item label="计费">{{ text(detail.chargeStatusText) }}<span
            v-if="detail.chargedAmount"> · {{ detail.chargedAmount }} 元</span></el-descriptions-item>
        <el-descriptions-item label="术前访视">{{
            detail.visitId ? '已关联' : '待补（急诊超前麻醉）'
          }}
        </el-descriptions-item>
        <el-descriptions-item label="手术时长">{{ text(detail.operationDurationText) }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="detail.warningText"
           class="rounded border border-amber-200 bg-amber-50 px-3 py-2 text-[12px] text-amber-700">
        {{ detail.warningText }}
      </div>

      <div>
        <div class="mb-2 text-sm font-medium text-slate-800">生命体征（{{ detail.vitalCount ?? 0 }} 条）</div>
        <el-table :data="detail.vitals || []" size="small" style="width: 100%">
          <el-table-column label="时刻" min-width="140">
            <template #default="{ row }">{{ fmt(row.sampleTime) }}</template>
          </el-table-column>
          <el-table-column label="血压" width="110">
            <template #default="{ row }">{{ text(row.systolic) }}/{{ text(row.diastolic) }}</template>
          </el-table-column>
          <el-table-column label="心率" prop="heartRate" width="80"/>
          <el-table-column label="呼吸" prop="respiration" width="80"/>
          <el-table-column label="SpO2" prop="spo2" width="80"/>
          <el-table-column label="EtCO2" prop="etco2" width="80"/>
          <el-table-column label="体温" prop="temperature" width="80"/>
          <el-table-column label="提示" min-width="120">
            <template #default="{ row }">
              <span v-if="row.abnormalText" class="text-[11px] text-red-600">{{ row.abnormalText }}</span>
              <span v-else class="text-[11px] text-slate-400">—</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div>
        <div class="mb-2 text-sm font-medium text-slate-800">麻醉用药（{{ detail.medCount ?? 0 }} 条）</div>
        <el-table :data="detail.meds || []" size="small" style="width: 100%">
          <el-table-column label="时刻" min-width="140">
            <template #default="{ row }">{{ fmt(row.medTime) }}</template>
          </el-table-column>
          <el-table-column label="阶段" width="80">
            <template #default="{ row }">{{ text(row.medPhaseText) }}</template>
          </el-table-column>
          <el-table-column label="药品" min-width="160" prop="drugName"/>
          <el-table-column label="剂量" width="120">
            <template #default="{ row }">{{ text(row.doseText) }}</template>
          </el-table-column>
          <el-table-column label="途径" width="110">
            <template #default="{ row }">{{ text(row.routeText) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Aim} from '@element-plus/icons-vue';
import {getAnesthesiaRecordDetail} from '@/api/inpatientAnesthesia';
import {chargeTagType, fmt, statusTagType, text} from '@/lib/anesthesia';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 父页面传当前行：详情没拉到前先用它兜底，打开瞬间不闪白
  row: {type: Object, default: null},
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const detail = ref(null);

watch(visible, async (open) => {
  if (!open) return;
  detail.value = props.row || null;
  try {
    const res = await getAnesthesiaRecordDetail(props.row.id);
    detail.value = (res.data || props.row);
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
});
</script>
