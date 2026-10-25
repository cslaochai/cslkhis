<template>
  <!-- 新增班次弹窗：完整建条（防重/时间校验在后端 shiftUpsert）。
       跨天班次（结束时间 <= 开始时间）由后端按次日处理，前端不拦，只在时长提示里标注。 -->
  <el-dialog v-model="visible" destroy-on-close title="新增字典" width="560px">
    <el-form label-position="left" @submit.prevent>
      <el-form-item label="班次名称" required>
        <el-input v-model="shiftAddForm.shiftName" maxlength="50" placeholder="如：康复科上午班"
                  show-word-limit/>
      </el-form-item>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="班次短码">
          <el-input v-model="shiftAddForm.shiftCode" maxlength="16" placeholder="如 AM / N / R"
                    clearable/>
        </el-form-item>
        <el-form-item label="显示缩写">
          <el-input v-model="shiftAddForm.shortName" maxlength="4" placeholder="如 夜 / 休 / 听"
                    clearable/>
        </el-form-item>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="开始时间" required>
          <el-select v-model="shiftAddForm.startTime" class="w-full" data-testid="shift-add-start" filterable>
            <el-option v-for="t in timeOptions" :key="t" :label="t" :value="t"/>
          </el-select>
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-select v-model="shiftAddForm.endTime" class="w-full" data-testid="shift-add-end" filterable>
            <el-option v-for="t in timeOptions" :key="t" :label="t" :value="t"/>
          </el-select>
        </el-form-item>
      </div>
      <p class="-mt-2 mb-3 text-xs text-slate-400">
        时长自动计算：{{ durationHint }}
      </p>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="适用科室">
          <el-select v-model="shiftAddForm.deptId" class="w-full" filterable placeholder="请选择适用科室">
            <el-option label="全部科室" :value="''"/>
            <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="班别">
          <el-select v-model="shiftAddForm.scheduleType" class="w-full" clearable placeholder="班别">
            <el-option v-for="s in shiftOptions" :key="s.value" :label="s.label" :value="s.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="适用域" required>
          <el-select v-model="shiftAddForm.useScope" :fit-input-width="false" class="w-full"
                     data-testid="shift-add-scope">
            <el-option v-for="o in SHIFT_SCOPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="排班图颜色">
          <el-color-picker v-model="shiftAddForm.color" show-alpha :predefine="COLOR_PRESETS"/>
        </el-form-item>
      </div>
      <el-form-item label="是否夜班">
        <el-radio-group v-model="shiftAddForm.isNight">
          <el-radio :value="1">夜班</el-radio>
          <el-radio :value="0">白班</el-radio>
        </el-radio-group>
        <span class="ml-3 text-xs text-slate-400">连班/岗后休息规则按此判定，不要靠起止时间推</span>
      </el-form-item>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="计工时">
          <el-switch v-model="shiftAddForm.countWorkHours"
                     active-text="计" inactive-text="不计"
                     active-value="1" inactive-value="0"/>
        </el-form-item>
        <el-form-item label="夜班津贴">
          <el-switch v-model="shiftAddForm.countNightAllowance"
                     active-text="计发" inactive-text="不计"
                     active-value="1" inactive-value="0"/>
        </el-form-item>
      </div>
      <el-form-item label="状态">
        <el-radio-group v-model="shiftAddForm.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'shift:dict:add'" :loading="shiftAddSaving" type="primary"
                 @click="handleShiftAddSubmit">
        确定
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {createShift} from '@/api/appoint';
import {SCHEDULE_TYPE_OPTIONS, SHIFT_SCOPE_OPTIONS, SHIFT_SCOPE_OUTPATIENT} from '@/lib/scheduleShift';

/**
 * 班次字典「新增」：与「编辑已有班次」字段与版式都不同（这里是完整建条：名称/起止时间/科室/类型都开放），
 * 所以单独一个组件，不与 ScheduleShiftEdit 合并。
 * 科室列表由页面加载一次后 prop 下发（字典列表列与这里同一份）。
 */
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  departments: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const shiftOptions = SCHEDULE_TYPE_OPTIONS;
// 排班图颜色预设：白班/中班/夜班/休息/听班 五档常用色
const COLOR_PRESETS = [
  '#67C23A', '#409EFF', '#F56C6C', '#909399', '#E6A23C',
];
// 挂号按时段：半小时一档的时刻（00:00 ~ 23:30 共 48 档），班次字典建条时选起止用
const timeOptions = [];
for (let h = 0; h < 24; h++) {
  const hh = String(h).padStart(2, '0');
  timeOptions.push(`${hh}:00`, `${hh}:30`);
}

const defaultForm = () => ({
  shiftName: '',
  shiftCode: '',
  shortName: '',
  color: '',
  startTime: '08:00',
  endTime: '12:00',
  deptId: '',
  scheduleType: null,
  useScope: SHIFT_SCOPE_OUTPATIENT,
  isNight: 0,
  countWorkHours: '1',
  countNightAllowance: '0',
  status: 1,
});

const shiftAddSaving = ref(false);
const shiftAddForm = ref(defaultForm());

// 时长提示：同日按正差算；endTime <= startTime 时按跨天（次日结束）算
const durationHint = computed(() => {
  const toMin = (t) => (t ? Number(t.slice(0, 2)) * 60 + Number(t.slice(3, 5)) : NaN);
  const s = toMin(shiftAddForm.value.startTime);
  const e = toMin(shiftAddForm.value.endTime);
  if (!Number.isFinite(s) || !Number.isFinite(e)) return '—';
  let diff = e - s;
  let cross = false;
  if (diff <= 0) {
    diff += 24 * 60;
    cross = true;
  }
  return `${diff} 分钟${cross ? '（跨天，结束时间属次日）' : ''}`;
});

watch(visible, (open) => {
  if (!open) return;
  shiftAddForm.value = defaultForm();
});

// ---------- 新增弹窗：完整建条 ----------
const handleShiftAddSubmit = async () => {
  const f = shiftAddForm.value;
  if (!f.shiftName.trim()) {
    ElMessage.warning('请填写班次名称');
    return;
  }
  if (!f.startTime || !f.endTime) {
    ElMessage.warning('请选择起止时间');
    return;
  }
  shiftAddSaving.value = true;
  try {
    await createShift({
      shiftName: f.shiftName.trim(),
      shiftCode: f.shiftCode?.trim() || null,
      shortName: f.shortName?.trim() || null,
      color: f.color || null,
      startTime: f.startTime,
      endTime: f.endTime,
      deptId: f.deptId || null,
      scheduleType: f.scheduleType,
      useScope: f.useScope,
      isNight: f.isNight,
      countWorkHours: Number(f.countWorkHours),
      countNightAllowance: Number(f.countNightAllowance),
      status: f.status,
    });
    ElMessage.success('班次已创建');
    visible.value = false;
    emit('saved');
  } catch (error) {
    /* 错误已由全局拦截器统一提示，无需在此弹窗 */
  } finally {
    shiftAddSaving.value = false;
  }
};
</script>
