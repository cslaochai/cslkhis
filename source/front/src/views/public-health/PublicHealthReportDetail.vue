<template>
  <el-dialog v-model="visible" :title="`上报详情 ${row.reportNo || ''}`" width="620px">
    <el-descriptions :column="2" border size="small">
      <el-descriptions-item label="患者">{{ row.patientName }}（{{ row.patientNo }}）</el-descriptions-item>
      <el-descriptions-item label="上报类型">{{ typeText(row.reportType) }}</el-descriptions-item>
      <el-descriptions-item label="诊断">{{ row.diagnosis }}</el-descriptions-item>
      <el-descriptions-item label="ICD-10">{{ row.diagnosisCode || '—' }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ statusText(row.reportStatus) }}</el-descriptions-item>
      <el-descriptions-item label="上报时间">{{ fmtTime(row.reportTime) }}</el-descriptions-item>
      <el-descriptions-item label="上报人">{{ row.reportBy || '—' }}</el-descriptions-item>
      <el-descriptions-item label="审核人">{{ row.auditBy || '—' }}</el-descriptions-item>
      <el-descriptions-item :span="2" label="上报内容">{{ row.reportContent || '—' }}</el-descriptions-item>
      <el-descriptions-item :span="2" label="审核意见">{{ row.auditRemark || '—' }}</el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<script setup>
/**
 * 公卫上报详情弹框（自 PublicHealthReportView 拆出）
 *
 * 只读展示列表行数据（该模块无单条详情接口，拆分前就是把 row 直接放进 detail），
 * 字典由页面统一加载后传入，与列表列同一口径。
 */
import {computed} from 'vue'
import {dictLabelText} from '@/lib/utils'

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 展示行快照
  row: {type: Object, default: () => ({})},
  // 上报类型字典（页面已加载）
  typeDict: {type: Array, default: () => []},
  // 状态字典（页面已加载）
  statusDict: {type: Array, default: () => []},
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '—')
const typeText = (v) => dictLabelText(props.typeDict, v)
const statusText = (v) => dictLabelText(props.statusDict, v)
</script>
