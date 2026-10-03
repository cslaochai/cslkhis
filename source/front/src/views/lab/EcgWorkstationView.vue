<script setup lang="ts">
/**
 * 心电工作站（菜单 417，sql/173）
 *
 * 超声/内镜/放射/病理都有独立工作站，本页补齐心电：
 *   签到 → 波形采集（12 导联，技师岗）→ 测量参数 / Holter 分析 → 报告书写（医师岗）→ 审核 → 发布。
 * 波形是报告的前置闸门（没有波形提交不了报告），Holter 还要再多一道分析闸门，
 * 这些闸门都在服务端 EcgServiceImpl 里，前端只负责把流程走通、把波形画出来。
 */
import {ref, computed, onMounted} from 'vue'
import {Search, EditPen, Check, Close, Promotion, Document, VideoCamera} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  getEcgListPage, getEcgDetailByRecordId, ecgCheckIn, ecgSimulateWave,
  ecgSaveMeasure, ecgSaveHolter, ecgSaveDraft, ecgSubmit, ecgAudit, ecgReject, ecgPublish,
  getEcgTemplateSelectList
} from '@/api/medicaltech'
import {patientGenderText} from '@/lib/patientGender'
import {hasPerm} from '@/lib/perm'
import EcgWavePanel from '@/components/his/EcgWavePanel.vue'
import {PAGE_SIZES, DEFAULT_PAGE_SIZE} from '@/lib/pagination'

interface Row {
  recordId: string
  recordNo: string
  patientName: string
  patientNo?: string
  gender?: number
  age?: number
  itemName: string
  bodyPart?: string
  applyDeptName?: string
  applyDoctorName?: string
  recordStatus?: number
  recordStatusText?: string
  waveId?: string
  ecgType?: number
  ecgTypeText?: string
  measureId?: string
  holterId?: string
  reportId?: string
  reportStatus?: number
  reportStatusText?: string
  writeBy?: string
  positiveFlagText?: string
  isCritical?: number
  rejectReason?: string
  reportVersion?: number
  createTime?: string
}

// 分栏：collectPending=记录状态 1~3（签到/采集是本站职责）；onlyUnwritten=已出结果但还没报告
const TABS = [
  {key: 'collect', label: '待采集', collectPending: true, reportStatus: null, onlyUnwritten: null},
  {key: 'unwritten', label: '待书写', collectPending: null, reportStatus: null, onlyUnwritten: true},
  {key: 'draft', label: '草稿', collectPending: null, reportStatus: 0, onlyUnwritten: null},
  {key: 'pending', label: '待审核', collectPending: null, reportStatus: 1, onlyUnwritten: null},
  {key: 'reviewed', label: '已审核', collectPending: null, reportStatus: 3, onlyUnwritten: null},
  {key: 'published', label: '已发布', collectPending: null, reportStatus: 4, onlyUnwritten: null},
  {key: 'all', label: '全部', collectPending: null, reportStatus: null, onlyUnwritten: null},
]

const activeTab = ref('collect')
const keyword = ref('')
const dateRange = ref<string[]>([])
const loading = ref(false)
const rows = ref<Row[]>([])
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
/** 各栏真实条数（探针请求拿 total，不用前端数出来的假数字） */
const tabCounts = ref<Record<string, number>>({})

const canCollect = computed(() => hasPerm('medtech:ecg:collect'))
const canWrite = computed(() => hasPerm('medtech:ecg:write'))
const canAudit = computed(() => hasPerm('medtech:ecg:audit'))
const canPublish = computed(() => hasPerm('medtech:ecg:publish'))

// ========== 列表 ==========

const currentTab = computed(() => TABS.find(t => t.key === activeTab.value) || TABS[6])

