<template>
  <el-dialog v-model="visible" data-testid="g15-visit-dialog" title="麻醉术前访视" width="780px">
    <el-form label-width="120px">
      <el-form-item label="手术申请单" required>
        <el-select v-model="visitForm.applyId" class="!w-full" data-testid="g15-visit-apply" filterable
                   placeholder="选择手术申请单">
          <el-option v-for="a in applies" :key="a.id" :label="applyLabel(a)" :value="String(a.id)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="ASA 分级">
        <el-select v-model="visitForm.asaGrade" class="!w-40" clearable data-testid="g15-visit-asa">
          <el-option v-for="o in A.asaOptions" :key="o.value" :label="o.label" :value="o.value"/>
        </el-select>
        <el-checkbox v-model="visitForm.asaEmergency" :false-value="0" :true-value="1" class="!ml-4">急诊（E）
        </el-checkbox>
      </el-form-item>
      <el-form-item label="气道评估">
        <el-select v-model="visitForm.mallampati" class="!w-56" clearable placeholder="Mallampati">
          <el-option v-for="o in A.mallampatiOptions" :key="o.value" :label="o.label" :value="o.value"/>
        </el-select>
        <el-select v-model="visitForm.neckMobility" class="!ml-2 !w-36" clearable placeholder="颈部活动度">
          <el-option v-for="o in A.neckMobilityOptions" :key="o.value" :label="o.label" :value="o.value"/>
        </el-select>
        <el-input v-model.number="visitForm.mouthOpenCm" class="!ml-2 !w-32" placeholder="张口度 cm"/>
      </el-form-item>
      <el-form-item label="困难气道">
        <el-radio-group v-model="visitForm.difficultAirway" data-testid="g15-visit-airway">
          <el-radio :value="0">否</el-radio>
          <el-radio :value="1">是</el-radio>
        </el-radio-group>
        <span class="ml-3 text-[12px] text-amber-600">标「是」必须填写备选方案</span>
      </el-form-item>
      <el-form-item label="既往麻醉史">
        <el-input v-model="visitForm.pastAnesthesiaHistory" :rows="2" placeholder="既往麻醉方式与不良反应"
                  type="textarea"/>
      </el-form-item>
      <el-form-item label="过敏史">
        <el-input v-model="visitForm.allergyHistory" placeholder="药物/食物/消毒剂过敏"/>
      </el-form-item>
      <el-form-item label="长期用药史">
        <el-input v-model="visitForm.medicationHistory" placeholder="抗凝药/降压药/激素等"/>
      </el-form-item>
      <el-form-item label="禁食禁饮">
        <el-select v-model="visitForm.npoStatus" class="!w-56">
          <el-option v-for="o in A.npoOptions" :key="o.value" :label="o.label" :value="o.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="身高 / 体重">
        <el-input v-model.number="visitForm.heightCm" class="!w-32" placeholder="cm"/>
        <el-input v-model.number="visitForm.weightKg" class="!ml-2 !w-32" placeholder="kg"/>
      </el-form-item>
      <el-form-item label="麻醉计划">
        <el-input v-model="visitForm.anesthesiaPlan" :rows="2" placeholder="方法 + 主要用药 + 体位" type="textarea"/>
      </el-form-item>
      <el-form-item label="风险评估">
        <el-input v-model="visitForm.riskAssessment" :rows="2" placeholder="ASA ≥ Ⅳ 级必填" type="textarea"/>
      </el-form-item>
      <el-form-item label="备选方案">
        <el-input v-model="visitForm.backupPlan" :rows="2" placeholder="困难气道时的备用方案（标困难气道必填）"
                  type="textarea"/>
      </el-form-item>
      <el-form-item label="访视结论">
        <el-select v-model="visitForm.conclusion" class="!w-56" clearable>
          <el-option v-for="o in A.visitConclusionOptions" :key="o.value" :label="o.label" :value="o.value"/>
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'ipd:anesthesia:add'" :loading="visitSubmitting" data-testid="g15-visit-submit"
                 type="primary" @click="submitVisit">保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {saveAnesthesiaVisit} from '@/api/inpatientAnesthesia';
import * as A from '@/lib/anesthesia';
import {applyLabel} from '@/lib/anesthesia';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  applies: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const visitSubmitting = ref(false);
const visitTargetId = ref('');
const visitForm = reactive({
  applyId: '',
  asaGrade: undefined,
  asaEmergency: 0,
  mallampati: undefined,
  neckMobility: undefined,
  mouthOpenCm: undefined,
  difficultAirway: 0,
  pastAnesthesiaHistory: '',
  allergyHistory: '',
  medicationHistory: '',
  npoStatus: 1,
  heightCm: undefined,
  weightKg: undefined,
  anesthesiaPlan: '',
  riskAssessment: '',
  backupPlan: '',
  conclusion: undefined,
  conclusionNote: '',
});

watch(visible, (open) => {
  if (!open) return;
  visitTargetId.value = '';
  visitForm.applyId = '';
  visitForm.asaGrade = undefined;
  visitForm.asaEmergency = 0;
  visitForm.mallampati = undefined;
  visitForm.neckMobility = undefined;
  visitForm.mouthOpenCm = undefined;
  visitForm.difficultAirway = 0;
  visitForm.pastAnesthesiaHistory = '';
  visitForm.allergyHistory = '';
  visitForm.medicationHistory = '';
  visitForm.npoStatus = 1;
  visitForm.heightCm = undefined;
  visitForm.weightKg = undefined;
  visitForm.anesthesiaPlan = '';
  visitForm.riskAssessment = '';
  visitForm.backupPlan = '';
  visitForm.conclusion = undefined;
  visitForm.conclusionNote = '';
});

const submitVisit = async () => {
  if (!visitForm.applyId) {
    ElMessage.warning('请选择手术申请单');
    return;
  }
  visitSubmitting.value = true;
  try {
    await saveAnesthesiaVisit({
      id: visitTargetId.value || undefined,
      applyId: visitForm.applyId,
      asaGrade: visitForm.asaGrade,
      asaEmergency: visitForm.asaEmergency,
      mallampati: visitForm.mallampati,
      neckMobility: visitForm.neckMobility,
      mouthOpenCm: visitForm.mouthOpenCm,
      difficultAirway: visitForm.difficultAirway,
      pastAnesthesiaHistory: visitForm.pastAnesthesiaHistory.trim() || undefined,
      allergyHistory: visitForm.allergyHistory.trim() || undefined,
      medicationHistory: visitForm.medicationHistory.trim() || undefined,
      npoStatus: visitForm.npoStatus,
      heightCm: visitForm.heightCm,
      weightKg: visitForm.weightKg,
      anesthesiaPlan: visitForm.anesthesiaPlan.trim() || undefined,
      riskAssessment: visitForm.riskAssessment.trim() || undefined,
      backupPlan: visitForm.backupPlan.trim() || undefined,
      conclusion: visitForm.conclusion,
      conclusionNote: visitForm.conclusionNote.trim() || undefined,
    });
    ElMessage.success('术前访视已保存（尚未给出结论，不能作为麻醉依据）');
    visible.value = false;
    emit('saved');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    visitSubmitting.value = false;
  }
};
</script>
