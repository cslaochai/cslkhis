<script setup lang="ts">
/**
 * 住院病历工作区（P2：结构化率 80% 的载体）
 *
 * 三条必须记住的口径：
 * 1. **结构化要素一条一列**：主诉/现病史/既往史/过敏史 + 体格检查按系统拆列 + 生命体征是数值列。
 *    这不是排版问题 —— 结构化率的分母就来自这份列清单（后端 RecordStructuredFields 是唯一口径），
 *    前端不自己算率、不自己定义"缺了哪项"，一律读后端给的 structuredFilled/Total/Rate/missingLabels。
 * 2. **数值 0 不是"没填"**：大便 0 次、尿量 0ml 都是合法观测值。前端不要做 `value || '—'` 这种
 *    "0 当空"的渲染（那样护士看到的就是一堆空的格子）。
 * 3. **AI 抽取的产出是候选值，不是自动填表**：`/ai/emrText/extract` 不写库，医生必须逐字段点「填入」
 *    才进表单 —— 这样"AI 有没有改过病历"的答案永远是"没有"。
 */
import {computed, onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {DataLine, Document, Plus, Refresh, Search, Warning} from '@element-plus/icons-vue'
import {getInpatientListPage} from '@/api/inpatient'
import {
  archiveInpatientRecord,
  getInpatientRecordDetail,
  getInpatientRecordListPage,
  getInpatientRecordLogs,
  getInpatientRecordStatusOptions,
  getInpatientRecordTypeOptions,
  getRecordQualityStat,
  saveInpatientRecord,
  submitInpatientRecord,
} from '@/api/inpatientRecord'
import {extractEmrText} from '@/api/ai'
import {searchIcd10} from '@/api/system'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'

interface AdmissionOption {
  admissionId: string
  patientNo?: string
  patientName?: string
  bedNo?: string
  wardName?: string
  deptName?: string
}

interface RecordRow {
  id: string
  recordNo: string
  admissionId: string
  patientName?: string
  wardName?: string
  bedNo?: string
  recordType?: number
  recordTypeText?: string
  recordTitle?: string
  recordTime?: string
  recordStatus?: number
  recordStatusText?: string
  doctorName?: string
  chiefComplaint?: string
  diagnosisName?: string
  structuredFilled?: number
  structuredTotal?: number
  structuredRate?: number
  structuredRateText?: string
  missingLabels?: string[]
  canEdit?: boolean
  canSubmit?: boolean
  canArchive?: boolean
  archiveByName?: string
}

interface ElementRow {
  code: string
  label: string
  group: string
  groupLabel: string
  filled: boolean
  value?: string
}

interface LogRow {
  id: string
  docType?: number
  docTypeText?: string
  recordNo?: string
  fieldLabel?: string
  operation?: string
  userName?: string
  oldValue?: string
  newValue?: string
  createTime?: string
}

interface QualityStat {
  recordCount?: number
  elementTotal?: number
  elementFilled?: number
  structuredRate?: number
  structuredRateText?: string
}

// ---------------- 基础数据 ----------------

const admissions = ref<AdmissionOption[]>([])
const admissionId = ref<string>('')
const typeOptions = ref<{ code: number; label: string }[]>([])
const statusOptions = ref<{ code: number; label: string }[]>([])

const fmtTime = (v?: string) => (v ? String(v).replace('T', ' ').slice(0, 16) : '—')
/** 数值 0 必须显示成 0，不能当空 */
const num = (v?: number | string) => (v === null || v === undefined || v === '' ? '—' : String(v))

const patientLabel = (a: AdmissionOption) =>
    `${a.bedNo || '—'} ${a.patientName || '—'}（${a.wardName || a.deptName || '—'}）`

const loadAdmissions = async () => {
  try {
    const res = await getInpatientListPage({admitStatus: 1, pageNum: 1, pageSize: 200})
    admissions.value = (res.data?.records || []) as AdmissionOption[]
    if (!admissionId.value && admissions.value.length > 0) {
      admissionId.value = String(admissions.value[0].admissionId)
    }
  } catch (error: any) {
    console.error('加载在院患者失败:', error)
  }
}

// ---------------- 列表 ----------------

const activeTab = ref('records')
const loading = ref(false)
const rows = ref<RecordRow[]>([])
const total = ref(0)
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE})
const query = ref({recordType: null as number | null, recordStatus: null as number | null, keyword: ''})

const stat = ref<QualityStat>({})

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await getInpatientRecordListPage({
      admissionId: admissionId.value || undefined,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      recordType: query.value.recordType ?? undefined,
      recordStatus: query.value.recordStatus ?? undefined,
      keyword: query.value.keyword || undefined,
    })
    rows.value = (res.data?.records || []) as RecordRow[]
    total.value = Number(res.data?.total ?? 0)
  } catch (error: any) {
    ElMessage.error(error.message || '加载病历列表失败')
  } finally {
    loading.value = false
  }
}

