<template>
  <!-- 人力标准弹窗（新增+编辑合并：row 为空即新增，带 id 即编辑） -->
  <el-dialog v-model="visible" :title="form.id ? '编辑配置标准' : '新增配置标准'" destroy-on-close
             width="520px">
    <el-form label-position="top" @submit.prevent>
      <el-form-item label="适用班次" required>
        <el-select v-model="form.shiftId" :fit-input-width="false" class="w-full" data-testid="ns-rule-shift">
          <el-option label="病区合计（病区级规则）" value="0"/>
          <el-option v-for="s in shifts" :key="s.shiftId" :label="s.shiftName" :value="String(s.shiftId)"/>
        </el-select>
      </el-form-item>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="最低在岗" required>
          <el-input-number v-model="form.minStaff" :max="99" :min="0" class="w-full" data-testid="ns-rule-min"/>
        </el-form-item>
        <el-form-item label="最高在岗">
          <el-input-number v-model="form.maxStaff" :max="99" :min="0" class="w-full" placeholder="留空=不封顶"/>
        </el-form-item>
        <el-form-item label="周工时上限(h)">
          <el-input-number v-model="form.maxWeekHours" :disabled="!isWardLevelRule" :max="168" :min="0"
                           :precision="1" :step="1"
                           class="w-full" data-testid="ns-rule-hours" placeholder="如 48.0"/>
        </el-form-item>
        <el-form-item label="连续夜班上限(天)">
          <el-input-number v-model="form.maxConsecutiveNightDays" :disabled="!isWardLevelRule" :max="31" :min="0"
                           class="w-full"/>
        </el-form-item>
        <el-form-item label="连续上班上限(天)">
          <el-input-number v-model="form.maxConsecutiveWorkDays" :disabled="!isWardLevelRule" :max="31" :min="0"
                           class="w-full"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </div>
      <p v-if="!isWardLevelRule" class="-mt-2 mb-3 text-xs text-slate-400">
        班次级行只校验最低/最高在岗，工时与连班上限一律在「病区合计」行上配。</p>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" maxlength="500" show-word-limit type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="PERM_EDIT" :loading="saving" data-testid="ns-rule-save" type="primary" @click="save">
        保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 人力配置标准 新增/编辑（合并为一个组件：form.id 有无决定标题与提交语义）。
 * 表单态与保存流程在本组件；班次候选由父页面（矩阵数据）传入。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {upsertNurseScheduleRule} from '@/api/nurseSchedule';
import {PERM_EDIT} from '@/lib/nurseSchedule';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 编辑目标行（null = 新增）
  row: {type: Object, default: null},
  // 当前排班单元的病区 id
  wardId: {type: [String, Number], default: null},
  // 班次候选：矩阵数据的一部分，由父页面传入
  shifts: {type: Array, default: () => []},
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const form = reactive({
  id: null, wardId: null, shiftId: '0',
  minStaff: null, maxStaff: null,
  maxWeekHours: null, maxConsecutiveNightDays: null,
  maxConsecutiveWorkDays: null, status: 1, remark: '',
});
const isWardLevelRule = computed(() => String(form.shiftId) === '0');
watch(() => props.modelValue, (v) => {
  if (!v)
    return;
  const row = props.row;
  Object.assign(form, row ? {
    id: String(row.id), wardId: String(row.wardId), shiftId: String(row.shiftId),
    minStaff: row.minStaff, maxStaff: row.maxStaff,
    maxWeekHours: row.maxWeekHours == null ? null : Number(row.maxWeekHours),
    maxConsecutiveNightDays: row.maxConsecutiveNightDays, maxConsecutiveWorkDays: row.maxConsecutiveWorkDays,
    status: row.status ?? 1, remark: row.remark || '',
  } : {
    id: null, wardId: props.wardId, shiftId: '0', minStaff: null, maxStaff: null,
    maxWeekHours: null, maxConsecutiveNightDays: null, maxConsecutiveWorkDays: null, status: 1, remark: '',
  });
});
const save = async () => {
  saving.value = true;
  try {
    const res = await upsertNurseScheduleRule({
      ...form, wardId: props.wardId, remark: form.remark || null,
      // 班次级行不填工时/连班上限：后端会把它们强制置空，界面上先讲明白
      maxWeekHours: isWardLevelRule.value ? form.maxWeekHours : null,
      maxConsecutiveNightDays: isWardLevelRule.value ? form.maxConsecutiveNightDays : null,
      maxConsecutiveWorkDays: isWardLevelRule.value ? form.maxConsecutiveWorkDays : null,
    });
    
    ElMessage.success(res.message || '标准已保存');
    visible.value = false;
    emit('saved');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    saving.value = false;
  }
};
</script>
