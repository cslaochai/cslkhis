<template>
  <!-- 变异登记弹窗 -->
  <el-dialog v-model="visible" data-testid="cp-variance-dialog" title="登记变异" width="620px">
    <el-form label-width="90px">
      <el-form-item label="路径日" required>
        <el-input-number v-model="varForm.dayNo" :max="maxDays || 60" :min="1" data-testid="cp-var-day"/>
      </el-form-item>
      <el-form-item label="变异类型" required>
        <el-select v-model="varForm.varianceType" :fit-input-width="false" class="w-56" data-testid="cp-var-type">
          <el-option v-for="d in varianceTypeDict" :key="d.dictValue" :label="d.dictLabel"
                     :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="发生日期" required>
        <el-date-picker v-model="varForm.occurredDate" :clearable="false" type="date" value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item label="变异原因" required>
        <el-input v-model="varForm.varianceReason" :rows="2" data-testid="cp-var-reason" maxlength="255" placeholder="实际诊疗如何偏离路径要求"
                  show-word-limit type="textarea"/>
      </el-form-item>
      <el-form-item label="处理措施">
        <el-input v-model="varForm.handling" :rows="2" maxlength="255" placeholder="选填" show-word-limit
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="varSaving" data-testid="cp-var-save-btn" type="primary" @click="saveVariance">保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 登记变异：变异是「入径单 + 路径日」下的一条留痕，所以入径 id、当前路径日、总日数由入径详情传进来。
 * 保存后由父级（入径详情）重取详情刷新变异台账。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {varianceUpsert} from '@/api/pathway';
import {localDateStr} from '@/lib/utils';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  enrollId: {type: [String, Number], default: ''},
  // 默认落在当前路径日，上限是路径总日数（越界的变异登记后端会拒）
  defaultDayNo: {type: Number, default: 1},
  maxDays: {type: Number, default: 60},
  varianceTypeDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const varSaving = ref(false);
const varForm = reactive({
  dayNo: 1,
  varianceType: 1,
  varianceReason: '',
  handling: '',
  occurredDate: '',
});

watch(visible, (open) => {
  if (!open) return;
  varForm.dayNo = Math.min(props.defaultDayNo || 1, props.maxDays || 1);
  varForm.varianceType = 1;
  varForm.varianceReason = '';
  varForm.handling = '';
  varForm.occurredDate = localDateStr();
});

const saveVariance = async () => {
  if (!varForm.varianceReason.trim()) {
    ElMessage.warning('变异原因必填');
    return;
  }
  varSaving.value = true;
  try {
    const res = await varianceUpsert({
      enrollId: props.enrollId,
      dayNo: Number(varForm.dayNo),
      varianceType: Number(varForm.varianceType),
      varianceReason: varForm.varianceReason.trim(),
      handling: varForm.handling.trim() || undefined,
      occurredDate: varForm.occurredDate,
    });
    if (res.code === 200) {
      ElMessage.success('变异已登记');
      visible.value = false;
      emit('saved');
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    varSaving.value = false;
  }
};
</script>
