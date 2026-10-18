<template>
  <!-- 修改排班：单条。医生不可换人（换人=别人的班次），日期只有一天 -->
  <el-dialog v-model="visible" destroy-on-close title="修改排班" width="780px">
    <el-form :model="formData" label-position="left">
      <div class="grid grid-cols-3 gap-4">
        <el-form-item label="科室">
          <el-input :model-value="formData.deptName" disabled/>
        </el-form-item>
        <el-form-item label="岗位类别" required>
          <el-select v-model="formData.staffType" class="w-full" data-testid="edit-staff-type"
                     @change="handleStaffTypeChange">
            <el-option v-for="o in STAFF_TYPE_DOCTOR_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
        <el-form-item :label="`${staffTypeText(formData.staffType)}人员`" required>
          <!-- 修改弹窗的人员是「单选下拉」：排错了人要能在这条上改回来（新增是多选，两个弹窗各自排版）。
               科室保持只读：换科室等于换一整批人，那是"删了重排"，不是在单条上改。 -->
          <el-select v-model="formData.doctorId" :disabled="!formData.deptId"
                     :placeholder="`选择${staffTypeText(formData.staffType)}人员`" class="w-full"
                     data-testid="edit-doctor" filterable @change="handleEditDoctorChange">
            <el-option v-for="e in employees" :key="e.id" :label="`${e.empName} - ${getTitleLabel(titleMap, e.title)}`"
                       :value="e.id"/>
          </el-select>
        </el-form-item>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item data-testid="form-schedule-date" label="排班日期" required>
          <el-date-picker
              v-model="formData.scheduleDate"
              :disabled-date="(time: Date) => time.getTime() < Date.now() - 86400000"
              class="!w-full"
              placeholder="选择日期"
              type="date"
              value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item v-if="staffTypeHasSource(formData.staffType)" label="诊室" required>
          <el-select v-model="formData.roomId" :disabled="!formData.deptId" class="w-full" placeholder="选择诊室">
            <el-option v-for="r in clinicRooms" :key="r.id" :label="r.name" :value="r.id"/>
          </el-select>
        </el-form-item>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="班次" required>
          <el-select v-model="shiftDictId" class="w-full" data-testid="edit-shift-dict"
                     filterable placeholder="选择班次（时间段由班次带出）"
                     @change="applyShiftDict">
            <el-option v-for="s in shiftDictOptions" :key="s.id" :label="shiftDictLabel(s)" :value="s.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="时间段">
          <el-input :model-value="formTimeRange" data-testid="edit-time-range" disabled
                    placeholder="选班次后自动带出"/>
        </el-form-item>
      </div>
      <!-- 号源/费用/预约只对医生岗有意义（出勤岗这些值后端一律清零，界面不给填） -->
      <template v-if="staffTypeHasSource(formData.staffType)">
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="号源数量" required>
            <el-input-number v-model="formData.totalSource" :max="999" :min="formData.usedSource || 1"
                             class="w-full"/>
            <p v-if="formData.usedSource" class="mt-1 text-xs text-slate-400">已挂 {{ formData.usedSource }}
              个，总数不能小于它</p>
          </el-form-item>
          <el-form-item label="门诊诊查费(元)">
            <el-input-number v-model="formData.diagnosisFee" :min="0" :precision="2" class="w-full"/>
            <p class="mt-1 text-xs text-slate-400">挂号费已按湘医保发〔2025〕58号废止，并入门诊诊查费（长沙基准价 9 元）</p>
          </el-form-item>
        </div>
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="专家号">
            <el-switch v-model="formData.isExpert" :active-value="1" :inactive-value="0"/>
          </el-form-item>
          <el-form-item v-if="formData.isExpert === 1" label="专家费(元)">
            <el-input-number v-model="formData.expertFee" :min="0" :precision="2" class="w-full"/>
          </el-form-item>
          <el-form-item label="开放预约">
            <el-switch v-model="formData.isAppointment" :active-value="1" :inactive-value="0"/>
          </el-form-item>
        </div>
      </template>
      <div v-if="staffTypeHasSource(formData.staffType) && formData.isAppointment === 1"
           class="grid grid-cols-3 gap-4">
        <el-form-item label="预约号源数">
          <div class="w-full">
            <el-input-number v-model="formData.appointmentSource" :max="formData.totalSource" :min="0"
                             class="w-full"/>
            <p class="mt-1 text-xs text-slate-400">划给网上预约的专用号，现场窗口不可占用；0 = 不划池</p>
          </div>
        </el-form-item>
      </div>
      <el-form-item label="状态">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">正常</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <!-- 时间片段明细：号源与占用的事实落在段上，主表只是 Σ段 汇总；可编辑时逐段调号源/预约池/停用 -->
    <div class="mt-2 rounded-lg border border-slate-200 bg-slate-50 p-3" data-testid="slot-detail">
      <div class="mb-2 flex items-center justify-between">
        <span class="text-sm font-medium text-slate-700">时间片段（半小时一档）</span>
        <div class="flex items-center gap-2">
          <span v-if="slotDetailRows.length" class="text-xs text-slate-400">
            共 {{
              slotDetailRows.length
            }} 段 · 余 {{
              slotDetailRows.reduce((a: number, s: any) => a + (s.availableSource || 0), 0)
            }} / {{ slotDetailRows.reduce((a: number, s: any) => a + (s.totalSource || 0), 0) }}
          </span>
          <el-button v-if="slotEditEnabled" :disabled="!slotDirty" :loading="slotSaving" data-testid="slot-save"
                     plain size="small"
                     type="primary" @click="handleSaveSlots">保存号源调整
          </el-button>
        </div>
      </div>
      <p v-if="slotDetailLoading" class="py-2 text-center text-xs text-slate-400">加载中…</p>
      <p v-else-if="!slotDetailRows.length" class="py-2 text-center text-xs text-slate-400">
        {{
          staffTypeHasSource(formData.staffType) ? '该排班暂无片段明细（保存后自动按半小时切分生成）' : '出勤排班不放号，不切分时间段'
        }}
      </p>
      <div v-else class="max-h-56 overflow-y-auto">
        <table class="w-full text-xs">
          <thead>
          <tr class="text-left text-slate-500">
            <th class="py-1.5 pr-2 font-medium">时段</th>
            <th class="py-1.5 pr-2 font-medium">总号</th>
            <th class="py-1.5 pr-2 font-medium">已挂</th>
            <th class="py-1.5 pr-2 font-medium">剩余</th>
            <th class="py-1.5 pr-2 font-medium">预约池</th>
            <th class="py-1.5 pr-2 font-medium">约余</th>
            <th class="py-1.5 font-medium">{{ slotEditEnabled ? '停用' : '状态' }}</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="s in slotDetailRows" :key="s.id" class="border-t border-slate-200 text-slate-700">
            <td class="py-1.5 pr-2 font-mono">{{ s.startTime }} ~ {{ s.endTime }}</td>
            <td class="py-1.5 pr-2">
              <el-input-number v-if="slotEditEnabled" v-model="s.totalSource" :max="999"
                               :min="s.usedSource || 0" class="!w-20" controls-position="right" size="small"/>
              <template v-else>{{ s.totalSource }}</template>
            </td>
            <td class="py-1.5 pr-2">{{ s.usedSource }}</td>
            <td :class="s.availableSource <= 0 ? 'text-red-500 font-medium' : 'text-emerald-600'" class="py-1.5 pr-2">
              {{ s.availableSource }}
            </td>
            <td class="py-1.5 pr-2">
              <el-input-number v-if="slotEditEnabled" v-model="s.appointmentSource" :max="s.totalSource"
                               :min="s.usedAppointmentSource || 0" class="!w-20" controls-position="right"
                               size="small"/>
              <template v-else>{{ (s.appointmentSource || 0) > 0 ? s.appointmentSource : '—' }}</template>
            </td>
            <td class="py-1.5 pr-2">{{
                (s.appointmentSource || 0) > 0 ? Math.max((s.appointmentSource || 0) - (s.usedAppointmentSource || 0), 0) : '—'
              }}
            </td>
            <td class="py-1.5">
              <el-switch v-if="slotEditEnabled" v-model="s.status" :active-value="1" :inactive-value="0"
                         active-text="用" inactive-text="停" inline-prompt/>
              <span v-else :class="s.status === 1 ? 'text-emerald-600' : 'text-red-500'">{{
                  s.status === 1 ? '正常' : '停用'
                }}</span>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <template #footer>
      <el-button v-perm="'outpatient:schedule:edit'" :loading="submitLoading" data-testid="edit-submit" type="primary"
                 @click="handleSubmit">保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {
  getScheduleSlots,
  getShiftSelectList,
  saveScheduleSlots,
  updateSchedule
} from '@/api/appoint';
import {getClinicRoomListAll, getEmployeeList} from '@/api/system';
import {SHIFT_SCOPE_OUTPATIENT, STAFF_TYPE_DOCTOR, STAFF_TYPE_DOCTOR_OPTIONS, staffTypeHasSource, staffTypeText} from '@/lib/scheduleShift';
import {fmtISO} from '@/lib/scheduleDate';
import {getTitleLabel} from '@/lib/scheduleTitle';

