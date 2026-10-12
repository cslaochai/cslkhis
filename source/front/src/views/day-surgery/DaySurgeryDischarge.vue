<template>
  <el-dialog v-model="visible" title="离院登记" width="600px">
    <el-form label-width="110px" size="small">
      <el-form-item label="离院方式" required>
        <el-radio-group v-model="disForm.leaveType">
          <el-radio :value="1">按时离院</el-radio>
          <el-radio :value="3">非计划再入院</el-radio>
        </el-radio-group>
        <div class="hint-line">转普通住院请走「转住院」动作（必须回填住院号）</div>
      </el-form-item>
      <el-form-item label="离院时间">
        <el-date-picker v-model="disForm.dischargeTime" placeholder="不填取当前时间" style="width: 100%"
                        type="datetime" value-format="YYYY-MM-DD HH:mm"/>
      </el-form-item>
      <el-form-item label="出院评估">
        <el-input v-model="disForm.dischargeRemark" :rows="3" placeholder="出院评估结论 / 医嘱交代" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="disLoading" type="primary" @click="submitDischarge">登记离院</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {dischargeDaySurgery} from '@/api/daySurgery';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const disForm = reactive({leaveType: 1, dischargeTime: '', dischargeRemark: ''});
const disLoading = ref(false);
watch(visible, (open) => {
  if (!open) return;
  disForm.leaveType = 1;
  disForm.dischargeTime = '';
  disForm.dischargeRemark = '';
});
const submitDischarge = async () => {
  disLoading.value = true;
  try {
    const res = await dischargeDaySurgery({
      id: props.row.id,
      leaveType: disForm.leaveType,
      dischargeTime: disForm.dischargeTime
          ? (disForm.dischargeTime.length === 16 ? `${disForm.dischargeTime}:00` : disForm.dischargeTime)
          : undefined,
      dischargeRemark: disForm.dischargeRemark.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success('离院已登记，请在 24 小时内完成随访');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '登记失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('登记失败');
  } finally {
    disLoading.value = false;
  }
};
</script>

<style scoped>
.hint-line {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
