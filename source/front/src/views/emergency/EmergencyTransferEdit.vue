<template>
  <el-dialog v-model="visible" destroy-on-close title="急诊转归 / 状态变更" width="460px" data-testid="emg-transfer-dialog">
    <div class="mb-3 rounded bg-slate-50 p-2 text-xs text-slate-600">
      <span class="font-bold text-slate-900">{{ row?.patientName }}</span>
      <span class="ml-2 font-mono text-slate-400">{{ row?.emergencyNo }}</span>
      <span class="ml-2">当前：{{ currentLabel }}</span>
    </div>
    <el-form label-width="84px" label-position="right">
      <el-form-item label="目标状态" required>
        <el-radio-group v-model="targetStatus">
          <el-radio :value="2" data-testid="emg-transfer-2">诊治中</el-radio>
          <el-radio :value="3" data-testid="emg-transfer-3">留观</el-radio>
          <el-radio :value="5" data-testid="emg-transfer-5">离院</el-radio>
          <el-radio :value="6" data-testid="emg-transfer-6">死亡</el-radio>
        </el-radio-group>
      </el-form-item>
      <template v-if="targetStatus === 3">
        <el-form-item label="留观病区" required>
          <el-select v-model="obsForm.wardId" class="!w-full" data-testid="emg-transfer-ward"
                     placeholder="选择急诊病区" @change="onWardChange">
            <el-option v-for="w in emergencyWards" :key="w.wardId" :label="w.wardName" :value="String(w.wardId)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="留观床位" required>
          <el-select v-model="obsForm.bedId" :disabled="!obsForm.wardId" class="!w-full"
                     data-testid="emg-transfer-bed" placeholder="先选病区">
            <el-option v-for="b in obsBeds" :key="b.bedId" :label="`${b.bedNo}（${b.bedType}）`" :value="String(b.bedId)"/>
          </el-select>
          <p v-if="obsForm.wardId && !obsBeds.length" class="mt-1 text-xs text-rose-500">该病区暂无空闲床位</p>
        </el-form-item>
      </template>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" data-testid="emg-transfer-ok" @click="doTransfer">确认转归</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getEmergencyBedSelectList, updateEmergencyStatus} from '@/api/emergency';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  wards: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});
const statusMap = {1: '候诊', 2: '诊治中', 3: '留观', 4: '转住院', 5: '离院', 6: '死亡'};
const currentLabel = computed(() => statusMap[props.row?.emergencyStatus] || '—');
const emergencyWards = computed(() => (props.wards || []).filter((w) => (w.wardName || '').startsWith('急诊')));

const targetStatus = ref(null);
const obsForm = ref({wardId: null, bedId: null});
const obsBeds = ref([]);

watch(visible, (v) => {
  if (v) {
    targetStatus.value = null;
    obsForm.value = {wardId: null, bedId: null};
    obsBeds.value = [];
  }
});

const onWardChange = async (wardId) => {
  obsForm.value.bedId = null;
  obsBeds.value = [];
  if (!wardId) return;
  try {
    const res = await getEmergencyBedSelectList({wardId, bedStatus: 1});
    obsBeds.value = res.data || [];
  } catch {
    obsBeds.value = [];
  }
};

const doTransfer = async () => {
  if (!targetStatus.value) {
    ElMessage.warning('请选择目标状态');
    return;
  }
  if (targetStatus.value === 3 && (!obsForm.value.wardId || !obsForm.value.bedId)) {
    ElMessage.warning('转入留观必须先选床位');
    return;
  }
  try {
    const extra = targetStatus.value === 3
      ? {observationWardId: obsForm.value.wardId, observationBedId: obsForm.value.bedId}
      : {};
    await updateEmergencyStatus(props.row.id, targetStatus.value, extra);
    ElMessage.success('状态更新成功');
    emit('saved');
    visible.value = false;
  } catch {
    // 错误由全局拦截器统一提示
  }
};
</script>
