<script setup lang="ts">
import {ref, computed, onMounted, onUnmounted, nextTick, watch} from 'vue'
import {Search, Plus, Edit, Filter, Clock, CircleCheck, CircleClose, Timer, Refresh, ArrowLeft, ArrowRight} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  getRegistrationList,
  getAppointStatusCount,
  getBoardRegistList,
  createRegistration,
  updateRegistration,
  cancelRegistration,
  getAvailableSchedule,
  getScheduleSlots,
  getScheduleSlotsBatch,
  getScheduleList,
  checkInByRegist,
  getRevisitRecordSelectList,
  previewRevisitFee
} from '@/api/appoint'
import {payBill, getBillDetailById} from '@/api/settlementBill'
import {submitRefundApply} from '@/api/refund'
import {createPatient} from '@/api/patient'
import {
  PATIENT_GENDER_OPTIONS,
  patientGenderSymbol,
  patientGenderText,
  patientAgeText,
  isPatientGenderCollected,
  isIdCardFormatLegal,
  isIdCardBirthDateLegal,
  isIdCardChecksumLegal,
  isPhoneLegal,
  birthDateFromIdCard
} from '@/lib/patientGender'
import {PATIENT_TYPE_OPTIONS, patientTypeToSettlementType} from '@/lib/patientType'
import PatientSelect from '@/components/his/PatientSelect.vue'
import {getDepartmentSelectList, getEmployeeList, getDictDataList, getDictDataMapList, getUserInfo} from '@/api/system'
import {DICT_TYPE} from '@/lib/dict-cache'
import {VISIT_TYPE} from '@/lib/dict'
// 号别（1 初诊 / 2 复诊）唯一口径：lib/statusColor.REVISIT_TYPE / revisitTypeOf
// 挂号号别（普通号/专家号/急诊号/免费号）唯一口径：REGIST_TYPE，与字典 his_regist_type 同源
import {revisitTypeOf, REGIST_TYPE, statusOf} from '@/lib/statusColor'
import {SCHEDULE_TYPE_OPTIONS} from '@/lib/scheduleShift'
import {REVISIT_SOURCE, revisitNeedsNoSchedule} from '@/lib/revisitPolicy'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'

const getThisWeekRange = () => {
  const now = new Date()
  const day = now.getDay() || 7
  const monday = new Date(now)
  monday.setDate(now.getDate() - day + 1)
  monday.setHours(0, 0, 0, 0)
  const sunday = new Date(monday)
  sunday.setDate(monday.getDate() + 6)
  sunday.setHours(23, 59, 59, 999)
  const fmt = (d: Date) => {
    const y = d.getFullYear(), m = String(d.getMonth() + 1).padStart(2, '0'), dd = String(d.getDate()).padStart(2, '0')
    return `${y}-${m}-${dd}`
  }
  return [fmt(monday), fmt(sunday)]
}

