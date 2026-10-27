<template>
  <el-dialog v-model="visible" data-testid="bill-supplement-dialog" title="医保补结算" width="600px">
    <div v-loading="checking">
      <template v-if="check">
        <el-alert v-if="!checkOk" :closable="false" :title="check.errMsg || '该账单暂不能补结算'" type="error"/>
        <template v-else>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="账单号">{{ bill.billNo }}</el-descriptions-item>
            <el-descriptions-item label="患者">{{ bill.patientName }}</el-descriptions-item>
            <el-descriptions-item label="医保统筹支付">{{ formatMoney(check.insurancePay) }}</el-descriptions-item>
            <el-descriptions-item label="医保个账支付">{{ formatMoney(check.personalAccount) }}</el-descriptions-item>
            <el-descriptions-item label="个人自付">{{ formatMoney(check.selfPay) }}</el-descriptions-item>
            <el-descriptions-item label="拟退现金差额">
              <span class="font-medium text-rose-600">{{ formatMoney(refundAmount) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="医保试算单号" :span="2">{{ check.receiptNo || '正式结算后生成' }}</el-descriptions-item>
          </el-descriptions>
          <el-input v-model="reason" class="mt-3" data-testid="bill-supplement-reason" maxlength="100"
                    placeholder="补结算原因（选填）"/>
          <p class="mt-2 text-xs text-slate-500">
            确认后：医保平台按试算结果补扣个账并出正式结算单号，患者多付的差额（统筹 + 个账）按原收款流水
            原路退回，账单回写医保结算方式，票据按「钱票同真」口径同步作废待重开。
          </p>
        </template>
      </template>
    </div>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-if="checkOk" :loading="submitting" data-testid="bill-supplement-confirm" type="primary"
                 @click="submit">确认补结算
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 医保补结算弹框（SettlementBillView 操作列入口）：
 * 打开即调 supplementCheck 预检（2201 试算 + 校验），展示拟退差额，确认后执行 supplement。
 * 校验不过（无降级标记/未付清/停保/医保侧拒绝）只展示原因，不给确认按钮。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {checkSupplementInsurance, supplementInsurance} from '@/api/settlementBill';
import {formatMoney} from '@/lib/utils';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  bill: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const checking = ref(false);
const submitting = ref(false);
const check = ref(null);
const reason = ref('');

const checkOk = computed(() => check.value && Number(check.value.infcode) === 0);
const refundAmount = computed(() => {
  if (!checkOk.value) return 0;
  return Number(check.value.insurancePay || 0) + Number(check.value.personalAccount || 0);
});

watch(visible, (open) => {
  if (!open) return;
  check.value = null;
  reason.value = '';
  runCheck();
});

async function runCheck() {
  checking.value = true;
  try {
    const res = await checkSupplementInsurance(props.bill.id);
    check.value = res.data;
  } catch (e) /* 错误已由全局拦截器统一提示 */ {
    check.value = {infcode: -1, errMsg: ''};
  } finally {
    checking.value = false;
  }
}

async function submit() {
  submitting.value = true;
  try {
    await supplementInsurance(props.bill.id, reason.value || undefined);
    ElMessage.success('医保补结算完成，差额已原路退回');
    visible.value = false;
    emit('saved');
  } catch (e) /* 错误已由全局拦截器统一提示 */ {

  } finally {
    submitting.value = false;
  }
}
</script>
