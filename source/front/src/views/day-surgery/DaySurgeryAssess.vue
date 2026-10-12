<template>
  <el-dialog v-model="visible" title="术前评估" width="600px">
    <el-form label-width="110px" size="small">
      <el-form-item label="评估结论" required>
        <el-radio-group v-model="evalForm.evalResult">
          <el-radio v-for="d in evalDict" :key="d.dictValue" :value="Number(d.dictValue)">{{ d.dictLabel }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="评估意见" required>
        <el-input v-model="evalForm.evalRemark" :rows="4" placeholder="禁忌筛查结论 / 适应证与麻醉评估（不通过不得安排手术）"
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="evalLoading" type="primary" @click="submitEval">提交评估</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {evaluateDaySurgery} from '@/api/daySurgery';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  evalDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const evalForm = reactive({evalResult: 1, evalRemark: ''});
const evalLoading = ref(false);
watch(visible, (open) => {
  if (!open) return;
  evalForm.evalResult = 1;
  evalForm.evalRemark = props.row?.evalRemark || '';
});
const submitEval = async () => {
  if (!evalForm.evalRemark.trim()) {
    ElMessage.warning('请填写评估意见（禁忌筛查结论要留痕）');
    return;
  }
  evalLoading.value = true;
  try {
    const res = await evaluateDaySurgery({
      id: props.row.id, evalResult: evalForm.evalResult, evalRemark: evalForm.evalRemark.trim(),
    });
    if (res.code === 200) {
      ElMessage.success(evalForm.evalResult === 1 ? '评估通过，可安排手术' : '评估不通过，不得安排日间手术');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '评估失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('评估失败');
  } finally {
    evalLoading.value = false;
  }
};
</script>
