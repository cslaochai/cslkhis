<script setup lang="ts">
import {ref, computed, onMounted} from 'vue'
import {Search, Aim, Check, Edit, View} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  getLaboratoryRecordListPage, getLaboratoryDetail, receiveSpecimen, inputLabResult, auditLaboratory
} from '@/api/medicaltech'
import {patientGenderText} from '@/lib/patientGender'
import {executeLabInterpret} from '@/api/ai'
import {getLaboratoryItemDetailList} from '@/api/system'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

interface LabResultItem {
  id?: number
  laboratoryItemId?: number
  laboratoryItemCode?: string
  laboratoryItemName: string
  resultValue: string
  resultUnit: string
  referenceRange: string
  /** null = 交给系统自动判定（后端按参考区间算，判不出来会保留并留痕） */
  abnormalFlag?: number | null
  abnormalDesc?: string
  /** 判定展示文案（正常/偏高/偏低/异常/未判定），由后端算好，前端不要自己用 abnormalFlag 拼 */
  abnormalFlagText?: string
  /** 判定留痕：判据，或未判定的原因 */
  judgeNote?: string
}

interface LaboratoryOrder {
  id: number
  recordNo: string
  itemName: string
  patientName: string
  patientNo?: string
  gender?: number
  age?: number
  doctorName: string
  deptName?: string
  orderTime: string
  statusCode: number
  status: string
  price: number
  reportSignId?: string
  reportSignedTime?: string
  auditSignId?: string
  auditSignedTime?: string
  specimenType?: string
  purpose?: string
  clinicalDiagnosis?: string
  results?: LabResultItem[]
}

const searchTerm = ref('')
const statusFilter = ref('all')
const selectedOrder = ref<LaboratoryOrder | null>(null)
const showDetailDialog = ref(false)
const showResultDialog = ref(false)
const showAuditDialog = ref(false)
const auditRemark = ref('')
const loading = ref(false)
const orders = ref<LaboratoryOrder[]>([])

const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
})
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

const laboratoryResultForm = ref<LabResultItem[]>([])
const laboratoryDiagnosis = ref('')
const laboratorySuggestions = ref('')

const statusMap: Record<number, string> = {
  1: '已登记',
  2: '已采样',
  3: '已接收',
  4: '检验中',
  5: '已出结果',
  6: '已审核',
  7: '已发布',
}

const statusTagType = (status: string) => {
  if (status.includes('已审核') || status.includes('已发布')) return 'success'
  if (status.includes('检验中')) return 'warning'
  if (status.includes('已取消')) return 'danger'
  return 'info'
}

/**
 * 异常标志展示文案一律取后端的 abnormalFlagText。
 *
 * 不要自己用 abnormalFlag 写三目判断：abnormal_flag 列的默认值是 0，
 * 「未判定」（参考区间无法解析）与「正常」在库里同值，
 * 用 `flag !== 1 && flag !== 2 ? '正常'` 会把「不知道」显示成「正常」。
 * 缺失就显示 —，不要替后端猜一个结论。
 */
const abnormalText = (row: LabResultItem) => row.abnormalFlagText || '—'

const abnormalTagType = (row: LabResultItem) => {
  const text = abnormalText(row)
  if (text === '偏高' || text === '异常') return 'danger'
  if (text === '偏低') return 'warning'
  if (text === '正常') return 'success'
  return 'info' // 未判定 / 未知
}

const filtered = computed(() =>
    orders.value.filter((o) => {
      const matchSearch = !searchTerm.value ||
          o.patientName?.includes(searchTerm.value) ||
          o.itemName?.includes(searchTerm.value) ||
          o.doctorName?.includes(searchTerm.value) ||
          o.recordNo?.includes(searchTerm.value)
      const matchStatus = statusFilter.value === 'all' || o.status === statusFilter.value
      return matchSearch && matchStatus
    })
)

