<template>
  <el-dialog v-model="visible" title="AI 调用详情" width="720px" :destroy-on-close="true">
    <el-descriptions v-if="row" :column="2" border size="small">
      <el-descriptions-item label="发生时间">{{ row.createTime?.replace('T', ' ').slice(0, 19) }}</el-descriptions-item>
      <el-descriptions-item label="能力">{{ capabilityLabel(row.capabilityKey) }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row) }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="模型">{{ row.model || '—' }}</el-descriptions-item>
      <el-descriptions-item label="服务提供方">{{ row.provider || '—' }}</el-descriptions-item>
      <el-descriptions-item label="提示词版本">{{ row.promptVersion || '—' }}</el-descriptions-item>
      <el-descriptions-item label="耗时">{{ row.latencyMs != null ? row.latencyMs + ' ms' : '—' }}</el-descriptions-item>
      <el-descriptions-item label="Token（入/出）">{{ row.promptTokens ?? '—' }} / {{ row.completionTokens ?? '—' }}</el-descriptions-item>
      <el-descriptions-item label="操作人">{{ row.operator || '—' }}</el-descriptions-item>
      <el-descriptions-item label="业务">
        <template v-if="row.bizType">{{ row.bizType }}<template v-if="row.bizId"> #{{ row.bizId }}</template></template>
        <span v-else>—</span>
      </el-descriptions-item>
      <el-descriptions-item label="输入摘要" :span="2">
        <div class="detail-text">{{ row.inputDigest || '—' }}</div>
      </el-descriptions-item>
      <el-descriptions-item label="输出摘要" :span="2">
        <div class="detail-text">{{ row.outputDigest || '—' }}</div>
      </el-descriptions-item>
      <el-descriptions-item v-if="row.errorMsg" label="错误信息" :span="2">
        <div class="detail-text text-red-500">{{ row.errorMsg }}</div>
      </el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: Boolean,
  row: Object,
})
const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const CAPABILITY_MAP = {
  icd10: 'ICD-10 编码',
  drug_audit: '处方审核',
  emr_qc: '病历质控',
  lab_interpret: '检验解读',
  emergency_triage: '急诊分诊',
  emr_extract: '病历抽取',
  emr_draft: '病历草拟',
  patient_report_explain: '患者报告解读',
  patient_triage_normalize: '导诊口语归一',
  knowledge_qa: '知识库问答',
  operation_qa: '运营问数',
  previsit_summary: '预问诊摘要',
  followup_compose: '随访话术',
  insurance_evidence: '医保证据判定',
  health_check: '连通性自检',
}
const capabilityLabel = (key) => CAPABILITY_MAP[key] || key

const STATUS_MAP = {
  1: { label: '成功', tag: 'success' },
  2: { label: '失败', tag: 'danger' },
  3: { label: '超时', tag: 'warning' },
  4: { label: '降级', tag: 'warning' },
  5: { label: '熔断', tag: 'danger' },
}
const statusTag = (s) => STATUS_MAP[s]?.tag || 'info'
const statusLabel = (row) => row.statusText || STATUS_MAP[row.status]?.label || row.status
</script>

<style scoped>
.detail-text {
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.6;
}
</style>