/**
 * 修改排班（单条）。行数据由 row 传入即编辑；与「新增排班」是两个组件（人员单选/单日/带段级号源表）。
 * 科室列表由页面加载一次 prop 下发；人员/诊室/班次是表单候选，本组件自加载。
 */
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 排班行：有值即编辑（本组件只在有行时打开）
  row: {type: Object, default: null},
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
/**
 * 班次回显：编辑时按落库的 shift_id 还原下拉。
 * 班次已停用 / 不适用于本科室 → 不在选项里，就不回显（宁空不猜：反查时间会撞「上午门诊 vs 专家门诊（上午）」）。
 */
const restoreShiftDict = (row, deptId) => {
  const shiftId = row?.shiftId ? String(row.shiftId) : null;
  loadShiftDict(deptId).then((opts) => {
    const hit = shiftId && opts.some((s) => String(s.id) === shiftId);
    shiftDictId.value = hit ? shiftId : null;
    // 回显不出来（班次停用/删除/跨科室）就连带清掉表单里的 shiftId：
    // 留着一个界面上看不见的班次ID，提交必然被后端拦
    formData.value.shiftId = hit ? shiftId : null;
  });
};

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

const doctorNameOf = (id) => (employees.value.find((e) => e.id === id)?.empName) || '';
/** 修改弹窗换了医生：doctorIds/doctorName 要跟着走（提交时按 doctorIds[0] 出医生） */
const handleEditDoctorChange = (id) => {
  formData.value.doctorIds = [id];
  formData.value.doctorName = doctorNameOf(id);
};
const roomNameOf = (id) => (clinicRooms.value.find((r) => r.id === id)?.name) || '';

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

