<template>
  <!-- 按出院记录生成 -->
  <el-dialog v-model="visible" title="按出院记录生成随访计划" width="520px">
    <el-form label-width="90px">
      <el-form-item label="出院记录ID" required>
        <el-input v-model="genForm.dischargeId" placeholder="出院记录ID（biz_discharge）"/>
      </el-form-item>
      <el-form-item label="随访类型">
        <el-select v-model="genForm.followupType" :fit-input-width="false" style="width: 220px">
          <el-option v-for="d in typeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="随访天数">
        <el-input-number v-model="genForm.daysOffset" :min="1" :precision="0"/>
        <span class="hint">计划时间 = 出院时间 + N 天（默认 7）</span>
      </el-form-item>
      <el-form-item label="内容覆盖">
        <el-input v-model="genForm.followupContent" :rows="2" placeholder="留空则按出院诊断自动生成" type="textarea"/>
      </el-form-item>
    </el-form>
    <div class="text-xs text-gray-400">
      日常不需要手工点这个：出院办理会自动建一条（有手术记录 → 术后随访，否则复诊提醒；死亡病例不建）。
      这里服务的是自动建单失败后的补录，以及慢病/用药指导这类额外计划。
    </div>
    <template #footer>
      <el-button v-perm="'inpatient:followup:add'" :loading="genLoading" type="primary" @click="submitGenerate">生成
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 按出院记录一键生成随访计划（幂等：同一出院记录重复生成返回已生成任务）。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {followupCreateFromDischarge} from '@/api/followup';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 随访类型字典由页面加载一次传下来（列表列同一份）
  typeDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const genLoading = ref(false);
const genForm = reactive({dischargeId: '', followupType: 1, daysOffset: 7, followupContent: ''});

watch(visible, (open) => {
  if (!open) return;
  Object.assign(genForm, {dischargeId: '', followupType: 1, daysOffset: 7, followupContent: ''});
});

const submitGenerate = async () => {
  if (!genForm.dischargeId) {
    ElMessage.warning('请填写出院记录ID');
    return;
  }
  genLoading.value = true;
  try {
    const res = await followupCreateFromDischarge({
      dischargeId: genForm.dischargeId,
      followupType: genForm.followupType,
      daysOffset: genForm.daysOffset,
      followupContent: genForm.followupContent || undefined,
    });
    if (res.code === 200) {
      ElMessage.success('随访计划已生成（同一出院记录重复生成会返回已生成任务）');
      visible.value = false;
      emit('saved');
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    genLoading.value = false;
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
