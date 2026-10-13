<template>
  <el-dialog v-model="visible" title="转普通住院" width="600px">
    <el-form label-width="120px" size="small">
      <el-form-item label="住院记录ID" required>
        <el-input v-model="trForm.transferAdmissionId" placeholder="biz_admission.admission_id（必填）"/>
      </el-form-item>
      <el-form-item label="转住院原因" required>
        <el-input v-model="trForm.transferRemark" :rows="3" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="trLoading" type="primary" @click="submitTransfer">确认转住院</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {transferDaySurgeryToIpd} from '@/api/daySurgery';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const trForm = reactive({transferAdmissionId: '', transferRemark: ''});
const trLoading = ref(false);
watch(visible, (open) => {
  if (!open) return;
  trForm.transferAdmissionId = '';
  trForm.transferRemark = '';
});
const submitTransfer = async () => {
  if (!trForm.transferAdmissionId) {
    ElMessage.warning('请填写转住院的住院号ID');
    return;
  }
  if (!trForm.transferRemark.trim()) {
    ElMessage.warning('请填写转住院原因');
    return;
  }
  trLoading.value = true;
  try {
    const res = await transferDaySurgeryToIpd({
      id: props.row.id,
      transferAdmissionId: trForm.transferAdmissionId,
      transferRemark: trForm.transferRemark.trim(),
    });
    if (res.code === 200) {
      ElMessage.success('已转住院');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '转住院失败');
    }
  } catch (e) {
    ElMessage.error('转住院失败');
  } finally {
    trLoading.value = false;
  }
};
</script>
