<template>
  <!-- 取消会诊 -->
  <el-dialog v-model="visible" destroy-on-close title="取消会诊申请" width="520px">
    <el-form label-width="96px">
      <el-form-item label="会诊号">
        <el-input :model-value="cancelForm.consultationNo" disabled/>
      </el-form-item>
      <el-form-item label="取消原因" required>
        <el-input v-model="cancelForm.cancelReason" :rows="3" data-testid="p4-cancel-reason" maxlength="500"
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'ipd:consultation:delete'" :loading="cancelLoading" data-testid="p4-cancel-submit"
                 type="danger" @click="submitCancel">确认取消
      </el-button>
      <el-button @click="visible = false">返回</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 取消会诊申请弹框：取消是一个临床决定，原因必填留痕（后端铁律，前端只按 canCancel 渲染入口）。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {cancelConsultation} from '@/api/inpatientConsultation';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 当前行
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const cancelLoading = ref(false);
const cancelForm = ref({consultationId: null, consultationNo: '', cancelReason: ''});
watch(visible, (open) => {
  if (open && props.row) {
    cancelForm.value = {consultationId: props.row.consultationId, consultationNo: props.row.consultationNo, cancelReason: ''};
  }
});

const submitCancel = async () => {
  if (!cancelForm.value.cancelReason || !String(cancelForm.value.cancelReason).trim()) {
    ElMessage.warning('请填写取消原因（取消是一个临床决定，必须有人负责）');
    return;
  }
  cancelLoading.value = true;
  try {
    await cancelConsultation({
      consultationId: cancelForm.value.consultationId,
      cancelReason: cancelForm.value.cancelReason,
    });
    ElMessage.success('会诊申请已取消');
    visible.value = false;
    emit('saved');
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    cancelLoading.value = false;
  }
};
</script>
