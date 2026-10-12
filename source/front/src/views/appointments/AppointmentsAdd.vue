<template>
  <el-dialog v-model="visible" destroy-on-close title="新增挂号" width="760px" @closed="resetForm">
    <!-- 患者来源切换（与急诊登记同一交互）：tab 只切换**患者输入区**，
         下面的科室 / 日期 / 号源 / 时段 / 就诊类型 / 结算方式 是共享字段 -->
    <div class="mb-4 flex rounded-lg border border-slate-200 bg-slate-50 p-1">
      <button
          :class="addMode === 'existing' ? 'bg-white text-blue-600 shadow-sm' : 'text-slate-500 hover:text-slate-700'"
          class="flex-1 rounded-md py-2 text-sm font-medium transition-colors"
          type="button"
          @click="switchPatientMode('existing')"
      >选择已有患者
      </button>
      <button
          :class="addMode === 'new' ? 'bg-white text-blue-600 shadow-sm' : 'text-slate-500 hover:text-slate-700'"
          class="flex-1 rounded-md py-2 text-sm font-medium transition-colors"
          type="button"
          @click="switchPatientMode('new')"
      >录入新患者
      </button>
    </div>

    <el-form :model="newAppointment" label-position="top">
      <!-- tab1：选择已有患者 -->
      <template v-if="addMode === 'existing'">
        <el-form-item label="选择患者" required>
          <PatientSelect
              v-model="newAppointment.patientId"
              @select="handlePatientSelect"
          />
        </el-form-item>
        <!-- 已选患者回显：挂号窗口最容易犯的错是挂错人，选完必须让人一眼确认 -->
        <div v-if="newAppointment.patientId" class="mb-4 rounded-lg border border-blue-200 bg-blue-50 p-3">
          <div class="flex items-center gap-3 text-sm">
            <span :class="['inline-flex h-8 w-8 items-center justify-center rounded-full text-sm font-bold text-white',
              newAppointment.patientGender === 1 ? 'bg-blue-500'
                : newAppointment.patientGender === 2 ? 'bg-pink-500' : 'bg-slate-400']">
              {{ patientGenderSymbol(newAppointment.patientGender) }}
            </span>
            <div>
              <span class="font-medium text-slate-900">{{ newAppointment.patientName }}</span>
              <span class="ml-2 text-xs text-slate-500">{{ newAppointment.patientNo }}</span>
            </div>
          </div>
        </div>
      </template>

      <!-- tab2：录入新患者 -->
      <template v-else>
        <div class="mb-4 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
          将在患者主档新建档案，确认挂号时一并提交；其余信息可到「患者管理」补全
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="患者姓名" required>
            <el-input v-model="newPatientForm.patientName" placeholder="请输入患者姓名"/>
          </el-form-item>
          <el-form-item label="性别" required>
            <el-radio-group v-model="newPatientForm.gender">
              <el-radio v-for="g in PATIENT_GENDER_OPTIONS" :key="g.value" :value="g.value">{{ g.label }}</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="出生日期">
            <el-date-picker
                v-model="newPatientForm.birthDate"
                class="w-full"
                placeholder="选择出生日期"
                type="date"
                value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="newPatientForm.phone" placeholder="选填（填了须为 11 位手机号）"/>
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="身份证号" required>
            <el-input v-model="newPatientForm.idCard" placeholder="请输入身份证号" @blur="syncPatientBirthDate"/>
          </el-form-item>
          <el-form-item label="患者类型">
            <el-select v-model="newPatientForm.patientType" class="w-full" placeholder="选择患者类型">
              <el-option v-for="o in PATIENT_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="家庭住址">
          <el-input v-model="newPatientForm.address" placeholder="请输入家庭住址"/>
        </el-form-item>
      </template>

      <!-- 就诊类型提到科室之前：选「复诊 + 当日回诊」时下面的科室/日期/号源整块都不填，
           放在最后等于让人先填完再看着它们消失 -->
      <el-form-item label="就诊类型">
        <el-select v-model="newAppointment.visitType" class="w-full" placeholder="选择类型"
                   @change="handleVisitTypeChange">
          <el-option v-for="item in VISIT_TYPE" :key="item.value" :label="item.label" :value="item.value"/>
        </el-select>
      </el-form-item>
      <template v-if="isRevisit">
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="复诊来源" required>
            <el-select v-model="newAppointment.revisitSource" class="w-full" placeholder="选择复诊来源">
              <el-option v-for="o in windowRevisitSourceOptions" :key="o.dictValue" :label="o.dictLabel"
                         :value="Number(o.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="原病历（哪一次就诊）" required>
            <el-select v-model="newAppointment.revisitRecordId" :disabled="!newAppointment.patientId"
                       :loading="revisitRecordLoading"
                       :placeholder="newAppointment.patientId ? '选择原病历' : '请先选择患者'"
                       class="w-full">
              <el-option v-for="r in revisitRecords" :key="r.id" :label="revisitRecordLabel(r)" :value="r.id"/>
            </el-select>
          </el-form-item>
        </div>
        <!-- 「查过了但没病历」要说清是没病历，不是没选（否则人会回去反复点患者） -->
        <p v-if="newAppointment.patientId && !revisitRecordLoading && !revisitRecords.length"
           class="mb-4 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
          该患者还没有可关联的就诊病历 —— 请改为初诊，或由医生在诊室里开「当日回诊」
        </p>
      </template>

      <!-- 当日回诊是同一次挂号的延续：不占号源，也就不需要科室 / 日期 / 号源 / 时段 -->
      <template v-if="!isSameDayReturn">
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="选择科室" required>
            <el-select v-model="newAppointment.deptId" class="w-full" filterable placeholder="选择科室"
                       @change="handleDeptChange">
              <el-option v-for="d in departments" :key="d.id"
                         :label="d.parentDeptName ? `${d.parentDeptName} / ${d.deptName}` : d.deptName"
                         :value="d.id ?? d.deptName"/>
            </el-select>
          </el-form-item>
          <el-form-item label="预约日期" required>
            <el-date-picker
                v-model="newAppointment.visitDate"
                :disabled-date="(time: Date) => time.getTime() < Date.now() - 86400000"
                class="w-full"
                placeholder="选择日期"
                type="date"
                value-format="YYYY-MM-DD"
                @change="handleDateChange"
            />
          </el-form-item>
        </div>
        <el-form-item label="选择号源" required>
          <el-select v-model="newAppointment.scheduleId" :disabled="!availableSchedules.length" :placeholder="schedulePlaceholder"
                     class="w-full" @change="handleScheduleChange">
            <el-option
                v-for="s in availableSchedules"
                :key="s.id"
                :label="`${s.doctorName} | ${s.roomName || '诊室待定'} | ${s.startTime}-${s.endTime} | 余号:${s.availableSource} | 挂号费:¥${s.registFee}`"
                :value="s.id ?? s.doctorName"
            />
          </el-select>
          <!-- 「查过了但没号」由 placeholder 说（schedulePlaceholder），这里不再重复第二遍 -->
        </el-form-item>
        <el-form-item label="时间段（半小时一档）">
          <!-- 号源事实在段上：选段 = 定下这半小时的号；余 0 的段禁用，段已满提示换相邻段 -->
          <el-select v-model="newAppointment.slotId" :disabled="!slotRows.length" :loading="slotLoading" class="w-full"
                     clearable placeholder="请选择时间段">
            <el-option v-for="s in slotRows" :key="s.id" :disabled="s.status !== 1 || s.availableSource <= 0" :label="slotLabel(s)"
                       :value="s.id"/>
          </el-select>
          <p v-if="newAppointment.scheduleId && !slotRows.length && !slotLoading"
             class="w-full text-xs text-slate-400 mt-1">该号源暂无时间段明细，将按整班次号源挂号</p>
        </el-form-item>
      </template>
      <div v-if="isRevisit && (revisitPreview || revisitPreviewLoading)"
           class="mb-4 rounded-lg border border-slate-200 bg-slate-50 px-3 py-2">
        <div class="flex items-center justify-between text-sm">
          <span class="text-slate-500">复诊应收</span>
          <span :class="Number(revisitPreview?.totalFee) > 0 ? 'text-red-500' : 'text-emerald-600'"
                class="font-mono text-lg font-bold">
            {{ revisitPreviewLoading ? '计算中…' : `¥${Number(revisitPreview?.totalFee || 0).toFixed(2)}` }}
          </span>
        </div>
        <div v-if="revisitPreview && !revisitPreviewLoading" class="mt-1 text-xs text-slate-500">
          挂号费 ¥{{ Number(revisitPreview.registFee || 0).toFixed(2) }} ·
          诊查费 ¥{{ Number(revisitPreview.diagnosisFee || 0).toFixed(2) }} ·
          {{ revisitPreview.policyName ? `命中策略「${revisitPreview.policyName}」` : '未命中策略，按全额收费' }}
        </div>
        <p v-if="revisitPreview?.reason && !revisitPreviewLoading" class="mt-1 text-xs text-slate-400">
          {{ revisitPreview.reason }}
        </p>
      </div>
      <el-form-item label="结算方式" required>
        <el-select v-model="newAppointment.settlementType" class="w-full" placeholder="选择结算方式">
          <el-option v-for="item in settlementTypes" :key="item.value" :label="item.label" :value="item.value"/>
        </el-select>
      </el-form-item>
      <div v-if="newAppointment.settlementType !== 1" class="rounded-lg border border-blue-200 bg-blue-50 p-3">
        <div class="grid grid-cols-2 gap-4">
          <el-form-item class="!mb-0" label="医保类型">
            <el-select v-model="newAppointment.medicalInsuranceType" class="w-full" filterable
                       placeholder="请选择医保类型">
              <el-option v-for="item in medicalInsuranceTypes" :key="item.dictValue" :label="item.dictLabel"
                         :value="item.dictValue ?? item.dictLabel"/>
            </el-select>
          </el-form-item>
          <el-form-item class="!mb-0" label="医保卡号">
            <el-input v-model="newAppointment.medicalInsuranceNo" placeholder="请输入医保卡号"/>
          </el-form-item>
        </div>
      </div>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="'opd:appointments:add'" :loading="addSubmitLoading" type="primary" @click="handleSubmit">
        确认挂号
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {
  createRegistration,
  getAvailableSchedule,
  getRevisitRecordSelectList,
  getScheduleSlots,
  previewRevisitFee
} from '@/api/appoint';
import {getBillDetailById} from '@/api/settlementBill';
import {createPatient} from '@/api/patient';
import {getDictDataList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {VISIT_TYPE} from '@/lib/dict';
import {
  birthDateFromIdCard,
  isIdCardBirthDateLegal,
  isIdCardChecksumLegal,
  isIdCardFormatLegal,
  isPatientGenderCollected,
  isPhoneLegal,
  PATIENT_GENDER_OPTIONS,
  patientGenderSymbol
} from '@/lib/patientGender';
import {PATIENT_TYPE_OPTIONS, patientTypeToSettlementType} from '@/lib/patientType';
import {REVISIT_SOURCE, revisitNeedsNoSchedule} from '@/lib/revisitPolicy';
import {isAllDept, settlementTypes, slotLabel} from '@/lib/appointments';
import PatientSelect from '@/components/his/PatientSelect.vue';

// 新增挂号弹窗（从 AppointmentsView 拆出）。
// 号源余量、占号、幂等的事实仍由提交本身把关：这里只是把表单 UI 与校验收拢到一处。
// seed = 看板带进来的预填（科室/日期/号源/段/医生），为空 = 工具栏「新增挂号」的空白开单。
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 科室候选（页面级参照数据，主页面按 deptType:1 加载后传入） */
  departments: {type: Array, default: () => []},
  /** 复诊来源字典（主页面加载一次，挂号记录列表的「来源」文案也用它） */
  revisitSourceDict: {type: Array, default: () => []},
  /** 看板预填种子：{deptId, visitDate, scheduleId?, slotId?, doctorId?, snapshot?}；空 = 普通开单 */
  seed: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved', 'pay']);
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});
const addMode = ref('existing');
const addSubmitLoading = ref(false);
// 录入新患者表单（tab2 内联，不再是独立弹窗）
const newPatientForm = ref({
  patientName: '',
  // 不预设性别：默认成"男"等于静默编造性别
  gender: null,
  birthDate: '',
  phone: '',
  idCard: '',
  address: '',
  patientType: 1, // 1-自费 2-城镇职工医保 3-城乡居民医保 4-公费 5-其他（口径见 @/lib/patientType）
});
const availableSchedules = ref([]);
const newAppointment = ref({
  patientId: null,
  patientName: '',
  patientNo: '',
  // 仅用于「已选患者」回显头像，不参与提交
  patientGender: null,
  deptId: null,
  visitDate: '',
  scheduleId: null,
  // 仅用于「号源下拉自动带出」：预填医生后不必先猜对是哪个号源，见 openAddFromPanel
  doctorId: null,
  // 时间片段（biz_schedule_slot.id）：号源事实在段上，挂号必须选段扣段
  slotId: null,
  visitType: 1,
  // 复诊上下文（仅 visitType=2 有值）：来源决定占不占号源、按哪条策略收钱；
  // 原病历是策略判定（间隔天数、同医生/同科室）的基准，缺它判不出价。
  revisitSource: null,
  revisitRecordId: null,
  settlementType: 1,
  medicalInsuranceType: '',
  medicalInsuranceNo: '',
});
/**
 * 号源下拉的 placeholder —— 跟着真实状态走，不是一句写死的话。
 *
 * 三种态必须是三句话：还没填科室/日期 → 告诉人先填什么；查过了但没号 → 说清是"没号"不是"没填"；
 * 有号 → 提示直接选，不再提科室日期（否则用户会回去反复点科室找"是不是漏填了"）。
 */
