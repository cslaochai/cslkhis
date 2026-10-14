<template>
  <el-dialog v-model="visible" destroy-on-close title="急诊登记" width="760px">
    <!-- 患者来源切换 -->
    <div class="mb-4 flex rounded-lg border border-slate-200 bg-slate-50 p-1">
      <button
          :class="addMode === 'existing' ? 'bg-white text-blue-600 shadow-sm' : 'text-slate-500 hover:text-slate-700'"
          class="flex-1 rounded-md py-2 text-sm font-medium transition-colors"
          @click="addMode = 'existing'"
      >选择已有患者
      </button>
      <button
          :class="addMode === 'new' ? 'bg-white text-blue-600 shadow-sm' : 'text-slate-500 hover:text-slate-700'"
          class="flex-1 rounded-md py-2 text-sm font-medium transition-colors"
          @click="addMode = 'new'"
      >录入新患者
      </button>
    </div>

    <el-form :model="addForm" label-position="left">
      <!-- 已有患者模式 -->
      <template v-if="addMode === 'existing'">
        <el-form-item label="选择患者" required>
          <PatientSelect
              v-model="addForm.patientId"
              placeholder="输入姓名 / 患者号 / 手机号 / 身份证号搜索"
              @select="handlePatientSelect"
          />
        </el-form-item>
        <!-- 已选患者信息回显 -->
        <div v-if="addForm.patientId" class="mb-4 rounded-lg border border-blue-200 bg-blue-50 p-3">
          <div class="flex items-center gap-3 text-sm">
              <span :class="['inline-flex h-8 w-8 items-center justify-center rounded-full text-sm font-bold text-white',
                genderDot[patientAvatarTone(addForm.gender)]]">
            {{ patientGenderSymbol(addForm.gender) }}
          </span>
            <div>
              <span class="font-medium text-slate-900">{{ addForm.patientName }}</span>
              <span class="ml-2 text-xs text-slate-500">{{ addForm.patientNo }} · {{
                  addForm.age
                }}岁 · {{ addForm.phone }}</span>
            </div>
          </div>
        </div>
      </template>

      <!-- 新患者模式 -->
      <template v-else>
        <div class="mb-4 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
          急诊直录患者，就诊后可到患者管理中补全信息
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="患者姓名" required>
            <el-input v-model="addForm.patientName" placeholder="请输入姓名"/>
          </el-form-item>
          <el-form-item label="性别" required>
            <el-radio-group v-model="addForm.gender">
              <el-radio v-for="g in PATIENT_GENDER_OPTIONS" :key="g.value" :value="g.value">{{ g.label }}</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="年龄" required>
            <el-input v-model.number="addForm.age" placeholder="请输入年龄" type="number"/>
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="addForm.phone" placeholder="选填（填了须为 11 位手机号）"/>
          </el-form-item>
        </div>
        <el-form-item label="身份证号">
          <el-input v-model="addForm.idCard" placeholder="选填，用于患者身份核验"/>
        </el-form-item>
      </template>

      <!-- 共享字段 -->
      <el-form-item label="主诉" required>
        <!-- 主诉一改就作废旧建议：否则可能采纳一条针对别的主诉算出来的级别 -->
        <el-input v-model="addForm.chiefComplaint" :rows="2" placeholder="请输入主诉" type="textarea"
                  @input="triageSuggestion = null"/>
      </el-form-item>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="分诊级别" required>
          <el-select v-model="addForm.triageLevel" class="w-full">
            <el-option v-for="(v, k) in triageLevelMap" :key="k" :label="v.label" :value="Number(k)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="接诊科室">
          <el-select v-model="addForm.deptId" class="w-full" clearable filterable placeholder="选择科室"
                     @change="handleDeptChange">
            <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id"/>
          </el-select>
        </el-form-item>
      </div>

      <!-- AI 分诊建议 -->
      <div class="mb-4 rounded-lg border border-slate-200 bg-slate-50/60 p-3">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-slate-700">AI 分诊建议</p>
            <p class="mt-0.5 text-xs text-slate-400">
              填写生命体征后由系统判定红旗征象；<b>只做建议，不会自动改分诊级别</b>
            </p>
          </div>
          <el-button
              :loading="triageSuggesting"
              plain
              size="small"
              type="primary"
              @click="runTriageSuggest"
          >
            获取建议
          </el-button>
        </div>

        <div class="mt-3 grid grid-cols-3 gap-2">
          <el-input-number v-model="vitalForm.temperature" :max="45" :min="30" :precision="1" :step="0.1"
                           class="!w-full" controls-position="right" placeholder="体温℃" size="small"/>
          <el-input-number v-model="vitalForm.pulse" :max="300" :min="0" class="!w-full" controls-position="right"
                           placeholder="脉搏" size="small"/>
          <el-input-number v-model="vitalForm.respiratory" :max="100" :min="0" class="!w-full"
                           controls-position="right" placeholder="呼吸" size="small"/>
          <el-input-number v-model="vitalForm.systolic" :max="300" :min="0" class="!w-full" controls-position="right"
                           placeholder="收缩压" size="small"/>
          <el-input-number v-model="vitalForm.diastolic" :max="200" :min="0" class="!w-full"
                           controls-position="right" placeholder="舒张压" size="small"/>
          <el-input-number v-model="vitalForm.spo2" :max="100" :min="0" class="!w-full" controls-position="right"
                           placeholder="血氧%" size="small"/>
        </div>
        <p class="mt-1 text-[11px] text-slate-400">
          体温 / 脉搏 / 呼吸 / 收缩压 / 舒张压 / 血氧饱和度。不填也能获取建议，但命中红旗征象的可能性会下降。
        </p>

        <div v-if="triageSuggestion" class="mt-3 space-y-2">
          <el-alert
              v-if="triageSuggestion.degraded"
              :closable="false"
              show-icon
              title="本次未经过大模型，建议来自确定性规则"
              type="warning"
          >
            <template #default>
              <span class="text-xs">{{ triageSuggestion.degradeReason }}</span>
            </template>
          </el-alert>

          <div v-if="triageSuggestion.redFlags?.length" class="rounded border border-red-200 bg-red-50 p-2">
            <p class="mb-1 text-xs font-medium text-red-700">命中红旗征象</p>
            <ul class="list-disc pl-4 text-xs text-red-700">
              <li v-for="(f, i) in triageSuggestion.redFlags" :key="i">{{ f }}</li>
            </ul>
          </div>

          <div class="flex flex-wrap items-center gap-2 text-xs">
            <el-tag :type="levelBasisTag(triageSuggestion.levelBasis)" effect="plain" size="small">
              依据：{{ levelBasisText(triageSuggestion.levelBasis) }}
            </el-tag>
            <span class="text-slate-500">系统建议</span>
            <span
                :class="['rounded border px-2 py-0.5 font-medium',
                       triageLevelMap[triageSuggestion.suggestedLevel]?.color || '']">
              {{ triageSuggestion.suggestedLevelText }}
            </span>
            <span v-if="triageSuggestion.suggestedZone" class="text-slate-500">
              {{ triageSuggestion.suggestedZone }}
            </span>
            <el-tag v-if="triageSuggestion.suggestedGreenChannel" size="small" type="success">
              {{ triageSuggestion.suggestedGreenChannel }}
            </el-tag>
            <span class="text-slate-500">
              当前已选 {{ triageLevelMap[addForm.triageLevel]?.label }}
            </span>
          </div>

          <div v-if="triageSuggestion.recommendActions?.length" class="rounded bg-white p-2">
            <p class="mb-1 text-xs font-medium text-slate-600">建议措施</p>
            <ul class="list-disc pl-4 text-xs text-slate-600">
              <li v-for="(a, i) in triageSuggestion.recommendActions" :key="i">{{ a }}</li>
            </ul>
          </div>

          <p v-if="triageSuggestion.reasoning" class="text-xs text-slate-500">
            {{ triageSuggestion.reasoning }}
          </p>

          <div class="flex items-center gap-2">
            <el-button v-if="canAdoptSuggestion" size="small" type="danger" @click="adoptSuggestion">
              采纳建议，提升为 {{ triageSuggestion.suggestedLevelText }}
            </el-button>
            <span v-else class="text-xs text-slate-500">
              系统建议不会降低人工分级，维持当前所选级别即可
            </span>
          </div>
        </div>
      </div>
      <el-form-item label="接诊医生">
        <el-select v-model="addForm.doctorId" :disabled="!addForm.deptId" class="w-full"
                   clearable filterable
                   no-data-text="该科室此刻无在岗值班医生" placeholder="选择此刻在岗的值班医生（留空＝交给系统按排班派单）">
          <el-option v-for="doc in employeeList" :key="doc.id" :label="doc.empName"
                     :value="doc.id">
            <div class="flex items-center justify-between">
              <span class="text-sm font-medium">{{ doc.empName }}</span>
              <span v-if="doc.startTime" class="text-xs text-slate-400">{{ doc.startTime }}-{{ doc.endTime }}</span>
            </div>
          </el-option>
        </el-select>
        <div v-if="!addForm.deptId" class="mt-1 text-xs text-slate-400">
          先选接诊科室；不指定医生时，登记会按该科室<b>当前在岗排班</b>自动派单
        </div>
        <div v-else-if="employeeList.length === 0" class="mt-1 text-xs text-amber-600">
          该科室此刻无在岗值班医生，本例会进入<b>待派单池</b>（可被超时催办），请说明原因
        </div>
      </el-form-item>
      <el-form-item v-if="addForm.deptId && !addForm.doctorId && employeeList.length === 0"
                    data-testid="emg-unassigned-reason" label="未派单原因">
        <el-input v-model="addForm.unassignedReason" maxlength="200" placeholder="如：值班医生出诊在外 / 夜班仅一名医生在抢救，留空则系统记「当日该科室无在岗排班医生」"
                  show-word-limit/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'opd:emergency:add'" type="danger" @click="handleAddSubmit">确认登记</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getEmergencyDutySelectList, registerEmergency} from '@/api/emergency';
