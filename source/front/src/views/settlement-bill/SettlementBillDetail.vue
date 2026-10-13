<template>
  <el-dialog v-model="visible" title="账单详情（行快照 + 收/退流水）" width="860px">
    <div v-loading="detailLoading">
      <template v-if="detail">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="账单号">{{ detail.billNo }}</el-descriptions-item>
          <el-descriptions-item label="患者">{{ detail.patientName }}（{{
              detail.patientNo || '—'
            }}）
          </el-descriptions-item>
          <el-descriptions-item label="状态">{{ billStatusText(detail.billStatus) }}</el-descriptions-item>
          <el-descriptions-item label="应收">¥{{ formatMoney(detail.totalAmount) }}</el-descriptions-item>
          <el-descriptions-item label="优惠">¥{{ formatMoney(detail.discountAmount) }}</el-descriptions-item>
          <el-descriptions-item label="统筹">¥{{ formatMoney(detail.poolAmount) }}</el-descriptions-item>
          <el-descriptions-item label="应缴">¥{{ formatMoney(detail.payableAmount) }}</el-descriptions-item>
          <el-descriptions-item label="已收">¥{{ formatMoney(detail.paidAmount) }}</el-descriptions-item>
          <el-descriptions-item label="尚欠">¥{{ formatMoney(detail.unpaidAmount) }}</el-descriptions-item>
        </el-descriptions>

        <div class="mt-3 mb-1 text-sm font-medium">账单行快照</div>
        <el-table :data="detail.items || []" border size="small">
          <el-table-column label="项目" min-width="150" prop="itemName" show-overflow-tooltip/>
          <el-table-column align="right" label="单价" min-width="80" prop="price">
            <template #default="{ row }">¥{{ formatMoney(row.price) }}</template>
          </el-table-column>
          <el-table-column align="right" label="数量" min-width="70" prop="quantity"/>
          <el-table-column align="right" label="金额" min-width="90" prop="amount">
            <template #default="{ row }">¥{{ formatMoney(row.amount) }}</template>
          </el-table-column>
        </el-table>

        <div class="mt-3 mb-1 text-sm font-medium">收/退流水</div>
        <el-table :data="detail.txns || []" border data-testid="bill-txn-table" size="small">
          <el-table-column class-name="font-mono text-xs" label="流水号" min-width="150" prop="txnNo"/>
          <el-table-column label="方向" min-width="70">
            <template #default="{ row }">{{
                dictLabelText(dictOptions(DICT_TYPE.PAY_DIRECTION), row.direction)
              }}
            </template>
          </el-table-column>
          <el-table-column label="渠道" min-width="90">
            <template #default="{ row }">{{ payMethodText(row.payMethod) }}</template>
          </el-table-column>
          <el-table-column align="right" label="金额" min-width="90" prop="amount">
            <template #default="{ row }">¥{{ formatMoney(row.amount) }}</template>
          </el-table-column>
          <el-table-column label="状态" min-width="80">
            <template #default="{ row }">{{
                dictLabelText(dictOptions(DICT_TYPE.PAY_TXN_STATUS), row.txnStatus)
              }}
            </template>
          </el-table-column>
          <el-table-column label="来源" min-width="90">
            <template #default="{ row }">{{ sourceTypeText(row.sourceType) }}</template>
          </el-table-column>
          <el-table-column class-name="font-mono text-xs" label="时间" min-width="150" prop="txnTime"/>
        </el-table>
      </template>
    </div>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getBillDetailById} from '@/api/settlementBill';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText, formatMoney} from '@/lib/utils';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
  // 字典由页面一次性加载（列表列也在用），这里只读取，不重复请求
  dicts: {type: Object, default: () => ({})},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const dictOptions = (type) => props.dicts[type] || [];
const billStatusText = (v) => dictLabelText(dictOptions(DICT_TYPE.BILL_STATUS), v);
const sourceTypeText = (v) => dictLabelText(dictOptions(DICT_TYPE.TXN_SOURCE), v);
const payMethodText = (v) => dictLabelText(dictOptions(DICT_TYPE.PAY_METHOD), v);

const detailLoading = ref(false);
const detail = ref(null);

watch(visible, async (open) => {
  if (!open || !props.id) return;
  detailLoading.value = true;
  try {
    const res = await getBillDetailById(props.id);
    detail.value = res.data || null;
  } catch (e) {
    detail.value = null;
    
  } finally {
    detailLoading.value = false;
  }
});
</script>
