<template>
  <el-dialog v-model="visible" :title="`病案首页 - ${summaryMeta.patientName}（${summaryMeta.admissionNo}）`"
             destroy-on-close top="4vh" width="1080px">
    <div v-loading="summaryLoading" class="max-h-[70vh] space-y-5 overflow-y-auto pr-1">
      <div class="flex flex-wrap items-center gap-2 rounded-lg bg-slate-50 p-3 text-xs">
        <el-tag :type="summaryStatusMap[summaryMeta.status]?.type || 'info'" effect="plain" size="small">
          {{ summaryStatusMap[summaryMeta.status]?.label || '未生成' }}
        </el-tag>
        <span class="text-slate-500">住院天数：{{ summaryMeta.days }} 天</span>
        <span class="text-slate-500">离院方式：{{ textOf(dischargeWayMap, summaryMeta.dischargeWay) }}</span>
        <span v-if="summaryMeta.readmit31d === 1" class="font-medium text-rose-600">31 日内再入院</span>
        <span class="ml-auto text-slate-400">归档后本页只读</span>
      </div>

      <fieldset :disabled="!summaryEditable" class="space-y-5">
        <!-- 基本信息 -->
        <div>
          <p class="mb-2 font-semibold text-slate-800">基本信息</p>
          <div class="grid grid-cols-3 gap-x-4">
            <el-form-item label="入院途径" label-width="90px">
              <el-select v-model="summaryForm.admitWay" class="!w-full">
                <el-option v-for="(label, code) in admitWayMap" :key="code" :label="label" :value="Number(code)"/>
              </el-select>
            </el-form-item>
            <el-form-item label="是否抢救" label-width="90px">
              <el-radio-group v-model="summaryForm.isRescue">
                <el-radio :value="0">否</el-radio>
                <el-radio :value="1">是</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="是否危重" label-width="90px">
              <el-radio-group v-model="summaryForm.isCritical">
                <el-radio :value="0">否</el-radio>
                <el-radio :value="1">是</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>
        </div>

        <!-- 诊断明细 -->
        <div class="rounded-lg border border-slate-200 p-4">
          <div class="mb-2 flex items-center justify-between">
            <p class="font-semibold text-slate-800">
              诊断明细
              <span class="ml-2 text-xs font-normal text-slate-400">主要诊断必须且只能 1 条</span>
            </p>
            <el-button :icon="Plus" size="small" type="primary" @click="addDiagnosis">添加诊断</el-button>
          </div>
          <el-table :data="summaryForm.diagnoses" border size="small">
            <el-table-column label="类型" width="120">
              <template #default="{ row }">
                <el-select v-model="row.diagType" size="small" @change="onDiagTypeChange(row)">
                  <el-option :value="1" label="主要诊断"/>
                  <el-option :value="2" label="其他诊断"/>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="ICD-10" width="130">
              <template #default="{ row }">
                <el-input v-model="row.icdCode" placeholder="J18.9" size="small"/>
              </template>
            </el-table-column>
            <el-table-column label="诊断名称" width="150">
              <template #default="{ row }">
                <el-input v-model="row.icdName" placeholder="肺炎" size="small"/>
              </template>
            </el-table-column>
            <el-table-column label="入院病情" width="130">
              <template #default="{ row }">
                <el-select v-model="row.admitCondition" size="small">
                  <el-option v-for="(label, code) in admitConditionMap" :key="code" :label="label"
                             :value="Number(code)"/>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="CC/MCC" width="105">
              <template #default="{ row }">
                <el-select v-model="row.ccLevel" size="small">
                  <el-option label="NONE" value="NONE"/>
                  <el-option label="CC" value="CC"/>
                  <el-option label="MCC" value="MCC"/>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="诊断依据（病历中的支持描述）" min-width="220">
              <template #default="{ row }">
                <el-input v-model="row.diagnosisBasis" placeholder="如 胸片示右下肺片状影，血象升高" size="small"/>
              </template>
            </el-table-column>
            <el-table-column align="center" label="操作" width="70">
              <template #default="{ $index }">
                <el-button :icon="Delete" link size="small" type="danger" @click="removeDiagnosis($index)"/>
              </template>
            </el-table-column>
          </el-table>
          <p v-if="!summaryForm.diagnoses.length" class="py-3 text-center text-xs text-slate-400">
            暂无诊断。没有主要诊断 DRG 无法分组，结算清单必填项也过不了。
          </p>
          <p class="mt-2 text-xs text-slate-400">
            「诊断依据」是四核对里「病历 vs 编码」的那条边：编码写 J18.9，病历里就得有能支撑肺炎的描述。
          </p>
        </div>

        <!-- 手术操作明细 -->
        <div class="rounded-lg border border-slate-200 p-4">
          <div class="mb-2 flex items-center justify-between">
            <p class="font-semibold text-slate-800">
              手术操作明细
              <span class="ml-2 text-xs font-normal text-slate-400">主要手术最多 1 条</span>
            </p>
            <el-button :icon="Plus" size="small" type="primary" @click="addOperation">添加手术</el-button>
          </div>
          <el-table :data="summaryForm.operations" border size="small">
            <el-table-column align="center" label="主要" width="70">
              <template #default="{ row }">
                <el-checkbox :model-value="row.isMain === 1"
                             @change="(v: any) => { row.isMain = v ? 1 : 0; onMainOpChange(row) }"/>
              </template>
            </el-table-column>
            <el-table-column label="ICD-9-CM-3" width="130">
              <template #default="{ row }">
                <el-input v-model="row.operationCode" placeholder="33.2400" size="small"/>
              </template>
            </el-table-column>
            <el-table-column label="手术操作名称" width="150">
              <template #default="{ row }">
                <el-input v-model="row.operationName" placeholder="支气管镜检查" size="small"/>
              </template>
            </el-table-column>
            <el-table-column label="手术日期" width="190">
              <template #default="{ row }">
                <el-date-picker v-model="row.operationDate" class="!w-full" size="small"
                                type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
              </template>
            </el-table-column>
            <el-table-column label="级别" width="95">
              <template #default="{ row }">
                <el-select v-model="row.operationLevel" size="small">
                  <el-option v-for="(label, code) in operationLevelMap" :key="code" :label="label"
                             :value="Number(code)"/>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="切口" width="95">
              <template #default="{ row }">
                <el-select v-model="row.incisionLevel" size="small">
                  <el-option v-for="(label, code) in incisionLevelMap" :key="code" :label="label"
                             :value="Number(code)"/>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="麻醉" width="105">
              <template #default="{ row }">
                <el-select v-model="row.anesthesiaType" size="small">
                  <el-option v-for="(label, code) in anesthesiaMap" :key="code" :label="label" :value="Number(code)"/>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="主刀" width="110">
              <template #default="{ row }">
                <el-input v-model="row.surgeonName" size="small"/>
              </template>
            </el-table-column>
            <el-table-column label="手术依据" min-width="180">
              <template #default="{ row }">
                <el-input v-model="row.operationBasis" placeholder="如 镜下见右下叶支气管黏膜充血" size="small"/>
              </template>
            </el-table-column>
            <el-table-column align="center" label="操作" width="70">
              <template #default="{ $index }">
                <el-button :icon="Delete" link size="small" type="danger" @click="removeOperation($index)"/>
              </template>
            </el-table-column>
          </el-table>
          <p v-if="!summaryForm.operations.length" class="py-3 text-center text-xs text-slate-400">
            无手术操作。术后不填手术记录，等于把本该拿的手术费留在桌上。
          </p>
        </div>

        <!-- 费用信息 -->
        <div class="rounded-lg border border-slate-200 p-4">
          <p class="mb-2 font-semibold text-slate-800">费用信息</p>
          <div class="grid grid-cols-4 gap-x-4">
            <el-form-item label="总费用" label-width="80px">
              <el-input-number v-model="summaryForm.totalAmount" :controls="false" :min="0" :precision="2"
                               class="!w-full"/>
            </el-form-item>
            <el-form-item label="西药费" label-width="80px">
              <el-input-number v-model="summaryForm.westernDrugAmount" :controls="false" :min="0" :precision="2"
                               class="!w-full"/>
            </el-form-item>
            <el-form-item label="中成药费" label-width="80px">
              <el-input-number v-model="summaryForm.chineseDrugAmount" :controls="false" :min="0" :precision="2"
                               class="!w-full"/>
            </el-form-item>
            <el-form-item label="中药饮片" label-width="80px">
              <el-input-number v-model="summaryForm.herbalAmount" :controls="false" :min="0" :precision="2"
                               class="!w-full"/>
            </el-form-item>
            <el-form-item label="检查费" label-width="80px">
              <el-input-number v-model="summaryForm.examAmount" :controls="false" :min="0" :precision="2"
                               class="!w-full"/>
            </el-form-item>
            <el-form-item label="检验费" label-width="80px">
              <el-input-number v-model="summaryForm.labAmount" :controls="false" :min="0" :precision="2"
                               class="!w-full"/>
            </el-form-item>
            <el-form-item label="治疗费" label-width="80px">
              <el-input-number v-model="summaryForm.treatmentAmount" :controls="false" :min="0" :precision="2"
                               class="!w-full"/>
            </el-form-item>
            <el-form-item label="手术费" label-width="80px">
              <el-input-number v-model="summaryForm.operationAmount" :controls="false" :min="0" :precision="2"
                               class="!w-full"/>
            </el-form-item>
            <el-form-item label="耗材费" label-width="80px">
              <el-input-number v-model="summaryForm.materialAmount" :controls="false" :min="0" :precision="2"
                               class="!w-full"/>
            </el-form-item>
            <el-form-item label="床位费" label-width="80px">
              <el-input-number v-model="summaryForm.bedAmount" :controls="false" :min="0" :precision="2"
                               class="!w-full"/>
            </el-form-item>
            <el-form-item label="护理费" label-width="80px">
              <el-input-number v-model="summaryForm.nursingAmount" :controls="false" :min="0" :precision="2"
                               class="!w-full"/>
            </el-form-item>
            <el-form-item label="其他费用" label-width="80px">
              <el-input-number v-model="summaryForm.otherAmount" :controls="false" :min="0" :precision="2"
                               class="!w-full"/>
            </el-form-item>
          </div>
          <el-form-item label="备注" label-width="80px">
            <el-input v-model="summaryForm.remark"/>
          </el-form-item>
        </div>
      </fieldset>
    </div>
    <template #footer>
      <el-button v-if="summaryEditable" v-perm="'ipd:inpatient:edit'" :loading="summaryLoading" type="primary"
                 @click="submitSummary">保存病案首页
      </el-button>
      <el-button v-else disabled>已归档，不可修改</el-button>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {Plus, Delete} from '@element-plus/icons-vue';