const loadStat = async () => {
  try {
    const res = await getRecordQualityStat(admissionId.value)
    stat.value = (res.data || {}) as QualityStat
  } catch (error: any) {
    stat.value = {}
  }
}

// ---------------- 修改留痕 ----------------

const logLoading = ref(false)
const logs = ref<LogRow[]>([])
const logTotal = ref(0)
const logPagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE})

const loadLogs = async () => {
  logLoading.value = true
  try {
    const res = await getInpatientRecordLogs({
      admissionId: admissionId.value || undefined,
      pageNum: logPagination.value.pageNum,
      pageSize: logPagination.value.pageSize,
    })
    logs.value = (res.data?.records || []) as LogRow[]
    logTotal.value = Number(res.data?.total ?? 0)
  } catch (error: any) {
    ElMessage.error(error.message || '加载修改留痕失败')
  } finally {
    logLoading.value = false
  }
}

const statCards = computed(() => [
  {
    label: '结构化率',
    value: stat.value.structuredRateText || '—',
    hint: `${stat.value.elementFilled ?? 0}/${stat.value.elementTotal ?? 0} 个要素`,
    color: 'text-emerald-600',
    bg: 'bg-emerald-50',
    icon: DataLine
  },
  {
    label: '文书份数',
    value: stat.value.recordCount ?? 0,
    hint: '含草稿与已归档',
    color: 'text-blue-600',
    bg: 'bg-blue-50',
    icon: Document
  },
  {
    label: '列表条数',
    value: total.value,
    hint: '当前筛选条件',
    color: 'text-slate-600',
    bg: 'bg-slate-100',
    icon: Search
  },
  {
    label: '修改留痕',
    value: logTotal.value,
    hint: '逐字段 diff，值真变才写',
    color: 'text-amber-600',
    bg: 'bg-amber-50',
    icon: Warning
  },
])

// ---------------- 新建 / 编辑 ----------------

const dialog = ref(false)
const saving = ref(false)
const editingId = ref<string>('')
const recordTypeOptions = ref<{ code: number; label: string }[]>([])

interface RecordForm {
  recordType: number
  recordTitle: string
  recordTime: string
  chiefComplaint: string
  presentIllness: string
  pastHistory: string
  personalHistory: string
  familyHistory: string
  allergyHistory: string
  temperature: number | undefined
  pulse: number | undefined
  respiration: number | undefined
  systolicPressure: number | undefined
  diastolicPressure: number | undefined
  height: number | undefined
  weight: number | undefined
  generalCondition: string
  skinMucosa: string
  headNeck: string
  chestLung: string
  heart: string
  abdomen: string
  spineLimbs: string
  nervousSystem: string
  specialistExam: string
  auxiliaryExam: string
  diagnosisName: string
  diagnosisCode: string
  treatmentPlan: string
  courseNote: string
  remark: string
}

const emptyForm = (): RecordForm => ({
  recordType: 1, recordTitle: '', recordTime: '',
  chiefComplaint: '', presentIllness: '', pastHistory: '', personalHistory: '', familyHistory: '', allergyHistory: '',
  temperature: undefined, pulse: undefined, respiration: undefined,
  systolicPressure: undefined, diastolicPressure: undefined, height: undefined, weight: undefined,
  generalCondition: '', skinMucosa: '', headNeck: '', chestLung: '', heart: '', abdomen: '',
  spineLimbs: '', nervousSystem: '', specialistExam: '',
  auxiliaryExam: '', diagnosisName: '', diagnosisCode: '', treatmentPlan: '', courseNote: '', remark: '',
})

const form = ref<RecordForm>(emptyForm())

const openCreate = () => {
  if (!admissionId.value) {
    ElMessage.warning('请先选择在院患者')
    return
  }
  editingId.value = ''
  form.value = emptyForm()
  icdCodes.value = []
  icdOptions.value = []
  icdNameOf.clear()
  aiCandidates.value = []
  aiRaw.value = ''
  aiNote.value = ''
  dialog.value = true
}

