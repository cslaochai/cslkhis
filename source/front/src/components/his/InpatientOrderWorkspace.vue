<script setup lang="ts">
/**
 * 住院医嘱工作区（P1：医嘱 → 校对 → 执行 → 计费）
 *
 * 医生站与护士站**共用这同一个组件**（`mode` 只改默认页签），原因：
 * 医嘱状态机与文案只允许有一份实现。前端各写一套 `switch(orderStatus)` 属于
 * 「同一语义两处实现」，后端一旦新增码值就会静默渲染成看着正常的错按钮 ——
 * 所以这里按钮的可用性一律读后端给的 canVerify / canStop / canCancel，
 * 文案一律读后端给的 xxxText，前端不自己判断码值。
 *
 * 三个必须记住的口径：
 * 1. 所有 ID 都是字符串。雪花ID 超过 JS 的 Number 安全整数范围，后端按字符串出参，
 *    前端不做 Number() 转换（转了就会静默丢精度，回头查详情必然「医嘱不存在」）。
 * 2. 「待执行队列」那个查询是 GET 但会写库（补当天长期医嘱计划行，幂等）。
 *    刻意不引入定时任务 —— 否则服务停机那天，全院长期医嘱的计划会集体缺失。
 * 3. 执行 = 已执行（计费）或 已跳过（必填原因，不计费）。跳过也留痕，不删除：
 *    飞检问的是「这条医嘱为什么没有执行记录」，答「删了」不成立。
 */
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Refresh, Plus, Delete, CircleCheck, Warning, Select, Collection,
} from '@element-plus/icons-vue'
import { getMyDeptInpatientListPage } from '@/api/inpatient'
import { getPatientFullDetail } from '@/api/patient'
import { getLaboratoryRecordListPage, getInspectionRecordListPage, getLaboratoryDetail } from '@/api/medicaltech'
import PatientDetailDialog from '@/components/his/PatientDetailDialog.vue'
import { getUserInfo } from '@/api/system'
import { getDrugSelectList, getInspectionSelectList, getLaboratorySelectList } from '@/api/system'
import { patientGenderText, patientAgeText } from '@/lib/patientGender'
import { DICT_TYPE, loadDictDataMap } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import { routeOptions, frequencyOptions, dosageUnitOptions, loadOrderUsageOptions } from '@/lib/drugUsage'
import { ORDER_CLASS_OPTIONS } from '@/lib/orderItemDict'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import {
  getInpatientOrderListPage,
  saveInpatientOrder,
  verifyInpatientOrder,
  stopInpatientOrder,
  cancelInpatientOrder,
  getInpatientOrderPendingVerifyCount,
  getInpatientOrderPendingExecCount,
  getOrderExecPendingList,
  completeOrderExec,
  getOrderExecList,
  startInfusion,
  addInfusionRound,
  finishInfusion,
  getInfusionRounds,
  getOrderTemplateSelectList,
  getOrderTemplateListPage,
  getOrderTemplateById,
  upsertOrderTemplate,
  deleteOrderTemplateById,
} from '@/api/inpatientOrder'
import { getActiveEnrollByAdmission, pathwayOrderCheck, varianceUpsert } from '@/api/pathway'

const props = withDefaults(defineProps<{ mode?: 'doctor' | 'nurse' }>(), { mode: 'doctor' })
const isNurse = computed(() => props.mode === 'nurse')

// 按 mode 选 ipd:order / ipd:nurse 前缀；护士没有 :delete 码，delete 类动作按 ipd:nurse:edit 判定
const orderPerm = (action: 'add' | 'edit' | 'delete') => {
  if (isNurse.value) return action === 'delete' ? 'ipd:nurse:edit' : `ipd:nurse:${action}`
  return `ipd:order:${action}`
}

interface OrderRow {
  id: string
  orderNo: string
  admissionId: string
  patientId: string
  patientNo?: string
  patientName?: string
  deptName?: string
  wardName?: string
  bedNo?: string
  orderType?: number
  orderTypeText?: string
  orderGroup?: string
  orderClass?: number
  orderClassText?: string
  itemCode?: string
  itemName?: string
  spec?: string
  unit?: string
  dosage?: number
  dosageUnit?: string
  route?: string
  frequency?: string
  quantity?: number
  price?: number
  amount?: number
  startTime?: string
  planEndTime?: string
  stopTime?: string
  orderTime?: string
  doctorName?: string
  verifyNurseName?: string
  verifyTime?: string
  stopDoctorName?: string
  stopReason?: string
  orderStatus?: number
  orderStatusText?: string
  isUrgent?: number
  isUrgentText?: string
  sourceText?: string
  remark?: string
  todayExecCount?: number
  pendingExecCount?: number
  canVerify?: boolean
  canCancel?: boolean
  canEdit?: boolean
  canStop?: boolean
}

interface ExecRow {
  id: string
  orderId: string
  orderNo?: string
  admissionId: string
  patientId: string
  patientName?: string
  wardName?: string
  bedNo?: string
  doctorName?: string
  orderType?: number
  orderTypeText?: string
  orderClass?: number
  orderClassText?: string
  itemName?: string
  spec?: string
  unit?: string
  dosage?: number
  dosageUnit?: string
  route?: string
  frequency?: string
  quantity?: number
  price?: number
  amount?: number
  isUrgent?: number
  orderStatus?: number
  orderStatusText?: string
  execSeq?: number
  planDate?: string
  planTime?: string
  execTime?: string
  execNurseName?: string
  execStatus?: number
  execStatusText?: string
  execNote?: string
  infusionStartTime?: string
  dripRate?: number
  infusionEndTime?: string
  adverseFlag?: number
  adverseNote?: string
  infusion?: boolean
  chargeId?: string
  chargeNo?: string
  charged?: boolean
}

interface AdmissionOption {
  admissionId: string
  /** 住院号（左栏页头展示） */
  admissionNo?: string
  patientId?: string
  patientNo?: string
  patientName?: string
  gender?: number
  age?: number
  bedNo?: string
  wardName?: string
  deptName?: string
  /** 主治/入院医生（患者横幅展示） */
  doctorName?: string
  /** 医保类型（患者横幅展示） */
  insuranceType?: string
  /** 已住院天数（后端算好，前端不用 admitTime 自算） */
  inpatientDays?: number
  /** 入院诊断文本（左栏列表行；未录时显示「未录诊断」，不回落到空） */
  diagnosis?: string
  /** 病案首页主要诊断（入院诊断未录时用它兜底，横幅上不能空着） */
  mainDiagnosisName?: string
  /** 入院时间（左栏列表行显示日期） */
  admitTime?: string
}

/** 检查回报行（字段口径 = BizInspectionRecordVO，「检查所见/结论」列表接口即带回） */
interface InsRecordRow {
  id: string
  recordNo?: string
  inspectionItemName?: string
  bodyPart?: string
  clinicalDiagnosis?: string
  inspectionPurpose?: string
  applyDeptName?: string
  applyDoctorName?: string
  inspectionDeptName?: string
  visitDate?: string
  appointmentTime?: string
  executeTime?: string
  reportTime?: string
  recordStatus?: number
  resultDescription?: string
  resultConclusion?: string
}

/** 检验回报行（字段口径 = BizLaboratoryRecordVO，结果明细只在详情接口里） */
interface LabRecordRow {
  id: string
  recordNo?: string
  laboratoryItemName?: string
  specimenType?: string
  specimenNo?: string
  diagnosis?: string
  applyDeptName?: string
  applyDoctorName?: string
  laboratoryDeptName?: string
  visitDate?: string
  sampleTime?: string
  receiveTime?: string
  reportTime?: string
  recordStatus?: number
  suggestions?: string
}

/** 检验报告详情（LaboratoryDetailVO：记录 + 结果明细 + 报告） */
interface LabDetail {
  record?: LabRecordRow
  results?: Array<{
    id: string
    laboratoryItemName?: string
    resultValue?: string
    resultUnit?: string
    referenceRange?: string
    abnormalFlagText?: string
    judgeNote?: string
  }>
  report?: {
    reportNo?: string
    conclusion?: string
    suggestions?: string
    reportContent?: string
    auditBy?: string
    auditTime?: string
    publishTime?: string
  }
}

interface ItemForm {
  orderClass: number
  /** 选中的字典项 id（仅用于下拉回显；提交时不传给后端）。手输类别为空 */
  dictId: string
  /**
   * 该行按「手工录入」渲染：本类别没有字典，或套用模板时编码在字典里已查不到（下架）。
   * 有字典却查不到时若不标记，下拉会因为值匹配不到任何选项而显示成空白，医生看不到项目名。
   */
  manual?: boolean
  itemCode: string
  itemName: string
  spec: string
  unit: string
  dosage: number | undefined
  dosageUnit: string
  route: string
  frequency: string
  quantity: number
  price: number | undefined
}

// ---------------- 基础数据 ----------------

/**
 * 工作区高度：撑满视口（真实 HIS 工作站是"面板铺满屏幕、内容在面板内滚"，
 * 不是网页那样整页滚动留一片空白底）。顶偏移必须**实测**：医生站页头只有面包屑，
 * 护士站上面还压着一排「医嘱执行 / 护理文书」单选，写死 calc(100vh-112px) 必算错一种。
 */
const rootRef = ref<HTMLElement | null>(null)
const workspaceH = ref(0)
const measureWorkspace = () => {
  const el = rootRef.value
  if (!el) return
  const top = el.getBoundingClientRect().top
  // 24 = main 的下内边距（p-6）；480 兜底：视口太矮时宁可出整页滚动条，也不把表格压成一条缝
  workspaceH.value = Math.max(480, window.innerHeight - top - 24)
}

// 左栏「在院患者」工作列表：真实 HIS 医生站的数据边界就是**本科室**（护士=本病区），
// 跨科患者走会诊邀请，不提供自由切「全部科室」的入口。
// 过滤在后端做（/patient/inpatient/listMyDeptPage 从登录态取 deptId），前端传的科室参数一律被忽略。
const admissions = ref<AdmissionOption[]>([])
const admissionId = ref<string>('')
/** 当前选中患者行（右栏页头展示用）。与 admissions 解耦：左栏按关键字过滤后，选中行不一定还在列表里 */
const currentAdmission = ref<AdmissionOption | null>(null)
const admissionKeyword = ref('')
const admissionLoading = ref(false)
const admissionTotal = ref(0)
/** 左栏单次拉取上限。超出时页面显式提示「仅显示前 N 人」，不静默截断 */
const ADMISSION_PAGE_SIZE = 200
/** 当前登录员工的科室（/auth/info），仅用于空态提示（未绑定科室时后端会 fail-closed 报错）。
 *  医生=本科室、护士=本病区，都按 deptId 过滤：
 *  本项目 sys_ward.dept_id 与科室是 1:1 铺底，且 /auth/info 只带 deptId，不带 wardId；
 *  将来一科室多病区时，需给 /auth/info 补 wardId 再改后端 listMyDeptPage。 */
const myDeptId = ref<string>('')
const myDeptName = ref<string>('')

const orderTypeMap = [
  { value: 1, label: '长期' },
  { value: 2, label: '临时' },
]
/**
 * 医嘱类别候选：直接复用 `lib/orderItemDict.js` 的 ORDER_CLASS_OPTIONS —— 以前这里抄了一份 1~9，
 * 加 10-临床营养时只改了 lib、**忘了改这份**，于是后端与字典里有了这个类别、医生的开立下拉里却没有
 * （表现为「全院医嘱类型里没有临床营养」）。同一份枚举写两遍必然漂移，删掉本地副本。
 */
const orderClassMap = ORDER_CLASS_OPTIONS
const orderStatusMap = [
  { value: 1, label: '待校对' }, { value: 2, label: '已校对' }, { value: 3, label: '执行中' },
  { value: 4, label: '已完成' }, { value: 5, label: '已停止' }, { value: 6, label: '已作废' },
]

const fmtTime = (v?: string) => (v ? String(v).replace('T', ' ').slice(0, 16) : '—')
const fmtMoney = (v?: number) => (v === null || v === undefined ? '—' : `¥${Number(v).toFixed(2)}`)

const statusTagType = (s?: number) => {
  if (s === 1) return 'danger'
  if (s === 2) return 'warning'
  if (s === 3) return 'primary'
  if (s === 4) return 'success'
  if (s === 5) return 'info'
  return 'info'
}

const fmtDate = (v?: string) => (v ? String(v).replace('T', ' ').slice(0, 10) : '—')

/** 当前登录员工的科室：只用于空态提示；真正的过滤在后端按登录态做 */
const loadMyDept = async () => {
  try {
    const res = await getUserInfo()
    myDeptId.value = res.data?.deptId ? String(res.data.deptId) : ''
    myDeptName.value = res.data?.deptName || ''
  } catch (error: any) {
    console.warn('获取当前用户科室失败:', error)
  }
}

