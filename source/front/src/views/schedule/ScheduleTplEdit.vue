<template>
  <!-- 新增/编辑排班模板弹窗 -->
  <el-dialog v-model="visible" :title="tplDialogTitle" destroy-on-close width="680px">
    <el-form :model="tplFormData" label-position="top">
      <div class="grid grid-cols-4 gap-4">
        <el-form-item label="科室" required>
          <el-select v-model="tplFormData.deptId" class="w-full" filterable placeholder="输入科室名称搜索"
                     @change="handleTplDeptChange">
            <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="岗位类别" required>
          <el-select v-model="tplFormData.staffType" class="w-full" data-testid="tpl-form-staff-type"
                     @change="handleTplStaffTypeChange">
            <el-option v-for="o in STAFF_TYPE_DOCTOR_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
        <el-form-item :label="`${staffTypeText(tplFormData.staffType)}人员`" required>
          <el-select v-model="tplFormData.doctorId" :disabled="!tplFormData.deptId"
                     :placeholder="`选择${staffTypeText(tplFormData.staffType)}人员`" class="w-full"
                     @change="handleTplDoctorChange">
            <el-option v-for="e in employees" :key="e.id" :label="`${e.empName} - ${getTitleLabel(titleMap, e.title)}`"
                       :value="e.id"/>
          </el-select>
        </el-form-item>
        <!-- 诊室只对医生岗有意义：出勤模板排的是「人在不在」，不是「在哪个房间接诊」 -->
        <el-form-item v-if="staffTypeHasSource(tplFormData.staffType)" label="诊室">
          <el-select v-model="tplFormData.roomId" :disabled="!tplFormData.deptId" class="w-full"
                     placeholder="选择诊室">
            <el-option v-for="r in clinicRooms" :key="r.id" :label="r.name" :value="r.id"/>
          </el-select>
        </el-form-item>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="星期几" required>
          <el-select v-model="tplFormData.weekDay" class="w-full">
            <el-option v-for="(label, day) in weekDayMap" :key="day" :label="label" :value="Number(day)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="单双周">
          <el-select v-model="tplFormData.weekParity" class="w-full">
            <el-option :value="0" label="每周"/>
            <el-option :value="1" label="单周"/>
            <el-option :value="2" label="双周"/>
          </el-select>
        </el-form-item>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="班次" required>
          <el-select v-model="tplShiftDictId" class="w-full" data-testid="tpl-shift-dict"
                     filterable placeholder="选择班次（时间段由班次带出）"
                     @change="applyShiftDict">
            <el-option v-for="s in shiftDictOptions" :key="s.id" :label="shiftDictLabel(s)" :value="s.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="时间段">
          <el-input :model-value="tplTimeRange" disabled placeholder="选班次后自动带出"/>
        </el-form-item>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="生效起始">
          <el-date-picker v-model="tplFormData.validFrom" class="!w-full" placeholder="长期有效" type="date"
                          value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="生效截止">
          <el-date-picker v-model="tplFormData.validUntil" class="!w-full" placeholder="长期有效"
                          type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
      </div>
      <!-- 号源/费用/预约只对医生岗有意义：出勤模板（护士/技师/收费…）只描述「谁每周哪天哪个班在岗」 -->
      <template v-if="staffTypeHasSource(tplFormData.staffType)">
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="号源数量" required>
            <el-input-number v-model="tplFormData.totalSource" :max="999" :min="1" class="w-full"/>
          </el-form-item>
          <el-form-item label="挂号费(元)">
            <el-input-number v-model="tplFormData.registFee" :min="0" :precision="2" class="w-full"/>
          </el-form-item>
          <el-form-item label="诊疗费(元)">
            <el-input-number v-model="tplFormData.diagnosisFee" :min="0" :precision="2" class="w-full"/>
          </el-form-item>
        </div>
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="专家号">
            <el-switch v-model="tplFormData.isExpert" :active-value="1" :inactive-value="0"/>
          </el-form-item>
          <el-form-item v-if="tplFormData.isExpert === 1" label="专家费(元)">
            <el-input-number v-model="tplFormData.expertFee" :min="0" :precision="2" class="w-full"/>
          </el-form-item>
        </div>
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="开放预约">
            <el-switch v-model="tplFormData.isAppointment" :active-value="1" :inactive-value="0"/>
          </el-form-item>
          <el-form-item v-if="tplFormData.isAppointment === 1" label="预约号源数">
            <el-input-number v-model="tplFormData.appointmentSource" :max="tplFormData.totalSource" :min="0"
                             class="w-full"/>
          </el-form-item>
        </div>
        <!-- 段级号源细化（可选）：改的是「生成的排班各半小时段各放几个号」，不改模板自身时间窗 -->
        <div class="rounded border border-slate-200 p-3 mb-4">
          <div class="flex items-center justify-between mb-2">
            <span class="text-sm font-medium text-slate-700">按段细化号源（可选）</span>
            <div class="flex gap-2">
              <el-button :disabled="!tplFormData.startTime" data-testid="tpl-slot-generate" size="small"
                         @click="generateTplSlots">按半小时均分生成段
              </el-button>
              <el-button v-if="Array.isArray(tplFormData.slots)" data-testid="tpl-slot-clear" size="small"
                         @click="tplFormData.slots = []">清空细分
              </el-button>
            </div>
          </div>
          <p class="text-xs text-slate-500 mb-2">
            未细化时生成的排班按半小时均分号源；细化后按每段配置放号。
            段必须铺满班次时间窗{{ tplTimeRange ? `（${tplTimeRange}）` : '' }}，各段合计须等于号源总数与预约号源数；清空并保存后回退均分。
          </p>
          <template v-if="Array.isArray(tplFormData.slots) && tplFormData.slots.length > 0">
            <el-table :data="tplFormData.slots" max-height="260" size="small">
              <el-table-column label="#" type="index" width="48"/>
              <el-table-column label="时间段" width="130">
                <template #default="{ row }">{{ row.startTime }} ~ {{ row.endTime }}</template>
              </el-table-column>
              <el-table-column label="段号源" min-width="130">
                <template #default="{ row }">
                  <el-input-number v-model="row.totalSource" :max="999" :min="0" class="!w-full"
                                   data-testid="tpl-slot-total" size="small"/>
                </template>
              </el-table-column>
              <el-table-column label="段预约池" min-width="130">
                <template #default="{ row }">
                  <el-input-number v-model="row.appointmentSource" :disabled="tplFormData.isAppointment !== 1"
                                   :max="999" :min="0"
                                   class="!w-full" size="small"/>
                </template>
              </el-table-column>
            </el-table>
            <p :class="tplSlotMismatch ? 'text-red-500' : 'text-emerald-600'" class="mt-2 text-xs"
               data-testid="tpl-slot-sum">
              合计：号源 {{ tplSlotSum.total }} / 期望 {{ tplSlotExpected.total }}，
              预约池 {{ tplSlotSum.appt }} / 期望 {{ tplSlotExpected.appt }}
              <span v-if="tplSlotMismatch">（不一致，无法保存）</span>
            </p>
          </template>
        </div>
      </template>
      <p v-else class="mb-3 text-xs text-slate-500">
        「{{ staffTypeText(tplFormData.staffType) }}」是<b>出勤模板</b>：生成出来的是出勤排班，
        不放号、不需要诊室、不计挂号费——这几项由后端一律置空。
      </p>
      <el-form-item label="状态">
        <el-radio-group v-model="tplFormData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="['outpatient:schedule:add','outpatient:schedule:edit']" :loading="tplSubmitLoading"
                 type="primary"
                 @click="handleTplSubmit">确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getShiftSelectList, saveScheduleTemplate} from '@/api/appoint';
