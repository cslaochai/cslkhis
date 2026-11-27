<template>
  <el-dialog v-model="visible" :title="opinionTitle" data-testid="correction-opinion-dialog" width="560px">
    <el-alert
        class="mb-3"
        :type="form.approved ? 'success' : 'warning'"
        :closable="false"
        :title="form.approved ? '通过后进入下一审批环节。' : '驳回后申请终态，申请人可按需重新发起。'"
    />
    <el-form label-width="90px">
      <el-form-item label="处理结论" required>
        <el-radio-group v-model="form.approved" data-testid="correction-opinion-approved">
          <el-radio :value="true">通过</el-radio>
          <el-radio :value="false">驳回</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="审批意见">
        <el-input
            v-model="form.comment"
            :rows="3"
            data-testid="correction-opinion-comment"
            maxlength="500"
            placeholder="审批意见（驳回时建议写明理由，可空）"
            show-word-limit
            type="textarea"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="saving" type="primary" @click="submit">确认</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {deptApproveCorrection, medicalApproveCorrection} from '@/api/emrCorrection';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 当前申请单行
  row: {type: Object, default: null},
  // dept=科室初审；medical=医务终审
  stage: {type: String, default: 'dept'},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const form = reactive({approved: true, comment: ''});

const opinionTitle = computed(() => (props.stage === 'dept' ? '科室审核' : '医务审批'));

watch(() => props.modelValue, (v) => {
  if (!v) return;
  form.approved = true;
  form.comment = '';
});

const submit = async () => {
  if (!props.row)
    return;
  saving.value = true;
  try {
    const payload = {
      id: props.row.id,
      approved: form.approved,
      comment: form.comment?.trim() || undefined,
    };
    if (props.stage === 'dept')
      await deptApproveCorrection(payload);
    else
      await medicalApproveCorrection(payload);
    ElMessage.success('审批完成');
    visible.value = false;
    emit('saved');
  } catch (error) {
    console.error('审批失败:', error);
  } finally {
    saving.value = false;
  }
};
</script>
