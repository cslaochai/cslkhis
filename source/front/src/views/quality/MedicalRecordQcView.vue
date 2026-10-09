<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Refresh, Search, WarningFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import {
  getQualityControlList,
  getQualityControlDetail,
  getQualityControlOverview,
  listQcRuleMetric,
  listQcCandidatePage,
  getQcTypeDict,
  executeQualityControl,
  executeQualityControlBatch,
  handleQualityControl,
  startQcFlow,
  approveQcFlow,
  returnQcFlow,
  resubmitQcFlow,
  finalQcFlow,
  getQcFlowListPage,
  getQcFlowDetail,
  getQcFlowActions,
} from '@/api/qualityControl'
import {
  flowStatusText,
  flowLevelText,
  flowActionText,
  flowGradeText,
  flowStatusTag,
} from '@/lib/recordQcFlow'

// ---------------- 概览 ----------------
const overviewLoading = ref(false)
const overview = ref<any>(null)

// ---------------- 质控单 ----------------
const qcLoading = ref(false)
const qcList = ref<any[]>([])
const qcTotal = ref(0)
const qcQuery = reactive<any>({
  recordSource: '',
  qcType: undefined,
  qcStatus: undefined,
  qcResult: undefined,
  keyword: '',
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
})

// ---------------- 详情抽屉 ----------------
const drawerVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<any>(null)
const handleRemark = ref('')
const handling = ref(false)

// ---------------- 待质控病历 ----------------
const candLoading = ref(false)
const candList = ref<any[]>([])
const candTotal = ref(0)
const candSelection = ref<any[]>([])
const candTableRef = ref()
const candQuery = reactive<any>({
  recordSource: 'INPATIENT',
  recordStatus: undefined,
  recordType: undefined,
  onlyUnQced: false,
  keyword: '',
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
})
const qcRunning = ref(false)
const lastResult = ref<any>(null)

// ---------------- 规则清单 ----------------
const ruleLoading = ref(false)
const ruleList = ref<any[]>([])

const qcTypeOptions = ref<any[]>([])
const activeTab = ref('qc')

const recordStatusOptions = [
  { value: 1, label: '草稿' },
  { value: 2, label: '已提交' },
  { value: 3, label: '已归档' },
  { value: 4, label: '已作废' },
]

const recordTypeOptions = [
  { value: 1, label: '入院记录' },
  { value: 2, label: '首次病程' },
  { value: 3, label: '日常病程' },
  { value: 4, label: '术前小结' },
  { value: 5, label: '手术记录' },
  { value: 6, label: '术后首次病程' },
  { value: 7, label: '出院记录' },
  { value: 8, label: '死亡记录' },
  { value: 9, label: '会诊记录' },
  { value: 10, label: '转科记录' },
  { value: 11, label: '输血记录' },
]

const dimColor: Record<string, string> = {
  1: '#1269B5',
  2: '#0E9488',
  3: '#D97706',
}

// ---------------- 加载 ----------------
const loadOverview = async () => {
  overviewLoading.value = true
  try {
    const res = await getQualityControlOverview()
    overview.value = res.data
  } catch (e: any) {
    ElMessage.error(e?.message || '加载质控概览失败')
  } finally {
    overviewLoading.value = false
  }
}

const loadQcList = async () => {
  qcLoading.value = true
  try {
    const params: any = { pageNum: qcQuery.pageNum, pageSize: qcQuery.pageSize }
    if (qcQuery.recordSource) params.recordSource = qcQuery.recordSource
    if (qcQuery.qcType !== undefined && qcQuery.qcType !== null) params.qcType = qcQuery.qcType
    if (qcQuery.qcStatus !== undefined && qcQuery.qcStatus !== null) params.qcStatus = qcQuery.qcStatus
    if (qcQuery.qcResult !== undefined && qcQuery.qcResult !== null) params.qcResult = qcQuery.qcResult
    if (qcQuery.keyword) params.keyword = qcQuery.keyword.trim()
    const res = await getQualityControlList(params)
    qcList.value = res.data?.records || []
    qcTotal.value = Number(res.data?.total ?? 0)
  } catch (e: any) {
    ElMessage.error(e?.message || '加载质控单失败')
  } finally {
    qcLoading.value = false
  }
}

const loadCandidates = async () => {
  candLoading.value = true
  try {
    const params: any = {
      recordSource: candQuery.recordSource,
      pageNum: candQuery.pageNum,
      pageSize: candQuery.pageSize,
    }
    if (candQuery.recordStatus) params.recordStatus = candQuery.recordStatus
    if (candQuery.recordType && candQuery.recordSource === 'INPATIENT') params.recordType = candQuery.recordType
    if (candQuery.onlyUnQced) params.onlyUnQced = true
    if (candQuery.keyword) params.keyword = candQuery.keyword.trim()
    const res = await listQcCandidatePage(params)
    candList.value = res.data?.records || []
    candTotal.value = Number(res.data?.total ?? 0)
  } catch (e: any) {
    ElMessage.error(e?.message || '加载待质控病历失败')
  } finally {
    candLoading.value = false
  }
}

const loadRules = async () => {
  ruleLoading.value = true
  try {
    const res = await listQcRuleMetric()
    ruleList.value = res.data || []
  } catch (e: any) {
    ElMessage.error(e?.message || '加载规则清单失败')
  } finally {
    ruleLoading.value = false
  }
}

const loadDicts = async () => {
  try {
    const res = await getQcTypeDict()
    qcTypeOptions.value = res.data || []
  } catch (e: any) {
    ElMessage.error(e?.message || '加载质控类型字典失败')
  }
}

const refreshAll = async () => {
  await Promise.all([loadOverview(), loadQcList(), loadCandidates(), loadRules()])
}

// ---------------- 质控单操作 ----------------
const openDetail = async (row: any) => {
  drawerVisible.value = true
  detailLoading.value = true
  detail.value = null
  handleRemark.value = ''
  try {
    const res = await getQualityControlDetail(row.id)
    detail.value = res.data
  } catch (e: any) {
    ElMessage.error(e?.message || '加载质控单详情失败')
  } finally {
    detailLoading.value = false
  }
}

const submitHandle = async (ignore: boolean) => {
  if (!detail.value) return
  handling.value = true
  try {
    await handleQualityControl(detail.value.id, { ignore, remark: handleRemark.value || undefined })
    ElMessage.success(ignore ? '已忽略该质控单' : '已标记为处理完成')
    drawerVisible.value = false
    await Promise.all([loadQcList(), loadOverview()])
  } catch (e: any) {
    ElMessage.error(e?.message || '处理失败')
  } finally {
    handling.value = false
  }
}

