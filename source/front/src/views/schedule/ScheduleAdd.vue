<template>
  <!-- 新增排班（批量：医生多选 × 日期区间）与修改排班（单条）是两个弹窗。
       以前一个弹窗靠 v-if="isEdit" 分叉，两套排版挤在一起，改一边就碰坏另一边。 -->
  <el-dialog v-model="visible" destroy-on-close title="新增排班" width="980px">
    <el-form :model="formData" label-position="left">
      <div class="grid grid-cols-3 gap-4">
        <el-form-item label="科室" required>
          <el-select v-model="formData.deptId" class="w-full" filterable placeholder="输入科室名称搜索"
                     @change="handleDeptChange">
            <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id"/>
          </el-select>
        </el-form-item>
        <!-- 岗位类别：排班对象不再只有医生（sql/195）。
             只有「医生」才配号源/诊室/门诊诊查费，换岗位要把人一起重选（不同岗位是不同的人）。 -->
        <el-form-item label="岗位类别" required>
          <el-select v-model="formData.staffType" class="w-full" data-testid="form-staff-type"
                     @change="handleStaffTypeChange">
            <el-option v-for="o in STAFF_TYPE_DOCTOR_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
        <el-form-item :label="`${staffTypeText(formData.staffType)}人员`" required>
          <el-select v-model="formData.doctorIds" :disabled="!formData.deptId" :max-collapse-tags="1"
                     :placeholder="`选择${staffTypeText(formData.staffType)}人员（可多选，一次排完）`" class="w-full"
                     collapse-tags collapse-tags-tooltip
                     data-testid="form-doctor" multiple @change="ensureDoctorRows">
            <el-option v-for="e in employees" :key="e.id" :label="`${e.empName} - ${getTitleLabel(titleMap, e.title)}`"
                       :value="e.id"/>
          </el-select>
        </el-form-item>
      </div>
      <p v-if="!staffTypeHasSource(formData.staffType)" class="-mt-2 mb-3 text-xs text-slate-500">
        {{ staffTypeText(formData.staffType) }}岗位是<b>出勤排班</b>：只排「谁哪天哪个班次在岗」，不放号、不需要诊室、不计门诊诊查费。
      </p>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item data-testid="form-date-range" label="排班日期" required>
          <el-date-picker
              v-model="formData.scheduleDateRange"
              :disabled-date="(time: Date) => time.getTime() < Date.now() - 86400000"
              :shortcuts="dateRangeShortcuts"
              class="!w-full"
              end-placeholder="结束日期"
              range-separator="至"
              start-placeholder="开始日期"
              type="daterange"
              value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="班次" required>
          <el-select v-model="shiftDictId" class="w-full" data-testid="form-shift-dict"
                     filterable placeholder="选择班次（时间段由班次带出）"
                     @change="applyShiftDict">
            <el-option v-for="s in shiftDictOptions" :key="s.id" :label="shiftDictLabel(s)" :value="s.id"/>
          </el-select>
        </el-form-item>
      </div>
      <div class="mb-3 -mt-1 text-xs text-slate-500">
        {{ batchPlanDesc }} · 时间段 {{ formTimeRange || '待选班次' }}（不可手填：统计按班次口径出，需要的时段先到「班次字典」建一条）
      </div>

      <!-- 默认值：只用来给「新加进来的人」补一行，以及一键覆盖已有行。
           号源/费用/预约是「人员 × 班次」级的值，不该整批共用一个数 —— 见下方逐人表格。
           只对医生岗有意义：出勤岗没有号源与费用，整块不显示（免得填了被后端清零还以为存上了）。 -->
      <div v-if="staffTypeHasSource(formData.staffType)"
           class="mb-3 rounded-lg border border-slate-200 bg-slate-50 p-3">
        <div class="mb-2 flex items-center justify-between">
          <span class="text-sm font-medium text-slate-700">默认值（新加入的人员按此补一行）</span>
          <el-button :disabled="!(formData.doctorIds || []).length" data-testid="apply-defaults" size="small"
                     @click="applyDefaultsToAllRows">应用到全部人员
          </el-button>
        </div>
        <div class="grid grid-cols-6 gap-3">
          <el-form-item label="号源数量">
            <el-input-number v-model="formData.totalSource" :max="999" :min="1" class="!w-full"
                             controls-position="right"/>
          </el-form-item>
          <el-form-item label="门诊诊查费(元)">
            <el-input-number v-model="formData.diagnosisFee" :min="0" :precision="2" class="!w-full"
                             controls-position="right"/>
            <div class="text-xs text-slate-500 mt-1">
              湘医保发〔2025〕58号：挂号费已废止并入门诊诊查费。长沙市基准价 9 元，知名专家加收 61 元（合计 70 元）。
            </div>
          </el-form-item>
          <el-form-item label="专家号">
            <el-switch v-model="formData.isExpert" :active-value="1" :inactive-value="0"/>
          </el-form-item>
          <el-form-item v-if="formData.isExpert === 1" label="专家费(元)">
            <el-input-number v-model="formData.expertFee" :min="0" :precision="2" class="!w-full"
                             controls-position="right"/>
          </el-form-item>
          <el-form-item label="开放预约">
            <el-switch v-model="formData.isAppointment" :active-value="1" :inactive-value="0"/>
          </el-form-item>
          <el-form-item v-if="formData.isAppointment === 1" label="预约号源">
            <el-input-number v-model="formData.appointmentSource" :max="formData.totalSource" :min="0"
                             class="!w-full" controls-position="right"/>
          </el-form-item>
        </div>
        <p class="text-xs text-slate-400">预约号源 = 划给网上预约的专用号，现场窗口不可占用；0 = 不划池</p>
      </div>

      <!-- 医生 × 排班参数：一人一行。诊室必须一人一间（排队号前缀按诊室生成、大屏按诊室导诊，
           两位医生同一时段挤一间 = 患者跑错房间）；号源与费用按人可不同。 -->
      <div v-if="(formData.doctorIds || []).length" class="mb-3 rounded-lg border border-slate-200 p-3"
           data-testid="doctor-room-map">
        <div class="mb-1 text-sm font-medium text-slate-700">
          {{
            staffTypeHasSource(formData.staffType)
                ? '人员 × 排班参数（一人一行，诊室不够就留空，提交前会拦）'
                : `本次将为以下 ${(formData.doctorIds || []).length} 位${staffTypeText(formData.staffType)}人员建立出勤排班`
          }}
        </div>
        <el-table
            :data="(formData.doctorIds || []).map((id: any) => ({ id, room: formData.doctorRows[String(id)] || doctorRowDefaults() }))"
            max-height="240"
            size="small">
          <el-table-column label="人员" width="110">
            <template #default="{ row }">
              <span class="truncate text-sm text-slate-700">{{ doctorNameOf(row.id) }}</span>
            </template>
          </el-table-column>
          <!-- 号源/诊室/费用只对医生岗有意义：出勤岗整块不显示 -->
          <el-table-column v-if="staffTypeHasSource(formData.staffType)" label="诊室" width="140">
            <template #default="{ row }">
              <el-select v-model="row.room.roomId" class="!w-full" clearable placeholder="选择诊室" size="small">
                <el-option v-for="r in clinicRooms" :key="r.id" :label="r.name" :value="r.id"/>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column v-if="staffTypeHasSource(formData.staffType)" label="号源" width="100">
            <template #default="{ row }">
              <el-input-number v-model="row.room.totalSource" :controls="false" :max="999" :min="1" class="!w-full"
                               size="small"/>
            </template>
          </el-table-column>
          <el-table-column v-if="staffTypeHasSource(formData.staffType)" label="门诊诊查费" width="120">
            <template #default="{ row }">
              <el-input-number v-model="row.room.diagnosisFee" :controls="false" :min="0" :precision="2"
                               class="!w-full"
                               size="small"/>
            </template>
          </el-table-column>
          <el-table-column v-if="staffTypeHasSource(formData.staffType)" align="center" label="专家号" width="80">
            <template #default="{ row }">
              <el-switch v-model="row.room.isExpert" :active-value="1" :inactive-value="0" size="small"/>
            </template>
          </el-table-column>
          <el-table-column v-if="staffTypeHasSource(formData.staffType)" label="专家费" width="100">
            <template #default="{ row }">
              <el-input-number v-model="row.room.expertFee" :controls="false" :disabled="row.room.isExpert !== 1"
                               :min="0" :precision="2"
                               class="!w-full" size="small"/>
            </template>
          </el-table-column>
          <el-table-column v-if="staffTypeHasSource(formData.staffType)" align="center" label="开放预约" width="90">
            <template #default="{ row }">
              <el-switch v-model="row.room.isAppointment" :active-value="1" :inactive-value="0" size="small"/>
            </template>
          </el-table-column>
          <el-table-column v-if="staffTypeHasSource(formData.staffType)" label="预约号源" min-width="100">
            <template #default="{ row }">
              <el-input-number v-model="row.room.appointmentSource" :controls="false"
                               :disabled="row.room.isAppointment !== 1"
                               :max="row.room.totalSource"
                               :min="0" class="!w-full" size="small"/>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-form-item label="状态">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">正常</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'outpatient:schedule:add'" :loading="submitLoading" data-testid="add-submit" type="primary"
                 @click="handleSubmit">确定
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {createScheduleBatch, getShiftSelectList} from '@/api/appoint';
import {getClinicRoomListAll, getEmployeeList} from '@/api/system';
import {SHIFT_SCOPE_OUTPATIENT, STAFF_TYPE_DOCTOR, STAFF_TYPE_DOCTOR_OPTIONS, staffTypeHasSource, staffTypeText} from '@/lib/scheduleShift';
import {batchDays, fmtISO} from '@/lib/scheduleDate';
import {expertDefaultOf, getTitleLabel} from '@/lib/scheduleTitle';

