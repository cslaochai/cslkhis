<template>
  <!-- 提交编码弹窗（表单与提交逻辑随弹框走） -->
  <el-dialog v-model="visible" :title="`提交编码 - ${row?.taskNo || ''}`" data-testid="ct-submit-dialog"
             width="560px">
    <div v-if="row" class="text-sm text-gray-600 mb-3">
      <div><span class="text-gray-400">病案：</span>{{ row.recordNo }} / {{
          row.patientName
        }}（{{ row.deptName || '—' }}）
      </div>
      <div v-if="row.diagnosis"><span class="text-gray-400">病历诊断：</span>{{ row.diagnosis }}</div>
    </div>
    <el-form label-width="100px">
      <el-form-item label="主诊断编码" required>
        <el-input v-model="submitForm.mainIcdCode" data-testid="ct-submit-code" maxlength="20"
                  placeholder="ICD-10 编码，如 I10.x00"/>
      </el-form-item>
      <el-form-item label="主诊断名称" required>
        <el-input v-model="submitForm.mainIcdName" data-testid="ct-submit-name" maxlength="200"
                  placeholder="如 原发性高血压"/>
      </el-form-item>
      <el-form-item label="其他诊断">
        <el-input v-model="submitForm.otherIcdText" :rows="3" maxlength="500" placeholder="其他诊断/手术 ICD，分号分隔（可空）"
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="'emr:codeTask:edit'" :loading="submitLoading" data-testid="ct-submit-save" type="primary"
                 @click="submitTask">提交
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {submitCodeTask} from '@/api/codeTask';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const submitForm = reactive({mainIcdCode: '', mainIcdName: '', otherIcdText: ''});
const submitLoading = ref(false);
watch(visible, (open) => {
  if (!open)
    return;
  submitForm.mainIcdCode = '';
  submitForm.mainIcdName = '';
  submitForm.otherIcdText = '';
});
const submitTask = async () => {
  if (!submitForm.mainIcdCode.trim() || !submitForm.mainIcdName.trim()) {
    ElMessage.warning('请填写主诊断 ICD 编码与名称');
    return;
  }
  submitLoading.value = true;
  try {
    const res = await submitCodeTask({
      id: props.row.id,
      mainIcdCode: submitForm.mainIcdCode.trim(),
      mainIcdName: submitForm.mainIcdName.trim(),
      otherIcdText: submitForm.otherIcdText.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success('编码已提交，等待审核');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '提交失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('提交失败');
  } finally {
    submitLoading.value = false;
  }
};
</script>
