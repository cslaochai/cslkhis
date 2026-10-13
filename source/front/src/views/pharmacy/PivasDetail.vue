<template>
  <!-- 明细弹窗（审方→排队→调配→核对 工作区） -->
  <el-dialog v-model="visible" :title="`静配单 ${detail?.pivasNo || ''}`" data-testid="pivas-detail-dialog"
             width="1080px">
    <div v-if="detail" v-loading="detailLoading" class="space-y-3">
      <div class="flex items-center gap-6 text-sm text-slate-600 flex-wrap">
        <span>调配日期：<b class="text-slate-800">{{ detail.admixDate }}</b></span>
        <span>患者：<b class="text-slate-800">{{ detail.patientName || '—' }}</b>（{{ detail.patientNo || '—' }}）</span>
        <span>病区：<b class="text-slate-800">{{ detail.wardName || '—' }}</b></span>
        <span>
          状态：<el-tag :type="pivasBatchStatusTag(detail.status)" size="small">{{
            batchStatusText(detail.status)
          }}</el-tag>
        </span>
        <span>生成人：{{ detail.generateBy || '—' }}</span>
        <el-button v-if="canLabel(detail.status)" v-perm="'pharmacy:pivas:edit'" :icon="Tickets" :loading="actLoading"
                   data-testid="pivas-label-btn"
                   size="small" type="primary" @click="onLabelBatch">打标签排队
        </el-button>
      </div>
      <el-table :data="detail.items || []" border data-testid="pivas-item-table" max-height="420" size="small">
        <el-table-column align="center" label="排队号" width="72">
          <template #default="{ row }">
            <span v-if="row.queueNo" class="font-semibold text-sky-700">{{ row.queueNo }}</span>
            <span v-else class="text-slate-300">—</span>
          </template>
        </el-table-column>
        <el-table-column label="药品" min-width="140" prop="drugName"/>
        <el-table-column label="规格" prop="spec" width="100"/>
        <el-table-column align="right" label="数量" width="90">
          <template #default="{ row }">{{ row.quantity }} {{ row.unit }}</template>
        </el-table-column>
        <el-table-column label="途径" prop="route" width="80"/>
        <el-table-column label="频次" prop="frequency" width="70"/>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="pivasItemStatusTag(row.status)" size="small">{{ itemStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审方人" prop="auditorName" width="90">
          <template #default="{ row }">{{ row.auditorName || '—' }}</template>
        </el-table-column>
        <el-table-column label="调配人" prop="compounderName" width="90">
          <template #default="{ row }">{{ row.compounderName || '—' }}</template>
        </el-table-column>
        <el-table-column label="核对人" prop="verifierName" width="90">
          <template #default="{ row }">{{ row.verifierName || '—' }}</template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="210">
          <template #default="{ row }">
            <el-button v-if="canAudit(row.status)" v-perm="'pharmacy:pivas:edit'" :icon="CircleCheck" :loading="actLoading"
                       data-testid="pivas-audit-pass-btn" plain
                       size="small" type="success" @click="onAuditPass(row)">审方通过
            </el-button>
            <el-button v-if="canAudit(row.status)" v-perm="'pharmacy:pivas:edit'" :icon="CircleClose" :loading="actLoading"
                       data-testid="pivas-audit-reject-btn" plain
                       size="small" type="danger" @click="onAuditReject(row)">退回
            </el-button>
            <el-button v-if="canCompound(row.status)" v-perm="'pharmacy:pivas:edit'" :icon="Van" :loading="actLoading"
                       data-testid="pivas-compound-btn" plain
                       size="small" type="primary" @click="onCompound(row)">调配
            </el-button>
            <el-button v-if="canVerify(row.status)" v-perm="'pharmacy:pivas:edit'" :icon="Box" :loading="actLoading"
                       data-testid="pivas-verify-btn" plain
                       size="small" type="success" @click="onVerify(row)">核对发放
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-4 text-sm text-slate-400">本单暂无明细</div>
        </template>
      </el-table>
      <div v-if="(detail.items || []).some((i: any) => i.rejectReason)" class="text-xs text-slate-500">
        <template v-for="i in detail.items.filter((x: any) => x.rejectReason)" :key="i.id">
          <div>审方退回留痕：{{ i.drugName }} × {{ i.quantity }} —— {{ i.rejectReason }}（{{ i.auditorName }}）</div>
        </template>
      </div>
      <div class="text-xs text-slate-400">
        说明：排队/打标签为流程节点留痕（标签打印预留，不驱动打印机）；费用结算仍走住院摆药链，本链不重复计费。
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
/**
 * 静配单明细弹窗 —— 从 views/pharmacy/PivasView.vue 抽出
 *
 * 明细级链路（审方通过/退回 → 打标签排队 → 调配 → 核对发放）只在本弹窗里操作，故一并搬入。
 * 状态机 1待审方→2已审方→3已排队→4已调配→5已核对发放 顺序推进不可跳级；本链零库存/零计费。
 * 每次动作成功后 emit('changed')，由父页刷新列表与统计（口径不变）。
 */
import { computed, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Box, CircleCheck, CircleClose, Tickets, Van } from '@element-plus/icons-vue';
import {
  auditPivasItem,
  compoundPivasItem,
  getPivasDetail,
  labelPivasBatch,
  verifyPivasItem,
} from '@/api/pivas';
import { dictLabelText } from '@/lib/utils';
import { canAudit, canCompound, canLabel, canVerify, pivasBatchStatusTag, pivasItemStatusTag } from '@/lib/pivas';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  id: { type: [Number, String], default: null },
  batchStatusDict: { type: Array, default: () => [] },
  itemStatusDict: { type: Array, default: () => [] },
});
const emit = defineEmits(['update:modelValue', 'changed']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const batchStatusText = (v) => dictLabelText(props.batchStatusDict, v);
const itemStatusText = (v) => dictLabelText(props.itemStatusDict, v);

const detail = ref(null);
const detailLoading = ref(false);
const actLoading = ref(false);

const load = async (id) => {
  detailLoading.value = true;
  try {
    const res = await getPivasDetail(id);
    if (res.code === 200)
      detail.value = res.data;
    
  } catch (e) {
    console.error(e);
    
  } finally {
    detailLoading.value = false;
  }
};
const refreshDetail = async () => {
  if (detail.value?.id)
    await load(detail.value.id);
  emit('changed');
};
const onAuditPass = async (item) => {
  actLoading.value = true;
  try {
    const res = await auditPivasItem({itemId: item.id, pass: true});
    if (res.code === 200) {
      ElMessage.success(`审方通过（${item.drugName} × ${item.quantity}）`);
      await refreshDetail();
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    actLoading.value = false;
  }
};
const onAuditReject = async (item) => {
  try {
    const {value} = await ElMessageBox.prompt(`确认退回「${item.drugName} × ${item.quantity}」？退回后该医嘱不进本次调配，可重新生成入单。`, '审方退回原因（必填）', {
      confirmButtonText: '确认退回',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '退回原因必填'
    });
    actLoading.value = true;
    const res = await auditPivasItem({itemId: item.id, pass: false, reason: value.trim()});
    if (res.code === 200) {
      ElMessage.success('已退回（该医嘱可重新入单）');
      await refreshDetail();
    } 
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel')
      console.error(e);
  } finally {
    actLoading.value = false;
  }
};
const onLabelBatch = async () => {
  if (!detail.value?.id)
    return;
  try {
    await ElMessageBox.confirm('确认对该单打标签排队？已审方明细将统一取排队号（标签打印为预留：只取号盖时间，不驱动打印机）。', '打标签排队', {
      confirmButtonText: '确认排队',
      cancelButtonText: '取消'
    });
    actLoading.value = true;
    const res = await labelPivasBatch({batchId: detail.value.id});
    if (res.code === 200) {
      ElMessage.success('已排队取号，等待调配');
      await refreshDetail();
    } 
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel')
      console.error(e);
  } finally {
    actLoading.value = false;
  }
};
const onCompound = async (item) => {
  actLoading.value = true;
  try {
    const res = await compoundPivasItem({itemId: item.id});
    if (res.code === 200) {
      ElMessage.success(`调配完成（${item.drugName}）`);
      await refreshDetail();
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    actLoading.value = false;
  }
};
const onVerify = async (item) => {
  actLoading.value = true;
  try {
    const res = await verifyPivasItem({itemId: item.id});
    if (res.code === 200) {
      ElMessage.success('成品核对通过，已配送病区');
      await refreshDetail();
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    actLoading.value = false;
  }
};

watch(() => props.modelValue, (v) => {
  if (v && props.id) load(props.id);
});

defineExpose({ reload: () => props.id && load(props.id) });
</script>
