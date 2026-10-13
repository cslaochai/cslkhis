<template>
  <!-- 报告弹窗 -->
  <el-dialog v-model="visible" :title="`内镜报告：${reportRow?.recordNo || ''}`" width="680px">
    <el-form label-width="90px">
      <el-form-item label="内镜所见" required>
        <el-input v-model="reportForm.findings" :rows="4" type="textarea"/>
      </el-form-item>
      <el-form-item label="内镜诊断" required>
        <el-input v-model="reportForm.diagnosis" :rows="3" type="textarea"/>
      </el-form-item>
      <el-form-item label="建议">
        <el-input v-model="reportForm.suggestion" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'medtech:endoscopy:edit'" :loading="actLoading" type="primary" @click="saveReport">提交报告
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 出内镜报告：所见/诊断/建议以行为初值（执行阶段录过的所见要能接着写），
 * 提交后端把状态推到「待审核」，审核人由服务端硬校验不能是报告人。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {endoscopyReport} from '@/api/medicaltech';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const reportRow = ref(null);
const reportForm = reactive({findings: '', diagnosis: '', suggestion: ''});
const actLoading = ref(false);

watch(visible, (open) => {
  if (!open) return;
  reportRow.value = props.row;
  Object.assign(reportForm, {
    findings: props.row?.findings || '',
    diagnosis: props.row?.diagnosis || '',
    suggestion: props.row?.suggestion || '',
  });
});

const saveReport = async () => {
  actLoading.value = true;
  try {
    const res = await endoscopyReport({recordId: reportRow.value.id, ...reportForm});
    if (res.code === 200) {
      ElMessage.success(res.message || '操作成功');
      visible.value = false;
      emit('saved');
    } else
      ElMessage.error(res.message || '操作失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('操作失败');
  } finally {
    actLoading.value = false;
  }
};
</script>