import {getClinicRoomListAll, getEmployeeList} from '@/api/system';
import {SHIFT_SCOPE_OUTPATIENT, STAFF_TYPE_DOCTOR, STAFF_TYPE_DOCTOR_OPTIONS, staffTypeHasSource, staffTypeText} from '@/lib/scheduleShift';
import {expertDefaultOf, getTitleLabel} from '@/lib/scheduleTitle';

/**
 * 排班模板「新增 / 编辑」共用一个组件：后端是同一个 saveScheduleTemplate 入口，
 * 两侧字段与版式一致，标题按 row 有无切换（row 空=新增，有=修改）。
 * 科室列表与星期口径由页面 prop 下发（模板列表列同一份）；人员/诊室/班次在组件内自加载。
 */
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  departments: {type: Array, default: () => []},
  titleMap: {type: Object, default: () => ({})},
  weekDayMap: {type: Object, default: () => ({})},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const isEdit = computed(() => !!props.row?.id);
const tplDialogTitle = computed(() => isEdit.value ? '修改模板' : '新增模板');

const employees = ref([]);
const clinicRooms = ref([]);
const tplSubmitLoading = ref(false);

const tplFormData = ref({});
const resetTplForm = () => {
  tplFormData.value = {
    id: null, deptId: null, deptName: '', doctorId: null, doctorName: '', staffType: STAFF_TYPE_DOCTOR,
    weekDay: 1, weekParity: 0, validFrom: '', validUntil: '',
    shiftId: null, startTime: '', endTime: '', totalSource: 20,
    roomId: null, roomName: '', registFee: 0, diagnosisFee: 0, isExpert: 0, expertFee: 0,
    isAppointment: 1, appointmentSource: 0, status: 1, remark: '', slots: null,
  };
};

