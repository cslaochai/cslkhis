<script setup lang="ts">
/**
 * 住院输血闭环（P4.4：申请 → 配血 → 发血 → 双人核对输注 → 完成 → 反应上报）
 *
 * 这个页面替代了原来的演示壳：它写死患者「马建军」「孙美玲」、输血单号「SX-2026-2201」、
 * 库存 126U、状态「输血中」—— 整页没有一个字来自数据库，而全库当时**一张输血表都没有**。
 * 按项目规范（AGENTS.md §2），这种页面必须重写为真实接口驱动。
 *
 * 九条口径：
 * 1. 状态机：0-待配血 → 1-已配血 → 2-已发血 → 3-输注中 → 4-已完成；0/1/2 → 5-已取消。
 * 2. **按钮可用性由后端给**（canCrossmatch / canIssue / canStart / canFinish / canCancel /
 *    canReportReaction），不按 transfusionStatus 码值 switch。
 * 3. **两个状态都要显示**：流程状态（走到哪一步）与配血状态（血配好没、合不合）。
 *    配血不合时流程会停在「待配血」，只显示流程状态就把"配了、但不合"看成"还没配"。
 * 4. **申请 ≠ 配血 ≠ 发血**：申请只登记"给谁、什么品种、多少袋、为什么"；
 *    血袋号与交叉配血结果是输血科的动作；双人核对与输注是护理岗的动作。
 * 5. **ABO / Rh 不相容会被后端整批拒绝** —— 直接展示后端文案，不要改写成"配血失败"。
 *    但「交叉配血结论不合」后端仍返回成功、数据会落库（消息里写明 N 袋不合不能发血），
 *    两者处理方式不同。
 * 6. **输注前必须双人核对**：两个护士不能是同一个人，1~6 项必核项一项不能缺。
 * 7. **完成才回写**：一次事务写 record_type=11 输血记录病历 + 病案首页 is_transfusion=1。
 *    列表的「病历号」就是这条链的证据，缺了就是链断了（后端会标红）。
 * 8. 有反应不改历史：完成之后用「上报反应」补登记，不要在输注中/未完成状态报。
 * 9. 所有 ID 都是字符串（雪花ID），不要 Number()。
 */
import { ref, computed, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Plus, Search, Warning, Delete } from '@element-plus/icons-vue'
import {
  getTransfusionApplyListPage,
  getTransfusionApplyDetail,
  saveTransfusionApply,
  crossmatchTransfusion,
  issueTransfusion,
  startTransfusion,
  finishTransfusion,
  reportTransfusionReaction,
  cancelTransfusion,
  getTransfusionUnfinishedCount,
  getBloodComponentList,
  getTransfusionCheckItems,
  getTransfusionReactionTypes,
  approveTransfusion,
} from '@/api/inpatientTransfusion'
import { getInpatientListPage } from '@/api/inpatient'
import { getEmployeeList } from '@/api/system'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'

interface AdmissionOption {
  admissionId: string
  admissionNo?: string
  patientName?: string
  patientNo?: string
  bedNo?: string
  wardName?: string
  deptName?: string
}

interface EmployeeOption {
  id: string
  empName?: string
  deptName?: string
}

interface CheckItemOption {
  code: number
  label: string
  required?: boolean
}

interface BagRow {
  id?: string
  bagNo?: string
  donorNo?: string
  bagAbo?: string
  bagRh?: string
  bloodComponent?: number
  bloodComponentText?: string
  spec?: string
  amount?: number
  amountUnit?: string
  sourceBank?: string
  collectDate?: string
  expireDate?: string
  crossmatchMain?: string
  crossmatchSide?: string
  crossmatchResult?: number
  crossmatchResultText?: string
  crossmatchTime?: string
  crossmatchDoctorName?: string
  bagStatus?: number
  bagStatusText?: string
  issueTime?: string
  expired?: boolean
  remark?: string
}

interface TransfusionRow {
  id: string
  applyNo?: string
  admissionId?: string
  admissionNo?: string
  admitStatus?: number
  admitStatusText?: string
  patientId?: string
  patientNo?: string
  patientName?: string
  genderText?: string
  age?: number
  applyDeptName?: string
  applyWardName?: string
  applyBedNo?: string
  applyDoctorName?: string
  applyTime?: string
  patientAbo?: string
  patientRh?: string
  bloodTypeText?: string
  aboMatchesArchive?: boolean
  bloodComponent?: number
  bloodComponentText?: string
  componentSpec?: string
  bagCount?: number
  matchedBagCount?: number
  bagProgressText?: string
  plannedAmount?: number
  amountUnit?: string
  transfusionPurpose?: string
  indication?: string
  preHb?: number
  preHct?: number
  prePlt?: number
  transfusionHistory?: string
  reactionHistory?: string
  pregnancyHistory?: string
  isEmergency?: number
  isEmergencyText?: string
  crossmatchStatus?: number
  crossmatchStatusText?: string
  crossmatchDoctorName?: string
  crossmatchTime?: string
  issueDoctorName?: string
  issueTime?: string
  checkItems?: string
  checkItemsText?: string
  checkNote?: string
  checkNurseName?: string
  checkNurse2Name?: string
  checkTime?: string
  infusionNurseName?: string
  infusionStartTime?: string
  infusionEndTime?: string
  actualAmount?: number
  infusionSpeed?: string
  observation?: string
  hasReaction?: number
  hasReactionText?: string
  reactionType?: string
  reactionDesc?: string
  reactionHandle?: string
  reactionReporterName?: string
  reactionTime?: string
  efficacyEval?: string
  postHb?: number
  postHct?: number
  postPlt?: number
  finishDoctorName?: string
  finishTime?: string
  recordId?: string
  recordNo?: string
  transfusionStatus?: number
  transfusionStatusText?: string
  cancelReason?: string
  cancelDoctorName?: string
  cancelTime?: string
  remark?: string
  durationMinutes?: number
  durationText?: string
  waitText?: string
  stalled?: boolean
  stalledText?: string
  canEdit?: boolean
  canCrossmatch?: boolean
  canIssue?: boolean
  canStart?: boolean
  canFinish?: boolean
  canCancel?: boolean
  canReportReaction?: boolean
  canApprove?: boolean
  amountMl?: number
  approveLevel?: number
  approveLevelText?: string
  approveStatus?: number
  approveStatusText?: string
  approveRejectReason?: string
  approveTime?: string
  approveMakeup?: number
  bags?: BagRow[]
}

const fmt = (v?: string) => (v ? String(v).replace('T', ' ') : '—')
const text = (v?: string | number) => (v === null || v === undefined || v === '' ? '—' : String(v))

const ABO_TYPES = ['A', 'B', 'O', 'AB']
const RH_TYPES = ['阳', '阴']
const AMOUNT_UNITS = ['U', 'ml', '治疗量']

// ---------------- 基础数据 ----------------

const admissions = ref<AdmissionOption[]>([])
const employees = ref<EmployeeOption[]>([])
const components = ref<{ code: number; label: string }[]>([])
const checkItemOptions = ref<CheckItemOption[]>([])
const reactionTypes = ref<string[]>([])

const admissionLabel = (a: AdmissionOption) =>
  `${a.bedNo || '—'} ${a.patientName || '—'}（${a.deptName || a.wardName || '—'}）`

const loadBaseData = async () => {
  try {
    const res = await getInpatientListPage({ admitStatus: 1, pageNum: 1, pageSize: 200 })
    admissions.value = (res.data?.records || []) as AdmissionOption[]
  } catch (error: any) {
    console.error('加载在院患者失败:', error)
  }
  try {
    const res = await getEmployeeList({})
    employees.value = (res.data || []) as EmployeeOption[]
  } catch (error: any) {
    console.error('加载员工失败:', error)
  }
  try {
    const res = await getBloodComponentList()
    components.value = (res.data || []) as { code: number; label: string }[]
  } catch (error: any) {
    console.error('加载血液品种字典失败:', error)
  }
  try {
    const res = await getTransfusionCheckItems()
    checkItemOptions.value = (res.data || []) as CheckItemOption[]
  } catch (error: any) {
    console.error('加载输血核对项失败:', error)
  }
  try {
    const res = await getTransfusionReactionTypes()
    reactionTypes.value = (res.data || []) as string[]
  } catch (error: any) {
    console.error('加载输血反应类型失败:', error)
  }
}

// ---------------- 列表 ----------------

