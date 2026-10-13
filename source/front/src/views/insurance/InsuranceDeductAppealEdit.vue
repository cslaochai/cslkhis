<template>
  <!-- ==================== 发起申诉 ==================== -->
  <el-dialog v-model="visible" :title="`发起申诉 ${appealForm.deductNo}`" data-testid="deduct-appeal-dialog"
             width="640px">
    <el-form :model="appealForm" label-width="100px">
      <el-form-item label="申诉理由" required>
        <el-input v-model="appealForm.appealReason" :rows="3" data-testid="deduct-appeal-reason"
                  placeholder="为什么认为这笔扣款不该扣，写事实不写情绪" type="textarea"/>
      </el-form-item>
      <el-form-item label="申诉材料">
        <el-input v-model="appealForm.appealMaterial" :rows="2" placeholder="附了哪些单据/截图/签字件"
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="appealSubmitting" data-testid="deduct-appeal-submit" type="primary" @click="submitAppeal">
        提交申诉
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {appealDeduct} from '@/api/insuranceDeduct';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const appealSubmitting = ref(false);
const appealForm = reactive({id: null, deductNo: '', appealReason: '', appealMaterial: ''});

watch(visible, (open) => {
  if (!open) return;
  Object.assign(appealForm, {
    id: props.row?.id ?? null, deductNo: props.row?.deductNo ?? '', appealReason: '', appealMaterial: ''
  });
});

const submitAppeal = async () => {
  if (!appealForm.appealReason) {
    ElMessage.warning('申诉理由必填，医保局看的就是这一句');
    return;
  }
  appealSubmitting.value = true;
  try {
    await appealDeduct({
      id: appealForm.id,
      appealReason: appealForm.appealReason,
      appealMaterial: appealForm.appealMaterial || undefined
    });
    ElMessage.success('已提交申诉，状态转为申诉中');
    visible.value = false;
    emit('saved');
  } catch (error) {
    ElMessage.error(error?.message || '申诉提交失败');
  } finally {
    appealSubmitting.value = false;
  }
};
</script>
