<template>
  <!-- 结案 -->
  <el-dialog v-model="visible" title="结案" width="600px">
    <el-form label-width="110px" size="small">
      <el-form-item label="处理途径" required>
        <el-select v-model="closeForm.dealType" placeholder="请选择" style="width: 100%">
          <el-option v-for="d in dicts.dealType" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="责任认定" required>
        <el-select v-model="closeForm.dutyType" placeholder="请选择" style="width: 100%">
          <el-option v-for="d in dicts.duty" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="赔偿金额(元)" required>
        <el-input-number v-model="closeForm.compensation" :min="0" :precision="2" :step="100"/>
        <span class="hint">无赔偿请填 0</span>
      </el-form-item>
      <el-form-item label="结论" required>
        <el-input v-model="closeForm.conclusion" :rows="4" placeholder="调查结论 / 处理结果（将写入跟踪台账）"
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="closeLoading" type="primary" @click="submitClose">结案</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// 结案（四项必填：处理途径 + 责任认定 + 赔偿金额 + 结论）
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {closeDispute} from '@/api/dispute';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  dicts: {type: Object, default: () => ({dealType: [], duty: []})},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const closeForm = reactive({
  dealType: null,
  dutyType: null,
  compensation: 0,
  conclusion: '',
});
const closeLoading = ref(false);

watch(visible, (open) => {
  if (!open) return;
  closeForm.dealType = null;
  closeForm.dutyType = null;
  closeForm.compensation = 0;
  closeForm.conclusion = '';
});

const submitClose = async () => {
  if (!props.row) return;
  if (!closeForm.dealType || !closeForm.dutyType) {
    ElMessage.warning('请选择处理途径与责任认定');
    return;
  }
  if (closeForm.compensation == null || Number(closeForm.compensation) < 0) {
    ElMessage.warning('赔偿金额不能为负（无赔偿请填 0）');
    return;
  }
  if (!closeForm.conclusion.trim()) {
    ElMessage.warning('请填写调查结论/处理结果');
    return;
  }
  closeLoading.value = true;
  try {
    const res = await closeDispute({
      id: props.row.id,
      dealType: closeForm.dealType,
      dutyType: closeForm.dutyType,
      compensation: Number(closeForm.compensation),
      conclusion: closeForm.conclusion.trim(),
    });
    if (res.code === 200) {
      ElMessage.success('已结案');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '结案失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('结案失败');
  } finally {
    closeLoading.value = false;
  }
};
</script>

<style scoped>
.hint {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