const rows = ref<TransfusionRow[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(DEFAULT_PAGE_SIZE)
const loading = ref(false)
const unfinishedCount = ref(0)

const filters = reactive({
  admissionId: '',
  transfusionStatus: '' as number | '',
  crossmatchStatus: '' as number | '',
  bloodComponent: '' as number | '',
  patientAbo: '',
  hasReaction: '' as number | '',
  keyword: '',
})

const loadList = async () => {
  loading.value = true
  try {
    const res = await getTransfusionApplyListPage({
      admissionId: filters.admissionId || undefined,
      transfusionStatus: filters.transfusionStatus === '' ? undefined : filters.transfusionStatus,
      crossmatchStatus: filters.crossmatchStatus === '' ? undefined : filters.crossmatchStatus,
      bloodComponent: filters.bloodComponent === '' ? undefined : filters.bloodComponent,
      patientAbo: filters.patientAbo || undefined,
      hasReaction: filters.hasReaction === '' ? undefined : filters.hasReaction,
      keyword: filters.keyword || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    })
    rows.value = (res.data?.records || []) as TransfusionRow[]
    total.value = Number(res.data?.total || 0)
  } catch (error: any) {
    ElMessage.error(error.message || '加载用血申请失败')
  } finally {
    loading.value = false
  }
}

const loadUnfinishedCount = async () => {
  try {
    const res = await getTransfusionUnfinishedCount({})
    unfinishedCount.value = Number(res.data || 0)
  } catch (error: any) {
    console.error('加载未完成用血数失败:', error)
  }
}

/** 每单的进行位置（列表里一眼看出卡在哪一步）——纯后端文案拼装，前端不加业务判断 */
const stageText = (row: TransfusionRow) => {
  if (row.transfusionStatus === 0) {
    return row.crossmatchStatus === 3 ? '① 配血不合，等待换血源重配' : '① 等待输血科配血'
  }
  if (row.transfusionStatus === 1) return '② 已配血，等待发血'
  if (row.transfusionStatus === 2) return '③ 已发血，等待双人核对后输注'
  if (row.transfusionStatus === 3) return '④ 输注中，等待登记完成'
  if (row.transfusionStatus === 4) return '⑤ 已完成并回写'
  return '已取消'
}

/** 配血不合：必须能一眼看见（流程状态会停在"待配血"，单看状态会把不合看成没配） */
const hasIncompatible = (row: TransfusionRow) => row.crossmatchStatus === 3

/** 回写链断了：已完成却没有病历锚点 */
const chainBroken = (row: TransfusionRow) =>
  row.transfusionStatus === 4 && !row.recordId

const statusTagType = (status?: number) => {
  if (status === 4) return 'success'
  if (status === 5) return 'info'
  if (status === 3) return 'warning'
  if (status === 0) return 'danger'
  return 'primary'
}

const crossmatchTagType = (status?: number) => {
  if (status === 2) return 'success'
  if (status === 3) return 'danger'
  if (status === 1) return 'warning'
  return 'info'
}

/** 审批状态 tag：0待审批- danger 1已通过- success 2已驳回- info 3急诊待补审- warning */
const approveTagType = (status?: number) => {
  if (status === 1) return 'success'
  if (status === 2) return 'info'
  if (status === 3) return 'warning'
  if (status === 0) return 'danger'
  return 'info'
}

const handleSearch = () => {
  pageNum.value = 1
  loadList()
}

const resetFilters = () => {
  filters.admissionId = ''
  filters.transfusionStatus = ''
  filters.crossmatchStatus = ''
  filters.bloodComponent = ''
  filters.patientAbo = ''
  filters.hasReaction = ''
  filters.keyword = ''
  pageNum.value = 1
  loadList()
}

// ---------------- 一、发起 / 修改用血申请 ----------------

const applyVisible = ref(false)
const applySubmitting = ref(false)
const applyForm = reactive({
  id: '',
  admissionId: '',
  patientAbo: '',
  patientRh: '',
  bloodComponent: undefined as number | undefined,
  componentSpec: '',
  bagCount: 2,
  plannedAmount: undefined as number | undefined,
  amountUnit: '',
  transfusionPurpose: '',
  indication: '',
  preHb: undefined as number | undefined,
  preHct: undefined as number | undefined,
  prePlt: undefined as number | undefined,
  transfusionHistory: '',
  reactionHistory: '',
  pregnancyHistory: '',
  isEmergency: 0,
  remark: '',
})

const currentAdmission = computed(() =>
  admissions.value.find((a) => String(a.admissionId) === String(applyForm.admissionId)),
)

const currentComponentLabel = computed(
  () => components.value.find((c) => c.code === applyForm.bloodComponent)?.label || '—',
)

const openApply = (row?: TransfusionRow) => {
  if (row) {
    applyForm.id = row.id
    applyForm.admissionId = row.admissionId || ''
    applyForm.patientAbo = row.patientAbo || ''
    applyForm.patientRh = row.patientRh || ''
    applyForm.bloodComponent = row.bloodComponent
    applyForm.componentSpec = row.componentSpec || ''
    applyForm.bagCount = row.bagCount || 1
    applyForm.plannedAmount = row.plannedAmount
    applyForm.amountUnit = row.amountUnit || ''
    applyForm.transfusionPurpose = row.transfusionPurpose || ''
    applyForm.indication = row.indication || ''
    applyForm.preHb = row.preHb
    applyForm.preHct = row.preHct
    applyForm.prePlt = row.prePlt
    applyForm.transfusionHistory = row.transfusionHistory || ''
    applyForm.reactionHistory = row.reactionHistory || ''
    applyForm.pregnancyHistory = row.pregnancyHistory || ''
    applyForm.isEmergency = row.isEmergency ?? 0
    applyForm.remark = row.remark || ''
  } else {
    applyForm.id = ''
    applyForm.admissionId = filters.admissionId || ''
    applyForm.patientAbo = ''
    applyForm.patientRh = '阳'
    applyForm.bloodComponent = undefined
    applyForm.componentSpec = ''
    applyForm.bagCount = 2
    applyForm.plannedAmount = undefined
    applyForm.amountUnit = ''
    applyForm.transfusionPurpose = ''
    applyForm.indication = ''
    applyForm.preHb = undefined
    applyForm.preHct = undefined
    applyForm.prePlt = undefined
    applyForm.transfusionHistory = ''
    applyForm.reactionHistory = ''
    applyForm.pregnancyHistory = ''
    applyForm.isEmergency = 0
    applyForm.remark = ''
  }
  applyVisible.value = true
}

const submitApply = async () => {
  if (!applyForm.admissionId) {
    ElMessage.warning('请选择在院患者')
    return
  }
  if (!applyForm.patientAbo) {
    ElMessage.warning('请选择受血者 ABO 血型（以本次输血前鉴定为准）')
    return
  }
  if (!applyForm.patientRh) {
    ElMessage.warning('请选择受血者 Rh 血型（Rh 阴性属稀有血型，直接决定备血方案）')
    return
  }
  if (applyForm.bloodComponent === undefined) {
    ElMessage.warning('请选择血液品种')
    return
  }
  if (!applyForm.bagCount || applyForm.bagCount < 1) {
    ElMessage.warning('申请袋数必须大于 0')
    return
  }
  if (!applyForm.indication.trim()) {
    ElMessage.warning('请填写输血指征（无指征用血是飞行检查的重点）')
    return
  }
  applySubmitting.value = true
  try {
    const res = await saveTransfusionApply({
      id: applyForm.id || undefined,
      admissionId: applyForm.admissionId,
      patientAbo: applyForm.patientAbo,
      patientRh: applyForm.patientRh,
      bloodComponent: applyForm.bloodComponent,
      componentSpec: applyForm.componentSpec.trim() || undefined,
      bagCount: applyForm.bagCount,
      plannedAmount: applyForm.plannedAmount,
      amountUnit: applyForm.amountUnit.trim() || undefined,
      transfusionPurpose: applyForm.transfusionPurpose.trim() || undefined,
      indication: applyForm.indication.trim(),
      preHb: applyForm.preHb,
      preHct: applyForm.preHct,
      prePlt: applyForm.prePlt,
      transfusionHistory: applyForm.transfusionHistory.trim() || undefined,
      reactionHistory: applyForm.reactionHistory.trim() || undefined,
      pregnancyHistory: applyForm.pregnancyHistory.trim() || undefined,
      isEmergency: applyForm.isEmergency,
      remark: applyForm.remark.trim() || undefined,
    })
    ElMessage.success(
      `${applyForm.id ? '用血申请已修改' : '用血申请已提交'}：${res.data || ''}（等待输血科配血）`,
    )
    applyVisible.value = false
    await Promise.all([loadList(), loadUnfinishedCount()])
  } catch (error: any) {
    ElMessage.error(error.message || '用血申请提交失败')
  } finally {
    applySubmitting.value = false
  }
}

// ---------------- 一·五、用血分级审批（sql/93） ----------------

const approveVisible = ref(false)
const approveSubmitting = ref(false)
const approveTarget = ref<TransfusionRow | null>(null)
const approveForm = reactive<{ approveResult: number | null; opinion: string }>({
  approveResult: null,
  opinion: '',
})

const openApprove = (row: TransfusionRow) => {
  approveTarget.value = row
  approveForm.approveResult = null
  approveForm.opinion = ''
  approveVisible.value = true
}

const submitApprove = async () => {
  if (!approveTarget.value) return
  if (!approveForm.approveResult) {
    ElMessage.warning('请选择审批结论（通过 / 驳回）')
    return
  }
  if (approveForm.approveResult === 2 && !approveForm.opinion.trim()) {
    ElMessage.warning('驳回必须写明原因（申请人要知道改什么才能重新提交）')
    return
  }
  approveSubmitting.value = true
  try {
    const res = await approveTransfusion({
      applyId: approveTarget.value.id,
      approveResult: approveForm.approveResult,
      opinion: approveForm.opinion.trim() || undefined,
    })
    ElMessage.success(res.message || '审批结论已提交')
    approveVisible.value = false
    await Promise.all([loadList(), loadUnfinishedCount()])
  } catch (error: any) {
    ElMessage.error(error.message || '审批提交失败')
  } finally {
    approveSubmitting.value = false
  }
}

// ---------------- 二、配血（逐袋） ----------------

const crossVisible = ref(false)
const crossSubmitting = ref(false)
const crossTarget = ref<TransfusionRow | null>(null)
const crossNote = ref('')
const bagRows = ref<any[]>([])

const blankBag = () => ({
  bagNo: '',
  donorNo: '',
  bagAbo: '',
  bagRh: '',
  spec: crossTarget.value?.componentSpec || '',
  amount: undefined as number | undefined,
  amountUnit: crossTarget.value?.amountUnit || '',
  sourceBank: '',
  collectDate: '',
  expireDate: '',
  crossmatchMain: '阴性',
  crossmatchSide: '阴性',
  crossmatchResult: 1,
  remark: '',
})

const openCrossmatch = (row: TransfusionRow) => {
  crossTarget.value = row
  crossNote.value = ''
  // 按申请袋数预生成行：少配一袋后端会拒（"未配齐不可发血"），
  // 但重复配同一袋后端会 update，所以预生成行不影响重配场景
  const need = Math.max(1, row.bagCount || 1)
  bagRows.value = Array.from({ length: need }, () => blankBag())
  crossVisible.value = true
}

const addBagRow = () => {
  bagRows.value.push(blankBag())
}

const removeBagRow = (idx: number) => {
  if (bagRows.value.length <= 1) {
    ElMessage.warning('至少保留一袋')
    return
  }
  bagRows.value.splice(idx, 1)
}

const submitCrossmatch = async () => {
  if (!crossTarget.value) return
  for (let i = 0; i < bagRows.value.length; i++) {
    const b = bagRows.value[i]
    if (!b.bagNo || !String(b.bagNo).trim()) {
      ElMessage.warning(`第 ${i + 1} 袋：血袋号不能为空`)
      return
    }
    if (!b.bagAbo) {
      ElMessage.warning(`第 ${i + 1} 袋：请选择血袋 ABO 血型`)
      return
    }
    if (!b.bagRh) {
      ElMessage.warning(`第 ${i + 1} 袋：请选择血袋 Rh 血型`)
      return
    }
    if (!b.expireDate) {
      ElMessage.warning(`第 ${i + 1} 袋：请填写有效期（超期血袋不得输注）`)
      return
    }
    if (!b.crossmatchResult) {
      ElMessage.warning(`第 ${i + 1} 袋：请选择配血结论`)
      return
    }
  }
  crossSubmitting.value = true
  try {
    const res = await crossmatchTransfusion({
      applyId: crossTarget.value.id,
      crossmatchNote: crossNote.value.trim() || undefined,
      bags: bagRows.value.map((b) => ({
        bagNo: String(b.bagNo).trim(),
        donorNo: b.donorNo?.trim() || undefined,
        bagAbo: b.bagAbo,
        bagRh: b.bagRh,
        spec: b.spec?.trim() || undefined,
        amount: b.amount,
        amountUnit: b.amountUnit?.trim() || undefined,
        sourceBank: b.sourceBank?.trim() || undefined,
        collectDate: b.collectDate || undefined,
        expireDate: b.expireDate || undefined,
        crossmatchMain: b.crossmatchMain || undefined,
        crossmatchSide: b.crossmatchSide || undefined,
        crossmatchResult: b.crossmatchResult,
        remark: b.remark?.trim() || undefined,
      })),
    })
    // 配血结论在**响应 message**里（不是 data）：后端 crossmatch 返回的是"结果说明"
    // —— 全部相合 / N 袋不合（仍是 200）/ 只配了 x/n 袋。读 res.data 会拿到 null，
    // 于是所有配血结果都显示成兜底文案，护士看不出到底配成没有。
    const msg = String(res.message || '配血结果已录入')
    if (msg.includes('不合')) {
      ElMessage.warning(msg)
    } else {
      ElMessage.success(msg)
    }
    crossVisible.value = false
    await Promise.all([loadList(), loadUnfinishedCount()])
  } catch (error: any) {
    ElMessage.error(error.message || '配血失败')
  } finally {
    crossSubmitting.value = false
  }
}

// ---------------- 三、发血 ----------------

const issueVisible = ref(false)
const issueSubmitting = ref(false)
const issueTarget = ref<TransfusionRow | null>(null)
const issueForm = reactive({ issueRemark: '' })

const openIssue = (row: TransfusionRow) => {
  issueTarget.value = row
  issueForm.issueRemark = ''
  issueVisible.value = true
}

const submitIssue = async () => {
  if (!issueTarget.value) return
  issueSubmitting.value = true
  try {
    await issueTransfusion({
      applyId: issueTarget.value.id,
      issueRemark: issueForm.issueRemark.trim() || undefined,
    })
    ElMessage.success('已发血，请病区双人核对后输注')
    issueVisible.value = false
    await Promise.all([loadList(), loadUnfinishedCount()])
  } catch (error: any) {
    ElMessage.error(error.message || '发血失败')
  } finally {
    issueSubmitting.value = false
  }
}

// ---------------- 四、开始输注（双人核对） ----------------

const startVisible = ref(false)
const startSubmitting = ref(false)
const startTarget = ref<TransfusionRow | null>(null)
const startForm = reactive({
  items: [] as number[],
  checkNote: '',
  checkNurseId: '',
  checkNurse2Id: '',
  infusionNurseId: '',
  infusionStartTime: '',
  infusionSpeed: '',
  observation: '',
})

const openStart = (row: TransfusionRow) => {
  startTarget.value = row
  startForm.items = []
  startForm.checkNote = ''
  startForm.checkNurseId = ''
  startForm.checkNurse2Id = ''
  startForm.infusionNurseId = ''
  startForm.infusionStartTime = ''
  startForm.infusionSpeed = ''
  startForm.observation = ''
  startVisible.value = true
}

const submitStart = async () => {
  if (!startTarget.value) return
  const missing = checkItemOptions.value
    .filter((i) => i.required && !startForm.items.includes(i.code))
    .map((i) => i.label)
  if (missing.length > 0) {
    ElMessage.warning(`输血前核对必核项未完成：${missing.join('；')}`)
    return
  }
  if (!startForm.checkNurseId || !startForm.checkNurse2Id) {
    ElMessage.warning('请选择两名核对护士（输血必须双人核对）')
    return
  }
  if (startForm.checkNurseId === startForm.checkNurse2Id) {
    ElMessage.warning('两名核对护士不能是同一个人（写同一个人等于没有双人核对）')
    return
  }
  if (!startForm.infusionStartTime) {
    ElMessage.warning('请选择输注开始时间')
    return
  }
  startSubmitting.value = true
  try {
    await startTransfusion({
      applyId: startTarget.value.id,
      checkItems: startForm.items.join(','),
      checkNote: startForm.checkNote.trim() || undefined,
      checkNurseId: startForm.checkNurseId,
      checkNurse2Id: startForm.checkNurse2Id,
      infusionNurseId: startForm.infusionNurseId || undefined,
      infusionStartTime: startForm.infusionStartTime,
      infusionSpeed: startForm.infusionSpeed.trim() || undefined,
      observation: startForm.observation.trim() || undefined,
    })
    ElMessage.success('已开始输注（双人核对完成）')
    startVisible.value = false
    await Promise.all([loadList(), loadUnfinishedCount()])
  } catch (error: any) {
    ElMessage.error(error.message || '开始输注失败')
  } finally {
    startSubmitting.value = false
  }
}

// ---------------- 五、完成（回写病历） ----------------

const finishVisible = ref(false)
const finishSubmitting = ref(false)
const finishTarget = ref<TransfusionRow | null>(null)
const finishForm = reactive({
  infusionEndTime: '',
  actualAmount: undefined as number | undefined,
  observation: '',
  efficacyEval: '',
  postHb: undefined as number | undefined,
  postHct: undefined as number | undefined,
  postPlt: undefined as number | undefined,
})

const openFinish = (row: TransfusionRow) => {
  finishTarget.value = row
  finishForm.infusionEndTime = ''
  finishForm.actualAmount = row.plannedAmount
  finishForm.observation = row.observation || ''
  finishForm.efficacyEval = ''
  finishForm.postHb = undefined
  finishForm.postHct = undefined
  finishForm.postPlt = undefined
  finishVisible.value = true
}

const submitFinish = async () => {
  if (!finishTarget.value) return
  if (!finishForm.infusionEndTime) {
    ElMessage.warning('请选择输注结束时间')
    return
  }
  if (!finishForm.actualAmount || finishForm.actualAmount <= 0) {
    ElMessage.warning('实际输注量必须大于 0')
    return
  }
  if (!finishForm.observation.trim()) {
    ElMessage.warning('请填写输注过程观察（开始后 15 分钟是反应高发期）')
    return
  }
  finishSubmitting.value = true
  try {
    await finishTransfusion({
      applyId: finishTarget.value.id,
      infusionEndTime: finishForm.infusionEndTime,
      actualAmount: finishForm.actualAmount,
      observation: finishForm.observation.trim(),
      efficacyEval: finishForm.efficacyEval.trim() || undefined,
      postHb: finishForm.postHb,
      postHct: finishForm.postHct,
      postPlt: finishForm.postPlt,
    })
    ElMessage.success('输血已完成（已回写输血记录病历与病案首页是否输血标志）')
    finishVisible.value = false
    await Promise.all([loadList(), loadUnfinishedCount()])
  } catch (error: any) {
    ElMessage.error(error.message || '登记完成失败')
  } finally {
    finishSubmitting.value = false
  }
}

// ---------------- 六、输血反应上报 ----------------

const reactionVisible = ref(false)
const reactionSubmitting = ref(false)
const reactionTarget = ref<TransfusionRow | null>(null)
const reactionForm = reactive({
  reactionType: '',
  reactionDesc: '',
  reactionHandle: '',
})

const openReaction = (row: TransfusionRow) => {
  reactionTarget.value = row
  reactionForm.reactionType = ''
  reactionForm.reactionDesc = ''
  reactionForm.reactionHandle = ''
  reactionVisible.value = true
}

const submitReaction = async () => {
  if (!reactionTarget.value) return
  if (!reactionForm.reactionType) {
    ElMessage.warning('请选择输血反应类型')
    return
  }
  if (!reactionForm.reactionDesc.trim()) {
    ElMessage.warning('请填写反应描述')
    return
  }
  if (!reactionForm.reactionHandle.trim()) {
    ElMessage.warning('请填写处理措施')
    return
  }
  reactionSubmitting.value = true
  try {
    await reportTransfusionReaction({
      applyId: reactionTarget.value.id,
      reactionType: reactionForm.reactionType,
      reactionDesc: reactionForm.reactionDesc.trim(),
      reactionHandle: reactionForm.reactionHandle.trim(),
    })
    ElMessage.success('输血反应已上报')
    reactionVisible.value = false
    await loadList()
  } catch (error: any) {
    ElMessage.error(error.message || '输血反应上报失败')
  } finally {
    reactionSubmitting.value = false
  }
}

// ---------------- 取消 ----------------

const handleCancel = async (row: TransfusionRow) => {
  try {
    const { value } = await ElMessageBox.prompt(
      `取消用血单 ${row.applyNo}（${text(row.patientName)} ${text(row.bloodComponentText)}）请填写原因。`,
      '取消用血申请',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '返回',
        inputPlaceholder: '如：患者转院 / 输血指征复查后不需要 / 血源不足改期',
        inputValidator: (v: string) => (v && v.trim() ? true : '取消原因不能为空'),
      },
    )
    await cancelTransfusion({ applyId: row.id, cancelReason: String(value).trim() })
    ElMessage.success('用血申请已取消')
    await Promise.all([loadList(), loadUnfinishedCount()])
  } catch (error: any) {
    if (error === 'cancel' || error?.message === 'cancel') return
    ElMessage.error(error.message || '取消失败')
  }
}