// ========== 班次字典（biz_shift）：模板的唯一时间段与班别来源 ==========
// 字典 id 是字符串（后端 BIGINT ToStringSerializer），v-model 用 string
const shiftDictOptions = ref([]);
const tplShiftDictId = ref(null);
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
 * 班次回显：编辑时按落库的 shift_id 还原下拉。
 * 班次已停用 / 不适用于本科室 → 不在选项里，就不回显（宁空不猜：反查时间会撞「上午门诊 vs 专家门诊（上午）」）。
 */
const restoreShiftDict = (row, deptId) => {
  const shiftId = row?.shiftId ? String(row.shiftId) : null;
  loadShiftDict(deptId).then((opts) => {
    const hit = shiftId && opts.some((s) => String(s.id) === shiftId);
    tplShiftDictId.value = hit ? shiftId : null;
    // 回显不出来（班次停用/删除/跨科室）就连带清掉表单里的 shiftId：
    // 留着一个界面上看不见的班次ID，提交必然被后端拦
    tplFormData.value.shiftId = hit ? shiftId : null;
  });
};
/**
 * 模板是「整个班次窗口」的定义（生成排班的母版）：时间段一律由班次带出，界面不给手改。
 * 需要非常规时段就到「班次字典」建一条，模板只负责选它。
 */
const applyShiftDict = (shiftId) => {
  const form = tplFormData.value;
  const hit = shiftDictOptions.value.find((s) => String(s.id) === String(shiftId));
  if (!hit) {
    // 选项被清空只可能是班次停用/删除后从列表里消失了：把选中值一起清掉，别留一个提交必失败的假选中
    tplShiftDictId.value = null;
    form.shiftId = null;
    form.startTime = '';
    form.endTime = '';
    return;
  }
  form.shiftId = String(hit.id);
  const winChanged = form.startTime !== hit.startTime || form.endTime !== hit.endTime;
  form.startTime = hit.startTime;
  form.endTime = hit.endTime;
  // 模板换班次=换时间窗：已细化的段配置不再铺满新窗，留着保存必失败 → 就地清空（生成时回退半小时均分）
  if (winChanged && Array.isArray(form.slots) && form.slots.length > 0) {
    form.slots = [];
    ElMessage.info('班次已变更，原按段细化号源配置已清空');
  }
};
const tplTimeRange = computed(() => tplFormData.value.startTime && tplFormData.value.endTime
    ? `${tplFormData.value.startTime}~${tplFormData.value.endTime}` : '');

