<template>
  <!-- 班次编辑弹窗：展示完整详情，仅名称可改（时间/科室/类型/状态只读） -->
  <el-dialog v-model="visible" destroy-on-close title="编辑班次" width="480px">
    <el-form label-position="top" @submit.prevent>
      <el-form-item label="班次名称" required>
        <el-input v-model="shiftEditName" data-testid="shift-edit-name" maxlength="50"
                  placeholder="班次名称（同科室范围内不可重复）"
                  show-word-limit/>
      </el-form-item>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="起止时间">
          <el-input :model-value="shiftEditRow ? `${shiftEditRow.startTime} ~ ${shiftEditRow.endTime}` : ''"
                    disabled/>
        </el-form-item>
        <el-form-item label="时长">
          <el-input :model-value="shiftEditRow ? `${shiftEditRow.durationMinutes} 分钟` : ''" disabled/>
        </el-form-item>
        <el-form-item label="适用科室">
          <el-input :model-value="shiftEditRow ? shiftDeptName(shiftEditRow.deptId) : ''" disabled/>
        </el-form-item>
        <el-form-item label="班次类型">
          <el-input :model-value="shiftEditRow?.scheduleType ? (shiftLabel[shiftEditRow.scheduleType] || '—') : '—'"
                    disabled/>
        </el-form-item>
      </div>
      <el-form-item label="适用域">
        <el-radio-group v-model="shiftEditUseScope" data-testid="shift-edit-scope">
          <el-radio :value="1">门诊/急诊排班</el-radio>
          <el-radio :value="2">病区护理排班</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="shiftEditStatus" data-testid="shift-edit-status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <p class="text-xs text-slate-400">可修改名称、适用域与启用状态；起止时间/适用科室/班次类型等口径调整需走 SQL 变更。
        改成「病区护理排班」后，这条班次就只会出现在护理排班页，门诊排班下拉里再也选不到它。</p>
    </el-form>
    <template #footer>
      <el-button v-perm="'outpatient:schedule:edit'" :loading="shiftEditSaving" type="primary"
                 @click="handleShiftEditSubmit">确定
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {createShift, renameShift, updateShiftStatus} from '@/api/appoint';
import {SCHEDULE_TYPE_TEXT} from '@/lib/scheduleShift';

/**
 * 班次字典的「编辑已有班次」：row 传入即编辑（后端 shiftUpsert 是同一个入口）。
 * 科室列表由页面加载一次后 prop 下发 —— 字典列表列与这里必须同一份，不能各拉各的。
 */
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

// 班次文案唯一口径在 lib/scheduleShift.js（1上午/2下午/3全天/4凌晨/5夜班）
const shiftLabel = SCHEDULE_TYPE_TEXT;
const shiftEditRow = ref(null);
const shiftEditName = ref('');
const shiftEditStatus = ref(1);
const shiftEditUseScope = ref(1);
const shiftEditSaving = ref(false);
const shiftDeptName = (deptId) => {
  if (!deptId)
    return '全院通用';
  const hit = props.departments.find((d) => String(d.id) === String(deptId));
  return hit?.deptName || `科室${deptId}`;
};

watch(visible, (open) => {
  if (!open)
    return;
  const row = props.row;
  if (!row)
    return;
  shiftEditRow.value = row;
  shiftEditName.value = row.shiftName;
  shiftEditStatus.value = row.status ?? 1;
  shiftEditUseScope.value = Number(row.useScope ?? 1);
});

// ---------- 编辑弹窗：展示完整详情，名称 + 启用状态可改 ----------
const handleShiftEditSubmit = async () => {
  const name = shiftEditName.value.trim();
  if (!name) {
    ElMessage.warning('班次名称不能为空');
    return;
  }
  shiftEditSaving.value = true;
  try {
    const row = shiftEditRow.value;
    const nameChanged = name !== row.shiftName;
    const statusChanged = shiftEditStatus.value !== row.status;
    const scopeChanged = Number(shiftEditUseScope.value) !== Number(row.useScope ?? 1);
    if (scopeChanged) {
      // 改适用域必须整条 upsert（后端只有 rename/updateStatus 两个单列接口，都不碰 use_scope）
      await createShift({
        id: row.id,
        shiftName: name,
        startTime: row.startTime,
        endTime: row.endTime,
        deptId: row.deptId,
        scheduleType: row.scheduleType,
        useScope: shiftEditUseScope.value,
        status: shiftEditStatus.value,
      });
    } else {
      if (nameChanged)
        await renameShift(row.id, name);
      if (statusChanged)
        await updateShiftStatus(row.id, shiftEditStatus.value);
    }
    if (!nameChanged && !statusChanged && !scopeChanged) {
      ElMessage.info('名称、状态与适用域均未修改');
    } else {
      ElMessage.success('保存成功');
    }
    visible.value = false;
    emit('saved');
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    shiftEditSaving.value = false;
  }
};
</script>
