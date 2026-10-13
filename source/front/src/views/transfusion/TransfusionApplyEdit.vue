<template>
  <el-dialog
      v-model="visible"
      :title="applyForm.id ? '修改用血申请' : '发起用血申请'"
      data-testid="p4-tf-apply-dialog"
      width="760px"
  >
    <el-form label-width="120px">
      <el-form-item label="在院患者" required>
        <el-select
            v-model="applyForm.admissionId"
            :disabled="!!applyForm.id"
            class="!w-full"
            data-testid="p4-tf-apply-admission"
            filterable
            placeholder="选择在院患者"
        >
          <el-option
              v-for="a in admissions"
              :key="a.admissionId"
              :label="admissionLabel(a)"
              :value="String(a.admissionId)"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="currentAdmission" label="申请科室">
        <span class="text-slate-700">
          {{ text(currentAdmission.deptName) }} / {{ text(currentAdmission.wardName) }} /
          {{ text(currentAdmission.bedNo) }}床
        </span>
        <span class="ml-2 text-[11px] text-slate-400">申请科室取患者当前科室，服务端写入，不需要填</span>
      </el-form-item>
      <el-form-item label="受血者血型" required>
        <div class="flex items-center gap-3">
          <el-select
              v-model="applyForm.patientAbo"
              class="!w-32"
              data-testid="p4-tf-apply-abo"
              placeholder="ABO"
          >
            <el-option v-for="t in aboTypes" :key="t" :label="`${t} 型`" :value="t"/>
          </el-select>
          <el-select
              v-model="applyForm.patientRh"
              class="!w-32"
              data-testid="p4-tf-apply-rh"
              placeholder="Rh"
          >
            <el-option v-for="t in rhTypes" :key="t" :label="`Rh ${t}性`" :value="t"/>
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="血液品种" required>
        <el-select
            v-model="applyForm.bloodComponent"
            class="!w-full"
            data-testid="p4-tf-apply-component"
            placeholder="选择血液品种"
        >
          <el-option v-for="c in components" :key="c.code" :label="c.label" :value="c.code"/>
        </el-select>
      </el-form-item>
      <el-form-item label="规格">
        <el-input
            v-model="applyForm.componentSpec"
            maxlength="64"
            placeholder="如 1.5U / 200ml / 1治疗量（可空）"
        />
      </el-form-item>
      <el-form-item label="申请袋数" required>
        <el-input
            v-model.number="applyForm.bagCount"
            class="!w-40"
            data-testid="p4-tf-apply-bagcount"
            placeholder="配血累计不得超过此数"
            type="number"
        />
        <span class="ml-3 text-[11px] text-slate-400">本次申请最多 20 袋</span>
      </el-form-item>
      <el-form-item label="申请总量 / 单位">
        <div class="flex items-center gap-3">
          <el-input
              v-model.number="applyForm.plannedAmount"
              class="!w-40"
              placeholder="总量"
          />
          <el-select v-model="applyForm.amountUnit" class="!w-32" clearable placeholder="单位">
            <el-option v-for="u in AMOUNT_UNITS" :key="u" :label="u" :value="u"/>
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="输血目的">
        <el-input
            v-model="applyForm.transfusionPurpose"
            maxlength="200"
            placeholder="如：纠正贫血 / 补充凝血因子 / 提升血小板（可空）"
        />
      </el-form-item>
      <el-form-item label="输血指征" required>
        <el-input
            v-model="applyForm.indication"
            :rows="3"
            data-testid="p4-tf-apply-indication"
            placeholder="指标 + 症状（如：Hb 62 g/L，活动后心悸气促）——无指征用血是飞行检查的重点"
            type="textarea"
        />
      </el-form-item>
      <el-form-item label="输血前 Hb">
        <div class="flex items-center gap-3">
          <el-input v-model.number="applyForm.preHb" class="!w-40" placeholder="Hb g/L"/>
          <el-input v-model.number="applyForm.preHct" class="!w-40" placeholder="HCT %"/>
          <el-input v-model.number="applyForm.prePlt" class="!w-48" placeholder="PLT ×10⁹/L"/>
        </div>
      </el-form-item>
      <el-form-item label="既往输血史">
        <el-input v-model="applyForm.transfusionHistory" maxlength="500" placeholder="可空"/>
      </el-form-item>
      <el-form-item label="既往反应史">
        <el-input v-model="applyForm.reactionHistory" maxlength="500" placeholder="有输血反应史会影响选血与备血方案"/>
      </el-form-item>
      <el-form-item label="妊娠史">
        <el-input v-model="applyForm.pregnancyHistory" maxlength="200"
                  placeholder="育龄女性填写（与不规则抗体产生相关）"/>
      </el-form-item>
      <el-form-item label="紧急用血">
        <el-radio-group v-model="applyForm.isEmergency">
          <el-radio :value="0">常规</el-radio>
          <el-radio :value="1">紧急</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="applyForm.remark" :rows="2" placeholder="可空" type="textarea"/>
      </el-form-item>
    </el-form>
    <div class="rounded border border-amber-200 bg-amber-50 px-3 py-2 text-[12px] leading-5 text-amber-700">
      <el-icon class="mr-1 align-middle">
        <Warning/>
      </el-icon>
      提交后血<b>尚未配</b>：血袋号与交叉配血结果由输血科在「配血」里逐袋录入。
      血型以本次输血前鉴定为准 —— 与患者档案不一致时系统会在备注里提示，但不阻断（档案常是旧的）。
    </div>
    <template #footer>
      <el-button v-perm="['medtech:transfusion:add','medtech:transfusion:edit']" :loading="applySubmitting"
                 data-testid="p4-tf-apply-submit" type="primary" @click="submitApply">
        {{ applyForm.id ? '保存修改' : '提交用血申请' }}
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// ---------------- 一、发起 / 修改用血申请 ----------------
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Warning} from '@element-plus/icons-vue';
import {saveTransfusionApply} from '@/api/inpatientTransfusion';

