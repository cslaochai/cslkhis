<template>
  <el-drawer v-model="visible" :title="`合规审核 ${detail.auditNo || ''}`" direction="rtl" size="72%">
    <div v-loading="detailLoading" class="detail-wrap">
      <el-descriptions :column="2" border data-testid="ca-detail-head" size="small">
        <el-descriptions-item label="结算清单号">{{ detail.settlementNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="审核类型">{{ typeText(detail.auditType) }}</el-descriptions-item>
        <el-descriptions-item label="风险等级">
          <el-tag :type="riskTagType(detail.riskLevel)" size="small">{{ detail.riskLevelText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="风险分">{{ detail.riskScore ?? '—' }}</el-descriptions-item>
        <el-descriptions-item label="DRG 分组">{{ detail.drgCode || '未分组' }}</el-descriptions-item>
        <el-descriptions-item label="DRG 权重">{{ detail.drgWeight ?? '—' }}</el-descriptions-item>
        <el-descriptions-item label="实际费用（元）">{{ money(detail.actualCost) }}</el-descriptions-item>
        <el-descriptions-item label="支付标准（元）">{{ money(detail.payStandard) }}</el-descriptions-item>
        <el-descriptions-item label="费用倍率">{{ detail.costRatio ?? '—' }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ fmtTime(detail.auditTime) }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="审核结论">{{ detail.conclusion || '—' }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="备注">{{ detail.remark || '—' }}</el-descriptions-item>
      </el-descriptions>

      <h4 class="sec">规则判定明细（{{ detail.hitItemCount ?? items.length }} 条命中，共 {{ items.length }} 条）</h4>
      <el-table :data="items" border data-testid="ca-detail-items" max-height="420" size="small" stripe>
        <el-table-column label="规则" prop="ruleCode" width="70"/>
        <el-table-column label="规则名称" min-width="180" prop="ruleName" show-overflow-tooltip/>
        <el-table-column label="分组" prop="ruleGroupText" width="90"/>
        <el-table-column label="判定" width="90">
          <template #default="{ row }">
            <el-tag :type="itemResultTagType(row.result)" size="small">{{ row.resultText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="风险" prop="riskLevelText" width="80"/>
        <el-table-column label="命中对象" prop="targetName" show-overflow-tooltip width="140"/>
        <el-table-column label="判定依据" min-width="240" prop="evidence" show-overflow-tooltip/>
        <el-table-column label="整改建议" min-width="200" prop="suggestion" show-overflow-tooltip/>
      </el-table>
      <p class="muted mt">「不适用」= 缺依据没法评估，不是「没 problem」，凑数当通过会让结论虚高。</p>

      <h4 class="sec">AI 证据判定</h4>
      <div v-if="hitItems.length" class="flex items-center gap-3 mb-2">
        <el-button :loading="aiEvidenceLoading" data-testid="ca-ai-evidence-btn" size="small" type="primary"
                   @click="runAiEvidence">
          AI 证据判定
        </el-button>
        <span class="muted">模型逐条阅读病历证据判「支持 / 反驳 / 证据不足」，仅提示人工复核，不改变规则结论</span>
      </div>
      <el-alert
          v-if="aiEvidence?.degraded"
          :closable="false" :title="`AI 证据判定暂不可用：${aiEvidence.degradeReason || '原因未知'}，规则结论以人工核对为准`" class="mb-2" data-testid="ca-ai-evidence-degraded"
          show-icon
          type="warning"/>
      <el-table
          v-if="aiEvidence && !aiEvidence.degraded"
          :data="aiEvidence.judgments" border data-testid="ca-ai-evidence-table" max-height="300" size="small">
        <el-table-column label="规则" prop="ruleCode" width="70"/>
        <el-table-column label="规则名称" min-width="160" prop="ruleName" show-overflow-tooltip/>
        <el-table-column label="证据判定" width="110">
          <template #default="{ row }">
            <el-tag :type="verdictTagType(row.verdict)" size="small">{{ row.verdictText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="理由" min-width="240" prop="reason" show-overflow-tooltip/>
        <el-table-column label="原文引用" min-width="200" prop="quote" show-overflow-tooltip/>
        <template #empty><span class="muted">模型未给出判定</span></template>
      </el-table>
      <p v-if="aiEvidence && !aiEvidence.degraded && aiEvidence.overall" class="muted mt">总评：{{
          aiEvidence.overall
        }}</p>
    </div>
  </el-drawer>
</template>

<script setup>
/**
 * 合规审核详情抽屉（从 ComplianceAuditView 拆出）：主表 + 规则明细 + AI 证据判定。
 * 父页先塞 row 防闪烁，再按 id 拉详情覆盖（口径与原页内 openDetail 一致）。
 */
import {computed, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {getComplianceAuditDetail} from '@/api/compliance'
import {judgeInsuranceEvidence} from '@/api/ai'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 审核记录 id：打开后按它拉详情 */
  id: {type: [String, Number], default: ''},
  /** 列表行数据：详情回来前先渲染它，防闪烁 */
  row: {type: Object, default: null},
  /** 以下格式化函数列表页也在用，由页面统一持有传入 */
  typeText: {type: Function, required: true},
  riskTagType: {type: Function, required: true},
  fmtTime: {type: Function, required: true},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const money = (v) => (v === null || v === undefined || v === '' ? '—' : Number(v).toFixed(2))
// 三态：命中红 / 通过绿 / 不适用灰 —— 缺依据的必须一眼看出是灰的
const itemResultTagType = (v) => ({1: 'danger', 2: 'success', 3: 'info'}[v] || 'info')

const detailLoading = ref(false)
const detail = ref({})
const items = ref([])
const aiEvidence = ref(null)

watch(visible, async (open) => {
  if (!open) return;
  detail.value = props.row || {}
  items.value = []
  aiEvidence.value = null
  if (!props.id) return
  detailLoading.value = true
  try {
    const res = await getComplianceAuditDetail(String(props.id))
    if (res.code === 200) {
      detail.value = res.data || props.row || {}
      items.value = res.data?.items || []
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    detailLoading.value = false
  }
})

// ---------------- AI 证据判定（G-07） ----------------
// 模型逐条读病历证据，判「支持/反驳/证据不足」；只读计算不写库，不改规则结论。
// degraded=true 时judgments 为空 —— 规则判定依据照常可见，警示必显，不许把「无判定」当「无风险」。
const hitItems = computed(() => items.value.filter(i => i.result === 1))
const aiEvidenceLoading = ref(false)
const runAiEvidence = async () => {
  if (!detail.value?.id) return
  aiEvidenceLoading.value = true
  aiEvidence.value = null
  try {
    const res = await judgeInsuranceEvidence({auditId: detail.value.id})
    if (res.code === 200) {
      aiEvidence.value = res.data
      if (res.data?.degraded) ElMessage.warning(`AI 证据判定暂不可用（${res.data.degradeReason || '原因未知'}），请人工核对证据`)
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    aiEvidenceLoading.value = false
  }
}
const verdictTagType = (v) => (v === 1 ? 'danger' : v === 2 ? 'success' : 'info')
</script>

<style scoped>
.muted {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.mt {
  margin-top: 8px;
}

.sec {
  margin: 14px 0 8px;
  font-size: 14px;
}

.detail-wrap {
  padding-bottom: 20px;
}
</style>