const schedulePlaceholder = computed(() => {
  if (!newAppointment.value.deptId || !newAppointment.value.visitDate)
    return '请先选择科室和日期';
  if (availableSchedules.value.length)
    return '请选择号源';
  return '该日期无可挂号源';
});
/**
 * 时间片段选项：所选号源下的半小时段（biz_schedule_slot），号源与占用的事实都在段上。
 * 选号源后加载一次；段不存在（历史排班数据）时列表为空 → 段下拉禁用，走无段旧路径。
 */
const slotRows = ref([]);
const slotLoading = ref(false);
const loadSlotRows = async (scheduleId) => {
  slotRows.value = [];
  slotLoading.value = false;
  const id = availableSchedules.value.find((x) => (x.id ?? x.doctorName) === scheduleId)?.id;
  if (!id)
    return;
  slotLoading.value = true;
  try {
    const res = await getScheduleSlots(id);
    slotRows.value = res.data || [];
  } catch (error) {
    console.error('加载时间段失败:', error);
  } finally {
    slotLoading.value = false;
  }
};
/** 选号源 → 换段列表并清掉已选段（旧段属于上一个号源） */
const handleScheduleChange = (scheduleId) => {
  newAppointment.value.slotId = null;
  loadSlotRows(scheduleId);
};
// 医保类型字典（原来在主页面 onMounted 预加载，拆出后改为打开时懒加载，同一接口同一口径）
const medicalInsuranceTypes = ref([]);
const loadMedicalInsuranceTypes = async () => {
  try {
    const res = await getDictDataList(DICT_TYPE.MEDICAL_INSURANCE_TYPE);
    medicalInsuranceTypes.value = res.data || [];
  } catch (error) {
    console.error('加载医保类型字典失败:', error);
  }
};
// 患者搜索已抽成 PatientSelect 组件
const handlePatientSelect = (patient) => {
  if (!patient)
    return;
  newAppointment.value.patientId = patient.id;
  newAppointment.value.patientName = patient.patientName;
  newAppointment.value.patientNo = patient.patientNo;
  newAppointment.value.patientGender = patient.gender ?? null;
  // 自动带出结算方式：患者类型(patient_type) 与结算方式(settlement_type) 同构，
  // 「其他」在结算侧没有对应档位 → 回落自费（口径见 @/lib/patientType）
  if (patient.patientType) {
    newAppointment.value.settlementType = patientTypeToSettlementType(patient.patientType);
  }
  // 带出医保卡号和医保类型
  newAppointment.value.medicalInsuranceType = patient.medicalInsuranceType || '';
  newAppointment.value.medicalInsuranceNo = patient.medicalInsuranceNo || '';
  // 换人 = 换病历：原来选中的原病历属于上一个人，留着就会拿别人的就诊日判策略
  newAppointment.value.revisitRecordId = null;
  if (isRevisit.value) {
    revisitRecords.value = [];
    loadRevisitRecords();
  }
};
const WINDOW_REVISIT_SOURCES = [REVISIT_SOURCE.SAME_DAY_RETURN, REVISIT_SOURCE.DOCTOR_ORDERED];
const windowRevisitSourceOptions = computed(() => (props.revisitSourceDict || []).filter((o) => WINDOW_REVISIT_SOURCES.includes(Number(o.dictValue))));
const revisitRecords = ref([]);
const revisitRecordLoading = ref(false);
const revisitPreview = ref(null);
const revisitPreviewLoading = ref(false);
const isRevisit = computed(() => Number(newAppointment.value.visitType) === 2);
/**
 * 当日回诊 = 同一次挂号的延续（拿检查结果回来复看）：不占号源，
 * 所以科室 / 日期 / 号源 / 时段整块都不填 —— 强行让人选号源，等于把一次就诊拆成两个号重复收费。
 */
