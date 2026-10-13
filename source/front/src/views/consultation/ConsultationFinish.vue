<template>
  <!-- 完成会诊 -->
  <el-dialog v-model="visible" destroy-on-close title="完成会诊" width="600px">
    <el-form label-width="96px">
      <el-form-item label="会诊号">
        <el-input :model-value="finishForm.consultationNo" disabled/>
      </el-form-item>
      <el-form-item label="会诊时间">
        <el-date-picker
            v-model="finishForm.consultTime"
            class="w-full"
            placeholder="默认当前时间"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item label="会诊结论" required>
        <el-input
            v-model="finishForm.conclusion"
            :rows="5"
            data-testid="p4-finish-conclusion"
            maxlength="1000"
            placeholder="会诊意见与处理建议（完成后自动回写住院病历）"
            show-word-limit
            type="textarea"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'ipd:consultation:edit'" :loading="finishLoading" data-testid="p4-finish-submit"
                 type="primary" @click="submitFinish">完成并回写病历
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 完成会诊弹框：完成后端回写住院病历，提示带出返回的病历ID（证据链口径见列表页注释）。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {finishConsultation} from '@/api/inpatientConsultation';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 当前行（列表「完成」或详情「完成会诊」传入）
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const finishLoading = ref(false);
const finishForm = ref({consultationId: null, consultationNo: '', conclusion: '', consultTime: ''});
watch(visible, (open) => {
  if (open && props.row) {
    finishForm.value = {
      consultationId: props.row.consultationId,
      consultationNo: props.row.consultationNo,
      conclusion: '',
      consultTime: ''
    };
  }
});

const submitFinish = async () => {
  if (!finishForm.value.conclusion || !String(finishForm.value.conclusion).trim()) {
    ElMessage.warning('请填写会诊结论（「已完成」而没有结论，病历上等于什么都没发生）');
    return;
  }
  finishLoading.value = true;
  try {
    const res = await finishConsultation({
      consultationId: finishForm.value.consultationId,
      conclusion: finishForm.value.conclusion,
      consultTime: finishForm.value.consultTime || undefined,
    });
    ElMessage.success(`会诊已完成，已回写病历（病历ID ${res.data || '—'}）`);
    visible.value = false;
    emit('saved');
  } catch (error) {
    ElMessage.error(error.message || '完成会诊失败');
  } finally {
    finishLoading.value = false;
  }
};
</script>