import {ElMessage} from 'element-plus';
import {getInpatientDetail, saveInpatientSummary} from '@/api/inpatient';
import {
  admitConditionMap,
  admitWayMap,
  anesthesiaMap,
  dischargeWayMap,
  incisionLevelMap,
  operationLevelMap,
  summaryStatusMap,
  textOf
} from '@/lib/inpatient';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 入院记录主键：病案首页自己按 id 取数
  id: {type: [String, Number], default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const summaryLoading = ref(false);
const summaryForm = reactive({
  admissionId: '',
  ageUnit: 1,
  admitWay: 1,
  isRescue: 0,
  isCritical: 0,
  totalAmount: null,
  westernDrugAmount: null,
  chineseDrugAmount: null,
  herbalAmount: null,
  examAmount: null,
  labAmount: null,
  treatmentAmount: null,
  operationAmount: null,
  materialAmount: null,
  bedAmount: null,
  nursingAmount: null,
  otherAmount: null,
  remark: '',
  diagnoses: [],
  operations: [],
});
const summaryMeta = reactive({
  patientName: '',
  admissionNo: '',
  status: 0,
  days: 0,
  dischargeWay: null,
  readmit31d: null
});

watch(visible, async (open) => {
  if (!open)
    return;
  summaryLoading.value = true;
  try {
    const res = await getInpatientDetail(props.id);
    const d = res.data || {};
    const s = d.summary || {};
    Object.assign(summaryForm, {
      admissionId: props.id,
      ageUnit: s.ageUnit ?? 1,
      admitWay: s.admitWay ?? d.admission?.admitWay ?? 1,
      isRescue: s.isRescue ?? 0,
      isCritical: s.isCritical ?? 0,
      totalAmount: s.totalAmount ?? null,
      westernDrugAmount: s.westernDrugAmount ?? null,
      chineseDrugAmount: s.chineseDrugAmount ?? null,
      herbalAmount: s.herbalAmount ?? null,
      examAmount: s.examAmount ?? null,
      labAmount: s.labAmount ?? null,
      treatmentAmount: s.treatmentAmount ?? null,
      operationAmount: s.operationAmount ?? null,
      materialAmount: s.materialAmount ?? null,
      bedAmount: s.bedAmount ?? null,
      nursingAmount: s.nursingAmount ?? null,
      otherAmount: s.otherAmount ?? null,
      remark: s.remark ?? '',
      diagnoses: (d.diagnoses || []).map((x) => ({...x})),
      operations: (d.operations || []).map((x) => ({...x})),
    });
    Object.assign(summaryMeta, {
      patientName: d.admission?.patientName || '',
      admissionNo: d.admission?.admissionNo || '',
      status: s.summaryStatus ?? 1,
      days: d.admission?.inpatientDays ?? 0,
      dischargeWay: s.dischargeWay ?? null,
      readmit31d: s.readmit31d ?? null,
    });
    // 诊断条数的主诊断约束由后端把关，但主要诊断必须排第一条，前端保存前顺手排一下
    normalizeOrder();
  } catch (e) {
    ElMessage.error(e?.message || '加载病案首页失败');
    visible.value = false;
  } finally {
    summaryLoading.value = false;
  }
});
// 主诊断 / 主手术恒排第一条，前端展示顺序与后端落库顺序保持一致
const normalizeOrder = () => {
  summaryForm.diagnoses.sort((a, b) => (a.diagType === 1 ? 0 : 1) - (b.diagType === 1 ? 0 : 1));
  summaryForm.operations.sort((a, b) => (a.isMain === 1 ? 0 : 1) - (b.isMain === 1 ? 0 : 1));
};
const addDiagnosis = () => {
  summaryForm.diagnoses.push({
    diagType: summaryForm.diagnoses.length === 0 ? 1 : 2,
    icdCode: '', icdName: '', admitCondition: 1, ccLevel: 'NONE', diagnosisBasis: '',
  });
};
const addOperation = () => {
  summaryForm.operations.push({
    isMain: summaryForm.operations.length === 0 ? 1 : 0,
    operationCode: '', operationName: '', operationDate: '', operationLevel: 2,
    incisionLevel: 1, anesthesiaType: 1, surgeonName: '', operationBasis: '',
  });
};
const removeDiagnosis = (i) => summaryForm.diagnoses.splice(i, 1);
const removeOperation = (i) => summaryForm.operations.splice(i, 1);
// 主诊断唯一性：勾了新的主诊断，就把旧的降为「其他诊断」，不让用户提交一个必然被拒的请求
const onDiagTypeChange = (row) => {
  if (row.diagType === 1) {
    summaryForm.diagnoses.forEach(d => {
      if (d !== row)
        d.diagType = 2;
    });
  }
  normalizeOrder();
};
const onMainOpChange = (row) => {
  if (row.isMain === 1) {
    summaryForm.operations.forEach(o => {
      if (o !== row)
        o.isMain = 0;
    });
  }
  normalizeOrder();
};
const submitSummary = async () => {
  summaryLoading.value = true;
  try {
    await saveInpatientSummary({...summaryForm});
    ElMessage.success('病案首页已保存');
    visible.value = false;
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.message || '保存病案首页失败');
  } finally {
    summaryLoading.value = false;
  }
};
// 按病案首页存在与否给出「可编辑」判断：归档后只读
const summaryEditable = computed(() => summaryMeta.status !== 3);
</script>