const statusCounts = computed(() => ({
  pending: orders.value.filter((o) => o.statusCode === 1 || o.statusCode === 2).length,
  processing: orders.value.filter((o) => o.statusCode === 3 || o.statusCode === 4).length,
  completed: orders.value.filter((o) => o.statusCode === 5 || o.statusCode === 6).length,
  published: orders.value.filter((o) => o.statusCode === 7).length,
}))

const loadData = async () => {
  loading.value = true
  try {
    const res = await getLaboratoryRecordListPage({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      patientName: searchTerm.value || undefined,
      recordStatus: statusFilter.value !== 'all' ? getStatusKey(statusFilter.value) : undefined,
    })
    const list = (res.data?.records || []).map((item: any) => ({
      id: item.id,
      recordNo: item.recordNo,
      itemName: item.laboratoryItemName,
      patientName: item.patientName,
      patientNo: item.patientNo,
      gender: item.gender,
      age: item.age,
      doctorName: item.applyDoctorName,
      deptName: item.applyDeptName,
      orderTime: item.createTime?.split('T')[0],
      statusCode: item.recordStatus || 1,
      // 未知码值渲染成「未知(n)」，不回落成某个合法状态
      status: statusMap[item.recordStatus] ?? (item.recordStatus == null ? '未知' : `未知(${item.recordStatus})`),
      price: item.price || 0,
      reportSignId: item.reportSignId,
      reportSignedTime: item.reportSignedTime,
      auditSignId: item.auditSignId,
      auditSignedTime: item.auditSignedTime,
      specimenType: item.specimenType,
      purpose: item.laboratoryPurpose,
      clinicalDiagnosis: item.clinicalDiagnosis,
      results: [],
    }))
    orders.value = list
    pagination.value.total = res.data?.total || 0
  } catch (error) {
    console.error('加载检验记录失败:', error)
  } finally {
    loading.value = false
  }
}

const getStatusKey = (statusName: string) => {
  for (const [key, value] of Object.entries(statusMap)) {
    if (value === statusName) return parseInt(key)
  }
  return null
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchTerm.value = ''
  statusFilter.value = 'all'
  pagination.value.pageNum = 1
  loadData()
}

const handleSizeChange = (val: number) => {
  pagination.value.pageSize = val
  pagination.value.pageNum = 1
  loadData()
}

const handleCurrentChange = (val: number) => {
  pagination.value.pageNum = val
  loadData()
}

const handleViewDetail = async (row: LaboratoryOrder) => {
  selectedOrder.value = row
  // 换记录时清掉上一次的解读结果，避免张冠李戴
  interpret.value = null
  interpretLoading.value = false
  // 加载检验结果
  try {
    const res = await getLaboratoryDetail(row.id)
    if (res.data?.results) {
      selectedOrder.value.results = res.data.results
    }
  } catch (e) {
    console.error('加载详情失败', e)
  }
  showDetailDialog.value = true
}

// ---------------- AI 检验解读 ----------------

const interpret = ref<any>(null)
const interpretLoading = ref(false)
const overwriteOnWriteBack = ref(false)

/**
 * 调用解读接口。
 *
 * overwrite 为 false 时只是拿一份结论草稿，不碰检验记录；
 * 只有用户明确勾选并点「写回结论」才会覆盖 diagnosis / suggestions。
 */
const runInterpret = async (overwrite: boolean) => {
  if (!selectedOrder.value) return
  if (overwrite && !overwriteOnWriteBack.value) {
    ElMessage.warning('请先勾选「同时写回检验记录的结论/建议」')
    return
  }
  interpretLoading.value = true
  try {
    const res = await executeLabInterpret({
      recordId: selectedOrder.value.id,
      overwriteConclusion: overwrite,
      includeTrend: true,
    })
    interpret.value = res.data
    if (overwrite) {
      ElMessage.success('结论已写回检验记录')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '解读失败')
  } finally {
    interpretLoading.value = false
  }
}

