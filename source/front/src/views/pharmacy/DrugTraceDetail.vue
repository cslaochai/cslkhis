<template>
  <el-dialog v-model="visible" data-testid="trace-detail-dialog" title="追溯码详情" width="720px">
    <el-descriptions v-if="row" :column="2" border>
      <el-descriptions-item label="追溯码">{{ row.traceCode }}</el-descriptions-item>
      <el-descriptions-item label="院内流水号">{{ row.traceNo }}</el-descriptions-item>
      <el-descriptions-item label="药品">{{
          row.drugName
        }}{{ row.specification ? `（${row.specification}）` : '' }}
      </el-descriptions-item>
      <el-descriptions-item label="批准文号">{{ row.approvalNumber || '-' }}</el-descriptions-item>
      <el-descriptions-item label="生产厂家">{{ row.manufacturer || '-' }}</el-descriptions-item>
      <el-descriptions-item label="批号">{{ row.stockBatchNo || '-' }}</el-descriptions-item>
      <el-descriptions-item label="码制">{{ codeTypeText(row.codeType) }}</el-descriptions-item>
      <el-descriptions-item label="产品标识">{{ row.drugDi || '-' }}</el-descriptions-item>
      <el-descriptions-item label="序列号">{{ row.serialNo || '-' }}</el-descriptions-item>
      <el-descriptions-item label="采集来源">{{ sourceText(row.sourceType) }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ statusText(row.status) }}</el-descriptions-item>
      <el-descriptions-item label="上传状态">{{ uploadText(row.uploadStatus) }}</el-descriptions-item>
      <el-descriptions-item label="采集人">{{ row.operatorName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="采集时间">{{ row.scanTime || '-' }}</el-descriptions-item>
      <el-descriptions-item label="发药单">{{ row.dispensingNo || '-' }}</el-descriptions-item>
      <el-descriptions-item label="患者">{{ row.patientName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="核销时间">{{ row.dispenseTime || '-' }}</el-descriptions-item>
      <el-descriptions-item label="核销人">{{ row.dispenseOperator || '-' }}</el-descriptions-item>
      <el-descriptions-item label="上传批次">{{ row.uploadBatchNo || '-' }}</el-descriptions-item>
      <el-descriptions-item label="上传时间">{{ row.uploadTime || '-' }}</el-descriptions-item>
      <el-descriptions-item :span="2" label="上传失败原因">{{ row.uploadFailReason || '-' }}</el-descriptions-item>
      <el-descriptions-item label="作废类型">{{
          row.voidType ? voidText(row.voidType) : '-'
        }}
      </el-descriptions-item>
      <el-descriptions-item label="作废原因">{{ row.voidReason || '-' }}</el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 追溯码详情（只读，自 DrugTraceView.vue 原样搬出：行数据由父页面直接传入，无二次请求）
 * 状态/来源/上传/作废类型文案仍走父页面加载的字典（台账列同名，不在子组件重复请求）。
 */
import { computed } from 'vue'
import { dictLabelText } from '@/lib/utils'
import { codeTypeText } from '@/lib/drugTrace'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  row: { type: Object, default: null },
  statusDict: { type: Array, default: () => [] },
  uploadDict: { type: Array, default: () => [] },
  sourceDict: { type: Array, default: () => [] },
  voidDict: { type: Array, default: () => [] },
})
const emit = defineEmits(['update:modelValue'])

const visible = computed({ get: () => props.modelValue, set: (v) => emit('update:modelValue', v) })

const statusText = (v) => dictLabelText(props.statusDict, v)
const uploadText = (v) => dictLabelText(props.uploadDict, v)
const sourceText = (v) => dictLabelText(props.sourceDict, v)
const voidText = (v) => dictLabelText(props.voidDict, v)
</script>