/**
 * 新增排班（批量：人员多选 × 日期区间，人 × 日一次请求）。
 * 与「修改排班」是两个组件：新增的人员是多选、日期是区间、号源费用按人一行；
 * 修改是单条、人员单选、日期一天 —— 字段与版式都不同，不合并。
 *
 * 科室列表由页面加载一次 prop 下发（面板筛选与这里同一份）；
 * 人员/诊室/班次是**表单专用候选**，在本组件里自加载，页面不再持有。
 */
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 打开时预填：面板空格子点「加排班」带进来的 科室/人员/日期；从「新增排班」按钮打开时只带岗位类别
  seed: {type: Object, default: null},
  departments: {type: Array, default: () => []},
  titleMap: {type: Object, default: () => ({})},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const employees = ref([]);
const clinicRooms = ref([]);
const submitLoading = ref(false);
const formData = ref({});

/** 今天（YYYY-MM-DD）：日期不可写过去的判定基准，ISO 串可直接字典序比较 */
const todayStr = computed(() => fmtISO(new Date()));

const dateRangeShortcuts = [
  {
    text: '今日',
    value: () => {
      const today = new Date();
      const dateStr = fmtISO(today);
      return [dateStr, dateStr];
    }
  },
  {
    text: '本周',
    value: () => {
      const today = new Date();
      const day = today.getDay();
      const diff = day === 0 ? 6 : day - 1;
      const start = new Date(today);
      start.setDate(today.getDate() - diff);
      const end = new Date(start);
      end.setDate(start.getDate() + 6);
      return [fmtISO(start), fmtISO(end)];
    }
  },
  {
    text: '下周',
    value: () => {
      const today = new Date();
      const day = today.getDay();
      const diff = day === 0 ? 6 : day - 1;
      const start = new Date(today);
      start.setDate(today.getDate() - diff + 7);
      const end = new Date(start);
      end.setDate(start.getDate() + 6);
      return [fmtISO(start), fmtISO(end)];
    }
  },
  {
    text: '本月',
    value: () => {
      const today = new Date();
      const first = new Date(today.getFullYear(), today.getMonth(), 1);
      const last = new Date(today.getFullYear(), today.getMonth() + 1, 0);
      return [fmtISO(first), fmtISO(last)];
    }
  }
];

