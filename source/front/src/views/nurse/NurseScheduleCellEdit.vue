<template>
  <!-- 点格排班弹窗 -->
  <el-dialog v-model="visible" destroy-on-close title="排班" width="460px">
    <el-form label-position="left" @submit.prevent>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="护士">
          <el-input :model-value="form.nurseName" disabled/>
        </el-form-item>
        <el-form-item label="日期">
          <el-input :model-value="form.scheduleDate" disabled/>
        </el-form-item>
      </div>
      <el-form-item label="排班状态" required>
        <el-radio-group v-model="form.scheduleStatus" data-testid="ns-cell-status">
          <el-radio v-for="o in statusOptions" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :required="form.scheduleStatus === 1" label="班次">
        <el-select v-model="form.shiftId" :disabled="form.scheduleStatus !== 1" :fit-input-width="false"
                   class="w-full"
                   data-testid="ns-cell-shift" placeholder="选择护理班次">
          <el-option v-for="s in shifts" :key="s.shiftId"
                     :label="`${s.shiftName} ${s.startTime}~${s.endTime}（${s.durationMinutes}分）`"
                     :value="String(s.shiftId)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" maxlength="500"
                  placeholder="如：外出培训" show-word-limit
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-if="form.id" v-perm="PERM_DELETE" :icon="Delete" plain type="danger"
                 @click="emit('delete', { ...form })">删除该格
      </el-button>
      <el-button v-perm="PERM_ADD" :loading="saving" data-testid="ns-cell-save" type="primary" @click="save">
        保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 周排班矩阵「点格排班」弹框：表单态 + 保存 + 规则提示弹窗在本组件。
 * 父页面负责权限判断与格子数据装配（openCell），这里只收装配好的 row。
 * 删除动作回传给父页面（台账页签也复用同一段删除流程）。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Delete} from '@element-plus/icons-vue';
import {upsertNurseScheduleCell} from '@/api/nurseSchedule';
import {PERM_ADD, PERM_DELETE} from '@/lib/nurseSchedule';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 父页面 openCell 装配好的格子数据（护士/日期/已有排班回显）
  row: {type: Object, default: null},
  // 排班单元类型（1-病区 2-门诊科室），保存时随 payload 上送
  unitType: {type: Number, default: 1},
  // 班次候选与状态字典：矩阵/台账共用，由父页面传入
  shifts: {type: Array, default: () => []},
  statusOptions: {type: Array, default: () => []},
});

const emit = defineEmits(['update:modelValue', 'saved', 'delete']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const form = reactive({
  wardId: '', employeeId: '', id: null,
  nurseName: '', scheduleDate: '', scheduleStatus: 1, shiftId: null, remark: '',
});
watch(() => props.modelValue, (v) => {
  if (v && props.row)
    Object.assign(form, props.row);
});
/** 告警文案要弹 HTML 框，先转义（后端消息里会带护士名/班次名等入库文本） */
const escapeHtml = (v) => String(v ?? '').replace(/[&<>"]/g, (c) => ({
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '&quot;'
}[c]));
const save = async () => {
  saving.value = true;
  try {
    const res = await upsertNurseScheduleCell({
      unitType: props.unitType,
      wardId: form.wardId,
      employeeId: form.employeeId,
      scheduleDate: form.scheduleDate,
      scheduleStatus: form.scheduleStatus,
      shiftId: Number(form.scheduleStatus) === 1 ? form.shiftId : null,
      remark: form.remark || null,
    });
    
    const warns = res.data?.warnings || [];
    ElMessage.success(`已保存${warns.length ? `（${warns.length} 条规则提示）` : ''}`);
    visible.value = false;
    emit('saved');
    if (warns.length) {
      ElMessageBox.alert(warns.map((w) => `<div>· ${escapeHtml(w.message || '')}</div>`).join(''), '本次排班的规则提示', {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '知道了'
      });
    }
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    saving.value = false;
  }
};
</script>
