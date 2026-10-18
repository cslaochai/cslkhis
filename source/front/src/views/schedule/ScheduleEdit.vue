<template>
  <!--
    排班详情（只读）：
    按真实 HIS（已发布且被患者预约/挂号引用的排班），医生/日期/班次/诊室/号别/挂号费等本体不可原地修改；
    排错只能停用后重排，调整号源走「加号」，停用/退号走格子上的「停」按钮。本弹窗只做查看。
  -->
  <el-dialog v-model="visible" destroy-on-close title="排班详情" width="720px">
    <div class="grid grid-cols-2 gap-x-8 gap-y-2.5">
      <div class="flex items-baseline gap-2">
        <span class="w-[72px] shrink-0 text-xs text-slate-400">科室</span>
        <span class="flex-1 text-sm text-slate-700">{{ formData.deptName || '—' }}</span>
      </div>
      <div class="flex items-baseline gap-2">
        <span class="w-[72px] shrink-0 text-xs text-slate-400">医生</span>
        <span class="flex-1 text-sm text-slate-700">{{ formData.doctorName || '—' }}</span>
      </div>
      <div class="flex items-baseline gap-2">
        <span class="w-[72px] shrink-0 text-xs text-slate-400">排班日期</span>
        <span class="flex-1 text-sm text-slate-700">{{ formData.scheduleDate || '—' }}</span>
      </div>
      <div class="flex items-baseline gap-2">
        <span class="w-[72px] shrink-0 text-xs text-slate-400">诊室</span>
        <span class="flex-1 text-sm text-slate-700">{{ formData.roomName || '—' }}</span>
      </div>
      <div class="flex items-baseline gap-2">
        <span class="w-[72px] shrink-0 text-xs text-slate-400">班次</span>
        <span class="flex-1 text-sm text-slate-700">{{ row?.shiftName || '—' }}</span>
      </div>
      <div class="flex items-baseline gap-2">
        <span class="w-[72px] shrink-0 text-xs text-slate-400">时间段</span>
        <span class="flex-1 font-mono text-sm text-slate-700">{{ formTimeRange || '—' }}</span>
      </div>
      <div class="flex items-baseline gap-2">
        <span class="w-[72px] shrink-0 text-xs text-slate-400">号源数</span>
        <span class="flex-1 text-sm text-slate-700">
          {{ formData.totalSource }}
          <span v-if="formData.usedSource" class="ml-1 text-xs text-slate-400">（已挂 {{ formData.usedSource }}）</span>
        </span>
      </div>
      <div class="flex items-baseline gap-2">
        <span class="w-[72px] shrink-0 text-xs text-slate-400">挂号费(元)</span>
        <span class="flex-1 text-sm text-slate-700">{{ formData.diagnosisFee }}</span>
      </div>
      <div class="flex items-baseline gap-2">
        <span class="w-[72px] shrink-0 text-xs text-slate-400">挂号级别</span>
        <span class="flex-1 text-sm text-slate-700">{{ tierLabel(formData.diagnosisTier) || '—' }}</span>
      </div>
      <div class="flex items-baseline gap-2">
        <span class="w-[72px] shrink-0 text-xs text-slate-400">支持预约</span>
        <span class="flex-1 text-sm text-slate-700">{{ formData.isAppointment === 1 ? '是' : '否' }}</span>
      </div>
      <div v-if="formData.isAppointment === 1" class="flex items-baseline gap-2">
        <span class="w-[72px] shrink-0 text-xs text-slate-400">预约号</span>
        <span class="flex-1 text-sm text-slate-700">{{ formData.appointmentSource }}</span>
      </div>
      <div class="flex items-baseline gap-2">
        <span class="w-[72px] shrink-0 text-xs text-slate-400">状态</span>
        <span :class="formData.status === 1 ? 'text-emerald-600' : 'text-red-500'" class="flex-1 text-sm">
          {{ formData.status === 1 ? '正常' : '停用' }}
        </span>
      </div>
    </div>

    <!-- 时间片段占用：只读查看各时段已挂/剩余，不在此改号源 -->
    <div class="mt-4 rounded-lg border border-slate-200 bg-slate-50 p-3" data-testid="slot-detail">
      <div class="mb-2 flex items-center justify-between">
        <span class="text-sm font-medium text-slate-700">时间片段（半小时一档）</span>
        <span v-if="slotDetailRows.length" class="text-xs text-slate-400">
          共 {{ slotDetailRows.length }} 段 · 余 {{ availableSum }} / {{ totalSum }}
        </span>
      </div>
      <p v-if="slotDetailLoading" class="py-2 text-center text-xs text-slate-400">加载中…</p>
      <p v-else-if="!slotDetailRows.length" class="py-2 text-center text-xs text-slate-400">该排班暂无片段明细</p>
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
            <th class="py-1.5 font-medium">状态</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="s in slotDetailRows" :key="s.id" class="border-t border-slate-200 text-slate-700">
            <td class="py-1.5 pr-2 font-mono">{{ s.startTime }} ~ {{ s.endTime }}</td>
            <td class="py-1.5 pr-2">{{ s.totalSource }}</td>
            <td class="py-1.5 pr-2">{{ s.usedSource }}</td>
            <td :class="s.availableSource <= 0 ? 'text-red-500 font-medium' : 'text-emerald-600'" class="py-1.5 pr-2">
              {{ s.availableSource }}
            </td>
            <td class="py-1.5 pr-2">{{ (s.appointmentSource || 0) > 0 ? s.appointmentSource : '—' }}</td>
            <td class="py-1.5 pr-2">{{
                (s.appointmentSource || 0) > 0 ? Math.max((s.appointmentSource || 0) - (s.usedAppointmentSource || 0), 0) : '—'
              }}
            </td>
            <td class="py-1.5">
              <span :class="s.status === 1 ? 'text-emerald-600' : 'text-red-500'">{{
                  s.status === 1 ? '正常' : '停用'
                }}</span>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <template #footer>
      <div class="flex items-center justify-end gap-2">
        <el-button v-if="formData.status === 1" v-perm="'outpatient:schedule:delete'" plain type="warning"
                   @click="emit('stop', row)">
          停诊
        </el-button>
        <el-button @click="visible = false">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {getScheduleSlots} from '@/api/appoint';
