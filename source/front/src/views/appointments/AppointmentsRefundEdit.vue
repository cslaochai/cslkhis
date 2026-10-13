<template>
  <el-dialog v-model="visible" destroy-on-close title="申请退费" width="520px">
    <el-form class="space-y-2" label-width="96px">
      <el-form-item label="患者">
        <span class="text-sm text-slate-800">{{ refundForm.patientName || '-' }}</span>
        <span class="ml-3 font-mono text-xs text-slate-500">{{ refundForm.registNo }}</span>
      </el-form-item>
      <el-form-item label="原收费单">
        <span class="font-mono text-xs text-slate-600">{{ refundForm.billNo || '-' }}</span>
      </el-form-item>
      <el-form-item label="退费类型" required>
        <el-select v-model="refundForm.refundType" :fit-input-width="false" class="!w-full" placeholder="请选择">
          <el-option v-for="o in refundTypeOptions" :key="o.dictValue" :label="o.dictLabel"
                     :value="Number(o.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="退费金额" required>
        <el-input-number v-model="refundForm.refundAmount" :max="refundForm.refundableAmount" :min="0" :precision="2"
                         :step="1" class="!w-full"/>
        <p class="mt-1 text-xs text-slate-400">可退金额 ¥{{
            refundForm.refundableAmount
          }}（实付扣掉已退部分），可改小做部分退费</p>
      </el-form-item>
      <el-form-item label="退费原因" required>
        <el-input v-model="refundForm.refundReason" :rows="3" placeholder="如：患者当日未到诊，申请退还挂号费"
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'finance:refund:add'" :loading="refundSubmitting" type="primary" @click="handleSubmitRefund">
        提交申请
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getDictDataMapList} from '@/api/system';
import {submitRefundApply} from '@/api/refund';

// 申请退费弹框（从 AppointmentsView 拆出）。
// 可退金额是账单事实：主页面查账单、判完「有没有得退」才打开本窗，initial 带进来的
// refundableAmount 已是净可退额；这里只做表单与提交，不再碰账单接口。
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 打开前的表单初始值：{patientName, registNo, patientId, patientNo, billId, billNo, refundableAmount} */
  initial: {type: Object, default: () => ({})},
});
// 原实现提交成功后只关窗、不刷列表（退费要走收费处审核，挂号状态没变），契约保持不变
const emit = defineEmits(['update:modelValue']);
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});
const refundSubmitting = ref(false);
const refundTypeOptions = ref([]);
const refundForm = ref({
  patientName: '',
  registNo: '',
  patientId: null,
  patientNo: '',
  billId: null,
  billNo: '',
  refundableAmount: 0,
  refundType: null,
  refundAmount: null,
  refundReason: '',
});
/** 退费类型字典只在第一次打开弹框时拉，之后复用（与「退费管理」页同一份 his_refund_apply_type） */
const loadRefundTypeOptions = async () => {
  if (refundTypeOptions.value.length)
    return;
  try {
    const res = await getDictDataMapList('his_refund_apply_type');
    refundTypeOptions.value = res.data?.['his_refund_apply_type'] || [];
  } catch (error) {
    // 字典拿不到不拦提交：类型是必填项，下拉空了用户自然会问，比静默给个假选项好
    console.error('加载退费类型字典失败:', error);
  }
};
watch(visible, (open) => {
  if (!open)
    return;
  const src = props.initial || {};
  refundForm.value = {
    patientName: src.patientName || '',
    registNo: src.registNo || '',
    patientId: src.patientId,
    patientNo: src.patientNo || '',
    billId: src.billId,
    billNo: src.billNo || '',
    refundableAmount: src.refundableAmount,
    // 挂号单是整单退费，默认带出「全部退费」(5)，允许改
    refundType: 5,
    refundAmount: src.refundableAmount,
    refundReason: '',
  };
  loadRefundTypeOptions();
});
const handleSubmitRefund = async () => {
  if (refundForm.value.refundType === null) {
    ElMessage.warning('请选择退费类型');
    return;
  }
  const amount = Number(refundForm.value.refundAmount);
  if (refundForm.value.refundAmount === null || Number.isNaN(amount) || amount <= 0) {
    ElMessage.warning('请填写正确的退费金额');
    return;
  }
  if (amount > refundForm.value.refundableAmount) {
    ElMessage.warning(`退费金额不能超过本单可退金额 ¥${refundForm.value.refundableAmount}`);
    return;
  }
  if (!refundForm.value.refundReason.trim()) {
    ElMessage.warning('请填写退费原因');
    return;
  }
  refundSubmitting.value = true;
  try {
    await submitRefundApply({
      billId: refundForm.value.billId,
      billNo: refundForm.value.billNo,
      patientId: refundForm.value.patientId,
      patientNo: refundForm.value.patientNo,
      patientName: refundForm.value.patientName,
      refundType: refundForm.value.refundType,
      refundReason: refundForm.value.refundReason.trim(),
      refundAmount: amount,
      // applyBy 不从前端编造，后端按当前登录人回填
    });
    ElMessage.success('退费申请已提交，请到收费处「退费管理」审核并执行');
    visible.value = false;
  } catch (error) {
    ElMessage.error(error.message || '提交退费申请失败');
  } finally {
    refundSubmitting.value = false;
  }
};
</script>
