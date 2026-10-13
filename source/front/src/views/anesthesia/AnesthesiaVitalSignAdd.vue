<template>
  <el-dialog v-model="visible" data-testid="g15-vital-dialog" title="追加生命体征" width="640px">
    <div v-if="vitalTarget" class="space-y-3">
      <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm text-slate-700">
        {{ text(vitalTarget.recordNo) }} · {{ text(vitalTarget.patientName) }}
        <div class="text-[11px] text-slate-500">采样时刻在同一张麻醉单里唯一：同一时刻两组值属于数据错误</div>
      </div>
      <el-form label-width="110px">
        <el-form-item data-testid="g15-vital-time" label="采样时刻" required>
          <el-date-picker v-model="vitalForm.sampleTime" class="!w-full" data-testid="g15-vital-time"
                          type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
        </el-form-item>
        <el-form-item label="血压 mmHg">
          <el-input v-model.number="vitalForm.systolic" class="!w-32" data-testid="g15-vital-sys"
                    placeholder="收缩压"/>
          <span class="mx-2 text-slate-400">/</span>
          <el-input v-model.number="vitalForm.diastolic" class="!w-32" placeholder="舒张压"/>
        </el-form-item>
        <el-form-item label="心率 / 呼吸">
          <el-input v-model.number="vitalForm.heartRate" class="!w-32" placeholder="次/分"/>
          <el-input v-model.number="vitalForm.respiration" class="!ml-2 !w-32" placeholder="次/分"/>
        </el-form-item>
        <el-form-item label="SpO2 / EtCO2">
          <el-input v-model.number="vitalForm.spo2" class="!w-32" placeholder="%"/>
          <el-input v-model.number="vitalForm.etco2" class="!ml-2 !w-32" placeholder="mmHg"/>
        </el-form-item>
        <el-form-item label="体温 ℃">
          <el-input v-model.number="vitalForm.temperature" class="!w-32" placeholder="如 36.5"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="vitalForm.remark" placeholder="如：诱导后、气腹30min"/>
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button v-perm="'ipd:anesthesia:edit'" :loading="vitalSubmitting" data-testid="g15-vital-submit"
                 type="primary" @click="submitVital">记录
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {addAnesthesiaVital} from '@/api/inpatientAnesthesia';
import {text} from '@/lib/anesthesia';

// ---- 生命体征 ----
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const vitalTarget = computed(() => props.row);
const vitalSubmitting = ref(false);
const vitalForm = reactive({
  sampleTime: '',
  systolic: undefined,
  diastolic: undefined,
  heartRate: undefined,
  respiration: undefined,
  temperature: undefined,
  spo2: undefined,
  etco2: undefined,
  remark: '',
});

watch(visible, (open) => {
  if (!open) return;
  vitalForm.sampleTime = '';
  vitalForm.systolic = undefined;
  vitalForm.diastolic = undefined;
  vitalForm.heartRate = undefined;
  vitalForm.respiration = undefined;
  vitalForm.temperature = undefined;
  vitalForm.spo2 = undefined;
  vitalForm.etco2 = undefined;
  vitalForm.remark = '';
});

const submitVital = async () => {
  if (!vitalTarget.value)
    return;
  if (!vitalForm.sampleTime) {
    ElMessage.warning('请选择采样时刻');
    return;
  }
  vitalSubmitting.value = true;
  try {
    await addAnesthesiaVital({
      recordId: vitalTarget.value.id,
      sampleTime: vitalForm.sampleTime,
      systolic: vitalForm.systolic,
      diastolic: vitalForm.diastolic,
      heartRate: vitalForm.heartRate,
      respiration: vitalForm.respiration,
      temperature: vitalForm.temperature,
      spo2: vitalForm.spo2,
      etco2: vitalForm.etco2,
      remark: vitalForm.remark.trim() || undefined,
    });
    ElMessage.success('生命体征已记录');
    visible.value = false;
    emit('saved');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    vitalSubmitting.value = false;
  }
};
</script>