const resetForm = (seed = {}) => {
  formData.value = {
    id: null,
    deptId: seed.deptId ?? null,
    deptName: seed.deptName ?? '',
    roomId: null,
    roomName: '',
    doctorId: seed.doctorId ?? null,
    doctorName: seed.doctorName ?? '',
    /**
     * 岗位类别（sql/195）：排班对象不再只有医生。
     * 只有 1-医生 才配号源/诊室/门诊诊查费/预约池；其余岗位是纯出勤排班
     * （提交时这些字段由后端一律清零，界面同样不显示，免得填了被静默丢弃还以为存上了）。
     */
    // 默认落在面板当前看的岗位（seed 给）：排班员切到「护理」再点新增，想排的就是护理班；
    // seed 没给时兜底成医生（与 lib/scheduleShift 的 STAFF_TYPE_DOCTOR 同一口径）
    staffType: seed.staffType ?? STAFF_TYPE_DOCTOR,
    /** 批量排班：新增时可多选医生（同科室多位医生一次排完）；编辑时长度恒为 1 */
    doctorIds: seed.doctorIds ? [...seed.doctorIds] : [],
    /**
     * 医生 → 该医生的排班参数（诊室/号源/费用/预约）。
     * 这些是「医生 × 班次」级的值：整批共用一个值，挂号台按条读出来就是同质数据
     * （五位医生号源都是 20、挂号费都是 0，看着像数据坏了）。所以每人一行可覆盖，
     * 表单顶上的同名字段在新增弹窗里只当「默认值」，改默认值不回刷已生成的行（要点「应用到全部」）。
     */
    doctorRows: {},
    scheduleDate: '',
    /** 批量日期范围（仅新增弹窗）：新增只认这个区间，排一天就起止填同一天 */
    scheduleDateRange: seed.scheduleDateRange ? [...seed.scheduleDateRange] : [],
    /** 班次ID（biz_shift.id）：排班唯一的时间段/班别来源，「班次」下拉选中才有值 */
    shiftId: null,
    startTime: '',
    endTime: '',
    totalSource: 20,
    // 已挂数：只给修改弹窗当号源下限用（总数不能小于已挂），提交时后端不认它
    usedSource: 0,
    registFee: 0,
    diagnosisFee: 0,
    isExpert: 0,
    expertFee: 0,
    isAppointment: 0,
    appointmentSource: 0,
    status: 1,
  };
};
const doctorRowDefaults = () => ({
  roomId: null,
  totalSource: formData.value.totalSource,
  registFee: formData.value.registFee,
  diagnosisFee: formData.value.diagnosisFee,
  isExpert: formData.value.isExpert,
  expertFee: formData.value.expertFee,
  isAppointment: formData.value.isAppointment,
  appointmentSource: formData.value.appointmentSource,
});
/**
 * 批量排班（多医生）：每位医生一行排班参数，其中诊室必须一人一间。
 * 为什么不共用一间：排队号前缀按诊室编号生成、大屏按诊室导诊，两位医生同一时段挤一间 = 患者跑错房间。
 * 规则：已有的行原样保留（人手工改过的不被冲掉）；新进来的医生按当前「默认值」补一行，
 *       诊室按科室诊室列表顺序取空闲，不够就留空，提交前由校验拦（启用态必须有诊室）。
 */