import {getDepartmentSelectList} from '@/api/system';
import {suggestEmergencyTriage} from '@/api/ai';
import PatientSelect from '@/components/his/PatientSelect.vue';
import {
  isIdCardFormatLegal,
  isPatientGenderCollected,
  isPhoneLegal,
  PATIENT_GENDER_OPTIONS,
  patientAvatarTone,
  patientGenderSymbol
} from '@/lib/patientGender';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 分诊级别字典由主页面持有（查询条与表格「分诊」列也在用），子组件不许再拉一份
  triageLevelMap: {type: Object, default: () => ({})},
  genderDot: {type: Object, default: () => ({})},
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const addMode = ref('existing'); // 选择已有患者 / 录入新患者
const addForm = ref({
  patientId: null,
  patientName: '',
  patientNo: '',
  // 不预设性别：默认成"男"等于静默编造性别（急诊也有性别专属判断）
  gender: null,
  age: 0,
  phone: '',
  idCard: '',
  chiefComplaint: '',
  triageLevel: 3,
  deptId: null,
  doctorId: null,
  // 只在「当前无在岗医生」时出现输入框；留空时后端兜底成「当日该科室无在岗排班医生」
  unassignedReason: '',
});

const departments = ref([]);
const employeeList = ref([]);

const loadDepartments = async () => {
  try {
    // 不传 scope → 默认按当前人过滤
    const res = await getDepartmentSelectList({deptType: 1});
    departments.value = res.data || [];
  } catch (error) {
    console.error('加载科室失败:', error);
  }
};

