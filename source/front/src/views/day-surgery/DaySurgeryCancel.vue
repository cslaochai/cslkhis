<template>
  <el-dialog v-model="visible" title="取消登记单" width="560px">
    <el-form label-width="100px" size="small">
      <el-form-item label="取消原因" required>
        <el-input v-model="cancelReason" :rows="4" placeholder="原因必填，将留痕" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="cancelLoading" type="primary" @click="submitCancel">确认取消</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {cancelDaySurgery} from '@/api/daySurgery';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const cancelReason = ref('');
const cancelLoading = ref(false);
watch(visible, (open) => {
  if (!open) return;
  cancelReason.value = '';
});
const submitCancel = async () => {
  if (!cancelReason.value.trim()) {
    ElMessage.warning('请填写取消原因');
    return;
  }
  cancelLoading.value = true;
  try {
    const res = await cancelDaySurgery({id: props.row.id, content: cancelReason.value.trim()});
    if (res.code === 200) {
      ElMessage.success('已取消');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '取消失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('取消失败');
  } finally {
    cancelLoading.value = false;
  }
};
</script>
