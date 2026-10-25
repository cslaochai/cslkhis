<template>
  <!-- 班次编辑弹窗：名称/短码/缩写/颜色/夜班/计工时/夜班津贴/适用域/状态可改；
       起止时间/适用科室/班别 保持只读（改了影响未来排班的默认值，需走 SQL）。
       提交统一走 shiftUpsert 整条覆盖，只读字段原样带回防止被 null 覆盖。 -->
  <el-dialog v-model="visible" destroy-on-close title="编辑班次" width="560px">
    <el-form label-position="left" @submit.prevent>
      <el-form-item label="班次名称" required>
        <el-input v-model="form.shiftName" data-testid="shift-edit-name" maxlength="50"
                  placeholder="班次名称（同科室范围内不可重复）"
                  show-word-limit/>
      </el-form-item>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="短码">
          <el-input v-model="form.shiftCode" maxlength="16" placeholder="如 AM / N / R" clearable/>
        </el-form-item>
        <el-form-item label="缩写">
          <el-input v-model="form.shortName" maxlength="4" placeholder="如 夜 / 休 / 听" clearable/>
        </el-form-item>
      </div>
      <el-form-item label="排班图颜色">
        <el-color-picker v-model="form.color" show-alpha :predefine="COLOR_PRESETS"/>
        <span v-if="!form.color" class="ml-3 text-xs text-slate-400">不选=前端默认色</span>
      </el-form-item>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="起止时间">
          <el-input :model-value="`${form.startTime} ~ ${form.endTime}`" disabled/>
        </el-form-item>
        <el-form-item label="时长">
          <el-input :model-value="`${form.durationMinutes} 分钟`" disabled/>
        </el-form-item>
        <el-form-item label="适用科室">
          <el-input :model-value="shiftDeptName(form.deptId)" disabled/>
        </el-form-item>
        <el-form-item label="班别">
          <el-input :model-value="form.scheduleType ? (shiftLabel[form.scheduleType] || '—') : '—'" disabled/>
        </el-form-item>
      </div>
      <el-form-item label="是否夜班">
        <el-radio-group v-model="form.isNight">
          <el-radio :value="1">夜班</el-radio>
          <el-radio :value="0">白班</el-radio>
        </el-radio-group>
        <span class="ml-3 text-xs text-slate-400">连班/岗后休息规则按此判定</span>
      </el-form-item>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="计工时">
          <el-switch v-model="form.countWorkHours"
                     active-text="计" inactive-text="不计"
                     active-value="1" inactive-value="0"/>
        </el-form-item>
        <el-form-item label="夜班津贴">
          <el-switch v-model="form.countNightAllowance"
                     active-text="计发" inactive-text="不计"
                     active-value="1" inactive-value="0"/>
        </el-form-item>
      </div>
      <el-form-item label="适用域">
        <el-radio-group v-model="form.useScope" data-testid="shift-edit-scope">
          <el-radio :value="1">门诊/急诊排班</el-radio>
          <el-radio :value="2">病区护理排班</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status" data-testid="shift-edit-status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <p class="text-xs text-slate-400">起止时间/适用科室/班别为铺底口径，不在此修改（需走 SQL）；其他字段改完即时生效，已排班次的时间快照不受影响。</p>
    </el-form>
    <template #footer>
      <el-button v-perm="'shift:dict:edit'" :loading="saving" type="primary"
                 @click="handleSubmit">确定
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {createShift} from '@/api/appoint';
import {SCHEDULE_TYPE_TEXT} from '@/lib/scheduleShift';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  departments: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const shiftLabel = SCHEDULE_TYPE_TEXT;
const COLOR_PRESETS = ['#67C23A', '#409EFF', '#F56C6C', '#909399', '#E6A23C', '#9B59B6'];

// 整条 upsert 提交，只读字段（startTime/endTime/deptId/scheduleType/applyStaffType/needRestHours/lateGraceMinutes）
// 必须原样带回，否则会被后端按 null/默认值覆盖。
const form = ref({});
const saving = ref(false);

const shiftDeptName = (deptId) => {
  if (!deptId) return '全院通用';
  const hit = props.departments.find((d) => String(d.id) === String(deptId));
  return hit?.deptName || `科室${deptId}`;
};

watch(visible, (open) => {
  if (!open) return;
  const r = props.row;
  if (!r) return;
  form.value = {
    id: r.id,
    shiftName: r.shiftName,
    shiftCode: r.shiftCode || '',
    shortName: r.shortName || '',
    color: r.color || '',
    startTime: r.startTime,
    endTime: r.endTime,
    durationMinutes: r.durationMinutes,
    deptId: r.deptId,
    scheduleType: r.scheduleType,
    applyStaffType: r.applyStaffType ?? null,
    useScope: Number(r.useScope ?? 1),
    isNight: r.isNight ?? 0,
    needRestHours: r.needRestHours ?? 0,
    lateGraceMinutes: r.lateGraceMinutes ?? 0,
    countWorkHours: String(r.countWorkHours ?? 1),
    countNightAllowance: String(r.countNightAllowance ?? 0),
    status: r.status ?? 1,
  };
});

const handleSubmit = async () => {
  const f = form.value;
  if (!f.shiftName?.trim()) {
    ElMessage.warning('班次名称不能为空');
    return;
  }
  saving.value = true;
  try {
    await createShift({
      id: f.id,
      shiftName: f.shiftName.trim(),
      shiftCode: f.shiftCode?.trim() || null,
      shortName: f.shortName?.trim() || null,
      color: f.color || null,
      startTime: f.startTime,
      endTime: f.endTime,
      deptId: f.deptId,
      scheduleType: f.scheduleType,
      applyStaffType: f.applyStaffType,
      useScope: f.useScope,
      isNight: f.isNight,
      needRestHours: f.needRestHours,
      lateGraceMinutes: f.lateGraceMinutes,
      countWorkHours: Number(f.countWorkHours),
      countNightAllowance: Number(f.countNightAllowance),
      status: f.status,
    });
    ElMessage.success('保存成功');
    visible.value = false;
    emit('saved');
  } catch (e) {
    /* 错误已由全局拦截器统一提示 */
  } finally {
    saving.value = false;
  }
};
</script>