const openEdit = async (row: RecordRow) => {
  try {
    const res = await getInpatientRecordDetail(row.id)
    const d = res.data || ({} as any)
    editingId.value = row.id
    form.value = {
      recordType: d.recordType ?? 1,
      recordTitle: d.recordTitle ?? '',
      recordTime: (d.recordTime || '').replace(' ', 'T'),
      chiefComplaint: d.chiefComplaint ?? '',
      presentIllness: d.presentIllness ?? '',
      pastHistory: d.pastHistory ?? '',
      personalHistory: d.personalHistory ?? '',
      familyHistory: d.familyHistory ?? '',
      allergyHistory: d.allergyHistory ?? '',
      temperature: d.temperature ?? undefined,
      pulse: d.pulse ?? undefined,
      respiration: d.respiration ?? undefined,
      systolicPressure: d.systolicPressure ?? undefined,
      diastolicPressure: d.diastolicPressure ?? undefined,
      height: d.height ?? undefined,
      weight: d.weight ?? undefined,
      generalCondition: d.generalCondition ?? '',
      skinMucosa: d.skinMucosa ?? '',
      headNeck: d.headNeck ?? '',
      chestLung: d.chestLung ?? '',
      heart: d.heart ?? '',
      abdomen: d.abdomen ?? '',
      spineLimbs: d.spineLimbs ?? '',
      nervousSystem: d.nervousSystem ?? '',
      specialistExam: d.specialistExam ?? '',
      auxiliaryExam: d.auxiliaryExam ?? '',
      diagnosisName: d.diagnosisName ?? '',
      diagnosisCode: d.diagnosisCode ?? '',
      treatmentPlan: d.treatmentPlan ?? '',
      courseNote: d.courseNote ?? '',
      remark: d.remark ?? '',
    }
    seedDiagnosis(d.diagnosisCode, d.diagnosisName)
    aiCandidates.value = []
    aiRaw.value = ''
    aiNote.value = ''
    dialog.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '加载病历详情失败')
  }
}

// ---------------- ICD-10 诊断选码（G3） ----------------

/**
 * 诊断为什么要用选码器而不是两个手输框：
 * 手输时医生凭记忆敲编码，敲错/敲简写都不会有人发现，而 `diagnosis_code` 是
 * 病案首页、DRG 分组、医保结算的上游依据 —— 错一个码下游全错，且**零报错**。
 *
 * 两个必须说清的现状（实测 2026-09-23）：
 * 1. 码表已从演示期 35 条扩到 **40477 条**，检索必须按相关性排序，
 *    否则输入「肺炎」首条会返回「A01.005+J17.0* 伤寒并发肺炎」（后端已修）。
 * 2. 存量数据里有**码表里查不到的编码**（`J18.900` / `J45.900` / `O14.900` /
 *    `K29.500` / `J20.900`）。所以选码器**不能**把不在选项里的值当空值丢掉 ——
 *    打开老病历时必须原样把编码显示出来，医生改不改由他决定。
 */
const icdOptions = ref<{ icdCode: string; icdName: string; icdCategory?: string }[]>([])
const icdLoading = ref(false)
/** 本次选中的编码（`diagnosis_code` 是分号分隔的多值列，这里用数组承载） */
const icdCodes = ref<string[]>([])
/** 编码 → 名称缓存：含历史数据回填，避免改码后把解析不出的诊断名写空 */
const icdNameOf = new Map<string, string>()
/** 请求序号：远程搜索是异步的，慢响应回来晚了不能覆盖新结果 */
let icdSeq = 0

const searchIcdOptions = async (query: string) => {
  const kw = (query || '').trim()
  if (!kw) {
    icdOptions.value = []
    return
  }
  const seq = ++icdSeq
  icdLoading.value = true
  try {
    const res = await searchIcd10(kw)
    if (seq !== icdSeq) return
    const seen = new Set<string>()
    icdOptions.value = ((res.data || []) as any[])
        .filter(o => o?.icdCode && !seen.has(o.icdCode) && seen.add(o.icdCode))
        .map(o => ({icdCode: o.icdCode, icdName: o.icdName, icdCategory: o.icdCategory}))
    icdOptions.value.forEach(o => icdNameOf.set(o.icdCode, o.icdName))
  } catch (error) {
    if (seq !== icdSeq) return
    icdOptions.value = []
    console.error('检索 ICD 编码失败:', error)
  } finally {
    if (seq === icdSeq) icdLoading.value = false
  }
}

/** 打开表单时把「分号分隔的双列」还原成编码数组，并用历史名称回填缓存 */
const seedDiagnosis = (codeStr?: string, nameStr?: string) => {
  const codes = String(codeStr || '').split(';').map(s => s.trim()).filter(Boolean)
  const names = String(nameStr || '').split(';').map(s => s.trim())
  icdCodes.value = codes
  if (codes.length > 0 && codes.length === names.length) {
    codes.forEach((code, i) => {
      if (names[i]) icdNameOf.set(code, names[i])
    })
  }
}

/**
 * 选码后回写两个文本列。
 * 名称只在**每个编码都能解析出名称**时才整体重写：只要有解析不出的（老码），
 * 就保留原文本不动 —— 宁可名称没跟着更新，也不能把诊断名写空。
 */
