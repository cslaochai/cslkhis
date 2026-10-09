<script setup lang="ts">
import {computed, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {
  getCriticalValueDetail,
  getInspectionDetail,
  getLaboratoryDetail,
  handleCriticalValue,
  receiveCriticalValue
} from '@/api/medicaltech'
import {messageLabel} from '@/lib/messageCatalog'

const props = defineProps<{
  modelValue: boolean
  message: any | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'processed'): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (v: boolean) => emit('update:modelValue', v),
})

const bizType = computed(() => props.message?.bizType || '')
const isCritical = computed(() => bizType.value === 'critical')

const loading = ref(false)
// inspection / report 走结构化 HTML（与报告详情同口径）；critical 走 Vue 模板（带动作）
const htmlContent = ref('')
const criticalDetail = ref<any>(null)

const dialogTitle = computed(() => {
  if (!props.message) return '消息详情'
  const label = messageLabel(bizType.value)
  const prefix = CATALOG_PREFIX[bizType.value] || ''
  return prefix ? `${prefix} · ${label}` : props.message.title || label
})

const CATALOG_PREFIX: Record<string, string> = {
  inspection: '检查报告详情',
  report: '检验报告详情',
  critical: '危急值处置',
}

const fmtTime = (v?: string) => (v ? String(v).replace('T', ' ').slice(0, 16) : '—')

const loadDetail = async () => {
  if (!props.message) return
  loading.value = true
  htmlContent.value = ''
  criticalDetail.value = null
  try {
    const id = props.message.bizId
    if (bizType.value === 'inspection') {
      htmlContent.value = await renderInspection(id)
    } else if (bizType.value === 'report') {
      htmlContent.value = await renderLaboratory(id)
    } else if (isCritical.value) {
      await loadCritical(id)
    }
    // 其他类型：直接展示 message.content（模板里兜底）
  } catch (error: any) {
    console.error('加载消息详情失败', error)
    ElMessage.error(error?.message || '加载消息详情失败')
  } finally {
    loading.value = false
  }
}

const loadCritical = async (id: any) => {
  const res = await getCriticalValueDetail(id)
  if (!res.data) {
    ElMessage.warning('未找到该危急值记录（可能已被作废或删除）')
    return
  }
  criticalDetail.value = res.data
}

/* ---------- 检查/检验报告结构化渲染（自 MessagesView 收口至此） ---------- */

const renderInspection = async (recordId: any): Promise<string> => {
  const res = await getInspectionDetail(recordId)
  const record = res.data?.record || res.data
  if (!record) {
    ElMessage.warning('未找到检查记录')
    return ''
  }
  return `
    <div class="space-y-3 text-sm">
      <div class="grid grid-cols-2 gap-2">
        <p><strong>患者姓名：</strong>${record.patientName || '-'}</p>
        <p><strong>检查项目：</strong>${record.inspectionItemName || '-'}</p>
        <p><strong>检查部位：</strong>${record.bodyPart || '-'}</p>
        <p><strong>检查目的：</strong>${record.inspectionPurpose || '-'}</p>
        <p><strong>临床诊断：</strong>${record.clinicalDiagnosis || '-'}</p>
      </div>
      ${record.resultDescription ? `
      <div class="border-t pt-3">
        <p class="font-bold text-slate-700 mb-2">检查所见：</p>
        <p class="text-sm bg-slate-50 p-2 rounded whitespace-pre-wrap">${record.resultDescription}</p>
      </div>` : ''}
      ${record.resultConclusion ? `
      <div class="border-t pt-3">
        <p class="font-bold text-emerald-700 mb-2">影像诊断/印象：</p>
        <p class="text-sm bg-emerald-50 p-2 rounded whitespace-pre-wrap">${record.resultConclusion}</p>
      </div>` : ''}
      ${record.suggestions ? `
      <div class="border-t pt-3">
        <p class="font-bold text-slate-700 mb-2">建议：</p>
        <p class="text-sm bg-blue-50 p-2 rounded whitespace-pre-wrap">${record.suggestions}</p>
      </div>` : ''}
    </div>`
}