// ========== 时间片段明细（号源与占用的事实都在段上，可逐段调号源/预约池/停用） ==========
const slotDetailRows = ref([]);
const slotDetailLoading = ref(false);
const slotSaving = ref(false);
// 载入时的段快照：与当前值逐段比对出「有没有改过」，没改就禁用保存
const slotSnapshot = ref('');
const slotEditableFields = (rows) => JSON.stringify(rows.map((s) => [s.id, s.totalSource, s.appointmentSource || 0, s.status]));
const slotEditEnabled = computed(() => formData.value.id && staffTypeHasSource(formData.value.staffType)
    && formData.value.status === 1
    // 过期与停诊后端拒收（历史班次以门诊日志为准），前端直接只读
    && !!formData.value.scheduleDate && formData.value.scheduleDate >= fmtISO(new Date()));
const slotDirty = computed(() => !!slotDetailRows.value.length && slotEditableFields(slotDetailRows.value) !== slotSnapshot.value);
const loadSlotDetail = async (scheduleId) => {
  slotDetailRows.value = [];
  slotSnapshot.value = '';
  if (!scheduleId)
    return;
  slotDetailLoading.value = true;
  try {
    const res = await getScheduleSlots(scheduleId);
    slotDetailRows.value = res.data || [];
    slotSnapshot.value = slotEditableFields(slotDetailRows.value);
  } catch (error) {
    console.error('加载时间片段失败:', error);
  } finally {
    slotDetailLoading.value = false;
  }
};
// 段级号源编辑：整批校验整批提交，Σ段由后端写回主表；used 系是事实，前端同样不许改
const handleSaveSlots = async () => {
  for (const s of slotDetailRows.value) {
    if (s.totalSource < (s.usedSource || 0)) {
      ElMessage.warning(`${s.startTime} 段号源不能小于已挂号数（${s.usedSource || 0}）`);
      return;
    }
    if ((s.appointmentSource || 0) > s.totalSource) {
      ElMessage.warning(`${s.startTime} 段预约号源不能大于号源总数`);
      return;
    }
    if ((s.appointmentSource || 0) < (s.usedAppointmentSource || 0)) {
      ElMessage.warning(`${s.startTime} 段预约号源不能小于已约数（${s.usedAppointmentSource || 0}）`);
      return;
    }
  }
  slotSaving.value = true;
  try {
    await saveScheduleSlots(formData.value.id, slotDetailRows.value.map((s) => ({
      id: s.id, totalSource: s.totalSource, appointmentSource: s.appointmentSource || 0, status: s.status,
    })));
    ElMessage.success('号源调整已保存');
    // Σ段变了：重拉段明细 + 面板（主表的号源总数/剩余是汇总冗余），表单里的汇总值同步对齐
    await loadSlotDetail(formData.value.id);
    formData.value.totalSource = slotDetailRows.value.reduce((a, s) => a + (s.totalSource || 0), 0);
    formData.value.appointmentSource = slotDetailRows.value.reduce((a, s) => a + (s.appointmentSource || 0), 0);
    emit('saved');
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    slotSaving.value = false;
  }
};