// row 为空 = 发起，有值 = 修改（后端是同一个 saveTransfusionApply，两侧字段与校验必须同一份）
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  admissions: {type: Array, default: () => []},
  admissionLabel: {type: Function, required: true},
  defaultAdmissionId: {type: String, default: ''},
  aboTypes: {type: Array, default: () => []},
  rhTypes: {type: Array, default: () => []},
  components: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));
const AMOUNT_UNITS = ['U', 'ml', '治疗量'];

const applySubmitting = ref(false);
const applyForm = reactive({
  id: '',
  admissionId: '',
  patientAbo: '',
  patientRh: '',
  bloodComponent: undefined,
  componentSpec: '',
  bagCount: 2,
  plannedAmount: undefined,
  amountUnit: '',
  transfusionPurpose: '',
  indication: '',
  preHb: undefined,
  preHct: undefined,
  prePlt: undefined,
  transfusionHistory: '',
  reactionHistory: '',
  pregnancyHistory: '',
  isEmergency: 0,
  remark: '',
});
const currentAdmission = computed(() => props.admissions.find((a) => String(a.admissionId) === String(applyForm.admissionId)));
const currentComponentLabel = computed(() => props.components.find((c) => c.code === applyForm.bloodComponent)?.label || '—');

watch(visible, (open) => {
  if (!open) return;
  const row = props.row;
  if (row) {
    applyForm.id = row.id;
    applyForm.admissionId = row.admissionId || '';
    applyForm.patientAbo = row.patientAbo || '';
    applyForm.patientRh = row.patientRh || '';
    applyForm.bloodComponent = row.bloodComponent;
    applyForm.componentSpec = row.componentSpec || '';
    applyForm.bagCount = row.bagCount || 1;
    applyForm.plannedAmount = row.plannedAmount;
    applyForm.amountUnit = row.amountUnit || '';
    applyForm.transfusionPurpose = row.transfusionPurpose || '';
    applyForm.indication = row.indication || '';
    applyForm.preHb = row.preHb;
    applyForm.preHct = row.preHct;
    applyForm.prePlt = row.prePlt;
    applyForm.transfusionHistory = row.transfusionHistory || '';
    applyForm.reactionHistory = row.reactionHistory || '';
    applyForm.pregnancyHistory = row.pregnancyHistory || '';
    applyForm.isEmergency = row.isEmergency ?? 0;
    applyForm.remark = row.remark || '';
  } else {
    applyForm.id = '';
    applyForm.admissionId = props.defaultAdmissionId || '';
    applyForm.patientAbo = '';
    applyForm.patientRh = '阳';
    applyForm.bloodComponent = undefined;
    applyForm.componentSpec = '';
    applyForm.bagCount = 2;
    applyForm.plannedAmount = undefined;
    applyForm.amountUnit = '';
    applyForm.transfusionPurpose = '';
    applyForm.indication = '';
    applyForm.preHb = undefined;
    applyForm.preHct = undefined;
    applyForm.prePlt = undefined;
    applyForm.transfusionHistory = '';
    applyForm.reactionHistory = '';
    applyForm.pregnancyHistory = '';
    applyForm.isEmergency = 0;
    applyForm.remark = '';
  }
});

const submitApply = async () => {
  if (!applyForm.admissionId) {
    ElMessage.warning('请选择在院患者');
    return;
  }
  if (!applyForm.patientAbo) {
    ElMessage.warning('请选择受血者 ABO 血型（以本次输血前鉴定为准）');
    return;
  }
  if (!applyForm.patientRh) {
    ElMessage.warning('请选择受血者 Rh 血型（Rh 阴性属稀有血型，直接决定备血方案）');
    return;
  }
  if (applyForm.bloodComponent === undefined) {
    ElMessage.warning('请选择血液品种');
    return;
  }
  if (!applyForm.bagCount || applyForm.bagCount < 1) {
    ElMessage.warning('申请袋数必须大于 0');
    return;
  }
  if (!applyForm.indication.trim()) {
    ElMessage.warning('请填写输血指征（无指征用血是飞行检查的重点）');
    return;
  }
  applySubmitting.value = true;
  try {
    const res = await saveTransfusionApply({
      id: applyForm.id || undefined,
      admissionId: applyForm.admissionId,
      patientAbo: applyForm.patientAbo,
      patientRh: applyForm.patientRh,
      bloodComponent: applyForm.bloodComponent,
      componentSpec: applyForm.componentSpec.trim() || undefined,
      bagCount: applyForm.bagCount,
      plannedAmount: applyForm.plannedAmount,
      amountUnit: applyForm.amountUnit.trim() || undefined,
      transfusionPurpose: applyForm.transfusionPurpose.trim() || undefined,
      indication: applyForm.indication.trim(),
      preHb: applyForm.preHb,
      preHct: applyForm.preHct,
      prePlt: applyForm.prePlt,
      transfusionHistory: applyForm.transfusionHistory.trim() || undefined,
      reactionHistory: applyForm.reactionHistory.trim() || undefined,
      pregnancyHistory: applyForm.pregnancyHistory.trim() || undefined,
      isEmergency: applyForm.isEmergency,
      remark: applyForm.remark.trim() || undefined,
    });
    ElMessage.success(`${applyForm.id ? '用血申请已修改' : '用血申请已提交'}：${res.data || ''}（等待输血科配血）`);
    visible.value = false;
    emit('saved');
  } catch (error) {
    ElMessage.error(error.message || '用血申请提交失败');
  } finally {
    applySubmitting.value = false;
  }
};
</script>