const loadDoctors = async (deptId) => {
  try {
    // 用「此刻在岗的值班医生」而不是门诊可挂号排班：急诊值班表号源恒为 0（不该被前台挂上门诊号），
    // 走 availableList 永远是空 → 页面写死"无排班医生"，而后端派单却查得到人，两边各说一套。
    const res = await getEmergencyDutySelectList(deptId);
    employeeList.value = (res.data || []).map((d) => ({
      id: d.doctorId,
      empName: d.doctorName,
      startTime: d.startTime,
      endTime: d.endTime,
    }));
  } catch (error) {
    employeeList.value = [];
  }
};

const handleDeptChange = (deptId) => {
  addForm.value.doctorId = null;
  if (deptId)
    loadDoctors(deptId);
  else
    employeeList.value = [];
};

const handlePatientSelect = (patient) => {
  if (!patient)
    return;
  addForm.value.patientId = patient.id;
  addForm.value.patientName = patient.patientName;
  addForm.value.patientNo = patient.patientNo;
  addForm.value.gender = patient.gender;
  addForm.value.age = patient.age || 0;
  addForm.value.phone = patient.phone || '';
};

// ==================== AI 分诊建议 ====================
//
// 分工：SpO2<90、收缩压<90、GCS≤8 这类红旗征象由后端确定性规则判定（HARD_RULE），
// 模型只在没有红旗征象时补充严重程度的判断。
//
// 「只升不降」在这一步必须由前端守住：登记时患者还没建档，
// 后端不知道该分诊台当前选的是几级，所以采纳建议时只在
// 「建议级别比当前选的更严重」时才允许改（数字更小 = 更严重）。
const vitalForm = ref({
  temperature: null,
  pulse: null,
  respiratory: null,
  systolic: null,
  diastolic: null,
  spo2: null,
});
const triageSuggestion = ref(null);
const triageSuggesting = ref(false);
const resetVitalForm = () => {
  vitalForm.value = {
    temperature: null, pulse: null, respiratory: null,
    systolic: null, diastolic: null, spo2: null,
  };
  triageSuggestion.value = null;
};
/** 只把填了值的项放进 JSON，空值不能当成 0（0 是一个结论） */
const buildVitalSigns = () => {
  const src = vitalForm.value;
  const out = {};
  const keys = ['temperature', 'pulse', 'respiratory', 'systolic', 'diastolic', 'spo2'];
  for (const k of keys) {
    const v = src[k];
    if (v !== null && v !== undefined && !Number.isNaN(Number(v))) {
      out[k] = Number(v);
    }
  }
  return Object.keys(out).length > 0 ? JSON.stringify(out) : undefined;
};
const runTriageSuggest = async () => {
  if (!addForm.value.chiefComplaint?.trim()) {
    ElMessage.warning('请先填写主诉');
    return;
  }
  triageSuggesting.value = true;
  try {
    const res = await suggestEmergencyTriage({
      chiefComplaint: addForm.value.chiefComplaint,
      vitalSigns: buildVitalSigns(),
      gender: addForm.value.gender || undefined,
      age: addForm.value.age || undefined,
    });
    triageSuggestion.value = res.data;
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    triageSuggesting.value = false;
  }
};
/** 建议比当前选的更严重才给「采纳」入口，绝不提供降级操作 */
const canAdoptSuggestion = computed(() => {
  const s = triageSuggestion.value;
  if (!s?.suggestedLevel)
    return false;
  return s.suggestedLevel < addForm.value.triageLevel;
});
const adoptSuggestion = () => {
  const s = triageSuggestion.value;
  if (!s?.suggestedLevel)
    return;
  if (s.suggestedLevel >= addForm.value.triageLevel) {
    ElMessage.warning('系统建议不会降低人工分级，请维持当前级别');
    return;
  }
  addForm.value.triageLevel = s.suggestedLevel;
  ElMessage.success(`已采纳建议：${s.suggestedLevelText}`);
};
const levelBasisText = (basis) => {
  if (basis === 'HARD_RULE')
    return '确定性红旗征象（硬规则）';
  if (basis === 'MANUAL')
    return '维持人工分级';
  if (basis === 'MODEL')
    return '大模型判断';
  return basis || '—';
};
const levelBasisTag = (basis) => {
  if (basis === 'HARD_RULE')
    return 'danger';
  if (basis === 'MODEL')
    return 'warning';
  return 'info';
};