const isSameDayReturn = computed(() => isRevisit.value && revisitNeedsNoSchedule(newAppointment.value.revisitSource));
const revisitRecordLabel = (r) => `${r.visitDate || '日期未知'} ${r.deptName || '-'} / ${r.doctorName || '-'}${r.diagnosisName ? ` | ${r.diagnosisName}` : ''}`;
const loadRevisitRecords = async () => {
  const patientId = newAppointment.value.patientId;
  if (!patientId)
    return;
  revisitRecordLoading.value = true;
  try {
    const res = await getRevisitRecordSelectList(patientId);
    revisitRecords.value = res.data || [];
  } catch (error) {
    console.error('加载复诊原病历失败:', error);
  } finally {
    revisitRecordLoading.value = false;
  }
};
let previewToken = 0;
watch(() => [newAppointment.value.visitType, newAppointment.value.revisitSource,
  newAppointment.value.revisitRecordId, newAppointment.value.scheduleId, newAppointment.value.patientId], async () => {
  const a = newAppointment.value;
  const token = ++previewToken;
  const ready = isRevisit.value && a.patientId && a.revisitSource && a.revisitRecordId
      && (isSameDayReturn.value || a.scheduleId);
  if (!ready) {
    revisitPreview.value = null;
    revisitPreviewLoading.value = false;
    return;
  }
  revisitPreviewLoading.value = true;
  try {
    const res = await previewRevisitFee({
      patientId: a.patientId,
      revisitSource: a.revisitSource,
      revisitRecordId: a.revisitRecordId,
      scheduleId: isSameDayReturn.value ? undefined : a.scheduleId,
    });
    if (token !== previewToken)
      return;
    revisitPreview.value = res.data || null;
  } catch (error) {
    if (token !== previewToken)
      return;
    revisitPreview.value = null;
    ElMessage.error(error?.message || '复诊费用预估失败');
  } finally {
    if (token === previewToken)
      revisitPreviewLoading.value = false;
  }
});
/** 切就诊类型：离开复诊就把复诊上下文清空，否则上一单选过的原病历会带着判定进下一张单 */
const handleVisitTypeChange = () => {
  if (!isRevisit.value) {
    newAppointment.value.revisitSource = null;
    newAppointment.value.revisitRecordId = null;
    return;
  }
  if (!newAppointment.value.revisitSource)
    newAppointment.value.revisitSource = REVISIT_SOURCE.SAME_DAY_RETURN;
  loadRevisitRecords();
};
// ========== 新建患者 ==========
/** 切换患者来源 tab；切到「录入新患者」时清空表单（避免上一单填的残留带过来） */
const switchPatientMode = (mode) => {
  addMode.value = mode;
  if (mode === 'new') {
    resetNewPatientForm();
    // 刚建档的人没有任何就诊记录，复诊无从关联（后端也会以「原病历不属于该患者」拒掉）
    if (isRevisit.value) {
      newAppointment.value.visitType = 1;
      newAppointment.value.revisitSource = null;
      newAppointment.value.revisitRecordId = null;
    }
  }
};
const resetNewPatientForm = () => {
  newPatientForm.value = {
    patientName: '',
    gender: null,
    birthDate: '',
    phone: '',
    idCard: '',
    address: '',
    patientType: 1,
  };
};
/** 身份证填完 → 把出生日期带出来（已手填过就不覆盖），与患者管理页同一口径 */
const syncPatientBirthDate = () => {
  if (newPatientForm.value.birthDate)
    return;
  const d = birthDateFromIdCard(newPatientForm.value.idCard);
  if (d)
    newPatientForm.value.birthDate = d;
};
/**
 * 校验「录入新患者」表单，不通过则提示并返回 false。
 *
 * 建档口径与患者管理页一致：姓名 / 性别 / 身份证必填，手机号"填了必须合法、空着放行"。
 * 这里比急诊登记严 —— 急诊面向三无患者（身份证可能根本要不到），门诊挂号窗口患者清醒、
 * 能出示证件，是补全主档成本最低的时机（身份证也是 EMPI 去重的匹配键）。
 */
