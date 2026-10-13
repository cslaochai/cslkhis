<template>
  <el-dialog v-model="visible" data-testid="g15-med-dialog" title="追加麻醉用药" width="600px">
    <div v-if="medTarget" class="space-y-3">
      <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm text-slate-700">
        {{ text(medTarget.recordNo) }} · {{ text(medTarget.patientName) }}
      </div>
      <el-form label-width="110px">
        <el-form-item data-testid="g15-med-time" label="给药时刻" required>
          <el-date-picker v-model="medForm.medTime" class="!w-full" data-testid="g15-med-time" type="datetime"
                          value-format="YYYY-MM-DD HH:mm:ss"/>
        </el-form-item>
        <el-form-item label="阶段">
          <el-select v-model="medForm.medPhase" class="!w-48">
            <el-option v-for="o in A.medPhaseOptions" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="药品名称" required>
          <el-input v-model="medForm.drugName" data-testid="g15-med-name" placeholder="如：丙泊酚 / 瑞芬太尼"/>
        </el-form-item>
        <el-form-item label="剂量">
          <el-input v-model.number="medForm.dose" class="!w-32" data-testid="g15-med-dose"/>
          <el-input v-model="medForm.unit" class="!ml-2 !w-24"/>
        </el-form-item>
        <el-form-item label="给药途径">
          <el-select v-model="medForm.route" class="!w-full">
            <el-option v-for="o in A.medRouteOptions" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button v-perm="'ipd:anesthesia:edit'" :loading="medSubmitting" data-testid="g15-med-submit" type="primary"
                 @click="submitMed">记录
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {addAnesthesiaMed} from '@/api/inpatientAnesthesia';
import * as A from '@/lib/anesthesia';
import {text} from '@/lib/anesthesia';

// ---- 用药 ----
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const medTarget = computed(() => props.row);
const medSubmitting = ref(false);
const medForm = reactive({
  medTime: '',
  medPhase: 2,
  drugName: '',
  dose: undefined,
  unit: 'mg',
  route: 1,
});

watch(visible, (open) => {
  if (!open) return;
  medForm.medTime = '';
  medForm.medPhase = 2;
  medForm.drugName = '';
  medForm.dose = undefined;
  medForm.unit = 'mg';
  medForm.route = 1;
});

const submitMed = async () => {
  if (!medTarget.value)
    return;
  if (!medForm.medTime) {
    ElMessage.warning('请选择给药时刻');
    return;
  }
  if (!medForm.drugName.trim()) {
    ElMessage.warning('药品名称不能为空（不知道给的什么药是不能接受的）');
    return;
  }
  medSubmitting.value = true;
  try {
    await addAnesthesiaMed({
      recordId: medTarget.value.id,
      medTime: medForm.medTime,
      medPhase: medForm.medPhase,
      drugName: medForm.drugName.trim(),
      dose: medForm.dose,
      unit: medForm.unit,
      route: medForm.route,
    });
    ElMessage.success('麻醉用药已记录');
    visible.value = false;
    emit('saved');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    medSubmitting.value = false;
  }
};
</script>