const handleReceiveSpecimen = async (row: LaboratoryOrder) => {
  try {
    await ElMessageBox.confirm(`确认接收 ${row.patientName} 的标本？`, '接收标本', {
      confirmButtonText: '确认接收',
      cancelButtonText: '取消',
      type: 'info',
    })
    await receiveSpecimen(row.id, '当前用户')
    ElMessage.success('标本接收成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '接收失败')
    }
  }
}

const handleResultEntry = async (row: LaboratoryOrder) => {
  selectedOrder.value = row
  // 加载已有的检验结果
  try {
    const res = await getLaboratoryDetail(row.id)
    if (res.data?.results?.length > 0) {
      laboratoryResultForm.value = res.data.results.map((r: any) => ({
        id: r.id,
        laboratoryItemId: r.laboratoryItemId,
        laboratoryItemCode: r.laboratoryItemCode,
        laboratoryItemName: r.laboratoryItemName,
        resultValue: r.resultValue || '',
        resultUnit: r.resultUnit || '',
        referenceRange: r.referenceRange || '',
        abnormalFlag: r.abnormalFlag ?? null,
        abnormalDesc: r.abnormalDesc || '',
        abnormalFlagText: r.abnormalFlagText,
        judgeNote: r.judgeNote,
      }))
    } else {
      // 根据检验大项目加载明细项目
      await loadLaboratoryItemDetails(row)
    }
  } catch {
    await loadLaboratoryItemDetails(row)
  }
  // 初始化诊断和建议
  laboratoryDiagnosis.value = ''
  laboratorySuggestions.value = ''
  showResultDialog.value = true
}

// 加载检验项目明细
const loadLaboratoryItemDetails = async (row: LaboratoryOrder) => {
  try {
    // 从检验记录中获取检验项目ID
    const recordRes = await getLaboratoryRecordListPage({
      patientId: row.patientId || undefined,
      pageNum: 1,
      pageSize: 100
    })
    const records = recordRes.data?.records || []
    const record = records.find((r: any) => r.id === row.id)

    if (record && record.laboratoryItemId) {
      // 根据检验项目ID加载明细
      const detailRes = await getLaboratoryItemDetailList(record.laboratoryItemId)
      const details = detailRes.data || []

      if (details.length > 0) {
        laboratoryResultForm.value = details.map((d: any) => ({
          laboratoryItemId: d.laboratoryItemId,
          laboratoryItemCode: d.itemCode,
          laboratoryItemName: d.itemName,
          resultValue: '',
          resultUnit: d.unit || '',
          referenceRange: d.referenceRange || '',
          abnormalFlag: null,
          abnormalDesc: '',
        }))
      } else {
        // 没有配置明细，使用默认
        laboratoryResultForm.value = [{
          laboratoryItemName: row.itemName,
          resultValue: '',
          resultUnit: '',
          referenceRange: '',
          abnormalFlag: null,
        }]
      }
    } else {
      // 没有找到检验项目ID，使用默认
      laboratoryResultForm.value = [{
        laboratoryItemName: row.itemName,
        resultValue: '',
        resultUnit: '',
        referenceRange: '',
        abnormalFlag: null,
      }]
    }
  } catch (e) {
    console.error('加载检验项目明细失败:', e)
    laboratoryResultForm.value = [{
      laboratoryItemName: row.itemName,
      resultValue: '',
      resultUnit: '',
      referenceRange: '',
      abnormalFlag: null,
    }]
  }
}

const handleSubmitResult = async () => {
  if (!selectedOrder.value) return

  try {
    await inputLabResult(selectedOrder.value.id, {
      results: laboratoryResultForm.value,
      diagnosis: laboratoryDiagnosis.value,
      suggestions: laboratorySuggestions.value,
    })
    ElMessage.success('检验结果提交成功')
    showResultDialog.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  }
}