// ========== 表单候选：人员 / 诊室 ==========
/**
 * 拉「可排班人员」候选（sql/195 起不限医生：护士/技师/药师/收费员都能排）。
 * 按岗位类别取人，不按 emp_type（后端 staffType 走「岗位表 → 角色 → 类别」口径）。
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

// ========== 模板段级号源（按段细化号源，可选） ==========
// 与后端生成侧同口径：半小时切窗；均分「base 给所有段，余数补给前面的段」；
// 预约池先同口径均分，超出段号源的部分顺延到后面有空余的段。
const tplToMinute = (t) => Number(t.slice(0, 2)) * 60 + Number(t.slice(3, 5));
const tplToHHmm = (m) => `${String(Math.floor(m / 60)).padStart(2, '0')}:${String(m % 60).padStart(2, '0')}`;
const generateTplSlots = () => {
  const f = tplFormData.value;
  if (!f.startTime || !f.endTime) {
    ElMessage.warning('请先选择班次');
    return;
  }
  const s0 = tplToMinute(f.startTime);
  const e0 = tplToMinute(f.endTime);
  if (s0 % 30 !== 0 || e0 % 30 !== 0 || e0 <= s0) {
    ElMessage.warning('该班次时间窗不是半小时整点，无法按段细化');
    return;
  }
  const segs = [];
  let cursor = s0;
  while (cursor < e0) {
    const next = Math.min(cursor + 30, e0);
    segs.push([cursor, next]);
    cursor = next;
  }
  const n = segs.length;
  const total = Number(f.totalSource) || 0;
  const appt = f.isAppointment === 1 ? (Number(f.appointmentSource) || 0) : 0;
  if (appt > total) {
    ElMessage.warning('预约号源数不能大于号源总数');
    return;
  }
  const spread = (amount) => {
    const base = Math.floor(amount / n);
    let rem = amount % n;
    return segs.map(() => base + (rem-- > 0 ? 1 : 0));
  };
  const totals = spread(total);
  const appts = spread(appt);
  for (let i = 0; i < n; i++) {
    if (appts[i] > totals[i]) {
      let overflow = appts[i] - totals[i];
      appts[i] = totals[i];
      for (let j = n - 1; j >= 0 && overflow > 0; j--) {
        const move = Math.min(totals[j] - appts[j], overflow);
        appts[j] += move;
        overflow -= move;
      }
    }
  }
  f.slots = segs.map((seg, i) => ({
    startTime: tplToHHmm(seg[0]), endTime: tplToHHmm(seg[1]),
    totalSource: totals[i], appointmentSource: appts[i],
  }));
};
/** Σ段实时对账：后端硬校验「Σ段=主表」，前端提前显示差在哪、拦下必失败的提交 */
const tplSlotSum = computed(() => {
  const rows = tplFormData.value.slots;
  if (!Array.isArray(rows) || rows.length === 0)
    return {total: 0, appt: 0};
  return {
    total: rows.reduce((s, r) => s + (Number(r.totalSource) || 0), 0),
    appt: rows.reduce((s, r) => s + (Number(r.appointmentSource) || 0), 0),
  };
});
const tplSlotExpected = computed(() => ({
  total: Number(tplFormData.value.totalSource) || 0,
  appt: tplFormData.value.isAppointment === 1 ? (Number(tplFormData.value.appointmentSource) || 0) : 0,
}));
const tplSlotMismatch = computed(() => tplSlotSum.value.total !== tplSlotExpected.value.total
    || tplSlotSum.value.appt !== tplSlotExpected.value.appt);

/** 模板换医生 = 换人重新定默认：专家开关/专家费按新医生档案重新带出（仍可手工改） */
const handleTplDoctorChange = (id) => {
  const dft = expertDefaultOf(props.titleMap, employees.value.find((e) => String(e.id) === String(id)));
  if (dft) {
    tplFormData.value.isExpert = dft.isExpert;
    tplFormData.value.expertFee = dft.expertFee;
  }
};
/**
 * 模板换岗位类别：换的是「规则链」——医生岗配号源/诊室/挂号费，其余岗位是纯出勤。
 * 所以换岗位要把人一起清掉重新选：原来选的医生在「收费」岗位视图里根本不该出现。
 */
const handleTplStaffTypeChange = () => {
  tplFormData.value.doctorId = null;
  tplFormData.value.doctorName = '';
  tplFormData.value.slots = null;
  if (tplFormData.value.deptId)
    loadEmployees(Number(tplFormData.value.deptId), tplFormData.value.staffType);
};
const handleTplDeptChange = (deptId) => {
  tplFormData.value.doctorId = null;
  tplFormData.value.doctorName = '';
  tplFormData.value.roomId = null;
  tplFormData.value.roomName = '';
  // 换科室：班次选项换成「新科室适用 + 通用」，原选中项可能不在新列表里
  tplShiftDictId.value = null;
  loadShiftDict(deptId);
  loadEmployees(deptId, tplFormData.value.staffType);
  loadClinicRooms(deptId);
};

