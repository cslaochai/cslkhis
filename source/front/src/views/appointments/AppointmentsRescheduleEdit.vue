<template>
  <!-- 看板改约：换号源（后端原子释放旧号/扣新号，同步就诊日期与医生） -->
  <el-dialog v-model="visible" destroy-on-close title="改约号源" width="480px">
    <div class="space-y-3">
      <div class="rounded bg-slate-50 px-3 py-2 text-sm text-slate-600">
        为 <span class="font-semibold text-slate-800">{{ rescheduleForm.patientName }}</span> 改约
        （当前就诊日期：{{ rescheduleForm.visitDate || '-' }}）
        <p v-if="rescheduleForm.currentSourceText" class="mt-1 text-xs text-slate-500">
          当前号源：{{ rescheduleForm.currentSourceText }}
          <template v-if="rescheduleForm.currentSlotText"> · 时间段 {{ rescheduleForm.currentSlotText }}</template>
        </p>
      </div>
      <el-form label-width="76px">
        <el-form-item label="就诊日期">
          <el-date-picker
              v-model="rescheduleForm.visitDate"
              :disabled-date="(d) => d.getTime() < Date.now() - 86400000"
              class="!w-full"
              placeholder="选择改约日期"
              type="date"
              value-format="YYYY-MM-DD"
              @change="() => { rescheduleForm.scheduleId = null; rescheduleForm.slotId = null; rescheduleSlots.value = []; loadRescheduleSchedules() }"
          />
        </el-form-item>
        <el-form-item label="新号源">
          <el-select v-model="rescheduleForm.scheduleId" :fit-input-width="false"
                     :loading="rescheduleLoading" class="!w-full" placeholder="选择新号源"
                     @change="handleRescheduleScheduleChange">
            <el-option v-for="s in rescheduleOptions" :key="s.id"
                       :label="`${s.scheduleDate} ${deskShiftLabel(s.scheduleType)} ${s.startTime}-${s.endTime} ${s.doctorName} 余${s.availableSource}`"
                       :value="s.id"/>
          </el-select>
          <p v-if="rescheduleForm.visitDate && !rescheduleLoading && !rescheduleOptions.length"
             class="mt-1 text-xs text-amber-600">
            {{ rescheduleSchedules.length ? '该日该医生暂无同费用的余号班次，换一天试试' : '该日此科室暂有余号班次，换一天试试' }}
          </p>
        </el-form-item>
        <el-form-item label="时间段">
          <!-- 选了段：后端按段迁移（还旧段/扣新段）；不选：只迁移主表号源 -->
          <el-select v-model="rescheduleForm.slotId" :disabled="!rescheduleSlots.length" :fit-input-width="false"
                     :loading="rescheduleSlotLoading" class="!w-full" clearable
                     placeholder="选择时间段（可选）">
            <el-option v-for="s in rescheduleSlots" :key="s.id" :disabled="s.status !== 1 || s.availableSource <= 0"
                       :label="slotLabel(s)" :value="s.id"/>
          </el-select>
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button v-perm="'opd:appointments:edit'" :loading="rescheduleLoading" type="primary"
                 @click="handleConfirmReschedule">确认改约
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getAvailableSchedule, getScheduleSlots, updateRegistration} from '@/api/appoint';
import {deskShiftLabel, isRegistPaid, isPaidRescheduleCandidate, scheduleFeeCents, slotLabel} from '@/lib/appointments';

// 看板改约弹窗（从 AppointmentsView 拆出）。提交仍是 updateRegistration 换 scheduleId/slotId：
// 释放旧号/扣新号是后端原子动作，页面只负责把「从哪改到哪」选清楚。
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 被改约的看板挂号卡（r） */
  row: {type: Object, default: null},
  /** 该挂号当前挂的排班本体（deskScheduleMap[r.scheduleId]），仅用于回显「当前号源」 */
  schedule: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});
const rescheduleLoading = ref(false);
const rescheduleForm = ref({
  registId: null,
  patientName: '',
  deptId: null,
  visitDate: '',
  scheduleId: null,
  // 新号源下的时间段（可选）：传了按段迁移（还旧段扣新段），不传只迁移主表号源
  slotId: null,
  // 后端 appointUpsert 是新增/修改共用一个 DTO，patientId/visitType 是必填项：
  // 改约只换号源、这两个值原样带回（修改分支只读 scheduleId/slotId/visitType，不会改写它们）
  patientId: null,
  visitType: null,
  // ↓ 仅展示用（不提交）：打开弹窗时把这条预约**现在**挂的号源/时间段带出来，
  //   否则改约的人对着一个空表单根本不知道自己正在从哪改到哪
  currentSourceText: '',
  currentSlotText: '',
});
const rescheduleSchedules = ref([]);
const rescheduleSlots = ref([]);
const rescheduleSlotLoading = ref(false);
/**
 * 已收费的号只留「同医生 + 同费用」的候选（与后端闸同一口径）：
 * 医生这一维在取数时就按 doctorId 收口（见 loadRescheduleSchedules），费用在前端比。
 * 原价取当前号源的门诊诊查费（挂号费已按湘医保发〔2025〕58号废止并入）；拿不到原价（历史号源已被删）时不比费用，交给后端判。
 */
