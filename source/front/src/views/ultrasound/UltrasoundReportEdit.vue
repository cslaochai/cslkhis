<template>
  <!-- 报告弹窗 -->
  <el-dialog v-model="visible" :title="`超声报告：${row?.recordNo || ''}`" width="680px">
    <el-form label-width="90px">
      <el-form-item label="超声所见" required>
        <el-input v-model="reportForm.findings" :rows="4" type="textarea"/>
      </el-form-item>
      <el-form-item label="超声提示" required>
        <el-input v-model="reportForm.conclusion" :rows="3" type="textarea"/>
      </el-form-item>
      <el-form-item label="建议">
        <el-input v-model="reportForm.suggestion" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'medtech:ultrasound:edit'" :loading="actLoading" type="primary" @click="saveReport">
        提交报告
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 超声报告（新增与修改共用一张表单：row 上已有所见/提示走修改，没有走新建，同一 report 接口）
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {ultrasoundReport} from '@/api/medicaltech';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const actLoading = ref(false);
const reportForm = reactive({findings: '', conclusion: '', suggestion: ''});

watch(visible, (open) => {
  if (!open) return;
  Object.assign(reportForm, {
    findings: props.row?.findings || '',
    conclusion: props.row?.conclusion || '',
    suggestion: props.row?.suggestion || '',
  });
});

const saveReport = async () => {
  actLoading.value = true;
  try {
    const res = await ultrasoundReport({recordId: props.row.id, ...reportForm});
    if (res.code === 200) {
      ElMessage.success(res.message || '操作成功');
      visible.value = false;
      emit('saved');
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    actLoading.value = false;
  }
};
</script>
