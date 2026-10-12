<template>
  <!-- ==================== 录入申诉结果 ==================== -->
  <el-dialog v-model="visible" :title="`录入申诉结果 ${resultForm.deductNo}`" data-testid="deduct-result-dialog"
             width="600px">
    <el-form :model="resultForm" label-width="100px">
      <el-form-item label="申诉结果" required>
        <el-radio-group v-model="resultForm.appealResult" data-testid="deduct-result-radio">
          <el-radio :value="1">申诉成功（扣款撤销）</el-radio>
          <el-radio :value="2">申诉驳回（维持扣款待缴）</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="结果说明">
        <el-input v-model="resultForm.appealResultRemark" :rows="3" placeholder="照医保局回复原文写" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="resultSubmitting" data-testid="deduct-result-submit" type="primary"
                 @click="submitAppealResult">保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {appealResultDeduct} from '@/api/insuranceDeduct';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const resultSubmitting = ref(false);
const resultForm = reactive({id: null, deductNo: '', appealResult: 1, appealResultRemark: ''});

watch(visible, (open) => {
  if (!open) return;
  Object.assign(resultForm, {
    id: props.row?.id ?? null, deductNo: props.row?.deductNo ?? '', appealResult: 1, appealResultRemark: ''
  });
});

const submitAppealResult = async () => {
  resultSubmitting.value = true;
  try {
    await appealResultDeduct({
      id: resultForm.id,
      appealResult: resultForm.appealResult,
      appealResultRemark: resultForm.appealResultRemark || undefined
    });
    ElMessage.success(resultForm.appealResult === 1 ? '已记申诉成功，扣款撤销' : '已记申诉驳回，转入待缴');
    visible.value = false;
    emit('saved');
  } catch (error) {
    ElMessage.error(error?.message || '申诉结果录入失败');
  } finally {
    resultSubmitting.value = false;
  }
};
</script>