import {STAFF_TYPE_DOCTOR} from '@/lib/scheduleShift';
import {tierLabel} from '@/lib/diagnosisTier';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 排班行：有值即打开详情（本组件只读，不修改）
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'stop']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const formData = ref({});

/** 时间段：由班次带出，只读展示 */
const formTimeRange = computed(() => formData.value.startTime && formData.value.endTime
    ? `${formData.value.startTime}~${formData.value.endTime}` : '');

// ========== 时间片段占用（只读） ==========
const slotDetailRows = ref([]);
const slotDetailLoading = ref(false);
const totalSum = computed(() => slotDetailRows.value.reduce((a, s) => a + (s.totalSource || 0), 0));
const availableSum = computed(() => slotDetailRows.value.reduce((a, s) => a + (s.availableSource || 0), 0));

const loadSlotDetail = async (scheduleId) => {
  slotDetailRows.value = [];
  if (!scheduleId)
    return;
  slotDetailLoading.value = true;
  try {
    const res = await getScheduleSlots(scheduleId);
    slotDetailRows.value = res.data || [];
  } catch (error) {
    console.error('加载时间片段失败:', error);
  } finally {
    slotDetailLoading.value = false;
  }
};

/** 把传进来的排班行铺进只读详情（岗位恒为医生，仅展示用） */
const fillForm = (row) => {
  formData.value = {
    id: row.id,
    deptName: row.deptName,
    roomName: row.roomName,
    doctorName: row.doctorName,
    staffType: STAFF_TYPE_DOCTOR,
    scheduleDate: row.scheduleDate,
    startTime: row.startTime || '',
    endTime: row.endTime || '',
    totalSource: row.totalSource,
    usedSource: row.usedSource || 0,
    diagnosisFee: row.diagnosisFee || 0,
    diagnosisTier: row.diagnosisTier || 1,
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
  loadSlotDetail(row.id);
});
</script>
