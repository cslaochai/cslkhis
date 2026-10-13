<template>
  <el-dialog v-model="visible" destroy-on-close title="发起退费申请" width="560px">
    <el-form class="space-y-2" label-width="96px">
      <el-form-item label="患者" required>
        <PatientSelect v-model="applyForm.patientId" @select="onPatientSelect"/>
      </el-form-item>
      <el-form-item label="结算账单" required>
        <el-select
            v-model="applyForm.billId"
            :disabled="!applyForm.patientId"
            :fit-input-width="false"
            :loading="billLoading"
            class="w-full"
            data-testid="refund-bill-select"
            filterable
            placeholder="请先选择患者"
            @change="onBillChange"
        >
          <el-option
              v-for="b in billOptions"
              :key="b.id"
              :label="`${b.billNo}（应收 ¥${formatMoney(b.payableAmount)}，已收 ¥${formatMoney(b.paidAmount)}）`"
              :value="b.id"
          />
        </el-select>
        <div class="w-full mt-1 text-xs text-slate-400">
          只列收过钱的账单（已支付 / 已退费）；金额必须落在账单行边界上，后端会列出可选金额。
        </div>
      </el-form-item>
      <el-form-item label="退费类型" required>
        <el-select v-model="applyForm.refundType" class="w-full" placeholder="请选择">
          <el-option
              v-for="o in refundTypeOptions"
              :key="o.dictValue"
              :label="o.dictLabel"
              :value="Number(o.dictValue)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="退费金额" required>
        <el-input-number v-model="applyForm.refundAmount" :min="0" :precision="2" :step="1" class="!w-full"/>
      </el-form-item>
      <el-form-item label="退费原因" required>
        <el-input v-model="applyForm.refundReason" :rows="3" placeholder="请填写退费原因" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'finance:refund:add'" :loading="applySubmitting" type="primary" @click="submitApply">
        提交申请
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {submitRefundApply} from '@/api/refund';
import {getBillListPage} from '@/api/settlementBill';
import PatientSelect from '@/components/his/PatientSelect.vue';
import {formatMoney} from '@/lib/utils';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 退费类型字典由页面一次性加载（列表列也在用），这里只读取
  refundTypeOptions: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const applySubmitting = ref(false);
const billLoading = ref(false);
const billOptions = ref([]);
const selectedPatient = ref(null);
const applyForm = reactive({
  patientId: null,
  billId: null,
  billNo: '',
  refundType: null,
  refundAmount: null,
  refundReason: '',
});

watch(visible, (open) => {
  if (!open) return;
  applyForm.patientId = null;
  applyForm.billId = null;
  applyForm.billNo = '';
  applyForm.refundType = null;
  applyForm.refundAmount = null;
  applyForm.refundReason = '';
  billOptions.value = [];
  selectedPatient.value = null;
});

async function onPatientSelect(patient) {
  selectedPatient.value = patient || null;
  applyForm.patientId = patient?.id ?? null;
  applyForm.billId = null;
  applyForm.billNo = '';
  applyForm.refundAmount = null;
  billOptions.value = [];
  if (!applyForm.patientId)
    return;
  billLoading.value = true;
  try {
    // 四层口径：退费锚点是结算账单（L2）。收过钱的账单才有钱可退：
    // 3-已支付 全额可退，5-已退费 还能续退；1/2 没收齐、4 已作废（没收过钱）不可退。
    const res = await getBillListPage({
      patientId: applyForm.patientId,
      billStatusList: [3, 5],
      pageNum: 1,
      pageSize: 200,
    });
    billOptions.value = res.data?.records || [];
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    billLoading.value = false;
  }
}

function onBillChange(billId) {
  const hit = billOptions.value.find(b => String(b.id) === String(billId));
  applyForm.billNo = hit?.billNo || '';
  // 默认带出已收合计，允许改小；金额必须落在账单行边界上（后端按整行退校验，报错会列出可选金额）
  const paid = hit?.paidAmount;
  applyForm.refundAmount = paid === null || paid === undefined ? null : Number(paid);
}

async function submitApply() {
  if (!applyForm.patientId) {
    ElMessage.warning('请先选择患者');
    return;
  }
  if (!applyForm.billId) {
    ElMessage.warning('请选择原结算账单');
    return;
  }
  if (applyForm.refundType === null) {
    ElMessage.warning('请选择退费类型');
    return;
  }
  if (applyForm.refundAmount === null || Number(applyForm.refundAmount) < 0) {
    ElMessage.warning('请填写正确的退费金额');
    return;
  }
  if (!applyForm.refundReason.trim()) {
    ElMessage.warning('请填写退费原因');
    return;
  }
  applySubmitting.value = true;
  try {
    await submitRefundApply({
      billId: applyForm.billId,
      billNo: applyForm.billNo,
      patientId: applyForm.patientId,
      patientNo: selectedPatient.value?.patientNo,
      patientName: selectedPatient.value?.name,
      refundType: applyForm.refundType,
      refundReason: applyForm.refundReason.trim(),
      refundAmount: applyForm.refundAmount,
      // applyBy 不从前端编造，后端按当前登录人回填
    });
    ElMessage.success('退费申请已提交，等待审核');
    visible.value = false;
    emit('saved');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    applySubmitting.value = false;
  }
}
</script>