watch(visible, async (open) => {
  if (!open)
    return;
  addForm.value = {
    patientId: null, patientName: '', patientNo: '', gender: null, age: 0,
    phone: '', idCard: '', chiefComplaint: '', triageLevel: 3, deptId: null, doctorId: null,
    unassignedReason: '',
  };
  addMode.value = 'existing';
  employeeList.value = [];
  resetVitalForm();
  if (departments.value.length === 0)
    await loadDepartments();
});

const handleAddSubmit = async () => {
  // 校验患者信息
  if (addMode.value === 'existing') {
    if (!addForm.value.patientId) {
      ElMessage.warning('请选择患者');
      return;
    }
  } else {
    if (!addForm.value.patientName) {
      ElMessage.warning('请填写患者姓名');
      return;
    }
    // 性别必选（含「未知」）：急诊同样有性别专属判断（妊娠、前列腺等）
    if (!isPatientGenderCollected(addForm.value.gender)) {
      ElMessage.warning('请选择性别（确实没问到请选「未知」）');
      return;
    }
    // 三无患者允许没有手机号/身份证；但填了就必须合法
    if (addForm.value.phone && !isPhoneLegal(addForm.value.phone)) {
      ElMessage.warning('手机号格式不正确：应为 11 位手机号');
      return;
    }
    if (addForm.value.idCard && !isIdCardFormatLegal(addForm.value.idCard)) {
      ElMessage.warning('身份证号格式不正确：应为 18 位（末位可为 X）');
      return;
    }
  }
  if (!addForm.value.chiefComplaint) {
    ElMessage.warning('请填写主诉');
    return;
  }
  try {
    const dept = departments.value.find((d) => d.id === addForm.value.deptId);
    const doc = employeeList.value.find((e) => e.id === addForm.value.doctorId);
    const data = {...addForm.value};
    if (!data)
      return;
    // 新患者模式下清空patientId，让后端知道这是急诊直录
    if (addMode.value === 'new') {
      data.patientId = null;
      data.patientNo = '';
    }
    if (dept)
      data.deptName = dept.deptName;
    if (doc)
      data.doctorName = doc.empName;
    await registerEmergency(data);
    // 派单结果要说清楚：登记成功不等于有人负责，用户必须知道自己刚才是不是把患者扔进了池子
    ElMessage.success(addForm.value.doctorId
        ? '急诊登记成功，已按所选医生派单'
        : (employeeList.value.length
            ? '急诊登记成功，已按当前在岗排班自动派单'
            : '急诊登记成功，该科室此刻无在岗医生，已进待派单池'));
    visible.value = false;
    emit('saved');
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
</script>