/** 把传进来的排班行铺进表单（原来 handleEdit 的填充逻辑，整段搬过来） */
const fillForm = (row) => {
  formData.value = {
    id: row.id,
    deptId: row.deptId,
    deptName: row.deptName,
    roomId: row.roomId,
    roomName: row.roomName,
    doctorId: row.doctorId,
    doctorName: row.doctorName,
    // 历史排班（sql/195 之前）没有岗位类别 → 后端返回 1（DDL 默认值），这里同样兜底
    staffType: row.staffType ?? STAFF_TYPE_DOCTOR,
    // 修改是单条：人员是单选下拉（排错人要在这一条上改回来），日期只有一天
    doctorIds: [row.doctorId],
    doctorRows: {},
    scheduleDate: row.scheduleDate,
    scheduleDateRange: [],
    shiftId: row.shiftId ? String(row.shiftId) : null,
    startTime: row.startTime || '',
    endTime: row.endTime || '',
    totalSource: row.totalSource,
    usedSource: row.usedSource || 0,
    registFee: row.registFee || 0,
    diagnosisFee: row.diagnosisFee || 0,
    isExpert: row.isExpert || 0,
    expertFee: row.expertFee || 0,
    isAppointment: row.isAppointment || 0,
    appointmentSource: row.appointmentSource || 0,
    status: row.status,
  };
};

watch(visible, (open) => {
  if (!open)
    return;
  const row = props.row;
  if (!row)
    return;
  fillForm(row);
  if (row.deptId) {
    loadEmployees(row.deptId, row.staffType ?? STAFF_TYPE_DOCTOR);
    loadClinicRooms(row.deptId);
  }
  // 标准班次按落库的 shift_id 回显；选项按科室过滤（该科室适用 + 全院通用）
  restoreShiftDict(row, row.deptId);
  loadSlotDetail(row.id);
});

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
  // 日期口径：编辑 = 单日（改日期=把这条搬到别天）；新增在另一个弹窗，只认日期区间。
  if (!formData.value.scheduleDate) {
    ElMessage.warning('请选择排班日期');
    return;
  }
  const days = [formData.value.scheduleDate];
  if (!days.length) {
    ElMessage.warning('请选择排班日期');
    return;
  }
  // 过去的日期一律不可写（服务端同样拦）
  const today = fmtISO(new Date());
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
  // 编辑是单条（诊室取表单里的）
  const pairs = doctorIds.map((id) => ({
    doctorId: id,
    doctorName: formData.value.doctorName || doctorNameOf(id),
    roomId: formData.value.roomId,
    roomName: formData.value.roomName || roomNameOf(formData.value.roomId),
  }));
  if (formData.value.status !== 0 && staffTypeHasSource(formData.value.staffType)) {
    const noRoom = pairs.filter((p) => !p.roomId && !p.roomName);
    if (noRoom.length) {
      ElMessage.warning(`启用状态的医生排班必须选诊室：${noRoom.map((p) => p.doctorName).join('、')} 还没分到诊室（分诊台按诊室编号发号，缺诊室患者找不到房间）`);
      return;
    }
  }
  submitLoading.value = true;
  try {
    // 编辑模式只修改单条记录
    const p = pairs[0];
    await updateSchedule({...formData.value, ...p, scheduleDate: days[0]});
    ElMessage.success('修改成功');
    visible.value = false;
    emit('saved');
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    submitLoading.value = false;
  }
};
</script>
