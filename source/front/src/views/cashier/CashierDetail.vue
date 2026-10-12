<template>
  <el-dialog v-model="visible" title="账单详情" width="960px">
    <div v-if="detail" v-loading="loading" class="space-y-3">
      <el-descriptions :column="4" border size="small">
        <el-descriptions-item label="账单号">{{ detail.billNo }}</el-descriptions-item>
        <el-descriptions-item label="患者">{{ detail.patientName }} {{ detail.patientNo }}</el-descriptions-item>
        <el-descriptions-item label="就诊单号">{{ detail.encounterNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="billStatusTag(detail.billStatus)" size="small">{{
              billStatusText(detail.billStatus)
            }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="账单类型">{{ billTypeText(detail.billType) }}</el-descriptions-item>
        <el-descriptions-item label="结算方式">
          {{ Number(detail.settlementMode) === 2 ? `医保 ${detail.insuranceType || ''}` : '自费' }}
        </el-descriptions-item>
        <el-descriptions-item label="应收 / 优惠">{{ money(detail.totalAmount) }} / {{
            money(detail.discountAmount)
          }}
        </el-descriptions-item>
        <el-descriptions-item label="统筹 / 个账">{{ money(detail.poolAmount) }} / {{
            money(detail.accountAmount)
          }}
        </el-descriptions-item>
        <el-descriptions-item label="应缴">{{ money(detail.payableAmount) }}</el-descriptions-item>
        <el-descriptions-item label="已收 / 已退">{{ money(detail.paidAmount) }} / {{
            money(detail.refundAmount)
          }}
        </el-descriptions-item>
        <el-descriptions-item label="尚需缴">{{ money(detail.unpaidAmount) }}</el-descriptions-item>
        <el-descriptions-item label="收讫时间">{{ detail.payTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="出账人">{{ detail.billByName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="出账时间">{{ detail.billTime }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="结清说明">{{ detail.closeReason || '—' }}</el-descriptions-item>
      </el-descriptions>

      <div>
        <h4 class="mb-1 text-sm font-bold text-slate-700">账单行快照（出账时冻结，不随记账行变化）</h4>
        <el-table :data="detail.items || []" max-height="240" size="small">
          <el-table-column label="项目" min-width="180" prop="itemName" show-overflow-tooltip/>
          <el-table-column label="科室" prop="deptName" show-overflow-tooltip width="120"/>
          <el-table-column align="right" label="数量×单价" width="130">
            <template #default="{ row }">{{ row.quantity }} × {{ money(row.price) }}</template>
          </el-table-column>
          <el-table-column align="right" label="金额" width="100">
            <template #default="{ row }">{{ money(row.amount) }}</template>
          </el-table-column>
          <el-table-column align="right" label="统筹" width="90">
            <template #default="{ row }">{{ money(row.poolAmount) }}</template>
          </el-table-column>
          <el-table-column align="right" label="自付" width="90">
            <template #default="{ row }">{{ money(row.selfAmount) }}</template>
          </el-table-column>
        </el-table>
      </div>

      <div>
        <h4 class="mb-1 text-sm font-bold text-slate-700">全部收/退流水（资金事实的唯一来源）</h4>
        <el-table :data="detail.txns || []" max-height="220" size="small">
          <el-table-column label="流水号" prop="txnNo" width="170"/>
          <el-table-column align="center" label="方向" width="70">
            <template #default="{ row }">{{ Number(row.direction) === 1 ? '收款' : '退款' }}</template>
          </el-table-column>
          <el-table-column label="方式" width="100">
            <template #default="{ row }">{{ payMethodText(row.payMethod) }}</template>
          </el-table-column>
          <el-table-column align="right" label="金额" width="110">
            <template #default="{ row }">
              <span :class="Number(row.amount) < 0 ? 'text-emerald-600' : 'font-medium'">{{
                  money(row.amount)
                }}</span>
            </template>
          </el-table-column>
          <el-table-column label="来源" width="120">
            <template #default="{ row }">{{ txnSourceText(row.sourceType) }}</template>
          </el-table-column>
          <el-table-column label="渠道流水号" prop="channelTxnNo" show-overflow-tooltip width="170"/>
          <el-table-column label="收银" prop="cashierName" width="90"/>
          <el-table-column label="时间" prop="txnTime" width="160"/>
          <el-table-column label="原因" min-width="140" prop="reason" show-overflow-tooltip/>
        </el-table>
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * L2 账单详情（账单头 + 账单行快照 + 全部收/退流水）—— 从 views/cashier/CashierView.vue 拆出
 *
 * 只读：账单行是出账时冻结的快照，收/退流水是资金事实的唯一来源，本页一律不翻状态、不改金额。
 * 字典/金额格式化与列表列同源，由页面加载后按 prop 传下（子组件不重复拉字典）。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getBillDetailById} from '@/api/settlementBill';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 账单 id */
  id: {type: [String, Number], default: null},
  money: {type: Function, required: true},
  billStatusText: {type: Function, required: true},
  billStatusTag: {type: Function, required: true},
  billTypeText: {type: Function, required: true},
  payMethodText: {type: Function, required: true},
  txnSourceText: {type: Function, required: true},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const loading = ref(false);
const detail = ref(null);

async function load() {
  loading.value = true;
  try {
    const res = await getBillDetailById(props.id);
    detail.value = res.data || null;
  } catch (error) {
    ElMessage.error(error.message || '加载账单详情失败');
  } finally {
    loading.value = false;
  }
}

watch(() => props.modelValue, (v) => {
  if (v) {
    detail.value = null;
    load();
  }
});

defineExpose({reload: load});
</script>
