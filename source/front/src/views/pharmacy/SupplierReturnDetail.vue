<template>
  <el-drawer v-model="visible" data-testid="sreturn-detail-drawer" size="1000px" title="退货单详情">
    <template v-if="detail">
      <div class="mb-3 flex items-center justify-between">
        <div class="flex items-center gap-2">
          <el-tag :type="returnStatusTagType(detail.status)">{{ detail.statusText }}</el-tag>
          <span class="text-sm text-slate-500">{{ detail.supplierName }}</span>
        </div>
        <div class="flex items-center gap-2">
          <el-button v-if="canConfirmReturn(detail)" v-perm="'pharmacy:supplierReturn:edit'" size="small"
                     type="warning" @click="$emit('confirm', detail)">确认退货
          </el-button>
          <el-button v-if="canEditReturn(detail)" v-perm="'pharmacy:supplierReturn:add'" size="small"
                     @click="$emit('modify', detail)">改明细
          </el-button>
          <el-button v-if="canDeleteReturn(detail)" v-perm="'pharmacy:supplierReturn:delete'" size="small"
                     type="danger" @click="$emit('delete', detail)">删除
          </el-button>
        </div>
      </div>

      <el-descriptions :column="3" border class="mb-4" size="small">
        <el-descriptions-item label="退货单号">{{ detail.returnNo }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ detail.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="原单据号">{{ detail.srcRefNo || '—' }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="退货原因">{{ detail.returnReason }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">{{ formatMoney(detail.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="批次数">{{ detail.totalItems }}</el-descriptions-item>
        <el-descriptions-item label="数量合计">{{ detail.totalQuantity }}</el-descriptions-item>
        <el-descriptions-item label="制单人 / 时间">{{ detail.createBy }} {{
            detail.createTime
          }}
        </el-descriptions-item>
        <el-descriptions-item label="退货人">{{ detail.returnBy || '—' }}</el-descriptions-item>
        <el-descriptions-item label="退货时间">{{ detail.returnTime || '—' }}</el-descriptions-item>
        <el-descriptions-item :span="3" label="作废人 / 原因">
          {{ detail.cancelBy || '—' }}{{ detail.cancelReason ? `（${detail.cancelReason}）` : '' }}
        </el-descriptions-item>
        <el-descriptions-item :span="3" label="备注">{{ detail.remark || '—' }}</el-descriptions-item>
      </el-descriptions>

      <el-table :data="detail.items || []" border data-testid="sreturn-item-detail-table" size="small">
        <el-table-column label="编码" prop="drugCode" width="95"/>
        <el-table-column label="药品" min-width="120" prop="drugName" show-overflow-tooltip/>
        <el-table-column label="规格" prop="specification" show-overflow-tooltip width="100"/>
        <el-table-column label="批号" prop="batchNo" width="95"/>
        <el-table-column label="效期至" prop="expiryDate" width="100"/>
        <el-table-column align="center" label="库位" width="70">
          <template #default="{ row }">{{ row.stockRoomText || roomText(row.stockRoom) }}</template>
        </el-table-column>
        <el-table-column align="right" label="成本价" width="80">
          <template #default="{ row }">{{ formatMoney(row.costPrice) }}</template>
        </el-table-column>
        <el-table-column align="right" label="退货数量" width="85">
          <template #default="{ row }">{{ row.quantity }}</template>
        </el-table-column>
        <el-table-column align="right" label="金额" width="90">
          <template #default="{ row }">{{ formatMoney(row.amount) }}</template>
        </el-table-column>
        <el-table-column align="right" label="批次剩余" width="90">
          <template #default="{ row }">{{ row.currentQuantity }}</template>
        </el-table-column>
      </el-table>

      <h3 class="mt-5 mb-2 text-sm font-semibold text-slate-700">本单落下的库存流水（退出去多少，账上就得少多少）</h3>
      <el-table :data="detail.logs || []" border data-testid="sreturn-log-table" size="small">
        <el-table-column label="药品" min-width="120" prop="drugName" show-overflow-tooltip/>
        <el-table-column label="批号" prop="batchNo" width="95"/>
        <el-table-column align="center" label="库位" width="70">
          <template #default="{ row }">{{ row.stockRoomText }}</template>
        </el-table-column>
        <el-table-column align="center" label="变动类型" width="100">
          <template #default="{ row }">{{ row.changeTypeText }}</template>
        </el-table-column>
        <el-table-column align="right" label="变动数量" width="90">
          <template #default="{ row }">
            <span class="text-rose-600">{{ row.changeQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column align="right" label="变动前 → 变动后" width="120">
          <template #default="{ row }">{{ row.quantityBefore }} → {{ row.quantityAfter }}</template>
        </el-table-column>
        <el-table-column label="来源单号" prop="sourceNo" width="150"/>
        <el-table-column label="操作人" prop="operatorName" width="90"/>
        <el-table-column label="时间" prop="createTime" width="150"/>
      </el-table>
      <div v-if="!detail.logs?.length" class="mt-2 text-sm text-slate-400">
        还没有流水 —— 这张单仍在「待退货」，建单本身不动库存。
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
/**
 * 供应商退货详情抽屉（自 SupplierReturnView.vue 原样搬出）
 * 状态动作（确认退货 / 改明细 / 删除）仍由父页面执行 —— 这里只负责展示与抛事件；
 * roomDict 由父页面加载一次共用，明细库位无后端文案时按字典兜底。
 */
import {computed} from 'vue';
import {dictLabelText, formatMoney} from '@/lib/utils';
import {canConfirmReturn, canDeleteReturn, canEditReturn, returnStatusTagType} from '@/lib/supplierReturn';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 退货单详情（含 items / logs；父页面打开抽屉前已拉好） */
  detail: {type: Object, default: null},
  roomDict: {type: Array, default: () => []}
});

const emit = defineEmits(['update:modelValue', 'confirm', 'modify', 'delete']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
});

const roomText = (v) => dictLabelText(props.roomDict, v);
</script>
