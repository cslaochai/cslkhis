<template>
  <el-drawer v-model="visible" destroy-on-close size="720px" title="处方详情">
    <template v-if="detailRow">
      <div class="space-y-4">
        <div class="grid grid-cols-2 gap-4 rounded-lg border border-slate-200 p-4">
          <div>
            <p class="text-xs text-slate-400">患者</p>
            <p class="text-sm font-medium text-slate-700">{{ detailRow.patientName }}（{{ detailRow.patientNo }}）</p>
          </div>
          <div>
            <p class="text-xs text-slate-400">处方号</p>
            <p class="font-mono text-sm font-medium text-slate-700">{{ detailRow.prescriptionNo }}</p>
          </div>
          <div>
            <p class="text-xs text-slate-400">科室 / 开方医生</p>
            <p class="text-sm font-medium text-slate-700">{{ detailRow.deptName }} / {{ detailRow.doctorName }}</p>
          </div>
          <div>
            <p class="text-xs text-slate-400">处方状态</p>
              <span
                  :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', statusColor(detailRow.prescriptionStatus)]">
                {{ statusText(detailRow.prescriptionStatus) }}
              </span>
          </div>
          <div>
            <p class="text-xs text-slate-400">临床诊断</p>
            <p class="text-sm font-medium text-slate-700">{{ detailRow.diagnosis || '—' }}</p>
          </div>
          <div>
            <p class="text-xs text-slate-400">金额</p>
            <p class="text-sm font-bold text-red-600">¥{{ detailRow.totalAmount || 0 }}</p>
          </div>
          <div v-if="detailRow.prescriptionStatus === 7"
               class="col-span-2 rounded border border-orange-200 bg-orange-50 p-2">
            <p class="text-xs text-orange-500">退回原因（已退回 {{ detailRow.returnCount || 1 }} 次）</p>
            <p class="text-sm text-orange-700">{{ detailRow.returnReason || '—' }}</p>
          </div>
        </div>

        <div class="rounded-lg border border-slate-200 p-4">
          <h4 class="mb-3 text-sm font-medium text-slate-700">签名</h4>
          <div class="grid grid-cols-2 gap-3 text-sm">
            <div>
              <p class="text-xs text-slate-400">开方医师签名</p>
              <p v-if="detailRow.doctorSignId" class="text-slate-700">已签 · {{
                  fmtTime(detailRow.doctorSignedTime)
                }}</p>
              <p v-else class="text-slate-400">未签</p>
            </div>
            <div>
              <p class="text-xs text-slate-400">审方药师签名</p>
              <p v-if="detailRow.auditSignId" class="text-slate-700">
                已签 · {{ detailRow.auditBy || '' }} · {{ fmtTime(detailRow.auditSignedTime) }}
              </p>
              <p v-else class="text-amber-600">未签</p>
            </div>
          </div>
          <div class="mt-3">
            <el-button :loading="verifying" size="small" @click="handleVerify">验证签名有效性</el-button>
          </div>
          <div v-if="verifyResults.length" class="mt-3 space-y-2">
            <div v-for="v in verifyResults" :key="v.signId" class="rounded border border-slate-200 p-2 text-xs">
              <div class="flex items-center gap-2">
                <span class="font-medium text-slate-700">{{ v.signSceneText }}</span>
                <span class="text-slate-400">第 {{ v.chainNo }} 环</span>
                <span class="text-slate-500">{{ v.signerName }}</span>
                <span :class="verifyLevelClass(v.conclusionLevel)" class="font-medium">{{ v.conclusion }}</span>
              </div>
              <div class="mt-1 font-mono text-[11px] text-slate-400">
                签名时摘要 {{ v.digestAtSign }} / 当前摘要 {{ v.digestNow || '（对象已不存在）' }}
              </div>
            </div>
          </div>
        </div>

        <div v-if="hitsOf(detailRow).length || blockedOf(detailRow)" class="rounded-lg border border-slate-200 p-4">
          <h4 class="mb-3 text-sm font-medium text-slate-700">合理用药核查</h4>
          <p v-if="blockedOf(detailRow)" class="mb-2 rounded border border-red-200 bg-red-50 p-2 text-xs text-red-700"
             data-testid="kb-drawer-block">
            {{ rationalOf(detailRow).blockMessage }}
          </p>
          <div v-for="h in hitsOf(detailRow)" :key="h.knowledgeId"
               class="mb-2 rounded border border-slate-200 p-2 text-xs last:mb-0">
            <div class="flex items-center gap-2">
                <span :class="['rounded px-1.5 py-0.5 font-medium',
                      h.severity === 1 ? 'bg-red-100 text-red-700' : 'bg-amber-100 text-amber-700']">
                  {{ h.severity === 1 ? '禁忌' : '慎用' }}
                </span>
              <span class="text-slate-500">{{ hitTypeText[h.hitType] || h.hitType }}</span>
              <span class="font-mono text-[11px] text-slate-400">条目 {{ h.knowledgeId }}</span>
            </div>
            <p class="mt-1 text-slate-700">{{ h.message }}</p>
            <p v-if="h.suggestion" class="mt-1 text-slate-500">建议：{{ h.suggestion }}</p>
          </div>
        </div>

        <div v-if="detailRow.details?.length">
          <h4 class="mb-2 text-sm font-medium text-slate-700">药品明细</h4>
          <el-table :data="detailRow.details" border size="small">
            <el-table-column label="药品名称" min-width="140" prop="drugName"/>
            <el-table-column label="规格" prop="specification" width="100"/>
            <el-table-column align="center" label="数量" prop="quantity" width="70"/>
            <el-table-column align="center" label="单位" prop="unit" width="60"/>
            <el-table-column label="频次" prop="frequency" width="80"/>
            <el-table-column label="途径" prop="route" width="80"/>
            <el-table-column align="right" label="金额" width="90">
              <template #default="{ row }">¥{{ row.amount || 0 }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
