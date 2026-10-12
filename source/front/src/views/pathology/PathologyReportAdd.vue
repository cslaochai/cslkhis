<template>
  <!-- 初诊弹窗 -->
  <el-dialog v-model="visible" :title="`病理初诊：${reportRow?.orderNo || ''}`" width="680px">
    <el-form label-width="100px">
      <el-form-item label="肉眼所见">
        <el-input v-model="reportForm.grossFindings" :rows="2" type="textarea"/>
      </el-form-item>
      <el-form-item label="镜下所见">
        <el-input v-model="reportForm.microscopyFindings" :rows="3" type="textarea"/>
      </el-form-item>
      <el-form-item label="免疫组化/特染">
        <el-input v-model="reportForm.ihcResult" :rows="2" type="textarea"/>
      </el-form-item>
      <el-form-item v-if="reportRow && Number(reportRow.isFrozen) === 1" label="冰冻结果" required>
        <el-input v-model="reportForm.frozenResult" :rows="2" placeholder="术中冰冻快速诊断结果（冰冻单必填）"
                  type="textarea"/>
      </el-form-item>
      <el-form-item label="病理诊断" required>
        <el-input v-model="reportForm.diagnosis" :rows="3" type="textarea"/>
      </el-form-item>
      <el-form-item label="建议">
        <el-input v-model="reportForm.suggestion" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="'medtech:pathology:edit'" :loading="actLoading" type="primary" @click="saveReport">提交初诊
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 病理初诊报告录入：只能新增（提交后状态推到「待审核」，不能回改），
 * 所见/诊断/建议以详情接口为初值 —— 列表行不带这些字段，所以本组件按 orderId 自取详情。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getPathologyDetail, pathologyReport} from '@/api/medicaltech';

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
const reportForm = reactive({
  grossFindings: '',
  microscopyFindings: '',
  ihcResult: '',
  diagnosis: '',
  suggestion: '',
  frozenResult: ''
});
const actLoading = ref(false);

const fillForm = (d) => {
  Object.assign(reportForm, {
    grossFindings: d?.grossFindings || '', microscopyFindings: d?.microscopyFindings || '',
    ihcResult: d?.ihcResult || '', diagnosis: d?.diagnosis || '',
    suggestion: d?.suggestion || '', frozenResult: d?.frozenResult || '',
  });
};

watch(visible, async (open) => {
  if (!open) return;
  reportRow.value = props.row;
  fillForm(props.row);
  if (!props.row?.id) return;
  try {
    const res = await getPathologyDetail(props.row.id);
    if (res.code === 200 && res.data) {
      reportRow.value = res.data;
      fillForm(res.data);
    } else
      ElMessage.error(res.message || '查询详情失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询详情失败');
  }
});

const saveReport = async () => {
  actLoading.value = true;
  try {
    const res = await pathologyReport({orderId: reportRow.value.id, ...reportForm});
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