const loadAdmissions = async () => {
  admissionLoading.value = true
  try {
    const res = await getMyDeptInpatientListPage({
      admitStatus: 1,
      patientName: admissionKeyword.value.trim() || undefined,
      pageNum: 1,
      pageSize: ADMISSION_PAGE_SIZE,
    })
    admissions.value = (res.data?.records || []) as AdmissionOption[]
    admissionTotal.value = Number(res.data?.total ?? 0)
    // 左右必须一致：当前选中的患者已不在列表里（换范围 / 改关键字）就落到第一条，列表为空则清空选中
    const stillIn = admissions.value.some(a => String(a.admissionId) === admissionId.value)
    if (!stillIn) {
      const first = admissions.value[0] || null
      admissionId.value = first ? String(first.admissionId) : ''
      currentAdmission.value = first
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载在院患者失败')
  } finally {
    admissionLoading.value = false
  }
}

const selectAdmission = async (a: AdmissionOption) => {
  if (String(a.admissionId) === admissionId.value) return
  admissionId.value = String(a.admissionId)
  currentAdmission.value = a
  await handleAdmissionChange()
}

const searchAdmissions = async () => {
  await loadAdmissions()
  await handleAdmissionChange()
}

// ---------------- 患者上下文横幅（真 HIS：床号/住院号/天数/主治/过敏常驻页头，不开弹框也要看得见） ----------------

const patientDialogVisible = ref(false)
/** 过敏史文本（展示口径走后端已脱敏的 getDetailById，前端不遮码） */
const allergyText = ref('')
/** 诊断口径：入院诊断优先，未录时用病案首页主要诊断，两者都没有才说「未录诊断」 */
const diagnosisText = computed(() => currentAdmission.value?.diagnosis || currentAdmission.value?.mainDiagnosisName || '未录诊断')

const loadAllergy = async (patientId?: string) => {
  allergyText.value = ''
  if (!patientId) return
  try {
    const res = await getPatientFullDetail(patientId)
    const d = res?.data
    if (!d) return
    const named = (d.allergies || []).map((a: any) => a.allergenName).filter(Boolean)
    allergyText.value = d.allergyHistory || (named.length ? `对 ${[...new Set(named)].join('、')} 过敏` : '')
  } catch (e) {
    // 过敏史只是横幅上的一行警示，拉不到不弹错、不挡医嘱主线
    console.warn('加载过敏史失败', e)
  }
}

// ---------------- 统计（撤掉四张全院统计卡，数字留在页签角标上） ----------------

const stats = ref({ pendingVerify: 0, pendingExec: 0 })

const loadStats = async () => {
  try {
    const [v, e] = await Promise.all([
      getInpatientOrderPendingVerifyCount(admissionId.value || undefined),
      getInpatientOrderPendingExecCount(admissionId.value || undefined),
    ])
    stats.value = { pendingVerify: Number(v.data ?? 0), pendingExec: Number(e.data ?? 0) }
  } catch (error: any) {
    console.error('加载医嘱统计失败:', error)
  }
}

// ---------------- 检查 / 检验回报（仅医生站） ----------------
//
// 为什么护士站不放这两个页签：`/medicaltech/inspection|laboratory/*` 在控制器上要求
// `opd:doctorWorkstation:list` 或 `medtech:*Workstation:list`，而护理岗（10014）两个码都没有 ——
// 放了就是点进去整屏 403。执行单上的检验结果由护士站的「待执行」队列承载，不需要回报视图。

/** 回报状态文案走字典（与后端 InsRecordStatusEnum / LabRecordStatusEnum 同源），前端不抄码表 */
const recordStatusDict = ref<Record<string, any[]>>({})
const insStatusText = (v?: number) => dictLabelText(recordStatusDict.value[DICT_TYPE.INSPECTION_RECORD_STATUS], v)
const labStatusText = (v?: number) => dictLabelText(recordStatusDict.value[DICT_TYPE.LABORATORY_RECORD_STATUS], v)
/** 状态色只看「报告出没出」：已出结果及之后是绿，已审核/已取消是灰，其余（登记~检测中）是进行中的蓝 */
const insTagType = (s?: number) => ((s === 4 || s === 5 || s === 6) ? 'success' : s === 7 ? 'info' : 'primary')
const labTagType = (s?: number) => ((s === 5 || s === 6 || s === 7) ? 'success' : s === 8 ? 'info' : 'primary')

const insRows = ref<InsRecordRow[]>([])
const insTotal = ref(0)
const insLoading = ref(false)
const insPagination = ref({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
/** 当前展开查看的检查报告（表格里只放结论摘要，点「查看报告」出全文） */
const insDetail = ref<InsRecordRow | null>(null)
const insDetailDialog = ref(false)

const labRows = ref<LabRecordRow[]>([])
const labTotal = ref(0)
const labLoading = ref(false)
const labPagination = ref({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
const labDetail = ref<LabDetail | null>(null)
const labDetailDialog = ref(false)
const labDetailLoading = ref(false)

/** 回报按患者查：接口入参是 patientId（不是 admissionId），住院/门诊同一患者的历史回报都能捞到 */
const currentPatientId = computed(() => currentAdmission.value?.patientId || '')

const loadInsRows = async () => {
  if (isNurse.value) return
  if (!currentPatientId.value) {
    insRows.value = []
    insTotal.value = 0
    return
  }
  insLoading.value = true
  try {
    const res = await getInspectionRecordListPage({
      patientId: currentPatientId.value,
      pageNum: insPagination.value.pageNum,
      pageSize: insPagination.value.pageSize,
    })
    insRows.value = (res.data?.records || []) as InsRecordRow[]
    insTotal.value = Number(res.data?.total ?? 0)
  } catch (error: any) {
    ElMessage.error(error.message || '加载检查回报失败')
  } finally {
    insLoading.value = false
  }
}

const loadLabRows = async () => {
  if (isNurse.value) return
  if (!currentPatientId.value) {
    labRows.value = []
    labTotal.value = 0
    return
  }
  labLoading.value = true
  try {
    const res = await getLaboratoryRecordListPage({
      patientId: currentPatientId.value,
      pageNum: labPagination.value.pageNum,
      pageSize: labPagination.value.pageSize,
    })
    labRows.value = (res.data?.records || []) as LabRecordRow[]
    labTotal.value = Number(res.data?.total ?? 0)
  } catch (error: any) {
    ElMessage.error(error.message || '加载检验回报失败')
  } finally {
    labLoading.value = false
  }
}

/** 检验结果明细只在详情接口里（列表 VO 没有 results），所以点「查看报告」才发这个请求 */
const openLabDetail = async (row: LabRecordRow) => {
  labDetailDialog.value = true
  labDetailLoading.value = true
  labDetail.value = null
  try {
    const res = await getLaboratoryDetail(row.id)
    labDetail.value = (res.data || null) as LabDetail | null
  } catch (error: any) {
    ElMessage.error(error.message || '加载检验报告失败')
  } finally {
    labDetailLoading.value = false
  }
}

// 检查的「检查所见 / 结论」列表接口就带回来了，不再多打一次详情
const openInsDetail = (row: InsRecordRow) => {
  insDetail.value = row
  insDetailDialog.value = true
}

// ---------------- 医嘱列表 ----------------

const activeTab = ref(isNurse.value ? 'verify' : 'order')

const orderLoading = ref(false)
const orders = ref<OrderRow[]>([])
const orderTotal = ref(0)
const orderQuery = ref({
  orderType: null as number | null,
  orderStatus: null as number | null,
  keyword: '',
})
const orderPagination = ref({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })

const loadOrders = async () => {
  orderLoading.value = true
  try {
    const res = await getInpatientOrderListPage({
      admissionId: admissionId.value || undefined,
      pageNum: orderPagination.value.pageNum,
      pageSize: orderPagination.value.pageSize,
      orderType: orderQuery.value.orderType ?? undefined,
      orderStatus: orderQuery.value.orderStatus ?? undefined,
      keyword: orderQuery.value.keyword || undefined,
    })
    orders.value = (res.data?.records || []) as OrderRow[]
    orderTotal.value = Number(res.data?.total ?? 0)
  } catch (error: any) {
    ElMessage.error(error.message || '加载医嘱列表失败')
  } finally {
    orderLoading.value = false
  }
}

// ---------------- 待校对 ----------------

const verifyLoading = ref(false)
const verifyRows = ref<OrderRow[]>([])
const verifyTotal = ref(0)
const verifyPagination = ref({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
const verifySelection = ref<OrderRow[]>([])

const loadVerifyRows = async () => {
  verifyLoading.value = true
  try {
    const res = await getInpatientOrderListPage({
      admissionId: admissionId.value || undefined,
      pendingVerifyOnly: 1,
      pageNum: verifyPagination.value.pageNum,
      pageSize: verifyPagination.value.pageSize,
    })
    verifyRows.value = (res.data?.records || []) as OrderRow[]
    verifyTotal.value = Number(res.data?.total ?? 0)
  } catch (error: any) {
    ElMessage.error(error.message || '加载待校对医嘱失败')
  } finally {
    verifyLoading.value = false
  }
}

const handleVerifySelection = (rows: OrderRow[]) => { verifySelection.value = rows }

// ---------------- 待执行 ----------------

const execLoading = ref(false)
const pendingRows = ref<ExecRow[]>([])
const execTotal = ref(0)
const pendingTotal = ref(0)
const pendingPagination = ref({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
const execSelection = ref<ExecRow[]>([])

const loadPendingRows = async () => {
  execLoading.value = true
  try {
    const res = await getOrderExecPendingList({
      admissionId: admissionId.value || undefined,
      pageNum: pendingPagination.value.pageNum,
      pageSize: pendingPagination.value.pageSize,
    })
    pendingRows.value = (res.data?.records || []) as ExecRow[]
    pendingTotal.value = Number(res.data?.total ?? 0)
  } catch (error: any) {
    ElMessage.error(error.message || '加载待执行队列失败')
  } finally {
    execLoading.value = false
  }
}

const handleExecSelection = (rows: ExecRow[]) => { execSelection.value = rows }

// ---------------- 执行记录 ----------------

const logLoading = ref(false)
const logRows = ref<ExecRow[]>([])
const logPagination = ref({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
const logQuery = ref({ execStatus: null as number | null })

const loadLogRows = async () => {
  logLoading.value = true
  try {
    const res = await getOrderExecList({
      admissionId: admissionId.value || undefined,
      execStatus: logQuery.value.execStatus ?? undefined,
      pageNum: logPagination.value.pageNum,
      pageSize: logPagination.value.pageSize,
    })
    logRows.value = (res.data?.records || []) as ExecRow[]
    execTotal.value = Number(res.data?.total ?? 0)
  } catch (error: any) {
    ElMessage.error(error.message || '加载执行记录失败')
  } finally {
    logLoading.value = false
  }
}

// ---------------- 刷新 ----------------

// ---------------- 临床路径软约束（仅医生站：在径横幅 + 开单偏离提示，不拦截） ----------------

const pathEnroll = ref<any>(null)

const todayStr = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const loadPathwayEnroll = async () => {
  pathEnroll.value = null
  if (isNurse.value || !admissionId.value) return
  try {
    const res: any = await getActiveEnrollByAdmission(admissionId.value)
    pathEnroll.value = res?.data || null
  } catch (e) {
    // 路径横幅只是提示，拉不到不弹错、不挡医嘱主线
    console.warn('加载临床路径失败', e)
  }
}

/** 本路径日计划步骤（横幅 chip） */
const pathPlanSteps = computed(() => {
  const e = pathEnroll.value
  if (!e) return [] as any[]
  return (e.steps || []).filter((s: any) => s.dayNo === e.currentDay)
})

const checkPathwayDeviation = async (items: { itemCode?: string; itemName: string }[]) => {
  if (!admissionId.value) return
  try {
    const res: any = await pathwayOrderCheck({
      admissionId: admissionId.value,
      items: items.map(i => ({ itemCode: i.itemCode, itemName: i.itemName })),
    })
    const c = res?.data
    if (!c?.enrolled || !c?.comparable || !(c.deviations || []).length) return
    const names = c.deviations.map((d: any) => d.itemName || d.itemCode).join('、')
    await ElMessageBox.confirm(
      `医嘱已开立，但本单有 ${c.deviations.length} 条不在「${c.pathwayName} ${c.version}」路径计划内：${names}。建议登记变异留痕（当前路径日 ${c.dayNo}/${c.totalDays}）。`,
      '临床路径偏离提示',
      { confirmButtonText: '登记变异', cancelButtonText: '暂不登记', type: 'warning' },
    )
    const { value } = await ElMessageBox.prompt('变异原因（医嘱变动）', '登记变异', {
      inputPattern: /\S+/, inputErrorMessage: '变异原因必填', inputValue: `偏离路径计划：${names}`,
    })
    await varianceUpsert({
      enrollId: c.enrollId, dayNo: c.dayNo, varianceType: 1,
      varianceReason: value.trim(), occurredDate: todayStr(),
    })
    ElMessage.success('变异已登记')
    await loadPathwayEnroll()
  } catch (e: any) {
    if (e !== 'cancel' && e?.message !== 'cancel') console.error('临床路径偏离比对失败', e)
  }
}

const reloadAll = async () => {
  // 待校对 / 待执行是**护士的**动作队列，医生站既没有这两个页签也不该为它们发请求；
  // 待校对/待执行计数同理（计数只喂这两个页签的角标）。
  const tasks: Promise<unknown>[] = [loadOrders(), loadLogRows()]
  if (isNurse.value) tasks.push(loadVerifyRows(), loadPendingRows(), loadStats())
  await Promise.all(tasks)
}

/** 页头「刷新」与换患者：连患者上下文（过敏史 + 检查/检验回报）一起重拉。
 *  医嘱开立/校对/执行之后只走 reloadAll —— 那些动作不改变回报，重复拉只是多三次请求。 */
const refreshAll = async () => {
  await Promise.all([loadAllergy(currentPatientId.value), loadInsRows(), loadLabRows(), loadPathwayEnroll(), reloadAll()])
}

const handleAdmissionChange = async () => {
  orderPagination.value.pageNum = 1
  verifyPagination.value.pageNum = 1
  pendingPagination.value.pageNum = 1
  logPagination.value.pageNum = 1
  insPagination.value.pageNum = 1
  labPagination.value.pageNum = 1
  // 换患者 = 横幅过敏史 + 医技回报 + 医嘱四张表全部重来，不能只刷一半（否则横幅还是上一个人的过敏史）
  await refreshAll()
}

// ---------------- 开立 / 修改医嘱 ----------------

const openDialog = ref(false)
const saving = ref(false)

const emptyItem = (): ItemForm => ({
  orderClass: 1, dictId: '', itemCode: '', itemName: '', spec: '', unit: '',
  dosage: undefined, dosageUnit: '', route: '', frequency: '', quantity: 1, price: undefined,
})

const form = ref({
  /** 空=新开一组；有值=修改这一条「待校对」医嘱（后端 updateOne 只认单条、且不许动组套共享字段） */
  id: '',
  orderType: 2,
  isUrgent: 0,
  orderGroup: '',
  startTime: '',
  planEndTime: '',
  remark: '',
  items: [emptyItem()] as ItemForm[],
})

/** 本次表单内容来自哪个模板（空=手开）。提交时映射成医嘱的 source：2-模板 1-医生 */
const appliedTemplateId = ref('')
const isEditing = computed(() => !!form.value.id)

/** 后端 DATETIME 出参 → el-date-picker 的 value-format（空格分隔，绝不传 ISO T） */
const toPickerTime = (v?: string) => (v ? String(v).replace('T', ' ').slice(0, 19) : '')

const openCreate = () => {
  form.value = { id: '', orderType: 2, isUrgent: 0, orderGroup: '', startTime: '', planEndTime: '', remark: '', items: [emptyItem()] }
  appliedTemplateId.value = ''
  // 下拉的 v-model 也要一起清：不然上次套过的模板还显示在框里，
  // 明细却是空的，再点同一个模板也不触发 change（值没变）→ 医生以为套过了
  applyTemplateId.value = ''
  openDialog.value = true
  loadOrderItemDicts().then(resolveDictIds)
  loadTemplateOptions()
}

/** 修改一条待校对医嘱：整张表单就是这一条项目，组套共享字段只读 */
const openEdit = (row: OrderRow) => {
  form.value = {
    id: String(row.id),
    orderType: row.orderType ?? 2,
    isUrgent: row.isUrgent ?? 0,
    orderGroup: row.orderGroup || '',
    startTime: toPickerTime(row.startTime),
    planEndTime: toPickerTime(row.planEndTime),
    remark: row.remark || '',
    items: [{
      orderClass: row.orderClass ?? 1,
      dictId: '',
      itemCode: row.itemCode || '',
      itemName: row.itemName || '',
      spec: row.spec || '',
      unit: row.unit || '',
      dosage: row.dosage ?? undefined,
      dosageUnit: row.dosageUnit || '',
      route: row.route || '',
      frequency: row.frequency || '',
      quantity: row.quantity ?? 1,
      price: row.price ?? undefined,
    }],
  }
  appliedTemplateId.value = ''
  openDialog.value = true
  loadOrderItemDicts().then(resolveDictIds)
}

const addItem = () => { form.value.items.push(emptyItem()) }
const removeItem = (idx: number) => {
  if (form.value.items.length <= 1) {
    ElMessage.warning('至少保留一条医嘱明细')
    return
  }
  form.value.items.splice(idx, 1)
}

// ---------------- 医嘱项目字典（G1）----------------
// 一次性并行拉三种字典，按 orderClass 分桶给对应行的选择器用。
// 只在第一次打开开立弹窗时加载（懒加载 + 缓存），失败不清空已有手输能力。
const drugOptions = ref<any[]>([])
const inspectionOptions = ref<any[]>([])
const laboratoryOptions = ref<any[]>([])
const dictLoading = ref(false)
const dictLoaded = ref(false)

const loadOrderItemDicts = async () => {
  if (dictLoaded.value || dictLoading.value) return
  dictLoading.value = true
  try {
    const [drugRes, inspRes, labRes] = await Promise.all([
      getDrugSelectList({}),          // 不传 drugType = 全部类型（西药/中成药/中药饮片）
      getInspectionSelectList(),
      getLaboratorySelectList(),
      // 途径/频次/剂量单位（sql/142 起走字典）：与项目字典一起拉，失败各自降级，互不影响
      loadOrderUsageOptions(),
    ])
    drugOptions.value = drugRes?.data || []
    inspectionOptions.value = inspRes?.data || []
    laboratoryOptions.value = labRes?.data || []
    dictLoaded.value = true
  } catch (e) {
    // 字典拉不到就退化成手输，不挡开立 —— 但把原因说出来，别让人以为"字典就是空的"
    console.error('加载医嘱项目字典失败，本次退化为手工录入', e)
    ElMessage.warning('医嘱项目字典加载失败，可手工录入项目名称')
  } finally {
    dictLoading.value = false
  }
}

/** 按行当前类别给候选字典（药品/检查/检验；其余类别无字典，走手输） */
const optionsOfClass = (orderClass: number) => {
  if (orderClass === 1) return drugOptions.value
  if (orderClass === 2) return inspectionOptions.value
  if (orderClass === 3) return laboratoryOptions.value
  return []
}

/**
 * 选中字典项 → 回填该行（val 是选中项的 id 字符串）。
 * 三条必须守住的口径：
 *  · 类别变了要清掉上一类的名称/编码/选中态，否则会出现"药品类别 + 检验项目名"的脏行；
 *  · 单价只做**默认带出**，医生可改（本项目价表与医嘱未强绑，价格开立时快照）；
 *  · 药品的规格/单位/编码必须一起带出，否则计费四核对失去锚点（这正是 G1 要解决的问题）。
 */
const pickDictItem = (idx: number, val: string) => {
  const item = form.value.items[idx]
  if (!item) return
  item.manual = false
  if (!val) {           // 清空选择：名称/编码跟着清掉，避免"有名称没编码"的孤儿行
    item.itemName = ''
    item.itemCode = ''
    return
  }
  const pool = optionsOfClass(item.orderClass)
  const hit = pool.find(o => String(o.id) === String(val))
  if (!hit) return
  item.itemName = hit.drugName || hit.itemName || ''
  item.itemCode = hit.drugCode || hit.itemCode || ''
  item.spec = hit.specification || hit.spec || ''
  item.unit = hit.unit || ''
  const price = hit.retailPrice ?? hit.price
  if (price != null) item.price = Number(price)
}

/** 换类别：清空已选项目，避免跨类脏数据（规格/单位/编码都跟着类别走） */
const onClassChange = (idx: number) => {
  const item = form.value.items[idx]
  if (!item) return
  item.dictId = ''
  item.manual = false
  item.itemCode = ''
  item.itemName = ''
  item.spec = ''
  item.unit = ''
  item.price = undefined
}


/** 按编码回查字典项（模板/医嘱行 → 下拉选中态与现价的唯一桥梁） */
const findDictByCode = (orderClass: number, itemCode?: string) => {
  if (!itemCode) return null
  return optionsOfClass(orderClass).find((o: any) => (o.drugCode || o.itemCode) === itemCode) || null
}

/** 字典池异步到位后，把每行的下拉选中态按编码补回来（否则下拉会显示空白或原始 id） */
const resolveDictIds = () => {
  form.value.items.forEach((item) => {
    if (!item.itemCode) return
    const hit = findDictByCode(item.orderClass, item.itemCode)
    if (hit) {
      item.dictId = String(hit.id)
      item.manual = false
    }
  })
}

// ---------------- 医嘱模板（个人模板：套用 / 另存为 / 管理） ----------------
// 模板不落库到医嘱表：套用只是回填开立表单，医生改完仍走 /order/save（source=2 记"来自模板"）。
// 医嘱的双签、组套同起同停、欠费管控、执行计划与计费快照因此完全不受影响。

const templateOptions = ref<any[]>([])
const templateLoading = ref(false)
/** 开立弹窗顶部「套用模板」的选中值：单选即套用，不做"选中后再点确定"两步 */
const applyTemplateId = ref('')

const loadTemplateOptions = async () => {
  templateLoading.value = true
  try {
    const res = await getOrderTemplateSelectList()
    templateOptions.value = res?.data || []
  } catch (error: any) {
    console.error('加载医嘱模板列表失败:', error)
    templateOptions.value = []
  } finally {
    templateLoading.value = false
  }
}

const applyTemplate = async (id: string) => {
  if (!id) return
  if (isEditing.value) {
    ElMessage.warning('修改单条医嘱时不能套用模板（一次只能改一条项目）')
    applyTemplateId.value = ''
    return
  }
  try {
    const res = await getOrderTemplateById(id)
    const tpl = res?.data
    const rows: any[] = tpl?.items || []
    if (!rows.length) {
      ElMessage.warning('该模板没有明细')
      applyTemplateId.value = ''
      return
    }
    const items: ItemForm[] = []
    const repriced: string[] = []
    const delisted: string[] = []
    rows.forEach((row: any) => {
      const item: ItemForm = {
        ...emptyItem(),
        orderClass: row.orderClass ?? 9,
        itemCode: row.itemCode || '',
        itemName: row.itemName || '',
        spec: row.spec || '',
        unit: row.unit || '',
        dosage: row.dosage ?? undefined,
        dosageUnit: row.dosageUnit || '',
        route: row.route || '',
        frequency: row.frequency || '',
        quantity: row.quantity ?? 1,
        price: row.price ?? undefined,
      }
      const hit = findDictByCode(item.orderClass, item.itemCode)
      if (hit) {
        item.dictId = String(hit.id)
        // 价格以**现价**为准：医嘱价是开立快照，模板里的旧价只是参考
        const now = Number(hit.retailPrice ?? hit.price)
        if (Number.isFinite(now) && now !== Number(item.price ?? 0)) {
          item.price = now
          repriced.push(item.itemName)
        }
      } else if (optionsOfClass(item.orderClass).length) {
        // 该项目所在类别有字典，但编码已查不到（多半是下架了）：
        // 退回手输把名字如实显示出来，让医生自己确认或重选，而不是留一个空下拉让他猜
        item.manual = true
        delisted.push(item.itemName)
      }
      items.push(item)
    })
    form.value.items = items
    if (tpl.orderType) form.value.orderType = tpl.orderType
    appliedTemplateId.value = String(id)
    ElMessage.success(`已套用模板「${tpl.templateName}」，${items.length} 条明细`)
    if (repriced.length) {
      ElMessage.warning(`${repriced.length} 条已按当前价改价（模板存的是录入时参考价）：${repriced.slice(0, 3).join('、')}${repriced.length > 3 ? ' …' : ''}`)
    }
    if (delisted.length) {
      ElMessage.warning(`${delisted.length} 条项目字典里已查不到，请核对名称或重新选择：${delisted.slice(0, 3).join('、')}${delisted.length > 3 ? ' …' : ''}`)
    }
  } catch (error: any) {
    ElMessage.error(error.message || '套用模板失败')
    applyTemplateId.value = ''
  }
}

const saveAsTemplate = async () => {
  const rows = form.value.items.filter(i => (i.itemName || '').trim().length > 0)
  if (!rows.length) {
    ElMessage.warning('当前没有填写医嘱项目，无法存为模板')
    return
  }
  let name = ''
  try {
    const { value } = await ElMessageBox.prompt(
      `把当前 ${rows.length} 条医嘱存为你自己的模板（下次在开立弹窗顶部「套用模板」里选它）。只存项目与用法，不存开始时间与加急。`,
      '另存为模板',
      {
        inputPlaceholder: '如：CAP 初始经验治疗 / 术后镇痛',
        confirmButtonText: '保存模板',
        cancelButtonText: '取消',
        inputValidator: (v: string) => (v && v.trim().length > 0 ? true : '模板名称不能为空'),
      },
    )
    name = (value || '').trim()
  } catch (error: any) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error.message || '获取模板名称失败')
    return
  }
  try {
    const res = await upsertOrderTemplate({
      templateName: name,
      orderType: form.value.orderType,
      items: rows.map(toOrderItemPayload),
    })
    ElMessage.success(`模板「${name}」已保存（${rows.length} 条）`)
    await loadTemplateOptions()
    applyTemplateId.value = String(res.data ?? '')
    if (templateDialog.value) await loadTemplatePage()
  } catch (error: any) {
    ElMessage.error(error.message || '保存模板失败')
  }
}

const templateDialog = ref(false)
const templateRows = ref<any[]>([])
const templateTotal = ref(0)
const templateQuery = ref({ keyword: '', pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
/** 展开预览的模板 id（明细行按点开哪条查哪条，不一次性全拉） */
const previewTemplateId = ref('')
const previewItems = ref<any[]>([])
/** 展开行由「明细」按钮驱动：el-table 的展开状态认行对象引用，翻页/刷新后一律收起 */
const templateTableRef = ref<any>(null)

const collapseTemplatePreview = () => {
  const old = templateRows.value.find(r => String(r.id) === previewTemplateId.value)
  if (old) templateTableRef.value?.toggleRowExpansion(old, false)
  previewTemplateId.value = ''
  previewItems.value = []
}

const openTemplateManager = () => {
  templateDialog.value = true
  previewTemplateId.value = ''
  previewItems.value = []
  loadTemplatePage()
}

const loadTemplatePage = async () => {
  templateLoading.value = true
  // 行对象会被整批换掉，展开状态跟着失效，先收起免得显示上一条的明细
  previewTemplateId.value = ''
  previewItems.value = []
  try {
    const res = await getOrderTemplateListPage({
      keyword: templateQuery.value.keyword.trim() || undefined,
      pageNum: templateQuery.value.pageNum,
      pageSize: templateQuery.value.pageSize,
    })
    templateRows.value = res?.data?.records || []
    templateTotal.value = Number(res?.data?.total ?? 0)
  } catch (error: any) {
    ElMessage.error(error.message || '加载模板列表失败')
  } finally {
    templateLoading.value = false
  }
}

const toggleTemplatePreview = async (row: any) => {
  const wasSame = previewTemplateId.value === String(row.id)
  collapseTemplatePreview()
  if (wasSame) return
  previewTemplateId.value = String(row.id)
  templateTableRef.value?.toggleRowExpansion(row, true)
  try {
    const res = await getOrderTemplateById(row.id)
    previewItems.value = res?.data?.items || []
  } catch (error: any) {
    ElMessage.error(error.message || '加载模板明细失败')
  }
}

const removeTemplate = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `删除模板「${row.templateName}」只影响以后套用，已经按它开出的医嘱不受影响。`,
      '删除模板', { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' },
    )
  } catch (error: any) {
    if (error === 'cancel' || error === 'close') return
    throw error
  }
  try {
    await deleteOrderTemplateById(row.id)
    if (applyTemplateId.value === String(row.id)) applyTemplateId.value = ''
    ElMessage.success('模板已删除')
    await loadTemplatePage()
    await loadTemplateOptions()
  } catch (error: any) {
    ElMessage.error(error.message || '删除模板失败')
  }
}

/** 医嘱明细行 → 后端 InpatientOrderItemDTO（开立、修改、存模板共用同一份映射，避免两处漂移） */
const toOrderItemPayload = (i: ItemForm) => ({
  orderClass: i.orderClass,
  itemCode: i.itemCode || undefined,
  itemName: i.itemName,
  spec: i.spec || undefined,
  unit: i.unit || undefined,
  dosage: i.dosage ?? undefined,
  dosageUnit: i.dosageUnit || undefined,
  route: i.route || undefined,
  frequency: i.frequency || undefined,
  quantity: i.quantity ?? 1,
  price: i.price ?? 0,
})

const submitOrder = async () => {
  if (!admissionId.value) {
    ElMessage.warning('请先选择在院患者')
    return
  }
  const items = form.value.items
    .filter(i => (i.itemName || '').trim().length > 0)
    .map(toOrderItemPayload)
  if (items.length === 0) {
    ElMessage.warning('请至少填写一条医嘱项目名称')
    return
  }
  if (isEditing.value && items.length > 1) {
    ElMessage.warning('修改单条医嘱只能保留一条项目；需要加项目请走「开立医嘱」')
    return
  }
  saving.value = true
  try {
    const res = await saveInpatientOrder({
      // 修改：只带 id 与这一条项目，组套共享字段原样回传（后端拒绝任何变更）
      id: form.value.id || undefined,
      admissionId: admissionId.value,
      orderType: form.value.orderType,
      orderGroup: form.value.orderGroup || undefined,
      isUrgent: form.value.isUrgent,
      startTime: form.value.startTime || undefined,
      planEndTime: form.value.planEndTime || undefined,
      remark: form.value.remark || undefined,
      // 来自模板的医嘱一律记 source=2，事后能统计"模板开立的医嘱"是哪些
      source: isEditing.value ? undefined : (appliedTemplateId.value ? 2 : 1),
      items,
    })
    ElMessage.success(isEditing.value
      ? `医嘱已修改（${res.data}），原开立签名已作废并重签，等待护士校对`
      : `医嘱已开立，组套号 ${res.data}`)
    openDialog.value = false
    await reloadAll()
    // 软约束：开单成功后与在径模板比对，只提示登记变异、不回滚医嘱
    if (!isEditing.value && pathEnroll.value) await checkPathwayDeviation(items)
  } catch (error: any) {
    ElMessage.error(error.message || (isEditing.value ? '修改医嘱失败' : '开立医嘱失败'))
  } finally {
    saving.value = false
  }
}

// ---------------- 校对 / 停止 / 作废 ----------------

const doVerify = async (ids: string[]) => {
  if (ids.length === 0) {
    ElMessage.warning('请先勾选要校对的医嘱')
    return
  }
  try {
    const res = await verifyInpatientOrder({ orderIds: ids })
    ElMessage.success(`已校对 ${res.data} 条医嘱`)
    await reloadAll()
  } catch (error: any) {
    ElMessage.error(error.message || '医嘱校对失败')
  }
}

const doStop = async (row: OrderRow) => {
  try {
    const { value } = await ElMessageBox.prompt(
      `医嘱 ${row.orderNo}（${row.itemName}）${row.orderGroup ? '，同组套医嘱会整组停止' : ''}。停止原因必填：`,
      '停止医嘱', { inputPlaceholder: '如：症状缓解，遵医嘱停用', confirmButtonText: '确认停止', cancelButtonText: '取消' },
    )
    const res = await stopInpatientOrder({ orderId: row.id, stopReason: value })
    ElMessage.success(`已停止 ${res.data} 条医嘱`)
    await reloadAll()
  } catch (error: any) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error.message || '停止医嘱失败')
  }
}

const doCancel = async (row: OrderRow) => {
  try {
    const { value } = await ElMessageBox.prompt(
      `医嘱 ${row.orderNo}（${row.itemName}）${row.orderGroup ? '，属组套的会整组作废' : ''}。作废原因必填：`,
      '作废医嘱', { inputPlaceholder: '如：开错项目', confirmButtonText: '确认作废', cancelButtonText: '取消' },
    )
    await cancelInpatientOrder({ orderId: row.id, cancelReason: value })
    ElMessage.success('医嘱已作废')
    await reloadAll()
  } catch (error: any) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error.message || '作废医嘱失败')
  }
}

// ---------------- 执行 ----------------

const doExecute = async (status: 2 | 3) => {
  const ids = execSelection.value.map(r => r.id)
  if (ids.length === 0) {
    ElMessage.warning('请先勾选要处理的执行记录')
    return
  }
  let note = ''
  if (status === 3) {
    try {
      const { value } = await ElMessageBox.prompt(
        '跳过必须写明原因（飞检问的是「这条医嘱为什么没有执行记录」）：',
        '跳过执行', { inputPlaceholder: '如：患者外出检查未做', confirmButtonText: '确认跳过', cancelButtonText: '取消' },
      )
      note = value
    } catch (error: any) {
      if (error === 'cancel' || error === 'close') return
      throw error
    }
  }
  try {
    const res = await completeOrderExec({
      execIds: ids,
      execStatus: status,
      execNote: note || undefined,
    })
    ElMessage.success(status === 2 ? `已记录执行 ${res.data} 条（已计费）` : `已记录跳过 ${res.data} 条（不计费）`)
    await reloadAll()
  } catch (error: any) {
    ElMessage.error(error.message || '医嘱执行处理失败')
  }
}

const doExecuteOne = async (row: ExecRow, status: 2 | 3) => {
  execSelection.value = [row]
  await doExecute(status)
}

// ---------------- 输液执行闭环（G14） ----------------
// 闭环挂在执行行上：开始（滴速）→ 巡视 → 结束（不良反应）。
// 入口显隐用后端算好的 exec.infusion 布尔（route 关键词口径在后端单点），前端不做 route 匹配。

const infusionDialog = ref(false)
const infusionLoading = ref(false)
const infusionExec = ref<ExecRow | null>(null)
const infusionRounds = ref<any[]>([])
const infusionDripRate = ref<number | undefined>(undefined)
const infusionForm = ref({
  dripRate: undefined as number | undefined,
  remainingVolume: undefined as number | undefined,
  adverseFlag: 0,
  adverseNote: '',
  remark: '',
})

type InfusionPhase = 'none' | 'running' | 'done'

const infusionPhase = (row: ExecRow): InfusionPhase => {
  if (row.infusionEndTime) return 'done'
  if (row.infusionStartTime) return 'running'
  return 'none'
}

const INFUSION_PHASE_TEXT: Record<InfusionPhase, string> = {
  none: '未开始',
  running: '输注中',
  done: '已结束',
}

const openInfusion = async (row: ExecRow) => {
  infusionExec.value = row
  infusionDripRate.value = row.dripRate ?? undefined
  infusionForm.value = { dripRate: undefined, remainingVolume: undefined, adverseFlag: 0, adverseNote: '', remark: '' }
  infusionDialog.value = true
  await loadInfusionRounds()
}

const loadInfusionRounds = async () => {
  if (!infusionExec.value) return
  infusionLoading.value = true
  try {
    const res = await getInfusionRounds(infusionExec.value.id)
    infusionRounds.value = (res.data || []) as any[]
  } catch (error: any) {
    ElMessage.error(error.message || '加载巡视记录失败')
  } finally {
    infusionLoading.value = false
  }
}

const requireExecRow = (): ExecRow => {
  if (!infusionExec.value) throw new Error('请先选择执行行')
  return infusionExec.value
}

const doStartInfusion = async () => {
  const row = requireExecRow()
  if (!infusionDripRate.value) {
    ElMessage.warning('开始输注必须记录滴速（滴/分）')
    return
  }
  try {
    const res = await startInfusion({ execId: row.id, dripRate: infusionDripRate.value })
    ElMessage.success('输注已开始')
    infusionExec.value = { ...infusionExec.value!, ...(res.data as ExecRow) }
    await loadLogRows()
  } catch (error: any) {
    ElMessage.error(error.message || '开始输注失败')
  }
}

const doInfusionRound = async () => {
  const row = requireExecRow()
  try {
    await addInfusionRound({
      execId: row.id,
      dripRate: infusionForm.value.dripRate ?? undefined,
      remainingVolume: infusionForm.value.remainingVolume ?? undefined,
      remark: infusionForm.value.remark || undefined,
    })
    ElMessage.success('巡视已记录')
    infusionForm.value.dripRate = undefined
    infusionForm.value.remainingVolume = undefined
    infusionForm.value.remark = ''
    await loadInfusionRounds()
  } catch (error: any) {
    ElMessage.error(error.message || '记录巡视失败')
  }
}

const doFinishInfusion = async () => {
  const row = requireExecRow()
  if (infusionForm.value.adverseFlag === 1 && !infusionForm.value.adverseNote) {
    ElMessage.warning('标记了不良反应，必须填写描述')
    return
  }
  try {
    const res = await finishInfusion({
      execId: row.id,
      adverseFlag: infusionForm.value.adverseFlag,
      adverseNote: infusionForm.value.adverseFlag === 1 ? infusionForm.value.adverseNote : undefined,
    })
    ElMessage.success('输注已结束')
    infusionExec.value = { ...infusionExec.value!, ...(res.data as ExecRow) }
    await loadLogRows()
  } catch (error: any) {
    ElMessage.error(error.message || '结束输注失败')
  }
}

onMounted(async () => {
  // 先定高再取数：workspaceH 为 0 时布局按内容高度排，接口回来才跳成满屏，
  // 现象就是"进来先空一下、过一秒才撑满"。顶偏移不依赖数据（上面只有面包屑/视图切换），挂载即测准。
  measureWorkspace()
  window.addEventListener('resize', measureWorkspace)

  await Promise.all([
    loadMyDept(),
    // 回报状态文案的字典先备好再渲染表格，否则首屏会闪一下「未知(4)」；护士站没有回报页签，不用拉
    isNurse.value ? Promise.resolve() : loadDictDataMap(
      `${DICT_TYPE.INSPECTION_RECORD_STATUS},${DICT_TYPE.LABORATORY_RECORD_STATUS}`,
    ).then((m) => { recordStatusDict.value = m }),
  ])
  await loadAdmissions()
  await refreshAll()
  await nextTick()
  // 二次校正：横幅/左栏计数文案变化后顶偏移不会动，但切页签与窗口缩放会（见下面的 watch）
  measureWorkspace()
})

onBeforeUnmount(() => window.removeEventListener('resize', measureWorkspace))

// 切页签时表体高度要重算：各页签上方工具条行数不同（换行时高 40 / 80），
// el-table 的 body 高度是挂载时按当时容器算的。
watch(activeTab, async () => {
  await nextTick()
  measureWorkspace()
})
</script>

<template>
  <div ref="rootRef" class="flex items-stretch gap-4" :style="workspaceH ? { height: `${workspaceH}px` } : undefined">
    <!-- ===== 左栏：在院患者工作列表 =====
         医生站与护士站的入口都是「先选患者」，所以只留这一个入口（原先页头那个全院下拉已撤，
         同一件事不留两套实现）。数据边界是本科室/本病区，由后端按登录态强制过滤，
         前端不提供「全部科室」切换（跨科患者走会诊邀请）。 -->
    <aside
      class="flex w-64 shrink-0 flex-col overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm"
      data-testid="p1-admission-list"
    >
      <div class="flex items-center justify-between border-b border-slate-100 px-3 py-2">
        <span class="text-sm font-medium text-slate-700">{{ isNurse ? '本病区在院患者' : '本科室在院患者' }}</span>
        <span class="text-xs text-slate-400" data-testid="p1-admission-total">{{ admissionTotal }} 人</span>
      </div>

      <div class="space-y-2 border-b border-slate-100 p-2">
        <el-input
          v-model="admissionKeyword"
          data-testid="p1-admission-search"
          size="small"
          placeholder="姓名 / 住院号"
          :prefix-icon="Search"
          clearable
          @keyup.enter="searchAdmissions"
          @clear="searchAdmissions"
        />
      </div>

      <div v-loading="admissionLoading" class="min-h-0 flex-1 overflow-y-auto p-1">
        <button
          v-for="a in admissions"
          :key="a.admissionId"
          type="button"
          data-testid="p1-admission-item"
          class="mb-0.5 w-full rounded-md px-2 py-1.5 text-left transition-colors"
          :class="String(a.admissionId) === admissionId ? 'bg-[#1269B5]/10 ring-1 ring-[#1269B5]/40' : 'hover:bg-slate-50'"
          @click="selectAdmission(a)"
        >
          <div class="flex items-center gap-1.5">
            <span class="shrink-0 rounded bg-slate-100 px-1 text-[11px] font-medium text-slate-600">
              {{ a.bedNo || '未分床' }}
            </span>
            <span
              class="truncate text-sm font-medium"
              :class="String(a.admissionId) === admissionId ? 'text-[#1269B5]' : 'text-slate-800'"
            >{{ a.patientName || '—' }}</span>
          </div>
          <div class="mt-0.5 truncate text-xs text-slate-400">
            {{ a.diagnosis || a.mainDiagnosisName || '未录诊断' }} · {{ fmtDate(a.admitTime) }} 入院
          </div>
        </button>

        <div v-if="!admissionLoading && !admissions.length" class="px-2 py-8 text-center text-xs text-slate-400">
          <!-- 空态要说清是「哪个空」：未绑定科室 / 有关键字没命中 / 本科室确实没人 -->
          <p data-testid="p1-admission-empty">{{ !myDeptId
            ? '当前账号未绑定科室，请在系统管理中维护所属科室'
            : (admissionKeyword.trim() ? '本科室没有符合条件的在院患者' : '本科室暂无在院患者') }}</p>
        </div>
      </div>

      <div
        v-if="admissionTotal > admissions.length"
        class="border-t border-slate-100 px-3 py-1.5 text-[11px] leading-4 text-amber-600"
      >共 {{ admissionTotal }} 人，仅显示前 {{ admissions.length }} 人，请用姓名缩小范围</div>
    </aside>

    <!-- ===== 右栏：医嘱工作区 ===== -->
    <div class="flex min-w-0 flex-1 flex-col gap-4">
      <div class="shrink-0">
        <h1 class="text-xl font-semibold text-slate-900">{{ isNurse ? '护士执行站（住院医嘱）' : '住院医生站' }}</h1>
        <p class="mt-1 text-sm text-slate-500">
          {{ isNurse
            ? '在院患者医嘱闭环：校对 → 执行（计费）/ 跳过（必填原因）→ 停止。未校对的医嘱不进执行队列；同一组套同起同停。'
            : '在院患者医嘱闭环：开立 → 护士校对 → 执行（计费）→ 停止/作废。未校对的医嘱不进执行队列；同一组套同起同停。' }}
        </p>
      </div>

      <!-- ===== 患者上下文横幅（常驻页头） =====
           真实 HIS 的住院工作站是「以患者为中心」的：床号、住院号、过敏史、主治、住院天数
           必须一直挂在页头 —— 医生开医嘱时最贵的动作是"回头再查一遍这是谁"。
           原先这里放的是四张全院统计卡（待校对/待执行/医嘱总数/执行记录），数字既不可点也不指向
           任何动作，在单患者工作区里纯属噪声；计数已并到下方页签的角标上，一眼看到且不占高度。 -->
      <div
        class="shrink-0 rounded-lg border border-slate-200 bg-white px-5 py-4 shadow-sm"
        data-testid="p1-current-patient"
      >
        <div class="flex flex-wrap items-start justify-between gap-4">
          <div class="min-w-0">
            <div class="flex flex-wrap items-baseline gap-x-3 gap-y-1">
              <span class="text-2xl font-semibold leading-8 text-slate-900">
                {{ currentAdmission?.bedNo || '未分床' }} · {{ currentAdmission?.patientName || '未选择患者' }}
              </span>
              <span class="text-base text-slate-600">
                {{ patientGenderText(currentAdmission?.gender) }}
                <span class="mx-1 text-slate-300">|</span>
                {{ patientAgeText(currentAdmission?.age) }}
                <span class="mx-1 text-slate-300">|</span>
                住院号 {{ currentAdmission?.admissionNo || '—' }}
                <span class="mx-1 text-slate-300">|</span>
                {{ isNurse ? '病区' : '科室' }} {{ currentAdmission?.wardName || currentAdmission?.deptName || '—' }}
              </span>
            </div>

            <div class="mt-2 flex flex-wrap items-center gap-x-3 gap-y-1 text-sm text-slate-600">
              <span>主治医生 <b class="font-medium text-slate-800">{{ currentAdmission?.doctorName || '—' }}</b></span>
              <span class="text-slate-300">·</span>
              <span>
                入院 {{ fmtTime(currentAdmission?.admitTime) }}
                <b v-if="currentAdmission?.inpatientDays !== undefined && currentAdmission?.inpatientDays !== null" class="font-medium text-slate-800">
                  （已住院 {{ currentAdmission.inpatientDays }} 天）
                </b>
              </span>
              <template v-if="currentAdmission?.insuranceType">
                <span class="text-slate-300">·</span>
                <span>医保 {{ currentAdmission.insuranceType }}</span>
              </template>
              <template v-if="currentAdmission?.patientNo">
                <span class="text-slate-300">·</span>
                <span>患者号 {{ currentAdmission.patientNo }}</span>
              </template>
            </div>

            <div class="mt-2 flex flex-wrap items-center gap-2">
              <span class="text-sm text-slate-500">入院诊断</span>
              <span class="max-w-[46rem] truncate text-sm font-medium text-slate-900">
                {{ diagnosisText }}
              </span>
              <!-- 过敏是**红条级**提示，不是普通字段：真 HIS 把它放在诊断同一行、用色块包起来，
                   因为医生扫一眼就能看到；藏进弹框等于没做。 -->
              <span
                v-if="allergyText"
                class="flex max-w-[34rem] items-center gap-1.5 rounded-md bg-red-50 px-2.5 py-1 text-sm font-medium text-red-700 ring-1 ring-red-200"
              >
                <el-icon class="shrink-0"><Warning /></el-icon>
                <span class="truncate">过敏：{{ allergyText }}</span>
              </span>
              <span v-else-if="currentAdmission" class="text-xs text-slate-400">暂无已知过敏史记录</span>
            </div>
          </div>

          <div class="flex shrink-0 items-center gap-2">
            <el-button :disabled="!currentAdmission" data-testid="p1-patient-profile" @click="patientDialogVisible = true">
              患者全景
            </el-button>
            <el-button :icon="Refresh" @click="refreshAll">刷新</el-button>
          </div>
        </div>
      </div>

      <!-- ===== 临床路径横幅（在径才出现）：当前路径日 + 本日计划步骤 =====
           软约束：只提醒「今天路径上该做什么」、开单后提示登记变异，绝不拦开医嘱。 -->
      <div v-if="pathEnroll"
           class="shrink-0 mt-2 rounded-lg border border-teal-200 bg-teal-50/60 px-5 py-2.5 shadow-sm"
           data-testid="cp-ws-banner">
        <div class="flex flex-wrap items-center gap-x-3 gap-y-1.5 text-sm">
          <span class="font-semibold text-teal-800">临床路径在径</span>
          <span class="text-slate-700">{{ pathEnroll.pathwayName }} {{ pathEnroll.version }}</span>
          <span class="text-slate-300">·</span>
          <span class="text-slate-700">当前路径日
            <b class="text-base font-semibold text-teal-700">{{ pathEnroll.currentDay }}</b>/{{ pathEnroll.totalDays }}
          </span>
          <span class="text-slate-300">·</span>
          <span class="text-slate-500">变异 {{ pathEnroll.varianceCount || 0 }} 次</span>
          <span class="ml-1 text-xs text-slate-400">（入径 {{ pathEnroll.enrollNo }} · {{ pathEnroll.enrollDate }}）</span>
        </div>
        <div class="mt-1.5 flex flex-wrap items-center gap-1.5">
          <span class="text-xs text-slate-500">本日计划</span>
          <span v-for="s in pathPlanSteps" :key="s.id"
                class="rounded-md bg-white px-2 py-0.5 text-xs text-teal-800 ring-1 ring-teal-200">
            {{ s.itemName }}
          </span>
          <span v-if="!pathPlanSteps.length" class="text-xs text-slate-400">本路径日无计划步骤</span>
        </div>
      </div>

    <!-- 工作区主体：页签即动作队列。ws-fill 让卡片撑满剩余高度、表格在自己的面板内滚
         （样式见文件末尾），否则 4 条医嘱下面是一片屏幕空白，不像工作站。 -->
    <el-tabs v-model="activeTab" class="ws-fill rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <!-- ============== 医嘱列表 ============== -->
      <el-tab-pane name="order">
        <template #label>
          <span class="inline-flex items-center gap-1.5">
            医嘱列表
            <span v-if="orderTotal" class="text-xs text-slate-400">({{ orderTotal }})</span>
          </span>
        </template>
        <div class="mb-3 flex flex-wrap items-center gap-3">
          <el-input
            v-model="orderQuery.keyword"
            placeholder="搜索医嘱号 / 项目名称"
            :prefix-icon="Search"
            class="!w-64"
            clearable
            @keyup.enter="loadOrders"
          />
          <el-select v-model="orderQuery.orderType" placeholder="医嘱类型" clearable class="!w-32">
            <el-option v-for="t in orderTypeMap" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
          <el-select v-model="orderQuery.orderStatus" placeholder="医嘱状态" clearable class="!w-32">
            <el-option v-for="t in orderStatusMap" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
          <el-button type="primary" @click="loadOrders">查询</el-button>
          <el-button class="!ml-auto" type="primary" :icon="Plus" v-perm="orderPerm('add')" data-testid="p1-open-order" @click="openCreate">
            开立医嘱
          </el-button>
        </div>

        <!-- 列宽口径：真实 HIS 的医嘱单是「一屏读完一条医嘱」，横向滚动等于让医生左右扫来扫去。
             所以把 组套 并进项目、停止/作废原因 并进状态、开立+校对+双签 合成一列，
             固定列合计压到 ~810px（1680 视口下无横滚）。 -->
        <el-table v-loading="orderLoading" data-testid="p1-order-table" :data="orders" height="100%" style="width: 100%" border>
          <el-table-column prop="orderNo" label="医嘱号" width="130" show-overflow-tooltip />
          <el-table-column label="患者 / 床位" width="100">
            <template #default="{ row }">
              <div class="text-sm font-medium text-slate-800">{{ row.patientName || '—' }}</div>
              <div class="text-xs text-slate-400">{{ row.bedNo || '—' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="类型" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.orderType === 1 ? 'primary' : 'info'" size="small" effect="plain">
                {{ row.orderTypeText }}
              </el-tag>
              <el-tag v-if="row.isUrgent === 1" type="danger" size="small" class="!mt-0.5">加急</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="医嘱内容" min-width="240">
            <template #default="{ row }">
              <div class="text-sm font-medium text-slate-800">
                {{ row.itemName || '—' }}
                <!-- 来源要在表上看得见：套模板开的医嘱事后要能捞出来（哪些方案被反复用、
                     是不是常用那几个），只在开立那一刻提示一下等于没记。 -->
                <el-tag v-if="row.source === 2" type="warning" size="small" effect="plain" class="!ml-1"
                  :data-testid="`p1-source-tag-${row.orderNo}`"
                >
                  {{ row.sourceText || '模板' }}
                </el-tag>
              </div>
              <div class="text-xs text-slate-500">
                {{ row.orderClassText }}
                <span v-if="row.spec"> · {{ row.spec }}</span>
                <span v-if="row.dosage"> · 单次 {{ row.dosage }}{{ row.dosageUnit || '' }} × {{ row.quantity ?? 1 }}</span>
                <span v-if="row.frequency || row.route"> · {{ [row.frequency, row.route].filter(Boolean).join(' · ') }}</span>
              </div>
              <div class="text-xs text-slate-400">
                {{ row.orderGroup ? `组套 ${row.orderGroup}（同起同停）` : '单条（无组套）' }}
                <span v-if="row.startTime"> · 起 {{ fmtTime(row.startTime) }}</span>
                <span v-if="row.stopTime"> · 停 {{ fmtTime(row.stopTime) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="80" align="right">
            <template #default="{ row }">
              <div class="text-sm text-slate-800">{{ fmtMoney(row.amount) }}</div>
              <div class="text-xs text-slate-400">{{ fmtMoney(row.price) }}/{{ row.unit || '次' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.orderStatus)" size="small">{{ row.orderStatusText }}</el-tag>
              <div v-if="row.pendingExecCount" class="mt-0.5 text-xs text-slate-400">待执行 {{ row.pendingExecCount }}</div>
              <!-- 停止/作废原因是**稽核要看的东西**，但它只在已停/已废的行上有意义，
                   单独占一列会让 90% 的行显示「—」，所以挂在状态下面。 -->
              <div v-if="row.stopReason" class="mt-0.5 text-xs text-red-600">{{ row.stopReason }}</div>
            </template>
          </el-table-column>
          <el-table-column label="开立 / 校对（双签）" width="190">
            <template #default="{ row }">
              <div class="text-xs text-slate-600">{{ row.doctorName || '—' }} · {{ fmtTime(row.orderTime) }}</div>
              <div class="text-xs text-slate-400">校对 {{ row.verifyNurseName || '—' }}{{ row.verifyTime ? ` · ${fmtTime(row.verifyTime)}` : '' }}</div>
              <!-- 医嘱是**双签**：医生开立一条、护士校对一条。两个勾必须分开显示 ——
                   合成一个"已签名"会让"护士还没校对"看起来和"校对完了"一样。 -->
              <div class="mt-0.5 text-xs" :data-testid="`p5-order-sign-${row.orderNo}`">
                <el-tag :type="row.doctorSigned ? 'success' : 'info'" size="small" :effect="row.doctorSigned ? 'light' : 'plain'">
                  开立{{ row.doctorSigned ? '已签' : '未签' }}
                </el-tag>
                <el-tag class="ml-1" :type="row.nurseSigned ? 'success' : 'info'" size="small" :effect="row.nurseSigned ? 'light' : 'plain'">
                  校对{{ row.nurseSigned ? '已签' : '未签' }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.canVerify" v-perm="orderPerm('edit')" type="primary" link size="small" @click="doVerify([row.id])">校对</el-button>
              <!-- 修改只对医生开放，且读后端 canEdit（=待校对）：后端 updateOne 早就支持单条改，
                   原先页面上没有入口，医生开错只能作废重开，白留一条作废记录。 -->
              <el-button v-if="!isNurse && row.canEdit" v-perm="'ipd:order:edit'" type="primary" link size="small" data-testid="p1-edit-order" @click="openEdit(row)">修改</el-button>
              <el-button v-if="row.canStop" v-perm="orderPerm('edit')" type="warning" link size="small" @click="doStop(row)">停止</el-button>
              <el-button v-if="row.canCancel" v-perm="orderPerm('delete')" type="danger" link size="small" @click="doCancel(row)">作废</el-button>
              <span v-if="!row.canVerify && !row.canStop && !row.canCancel && !(!isNurse && row.canEdit)" class="text-xs text-slate-400">—</span>
            </template>
          </el-table-column>
          <template #empty>
            <div class="py-6 text-sm text-slate-400">该患者暂无医嘱，点右上「开立医嘱」新建</div>
          </template>
        </el-table>
        <div class="mt-3 flex justify-end">
          <el-pagination
            v-model:current-page="orderPagination.pageNum"
            v-model:page-size="orderPagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="orderTotal"
            layout="total, sizes, prev, pager, next"
            @size-change="loadOrders"
            @current-change="loadOrders"
          />
        </div>
      </el-tab-pane>

      <!-- ============== 待校对（护士站专属：校对是护士的动作，医生站不放这个队列） ============== -->
      <el-tab-pane v-if="isNurse" name="verify">
        <template #label>
          <span class="inline-flex items-center gap-1.5">
            待校对
            <el-tag v-if="stats.pendingVerify" type="danger" size="small" effect="light">{{ stats.pendingVerify }}</el-tag>
          </span>
        </template>
        <div class="mb-3 flex flex-wrap items-center gap-3">
          <el-alert
            type="warning"
            :closable="false"
            show-icon
            title="未校对的医嘱不会进入执行队列（医嘱双人核对的最低要求）。批量校对只要有一条状态不合法就整批拒绝，不做部分成功。"
            class="!py-1"
          />
          <el-button
            type="primary"
            :icon="CircleCheck"
            v-perm="orderPerm('edit')"
            data-testid="p1-verify-selected"
            @click="doVerify(verifySelection.map(r => r.id))"
          >
            批量校对（{{ verifySelection.length }}）
          </el-button>
        </div>
        <el-table
          v-loading="verifyLoading"
          data-testid="p1-verify-table"
          height="100%"
          :data="verifyRows"
          style="width: 100%"
          border
          @selection-change="handleVerifySelection"
        >
          <el-table-column type="selection" width="46" />
          <el-table-column prop="orderNo" label="医嘱号" width="130" show-overflow-tooltip />
          <el-table-column label="患者 / 床位" width="100">
            <template #default="{ row }">
              <div class="text-sm text-slate-800">{{ row.patientName }}</div>
              <div class="text-xs text-slate-400">{{ row.bedNo }}</div>
            </template>
          </el-table-column>
          <el-table-column label="项目" min-width="240">
            <template #default="{ row }">
              <div class="text-sm font-medium text-slate-800">{{ row.itemName }}</div>
              <div class="text-xs text-slate-400">
                {{ row.orderTypeText }} · {{ row.orderClassText }}
                <span v-if="row.dosage"> · {{ row.dosage }}{{ row.dosageUnit }} {{ row.route }}</span>
              </div>
              <!-- 校对是**按组套整体核**的（同起同停），组套号必须在校对列表上直接可见，
                   否则护士只能一条一条点开看它们是不是一组。 -->
              <div class="text-xs text-slate-500">{{ row.orderGroup ? `组套 ${row.orderGroup}（同起同停）` : '单条（无组套）' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="开立（签名）" width="170">
            <template #default="{ row }">
              <div class="text-xs text-slate-600">{{ row.doctorName || '—' }} · {{ fmtTime(row.orderTime) }}</div>
              <!-- 待校对列表特别要看签名：开立名没签上的医嘱在校对前就能发现，
                   不用等到出了纠纷才去翻签名中心。 -->
              <el-tag :type="row.doctorSigned ? 'success' : 'danger'" size="small" :data-testid="`p5-order-verify-sign-${row.orderNo}`">
                开立{{ row.doctorSigned ? '已签' : '未签' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="110" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" v-perm="orderPerm('edit')" @click="doVerify([row.id])">校对</el-button>
              <el-button type="danger" link size="small" v-perm="orderPerm('delete')" @click="doCancel(row)">作废</el-button>
            </template>
          </el-table-column>
          <template #empty><div class="py-6 text-sm text-slate-400">没有待校对医嘱</div></template>
        </el-table>
        <div class="mt-3 flex justify-end">
          <el-pagination
            v-model:current-page="verifyPagination.pageNum"
            v-model:page-size="verifyPagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="verifyTotal"
            layout="total, sizes, prev, pager, next"
            @size-change="loadVerifyRows"
            @current-change="loadVerifyRows"
          />
        </div>
      </el-tab-pane>

      <!-- ============== 待执行（护士站专属：执行/跳过是护士的动作） ============== -->
      <el-tab-pane v-if="isNurse" name="exec">
        <template #label>
          <span class="inline-flex items-center gap-1.5">
            待执行
            <el-tag v-if="stats.pendingExec" type="warning" size="small" effect="light">{{ stats.pendingExec }}</el-tag>
          </span>
        </template>
        <div class="mb-3 flex flex-wrap items-center gap-3">
          <el-alert
            type="info"
            :closable="false"
            show-icon
            title="加急优先、按计划时间升序。已执行会同步计费；跳过必须写明原因且不计费（执行记录一律留痕，不删除）。"
            class="!py-1"
          />
          <el-button
            type="primary"
            :icon="Select"
            v-perm="orderPerm('edit')"
            data-testid="p1-exec-selected"
            @click="doExecute(2)"
          >
            批量执行（{{ execSelection.length }}）
          </el-button>
          <el-button v-perm="orderPerm('edit')" @click="doExecute(3)">批量跳过</el-button>
        </div>
        <el-table
          v-loading="execLoading"
          data-testid="p1-exec-table"
          height="100%"
          :data="pendingRows"
          style="width: 100%"
          border
          @selection-change="handleExecSelection"
        >
          <el-table-column type="selection" width="46" />
          <el-table-column label="计划时间" width="140">
            <template #default="{ row }">
              <div class="text-xs text-slate-700">{{ fmtTime(row.planTime) }}</div>
              <el-tag v-if="row.isUrgent === 1" type="danger" size="small" effect="dark">加急</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="床位 / 患者" width="100">
            <template #default="{ row }">
              <div class="text-sm font-medium text-slate-800">{{ row.bedNo || '—' }}</div>
              <div class="text-xs text-slate-400">{{ row.patientName || '—' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="医嘱内容" min-width="240">
            <template #default="{ row }">
              <div class="text-sm font-medium text-slate-800">
                {{ row.itemName }}
                <el-tag v-if="row.isUrgent === 1" type="danger" size="small" class="!ml-1">加急</el-tag>
              </div>
              <div class="text-xs text-slate-400">
                {{ row.orderTypeText }} · {{ row.orderClassText }} · {{ row.orderNo }}
              </div>
              <div class="text-xs text-slate-500">
                <span v-if="row.dosage">{{ row.dosage }}{{ row.dosageUnit }} {{ row.route }} {{ row.frequency }} × {{ row.quantity }} · </span>开立 {{ row.doctorName || '—' }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="计费" width="110">
            <template #default="{ row }">
              <el-tag :type="row.charged ? 'success' : 'info'" size="small" effect="plain">
                {{ row.charged ? '已计费' : '未计费' }}
              </el-tag>
              <div class="text-xs text-slate-400">{{ fmtMoney(row.amount) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" v-perm="orderPerm('edit')" @click="doExecuteOne(row, 2)">执行</el-button>
              <el-button type="warning" link size="small" v-perm="orderPerm('edit')" @click="doExecuteOne(row, 3)">跳过</el-button>
            </template>
          </el-table-column>
          <template #empty><div class="py-6 text-sm text-slate-400">没有待执行医嘱</div></template>
        </el-table>
        <div class="mt-3 flex justify-end">
          <el-pagination
            v-model:current-page="pendingPagination.pageNum"
            v-model:page-size="pendingPagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="pendingTotal"
            layout="total, sizes, prev, pager, next"
            @size-change="loadPendingRows"
            @current-change="loadPendingRows"
          />
        </div>
      </el-tab-pane>

      <!-- ============== 执行记录 ============== -->
      <el-tab-pane label="执行记录" name="execLog">
        <div class="mb-3 flex flex-wrap items-center gap-3">
          <el-select v-model="logQuery.execStatus" placeholder="执行状态" clearable class="!w-32" @change="loadLogRows">
            <el-option label="待执行" :value="1" />
            <el-option label="已执行" :value="2" />
            <el-option label="已跳过" :value="3" />
          </el-select>
          <el-button type="primary" @click="loadLogRows">查询</el-button>
        </div>
        <el-table v-loading="logLoading" data-testid="p1-execlog-table" :data="logRows" height="100%" style="width: 100%" border>
          <el-table-column label="计划 / 执行" width="150">
            <template #default="{ row }">
              <div class="text-xs text-slate-500">计划 {{ fmtTime(row.planTime) }}</div>
              <div class="text-xs text-slate-700">执行 {{ fmtTime(row.execTime) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="患者 / 床位" width="100">
            <template #default="{ row }">
              <div class="text-sm text-slate-800">{{ row.patientName }}</div>
              <div class="text-xs text-slate-400">{{ row.bedNo }}</div>
            </template>
          </el-table-column>
          <el-table-column label="医嘱内容" min-width="220">
            <template #default="{ row }">
              <div class="text-sm text-slate-800">{{ row.itemName }}</div>
              <div class="text-xs text-slate-400">
                {{ row.orderNo }} · {{ row.orderClassText }} · 第 {{ row.execSeq }} 次 · 医嘱{{ row.orderStatusText || '—' }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="执行状态" width="150">
            <template #default="{ row }">
              <el-tag :type="row.execStatus === 2 ? 'success' : row.execStatus === 3 ? 'warning' : 'info'" size="small">
                {{ row.execStatusText }}
              </el-tag>
              <div v-if="row.execNurseName" class="text-xs text-slate-400">{{ row.execNurseName }}</div>
              <!-- 跳过原因与执行状态是一回事的两半（"没执行"必须当场说清为什么），
                   单独开一列会让 90% 的行显示「—」。 -->
              <div v-if="row.execNote" class="text-xs" :class="row.execStatus === 3 ? 'text-red-600' : 'text-slate-500'">
                {{ row.execNote }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="输液闭环" width="110">
            <template #default="{ row }">
              <template v-if="row.infusion">
                <el-tag
                  :type="infusionPhase(row) === 'done' ? 'info' : infusionPhase(row) === 'running' ? 'success' : 'warning'"
                  size="small" data-testid="g14-infusion-tag"
                >
                  {{ INFUSION_PHASE_TEXT[infusionPhase(row)] }}
                </el-tag>
                <div v-if="row.dripRate" class="text-xs text-slate-400">{{ row.dripRate }} 滴/分</div>
              </template>
              <span v-else class="text-xs text-slate-300">—</span>
            </template>
          </el-table-column>
          <el-table-column label="计费" width="120">
            <template #default="{ row }">
              <el-tag :type="row.charged ? 'success' : 'info'" size="small" effect="plain">
                {{ row.charged ? '已计费' : '未计费' }}
              </el-tag>
              <div v-if="row.chargeNo" class="text-xs text-slate-400">{{ row.chargeNo }}</div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.infusion && row.execStatus === 2"
                type="primary" link size="small" data-testid="g14-open-infusion"
                @click="openInfusion(row)"
              >
                输液
              </el-button>
            </template>
          </el-table-column>
          <template #empty><div class="py-6 text-sm text-slate-400">暂无执行记录</div></template>
        </el-table>
        <div class="mt-3 flex justify-end">
          <el-pagination
            v-model:current-page="logPagination.pageNum"
            v-model:page-size="logPagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="execTotal"
            layout="total, sizes, prev, pager, next"
            @size-change="loadLogRows"
            @current-change="loadLogRows"
          />
        </div>
      </el-tab-pane>

      <!-- ============== 检验回报（仅医生站） ==============
           真实 HIS 的住院医生站必定带「检验 / 检查回报」：医生开完医嘱之后的下一个动作就是看回报，
           原先只能在护士站之外另开医技工作站，等于把医生的工作流截成两段。 -->
      <el-tab-pane v-if="!isNurse" label="检验回报" name="lab">
        <div class="mb-3 flex flex-wrap items-center gap-3">
          <p class="text-sm text-slate-500">
            按患者查询，含门诊与住院的历史回报；异常项由后端判定并给文案，前端不参与判读。
          </p>
          <el-button :icon="Refresh" size="small" @click="loadLabRows">刷新回报</el-button>
        </div>

        <el-table v-loading="labLoading" data-testid="p1-lab-table" :data="labRows" height="100%" style="width: 100%" border>
          <el-table-column prop="recordNo" label="检验号" width="140" show-overflow-tooltip />
          <el-table-column label="项目 / 标本" min-width="200">
            <template #default="{ row }">
              <div class="text-sm font-medium text-slate-800">{{ row.laboratoryItemName || '—' }}</div>
              <div class="text-xs text-slate-400">
                {{ row.specimenType || '无标本' }}
                <span v-if="row.specimenNo"> · {{ row.specimenNo }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="labTagType(row.recordStatus)" size="small">
                {{ labStatusText(row.recordStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="开单" width="120">
            <template #default="{ row }">
              <div class="text-xs text-slate-600">{{ row.applyDoctorName || '—' }}</div>
              <div class="text-xs text-slate-400">{{ row.applyDeptName || '—' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="采样 / 报告" width="160">
            <template #default="{ row }">
              <div class="text-xs text-slate-600">采样 {{ fmtTime(row.sampleTime) }}</div>
              <div class="text-xs text-slate-400">报告 {{ fmtTime(row.reportTime) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="结论" min-width="180">
            <template #default="{ row }">
              <span class="text-sm text-slate-700">{{ row.suggestions || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" :data-testid="`p1-lab-detail-${row.id}`" @click="openLabDetail(row)">
                查看报告
              </el-button>
            </template>
          </el-table-column>
          <template #empty>
            <div class="py-6 text-sm text-slate-400">{{ currentPatientId ? '该患者暂无检验申请与回报' : '请先在左侧选择患者' }}</div>
          </template>
        </el-table>
        <div class="mt-3 flex justify-end">
          <el-pagination
            v-model:current-page="labPagination.pageNum"
            v-model:page-size="labPagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="labTotal"
            layout="total, sizes, prev, pager, next"
            @size-change="loadLabRows"
            @current-change="loadLabRows"
          />
        </div>
      </el-tab-pane>

      <!-- ============== 检查回报（仅医生站） ============== -->
      <el-tab-pane v-if="!isNurse" label="检查回报" name="inspection">
        <div class="mb-3 flex flex-wrap items-center gap-3">
          <p class="text-sm text-slate-500">
            按患者查询影像与功能检查回报；「检查所见 / 结论」为报告原文，未出报告时为空。
          </p>
          <el-button :icon="Refresh" size="small" @click="loadInsRows">刷新回报</el-button>
        </div>

        <el-table v-loading="insLoading" data-testid="p1-inspection-table" :data="insRows" height="100%" style="width: 100%" border>
          <el-table-column prop="recordNo" label="检查号" width="140" show-overflow-tooltip />
          <el-table-column label="项目 / 部位" min-width="200">
            <template #default="{ row }">
              <div class="text-sm font-medium text-slate-800">{{ row.inspectionItemName || '—' }}</div>
              <div class="text-xs text-slate-400">{{ row.bodyPart || '—' }}<span v-if="row.inspectionDeptName"> · {{ row.inspectionDeptName }}</span></div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="insTagType(row.recordStatus)" size="small">
                {{ insStatusText(row.recordStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="开单" width="120">
            <template #default="{ row }">
              <div class="text-xs text-slate-600">{{ row.applyDoctorName || '—' }}</div>
              <div class="text-xs text-slate-400">{{ row.applyDeptName || '—' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="预约 / 报告" width="160">
            <template #default="{ row }">
              <div class="text-xs text-slate-600">预约 {{ fmtTime(row.appointmentTime) }}</div>
              <div class="text-xs text-slate-400">报告 {{ fmtTime(row.reportTime) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="结论摘要" min-width="200">
            <template #default="{ row }">
              <span class="text-sm text-slate-700">{{ row.resultConclusion || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" :data-testid="`p1-ins-detail-${row.id}`" @click="openInsDetail(row)">
                查看报告
              </el-button>
            </template>
          </el-table-column>
          <template #empty>
            <div class="py-6 text-sm text-slate-400">{{ currentPatientId ? '该患者暂无检查申请与回报' : '请先在左侧选择患者' }}</div>
          </template>
        </el-table>
        <div class="mt-3 flex justify-end">
          <el-pagination
            v-model:current-page="insPagination.pageNum"
            v-model:page-size="insPagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="insTotal"
            layout="total, sizes, prev, pager, next"
            @size-change="loadInsRows"
            @current-change="loadInsRows"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- ============== 患者全景弹框（全站唯一实现，数据源 = 主档详情 + CDR 时间轴） ============== -->
    <PatientDetailDialog
      v-model="patientDialogVisible"
      :patient-id="currentPatientId"
      :patient="currentAdmission ? { patientId: currentAdmission.patientId, patientName: currentAdmission.patientName } : null"
    />

    <!-- ============== 检验报告详情（只读：结果明细 + 判定文案来自后端） ============== -->
    <el-dialog v-model="labDetailDialog" title="检验报告" width="860px" top="6vh">
      <div v-loading="labDetailLoading" class="min-h-[10rem]">
        <template v-if="labDetail">
          <div class="flex flex-wrap items-baseline gap-x-4 gap-y-1 text-sm">
            <span class="text-lg font-semibold text-slate-900">{{ labDetail.record?.laboratoryItemName || '—' }}</span>
            <span class="text-slate-600">{{ labDetail.record?.recordNo || '—' }}</span>
            <span class="text-slate-500">{{ labStatusText(labDetail.record?.recordStatus) }}</span>
            <span class="text-slate-500">标本 {{ labDetail.record?.specimenType || '—' }}</span>
            <span class="text-slate-500">采样 {{ fmtTime(labDetail.record?.sampleTime) }}</span>
            <span class="text-slate-500">报告 {{ fmtTime(labDetail.record?.reportTime) }}</span>
          </div>

          <el-table :data="labDetail.results || []" class="mt-3" border size="small" data-testid="p1-lab-detail-table">
            <el-table-column prop="laboratoryItemName" label="项目" min-width="160" />
            <el-table-column label="结果" width="120" align="right">
              <template #default="{ row }">
                <span class="font-medium text-slate-900">{{ row.resultValue ?? '—' }}</span>
                <span class="ml-1 text-xs text-slate-400">{{ row.resultUnit || '' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="referenceRange" label="参考区间" width="150" align="right" />
            <el-table-column label="判定" width="90" align="center">
              <template #default="{ row }">
                <el-tag
                  size="small"
                  :type="row.abnormalFlagText === '正常' || !row.abnormalFlagText ? 'info' : 'danger'"
                  :effect="row.abnormalFlagText === '正常' || !row.abnormalFlagText ? 'plain' : 'light'"
                >
                  {{ row.abnormalFlagText || '—' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="备注 / 判定依据" min-width="160">
              <template #default="{ row }">
                <span class="text-xs text-slate-500">{{ row.judgeNote || row.remark || '—' }}</span>
              </template>
            </el-table-column>
            <template #empty><div class="py-6 text-sm text-slate-400">该申请尚未录入结果明细</div></template>
          </el-table>

          <div v-if="labDetail.report" class="mt-3 rounded-md bg-slate-50 p-3 text-sm text-slate-700">
            <p><span class="text-slate-500">报告号</span> {{ labDetail.report.reportNo || '—' }}</p>
            <p v-if="labDetail.report.conclusion"><span class="text-slate-500">结论</span> {{ labDetail.report.conclusion }}</p>
            <p v-if="labDetail.report.suggestions"><span class="text-slate-500">建议</span> {{ labDetail.report.suggestions }}</p>
            <p class="text-xs text-slate-400">审核 {{ labDetail.report.auditBy || '—' }} · {{ fmtTime(labDetail.report.auditTime) }}</p>
          </div>
        </template>
        <p v-else-if="!labDetailLoading" class="text-sm text-slate-500">未取到报告内容，可关闭后重试。</p>
      </div>
      <template #footer><el-button @click="labDetailDialog = false">关闭</el-button></template>
    </el-dialog>

    <!-- ============== 检查报告详情（只读，字段来自列表接口，无需二次请求） ============== -->
    <el-dialog v-model="insDetailDialog" title="检查报告" width="760px" top="8vh">
      <div v-if="insDetail" class="space-y-3 text-sm">
        <div class="flex flex-wrap items-baseline gap-x-4 gap-y-1">
          <span class="text-lg font-semibold text-slate-900">{{ insDetail.inspectionItemName || '—' }}</span>
          <span class="text-slate-600">{{ insDetail.recordNo || '—' }}</span>
          <span class="text-slate-500">{{ insStatusText(insDetail.recordStatus) }}</span>
          <span class="text-slate-500">部位 {{ insDetail.bodyPart || '—' }}</span>
          <span class="text-slate-500">执行 {{ fmtTime(insDetail.executeTime) }}</span>
        </div>
        <div v-if="insDetail.clinicalDiagnosis">
          <p class="text-xs text-slate-500">临床诊断</p>
          <p class="text-slate-800">{{ insDetail.clinicalDiagnosis }}</p>
        </div>
        <div v-if="insDetail.inspectionPurpose">
          <p class="text-xs text-slate-500">检查目的</p>
          <p class="text-slate-800">{{ insDetail.inspectionPurpose }}</p>
        </div>
        <div>
          <p class="text-xs text-slate-500">检查所见</p>
          <p class="whitespace-pre-wrap text-slate-800">{{ insDetail.resultDescription || '暂未填写' }}</p>
        </div>
        <div>
          <p class="text-xs text-slate-500">检查结论</p>
          <p class="whitespace-pre-wrap font-medium text-slate-900">{{ insDetail.resultConclusion || '暂未出具' }}</p>
        </div>
      </div>
      <template #footer><el-button @click="insDetailDialog = false">关闭</el-button></template>
    </el-dialog>

    <!-- ============== 输液执行闭环弹窗（G14） ============== -->
    <el-dialog v-model="infusionDialog" title="输液执行闭环（开始 → 巡视 → 结束）" width="680px" top="6vh">
      <div v-if="infusionExec" class="mb-3 rounded border border-slate-200 bg-slate-50 p-3 text-sm">
        <span class="font-medium text-slate-800">{{ infusionExec.patientName }}</span>
        <span class="ml-2 text-xs text-slate-500">床 {{ infusionExec.bedNo || '—' }} · {{ infusionExec.itemName }} · 第 {{ infusionExec.execSeq }} 次</span>
        <el-tag class="ml-2" size="small" :type="infusionPhase(infusionExec) === 'done' ? 'info' : infusionPhase(infusionExec) === 'running' ? 'success' : 'warning'"
          data-testid="g14-infusion-phase">
          {{ INFUSION_PHASE_TEXT[infusionPhase(infusionExec)] }}
        </el-tag>
      </div>

      <div class="grid grid-cols-1 gap-3 md:grid-cols-3">
        <!-- ① 开始 -->
        <div class="rounded border border-slate-200 p-3" data-testid="g14-infusion-start">
          <p class="mb-2 text-sm font-medium text-slate-700">① 开始输注</p>
          <template v-if="infusionExec && !infusionExec.infusionStartTime">
            <el-input-number v-model="infusionDripRate" :min="1" :max="300" controls-position="right"
              placeholder="滴速" style="width: 100%" />
            <el-button type="primary" class="mt-2 !w-full" v-perm="orderPerm('edit')" data-testid="g14-do-start" @click="doStartInfusion">开始输注</el-button>
          </template>
          <p v-else class="text-xs text-slate-500" data-testid="g14-started-at">
            {{ infusionExec?.infusionStartTime ? `已于 ${String(infusionExec.infusionStartTime).replace('T', ' ').slice(0, 16)} 开始（${infusionExec.dripRate ?? '—'} 滴/分）` : '—' }}
          </p>
        </div>
        <!-- ② 巡视 -->
        <div class="rounded border border-slate-200 p-3" data-testid="g14-infusion-round">
          <p class="mb-2 text-sm font-medium text-slate-700">② 巡视</p>
          <template v-if="infusionExec && infusionExec.infusionStartTime && !infusionExec.infusionEndTime">
            <el-input-number v-model="infusionForm.dripRate" :min="1" :max="300" controls-position="right"
              placeholder="当前滴速（滴/分）" style="width: 100%" />
            <el-input-number v-model="infusionForm.remainingVolume" :min="0" :max="5000" controls-position="right"
              placeholder="余量 ml" style="width: 100%" class="mt-2" />
            <el-input v-model="infusionForm.remark" placeholder="穿刺部位/局部情况" class="mt-2" />
            <el-button type="primary" class="mt-2 !w-full" v-perm="orderPerm('add')" data-testid="g14-do-round" @click="doInfusionRound">记录巡视</el-button>
          </template>
          <p v-else class="text-xs text-slate-400">{{ infusionExec?.infusionEndTime ? '该袋已结束' : '先「开始输注」再巡视' }}</p>
        </div>
        <!-- ③ 结束 -->
        <div class="rounded border border-slate-200 p-3" data-testid="g14-infusion-finish">
          <p class="mb-2 text-sm font-medium text-slate-700">③ 结束输注</p>
          <template v-if="infusionExec && infusionExec.infusionStartTime && !infusionExec.infusionEndTime">
            <el-radio-group v-model="infusionForm.adverseFlag">
              <el-radio :value="0">无不良反应</el-radio>
              <el-radio :value="1">有不良反应</el-radio>
            </el-radio-group>
            <el-input v-if="infusionForm.adverseFlag === 1" v-model="infusionForm.adverseNote" type="textarea" :rows="2"
              placeholder="不良反应描述（必填，事后追溯的起点）" class="mt-2" />
            <el-button type="warning" class="mt-2 !w-full" v-perm="orderPerm('edit')" data-testid="g14-do-finish" @click="doFinishInfusion">结束输注</el-button>
          </template>
          <p v-else class="text-xs text-slate-500" data-testid="g14-finished-at">
            {{ infusionExec?.infusionEndTime ? `已于 ${String(infusionExec.infusionEndTime).replace('T', ' ').slice(0, 16)} 结束${infusionExec.adverseFlag === 1 ? '（有不良反应：' + (infusionExec.adverseNote || '—') + '）' : '（无不良反应）'}` : '—' }}
          </p>
        </div>
      </div>

      <div class="mt-4">
        <p class="mb-2 text-sm font-medium text-slate-700">巡视记录（时间升序，共 {{ infusionRounds.length }} 条）</p>
        <el-timeline v-if="infusionRounds.length" data-testid="g14-rounds">
          <el-timeline-item v-for="r in infusionRounds" :key="r.id" :timestamp="String(r.roundTime || '').replace('T', ' ').slice(0, 16)">
            <span v-if="r.dripRate" class="text-sm text-slate-700">{{ r.dripRate }} 滴/分</span>
            <span v-if="r.remainingVolume != null" class="ml-2 text-sm text-slate-700">余 {{ r.remainingVolume }} ml</span>
            <span class="ml-2 text-xs text-slate-400">{{ r.roundNurseName || '' }} {{ r.remark || '' }}</span>
          </el-timeline-item>
        </el-timeline>
        <div v-else class="py-3 text-center text-sm text-slate-400">暂无巡视记录</div>
      </div>
    </el-dialog>

    <!-- ============== 开立 / 修改医嘱弹窗 ============== -->
    <el-dialog v-model="openDialog" :title="isEditing ? '修改待校对医嘱（仅本条项目，组套共享字段不可改）' : '开立住院医嘱（一次提交 = 一个组套）'" width="1000px" top="6vh">
      <el-form label-width="90px">
        <div class="grid grid-cols-1 gap-x-4 md:grid-cols-3">
          <el-form-item label="医嘱类型">
            <el-select v-model="form.orderType" data-testid="p1-form-type" class="!w-full" :disabled="isEditing">
              <el-option v-for="t in orderTypeMap" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="加急">
            <el-select v-model="form.isUrgent" class="!w-full" :disabled="isEditing">
              <el-option label="普通" :value="0" />
              <el-option label="加急" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item label="组套号">
            <el-input v-model="form.orderGroup" :disabled="isEditing" placeholder="留空由系统生成；同一组套同起同停" />
          </el-form-item>
          <el-form-item label="开始时间">
            <el-date-picker
              v-model="form.startTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              :disabled="isEditing"
              placeholder="留空取当前时间"
              class="!w-full"
            />
          </el-form-item>
          <el-form-item label="计划结束">
            <el-date-picker
              v-model="form.planEndTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="长期医嘱可留空"
              class="!w-full"
            />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.remark" placeholder="选填" />
          </el-form-item>
        </div>

        <div class="mb-2 flex flex-wrap items-center justify-between gap-2">
          <span class="text-sm font-medium text-slate-700">医嘱明细</span>
          <div class="flex flex-wrap items-center gap-2">
            <!-- 模板只服务医生：后端模板接口要 ipd:order:*，护士挂上入口也只会拿到 403 -->
            <template v-if="!isNurse">
              <!-- 单选即套用（不做"选中再点确定"两步）。价格按现价重取，开始时间与加急不由模板决定。 -->
              <el-select
                v-if="!isEditing"
                v-model="applyTemplateId"
                data-testid="p1-apply-template"
                size="small"
                class="!w-60"
                :fit-input-width="false"
                filterable
                clearable
                :loading="templateLoading"
                placeholder="套用我的模板"
                @change="applyTemplate"
              >
                <el-option v-for="t in templateOptions" :key="t.id" :label="t.templateName" :value="String(t.id)">
                  <span>{{ t.templateName }}</span>
                  <span class="float-right text-xs text-slate-400">{{ t.itemCount }} 条 · {{ t.orderTypeText }}</span>
                </el-option>
                <template #empty>
                  <div class="px-3 py-2 text-xs text-slate-400">还没有模板：填好下面的明细后点「另存为模板」</div>
                </template>
              </el-select>
              <el-button v-if="!isEditing" size="small" :icon="Collection" v-perm="'ipd:order:list'" data-testid="p1-template-manager" @click="openTemplateManager">
                模板管理
              </el-button>
              <el-button size="small" v-perm="'ipd:order:add'" data-testid="p1-save-template" @click="saveAsTemplate">另存为模板</el-button>
            </template>
            <el-button v-if="!isEditing" size="small" type="primary" plain :icon="Plus" @click="addItem">加一条</el-button>
          </div>
        </div>

        <div v-for="(item, idx) in form.items" :key="idx" class="mb-3 rounded border border-slate-200 bg-slate-50 p-3">
          <div class="grid grid-cols-2 gap-x-3 gap-y-2 md:grid-cols-4">
            <el-form-item label="类别" label-width="56px" class="!mb-0">
              <el-select v-model="item.orderClass" data-testid="p1-item-class" class="!w-full" @change="onClassChange(idx)">
                <el-option v-for="c in orderClassMap" :key="c.value" :label="c.label" :value="c.value" />
              </el-select>
            </el-form-item>
            <!-- 项目：有字典的类别（药品/检查/检验）给可搜索下拉，选中自动带出编码/规格/单位/单价；
                 无字典的类别（治疗/护理/手术/输血/监护/其他/临床营养）退回手输，但保留同一 testid 便于验证。
                 item.manual：套用模板时该编码在字典里已查不到（下架），也走手输 ——
                 否则下拉因为值匹配不到任何选项会显示成空白，医生看不见项目名却把旧名提交上去。
                 ⚠ v-model 绑的是**选中项 id**（字符串），不是 itemCode —— 两者若混用，下拉会
                 因为"值匹配不到任何选项"而回显原始 id。itemCode 由 pickDictItem 回填。 -->
            <el-form-item label="项目" label-width="56px" class="!mb-0 md:col-span-2">
              <el-select
                  v-if="optionsOfClass(item.orderClass).length && !item.manual"
                  v-model="item.dictId"
                  data-testid="p1-item-name"
                  filterable
                  clearable
                  class="!w-full"
                  :loading="dictLoading"
                  :fit-input-width="false"
                  placeholder="搜索并选择项目（可输入名称筛选）"
                  @change="(v: string) => pickDictItem(idx, v)"
              >
                <el-option
                    v-for="o in optionsOfClass(item.orderClass)"
                    :key="o.id"
                    :label="o.drugName || o.itemName"
                    :value="String(o.id)"
                >
                  <span>{{ o.drugName || o.itemName }}</span>
                  <span class="float-right text-xs text-slate-400">
                    {{ o.specification || o.spec || '' }}
                    <template v-if="o.retailPrice != null || o.price != null"> ¥{{ o.retailPrice ?? o.price }}</template>
                  </span>
                </el-option>
              </el-select>
              <el-input v-else v-model="item.itemName" data-testid="p1-item-name" :placeholder="item.manual ? '项目名称（字典里已查不到该编码，请核对或改用下拉重选）' : '项目/药品名称（必填，本类别无字典，手工录入）'" />
            </el-form-item>
            <el-form-item label="编码" label-width="56px" class="!mb-0">
              <el-input v-model="item.itemCode" placeholder="选项目自动带出" />
            </el-form-item>
            <el-form-item label="规格" label-width="56px" class="!mb-0">
              <el-input v-model="item.spec" placeholder="如 1.0g" />
            </el-form-item>
            <el-form-item label="单位" label-width="56px" class="!mb-0">
              <el-input v-model="item.unit" placeholder="支/次" />
            </el-form-item>
            <el-form-item label="剂量" label-width="56px" class="!mb-0">
              <el-input v-model="item.dosage" placeholder="药品必填" />
            </el-form-item>
            <!-- 剂量单位/途径/频次：口径单点在 lib/drugUsage.js（sql/142 起从字典读，读不到用内置候选） -->
            <el-form-item label="剂量单位" label-width="76px" class="!mb-0">
              <el-select v-model="item.dosageUnit" filterable allow-create clearable class="!w-full" placeholder="g/ml">
                <el-option v-for="u in dosageUnitOptions" :key="u" :label="u" :value="u" />
              </el-select>
            </el-form-item>
            <el-form-item label="途径" label-width="56px" class="!mb-0">
              <el-select v-model="item.route" filterable allow-create clearable class="!w-full" placeholder="口服/静滴">
                <el-option v-for="r in routeOptions" :key="r" :label="r" :value="r" />
              </el-select>
            </el-form-item>
            <el-form-item label="频次" label-width="56px" class="!mb-0">
              <el-select v-model="item.frequency" filterable allow-create clearable class="!w-full" placeholder="qd/bid">
                <el-option v-for="f in frequencyOptions" :key="f.value" :label="f.label" :value="f.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="数量" label-width="56px" class="!mb-0">
              <el-input v-model="item.quantity" />
            </el-form-item>
            <el-form-item label="单价" label-width="56px" class="!mb-0">
              <el-input v-model="item.price" data-testid="p1-item-price" placeholder="选项目自动带出，可改" />
            </el-form-item>
            <div class="flex items-center">
              <el-button v-if="!isEditing" type="danger" link :icon="Delete" @click="removeItem(idx)">删除本行</el-button>
              <span v-else class="text-xs text-slate-400">修改只改这一条</span>
            </div>
          </div>
        </div>

        <el-alert
          type="info"
          :closable="false"
          show-icon
          :title="isEditing
            ? '修改会把原开立签名作废并按新内容重签（绑的是旧内容），护士那边重新按「待校对」处理。'
            : '药品/检查/检验请从字典选择（自动带出编码·规格·单位·单价，计费与审方靠它对齐）；药品医嘱必须填全「单次剂量 / 剂量单位 / 给药途径」，缺一个护士执行时只能靠猜，这是后端硬规则。填好后可「另存为模板」，下次在上方「套用我的模板」一键带出（模板只存项目与用法，价格按现价重取）。'
          "
        />
      </el-form>
      <template #footer>
        <el-button @click="openDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" v-perm="orderPerm('add')" data-testid="p1-submit-order" @click="submitOrder">
          {{ isEditing ? '保存修改' : '提交医嘱' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ============== 医嘱模板管理（医生个人） ============== -->
    <el-dialog v-model="templateDialog" title="我的医嘱模板" width="900px" top="8vh">
      <div class="mb-3 flex flex-wrap items-center gap-3">
        <el-input
          v-model="templateQuery.keyword"
          data-testid="p1-template-search"
          size="small"
          class="!w-64"
          placeholder="模板名称 / 适用场景"
          :prefix-icon="Search"
          clearable
          @keyup.enter="templateQuery.pageNum = 1; loadTemplatePage()"
        />
        <el-button size="small" type="primary" @click="templateQuery.pageNum = 1; loadTemplatePage()">查询</el-button>
        <span class="text-xs text-slate-400">模板只属于你的账户，不会出现在别人的下拉里；删除只影响以后套用，已开出的医嘱不受影响。</span>
      </div>

      <el-table
        ref="templateTableRef"
        v-loading="templateLoading"
        data-testid="p1-template-table"
        :data="templateRows"
        row-key="id"
        style="width: 100%"
        border
      >
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="px-6 py-2">
              <p v-if="previewTemplateId !== String(row.id)" class="text-xs text-slate-400">点上方「明细」加载模板项目</p>
              <template v-else>
                <p v-if="!previewItems.length" class="text-xs text-slate-400">没有明细</p>
                <el-table v-else :data="previewItems" size="small" border max-height="220" style="width: 100%">
                  <el-table-column label="类别" width="80">
                    <template #default="scope"><span class="text-xs">{{ scope.row.orderClassText }}</span></template>
                  </el-table-column>
                  <el-table-column label="项目" min-width="200">
                    <template #default="scope">
                      <div class="text-sm text-slate-800">{{ scope.row.itemName }}</div>
                      <div class="text-xs text-slate-400">{{ scope.row.itemCode || '无编码' }}{{ scope.row.spec ? ' · ' + scope.row.spec : '' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="用法" min-width="180">
                    <template #default="scope">
                      <span class="text-xs text-slate-600">
                        {{ scope.row.dosage ? scope.row.dosage + (scope.row.dosageUnit || '') : '' }}
                        {{ scope.row.route || '' }} {{ scope.row.frequency || '' }}
                        × {{ scope.row.quantity ?? 1 }}{{ scope.row.unit || '' }}
                      </span>
                    </template>
                  </el-table-column>
                  <el-table-column label="参考价" width="100" align="right">
                    <template #default="scope"><span class="text-xs">{{ fmtMoney(scope.row.price) }}</span></template>
                  </el-table-column>
                </el-table>
              </template>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="templateName" label="模板名称" min-width="180" />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.orderType === 1 ? 'primary' : 'info'" size="small" effect="plain">{{ row.orderTypeText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="itemCount" label="条数" width="70" align="center" />
        <el-table-column label="适用场景 / 备注" min-width="180">
          <template #default="{ row }"><span class="text-xs text-slate-500">{{ row.remark || '—' }}</span></template>
        </el-table-column>
        <el-table-column label="创建时间" width="140">
          <template #default="{ row }"><span class="text-xs">{{ fmtTime(row.createTime) }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="toggleTemplatePreview(row)">
              {{ previewTemplateId === String(row.id) ? '收起' : '明细' }}
            </el-button>
            <el-button type="primary" link size="small" v-perm="'ipd:order:add'" @click="applyTemplate(String(row.id))">套用</el-button>
            <el-button type="danger" link size="small" v-perm="'ipd:order:delete'" @click="removeTemplate(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-6 text-sm text-slate-400">还没有模板。在「开立医嘱」里填好明细后点「另存为模板」即可。</div>
        </template>
      </el-table>
      <div class="mt-3 flex justify-end">
        <el-pagination
          v-model:current-page="templateQuery.pageNum"
          v-model:page-size="templateQuery.pageSize"
          :page-sizes="PAGE_SIZES"
          :total="templateTotal"
          layout="total, sizes, prev, pager, next"
          @size-change="loadTemplatePage"
          @current-change="loadTemplatePage"
        />
      </div>
      <template #footer>
        <span class="mr-3 text-xs text-slate-400">「套用」会把明细灌进开立弹窗，价格按当前字典价重取</span>
        <el-button type="primary" @click="templateDialog = false">关闭</el-button>
      </template>
    </el-dialog>
    </div>
  </div>
</template>

<style scoped>
/* 工作区撑满视口：高度从卡片一层层传到表格，表格在自己的 body 里滚，分页条钉在卡片底部。
   不这么做的现象是「4 条医嘱 + 一片到屏幕底的空白」，那是网页思维，不是工作站。 */
.ws-fill {
  display: flex;
  flex: 1 1 auto;
  flex-direction: column;
  min-height: 0;
}
.ws-fill :deep(.el-tabs__header) {
  flex-shrink: 0;
  margin-bottom: 12px;
}
.ws-fill :deep(.el-tabs__content) {
  display: flex;
  flex: 1 1 auto;
  min-height: 0;
  overflow: hidden;
}
.ws-fill :deep(.el-tabs__content > .el-tab-pane) {
  display: flex;
  flex: 1 1 auto;
  min-height: 0;
  flex-direction: column;
}
.ws-fill :deep(.el-tabs__content > .el-tab-pane > .el-table) {
  flex: 1 1 auto;
  min-height: 0;
}
/* 工具条、批量操作条、分页条都不许被压扁 */
.ws-fill :deep(.el-tabs__content > .el-tab-pane > *:not(.el-table)) {
  flex-shrink: 0;
}
</style>