/**
 * 处方详情抽屉（处方全字段 + 签名验签 + 合理用药核查）—— 从 views/pharmacy-window/PrescriptionAuditView.vue 拆出
 *
 * 处方内容后端没有按 id 单查接口，抽屉直接摆列表带出的那一行；
 * 合理用药结论与列表列同源（页面批量核查后按函数 prop 传下），抽屉不重复核查、不自己判断；
 * 验签走 verifySignatureByBiz，结论原样摆出，不改措辞。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {verifySignatureByBiz} from '@/api/signature';

// 与后端 SignBizType.PRESCRIPTION 同值
const PRESCRIPTION_BIZ_TYPE = 4;

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 处方行 */
  row: {type: Object, default: null},
  /** 处方状态文案/配色（列表列同源） */
  statusText: {type: Function, required: true},
  statusColor: {type: Function, required: true},
  /** 合理用药核查结果（列表列同源，页面批量核查后传下） */
  rationalOf: {type: Function, required: true},
  hitsOf: {type: Function, required: true},
  blockedOf: {type: Function, required: true},
  hitTypeText: {type: Object, default: () => ({})},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const detailRow = computed(() => props.row);
const verifying = ref(false);
const verifyResults = ref([]);

const fmtTime = (v) => (v ? String(v).replace('T', ' ').slice(0, 19) : '—');

/** 验证该处方全部签名的有效性（含已作废） */
const handleVerify = async () => {
  if (!detailRow.value)
    return;
  verifying.value = true;
  try {
    const res = await verifySignatureByBiz(PRESCRIPTION_BIZ_TYPE, detailRow.value.id);
    verifyResults.value = res.data || [];
    if (!verifyResults.value.length) {
      ElMessage.warning('该处方没有任何签名记录');
    }
  } catch (error) {
    ElMessage.error(error.message || '验签失败');
  } finally {
    verifying.value = false;
  }
};
const verifyLevelClass = (level) => {
  if (level === 1)
    return 'text-emerald-600';
  if (level === 2)
    return 'text-amber-600';
  return 'text-red-600';
};

watch(() => props.modelValue, (v) => {
  if (v)
    verifyResults.value = [];
});
</script>