const ensureDoctorRows = () => {
  const ids = formData.value.doctorIds || [];
  const rooms = clinicRooms.value || [];
  const prev = formData.value.doctorRows || {};
  const used = new Set(ids.map((id) => prev[String(id)]?.roomId).filter(Boolean));
  const map = {};
  for (const id of ids) {
    const key = String(id);
    const kept = prev[key];
    if (kept) {
      const roomStillValid = kept.roomId != null && rooms.some((r) => r.id === kept.roomId);
      map[key] = roomStillValid ? kept : {...kept, roomId: null};
      if (roomStillValid)
        used.add(kept.roomId);
      continue;
    }
    const row = doctorRowDefaults();
    // 新医生行的专家开关/专家费按该医生档案带出，而不是沿用顶部默认值
    const dft = expertDefaultOf(props.titleMap, employees.value.find((e) => String(e.id) === String(id)));
    if (dft) {
      row.isExpert = dft.isExpert;
      row.expertFee = dft.expertFee;
    }
    const free = rooms.find((r) => !used.has(r.id));
    if (free) {
      row.roomId = free.id;
      used.add(free.id);
    }
    map[key] = row;
  }
  formData.value.doctorRows = map;
};
/** 把顶部的「默认值」刷进每一行（诊室不动：一间房已经分好了，重刷会把人挪房） */
const applyDefaultsToAllRows = () => {
  const map = formData.value.doctorRows || {};
  for (const key of Object.keys(map)) {
    const {roomId, ...rest} = doctorRowDefaults();
    map[key] = {...map[key], ...rest};
  }
  formData.value.doctorRows = {...map};
  ElMessage.success(`已把默认值应用到 ${Object.keys(map).length} 位人员`);
};
/** 选医生（或科室诊室列表到位）都要重算分配 */
watch(() => formData.value.doctorIds, ensureDoctorRows, {deep: true});
watch(clinicRooms, ensureDoctorRows);
const doctorNameOf = (id) => (employees.value.find((e) => e.id === id)?.empName) || '';
const roomNameOf = (id) => (clinicRooms.value.find((r) => r.id === id)?.name) || '';
/** 新增批量实际会生成的条数：医生数 × 天数（天数只来自日期区间，排一天就是起止同一天） */
const batchPlanCount = computed(() => {
  const doctors = (formData.value.doctorIds || []).length || 0;
  const days = formData.value.scheduleDateRange?.length === 2 ? batchDays(formData.value.scheduleDateRange).length : 0;
  return doctors * days;
});
const batchPlanDesc = computed(() => {
  const days = formData.value.scheduleDateRange?.length === 2 ? batchDays(formData.value.scheduleDateRange).length : 0;
  const doctors = (formData.value.doctorIds || []).length || 0;
  return days && doctors
      ? `${doctors} 位${staffTypeText(formData.value.staffType)}人员 × ${days} 天 = ${batchPlanCount.value} 条` : '';
});
/** 表单里的时间段：只读展示，值由班次带出（手填时段等于绕开班次字典，统计口径就散了） */
const formTimeRange = computed(() => formData.value.startTime && formData.value.endTime
    ? `${formData.value.startTime}~${formData.value.endTime}` : '');

