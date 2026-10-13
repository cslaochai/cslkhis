<template>
  <el-dialog v-model="visible" data-testid="p4-tf-issue-dialog" title="发血" width="560px">
    <div v-if="row" class="space-y-3">
      <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm">
        <div class="text-slate-900">{{ text(row.patientName) }}（{{ text(row.admissionNo) }}）</div>
        <div class="mt-1 text-slate-700">
          受血者 {{ text(row.bloodTypeText) }} ·
          {{ text(row.bloodComponentText) }} · {{ text(row.bagProgressText) }}
        </div>
        <div class="text-[12px] text-slate-500">
          配血状态：{{ text(row.crossmatchStatusText) }} ·
          {{ text(row.crossmatchDoctorName) }} {{ fmt(row.crossmatchTime) }}
        </div>
      </div>
      <el-form label-width="90px">
        <el-form-item label="发血备注">
          <el-input v-model="issueForm.issueRemark" :rows="2" placeholder="如：已核对血袋外观完好（可空）"
                    type="textarea"/>
        </el-form-item>
      </el-form>
      <el-alert :closable="false" show-icon type="info">
        发血后状态变为「已发血」，血袋状态全部置为已发血；下一步由病区护士双人核对后开始输注。
      </el-alert>
    </div>
    <template #footer>
      <el-button v-perm="'medtech:transfusion:edit'" :loading="issueSubmitting" data-testid="p4-tf-issue-submit"
                 type="primary" @click="submitIssue">
        确认发血
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// ---------------- 三、发血 ----------------
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {issueTransfusion} from '@/api/inpatientTransfusion';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const fmt = (v) => (v ? String(v).replace('T', ' ') : '—');
const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));

const issueSubmitting = ref(false);
const issueForm = reactive({issueRemark: ''});

watch(visible, (open) => {
  if (!open) return;
  issueForm.issueRemark = '';
});

const submitIssue = async () => {
  if (!props.row)
    return;
  issueSubmitting.value = true;
  try {
    await issueTransfusion({
      applyId: props.row.id,
      issueRemark: issueForm.issueRemark.trim() || undefined,
    });
    ElMessage.success('已发血，请病区双人核对后输注');
    visible.value = false;
    emit('saved');
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    issueSubmitting.value = false;
  }
};
</script>