const renderLaboratory = async (recordId: any): Promise<string> => {
  const res = await getLaboratoryDetail(recordId)
  const detail = res.data
  const record = detail?.record || {}
  const results = detail?.results || []

  let resultsHtml = ''
  if (results.length > 0) {
    resultsHtml = `
      <div class="border-t pt-3">
        <p class="font-bold text-slate-700 mb-2">检验结果明细（共 ${results.length} 项）</p>
        <table class="w-full text-sm border-collapse">
          <thead>
            <tr class="bg-slate-100">
              <th class="border border-slate-300 px-3 py-2 text-left">项目名称</th>
              <th class="border border-slate-300 px-3 py-2 text-left">结果</th>
              <th class="border border-slate-300 px-3 py-2 text-left">单位</th>
              <th class="border border-slate-300 px-3 py-2 text-left">参考范围</th>
              <th class="border border-slate-300 px-3 py-2 text-left">状态</th>
            </tr>
          </thead>
          <tbody>
            ${results.map((r: any) => `
              <tr>
                <td class="border border-slate-300 px-3 py-2">${r.itemName || '-'}</td>
                <td class="border border-slate-300 px-3 py-2 ${r.abnormalFlag === 1 ? 'text-red-600 font-bold' : ''}">${r.resultValue || '-'}</td>
                <td class="border border-slate-300 px-3 py-2">${r.resultUnit || '-'}</td>
                <td class="border border-slate-300 px-3 py-2">${r.referenceRange || '-'}</td>
                <td class="border border-slate-300 px-3 py-2">${r.abnormalFlagText || '—'}</td>
              </tr>`).join('')}
          </tbody>
        </table>
      </div>`
  }
  return `
    <div class="space-y-3 text-sm">
      <div class="grid grid-cols-2 gap-2">
        <p><strong>患者姓名：</strong>${record.patientName || '-'}</p>
        <p><strong>检验项目：</strong>${record.laboratoryItemName || '-'}</p>
      </div>
      ${resultsHtml}
    </div>`
}

/* ---------- 危急值闭环动作 ---------- */

const receiving = ref(false)
const handleMeasure = ref('')
const handling = ref(false)

/** 1 待接收 → 可确认接收；2 已接收 → 可填处置；3 已处置 / 4 已作废 → 只读 */
const canReceive = computed(() => criticalDetail.value?.status === 1)
const canHandle = computed(() => criticalDetail.value?.status === 2)

const submitReceive = async () => {
  if (!criticalDetail.value) return
  receiving.value = true
  try {
    await receiveCriticalValue(criticalDetail.value.id)
    ElMessage.success('已确认接收，请及时填写处置措施')
    await loadCritical(criticalDetail.value.id)
    emit('processed')
  } catch (error: any) {
    ElMessage.error(error?.message || '接收失败')
  } finally {
    receiving.value = false
  }
}

const submitHandle = async () => {
  if (!criticalDetail.value) return
  if (!handleMeasure.value.trim()) {
    ElMessage.warning('请填写处置措施')
    return
  }
  handling.value = true
  try {
    await handleCriticalValue(criticalDetail.value.id, handleMeasure.value.trim())
    ElMessage.success('处置已记录，危急值闭环完成')
    await loadCritical(criticalDetail.value.id)
    emit('processed')
  } catch (error: any) {
    ElMessage.error(error?.message || '处置失败')
  } finally {
    handling.value = false
  }
}

watch(visible, (v) => {
  if (v) {
    handleMeasure.value = ''
    loadDetail()
  }
})
</script>

