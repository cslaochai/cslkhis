<template>
  <!-- ==================== 确认扣款并追责 ==================== -->
  <el-dialog v-model="visible" :title="`确认扣款并追责 ${confirmForm.deductNo}`" data-testid="deduct-confirm-dialog"
             width="680px">
    <el-alert :closable="false" :title="`扣款金额 ${money(confirmForm.deductAmount)}；共担时科室+个人分摊之和不得超过它，差额视为院方承担`" class="mb-3"
              type="info"/>
    <el-form :model="confirmForm" label-width="110px">
      <el-form-item label="责任科室" required>
        <el-select v-model="confirmForm.liableDeptId" data-testid="deduct-confirm-dept" filterable style="width: 100%"
                   @change="onConfirmDept">
          <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="String(d.id)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="责任人">
        <el-input v-model="confirmForm.liableEmpName" placeholder="个人承担或共担时要写到人"/>
      </el-form-item>
      <el-form-item label="损失承担方式" required>
        <el-select v-model="confirmForm.lossBearType" data-testid="deduct-confirm-bear" style="width: 100%">
          <el-option v-for="d in dictOptions(DICT_TYPE.YB_LOSS_BEAR)" :key="d.dictValue" :label="d.dictLabel"
                     :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="科室承担">
            <el-input-number v-model="confirmForm.bearDeptAmount" :controls="false" :min="0" :precision="2"
                             data-testid="deduct-confirm-dept-amount" style="width: 100%"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="个人承担">
            <el-input-number v-model="confirmForm.bearEmpAmount" :controls="false" :min="0" :precision="2"
                             data-testid="deduct-confirm-emp-amount" style="width: 100%"/>
          </el-form-item>
        </el-col>
      </el-row>
      <div v-if="confirmForm.lossBearType === 4" class="text-xs text-slate-500 ml-[110px]">
        已分摊 {{ money(bearSum) }} / {{ money(confirmForm.deductAmount) }}
      </div>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="confirmSubmitting" data-testid="deduct-confirm-submit" type="primary"
                 @click="submitConfirm">确认扣款
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {DICT_TYPE} from '@/lib/dict-cache';
import {confirmDeduct} from '@/api/insuranceDeduct';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  dicts: {type: Object, default: () => ({})},
  deptOptions: {type: Array, default: () => []},
  money: {type: Function, required: true},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const dictOptions = (type) => props.dicts[type] || [];
const deptNameOf = (id) => props.deptOptions.find((d) => String(d.id) === String(id))?.deptName || '';

const confirmSubmitting = ref(false);
const confirmForm = reactive({
  id: null, deductNo: '', deductAmount: 0, liableDeptId: undefined,
  liableDeptName: '', liableEmpName: '', lossBearType: 2, bearDeptAmount: undefined, bearEmpAmount: undefined
});

watch(visible, (open) => {
  if (!open) return;
  const row = props.row || {};
  Object.assign(confirmForm, {
    id: row.id, deductNo: row.deductNo, deductAmount: Number(row.deductAmount || 0),
    liableDeptId: row.deptId ? String(row.deptId) : undefined, liableDeptName: row.deptName || '',
    liableEmpName: row.doctorName || '', lossBearType: row.lossBearType || 2,
    bearDeptAmount: row.bearDeptAmount ?? undefined, bearEmpAmount: row.bearEmpAmount ?? undefined
  });
  if (!confirmForm.liableDeptId) {
    confirmForm.liableDeptName = '';
  }
});

const onConfirmDept = (id) => {
  confirmForm.liableDeptName = id ? deptNameOf(id) : '';
};
const bearSum = computed(() => Number(confirmForm.bearDeptAmount || 0) + Number(confirmForm.bearEmpAmount || 0));
const submitConfirm = async () => {
  if (!confirmForm.liableDeptId) {
    ElMessage.warning('责任科室必选，追责追不到科室等于没追');
    return;
  }
  if (confirmForm.lossBearType === 4 && bearSum.value > confirmForm.deductAmount) {
    ElMessage.warning(`分摊合计 ${props.money(bearSum.value)} 已超过扣款 ${props.money(confirmForm.deductAmount)}，分摊不能超出实扣`);
    return;
  }
  confirmSubmitting.value = true;
  try {
    await confirmDeduct({
      id: confirmForm.id,
      liableDeptId: confirmForm.liableDeptId,
      liableDeptName: confirmForm.liableDeptName || undefined,
      liableEmpName: confirmForm.liableEmpName || undefined,
      lossBearType: confirmForm.lossBearType,
      bearDeptAmount: confirmForm.bearDeptAmount ?? undefined,
      bearEmpAmount: confirmForm.bearEmpAmount ?? undefined
    });
    ElMessage.success('已确认扣款并追责，转入待缴');
    visible.value = false;
    emit('saved');
  } catch (error) {
    ElMessage.error(error?.message || '确认追责失败');
  } finally {
    confirmSubmitting.value = false;
  }
};
</script>
