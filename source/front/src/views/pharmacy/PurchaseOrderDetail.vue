<template>
  <!-- 详情抽屉 -->
  <el-drawer v-model="visible" data-testid="order-detail-drawer" size="720px" title="订单详情">
    <template v-if="detail">
      <el-descriptions :column="2" border class="mb-4" size="small">
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ detail.supplierName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ detail.orderTime }}</el-descriptions-item>
        <el-descriptions-item label="总额">{{ formatMoney(detail.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="审批状态">
          <el-tag :type="approvalTagType(detail.approvalStatus)">{{ approvalText(detail.approvalStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="收货状态">
          <el-tag :type="orderInboundTagType(detail)">{{ orderInboundText(detail) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detail.createBy || '—' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detail.remark || '—' }}</el-descriptions-item>
      </el-descriptions>

      <el-table :data="detail.items || []" border size="small">
        <el-table-column label="药品" min-width="140" prop="drugName" show-overflow-tooltip/>
        <el-table-column label="规格" prop="specification" width="110"/>
        <el-table-column label="批号" prop="batchNo" width="100"/>
        <el-table-column label="效期至" prop="expiryDate" width="100"/>
        <el-table-column align="right" label="数量" prop="quantity" width="70"/>
        <el-table-column align="right" label="单价" width="90">
          <template #default="{ row }">{{ formatMoney(row.unitPrice) }}</template>
        </el-table-column>
        <el-table-column align="right" label="金额" width="100">
          <template #default="{ row }">{{ formatMoney(row.amount) }}</template>
        </el-table-column>
      </el-table>
    </template>
  </el-drawer>
</template>

<script setup>
/**
 * 采购订单详情抽屉 —— 从 views/pharmacy/PurchaseOrderView.vue 抽出（只读）
 *
 * 收货状态不是订单表的列：inboundDone / inboundNo 由入库单派生，语义判定单点 lib/purchase.js。
 * 审批文案走字典 his_purchase_approval_status（父页加载后作 prop 传入，列表列共用一份）。
 */
import { computed, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { getPurchaseOrderDetail } from '@/api/purchase';
import { dictLabelText, formatMoney } from '@/lib/utils';
import { approvalTagType, orderInboundTagType, orderInboundText } from '@/lib/purchase';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  orderId: { type: [Number, String], default: null },
  approvalDict: { type: Array, default: () => [] },
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const approvalText = (v) => dictLabelText(props.approvalDict, v);
const detail = ref(null);

const load = async (orderId) => {
  try {
    const res = await getPurchaseOrderDetail(orderId);
    if (res.code === 200)
      detail.value = res.data;
    
  } catch (e) {
    console.error(e);
  }
};

watch(() => props.modelValue, (v) => {
  if (v && props.orderId)
    load(props.orderId);
});

defineExpose({ reload: () => props.orderId && load(props.orderId) });
</script>
