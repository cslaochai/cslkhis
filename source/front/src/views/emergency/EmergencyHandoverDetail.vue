<template>
  <!-- 交班单明细：只读凭证，没有确定键 -->
  <el-dialog v-model="visible" destroy-on-close title="交班单明细" width="1000px">
    <div v-loading="hoDetailLoading" class="space-y-4">
      <template v-if="hoDetail?.handover">
        <div class="grid grid-cols-4 gap-4 rounded-lg border border-slate-200 bg-slate-50 p-3 text-xs">
          <div>
            <p class="text-slate-400">交班单号</p>
            <p class="font-mono font-medium text-slate-800">{{ hoDetail.handover.handoverNo }}</p>
          </div>
          <div>
            <p class="text-slate-400">交班 → 接班</p>
            <p class="font-medium text-slate-800">{{ hoDetail.handover.fromEmpName }} →
              {{ hoDetail.handover.takeEmpName }}</p>
          </div>
          <div>
            <p class="text-slate-400">科室 / 班次</p>
            <p class="font-medium text-slate-800">{{ hoDetail.handover.deptName }}
              {{ hoDetail.handover.shiftName || '—' }}</p>
          </div>
          <div>
            <p class="text-slate-400">本班时段</p>
            <p class="font-medium text-slate-800">{{ hoDetail.handover.periodBegin }} ~ {{
                hoDetail.handover.periodEnd
              }}</p>
          </div>
          <div v-if="hoDetail.handover.remark" class="col-span-4">
            <p class="text-slate-400">整班备注</p>
            <p class="text-slate-700">{{ hoDetail.handover.remark }}</p>
          </div>
        </div>
        <el-table :data="hoDetail.items" max-height="360" style="width: 100%">
          <el-table-column label="患者" width="120">
            <template #default="{ row }">
              <div class="text-sm font-medium text-slate-900">{{ row.patientName }}</div>
              <div class="font-mono text-xs text-slate-400">{{ row.emergencyNo }}</div>
            </template>
          </el-table-column>
          <el-table-column label="分诊/状态" width="110">
            <template #default="{ row }">
              <div class="text-xs text-slate-700">{{ row.triageLevelText }}</div>
              <div class="text-xs text-slate-400">{{ row.emergencyStatusText }}</div>
            </template>
          </el-table-column>
          <el-table-column label="原负责 → 接续" width="150">
            <template #default="{ row }">
              <span class="text-xs text-slate-600">{{ row.fromDoctorName || '无人负责' }}</span>
              <span class="mx-1 text-xs text-slate-400">→</span>
              <span class="text-xs font-medium text-slate-800">{{ row.takeDoctorName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="计时" width="90">
            <template #default="{ row }">
              <span class="text-xs text-slate-600">{{ row.waitMinutes != null ? row.waitMinutes + '分' : '' }}</span>
              <span class="text-xs text-slate-600">{{ row.obsHours != null ? row.obsHours + '小时' : '' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="去向交代" min-width="180">
            <template #default="{ row }">
              <span class="text-xs text-slate-700">{{ row.disposition }}</span>
            </template>
          </el-table-column>
          <el-table-column label="补充交代" min-width="150">
            <template #default="{ row }">
              <span class="text-xs text-slate-500">{{ row.handoverNote || '—' }}</span>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </div>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getEmergencyHandoverDetail} from '@/api/emergency';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const hoDetailLoading = ref(false);
const hoDetail = ref(null);

const loadHoDetail = async () => {
  hoDetailLoading.value = true;
  try {
    const res = await getEmergencyHandoverDetail(String(props.id));
    hoDetail.value = res.data;
  } catch (error) {
    ElMessage.error(error.message || '交班单加载失败');
  } finally {
    hoDetailLoading.value = false;
  }
};

watch(visible, (open) => {
  if (!open)
    return;
  hoDetail.value = null;
  loadHoDetail();
});
</script>
