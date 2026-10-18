<template>
  <el-dialog v-model="visible" destroy-on-close title="发起退费申请" width="880px">
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
          只列收过钱的账单（已支付 / 已退费）。
        </div>
      </el-form-item>
      <el-form-item label="退费类型" required>
        <el-select v-model="applyForm.refundType" class="w-full" data-testid="refund-type-select" placeholder="请选择"
                   @change="onRefundTypeChange">
          <el-option
              v-for="o in refundTypeOptions"
              :key="o.dictValue"
              :label="o.dictLabel"
              :value="Number(o.dictValue)"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <!-- 候选行：按来源单号（处方号/申请单号）圈选，每行可改退费数量（共 5 盒可只退 2 盒）。
         默认全选 = 该类型整类退；取消勾选、改数量都是部分退。 -->
    <div v-if="lineLoading || candidateLines.length > 0" class="mt-1">
      <div class="mb-1 flex items-center justify-between">
        <span class="text-sm font-medium">可退明细（勾选要退的行，数量可改）</span>
        <span class="text-sm text-slate-500">
          合计退费 <span class="font-semibold text-slate-800" data-testid="refund-selected-total">¥{{ formatMoney(selectedTotal) }}</span>
        </span>
      </div>
      <el-table v-loading="lineLoading" :data="candidateLines" border data-testid="refund-line-table" max-height="280"
                size="small">
        <el-table-column width="42">
          <template #default="{ row }">
            <el-checkbox v-model="row.checked" data-testid="refund-line-check"/>
          </template>
        </el-table-column>
        <el-table-column label="项目" min-width="140" prop="itemName" show-overflow-tooltip/>
        <el-table-column label="来源" width="90">
          <template #default="{ row }">
            <span class="text-xs text-slate-500">{{ sourceTypeText(row.sourceType) }}</span>
          </template>
        </el-table-column>
        <el-table-column class-name="font-mono text-xs" label="来源单号" min-width="140" prop="sourceNo"
                         show-overflow-tooltip>
          <template #default="{ row }">{{ row.sourceNo || '—' }}</template>
        </el-table-column>
        <el-table-column align="right" label="剩余可退" width="100">
          <template #default="{ row }">{{ row.quantity }} {{ row.unit }}</template>
        </el-table-column>
        <el-table-column align="right" label="可退金额" width="90">
          <template #default="{ row }">¥{{ formatMoney(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="退费数量" width="140">
          <template #default="{ row }">
            <el-input-number
                v-model="row.refundQty"
                :max="row.quantity"
                :min="0.01"
                :precision="2"
                :step="1"
                class="!w-full"
                data-testid="refund-qty-input"
                size="small"
            />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-form class="mt-2" label-width="96px">
      <el-form-item label="退费原因" required>
        <el-input v-model="applyForm.refundReason" :rows="3" placeholder="请填写退费原因" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'finance:refund:add'" :loading="applySubmitting" data-testid="refund-apply-submit"
                 type="primary" @click="submitApply">
        提交申请
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, onMounted, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getRefundableLines, submitRefundApply} from '@/api/refund';
import {getBillListPage} from '@/api/settlementBill';
import {getDictDataMapList} from '@/api/system';
import PatientSelect from '@/components/his/PatientSelect.vue';
import {dictLabelText, formatMoney} from '@/lib/utils';

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

// 候选行：来源类型字典只本弹框用，随组件自加载（列表列不渲染它）
const feeSourceOptions = ref([]);
const sourceTypeText = (v) => dictLabelText(feeSourceOptions.value, v);

// 候选行（可退明细）：checked = 勾选，refundQty = 本行退费数量（默认剩余量）
const candidateLines = ref([]);
const lineLoading = ref(false);

watch(visible, (open) => {
  if (!open) return;
  applyForm.patientId = null;
  applyForm.billId = null;
  applyForm.billNo = '';
  applyForm.refundType = null;
  applyForm.refundAmount = null;
  applyForm.refundReason = '';
  billOptions.value = [];
  candidateLines.value = [];
  selectedPatient.value = null;
});

onMounted(async () => {
  try {
    const res = await getDictDataMapList('his_fee_source_type');
    if (res.code === 200 && res.data) {
      feeSourceOptions.value = res.data['his_fee_source_type'] || [];
    }
  } catch (e) {
    // 字典拿不到 → 来源列渲染「未知(n)」，不阻塞圈选
    console.error('加载费用来源字典失败', e);
  }
});

async function onPatientSelect(patient) {
  selectedPatient.value = patient || null;
  applyForm.patientId = patient?.id ?? null;
  applyForm.billId = null;
  applyForm.billNo = '';
  billOptions.value = [];
  candidateLines.value = [];
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
  loadCandidateLines();
}

function onRefundTypeChange() {
  loadCandidateLines();
}

async function loadCandidateLines() {
  candidateLines.value = [];
  if (!applyForm.billId || applyForm.refundType === null)
    return;
  lineLoading.value = true;
  try {
    const res = await getRefundableLines(applyForm.billId, applyForm.refundType);
    candidateLines.value = (res.data || []).map(l => ({
      ...l,
      checked: true,
      // 默认退整行（剩余量全退）；要部分退就改小（共 5 盒退 2 盒）
      refundQty: l.quantity === null || l.quantity === undefined ? null : Number(l.quantity),
    }));
    if (candidateLines.value.length === 0) {
      ElMessage.warning('该账单没有此类型的可退明细（可能已全部退过）');
    }
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{

  } finally {
    lineLoading.value = false;
  }
}

/** 勾选/改数量后合计自动重算：整行按可退金额，部分行按单价×数量（与后端按行快照汇总同口径） */
const selectedTotal = computed(() => {
  let total = 0;
  for (const row of candidateLines.value) {
    if (!row.checked)
      continue;
    const qty = Number(row.refundQty || 0);
    const remaining = Number(row.quantity || 0);
    const amount = qty >= remaining ? Number(row.amount || 0) : Number(row.price || 0) * qty;
    total += amount;
  }
  return Math.round(total * 100) / 100;
});

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
  const chosen = candidateLines.value.filter(l => l.checked);
  if (candidateLines.value.length > 0 && chosen.length === 0) {
    ElMessage.warning('请至少勾选一行要退的明细');
    return;
  }
  if (chosen.some(l => !l.refundQty || Number(l.refundQty) <= 0)) {
    ElMessage.warning('退费数量必须大于 0');
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
      // 合计只作展示：服务端按行快照汇总覆盖，前端算的数不作数
      refundAmount: selectedTotal.value,
      // 圈选结果：退了哪几行、各退多少数量；refundQuantity 已达剩余量时等价整行退
      items: chosen.map(l => ({
        feeRecordId: l.feeRecordId,
        refundQuantity: l.refundQty,
      })),
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
