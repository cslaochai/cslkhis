<template>
  <el-dialog v-model="visible" :title="`失控处理：${row?.itemName || ''}`" width="560px">
    <div class="mb-2 text-sm">
      <el-tag size="small" type="danger">{{ row?.statusText }}</el-tag>
      <span class="ml-2 text-gray-500">Z={{ row?.zScore }}　命中：{{ row?.violatedRules || '-' }}</span>
    </div>
    <el-form label-width="100px">
      <el-form-item label="原因分析" required>
        <el-input v-model="form.handleCause" :rows="3" type="textarea"/>
      </el-form-item>
      <el-form-item label="纠正措施" required>
        <el-input v-model="form.handleMeasure" :rows="3" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'medtech:lisQc:edit'" :loading="saving" type="primary" @click="save">
        提交处理
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {qcHandle} from '@/api/medicaltech';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const form = reactive({handleCause: '', handleMeasure: ''});
watch(visible, (open) => {
  if (!open) return;
  const row = props.row;
  Object.assign(form, {handleCause: row.handleCause || '', handleMeasure: row.handleMeasure || ''});
});
const save = async () => {
  if (!form.handleCause.trim() || !form.handleMeasure.trim()) {
    ElMessage.warning('失控原因与纠正措施必填');
    return;
  }
  saving.value = true;
  try {
    const res = await qcHandle({
      recordId: props.row.id,
      handleCause: form.handleCause.trim(),
      handleMeasure: form.handleMeasure.trim()
    });
    if (res.code === 200) {
      ElMessage.success('已处理，待第二人复核');
      visible.value = false;
      emit('saved');
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    saving.value = false;
  }
};
</script>
