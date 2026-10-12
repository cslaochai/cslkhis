<template>
  <el-dialog v-model="visible" destroy-on-close title="出院办理" width="640px">
    <div class="mb-3 rounded-lg bg-amber-50 p-3 text-xs text-amber-700">
      {{ dischargeForm.patientName }} · {{ dischargeForm.admissionNo }} —— 提交后：床位释放、入院状态置已出院、
      病案首页回写住院天数与离院方式，并自动判定 31 日内再入院。
    </div>
    <el-form :model="dischargeForm" label-width="110px">
      <div class="grid grid-cols-2 gap-x-4">
        <el-form-item label="离院方式" required>
          <el-select v-model="dischargeForm.dischargeWay" class="!w-full" @change="onDischargeWayChange">
            <el-option v-for="(label, code) in dischargeWayMap" :key="code" :label="label" :value="Number(code)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="是否死亡">
          <el-radio-group v-model="dischargeForm.deathFlag" @change="onDeathFlagChange">
            <el-radio :value="0">否</el-radio>
            <el-radio :value="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出院时间">
          <el-date-picker
              v-model="dischargeForm.dischargeTime"
              class="!w-full"
              placeholder="不填取当前时间"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="出院医生">
          <el-select v-model="dischargeForm.dischargeDoctorId" class="!w-full" clearable placeholder="选择医生">
            <el-option v-for="e in employees" :key="e.id" :label="e.empName" :value="e.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="出院诊断编码">
          <el-input v-model="dischargeForm.dischargeDiagnosisCode" placeholder="ICD-10"/>
        </el-form-item>
        <el-form-item label="出院诊断">
          <el-input v-model="dischargeForm.dischargeDiagnosis" placeholder="如 肺炎（治愈）"/>
        </el-form-item>
      </div>
      <el-form-item label="出院小结">
        <el-input v-model="dischargeForm.dischargeSummary" :rows="3" placeholder="住院经过、治疗结果、出院带药与随访要求"
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <div class="rounded-lg bg-slate-50 p-3 text-xs text-slate-500">
      死亡病例必须同时满足「是否死亡=是」与「离院方式=死亡」，两者不一致会被拒绝 —— 这两个字段是医保审核死亡病例的第一着眼点。
    </div>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="'ipd:inpatient:edit'" :loading="dischargeLoading" type="primary" @click="submitDischarge">
        确认出院
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {dischargeInpatient} from '@/api/inpatient';
import {dischargeWayMap} from '@/lib/inpatient';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 当前住院列表行：取 admissionId / patientName / admissionNo / 诊断兜底
  row: {type: Object, default: null},
  // 医生名册由主页面加载一次（入院登记弹框共用），以 prop 传下来
  employees: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const dischargeLoading = ref(false);
const dischargeForm = reactive({
  admissionId: '',
  dischargeTime: '',
  dischargeDoctorId: null,
  dischargeWay: 1,
  deathFlag: 0,
  dischargeDiagnosis: '',
  dischargeDiagnosisCode: '',
  dischargeSummary: '',
  patientName: '',
  admissionNo: '',
});

watch(visible, (open) => {
  if (!open)
    return;
  const row = props.row || {};
  Object.assign(dischargeForm, {
    admissionId: row.admissionId,
    dischargeTime: '',
    dischargeDoctorId: null,
    dischargeWay: 1,
    deathFlag: 0,
    dischargeDiagnosis: row.mainDiagnosisName || row.diagnosis || '',
    dischargeDiagnosisCode: '',
    dischargeSummary: '',
    patientName: row.patientName || '',
    admissionNo: row.admissionNo,
  });
});
// 死亡标志与离院方式必须一致：这是病案首页的硬约束，后端会拒，前端先联动起来
const onDischargeWayChange = (v) => {
  if (v === 5)
    dischargeForm.deathFlag = 1;
  else
    dischargeForm.deathFlag = 0;
};
const onDeathFlagChange = (v) => {
  if (v === 1)
    dischargeForm.dischargeWay = 5;
  else if (dischargeForm.dischargeWay === 5)
    dischargeForm.dischargeWay = 1;
};
const submitDischarge = async () => {
  dischargeLoading.value = true;
  try {
    await dischargeInpatient({
      ...dischargeForm,
      dischargeTime: dischargeForm.dischargeTime || undefined,
    });
    ElMessage.success('出院办理成功：床位已释放、病案首页已回写住院天数与离院方式');
    visible.value = false;
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.message || '出院办理失败');
  } finally {
    dischargeLoading.value = false;
  }
};
</script>