const handleAudit = async (row: LaboratoryOrder) => {
  selectedOrder.value = {...row}
  auditRemark.value = ''
  // 加载检验结果
  try {
    const res = await getLaboratoryDetail(row.id)
    if (res.data?.record) {
      Object.assign(selectedOrder.value, res.data.record)
    }
    if (res.data?.results) {
      selectedOrder.value.results = res.data.results
    }
  } catch (e) {
    console.error('加载详情失败', e)
  }
  showAuditDialog.value = true
}

const handleSubmitAudit = async () => {
  if (!selectedOrder.value) return

  try {
    await auditLaboratory(selectedOrder.value.id, '当前用户')
    ElMessage.success('审核成功')
    showAuditDialog.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '审核失败')
  }
}

const addLabResultItem = () => {
  laboratoryResultForm.value.push({
    laboratoryItemId: undefined,
    laboratoryItemCode: '',
    laboratoryItemName: '',
    resultValue: '',
    resultUnit: '',
    referenceRange: '',
    abnormalFlag: null,
  })
}

const removeLabResultItem = (index: number) => {
  laboratoryResultForm.value.splice(index, 1)
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div v-loading="loading">
    <!-- 统计卡片 -->
    <div class="mb-3 grid grid-cols-2 gap-4 sm:grid-cols-4">
      <div v-for="item in [
        { label: '待处理', value: statusCounts.pending, color: 'text-slate-600', bg: 'bg-slate-50' },
        { label: '检验中', value: statusCounts.processing, color: 'text-amber-600', bg: 'bg-amber-50' },
        { label: '待审核', value: statusCounts.completed, color: 'text-blue-600', bg: 'bg-blue-50' },
        { label: '已发布', value: statusCounts.published, color: 'text-emerald-600', bg: 'bg-emerald-50' },
      ]" :key="item.label" class="rounded-lg border border-slate-200 bg-white p-4 text-center shadow-sm">
        <p :class="['text-2xl font-bold', item.color]">{{ item.value }}</p>
        <p class="mt-1 text-xs text-slate-500">{{ item.label }}</p>
      </div>
    </div>

    <!-- 查询卡 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input v-model="searchTerm" placeholder="搜索患者/项目/医生..." :prefix-icon="Search" class="!w-64"
                    @keyup.enter="handleSearch"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="statusFilter" class="!w-32" placeholder="状态">
            <el-option label="全部状态" value="all"/>
            <el-option v-for="s in ['已登记','已采样','已接收','检验中','已出结果','已审核']" :key="s" :label="s"
                       :value="s"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格卡 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="filtered" style="width: 100%" stripe :max-height="tableMaxHeight">
        <el-table-column prop="recordNo" label="记录号" width="200"/>
        <el-table-column prop="patientName" label="患者" width="150"/>
        <el-table-column label="性别/年龄" width="200" align="center">
          <template #default="{ row }">
            <span class="text-xs text-slate-600">{{ patientGenderText(row.gender) }} {{
                row.age
              }}岁</span>
          </template>
        </el-table-column>
        <el-table-column prop="itemName" label="检验项目" min-width="120"/>
        <el-table-column label="标本" width="80">
          <template #default="{ row }">
            <span class="text-xs">{{ row.specimenType }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="申请科室" width="150"/>
        <el-table-column prop="doctorName" label="申请医生" width="150"/>
        <el-table-column prop="orderTime" label="申请时间" width="200"/>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag effect="plain" size="small" :type="statusTagType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="签名" width="120" align="center">
          <template #default="{ row }">
            <div class="flex flex-col items-center gap-0.5 text-xs leading-5">
              <span :class="row.reportSignId ? 'text-emerald-600' : 'text-slate-400'">
                报告{{ row.reportSignId ? '已签' : '未签' }}
              </span>
              <span :class="row.auditSignId ? 'text-emerald-600' : 'text-amber-600'">
                审核{{ row.auditSignId ? '已签' : '未签' }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">
              <el-icon class="mr-0.5">
                <View/>
              </el-icon>
              详情
            </el-button>
            <el-button
                v-if="row.statusCode === 1"
                v-perm="'medtech:laboratoryWorkstation:edit'"
                type="info" link size="small"
                @click="handleReceiveSpecimen(row)">
              接收标本
            </el-button>
            <el-button
                v-if="row.statusCode === 2 || row.statusCode === 3"
                v-perm="'medtech:laboratoryWorkstation:edit'"
                type="warning" link size="small"
                @click="handleResultEntry(row)">
              <el-icon class="mr-0.5">
                <Edit/>
              </el-icon>
              录入
            </el-button>
            <el-button
                v-if="row.statusCode === 4 || row.statusCode === 5"
                v-perm="'medtech:laboratoryWorkstation:edit'"
                type="success" link size="small"
                @click="handleAudit(row)">
              审核
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="showDetailDialog" :title="`检验详情 - ${selectedOrder?.itemName}`" width="920px"
               destroy-on-close>
      <template v-if="selectedOrder">
        <div class="space-y-4 py-2">
          <div class="grid grid-cols-2 gap-4 rounded-lg border border-slate-200 p-4">
            <div><p class="text-xs text-slate-400">患者</p>
              <p class="text-sm font-medium">{{ selectedOrder.patientName }}</p></div>
            <div><p class="text-xs text-slate-400">记录号</p>
              <p class="text-sm font-medium">{{ selectedOrder.recordNo }}</p></div>
            <div><p class="text-xs text-slate-400">申请医生</p>
              <p class="text-sm font-medium">{{ selectedOrder.doctorName }}</p></div>
            <div><p class="text-xs text-slate-400">申请科室</p>
              <p class="text-sm font-medium">{{ selectedOrder.deptName }}</p></div>
            <div><p class="text-xs text-slate-400">检验项目</p>
              <p class="text-sm font-medium">{{ selectedOrder.itemName }}</p></div>
            <div><p class="text-xs text-slate-400">标本类型</p>
              <p class="text-sm font-medium">{{ selectedOrder.specimenType }}</p></div>
            <div><p class="text-xs text-slate-400">费用</p>
              <p class="text-sm font-medium">¥{{ selectedOrder.price }}</p></div>
            <div><p class="text-xs text-slate-400">临床诊断</p>
              <p class="text-sm font-medium">{{ selectedOrder.clinicalDiagnosis }}</p></div>
          </div>

          <!-- 检验结果 -->
          <div v-if="selectedOrder.results?.length" class="rounded-lg border border-slate-200 p-4">
            <p class="mb-3 text-sm font-medium text-slate-700">检验结果</p>
            <el-table :data="selectedOrder.results" size="small" border>
              <el-table-column prop="laboratoryItemName" label="项目" min-width="120"/>
              <el-table-column prop="resultValue" label="结果" width="100"/>
              <el-table-column prop="resultUnit" label="单位" width="80"/>
              <el-table-column prop="referenceRange" label="参考范围" width="100"/>
              <el-table-column label="状态" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="abnormalTagType(row)" size="small" effect="plain">
                    {{ abnormalText(row) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="判定说明" min-width="160" show-overflow-tooltip>
                <template #default="{ row }">
                  <span class="text-xs text-slate-500">{{ row.judgeNote || '—' }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- AI 检验解读 -->
          <div class="rounded-lg border border-slate-200 p-4">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm font-medium text-slate-700">检验结果智能解读</p>
                <p class="mt-0.5 text-xs text-slate-400">
                  异常项、危急值、趋势由系统按规则算出，模型只负责解释它们组合起来意味着什么
                </p>
              </div>
              <el-button
                type="primary"
                :loading="interpretLoading"
                :disabled="!selectedOrder.results?.length"
                @click="runInterpret(false)"
              >
                {{ interpret ? '重新解读' : 'AI 解读' }}
              </el-button>
            </div>

            <div v-if="interpret" class="mt-3 space-y-3">
              <!-- 降级提示：必须让医生知道这条结论是谁给的 -->
              <el-alert
                v-if="interpret.degraded"
                type="warning"
                :closable="false"
                show-icon
                title="本次未经过大模型，结论来自确定性规则"
              >
                <template #default>
                  <span class="text-xs">{{ interpret.degradeReason }}</span>
                </template>
              </el-alert>
              <el-alert
                v-else
                type="success"
                :closable="false"
                show-icon
                title="已由大模型解读"
              />

              <!-- 计数 -->
              <div class="grid grid-cols-4 gap-2 text-center">
                <div class="rounded bg-slate-50 py-2">
                  <p class="text-lg font-bold text-slate-800">{{ interpret.itemCount ?? 0 }}</p>
                  <p class="text-xs text-slate-500">结果项</p>
                </div>
                <div class="rounded bg-red-50 py-2">
                  <p class="text-lg font-bold text-red-600">{{ interpret.abnormalCount ?? 0 }}</p>
                  <p class="text-xs text-slate-500">异常</p>
                </div>
                <div class="rounded bg-red-50 py-2">
                  <p class="text-lg font-bold text-red-600">{{ interpret.criticalCount ?? 0 }}</p>
                  <p class="text-xs text-slate-500">危急值</p>
                </div>
                <div class="rounded py-2" :class="interpret.unjudgedCount ? 'bg-amber-50' : 'bg-slate-50'">
                  <p class="text-lg font-bold" :class="interpret.unjudgedCount ? 'text-amber-600' : 'text-slate-800'">
                    {{ interpret.unjudgedCount ?? 0 }}
                  </p>
                  <p class="text-xs text-slate-500">未判定</p>
                </div>
              </div>

              <!-- 趋势 -->
              <div v-if="interpret.trends?.length" class="rounded border border-slate-200 p-3">
                <p class="mb-2 text-xs font-medium text-slate-600">历史趋势（系统比对得出）</p>
                <div v-for="t in interpret.trends" :key="t.itemName" class="flex items-center gap-2 py-0.5 text-xs">
                  <span class="font-medium text-slate-700">{{ t.itemName }}</span>
                  <span :class="t.direction === '升高' ? 'text-red-600' : 'text-blue-600'">
                    {{ t.changeText }}
                  </span>
                  <span v-if="t.unit" class="text-slate-400">{{ t.unit }}</span>
                  <span v-if="t.magnitudeText" class="text-slate-400">· {{ t.magnitudeText }}</span>
                </div>
              </div>

              <!-- 逐项标志 -->
              <el-table :data="interpret.items || []" size="small" border max-height="240">
                <el-table-column prop="itemName" label="项目" min-width="110" />
                <el-table-column label="结果" width="110">
                  <template #default="{ row }">
                    {{ row.resultValue }} <span class="text-xs text-slate-400">{{ row.resultUnit }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="referenceRange" label="参考区间" width="110" />
                <el-table-column label="标志" width="80" align="center">
                  <template #default="{ row }">
                    <el-tag :type="abnormalTagType(row)" size="small" effect="plain">
                      {{ abnormalText(row) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="危急" width="70" align="center">
                  <template #default="{ row }">
                    <el-tag v-if="row.critical" type="danger" size="small">危急</el-tag>
                    <span v-else class="text-xs text-slate-300">—</span>
                  </template>
                </el-table-column>
                <el-table-column label="判定留痕" min-width="170" show-overflow-tooltip>
                  <template #default="{ row }">
                    <span class="text-xs text-slate-500">{{ row.judgeNote || '—' }}</span>
                  </template>
                </el-table-column>
              </el-table>

              <!-- 结论与建议 -->
              <div class="rounded border border-slate-200 p-3">
                <p class="mb-1 text-xs font-medium text-slate-600">结论草稿</p>
                <p class="whitespace-pre-wrap text-sm text-slate-800">{{ interpret.conclusion }}</p>

                <template v-if="interpret.trendSummary">
                  <p class="mt-3 mb-1 text-xs font-medium text-slate-600">趋势解读</p>
                  <p class="text-sm text-slate-700">{{ interpret.trendSummary }}</p>
                </template>

                <template v-if="interpret.suggestions?.length">
                  <p class="mt-3 mb-1 text-xs font-medium text-slate-600">建议</p>
                  <ul class="list-disc pl-5 text-sm text-slate-700">
                    <li v-for="(s, i) in interpret.suggestions" :key="i">{{ s }}</li>
                  </ul>
                </template>

                <template v-if="interpret.attentionPoints?.length">
                  <p class="mt-3 mb-1 text-xs font-medium text-slate-600">需要重点关注</p>
                  <ul class="list-disc pl-5 text-sm text-red-600">
                    <li v-for="(s, i) in interpret.attentionPoints" :key="i">{{ s }}</li>
                  </ul>
                </template>

                <p v-if="interpret.saveTip" class="mt-2 text-xs text-slate-400">{{ interpret.saveTip }}</p>
                <p v-if="interpret.latencyMs != null" class="mt-1 text-xs text-slate-300">
                  耗时 {{ interpret.latencyMs }} ms
                </p>
              </div>

              <div class="flex items-center justify-between">
                <el-checkbox v-model="overwriteOnWriteBack">
                  同时写回检验记录的「结论/建议」（会覆盖检验科已填内容）
                </el-checkbox>
                <el-button
                  v-perm="'medtech:laboratoryWorkstation:edit'"
                  type="primary"
                  plain
                  size="small"
                  :loading="interpretLoading"
                  @click="runInterpret(true)"
                >
                  写回结论
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 结果录入对话框 -->
    <el-dialog v-model="showResultDialog" title="检验结果录入" width="700px" destroy-on-close>
      <template v-if="selectedOrder">
        <div class="space-y-4">
          <div class="rounded-lg bg-slate-50 p-3 text-sm">
            <span class="text-slate-500">患者：</span>
            <span class="font-medium">{{ selectedOrder.patientName }}</span>
            <span class="mx-2 text-slate-300">|</span>
            <span class="text-slate-500">项目：</span>
            <span class="font-medium">{{ selectedOrder.itemName }}</span>
          </div>

          <div class="flex items-center justify-between">
            <label class="text-sm font-medium text-slate-700">检验结果明细</label>
            <span class="text-xs text-slate-400">请根据检验结果填写各指标值</span>
          </div>
          <div class="space-y-3">
            <div v-for="(item, index) in laboratoryResultForm" :key="index"
                 class="rounded-lg border border-slate-200 p-3">
              <div class="mb-2 flex items-center gap-2">
                <span class="text-sm font-medium text-slate-700">{{ item.laboratoryItemName }}</span>
                <span class="text-xs text-slate-400">({{ item.laboratoryItemCode }})</span>
              </div>
              <div class="grid grid-cols-3 gap-3">
                <div>
                  <label class="mb-1 block text-xs text-slate-500">结果值 <span class="text-red-500">*</span></label>
                  <el-input v-model="item.resultValue" placeholder="结果值" size="small"/>
                </div>
                <div>
                  <label class="mb-1 block text-xs text-slate-500">单位</label>
                  <el-input v-model="item.resultUnit" placeholder="单位" size="small" disabled/>
                </div>
                <div>
                  <label class="mb-1 block text-xs text-slate-500">参考范围</label>
                  <el-input v-model="item.referenceRange" placeholder="参考范围" size="small" disabled/>
                </div>
              </div>
              <div class="mt-2 flex items-center gap-3">
                <el-select v-model="item.abnormalFlag" placeholder="异常标志" size="small" class="!w-40" clearable>
                  <el-option label="由系统自动判定" :value="null"/>
                  <el-option label="正常" :value="0"/>
                  <el-option label="偏高" :value="1"/>
                  <el-option label="偏低" :value="2"/>
                  <el-option label="异常" :value="3"/>
                </el-select>
                <el-input v-model="item.abnormalDesc" placeholder="异常描述（可选）" size="small" class="flex-1"/>
              </div>
              <p class="mt-1 text-[11px] text-slate-400">
                结果提交后系统会按参考区间自动判定并覆盖这里的取值；
                只有参考区间无法解析等判不出来的情况，才用得上人工指定。
              </p>
            </div>
          </div>

          <!-- 检验结论和建议 -->
          <div class="space-y-3 border-t pt-3">
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">检验结论/诊断</label>
              <el-input v-model="laboratoryDiagnosis" type="textarea" :rows="2"
                        placeholder="请根据检验结果给出结论，如：各项指标均在正常范围内"/>
            </div>
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">建议</label>
              <el-input v-model="laboratorySuggestions" type="textarea" :rows="2"
                        placeholder="请填写建议，如：建议复查、注意饮食等"/>
            </div>
          </div>
        </div>
      </template>
      <template #footer>
        <el-button @click="showResultDialog = false">取消</el-button>
        <el-button v-perm="'medtech:laboratoryWorkstation:edit'" type="primary" @click="handleSubmitResult">提交结果</el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="showAuditDialog" title="审核检验报告" width="700px" destroy-on-close>
      <template v-if="selectedOrder">
        <div class="space-y-4">
          <!-- 患者信息 -->
          <div class="rounded-lg bg-slate-50 p-4">
            <h4 class="mb-2 text-sm font-bold text-slate-700">患者信息</h4>
            <div class="grid grid-cols-3 gap-3 text-sm">
              <div><span class="text-slate-500">患者：</span><span class="font-medium">{{
                  selectedOrder.patientName
                }}</span></div>
              <div><span class="text-slate-500">记录号：</span><span class="font-medium">{{
                  selectedOrder.recordNo
                }}</span></div>
              <div><span class="text-slate-500">申请科室：</span><span class="font-medium">{{
                  selectedOrder.deptName
                }}</span></div>
              <div><span class="text-slate-500">检验项目：</span><span class="font-medium">{{
                  selectedOrder.itemName
                }}</span></div>
              <div><span class="text-slate-500">申请医生：</span><span class="font-medium">{{
                  selectedOrder.doctorName
                }}</span></div>
              <div><span class="text-slate-500">标本类型：</span><span class="font-medium">{{
                  selectedOrder.specimenType
                }}</span></div>
            </div>
          </div>

          <!-- 检验结果 -->
          <div v-if="selectedOrder.results?.length" class="rounded-lg border border-slate-200 p-4">
            <h4 class="mb-3 text-sm font-bold text-slate-700">检验结果明细</h4>
            <el-table :data="selectedOrder.results" size="small" border max-height="300">
              <el-table-column prop="laboratoryItemName" label="项目名称" min-width="120"/>
              <el-table-column prop="resultValue" label="结果" width="100"/>
              <el-table-column prop="resultUnit" label="单位" width="80"/>
              <el-table-column prop="referenceRange" label="参考范围" width="100"/>
              <el-table-column label="状态" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="abnormalTagType(row)" size="small" effect="plain">
                    {{ abnormalText(row) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 审核意见 -->
          <div class="rounded-lg border border-slate-200 p-4">
            <h4 class="mb-2 text-sm font-bold text-slate-700">审核意见</h4>
            <el-input v-model="auditRemark" type="textarea" :rows="3" placeholder="请输入审核意见（可选）..."/>
          </div>
        </div>
      </template>
      <template #footer>
        <el-button @click="showAuditDialog = false">取消</el-button>
        <el-button v-perm="'medtech:laboratoryWorkstation:edit'" type="primary" @click="handleSubmitAudit">
          <el-icon class="mr-0.5">
            <Check/>
          </el-icon>
          确认审核通过
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