const validateNewPatientForm = () => {
  if (!newPatientForm.value.patientName) {
    ElMessage.warning('请输入患者姓名');
    return false;
  }
  if (!isPatientGenderCollected(newPatientForm.value.gender)) {
    ElMessage.warning('请选择性别（确实没问到请选「未知」）');
    return false;
  }
  if (!newPatientForm.value.idCard) {
    ElMessage.warning('请输入身份证号');
    return false;
  }
  if (!isIdCardFormatLegal(newPatientForm.value.idCard)) {
    ElMessage.warning('身份证号格式不正确：应为 18 位（末位可为 X）');
    return false;
  }
  // 出生日期单独判：校验位对了不代表日期存在（19990230 这种），提示要指到点子上
  if (!isIdCardBirthDateLegal(newPatientForm.value.idCard)) {
    ElMessage.warning('身份证号中的出生日期不存在，请核对');
    return false;
  }
  if (!isIdCardChecksumLegal(newPatientForm.value.idCard)) {
    ElMessage.warning('身份证号校验位不正确，请核对');
    return false;
  }
  if (newPatientForm.value.phone && !isPhoneLegal(newPatientForm.value.phone)) {
    ElMessage.warning('手机号格式不正确：应为 11 位手机号');
    return false;
  }
  return true;
};
const handleDeptChange = () => {
  newAppointment.value.scheduleId = null;
  newAppointment.value.slotId = null;
  slotRows.value = [];
  availableSchedules.value = [];
  loadAvailableSchedules();
};
const handleDateChange = () => {
  newAppointment.value.scheduleId = null;
  newAppointment.value.slotId = null;
  slotRows.value = [];
  availableSchedules.value = [];
  loadAvailableSchedules();
};
const loadAvailableSchedules = async () => {
  if (!newAppointment.value.deptId || !newAppointment.value.visitDate)
    return;
  try {
    const res = await getAvailableSchedule(newAppointment.value.deptId, newAppointment.value.visitDate);
    availableSchedules.value = res.data || [];
  } catch (error) {
    console.error('加载号源失败:', error);
  }
};
const handleSubmit = async () => {
  // ① 患者：按 tab 分支校验
  if (addMode.value === 'existing') {
    if (!newAppointment.value.patientId) {
      ElMessage.warning('请选择患者');
      return;
    }
  } else if (!validateNewPatientForm()) {
    return;
  }
  // ② 复诊：来源与原病历必填（当日回诊免掉科室/日期/号源，但这两项是策略判定的基准，不能省）
  if (isRevisit.value) {
    if (!newAppointment.value.revisitSource) {
      ElMessage.warning('请选择复诊来源');
      return;
    }
    if (!newAppointment.value.revisitRecordId) {
      ElMessage.warning('请选择原病历（复诊要关联的那一次就诊）');
      return;
    }
  }
  // ③ 共享字段（两个 tab 共用同一张表单，都要校验）；当日回诊不占号源，整块跳过
  if (!isSameDayReturn.value) {
    if (!newAppointment.value.deptId) {
      ElMessage.warning('请选择科室');
      return;
    }
    if (!newAppointment.value.visitDate) {
      ElMessage.warning('请选择预约日期');
      return;
    }
    if (!newAppointment.value.scheduleId) {
      ElMessage.warning('请选择号源');
      return;
    }
    // 号源事实在段上：有段可选时必须选段（后端按段扣号源），不选段会退化为只扣主表的旧路径
    if (slotRows.value.length && !newAppointment.value.slotId) {
      ElMessage.warning('请选择时间段');
      return;
    }
  }
  addSubmitLoading.value = true;
  try {
    let patientId = newAppointment.value.patientId;
    let patientName = newAppointment.value.patientName;
    // ④ 录入新患者：先建档拿 ID，再挂号 —— 一次点击完成两步。
    // 顺序不能反：挂号接口的 patientId 是 @NotNull，必须先有患者主键。
    // 这不等于数据库事务级原子（页面层两次调用）：若建档成功而挂号失败（如号源被抢空），
    // 患者档案会留下 —— 这里刻意**不**回滚，改个号源再点一次即可，不用重填身份证；
    // 患者主档本身也是独立有效的资产（患者管理页单独建档同样是常态）。
    if (addMode.value === 'new') {
      const created = await createPatient(newPatientForm.value);
      const np = created.data;
      if (!np?.id) {
        throw new Error('患者已创建但未返回患者ID，请到「患者管理」确认后重新挂号');
      }
      patientId = np.id;
      patientName = np.patientName || newPatientForm.value.patientName;
    }
    const res = await createRegistration({
      patientId: patientId,
      patientName: patientName,
      // 当日回诊即使是从看板某一格开单（那里会预填号源），也必须清空：
      // 它不占号源，后端拿到 scheduleId 会以「请勿为当日回诊选择号源」拒掉
      scheduleId: isSameDayReturn.value ? null : newAppointment.value.scheduleId,
      slotId: isSameDayReturn.value ? null : (newAppointment.value.slotId || undefined),
      visitType: newAppointment.value.visitType,
      revisitSource: isRevisit.value ? newAppointment.value.revisitSource : undefined,
      revisitRecordId: isRevisit.value ? newAppointment.value.revisitRecordId : undefined,
      settlementType: newAppointment.value.settlementType,
      medicalInsuranceType: newAppointment.value.medicalInsuranceType,
      medicalInsuranceNo: newAppointment.value.medicalInsuranceNo,
    });
    ElMessage.success(addMode.value === 'new' ? '患者建档并挂号成功' : '挂号成功');
    visible.value = false;
    const data = res.data || {};
    // 查询账单：金额用来弹缴费窗，状态用来决定**要不要**弹
    let amount = 0;
    let billStatus = null;
    // 免收：后端不记账不出账（billId 为 null），直接提示不弹窗
    if (!data.billId) {
      ElMessage.success(isSameDayReturn.value
          ? '当日回诊免收挂号费，患者可直接签到就诊'
          : '该号按收费策略免收，无需缴费，患者可直接签到');
      emit('saved');
      resetForm();
      return;
    }
    try {
      const billRes = await getBillDetailById(data.billId);
      const bill = billRes.data || {};
      amount = bill.unpaidAmount ?? bill.payableAmount ?? 0;
      billStatus = bill.billStatus ?? null;
    } catch (e) {
      console.error('获取账单详情失败:', e);
    }
    // 账单已付清：再弹一次缴费窗等于让人对一笔已平的账重复支付
    if (Number(billStatus) === 3) {
      ElMessage.success('该号已缴费，无需重复支付');
      emit('saved');
      resetForm();
      return;
    }
    // 收款弹窗（QrPayDialog，共享组件）留在主页面：这里只把账单事实交回去
    emit('pay', {
      registNo: data.registNo || '',
      patientName: patientName,
      amount: amount,
      registId: data.id || null,
      billId: data.billId || null,
      billNo: data.billNo || '',
    });
    emit('saved');
    resetForm();
  } catch (error) {
    ElMessage.error(error.message || '挂号失败');
  } finally {
    addSubmitLoading.value = false;
  }
};
const resetForm = () => {
  newAppointment.value = {
    patientId: null,
    patientName: '',
    patientNo: '',
    patientGender: null,
    deptId: null,
    visitDate: '',
    scheduleId: null,
    doctorId: null,
    slotId: null,
    visitType: 1,
    revisitSource: null,
    revisitRecordId: null,
    settlementType: 1,
    medicalInsuranceType: '',
    medicalInsuranceNo: '',
  };
  availableSchedules.value = [];
  slotRows.value = [];
  revisitRecords.value = [];
  // 上一单的预估金额留在屏上，会被当成这一单要收的钱
  revisitPreview.value = null;
  revisitPreviewLoading.value = false;
  // 关掉弹窗后回到「选择已有患者」，并把新患者表单清空（否则下次打开带出上一单的姓名/身份证）
  addMode.value = 'existing';
  resetNewPatientForm();
};
/** 号源被抢空 / 停诊后的兜底文案；返回 true 表示「该把号源下拉重置掉」 */
const warnSeedScheduleStale = (seedScheduleId) => {
  const exist = availableSchedules.value.find((x) => x.id === seedScheduleId);
  if (exist)
    return false;
  newAppointment.value.scheduleId = null;
  ElMessage.warning('该号源已变更或约满，请重新选择号源');
  return true;
};
const fetchSchedulesForSeed = async (seed) => {
  const visitDate = seed.visitDate;
  const deptId = isAllDept(seed.deptId) ? null : seed.deptId;
  if (!deptId || !visitDate)
    return;
  const snapshot = seed.snapshot;
  const scheduleId = seed.scheduleId;
  if (snapshot?.scheduleDate === visitDate) {
    // 面板缓存里这一天的号源：「选中一条」靠它定位，下拉本身还是走接口要最新的
    const cached = snapshot.list || [];
    if (!cached.length) {
      ElMessage.warning('该科室该日期无可挂号源');
      return;
    }
    // ⚠️ 必须 await：段列表要跟着号源走（先定号源再选段），不 await 的话
    //    调用方拿到的还是空号源列表，段也就选不上（表现为"点 9 点那格，弹窗里时间段是空的"）
    await loadAvailableSchedules();
    warnSeedScheduleStale(scheduleId);
    return;
  }
  await loadAvailableSchedules();
};
const openAddFromPanel = async (seed = {}) => {
  resetForm();
  newAppointment.value.deptId = isAllDept(seed.deptId) ? null : (seed.deptId ?? null);
  newAppointment.value.visitDate = seed.visitDate || '';
  newAppointment.value.doctorId = seed.doctorId ?? null;
  await fetchSchedulesForSeed(seed);
  // 医生列点了「新增」但没指定具体号源：把这位医生的号预先选上（当天多条排班则留给用户挑）
  if (!newAppointment.value.scheduleId && newAppointment.value.doctorId) {
    const hit = availableSchedules.value.filter((s) => s.doctorId === newAppointment.value.doctorId);
    if (hit.length === 1)
      newAppointment.value.scheduleId = hit[0].id;
  }
  // 面板点的是某一格（日视图 = 某半小时档）：段列表跟着号源走，必须先定号源再选段，顺序不能反。
  // 段可能已被排班重建删掉 → 校验它还在新列表里，不在就不预选（让人自己挑，不静默挂到别的档）。
  if (seed.slotId && newAppointment.value.scheduleId) {
    await loadSlotRows(newAppointment.value.scheduleId);
    if (slotRows.value.some((s) => String(s.id) === String(seed.slotId))) {
      newAppointment.value.slotId = seed.slotId;
    }
  }
};
watch(visible, (open) => {
  if (!open)
    return;
  if (!medicalInsuranceTypes.value.length)
    loadMedicalInsuranceTypes();
  // 看板开单：seed 里有科室+日期（有时还有号源/段/医生），走原来的预填链路；
  // 工具栏「新增挂号」没有 seed，开一单干净的空表。
  if (props.seed)
    void openAddFromPanel(props.seed);
  else
    resetForm();
});
</script>