// ---------------- 详情 ----------------

const detailVisible = ref(false)
const detail = ref<TransfusionRow | null>(null)

const openDetail = async (row: TransfusionRow) => {
  try {
    const res = await getTransfusionApplyDetail(row.id)
    detail.value = res.data as TransfusionRow
    detailVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '加载详情失败')
  }
}

onMounted(() => {
  loadBaseData()
  loadList()
  loadUnfinishedCount()
})
</script>

<template>
  <div class="space-y-6">
    <!-- 标题 + 在院患者过滤 -->
    <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-xl font-semibold text-slate-900">输血管理</h1>
        <p class="mt-1 text-sm text-slate-500">
          住院输血闭环：用血申请 → 配血（血型鉴定 + 交叉配血）→ 发血 → 双人核对输注 → 完成回写输血记录。
          未配血不可发血、未发血不可输注、输注中与已完成不可取消。
        </p>
      </div>
      <div class="flex items-center gap-3">
        <el-select
          v-model="filters.admissionId"
          data-testid="p4-tf-admission"
          placeholder="按在院患者过滤"
          filterable
          clearable
          class="!w-72"
          @change="handleSearch"
        >
          <el-option
            v-for="a in admissions"
            :key="a.admissionId"
            :label="admissionLabel(a)"
            :value="String(a.admissionId)"
          />
        </el-select>
        <el-button v-perm="'medtech:transfusion:add'" type="primary" :icon="Plus" data-testid="p4-tf-apply" @click="openApply()">
          发起用血申请
        </el-button>
        <el-button :icon="Refresh" @click="handleSearch">刷新</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-2 gap-4 lg:grid-cols-3">
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">未完成用血</p>
        <p class="text-lg font-bold text-slate-900" data-testid="p4-tf-unfinished">{{ unfinishedCount }}</p>
        <p class="text-[11px] text-slate-400">待配血 + 已配血 + 已发血 + 输注中</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">当前筛选结果</p>
        <p class="text-lg font-bold text-slate-900" data-testid="p4-tf-total">{{ total }}</p>
        <p class="text-[11px] text-slate-400">共 {{ total }} 条用血申请</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">本页配血不合 / 回写链不完整</p>
        <p
          class="text-lg font-bold"
          :class="
            rows.filter((r) => hasIncompatible(r) || chainBroken(r)).length > 0
              ? 'text-red-600'
              : 'text-slate-900'
          "
          data-testid="p4-tf-abnormal"
        >
          {{ rows.filter((r) => hasIncompatible(r) || chainBroken(r)).length }}
        </p>
        <p class="text-[11px] text-slate-400">配血不合不可发血；已完成必须有病历号</p>
      </div>
    </div>

    <!-- 筛选 + 表格 -->
    <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div class="mb-4 flex flex-wrap items-center gap-3">
        <el-select
          v-model="filters.transfusionStatus"
          data-testid="p4-tf-filter-status"
          placeholder="流程状态"
          clearable
          class="!w-36"
          @change="handleSearch"
        >
          <el-option label="待配血" :value="0" />
          <el-option label="已配血" :value="1" />
          <el-option label="已发血" :value="2" />
          <el-option label="输注中" :value="3" />
          <el-option label="已完成" :value="4" />
          <el-option label="已取消" :value="5" />
        </el-select>
        <el-select
          v-model="filters.crossmatchStatus"
          data-testid="p4-tf-filter-crossmatch"
          placeholder="配血状态"
          clearable
          class="!w-40"
          @change="handleSearch"
        >
          <el-option label="待配血" :value="0" />
          <el-option label="配血中（未配齐）" :value="1" />
          <el-option label="全部相合" :value="2" />
          <el-option label="存在配血不合" :value="3" />
        </el-select>
        <el-select
          v-model="filters.bloodComponent"
          data-testid="p4-tf-filter-component"
          placeholder="血液品种"
          clearable
          class="!w-36"
          @change="handleSearch"
        >
          <el-option v-for="c in components" :key="c.code" :label="c.label" :value="c.code" />
        </el-select>
        <el-select
          v-model="filters.patientAbo"
          data-testid="p4-tf-filter-abo"
          placeholder="受血者血型"
          clearable
          class="!w-32"
          @change="handleSearch"
        >
          <el-option v-for="t in ABO_TYPES" :key="t" :label="`${t} 型`" :value="t" />
        </el-select>
        <el-select
          v-model="filters.hasReaction"
          data-testid="p4-tf-filter-reaction"
          placeholder="输血反应"
          clearable
          class="!w-36"
          @change="handleSearch"
        >
          <el-option label="未上报反应" :value="0" />
          <el-option label="已上报有反应" :value="1" />
        </el-select>
        <el-input
          v-model="filters.keyword"
          placeholder="输血单号 / 入院号 / 患者 / 目的 / 指征"
          clearable
          class="!w-72"
          :prefix-icon="Search"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" style="width: 100%" data-testid="p4-tf-table">
        <el-table-column prop="applyNo" label="用血单号" width="150" />
        <el-table-column label="患者" min-width="140">
          <template #default="{ row }">
            <div class="text-slate-900">{{ text(row.patientName) }}</div>
            <div class="text-[11px] text-slate-400">{{ text(row.admissionNo) }}</div>
            <div class="text-[11px] text-slate-400">
              {{ text(row.applyBedNo) }}床 · {{ text(row.applyDeptName) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="血型 / 品种" min-width="180">
          <template #default="{ row }">
            <div class="font-medium text-slate-900">{{ text(row.bloodTypeText) }}</div>
            <div class="text-[11px] text-slate-600">
              {{ text(row.bloodComponentText) }}
              <span v-if="row.componentSpec" class="text-slate-400">（{{ row.componentSpec }}）</span>
            </div>
            <div class="text-[11px] text-slate-400">
              申请 {{ text(row.bagCount) }} 袋
              <span v-if="row.plannedAmount">
                / {{ row.plannedAmount }}{{ row.amountUnit || '' }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="用血审批" min-width="150">
          <template #default="{ row }">
            <el-tag
              :type="approveTagType(row.approveStatus)"
              size="small"
              effect="plain"
              data-testid="p4-tf-approve-status"
            >
              {{ text(row.approveStatusText) }}
            </el-tag>
            <div class="mt-1 text-[11px] text-slate-500">
              {{ text(row.approveLevelText) }}
              <span v-if="row.amountMl != null">· {{ row.amountMl }}ml</span>
            </div>
            <div v-if="row.approveMakeup === 1" class="text-[11px] text-amber-600">急诊后补</div>
            <div v-if="row.approveRejectReason" class="text-[11px] text-red-600">
              驳回：{{ row.approveRejectReason }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="配血" min-width="170">
          <template #default="{ row }">
            <el-tag :type="crossmatchTagType(row.crossmatchStatus)" size="small" effect="plain">
              {{ text(row.crossmatchStatusText) }}
            </el-tag>
            <div class="mt-1 text-[11px] text-slate-500">{{ text(row.bagProgressText) }}</div>
            <div v-if="row.crossmatchDoctorName" class="text-[11px] text-slate-400">
              由 {{ row.crossmatchDoctorName }} 于 {{ fmt(row.crossmatchTime) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态 / 进度" min-width="190">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.transfusionStatus)" size="small" effect="plain">
              {{ text(row.transfusionStatusText) }}
            </el-tag>
            <div class="mt-1 text-[11px] text-slate-500">{{ stageText(row) }}</div>
            <div v-if="row.waitText && row.transfusionStatus !== 4" class="text-[11px] text-slate-400">
              {{ row.waitText }}
            </div>
            <div v-if="row.stalled" class="mt-1 text-[11px] font-medium text-red-600">
              <el-icon class="mr-1 align-middle"><Warning /></el-icon>{{ text(row.stalledText) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="双人核对 / 输注" min-width="180">
          <template #default="{ row }">
            <div v-if="!row.checkNurseName" class="text-[11px] text-slate-400">—（尚未核对）</div>
            <template v-else>
              <div class="text-[11px] text-slate-700">
                {{ row.checkNurseName }} / {{ text(row.checkNurse2Name) }}
              </div>
              <div class="text-[11px] text-slate-400">{{ fmt(row.checkTime) }}</div>
              <div v-if="row.actualAmount" class="text-[11px] text-slate-500">
                实输 {{ row.actualAmount }}{{ row.amountUnit || '' }}
                <span v-if="row.durationText">（{{ row.durationText }}）</span>
              </div>
            </template>
          </template>
        </el-table-column>
        <el-table-column label="反应 / 回写病历" min-width="170">
          <template #default="{ row }">
            <div v-if="row.hasReaction === 1" class="text-[11px] font-medium text-red-600">
              <el-icon class="mr-1 align-middle"><Warning /></el-icon>{{ text(row.reactionType) }}
            </div>
            <div v-else class="text-[11px] text-slate-400">{{ text(row.hasReactionText) }}</div>
            <div v-if="row.recordNo" class="mt-1 text-[11px] text-slate-700">
              病历号 {{ row.recordNo }}
            </div>
            <div v-else-if="row.transfusionStatus === 4" class="mt-1 text-[11px] text-red-600">
              回写链不完整（无病历号）
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.canApprove"
              v-perm="'medtech:transfusion:edit'"
              type="success"
              link
              data-testid="p4-tf-approve"
              @click="openApprove(row)"
            >
              审批
            </el-button>
            <el-button
              v-if="row.canCrossmatch"
              v-perm="'medtech:transfusion:edit'"
              type="warning"
              link
              data-testid="p4-tf-crossmatch"
              @click="openCrossmatch(row)"
            >
              配血
            </el-button>
            <el-button
              v-if="row.canIssue"
              v-perm="'medtech:transfusion:edit'"
              type="primary"
              link
              data-testid="p4-tf-issue"
              @click="openIssue(row)"
            >
              发血
            </el-button>
            <el-button
              v-if="row.canStart"
              v-perm="'medtech:transfusion:edit'"
              type="primary"
              link
              data-testid="p4-tf-start"
              @click="openStart(row)"
            >
              开始输注
            </el-button>
            <el-button
              v-if="row.canFinish"
              v-perm="'medtech:transfusion:edit'"
              type="success"
              link
              data-testid="p4-tf-finish"
              @click="openFinish(row)"
            >
              登记完成
            </el-button>
            <el-button
              v-if="row.canReportReaction"
              v-perm="'medtech:transfusion:add'"
              type="danger"
              link
              data-testid="p4-tf-reaction"
              @click="openReaction(row)"
            >
              上报反应
            </el-button>
            <el-button
              v-if="row.canEdit"
              v-perm="'medtech:transfusion:edit'"
              type="primary"
              link
              data-testid="p4-tf-edit"
              @click="openApply(row)"
            >
              修改
            </el-button>
            <el-button
              v-if="row.canCancel"
              v-perm="'medtech:transfusion:delete'"
              type="danger"
              link
              data-testid="p4-tf-cancel"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
            <el-button type="info" link data-testid="p4-tf-detail" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-6 text-sm text-slate-400" data-testid="p4-tf-empty">
            暂无用血申请（点击右上角「发起用血申请」开始）
          </div>
        </template>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="PAGE_SIZES"
          layout="total, sizes, prev, pager, next"
          @current-change="loadList"
          @size-change="handleSearch"
        />
      </div>
    </div>

    <!-- 发起 / 修改用血申请 -->
    <el-dialog
      v-model="applyVisible"
      :title="applyForm.id ? '修改用血申请' : '发起用血申请'"
      width="760px"
      data-testid="p4-tf-apply-dialog"
    >
      <el-form label-width="120px">
        <el-form-item label="在院患者" required>
          <el-select
            v-model="applyForm.admissionId"
            data-testid="p4-tf-apply-admission"
            placeholder="选择在院患者"
            filterable
            class="!w-full"
            :disabled="!!applyForm.id"
          >
            <el-option
              v-for="a in admissions"
              :key="a.admissionId"
              :label="admissionLabel(a)"
              :value="String(a.admissionId)"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="currentAdmission" label="申请科室">
          <span class="text-slate-700">
            {{ text(currentAdmission.deptName) }} / {{ text(currentAdmission.wardName) }} /
            {{ text(currentAdmission.bedNo) }}床
          </span>
          <span class="ml-2 text-[11px] text-slate-400">申请科室取患者当前科室，服务端写入，不需要填</span>
        </el-form-item>
        <el-form-item label="受血者血型" required>
          <div class="flex items-center gap-3">
            <el-select
              v-model="applyForm.patientAbo"
              data-testid="p4-tf-apply-abo"
              placeholder="ABO"
              class="!w-32"
            >
              <el-option v-for="t in ABO_TYPES" :key="t" :label="`${t} 型`" :value="t" />
            </el-select>
            <el-select
              v-model="applyForm.patientRh"
              data-testid="p4-tf-apply-rh"
              placeholder="Rh"
              class="!w-32"
            >
              <el-option v-for="t in RH_TYPES" :key="t" :label="`Rh ${t}性`" :value="t" />
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="血液品种" required>
          <el-select
            v-model="applyForm.bloodComponent"
            data-testid="p4-tf-apply-component"
            placeholder="选择血液品种"
            class="!w-full"
          >
            <el-option v-for="c in components" :key="c.code" :label="c.label" :value="c.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="规格">
          <el-input
            v-model="applyForm.componentSpec"
            placeholder="如 1.5U / 200ml / 1治疗量（可空）"
            maxlength="64"
          />
        </el-form-item>
        <el-form-item label="申请袋数" required>
          <el-input
            v-model.number="applyForm.bagCount"
            data-testid="p4-tf-apply-bagcount"
            type="number"
            placeholder="配血累计不得超过此数"
            class="!w-40"
          />
          <span class="ml-3 text-[11px] text-slate-400">本次申请最多 20 袋</span>
        </el-form-item>
        <el-form-item label="申请总量 / 单位">
          <div class="flex items-center gap-3">
            <el-input
              v-model.number="applyForm.plannedAmount"
              placeholder="总量"
              class="!w-40"
            />
            <el-select v-model="applyForm.amountUnit" placeholder="单位" clearable class="!w-32">
              <el-option v-for="u in AMOUNT_UNITS" :key="u" :label="u" :value="u" />
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="输血目的">
          <el-input
            v-model="applyForm.transfusionPurpose"
            placeholder="如：纠正贫血 / 补充凝血因子 / 提升血小板（可空）"
            maxlength="200"
          />
        </el-form-item>
        <el-form-item label="输血指征" required>
          <el-input
            v-model="applyForm.indication"
            data-testid="p4-tf-apply-indication"
            type="textarea"
            :rows="3"
            placeholder="指标 + 症状（如：Hb 62 g/L，活动后心悸气促）——无指征用血是飞行检查的重点"
          />
        </el-form-item>
        <el-form-item label="输血前 Hb">
          <div class="flex items-center gap-3">
            <el-input v-model.number="applyForm.preHb" placeholder="Hb g/L" class="!w-40" />
            <el-input v-model.number="applyForm.preHct" placeholder="HCT %" class="!w-40" />
            <el-input v-model.number="applyForm.prePlt" placeholder="PLT ×10⁹/L" class="!w-48" />
          </div>
        </el-form-item>
        <el-form-item label="既往输血史">
          <el-input v-model="applyForm.transfusionHistory" placeholder="可空" maxlength="500" />
        </el-form-item>
        <el-form-item label="既往反应史">
          <el-input v-model="applyForm.reactionHistory" placeholder="有输血反应史会影响选血与备血方案" maxlength="500" />
        </el-form-item>
        <el-form-item label="妊娠史">
          <el-input v-model="applyForm.pregnancyHistory" placeholder="育龄女性填写（与不规则抗体产生相关）" maxlength="200" />
        </el-form-item>
        <el-form-item label="紧急用血">
          <el-radio-group v-model="applyForm.isEmergency">
            <el-radio :value="0">常规</el-radio>
            <el-radio :value="1">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="applyForm.remark" type="textarea" :rows="2" placeholder="可空" />
        </el-form-item>
      </el-form>
      <div class="rounded border border-amber-200 bg-amber-50 px-3 py-2 text-[12px] leading-5 text-amber-700">
        <el-icon class="mr-1 align-middle"><Warning /></el-icon>
        提交后血<b>尚未配</b>：血袋号与交叉配血结果由输血科在「配血」里逐袋录入。
        血型以本次输血前鉴定为准 —— 与患者档案不一致时系统会在备注里提示，但不阻断（档案常是旧的）。
      </div>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button v-perm="['medtech:transfusion:add','medtech:transfusion:edit']" type="primary" :loading="applySubmitting" data-testid="p4-tf-apply-submit" @click="submitApply">
          {{ applyForm.id ? '保存修改' : '提交用血申请' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 用血分级审批 -->
    <el-dialog v-model="approveVisible" title="用血分级审批" width="520px" data-testid="p4-tf-approve-dialog">
      <div v-if="approveTarget" class="mb-3 text-xs text-slate-600 space-y-1">
        <div>
          单号 <b>{{ approveTarget.applyNo }}</b> ·
          {{ text(approveTarget.patientName) }} ·
          {{ text(approveTarget.bloodComponentText) }} {{ text(approveTarget.bagCount) }} 袋
        </div>
        <div>
          折算量 <b>{{ approveTarget.amountMl != null ? approveTarget.amountMl + 'ml' : '待折算（按最高级审批）' }}</b>
          · 属「{{ text(approveTarget.approveLevelText) }}」审核签发
          <el-tag v-if="approveTarget.approveStatus === 3" size="small" type="warning" effect="plain" class="ml-1">急诊补审</el-tag>
        </div>
        <div v-if="approveTarget.approveRejectReason" class="text-red-600">
          上次驳回原因：{{ approveTarget.approveRejectReason }}
        </div>
      </div>
      <el-form label-width="72px">
        <el-form-item label="审批结论" required>
          <el-radio-group v-model="approveForm.approveResult" data-testid="p4-tf-approve-result">
            <el-radio :value="1" data-testid="p4-tf-approve-result-1">通过（可进入配血）</el-radio>
            <el-radio :value="2" data-testid="p4-tf-approve-result-2">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input
            v-model="approveForm.opinion"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            :placeholder="approveForm.approveResult === 2 ? '驳回原因（必填）：写明申请人要改什么' : '可空；同意意见或备注'"
            data-testid="p4-tf-approve-opinion"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button v-perm="'medtech:transfusion:edit'" type="primary" :loading="approveSubmitting" data-testid="p4-tf-approve-submit" @click="submitApprove">
          提交审批结论
        </el-button>
      </template>
    </el-dialog>

    <!-- 配血 -->
    <el-dialog v-model="crossVisible" title="配血（血型鉴定 + 交叉配血）" width="1080px" data-testid="p4-tf-cross-dialog">
      <div v-if="crossTarget" class="space-y-3">
        <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm">
          <div class="text-slate-900">
            {{ text(crossTarget.patientName) }}（{{ text(crossTarget.admissionNo) }}）
            <el-tag v-if="crossTarget.isEmergency === 1" type="danger" size="small" class="ml-2">紧急</el-tag>
          </div>
          <div class="mt-1 text-slate-700">
            受血者 {{ text(crossTarget.bloodTypeText) }} ·
            {{ text(crossTarget.bloodComponentText) }}（{{ text(crossTarget.componentSpec) }}）·
            申请 {{ text(crossTarget.bagCount) }} 袋
          </div>
          <div class="text-[12px] text-slate-500">输血指征：{{ text(crossTarget.indication) }}</div>
        </div>
        <el-alert type="warning" :closable="false" show-icon>
          红细胞类按红细胞规则判 ABO 相容（A←A/O、B←B/O、AB←AB/A/B/O、O←O），
          血浆类方向相反（A←A/AB、B←B/AB、AB←AB、O←全）；受血者 Rh 阴性时血袋必须 Rh 阴性。
          不相容会被后端<b>直接拒绝</b>（ABO 不相容输注是致死性差错）。
          血袋号全局唯一：一袋血只能给一个人。
        </el-alert>
        <div class="flex items-center justify-between">
          <span class="text-[12px] text-slate-500">
            本次录入 {{ bagRows.length }} 袋；已配 {{ crossTarget.matchedBagCount || 0 }}/{{ crossTarget.bagCount }} 袋。
            可分多批提交（血站分批到货是常态），累计不得超过申请袋数。
          </span>
          <el-button v-perm="'medtech:transfusion:add'" :icon="Plus" size="small" @click="addBagRow">增加一袋</el-button>
        </div>
        <div v-for="(b, idx) in bagRows" :key="idx" class="rounded border border-slate-200 p-3">
          <div class="mb-2 flex items-center justify-between">
            <span class="text-[13px] font-medium text-slate-700">第 {{ idx + 1 }} 袋</span>
            <el-button v-perm="'medtech:transfusion:delete'" type="danger" link :icon="Delete" @click="removeBagRow(idx)">删除</el-button>
          </div>
          <div class="grid grid-cols-2 gap-3 lg:grid-cols-4">
            <el-input v-model="b.bagNo" :data-testid="`p4-tf-bag-no-${idx}`" placeholder="血袋号 *" />
            <el-input v-model="b.donorNo" placeholder="献血编号" />
            <el-select v-model="b.bagAbo" :data-testid="`p4-tf-bag-abo-${idx}`" placeholder="血袋 ABO *">
              <el-option v-for="t in ABO_TYPES" :key="t" :label="`${t} 型`" :value="t" />
            </el-select>
            <el-select v-model="b.bagRh" :data-testid="`p4-tf-bag-rh-${idx}`" placeholder="血袋 Rh *">
              <el-option v-for="t in RH_TYPES" :key="t" :label="`Rh ${t}性`" :value="t" />
            </el-select>
            <el-input v-model="b.spec" placeholder="规格" />
            <el-input v-model.number="b.amount" placeholder="血量" />
            <el-input v-model="b.amountUnit" placeholder="单位（U/ml/治疗量）" />
            <el-input v-model="b.sourceBank" placeholder="来源血站" />
            <el-date-picker
              v-model="b.collectDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="采集日期"
              class="!w-full"
            />
            <el-date-picker
              v-model="b.expireDate"
              :data-testid="`p4-tf-bag-expire-${idx}`"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="有效期至 *"
              class="!w-full"
            />
            <el-select v-model="b.crossmatchMain" placeholder="主侧">
              <el-option label="阴性（相合）" value="阴性" />
              <el-option label="阳性（不合）" value="阳性" />
            </el-select>
            <el-select v-model="b.crossmatchSide" placeholder="次侧">
              <el-option label="阴性（相合）" value="阴性" />
              <el-option label="阳性（不合）" value="阳性" />
            </el-select>
            <el-select v-model="b.crossmatchResult" :data-testid="`p4-tf-bag-result-${idx}`" placeholder="配血结论 *">
              <el-option label="相合" :value="1" />
              <el-option label="不合" :value="2" />
            </el-select>
            <el-input v-model="b.remark" placeholder="备注" />
          </div>
        </div>
        <el-form label-width="90px">
          <el-form-item label="配血备注">
            <el-input
              v-model="crossNote"
              data-testid="p4-tf-cross-note"
              type="textarea"
              :rows="2"
              placeholder="可空；有配血不合时必须写明原因与后续处理"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="crossVisible = false">取消</el-button>
        <el-button
          v-perm="'medtech:transfusion:edit'"
          type="primary"
          :loading="crossSubmitting"
          data-testid="p4-tf-cross-submit"
          @click="submitCrossmatch"
        >
          提交配血结果
        </el-button>
      </template>
    </el-dialog>

    <!-- 发血 -->
    <el-dialog v-model="issueVisible" title="发血" width="560px" data-testid="p4-tf-issue-dialog">
      <div v-if="issueTarget" class="space-y-3">
        <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm">
          <div class="text-slate-900">{{ text(issueTarget.patientName) }}（{{ text(issueTarget.admissionNo) }}）</div>
          <div class="mt-1 text-slate-700">
            受血者 {{ text(issueTarget.bloodTypeText) }} ·
            {{ text(issueTarget.bloodComponentText) }} · {{ text(issueTarget.bagProgressText) }}
          </div>
          <div class="text-[12px] text-slate-500">
            配血状态：{{ text(issueTarget.crossmatchStatusText) }} ·
            {{ text(issueTarget.crossmatchDoctorName) }} {{ fmt(issueTarget.crossmatchTime) }}
          </div>
        </div>
        <el-form label-width="90px">
          <el-form-item label="发血备注">
            <el-input v-model="issueForm.issueRemark" type="textarea" :rows="2" placeholder="如：已核对血袋外观完好（可空）" />
          </el-form-item>
        </el-form>
        <el-alert type="info" :closable="false" show-icon>
          发血后状态变为「已发血」，血袋状态全部置为已发血；下一步由病区护士双人核对后开始输注。
        </el-alert>
      </div>
      <template #footer>
        <el-button @click="issueVisible = false">取消</el-button>
        <el-button v-perm="'medtech:transfusion:edit'" type="primary" :loading="issueSubmitting" data-testid="p4-tf-issue-submit" @click="submitIssue">
          确认发血
        </el-button>
      </template>
    </el-dialog>

    <!-- 开始输注（双人核对） -->
    <el-dialog
      v-model="startVisible"
      title="开始输注（输血前双人核对）"
      width="700px"
      data-testid="p4-tf-start-dialog"
    >
      <div v-if="startTarget" class="space-y-3">
        <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm">
          <div class="text-slate-900">{{ text(startTarget.patientName) }}</div>
          <div class="text-slate-700">
            {{ text(startTarget.bloodTypeText) }} · {{ text(startTarget.bloodComponentText) }} ·
            {{ text(startTarget.bagProgressText) }}
          </div>
          <div class="text-[12px] text-slate-500">
            发血人 {{ text(startTarget.issueDoctorName) }} · {{ fmt(startTarget.issueTime) }}
          </div>
        </div>
        <div class="text-[12px] text-slate-500">
          标 <span class="text-red-500">*</span> 的是必核项，缺任何一项都无法提交 ——
          输血是唯一要求双人核对的护理操作，"已核对无误"这句话在飞检时回答不了"到底核了哪几项"。
        </div>
        <el-checkbox-group v-model="startForm.items" class="flex flex-col gap-2">
          <el-checkbox
            v-for="i in checkItemOptions"
            :key="i.code"
            :value="i.code"
            :data-testid="`p4-tf-check-item-${i.code}`"
          >
            <span class="text-slate-700">{{ i.label }}</span>
            <span v-if="i.required" class="ml-1 text-red-500">*</span>
          </el-checkbox>
        </el-checkbox-group>
        <el-form label-width="110px">
          <el-form-item label="核对护士 1" required>
            <el-select
              v-model="startForm.checkNurseId"
              data-testid="p4-tf-check-nurse1"
              placeholder="选择核对护士1"
              filterable
              class="!w-full"
            >
              <el-option
                v-for="e in employees"
                :key="e.id"
                :label="`${e.empName || e.id}${e.deptName ? '（' + e.deptName + '）' : ''}`"
                :value="String(e.id)"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="核对护士 2" required>
            <el-select
              v-model="startForm.checkNurse2Id"
              data-testid="p4-tf-check-nurse2"
              placeholder="必须与护士1 不同人"
              filterable
              class="!w-full"
            >
              <el-option
                v-for="e in employees"
                :key="e.id"
                :label="`${e.empName || e.id}${e.deptName ? '（' + e.deptName + '）' : ''}`"
                :value="String(e.id)"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="核对异常说明">
            <el-input
              v-model="startForm.checkNote"
              data-testid="p4-tf-check-note"
              type="textarea"
              :rows="2"
              placeholder="正常可空；有异常必须写（如：血袋外观有轻微气泡，已请血库复核）"
            />
          </el-form-item>
          <el-form-item label="输注开始时间" required>
            <el-date-picker
              v-model="startForm.infusionStartTime"
              data-testid="p4-tf-start-time"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="选择开始时间"
              class="!w-full"
            />
          </el-form-item>
          <el-form-item label="执行护士">
            <el-select
              v-model="startForm.infusionNurseId"
              placeholder="为空 = 由核对护士1 执行"
              filterable
              clearable
              class="!w-full"
            >
              <el-option v-for="e in employees" :key="e.id" :label="e.empName || e.id" :value="String(e.id)" />
            </el-select>
          </el-form-item>
          <el-form-item label="滴速">
            <el-input v-model="startForm.infusionSpeed" placeholder="如 60滴/分（前 15 分钟须慢速）" maxlength="32" />
          </el-form-item>
          <el-form-item label="输注观察">
            <el-input
              v-model="startForm.observation"
              type="textarea"
              :rows="2"
              placeholder="开始输注时的生命体征（也可在「登记完成」时补填）"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="startVisible = false">取消</el-button>
        <el-button
          v-perm="'medtech:transfusion:edit'"
          type="primary"
          :loading="startSubmitting"
          data-testid="p4-tf-start-submit"
          @click="submitStart"
        >
          确认已核对并开始输注
        </el-button>
      </template>
    </el-dialog>

    <!-- 登记完成 -->
    <el-dialog
      v-model="finishVisible"
      title="登记输血完成（回写输血记录病历 + 病案首页是否输血）"
      width="700px"
      data-testid="p4-tf-finish-dialog"
    >
      <div v-if="finishTarget" class="space-y-3">
        <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm">
          <div class="text-slate-900">{{ text(finishTarget.patientName) }}</div>
          <div class="text-slate-700">
            {{ text(finishTarget.bloodTypeText) }} · {{ text(finishTarget.bloodComponentText) }} ·
            {{ text(finishTarget.bagProgressText) }}
          </div>
          <div class="text-[12px] text-slate-500">
            双人核对：{{ text(finishTarget.checkNurseName) }} / {{ text(finishTarget.checkNurse2Name) }} ·
            开始 {{ fmt(finishTarget.infusionStartTime) }}
          </div>
        </div>
        <el-form label-width="120px">
          <el-form-item label="输注结束时间" required>
            <el-date-picker
              v-model="finishForm.infusionEndTime"
              data-testid="p4-tf-finish-time"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="选择结束时间"
              class="!w-full"
            />
          </el-form-item>
          <el-form-item label="实际输注量" required>
            <el-input
              v-model.number="finishForm.actualAmount"
              data-testid="p4-tf-finish-amount"
              placeholder="必须大于 0"
              class="!w-40"
            />
            <span class="ml-2 text-[12px] text-slate-500">{{ text(finishTarget.amountUnit) }}</span>
          </el-form-item>
          <el-form-item label="输注观察" required>
            <el-input
              v-model="finishForm.observation"
              data-testid="p4-tf-finish-observation"
              type="textarea"
              :rows="3"
              placeholder="输注过程与输注后观察：生命体征、有无寒战发热皮疹等（开始后 15 分钟是反应高发期）"
            />
          </el-form-item>
          <el-form-item label="疗效评估">
            <el-input
              v-model="finishForm.efficacyEval"
              type="textarea"
              :rows="2"
              placeholder="如：输注后心悸气促改善，复查 Hb 升至 85 g/L"
            />
          </el-form-item>
          <el-form-item label="输血后复查">
            <div class="flex items-center gap-3">
              <el-input v-model.number="finishForm.postHb" placeholder="Hb g/L" class="!w-40" />
              <el-input v-model.number="finishForm.postHct" placeholder="HCT %" class="!w-40" />
              <el-input v-model.number="finishForm.postPlt" placeholder="PLT ×10⁹/L" class="!w-48" />
            </div>
          </el-form-item>
        </el-form>
        <el-alert type="info" :closable="false" show-icon>
          完成后系统会回写一份<b>输血记录</b>病历（record_type=11），并把病案首页「是否输血」置为是。
          如输注后出现不良反应，请用列表里的「上报反应」补登记（不改回本单状态）。
        </el-alert>
      </div>
      <template #footer>
        <el-button @click="finishVisible = false">取消</el-button>
        <el-button
          v-perm="'medtech:transfusion:edit'"
          type="primary"
          :loading="finishSubmitting"
          data-testid="p4-tf-finish-submit"
          @click="submitFinish"
        >
          确认完成并回写
        </el-button>
      </template>
    </el-dialog>

    <!-- 输血反应上报 -->
    <el-dialog
      v-model="reactionVisible"
      title="输血反应上报"
      width="620px"
      data-testid="p4-tf-reaction-dialog"
    >
      <div v-if="reactionTarget" class="space-y-3">
        <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm">
          <div class="text-slate-900">{{ text(reactionTarget.patientName) }}（{{ text(reactionTarget.applyNo) }}）</div>
          <div class="mt-1 text-slate-700">
            {{ text(reactionTarget.bloodTypeText) }} · {{ text(reactionTarget.bloodComponentText) }} ·
            输注 {{ fmt(reactionTarget.infusionStartTime) }} ~ {{ fmt(reactionTarget.infusionEndTime) }}
          </div>
        </div>
        <el-form label-width="100px">
          <el-form-item label="反应类型" required>
            <el-select
              v-model="reactionForm.reactionType"
              data-testid="p4-tf-reaction-type"
              placeholder="从字典选择（不接受自由填写）"
              class="!w-full"
            >
              <el-option v-for="t in reactionTypes" :key="t" :label="t" :value="t" />
            </el-select>
          </el-form-item>
          <el-form-item label="反应描述" required>
            <el-input
              v-model="reactionForm.reactionDesc"
              data-testid="p4-tf-reaction-desc"
              type="textarea"
              :rows="3"
              placeholder="什么时候出现什么症状、生命体征怎么变的"
            />
          </el-form-item>
          <el-form-item label="处理措施" required>
            <el-input
              v-model="reactionForm.reactionHandle"
              data-testid="p4-tf-reaction-handle"
              type="textarea"
              :rows="3"
              placeholder="停药 / 给氧 / 用药 / 是否上报血库与医务科"
            />
          </el-form-item>
        </el-form>
        <el-alert type="warning" :closable="false" show-icon>
          上报只置「有输血反应」标记并登记详情，<b>不回改历史状态</b> ——
          把已完成的输血单改回输注中就是在改历史，还会破坏"完成=已回写病历"的一致性。
        </el-alert>
      </div>
      <template #footer>
        <el-button @click="reactionVisible = false">取消</el-button>
        <el-button
          v-perm="'medtech:transfusion:add'"
          type="primary"
          :loading="reactionSubmitting"
          data-testid="p4-tf-reaction-submit"
          @click="submitReaction"
        >
          确认上报
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" title="用血单详情" width="920px" data-testid="p4-tf-detail-dialog">
      <div v-if="detail" class="space-y-4">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="用血单号">{{ text(detail.applyNo) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            {{ text(detail.transfusionStatusText) }} / 配血：{{ text(detail.crossmatchStatusText) }}
          </el-descriptions-item>
          <el-descriptions-item label="患者">
            {{ text(detail.patientName) }}（{{ text(detail.admissionNo) }}）
          </el-descriptions-item>
          <el-descriptions-item label="床号 / 科室">
            {{ text(detail.applyBedNo) }}床 / {{ text(detail.applyDeptName) }}
          </el-descriptions-item>
          <el-descriptions-item label="受血者血型">
            {{ text(detail.bloodTypeText) }}
            <span v-if="detail.aboMatchesArchive === false" class="ml-2 text-[11px] text-amber-600">
              （与档案不一致，以本次为准）
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="血液品种">
            {{ text(detail.bloodComponentText) }}
            <span v-if="detail.componentSpec" class="text-slate-400">（{{ detail.componentSpec }}）</span>
          </el-descriptions-item>
          <el-descriptions-item label="申请袋数 / 总量">
            {{ text(detail.bagCount) }} 袋 /
            {{ detail.plannedAmount ? detail.plannedAmount + (detail.amountUnit || '') : '—' }}
            <span class="ml-2 text-slate-400">{{ text(detail.bagProgressText) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="紧急用血">{{ text(detail.isEmergencyText) }}</el-descriptions-item>
          <el-descriptions-item label="申请医生 / 时间">
            {{ text(detail.applyDoctorName) }} / {{ fmt(detail.applyTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="输血目的">{{ text(detail.transfusionPurpose) }}</el-descriptions-item>
          <el-descriptions-item label="输血指征" :span="2">{{ text(detail.indication) }}</el-descriptions-item>
          <el-descriptions-item label="输血前指标" :span="2">
            Hb {{ text(detail.preHb) }} g/L · HCT {{ text(detail.preHct) }}% · PLT {{ text(detail.prePlt) }}×10⁹/L
          </el-descriptions-item>
          <el-descriptions-item label="既往输血史">{{ text(detail.transfusionHistory) }}</el-descriptions-item>
          <el-descriptions-item label="既往反应史">{{ text(detail.reactionHistory) }}</el-descriptions-item>
          <el-descriptions-item label="妊娠史">{{ text(detail.pregnancyHistory) }}</el-descriptions-item>
          <el-descriptions-item label="配血人 / 时间">
            {{ text(detail.crossmatchDoctorName) }} / {{ fmt(detail.crossmatchTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="发血人 / 时间">
            {{ text(detail.issueDoctorName) }} / {{ fmt(detail.issueTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="双人核对" :span="2">
            <span v-if="!detail.checkNurseName" class="text-slate-400">尚未核对</span>
            <span v-else>
              {{ detail.checkNurseName }} / {{ text(detail.checkNurse2Name) }}
              <span class="text-slate-400">于 {{ fmt(detail.checkTime) }}</span>
              <div class="text-[11px] text-slate-500">{{ text(detail.checkItemsText) }}</div>
            </span>
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.checkNote" label="核对异常说明" :span="2">
            {{ detail.checkNote }}
          </el-descriptions-item>
          <el-descriptions-item label="输注时间 / 时长">
            {{ fmt(detail.infusionStartTime) }} ~ {{ fmt(detail.infusionEndTime) }}
            <span v-if="detail.durationText" class="text-slate-400">（{{ detail.durationText }}）</span>
          </el-descriptions-item>
          <el-descriptions-item label="实际输注量">
            {{ text(detail.actualAmount) }}{{ detail.amountUnit || '' }}
            <span v-if="detail.infusionSpeed" class="text-slate-400">· {{ detail.infusionSpeed }}</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.observation" label="输注观察" :span="2">
            {{ detail.observation }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.efficacyEval" label="疗效评估" :span="2">
            {{ detail.efficacyEval }}
          </el-descriptions-item>
          <el-descriptions-item label="输血后复查" :span="2">
            Hb {{ text(detail.postHb) }} g/L · HCT {{ text(detail.postHct) }}% · PLT {{ text(detail.postPlt) }}×10⁹/L
          </el-descriptions-item>
          <el-descriptions-item label="输血反应" :span="2">
            <span v-if="detail.hasReaction === 1" class="font-medium text-red-600">
              {{ text(detail.reactionType) }}：{{ text(detail.reactionDesc) }}；处理：{{ text(detail.reactionHandle) }}
              <div class="text-[11px] text-slate-500">
                由 {{ text(detail.reactionReporterName) }} 于 {{ fmt(detail.reactionTime) }} 上报
              </div>
            </span>
            <span v-else class="text-slate-400">{{ text(detail.hasReactionText) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="回写病历号" :span="2">
            <span v-if="detail.recordNo" class="text-slate-700">{{ detail.recordNo }}（输血记录）</span>
            <span v-else class="text-slate-400">尚未回写（输血未完成）</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.cancelReason" label="取消原因" :span="2">
            {{ detail.cancelReason }}（{{ text(detail.cancelDoctorName) }} {{ fmt(detail.cancelTime) }}）
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.remark" label="备注" :span="2">{{ detail.remark }}</el-descriptions-item>
        </el-descriptions>

        <div>
          <div class="mb-2 text-[13px] font-medium text-slate-700">
            血袋明细（{{ (detail.bags || []).length }} 袋）
          </div>
          <el-table :data="detail.bags || []" size="small" border style="width: 100%">
            <el-table-column prop="bagNo" label="血袋号" min-width="140" />
            <el-table-column prop="donorNo" label="献血编号" min-width="120" />
            <el-table-column label="血型" width="110">
              <template #default="{ row }">{{ text(row.bloodTypeText) }}</template>
            </el-table-column>
            <el-table-column label="规格 / 血量" min-width="130">
              <template #default="{ row }">
                {{ text(row.spec) }}
                <span v-if="row.amount" class="text-slate-400">{{ row.amount }}{{ row.amountUnit || '' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="来源血站" min-width="120">
              <template #default="{ row }">{{ text(row.sourceBank) }}</template>
            </el-table-column>
            <el-table-column label="有效期" width="140">
              <template #default="{ row }">
                <span :class="row.expired ? 'text-red-600' : 'text-slate-700'">
                  {{ text(row.expireDate) }}{{ row.expired ? '（已过期）' : '' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="配血结果" min-width="130">
              <template #default="{ row }">
                <el-tag :type="row.crossmatchResult === 1 ? 'success' : 'danger'" size="small" effect="plain">
                  {{ text(row.crossmatchResultText) }}
                </el-tag>
                <div class="text-[11px] text-slate-400">
                  主 {{ text(row.crossmatchMain) }} / 次 {{ text(row.crossmatchSide) }}
                </div>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">{{ text(row.bagStatusText) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>