const fmtDate = (d: Date) => {
  const y = d.getFullYear(), m = String(d.getMonth() + 1).padStart(2, '0'), dd = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${dd}`
}

const dateShortcuts = [
  {
    text: '今天',
    value: () => {
      const today = new Date()
      return [fmtDate(today), fmtDate(today)]
    },
  },
  {
    text: '本周',
    value: () => getThisWeekRange(),
  },
  {
    text: '本月',
    value: () => {
      const now = new Date()
      const first = new Date(now.getFullYear(), now.getMonth(), 1)
      const last = new Date(now.getFullYear(), now.getMonth() + 1, 0)
      return [fmtDate(first), fmtDate(last)]
    },
  },
]

const searchForm = ref({
  patientId: null as number | null,
  patientName: '',
  deptId: null as number | null,
  doctorId: null as number | null,
  registStatus: null as number | null,
  visitType: null as number | null,
  // 挂号员 95% 操作在当日：默认只看今天的就诊
  visitDate: fmtDate(new Date()) as string | null,
  registDateRange: getThisWeekRange() as [string, string] | null,
})
const showAddDialog = ref(false)
const showEditDialog = ref(false)
const showPaymentDialog = ref(false)
const paymentLoading = ref(false)
const paymentInfo = ref({
  registNo: '',
  patientName: '',
  amount: 0,
  registId: null as number | null,
  billId: null as number | null,
  billNo: '',
})
const loading = ref(false)
const submitLoading = ref(false)
const appointments = ref<any[]>([])
const departments = ref<any[]>([])
const employeeList = ref<any[]>([])
const availableSchedules = ref<any[]>([])

// 新增挂号弹窗：患者来源切换（与急诊登记同一交互）
// 两个 tab 只切换**患者输入区**，科室 / 日期 / 号源 / 时段 / 就诊类型 / 结算方式 是共享字段 ——
// 所以「录入新患者」不用先建档再挂号两步走，一次提交即可（见 handleSubmit）。
const addMode = ref<'existing' | 'new'>('existing')
const addSubmitLoading = ref(false)

// 录入新患者表单（tab2 内联，不再是独立弹窗）
const newPatientForm = ref({
  patientName: '',
  // 不预设性别：默认成"男"等于静默编造性别
  gender: null as number | null,
  birthDate: '',
  phone: '',
  idCard: '',
  address: '',
  patientType: 1, // 1-自费 2-城镇职工医保 3-城乡居民医保 4-公费 5-其他（口径见 @/lib/patientType）
})

const editForm = ref({
  id: null as number | null,
  deptId: null as number | null,
  doctorId: null as number | null,
  scheduleId: null as number | null,
  visitDate: '',
  visitType: '',
})
const editSchedules = ref<any[]>([])

const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
})

const statusConfig: Record<string, { color: string; icon: any }> = {
  '已挂号': {
    color: 'bg-emerald-100 text-emerald-700 border-emerald-200',
    icon: CircleCheck
  },
  '已签到': {
    color: 'bg-blue-100 text-blue-700 border-blue-200',
    icon: CircleCheck // 签到使用专门的打卡/用户图标更合适
  },
  '已接诊': {
    color: 'bg-purple-100 text-purple-700 border-purple-200',
    icon: Timer
  },
  '已就诊': {
    color: 'bg-slate-100 text-slate-600 border-slate-200',
    icon: CircleCheck
  },
  '已退号': {
    color: 'bg-red-100 text-red-600 border-red-200',
    icon: CircleClose
  },
  '已过号': {
    color: 'bg-orange-100 text-orange-700 border-orange-200',
    icon: Clock
  },
  '爽约': {
    color: 'bg-rose-100 text-rose-700 border-rose-200',
    icon: Clock
  },
  '未就诊': {
    color: 'bg-slate-100 text-slate-500 border-slate-200',
    icon: Clock
  },
}

const statusMap: Record<string, string> = {
  '1': '已挂号',
  '2': '已签到',
  '3': '已接诊',
  '4': '已就诊',
  '5': '已退号',
  '6': '已过号',
  '7': '爽约',
  '8': '未就诊',
}

// 号别配色已收口到 lib/statusColor.REVISIT_TYPE（原来这里还有一份 typeColors，已删）

const newAppointment = ref({
  patientId: null as number | null,
  patientName: '',
  patientNo: '',
  // 仅用于「已选患者」回显头像，不参与提交
  patientGender: null as number | null,
  deptId: null as number | null,
  visitDate: '',
  scheduleId: null as number | null,
  // 仅用于「号源下拉自动带出」：预填医生后不必先猜对是哪个号源，见 openAddFromPanel
  doctorId: null as number | null,
  // 时间片段（biz_schedule_slot.id）：号源事实在段上，挂号必须选段扣段
  slotId: null as number | null,
  visitType: 1,
  // 复诊上下文（仅 visitType=2 有值）：来源决定占不占号源、按哪条策略收钱；
  // 原病历是策略判定（间隔天数、同医生/同科室）的基准，缺它判不出价。
  revisitSource: null as number | null,
  revisitRecordId: null as string | number | null,
  settlementType: 1,
  medicalInsuranceType: '',
  medicalInsuranceNo: '',
})

/**
 * 号源下拉的 placeholder —— 跟着真实状态走，不是一句写死的话。
 *
 * 三种态必须是三句话：还没填科室/日期 → 告诉人先填什么；查过了但没号 → 说清是"没号"不是"没填"；
 * 有号 → 提示直接选，不再提科室日期（否则用户会回去反复点科室找"是不是漏填了"）。
 */
const schedulePlaceholder = computed(() => {
  if (!newAppointment.value.deptId || !newAppointment.value.visitDate) return '请先选择科室和日期'
  if (availableSchedules.value.length) return '请选择号源'
  return '该日期无可挂号源'
})

/**
 * 时间片段选项：所选号源下的半小时段（biz_schedule_slot），号源与占用的事实都在段上。
 * 选号源后加载一次；段不存在（历史排班数据）时列表为空 → 段下拉禁用，走无段旧路径。
 */
const slotRows = ref<any[]>([])
const slotLoading = ref(false)

const slotLabel = (s: any) => `${s.startTime} ~ ${s.endTime}（余 ${s.availableSource}/${s.totalSource}）`

const loadSlotRows = async (scheduleId: any) => {
  slotRows.value = []
  slotLoading.value = false
  const id = availableSchedules.value.find((x: any) => (x.id ?? x.doctorName) === scheduleId)?.id
  if (!id) return
  slotLoading.value = true
  try {
    const res = await getScheduleSlots(id)
    slotRows.value = res.data || []
  } catch (error) {
    console.error('加载时间段失败:', error)
  } finally {
    slotLoading.value = false
  }
}

/** 选号源 → 换段列表并清掉已选段（旧段属于上一个号源） */
const handleScheduleChange = (scheduleId: any) => {
  newAppointment.value.slotId = null
  loadSlotRows(scheduleId)
}

// 结算方式选项（口径 = 字典 his_settlement_type_regist，与 biz_appoint_info.settlement_type 列注释一致）
const settlementTypes = [
  {value: 1, label: '自费', color: 'text-slate-600'},
  {value: 2, label: '城镇职工医保', color: 'text-blue-600'},
  {value: 3, label: '城乡居民医保', color: 'text-emerald-600'},
  {value: 4, label: '公费', color: 'text-purple-600'},
  {value: 5, label: '商业保险', color: 'text-amber-600'},
]

/**
 * 结算方式文案。命中不到就出「未知(n)」——**不要回落成「自费」**：
 * 结算方式决定这单走不走医保结算，把它显示成自费会让人以为患者没医保。
 */
const settlementLabel = (code: any) => {
  if (code === null || code === undefined || code === '') return '未知'
  return settlementTypes.find(t => t.value === Number(code))?.label || `未知(${code})`
}

const statusCounts = ref({
  total: 0,
  waiting: 0,   // 已挂号(1)，未签到
  checkedIn: 0, // 已签到(2)
  completed: 0, // 已就诊(4)
  cancelled: 0, // 已退号(5)
  overdue: 0,   // 已过号(6)
})

// 患者搜索已抽成 PatientSelect 组件

const handlePatientSelect = (patient: any) => {
  if (!patient) return
  newAppointment.value.patientId = patient.id
  newAppointment.value.patientName = patient.patientName
  newAppointment.value.patientNo = patient.patientNo
  newAppointment.value.patientGender = patient.gender ?? null
  // 自动带出结算方式：患者类型(patient_type) 与结算方式(settlement_type) 同构，
  // 「其他」在结算侧没有对应档位 → 回落自费（口径见 @/lib/patientType）
  if (patient.patientType) {
    newAppointment.value.settlementType = patientTypeToSettlementType(patient.patientType)
  }
  // 带出医保卡号和医保类型
  newAppointment.value.medicalInsuranceType = patient.medicalInsuranceType || ''
  newAppointment.value.medicalInsuranceNo = patient.medicalInsuranceNo || ''
  // 换人 = 换病历：原来选中的原病历属于上一个人，留着就会拿别人的就诊日判策略
  newAppointment.value.revisitRecordId = null
  if (isRevisit.value) {
    revisitRecords.value = []
    loadRevisitRecords()
  }
}

// ========== 复诊（来源 + 原病历 + 费用预估，sql/121） ==========

/**
 * 窗口放开的复诊来源：只有「当日回诊」和「医嘱复诊预约」。
 *
 * 3-患者自助复诊 的语义就是患者在小程序上自己发起，窗口代点会让「谁发起的」这条事实失真；
 * 4-随访计划复诊 必须由随访任务生成（那一边要把复诊号回写到任务上，两边才对得上）。
 * 字典 his_revisit_source 仍是四个值，这里只是本入口的口径收窄。
 */
const WINDOW_REVISIT_SOURCES = [REVISIT_SOURCE.SAME_DAY_RETURN, REVISIT_SOURCE.DOCTOR_ORDERED]
const revisitSourceDict = ref<any[]>([])
const windowRevisitSourceOptions = computed(() =>
    revisitSourceDict.value.filter((o: any) => WINDOW_REVISIT_SOURCES.includes(Number(o.dictValue)))
)

const revisitRecords = ref<any[]>([])
const revisitRecordLoading = ref(false)
const revisitPreview = ref<any | null>(null)
const revisitPreviewLoading = ref(false)

const isRevisit = computed(() => Number(newAppointment.value.visitType) === 2)
/**
 * 当日回诊 = 同一次挂号的延续（拿检查结果回来复看）：不占号源，
 * 所以科室 / 日期 / 号源 / 时段整块都不填 —— 强行让人选号源，等于把一次就诊拆成两个号重复收费。
 */
const isSameDayReturn = computed(() => isRevisit.value && revisitNeedsNoSchedule(newAppointment.value.revisitSource))

const loadRevisitSourceDict = async () => {
  try {
    const res = await getDictDataList(DICT_TYPE.REVISIT_SOURCE)
    revisitSourceDict.value = res.data || []
  } catch (error) {
    console.error('加载复诊来源字典失败:', error)
  }
}

/**
 * 复诊来源文案（列表里那一小截灰字）。
 * 命中不到不回落成「复诊」——号别那里已经写了复诊，重复一遍没有信息，
 * 而「字典里查不到的码」恰恰是需要一眼看见的异常（说明有人直接写了库）。
 */
const revisitSourceText = (code: any) => {
  const hit = revisitSourceDict.value.find((o: any) => Number(o.dictValue) === Number(code))
  return hit ? hit.dictLabel : `来源${code}`
}

const revisitRecordLabel = (r: any) =>
    `${r.visitDate || '日期未知'} ${r.deptName || '-'} / ${r.doctorName || '-'}${r.diagnosisName ? ` | ${r.diagnosisName}` : ''}`

const loadRevisitRecords = async () => {
  const patientId = newAppointment.value.patientId
  if (!patientId) return
  revisitRecordLoading.value = true
  try {
    const res = await getRevisitRecordSelectList(patientId)
    revisitRecords.value = res.data || []
  } catch (error) {
    console.error('加载复诊原病历失败:', error)
  } finally {
    revisitRecordLoading.value = false
  }
}

/**
 * 费用预估：来源、原病历、（要占号源时）号源三者齐了才问。
 *
 * 齐了才问是因为金额由号源上的挂号费/诊查费再乘策略决定 —— 没选号源时预估出来的数
 * 与实收必然对不上，宁可先不显示，也不给一个会变来变去的数。
 * previewToken 掐掉过期响应：连点两次号源会并发两个请求，后到的旧响应不能覆盖新结果。
 */
let previewToken = 0
watch(
    () => [newAppointment.value.visitType, newAppointment.value.revisitSource,
      newAppointment.value.revisitRecordId, newAppointment.value.scheduleId, newAppointment.value.patientId],
    async () => {
      const a = newAppointment.value
      const token = ++previewToken
      const ready = isRevisit.value && a.patientId && a.revisitSource && a.revisitRecordId
          && (isSameDayReturn.value || a.scheduleId)
      if (!ready) {
        revisitPreview.value = null
        revisitPreviewLoading.value = false
        return
      }
      revisitPreviewLoading.value = true
      try {
        const res = await previewRevisitFee({
          patientId: a.patientId,
          revisitSource: a.revisitSource,
          revisitRecordId: a.revisitRecordId,
          scheduleId: isSameDayReturn.value ? undefined : a.scheduleId,
        })
        if (token !== previewToken) return
        revisitPreview.value = res.data || null
      } catch (error: any) {
        if (token !== previewToken) return
        revisitPreview.value = null
        ElMessage.error(error?.message || '复诊费用预估失败')
      } finally {
        if (token === previewToken) revisitPreviewLoading.value = false
      }
    }
)

/** 切就诊类型：离开复诊就把复诊上下文清空，否则上一单选过的原病历会带着判定进下一张单 */
const handleVisitTypeChange = () => {
  if (!isRevisit.value) {
    newAppointment.value.revisitSource = null
    newAppointment.value.revisitRecordId = null
    return
  }
  if (!newAppointment.value.revisitSource) newAppointment.value.revisitSource = REVISIT_SOURCE.SAME_DAY_RETURN
  loadRevisitRecords()
}

// ========== 新建患者 ==========
/** 切换患者来源 tab；切到「录入新患者」时清空表单（避免上一单填的残留带过来） */
const switchPatientMode = (mode: 'existing' | 'new') => {
  addMode.value = mode
  if (mode === 'new') {
    resetNewPatientForm()
    // 刚建档的人没有任何就诊记录，复诊无从关联（后端也会以「原病历不属于该患者」拒掉）
    if (isRevisit.value) {
      newAppointment.value.visitType = 1
      newAppointment.value.revisitSource = null
      newAppointment.value.revisitRecordId = null
    }
  }
}

const resetNewPatientForm = () => {
  newPatientForm.value = {
    patientName: '',
    gender: null,
    birthDate: '',
    phone: '',
    idCard: '',
    address: '',
    patientType: 1,
  }
}

/** 身份证填完 → 把出生日期带出来（已手填过就不覆盖），与患者管理页同一口径 */
const syncPatientBirthDate = () => {
  if (newPatientForm.value.birthDate) return
  const d = birthDateFromIdCard(newPatientForm.value.idCard)
  if (d) newPatientForm.value.birthDate = d
}

/**
 * 校验「录入新患者」表单，不通过则提示并返回 false。
 *
 * 建档口径与患者管理页一致：姓名 / 性别 / 身份证必填，手机号"填了必须合法、空着放行"。
 * 这里比急诊登记严 —— 急诊面向三无患者（身份证可能根本要不到），门诊挂号窗口患者清醒、
 * 能出示证件，是补全主档成本最低的时机（身份证也是 EMPI 去重的匹配键）。
 */
const validateNewPatientForm = () => {
  if (!newPatientForm.value.patientName) {
    ElMessage.warning('请输入患者姓名')
    return false
  }
  if (!isPatientGenderCollected(newPatientForm.value.gender)) {
    ElMessage.warning('请选择性别（确实没问到请选「未知」）')
    return false
  }
  if (!newPatientForm.value.idCard) {
    ElMessage.warning('请输入身份证号')
    return false
  }
  if (!isIdCardFormatLegal(newPatientForm.value.idCard)) {
    ElMessage.warning('身份证号格式不正确：应为 18 位（末位可为 X）')
    return false
  }
  // 出生日期单独判：校验位对了不代表日期存在（19990230 这种），提示要指到点子上
  if (!isIdCardBirthDateLegal(newPatientForm.value.idCard)) {
    ElMessage.warning('身份证号中的出生日期不存在，请核对')
    return false
  }
  if (!isIdCardChecksumLegal(newPatientForm.value.idCard)) {
    ElMessage.warning('身份证号校验位不正确，请核对')
    return false
  }
  if (newPatientForm.value.phone && !isPhoneLegal(newPatientForm.value.phone)) {
    ElMessage.warning('手机号格式不正确：应为 11 位手机号')
    return false
  }
  return true
}

const handleDeptChange = () => {
  newAppointment.value.scheduleId = null
  newAppointment.value.slotId = null
  slotRows.value = []
  availableSchedules.value = []
  loadAvailableSchedules()
}

const handleDateChange = () => {
  newAppointment.value.scheduleId = null
  newAppointment.value.slotId = null
  slotRows.value = []
  availableSchedules.value = []
  loadAvailableSchedules()
}

const loadAvailableSchedules = async () => {
  if (!newAppointment.value.deptId || !newAppointment.value.visitDate) return
  try {
    const res = await getAvailableSchedule(newAppointment.value.deptId, newAppointment.value.visitDate)
    availableSchedules.value = res.data || []
  } catch (error) {
    console.error('加载号源失败:', error)
  }
}

const handleSubmit = async () => {
  // ① 患者：按 tab 分支校验
  if (addMode.value === 'existing') {
    if (!newAppointment.value.patientId) {
      ElMessage.warning('请选择患者')
      return
    }
  } else if (!validateNewPatientForm()) {
    return
  }
  // ② 复诊：来源与原病历必填（当日回诊免掉科室/日期/号源，但这两项是策略判定的基准，不能省）
  if (isRevisit.value) {
    if (!newAppointment.value.revisitSource) {
      ElMessage.warning('请选择复诊来源')
      return
    }
    if (!newAppointment.value.revisitRecordId) {
      ElMessage.warning('请选择原病历（复诊要关联的那一次就诊）')
      return
    }
  }
  // ③ 共享字段（两个 tab 共用同一张表单，都要校验）；当日回诊不占号源，整块跳过
  if (!isSameDayReturn.value) {
    if (!newAppointment.value.deptId) {
      ElMessage.warning('请选择科室')
      return
    }
    if (!newAppointment.value.visitDate) {
      ElMessage.warning('请选择预约日期')
      return
    }
    if (!newAppointment.value.scheduleId) {
      ElMessage.warning('请选择号源')
      return
    }
    // 号源事实在段上：有段可选时必须选段（后端按段扣号源），不选段会退化为只扣主表的旧路径
    if (slotRows.value.length && !newAppointment.value.slotId) {
      ElMessage.warning('请选择时间段')
      return
    }
  }

  addSubmitLoading.value = true
  try {
    let patientId = newAppointment.value.patientId
    let patientName = newAppointment.value.patientName

    // ④ 录入新患者：先建档拿 ID，再挂号 —— 一次点击完成两步。
    // 顺序不能反：挂号接口的 patientId 是 @NotNull，必须先有患者主键。
    // 这不等于数据库事务级原子（页面层两次调用）：若建档成功而挂号失败（如号源被抢空），
    // 患者档案会留下 —— 这里刻意**不**回滚，改个号源再点一次即可，不用重填身份证；
    // 患者主档本身也是独立有效的资产（患者管理页单独建档同样是常态）。
    if (addMode.value === 'new') {
      const created = await createPatient(newPatientForm.value)
      const np = created.data
      if (!np?.id) {
        throw new Error('患者已创建但未返回患者ID，请到「患者管理」确认后重新挂号')
      }
      patientId = np.id
      patientName = np.patientName || newPatientForm.value.patientName
    }

    const res = await createRegistration({
      patientId: patientId,
      patientName: patientName,
      // 当日回诊即使是从看板某一格开单（那里会预填号源），也必须清空：
      // 它不占号源，后端拿到 scheduleId 会以「请勿为当日回诊选择号源」拒掉
      scheduleId: isSameDayReturn.value ? null : newAppointment.value.scheduleId,
      slotId: isSameDayReturn.value ? null : (newAppointment.value.slotId || undefined),
      visitType: newAppointment.value.visitType,
      revisitSource: isRevisit.value ? newAppointment.value.revisitSource : undefined,
      revisitRecordId: isRevisit.value ? newAppointment.value.revisitRecordId : undefined,
      settlementType: newAppointment.value.settlementType,
      medicalInsuranceType: newAppointment.value.medicalInsuranceType,
      medicalInsuranceNo: newAppointment.value.medicalInsuranceNo,
    })
    ElMessage.success(addMode.value === 'new' ? '患者建档并挂号成功' : '挂号成功')
    showAddDialog.value = false
    const data = res.data || {}
    // 查询账单：金额用来弹缴费窗，状态用来决定**要不要**弹
    let amount = 0
    let billStatus = null
    // 免收：后端不记账不出账（billId 为 null），直接提示不弹窗
    if (!data.billId) {
      ElMessage.success(isSameDayReturn.value
          ? '当日回诊免收挂号费，患者可直接签到就诊'
          : '该号按收费策略免收，无需缴费，患者可直接签到')
      reloadListAndStats()
      resetForm()
      return
    }
    try {
      const billRes = await getBillDetailById(data.billId)
      const bill = billRes.data || {}
      amount = bill.payableAmount || 0
      billStatus = bill.billStatus ?? null
    } catch (e) {
      console.error('获取账单详情失败:', e)
    }
    // 账单已付清：再弹一次缴费窗等于让人对一笔已平的账重复支付
    if (Number(billStatus) === 3) {
      ElMessage.success('该号已缴费，无需重复支付')
      reloadListAndStats()
      resetForm()
      return
    }
    paymentInfo.value = {
      registNo: data.registNo || '',
      patientName: patientName,
      amount: amount,
      registId: data.id || null,
      billId: data.billId || null,
      billNo: data.billNo || '',
    }
    showPaymentDialog.value = true
    reloadListAndStats()
    resetForm()
  } catch (error) {
    ElMessage.error(error.message || '挂号失败')
  } finally {
    addSubmitLoading.value = false
  }
}

const resetForm = () => {
  newAppointment.value = {
    patientId: null,
    patientName: '',
    patientNo: '',
    patientGender: null,
    deptId: null,
    visitDate: '',
    scheduleId: null,
    doctorId: null,
    slotId: null,
    visitType: 1,
    revisitSource: null,
    revisitRecordId: null,
    settlementType: 1,
    medicalInsuranceType: '',
    medicalInsuranceNo: '',
  }
  availableSchedules.value = []
  slotRows.value = []
  revisitRecords.value = []
  // 上一单的预估金额留在屏上，会被当成这一单要收的钱
  revisitPreview.value = null
  revisitPreviewLoading.value = false
  // 关掉弹窗后回到「选择已有患者」，并把新患者表单清空（否则下次打开带出上一单的姓名/身份证）
  addMode.value = 'existing'
  resetNewPatientForm()
}

const handleCheckIn = async (row: any) => {
  try {
    await checkInByRegist(row.id)
    ElMessage.success('签到成功，患者已进入候诊队列')
    reloadListAndStats()
  } catch (error: any) {
    ElMessage.error(error.message || '签到失败')
  }
}

const handleCancel = async (row: any) => {
  // 与后端 cancelRegist 同一口径：只有「已挂号(1)」「已签到(2)」能退。
  // 已接诊(3) 之后诊疗已经发生，退号等于篡改诊疗事实（后端也会拦，这里先给一句人话）。
  if (!canCancelRegist(row)) {
    ElMessage.warning(sourceLockReason(row) || '当前状态不允许退号')
    return
  }
  try {
    await ElMessageBox.confirm(
        Number(row.registStatus) === 2
            ? `该患者已签到入队，确定退号？退号后其候诊队列行会一并置为已退号。`
            : '确定要取消该预约吗？',
        '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        })
    await cancelRegistration(row.id, '患者主动取消')
    ElMessage.success('取消成功')
    reloadListAndStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消失败')
    }
  }
}

const handleEdit = async (row: any) => {
  editForm.value = {
    id: row.id,
    deptId: row.deptId,
    doctorId: row.doctorId,
    scheduleId: null,
    visitDate: row.visitDate || '',
    visitType: row.visitType || 1,
  }
  // 加载当前科室的医生列表
  if (row.deptId) {
    try {
      const res = await getEmployeeList({empType: 1, deptId: row.deptId})
      employeeList.value = res.data?.records || res.data || []
    } catch (e) {
      employeeList.value = []
    }
  }
  showEditDialog.value = true
}

const handleEditDeptChange = async (deptId: number) => {
  editForm.value.doctorId = null
  editForm.value.scheduleId = null
  editSchedules.value = []
  try {
    const res = await getEmployeeList({empType: 1, deptId})
    employeeList.value = res.data?.records || res.data || []
  } catch (e) {
    employeeList.value = []
  }
}

const handleEditDateChange = async () => {
  editForm.value.scheduleId = null
  editSchedules.value = []
  if (!editForm.value.deptId || !editForm.value.visitDate) return
  try {
    const res = await getAvailableSchedule(editForm.value.deptId, editForm.value.visitDate)
    editSchedules.value = res.data || []
  } catch (e) {
    editSchedules.value = []
  }
}

const handleEditSubmit = async () => {
  if (!editForm.value.deptId || !editForm.value.visitDate || !editForm.value.scheduleId) {
    ElMessage.warning('请完善修改信息')
    return
  }
  submitLoading.value = true
  try {
    await updateRegistration({
      id: editForm.value.id,
      scheduleId: editForm.value.scheduleId,
      visitType: editForm.value.visitType,
    })
    ElMessage.success('修改成功')
    showEditDialog.value = false
    reloadListAndStats()
  } catch (error) {
    ElMessage.error(error.message || '修改失败')
  } finally {
    submitLoading.value = false
  }
}

// ========== 显示支付弹框 ==========
const handleShowPayment = async (row: any) => {
  if (!row.billId) {
    ElMessage.error('未找到关联的账单')
    return
  }
  // 查询账单详情获取金额
  let amount = 0
  try {
    const billRes = await getBillDetailById(row.billId)
    amount = billRes.data?.payableAmount || 0
  } catch (e) {
    console.error('获取账单详情失败:', e)
  }
  paymentInfo.value = {
    registNo: row.registNo || '',
    patientName: row.patientName,
    amount: amount,
    registId: row.id || null,
    billId: row.billId || null,
    billNo: row.billNo || '',
  }
  showPaymentDialog.value = true
}

// ========== 支付确认 ==========
const handleConfirmPayment = async () => {
  if (!paymentInfo.value.billId) {
    ElMessage.error('未找到账单')
    return
  }
  paymentLoading.value = true
  try {
    await payBill({
      billId: paymentInfo.value.billId,
      items: [{ payMethod: 2, amount: Number(paymentInfo.value.amount) }], // 微信支付
    })
    showPaymentDialog.value = false
    ElMessage.success('支付成功')
    reloadListAndStats()
  } catch (error: any) {
    ElMessage.error(error.message || '支付失败')
  } finally {
    paymentLoading.value = false
  }
}

// ========== 过期号的「申请退费」 ==========
//
// 为什么退号管不到这件事：退号是「把当天这个号作废 + 号源回池 + 顺手退费」，
// 昨天的号源属过去日期，回不了池（见后端 DayEndSettleMapper 类注释），所以日期一过就不允许退号。
// 但患者昨天没来、今天来要钱是真实诉求 —— 走的是另一条链路：
// 挂号行保持「爽约/未就诊」不动（那是就诊事实，改了医保和报表对不上），
// 钱走「退费申请 → 收费处审核 → 执行」，全程留痕、允许跨期。
// 发起入口在挂号记录页（窗口天天在用），审核与执行留在收费处的「退费管理」页。
const showRefundDialog = ref(false)
const refundSubmitting = ref(false)
const refundTypeOptions = ref<any[]>([])
const refundForm = ref({
  patientName: '',
  registNo: '',
  patientId: null as any,
  patientNo: '',
  billId: null as any,
  billNo: '',
  refundableAmount: 0,
  refundType: null as number | null,
  refundAmount: null as number | null,
  refundReason: '',
})

/** 退费类型字典只在第一次打开弹框时拉，之后复用（与「退费管理」页同一份 his_refund_apply_type） */
const loadRefundTypeOptions = async () => {
  if (refundTypeOptions.value.length) return
  try {
    const res = await getDictDataMapList('his_refund_apply_type')
    refundTypeOptions.value = res.data?.['his_refund_apply_type'] || []
  } catch (error) {
    // 字典拿不到不拦提交：类型是必填项，下拉空了用户自然会问，比静默给个假选项好
    console.error('加载退费类型字典失败:', error)
  }
}

const handleApplyRefund = async (row: any) => {
  if (!canApplyRefund(row)) {
    ElMessage.warning('该挂号不满足申请退费条件（需就诊日已过、已收费且人未到诊）')
    return
  }
  let amount = 0
  try {
    const billRes = await getBillDetailById(row.billId)
    const bill = billRes.data || {}
    const paid = Number(bill.paidAmount ?? 0)
    // 上限是「还能退多少」而不是「已收」：退过一部分的账单按净额给上限，提交时会被服务端打回，
    // 窗口只会觉得系统抽风（真正拦它的那句「不能超过可退金额」看着像前后矛盾）。
    const refunded = Number(bill.refundAmount ?? 0)
    amount = paid - refunded
  } catch (error) {
    console.error('获取收费单详情失败:', error)
  }
  // 金额拿不到就不开弹框：一个「实付 ¥0、上限 0」的退费表单只会让人以为系统坏了
  if (!(amount > 0)) {
    ElMessage.error('未能取到该挂号的可退金额（可能已经退过），请稍后重试或直接到医院收费处办理')
    return
  }
  refundForm.value = {
    patientName: row.patientName || '',
    registNo: row.registNo || '',
    patientId: row.patientId,
    patientNo: row.patientNo || '',
    billId: row.billId,
    billNo: row.billNo || '',
    refundableAmount: amount,
    // 挂号单是整单退费，默认带出「全部退费」(5)，允许改
    refundType: 5,
    refundAmount: amount,
    refundReason: '',
  }
  showRefundDialog.value = true
  loadRefundTypeOptions()
}

const handleSubmitRefund = async () => {
  if (refundForm.value.refundType === null) {
    ElMessage.warning('请选择退费类型')
    return
  }
  const amount = Number(refundForm.value.refundAmount)
  if (refundForm.value.refundAmount === null || Number.isNaN(amount) || amount <= 0) {
    ElMessage.warning('请填写正确的退费金额')
    return
  }
  if (amount > refundForm.value.refundableAmount) {
    ElMessage.warning(`退费金额不能超过本单可退金额 ¥${refundForm.value.refundableAmount}`)
    return
  }
  if (!refundForm.value.refundReason.trim()) {
    ElMessage.warning('请填写退费原因')
    return
  }
  refundSubmitting.value = true
  try {
    await submitRefundApply({
      billId: refundForm.value.billId,
      billNo: refundForm.value.billNo,
      patientId: refundForm.value.patientId,
      patientNo: refundForm.value.patientNo,
      patientName: refundForm.value.patientName,
      refundType: refundForm.value.refundType,
      refundReason: refundForm.value.refundReason.trim(),
      refundAmount: amount,
      // applyBy 不从前端编造，后端按当前登录人回填
    })
    ElMessage.success('退费申请已提交，请到收费处「退费管理」审核并执行')
    showRefundDialog.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '提交退费申请失败')
  } finally {
    refundSubmitting.value = false
  }
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  // 统计范围跟着筛选条件走 → 必须一起刷，否则「筛选后列表 3 条、卡片还是全院总数」
  reloadListAndStats()
}

const handleReset = () => {
  searchForm.value = {
    patientId: null,
    patientName: '',
    deptId: null,
    doctorId: null,
    registStatus: null,
    visitType: null,
    visitDate: fmtDate(new Date()),
    registDateRange: getThisWeekRange(),
  }
  handleSearch()
}

// 翻页/改每页大小只影响列表本身，统计范围（筛选条件）没变 → 不必重算统计
const handleSizeChange = (val: number) => {
  pagination.value.pageSize = val
  pagination.value.pageNum = 1
  loadData()
}

const handleCurrentChange = (val: number) => {
  pagination.value.pageNum = val
  loadData()
}

/**
 * 列表与状态卡**共用**的筛选条件。
 *
 * 两处各拼一遍是这次的真凶之一：原来状态卡只发 `{pageNum:1,pageSize:1}`，
 * 把「当前筛选条件的统计」做成了「全院总数」——筛选到某科室后列表 3 条、卡片还是 4000。
 * 抽成一处后，卡片与列表的口径在结构上不可能分叉。
 */
const buildQueryParams = (withPaging = true) => {
  const params: any = {}
  if (withPaging) {
    params.pageNum = pagination.value.pageNum
    params.pageSize = pagination.value.pageSize
  }
  if (searchForm.value.patientId) params.patientId = searchForm.value.patientId
  if (searchForm.value.deptId) params.deptId = searchForm.value.deptId
  // 这里刻意**不**下发 doctorId：`searchForm.doctorId` 全页没有任何输入控件，
  // 恒为 null，下发它只是给「查不到数据」留一个没人会想到的开关。
  // 挂号记录页的医生筛选要不要做是另一件事（要做就得给控件 + 后端真的支持这个条件）。
  if (searchForm.value.registStatus != null) params.registStatus = searchForm.value.registStatus
  if (searchForm.value.visitDate) params.visitDate = searchForm.value.visitDate
  if (searchForm.value.registDateRange?.[0]) params.beginTime = searchForm.value.registDateRange[0] + ' 00:00:00'
  if (searchForm.value.registDateRange?.[1]) params.endTime = searchForm.value.registDateRange[1] + ' 23:59:59'
  return params
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRegistrationList(buildQueryParams())
    let records = res.data?.records || []
    // visitType 不在数据库字段中，前端过滤
    if (searchForm.value.visitType) {
      records = records.filter((r: any) => r.visitType === searchForm.value.visitType)
    }
    appointments.value = records
    pagination.value.total = res.data?.total || 0
  } catch (error) {
    console.error('加载预约列表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadStatusCounts = async () => {
  // 一次请求拿六个数（原来发 6 次 listPage、每次 pageSize=1 只为读 total）。
  // 口径（1已挂号 2已签到 4已就诊 5已退号 6已过号）收口在后端 AppointStatusEnum，
  // 前端不再自己映射码值 —— 之前把 cancelled 数成 4（已就诊）就是这么来的。
  //
  // 卡片口径 = 「今天的数据」：统计查询**不带挂号时间区间**（searchForm.registDateRange
  // 默认是本周，是给下方挂号记录列表用的）。带着它会把「提前挂号、今天就诊」之外的人
  // 也算进来/漏出去，六个数就不再是就诊日当天的口径。其余筛选（科室等）仍然跟随。
  try {
    const statParams = buildQueryParams(false)
    delete statParams.beginTime
    delete statParams.endTime
    const res = await getAppointStatusCount(statParams)
    const d = res.data || {}
    statusCounts.value = {
      total: d.total || 0,
      waiting: d.waiting || 0,
      checkedIn: d.checkedIn || 0,
      completed: d.completed || 0,
      cancelled: d.refunded || 0,
      overdue: d.overdue || 0,
    }
    await nextTick()
    calcDeskHeight()
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

/** 列表与统计一起刷新 —— 凡是会改变统计范围的操作都走这里，避免只刷一半 */
const reloadListAndStats = () => {
  loadData()
  loadStatusCounts()
}

const loadDepartments = async () => {
  try {
    // 不传 scope → 默认按当前人过滤：挂号员只看到被授权的挂号科室。
    // 门诊部 / 管理员这类不限权用户天然拿到全部科室，不需要在这里做角色判断。
    const res = await getDepartmentSelectList({deptType: 1})
    departments.value = res.data || []
  } catch (error) {
    console.error('加载科室列表失败:', error)
  }
}

/**
 * 看板/号源面板的默认科室 = 「我的科室」。
 *
 * 真实 HIS 的挂号工作台是**按科室数据权限**给范围的：挂号员被授权若干科室（只能挂这些科的号），
 * 默认选中主科室；门诊部 / 门诊办看全院，用来监控排班容量与号源使用。
 * 也就是说这本来是个"数据权限"问题，不是一个看板菜单的开关。
 *
 * 本项目现状：科室数据权限已收口在后端（`DeptScopeGuard`：号源/挂号/排班查询按
 * `sys_employee_post` 授权 + 主科室兜底过滤，越权传 deptId 直接拒）。所以前端这一层
 * 只负责"默认落在哪个科室"，**不负责隐藏科室** —— 下拉列出的是当前人可授权范围内的科室
 * （`/system/department/selectList` 默认按当前人过滤），能不能查出数据由服务端说了算。
 *   默认落在 `/auth/info` 的 deptId（取不到就退回「全部科室」= 不限科室），
 *   下拉里选「全部科室 / 全部医生」即在授权范围内看全部（门诊部/管理员就是这么用的）。
 */
// 当前登录人的主科室（`/auth/info.deptId`）：只用于「看板默认落本科室」这一件事。
// （原左栏「我的科室」快捷按钮已按需求移除，切科室统一走上方「科室」下拉。）
const myDeptId = ref<any>(null)

const applyDefaultDept = (deptId: any) => {
  // 必须回到下拉里真实存在的那个 option 值,否则 el-select 会显示成一个匹配不上的裸数字
  const hit = departments.value.find(d => String(d.id) === String(deptId))
  if (!hit) return
  myDeptId.value = hit.id
  // 默认落「我的科室」：一个科室的号源一屏看不完，全院号源铺出来没有操作性。
  // 「全部科室」仍然可选（清不掉，是筛选项不是可清空按钮）—— 门诊部监控容量时用。
  // （原号源面板 tab 已并入周视图，不再有第二份 deskDeptId 的镜像）
  if (deskDeptId.value == null) deskDeptId.value = hit.id
}

const loadDefaultDept = async () => {
  try {
    const res = await getUserInfo()
    applyDefaultDept(res.data?.deptId)
  } catch (error) {
    // 取不到就保持「全部科室」，不阻断看板（默认值不该成为故障点）
    console.error('加载当前用户科室失败，看板默认「全部科室」:', error)
  }
}

// 医保类型字典
const medicalInsuranceTypes = ref<any[]>([])
const loadMedicalInsuranceTypes = async () => {
  try {
    const res = await getDictDataList(DICT_TYPE.MEDICAL_INSURANCE_TYPE)
    medicalInsuranceTypes.value = res.data || []
  } catch (error) {
    console.error('加载医保类型字典失败:', error)
  }
}

// ========== 挂号工作台（日 / 周视图） ==========
// tab 顺序：挂号工作台 → 挂号记录（挂号窗口先看「今天/本周有哪些约」，再查历史单）
// 原「号源面板」tab 已并入周视图 —— 它是看板的严格子集（同一张表、同 24 字段、同维度，
// 只差 available_source > 0），现降级为周视图工具栏上一个「隐藏已满/停诊」开关。

/**
 * 「全部科室」在下拉里的显式取值。
 *
 * 为什么不用 null 表达「全部」：null 在 el-select 里等于「没选」，placeholder 会盖上来，
 * 用户没法区分「我选了全部」和「我还没选」；而清空 ✕ 又会被误当成"取消筛选"。
 * 用一个不可能与真实科室 ID 冲突的字符串当哨兵，语义与显示都唯一。
 * 外部零影响：只在视图层存在，取数时才翻译成"不下发 deptId"。
 */
const ALL_DEPT = '__ALL__'
const isAllDept = (v: any) => v === ALL_DEPT || v == null || v === ''
const activeTab = ref('desk')
/** 弹框里的「格子」：周视图下 = 某医生 × 某天（2026-09-22 起周视图的行就是医生，不再是班次） */
const selectedCell = ref<{date: string; doctorId: any; doctorName: string} | null>(null)
/** 周视图格子的「医生号源明细」弹框（原来是一张内联表，压在表格下面把看板挤出屏幕） */
const showCellDialog = ref(false)
// 弹框跟着「选中的格子」活：切视图 / 翻周 / 下钻都会清掉 selectedCell，
// 那时弹框必须一起关 —— 否则屏上留着一个指向已不存在的格子的窗（切去日视图还在）。
watch(selectedCell, v => {
  if (!v) showCellDialog.value = false
})

/**
 * 周视图「隐藏已满/停诊」开关。
 *
 * 原「号源面板」tab 的全部内容 = 打开这个开关的周视图：两者是同一张表
 * （`biz_schedule`）、同 24 个字段、同「班次 × 日期」维度，只差 `available_source > 0`
 * 这一个过滤条件（实测本周 66 个班次里只有 3 个已满，差异率 4.5%）。
 * 4.5% 的差异不配拥有一个 tab —— 它是个 filter。
 *
 * 默认**关**：挂号员更关心「这天整体什么样」，已满也是信息（满了才知道要改推荐别的天）。
 * 打开后才是「只看能挂的号」——这就是原来切到号源面板的人才需要的东西。
 */
const deskHideFull = ref(false)

// ========== 左侧 rail：迷你月历（纯日期导航） ==========

/**
 * 月历当前显示的月份（`YYYY-MM-01`）。与 `deskDay` **解耦** ——
 * 用户可以在月历上翻到 10 月看有没有排班，而右侧仍停在 9-21；点某天才会把两侧对齐。
 * 若把两者绑成同一个值，"翻月看未来"这个动作就会把右侧也翻走，等于不能纯浏览。
 */
const calMonth = ref(fmtDate(new Date()).slice(0, 8) + '01')

/**
 * 月历格子上的「有排班」圆点数据源。
 *
 * ⚠️ 为什么用**整月一次区间查询**，而不是按天查 31 次：
 * 排班表 `biz_schedule` 全库仅 74 条 / 覆盖 14 天（实测），整月一次拿回来比周视图（66 条）还小；
 * 而按天 31 次明细查询正是本页刚修掉的「首屏 7 请求」的 4.4 倍放大版。
 * 一次 `startDate/endDate` 区间查询就能覆盖圆点所需的全部信息，没有第二个选择。
 *
 * ⚠️ 只存「哪天有排班」这个事实，**不存每天几条、更不显示「N 个号」**：
 * 显示数量会诱导后续实现去算"某天还剩多少号"，那就必须逐日聚合（见上一条）。
 * 圆点回答的是"这天开不开诊"，不是"这天好不好挂"——后者请点进去看日视图。
 */
const calServiceDays = ref<Set<string>>(new Set())
const calLoading = ref(false)

const calMonthLabel = computed(() => {
  const [y, m] = calMonth.value.split('-')
  return `${y} 年 ${Number(m)} 月`
})

/** 月历 42 格（6 周 × 7 天，周一起）—— 固定 6 行，翻月时高度不跳 */
const calCells = computed(() => {
  const first = new Date(calMonth.value + 'T00:00:00')
  const firstDow = first.getDay() || 7           // 周一=1 … 周日=7
  const start = new Date(first)
  start.setDate(first.getDate() - (firstDow - 1))
  const mon = calMonth.value.slice(0, 7)
  const cells: {date: string; day: number; inMonth: boolean; hasService: boolean; isToday: boolean}[] = []
  for (let i = 0; i < 42; i++) {
    const d = new Date(start)
    d.setDate(start.getDate() + i)
    const ds = fmtDate(d)
    cells.push({
      date: ds,
      day: d.getDate(),
      inMonth: ds.slice(0, 7) === mon,
      hasService: calServiceDays.value.has(ds),
      isToday: ds === fmtDate(new Date()),
    })
  }
  return cells
})

/**
 * 月历取数 = 当前月份的全部排班（一次区间查询）。
 *
 * 科室筛选**跟随主区**：主区看呼吸内科时，月历圆点也只标呼吸内科开诊的日子，
 * 否则圆点点出来一堆、点进去却是空的（"有排班"与"我能看的排班"必须同一个口径）。
 */
const loadCalServiceDays = async () => {
  calLoading.value = true
  try {
    const start = calMonth.value
    const end = fmtDate(new Date(new Date(calMonth.value + 'T00:00:00').getFullYear(),
                               new Date(calMonth.value + 'T00:00:00').getMonth() + 1, 0))
    const params: any = {startDate: start, endDate: end}
    if (!isAllDept(deskDeptId.value)) params.deptId = deskDeptId.value
    if (deskDoctorId.value) params.doctorId = deskDoctorId.value
    const res = await getScheduleList(params)
    const scheds = (res.data?.records || res.data || []) as any[]
    calServiceDays.value = new Set(scheds.map((s: any) => String(s.scheduleDate || '').slice(0, 10)))
  } catch (error) {
    // 圆点取不到就退化成"没有圆点"，不阻断月历导航这个主功能
    console.error('加载月历排班标记失败:', error)
  } finally {
    calLoading.value = false
  }
}

/**
 * 月历翻月。
 *
 * ⚠️ 只改月历自己的月份，**不动右侧 deskDay** —— 见 `calMonth` 的注释。
 * 展开月历浏览未来排班时不该把工作区带着一起跳。
 */
const handleCalShift = (delta: number) => {
  const d = new Date(calMonth.value + 'T00:00:00')
  d.setMonth(d.getMonth() + delta)
  calMonth.value = fmtDate(new Date(d.getFullYear(), d.getMonth(), 1))
  loadCalServiceDays()
}

/** 月历「今天」：把月历翻回本月（右侧是否跟过去由调用处决定） */
const handleCalToday = () => {
  calMonth.value = fmtDate(new Date()).slice(0, 8) + '01'
  loadCalServiceDays()
}

/**
 * 点月历某天 → 右侧切到该天（日视图）。
 *
 * 若该天不在当前月历显示的月份，顺手把月历也翻过去（否则跨月的日期没有对应的月历格子高亮）。
 */
const handleCalPick = (date: string) => {
  if (date.slice(0, 7) !== calMonth.value.slice(0, 7)) {
    calMonth.value = date.slice(0, 8) + '01'
    loadCalServiceDays()
  }
  deskDay.value = date
  deskViewMode.value = 'day'
  deskWeekOffset.value = 0
  selectedCell.value = null
  loadDesk()
}

// 2026-09-22：左栏「我的科室」快捷块按需求移除，切科室统一走上方「科室」下拉。

const weekdayMap: Record<number, string> = {0: '周日', 1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六'}

const boardShifts = SCHEDULE_TYPE_OPTIONS.map(o => ({type: o.value, label: o.label}))

const mondayOf = (offset: number) => {
  const now = new Date()
  const day = now.getDay() || 7
  const monday = new Date(now)
  monday.setDate(now.getDate() - day + 1 + offset * 7)
  return monday
}

// ========== 预约看板（日 / 周 两种视图，卡片可改约/退号） ==========
//
// 视图口径照真实 HIS 的挂号工作台来：日/周是**同一张工作台的视图切换**，不是三个菜单
// （行心、东华等挂号工作台都是日/周/月模式切换；排班大屏同理）。
//   周视图 = 排班容量视角：这周哪天满、哪天没排班、余号还剩多少（门诊部 / 挂号组长看）；
//   日视图 = 窗口操作视角：今天每位医生还剩几个号、挂了谁、谁要改约退号（挂号窗口看）。
// 同一份数据的两档放大倍数，所以做成视图切换 + 周视图点日期下钻，而不是新增菜单。
const deskViewMode = ref<'day' | 'week'>('day')
const deskDay = ref(fmtDate(new Date()))
const deskWeekOffset = ref(0)
const deskDeptId = ref<number | null>(null)
const deskDoctorId = ref<number | null>(null)
const deskLoading = ref(false)
const deskDays = ref<string[]>([])  // 当前窗口的日期：日视图 1 天 / 周视图 7 天
const deskRegs = ref<Record<string, any[]>>({})  // 就诊日 -> 挂号
const deskSchedules = ref<any[]>([])  // 当前窗口排班（带 totalSource / availableSource，余号直接用它）
const deskScheduleMap = ref<Record<string, any>>({})
/**
 * 当前窗口的**时间片段**（biz_schedule_slot，半小时一档）。
 *
 * 日视图的 Y 轴已经细到半小时，「某医生某半小时还剩几个号」这个数字的**唯一真值在段上**
 * （段之间号源并不等分，实测 20 个号切 8 段 = 3,3,3,3,2,2,2,2 —— 用主表 available 除以段数去摊
 * 出来的每一行数字都是假的）。所以日视图必须拉段，不能用排班主表凑。
 *
 * 只服务日视图：周视图的行仍是「班次」，用主表合计即可，不拉（省一次请求）。
 */
const deskSlots = ref<any[]>([])

/**
 * 周视图每格最多铺几张卡。
 *
 * 一个科室 4 位医生 × 每人 20 个号 = 单日 80 张卡，一周 560 张 —— 全铺出来不只是"不好看"，
 * DOM 会把浏览器拖死。真实 HIS 也是每格铺一屏内能看完的条数 + 「还有 N 人 → 看当天」。
 *
 * 阈值定 10：一格一天里 上午/下午/全天 三个班次合计 30 条是能看下来的（老王实测板幅够大），
 * 所以按「单个班次 10 条」折叠 —— 从第 11 个人才开始收，不是第 4 个。
 * 日视图单个医生单班次最多 20 张，仍然全铺。
 */
const DESK_WEEK_CELL_MAX = 10

const deskWeekLabel = computed(() =>
  deskDays.value.length ? `${deskDays.value[0]} ~ ${deskDays.value[6]}` : '')

/** 工具栏中间的日期文案：日视图是「2026-09-21 周一」，周视图是「上周一 ~ 本周日」 */
const deskRangeLabel = computed(() =>
  deskViewMode.value === 'day'
    ? `${deskDay.value} ${weekdayMap[new Date(deskDay.value + 'T00:00:00').getDay()]}`
    : deskWeekLabel.value)

/**
 * 医生排序：**按 doctor_id 升序**。
 *
 * 2026-09-22：试过拼音序（`Intl.Collator('zh-Hans-CN')`），最后改回 id ——
 * 这份库里的医生名是"医生甲 / 压测医生丙 / 权限验证探针医生"这类**没法按姓氏查**的名字，
 * 拼音序排出来人是找不到的；而 id 顺序**永远稳定**（同一个 id 永远在同一列/同一行），
 * 挂号员熟了之后记的是位置而不是名字 —— 位置漂移比"顺序不好看"难受得多。
 *
 * 前后端同口径：后端 `/schedule/list` 与 `/system/employee/selectList` 也都按 doctor_id / id 排，
 * 所以接口喂过来的顺序和这里算出来的一致，不存在"两个地方各排一次、结果不一样"。
 *
 * ⚠️ 必须转**数值**再比：doctor_id 是 BIGINT，按字符串比会出 "10" < "9" 的字典序倒挂；
 *    超过 2^53 的雪花 id 用 Number 也保不住精度，所以走 BigInt。
 */
const byDoctorId = (a: any, b: any) => {
  const x = BigInt(String(a?.id ?? a?.doctorId ?? 0))
  const y = BigInt(String(b?.id ?? b?.doctorId ?? 0))
  return x < y ? -1 : x > y ? 1 : 0
}

/**
 * 医生下拉选项 = 当前窗口「有排班」的医生。
 *
 * 直接从已加载的排班里取，不再单独调一套「科室-医生」联动接口：
 * 与科室筛选天然一致，不会出现「下拉里有人、看板上没他」的错位。
 * 按拼音序排：下拉里十几个人混着排的时候，找到目标全靠肉眼过滤。
 */
const deskDoctorOptions = computed(() => {
  const seen = new Map<string, any>()
  for (const s of deskSchedules.value) {
    if (s.doctorId && !seen.has(s.doctorId)) {
      seen.set(s.doctorId, {id: s.doctorId, doctorName: s.doctorName || '未定医生', deptName: s.deptName})
    }
  }
  return [...seen.values()].sort(byDoctorId)
})

/** 当前窗口的日期列表：日视图 1 天，周视图 7 天（周一起） */
const deskDateList = () => {
  if (deskViewMode.value === 'day') return [deskDay.value]
  const monday = mondayOf(deskWeekOffset.value)
  const days: string[] = []
  for (let i = 0; i < 7; i++) {
    const d = new Date(monday)
    d.setDate(monday.getDate() + i)
    days.push(fmtDate(d))
  }
  return days
}

/**
 * 看板取数 —— **整段一次查全**，不按天拆、不翻页。
 *
 * 排班与挂号走同一个日期区间：排班本来一次就查整段（`/schedule/list` 带 startDate/endDate），
 * 挂号以前却按天拆成 7 路、每路各自翻页（`pageSize=200`，单日超 200 条就再来一页），
 * 周视图一次开 7 个请求、数据多时是 7×页数 —— 而结果和一次区间查询完全一样。
 *
 * 为什么挂号必须整段全量：每格要显示「已挂 N / 总号源」和「还有 M 人 → 看当天」，
 * 拿到某一页根本没法算。后端 `/appoint/boardList` 因此刻意不分页。
 *
 * 也不再把 7 天的结果分别 fill 回去：一次拿回整段、一个循环归位，
 * 不存在「7 个请求里有一个失败 → 那一天静默变空」这种半死不活的中间态。
 */
const loadDesk = async () => {
  deskLoading.value = true
  try {
    // 周视图的行 = 医生名册（含没排班的），必须排在之行加载完再渲染，否则首帧缺行
    if (deskViewMode.value === 'week') await loadWeekRoster()
    const days = deskDateList()
    deskDays.value = days
    const params: any = {startDate: days[0], endDate: days[days.length - 1]}
    if (!isAllDept(deskDeptId.value)) params.deptId = deskDeptId.value
    if (deskDoctorId.value) params.doctorId = deskDoctorId.value

    // 排班 + 挂号并行，两个都是「整段一次查」，共 2 个请求
    const [schedRes, regRes] = await Promise.all([
      getScheduleList(params),
      getBoardRegistList(params),
    ])

    const scheds = schedRes.data?.records || schedRes.data || []
    deskSchedules.value = scheds
    const map: Record<string, any> = {}
    scheds.forEach((s: any) => { map[s.id] = s })
    deskScheduleMap.value = map

    // 按就诊日归位：口径是 visit_date（就诊日），与「今日队列」一致，不是到院日
    const regs: Record<string, any[]> = {}
    days.forEach(d => { regs[d] = [] })
    for (const r of ((regRes.data || []) as any[])) {
      const d = String(r?.visitDate || '').slice(0, 10)
      if (regs[d]) regs[d].push(r)
    }
    deskRegs.value = regs
    await loadDeskSlots(scheds)
    // 整轴 48 行：渲染完立刻把视口挪到当天第一档，别让人从 0 点往下翻
    await nextTick()
    scrollToFirstServiceRow()
  } catch (error) {
    console.error('加载预约看板失败:', error)
  } finally {
    deskLoading.value = false
  }
}

/**
 * 拉当前窗口排班的**时间片段**（批量一次，不逐条打请求）。
 *
 * 失败不抛、只清空：段只影响「格子里那几个数字」的精度，
 * 排班/挂号本身已经画出来了，不能因为一次段查询失败把整屏弄没。
 * 空数组会让日视图那几格显示「无段数据」，是可见的降级，不是静默丢数。
 */
const loadDeskSlots = async (scheds: any[]) => {
  if (deskViewMode.value !== 'day') {
    deskSlots.value = []
    return
  }
  const ids = (scheds || []).map((s: any) => s.id).filter(Boolean)
  if (!ids.length) {
    deskSlots.value = []
    return
  }
  try {
    const res = await getScheduleSlotsBatch(ids)
    deskSlots.value = res.data || []
  } catch (error) {
    console.error('加载时间片段失败:', error)
    deskSlots.value = []
  }
}

/**
 * 周视图的 Y 轴是**医生**，所以必须先有一份「医生名册」。
 *
 * 没排班的医生在 `biz_schedule` 里压根查不到 —— 只按排班去重凑不出他的行，
 * 而"这周谁还没排班"正是周视图要回答的半句话（另一半是"哪天满了"）。
 * 所以行 = 名册（在岗医生）∪ 排班数据，没有排班的医生占一整行七格「未排班」。
 *
 * 与排班面板同一个数据源 + 同一套过滤（`/system/employee/selectList`，empType=1 且 status=1）：
 * 两个页面对"谁是医生"必须给出同一个答案，否则会出现"排班页有他、看板没有他"。
 *
 * 反向的并集不能省：排班里的医生可能是外院/多点执业的（sys_employee 里查不到），
 * 只按名册出行会**漏掉真实排班**。
 */
const weekRoster = ref<any[]>([])
let rosterDeptKey: string | null = null
const loadWeekRoster = async () => {
  // 只跟科室有关，与翻周/翻天无关 → 按科室缓存，翻周不重复请求
  const key = String(deskDeptId.value ?? '__ALL__')
  if (rosterDeptKey === key) return
  try {
    const params: any = {empType: 1}
    if (!isAllDept(deskDeptId.value)) params.deptId = deskDeptId.value
    const res = await getEmployeeList(params)
    const list: any[] = res.data?.records || res.data || []
    weekRoster.value = list
      .filter((e: any) => e.status === 1)
      .map((e: any) => ({
        doctorId: e.id,
        doctorName: e.empName,
        deptId: e.deptId,
        deptName: e.deptName || departments.value.find((d: any) => String(d.id) === String(e.deptId))?.deptName || '',
      }))
    rosterDeptKey = key
  } catch (error) {
    // 失败不清空也不抛：拿不到名册时退化为「只有排班里的医生」，行会少，但绝不白屏
    console.error('加载医生名册失败:', error)
  }
}

// ========== 看板高度：撑满当前窗口 ==========
//
// Y 轴是全天 48 档，写死 620px 等于让人一路滚。所以按视口算高度：容器有多高就是多高，
// 超出的部分在**容器内部**滚（表头吸顶），主区那条页面级滚动条由下面的二次校正压掉。
//
// ⚠️ 上限必须夹住：页面向下滚之后容器 rect.top 会变负，直接拿它算会得到比窗口更高的表
//    （越往下滚表越高，永远滚不到底），所以上界锁 innerHeight - 120。
const deskScrollEl = ref<HTMLElement | null>(null)
const deskMaxH = ref(620)

/**
 * 往上找真正滚动的那个祖先（DashboardLayout 里是 flex-1 overflow-auto 的主区）。
 *
 * 注意从 **parentElement** 开始，跳过 `deskScrollEl` 自己 —— 它也是 overflow-auto，
 * 但它那条滚动条是看板内容滚动（48 行时间轴的正常行为），不是页面溢出。
 */
const findScrollParent = (el: HTMLElement | null): HTMLElement | null => {
  let p = el?.parentElement || null
  while (p) {
    const oy = getComputedStyle(p).overflowY
    if ((oy === 'auto' || oy === 'scroll') && p.scrollHeight > p.clientHeight + 1) return p
    p = p.parentElement
  }
  return null
}

const calcDeskHeight = () => {
  const el = deskScrollEl.value
  if (!el) return
  const top = el.getBoundingClientRect().top
  const h = window.innerHeight - top - 16
  deskMaxH.value = Math.max(320, Math.min(h, window.innerHeight - 120))
  // 二次校正：容器的上下都还有东西是 `innerHeight - top` 算不到的 ——
  //   上：工具栏内容随视图/数据折行时容器会下移；
  //   下：main 的 padding-bottom(24) + 卡片 p-4(16) + 卡片外层的 space-y-6(24)。
  // 这几项加起来几十 px，硬编码等于换个页面就错 —— 所以按主区的**真实溢出**逐帧收敛。
  // 最多收 3 次：够。（左侧日历列已用同一个 deskMaxH 封顶，每次收缩都会真的减少行高，
  // 不会出现「越收溢出还在」的假收敛 —— 那正是只封桌子的旧版本踩的坑）
  const settle = (times: number) => requestAnimationFrame(() => {
    const sc = findScrollParent(el)
    if (!sc || times <= 0) return
    const over = sc.scrollHeight - sc.clientHeight
    if (over <= 0) return
    deskMaxH.value = Math.max(320, deskMaxH.value - over)
    settle(times - 1)
  })
  settle(3)
}

/**
 * 数据到位后把视口落到当天**第一个有排班的档**。
 *
 * 刻度压缩之后，第一行本来就是早班第一档（scrollTop ≈ 0）；这里留着是因为压缩只按
 * 「有没有排班」判，筛掉号源已满的档之后首行仍可能不是第一档 —— 逻辑不变，只是通常不滚。
 */
const scrollToFirstServiceRow = () => {
  const el = deskScrollEl.value
  if (!el || deskViewMode.value !== 'day') return
  let lo = Infinity
  for (const s of deskSchedules.value) {
    if (s.scheduleDate !== deskDay.value) continue
    const a = toMin(s.startTime)
    if (a != null) lo = Math.min(lo, a)
  }
  if (!Number.isFinite(lo)) {
    el.scrollTop = 0
    return
  }
  const key = fmtMin(Math.floor(lo / 30) * 30)
  const td = el.querySelector(`tbody td[data-row="${key}"]`) as HTMLElement | null
  const tr = td?.parentElement as HTMLElement | null
  if (!tr) {
    el.scrollTop = 0
    return
  }
  // 用 rect 差值而不是 offsetTop：tr 的 offsetParent 未必是滚动容器（中间没有任何定位元素时
  // 会一路算到 body），届时 offsetTop 会把 Header 的高度也算进去，滚过头。
  // ⚠️ 必须扣掉吸顶表头的高度：thead 是 sticky 的，滚完永远盖在容器顶部 ——
  //    把首档滚到"距容器顶 8px"等于滚到表头**背后**，用户看到的第一行其实是下一档
  //    （实测 08:00 被盖住、屏上从 08:30 开始）。落到表头下方 8px 才是真的可见。
  const thead = el.querySelector('thead')
  const headH = thead ? thead.getBoundingClientRect().height : 0
  el.scrollTop = Math.max(0,
    tr.getBoundingClientRect().top - el.getBoundingClientRect().top + el.scrollTop - headH - 8)
}

const onWinResize = () => calcDeskHeight()

// ---- 格子取数：两个视图共用同一组函数，只有"按天+班次"还是"按医生+班次"的差别 ----

/** 余号合计（余号口径 = bis_schedule.available_source，与号源面板一致） */
const deskAvail = (schedules: any[]) =>
  schedules.reduce((sum, s) => sum + (Number(s.availableSource) || 0), 0)

/** 总号源合计 */
const deskTotal = (schedules: any[]) =>
  schedules.reduce((sum, s) => sum + (Number(s.totalSource) || 0), 0)

/** 某组排班下的在板挂号：已退号不上板（历史在挂号记录 tab 查） */
const deskRegsOfSchedules = (date: string, schedules: any[]) => {
  if (!schedules.length) return []
  const ids = new Set(schedules.map(s => s.id))
  return (deskRegs.value[date] || []).filter(r => Number(r.registStatus) !== 5 && ids.has(r.scheduleId))
}

/**
 * 周视图格子 = **某医生 × 某天**（跨当天全部班次合计）。
 *
 * 原来这一格是「某天 × 某班次 × 全院医生」：一行医生当格子内容铺不下，
 * 医生维度的问句（"张三这周还剩多少号"）根本问不出来 —— 现在它就是行，直接读一行即可。
 */
const weekCellSchedules = (date: string, doctorId: any) =>
  deskSchedules.value.filter(s =>
    s.scheduleDate === date && String(s.doctorId) === String(doctorId))

const weekCellRegs = (date: string, doctorId: any) =>
  deskRegsOfSchedules(date, weekCellSchedules(date, doctorId))

/** 日视图某医生当天的排班（按段定位用，不限于某班次） */
const dayDoctorSchedsOf = (doctorId: any) =>
  deskSchedules.value.filter(s => s.scheduleDate === deskDay.value && s.doctorId === doctorId)

/** 日视图格子：某医生 × 某半小时段 —— 覆盖这一段的排班（开单时要的是排班，不是段） */
const daySlotCellSchedules = (doctorId: any, slotStart: string) => {
  const m = toMin(slotStart)
  if (m == null) return []
  return dayDoctorSchedsOf(doctorId).filter(s => {
    const a = toMin(s.startTime)
    const b = toMin(s.endTime)
    return a != null && b != null && a <= m && m < b
  })
}

/**
 * 日视图格子的「号源数字来源」——**段投影**。
 *
 * 有段 → 用真段（段之间号源不等分，只有段上的数字是真的）。
 * 无段（历史排班，号源只在主表）→ 整条排班的号源记在它窗口的**第一行**，其余行不记：
 * 否则同一条排班的 20 个号会被它覆盖的 8 行各算一遍，格子加总 160 对不上列头的 20。
 */
const daySlotSegments = (scheds: any[], slotStart: string) => {
  const ids = new Set(scheds.map((s: any) => String(s.id)))
  const segs = deskSlots.value.filter((s: any) =>
    ids.has(String(s.scheduleId)) && String(s.startTime).slice(0, 5) === slotStart)
  if (segs.length) return segs
  const m = toMin(slotStart)
  return scheds.filter((s: any) => {
    const a = toMin(s.startTime)
    return firstSlotStartOf(s.id) == null && a != null && Math.floor(a / 30) * 30 === m
  })
}

/**
 * 日视图格子里的人：某医生 × 某半小时段的挂号。
 *
 * 挂号自带段快照（`slotStart`/`slotEnd`），直接按它归位 —— 不靠排班窗口去猜。
 * 没有段快照的历史挂号：不猜他挂的是哪一档，统一收到所属排班的第一段并标 `未选段`，
 * 数据不丢，也不假装知道。
 */
const daySlotCellRegs = (doctorId: any, slotStart: string) => {
  const date = deskDay.value
  const ids = new Set(dayDoctorSchedsOf(doctorId).map((s: any) => s.id))
  const all = (deskRegs.value[date] || []).filter((r: any) =>
    Number(r.registStatus) !== 5 && ids.has(r.scheduleId))
  const hit: any[] = []
  const fallback: any[] = []
  for (const r of all) {
    const head = String(r.slotStart || '').slice(0, 5)
    if (head === slotStart) {
      hit.push(r)
    // 无快照的挂号：段表的第一段优先，段表查不到再退回排班窗口首档（见 fallbackSlotStartOf）。
    // 两档都兜不住才真的丢 —— 那种情况下宁可也让它在首档出现，也不要静默不见。
    } else if (!head && (fallbackSlotStartOf(r.scheduleId) ?? null) === slotStart) {
      fallback.push({...r, __unslotted: true})
    }
  }
  return [...hit, ...fallback]
}

/** 日视图格子：某医生 × 某班次（当天）—— 周视图的「隐藏已满」总账仍按班次口径算，保留 */
const dayCellSchedules = (doctorId: any, type: number) =>
  deskSchedules.value.filter(s => s.doctorId === doctorId && Number(s.scheduleType) === type)

const dayCellRegs = (doctorId: any, type: number) =>
  deskRegsOfSchedules(deskDay.value, dayCellSchedules(doctorId, type))

/**
 * 把一组排班去重成「医生集合」。
 *
 * 日视图用它按**当天**去重出医生列，周视图按**整周窗口**去重出医生行 ——
 * 同一套身份口径（`doctorId` 唯一），换周/换天不会出现"这列多了个人"。
 */
const doctorsOfSchedules = (scheds: any[]) => {
  const seen = new Map<string, any>()
  for (const s of scheds) {
    if (!s.doctorId) continue
    const key = String(s.doctorId)
    const exist = seen.get(key)
    if (!exist) {
      seen.set(key, {
        id: s.doctorId,
        doctorName: s.doctorName || '未定医生',
        deptId: s.deptId,
        deptName: s.deptName,
        roomName: s.roomName,
      })
    } else if (!exist.roomName && s.roomName) {
      exist.roomName = s.roomName
    }
  }
  return seen
}

/** 日视图的医生列：当天有排班的医生（同一医生多诊室多班次合成一列），按拼音序排 */
const dayDoctors = computed(() =>
  [...doctorsOfSchedules(deskSchedules.value.filter(s => s.scheduleDate === deskDay.value)).values()]
    .sort(byDoctorId))

/**
 * 周视图的行 = **医生**（2026-09-22：原为班次，行是半天粒度、格里塞满患者卡片，
 * 回答不了"某医生这周排没排、还剩多少"）。
 *
 * 行取 「在岗医生名册 ∪ 本周有排班的医生」：
 *   名册兜住没排班的人（`weekRoster`），排班兜住名册里没有的外院/多点执业医生 —— 两边都不能省。
 * 选中了具体医生时只留他一行：周视图此时就是"这一位医生的一周"，别人的行是噪声。
 * 同日视图一样按拼音序排 —— 两边的行/列顺序打架，翻一次视图就得重新找人。
 */
const weekDoctorRows = computed(() => {
  if (deskViewMode.value !== 'week') return []
  const seen = doctorsOfSchedules(deskSchedules.value)
  for (const e of weekRoster.value) {
    const key = String(e.doctorId)
    if (!seen.has(key)) {
      seen.set(key, {
        id: e.doctorId,
        doctorName: e.doctorName || '未定医生',
        deptId: e.deptId,
        deptName: e.deptName,
        roomName: '',
      })
    }
  }
  let list = [...seen.values()]
  if (deskDoctorId.value) {
    list = list.filter(d => String(d.id) === String(deskDoctorId.value))
  }
  // 行内的统计（已挂 / 剩余）在下面 weekRowStat 里按 `deskSchedules` 现算，不在这里缓存：
  // 挂号/退号之后 deskRegs 变了，行要有反应 —— 缓存一份就等于让行停在上一刻。
  // 行序与日视图的列序用同一个比较器（byDoctorId），翻视图时位置不漂移。
  return list.sort(byDoctorId).map(d => ({
    key: String(d.id),
    doctorId: d.id,
    label: d.doctorName,
    deptName: d.deptName,
  }))
})

/**
 * 周视图行（某医生）在**当前这一周**的合计。
 *
 * 「这个医生有了多少个挂号患者、还剩余多少」在周视图里要有两个尺度：
 * 格子（某天）由 `deskCell` 给，整周合计由这里给 —— 后者直接写在行首，扫一行就知道这人这周忙不忙。
 *
 * 口径与格子严格同源：`deskTotal` / `deskAvail` / `deskRegsOfSchedules`（已退号不计数），
 * 不另算一套 —— 否则行首说 20、七格加起来是 18，同一屏两个数字打架。
 */
const weekRowStat = (doctorId: any) => {
  const scheds = deskSchedules.value.filter(s => String(s.doctorId) === String(doctorId))
  let regs = 0
  if (scheds.length) {
    for (const d of deskDays.value) {
      regs += deskRegsOfSchedules(d, scheds.filter(s => s.scheduleDate === d)).length
    }
  }
  return {total: deskTotal(scheds), avail: deskAvail(scheds), regs}
}

// ========== 日视图 Y 轴：全天时间刻度（00:00 ~ 24:00，半小时一档）+ 刻度压缩 ==========
//
// 为什么是**固定刻度**（0~24 点整轴）而不是"当天有排班的时间窗并集"：
// 挂号员在板上找的是"9 点那档还有没有号"，时间轴本身必须是稳定参照系 ——
// 轴跟着排班伸缩的话，同一行换一天就是另一个时刻，每天都要重新目视对齐
// （这正是上一个版本的问题：轴长随排班变，行首还错着位）。
// 真实 HIS / 日历类排班控件都是 0~24 点整轴 + 内部滚动，这里照做。
//
// 为什么行是半小时而不是「上午 / 下午」：号源的事实落在半小时段上（biz_schedule_slot），
// 而且段之间**并不等分**（实测 20 个号切 8 段 = 3,3,3,3,2,2,2,2）。
// 用「上午」一行来显示，等于把 8 个不同余号的段压成一个数 —— 回答不了"9 点那档还有没有号"，
// 而这正是分时段预约挂号要回答的问题。
//
// 整轴 48 行 ≠ 48 行都画：没人排班的档**压缩掉**（见 daySlotRows），
// 压缩后一天通常只剩十几行，一屏看得全，轴内部的滚动不再是常态。

/**
 * "HH:mm" / "HH:mm:ss" → 当天分钟数。解析不了返回 null —— 别让 NaN 混进大小比较（NaN 比较恒 false，会静默吞行）
 *
 * ⚠️ 秒必须可选：实测后端既回 `08:00` 也回 `08:00:00`（看板压测夹具就是带秒的）。
 *    写成严格 `$` 结尾时，`08:00:00` 解析成 null → 这一条排班被 `continue` 掉，
 *    后果是**这一档整行消失、这位医生在日视图连列都没有**，且不报任何错。
 */
const toMin = (hhmm: any): number | null => {
  const m = /^(\d{1,2}):(\d{2})(?::(\d{2}))?$/.exec(String(hhmm ?? '').trim())
  return m ? Number(m[1]) * 60 + Number(m[2]) : null
}

const fmtMin = (m: number) =>
  `${String(Math.floor(m / 60)).padStart(2, '0')}:${String(m % 60).padStart(2, '0')}`

/**
 * 时段分组（只用于行首的分组标签，不参与任何取数）。
 * 四档与 `biz_schedule.schedule_type` 对齐：4凌晨 / 1上午 / 2下午 / 晚上。
 */
const slotGroupOf = (m: number) =>
  m < 8 * 60 ? '凌晨' : m < 12 * 60 ? '上午' : m < 18 * 60 ? '下午' : '晚上'

/** 某排班的段起点（无段返回 null）。**只用于判断"这条排班有没有段数据"**，别拿它给人归位 */
const firstSlotStartOf = (scheduleId: any) => {
  const hit = deskSlots.value.find((s: any) => String(s.scheduleId) === String(scheduleId))
  return hit ? String(hit.startTime).slice(0, 5) : null
}

/**
 * 没有段快照的挂号该落在哪一档。
 *
 * ⚠️ 不能复用 `firstSlotStartOf`：那个函数对"没段数据的排班"返回 null，
 *    于是**历史排班（号源只在主表，biz_schedule_slot 里根本没有它的段）上的挂号
 *    会一档都落不上 —— 整批人在日视图里凭空消失**。
 *    实测压测科室 94 条挂号全挂在无段的排班上，改半小时档之后页面卡片数一度是 0，
 *    而同一屏的号源数字还写着"已挂 0 / 40 余 22"（used=18）—— 数字说用了 18 个、
 *    名单一张都没有，这种"静默丢人"是挂号窗口最不能接受的一类错误。
 *
 * 所以这里分两级兜底：段表的第一段 → 排班窗口的第一档（`start_time` 向下对齐到半点）。
 */
const fallbackSlotStartOf = (scheduleId: any) => {
  const seg = firstSlotStartOf(scheduleId)
  if (seg) return seg
  const s = deskSchedules.value.find((x: any) => String(x.id) === String(scheduleId))
  const a = toMin(s?.startTime)
  return a == null ? null : fmtMin(Math.floor(a / 30) * 30)
}

/**
 * 日视图 Y 轴：全天 48 档 `00:00~00:30` … `23:30~24:00`，**但只渲染有排班的档**（刻度压缩）。
 *
 * 压缩规则：这一档所有医生都没排班 → 这一行不出现。48 行里绝大多数是空档（凌晨、半夜、午休），
 * 全画出来等于让人滚三屏找早班第一档 —— 而"这档没人排班"这个事实，用**不存在**表达反而更清楚
 * （空行和"排了但没号"长得一样，是上一版被吐槽的原因）。
 *
 * 与"取当天排班时间窗并集"的差别：并集保留的是**连续区间**，区间里的空档照样画；
 * 压缩是按档独立的 —— 上午满、中午空、下午满，画出来就是上午和下午两段，中间不补空行。
 *
 * ⚠️ 压缩掉的一段必须看得出来：否则 11:30 下面直接接 14:00，读的人分不清"中间没人排班"
 *    还是"被筛选/隐藏开关筛掉了"。所以断档处打一条**加粗分隔线**（`sep`），
 *    与「凌晨/上午/下午/晚上」的分组线走同一个视觉通道 —— 段落靠线分，不再靠文字标签。
 */
const DAY_SLOT_STEP = 30
const daySlotRows = computed(() => {
  const rows: {key: string; label: string; group: string; sep: boolean; start: string}[] = []
  if (deskViewMode.value !== 'day') return rows
  const doctors = dayDoctors.value
  // 压缩判据只看「有没有排班覆盖这一档」，不看号源是否已满：
  // 满的档要留在板上（它上面还挂着"已挂/余号"和挂号卡片），藏掉等于把已发生的号一起藏了。
  const busy = (start: string) => doctors.some(d => daySlotCellSchedules(d.id, start).length > 0)
  let prev = -DAY_SLOT_STEP // 上一保留档的分钟数：初值保证第一行不会被误判成断档
  for (let m = 0; m < 24 * 60; m += DAY_SLOT_STEP) {
    const start = fmtMin(m)
    if (!busy(start)) continue
    const gap = m !== prev + DAY_SLOT_STEP
    rows.push({
      key: start,
      label: `${start}~${fmtMin(m + DAY_SLOT_STEP)}`,
      start,
      group: slotGroupOf(m),
      // 第一行不画线（它上面没有东西需要分隔）；断档或跨组 → 加粗线
      sep: rows.length > 0 && (gap || slotGroupOf(m) !== slotGroupOf(prev)),
    })
    prev = m
  }
  return rows
})

/**
 * 当前视图的行 —— 日视图 = 半小时段、周视图 = **医生**。
 *
 * 2026-09-22：周视图原是「班次」行，一格 = 一天 × 一个班次 × 全院医生 →
 * 格里只能塞"患者名单"，"某医生还剩几个号"要点开才知道；改成医生行之后格子天然回答这事。
 *
 * 两个视图共用同一张表，只有行的定义不同（与 `deskColumns` 列的定义同理），
 * 这样格子渲染 / 卡片 / 开单都只有一份实现。
 */
const deskRows = computed<{key: any; label: string; group?: string; sep?: boolean; start?: string; doctorId?: any; deptName?: string}[]>(() =>
  deskViewMode.value === 'day'
    ? daySlotRows.value
    : weekDoctorRows.value)

/** 余号文案：0 → 约满，1~4 → 少量余号（真实 HIS 排班大屏的提示口径） */
const deskAvailText = (schedules: any[]) => {
  const avail = deskAvail(schedules)
  if (avail <= 0) return '约满'
  return avail < 5 ? `余 ${avail}（少量）` : `余 ${avail}`
}

const deskAvailClass = (schedules: any[]) => {
  const avail = deskAvail(schedules)
  if (avail <= 0) return 'text-red-500'
  return avail < 5 ? 'text-amber-600' : 'text-emerald-600'
}

/** 某医生当天的全部排班（列头余号用；同一排班只算一次） */
const dayDoctorSchedules = (doctorId: any) =>
  deskSchedules.value.filter(s => s.scheduleDate === deskDay.value && s.doctorId === doctorId)

/**
 * 这位医生在当前视图范围内**出的是不是专家号**（`biz_schedule.is_expert`）。
 *
 * 为什么挂在医生名后面而不是挂号记录上：挂号窗口找的是"今天哪位是专家"，
 * 视线落在行首/列头的医生名上，号别要跟着名字走才有意义；
 * 卡片上的号别由 `REGIST_TYPE` 单独渲染（那是一笔挂号自己的属性，两回事）。
 *
 * 判据用**任一条排班是专家**而不是"全部都是"：同一位医生上午专家、下午普通是常态，
 * 判成"全部"会让专家门诊那半天被显示成普通号 —— 漏标比多标代价大（专家号挂号费不同、患者冲着专家来的）。
 * 范围跟着当前视图收口（日视图只看当天），否则周视图上周排过专家这周也挂着牌。
 */
const isExpertDoctor = (doctorId: any) => {
  const scope = deskViewMode.value === 'day'
    ? deskSchedules.value.filter((s: any) => s.scheduleDate === deskDay.value)
    : deskSchedules.value
  return scope.some((s: any) => String(s.doctorId) === String(doctorId) && Number(s.isExpert) === 1)
}

/**
 * 看板列 = 日视图的「医生」/ 周视图的「日期」。
 *
 * 两个视图共用同一张表，只有列的定义不同 —— 这样班次行、格子渲染、卡片都只有一份实现，
 * 不会出现「日视图改好了、周视图还是旧样式」这种两套代码的漂移。
 */
const deskColumns = computed(() => {
  if (deskViewMode.value === 'day') {
    return dayDoctors.value.map(d => ({
      key: 'doc-' + d.id,
      title: d.doctorName,
      sub: [d.deptName, d.roomName].filter(Boolean).join(' · '),
      doctorId: d.id as any,
      // 格子开单（addFromCell）的科室兜底：工具栏可能是「全部科室」，此时只能从排班本身取真实科室
      deptId: d.deptId as any,
      date: deskDay.value,
    }))
  }
  return deskDays.value.map(d => ({
    key: 'day-' + d,
    title: d,
    sub: weekdayMap[new Date(d + 'T00:00:00').getDay()],
    doctorId: null as any,
    date: d,
  }))
})

/**
 * 格子数据一次性算好，别在模板里反复 find/filter。
 *
 * 周视图 21 格、日视图最多「医生数 × 3」格，每格都在模板里现算会把每次渲染变成 O(格子数 × 挂号数)。
 */
const deskCellMap = computed<Record<string, {schedules: any[]; regs: any[]; srcSchedules: any[]}>>(() => {
  const map: Record<string, {schedules: any[]; regs: any[]; srcSchedules: any[]}> = {}
  for (const col of deskColumns.value) {
    for (const row of deskRows.value) {
      if (deskViewMode.value === 'day') {
        const srcSchedules = daySlotCellSchedules(col.doctorId, row.start as string)
        map[`${col.key}|${row.key}`] = {
          // schedules = 段投影：余号/总数一律取段上的真值（下面的 deskTotal / deskAvailText 直接用它）
          schedules: daySlotSegments(srcSchedules, row.start as string),
          srcSchedules,
          regs: daySlotCellRegs(col.doctorId, row.start as string),
        }
      } else {
        const schedules = weekCellSchedules(col.date, row.key)
        map[`${col.key}|${row.key}`] = {schedules, srcSchedules: schedules, regs: deskRegsOfSchedules(col.date, schedules)}
      }
    }
  }
  return map
})

/**
 * 取一格。行键在日视图是 `"08:30"`、周视图是班次数字(1/2/3/4)，同一视图内唯一、跨视图不混用。
 */
const deskCell = (colKey: string, rowKey: any) =>
  deskCellMap.value[`${colKey}|${rowKey}`] || {schedules: [], regs: [], srcSchedules: []}

/**
 * 这一格「没号可挂」= 压根**没有排班**（2026-09-22）。
 *
 * 判据用 `srcSchedules`（覆盖这一档的排班本体），不用段投影：段没拉到 ≠ 没排班。
 * 判定收口成一个函数是因为它现在同时决定两件事：
 *   · 文案走「未排班」分支
 *   · 格子走 `desk-cell-na`（淡灰底 + `cursor: not-allowed`）
 * 两处各写一遍 `!…srcSchedules.length` 迟早会出现"字写着未排班、格子看着能点"。
 */
const isCellUnavailable = (col: any, rowKey: any) =>
  !deskCell(col.key, rowKey).srcSchedules.length

const showRescheduleDialog = ref(false)
const rescheduleLoading = ref(false)
const rescheduleForm = ref({
  registId: null as any,
  patientName: '',
  deptId: null as number | null,
  visitDate: '' as string | null,
  scheduleId: null as number | null,
  // 新号源下的时间段（可选）：传了按段迁移（还旧段扣新段），不传只迁移主表号源
  slotId: null as number | null,
})
const rescheduleSchedules = ref<any[]>([])
const rescheduleSlots = ref<any[]>([])
const rescheduleSlotLoading = ref(false)

/** 改约选新号源 → 加载其时间段并清掉已选段（旧段属于上一个号源） */
const handleRescheduleScheduleChange = (scheduleId: any) => {
  rescheduleForm.value.slotId = null
  rescheduleSlots.value = []
  const s = rescheduleSchedules.value.find((x: any) => x.id === scheduleId)
  if (!s?.id) return
  rescheduleSlotLoading.value = true
  getScheduleSlots(s.id).then((res: any) => {
    rescheduleSlots.value = res.data || []
  }).catch((error: any) => {
    console.error('加载改约时间段失败:', error)
  }).finally(() => {
    rescheduleSlotLoading.value = false
  })
}

/** 挂号状态点颜色（1挂号 2签到 3接诊 4就诊 5退号 6过号 7爽约 8未就诊） */
const regStatusDot: Record<number, string> = {
  1: 'bg-blue-500',
  2: 'bg-cyan-500',
  3: 'bg-purple-500',
  4: 'bg-emerald-500',
  5: 'bg-red-400',
  6: 'bg-orange-500',
  7: 'bg-rose-600',
  8: 'bg-slate-400',
}

/**
 * 卡片状态标签：每个状态都有一句话说明，颜色与色点一致。
 *
 * 口径 = `AppointStatusEnum`（1已挂号 2已签到 3已接诊 4已就诊 5已退号 6已过号 7爽约 8未就诊）。
 * 「已退号(5)」不定义 —— 退号不上板（历史去挂号记录 tab 查）。
 * 别把「已签到」写成「候诊中」：那是 `QueueStatusEnum` 的词，两套枚举不能互套。
 */
const deskStatusChip: Record<number, { text: string; cls: string; dot: string }> = {
  1: {text: '已挂号', cls: 'bg-blue-50 text-blue-600', dot: 'bg-blue-500'},
  2: {text: '已签到', cls: 'bg-cyan-50 text-cyan-700', dot: 'bg-cyan-500'},
  3: {text: '就诊中', cls: 'bg-purple-100 text-purple-700', dot: 'bg-purple-500'},
  4: {text: '已就诊', cls: 'bg-emerald-100 text-emerald-700', dot: 'bg-emerald-500'},
  6: {text: '已过号', cls: 'bg-orange-100 text-orange-700', dot: 'bg-orange-500'},
  7: {text: '爽约', cls: 'bg-rose-100 text-rose-700', dot: 'bg-rose-600'},
  8: {text: '未就诊', cls: 'bg-slate-100 text-slate-500', dot: 'bg-slate-400'},
}

const deskShiftLabel = (type: number) =>
  boardShifts.find(s => s.type === type)?.label || ''

const handleDeskDeptChange = () => {
  // 换科室后原来选中的医生可能压根不在这个科室，必须清掉，否则看板会空得莫名其妙
  deskDoctorId.value = null
  // 月历圆点跟随科室：主区看呼吸内科，圆点也只标呼吸内科开诊的日子，否则点进去是空的
  loadCalServiceDays()
  loadDesk()
}

const handleDeskDoctorChange = () => {
  loadCalServiceDays()
  loadDesk()
}

/**
 * 看板工具栏的「新增预约」：不带具体号源（看板是全局视角，格子才是具体某一档），
 * 但必须带科室 + 日期 —— 这两项在工具栏上是可见的事实，让用户重选一遍等于白给。
 * 「全部科室」没有具体科室可带，`openAddFromPanel` 会置空不猜（见那里的第 ③ 条）。
 */
const handleDeskAdd = () => {
  addFromPanel({
    deptId: deskDeptId.value,
    visitDate: deskViewMode.value === 'day' ? deskDay.value : (deskDays.value[0] || ''),
  })
}

/** 视图切换：日视图翻天、周视图翻周（同一个工具栏按钮，语义随视图变） */
const handleDeskViewChange = async () => {
  // 明细表只属于周视图（它按「日期 × 医生」定位）；切走时留着会指向一个看不见的格子
  selectedCell.value = null
  // 2026-09-22：周视图没有「隐藏已满/停诊」开关了（只在日视图左栏），
  // 从日视图带着"已勾选"切过来会让周视图静默少掉几个班次、而开关还不在这一屏 —— 关不掉。
  // 切到周视图一律复位：这一屏没有开关，就必须是未隐藏的全量。
  if (deskViewMode.value === 'week') deskHideFull.value = false
  await loadDesk()
  // ⚠️ 切视图必须重算看板高度：日/周的工具栏内容不一样（周视图多一串「隐藏已满/停诊」开关），
  //    容器一折行工具栏就多占 44px —— 而看板高度是首屏量好的常量，不会自己跟着长高，
  //    结果就是主区冒出一条纵向滚动条（实测 week 溢出 44px / day 为 0）。重算一下把它收回去。
  await nextTick()
  calcDeskHeight()
}

const handleDeskStep = (delta: number) => {
  selectedCell.value = null
  if (deskViewMode.value === 'day') {
    const d = new Date(deskDay.value + 'T00:00:00')
    d.setDate(d.getDate() + delta)
    deskDay.value = fmtDate(d)
  } else {
    deskWeekOffset.value += delta
  }
  loadDesk()
}

/**
 * 刷新：重刷**当前视图**（日视图 = 这一天，周视图 = 这一周），多余的一点都不动。
 *
 * 名册/月历圆点也一起刷新：新入职的医生要出现在周视图行里、新排的班要在月历上有点，
 * 只重拉排班会出现"板上有他、月历没点"。两个都是各自的按科室缓存，重复点不会打无效请求。
 */
const handleDeskRefresh = async () => {
  selectedCell.value = null
  rosterDeptKey = null       // 强制重取名册：新入职/离职的人要反映到周视图的行上
  await loadDesk()
  loadCalServiceDays()       // 不 await：圆点是导航装饰，晚一拍到不影响任何操作
}

const handleDeskToday = () => {
  deskDay.value = fmtDate(new Date())
  deskWeekOffset.value = 0
  selectedCell.value = null
  loadDesk()
}

/**
 * 周视图点日期（列头或「还有 N 人」）→ 下钻到当天日视图。
 *
 * 这就是真实 HIS 的「格子里只铺两块，点开切到日模式看全部」：
 * 周视图回答"哪天忙"，日视图回答"具体是谁"，两者共用同一份已加载数据，切过去不再打接口以外的东西。
 */
const drillToDay = (date: string) => {
  deskDay.value = date
  deskViewMode.value = 'day'
  selectedCell.value = null
  loadDesk()
}

/**
 * 这笔挂号的钱收了没有 —— 已收费(2) / 部分退费(4) 都表示钱已经动过。
 *
 * 口径 = `biz_charge_info.charge_status`（his_charge_status 字典），
 * 由后端 `BizAppointInfoListVO.paymentStatus` 带出来。
 */
const isRegistPaid = (r: any) => [2, 4].includes(Number(r?.paymentStatus))

/**
 * 号源能不能改（改约 / 换号源）—— **只有「已挂号(1)」且这笔还没收钱**。
 *
 * 口径与后端 `AppointStatusEnum.isSourceChangeAllowed` 一致，真边界在 `updateRegist`。
 *
 * 为什么已签到(2) 也不给改：号源已经消耗、患者已经在队列里排着，
 * 换号源得连带把队列行搬走，那不是改约而是「先退再挂」。
 *
 * 为什么已收费(2/4) 不给改：收费单是挂在挂号单上的，挂号记录一改（科室/医生/日期/号别），
 * 收费单就与挂号事实对不上——财务对账、科室日报、医保对账全部错位，普通号↔专家号的价差也没人处理。
 * 手机端/网上预约付过款的尤其如此：钱已经在微信/支付宝里，挂号台把号源一换，没人能把这笔钱挪过去。
 * 真实窗口的做法就是这一条：已缴费患者换号走「退号退费（按原支付渠道退回）→ 重新挂号」。
 * 后端同样拦（`AppointServiceImpl#assertSourceChangeUnpaid`），前端这层只是别让人点了才被拒。
 */
const canReschedule = (r: any) => Number(r?.registStatus) === 1 && !isRegistPaid(r) && !isVisitDatePast(r)

/**
 * 就诊日是否已经过去（今天 09-26 看 09-25 的号 → true）。
 *
 * 状态闸门（1/2 可退）管不到日期：日终结转没跑的那天（夜里关机、停诊、导数据），
 * 昨天的号还停在「已挂号」，于是满屏退号/改约按钮 —— 而这两件事都不成立，
 * 号源属过去日期，退了也还不回池（见后端 DayEndSettleMapper 类注释）。
 *
 * 只到「日」不到「时段」：当天下午的号没来看、傍晚来窗口退钱是合理诉求。
 * 后端同口径在 `AppointServiceImpl#visitDatePastReason`，前端这层只是「不让点」。
 */
const isVisitDatePast = (r: any) => {
  const d = String(r?.visitDate || '').slice(0, 10)
  return !!d && d < fmtDate(new Date())
}

/**
 * 能不能退号 —— **只有「已挂号(1)」「已签到(2)」，且就诊日还没过**。
 *
 * 与后端 `AppointStatusEnum.isCancelable` + `visitDatePastReason` 同一口径。前端这层只是「不让点」，边界在后端
 * `cancelRegist`（会再拦一次并给出原因）。其余状态都不给退：
 *   已接诊(3)/已就诊(4) 是诊疗事实；已过号(6)/爽约(7)/未就诊(8) 的号源属过去日期，
 *   退了也还不了池（见 DayEndSettleMapper）；已退号(5) 更没有重复退的道理。
 *
 * 已收费的**能退**：退号会联动退费（后端 `settleChargeOnRegistCancel` 走 SPI 到 his-charge），
 * 已收费→按原渠道退回，待收费→收费单作废。不给退反而会把患者困住 —— 钱退不回来才是真事故。
 */
const canCancelRegist = (r: any) => [1, 2].includes(Number(r?.registStatus)) && !isVisitDatePast(r)

/**
 * 能不能「申请退费」—— 人没来、日期已过、钱已收进账。
 *
 * 退号做不到的那一半（跨期退费）由它补上：挂号行保持「爽约/未就诊」不动（那是就诊事实），
 * 钱走「退费申请 → 收费处审核 → 执行」这条留痕链路，允许跨期。
 * 已退号(5) 排除：退号时已经联动退过费了，再申请就是重复退款。
 * 已接诊(3)/已就诊(4) 排除：人看完了病，挂号费不该退。
 */
const canApplyRefund = (r: any) => !!r?.billId
    && isVisitDatePast(r)
    && isRegistPaid(r)
    && [1, 2, 6, 7, 8].includes(Number(r?.registStatus))

/** 锁死原因文案：挂到卡片 title 上，别让用户点了没反应还不知道为什么 */
const sourceLockReason = (r: any) => {
  if (isVisitDatePast(r)) {
    return `就诊日 ${String(r?.visitDate || '').slice(0, 10)} 已过，不能退号或改约，患者未到诊需退挂号费的，请到「挂号记录」里发起退费申请`
  }
  if (Number(r?.registStatus) === 1 && isRegistPaid(r)) {
    return '该挂号已收费，不能直接变更号源；请先退号退费（按原支付渠道退回）后重新挂号'
  }
  switch (Number(r?.registStatus)) {
    case 3:
      return '已接诊（就诊中）：诊疗已开始，不再允许调整或变更号源'
    case 4:
      return '已就诊：诊疗已结束，不再允许调整或变更号源'
    case 5:
      return '已退号'
    case 6:
      return '已过号'
    case 7:
      return '爽约（日终结转收尾）：该号源属过去日期，不可变更'
    case 8:
      return '未就诊（日终结转收尾）：该号源属过去日期，不可变更'
    default:
      return ''
  }
}

// ========== 看板卡片上的患者信息（一行一件事，格子只有 ~110px 宽） ==========

/** 性别年龄：「男 52岁」；没采集到的那一项就不显示，不编造 */
const deskPatientMeta = (r: any) => {
  const parts: string[] = []
  const g = patientGenderText(r?.gender)
  if (g !== '—') parts.push(g)
  const a = patientAgeText(r?.age)
  if (a !== '—') parts.push(a)
  return parts.join(' ')
}

/** 就诊号（排队号）：签到才建队列行，所以未签到时为空，不写占位 */
const deskCardVisitNo = (r: any) => (r?.queueNo ? `${r.queueNo} 号` : '')

/** 第二行：医生 · 号别。急诊号要一眼能挑出来（走另一条就诊流程） */
const deskCardDoctorLine = (r: any) => {
  const parts: string[] = []
  parts.push(r?.doctorName || '未定医生')
  parts.push(statusOf(REGIST_TYPE, r?.registType).label)
  return parts.join(' · ')
}

/**
 * 第三行：结算方式。自费是默认值不写（每张卡都刷一行「自费」等于没信息）。
 * 就诊号在第一行年龄后面，所以本行只剩非自费的结算信息。
 */
const deskCardMetaLine = (r: any) => {
  if (Number(r?.settlementType) !== 1) return settlementLabel(r?.settlementType)
  return ''
}

/**
 * 卡片悬停提示：动作说明 + 完整患者信息（卡片上被截断的在这里兜底）+ 电话 + 挂号单号。
 * 电话放这里而不是占卡片一行 —— 改约/退号前要联系患者时才需要，平时不用看。
 */
const deskCardTitle = (r: any) => {
  const action = canReschedule(r) ? '点击改约（换号源）' : (sourceLockReason(r) || '当前状态不可改约')
  const meta = deskPatientMeta(r)
  const doctor = deskCardDoctorLine(r)
  const visitNo = deskCardVisitNo(r)
  const settlement = deskCardMetaLine(r)
  const info = [meta, visitNo, doctor, settlement].filter(Boolean).join(' · ')
  const extra = [info, r?.phone ? `电话 ${r.phone}` : '', r?.registNo ? `就诊号 ${r.registNo}` : '']
    .filter(Boolean)
    .join('　')
  return extra ? `${action}\n${extra}` : action
}

/**
 * 点卡片：能改约的进改约弹窗；已锁死的状态给一句人话。
 * 卡片统一 cursor-pointer —— 点了有反馈才配得上手型（点了没反应最让人困惑）。
 */
const handleDeskCardClick = (r: any) => {
  if (canReschedule(r)) {
    handleReschedule(r)
    return
  }
  ElMessage.warning(sourceLockReason(r) || '当前状态不允许变更号源')
}

/** 点患者卡：改约（换号源，后端原子释放旧号/扣新号并同步就诊日期与医生） */
const handleReschedule = (r: any) => {
  // 双保险：即使某处漏改了模板的 @click，也不让已就诊/就诊中的卡进到改约弹窗
  if (!canReschedule(r)) {
    ElMessage.warning(sourceLockReason(r) || '当前状态不允许变更号源')
    return
  }
  rescheduleForm.value = {
    registId: r.id,
    patientName: r.patientName,
    deptId: r.deptId,
    visitDate: r.visitDate,
    scheduleId: null,
    slotId: null,
  }
  rescheduleSchedules.value = []
  rescheduleSlots.value = []
  showRescheduleDialog.value = true
  loadRescheduleSchedules()
}

const loadRescheduleSchedules = async () => {
  if (!rescheduleForm.value.deptId || !rescheduleForm.value.visitDate) {
    rescheduleSchedules.value = []
    return
  }
  rescheduleLoading.value = true
  try {
    const res = await getAvailableSchedule(rescheduleForm.value.deptId, rescheduleForm.value.visitDate)
    // 改约只列还有余号的班次
    rescheduleSchedules.value = (res.data || []).filter((s: any) => s.availableSource > 0)
  } catch (error) {
    console.error('加载改约号源失败:', error)
  } finally {
    rescheduleLoading.value = false
  }
}

const handleConfirmReschedule = async () => {
  if (!rescheduleForm.value.scheduleId) {
    ElMessage.warning('请选择新号源')
    return
  }
  rescheduleLoading.value = true
  try {
    await updateRegistration({
      id: rescheduleForm.value.registId,
      scheduleId: rescheduleForm.value.scheduleId,
      slotId: rescheduleForm.value.slotId || undefined,
    } as any)
    showRescheduleDialog.value = false
    ElMessage.success('改约成功')
    loadDesk()
  } catch (error: any) {
    ElMessage.error(error.message || '改约失败')
  } finally {
    rescheduleLoading.value = false
  }
}

const handleDeskCancel = async (r: any) => {
  // 双保险：即使某处漏改了模板的 v-if，也不让不可退的卡走到接口
  if (!canCancelRegist(r)) {
    ElMessage.warning(sourceLockReason(r) || '当前状态不允许退号')
    return
  }
  try {
    await ElMessageBox.confirm(`确认为 ${r.patientName} 办理退号？`, '退号确认', {
      confirmButtonText: '退号', cancelButtonText: '取消', type: 'warning',
    })
  } catch { return }
  try {
    await cancelRegistration(r.id, '看板退号')
    ElMessage.success('退号成功')
    loadDesk()
  } catch (error: any) {
    ElMessage.error(error.message || '退号失败')
  }
}

const handleTabChange = (tab: any) => {
  // 首次切到预约看板时加载本周数据（号源面板已并入周视图，不再单独取数）
  if (tab === 'desk' && !deskDays.value.length) {
    loadDesk()
    loadCalServiceDays()
  }
}

/**
 * 周视图格子里的排班 —— 原「号源面板」的取数就长在这上面。
 *
 * `all` = 该格全部排班；`visible` = 应用「隐藏已满/停诊」后的可见排班。
 * 两者刻意分开：开关只影响**显示**，不能让人误以为「已满的班次不存在」
 * （给患者改推荐时间时，知道"上午满了"和"上午没班"是两回事）。
 */
const cellAll = (date: string, doctorId: any) =>
  weekCellSchedules(date, doctorId)

const cellSchedules = (date: string, doctorId: any) => {
  const all = cellAll(date, doctorId)
  if (!deskHideFull.value) return all
  return all.filter(s => Number(s.availableSource) > 0)
}

/** 该格被开关藏掉了几个班次（用于「已满/停诊已隐藏 N」提示，避免静默消失） */
const cellHiddenCount = (date: string, doctorId: any) =>
  cellAll(date, doctorId).length - cellSchedules(date, doctorId).length

/**
 * 当前视图被藏掉的班次总数：开关打开时给一句总账，别让人以为号源凭空少了。
 *
 * 日视图也要有这个总账（原来只算了周视图）—— 否则在日视图打开开关，
 * 某个医生整个半天凭空消失，没有任何提示。
 */
const deskHiddenTotal = computed(() =>
  (deskColumns.value || []).reduce((sum: number, col: any) =>
    sum + deskRows.value.reduce((s: number, row: any) => s + cellHiddenCountFor(col, row.key), 0), 0))

/**
 * 周视图格子的明细**只由「余号」按钮触发**（2026-09-22）。
 *
 * 原实现的单击=弹明细有个致命副作用：周视图一行一位医生，看板是"横着扫"的，
 * 任何一次误点（甚至只是想选中/瞄一眼）都会弹出一层模态 —— 而在挂号窗口这种
 * 手速场景下，弹框要比"多按一下"贵得多。可点的事情明确放在按钮上，误触成本就消失了。
 *
 * 双击仍然是开单：那是挂号员的肌肉记忆（点两下 = 给我挂上），与"按钮开明细"不冲突。
 */
const openCellDialog = (date: string, doctorId: any) => {
  if (!cellSchedules(date, doctorId).length) return
  const rowHit = weekDoctorRows.value.find(r => String(r.doctorId) === String(doctorId))
  selectedCell.value = {date, doctorId, doctorName: rowHit?.label || ''}
  showCellDialog.value = true
}

/**
 * 格子双击 = 开单 —— **只在日视图**（2026-09-22）。
 *
 * 周视图一格 = 一位医生一整天，双击落点不是"某个号源"而是"这一整天"，
 * 开出来的单子还得再挑一次号源；而且周视图是**横着扫**的（一行一位医生、一列一天），
 * 扫的过程中误触一次双击就弹开单窗，比"多按一下"贵得多。
 * 周视图要开单走格子里那个「预约」按钮 —— 意图明确、一次点击，不用赌手速。
 */
const handleDeskCellDblclick = (col: any, rowKey: any) => {
  if (deskViewMode.value !== 'day') return
  addFromCell(col, rowKey)
}

/**
 * 按**列**取格子排班 —— 日视图列 = 医生、周视图列 = 日期，两种格子的定位维度不同。
 *
 * 上面那几个 `(date, doctorId)` 版函数是周视图专用（周视图列就是日期、行是医生）；
 * 双击开单要在两个视图都工作，所以这里再包一层按 `col` 定位的版本，
 * 让调用方只关心"这一格"，不关心当前是哪个视图。
 *
 * `all` = 全部排班，`visible` = 应用「隐藏已满/停诊」后的可见排班（与周视图口径一致）。
 */
const cellAllFor = (col: any, rowKey: any) =>
  deskViewMode.value === 'day'
    ? daySlotCellSchedules(col.doctorId, String(rowKey))
    : cellAll(col.date, rowKey)

const cellSchedulesFor = (col: any, rowKey: any) => {
  const all = cellAllFor(col, rowKey)
  if (!deskHideFull.value) return all
  // 日视图的「满」是**段级**的：这一档没号就算满 —— 同一位医生 10 点那档还有号，
  // 不代表 8 点这档还有号，用主表 available>0 判断会把满的档也放出来。
  if (deskViewMode.value === 'day') {
    return all.filter((s: any) => deskAvail(daySlotSegments([s], String(rowKey))) > 0)
  }
  return all.filter(s => Number(s.availableSource) > 0)
}

const cellHiddenCountFor = (col: any, rowKey: any) =>
  cellAllFor(col, rowKey).length - cellSchedulesFor(col, rowKey).length

/**
 * 日历下方的「当天概况」：医生数 / 总号源 / 已挂号 / 剩余号源。
 *
 * 数据全部来自**已加载的** `deskSchedules` + `deskRegs`，不新增任何接口 ——
 * 这些数字本来就画在每个医生的格子里（"已挂 2 / 总 50 · 余 47"），
 * 只是要点一遍每格才能加总。日历旁边给一份合计，回答的是"这天忙不忙、还能挂几个"。
 *
 * ⚠️ 口径与格子严格一致：
 *   · 只用当前窗口那天（日视图 = `deskDay`），不跨天
 *   · 「医生数」按 `doctorId` 去重（同一医生上午下午两个班次算 1 位）
 *   · 「已挂号」沿用 `deskRegsOfSchedules`（已退号不计数），与格子里的"已挂 N"同源
 *   · 「剩余号源」= `availableSource` 合计，与格子里的"余 N"同源
 *   不在这块里另算一套口径 —— 否则日历说 50、格子加起来是 47，就是"同一屏两个数字打架"。
 *
 * ⚠️ 这块**不显示"隐藏已满"开关的影响**：开关是"看什么"，概况是"实际有多少"。
 * 开关开着时格子少了几个班次，但这天的容量事实没变（已满也是这天的号源），
 * 所以概况始终按 `deskSchedules` 全量算 —— 与 `deskHiddenTotal` 那句"已隐藏 N 个"配套看。
 */
const deskDayStat = computed(() => {
  const date = deskDay.value
  const dayScheds = deskSchedules.value.filter(s => s.scheduleDate === date)
  const doctors = new Set(dayScheds.map(s => s.doctorId).filter(Boolean)).size
  const total = dayScheds.reduce((sum, s) => sum + (Number(s.totalSource) || 0), 0)
  const avail = dayScheds.reduce((sum, s) => sum + (Number(s.availableSource) || 0), 0)
  const regs = deskRegsOfSchedules(date, dayScheds).length
  // 已用占比 = 已挂号 / 总号源（不是 1 - 余号/总数：两者在有"停用号源"时会不等，
  // 挂号员关心的是"发出去了多少号"，用实际挂号数更直白）
  const usedPct = total > 0 ? Math.min(100, Math.round((regs / total) * 100)) : 0
  return {doctors, total, avail, regs, usedPct}
})

/**
 * 从面板点「新增预约」进来时，号源怎么带出来。
 *
 * 默认 = 按面板当前的「科室 + 日期」**重新走接口**问一次最新号源，而不是沿用面板缓存：
 * 号源面板/看板是上一刻的快照，余号随时被别人抢走 —— 照缓存预填出「余号 3」，点确认时才报号源已满，
 * 是挂号窗口最难受的一类失败。
 *
 * 唯一例外：面板上明确点了某一位医生的号源（`preferScheduleId`），那本来就该挂这一条，
 * 仍按 snapshot 提示「这一条可能已变更」，但不用走接口猜他的号还在不在。
 */
type AddSeed = {
  deptId?: any
  visitDate?: string
  scheduleId?: any
  doctorId?: any
  // 时间片段ID（日视图点某一格进来时带上：这一格就是这一档，别让人再选一遍）
  slotId?: any
  snapshot?: {scheduleDate: string; list: any[]}
}

/** 号源被抢空 / 停诊后的兜底文案；返回 true 表示「该把号源下拉重置掉」 */
const warnSeedScheduleStale = (seedScheduleId: any) => {
  const exist = availableSchedules.value.find((x: any) => x.id === seedScheduleId)
  if (exist) return false
  newAppointment.value.scheduleId = null
  ElMessage.warning('该号源已变更或约满，请重新选择号源')
  return true
}

const fetchSchedulesForSeed = async (seed: AddSeed) => {
  const visitDate = seed.visitDate
  // 科室可能是哨兵「全部科室」——挂号单必须落到一个具体科室（后端 @NotNull），
  // 所以这种情况**不预填**，把选择权留给人，别猜。
  const deptId = isAllDept(seed.deptId) ? null : seed.deptId
  if (!deptId || !visitDate) return
  const snapshot = seed.snapshot
  const scheduleId = seed.scheduleId
  if (snapshot?.scheduleDate === visitDate) {
    // 面板缓存里这一天的号源：「选中一条」靠它定位，下拉本身还是走接口要最新的
    const cached = snapshot.list || []
    if (!cached.length) {
      ElMessage.warning('该科室该日期无可挂号源')
      return
    }
    // ⚠️ 必须 await：段列表要跟着号源走（先定号源再选段），不 await 的话
    //    调用方拿到的还是空号源列表，段也就选不上（表现为"点 9 点那格，弹窗里时间段是空的"）
    await loadAvailableSchedules()
    warnSeedScheduleStale(scheduleId)
    return
  }
  await loadAvailableSchedules()
}

/**
 * 挂号弹窗的统一预填入口。
 *
 * 调用方有两个：看板格子的「预约」按钮 / 格子双击（addFromCell，带整格上下文），
 * 周视图号源明细的「去挂号」（goRegister，直接指定某一条号源）。
 * 原先的工具栏按钮 / 医生列头加号 / 明细面板按钮三处入口已按需求收口掉。
 *
 * 三条硬要求：
 *   ① 必须**带上当前上下文**（科室 + 日期）预填 —— 不然等于开一个空单，还得把科室日期重选一遍；
 *   ② 赋值必须在 `showAddDialog = true` **之前**：弹窗是 destroy-on-close + @closed 清表单，
 *      反过来的话这次赋值会被上一轮的 resetForm 冲掉；
 *   ③ 「全部科室」这种没有具体科室可带的情况，直接开空单不预填，不猜。
 */
const openAddFromPanel = async (seed: AddSeed = {}) => {
  resetForm()
  newAppointment.value.deptId = isAllDept(seed.deptId) ? null : (seed.deptId ?? null)
  newAppointment.value.visitDate = seed.visitDate || ''
  newAppointment.value.doctorId = seed.doctorId ?? null
  showAddDialog.value = true
  await fetchSchedulesForSeed(seed)
  // 医生列点了「新增」但没指定具体号源：把这位医生的号预先选上（当天多条排班则留给用户挑）
  if (!newAppointment.value.scheduleId && newAppointment.value.doctorId) {
    const hit = availableSchedules.value.filter((s: any) => s.doctorId === newAppointment.value.doctorId)
    if (hit.length === 1) newAppointment.value.scheduleId = hit[0].id
  }
  // 面板点的是某一格（日视图 = 某半小时档）：段列表跟着号源走，必须先定号源再选段，顺序不能反。
  // 段可能已被排班重建删掉 → 校验它还在新列表里，不在就不预选（让人自己挑，不静默挂到别的档）。
  if (seed.slotId && newAppointment.value.scheduleId) {
    await loadSlotRows(newAppointment.value.scheduleId)
    if (slotRows.value.some((s: any) => String(s.id) === String(seed.slotId))) {
      newAppointment.value.slotId = seed.slotId
    }
  }
}

/**
 * 给模板用的薄包装：把 async 函数变成"返回 undefined"。
 *
 * 直接 `@click="openAddFromPanel(...)"` 会把 Promise 当返回值交给 Vue —— 事件返回值只用于
 * 阻止默认行为，Promise 恒为真值，语义上是错的（虽然目前不炸）。包装一层，行为明确。
 */
const addFromPanel = (seed: AddSeed = {}) => {
  void openAddFromPanel(seed)
}

const goRegister = (s: any) => {
  addFromPanel({
    deptId: s.deptId,
    visitDate: s.scheduleDate,
    scheduleId: s.id,
    // 号源面板已并入周视图，两者现在共用 deskSchedules 一份数据
    snapshot: {scheduleDate: s.scheduleDate, list: deskSchedules.value.filter(x => x.scheduleDate === s.scheduleDate)},
  })
}

/**
 * 格子上的开单入口（两个触发姿势共用这一个函数）：
 *   · 点格子里的「预约」按钮（有排班的格子才有这个按钮）
 *   · 双击格子空白处（真实 HIS 挂号工作台的核心交互）
 *
 * 双击与周视图单击的分工（两者语义不同，不冲突）：周视图单击格子 = 弹该格的
 * 「医生号源明细」（回答"还有谁能挂"），双击/点按钮 = 直接开单（回答"给我挂上"）。
 * 所以这里只开单，不置「选中的格子」；双击到达时那边的单击定时器已被掐掉（见 handleDeskCellDblclick）。
 *
 * 科室取该格排班自己的科室，**不用工具栏的 deskDeptId**：工具栏选「全部科室」（= `__ALL__`）时，
 * 同一格会混着多个科室的排班，而挂号单必须落到一个具体科室 —— 用哨兵预填会让
 * `loadAvailableSchedules` 因缺科室直接 return，号源下拉 0 项且不报错（实测 3 条接口数据 / 0 个下拉项，
 * "看起来开了弹窗，其实挂了空单"）。格子里的排班自带 `deptId`，用它才是唯一无歧义的来源。
 *
 * 未排班 / 全被隐藏：不开窗（没有排班就没有号源，开出来是个空单，用户还得自己关掉）。
 * 给一次轻提示说明原因 —— 静默无反应会让人以为页面卡了。
 */
const addFromCell = (col: any, rowKey: any) => {
  const cellScheds = cellSchedulesFor(col, rowKey)
  if (!cellScheds.length) {
    // 分清两种"没号源"：本来就没排班 vs 被「隐藏已满/停诊」开关藏掉了
    const hidden = cellHiddenCountFor(col, rowKey)
    ElMessage.info(hidden > 0
      ? `该时段 ${hidden} 个班次已满/停诊（当前被「隐藏已满/停诊」开关隐藏），关掉开关可查看并开单`
      : '该时段没有排班，无法新增预约')
    return
  }
  // 日视图这一格 = 某医生的某半小时档 → 把这一档直接带上，挂号单不必再选一遍时间段
  let slotId: any
  if (deskViewMode.value === 'day') {
    const ids = new Set(cellScheds.map((s: any) => String(s.id)))
    const segs = deskSlots.value.filter((s: any) =>
      ids.has(String(s.scheduleId)) && String(s.startTime).slice(0, 5) === String(rowKey))
    if (segs.length === 1) slotId = segs[0].id
  }
  // 多个号源（同医生多诊室 / 同格多医生）→ 不猜第一条，把 scheduleId 留空让人在下拉里选
  const deptId = cellScheds[0]?.deptId ?? col.deptId ?? deskDeptId.value
  const snapshotDate = deskViewMode.value === 'day' ? deskDay.value : col.date
  const onlyOne = cellScheds.length === 1
  addFromPanel({
    deptId,
    visitDate: snapshotDate,
    scheduleId: onlyOne ? cellScheds[0].id : undefined,
    // 周视图的行就是这位医生、日视图的列也是这位医生 —— 两种视图都能把医生带上，
    // 号源下拉直接落到他身上，不用在下拉里再找一遍
    doctorId: deskViewMode.value === 'day' ? col.doctorId : rowKey,
    slotId,
    snapshot: {scheduleDate: snapshotDate, list: cellScheds},
  })
}

onMounted(async () => {
  // 列表 + 统计：2 次请求（原来是 listPage 7 次：1 次列表 + 6 次「读 total」）
  reloadListAndStats()
  loadMedicalInsuranceTypes()
  loadRevisitSourceDict()
  // 顺序有讲究：科室列表先到（默认科室要命中下拉里真实存在的 option），再取当前用户科室，最后才拉看板
  await loadDepartments()
  await loadDefaultDept()
  // 默认 tab 是「预约看板」，但 @tab-change 只在用户点击时触发 → 首屏必须自己拉一次
  if (activeTab.value === 'desk') {
    loadDesk()
    // 月历圆点：与主区并行拉，不 await（它是导航装饰，晚 200ms 到不影响任何操作）
    loadCalServiceDays()
  }
  // 表格撑满窗口：首屏 DOM 出来之后才量得到位置，所以放在 nextTick 里
  await nextTick()
  calcDeskHeight()
  window.addEventListener('resize', onWinResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onWinResize)
})
</script>

<template>
  <div class="space-y-6">
    <!-- 六卡口径 = 就诊日当天（统计查询只带 visitDate，不带挂号时间区间），
         数值真值来自 /appoint/statusCount，验证脚本按 testid 取页面值与接口对账 -->
    <div class="grid grid-cols-3 gap-4 sm:grid-cols-6" data-testid="status-cards">
      <div v-for="item in [
        { label: '总挂号', value: statusCounts.total, color: 'text-slate-700' },
        { label: '待就诊', value: statusCounts.waiting, color: 'text-blue-600' },
        { label: '已签到', value: statusCounts.checkedIn, color: 'text-cyan-600' },
        { label: '已完成', value: statusCounts.completed, color: 'text-emerald-600' },
        { label: '已退号', value: statusCounts.cancelled, color: 'text-red-500' },
        { label: '已过号', value: statusCounts.overdue, color: 'text-orange-500' },
      ]" :key="item.label" :data-testid="'status-card-' + item.label"
         class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-center shadow-sm">
        <p :class="['text-xl font-bold leading-6', item.color]">{{ item.value }}</p>
        <p class="text-xs text-slate-500">{{ item.label }}</p>
      </div>
    </div>
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="预约看板" name="desk" lazy>
        <!-- 日历已从「页面级左 rail」移入**日视图内部**（原 rail 200px、格子 24px 高，太小点不准）。
             日视图 = 左日历 + 右「班次 × 医生」；周视图本身是一周三列，不再挂日历。 -->
        <div class="desk-board rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div class="flex flex-wrap items-center gap-3">
            <!-- 日 / 周 视图切换：真实 HIS 挂号工作台的做法（同一张台子切视图），不是两个菜单 -->
            <el-radio-group v-model="deskViewMode" @change="handleDeskViewChange">
              <el-radio-button value="day">日视图</el-radio-button>
              <el-radio-button value="week">周视图</el-radio-button>
            </el-radio-group>
            <span class="text-sm font-medium text-slate-700">科室</span>
            <el-select v-model="deskDeptId" data-testid="desk-dept-select" placeholder="全部科室" filterable
                       class="!w-44" @change="handleDeskDeptChange">
              <!-- 「全部科室」必须是**下拉里的一项**：placeholder 只在没值时显示，
                   用户展开下拉是找不到「全部」的（原来靠 clearable 的 ✕ 表达"全部"，
                   选完还看不到"我现在看的是全部"。显式选项 + 常驻不清理才说得清） -->
              <el-option label="全部科室" :value="ALL_DEPT"/>
              <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id ?? d.deptName"/>
            </el-select>
            <span class="text-sm font-medium text-slate-700">医生</span>
            <el-select v-model="deskDoctorId" data-testid="desk-doctor-select" placeholder="全部医生" clearable filterable
                       class="!w-40" @change="handleDeskDoctorChange">
              <!-- 专家标识也挂到下拉里：挑医生是按名册挑的，号别得在挑的时候就能看见
                   （label 仍是纯姓名 —— 那是搜索比对用的，掺进标识会让人搜「专家」搜不到人） -->
              <el-option v-for="doc in deskDoctorOptions" :key="doc.id" :label="doc.doctorName" :value="doc.id">
                <span>{{ doc.doctorName }}</span>
                <span v-if="isExpertDoctor(doc.id)"
                      class="ml-1 rounded bg-amber-50 px-1 text-[10px] font-medium text-amber-600 ring-1 ring-amber-200">专家</span>
              </el-option>
            </el-select>
            <!-- 2026-09-22：周视图的「隐藏已满/停诊」开关整个去掉。
                 周视图一格是一位医生一整天，藏着已满是"这位医生今天到底出不出诊"说不清，
                 而日视图（半小时档）里满档很多、藏起来才有用 —— 开关只留在日视图左栏。
                 ⚠️ 去掉开关 = 周视图永远看全量，切到周视图时 handleDeskViewChange 会复位 deskHideFull，
                    否则从日视图带着勾选切过来会静默少掉几个班次还没处关。 -->
            <!-- 日期导航：**只留周视图**。
                 日视图的日期由左侧日历负责（点哪天加载哪天），工具栏再显示一遍"2026-09-21 周一"
                 等于同一屏两个地方说同一个事实，而且"前一天/后一天"还得跟日历抢着改同一天 →
                 日视图下这三个按钮整个不渲染，只留周视图的「上一周/下一周/回到今天」。 -->
            <!-- 翻周用 ‹ › 图标按钮：文字按钮一排四个把工具栏撑得很宽，
                 而"上一周/下一周"的语义靠箭头本身就够（与左侧月历的 ‹ › 同一个视觉语言） -->
            <div v-if="deskViewMode === 'week'" class="flex items-center gap-2">
              <el-button :icon="ArrowLeft" title="上一周" @click="handleDeskStep(-1)"/>
              <span data-testid="desk-range" class="min-w-[210px] text-center text-base font-medium text-slate-600">{{ deskRangeLabel }}</span>
              <el-button :icon="ArrowRight" title="下一周" @click="handleDeskStep(1)"/>
              <el-button @click="handleDeskToday">回到本周</el-button>
            </div>
            <!-- 「新增预约」放在筛选栏最右侧：看板就是挂号员的主工作台，
                 开单入口必须在同一屏够得着（不用先切到「挂号记录」tab 才能开单）。
                 预填当前看板的科室 + 日期（日视图=当前这天 / 周视图=本周第一天），
                 开出来不是空单 —— 科室、日期不必再选一遍。 -->
            <!-- 「新增预约」放在筛选栏最右侧：看板就是挂号员的主工作台，
                 开单入口必须在同一屏够得着（不用先切到「挂号记录」tab 才能开单）。
                 预填当前看板的科室 + 日期（日视图=当前这天 / 周视图=本周第一天），
                 开出来不是空单 —— 科室、日期不必再选一遍。

                 「刷新」放在它左边，两个视图都要有：余号是会被别人随时抢走的，
                 板上的数字是上一刻的快照 —— 窗口收下一位患者之前必须先重看一眼，
                 否则照着老数字推荐号源，点确认时报「号源已满」是最难受的一类失败。
                 只重载看板本体（排班 + 挂号 + 段 + 医生名册/月历圆点），不动页面上方的统计卡
                 （那些是"挂号记录"tab 的口径，跟看板不是同一张表，一起刷反而更慢也更乱）。 -->
            <div class="ml-auto flex items-center gap-2">
              <el-button data-testid="desk-refresh" :icon="Refresh" :loading="deskLoading"
                         @click="handleDeskRefresh">刷新</el-button>
              <el-button v-perm="'opd:appointments:add'" type="primary" :icon="Plus" data-testid="desk-add-appoint"
                         @click="handleDeskAdd">新增预约</el-button>
            </div>
          </div>
          <!-- 2026-09-22：「隐藏已满/停诊」从顶部工具栏挪到看板**下方**（紧贴表格）——
               它是看板的显示开关，跟着表走比挂在筛选行里更近；「隐藏 N 个」的总账提示随行。 -->
          <!-- 日视图 = 左日历 + 右「班次 × 医生」。日历只属于日视图（周视图本身就是一周七列，
               再放一个月份日历两边都在说"日期"，反而互相打岔），所以用 v-if 挂在日视图里。 -->
          <!-- items-stretch：左栏拉满与右边表格同样的高度（2026-09-22 需求），
               不再按内容自适应——内容不足时下方留白，整块边框对齐看板。 -->
          <div class="mt-3 flex items-stretch gap-4">
            <!-- 日历：日视图的日期导航。收小到 220px —— 300px 抢了医生表的宽度，
                 而日历只回答"哪天"，格子 32px 够点。 -->
            <!-- 日历列同样按 deskMaxH 封顶：它比右侧时间表还高时（实测高 109px），
                 撑高的是整个 flex 行 → 主区溢出多出一条滚动条。封顶 + 内部滚动，
                 高度决策只有 deskMaxH 一个来源，不会出现左右各算一套。 -->
            <aside v-if="deskViewMode === 'day'" data-testid="desk-cal"
                   :style="{maxHeight: deskMaxH + 'px'}"
                   class="flex w-[220px] shrink-0 flex-col overflow-y-auto rounded-lg border border-slate-200 bg-slate-50/50 p-2.5">
              <div class="mb-1.5 flex items-center justify-between">
                <button data-testid="cal-prev"
                        class="rounded border border-slate-200 bg-white px-1.5 text-base leading-6 text-slate-600 hover:bg-slate-100"
                        title="上一个月" @click="handleCalShift(-1)">&lsaquo;</button>
                <span data-testid="cal-month" class="text-sm font-semibold text-slate-700">{{ calMonthLabel }}</span>
                <button data-testid="cal-next"
                        class="rounded border border-slate-200 bg-white px-1.5 text-base leading-6 text-slate-600 hover:bg-slate-100"
                        title="下一个月" @click="handleCalShift(1)">&rsaquo;</button>
              </div>
              <!-- 当前看的是哪天：日期导航从工具栏挪进日历后，这里必须有明确回显，
                   否则"我今天看的是 21 号"只能靠格子里那点浅蓝去猜（今天/选中两个高亮要分清）。 -->
              <p data-testid="cal-picked" class="mb-1.5 rounded bg-blue-50 px-2 py-1 text-center text-xs font-medium text-blue-700">
                {{ deskDay }} {{ weekdayMap[new Date(deskDay + 'T00:00:00').getDay()] }}
                <span v-if="deskDay === fmtDate(new Date())" class="ml-1 text-[10px] text-blue-400">今天</span>
              </p>
              <div class="grid grid-cols-7 gap-1 text-center">
                <span v-for="w in ['一','二','三','四','五','六','日']" :key="'w' + w"
                      class="py-0.5 text-[11px] font-medium text-slate-400">{{ w }}</span>
              </div>
              <div v-loading="calLoading" class="mt-0.5 grid grid-cols-7 gap-1">
                <button v-for="c in calCells" :key="c.date"
                        data-testid="cal-cell" :data-date="c.date"
                        :data-in-month="c.inMonth ? '1' : '0'"
                        :data-has-service="c.hasService ? '1' : '0'"
                        :title="c.hasService ? `${c.date} 有排班` : c.date"
                        class="relative flex h-8 w-full items-center justify-center rounded text-sm transition-colors"
                        :class="c.date === deskDay
                          ? 'bg-blue-600 font-semibold text-white shadow-sm hover:bg-blue-700'
                          : (!c.inMonth
                            ? 'text-slate-300 hover:bg-slate-50'
                            : (c.isToday
                              ? 'bg-white font-semibold text-blue-600 ring-1 ring-inset ring-blue-300 hover:bg-blue-50'
                              : 'bg-white text-slate-700 hover:bg-slate-100'))"
                        @click="handleCalPick(c.date)">
                  {{ c.day }}
                  <!-- 今天只标一个「今」字（蓝字 + 细蓝框），不再用实心蓝底：
                       实心底是「我选中了哪天」的信号，被"今天"占掉之后，选中态反而只剩一层浅蓝底，
                       两个状态抢同一个视觉通道 → 看不出当前看的是哪天 -->
                  <span v-if="c.isToday"
                        class="absolute right-0.5 top-0 text-[9px] font-normal leading-none"
                        :class="c.date === deskDay ? 'text-blue-100' : 'text-blue-500'">今</span>
                  <!-- 有排班的日子加圆点（今天也加：底色不冲突了，两个信号各说一件事） -->
                  <span v-if="c.hasService"
                        data-testid="cal-dot"
                        class="absolute bottom-0.5 left-1/2 h-1 w-1 -translate-x-1/2 rounded-full"
                        :class="c.date === deskDay ? 'bg-white' : 'bg-emerald-500'"></span>
                </button>
              </div>
              <div class="mt-1 flex items-center justify-between text-[11px] text-slate-400">
                <span class="flex items-center gap-1">
                  <span class="h-1 w-1 rounded-full bg-emerald-500"></span> 有排班
                </span>
                <button data-testid="cal-today" class="text-blue-600 hover:underline"
                        @click="handleCalToday(); handleCalPick(fmtDate(new Date()))">回今天</button>
              </div>

              <!-- ===== 日历下方：当天容量概况 + 我的科室 =====
                   为什么放"当天容量"：日历回答"哪天"，但挂号员接着要问的是"这天忙不忙、还能挂几个"。
                   这数据格子本身就有（已挂数 / 余号就画在每个医生的格子里），但要点一遍每格才知道总数。
                   这里直接用**已加载的 deskSchedules + deskRegs** 汇总，不新增任何接口。 -->
              <div class="my-2 border-t border-slate-200"></div>
              <p class="mb-1.5 text-[11px] text-slate-400">{{ deskDay }} 概况</p>
              <!-- 2×2 排布：四个数字竖着排要 130px，日历列因此高过右边的表（多出来的部分
                   会把整个 flex 行撑高 → 主区溢出一条滚动条）。改成两行两列省一半高度，
                   数值仍然 percentile 一个不少。 -->
              <div class="grid grid-cols-2 gap-1.5">
                <div class="flex items-baseline justify-between rounded bg-white px-2 py-1">
                  <span class="text-[11px] text-slate-500">医生</span>
                  <span data-testid="cal-stat-doctors" class="text-sm font-semibold text-slate-700">
                    {{ deskDayStat.doctors }}<span class="text-xs font-normal text-slate-400"> 位</span>
                  </span>
                </div>
                <div class="flex items-baseline justify-between rounded bg-white px-2 py-1">
                  <span class="text-[11px] text-slate-500">总号源</span>
                  <span data-testid="cal-stat-total" class="text-sm font-semibold text-slate-700">
                    {{ deskDayStat.total }}<span class="text-xs font-normal text-slate-400"> 个</span>
                  </span>
                </div>
                <div class="flex items-baseline justify-between rounded bg-white px-2 py-1">
                  <span class="text-[11px] text-slate-500">已挂号</span>
                  <span data-testid="cal-stat-regs" class="text-sm font-semibold text-blue-600">
                    {{ deskDayStat.regs }}<span class="text-xs font-normal text-slate-400"> 人</span>
                  </span>
                </div>
                <div class="flex items-baseline justify-between rounded bg-white px-2 py-1">
                  <span class="text-[11px] text-slate-500">剩余</span>
                  <span data-testid="cal-stat-avail" class="text-sm font-semibold"
                        :class="deskDayStat.avail > 0 ? 'text-emerald-600' : 'text-slate-400'">
                    {{ deskDayStat.avail }}<span class="text-xs font-normal text-slate-400"> 个</span>
                  </span>
                </div>
              </div>
              <!-- 剩余号源的占比条：一句话说不清的"快满了"用视觉表达 -->
              <div class="mt-1.5 h-1.5 w-full overflow-hidden rounded-full bg-slate-200">
                <div class="h-full rounded-full transition-all"
                     :class="deskDayStat.usedPct >= 90 ? 'bg-rose-400' : (deskDayStat.usedPct >= 70 ? 'bg-amber-400' : 'bg-emerald-500')"
                     :style="{width: deskDayStat.usedPct + '%'}"></div>
              </div>
              <p class="text-center text-[11px] text-slate-400">已用 {{ deskDayStat.usedPct }}%</p>

              <!-- 「隐藏已满/停诊」开关放在**概况下方**（自顶部工具栏挪来）：
                   它管的是"这天看得到哪些号"，跟同一天的容量概况是一件事，
                   比挂在筛选行里更靠近它影响的数字；藏掉 N 个的总账提示随行。
                   2026-09-22：原「我的科室」快捷块整块移除（按钮 + 标题 + 提示语），
                   切科室统一走上方「科室」下拉。 -->
              <div class="mt-2 border-t border-slate-200 pt-2">
                <el-checkbox v-model="deskHideFull" data-testid="desk-hide-full">隐藏已满/停诊</el-checkbox>
                <p v-if="deskHideFull && deskHiddenTotal > 0" data-testid="desk-hidden-tip"
                   class="mt-1 text-[11px] leading-4 text-amber-600">
                  已隐藏 {{ deskHiddenTotal }} 个已满/停诊班次（关掉开关即可看到）
                </p>
              </div>
            </aside>

            <!-- 右：班次 × 医生 / 班次 × 日期 表 -->
            <div class="min-w-0 flex-1">
          <!-- 高度按视口算（撑满窗口）：日视图 18 行起步，写死高度等于强迫人一直滚 -->
          <div ref="deskScrollEl" v-loading="deskLoading" data-testid="desk-scroll"
               class="overflow-auto rounded border border-slate-200"
               :style="{height: deskMaxH + 'px'}">
            <!-- 空态要说清「是没排班还是筛掉了」：默认落在我的科室，门诊部/管理员选「全部科室」即可看全院 -->
            <div v-if="!deskLoading && !deskColumns.length" data-testid="desk-empty"
                 class="flex flex-col items-center justify-center gap-1 py-12 text-center">
              <p class="text-base text-slate-500">
                {{ isAllDept(deskDeptId) ? '当前时间窗口内没有任何排班（已含全部科室）' : '本科室当前时间窗口内没有排班' }}
              </p>
              <p class="text-sm text-slate-400">
                看板默认只显示「我的科室」，把上方「科室」选成「全部科室」即可查看全院；也可以把日期翻到有排班的那天。
              </p>
            </div>
            <table v-else class="w-full min-w-[960px] border-collapse text-sm">
              <thead class="sticky top-0 z-20">
                <tr>
                  <!-- 行首列头跟着行变（日视图=时间段 / 周视图=班次）：
                       表头写「班次」而行里是 08:00~08:30，读的人得自己猜这两者的关系 -->
                  <!-- 日视图的网格线要看得见（slate-300 而不是周视图的 slate-100）：
                       半小时一档的行多且密，线太淡时整张表糊成一片，扫不出哪一档有号 -->
                  <!-- 行首列头跟着行变（日视图=时间段 / 周视图=医生）：
                       表头写「班次」而行里是医生名，读的人得自己猜这两者的关系。
                       周视图列宽放大到 150px：行首要容得下「医生名 + 本周已挂/剩余」两行 -->
                  <th class="sticky left-0 z-30 border bg-slate-50 p-2 text-center font-medium text-slate-500"
                      :class="[deskViewMode === 'day' ? 'w-[96px] border-slate-300' : 'w-[150px] border-slate-100']">
                    {{ deskViewMode === 'day' ? '时间段' : '医生' }}
                  </th>
                  <th v-for="col in deskColumns" :key="col.key"
                      class="border bg-slate-50 p-2 text-center align-top"
                      :class="deskViewMode === 'day' ? 'border-slate-300' : 'border-slate-100'">
                    <!-- 周视图：日期列头可点，下钻到当天日视图。可点性必须在静态态可见，不能只靠 hover tooltip -->
                    <template v-if="deskViewMode === 'week'">
                      <p data-testid="desk-date-head" :data-date="col.date"
                         class="group inline-flex cursor-pointer items-center gap-1 rounded px-1.5 py-0.5 text-sm font-semibold
                                transition-colors hover:bg-blue-50"
                         :class="col.date === fmtDate(new Date()) ? 'text-blue-600' : 'text-slate-700 hover:text-blue-600'"
                         title="点击查看当天的日视图"
                         @click="drillToDay(col.date)">
                        <span class="underline decoration-dotted decoration-1 underline-offset-2">{{ col.title }}</span>
                        <svg class="h-3 w-3 shrink-0 text-blue-500 opacity-60 transition-opacity group-hover:opacity-100"
                             viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                          <path fill="currentColor" d="M512 64a448 448 0 1 1 0 896 448 448 0 0 1 0-896m0 64a384 384 0 1 0 0 768 384 384 0 0 0 0-768m32 128v224h192v64H480V256z"/>
                        </svg>
                      </p>
                      <p class="text-xs text-slate-400">{{ col.sub }}</p>
                    </template>
                    <!-- 日视图：医生列头 + 当天余号（0=约满 / <5=少量余号） -->
                    <template v-else>
                      <p data-testid="desk-doctor-head" :data-doctor-id="col.doctorId"
                         class="flex items-center justify-center gap-1 text-sm font-semibold text-slate-700">
                        <span>{{ col.title }}</span>
                        <span v-if="isExpertDoctor(col.doctorId)" data-testid="desk-expert-badge"
                              class="shrink-0 rounded bg-amber-50 px-1 text-[10px] font-medium leading-4 text-amber-600 ring-1 ring-amber-200"
                              title="专家号">专家</span>
                      </p>
                      <p class="text-xs text-slate-400">{{ col.sub || '—' }}</p>
                      <p data-testid="desk-doctor-avail"
                         class="text-xs font-medium" :class="deskAvailClass(dayDoctorSchedules(col.doctorId))">
                        总 {{ deskTotal(dayDoctorSchedules(col.doctorId)) }} ·
                        {{ deskAvailText(dayDoctorSchedules(col.doctorId)) }}
                      </p>
                    </template>
                  </th>
                </tr>
              </thead>
              <tbody>
                <!-- desk-row：整行 hover 的高亮挂在这一层（见文件末尾 <style scoped>）。
                     两个视图的行都是"横着扫"的：日视图半小时一档十几行、周视图一位医生一行，
                     没有行高亮时看到第 6 行就串行了 —— 底色是唯一的横向导轨。 -->
                <tr v-for="row in deskRows" :key="'row-' + row.key" class="desk-row" :data-row-key="row.key">
                  <!-- 日视图行首 = 半小时档（08:00~08:30）。
                       分组不再写「凌晨/上午/下午/晚上」字样：时间戳本身就含这个信息（08:00 就是上午），
                       再挂一个文字标签等于同一件事说两遍，还挤占 96px 的行首列宽。
                       段落改由**加粗分隔线**表达（`row.sep`）：跨组、以及刻度压缩掉的空档处各画一条。 -->
                  <!-- 周视图行首 = 一位医生：名字 + 科室 + **本周合计**（已挂多少 / 还剩多少）。
                       「这个医生有了多少个挂号患者、还剩余多少」这句问的是整周，
                       写在行首才扫得出来；逐天的数字在右边七格里。 -->
                  <template v-if="deskViewMode === 'week'">
                    <td class="desk-row-head sticky left-0 z-10 border border-slate-100 bg-slate-50 p-2 align-top">
                      <p data-testid="desk-row-doctor" :data-doctor-id="row.doctorId"
                         class="flex items-center gap-1 text-sm font-semibold text-slate-700">
                        <span>{{ row.label }}</span>
                        <span v-if="isExpertDoctor(row.doctorId)" data-testid="desk-expert-badge"
                              class="shrink-0 rounded bg-amber-50 px-1 text-[10px] font-medium leading-4 text-amber-600 ring-1 ring-amber-200"
                              title="专家号">专家</span>
                      </p>
                      <p class="text-[11px] text-slate-400">{{ row.deptName || '—' }}</p>
                      <p data-testid="desk-row-stat" class="mt-0.5 text-[11px] leading-4">
                        <span class="text-blue-600">已挂 {{ weekRowStat(row.doctorId).regs }}</span>
                        <span class="text-slate-300"> / </span>
                        <span :class="deskAvailClass(deskSchedules.filter(s => String(s.doctorId) === String(row.doctorId)))">
                          {{ weekRowStat(row.doctorId).avail > 0 ? '余 ' + weekRowStat(row.doctorId).avail : '约满' }}
                        </span>
                      </p>
                    </td>
                  </template>
                  <!-- 日视图行首 = 半小时档（08:00~08:30）。
                       分组不再写「凌晨/上午/下午/晚上」字样：时间戳本身就含这个信息（08:00 就是上午），
                       再挂一个文字标签等于同一件事说两遍，还挤占 96px 的行首列宽。
                       段落改由**加粗分隔线**表达（`row.sep`）：跨组、以及刻度压缩掉的空档处各画一条。 -->
                  <td v-else class="desk-row-head sticky left-0 z-10 border bg-slate-50 p-2 text-center font-medium text-slate-600"
                      data-testid="desk-row-slot"
                      :data-group="row.group"
                      :class="['border-slate-300', row.sep ? 'border-t-2 !border-t-slate-500' : '']">
                    <span class="font-mono text-xs">{{ row.label }}</span>
                  </td>
                      <!-- 2026-09-22：格子本身不再绑单击（原来单击=弹号源明细）——
                           周视图一行就是一位医生，横着扫表格时会频繁误触弹出模态。
                           看明细的唯一入口是格子里的「余号」按钮；双击格子 = 开单（挂号窗口的手速习惯保留）。
                           ⚠️ 这条注释不能挪进 td 的属性区：HTML 注释出现在标签内部会被当成属性值解析，
                               编译器报的是 "Invalid Character `，`"，看着像语法错其实是注释位置错。 -->
                  <td v-for="col in deskColumns" :key="col.key + '-' + row.key"
                      data-testid="desk-cell" :data-col="col.key" :data-date="col.date"
                      :data-row="row.key" :data-shift="row.key"
                      :data-na="isCellUnavailable(col, row.key) ? '1' : '0'"
                      :class="['min-w-[168px] border align-top',
                               deskViewMode === 'day' ? 'p-1 border-slate-300' : 'p-1.5 border-slate-100',
                               row.sep ? 'border-t-2 !border-t-slate-500' : '',
                               isCellUnavailable(col, row.key) ? 'desk-cell-na' : '']"
                      @dblclick="handleDeskCellDblclick(col, row.key)">
                    <!-- 没排班 ≠ 排了没人挂：前者要看排班表，后者要看余号，两种空态必须分得清。
                         「未排班」还要再分一层：是本来就没排，还是被「隐藏已满/停诊」藏掉了 ——
                         静默消失会让人误以为"这天上午没医生"。两个视图都适用（日视图同样会藏掉医生整个半天）。 -->
                    <template v-if="cellHiddenCountFor(col, row.key) > 0
                                    && !cellSchedulesFor(col, row.key).length">
                      <!-- 日视图行细到半小时后空行很多，空态压到 h-8：18 行 × 44px 光空行就一屏半 -->
                      <p data-testid="desk-cell-allhidden"
                         :class="['flex items-center justify-center text-center text-xs text-amber-600', deskViewMode === 'day' ? 'h-8' : 'h-11']">
                        {{ cellAllFor(col, row.key).length }} 个班次已满/停诊（已隐藏）
                      </p>
                    </template>
                    <!-- 「未排班」判据用 srcSchedules（覆盖这一档的排班），不用段投影：
                         排班在但段没拉到时，段投影是空的 —— 那不是"没排班"，是"没段数据"，两回事 -->
                    <!-- 空态用一个「-」：这一格本来就没有信息，写「未排班」三个字是把同一句话
                         在几十个格子里重复刷屏，扫表格时眼睛读到的是一片字而不是号源。
                         点不动由 td 上的 cursor: not-allowed 表达，不靠文案。 -->
                    <p v-else-if="isCellUnavailable(col, row.key)" data-testid="desk-cell-na"
                       :class="['flex items-center justify-center text-xs text-slate-400', deskViewMode === 'day' ? 'h-8' : 'h-11']">-</p>
                    <template v-else>
                      <div class="mb-1 flex items-center justify-between gap-1 text-xs">
                        <span class="text-slate-500">
                          已挂 {{ deskCell(col.key, row.key).regs.length }} / {{ deskTotal(deskCell(col.key, row.key).schedules) }}
                        </span>
                        <span class="flex shrink-0 items-center gap-1">
                          <span class="font-medium" :class="deskAvailClass(deskCell(col.key, row.key).schedules)">
                            {{ deskAvailText(deskCell(col.key, row.key).schedules) }}
                          </span>
                          <!-- 「余号」按钮：周视图专供。
                               行已经是某一位医生了，格子里的「余 N」只给总数 —— 问"上午还剩多少、下午还剩多少"
                               得看明细，而这个按钮就在数字旁边，不用去点整个格子（点格子 = 双击的一半，容易误触发开单）。
                               2026-09-22：取代格子底部那行「N 个班次 · 查看时段/余号」文字入口 ——
                               可点的事情要做成按钮，写一行小字让人去猜哪里能点。 -->
                          <button v-if="deskViewMode === 'week'" data-testid="desk-cell-avail"
                                  class="shrink-0 cursor-pointer rounded border border-slate-200 bg-white px-1 text-xs leading-4 text-slate-600 hover:bg-slate-50"
                                  title="查看这位医生当天的每个班次：时段 / 余号 / 挂号费"
                                  @click.stop="openCellDialog(col.date, row.key)"
                                  @dblclick.stop>余号</button>
                          <!-- 预约入口收口到格子：有排班才渲染（本行就在「有排班」分支里，未排班/已满停诊已隐藏的格子没有按钮）。
                               点击与双击开单共用同一份预填（addFromCell）：科室/日期/医生带全，
                               格子里只有一条号源时直接选中，多条不猜留给窗口挑。dblclick.stop：别再冒泡到 td 上二次开单。 -->
                          <button data-testid="desk-cell-add"
                                  class="shrink-0 cursor-pointer rounded border border-blue-200 bg-white px-1 text-xs font-bold leading-4 text-blue-600 hover:bg-blue-50"
                                  title="新增预约：带入该时段的排班、医生、诊室、科室"
                                  @click.stop="addFromCell(col, row.key)"
                                  @dblclick.stop>预约</button>
                        </span>
                      </div>
                      <!-- 日视图：当天该医生该班次的患者全铺（单班次最多一个号源数，铺得下） -->
                      <template v-if="deskViewMode === 'day'">
                        <div v-for="r in deskCell(col.key, row.key).regs" :key="r.id"
                             data-testid="desk-card"
                             :data-regist-id="r.id"
                             :data-regist-status="r.registStatus"
                             class="group mb-1 rounded border px-2 py-1 text-sm leading-6"
                             :class="canReschedule(r)
                                ? 'cursor-pointer border-slate-200 bg-white hover:border-blue-300'
                                : 'cursor-default border-slate-100 bg-slate-50 hover:border-slate-300'"
                             :title="deskCardTitle(r)"
                             @click="handleDeskCardClick(r)">
                          <div class="flex items-center gap-1.5">
                            <span :class="['h-2 w-2 shrink-0 rounded-full',
                              deskStatusChip[Number(r.registStatus)]?.dot || regStatusDot[Number(r.registStatus)] || 'bg-slate-300']"></span>
                            <span class="truncate font-medium"
                                  :class="Number(r.registStatus) >= 5 ? 'text-slate-400' : 'text-slate-700'">
                              {{ r.patientName }}
                            </span>
                            <span v-if="deskPatientMeta(r)" class="shrink-0 text-xs text-slate-500 whitespace-nowrap">
                              {{ deskPatientMeta(r) }}
                            </span>
                            <span v-if="deskCardVisitNo(r)" class="shrink-0 text-xs text-slate-500 whitespace-nowrap">
                              {{ deskCardVisitNo(r) }}
                            </span>
                            <!-- 历史挂号没有段快照：不猜他挂的是哪一档，标出来让人知道这行是兜底归到首段的 -->
                            <span v-if="r.__unslotted"
                                  class="shrink-0 rounded bg-slate-100 px-1 py-0.5 text-[10px] text-slate-500">未选段</span>
                            <span v-if="deskStatusChip[Number(r.registStatus)]"
                                  class="ml-auto shrink-0 rounded px-1 py-0.5 text-xs font-medium whitespace-nowrap"
                                  :class="deskStatusChip[Number(r.registStatus)].cls">
                              {{ deskStatusChip[Number(r.registStatus)].text }}
                            </span>
                          </div>
                          <p class="truncate text-xs text-slate-500">
                            {{ deskCardDoctorLine(r) }}
                          </p>
                          <div v-if="deskCardMetaLine(r) || isRegistPaid(r)" class="flex items-center gap-1">
                            <p v-if="deskCardMetaLine(r)" class="truncate text-xs text-slate-500">
                              {{ deskCardMetaLine(r) }}
                            </p>
                            <!-- 已收费 = 这笔钱已经在账上，改号源按钮因此不出现（后端同样拦） -->
                            <span v-if="isRegistPaid(r)"
                                  class="shrink-0 rounded bg-emerald-50 px-1 py-0.5 text-xs font-medium text-emerald-700">
                              已收费
                            </span>
                          </div>
                          <!-- 只有「已挂号/已签到」才出现退号入口；按钮常驻，且是手型（能点就得像能点） -->
                          <div v-if="canCancelRegist(r)" class="mt-1 flex justify-end">
                            <button data-testid="desk-cancel"
                                    class="cursor-pointer rounded border border-red-200 bg-white px-2 py-0.5 text-xs font-medium text-red-500 hover:bg-red-50"
                                    @click.stop="handleDeskCancel(r)">退号</button>
                          </div>
                        </div>
                        <!-- 日视图不再写「未挂号」：格子顶部已经写了「已挂 0 / 3 · 余 3」，
                             半小时档一行一个"未挂号"会把整屏刷成同一句话 -->
                      </template>
                      <!-- 周视图：每格只铺前几张（一天 4 医生 × 20 号 = 80 张，全铺会把 DOM 拖死），其余下钻日视图 -->
                      <template v-else>
                        <div v-for="r in deskCell(col.key, row.key).regs.slice(0, DESK_WEEK_CELL_MAX)"
                             :key="r.id"
                             data-testid="desk-week-card" :data-regist-id="r.id"
                             class="flex items-center gap-1 leading-6"
                             :title="`${r.patientName}｜${deskCardDoctorLine(r)}${sourceLockReason(r) ? '｜' + sourceLockReason(r) : ''}`">
                          <span :class="['h-1.5 w-1.5 shrink-0 rounded-full',
                            deskStatusChip[Number(r.registStatus)]?.dot || regStatusDot[Number(r.registStatus)] || 'bg-slate-300']"></span>
                          <span class="truncate text-xs"
                                :class="Number(r.registStatus) >= 5 ? 'text-slate-400' : 'text-slate-700'">
                            {{ r.patientName }}
                          </span>
                          <span v-if="deskStatusChip[Number(r.registStatus)]"
                                class="ml-auto shrink-0 text-xs"
                                :class="deskStatusChip[Number(r.registStatus)].cls.split(' ').filter((c: string) => c.startsWith('text-')).join(' ')">
                            {{ deskStatusChip[Number(r.registStatus)].text }}
                          </span>
                        </div>
                        <p v-if="deskCell(col.key, row.key).regs.length > DESK_WEEK_CELL_MAX"
                           data-testid="desk-cell-more"
                           class="cursor-pointer text-xs text-blue-600 hover:underline"
                           @click="drillToDay(col.date)">
                          还有 {{ deskCell(col.key, row.key).regs.length - DESK_WEEK_CELL_MAX }} 人 → 看当天
                        </p>
                        <!-- 2026-09-22：「未挂号」这行字删掉了。
                             一格空着就是"排了没人挂"，半天/全天格子里重复这仨字把页面刷成同一句话，
                             还和隔壁「未排班」（=没号可挂）混着看 —— 两种空态必须有视觉差别，
                             空白 vs 文字正好是差别本身。 -->
                      </template>
                      <!-- 2026-09-22：周视图格子底部的「N 个班次 · 查看时段/余号」文字入口已去掉 ——
                           同一个动作收口到格子顶部的「余号」按钮（紧挨着余号数字，可点性明确）。
                           保留 @click 在 td 上：双击=开单，单击再/该格仍看得到这里的卡片，不需要多一层提示文字。 -->
                    </template>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 周视图格子的医生号源明细已改成弹框（见下方 el-dialog）：
               内联表压在表格下面，看板本身就得为它让出半屏高度 —— 而它只在点开那一格时才有用。 -->
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="挂号记录" name="records">
    <div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <el-form inline>
          <el-form-item label="患者">
            <PatientSelect
                v-model="searchForm.patientId"
                placeholder="搜索患者"
                width="192px"
                :page-size="20"
                size="small"
            />
          </el-form-item>
          <el-form-item label="科室">
            <el-select v-model="searchForm.deptId" placeholder="全部科室" filterable clearable class="!w-40">
              <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id ?? d.deptName"/>
            </el-select>
          </el-form-item>
          <el-form-item label="就诊状态">
            <el-select v-model="searchForm.registStatus" placeholder="全部" clearable class="!w-32">
              <el-option v-for="(label, val) in statusMap" :key="val" :label="label" :value="Number(val)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="就诊类型">
            <el-select v-model="searchForm.visitType" placeholder="全部" clearable class="!w-32">
              <el-option v-for="item in VISIT_TYPE" :key="item.value" :label="item.label" :value="item.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="就诊日期">
            <el-date-picker
                v-model="searchForm.visitDate"
                type="date"
                placeholder="全部日期"
                value-format="YYYY-MM-DD"
                clearable
                class="!w-40"
                @change="handleSearch"
            />
          </el-form-item>
          <el-form-item label="挂号时间">
            <el-date-picker
                v-model="searchForm.registDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                class="!w-72"
                :shortcuts="dateShortcuts"
            />
          </el-form-item>
          <el-form-item class="ml-auto">
            <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
            <el-button v-perm="'opd:appointments:add'" type="primary" :icon="Plus" class="ml-2" @click="showAddDialog = true">新增挂号</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="mt-4 rounded-lg border border-slate-200 bg-white shadow-sm">
        <el-table :data="appointments" v-loading="loading" style="width: 100%">
          <el-table-column label="就诊号" width="200">
            <template #default="{ row }">
              <span class="font-mono text-sm text-slate-600">{{ row.registNo || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="patientName" label="患者姓名" width="120"/>
          <el-table-column prop="patientNo" label="患者编号" width="200"/>
          <el-table-column label="就诊日期" width="110">
            <template #default="{ row }">
              <span class="font-mono text-xs text-slate-600">{{ row.visitDate || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="deptName" label="科室" width="120"/>
          <el-table-column prop="doctorName" label="医生" width="120"/>
          <el-table-column label="类型" width="150">
            <template #default="{ row }">
              <!-- 号别口径同 lib/statusColor：未知/未标注显「未标注」，不回落成初诊/复诊 -->
              <el-tag :class="revisitTypeOf(row.visitType).color + ' border'" effect="plain"
                      size="small">{{ revisitTypeOf(row.visitType).label }}
              </el-tag>
              <!-- 复诊来源决定这张号收没收费，账对不上时第一眼要看的就是它 -->
              <span v-if="row.revisitSource" class="ml-1 text-xs text-slate-500">
                {{ revisitSourceText(row.revisitSource) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="结算方式" width="120">
            <template #default="{ row }">
              <span
                  :class="['text-xs font-medium', settlementTypes.find(t => t.value === row.settlementType)?.color || 'text-slate-600']">
                {{ settlementLabel(row.settlementType) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="医保卡号" width="200">
            <template #default="{ row }">
              <span v-if="row.medicalInsuranceNo" class="font-mono text-xs text-slate-600">{{
                  row.medicalInsuranceNo
                }}</span>
              <span v-else class="text-xs text-slate-400">-</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :class="statusConfig[statusMap[row.registStatus]]?.color || null" effect="plain" size="small"
                      class="border gap-1">
                {{ statusMap[row.registStatus] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="300" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.registStatus === 1" type="success" link size="small" @click="handleCheckIn(row)">
                签到
              </el-button>
              <el-button v-if="row.registStatus === 1 && row.paymentStatus === 1" v-perm="'opd:appointments:edit'" type="primary" link :icon="Edit"
                         size="small" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button v-if="row.paymentStatus === 1" type="success" link size="small"
                         @click="handleShowPayment(row)">
                支付
              </el-button>
              <!-- 只有「已挂号(1)」「已签到(2)」才有退号入口，其余状态一律不出现（含灰色禁用态）：
                   已接诊/已就诊是诊疗已经发生，已过号/爽约/未就诊的号源属过去日期、退了也还不了池
                   （见 DayEndSettleMapper），已退号更没有重复退的道理。
                   状态本身在左侧「状态」列已写明，不需要再摆一个点不动的按钮。 -->
              <el-button v-if="canCancelRegist(row)" v-perm="'opd:appointments:delete'" type="danger" link size="small" @click="handleCancel(row)">
                退号
              </el-button>
              <!-- 就诊日一过，退号入口消失，但「人昨天没来、今天要退钱」的诉求还在。
                   这条出口走退费申请（挂号状态不动，钱由收费处审核执行），见脚本区同名注释。 -->
              <el-button v-if="canApplyRefund(row)" v-perm="'finance:refund:add'" type="warning" link size="small"
                         data-testid="apply-refund" @click="handleApplyRefund(row)">
                申请退费
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="appointments.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">
          暂无挂号记录
        </div>
        <div class="mt-4 flex justify-end">
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
      </div>
    </div>
      </el-tab-pane>

    </el-tabs>

    <!-- 看板改约：换号源（后端原子释放旧号/扣新号，同步就诊日期与医生） -->
    <el-dialog v-model="showRescheduleDialog" title="改约号源" width="480px" destroy-on-close>
      <div class="space-y-3">
        <div class="rounded bg-slate-50 px-3 py-2 text-sm text-slate-600">
          为 <span class="font-semibold text-slate-800">{{ rescheduleForm.patientName }}</span> 改约
          （当前就诊日期：{{ rescheduleForm.visitDate || '-' }}）
        </div>
        <el-form label-width="76px">
          <el-form-item label="就诊日期">
            <el-date-picker
                v-model="rescheduleForm.visitDate"
                type="date"
                placeholder="选择改约日期"
                value-format="YYYY-MM-DD"
                :disabled-date="(d) => d.getTime() < Date.now() - 86400000"
                class="!w-full"
                @change="() => { rescheduleForm.scheduleId = null; rescheduleForm.slotId = null; rescheduleSlots.value = []; loadRescheduleSchedules() }"
            />
          </el-form-item>
          <el-form-item label="新号源">
            <el-select v-model="rescheduleForm.scheduleId" placeholder="选择新号源"
                       :loading="rescheduleLoading" class="!w-full" :fit-input-width="false"
                       @change="handleRescheduleScheduleChange">
              <el-option v-for="s in rescheduleSchedules" :key="s.id" :value="s.id"
                         :label="`${s.scheduleDate} ${deskShiftLabel(s.scheduleType)} ${s.startTime}-${s.endTime} ${s.doctorName} 余${s.availableSource}`"/>
            </el-select>
            <p v-if="rescheduleForm.visitDate && !rescheduleSchedules.length && !rescheduleLoading"
               class="mt-1 text-xs text-amber-600">该日此科室暂有余号班次，换一天试试</p>
          </el-form-item>
          <el-form-item label="时间段">
            <!-- 选了段：后端按段迁移（还旧段/扣新段）；不选：只迁移主表号源 -->
            <el-select v-model="rescheduleForm.slotId" placeholder="选择时间段（可选）" clearable
                       class="!w-full" :loading="rescheduleSlotLoading" :disabled="!rescheduleSlots.length"
                       :fit-input-width="false">
              <el-option v-for="s in rescheduleSlots" :key="s.id" :value="s.id"
                         :label="slotLabel(s)" :disabled="s.status !== 1 || s.availableSource <= 0"/>
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showRescheduleDialog = false">取消</el-button>
        <el-button v-perm="'opd:appointments:edit'" type="primary" :loading="rescheduleLoading" @click="handleConfirmReschedule">确认改约</el-button>
      </template>
    </el-dialog>

    <!-- 过期号的退费申请：挂号状态不动，只把钱走「申请 → 收费处审核 → 执行」链路。
         这里只是发起口，审核/执行都在收费处「退费管理」页，所以按钮文案用「提交申请」而不是「确认退费」。 -->
    <el-dialog v-model="showRefundDialog" title="申请退费" width="520px" destroy-on-close>
      <el-form label-width="96px" class="space-y-2">
        <el-form-item label="患者">
          <span class="text-sm text-slate-800">{{ refundForm.patientName || '-' }}</span>
          <span class="ml-3 font-mono text-xs text-slate-500">{{ refundForm.registNo }}</span>
        </el-form-item>
        <el-form-item label="原收费单">
          <span class="font-mono text-xs text-slate-600">{{ refundForm.billNo || '-' }}</span>
        </el-form-item>
        <el-form-item label="退费类型" required>
          <el-select v-model="refundForm.refundType" placeholder="请选择" class="!w-full" :fit-input-width="false">
            <el-option v-for="o in refundTypeOptions" :key="o.dictValue" :label="o.dictLabel"
                       :value="Number(o.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="退费金额" required>
          <el-input-number v-model="refundForm.refundAmount" :min="0" :max="refundForm.refundableAmount" :precision="2"
                           :step="1" class="!w-full"/>
          <p class="mt-1 text-xs text-slate-400">可退金额 ¥{{ refundForm.refundableAmount }}（实付扣掉已退部分），可改小做部分退费</p>
        </el-form-item>
        <el-form-item label="退费原因" required>
          <el-input v-model="refundForm.refundReason" type="textarea" :rows="3" placeholder="如：患者当日未到诊，申请退还挂号费"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRefundDialog = false">取消</el-button>
        <el-button v-perm="'finance:refund:add'" type="primary" :loading="refundSubmitting" @click="handleSubmitRefund">
          提交申请
        </el-button>
      </template>
    </el-dialog>

    <!-- 周视图格子点开的「医生号源明细」。
         为什么是弹框而不是内联表：内联表常驻在表格下面，看板高度要为它让出半屏
         （实测挤掉 200+ px，日视图那张撑满窗口的表直接被压回"看半屏滚半屏"）；
         而它只在"我想知道这一格还有谁能挂"那一刻才需要 —— 按需弹、用完关。 -->
    <!-- 宽度给到 900px + 表格自带横向滚动：原来 720px 时列被挤到互相贴边，
         「余号 / 总数」和「挂号费」连在一起读不出来 —— 弹框是要当收据看的，挤不得。
         去掉「医生」列：标题里已经写了是哪位医生，占 110px 却只重复同一个名字。 -->
    <el-dialog v-model="showCellDialog" width="900px" destroy-on-close
               :title="selectedCell
                 ? `${selectedCell.date} · ${selectedCell.doctorName} 号源明细`
                 : '医生号源明细'">
      <div v-if="selectedCell" data-testid="desk-cell-detail">
        <el-table :data="cellSchedules(selectedCell.date, selectedCell.doctorId)" size="small" border max-height="520">
          <el-table-column label="班次" width="110">
            <template #default="{ row }">
              <span class="text-xs text-slate-600">{{ deskShiftLabel(Number(row.scheduleType)) || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="时段" width="180">
            <template #default="{ row }">
              <span class="font-mono text-xs text-slate-600">{{ row.startTime }}-{{ row.endTime }}</span>
            </template>
          </el-table-column>
          <el-table-column label="诊室" width="140">
            <template #default="{ row }">
              <span class="text-xs text-slate-600">{{ row.roomName || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="余号 / 总数" width="140">
            <template #default="{ row }">
              <span :class="['text-sm font-medium', row.availableSource > 0 ? 'text-emerald-600' : 'text-slate-400']">
                {{ row.availableSource }}
              </span>
              <span class="text-xs text-slate-400"> / {{ row.totalSource }}</span>
            </template>
          </el-table-column>
          <el-table-column label="挂号费" width="120" align="right">
            <template #default="{ row }">
              <span class="text-sm text-slate-700">¥{{ row.registFee || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="专家" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.isExpert === 1" type="warning" size="small" effect="plain">专家</el-tag>
              <span v-else class="text-xs text-slate-400">普通</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button v-perm="'opd:appointments:add'" type="primary" size="small" link :disabled="row.availableSource <= 0"
                         @click="goRegister(row)">
                去挂号
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <!-- 开关藏掉的班次要给总账：弹框里"只有 3 位医生"和"实际排了 5 位"必须说清 -->
        <p v-if="cellHiddenCount(selectedCell.date, selectedCell.doctorId) > 0"
           class="mt-2 text-xs text-amber-600">
          另有 {{ cellHiddenCount(selectedCell.date, selectedCell.doctorId) }} 个班次已满/停诊，
          当前被「隐藏已满/停诊」开关隐藏
        </p>
      </div>
      <template #footer>
        <el-button @click="showCellDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showAddDialog" title="新增挂号" width="760px" destroy-on-close @closed="resetForm">
      <!-- 患者来源切换（与急诊登记同一交互）：tab 只切换**患者输入区**，
           下面的科室 / 日期 / 号源 / 时段 / 就诊类型 / 结算方式 是共享字段 -->
      <div class="mb-4 flex rounded-lg border border-slate-200 bg-slate-50 p-1">
        <button
            type="button"
            class="flex-1 rounded-md py-2 text-sm font-medium transition-colors"
            :class="addMode === 'existing' ? 'bg-white text-blue-600 shadow-sm' : 'text-slate-500 hover:text-slate-700'"
            @click="switchPatientMode('existing')"
        >选择已有患者</button>
        <button
            type="button"
            class="flex-1 rounded-md py-2 text-sm font-medium transition-colors"
            :class="addMode === 'new' ? 'bg-white text-blue-600 shadow-sm' : 'text-slate-500 hover:text-slate-700'"
            @click="switchPatientMode('new')"
        >录入新患者</button>
      </div>

      <el-form label-position="top" :model="newAppointment">
        <!-- tab1：选择已有患者 -->
        <template v-if="addMode === 'existing'">
          <el-form-item label="选择患者" required>
            <PatientSelect
                v-model="newAppointment.patientId"
                @select="handlePatientSelect"
            />
          </el-form-item>
          <!-- 已选患者回显：挂号窗口最容易犯的错是挂错人，选完必须让人一眼确认 -->
          <div v-if="newAppointment.patientId" class="mb-4 rounded-lg border border-blue-200 bg-blue-50 p-3">
            <div class="flex items-center gap-3 text-sm">
              <span :class="['inline-flex h-8 w-8 items-center justify-center rounded-full text-sm font-bold text-white',
                newAppointment.patientGender === 1 ? 'bg-blue-500'
                  : newAppointment.patientGender === 2 ? 'bg-pink-500' : 'bg-slate-400']">
                {{ patientGenderSymbol(newAppointment.patientGender) }}
              </span>
              <div>
                <span class="font-medium text-slate-900">{{ newAppointment.patientName }}</span>
                <span class="ml-2 text-xs text-slate-500">{{ newAppointment.patientNo }}</span>
              </div>
            </div>
          </div>
        </template>

        <!-- tab2：录入新患者 -->
        <template v-else>
          <div class="mb-4 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
            将在患者主档新建档案，确认挂号时一并提交；其余信息可到「患者管理」补全
          </div>
          <div class="grid grid-cols-2 gap-4">
            <el-form-item label="患者姓名" required>
              <el-input v-model="newPatientForm.patientName" placeholder="请输入患者姓名"/>
            </el-form-item>
            <el-form-item label="性别" required>
              <el-radio-group v-model="newPatientForm.gender">
                <el-radio v-for="g in PATIENT_GENDER_OPTIONS" :key="g.value" :value="g.value">{{ g.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <el-form-item label="出生日期">
              <el-date-picker
                  v-model="newPatientForm.birthDate"
                  type="date"
                  placeholder="选择出生日期"
                  value-format="YYYY-MM-DD"
                  class="w-full"
              />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="newPatientForm.phone" placeholder="选填（填了须为 11 位手机号）"/>
            </el-form-item>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <el-form-item label="身份证号" required>
              <el-input v-model="newPatientForm.idCard" placeholder="请输入身份证号" @blur="syncPatientBirthDate"/>
            </el-form-item>
            <el-form-item label="患者类型">
              <el-select v-model="newPatientForm.patientType" placeholder="选择患者类型" class="w-full">
                <el-option v-for="o in PATIENT_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
              </el-select>
            </el-form-item>
          </div>
          <el-form-item label="家庭住址">
            <el-input v-model="newPatientForm.address" placeholder="请输入家庭住址"/>
          </el-form-item>
        </template>

        <!-- 就诊类型提到科室之前：选「复诊 + 当日回诊」时下面的科室/日期/号源整块都不填，
             放在最后等于让人先填完再看着它们消失 -->
        <el-form-item label="就诊类型">
          <el-select v-model="newAppointment.visitType" placeholder="选择类型" class="w-full"
                     @change="handleVisitTypeChange">
            <el-option v-for="item in VISIT_TYPE" :key="item.value" :label="item.label" :value="item.value"/>
          </el-select>
        </el-form-item>
        <template v-if="isRevisit">
          <div class="grid grid-cols-2 gap-4">
            <el-form-item label="复诊来源" required>
              <el-select v-model="newAppointment.revisitSource" placeholder="选择复诊来源" class="w-full">
                <el-option v-for="o in windowRevisitSourceOptions" :key="o.dictValue" :label="o.dictLabel"
                           :value="Number(o.dictValue)"/>
              </el-select>
            </el-form-item>
            <el-form-item label="原病历（哪一次就诊）" required>
              <el-select v-model="newAppointment.revisitRecordId" class="w-full"
                         :loading="revisitRecordLoading"
                         :placeholder="newAppointment.patientId ? '选择原病历' : '请先选择患者'"
                         :disabled="!newAppointment.patientId">
                <el-option v-for="r in revisitRecords" :key="r.id" :label="revisitRecordLabel(r)" :value="r.id"/>
              </el-select>
            </el-form-item>
          </div>
          <!-- 「查过了但没病历」要说清是没病历，不是没选（否则人会回去反复点患者） -->
          <p v-if="newAppointment.patientId && !revisitRecordLoading && !revisitRecords.length"
             class="mb-4 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
            该患者还没有可关联的就诊病历 —— 请改为初诊，或由医生在诊室里开「当日回诊」
          </p>
        </template>

        <!-- 当日回诊是同一次挂号的延续：不占号源，也就不需要科室 / 日期 / 号源 / 时段 -->
        <template v-if="!isSameDayReturn">
          <div class="grid grid-cols-2 gap-4">
            <el-form-item label="选择科室" required>
              <el-select v-model="newAppointment.deptId" filterable placeholder="选择科室" class="w-full"
                         @change="handleDeptChange">
                <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id ?? d.deptName"/>
              </el-select>
            </el-form-item>
            <el-form-item label="预约日期" required>
              <el-date-picker
                  v-model="newAppointment.visitDate"
                  type="date"
                  placeholder="选择日期"
                  value-format="YYYY-MM-DD"
                  class="w-full"
                  :disabled-date="(time: Date) => time.getTime() < Date.now() - 86400000"
                  @change="handleDateChange"
              />
            </el-form-item>
          </div>
          <el-form-item label="选择号源" required>
            <!-- placeholder 必须动态：原来写死「请先选择科室和日期」，等科室日期都填好、号源也拉回来了，
                 它还挂着那句话 —— 用户会以为是自己没填，回去反复点科室。空态提示要跟着真实状态走 -->
            <el-select v-model="newAppointment.scheduleId" :placeholder="schedulePlaceholder" class="w-full"
                       :disabled="!availableSchedules.length" @change="handleScheduleChange">
              <el-option
                  v-for="s in availableSchedules"
                  :key="s.id"
                  :label="`${s.doctorName} | ${s.roomName || '诊室待定'} | ${s.startTime}-${s.endTime} | 余号:${s.availableSource} | 挂号费:¥${s.registFee}`"
                  :value="s.id ?? s.doctorName"
              />
            </el-select>
            <!-- 「查过了但没号」由 placeholder 说（schedulePlaceholder），这里不再重复第二遍 -->
          </el-form-item>
          <el-form-item label="时间段（半小时一档）">
            <!-- 号源事实在段上：选段 = 定下这半小时的号；余 0 的段禁用，段已满提示换相邻段 -->
            <el-select v-model="newAppointment.slotId" placeholder="请选择时间段" clearable class="w-full"
                       :loading="slotLoading" :disabled="!slotRows.length">
              <el-option v-for="s in slotRows" :key="s.id" :label="slotLabel(s)" :value="s.id"
                         :disabled="s.status !== 1 || s.availableSource <= 0"/>
            </el-select>
            <p v-if="newAppointment.scheduleId && !slotRows.length && !slotLoading"
               class="w-full text-xs text-slate-400 mt-1">该号源暂无时间段明细，将按整班次号源挂号</p>
          </el-form-item>
        </template>
        <!-- 费用预估：与后端实收走同一个 decide，命中哪条策略、免了哪几项原样显示 ——
             免钱这件事必须当场说得清，不能让收费员自己猜为什么这张号是 0 元 -->
        <div v-if="isRevisit && (revisitPreview || revisitPreviewLoading)"
             class="mb-4 rounded-lg border border-slate-200 bg-slate-50 px-3 py-2">
          <div class="flex items-center justify-between text-sm">
            <span class="text-slate-500">复诊应收</span>
            <span class="font-mono text-lg font-bold"
                  :class="Number(revisitPreview?.totalFee) > 0 ? 'text-red-500' : 'text-emerald-600'">
              {{ revisitPreviewLoading ? '计算中…' : `¥${Number(revisitPreview?.totalFee || 0).toFixed(2)}` }}
            </span>
          </div>
          <div v-if="revisitPreview && !revisitPreviewLoading" class="mt-1 text-xs text-slate-500">
            挂号费 ¥{{ Number(revisitPreview.registFee || 0).toFixed(2) }} ·
            诊查费 ¥{{ Number(revisitPreview.diagnosisFee || 0).toFixed(2) }} ·
            {{ revisitPreview.policyName ? `命中策略「${revisitPreview.policyName}」` : '未命中策略，按全额收费' }}
          </div>
          <p v-if="revisitPreview?.reason && !revisitPreviewLoading" class="mt-1 text-xs text-slate-400">
            {{ revisitPreview.reason }}
          </p>
        </div>
        <el-form-item label="结算方式" required>
          <el-select v-model="newAppointment.settlementType" placeholder="选择结算方式" class="w-full">
            <el-option v-for="item in settlementTypes" :key="item.value" :label="item.label" :value="item.value"/>
          </el-select>
        </el-form-item>
        <div v-if="newAppointment.settlementType !== 1" class="rounded-lg border border-blue-200 bg-blue-50 p-3">
          <div class="grid grid-cols-2 gap-4">
            <el-form-item label="医保类型" class="!mb-0">
              <el-select v-model="newAppointment.medicalInsuranceType" placeholder="请选择医保类型" filterable
                         class="w-full">
                <el-option v-for="item in medicalInsuranceTypes" :key="item.dictValue" :label="item.dictLabel"
                           :value="item.dictValue ?? item.dictLabel"/>
              </el-select>
            </el-form-item>
            <el-form-item label="医保卡号" class="!mb-0">
              <el-input v-model="newAppointment.medicalInsuranceNo" placeholder="请输入医保卡号"/>
            </el-form-item>
          </div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button v-perm="'opd:appointments:add'" type="primary" :loading="addSubmitLoading" @click="handleSubmit">确认挂号</el-button>
      </template>
    </el-dialog>

    <!-- 编辑挂号弹窗 -->
    <el-dialog v-model="showEditDialog" title="修改挂号" width="600px" destroy-on-close>
      <el-form label-position="top" :model="editForm">
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="选择科室" required>
            <el-select v-model="editForm.deptId" placeholder="选择科室" class="w-full" @change="handleEditDeptChange">
              <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id ?? d.deptName"/>
            </el-select>
          </el-form-item>
          <el-form-item label="选择医生">
            <el-select v-model="editForm.doctorId" placeholder="选择医生" class="w-full">
              <el-option v-for="e in employeeList" :key="e.id" :label="`${e.empName} - ${e.title || ''}`"
                         :value="e.id ?? e.empName"/>
            </el-select>
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="预约日期" required>
            <el-date-picker
                v-model="editForm.visitDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                class="w-full"
                :disabled-date="(time: Date) => time.getTime() < Date.now() - 86400000"
                @change="handleEditDateChange"
            />
          </el-form-item>
          <el-form-item label="就诊类型">
            <el-select v-model="editForm.visitType" class="w-full">
              <el-option v-for="item in VISIT_TYPE" :key="item.value" :label="item.label" :value="item.value"/>
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="选择号源" required>
          <el-select v-model="editForm.scheduleId" placeholder="请先选择科室和日期" class="w-full"
                     :disabled="!editSchedules.length">
            <el-option
                v-for="s in editSchedules"
                :key="s.id"
                :label="`${s.doctorName} | ${s.startTime}-${s.endTime} | 余号:${s.availableSource} | 挂号费:¥${s.registFee}`"
                :value="s.id ?? s.doctorName"
            />
          </el-select>
          <p v-if="editForm.deptId && editForm.visitDate && !editSchedules.length" class="mt-1 text-xs text-slate-400">
            该日期无可挂号源
          </p>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button v-perm="'opd:appointments:edit'" type="primary" :loading="submitLoading" @click="handleEditSubmit">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- 支付弹窗 -->
    <el-dialog v-model="showPaymentDialog" title="挂号缴费" width="420px" :close-on-click-modal="false"
               :close-on-press-escape="false" :show-close="false" destroy-on-close>
      <div class="flex flex-col items-center py-4">
        <div class="mb-4 text-center">
          <p class="text-sm text-slate-500">收费单号</p>
          <p class="font-mono text-lg font-bold text-slate-900">{{ paymentInfo.billNo || '-' }}</p>
        </div>
        <div class="mb-4 text-center">
          <p class="text-sm text-slate-500">患者</p>
          <p class="text-base font-medium text-slate-700">{{ paymentInfo.patientName }}</p>
        </div>
        <div class="mb-6 text-center">
          <p class="text-sm text-slate-500">应付金额</p>
          <p class="text-3xl font-bold text-red-500">¥{{ paymentInfo.amount.toFixed(2) }}</p>
        </div>
        <!-- 二维码 -->
        <div class="rounded-lg border border-slate-200 p-3">
          <img
              :src="`https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=PAY:${paymentInfo.billNo}:${paymentInfo.amount}`"
              alt="支付二维码"
              class="h-[180px] w-[180px]"
          />
        </div>
        <p class="mt-3 text-xs text-slate-400">请使用微信/支付宝扫码支付</p>
      </div>
      <template #footer>
        <div class="flex justify-between">
          <el-button @click="showPaymentDialog = false" type="info" plain>稍后支付</el-button>
          <el-button type="success" :loading="paymentLoading" @click="handleConfirmPayment">
            确认已支付
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.patient-select :deep(.el-select-dropdown__item) {
  height: auto;
  min-height: 72px;
  padding: 8px 20px;
  line-height: 1.5;
}

/* ===== 看板表格：行 hover 导轨 + 未排班格子的"不可点"态 =====
   两条都是**表格级别**的视觉，落在 scoped 里而不是逐格写 class：
   td 上已经挂了一串 tailwind 背景类（bg-slate-50 / bg-white），
   用 CSS 特异性（tr.desk-row:hover > td）才盖得住；tailwind 的 group-hover
   和 bg-* 同权重，谁生效取决于生成顺序 —— 那种靠运气的写法不能用。 */

/* 行 hover：整行淡蓝 + 行首列再深一档（横着扫十几行时，底色是唯一的横向导轨）。
   行首列必须一起变：它是 sticky 的，卡片区变了行首不变，眼睛会丢掉"这是哪一行"。 */
tr.desk-row:hover > td {
  background-color: #e8f1fc;
}

tr.desk-row:hover > td.desk-row-head {
  background-color: #d3e4f8;
}

/* 未排班：**只留禁止光标，不给底色**（2026-09-22 改）。
   淡灰底的问题是一大片「未排班」连起来会变成一块视觉禁区 —— 扫表格时注意力被空格子抢走，
   真正有号源的格子反而被淹掉。空态用一个「-」就够了，底色是重复表达。
   光标必须留：这一格没有号源，点它不会有任何反应，得让人在按下去之前就知道。
   ⚠️ 不再挡行 hover：未排班的格子也跟着整行变蓝，导轨才是一条整线（跳过格子 = 导轨断在中间）。 */
td.desk-cell-na {
  cursor: not-allowed;
}
</style>

<style>
/* ===== 预约看板整体放大一档（2026-09-24 需求：字太小看不清） =====
   看板（工具栏/日历栏/表格）整块用 .desk-board 圈住，这里按容器批量覆盖字号，
   不逐处改 tailwind class（同一 utility 在文件里出现几十次，逐条改必漏）。
   ⚠️ 必须放在**非 scoped** 块里：scoped 会给复合选择器最后一个元素加 [data-v-xxx]，
      `.desk-board .text-xs[data-v-xxx]` 命不中子元素（utility 在组件模板上、没有该属性），
      现象是"规则写了完全没生效"。
   Tailwind v4 的 utility 在 @layer utilities 内，无层级样式恒定压制有层级样式，不用 !important。
   弹框（el-dialog teleport 到 body）不在 .desk-board 内，零影响。 */
.desk-board .text-xs { font-size: 14px; }
.desk-board .text-\[11px\] { font-size: 13px; }
.desk-board .text-\[10px\] { font-size: 12px; }
.desk-board .text-\[9px\] { font-size: 11px; }
/* 表格内 text-sm（卡片主体、周视图日期列头）14px → 16px */
.desk-board table .text-sm { font-size: 16px; }
.desk-board table td,
.desk-board table th { line-height: 1.55; }
/* 灰色文字同步加深一档（slate-400 在 12px 下几乎不可读，放大后仍偏淡） */
.desk-board .text-slate-300 { color: #94a3b8; }
.desk-board .text-slate-400 { color: #64748b; }
.desk-board .text-slate-500 { color: #475569; }
/* 例外：日历下方 2×2 概况的标签（11px）只放到 12px —— 左栏只有 220px 宽，
   13px 时「总号源 / 237 个」折成三行，实测比小一号更难读。 */
.desk-board aside .grid [class*="text-[11px]"] { font-size: 12px; }
</style>

<style>
.patient-select-dropdown {
  width: 500px !important;
}
</style>
