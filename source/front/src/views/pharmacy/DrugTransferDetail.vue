<template>
  <!-- 详情 -->
  <el-drawer v-model="visible" data-testid="transfer-detail-drawer" size="1040px" title="调拨单详情">
    <template v-if="detail">
      <div class="mb-3 flex items-center justify-between">
        <div class="flex items-center gap-2">
          <el-tag :type="transferStatusTagType(detail.status)">{{ detail.statusText }}</el-tag>
          <span class="text-sm text-slate-500">{{ detail.transferTypeText }}：{{
              detail.fromRoomText
            }} → {{ detail.toRoomText }}</span>
        </div>
        <div class="flex items-center gap-2">
          <el-button v-if="canTransferOut(detail)" v-perm="'pharmacy:drugTransfer:edit'" size="small" type="warning"
                     @click="emit('out', detail)">确认发出
          </el-button>
          <el-button v-if="canTransferIn(detail)" v-perm="'pharmacy:drugTransfer:edit'" size="small" type="success"
                     @click="emit('in', detail)">确认接收
          </el-button>
          <el-button v-if="canEditTransfer(detail)" v-perm="'pharmacy:drugTransfer:add'" size="small"
                     @click="emit('modify', detail)">改明细
          </el-button>
          <el-button v-if="canDeleteTransfer(detail)" v-perm="'pharmacy:drugTransfer:delete'" size="small"
                     type="danger" @click="emit('delete', detail)">删除
          </el-button>
        </div>
      </div>

      <el-descriptions :column="3" border class="mb-4" size="small">
        <el-descriptions-item label="调拨单号">{{ detail.transferNo }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="事由">{{ detail.reason }}</el-descriptions-item>
        <el-descriptions-item label="批次数">{{ detail.totalItems }}</el-descriptions-item>
        <el-descriptions-item label="申请合计">{{ detail.totalQuantity }}</el-descriptions-item>
        <el-descriptions-item label="已发出 / 已接收">
          {{ detail.outQuantity }} / {{ detail.inQuantity }}
        </el-descriptions-item>
        <el-descriptions-item label="金额合计">{{ formatMoney(detail.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="制单人">{{ detail.createBy || '—' }}</el-descriptions-item>
        <el-descriptions-item label="制单时间">{{ detail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="发出人">{{ detail.outBy || '—' }}</el-descriptions-item>
        <el-descriptions-item label="发出时间">{{ detail.outTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="接收人">{{ detail.inBy || '—' }}</el-descriptions-item>
        <el-descriptions-item label="接收时间">{{ detail.inTime || '—' }}</el-descriptions-item>
        <el-descriptions-item :span="3" label="作废人 / 原因">
          {{ detail.cancelBy || '—' }}{{ detail.cancelReason ? `（${detail.cancelReason}）` : '' }}
        </el-descriptions-item>
        <el-descriptions-item :span="3" label="备注">{{ detail.remark || '—' }}</el-descriptions-item>
      </el-descriptions>

      <el-table :data="detail.items || []" border data-testid="transfer-item-detail-table" size="small">
        <el-table-column label="编码" prop="drugCode" width="95"/>
        <el-table-column label="药品" min-width="120" prop="drugName" show-overflow-tooltip/>
        <el-table-column label="规格" prop="specification" show-overflow-tooltip width="100"/>
        <el-table-column label="批号" prop="batchNo" width="95"/>
        <el-table-column label="效期至" prop="expiryDate" width="100"/>
        <el-table-column align="right" label="成本价" width="80">
          <template #default="{ row }">{{ formatMoney(row.costPrice) }}</template>
        </el-table-column>
        <el-table-column align="right" label="调拨数量" width="85">
          <template #default="{ row }">{{ row.applyQuantity }}</template>
        </el-table-column>
        <el-table-column align="right" label="金额" width="90">
          <template #default="{ row }">{{ formatMoney(row.amount) }}</template>
        </el-table-column>
        <el-table-column align="right" label="发出方余额" width="95">
          <template #default="{ row }">{{ row.fromQuantity }}</template>
        </el-table-column>
        <el-table-column align="right" label="接收方余额" width="95">
          <template #default="{ row }">{{ row.toQuantity === null ? '—' : row.toQuantity }}</template>
        </el-table-column>
        <el-table-column align="center" label="进度" width="110">
          <template #default="{ row }">
            <el-tag :type="Number(row.inFlag) === 1 ? 'success' : Number(row.outFlag) === 1 ? 'warning' : 'info'"
                    size="small">
              {{ Number(row.inFlag) === 1 ? '已接收' : Number(row.outFlag) === 1 ? '待接收' : '未发出' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <h3 class="mt-5 mb-2 flex items-center gap-2 text-sm font-semibold text-slate-700">
        本单落下的库存流水
        <el-tag v-if="detail.logs?.length" :type="balanced ? 'success' : 'warning'" size="small">
          {{ balanced ? '出=入 已闭环' : '只在途（尚未接收）' }}
        </el-tag>
      </h3>
      <el-table :data="detail.logs || []" border data-testid="transfer-log-table" size="small">
        <el-table-column label="药品" min-width="120" prop="drugName" show-overflow-tooltip/>
        <el-table-column label="批号" prop="batchNo" width="95"/>
        <el-table-column align="center" label="库位" width="80">
          <template #default="{ row }">{{ row.stockRoomText }}</template>
        </el-table-column>
        <el-table-column align="center" label="变动类型" width="100">
          <template #default="{ row }">{{ row.changeTypeText }}</template>
        </el-table-column>
        <el-table-column align="right" label="变动数量" width="90">
          <template #default="{ row }">
            <span :class="Number(row.changeQuantity) < 0 ? 'text-rose-600' : 'text-emerald-700'">
              {{ signedQuantity(row.changeQuantity) }}
            </span>
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
        还没有流水 —— 这张单仍在「待发出」，保存明细本身不动库存。
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
/**
 * 调拨单详情抽屉 —— 从 views/pharmacy/DrugTransferView.vue 抽出
 *
 * 状态机：1待发出 →（确认发出）2待接收 →（确认接收）3已完成；只有「待发出」可改明细/作废/删除。
 * 发出与接收各动一次库存（7-调拨出库负 / 8-调拨入库正），两行流水合计 0 即账实闭环，
 * balanced 用 isTransferBalanced(logs) 直接算给操作人看。
 * 头部的 确认发出/确认接收/改明细/删除 是列表也在用的状态机动作，交回父页跑（emit 出去）。
 */
import { computed, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { getDrugTransferDetail } from '@/api/pharmacy';
import { formatMoney } from '@/lib/utils';
import {
  canDeleteTransfer,
  canEditTransfer,
  canTransferIn,
  canTransferOut,
  isTransferBalanced,
  transferStatusTagType,
} from '@/lib/drugTransfer';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  id: { type: [Number, String], default: null },
});
const emit = defineEmits(['update:modelValue', 'out', 'in', 'modify', 'delete']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const detail = ref(null);
const balanced = computed(() => isTransferBalanced(detail.value?.logs));
const signedQuantity = (v) => {
  if (v === null || v === undefined)
    return '—';
  const n = Number(v);
  return (n > 0 ? '+' : '') + n;
};
const load = async (id) => {
  try {
    const res = await getDrugTransferDetail(id);
    if (res.code === 200)
      detail.value = res.data;
    else
      ElMessage.error(res.message || '加载详情失败');
  } catch (e) {
    console.error(e);
  }
};

watch(() => props.modelValue, (v) => {
  if (v && props.id) load(props.id);
});
watch(() => props.id, (id) => {
  if (props.modelValue && id) load(id);
});

defineExpose({ reload: load });
</script>