const syncDiagnosisText = () => {
  const codes = icdCodes.value.filter(Boolean)
  form.value.diagnosisCode = codes.join(';')
  const names = codes.map(code => icdNameOf.get(code))
  if (names.length > 0 && names.every(n => n)) {
    form.value.diagnosisName = names.join(';')
  }
}

// ---------------- AI 抽取（候选值，不自动填表） ----------------

const aiRaw = ref('')
const aiLoading = ref(false)
const aiNote = ref('')
const aiCandidates = ref<{ field: string; fieldLabel: string; value: string; source: string }[]>([])

/** 只认这份白名单：模型给出的其它字段一律不进表单（防"顺手多填"） */
const AI_FIELD_MAP: Record<string, string> = {
  chiefComplaint: 'chiefComplaint',
  presentIllness: 'presentIllness',
  pastHistory: 'pastHistory',
  personalHistory: 'personalHistory',
  familyHistory: 'familyHistory',
  allergyHistory: 'allergyHistory',
  generalCondition: 'generalCondition',
  skinMucosa: 'skinMucosa',
  headNeck: 'headNeck',
  chestLung: 'chestLung',
  heart: 'heart',
  abdomen: 'abdomen',
  spineLimbs: 'spineLimbs',
  nervousSystem: 'nervousSystem',
  specialistExam: 'specialistExam',
  auxiliaryExam: 'auxiliaryExam',
  treatmentPlan: 'treatmentPlan',
}

const extractByAi = async () => {
  if (!aiRaw.value.trim()) {
    ElMessage.warning('请先粘贴待抽取的病历文本')
    return
  }
  aiLoading.value = true
  aiNote.value = ''
  try {
    const res = await extractEmrText({rawText: aiRaw.value})
    const data = res.data || ({} as any)
    aiCandidates.value = (data.fields || []).filter((f: any) => AI_FIELD_MAP[f.field])
    const rejected = Number(data.rejectedCount ?? 0)
    aiNote.value = rejected > 0
        ? `抽取完成，${rejected} 条被判为「原文中找不到依据 / 字段不可写」已丢弃`
        : '抽取完成，请逐条核对后点「填入」'
  } catch (error: any) {
    ElMessage.error(error.message || '病历文本抽取失败')
  } finally {
    aiLoading.value = false
  }
}

const applyCandidate = (c: { field: string; value: string }) => {
  const key = AI_FIELD_MAP[c.field]
  if (!key) return
      ;
  (form.value as any)[key] = c.value
  ElMessage.success(`已填入「${c.field}」（仍需医生核对）`)
}

// ---------------- 保存 / 提交 / 归档 ----------------

const submitRecord = async () => {
  if (!admissionId.value) {
    ElMessage.warning('请先选择在院患者')
    return
  }
  saving.value = true
  try {
    const payload: any = {...form.value, admissionId: admissionId.value}
    if (editingId.value) {
      payload.id = editingId.value
    }
    // 「传什么覆盖什么」：空字符串保持空串（置空是有意的动作，服务层会留痕）
    await saveInpatientRecord(payload)
    ElMessage.success(editingId.value ? '病历已修改（变更已逐字段留痕）' : '病历文书已创建')
    dialog.value = false
    await reloadAll()
  } catch (error: any) {
    ElMessage.error(error.message || '保存病历失败')
  } finally {
    saving.value = false
  }
}

const doSubmit = async (row: RecordRow) => {
  try {
    const res = await submitInpatientRecord({ids: [row.id]})
    ElMessage.success(`已提交 ${res.data} 份病历`)
    await reloadAll()
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  }
}

const doArchive = async (row: RecordRow) => {
  try {
    const {value} = await ElMessageBox.prompt(
        `归档 ${row.recordNo}（${row.recordTypeText || ''}）。归档是单向门，归档后禁止修改：`,
        '归档病历文书',
        {inputPlaceholder: '如：已送病案室，2026-09 批次', confirmButtonText: '确认归档', cancelButtonText: '取消'},
    )
    if (!value) {
      ElMessage.warning('归档说明必填')
      return
    }
    const res = await archiveInpatientRecord({ids: [row.id], remark: value})
    ElMessage.success(`已归档 ${res.data} 份病历`)
    await reloadAll()
  } catch (error: any) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error.message || '归档失败')
  }
}

// ---------------- 详情（要素明细） ----------------

const detailDialog = ref(false)
const detail = ref<any>({})
const detailElements = ref<ElementRow[]>([])

const openDetail = async (row: RecordRow) => {
  try {
    const res = await getInpatientRecordDetail(row.id)
    detail.value = res.data || {}
    detailElements.value = (res.data?.elements || []) as ElementRow[]
    detailDialog.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '加载病历详情失败')
  }
}

const rateColor = (rate?: number) => {
  if (rate === undefined || rate === null) return '#94a3b8'
  if (rate >= 80) return '#16a34a'
  if (rate >= 60) return '#f59e0b'
  return '#ef4444'
}

