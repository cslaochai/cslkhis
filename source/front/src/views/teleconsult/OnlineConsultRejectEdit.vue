<template>
  <el-dialog v-model="visible" title="退诊" width="560px">
    <el-form label-width="100px" size="small">
      <el-form-item label="退诊原因" required>
        <el-input v-model="rejectReason" :rows="4" placeholder="原因必填（如：非本科室诊疗范围）" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="rejectLoading" type="primary" @click="submitReject">确定退诊</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 退诊（待接诊才可退诊是后端铁律，前端只在能点时给按钮）。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {rejectOnlineConsult} from '@/api/teleconsult';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const rejectReason = ref('');
const rejectLoading = ref(false);

watch(visible, (open) => {
  if (!open) return;
  rejectReason.value = '';
});

const submitReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写退诊原因');
    return;
  }
  rejectLoading.value = true;
  try {
    const res = await rejectOnlineConsult({id: props.row.id, content: rejectReason.value.trim()});
    if (res.code === 200) {
      ElMessage.success('已退诊');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '退诊失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('退诊失败');
  } finally {
    rejectLoading.value = false;
  }
};
</script>