const origFeeCents = computed(() => (props.schedule ? scheduleFeeCents(props.schedule) : null));
const rescheduleOptions = computed(() => rescheduleSchedules.value
    .filter((s) => isPaidRescheduleCandidate(props.row, origFeeCents.value, s)));
watch(visible, (open) => {
  if (!open)
    return;
  const r = props.row || {};
  rescheduleForm.value = {
    registId: r.id,
    patientName: r.patientName,
    deptId: r.deptId,
    visitDate: r.visitDate,
    scheduleId: r.scheduleId || null,
    slotId: r.slotId || null,
    patientId: r.patientId ?? null,
    visitType: r.visitType ?? null,
    // 当前号源 = 挂号挂的那条排班（主页面从 deskScheduleMap 取好传进 schedule）；时间段优先用挂号
    // 自带的段快照（精确到半小时档），没有段快照（历史挂号）就只展示排班窗口
    currentSourceText: (() => {
      const sched = props.schedule;
      if (!sched)
        return '';
      return `${sched.scheduleDate || r.visitDate || ''} ${deskShiftLabel(Number(sched.scheduleType))} ${sched.startTime}-${sched.endTime}`.trim();
    })(),
    currentSlotText: r.slotStart
        ? `${String(r.slotStart).slice(0, 5)}-${String(r.slotEnd).slice(0, 5)}`
        : '',
  };
  rescheduleSchedules.value = [];
  rescheduleSlots.value = [];
  loadRescheduleSchedules();
});
/** 改约选新号源 → 加载其时间段并清掉已选段（旧段属于上一个号源） */
const handleRescheduleScheduleChange = (scheduleId) => {
  rescheduleForm.value.slotId = null;
  rescheduleSlots.value = [];
  const s = rescheduleSchedules.value.find((x) => x.id === scheduleId);
  if (!s?.id)
    return;
  rescheduleSlotLoading.value = true;
  getScheduleSlots(s.id).then((res) => {
    rescheduleSlots.value = res.data || [];
  }).catch((error) => {
    console.error('加载改约时间段失败:', error);
  }).finally(() => {
    rescheduleSlotLoading.value = false;
  });
};
const loadRescheduleSchedules = async () => {
  if (!rescheduleForm.value.deptId || !rescheduleForm.value.visitDate) {
    rescheduleSchedules.value = [];
    return;
  }
  rescheduleLoading.value = true;
  try {
    // 已收费只认同医生的号源，取数时就按 doctorId 收口，不把不该出现的候选捞回前端再筛
    const doctorId = isRegistPaid(props.row) ? props.row?.doctorId : undefined;
    const res = await getAvailableSchedule(rescheduleForm.value.deptId, rescheduleForm.value.visitDate, doctorId);
    // 改约只列还有余号的班次
    rescheduleSchedules.value = (res.data || []).filter((s) => s.availableSource > 0);
  } catch (error) {
    console.error('加载改约号源失败:', error);
  } finally {
    rescheduleLoading.value = false;
  }
};
const handleConfirmReschedule = async () => {
  if (!rescheduleForm.value.scheduleId) {
    ElMessage.warning('请选择新号源');
    return;
  }
  rescheduleLoading.value = true;
  try {
    await updateRegistration({
      id: rescheduleForm.value.registId,
      patientId: rescheduleForm.value.patientId,
      scheduleId: rescheduleForm.value.scheduleId,
      slotId: rescheduleForm.value.slotId || undefined,
      visitType: rescheduleForm.value.visitType,
      // 改约走的是挂号的同一个 DTO（AppointUpsertDTO），registSource 已必填；
      // 改约不改来源，传「1-窗口挂号」与原默认口径一致（该字段在改约分支后端不落库）
      registSource: 1,
    });
    visible.value = false;
    ElMessage.success('改约成功');
    emit('saved');
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    rescheduleLoading.value = false;
  }
};
</script>
