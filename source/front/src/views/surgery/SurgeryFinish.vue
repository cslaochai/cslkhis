<template>
  <!-- 登记手术完成 -->
  <el-dialog
      v-model="visible"
      data-testid="p4-op-finish-dialog"
      title="登记手术完成（回写首页明细 + 手术记录病历）"
      width="760px"
  >
    <div v-if="finishTarget" class="space-y-3">
      <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm">
        <div class="text-slate-900">{{ text(finishTarget.patientName) }}</div>
        <div class="text-slate-700">
          拟施：{{ text(finishTarget.plannedOperationName) }} ·
          {{ text(finishTarget.operationRoom) }} ·
          主刀 {{ text(finishTarget.surgeonName) }}
        </div>
        <div class="text-[12px] text-slate-500">
          术前核对：{{ text(finishTarget.preopCheckItemsText) }}
        </div>
      </div>
      <el-form label-width="110px">
        <el-form-item label="实际手术" required>
          <el-input
              v-model="finishForm.actualOperationName"
              data-testid="p4-op-finish-name"
              maxlength="200"
              placeholder="实际做的是什么（与拟施不一致时以这里为准，首页记的是它）"
          />
        </el-form-item>
        <el-form-item label="实际编码">
          <el-input v-model="finishForm.actualOperationCode" placeholder="ICD-9-CM-3（可空）"/>
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-date-picker
              v-model="finishForm.operationStartTime"
              class="!w-full"
              data-testid="p4-op-finish-start"
              placeholder="切皮时间"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-date-picker
              v-model="finishForm.operationEndTime"
              class="!w-full"
              data-testid="p4-op-finish-end"
              placeholder="关腹/关胸时间"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="术中出血量">
          <el-input v-model.number="finishForm.bloodLoss" placeholder="ml（0 也是有效观测值）"/>
        </el-form-item>
        <el-form-item label="术中所见" required>
          <el-input
              v-model="finishForm.intraopFindings"
              :rows="3"
              data-testid="p4-op-finish-findings"
              placeholder="四核对里「编码有没有病历支持」追的就是这一段"
              type="textarea"
          />
        </el-form-item>
        <el-form-item label="手术经过" required>
          <el-input
              v-model="finishForm.intraopProcedure"
              :rows="3"
              data-testid="p4-op-finish-procedure"
              placeholder="操作步骤"
              type="textarea"
          />
        </el-form-item>
        <el-form-item label="术后处理" required>
          <el-input
              v-model="finishForm.postopNote"
              :rows="3"
              data-testid="p4-op-finish-postop"
              placeholder="术后处理与注意事项（缺了等于「做完就不管了」）"
              type="textarea"
          />
        </el-form-item>
        <el-form-item label="标本送检">
          <el-input v-model="finishForm.specimenSent" placeholder="无标本请写「无」，不要留空"/>
        </el-form-item>
      </el-form>
      <el-alert :closable="false" show-icon type="warning">
        提交即回写两份正式文书：病案首页手术明细 + record_type=5 手术记录病历（签名为主刀医师）。
        回写失败整笔回滚，不会出现"状态已完成、首页和病历里查不到"。
      </el-alert>
    </div>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button
          v-perm="'ipd:surgery:edit'"
          :loading="finishSubmitting"
          data-testid="p4-op-finish-submit"
          type="primary"
          @click="submitFinish"
      >
        确认完成并回写
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {finishOperation} from '@/api/inpatientOperation';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 当前要登记完成的那一条手术申请
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));

const finishSubmitting = ref(false);
const finishTarget = computed(() => props.row);
const finishForm = reactive({
  actualOperationCode: '',
  actualOperationName: '',
  operationStartTime: '',
  operationEndTime: '',
  bloodLoss: undefined,
  intraopFindings: '',
  intraopProcedure: '',
  postopNote: '',
  specimenSent: '',
});

watch(visible, (open) => {
  if (!open || !props.row) return;
  const row = props.row;
  finishForm.actualOperationCode = row.plannedOperationCode || '';
  // 默认带出拟施术式 —— 大多数情况一致，不一致时医生会改，改了就按实际回写首页
  finishForm.actualOperationName = row.plannedOperationName || '';
  finishForm.operationStartTime = row.plannedStartTime || '';
  finishForm.operationEndTime = row.plannedEndTime || '';
  finishForm.bloodLoss = undefined;
  finishForm.intraopFindings = '';
  finishForm.intraopProcedure = '';
  finishForm.postopNote = '';
  finishForm.specimenSent = '';
});

const submitFinish = async () => {
  if (!finishTarget.value)
    return;
  if (!finishForm.actualOperationName.trim()) {
    ElMessage.warning('请填写实际手术名称');
    return;
  }
  if (!finishForm.operationStartTime || !finishForm.operationEndTime) {
    ElMessage.warning('请填写实际开始与结束时间');
    return;
  }
  if (!finishForm.intraopFindings.trim() || !finishForm.intraopProcedure.trim() || !finishForm.postopNote.trim()) {
    ElMessage.warning('术中所见、手术经过、术后处理都不能为空');
    return;
  }
  finishSubmitting.value = true;
  try {
    await finishOperation({
      applyId: finishTarget.value.id,
      actualOperationCode: finishForm.actualOperationCode.trim() || undefined,
      actualOperationName: finishForm.actualOperationName.trim(),
      operationStartTime: finishForm.operationStartTime,
      operationEndTime: finishForm.operationEndTime,
      bloodLoss: finishForm.bloodLoss,
      intraopFindings: finishForm.intraopFindings.trim(),
      intraopProcedure: finishForm.intraopProcedure.trim(),
      postopNote: finishForm.postopNote.trim(),
      specimenSent: finishForm.specimenSent.trim() || undefined,
    });
    ElMessage.success('手术已完成（已回写病案首页手术明细与手术记录病历）');
    visible.value = false;
    emit('saved');
  } catch (error) {
    ElMessage.error(error.message || '登记手术完成失败');
  } finally {
    finishSubmitting.value = false;
  }
};
</script>
