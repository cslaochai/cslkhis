<template>
  <!-- ==================== 批次结项 ==================== -->
  <el-dialog v-model="visible" :title="`批次结项 ${concludeForm.inspectNo}`" data-testid="inspect-conclude-dialog"
             width="640px">
    <el-alert :closable="false" class="mb-3" title="结项后批次内容不可再改，结论会永久留档" type="info"/>
    <el-form :model="concludeForm" label-width="90px">
      <el-form-item label="结项结论" required>
        <el-input v-model="concludeForm.conclusion" :rows="4" data-testid="inspect-conclude-text"
                  placeholder="抽查多少份、发现几类问题、合计扣款多少、整改要求是什么"
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="concludeSubmitting" data-testid="inspect-conclude-submit" type="primary"
                 @click="submitConclude">确认结项
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {concludeYbInspection} from '@/api/insuranceDeduct';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const concludeSubmitting = ref(false);
const concludeForm = reactive({id: null, inspectNo: '', conclusion: ''});

watch(visible, (open) => {
  if (!open) return;
  Object.assign(concludeForm, {
    id: props.row?.id ?? null, inspectNo: props.row?.inspectNo ?? '', conclusion: ''
  });
});

const submitConclude = async () => {
  if (!concludeForm.conclusion) {
    ElMessage.warning('结项结论必填，飞检收尾要留下结论');
    return;
  }
  concludeSubmitting.value = true;
  try {
    await concludeYbInspection({id: concludeForm.id, conclusion: concludeForm.conclusion});
    ElMessage.success('批次已结项');
    visible.value = false;
    emit('saved');
  } catch (error) {
    ElMessage.error(error?.message || '结项失败');
  } finally {
    concludeSubmitting.value = false;
  }
};
</script>