// ========== 班次字典（biz_shift）：排班的唯一时间段与班别来源 ==========
// 字典 id 是字符串（后端 BIGINT ToStringSerializer），v-model 用 string
const shiftDictOptions = ref([]);
const shiftDictId = ref(null);
/** 拉班次：选了科室只显示「该科室适用 + 全院通用」的启用班次；本页面只用门诊/急诊那半册 */
const loadShiftDict = async (deptId) => {
  try {
    const params = {status: 1, useScope: SHIFT_SCOPE_OUTPATIENT};
    if (deptId)
      params.deptId = deptId;
    const res = await getShiftSelectList(params);
    shiftDictOptions.value = res.data || [];
    return shiftDictOptions.value;
  } catch (error) {
    console.error('加载班次失败:', error);
    return [];
  }
};
const shiftDictLabel = (s) => `${s.shiftName} ${s.startTime}~${s.endTime}`;
/**
 * 选中班次：时间段由班次带出，表单里不再给改。
 * 需要非常规时段就到「班次字典」建一条，这里只负责选它。
 */
const applyShiftDict = (shiftId) => {
  const form = formData.value;
  const hit = shiftDictOptions.value.find((s) => String(s.id) === String(shiftId));
  if (!hit) {
    // 选项被清空只可能是班次停用/删除后从列表里消失了：把选中值一起清掉，别留一个提交必失败的假选中
    shiftDictId.value = null;
    form.shiftId = null;
    form.startTime = '';
    form.endTime = '';
    return;
  }
  form.shiftId = String(hit.id);
  form.startTime = hit.startTime;
  form.endTime = hit.endTime;
};

