<template>
  <el-drawer v-model="visible" data-testid="inbound-detail-drawer" size="820px" title="入库单详情">
    <template v-if="detail">
      <el-descriptions :column="2" border class="mb-4" size="small">
        <el-descriptions-item label="入库单号">{{ detail.inboundNo }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ typeText(detail.inboundType) }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ detail.supplier || '—' }}</el-descriptions-item>
        <el-descriptions-item label="采购订单号">{{ detail.purchaseOrderNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="inboundStatusTagType(detail.inboundStatus)">{{ statusText(detail.inboundStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="金额合计">{{ formatMoney(detail.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ detail.auditBy || '—' }}</el-descriptions-item>
        <el-descriptions-item label="入库人">{{ detail.inboundBy || '—' }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="取消原因">{{ detail.cancelReason || '—' }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="备注">{{ detail.remark || '—' }}</el-descriptions-item>
      </el-descriptions>

      <el-table :data="detail.items || []" border data-testid="inbound-detail-table" size="small">
        <el-table-column label="编码" prop="drugCode" width="100"/>
        <el-table-column label="药品" min-width="130" prop="drugName" show-overflow-tooltip/>
        <el-table-column label="规格" prop="specification" width="100"/>
        <el-table-column label="批号" prop="batchNo" width="90"/>
        <el-table-column label="效期至" prop="expiryDate" width="100"/>
        <el-table-column align="right" label="数量" prop="quantity" width="70"/>
        <el-table-column align="right" label="成本价" width="90">
          <template #default="{ row }">{{ formatMoney(row.costPrice) }}</template>
        </el-table-column>
        <el-table-column align="right" label="金额" width="95">
          <template #default="{ row }">{{ formatMoney(row.amount) }}</template>
        </el-table-column>
        <el-table-column align="center" label="明细状态" width="90">
          <template #default="{ row }">
            <el-tag :type="detailStatusTagType(row.detailStatus)" size="small">
              {{ detailStatusText(row.detailStatus) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div class="mt-2 text-sm text-slate-500 text-right">有效明细金额合计：¥{{ activeAmount }}</div>
    </template>
  </el-drawer>
</template>

<script setup>
/**
 * 药品入库单详情抽屉（自 DrugInboundView.vue 原样搬出：打开时按 id 拉明细，字段与合计口径未变）
 * 类型/状态文案仍走父页面加载的字典（列表列同名，不在子组件重复请求）。
 */
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getInboundDetail } from '@/api/purchase'
import { dictLabelText, formatMoney } from '@/lib/utils'
import { detailStatusTagType, detailStatusText, inboundStatusTagType } from '@/lib/purchase'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  id: { type: [Number, String], default: null },
  typeDict: { type: Array, default: () => [] },
  statusDict: { type: Array, default: () => [] },
})
const emit = defineEmits(['update:modelValue'])

const visible = computed({ get: () => props.modelValue, set: (v) => emit('update:modelValue', v) })

const detail = ref(null)
const statusText = (v) => dictLabelText(props.statusDict, v)
const typeText = (v) => dictLabelText(props.typeDict, v)
const activeAmount = computed(() => (detail.value?.items || [])
    .filter((d) => Number(d.detailStatus) !== 3)
    .reduce((s, d) => s + Number(d.amount || 0), 0)
    .toFixed(2))

watch(visible, async (open) => {
  if (!open || !props.id) return
  try {
    const res = await getInboundDetail(props.id)
    if (res.code === 200) {
      detail.value = res.data
    } else {
      ElMessage.error(res.message || '加载详情失败')
    }
  } catch (e) {
    console.error(e)
  }
})
</script>
