<template>
  <!-- ==================== 录入缴回 ==================== -->
  <el-dialog v-model="visible" :title="`录入缴回 ${paybackForm.deductNo}`" data-testid="deduct-payback-dialog"
             width="600px">
    <el-alert :closable="false" :title="`缴回金额须等于扣款金额 ${money(paybackForm.deductAmount)}，差额走院内财务承担流程`" class="mb-3"
              type="warning"/>
    <el-form :model="paybackForm" label-width="110px">
      <el-form-item label="缴回金额" required>
        <el-input-number v-model="paybackForm.paidAmount" :controls="false" :min="0.01" :precision="2"
                         data-testid="deduct-payback-amount" style="width: 100%"/>
      </el-form-item>
      <el-form-item data-testid="deduct-payback-date" label="缴回日期" required>
        <el-date-picker v-model="paybackForm.paybackDate" style="width: 100%" type="date" value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item label="凭证号/流水" required>
        <el-input v-model="paybackForm.paybackVoucher" data-testid="deduct-payback-voucher"
                  placeholder="如 YB-PAY-20260918-0037"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="paybackSubmitting" data-testid="deduct-payback-submit" type="primary"
                 @click="submitPayback">确认缴回
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {paybackDeduct} from '@/api/insuranceDeduct';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  money: {type: Function, required: true},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const paybackSubmitting = ref(false);
const paybackForm = reactive({
  id: null,
  deductNo: '',
  deductAmount: 0,
  paidAmount: undefined,
  paybackDate: '',
  paybackVoucher: ''
});

watch(visible, (open) => {
  if (!open) return;
  const row = props.row || {};
  Object.assign(paybackForm, {
    id: row.id, deductNo: row.deductNo, deductAmount: Number(row.deductAmount || 0),
    paidAmount: Number(row.deductAmount || 0), paybackDate: '', paybackVoucher: ''
  });
});

const submitPayback = async () => {
  if (!paybackForm.paidAmount || !paybackForm.paybackDate || !paybackForm.paybackVoucher) {
    ElMessage.warning('缴回金额、缴回日期、凭证号都要填');
    return;
  }
  paybackSubmitting.value = true;
  try {
    await paybackDeduct({
      id: paybackForm.id, paidAmount: paybackForm.paidAmount,
      paybackDate: paybackForm.paybackDate, paybackVoucher: paybackForm.paybackVoucher
    });
    ElMessage.success('已录入缴回，这张扣款单闭环');
    visible.value = false;
    emit('saved');
  } catch (error) {
    // 缴回金额与实扣不符、未确认追责先缴回这类情况由后端判定，拒的原因必须原样回给用户，
    // 否则点「确定」后弹窗静止、用户以为页面坏了
    ElMessage.error(error?.message || '缴回登记失败');
  } finally {
    paybackSubmitting.value = false;
  }
};
</script>