// ---------------- 执行质控 ----------------
const runQc = async (row: any) => {
  qcRunning.value = true
  try {
    const res = await executeQualityControl({
      recordSource: candQuery.recordSource,
      recordId: row.recordId,
      qcType: 0,
    })
    lastResult.value = res.data
    ElMessage.success(
      `${row.recordNo}：${res.data?.score ?? '—'} 分 / ${res.data?.gradeText ?? '—'}级 / ${res.data?.qcResultText ?? ''}`,
    )
    await Promise.all([loadQcList(), loadCandidates(), loadOverview(), loadRules()])
  } catch (e: any) {
    ElMessage.error(e?.message || '执行质控失败')
  } finally {
    qcRunning.value = false
  }
}

const runBatch = async () => {
  if (!candSelection.value.length) {
    ElMessage.warning('请先勾选要质控的病历')
    return
  }
  qcRunning.value = true
  try {
    const res = await executeQualityControlBatch({
      recordSource: candQuery.recordSource,
      recordIds: candSelection.value.map((row: any) => row.recordId),
      qcType: 0,
    })
    const results = res.data || []
    // 出参是 VO：结论看 qcResult（1 通过 / 0 不通过），不是引擎内部的 pass
    const failed = results.filter((r: any) => Number(r.qcResult) === 0).length
    lastResult.value = results[0] || null
    ElMessage.success(`已质控 ${results.length} 份，其中 ${failed} 份不通过`)
    candTableRef.value?.clearSelection?.()
    await Promise.all([loadQcList(), loadCandidates(), loadOverview(), loadRules()])
  } catch (e: any) {
    ElMessage.error(e?.message || '批量质控失败')
  } finally {
    qcRunning.value = false
  }
}

const onSelectionChange = (rows: any[]) => {
  candSelection.value = rows
}

const switchSource = (source: string) => {
  candQuery.recordSource = source
  candQuery.pageNum = 1
  candQuery.recordStatus = undefined
  candQuery.recordType = undefined
  candSelection.value = []
  candTableRef.value?.clearSelection?.()
  loadCandidates()
}

// ---------------- 展示辅助 ----------------
const num = (v: any) => Number(v ?? 0)
const scoreText = (v: any) => (v === null || v === undefined ? '—' : String(v))
const rateText = (v: any) => (v === null || v === undefined ? '无样本' : `${Number(v).toFixed(1)}%`)
const gradeTag = (g: any) => (g === '甲' ? 'success' : g === '乙' ? 'warning' : g === '丙' ? 'danger' : 'info')
// 严重度标签色：3-否决（红）2-重要（橙）1-提示（灰蓝）0-无问题（绿）
// 0 必须显式给一档，否则它会落到最后的分支上，被渲染成"提示"色
const severityTag = (s: any) => (s === 3 ? 'danger' : s === 2 ? 'warning' : s === 1 ? 'info' : 'success')
const dimensions = computed(() => overview.value?.dimensionIssues || [])
const emptyRules = computed(() => ruleList.value.filter((r: any) => r.empty))

// ---------------- 三级质控流转（G18） ----------------
// 状态机在后端收口；本页按 flow.flowStatus / currentLevel 只做按钮显隐，
// 不在前端判断「谁有权限」，操作人由服务端记录。
const flowLoading = ref(false)
const flowList = ref<any[]>([])
const flowTotal = ref(0)
const flowQuery = reactive<any>({
  flowStatus: undefined,
  currentLevel: undefined,
  keyword: '',
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
})

const loadFlowList = async () => {
  flowLoading.value = true
  try {
    const params: any = { pageNum: flowQuery.pageNum, pageSize: flowQuery.pageSize }
    if (flowQuery.flowStatus !== undefined && flowQuery.flowStatus !== null) params.flowStatus = flowQuery.flowStatus
    if (flowQuery.currentLevel !== undefined && flowQuery.currentLevel !== null) params.currentLevel = flowQuery.currentLevel
    if (flowQuery.keyword) params.keyword = flowQuery.keyword.trim()
    const res = await getQcFlowListPage(params)
    flowList.value = res.data?.records || []
    flowTotal.value = Number(res.data?.total ?? 0)
  } catch (e: any) {
    ElMessage.error(e?.message || '加载流转单失败')
  } finally {
    flowLoading.value = false
  }
}