/** 把传进来的模板行铺进表单（原来 handleTplEdit 的填充逻辑，整段搬过来） */
const fillForm = (row) => {
  tplFormData.value = {
    id: row.id,
    deptId: row.deptId,
    deptName: row.deptName || '',
    doctorId: row.doctorId,
    doctorName: row.doctorName || '',
    // 历史模板（sql/195 之前）没有岗位类别 → 后端返回 1（DDL 默认值），这里同样兜底成医生
    staffType: row.staffType ?? STAFF_TYPE_DOCTOR,
    weekDay: row.weekDay,
    weekParity: row.weekParity || 0,
    validFrom: row.validFrom || '',
    validUntil: row.validUntil || '',
    shiftId: row.shiftId ? String(row.shiftId) : null,
    startTime: row.startTime || '',
    endTime: row.endTime || '',
    totalSource: row.totalSource,
    roomId: row.roomId,
    roomName: row.roomName || '',
    registFee: row.registFee || 0,
    diagnosisFee: row.diagnosisFee || 0,
    isExpert: row.isExpert || 0,
    expertFee: row.expertFee || 0,
    isAppointment: row.isAppointment || 0,
    appointmentSource: row.appointmentSource || 0,
    status: row.status,
    remark: row.remark || '',
    // 已细化的模板把段配置带进表单（整批替换口径）；未细化保持 null，提交时不动后端现状
    slots: Array.isArray(row.slots) && row.slots.length > 0
        ? row.slots.map((s) => ({
          startTime: s.startTime, endTime: s.endTime,
          totalSource: s.totalSource, appointmentSource: s.appointmentSource,
        }))
        : (Array.isArray(row.slots) ? [] : null),
  };
};

watch(visible, (open) => {
  if (!open)
    return;
  const row = props.row;
  if (!row) {
    resetTplForm();
    tplShiftDictId.value = null;
    loadShiftDict(null);
    return;
  }
  fillForm(row);
  if (row.deptId) {
    loadEmployees(Number(row.deptId), tplFormData.value.staffType);
    loadClinicRooms(Number(row.deptId));
  }
  // 标准班次按落库的 shift_id 回显；选项按科室过滤（该科室适用 + 全院通用）
  restoreShiftDict(row, row.deptId);
});

const handleTplSubmit = async () => {
  if (!tplFormData.value.deptId) {
    ElMessage.warning('请选择科室');
    return;
  }
  if (!tplFormData.value.doctorId) {
    ElMessage.warning('请选择排班人员');
    return;
  }
  if (!tplFormData.value.shiftId) {
    ElMessage.warning('请选择班次');
    return;
  }
  if (staffTypeHasSource(tplFormData.value.staffType)
      && tplFormData.value.isAppointment === 1 && tplFormData.value.appointmentSource > tplFormData.value.totalSource) {
    ElMessage.warning('预约号源数不能大于号源总数');
    return;
  }
  // 段级号源提交前对账：Σ段=主表是后端硬校验，不一致时提交必失败，前端先拦一次省一次往返
  if (staffTypeHasSource(tplFormData.value.staffType)
      && Array.isArray(tplFormData.value.slots) && tplFormData.value.slots.length > 0 && tplSlotMismatch.value) {
    ElMessage.warning('各段号源/预约池合计与总数不一致，请调整后再保存');
    return;
  }
  const dept = props.departments.find((d) => d.id === tplFormData.value.deptId);
  const room = clinicRooms.value.find((r) => r.id === tplFormData.value.roomId);
  const emp = employees.value.find((e) => e.id === tplFormData.value.doctorId);
  if (dept)
    tplFormData.value.deptName = dept.deptName;
  if (room)
    tplFormData.value.roomName = room.name;
  if (emp)
    tplFormData.value.doctorName = emp.empName;
  tplSubmitLoading.value = true;
  try {
    await saveScheduleTemplate(tplFormData.value);
    ElMessage.success(tplFormData.value.id ? '模板已更新' : '模板已创建');
    visible.value = false;
    emit('saved');
  } catch (error) {
    ElMessage.error(error.message || '保存失败');
  } finally {
    tplSubmitLoading.value = false;
  }
};
</script>