const statusTagType = (s?: number) => {
  if (s === 1) return 'info'
  if (s === 2) return 'warning'
  if (s === 3) return 'success'
  return 'info'
}

// ---------------- 刷新 ----------------

const reloadAll = async () => {
  await Promise.all([loadRecords(), loadLogs(), loadStat()])
}

const handleAdmissionChange = async () => {
  pagination.value.pageNum = 1
  logPagination.value.pageNum = 1
  await reloadAll()
}

onMounted(async () => {
  try {
    const [t, s] = await Promise.all([getInpatientRecordTypeOptions(), getInpatientRecordStatusOptions()])
    typeOptions.value = (t.data || []) as any
    statusOptions.value = (s.data || []) as any
    recordTypeOptions.value = typeOptions.value
  } catch (error: any) {
    console.error('加载下拉失败:', error)
  }
  await loadAdmissions()
  await reloadAll()
})
</script>

<template>
  <div class="space-y-6">
    <!-- 页头 -->
    <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div class="flex items-center gap-3">
        <el-select
            v-model="admissionId"
            data-testid="p2-admission-select"
            placeholder="选择在院患者"
            filterable
            class="!w-80"
            @change="handleAdmissionChange"
        >
          <el-option v-for="a in admissions" :key="a.admissionId" :label="patientLabel(a)"
                     :value="String(a.admissionId)"/>
        </el-select>
        <el-button :icon="Refresh" @click="reloadAll">刷新</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-2 gap-4 lg:grid-cols-4">
      <div
          v-for="s in statCards"
          :key="s.label"
          class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
      >
        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg" :class="s.bg">
          <el-icon class="h-5 w-5" :class="s.color">
            <component :is="s.icon"/>
          </el-icon>
        </div>
        <div class="min-w-0">
          <p class="truncate text-xs text-slate-500">{{ s.label }}</p>
          <p class="text-lg font-bold text-slate-900">{{ s.value }}</p>
          <p class="truncate text-[11px] text-slate-400">{{ s.hint }}</p>
        </div>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <!-- ============== 病历列表 ============== -->
      <el-tab-pane label="病历列表" name="records">
        <div class="mb-3 flex flex-wrap items-center gap-3">
          <el-input
              v-model="query.keyword"
              placeholder="搜索文书号 / 标题 / 主诉 / 诊断"
              :prefix-icon="Search"
              class="!w-64"
              clearable
              @keyup.enter="loadRecords"
          />
          <el-select v-model="query.recordType" placeholder="文书类型" clearable class="!w-36">
            <el-option v-for="t in typeOptions" :key="t.code" :label="t.label" :value="t.code"/>
          </el-select>
          <el-select v-model="query.recordStatus" placeholder="文书状态" clearable class="!w-32">
            <el-option v-for="t in statusOptions" :key="t.code" :label="t.label" :value="t.code"/>
          </el-select>
          <el-button type="primary" @click="loadRecords">查询</el-button>
          <el-button class="!ml-auto" type="primary" :icon="Plus" v-perm="'ipd:record:add'" data-testid="p2-open-record"
                     @click="openCreate">
            新建病历文书
          </el-button>
        </div>

        <el-table v-loading="loading" data-testid="p2-record-table" :data="rows" style="width: 100%" border>
          <el-table-column prop="recordNo" label="文书号" width="150"/>
          <el-table-column label="类型" width="110">
            <template #default="{ row }">
              <el-tag size="small" effect="plain">{{ row.recordTypeText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="标题 / 主诉" min-width="220">
            <template #default="{ row }">
              <div class="text-sm font-medium text-slate-800">{{ row.recordTitle || row.recordTypeText || '—' }}</div>
              <div class="truncate text-xs text-slate-400">{{ row.chiefComplaint || '—' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="诊断" min-width="160">
            <template #default="{ row }">
              <span class="text-xs text-slate-600">{{ row.diagnosisName || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="结构化率" width="150">
            <template #default="{ row }">
              <div class="text-sm font-medium" :style="{ color: rateColor(row.structuredRate) }">
                {{ row.structuredRateText }}
              </div>
              <div class="text-xs text-slate-400">{{ row.structuredFilled ?? 0 }}/{{ row.structuredTotal ?? 0 }} 要素
              </div>
            </template>
          </el-table-column>
          <el-table-column label="缺项" min-width="180">
            <template #default="{ row }">
              <span v-if="row.missingLabels && row.missingLabels.length" class="text-xs text-rose-500">
                {{ row.missingLabels.slice(0, 4).join('、') }}{{ row.missingLabels.length > 4 ? ' …' : '' }}
              </span>
              <span v-else class="text-xs text-emerald-600">无缺失</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.recordStatus)" size="small">{{ row.recordStatusText }}</el-tag>
              <div v-if="row.archiveByName" class="mt-0.5 text-xs text-slate-400">归档 {{ row.archiveByName }}</div>
            </template>
          </el-table-column>
          <el-table-column label="签名" width="130">
            <template #default="{ row }">
              <!-- 三态分开显示：0-未签名 / 1-已签名 / 2-签名已失效。
                   「2-签名已失效」绝不能回落成「未签名」—— "有人作废过签名"和"从来没签过"
                   是两件完全不同的事实（P5.5 口径）。码值未知时如实显示未知(n)，不猜。 -->
              <el-tag v-if="row.signStatus === 1" type="success" size="small"
                      :data-testid="`p5-sign-tag-${row.recordNo}`">已签名
              </el-tag>
              <el-tag v-else-if="row.signStatus === 2" type="warning" size="small"
                      :data-testid="`p5-sign-tag-${row.recordNo}`">签名已失效
              </el-tag>
              <el-tag v-else-if="row.signStatus === 0" type="info" size="small" effect="plain"
                      :data-testid="`p5-sign-tag-${row.recordNo}`">未签名
              </el-tag>
              <span v-else class="text-xs text-slate-400">未知({{ row.signStatus }})</span>
              <div v-if="row.signedTime" class="mt-0.5 text-xs text-slate-400">{{ fmtTime(row.signedTime) }}</div>
              <div v-else-if="row.signStatus === 0 && row.recordStatus === 3" class="mt-0.5 text-xs text-slate-400">
                存量文书，不补签
              </div>
            </template>
          </el-table-column>
          <el-table-column label="书写 / 时间" width="150">
            <template #default="{ row }">
              <div class="text-xs text-slate-600">{{ row.doctorName || '—' }}</div>
              <div class="text-xs text-slate-400">{{ fmtTime(row.recordTime) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.canEdit" v-perm="'ipd:record:edit'" type="primary" link size="small"
                         @click="openEdit(row)">编辑
              </el-button>
              <el-button type="info" link size="small" @click="openDetail(row)">要素明细</el-button>
              <el-button v-if="row.canSubmit" v-perm="'ipd:record:edit'" type="warning" link size="small"
                         data-testid="p2-submit-btn" @click="doSubmit(row)">提交
              </el-button>
              <el-button v-if="row.canArchive" v-perm="'ipd:record:edit'" type="success" link size="small"
                         data-testid="p2-archive-btn" @click="doArchive(row)">归档
              </el-button>
              <span v-if="!row.canEdit && !row.canSubmit && !row.canArchive"
                    class="text-xs text-slate-400">已封存</span>
            </template>
          </el-table-column>
          <template #empty>
            <div class="py-6 text-sm text-slate-400">该患者暂无病历文书，点右上「新建病历文书」</div>
          </template>
        </el-table>
        <div class="mt-3 flex justify-end">
          <el-pagination
              v-model:current-page="pagination.pageNum"
              v-model:page-size="pagination.pageSize"
              :page-sizes="PAGE_SIZES"
              :total="total"
              layout="total, sizes, prev, pager, next"
              @size-change="loadRecords"
              @current-change="loadRecords"
          />
        </div>
      </el-tab-pane>

      <!-- ============== 修改留痕 ============== -->
      <el-tab-pane label="修改留痕" name="logs">
        <el-table v-loading="logLoading" data-testid="p2-log-table" :data="logs" style="width: 100%" border>
          <el-table-column label="时间" width="150">
            <template #default="{ row }">{{ fmtTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column prop="recordNo" label="文书号" width="150"/>
          <el-table-column prop="operation" label="操作" width="100">
            <template #default="{ row }">
              <el-tag size="small" effect="plain">{{ row.operation }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="fieldLabel" label="字段" width="140"/>
          <el-table-column label="变更前" min-width="200">
            <template #default="{ row }">
              <span class="text-xs text-slate-500">{{ row.oldValue || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="变更后" min-width="200">
            <template #default="{ row }">
              <span class="text-xs text-slate-800">{{ row.newValue || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="userName" label="操作人" width="120"/>
          <template #empty>
            <div class="py-6 text-sm text-slate-400">还没有修改留痕（逐字段 diff，值真变才写）</div>
          </template>
        </el-table>
        <div class="mt-3 flex justify-end">
          <el-pagination
              v-model:current-page="logPagination.pageNum"
              v-model:page-size="logPagination.pageSize"
              :page-sizes="PAGE_SIZES"
              :total="logTotal"
              layout="total, sizes, prev, pager, next"
              @size-change="loadLogs"
              @current-change="loadLogs"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- ============== 新建 / 编辑 ============== -->
    <el-dialog
        v-model="dialog"
        :title="editingId ? '编辑病历文书（传什么覆盖什么，变更逐字段留痕）' : '新建病历文书（结构化要素）'"
        width="72%"
        top="4vh"
    >
      <el-scrollbar max-height="62vh">
        <el-form label-width="110px" class="pr-2">
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="文书类型" required>
                <el-select
                    v-model="form.recordType"
                    data-testid="p2-form-type"
                    :disabled="!!editingId"
                    class="!w-full"
                >
                  <el-option v-for="t in recordTypeOptions" :key="t.code" :label="t.label" :value="t.code"/>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="文书标题">
                <el-input v-model="form.recordTitle" placeholder="留空则取类型文案"/>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="记录时间">
                <el-date-picker
                    v-model="form.recordTime"
                    type="datetime"
                    value-format="YYYY-MM-DDTHH:mm:ss"
                    placeholder="留空取当前时间"
                    class="!w-full"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- AI 抽取：产出是候选值，必须医生点「填入」 -->
          <el-card shadow="never" class="mb-4">
            <template #header>
              <div class="flex items-center justify-between">
                <span class="text-sm font-medium">AI 结构化抽取（不写库，产出只是候选值）</span>
                <el-button size="small" type="primary" data-testid="p2-ai-extract" :loading="aiLoading"
                           @click="extractByAi">
                  抽取
                </el-button>
              </div>
            </template>
            <el-input
                v-model="aiRaw"
                data-testid="p2-ai-text"
                type="textarea"
                :rows="3"
                placeholder="粘贴外院病历 / 口述转写文本，如：主诉：咳嗽发热3天。现病史：…… 既往史：……"
            />
            <div v-if="aiNote" class="mt-2 text-xs text-emerald-600">{{ aiNote }}</div>
            <div v-if="aiCandidates.length" class="mt-3 space-y-2">
              <div
                  v-for="c in aiCandidates"
                  :key="c.field"
                  class="flex items-start gap-3 rounded border border-slate-200 bg-slate-50 p-2"
              >
                <span class="w-20 shrink-0 text-xs text-slate-500">{{ c.fieldLabel }}</span>
                <span class="flex-1 text-xs text-slate-700">{{ c.value }}</span>
                <el-tag size="small" effect="plain">{{ c.source === 'HARD_RULE' ? '原文切分' : '模型搬运' }}</el-tag>
                <el-button size="small" type="primary" link @click="applyCandidate(c)">填入</el-button>
              </div>
            </div>
          </el-card>

          <!-- 病史 -->
          <div class="mb-1 text-sm font-semibold text-slate-700">病史要素</div>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="主诉">
                <el-input v-model="form.chiefComplaint" data-testid="p2-chief-complaint" placeholder="症状 + 持续时间"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="过敏史">
                <el-input v-model="form.allergyHistory" placeholder="无过敏史请写「否认」"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="现病史">
            <el-input v-model="form.presentIllness" type="textarea" :rows="2"/>
          </el-form-item>
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="既往史">
                <el-input v-model="form.pastHistory" type="textarea" :rows="2"/>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="个人史">
                <el-input v-model="form.personalHistory" type="textarea" :rows="2"/>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="家族史">
                <el-input v-model="form.familyHistory" type="textarea" :rows="2"/>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 生命体征（数值列） -->
          <div class="mb-1 text-sm font-semibold text-slate-700">生命体征（数值列，0 不是空值）</div>
          <el-row :gutter="16">
            <el-col :span="4">
              <el-form-item label="体温℃">
                <el-input-number v-model="form.temperature" :precision="1" :step="0.1" :min="34" :max="43"
                                 controls-position="right" class="!w-full"/>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="脉搏">
                <el-input-number v-model="form.pulse" :min="20" :max="250" controls-position="right" class="!w-full"/>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="呼吸">
                <el-input-number v-model="form.respiration" :min="5" :max="80" controls-position="right"
                                 class="!w-full"/>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="收缩压">
                <el-input-number v-model="form.systolicPressure" :min="40" :max="300" controls-position="right"
                                 class="!w-full"/>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="舒张压">
                <el-input-number v-model="form.diastolicPressure" :min="20" :max="200" controls-position="right"
                                 class="!w-full"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="6">
              <el-form-item label="身高cm">
                <el-input-number v-model="form.height" :precision="1" :min="30" :max="250" controls-position="right"
                                 class="!w-full"/>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="体重kg">
                <el-input-number v-model="form.weight" :precision="1" :min="1" :max="300" controls-position="right"
                                 class="!w-full"/>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 体格检查 -->
          <div class="mb-1 text-sm font-semibold text-slate-700">体格检查（按系统拆列，缺哪一项算得出来）</div>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="一般情况">
                <el-input v-model="form.generalCondition"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="皮肤黏膜">
                <el-input v-model="form.skinMucosa"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="头颈部">
                <el-input v-model="form.headNeck"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="胸部及肺">
                <el-input v-model="form.chestLung"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="心脏">
                <el-input v-model="form.heart"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="腹部">
                <el-input v-model="form.abdomen" data-testid="p2-abdomen"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="脊柱四肢">
                <el-input v-model="form.spineLimbs"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="神经系统">
                <el-input v-model="form.nervousSystem"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="专科检查">
            <el-input v-model="form.specialistExam" type="textarea" :rows="2"/>
          </el-form-item>

          <!-- 结论 -->
          <div class="mb-1 text-sm font-semibold text-slate-700">诊疗过程与结论</div>
          <el-form-item label="辅助检查">
            <el-input v-model="form.auxiliaryExam" type="textarea" :rows="2"/>
          </el-form-item>
          <el-row :gutter="16">
            <el-col :span="14">
              <el-form-item label="诊断编码（ICD-10）">
                <!-- 只能从码表里选：编码是病案首页/DRG/医保结算的上游依据，
                     放开自由输入等于把「码写错」变成无人可查的静默错误。
                     但**不**禁止显示历史值：存量数据里 J18.900 等码已不在码表，
                     el-select 在无匹配项时会原样显示该值，医生据此决定改不改。 -->
                <el-select
                    v-model="icdCodes"
                    data-testid="p2-diagnosis"
                    class="!w-full"
                    multiple
                    filterable
                    remote
                    clearable
                    reserve-keyword
                    :remote-method="searchIcdOptions"
                    :loading="icdLoading"
                    placeholder="输入疾病名称或 ICD 编码检索，可多选"
                    @change="syncDiagnosisText"
                >
                  <el-option
                      v-for="o in icdOptions"
                      :key="o.icdCode"
                      :label="`${o.icdCode} ${o.icdName}`"
                      :value="o.icdCode"
                  >
                    <span class="font-mono text-xs text-slate-500">{{ o.icdCode }}</span>
                    <span class="ml-2 text-slate-800">{{ o.icdName }}</span>
                    <span v-if="o.icdCategory" class="ml-2 text-xs text-slate-400">{{ o.icdCategory }}</span>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="10">
              <el-form-item label="诊断名称">
                <el-input
                    v-model="form.diagnosisName"
                    data-testid="p2-diagnosis-name"
                    placeholder="选中编码后自动带出，可再补充"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="诊疗计划">
            <el-input v-model="form.treatmentPlan" type="textarea" :rows="2"/>
          </el-form-item>
          <el-form-item label="病程正文">
            <el-input v-model="form.courseNote" type="textarea" :rows="2" placeholder="病程类文书填这里"/>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.remark" data-testid="p2-remark"/>
          </el-form-item>
        </el-form>
      </el-scrollbar>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" v-perm="['ipd:record:add','ipd:record:edit']"
                   data-testid="p2-submit-record" @click="submitRecord">
          {{ editingId ? '保存修改' : '创建文书' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ============== 要素明细 ============== -->
    <el-dialog v-model="detailDialog" title="结构化要素明细" width="60%">
      <div class="mb-3 text-sm text-slate-600">
        {{ detail.patientName || '—' }} · {{ detail.recordTypeText || '—' }} · {{ detail.recordNo || '—' }}
        <span class="ml-3 font-semibold" :style="{ color: rateColor(detail.structuredRate) }">
          结构化率 {{ detail.structuredRateText || '—' }}（{{
            detail.structuredFilled ?? 0
          }}/{{ detail.structuredTotal ?? 0 }}）
        </span>
        <el-tag
            class="ml-3"
            size="small"
            :type="detail.signStatus === 1 ? 'success' : detail.signStatus === 2 ? 'warning' : 'info'"
            :data-testid="`p5-sign-detail-tag-${detail.recordNo}`"
        >{{ detail.signStatusText || ('未知(' + detail.signStatus + ')') }}
        </el-tag>
      </div>
      <!-- 锁提示由后端给：按钮能不能按、为什么不能按，前端不自判状态（同一口径只维护一处） -->
      <div
          v-if="detail.signLockHint"
          data-testid="p5-sign-lock-hint"
          class="mb-3 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs leading-5 text-amber-900"
      >{{ detail.signLockHint }}
      </div>
      <el-table data-testid="p2-element-table" :data="detailElements" style="width: 100%" border max-height="50vh">
        <el-table-column prop="groupLabel" label="分组" width="140"/>
        <el-table-column prop="label" label="要素" width="140"/>
        <el-table-column label="是否已填" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.filled ? 'success' : 'danger'" size="small">{{ row.filled ? '已填' : '缺失' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="当前值" min-width="200">
          <template #default="{ row }">
            <span class="text-xs text-slate-700">{{ num(row.value) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="detailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>
