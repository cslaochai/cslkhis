<template>
  <el-dialog v-model="visible" data-testid="p4-tf-approve-dialog" title="用血分级审批" width="520px">
    <div v-if="row" class="mb-3 text-xs text-slate-600 space-y-1">
      <div>
        单号 <b>{{ row.applyNo }}</b> ·
        {{ text(row.patientName) }} ·
        {{ text(row.bloodComponentText) }} {{ text(row.bagCount) }} 袋
      </div>
      <div>
        折算量 <b>{{ row.amountMl != null ? row.amountMl + 'ml' : '待折算（按最高级审批）' }}</b>
        · 属「{{ text(row.approveLevelText) }}」审核签发
        <el-tag v-if="row.approveStatus === 3" class="ml-1" effect="plain" size="small" type="warning">
          急诊补审
        </el-tag>
      </div>
      <div v-if="row.approveRejectReason" class="text-red-600">
        上次驳回原因：{{ row.approveRejectReason }}
      </div>
    </div>
    <el-form label-width="72px">
      <el-form-item label="审批结论" required>
        <el-radio-group v-model="approveForm.approveResult" data-testid="p4-tf-approve-result">
          <el-radio :value="1" data-testid="p4-tf-approve-result-1">通过（可进入配血）</el-radio>
          <el-radio :value="2" data-testid="p4-tf-approve-result-2">驳回</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="审批意见">
        <el-input
            v-model="approveForm.opinion"
            :placeholder="approveForm.approveResult === 2 ? '驳回原因（必填）：写明申请人要改什么' : '可空；同意意见或备注'"
            :rows="3"
            data-testid="p4-tf-approve-opinion"
            maxlength="200"
            show-word-limit
            type="textarea"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'medtech:transfusion:edit'" :loading="approveSubmitting" data-testid="p4-tf-approve-submit"
                 type="primary" @click="submitApprove">
        提交审批结论
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// ---------------- 一·五、用血分级审批（sql/93） ----------------
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {approveTransfusion} from '@/api/inpatientTransfusion';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));

const approveSubmitting = ref(false);
const approveForm = reactive({
  approveResult: null,
  opinion: '',
});

watch(visible, (open) => {
  if (!open) return;
  approveForm.approveResult = null;
  approveForm.opinion = '';
});

const submitApprove = async () => {
  if (!props.row)
    return;
  if (!approveForm.approveResult) {
    ElMessage.warning('请选择审批结论（通过 / 驳回）');
    return;
  }
  if (approveForm.approveResult === 2 && !approveForm.opinion.trim()) {
    ElMessage.warning('驳回必须写明原因（申请人要知道改什么才能重新提交）');
    return;
  }
  approveSubmitting.value = true;
  try {
    const res = await approveTransfusion({
      applyId: props.row.id,
      approveResult: approveForm.approveResult,
      opinion: approveForm.opinion.trim() || undefined,
    });
    ElMessage.success(res.message || '审批结论已提交');
    visible.value = false;
    emit('saved');
  } catch (error) {
    ElMessage.error(error.message || '审批提交失败');
  } finally {
    approveSubmitting.value = false;
  }
};
</script>