// ========== 表单候选：人员 / 诊室（本组件自加载，页面不再持有） ==========
/**
 * 拉「可排班人员」候选（sql/195 起不限医生：护士/技师/药师/收费员都能排）。
 *
 * <b>按岗位类别取人，不按 emp_type</b>：emp_type 是员工档案上的冗余分类，与岗位表
 * sys_employee_post 不一致（档案说护士、岗位表说收费员），传它会在名册里出现
 * 「选得到、排上去才发现岗位不对」的人。后端按 staffType 走「岗位表 → 角色 → 类别」，
 * 传了 staffType 时 deptId 也按岗位所在科室过滤（而不是员工档案的主科室快照）。
 */
const loadEmployees = async (deptId, staffType) => {
  try {
    const params = {staffType: staffType ?? STAFF_TYPE_DOCTOR};
    if (deptId)
      params.deptId = deptId;
    const res = await getEmployeeList(params);
    employees.value = res.data?.records || res.data || [];
  } catch (error) {
    console.error('加载排班人员列表失败:', error);
  }
};
const loadClinicRooms = async (deptId) => {
  try {
    const params = {status: 1};
    if (deptId)
      params.deptId = deptId;
    const res = await getClinicRoomListAll(params);
    clinicRooms.value = res.data?.records || res.data || [];
  } catch (error) {
    console.error('加载诊室列表失败:', error);
  }
};

watch(visible, (open) => {
  if (!open)
    return;
  const seed = props.seed || {};
  resetForm(seed);
  shiftDictId.value = null;
  // 人员候选按当前岗位类别拉：切到「护理」再点新增，下拉里就该是护士，不是上一次留下的医生
  if (seed.deptId) {
    loadEmployees(seed.deptId, formData.value.staffType);
    loadClinicRooms(seed.deptId);
    loadShiftDict(seed.deptId);
  } else {
    loadEmployees(undefined, formData.value.staffType);
    loadShiftDict(null);
  }
});

const handleDeptChange = (deptId) => {
  formData.value.doctorId = null;
  formData.value.doctorName = '';
  formData.value.doctorIds = [];
  formData.value.doctorRows = {};
  formData.value.roomId = null;
  formData.value.roomName = '';
  // 换科室：班次选项换成「新科室适用 + 通用」，原选中项可能不在新列表里 → 一律清掉重选
  shiftDictId.value = null;
  formData.value.shiftId = null;
  formData.value.startTime = '';
  formData.value.endTime = '';
  loadShiftDict(deptId);
  loadEmployees(deptId, formData.value.staffType);
  loadClinicRooms(deptId);
};
/**
 * 换岗位类别：换的是「规则链」，所以人必须重选（原来选的医生在收费岗视图里根本不该出现），
 * 诊室/号源/费用这些只对医生有意义的值也一起回到默认。
 */
const handleStaffTypeChange = () => {
  formData.value.doctorId = null;
  formData.value.doctorName = '';
  formData.value.doctorIds = [];
  formData.value.doctorRows = {};
  formData.value.roomId = null;
  formData.value.roomName = '';
  if (formData.value.deptId)
    loadEmployees(Number(formData.value.deptId), formData.value.staffType);
};

