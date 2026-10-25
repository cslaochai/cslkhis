<template>
  <!-- 点格排班弹窗 -->
  <el-dialog v-model="visible" destroy-on-close title="排班" width="460px">
    <!-- 只读态：只有审核发布权（片区科护士长）时表单整体禁用，footer 只留审核动作，编制归病区护士长 -->
    <p v-if="readOnly" class="mb-2 text-xs text-slate-500" data-testid="ns-cell-readonly-tip">
      当前岗位只有审核发布权：班次与状态由病区护士长编制，这里只核对并提交结论。
    </p>
    <el-form label-position="left" :disabled="readOnly" @submit.prevent>
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
      <el-form-item label="兼派公共工位">
        <el-select v-model="form.stationId" clearable filterable
                   placeholder="不兼派则留空"
                   style="width: 100%" data-testid="ns-cell-station">
          <el-option v-for="s in stationOptions" :key="s.id"
                     :label="s.stationName" :value="s.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" maxlength="500"
                  placeholder="如：外出培训" show-word-limit
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <!-- 发布态（G-15）：这一格当前的态与还能走的流转。请求由父页面统一发（台账表格也走同一段），
           弹框只回传意图，免得同一个动作在两边各写一份刷新逻辑 -->
      <div v-if="form.id" class="mb-2 flex flex-wrap items-center justify-end gap-2">
        <el-tag :data-testid="`ns-cell-publish-tag-${form.id}`" :type="publishStatusTagType(form.publishStatus)"
                effect="plain" size="small">
          {{ form.publishStatusText || '—' }}
        </el-tag>
        <p v-if="form.publishStatus === PUBLISH_ADJUSTING" class="mb-1 w-full text-xs text-amber-600">
          调班中：直接修改后点「保存」，将自动转为「已变更」并生效，无需再次审核。
        </p>
        <el-button v-if="canSubmitPublish(form)" v-perm="PERM_ADD"
                   :data-testid="`ns-cell-publish-submit-${form.id}`" link size="small" type="primary"
                   @click="emit('transition', { action: 'submit', row: { ...form } })">提交审核
        </el-button>
        <el-button v-if="canAuditPublish(form)" v-perm="PERM_AUDIT"
                   :data-testid="`ns-cell-publish-audit-${form.id}`" link size="small" type="success"
                   @click="emit('transition', { action: 'audit', row: { ...form } })">审核发布
        </el-button>
        <el-button v-if="canAdjustPublish(form)" v-perm="PERM_EDIT"
                   :data-testid="`ns-cell-publish-adjust-${form.id}`" link size="small" type="warning"
                   @click="emit('transition', { action: 'adjust', row: { ...form } })">进入调班
        </el-button>
        <el-button v-if="canRecallPublish(form)" v-perm="PERM_EDIT"
                   :data-testid="`ns-cell-publish-recall-${form.id}`" link size="small" type="info"
                   @click="emit('transition', { action: 'recall', row: { ...form } })">撤回
        </el-button>
      </div>
      <el-button v-if="form.id && !readOnly" v-perm="PERM_DELETE" :icon="Delete" plain type="danger"
                 @click="emit('delete', { ...form })">删除该格
      </el-button>
      <el-button v-if="!readOnly" v-perm="PERM_ADD" :loading="saving" data-testid="ns-cell-save" type="primary" @click="save">
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
import {deleteOpdAssign, opdAssignUpsert} from '@/api/opdStation';
import {publishStatusTagType, PUBLISH_ADJUSTING} from '@/lib/publishStatus';
import {
  canAdjustPublish,
  canAuditPublish,
  canRecallPublish,
  canSubmitPublish,
  PERM_ADD,
  PERM_AUDIT,
  PERM_DELETE,
  PERM_EDIT,
} from '@/lib/nurseSchedule';

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
  // 公共工位下拉（兼派用）与借出方单元名（派岗表 sourceOrgName 快照）
  stationOptions: {type: Array, default: () => []},
  sourceOrgName: {type: String, default: ''},
  // 只读渲染：父页面按「有没有排班维护权」传入，禁用表单并收起保存/删除，只留发布态动作
  readOnly: {type: Boolean, default: false},
});

const emit = defineEmits(['update:modelValue', 'saved', 'delete', 'transition']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const form = reactive({
  wardId: '', employeeId: '', id: null,
  nurseName: '', scheduleDate: '', scheduleStatus: 1, shiftId: null, remark: '',
  // 兼派公共工位：stationId 选中的工位；assignId 该人该天已有的派岗记录（清空时据此撤销）
  stationId: null, assignId: null,
  // 发布态（码值 + 后端文案）：由父页面 openCell 从矩阵格带上，只读，不参与保存 payload
  publishStatus: null, publishStatusText: '',
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
    // 兼派公共工位：病区排班照常落 biz_schedule，这里只同步独立派岗表（is_loan=1），不并入出勤底座
    await syncStationAssign();
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

/**
 * 同步兼派公共工位到独立派岗表：选了工位就写入/更新，清空且已有派岗就撤销。
 * 与病区出勤底座分离，失败由全局拦截器提示，不回滚已保存的病区排班。
 */
const syncStationAssign = async () => {
  try {
    // 仅「上班」才兼派公共工位；休息/请假/培训/停排不派人，已有兼派则一并撤销
    if (Number(form.scheduleStatus) === 1 && form.stationId) {
      await opdAssignUpsert({
        id: form.assignId || undefined,
        assignDate: form.scheduleDate,
        employeeId: form.employeeId,
        stationId: form.stationId,
        staffType: 2,
        isLoan: 1,
        // unitType 1=病区→借出方类型2病区；2=门诊科室→借出方类型1科室
        sourceOrgType: props.unitType === 2 ? 1 : 2,
        sourceOrgName: props.sourceOrgName || '',
      });
    } else if (form.assignId) {
      await deleteOpdAssign(form.assignId);
    }
  } catch (e) {
    /* 全局拦截器已提示 */
  }
};
</script>