<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="60%" destroy-on-close>
    <div v-loading="loading" class="min-h-[120px]">
      <!-- 危急值：结构化详情 + 闭环动作 -->
      <template v-if="isCritical && criticalDetail">
        <div class="space-y-3 text-sm">
          <div class="flex flex-wrap items-center gap-2 rounded-lg border border-red-200 bg-red-50 p-3">
            <span class="font-semibold text-red-700">{{ criticalDetail.itemName || '-' }}</span>
            <span class="text-slate-600">{{ criticalDetail.patientName || '-' }}
              {{ criticalDetail.genderText || '' }} {{
                criticalDetail.age != null ? criticalDetail.age + '岁' : ''
              }}</span>
            <el-tag
                :type="criticalDetail.status === 1 ? 'danger' : criticalDetail.status === 2 ? 'warning' : criticalDetail.status === 4 ? 'info' : 'success'"
                size="small">
              {{ criticalDetail.statusText || `未知(${criticalDetail.status})` }}
            </el-tag>
            <el-tag v-if="criticalDetail.overdue && criticalDetail.status !== 3 && criticalDetail.status !== 4"
                    type="danger" size="small" effect="dark">已超时
            </el-tag>
          </div>

          <div class="grid grid-cols-2 gap-2">
            <p><strong>危急值号：</strong>{{ criticalDetail.criticalNo || '-' }}</p>
            <p><strong>结果：</strong>
              <span class="font-semibold text-red-600">{{ criticalDetail.resultValue || '-' }}</span>
              {{ criticalDetail.resultUnit || '' }}
              <span class="text-slate-400">（参考 {{ criticalDetail.referenceRange || '-' }}）</span>
            </p>
            <p><strong>异常类型：</strong>{{ criticalDetail.criticalTypeText || '-' }}</p>
            <p><strong>危急描述：</strong>{{ criticalDetail.criticalDesc || '-' }}</p>
            <p><strong>报告科室：</strong>{{ criticalDetail.reportDeptName || '-' }}</p>
            <p><strong>报告人：</strong>{{ criticalDetail.reportBy || '-' }}（{{ fmtTime(criticalDetail.reportTime) }}）
            </p>
            <p><strong>处置时限：</strong>{{ fmtTime(criticalDetail.deadlineTime) }}</p>
            <p v-if="criticalDetail.receiveBy"><strong>接收：</strong>{{
                criticalDetail.receiveBy
              }}（{{ fmtTime(criticalDetail.receiveTime) }}）</p>
            <p v-if="criticalDetail.handleBy"><strong>处置：</strong>{{
                criticalDetail.handleBy
              }}（{{ fmtTime(criticalDetail.handleTime) }}）</p>
          </div>

          <div v-if="criticalDetail.handleMeasure" class="border-t pt-3">
            <p class="mb-1 font-bold text-slate-700">处置措施：</p>
            <p class="whitespace-pre-wrap rounded bg-slate-50 p-2">{{ criticalDetail.handleMeasure }}</p>
          </div>

          <!-- 闭环动作：待接收 → 确认接收；已接收 → 填处置措施 -->
          <div v-if="canReceive" class="border-t pt-3">
            <el-button type="danger" :loading="receiving" v-perm="'portal:messages:edit'" @click="submitReceive">
              确认接收
            </el-button>
            <p class="mt-2 text-xs text-slate-400">确认接收后须在处置时限内填写处置措施完成闭环。</p>
          </div>
          <div v-else-if="canHandle" class="space-y-2 border-t pt-3">
            <el-input v-model="handleMeasure" type="textarea" :rows="3" maxlength="500" show-word-limit
                      placeholder="请填写处置措施（用药/复查/通知家属等临床动作，必填）"/>
            <el-button type="primary" :loading="handling" v-perm="'portal:messages:edit'" @click="submitHandle">
              提交处置
            </el-button>
          </div>
        </div>
      </template>

      <!-- 检查/检验报告：结构化 HTML -->
      <template v-else-if="htmlContent">
        <div v-html="htmlContent"></div>
      </template>

      <!-- 兜底：未接通处理链路的类型，展示消息正文 -->
      <template v-else-if="!loading && message">
        <p class="whitespace-pre-wrap text-sm text-slate-600">{{ message.content }}</p>
      </template>
    </div>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>