const handleSubmit = async () => {
  if (!formData.value.deptId) {
    ElMessage.warning('请选择科室');
    return;
  }
  const doctorIds = (formData.value.doctorIds || []).filter(Boolean);
  if (!doctorIds.length) {
    ElMessage.warning(`请选择${staffTypeText(formData.value.staffType)}人员（同科室多位可多选，一次排完）`);
    return;
  }
  // 日期口径：新增 = 只认日期区间，排一天就起止填同一天（编辑在另一个弹窗，单日）。
  if (formData.value.scheduleDateRange?.length !== 2) {
    ElMessage.warning('请选择排班日期');
    return;
  }
  const days = batchDays(formData.value.scheduleDateRange);
  // 与后端 batchUpsert 的 @Size(max=31) 同一口径，这里只是把提示提前到提交前
  if (days.length > 31) {
    ElMessage.warning(`日期区间最多 31 天（当前 ${days.length} 天），跨月请分批排`);
    return;
  }
  if (!days.length) {
    ElMessage.warning('请选择排班日期');
    return;
  }
  // 过去的日期一律不可写（服务端同样拦，日期选择器已禁选，这里兜住手输与批量范围）
  const today = todayStr.value;
  const pastDays = days.filter(d => d < today);
  if (pastDays.length) {
    ElMessage.warning(`排班日期不能早于今天：${pastDays.join('、')} 已过去，历史排班只读（以门诊日志为准）`);
    return;
  }
  // 班次是时间段的唯一入口：没选班次就没有时间窗（服务端 resolveForScheduling 同样拦）
  if (!formData.value.shiftId) {
    ElMessage.warning('请选择班次：时间段由班次带出，需要的时段在「班次字典」里先建一条');
    return;
  }
  const dept = props.departments.find((d) => d.id === formData.value.deptId);
  if (dept)
    formData.value.deptName = dept.deptName;
  // 新增是「每位医生一行、各填各的」（见 doctorRows：号源/费用/预约按人不同，
  // 整批共用一个值会让挂号台读出来全是同质数据）
  const pairs = doctorIds.map((id) => {
    const row = formData.value.doctorRows[String(id)] || doctorRowDefaults();
    const rid = row.roomId ?? null;
    return {
      doctorId: id,
      doctorName: doctorNameOf(id),
      roomId: rid,
      roomName: roomNameOf(rid),
      totalSource: row.totalSource,
      registFee: row.registFee,
      diagnosisFee: row.diagnosisFee,
      isExpert: row.isExpert,
      expertFee: row.isExpert === 1 ? row.expertFee : 0,
      isAppointment: row.isAppointment,
      appointmentSource: row.isAppointment === 1 ? Math.min(row.appointmentSource, row.totalSource) : 0,
    };
  });
  if (formData.value.status !== 0 && staffTypeHasSource(formData.value.staffType)) {
    const noRoom = pairs.filter((p) => !p.roomId && !p.roomName);
    if (noRoom.length) {
      ElMessage.warning(`启用状态的医生排班必须选诊室：${noRoom.map((p) => p.doctorName).join('、')} 还没分到诊室（分诊台按诊室编号发号，缺诊室患者找不到房间）`);
      return;
    }
  }
  submitLoading.value = true;
  try {
    const res = await createScheduleBatch({
      deptId: formData.value.deptId,
      deptName: formData.value.deptName,
      staffType: formData.value.staffType,
      shiftId: formData.value.shiftId,
      status: formData.value.status,
      scheduleDates: days,
      items: pairs.map((p) => ({
        doctorId: p.doctorId,
        doctorName: p.doctorName,
        roomId: p.roomId,
        roomName: p.roomName,
        totalSource: p.totalSource,
        registFee: p.registFee,
        diagnosisFee: p.diagnosisFee,
        isExpert: p.isExpert,
        expertFee: p.expertFee,
        isAppointment: p.isAppointment,
        appointmentSource: p.appointmentSource,
      })),
    });
    ElMessage.success(res.data?.message || `成功创建 ${res.data?.created ?? days.length * pairs.length} 条排班`);
    visible.value = false;
    emit('saved');
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    submitLoading.value = false;
  }
};
</script>