// 发起弹窗：从候选病历选一份（复用候选接口，含门诊/住院来源切换）
const startDialog = ref(false)
const startQuery = reactive<any>({ recordSource: 'INPATIENT', keyword: '', pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
const startList = ref<any[]>([])
const startTotal = ref(0)
const startLoading = ref(false)
const startRow = ref<any>(null)

const loadStartCandidates = async () => {
  startLoading.value = true
  try {
    const params: any = {
      recordSource: startQuery.recordSource,
      pageNum: startQuery.pageNum,
      pageSize: startQuery.pageSize,
    }
    if (startQuery.keyword) params.keyword = startQuery.keyword.trim()
    const res = await listQcCandidatePage(params)
    startList.value = res.data?.records || []
    startTotal.value = Number(res.data?.total ?? 0)
  } catch (e: any) {
    ElMessage.error(e?.message || '加载候选病历失败')
  } finally {
    startLoading.value = false
  }
}

const openStartDialog = () => {
  startRow.value = null
  startDialog.value = true
  loadStartCandidates()
}

const submitStart = async () => {
  if (!startRow.value) {
    ElMessage.warning('请先选择一份病历')
    return
  }
  try {
    await startQcFlow({ recordId: startRow.value.recordId })
    ElMessage.success('已发起，进入科级待审')
    startDialog.value = false
    await loadFlowList()
  } catch (e: any) {
    ElMessage.error(e?.message || '发起失败')
  }
}

// 详情抽屉（含时间线 + 分级操作）
const flowDrawer = ref(false)
const flowDetailLoading = ref(false)
const flowDetail = ref<any>(null)
const flowActions = ref<any[]>([])
const flowBusy = ref(false)
const flowOpinion = ref('')
const flowDefect = ref('')
const flowRequirement = ref('')
const flowDeadline = ref('')
const flowGrade = ref<number | undefined>(undefined)
const flowScore = ref<number | undefined>(undefined)

const openFlowDrawer = async (row: any) => {
  flowDrawer.value = true
  flowDetailLoading.value = true
  flowOpinion.value = ''
  flowDefect.value = ''
  flowRequirement.value = ''
  flowDeadline.value = ''
  flowGrade.value = undefined
  flowScore.value = undefined
  try {
    const [d, a] = await Promise.all([getQcFlowDetail(row.id), getQcFlowActions(row.id)])
    flowDetail.value = d.data
    flowActions.value = a.data || []
  } catch (e: any) {
    ElMessage.error(e?.message || '加载流转详情失败')
  } finally {
    flowDetailLoading.value = false
  }
}

const reloadFlow = async () => {
  const id = flowDetail.value?.id
  flowDrawer.value = false
  await loadFlowList()
  if (id) {
    const fresh = flowList.value.find((f: any) => Number(f.id) === Number(id))
    if (fresh) await openFlowDrawer(fresh)
  }
}

const doApprove = async () => {
  flowBusy.value = true
  try {
    await approveQcFlow({ flowId: flowDetail.value.id, opinion: flowOpinion.value || undefined })
    ElMessage.success('审核通过，已送下一级')
    await reloadFlow()
  } catch (e: any) {
    ElMessage.error(e?.message || '审核失败')
  } finally {
    flowBusy.value = false
  }
}

const doReturn = async () => {
  if (!flowDefect.value.trim() || !flowRequirement.value.trim()) {
    ElMessage.warning('缺陷明细与整改要求必填——退回必须写清缺陷')
    return
  }
  flowBusy.value = true
  try {
    await returnQcFlow({
      flowId: flowDetail.value.id,
      defectDetail: flowDefect.value,
      requirement: flowRequirement.value,
      returnDeadline: flowDeadline.value || undefined,
      opinion: flowOpinion.value || undefined,
    })
    ElMessage.success('已退回整改')
    await reloadFlow()
  } catch (e: any) {
    ElMessage.error(e?.message || '退回失败')
  } finally {
    flowBusy.value = false
  }
}

const doResubmit = async () => {
  flowBusy.value = true
  try {
    await resubmitQcFlow({ flowId: flowDetail.value.id, opinion: flowOpinion.value || undefined })
    ElMessage.success('整改已提交')
    await reloadFlow()
  } catch (e: any) {
    ElMessage.error(e?.message || '提交失败')
  } finally {
    flowBusy.value = false
  }
}

const doFinal = async () => {
  if (!flowGrade.value) {
    ElMessage.warning('终审必须定级（甲/乙/丙）')
    return
  }
  flowBusy.value = true
  try {
    await finalQcFlow({
      flowId: flowDetail.value.id,
      grade: flowGrade.value,
      finalScore: flowScore.value ?? undefined,
      finalOpinion: flowOpinion.value || undefined,
    })
    ElMessage.success('终审通过')
    await reloadFlow()
  } catch (e: any) {
    ElMessage.error(e?.message || '终审失败')
  } finally {
    flowBusy.value = false
  }
}

onMounted(async () => {
  await loadDicts()
  await refreshAll()
  await loadFlowList()
})
</script>

<template>
  <div class="space-y-6">
    <!-- 标题 -->
    <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div>
        <h1 class="text-xl font-semibold text-slate-900">病案质控工作台</h1>
        <p class="mt-1 max-w-4xl text-sm text-slate-500">
          按
          <el-text class="mx-1" size="small">完整性 / 规范性 / 逻辑性</el-text>
          三个维度对门诊病历与住院文书做规则质控，每条问题都定位到具体字段并给出整改建议。
        </p>
      </div>
      <el-button :icon="Refresh" @click="refreshAll">刷新</el-button>
    </div>

    <!-- 口径说明 -->
    <div class="rounded-lg border border-blue-100 bg-blue-50 px-4 py-3 text-xs leading-6 text-slate-700">
      <p><span class="font-medium text-slate-900">得分与结论是两件事：</span>得分是 100 分制扣分（甲级≥90 / 乙级75~89 / 丙级&lt;75），结论只是「通过 / 不通过」。命中任何<b>重要</b>及以上的问题即不通过，所以「80 分但不通过」不是矛盾。</p>
      <p><span class="font-medium text-slate-900">有否决项命中必定判为丙级：</span>缺主诉 / 缺现病史 / 缺诊断 / 缺过敏史 / 记录类文书无正文 / 性别与诊断矛盾 / 生命体征越界 —— 这些是一票否决项。</p>
      <p><span class="font-medium text-slate-900">必填项规则按文书类型限定：</span>只有入院记录与门诊病历要求主诉、现病史、过敏史；手术记录、会诊记录、转科记录、输血记录<b>本来就不需要</b>，不会被误报。</p>
      <p><span class="font-medium text-slate-900">平均分与甲级率只统计「综合质控」（qc_type=0）：</span>完整性 / 规范性 / 逻辑性单独出单时，分数只代表那一个维度（缺主诉属完整性，单独跑逻辑性就看不到），混在一起会把甲级率抬高。单量类数字（总数 / 待处理 / 通过 / 不通过）统计全部质控单。</p>
      <p><span class="font-medium text-slate-900">得分显示为「—」表示该单没有得分</span>（旧版质控，或 qc_type=4 的 AI 内涵质控），不计入平均分与甲级率，不要当成满分通过。</p>
    </div>

    <!-- 概览 -->
    <div v-loading="overviewLoading" v-if="overview" data-testid="p5-qc-overview" class="grid grid-cols-2 gap-4 lg:grid-cols-5">
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">质控单总数</p>
        <p class="mt-1 text-2xl font-semibold text-slate-900" data-testid="p5-qc-stat-total">{{ overview.total }}</p>
        <p class="mt-1 text-xs text-slate-400">待处理 {{ overview.pendingCount }} 份</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">不通过</p>
        <p class="mt-1 text-2xl font-semibold text-red-600" data-testid="p5-qc-stat-failed">{{ overview.failedCount }}</p>
        <p class="mt-1 text-xs text-slate-400">通过 {{ overview.passedCount }} 份</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">平均分</p>
        <p class="mt-1 text-2xl font-semibold text-slate-900" data-testid="p5-qc-stat-avg">{{ overview.avgScore === null ? '无样本' : overview.avgScore }}</p>
        <p class="mt-1 text-xs text-slate-400">
          综合质控已评分 {{ overview.scoredCount }} 份 · 无得分的单 {{ overview.unscoredCount }} 份（旧版 / AI 内涵质控，不计入）
        </p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">甲级率</p>
        <p class="mt-1 text-2xl font-semibold text-emerald-600" data-testid="p5-qc-stat-gradea">{{ rateText(overview.gradeARate) }}</p>
        <p class="mt-1 text-xs text-slate-400">
          甲 {{ overview.gradeACount }} · 乙 {{ overview.gradeBCount }} · 丙 {{ overview.gradeCCount }}（综合质控）
        </p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">问题明细</p>
        <p class="mt-1 text-2xl font-semibold text-amber-600" data-testid="p5-qc-stat-issues">{{ overview.issueCount }}</p>
        <p class="mt-1 text-xs text-slate-400">涉及 {{ overview.issueRecordCount }} 份病历</p>
      </div>
    </div>

    <!-- 维度分布 -->
    <div v-if="overview" class="grid grid-cols-1 gap-4 sm:grid-cols-3">
      <div
        v-for="d in dimensions"
        :key="d.dimension"
        :data-testid="`p5-qc-dim-${d.dimension}`"
        class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
      >
        <div class="flex items-center justify-between">
          <span class="text-sm font-medium text-slate-900">{{ d.dimensionText }}</span>
          <span class="rounded px-1.5 py-0.5 text-xs" :style="{ background: `${dimColor[d.dimension]}1A`, color: dimColor[d.dimension] }">
            {{ num(d.issueCount) }} 条
          </span>
        </div>
        <p class="mt-2 text-2xl font-semibold" :style="{ color: dimColor[d.dimension] }">{{ num(d.deductTotal) }}</p>
        <p class="mt-1 text-xs text-slate-400">累计扣分</p>
      </div>
    </div>

    <el-tabs v-model="activeTab">
      <!-- ---------------- 质控单 ---------------- -->
      <el-tab-pane label="质控单" name="qc">
        <div class="rounded-lg border border-slate-200 bg-white shadow-sm">
          <div class="flex flex-wrap items-center gap-2 border-b border-slate-100 px-4 py-3">
            <el-select v-model="qcQuery.recordSource" placeholder="来源" clearable style="width: 130px" data-testid="p5-qc-f-source" @change="() => { qcQuery.pageNum = 1; loadQcList() }">
              <el-option label="门诊病历" value="OUTPATIENT" />
              <el-option label="住院文书" value="INPATIENT" />
            </el-select>
            <el-select v-model="qcQuery.qcType" placeholder="质控类型" clearable style="width: 150px" data-testid="p5-qc-f-type" @change="() => { qcQuery.pageNum = 1; loadQcList() }">
              <el-option v-for="o in qcTypeOptions" :key="o.code" :label="o.text" :value="o.code" />
            </el-select>
            <el-select v-model="qcQuery.qcResult" placeholder="结论" clearable style="width: 110px" data-testid="p5-qc-f-result" @change="() => { qcQuery.pageNum = 1; loadQcList() }">
              <el-option label="通过" :value="1" />
              <el-option label="不通过" :value="0" />
            </el-select>
            <el-select v-model="qcQuery.qcStatus" placeholder="处理状态" clearable style="width: 120px" data-testid="p5-qc-f-status" @change="() => { qcQuery.pageNum = 1; loadQcList() }">
              <el-option label="待处理" :value="1" />
              <el-option label="已处理" :value="2" />
              <el-option label="已忽略" :value="3" />
            </el-select>
            <el-input v-model="qcQuery.keyword" placeholder="质控单号 / 病历号 / 患者姓名" clearable style="width: 240px" data-testid="p5-qc-f-keyword" @keyup.enter="() => { qcQuery.pageNum = 1; loadQcList() }">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button type="primary" @click="() => { qcQuery.pageNum = 1; loadQcList() }">查询</el-button>
            <el-button @click="() => { qcQuery.recordSource = ''; qcQuery.qcType = undefined; qcQuery.qcResult = undefined; qcQuery.qcStatus = undefined; qcQuery.keyword = ''; qcQuery.pageNum = 1; loadQcList() }">清除</el-button>
          </div>

          <div v-loading="qcLoading" class="p-4">
            <el-table :data="qcList" size="small" border data-testid="p5-qc-table" @row-click="openDetail">
              <el-table-column label="质控单号" width="170">
                <template #default="{ row }">
                  <span class="text-xs font-medium text-slate-700">{{ row.qcNo }}</span>
                </template>
              </el-table-column>
              <el-table-column label="来源" width="90">
                <template #default="{ row }">
                  <div class="text-xs">{{ row.recordSourceText || '—' }}</div>
                  <div class="text-xs text-slate-400">{{ row.recordTypeText || '' }}</div>
                </template>
              </el-table-column>
              <el-table-column label="病历 / 患者" width="180">
                <template #default="{ row }">
                  <div class="text-xs">{{ row.recordNo || '—' }}</div>
                  <div class="text-xs text-slate-400">{{ row.patientName || '—' }} <span v-if="row.deptName">· {{ row.deptName }}</span></div>
                </template>
              </el-table-column>
              <el-table-column label="类型" width="110">
                <template #default="{ row }">
                  <span class="text-xs">{{ row.qcTypeText }}</span>
                </template>
              </el-table-column>
              <el-table-column label="得分" width="80">
                <template #default="{ row }">
                  <span class="text-sm font-semibold" data-testid="p5-qc-row-score">{{ scoreText(row.score) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="等级" width="70">
                <template #default="{ row }">
                  <el-tag v-if="row.gradeText" size="small" :type="gradeTag(row.gradeText)">{{ row.gradeText }}</el-tag>
                  <span v-else class="text-xs text-slate-400">—</span>
                </template>
              </el-table-column>
              <el-table-column label="结论" width="90">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.qcResult === 1 ? 'success' : 'danger'">{{ row.qcResultText }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="问题数" width="80">
                <template #default="{ row }">
                  <span class="text-xs" data-testid="p5-qc-row-issuecount">{{ num(row.errorCount) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="最高严重度" width="100">
                <template #default="{ row }">
                  <!-- 0 = 无问题，是引擎的取值（不是"没质控"）。用 v-if="row.severityMax" 会因为 0 在 JS 里是 falsy
                       而整格渲染成「—」，满分的病历看起来像数据缺失。 -->
                  <el-tag v-if="row.severityMax !== null && row.severityMax !== undefined" size="small" :type="severityTag(row.severityMax)">{{ row.severityMaxText }}</el-tag>
                  <span v-else class="text-xs text-slate-400">—</span>
                </template>
              </el-table-column>
              <el-table-column label="处理状态" width="90">
                <template #default="{ row }">
                  <span class="text-xs">{{ row.qcStatusText }}</span>
                </template>
              </el-table-column>
              <el-table-column label="质控人 / 时间" width="150">
                <template #default="{ row }">
                  <div class="text-xs">{{ row.qcBy || '—' }}</div>
                  <div class="text-xs text-slate-400">{{ row.qcTime || '—' }}</div>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" size="small" :data-testid="`p5-qc-open-${row.qcNo}`" @click.stop="openDetail(row)">明细</el-button>
                </template>
              </el-table-column>
              <template #empty>
                <div class="py-6 text-sm text-slate-400">暂无质控单，可到「待质控病历」执行质控</div>
              </template>
            </el-table>

            <div class="mt-3 flex items-center justify-between">
              <p class="text-xs text-slate-500" data-testid="p5-qc-total">共 {{ qcTotal }} 张质控单</p>
              <el-pagination
                v-model:current-page="qcQuery.pageNum"
                v-model:page-size="qcQuery.pageSize"
                :total="qcTotal"
                :page-sizes="PAGE_SIZES"
                layout="sizes, prev, pager, next"
                @current-change="loadQcList"
                @size-change="() => { qcQuery.pageNum = 1; loadQcList() }"
              />
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- ---------------- 三级质控流转（G18） ---------------- -->
      <el-tab-pane label="三级质控流转" name="flow">
        <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div class="flex items-center gap-2">
            <el-select v-model="flowQuery.flowStatus" placeholder="流转状态" clearable style="width: 140px" data-testid="g18-f-status" @change="() => { flowQuery.pageNum = 1; loadFlowList() }">
              <el-option v-for="(t, v) in { 1: '科级待审', 2: '病案室待审', 3: '医务处待审', 4: '终审通过', 5: '整改中' }" :key="v" :label="t" :value="Number(v)" />
            </el-select>
            <el-select v-model="flowQuery.currentLevel" placeholder="当前级" clearable style="width: 120px" data-testid="g18-f-level" @change="() => { flowQuery.pageNum = 1; loadFlowList() }">
              <el-option label="科级" :value="1" />
              <el-option label="病案室" :value="2" />
              <el-option label="医务处" :value="3" />
            </el-select>
            <el-input v-model="flowQuery.keyword" placeholder="流转单号 / 患者姓名 / 科室" clearable style="width: 240px" data-testid="g18-f-keyword" @keyup.enter="() => { flowQuery.pageNum = 1; loadFlowList() }">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button class="!ml-auto" type="primary" data-testid="g18-open-start" v-perm="'qc:recordQc:add'" @click="openStartDialog">发起流转</el-button>
            <el-button :icon="Refresh" circle @click="loadFlowList" />
          </div>
          <p class="mt-2 text-xs text-slate-400">流转口径：科级初审 → 病案室复审 → 医务处终审（定级）。任一级可退回科室整改，整改提交后回到退回级；同一病历同时只允许一条在途。</p>
          <el-table :data="flowList" v-loading="flowLoading" class="mt-3" data-testid="g18-flow-table" size="default">
            <el-table-column label="流转单号" prop="flowNo" width="150" />
            <el-table-column label="患者" prop="patientName" width="100" />
            <el-table-column label="科室" prop="deptName" min-width="130" />
            <el-table-column label="来源" width="80">
              <template #default="{ row }">{{ row.recordSourceText }}</template>
            </el-table-column>
            <el-table-column label="流转状态" width="110">
              <template #default="{ row }">
                <el-tag :type="flowStatusTag(row.flowStatus)" size="small" :data-testid="`g18-status-${row.id}`">{{ flowStatusText(row.flowStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="当前级" width="90">
              <template #default="{ row }">{{ row.currentLevelText }}</template>
            </el-table-column>
            <el-table-column label="最近退回缺陷" min-width="180">
              <template #default="{ row }">
                <span v-if="row.returnReason" class="text-xs text-amber-600">{{ row.returnReason }}</span>
                <span v-else class="text-xs text-slate-300">—</span>
              </template>
            </el-table-column>
            <el-table-column label="终审定级" width="90">
              <template #default="{ row }">{{ flowGradeText(row.grade) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="90" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="primary" link :data-testid="`g18-detail-${row.id}`" @click="openFlowDrawer(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination class="mt-3 justify-end" layout="total, prev, pager, next" :total="flowTotal" :page-size="flowQuery.pageSize" v-model:current-page="flowQuery.pageNum" @current-change="loadFlowList" />
        </div>
      </el-tab-pane>

      <!-- ---------------- 待质控病历 ---------------- -->
      <el-tab-pane label="待质控病历" name="candidate">
        <div class="rounded-lg border border-slate-200 bg-white shadow-sm">
          <div class="flex flex-wrap items-center gap-2 border-b border-slate-100 px-4 py-3">
            <el-radio-group v-model="candQuery.recordSource" data-testid="p5-qc-cand-source" @change="switchSource(candQuery.recordSource)">
              <el-radio-button label="INPATIENT">住院文书</el-radio-button>
              <el-radio-button label="OUTPATIENT">门诊病历</el-radio-button>
            </el-radio-group>
            <el-select v-if="candQuery.recordSource === 'INPATIENT'" v-model="candQuery.recordType" placeholder="文书类型" clearable style="width: 140px" data-testid="p5-qc-cand-recordtype" @change="() => { candQuery.pageNum = 1; loadCandidates() }">
              <el-option v-for="o in recordTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
            <el-select v-model="candQuery.recordStatus" placeholder="病历状态" clearable style="width: 120px" data-testid="p5-qc-cand-status" @change="() => { candQuery.pageNum = 1; loadCandidates() }">
              <el-option v-for="o in recordStatusOptions" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
            <el-checkbox v-model="candQuery.onlyUnQced" data-testid="p5-qc-cand-onlyunqced" @change="() => { candQuery.pageNum = 1; loadCandidates() }">只看未质控</el-checkbox>
            <el-input v-model="candQuery.keyword" placeholder="病历号 / 患者姓名" clearable style="width: 200px" data-testid="p5-qc-cand-keyword" @keyup.enter="() => { candQuery.pageNum = 1; loadCandidates() }">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button type="primary" @click="() => { candQuery.pageNum = 1; loadCandidates() }">查询</el-button>
            <el-button type="warning" :loading="qcRunning" data-testid="p5-qc-batch" v-perm="'qc:recordQc:add'" @click="runBatch">
              批量质控（{{ candSelection.length }}）
            </el-button>
          </div>

          <div v-loading="candLoading" class="p-4">
            <el-table ref="candTableRef" :data="candList" size="small" border data-testid="p5-qc-cand-table" @selection-change="onSelectionChange">
              <el-table-column type="selection" width="45" />
              <el-table-column label="病历号" width="160">
                <template #default="{ row }">
                  <div class="text-xs font-medium">{{ row.recordNo }}</div>
                  <div class="text-xs text-slate-400">{{ row.recordTypeText || row.recordSourceText }}</div>
                </template>
              </el-table-column>
              <el-table-column label="患者" width="140">
                <template #default="{ row }">
                  <div class="text-xs">{{ row.patientName || '—' }}</div>
                  <div class="text-xs text-slate-400">{{ row.genderText || '—' }} · {{ row.age ?? '—' }}岁</div>
                </template>
              </el-table-column>
              <el-table-column label="科室 / 医师" width="140">
                <template #default="{ row }">
                  <div class="text-xs">{{ row.deptName || '—' }}</div>
                  <div class="text-xs text-slate-400">{{ row.doctorName || '—' }}</div>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="80">
                <template #default="{ row }">
                  <span class="text-xs">{{ row.recordStatusText || '—' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="记录时间" width="150">
                <template #default="{ row }">
                  <span class="text-xs text-slate-500">{{ row.recordTime || '—' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="最近质控" width="220">
                <template #default="{ row }">
                  <template v-if="row.qced">
                    <span class="text-xs" :data-testid="`p5-qc-cand-lastqc-${row.recordNo}`">
                      {{ scoreText(row.lastScore) }} 分 / {{ row.lastGrade || '—' }}级 / {{ row.lastResult === 1 ? '通过' : '不通过' }}
                    </span>
                    <div class="text-xs text-slate-400">{{ row.lastQcTime || '' }}</div>
                  </template>
                  <span v-else class="text-xs text-slate-400" :data-testid="`p5-qc-cand-unqced-${row.recordNo}`">从未质控</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" size="small" :loading="qcRunning" :data-testid="`p5-qc-run-${row.recordNo}`" v-perm="'qc:recordQc:add'" @click.stop="runQc(row)">执行质控</el-button>
                </template>
              </el-table-column>
              <template #empty>
                <div class="py-6 text-sm text-slate-400">没有符合条件的病历</div>
              </template>
            </el-table>

            <div class="mt-3 flex items-center justify-between">
              <p class="text-xs text-slate-500" data-testid="p5-qc-cand-total">共 {{ candTotal }} 份</p>
              <el-pagination
                v-model:current-page="candQuery.pageNum"
                v-model:page-size="candQuery.pageSize"
                :total="candTotal"
                :page-sizes="PAGE_SIZES"
                layout="sizes, prev, pager, next"
                @current-change="loadCandidates"
                @size-change="() => { candQuery.pageNum = 1; loadCandidates() }"
              />
            </div>
          </div>
        </div>

        <!-- 最近一次执行结果 -->
        <div v-if="lastResult" data-testid="p5-qc-lastresult" class="mt-4 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div class="flex items-center justify-between">
            <h2 class="text-sm font-semibold text-slate-900">最近一次质控结论</h2>
            <span class="text-xs text-slate-500">{{ lastResult.qcNo }}</span>
          </div>
          <!-- 执行接口返回的是**落库后回读的质控单 VO**，不是引擎内部的 QcResult。
               所以这里只能读 VO 字段（qcResult / errorCount / severityMax），
               读 pass / vetoCount / summary 会取到 undefined ——
               `!undefined` 为 true，满分级别的病历会被显示成「不通过」，是最容易被放过的假 bug。 -->
          <div class="mt-2 flex flex-wrap items-center gap-4 text-sm">
            <span>病历：<b>{{ lastResult.recordNo || '—' }}</b>（{{ lastResult.recordSourceText }}{{ lastResult.recordTypeText ? ' · ' + lastResult.recordTypeText : '' }}）</span>
            <span>患者：<b>{{ lastResult.patientName || '—' }}</b></span>
            <span>得分：<b data-testid="p5-qc-last-score">{{ scoreText(lastResult.score) }}</b></span>
            <span>等级：<b>{{ lastResult.gradeText || '—' }}</b></span>
            <span>结论：<b :data-testid="'p5-qc-last-result'" :class="lastResult.qcResult === 1 ? 'text-emerald-600' : 'text-red-600'">{{ lastResult.qcResultText }}</b></span>
            <span>问题：<b data-testid="p5-qc-last-issuecount">{{ num(lastResult.errorCount) }}</b> 条</span>
            <span>最高严重度：<b>{{ lastResult.severityMaxText || '—' }}</b></span>
          </div>
          <p class="mt-2 text-xs text-slate-500" data-testid="p5-qc-last-summary">
            {{ num(lastResult.errorCount) ? lastResult.errorDetail : '未发现问题，得分 ' + scoreText(lastResult.score) + ' 分' }}
          </p>
          <el-table :data="lastResult.issues || []" size="small" border class="mt-3" data-testid="p5-qc-last-issues">
            <el-table-column label="维度" width="80">
              <template #default="{ row }">
                <span class="text-xs" :style="{ color: dimColor[row.dimension] }">{{ row.dimensionText }}</span>
              </template>
            </el-table-column>
            <el-table-column label="严重度" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="severityTag(row.severity)">{{ row.severityText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="规则" width="90">
              <template #default="{ row }">
                <span class="text-xs">{{ row.ruleCode }}</span>
              </template>
            </el-table-column>
            <el-table-column label="字段" width="90">
              <template #default="{ row }">
                <span class="text-xs">{{ row.fieldName }}</span>
              </template>
            </el-table-column>
            <el-table-column label="问题" min-width="260">
              <template #default="{ row }">
                <div class="text-xs leading-5">{{ row.errorDetail }}</div>
              </template>
            </el-table-column>
            <el-table-column label="原文证据" min-width="160">
              <template #default="{ row }">
                <span class="text-xs text-slate-500">{{ row.evidence }}</span>
              </template>
            </el-table-column>
            <el-table-column label="扣分" width="70">
              <template #default="{ row }">
                <span class="text-xs">-{{ row.deduct }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- ---------------- 规则清单 ---------------- -->
      <el-tab-pane label="规则清单" name="rules">
        <div v-if="emptyRules.length" data-testid="p5-qc-empty-alert" class="mb-4 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-xs text-amber-800">
          <div class="flex items-start gap-2">
            <el-icon class="mt-0.5"><WarningFilled /></el-icon>
            <div>
              <p class="font-medium">有 {{ emptyRules.length }} 条规则至今一次都没命中过：</p>
              <p class="mt-1 leading-6">
                <span v-for="r in emptyRules" :key="r.ruleCode" class="mr-2 inline-block">{{ r.ruleName }}（{{ r.ruleCode }}）</span>
              </p>
              <p class="mt-1 text-amber-700">0 命中是观测值，不等于"这条规则没问题"，也不等于"这条规则一定生效了"。</p>
            </div>
          </div>
        </div>

        <div class="rounded-lg border border-slate-200 bg-white shadow-sm">
          <div v-loading="ruleLoading" class="p-4">
            <el-table :data="ruleList" size="small" border data-testid="p5-qc-rules">
              <el-table-column label="编码" width="70">
                <template #default="{ row }">
                  <span class="text-xs font-medium">{{ row.ruleCode }}</span>
                </template>
              </el-table-column>
              <el-table-column label="规则" min-width="200">
                <template #default="{ row }">
                  <div class="text-xs">{{ row.ruleName }}</div>
                  <div class="text-xs text-slate-400">{{ row.basis }}</div>
                </template>
              </el-table-column>
              <el-table-column label="维度" width="80">
                <template #default="{ row }">
                  <span class="text-xs" :style="{ color: dimColor[row.dimension] }">{{ row.dimensionText }}</span>
                </template>
              </el-table-column>
              <el-table-column label="严重度" width="80">
                <template #default="{ row }">
                  <el-tag size="small" :type="severityTag(row.severity)">{{ row.severityText }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="适用范围" width="180">
                <template #default="{ row }">
                  <span class="text-xs text-slate-500">{{ row.scopeText }}</span>
                </template>
              </el-table-column>
              <el-table-column label="单条扣分" width="80">
                <template #default="{ row }">
                  <span class="text-xs">{{ row.deduct }}</span>
                </template>
              </el-table-column>
              <el-table-column label="命中次数" width="110">
                <template #default="{ row }">
                  <el-tag v-if="row.empty" size="small" type="warning" :data-testid="`p5-qc-rule-empty-${row.ruleCode}`">0 命中</el-tag>
                  <span v-else class="text-xs font-medium text-red-600" :data-testid="`p5-qc-rule-hit-${row.ruleCode}`">{{ num(row.hitCount) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="涉及质控单" width="100">
                <template #default="{ row }">
                  <span class="text-xs">{{ num(row.qcCount) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="累计扣分" width="90">
                <template #default="{ row }">
                  <span class="text-xs">{{ num(row.deductTotal) }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 质控单明细抽屉 -->
    <el-drawer v-model="drawerVisible" title="质控单明细" size="62%">
      <div v-loading="detailLoading">
        <template v-if="detail">
          <div class="flex flex-wrap items-center gap-4 text-sm">
            <span>质控单：<b>{{ detail.qcNo }}</b></span>
            <span>病历：<b>{{ detail.recordNo || '—' }}</b>（{{ detail.recordSourceText }}{{ detail.recordTypeText ? ' · ' + detail.recordTypeText : '' }}）</span>
            <span>患者：<b>{{ detail.patientName || '—' }}</b></span>
            <span data-testid="p5-qc-detail-score">得分：<b>{{ scoreText(detail.score) }}</b></span>
            <span>等级：<b>{{ detail.gradeText || '—' }}</b></span>
            <span :class="detail.qcResult === 1 ? 'text-emerald-600' : 'text-red-600'">
              结论：<b>{{ detail.qcResultText }}</b>
            </span>
            <span data-testid="p5-qc-detail-issuecount">问题：<b>{{ num(detail.errorCount) }}</b> 条</span>
          </div>
          <p class="mt-2 text-xs text-slate-500">{{ detail.qcContent }} · 质控人 {{ detail.qcBy || '—' }} · {{ detail.qcTime || '—' }}</p>

          <!-- 明细表为空但 error_count>0：这类单子的问题只存在 error_detail 文本里
               （旧版质控，以及 qc_type=4 的 AI 内涵质控 —— AI 的发现是自由文本，没有结构化明细）。
               不兜底的话，抽屉会显示「问题：6 条」配一张空表，看起来像数据丢了。 -->
          <div
            v-if="(detail.issues || []).length === 0 && num(detail.errorCount) > 0"
            data-testid="p5-qc-detail-textdetail"
            class="mt-4 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-xs leading-6 text-amber-900"
          >
            <p class="font-medium">这张质控单的 {{ num(detail.errorCount) }} 条问题没有结构化明细（{{ detail.qcTypeText }}），以下为质控原文：</p>
            <p class="mt-1 whitespace-pre-wrap">{{ detail.errorDetail }}</p>
          </div>

          <el-table :data="detail.issues || []" size="small" border class="mt-4" data-testid="p5-qc-detail-issues">
            <el-table-column label="维度" width="80">
              <template #default="{ row }">
                <span class="text-xs" :style="{ color: dimColor[row.dimension] }">{{ row.dimensionText }}</span>
              </template>
            </el-table-column>
            <el-table-column label="严重度" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="severityTag(row.severity)">{{ row.severityText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="规则" width="150">
              <template #default="{ row }">
                <div class="text-xs">{{ row.ruleName }}</div>
                <div class="text-xs text-slate-400">{{ row.ruleCode }}</div>
              </template>
            </el-table-column>
            <el-table-column label="字段" width="90">
              <template #default="{ row }">
                <span class="text-xs">{{ row.fieldName }}</span>
              </template>
            </el-table-column>
            <el-table-column label="问题" min-width="240">
              <template #default="{ row }">
                <div class="text-xs leading-5">{{ row.errorDetail }}</div>
              </template>
            </el-table-column>
            <el-table-column label="整改建议" min-width="200">
              <template #default="{ row }">
                <div class="text-xs leading-5 text-slate-500">{{ row.suggestion }}</div>
              </template>
            </el-table-column>
            <el-table-column label="原文证据" min-width="140">
              <template #default="{ row }">
                <span class="text-xs text-slate-500">{{ row.evidence }}</span>
              </template>
            </el-table-column>
            <el-table-column label="扣分" width="70">
              <template #default="{ row }">
                <span class="text-xs">-{{ row.deduct }}</span>
              </template>
            </el-table-column>
            <template #empty>
              <div class="py-4 text-sm text-slate-400">这张质控单没有问题明细</div>
            </template>
          </el-table>

          <div v-if="detail.qcStatus === 1" class="mt-4 rounded-lg border border-slate-200 bg-slate-50 p-4">
            <p class="text-xs font-medium text-slate-700">处理这张质控单</p>
            <el-input v-model="handleRemark" class="mt-2" placeholder="处理备注（可空）" data-testid="p5-qc-handle-remark" />
            <div class="mt-3 flex gap-2">
              <el-button type="primary" size="small" :loading="handling" data-testid="p5-qc-handle-done" v-perm="'qc:recordQc:edit'" @click="submitHandle(false)">整改完成</el-button>
              <el-button size="small" :loading="handling" data-testid="p5-qc-handle-ignore" v-perm="'qc:recordQc:edit'" @click="submitHandle(true)">忽略</el-button>
            </div>
          </div>
          <div v-else class="mt-4 rounded-lg border border-slate-200 bg-slate-50 p-3 text-xs text-slate-500">
            当前状态：{{ detail.qcStatusText }}<span v-if="detail.remark"> · 备注：{{ detail.remark }}</span>
          </div>
        </template>
      </div>
    </el-drawer>

    <!-- ============== 发起三级质控流转弹窗 ============== -->
    <el-dialog v-model="startDialog" title="发起三级质控流转" width="780px" data-testid="g18-start-dialog">
      <div class="flex items-center gap-2">
        <el-radio-group v-model="startQuery.recordSource" @change="() => { startQuery.pageNum = 1; loadStartCandidates() }">
          <el-radio-button value="INPATIENT">住院病历</el-radio-button>
          <el-radio-button value="OUTPATIENT">门诊病历</el-radio-button>
        </el-radio-group>
        <el-input v-model="startQuery.keyword" placeholder="病历号 / 患者姓名" clearable style="width: 220px" data-testid="g18-start-keyword" @keyup.enter="() => { startQuery.pageNum = 1; loadStartCandidates() }" />
      </div>
      <el-table :data="startList" v-loading="startLoading" class="mt-3" highlight-current-row data-testid="g18-start-table" @current-change="(row: any) => (startRow = row)">
        <el-table-column label="病历号" prop="recordNo" width="140" />
        <el-table-column label="患者" prop="patientName" width="100" />
        <el-table-column label="科室" prop="deptName" min-width="130" />
        <el-table-column label="诊断" prop="diagnosis" min-width="160" show-overflow-tooltip />
      </el-table>
      <el-pagination class="mt-2 justify-end" layout="total, prev, pager, next" :total="startTotal" :page-size="startQuery.pageSize" v-model:current-page="startQuery.pageNum" @current-change="loadStartCandidates" />
      <p class="mt-2 text-xs text-slate-400">发起后进入科级待审；同一病历同时只允许一条在途流转。</p>
      <template #footer>
        <el-button @click="startDialog = false">取消</el-button>
        <el-button type="primary" :disabled="!startRow" data-testid="g18-start-confirm" v-perm="'qc:recordQc:add'" @click="submitStart">发起流转</el-button>
      </template>
    </el-dialog>

    <!-- ============== 三级质控流转详情抽屉（时间线 + 分级操作） ============== -->
    <el-drawer v-model="flowDrawer" title="三级质控流转详情" size="560px" data-testid="g18-drawer">
      <div v-loading="flowDetailLoading">
        <template v-if="flowDetail">
          <div class="grid grid-cols-2 gap-2 text-sm">
            <div><span class="text-slate-400">流转单号：</span>{{ flowDetail.flowNo }}</div>
            <div><span class="text-slate-400">状态：</span>
              <el-tag :type="flowStatusTag(flowDetail.flowStatus)" size="small" data-testid="g18-drawer-status">{{ flowStatusText(flowDetail.flowStatus) }}</el-tag>
            </div>
            <div><span class="text-slate-400">患者：</span>{{ flowDetail.patientName }}</div>
            <div><span class="text-slate-400">科室：</span>{{ flowDetail.deptName || '—' }}</div>
            <div><span class="text-slate-400">当前级：</span>{{ flowDetail.currentLevelText }}</div>
            <div><span class="text-slate-400">终审定级：</span>{{ flowGradeText(flowDetail.grade) }}<span v-if="flowDetail.finalScore !== null && flowDetail.finalScore !== undefined">（{{ flowDetail.finalScore }} 分）</span></div>
          </div>
          <div v-if="flowDetail.flowStatus === 5" class="mt-3 rounded-lg border border-amber-200 bg-amber-50 p-3 text-xs text-amber-700" data-testid="g18-rework-box">
            <p class="font-medium">整改中（{{ flowDetail.returnLevelText }}退回）</p>
            <p class="mt-1">缺陷：{{ flowDetail.returnReason }}</p>
            <p class="mt-1">要求：{{ flowDetail.returnRequirement }}<span v-if="flowDetail.returnDeadline">（期限 {{ flowDetail.returnDeadline }}）</span></p>
          </div>

          <!-- 分级操作：按钮显隐按 flowStatus / currentLevel，状态判断在后端 -->
          <div class="mt-4 rounded-lg border border-slate-200 bg-slate-50 p-3">
            <el-input v-model="flowOpinion" placeholder="审核 / 整改意见（可空）" data-testid="g18-opinion" />
            <template v-if="flowDetail.flowStatus === 1 || flowDetail.flowStatus === 2">
              <div class="mt-2 flex gap-2">
                <el-button type="primary" size="small" :loading="flowBusy" data-testid="g18-approve" v-perm="'qc:recordQc:edit'" @click="doApprove">通过，送{{ flowDetail.currentLevel === 1 ? '病案室' : '医务处' }}</el-button>
              </div>
              <el-input v-model="flowDefect" class="mt-2" type="textarea" :rows="2" placeholder="缺陷明细（退回必填）" data-testid="g18-defect" />
              <el-input v-model="flowRequirement" class="mt-2" placeholder="整改要求（退回必填）" data-testid="g18-requirement" />
              <div class="mt-2 flex items-center gap-2">
                <el-date-picker v-model="flowDeadline" type="date" value-format="YYYY-MM-DD" placeholder="整改期限（可空）" style="width: 180px" />
                <el-button type="warning" size="small" :loading="flowBusy" data-testid="g18-return" v-perm="'qc:recordQc:edit'" @click="doReturn">退回整改</el-button>
              </div>
            </template>
            <template v-else-if="flowDetail.flowStatus === 5">
              <el-button class="mt-2" type="primary" size="small" :loading="flowBusy" data-testid="g18-resubmit" v-perm="'qc:recordQc:edit'" @click="doResubmit">整改完成，提交{{ flowDetail.currentLevelText }}</el-button>
            </template>
            <template v-else-if="flowDetail.flowStatus === 3">
              <div class="mt-2 flex items-center gap-2">
                <el-select v-model="flowGrade" placeholder="定级（必填）" style="width: 130px" data-testid="g18-grade">
                  <el-option label="甲级" :value="1" />
                  <el-option label="乙级" :value="2" />
                  <el-option label="丙级" :value="3" />
                </el-select>
                <el-input-number v-model="flowScore" :min="0" :max="100" placeholder="评分" style="width: 120px" />
              </div>
              <el-button class="mt-2" type="primary" size="small" :loading="flowBusy" data-testid="g18-final" v-perm="'qc:recordQc:edit'" @click="doFinal">终审通过（终态）</el-button>
            </template>
            <div v-else-if="flowDetail.flowStatus === 4" class="mt-2 text-xs text-slate-500">
              已终审通过（{{ flowGradeText(flowDetail.grade) }}）——终态，不可再操作。
            </div>
          </div>

          <!-- 时间线 -->
          <p class="mt-4 mb-2 text-sm font-medium text-slate-700">流转时间线</p>
          <el-timeline>
            <el-timeline-item v-for="a in flowActions" :key="a.id" :timestamp="a.actionTime" placement="top">
              <p class="text-sm"><span class="font-medium text-slate-700">{{ a.actionText }}</span><span class="ml-1 text-xs text-slate-400">{{ a.levelText }} · {{ a.operatorName }}</span></p>
              <p v-if="a.opinion" class="text-xs text-slate-500">{{ a.opinion }}</p>
              <p v-if="a.defectDetail" class="text-xs text-amber-600">缺陷：{{ a.defectDetail }}</p>
              <p v-if="a.requirement" class="text-xs text-amber-600">要求：{{ a.requirement }}</p>
            </el-timeline-item>
          </el-timeline>
        </template>
      </div>
    </el-drawer>
  </div>
</template>