const buildQuery = (extra = {}) => ({
  pageNum: pagination.value.pageNum,
  pageSize: pagination.value.pageSize,
  keyword: keyword.value || undefined,
  collectPending: currentTab.value.collectPending ?? undefined,
  reportStatus: currentTab.value.reportStatus ?? undefined,
  onlyUnwritten: currentTab.value.onlyUnwritten ?? undefined,
  startDate: dateRange.value?.[0] || undefined,
  endDate: dateRange.value?.[1] || undefined,
  ...extra,
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getEcgListPage(buildQuery())
    rows.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
  } catch (e: any) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

/** 各栏条数：只要 total，pageSize 传 1（不受用户翻页控制） */
const loadTabCounts = async () => {
  const jobs = TABS.filter(t => t.key !== 'all').map(async (t) => {
    try {
      const res = await getEcgListPage({
        pageNum: 1, pageSize: 1,
        collectPending: t.collectPending ?? undefined,
        reportStatus: t.reportStatus ?? undefined,
        onlyUnwritten: t.onlyUnwritten ?? undefined,
        keyword: keyword.value || undefined,
        startDate: dateRange.value?.[0] || undefined,
        endDate: dateRange.value?.[1] || undefined,
      })
      tabCounts.value[t.key] = res.data?.total || 0
    } catch {
      tabCounts.value[t.key] = 0
    }
  })
  await Promise.all(jobs)
}

const refresh = async () => {
  pagination.value.pageNum = 1
  await Promise.all([loadData(), loadTabCounts()])
}

const handleTabChange = async () => {
  pagination.value.pageNum = 1
  await loadData()
}

const handleSizeChange = (v: number) => {
  pagination.value.pageSize = v
  pagination.value.pageNum = 1
  loadData()
}
const handleCurrentChange = (v: number) => {
  pagination.value.pageNum = v
  loadData()
}

// ========== 签到 / 波形采集（技师岗） ==========

const RHYTHM_OPTIONS = [
  {value: 1, label: '窦性心律'},
  {value: 2, label: '窦性心动过速'},
  {value: 3, label: '窦性心动过缓'},
  {value: 4, label: '心房颤动'},
  {value: 5, label: '室性早搏'},
]

const showCollect = ref(false)
const collectRow = ref<Row | null>(null)
const collectDetail = ref<any>(null)
const collectSubmitting = ref(false)
const rhythmCode = ref(1)

const openCollect = async (row: Row) => {
  collectRow.value = row
  rhythmCode.value = 1
  collectDetail.value = null
  try {
    const res = await getEcgDetailByRecordId(row.recordId)
    collectDetail.value = res.data
  } catch (e: any) {
    ElMessage.error(e?.message || '加载详情失败')
  }
  showCollect.value = true
}

const doCheckIn = async () => {
  collectSubmitting.value = true
  try {
    const res = await ecgCheckIn(collectRow.value?.recordId)
    collectDetail.value = res.data
    collectRow.value = {...collectRow.value!, recordStatus: 2, recordStatusText: '已签到'}
    ElMessage.success('已签到')
    await Promise.all([loadData(), loadTabCounts()])
  } catch (e: any) {
    ElMessage.error(e?.message || '签到失败')
  } finally {
    collectSubmitting.value = false
  }
}

const doSimulate = async () => {
  collectSubmitting.value = true
  try {
    const res = await ecgSimulateWave({recordId: collectRow.value?.recordId, rhythmCode: rhythmCode.value})
    collectDetail.value = res.data
    ElMessage.success('波形已入库（该检查已出结果）')
    await Promise.all([loadData(), loadTabCounts()])
  } catch (e: any) {
    ElMessage.error(e?.message || '采集失败')
  } finally {
    collectSubmitting.value = false
  }
}

// ========== 报告书写（医师岗） ==========

const showWrite = ref(false)
const submitting = ref(false)
const detail = ref<any>(null)
const templates = ref<any[]>([])
const form = ref({
  reportId: undefined as string | undefined,
  recordId: undefined as string | undefined,
  templateId: undefined as string | undefined,
  reportContent: '',
  conclusion: '',
  suggestions: '',
  positiveFlag: 0,
  isCritical: 0,
})
// 测量参数
const measureForm = ref({
  hr: undefined as number | undefined,
  prMs: undefined as number | undefined,
  qrsMs: undefined as number | undefined,
  qtMs: undefined as number | undefined,
  qtcMs: undefined as number | undefined,
  pAxis: undefined as number | undefined,
  qrsAxis: undefined as number | undefined,
  tAxis: undefined as number | undefined,
  rhythmText: '',
})
// Holter 分析（仅 ecgType=2 出现）
const holterForm = ref({
  wearStartTime: '' as string,
  wearEndTime: '' as string,
  totalBeats: undefined as number | undefined,
  avgHr: undefined as number | undefined,
  maxHr: undefined as number | undefined,
  maxHrTime: '',
  minHr: undefined as number | undefined,
  minHrTime: '',
  afibFlag: 0,
  afibBeats: undefined as number | undefined,
  svcCount: undefined as number | undefined,
  pvcCount: undefined as number | undefined,
  vtCount: undefined as number | undefined,
  pauseCount: undefined as number | undefined,
  longestPauseMs: undefined as number | undefined,
  stEpisodeCount: undefined as number | undefined,
})

const POSITIVE_OPTIONS = [
  {value: 0, label: '未判定'},
  {value: 1, label: '阴性'},
  {value: 2, label: '阳性'},
  {value: 3, label: '未见异常'},
]

const readonlyMode = computed(() => {
  const st = detail.value?.reportStatus
  return st === 4 || st === 5
})
const isHolter = computed(() => detail.value?.ecgType === 2)

const openWrite = async (row: Row) => {
  try {
    const res = await getEcgDetailByRecordId(row.recordId)
    detail.value = res.data
    form.value = {
      reportId: res.data?.reportId,
      recordId: res.data?.recordId,
      templateId: res.data?.templateId,
      reportContent: res.data?.reportContent || '',
      conclusion: res.data?.conclusion || '',
      suggestions: res.data?.suggestions || '',
      positiveFlag: res.data?.positiveFlag ?? 0,
      isCritical: res.data?.isCritical ?? 0,
    }
    measureForm.value = {
      hr: res.data?.hr, prMs: res.data?.prMs, qrsMs: res.data?.qrsMs,
      qtMs: res.data?.qtMs, qtcMs: res.data?.qtcMs,
      pAxis: res.data?.pAxis, qrsAxis: res.data?.qrsAxis, tAxis: res.data?.tAxis,
      rhythmText: res.data?.rhythmText || '',
    }
    holterForm.value = {
      wearStartTime: res.data?.wearStartTime || '', wearEndTime: res.data?.wearEndTime || '',
      totalBeats: res.data?.totalBeats, avgHr: res.data?.avgHr,
      maxHr: res.data?.maxHr, maxHrTime: res.data?.maxHrTime || '',
      minHr: res.data?.minHr, minHrTime: res.data?.minHrTime || '',
      afibFlag: res.data?.afibFlag ?? 0, afibBeats: res.data?.afibBeats,
      svcCount: res.data?.svcCount, pvcCount: res.data?.pvcCount,
      vtCount: res.data?.vtCount, pauseCount: res.data?.pauseCount,
      longestPauseMs: res.data?.longestPauseMs, stEpisodeCount: res.data?.stEpisodeCount,
    }
    if (!templates.value.length) {
      const t = await getEcgTemplateSelectList(res.data?.ecgType)
      templates.value = t.data || []
    }
    showWrite.value = true
  } catch (e: any) {
    ElMessage.error(e?.message || '打开失败')
  }
}

/** 套用模板：三段模板文本填进报告表单（会覆盖已写内容，所以先问一句） */
const applyTemplate = async (id: string) => {
  const t = templates.value.find(x => x.id === id)
  if (!t) return
  if (form.value.reportContent?.trim()) {
    try {
      await ElMessageBox.confirm('套用模板会覆盖当前已填写的心电图所见 / 诊断 / 建议，继续？', '套用模板', {
        confirmButtonText: '覆盖', cancelButtonText: '取消', type: 'warning',
      })
    } catch {
      return
    }
  }
  form.value.reportContent = t.findingTpl || ''
  form.value.conclusion = t.conclusionTpl || ''
  form.value.suggestions = t.suggestionTpl || ''
}

const doSaveMeasure = async () => {
  if (!measureForm.value.hr) return ElMessage.warning('请先填写心率')
  submitting.value = true
  try {
    const res = await ecgSaveMeasure({recordId: detail.value?.recordId, ...measureForm.value})
    detail.value = res.data
    ElMessage.success('测量参数已保存')
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

const doSaveHolter = async () => {
  if (!holterForm.value.wearStartTime || !holterForm.value.wearEndTime) {
    return ElMessage.warning('请先填写佩戴起止时间')
  }
  submitting.value = true
  try {
    const res = await ecgSaveHolter({recordId: detail.value?.recordId, ...holterForm.value})
    detail.value = res.data
    ElMessage.success('Holter 分析已保存')
    await Promise.all([loadData(), loadTabCounts()])
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

const payload = () => ({
  reportId: form.value.reportId,
  recordId: form.value.recordId,
  templateId: form.value.templateId,
  reportContent: form.value.reportContent,
  conclusion: form.value.conclusion,
  suggestions: form.value.suggestions,
  positiveFlag: form.value.positiveFlag,
  isCritical: form.value.isCritical,
})

const doSaveDraft = async () => {
  submitting.value = true
  try {
    const res = await ecgSaveDraft(payload())
    detail.value = res.data
    form.value.reportId = res.data?.reportId
    ElMessage.success('草稿已保存')
    await Promise.all([loadData(), loadTabCounts()])
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

const doSubmit = async () => {
  if (!detail.value?.waveId) {
    return ElMessage.warning('该检查还没有波形数据：请先完成波形采集再提交')
  }
  if (isHolter.value && !detail.value?.holterId) {
    return ElMessage.warning('Holter 检查必须先保存动态心电分析结果再提交')
  }
  if (!form.value.reportContent?.trim()) return ElMessage.warning('请先填写心电图所见')
  if (!form.value.conclusion?.trim()) return ElMessage.warning('请先填写心电图诊断')
  try {
    await ElMessageBox.confirm('提交后报告进入待审核，并由你完成报告医师签名。确认提交？', '提交审核', {
      confirmButtonText: '提交', cancelButtonText: '再改改', type: 'warning',
    })
  } catch {
    return
  }
  submitting.value = true
  try {
    await ecgSubmit(payload())
    ElMessage.success('已提交审核（报告医师签名已完成）')
    showWrite.value = false
    await Promise.all([loadData(), loadTabCounts()])
  } catch (e: any) {
    ElMessage.error(e?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

// ========== 审核 / 退回 ==========

const showAudit = ref(false)
const auditRow = ref<Row | null>(null)
const auditOpinion = ref('')
const auditDetail = ref<any>(null)

const openAudit = async (row: Row) => {
  if (!row.reportId) return ElMessage.warning('该检查还没有报告')
  auditRow.value = row
  auditOpinion.value = ''
  try {
    const res = await getEcgDetailByRecordId(row.recordId)
    auditDetail.value = res.data
  } catch (e: any) {
    ElMessage.error(e?.message || '加载详情失败')
  }
  showAudit.value = true
}

const doAudit = async () => {
  submitting.value = true
  try {
    await ecgAudit({reportId: auditRow.value?.reportId, reason: auditOpinion.value})
    ElMessage.success('审核通过（审核医师签名已完成）')
    showAudit.value = false
    await Promise.all([loadData(), loadTabCounts()])
  } catch (e: any) {
    // 服务端会拒绝「自己审自己」与「跳过审核」，这两条必须原样弹出来，不能吞掉
    ElMessage.error(e?.message || '审核失败')
  } finally {
    submitting.value = false
  }
}

const doReject = async () => {
  if (!auditOpinion.value?.trim()) return ElMessage.warning('退回必须写明原因')
  submitting.value = true
  try {
    await ecgReject({reportId: auditRow.value?.reportId, reason: auditOpinion.value})
    ElMessage.success('已退回，报告回到草稿')
    showAudit.value = false
    await Promise.all([loadData(), loadTabCounts()])
  } catch (e: any) {
    ElMessage.error(e?.message || '退回失败')
  } finally {
    submitting.value = false
  }
}

const doPublish = async (row: Row) => {
  if (!row.reportId) return
  try {
    await ElMessageBox.confirm('发布后临床医生与患者端可见该报告，确认发布？', '发布报告', {
      confirmButtonText: '发布', cancelButtonText: '取消', type: 'warning',
    })
  } catch {
    return
  }
  try {
    await ecgPublish(row.reportId)
    ElMessage.success('报告已发布')
    await Promise.all([loadData(), loadTabCounts()])
  } catch (e: any) {
    ElMessage.error(e?.message || '发布失败')
  }
}

const statusTagType = (st?: number) => {
  if (st == null) return 'info'
  if (st === 4) return 'success'
  if (st === 3) return 'success'
  if (st === 1) return 'warning'
  if (st === 5) return 'danger'
  return 'info'
}

const recordTagType = (st?: number) => {
  if (st == null) return 'info'
  if (st >= 4) return 'success'
  if (st >= 2) return 'warning'
  return 'info'
}

onMounted(refresh)
</script>

<template>
  <div v-loading="loading" class="space-y-4">
    <!-- 筛选 -->
    <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div class="flex flex-wrap items-center gap-3">
        <el-input v-model="keyword" placeholder="患者姓名 / 患者号 / 记录号 / 项目" :prefix-icon="Search"
                  class="!w-72" clearable @keyup.enter="refresh"/>
        <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD"
                        start-placeholder="开始日期" end-placeholder="截止日期" class="!w-64" @change="refresh"/>
        <el-button type="primary" @click="refresh">查询</el-button>
        <el-button @click="keyword = ''; dateRange = []; refresh()">重置</el-button>
      </div>
    </div>

    <!-- 分栏 -->
    <div class="rounded-lg border border-slate-200 bg-white shadow-sm">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="px-4 pt-2">
        <el-tab-pane v-for="t in TABS" :key="t.key" :name="t.key">
          <template #label>
            <span>{{ t.label }}</span>
            <span v-if="t.key !== 'all'" class="ml-1 text-xs text-slate-400">({{ tabCounts[t.key] ?? 0 }})</span>
          </template>
        </el-tab-pane>
      </el-tabs>

      <el-table :data="rows" style="width: 100%" size="small">
        <el-table-column prop="recordNo" label="记录号" width="170"/>
        <el-table-column label="患者" width="140">
          <template #default="{ row }">
            <span>{{ row.patientName }}</span>
            <span class="ml-1 text-xs text-slate-400">{{ patientGenderText(row.gender) }} {{ row.age }}岁</span>
          </template>
        </el-table-column>
        <el-table-column prop="itemName" label="检查项目" min-width="150"/>
        <el-table-column label="记录状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :type="recordTagType(row.recordStatus)">
              {{ row.recordStatusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="波形" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.waveId" size="small" effect="plain" type="success">
              {{ row.ecgTypeText || '常规' }}
            </el-tag>
            <span v-else class="text-xs text-slate-300">未采集</span>
          </template>
        </el-table-column>
        <el-table-column label="测量/Holter" width="100" align="center">
          <template #default="{ row }">
            <span class="text-xs">
              {{ [row.measureId ? '测量' : '', row.holterId ? 'Holter' : ''].filter(Boolean).join('/') || '—' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="报告状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :type="statusTagType(row.reportStatus)">
              {{ row.reportStatusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报告医师" width="100">
          <template #default="{ row }">
            <span class="text-xs">{{ row.writeBy || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="申请科室" width="120">
          <template #default="{ row }">
            <span class="text-xs">{{ row.applyDeptName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.recordStatus === 1" v-perm="'medtech:ecg:collect'" type="warning" link size="small"
                       @click="openCollect(row)">
              <el-icon class="mr-0.5">
                <VideoCamera/>
              </el-icon>
              签到
            </el-button>
            <el-button v-else-if="row.recordStatus === 2 || row.recordStatus === 3" v-perm="'medtech:ecg:collect'"
                       type="warning" link size="small" @click="openCollect(row)">
              <el-icon class="mr-0.5">
                <VideoCamera/>
              </el-icon>
              采集
            </el-button>
            <el-button v-if="row.recordStatus >= 4" v-perm="'medtech:ecg:write'" type="primary" link size="small"
                       @click="openWrite(row)">
              <el-icon class="mr-0.5">
                <EditPen/>
              </el-icon>
              {{ row.reportId ? '查看/修改' : '写报告' }}
            </el-button>
            <el-button v-if="row.reportStatus === 1" v-perm="'medtech:ecg:audit'" type="warning" link size="small"
                       @click="openAudit(row)">
              <el-icon class="mr-0.5">
                <Check/>
              </el-icon>
              审核
            </el-button>
            <el-button v-if="row.reportStatus === 3" v-perm="'medtech:ecg:publish'" type="success" link size="small"
                       @click="doPublish(row)">
              <el-icon class="mr-0.5">
                <Promotion/>
              </el-icon>
              发布
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-3 flex justify-end border-t border-slate-100 px-4 py-3">
        <el-pagination v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize"
                       :page-sizes="PAGE_SIZES" :total="pagination.total"
                       layout="total, sizes, prev, pager, next, jumper"
                       @size-change="handleSizeChange" @current-change="handleCurrentChange"/>
      </div>
    </div>

    <!-- 签到 / 波形采集 -->
    <el-dialog v-model="showCollect" width="860px" destroy-on-close
               :title="`心电采集 - ${collectDetail?.patientName || ''} ${collectDetail?.itemName || ''}`">
      <template v-if="collectDetail">
        <div class="space-y-3">
          <div class="rounded-lg bg-slate-50 p-3 text-xs leading-6">
            <span class="text-slate-400">患者：</span>{{ collectDetail.patientName }}
            {{ patientGenderText(collectDetail.gender) }} {{ collectDetail.age }}岁
            <span class="mx-2 text-slate-300">|</span>
            <span class="text-slate-400">记录号：</span>{{ collectDetail.recordNo }}
            <span class="mx-2 text-slate-300">|</span>
            <span class="text-slate-400">状态：</span>{{ collectDetail.recordStatusText }}
            <span class="mx-2 text-slate-300">|</span>
            <span class="text-slate-400">临床诊断：</span>{{ collectDetail.clinicalDiagnosis || '—' }}
          </div>

          <div v-if="collectDetail.recordStatus === 1" class="rounded-lg border border-amber-200 bg-amber-50 p-3">
            <span class="text-sm text-amber-700">该检查还是「已登记」，需要先签到再采集。</span>
            <el-button v-perm="'medtech:ecg:collect'" type="warning" size="small" class="ml-3"
                       :loading="collectSubmitting" @click="doCheckIn">签到
            </el-button>
          </div>

          <div v-else class="flex flex-wrap items-center gap-3">
            <span class="text-sm text-slate-600">演示/联调采集（真实设备走 POST /medicaltech/ecg/collectWave）</span>
            <el-select v-model="rhythmCode" class="!w-40" :disabled="collectDetail.recordStatus >= 5">
              <el-option v-for="o in RHYTHM_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
            <el-button v-perm="'medtech:ecg:collect'" type="primary" :loading="collectSubmitting"
                       :disabled="collectDetail.recordStatus >= 5" @click="doSimulate">
              {{ collectDetail.waveId ? '重新采集（覆盖）' : '模拟采集 12 导联' }}
            </el-button>
            <span v-if="collectDetail.recordStatus >= 5" class="text-xs text-slate-400">
              报告已进入审核/发布流程，波形锁定不能重采
            </span>
          </div>

          <EcgWavePanel :wave-data="collectDetail.waveData" title="12 导联心电图"/>
        </div>
      </template>
    </el-dialog>

    <!-- 报告书写 -->
    <el-dialog v-model="showWrite" width="1200px" destroy-on-close top="4vh"
               :title="`心电报告书写 - ${detail?.patientName || ''} ${detail?.itemName || ''}`">
      <template v-if="detail">
        <div class="grid grid-cols-5 gap-4">
          <!-- 左：波形 + 测量 + Holter -->
          <div class="col-span-2 space-y-3">
            <EcgWavePanel :wave-data="detail.waveData" :title="detail.ecgTypeText || '12 导联心电图'"/>

            <div class="rounded-lg border border-slate-200 p-3">
              <p class="mb-2 text-sm font-medium text-slate-700">测量参数</p>
              <div class="grid grid-cols-3 gap-x-2 gap-y-2">
                <div>
                  <label class="text-xs text-slate-500">心率 bpm</label>
                  <el-input-number v-model="measureForm.hr" :min="20" :max="300" size="small" class="!w-full"
                                   controls-position="right" :disabled="readonlyMode"/>
                </div>
                <div>
                  <label class="text-xs text-slate-500">PR ms</label>
                  <el-input-number v-model="measureForm.prMs" :min="0" :max="500" size="small" class="!w-full"
                                   controls-position="right" :disabled="readonlyMode"/>
                </div>
                <div>
                  <label class="text-xs text-slate-500">QRS ms</label>
                  <el-input-number v-model="measureForm.qrsMs" :min="0" :max="300" size="small" class="!w-full"
                                   controls-position="right" :disabled="readonlyMode"/>
                </div>
                <div>
                  <label class="text-xs text-slate-500">QT ms</label>
                  <el-input-number v-model="measureForm.qtMs" :min="0" :max="700" size="small" class="!w-full"
                                   controls-position="right" :disabled="readonlyMode"/>
                </div>
                <div>
                  <label class="text-xs text-slate-500">QTc ms</label>
                  <el-input-number v-model="measureForm.qtcMs" :min="0" :max="700" size="small" class="!w-full"
                                   controls-position="right" :disabled="readonlyMode"/>
                </div>
                <div>
                  <label class="text-xs text-slate-500">节律</label>
                  <el-input v-model="measureForm.rhythmText" size="small" maxlength="100"
                            placeholder="如 窦性心律" :disabled="readonlyMode"/>
                </div>
                <div>
                  <label class="text-xs text-slate-500">P 电轴°</label>
                  <el-input-number v-model="measureForm.pAxis" :min="-180" :max="180" size="small" class="!w-full"
                                   controls-position="right" :disabled="readonlyMode"/>
                </div>
                <div>
                  <label class="text-xs text-slate-500">QRS 电轴°</label>
                  <el-input-number v-model="measureForm.qrsAxis" :min="-180" :max="180" size="small" class="!w-full"
                                   controls-position="right" :disabled="readonlyMode"/>
                </div>
                <div>
                  <label class="text-xs text-slate-500">T 电轴°</label>
                  <el-input-number v-model="measureForm.tAxis" :min="-180" :max="180" size="small" class="!w-full"
                                   controls-position="right" :disabled="readonlyMode"/>
                </div>
              </div>
              <el-button v-if="!readonlyMode" v-perm="'medtech:ecg:write'" size="small" class="mt-2"
                         :loading="submitting" @click="doSaveMeasure">保存测量参数
              </el-button>
            </div>

            <!-- Holter 分析（仅动态心电出现） -->
            <div v-if="isHolter" class="rounded-lg border border-slate-200 p-3">
              <p class="mb-2 text-sm font-medium text-slate-700">
                Holter 动态心电分析
                <el-tag v-if="detail.holterId" size="small" type="success" class="ml-1">已分析</el-tag>
                <el-tag v-else size="small" type="danger" class="ml-1">未分析（提交报告前必填）</el-tag>
              </p>
              <div class="grid grid-cols-2 gap-x-2 gap-y-2">
                <div class="col-span-2 grid grid-cols-2 gap-2">
                  <div>
                    <label class="text-xs text-slate-500">佩戴开始</label>
                    <el-date-picker v-model="holterForm.wearStartTime" type="datetime" size="small"
                                    value-format="YYYY-MM-DD HH:mm:ss" class="!w-full" :disabled="readonlyMode"/>
                  </div>
                  <div>
                    <label class="text-xs text-slate-500">佩戴结束</label>
                    <el-date-picker v-model="holterForm.wearEndTime" type="datetime" size="small"
                                    value-format="YYYY-MM-DD HH:mm:ss" class="!w-full" :disabled="readonlyMode"/>
                  </div>
                </div>
                <div>
                  <label class="text-xs text-slate-500">总心搏</label>
                  <el-input-number v-model="holterForm.totalBeats" :min="0" size="small" class="!w-full"
                                   controls-position="right" :disabled="readonlyMode"/>
                </div>
                <div>
                  <label class="text-xs text-slate-500">平均心率 bpm</label>
                  <el-input-number v-model="holterForm.avgHr" :min="0" :max="250" size="small" class="!w-full"
                                   controls-position="right" :disabled="readonlyMode"/>
                </div>
                <div>
                  <label class="text-xs text-slate-500">最快心率</label>
                  <el-input-number v-model="holterForm.maxHr" :min="0" :max="250" size="small" class="!w-full"
                                   controls-position="right" :disabled="readonlyMode"/>
                </div>
                <div>
                  <label class="text-xs text-slate-500">最慢心率</label>
                  <el-input-number v-model="holterForm.minHr" :min="0" :max="250" size="small" class="!w-full"
                                   controls-position="right" :disabled="readonlyMode"/>
                </div>
                <div>
                  <label class="text-xs text-slate-500">房颤</label>
                  <el-select v-model="holterForm.afibFlag" size="small" class="!w-full" :disabled="readonlyMode">
                    <el-option label="未检出" :value="0"/>
                    <el-option label="检出" :value="1"/>
                  </el-select>
                </div>
                <div>
                  <label class="text-xs text-slate-500">房颤心搏</label>
                  <el-input-number v-model="holterForm.afibBeats" :min="0" size="small" class="!w-full"
                                   controls-position="right" :disabled="readonlyMode"/>
                </div>
                <div>
                  <label class="text-xs text-slate-500">室上早</label>
                  <el-input-number v-model="holterForm.svcCount" :min="0" size="small" class="!w-full"
                                   controls-position="right" :disabled="readonlyMode"/>
                </div>
                <div>
                  <label class="text-xs text-slate-500">室早</label>
                  <el-input-number v-model="holterForm.pvcCount" :min="0" size="small" class="!w-full"
                                   controls-position="right" :disabled="readonlyMode"/>
                </div>
                <div>
                  <label class="text-xs text-slate-500">室速阵次</label>
                  <el-input-number v-model="holterForm.vtCount" :min="0" size="small" class="!w-full"
                                   controls-position="right" :disabled="readonlyMode"/>
                </div>
                <div>
                  <label class="text-xs text-slate-500">停搏次数</label>
                  <el-input-number v-model="holterForm.pauseCount" :min="0" size="small" class="!w-full"
                                   controls-position="right" :disabled="readonlyMode"/>
                </div>
                <div>
                  <label class="text-xs text-slate-500">最长停搏 ms</label>
                  <el-input-number v-model="holterForm.longestPauseMs" :min="0" size="small" class="!w-full"
                                   controls-position="right" :disabled="readonlyMode"/>
                </div>
                <div>
                  <label class="text-xs text-slate-500">ST 段改变阵次</label>
                  <el-input-number v-model="holterForm.stEpisodeCount" :min="0" size="small" class="!w-full"
                                   controls-position="right" :disabled="readonlyMode"/>
                </div>
              </div>
              <el-button v-if="!readonlyMode" v-perm="'medtech:ecg:write'" size="small" type="primary" plain
                         class="mt-2" :loading="submitting" @click="doSaveHolter">保存 Holter 分析
              </el-button>
            </div>
          </div>

          <!-- 右：报告书写 -->
          <div class="col-span-3 space-y-3">
            <div class="rounded-lg bg-slate-50 p-3 text-xs leading-6">
              <span class="text-slate-400">患者：</span>{{ detail.patientName }}
              {{ patientGenderText(detail.gender) }} {{ detail.age }}岁
              <span class="mx-2 text-slate-300">|</span>
              <span class="text-slate-400">记录号：</span>{{ detail.recordNo }}
              <span class="mx-2 text-slate-300">|</span>
              <span class="text-slate-400">申请：</span>{{ detail.applyDeptName || '—' }}
              {{ detail.applyDoctorName || '' }}
              <span class="mx-2 text-slate-300">|</span>
              <span class="text-slate-400">临床诊断：</span>{{ detail.clinicalDiagnosis || '—' }}
            </div>

            <div class="flex items-center gap-2">
              <span class="text-sm text-slate-600">报告模板</span>
              <el-select v-model="form.templateId" placeholder="选择模板（可选）" class="!w-64" clearable
                         :disabled="readonlyMode" @change="applyTemplate">
                <el-option v-for="t in templates" :key="t.id" :label="t.templateName" :value="t.id"/>
              </el-select>
            </div>
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">心电图所见
                <span class="text-red-500">*</span></label>
              <el-input v-model="form.reportContent" type="textarea" :disabled="readonlyMode"
                        :autosize="{ minRows: 6, maxRows: 12 }" placeholder="描述各导联节律、波形、ST-T 改变…"/>
            </div>
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">心电图诊断
                <span class="text-red-500">*</span></label>
              <el-input v-model="form.conclusion" type="textarea" :disabled="readonlyMode"
                        :autosize="{ minRows: 3, maxRows: 6 }" placeholder="给出诊断意见…"/>
            </div>
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">建议</label>
              <el-input v-model="form.suggestions" type="textarea" :disabled="readonlyMode"
                        :autosize="{ minRows: 2, maxRows: 4 }"/>
            </div>
            <div class="flex items-center gap-6">
              <div class="flex items-center gap-2">
                <span class="text-sm text-slate-600">阴阳性</span>
                <el-select v-model="form.positiveFlag" class="!w-32" :disabled="readonlyMode">
                  <el-option v-for="o in POSITIVE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
                </el-select>
              </div>
              <el-checkbox v-model="form.isCritical" :true-label="1" :false-label="0"
                           :disabled="readonlyMode">危急
              </el-checkbox>
            </div>
            <div v-if="detail.rejectReason" class="rounded-lg border border-amber-200 bg-amber-50 p-3 text-xs">
              <span class="font-medium text-amber-700">上次退回原因：</span>{{ detail.rejectReason }}
              <span class="ml-2 text-slate-400">（第 {{ detail.reportVersion }} 版）</span>
            </div>
          </div>
        </div>
      </template>
      <template #footer>
        <el-button @click="showWrite = false">关闭</el-button>
        <el-button v-if="!readonlyMode" v-perm="'medtech:ecg:write'" :loading="submitting" @click="doSaveDraft">
          <el-icon class="mr-0.5">
            <Document/>
          </el-icon>
          保存草稿
        </el-button>
        <el-button v-if="!readonlyMode" v-perm="'medtech:ecg:write'" type="primary" :loading="submitting"
                   @click="doSubmit">
          <el-icon class="mr-0.5">
            <Check/>
          </el-icon>
          提交审核
        </el-button>
      </template>
    </el-dialog>

    <!-- 审核 -->
    <el-dialog v-model="showAudit" width="1100px" destroy-on-close top="4vh" title="心电报告审核">
      <template v-if="auditDetail">
        <div class="grid grid-cols-5 gap-4">
          <div class="col-span-2 space-y-3">
            <EcgWavePanel :wave-data="auditDetail.waveData" :title="auditDetail.ecgTypeText || '12 导联心电图'"/>
            <div v-if="auditDetail.hr || auditDetail.measureId" class="rounded-lg bg-slate-50 p-3 text-xs leading-6">
              <span class="text-slate-400">测量：</span>
              心率 {{ auditDetail.hr ?? '—' }}bpm · PR {{ auditDetail.prMs ?? '—' }}ms ·
              QRS {{ auditDetail.qrsMs ?? '—' }}ms · QTc {{ auditDetail.qtcMs ?? '—' }}ms
              <span v-if="auditDetail.rhythmText"> · {{ auditDetail.rhythmText }}</span>
            </div>
            <div v-if="auditDetail.holterId" class="rounded-lg bg-slate-50 p-3 text-xs leading-6">
              <span class="text-slate-400">Holter：</span>
              总心搏 {{ auditDetail.totalBeats ?? '—' }} · 平均 {{ auditDetail.avgHr ?? '—' }}bpm ·
              最快 {{ auditDetail.maxHr ?? '—' }} · 最慢 {{ auditDetail.minHr ?? '—' }} ·
              室早 {{ auditDetail.pvcCount ?? 0 }} · 室上早 {{ auditDetail.svcCount ?? 0 }} ·
              房颤 {{ auditDetail.afibFlag === 1 ? '检出' : '未检出' }} ·
              最长停搏 {{ auditDetail.longestPauseMs ?? 0 }}ms
            </div>
          </div>
          <div class="col-span-3 space-y-3">
            <div class="rounded-lg bg-slate-50 p-3 text-xs leading-6">
              <span class="text-slate-400">患者：</span>{{ auditDetail.patientName }}
              <span class="mx-2 text-slate-300">|</span>
              <span class="text-slate-400">项目：</span>{{ auditDetail.itemName }}
              <span class="mx-2 text-slate-300">|</span>
              <span class="text-slate-400">报告医师：</span>{{ auditDetail.writeBy || '—' }}
            </div>
            <div class="rounded-lg border border-slate-200 p-3">
              <p class="mb-1 text-sm font-medium text-slate-700">心电图所见</p>
              <p class="whitespace-pre-wrap text-sm text-slate-600">{{ auditDetail.reportContent || '—' }}</p>
            </div>
            <div class="rounded-lg border border-slate-200 p-3">
              <p class="mb-1 text-sm font-medium text-slate-700">心电图诊断</p>
              <p class="whitespace-pre-wrap text-sm text-slate-600">{{ auditDetail.conclusion || '—' }}</p>
            </div>
            <div v-if="auditDetail.suggestions" class="rounded-lg border border-slate-200 p-3">
              <p class="mb-1 text-sm font-medium text-slate-700">建议</p>
              <p class="whitespace-pre-wrap text-sm text-slate-600">{{ auditDetail.suggestions }}</p>
            </div>
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">审核意见</label>
              <el-input v-model="auditOpinion" type="textarea" :rows="3"
                        placeholder="审核意见（选填）；点「退回」时这里是必填的退回原因"/>
            </div>
          </div>
        </div>
      </template>
      <template #footer>
        <el-button @click="showAudit = false">取消</el-button>
        <el-button v-perm="'medtech:ecg:audit'" type="danger" :loading="submitting" @click="doReject">
          <el-icon class="mr-0.5">
            <Close/>
          </el-icon>
          退回重写
        </el-button>
        <el-button v-perm="'medtech:ecg:audit'" type="primary" :loading="submitting" @click="doAudit">
          <el-icon class="mr-0.5">
            <Check/>
          </el-icon>
          审核通过
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
