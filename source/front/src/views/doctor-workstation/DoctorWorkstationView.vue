<script setup lang="ts">
import {computed, onMounted, onUnmounted, reactive, ref, watch} from 'vue'
import {useRoute} from 'vue-router'
import {patientGenderText} from '@/lib/patientGender'
import {
  ArrowDown,
  ArrowLeft,
  ArrowRight,
  CircleCheck,
  Clock,
  Coin,
  CopyDocument,
  Delete,
  Document,
  InfoFilled,
  Loading,
  MagicStick,
  Monitor,
  Plus,
  Printer,
  Reading,
  RefreshRight,
  Search,
  Tickets,
  VideoPause,
  VideoPlay,
  Warning
} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox} from 'element-plus'
// 批次B：三栏同屏抽出的公共组件
import PatientBriefBar from '@/components/his/PatientBriefBar.vue'
import RevisitAppointDialog from '@/components/his/RevisitAppointDialog.vue'
import {REVISIT_SOURCE} from '@/lib/revisitPolicy'
import PatientDetailDialog from '@/components/his/PatientDetailDialog.vue'
import {useCurrentPatientStore} from '@/stores/currentPatient'
import PageActionBar from '@/components/his/PageActionBar.vue'
import OrderPanel from '@/components/his/OrderPanel.vue'
import {
  callNextQueue,
  callPatient,
  createRevisitRegistration,
  estimateInsurance,
  getCurrentDoctorStatus,
  getQueueStats,
  getTodayQueueList,
  recallPatient,
  setDoctorStatus
} from '@/api/appoint'
import {
  getDepartmentSelectList,
  getDictDataMapList,
  getDrugSelectList,
  getInspectionSelectList,
  getLaboratorySelectList,
  getPatientTagListAll,
  predictIcd10,
  searchIcd10,
  searchInspectionItem,
  searchLaboratoryItem
} from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import {getByRegistId, getEmrRecordList} from '@/api/emr'
import {createAdmissionOrder} from '@/api/admissionOrder'
import {draftEmrText, extractEmrText} from '@/api/ai'
import {localDateStr, shortQueueNo} from '@/lib/utils'
// 「号别」「队列状态」一律走 lib/statusColor 这一份口径：
// 原来队列行只凭 `visitType` 各写一套兜底 —— 列表 `===1 ? 初诊 : 复诊`、患者条 `===2 ? 复诊 : 初诊`，
// visitType 为 null（队列行没挂到挂号）时一处说「复诊」一处说「初诊」，同一条数据两个相反结论。
import {QUEUE_STATUS, queueVisitBadgeOf} from '@/lib/statusColor'
import {
  deleteDiagTemplate,
  deleteDrugPackage,
  deleteInspectionApply,
  deleteInspectionTemplate,
  deleteLaboratoryApply,
  deleteLaboratoryTemplate,
  deleteRxTemplate,
  getDiagTemplates,
  getDrugPackageDetail,
  getDrugPackages,
  getInspectionApplyList,
  getInspectionTemplates,
  getLaboratoryApplyList,
  getLaboratoryTemplates,
  getPrescriptionList,
  getRxTemplateDetail,
  getRxTemplates,
  saveDiagTemplates,
  saveDrugPackage,
  saveInspectionApply,
  saveInspectionTemplate,
  saveLaboratoryApply,
  saveLaboratoryTemplate,
  saveMedicalRecord,
  saveRxTemplate,
  submitMedicalRecord
} from '@/api/doctor'
import {getStockList} from '@/api/pharmacy'
import {addPatientTag, getPatientDetail, getPatientTags, removePatientTag} from '@/api/patient'
import {getInspectionDetail, getLaboratoryDetail, getLaboratoryRecordList} from '@/api/medicaltech'
import {listItemsByPatient} from '@/api/settlementBill'


// ========== 状态 ==========
// 队列状态口径统一到 lib/statusColor.QUEUE_STATUS（QueueStatusEnum 2/3/4/5/6）。
// 原来本地这份把 4-已就诊 写成红色、5-已退号 写成绿色，与全站（也是 lib/statusColor）正好相反。
const queueStatusMap: Record<number, { label: string; color: string }> = QUEUE_STATUS

const loading = ref(false)
/**
 * 批次E/F：「加载中」和「真的没有」必须分开。
 *
 * 选中患者后是 5 个并发请求（既往病历/处方/检查/检验/收费），原实现不看加载状态就渲染
 * 「暂无处方 / 暂无收费记录」—— 医生点开患者的一瞬间会被喂一句假话，等请求回来才自我更正。
 * 这里用一个在途计数把它们兜住：加载中显示「加载中…」，回来之后才有资格说「暂无」。
 */
const patientDataPending = ref(0)
const patientDataLoading = computed(() => patientDataPending.value > 0)
const trackPatientLoad = (p: Promise<any>) => {
  patientDataPending.value++
  // 各 loader 内部都自己吞异常，这里只负责收尾计数
  return p.finally(() => {
    patientDataPending.value = Math.max(0, patientDataPending.value - 1)
  })
}
const departments = ref<any[]>([])
const selectedDeptId = ref<number | null>(null)
const queueList = ref<any[]>([])

// ========== 顶部搜索切换当前患者 ==========
// Header 在别处选中患者后会带 patientId 跳到这里（或本页直接选中）。这里决定「能不能接」：
// 只有患者出现在今日候诊队列（即有当日挂号）才允许进入接诊流程 —— 开单要绑挂号单，
// 主档行没有 registId。不在队列时不再让这次搜索落空：提示 + 把患者交回详情框看档案。
//
// 时序坑：从别的页面搜患者跳过来时，本组件 onMounted 早于队列接口返回，
// 此刻 queueList 还是空的，不能立刻判定「不在队列」——先把患者挂起，等队列到了再判。
const currentPatientStore = useCurrentPatientStore()
const route = useRoute()
const queueLoaded = ref(false)
/** 已处理过的切换时间戳，避免 watch 与 onMounted 重复消费同一次切换 */
let handledSwitchAt = 0
/** 待判定患者（队列还没加载完时暂存） */
let pendingSwitch: any = null

/**
 * 本次进入医生站是「为某个特定患者而来」（顶部搜索跳转 / 刷新恢复 / URL 带 patientId）。
 * 为 true 时首次加载**不**自动选中队列里正在就诊的患者：医生是来找人的，
 * 先选中别人等于把「找人」变成「切换到别人」，而开单挂当前患者。
 * 由 onMounted 设置，applyPendingSwitch 落地后复位（不影响之后的手动操作）。
 */
let enteredWithTarget = false

/**
 * URL 上显式带了 patientId 且会话里没有当前患者（手输 URL / 外链进入）—— 这算一次「用户意图」，
 * 让 applyPendingSwitch 给反馈（提示 + 交回详情框）。
 * 与「切菜单重建」的区别：那种情况 store 里**有**患者（会话级持久化恢复的），不该重复提示。
 */
let urlIntent = false

/**
 * 队列优先级：就诊中 > 候诊中 > 已完成 > 已过号 > 已退号。
 *
 * 列表显示（filteredQueue）与「该接哪一条」（pickQueueRow）**必须共用这一份**：
 * 两处各写一套的后果是「列表把复诊排在第一位，顶栏搜索却选中了初诊那条」——
 * 医生按列表顺序理解，页面按另一套逻辑选人。
 */
const QUEUE_STATUS_ORDER: Record<string, number> = {3: 0, 2: 1, 4: 2, 5: 3, 6: 4}

/** 队列行比较器：先状态优先级，同状态按到达时间，再按序号（保证排序稳定） */
const compareQueueRow = (a: any, b: any) => {
  const oa = QUEUE_STATUS_ORDER[a.queueStatus] ?? 5
  const ob = QUEUE_STATUS_ORDER[b.queueStatus] ?? 5
  if (oa !== ob) return oa - ob
  if (a.arriveTime && b.arriveTime) {
    const ta = new Date(a.arriveTime).getTime()
    const tb = new Date(b.arriveTime).getTime()
    if (ta !== tb) return ta - tb
  }
  return (a.sequenceNo || 0) - (b.sequenceNo || 0)
}

/**
 * 同一个患者今天可能有多条队列记录（初诊 + 复诊 / 多次挂号），挑出「该接的那一条」。
 *
 * 不能直接 `find()`：后端 getTodayQueueList 是 `orderByAsc(sequence_no)`，
 * find 撞上的是**序号最小的那条 = 初诊那一条**，哪怕它已经就诊完成、而下午的复诊还在候诊。
 * 危害在于 selectPatient(row) 会把这条的 `registId` 写进 recordForm ——
 * 也就是**病历 / 医嘱挂在哪一次就诊上**：接错诊次 = 给已完成的那次继续写病历
 * （复诊自带 revisitRecordId 要求回原病历，口径更不能靠数组顺序碰运气）。
 */
const pickQueueRow = (rows: any[]) => {
  if (!rows || !rows.length) return null
  return [...rows].sort(compareQueueRow)[0]
}

const applyPendingSwitch = () => {
  const p = pendingSwitch
  if (!p) return
  pendingSwitch = null
  // 这次是不是「用户在顶栏主动选的人」？只有用户主动选人才给成功 / 警告提示。
  // 切菜单会让本组件卸载重建，而 store 是会话级持久化的（患者还在），
  // 靠组件自己的变量判断会把每次重建都当成一次新切换 —— 现象是每点一个菜单
  // 弹一次「已切换接诊患者 xxx」。提示跟着「用户选人」与「手输 URL 的显式意图」，
  // 不跟「组件重建」。
  const notify = currentPatientStore.consumeSwitchNotice() || urlIntent
  urlIntent = false
  // 同一患者今天多条队列时，挑「该接的那一条」而不是数组第一条（见 pickQueueRow）
  const target = pickQueueRow(queueList.value.filter((q: any) => String(q.patientId) === String(p.id)))
  if (target) {
    selectPatient(target)
    if (notify) {
      ElMessage.success(`已切换接诊患者：${target.patientName || p.patientName}`)
    }
  } else {
    if (notify) {
      // 用户主动搜的人不在队列：要说清楚，并把这次搜索交回详情框（不让它落空）
      ElMessage.warning(`${p.patientName || '该患者'} 不在您的今日候诊队列中，已改为展示患者档案`)
      currentPatientStore.requestDetail(p.id)
    }
    // 不是用户主动选的（切菜单重建 / 会话恢复）且人已不在队列 → 静默收摊：
    // 不弹档也不提示，否则跨天残留会让医生每点一次菜单就被弹一次档案。
    // 顶栏那条无论哪种来源都要回滚 —— 条子说「当前患者是A」而页面没选中任何人，
    // 在防开错人的场景里比不显示更危险。
    // ⚠ 必须用 toStorePatient 映射：currentPatient 存的是**队列行**，它的 id 是队列号不是患者 id，
    //   直接 syncPatient(currentPatient.value) 会让顶栏拿队列 id 去查患者主档（实测 500：
    //   GET /patient/getById?patientId=2990000000000000903）。
    // 用 syncPatient/syncClear：它们不动 switchedAt，不会把这次回滚又当成一次新切换。
    if (currentPatient.value) {
      currentPatientStore.syncPatient(toStorePatient(currentPatient.value))
    } else {
      currentPatientStore.syncClear()
    }
  }
  // 本次「带目标进入」已落地（选中了 or 判定为不在队列），复位，
  // 之后医生在页面里手动换人、叫号都不受这次进入的影响。
  enteredWithTarget = false
}

/**
 * 队列行 → 「当前患者」条所需的身份字段。
 *
 * 队列行的 `id` 是**队列号**（biz_queue.id），患者主档 id 在 `patientId` 上 —— 两者都是雪花 ID、
 * 长度一样，混用不会报类型错，只会拿队列号去查主档（查不到 → 500 或空白）。
 * 这个字段名差异踩过一次（见上），所以映射只留这一处，别在别处手写对象字面量。
 */
const toStorePatient = (row: any) => ({
  id: String(row.patientId),
  patientName: row.patientName,
  patientNo: row.patientNo,
  gender: row.gender,
  age: row.age,
})

const consumeSwitch = () => {
  const p = currentPatientStore.patient
  const ts = currentPatientStore.switchedAt
  if (!p || !ts || ts === handledSwitchAt) return
  handledSwitchAt = ts
  pendingSwitch = p
  if (queueLoaded.value) applyPendingSwitch()
}

watch(() => currentPatientStore.switchedAt, consumeSwitch)

const currentPatient = ref<any>(null)
const activeTab = ref('record')
const queueSearch = ref('')
const queueFilter = ref('all')
const userInfo = ref<any>({})

const stats = ref({waiting: 0, called: 0, inProgress: 0})

// 今日待办
const todoList = ref<any[]>([])
const todoStats = ref({pendingReview: 0, pendingReport: 0, pendingFollowUp: 0, pendingConsult: 0})
const todoSearch = ref('')

const filteredTodoList = computed(() => {
  if (!todoSearch.value) return todoList.value
  const kw = todoSearch.value.toLowerCase()
  return todoList.value.filter((item: any) =>
      item.title?.toLowerCase().includes(kw) || item.content?.toLowerCase().includes(kw)
  )
})

// 当前叫号患者（同一患者/多位患者可能有多条 status=3，用 pickQueueRow 稳定取「最早该接的那条」，
// 与列表排序同口径；直接 find 取的是后端序号最小的那条，不稳定）
const currentCalledPatient = computed(() => {
  return pickQueueRow(queueList.value.filter((q: any) => q.queueStatus === 3))
})

const waitingCount = computed(() => {
  return queueList.value.filter((q: any) => q.queueStatus === 2).length
})

/**
 * 叫号面板「下一位」预览：与后端 callNext 取号同口径
 * （IFNULL(triage_level,4) ASC, sequence_no ASC），医生点「接诊下一位」接到的就应该是这一条。
 * 排序写不一致的后果：面板预告 A、实际叫到 B，医生按面板理解现场。
 */
const nextWaiting = computed(() => {
  const waiting = queueList.value.filter((q: any) => q.queueStatus === 2)
  if (!waiting.length) return null
  return [...waiting].sort((a: any, b: any) => {
    const la = a.triageLevel ?? 4
    const lb = b.triageLevel ?? 4
    if (la !== lb) return la - lb
    return (a.sequenceNo || 0) - (b.sequenceNo || 0)
  })[0]
})

const formatArriveTime = (time: string) => {
  if (!time) return ''
  const d = new Date(time)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const calcWaitMinutes = (row: any) => {
  if (row.waitDuration != null) return row.waitDuration
  if (!row.arriveTime) return 0
  const arrive = new Date(row.arriveTime)
  const now = row.queueStatus === 3 && row.startTime ? new Date(row.startTime) : new Date()
  return Math.max(0, Math.floor((now.getTime() - arrive.getTime()) / 60000))
}

const waitDurationClass = (minutes: number) => {
  if (minutes >= 30) return 'text-red-500 font-medium'
  if (minutes >= 15) return 'text-amber-500'
  return 'text-slate-400'
}

// ========== 费用计算 ==========
const drugTotalAmount = computed(() => {
  return prescriptionList.value.reduce((sum: number, p: any) => {
    return sum + (p.details || []).reduce((s: number, d: any) => s + (d.amount || 0), 0)
  }, 0)
})
const inspectionTotalAmount = computed(() => {
  return inspectionRecords.value.reduce((sum: number, r: any) => sum + (r.price || 0), 0)
})
const laboratoryTotalAmount = computed(() => {
  return laboratoryRecords.value.reduce((sum: number, r: any) => sum + (r.price || 0), 0)
})
const totalBillAmount = computed(() => drugTotalAmount.value + inspectionTotalAmount.value + laboratoryTotalAmount.value)
const maxBillAmount = computed(() => Math.max(totalBillAmount.value, 1))
const drugPercentage = computed(() => Math.round((drugTotalAmount.value / maxBillAmount.value) * 100))
const inspectionPercentage = computed(() => Math.round((inspectionTotalAmount.value / maxBillAmount.value) * 100))
const laboratoryPercentage = computed(() => Math.round((laboratoryTotalAmount.value / maxBillAmount.value) * 100))

// 医保信息
const insuranceInfo = ref<any>(null)
const loadInsuranceInfo = async () => {
  if (!currentPatient.value) {
    insuranceInfo.value = null
    return
  }
  // 根据所有处方实际金额计算
  const drugTotal = prescriptionList.value.reduce((sum: number, p: any) => {
    return sum + (p.details || []).reduce((s: number, d: any) => s + (d.amount || 0), 0)
  }, 0)
  // 计算检查申请费用（批次E/E1：开单即落库，这里只算「已提交未缴费(applyStatus=1)」的金额，
  // 已缴费(2)的不重复计入，已取消(6)的不算）
  const inspectionTotal = inspectionRecords.value
      .filter((r: any) => r.applyStatus === 1)
      .reduce((sum: number, r: any) => sum + (r.price || 0), 0)
  // 计算检验申请费用（口径同检查）
  const laboratoryTotal = laboratoryRecords.value
      .filter((r: any) => r.applyStatus === 1)
      .reduce((sum: number, r: any) => sum + (r.price || 0), 0)
  try {
    const res = await estimateInsurance({
      settlementType: currentPatient.value.settlementType || 1,
      medicalInsuranceType: currentPatient.value.medicalInsuranceType || '',
      drugTotal: drugTotal,
      inspectionTotal: inspectionTotal,
      laboratoryTotal: laboratoryTotal,
    })
    insuranceInfo.value = res.data || null
  } catch (error) {
    console.error('加载医保信息失败:', error)
    insuranceInfo.value = null
  }
}

// 过滤后的队列
const filteredQueue = computed(() => {
  let list = queueList.value
  if (queueSearch.value) {
    const kw = queueSearch.value.toLowerCase()
    list = list.filter((q: any) =>
        q.patientName?.toLowerCase().includes(kw) ||
        q.registNo?.toLowerCase().includes(kw) ||
        q.queueNo?.toLowerCase().includes(kw)
    )
  }
  // 队列状态口径以 QueueStatusEnum 为准：2 候诊中 / 3 就诊中（不是挂号的 1 已挂号）
  if (queueFilter.value === 'waiting') list = list.filter((q: any) => q.queueStatus === 2)
  else if (queueFilter.value === 'consulting') list = list.filter((q: any) => q.queueStatus === 3)
  else if (queueFilter.value === 'emergency') list = list.filter((q: any) => q.registType === 3)

  // 排序与「该接哪一条」（pickQueueRow）共用 compareQueueRow，口径只有一处：
  // 就诊中 > 候诊中 > 已完成 > 已过号 > 已退号。
  // 两处各写一套的后果是「列表第一行排的是复诊，顶栏搜索却选中了初诊那条」。
  return [...list].sort(compareQueueRow)
})

// ========== 病历表单 ==========
const recordForm = reactive({
  id: null as number | null,
  recordNo: '',
  patientId: null as number | null,
  patientNo: '',
  patientName: '',
  gender: 0,
  age: 0,
  registId: null as number | null,
  registNo: '',
  visitDate: '',
  deptId: null as number | null,
  deptName: '',
  doctorId: null as number | null,
  doctorName: '',
  chiefComplaint: '',
  presentIllness: '',
  pastHistory: '',
  personalHistory: '',
  familyHistory: '',
  allergyHistory: '',
  temperature: '',
  pulse: '',
  respiration: '',
  systolicPressure: '',
  diastolicPressure: '',
  generalCondition: '',
  skinMucosa: '',
  headNeck: '',
  chestLung: '',
  heart: '',
  abdomen: '',
  spineLimbs: '',
  nervousSystem: '',
  specialistExam: '',
  auxiliaryExam: '',
  diagnosis: '',
  diagnosisCode: '',
  diagnosisName: '',
  treatmentPlan: '',
  guidePdfPath: '',
  recordStatus: 1,
  reviewStatus: 0,
})

// 体格检查分节折叠态：null=自动（任一项目有内容就展开），true/false=用户手动覆盖；切患者回自动
const EXAM_FIELDS = ['temperature', 'pulse', 'respiration', 'systolicPressure', 'diastolicPressure',
  'generalCondition', 'skinMucosa', 'headNeck', 'chestLung', 'heart', 'abdomen', 'spineLimbs', 'nervousSystem',
  'specialistExam'] as const
const examHasContent = computed(() => EXAM_FIELDS.some((k) => !!recordForm[k]))
const examToggled = ref<boolean | null>(null)
const examVisible = computed(() => examToggled.value ?? examHasContent.value)

// 复制上次查体：真实 HIS 复诊高频动作。只搬 EXAM_FIELDS（查体所见），
// 生命体征是当次实测数据、质控禁止拷贝，所以不在复制范围内。
const copyExamLoading = ref(false)
const handleCopyLastExam = async () => {
  const patientId = currentPatient.value?.patientId
  if (!patientId) return
  copyExamLoading.value = true
  try {
    const res = await getEmrRecordList({patientId})
    // 后端按 createTime 倒序；排除本次病历（同一次挂号产生的记录）
    const last = (res.data || []).find((r: any) =>
      String(r.registId) !== String(recordForm.registId ?? '')
      && String(r.id) !== String(recordForm.id ?? ''))
    if (!last) {
      ElMessage.info('该患者没有可复制的既往查体')
      return
    }
    const copied = EXAM_FIELDS.filter((k) => !['temperature', 'pulse', 'respiration', 'systolicPressure', 'diastolicPressure'].includes(k) && !!last[k])
    if (copied.length === 0) {
      ElMessage.info('上次病历未填写查体所见')
      return
    }
    copied.forEach((k) => { recordForm[k] = last[k] })
    examToggled.value = true
    ElMessage.success(`已复制 ${last.visitDate || '上次'} 的查体所见（${copied.length} 项），请核对修改`)
  } finally {
    copyExamLoading.value = false
  }
}

const inspectionRecords = ref<any[]>([])
const laboratoryRecords = ref<any[]>([])
// 四层改造：患者维度账单行快照（扁平列表，一个患者可能跨多张账单）
const patientChargeItems = ref<any[]>([])
const chargeCollapseActive = ref<string[]>(['chargeInfo', 'chargeDetails'])

// 按 itemType 聚合（药品类 = 西药/中成药/中药饮片 → [2,3,4]）
const getChargeDetailsByType = (type: number | number[]) => {
  if (!patientChargeItems.value?.length) return []
  const types = Array.isArray(type) ? type : [type]
  return patientChargeItems.value.filter((item: any) => types.includes(item.itemType))
}
const getTypeTotal = (type: number | number[]) => {
  return getChargeDetailsByType(type).reduce((sum: number, item: any) => sum + (item.amount || 0), 0)
}
// 患者收费汇总：总额 + 医保拆分（统筹/账户/自付）现算自用
const chargeSummary = computed(() => {
  const items = patientChargeItems.value || []
  return {
    total: items.reduce((s: number, i: any) => s + (i.amount || 0), 0),
    insurance: items.reduce((s: number, i: any) => s + (i.poolAmount || 0), 0),
    account: items.reduce((s: number, i: any) => s + (i.accountAmount || 0), 0),
    self: items.reduce((s: number, i: any) => s + (i.selfAmount || 0), 0),
  }
})

// ========== 处方表单 ==========
const prescriptionForm = reactive({
  patientId: null as number | null,
  patientNo: '',
  patientName: '',
  gender: 0,
  age: 0,
  registId: null as number | null,
  registNo: '',
  deptId: null as number | null,
  deptName: '',
  doctorId: null as number | null,
  doctorName: '',
  prescriptionType: 1,
  diagnosis: '',
  usageInstruction: '',
  // 中药饮片方专属，挂在处方头（一张方一个剂数，不是每味药一个）
  doseCount: 7,
  decoctFlag: null as number | null,
  details: [] as any[],
})

const newDrug = reactive({
  drugId: null as number | null,
  drugCode: '',
  drugName: '',
  genericName: '',
  specification: '',
  dosageForm: '',
  unit: '盒',
  quantity: 1,
  price: 0,
  usageDosage: '',
  frequency: '一日三次',
  route: '口服',
  duration: 7,
  singleDosage: '',
})

// ========== 检查申请表单 ==========
const inspectionForm = reactive({
  patientId: null as number | null,
  patientNo: '',
  patientName: '',
  gender: 0,
  age: 0,
  registId: null as number | null,
  registNo: '',
  deptId: null as number | null,
  deptName: '',
  doctorId: null as number | null,
  doctorName: '',
  inspectionItemId: null as number | null,
  inspectionItemName: '',
  bodyPart: '',
  inspectionPurpose: '',
  preparation: '',
  clinicalDiagnosis: '',
  isEmergency: 0,
})

// ========== 检验申请表单 ==========
const laboratoryForm = reactive({
  patientId: null as number | null,
  patientNo: '',
  patientName: '',
  gender: 0,
  age: 0,
  registId: null as number | null,
  registNo: '',
  deptId: null as number | null,
  deptName: '',
  doctorId: null as number | null,
  doctorName: '',
  laboratoryItemId: null as number | null,
  laboratoryItemName: '',
  specimenType: '血液',
  laboratoryPurpose: '',
  clinicalDiagnosis: '',
  isFasting: 0,
  isEmergency: 0,
})

// ========== 处方列表管理 ==========
const prescriptionList = ref<any[]>([])
const currentPrescriptionIdx = ref(0)
const currentPrescriptionType = ref(1)

const currentPrescription = computed(() => {
  return prescriptionList.value.find(p => p.prescriptionType === currentPrescriptionType.value) || null
})

const prescriptionTypeLabel = (type: number) => {
  const map: Record<number, string> = {1: '西药', 2: '中成药', 3: '中药饮片'}
  return map[type] || '未知'
}

// ========== 中药饮片：剂数在处方头、克数按「每剂克数 × 剂数」算 ==========
// 煎法与代煎/自煎都走字典（sql/139），字典里加一味「焦三仙」这类特殊脚注不需要改代码。
const tcmMethodDict = ref<any[]>([])
const tcmDecoctFlagDict = ref<any[]>([])
const loadTcmDicts = async () => {
  try {
    const res: any = await getDictDataMapList(`${DICT_TYPE.TCM_DECOCT_METHOD},${DICT_TYPE.TCM_DECOCT_FLAG}`)
    tcmMethodDict.value = res?.data?.[DICT_TYPE.TCM_DECOCT_METHOD] || []
    tcmDecoctFlagDict.value = res?.data?.[DICT_TYPE.TCM_DECOCT_FLAG] || []
  } catch (e) {
    console.error('加载中药煎法字典失败', e)
  }
}

/** 每剂克数：单剂剂量列只填数字（后端同样按数字解析，口径一致） */
const tcmPerDoseGrams = (item: any): number => {
  const n = Number(String(item?.singleDosage ?? '').trim())
  return Number.isFinite(n) && n > 0 ? n : 0
}
/** 实发总克数 = 每剂克数 × 剂数，也就是提交给后端的 quantity */
const tcmGramsOf = (item: any): number => {
  const g = tcmPerDoseGrams(item) * (Number(prescriptionForm.doseCount) || 0)
  return Math.round(g * 100) / 100
}
/** 饮片零售价是「元/档案单位(kg)」，按克开方要先除以换算率换成元/克 */
const tcmPerGramPrice = (drug: any): number => {
  const gpu = Number(drug?.gramPerUnit) || 0
  return gpu > 0 ? Number(drug.retailPrice || 0) / gpu : Number(drug?.retailPrice || 0)
}
/** 预估金额 = 行上单价（元/克）× 实发克数；真实金额以后端按 4 位单价重算为准 */
const tcmAmountOf = (item: any): number => {
  return Math.round((Number(item?.price) || 0) * tcmGramsOf(item) * 100) / 100
}
const applyTcmGrams = (item: any) => {
  item.unit = 'g'
  item.quantity = tcmGramsOf(item)
  item.amount = tcmAmountOf(item)
}
/** 把「当前类型那张方」灌进编辑态：明细 + 剂数 + 煎服方式（历史方没填过剂数就沿用 7 剂默认） */
const bindPrescriptionForm = () => {
  const p: any = currentPrescription.value
  prescriptionForm.details = p?.details || []
  prescriptionForm.doseCount = p?.doseCount > 0 ? p.doseCount : 7
  prescriptionForm.decoctFlag = p?.decoctFlag ?? null
}
// 剂数一改，整张方的克数与金额跟着重算（只是本地预估，真实金额后端按 4 位单价算）
watch(() => prescriptionForm.doseCount, () => {
  if (currentPrescriptionType.value !== 3) return
  prescriptionForm.details.forEach((item: any) => {
    if (item.drugId) applyTcmGrams(item)
  })
  loadInsuranceInfo()
})

const recordSaved = ref(false)
const icd10Results = ref<any[]>([])
const icd10Loading = ref(false)
// 按类型分组的药品数据
const drugResultsByType: Record<number, ref<any[]>> = {
  1: ref([]),  // 西药
  2: ref([]),  // 中成药
  3: ref([]),  // 中药饮片
}
const drugLoading = ref(false)
// 当前处方类型的药品列表
const drugResults = computed(() => {
  return drugResultsByType[currentPrescriptionType.value]?.value || []
})
const inspectionItemResults = ref<any[]>([])
const inspectionItemLoading = ref(false)
const laboratoryItemResults = ref<any[]>([])
const laboratoryItemLoading = ref(false)
let refreshTimer: ReturnType<typeof setInterval> | null = null

// ========== 结诊状态 ==========
const isVisitCompleted = computed(() => currentPatient.value?.queueStatus === 4)
const showGuideSheetDialog = ref(false)

// ========== 患者标签管理 ==========
const patientTags = ref<any[]>([])
const allTags = ref<any[]>([])
const showTagDialog = ref(false)
const tagSearchKeyword = ref('')

// ========== 个人模板管理 ==========
const showDiagTemplateDialog = ref(false)
const showRxTemplateDialog = ref(false)
const showPackageDialog = ref(false)
const showInspectionTemplateDialog = ref(false)
const showAddInspectionTemplateDialog = ref(false)
const showLaboratoryTemplateDialog = ref(false)
const showAddLaboratoryTemplateDialog = ref(false)
const myDiagTemplates = ref<any[]>([])
const myRxTemplates = ref<any[]>([])
const myPackages = ref<any[]>([])
const myInspectionTemplates = ref<any[]>([])
const myLaboratoryTemplates = ref<any[]>([])
const newTemplateName = ref('')
const diagSearchKeyword = ref('')
const diagSearchResults = ref<any[]>([])
const diagSearchLoading = ref(false)
const showAddPackageDialog = ref(false)
const newPackageName = ref('')

// 检查申请模板相关
const inspectionTemplateName = ref('')
const inspectionTemplateForm = ref({
  inspectionItemId: null as number | null,
  inspectionItemName: '',
  bodyPart: '',
  inspectionPurpose: '',
  isEmergency: 0
})
const inspectionTemplateItemResults = ref<any[]>([])
const inspectionTemplateItemLoading = ref(false)

// 检验申请模板相关
const laboratoryTemplateName = ref('')
const laboratoryTemplateForm = ref({
  laboratoryItemId: null as number | null,
  laboratoryItemName: '',
  sampleType: '',
  inspectionPurpose: '',
  isEmergency: 0
})
const laboratoryTemplateItemResults = ref<any[]>([])
const laboratoryTemplateItemLoading = ref(false)

const loadDiagTemplates = async () => {
  try {
    const res = await getDiagTemplates()
    myDiagTemplates.value = res.data || []
  } catch (e) {
    console.error('加载常用诊断失败', e)
  }
}

const handleDeleteDiagTemplate = async (idx: number) => {
  const item = myDiagTemplates.value[idx]
  try {
    await deleteDiagTemplate(item.id)
    myDiagTemplates.value.splice(idx, 1)
    ElMessage.success('常用诊断已删除')
  } catch (e: any) {
    ElMessage.error(e.message || '删除失败')
  }
}

const handleDiagSearchInDialog = async (query: string) => {
  if (!query) {
    diagSearchResults.value = [];
    return
  }
  diagSearchLoading.value = true
  try {
    const res = await searchIcd10(query)
    diagSearchResults.value = (res.data || []).filter((item: any) =>
        !myDiagTemplates.value.some((t: any) => t.icdCode === item.icdCode)
    )
  } catch (e) {
    console.error('搜索失败', e)
  } finally {
    diagSearchLoading.value = false
  }
}

const handleAddDiagFromDialog = async (item: any) => {
  const newList = [...myDiagTemplates.value, {icdCode: item.icdCode, icdName: item.icdName}]
  try {
    await saveDiagTemplates(newList)
    myDiagTemplates.value = newList
    diagSearchKeyword.value = ''
    diagSearchResults.value = []
    ElMessage.success('已添加')
  } catch (e: any) {
    ElMessage.error(e.message || '添加失败')
  }
}

const handleQuickSelectDiag = (tpl: any) => {
  recordForm.diagnosis = tpl.icdName
  recordForm.diagnosisCode = tpl.icdCode
  recordForm.diagnosisName = tpl.icdName
}

const handleAddDiagToTemplate = async () => {
  if (!recordForm.diagnosisCode || !recordForm.diagnosisName) {
    ElMessage.warning('请先选择诊断')
    return
  }
  const exists = myDiagTemplates.value.some((t: any) => t.icdCode === recordForm.diagnosisCode)
  if (exists) {
    ElMessage.info('该诊断已在常用列表中')
    return
  }
  try {
    const newList = [...myDiagTemplates.value, {icdCode: recordForm.diagnosisCode, icdName: recordForm.diagnosisName}]
    await saveDiagTemplates(newList)
    myDiagTemplates.value = newList
    ElMessage.success('已添加到常用诊断')
  } catch (e: any) {
    ElMessage.error(e.message || '添加失败')
  }
}

const handleSaveDiagTemplates = async () => {
  try {
    await saveDiagTemplates(myDiagTemplates.value)
    ElMessage.success('保存成功')
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  }
}

const loadRxTemplates = async () => {
  try {
    const res = await getRxTemplates()
    myRxTemplates.value = res.data || []
  } catch (e) {
    console.error('加载处方模板失败', e)
  }
}

const handleAddRxTemplate = async () => {
  if (!newTemplateName.value) {
    ElMessage.warning('请输入模板名称')
    return
  }
  if (prescriptionForm.details.length === 0) {
    ElMessage.warning('当前处方为空，请先添加药品')
    return
  }
  try {
    await saveRxTemplate({
      templateName: newTemplateName.value,
      details: [...prescriptionForm.details],
    })
    newTemplateName.value = ''
    ElMessage.success('模板保存成功')
    loadRxTemplates()
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  }
}

const handleApplyRxTemplate = async (tpl: any) => {
  try {
    const res = await getRxTemplateDetail(tpl.id)
    prescriptionForm.details = (res.data?.details || []).map((d: any) => ({...d}))
    loadInsuranceInfo()
    showRxTemplateDialog.value = false
    ElMessage.success(`已套用模板「${tpl.templateName}」`)
  } catch (e: any) {
    ElMessage.error(e.message || '套用失败')
  }
}

const handleApplyRxTemplateToRecord = async (record: any, tpl: any) => {
  try {
    const res = await getRxTemplateDetail(tpl.id)
    const details = res.data?.details || []
    if (details.length > 0) {
      const d = details[0]
      record.drugId = d.drugId
      record.drugCode = d.drugCode
      record.drugName = d.drugName
      record.specification = d.specification || ''
      record.unit = d.unit || '盒'
      record.singleDosage = d.singleDosage || ''
      record.frequency = d.frequency || ''
      record.route = d.route || ''
      record.quantity = d.quantity || 1
      record.duration = d.duration || 7
      record.price = d.price || 0
      record.amount = (d.quantity || 1) * (d.price || 0)
      // 饮片模板里的 quantity 未必对得上本张方的剂数，克数一律按「每剂克数 × 剂数」重算
      if (currentPrescriptionType.value === 3) applyTcmGrams(record)
    }
    ElMessage.success(`已套用模板「${tpl.templateName}」`)
  } catch (e: any) {
    ElMessage.error(e.message || '套用失败')
  }
}

const handleDeleteRxTemplate = async (idx: number) => {
  const item = myRxTemplates.value[idx]
  try {
    await deleteRxTemplate(item.id)
    myRxTemplates.value.splice(idx, 1)
    ElMessage.success('处方模板已删除')
  } catch (e: any) {
    ElMessage.error(e.message || '删除失败')
  }
}

const loadDrugPackages = async () => {
  try {
    const res = await getDrugPackages()
    myPackages.value = res.data || []
  } catch (e) {
    console.error('加载药品套餐失败', e)
  }
}

const handleApplyPackage = async (pkg: any) => {
  try {
    const res = await getDrugPackageDetail(pkg.id)
    const details = res.data?.details || []
    details.forEach((d: any) => {
      if (d.itemType === 1) {
        prescriptionForm.details.push({...d, drugId: d.itemId, drugName: d.itemName})
      } else if (d.itemType === 2) {
        inspectionForm.items.push({itemId: d.itemId, itemName: d.itemName, price: d.price})
      } else if (d.itemType === 3) {
        laboratoryForm.items.push({itemId: d.itemId, itemName: d.itemName, price: d.price})
      }
    })
    showPackageDialog.value = false
    ElMessage.success(`已套用套餐「${pkg.packageName}」`)
  } catch (e: any) {
    ElMessage.error(e.message || '套用失败')
  }
}

const handleDeletePackage = async (idx: number) => {
  const item = myPackages.value[idx]
  try {
    await deleteDrugPackage(item.id)
    myPackages.value.splice(idx, 1)
    ElMessage.success('套餐已删除')
  } catch (e: any) {
    ElMessage.error(e.message || '删除失败')
  }
}

const handleSaveNewPackage = async () => {
  if (!newPackageName.value) {
    ElMessage.warning('请输入套餐名称')
    return
  }
  const details: any[] = []
  // 药品
  prescriptionForm.details.forEach((d: any) => {
    details.push({
      itemType: 1, itemId: d.drugId, itemCode: d.drugCode, itemName: d.drugName,
      specification: d.specification, unit: d.unit, quantity: d.quantity,
      price: d.price, usageDosage: d.usageDosage, frequency: d.frequency,
      route: d.route, duration: d.duration
    })
  })
  // 检查
  inspectionForm.items?.forEach((d: any) => {
    details.push({
      itemType: 2, itemId: d.itemId, itemCode: d.itemCode || '', itemName: d.itemName,
      specification: '', unit: '', quantity: 1, price: d.price || 0
    })
  })
  // 检验
  laboratoryForm.items?.forEach((d: any) => {
    details.push({
      itemType: 3, itemId: d.itemId, itemCode: d.itemCode || '', itemName: d.itemName,
      specification: '', unit: '', quantity: 1, price: d.price || 0
    })
  })
  if (details.length === 0) {
    ElMessage.warning('当前没有可保存的项目，请先添加处方/检查/检验')
    return
  }
  try {
    await saveDrugPackage({packageName: newPackageName.value, details})
    newPackageName.value = ''
    showAddPackageDialog.value = false
    ElMessage.success('套餐保存成功')
    loadDrugPackages()
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  }
}

// ========== 检查申请模板 ==========
const loadInspectionTemplates = async () => {
  try {
    const res = await getInspectionTemplates()
    myInspectionTemplates.value = res.data || []
  } catch (e) {
    console.error('加载检查申请模板失败', e)
  }
}

const handleDeleteInspectionTemplate = async (idx: number) => {
  const item = myInspectionTemplates.value[idx]
  try {
    await deleteInspectionTemplate(item.id)
    myInspectionTemplates.value.splice(idx, 1)
    ElMessage.success('检查模板已删除')
  } catch (e: any) {
    ElMessage.error(e.message || '删除失败')
  }
}

const handleInspectionTemplateItemSearch = async (query: string) => {
  if (!query) {
    inspectionTemplateItemResults.value = []
    return
  }
  inspectionTemplateItemLoading.value = true
  try {
    const res = await searchInspectionItem(query)
    inspectionTemplateItemResults.value = res.data || []
  } catch (e) {
    console.error('搜索检查项目失败', e)
  } finally {
    inspectionTemplateItemLoading.value = false
  }
}

const handleInspectionTemplateItemSelect = (itemId: number) => {
  const item = inspectionTemplateItemResults.value.find((i: any) => i.id === itemId)
  if (item) {
    inspectionTemplateForm.value.inspectionItemId = item.id
    inspectionTemplateForm.value.inspectionItemName = item.itemName
  }
}

const handleSaveInspectionTemplate = async () => {
  if (!inspectionTemplateName.value) {
    ElMessage.warning('请输入模板名称')
    return
  }
  if (!inspectionTemplateForm.value.inspectionItemId) {
    ElMessage.warning('请选择检查项目')
    return
  }
  try {
    await saveInspectionTemplate({
      templateName: inspectionTemplateName.value,
      ...inspectionTemplateForm.value
    })
    inspectionTemplateName.value = ''
    inspectionTemplateForm.value = {
      inspectionItemId: null,
      inspectionItemName: '',
      bodyPart: '',
      inspectionPurpose: '',
      isEmergency: 0
    }
    ElMessage.success('模板保存成功')
    loadInspectionTemplates()
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  }
}

const handleApplyInspectionTemplate = (tpl: any) => {
  inspectionForm.inspectionItemId = tpl.inspectionItemId
  inspectionForm.bodyPart = tpl.bodyPart || ''
  inspectionForm.inspectionPurpose = tpl.inspectionPurpose || ''
  inspectionForm.isEmergency = tpl.isEmergency || 0
  showInspectionTemplateDialog.value = false
  ElMessage.success(`已套用模板「${tpl.templateName}」`)
}

const handleApplyInspectionTemplateToRecord = (record: any, tpl: any) => {
  record.inspectionItemId = tpl.inspectionItemId
  record.inspectionItemName = tpl.inspectionItemName || ''
  record.bodyPart = tpl.bodyPart || ''
  record.inspectionPurpose = tpl.inspectionPurpose || ''
  record.isEmergency = tpl.isEmergency || 0
  record.price = tpl.price || 0
  ElMessage.success(`已套用模板「${tpl.templateName}」`)
}

// ========== 检验申请模板 ==========
const loadLaboratoryTemplates = async () => {
  try {
    const res = await getLaboratoryTemplates()
    myLaboratoryTemplates.value = res.data || []
  } catch (e) {
    console.error('加载检验申请模板失败', e)
  }
}

const handleDeleteLaboratoryTemplate = async (idx: number) => {
  const item = myLaboratoryTemplates.value[idx]
  try {
    await deleteLaboratoryTemplate(item.id)
    myLaboratoryTemplates.value.splice(idx, 1)
    ElMessage.success('检验模板已删除')
  } catch (e: any) {
    ElMessage.error(e.message || '删除失败')
  }
}

const handleLaboratoryTemplateItemSearch = async (query: string) => {
  if (!query) {
    laboratoryTemplateItemResults.value = []
    return
  }
  laboratoryTemplateItemLoading.value = true
  try {
    const res = await searchLaboratoryItem(query)
    laboratoryTemplateItemResults.value = res.data || []
  } catch (e) {
    console.error('搜索检验项目失败', e)
  } finally {
    laboratoryTemplateItemLoading.value = false
  }
}

const handleLaboratoryTemplateItemSelect = (itemId: number) => {
  const item = laboratoryTemplateItemResults.value.find((i: any) => i.id === itemId)
  if (item) {
    laboratoryTemplateForm.value.laboratoryItemId = item.id
    laboratoryTemplateForm.value.laboratoryItemName = item.itemName
  }
}

const handleSaveLaboratoryTemplate = async () => {
  if (!laboratoryTemplateName.value) {
    ElMessage.warning('请输入模板名称')
    return
  }
  if (!laboratoryTemplateForm.value.laboratoryItemId) {
    ElMessage.warning('请选择检验项目')
    return
  }
  try {
    await saveLaboratoryTemplate({
      templateName: laboratoryTemplateName.value,
      ...laboratoryTemplateForm.value
    })
    laboratoryTemplateName.value = ''
    laboratoryTemplateForm.value = {
      laboratoryItemId: null,
      laboratoryItemName: '',
      sampleType: '',
      inspectionPurpose: '',
      isEmergency: 0
    }
    ElMessage.success('模板保存成功')
    loadLaboratoryTemplates()
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  }
}

const handleApplyLaboratoryTemplate = (tpl: any) => {
  laboratoryForm.laboratoryItemId = tpl.laboratoryItemId
  laboratoryForm.sampleType = tpl.sampleType || ''
  laboratoryForm.inspectionPurpose = tpl.inspectionPurpose || ''
  laboratoryForm.isEmergency = tpl.isEmergency || 0
  showLaboratoryTemplateDialog.value = false
  ElMessage.success(`已套用模板「${tpl.templateName}」`)
}

const filteredAllTags = computed(() => {
  if (!tagSearchKeyword.value) return allTags.value
  const kw = tagSearchKeyword.value.toLowerCase()
  return allTags.value.filter((tag: any) => tag.tagName?.toLowerCase().includes(kw))
})

const loadPatientTags = async () => {
  if (!currentPatient.value?.patientId) return
  try {
    const res = await getPatientTags({patientId: currentPatient.value.patientId})
    patientTags.value = res.data || []
  } catch (error) {
    console.error('加载患者标签失败:', error)
  }
}

const patientDetail = ref<any>(null)
const loadPatientDetail = async () => {
  if (!currentPatient.value?.patientId) return
  try {
    const res = await getPatientDetail(currentPatient.value.patientId)
    patientDetail.value = res.data || null
  } catch (error) {
    console.error('加载患者详情失败:', error)
  }
}

const loadPatientChargeInfo = async () => {
  if (!currentPatient.value?.patientId) return
  try {
    const res = await listItemsByPatient(currentPatient.value.patientId)
    patientChargeItems.value = res.data || []
  } catch (error) {
    console.error('加载收费信息失败:', error)
    patientChargeItems.value = []
  }
}

const copyToClipboard = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}

const loadAllTags = async () => {
  try {
    const res = await getPatientTagListAll({})
    allTags.value = res.data?.records || res.data || []
  } catch (error) {
    console.error('加载标签列表失败:', error)
  }
}

const handleOpenTagDialog = async () => {
  await loadAllTags()
  await loadPatientTags()
  showTagDialog.value = true
}

const handleAddTag = async (tagId: number) => {
  if (!currentPatient.value?.patientId) return
  try {
    await addPatientTag({
      patientId: currentPatient.value.patientId,
      tagId: tagId,
      sourceType: 1
    })
    await loadPatientTags()
    ElMessage.success('标签添加成功')
  } catch (error: any) {
    ElMessage.error(error.message || '添加失败')
  }
}

const handleRemoveTag = async (tagId: number) => {
  if (!currentPatient.value?.patientId) return
  try {
    await removePatientTag({
      patientId: currentPatient.value.patientId,
      tagId: tagId
    })
    await loadPatientTags()
    ElMessage.success('标签已移除')
  } catch (error: any) {
    ElMessage.error(error.message || '移除失败')
  }
}

const isTagAdded = (tagId: number) => {
  return patientTags.value.some((t: any) => t.tagId === tagId)
}

// ========== 数据加载 ==========
let isFirstLoad = true

const loadData = async () => {
  loading.value = true
  /** 本次队列加载是否失败：失败时不能拿空队列去判定「这人不在队列」（会误伤自己的患者） */
  let queueLoadFailed = false
  try {
    const today = localDateStr()
    const [listRes, statsRes] = await Promise.all([
      getTodayQueueList({date: today}),
      getQueueStats(),
    ])

    queueList.value = listRes.data || []
    stats.value = statsRes.data || {waiting: 0, called: 0, inProgress: 0}

    // 初次加载时，自动选中当前就诊中的患者。
    // 两种情况下不自动选：
    //   ① 已经有选中患者（页面自己已经定了接诊对象）；
    //   ② 本次进入是「为某个特定患者而来」（顶部搜索跳转 / 刷新恢复 / URL 带 patientId）——
    //      医生是来找那个人的，先自动选中队列里的另一个人，等于把一次「找人」变成了
    //      「切换到别人」，而开单、开药、写病历挂的都是当前患者，看错人就是开错单。
    //      （准入判定见 lib/todayQueue.js，正常路径下这种人根本不会到这里。）
    // isFirstLoad 必须**无条件**置 false：原来这行写在 if 内部，只要首屏时已有选中患者
    // 它就永远不复位，之后 30 秒一次的定时刷新会突然把医生正在看的患者换掉。
    if (isFirstLoad) {
      if (!currentPatient.value && !enteredWithTarget) {
        // 用 pickQueueRow 而不是 find：同一位患者可能有多条 queueStatus=3 的队列记录
        // （数据里就存在），find 取的是后端序号最小的那条，不稳定也不一定是最早到的那次
        const consultingPatient = pickQueueRow(queueList.value.filter((q: any) => q.queueStatus === 3))
        if (consultingPatient) {
          selectPatient(consultingPatient)
        }
      }
      isFirstLoad = false
    }

    // 如果有当前叫号患者且在新列表中找不到，清空选中
    if (currentPatient.value) {
      const stillExists = queueList.value.find((q: any) => q.id === currentPatient.value.id)
      if (!stillExists) {
        currentPatient.value = null
        // 顶部提示条同步清掉：条子说「当前患者是A」而页面已经没选中任何人，
        // 在防开错人的场景里比不显示更危险
        currentPatientStore.syncClear()
      }
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    queueLoadFailed = true
  } finally {
    loading.value = false
    // 队列第一次加载结束后，把挂起的待切换患者落地（在队列里→选中，不在→提示并转详情框）。
    // 放在 finally 是为了任何情况下都不会把患者永久挂起。
    if (!queueLoaded.value) {
      queueLoaded.value = true
      if (queueLoadFailed) {
        // 队列没拉到就说「这人不在您今日队列」是假话——医生搜自己的患者也会被弹档。
        // 所以这里只清挂起状态、不判定、不提示，并把顶栏那条也清掉：
        // 顶栏显示「当前患者是 A」而页面谁都没选中，在防开错人的场景里比不显示更危险。
        pendingSwitch = null
        enteredWithTarget = false
        if (currentPatientStore.patient) {
          currentPatientStore.syncClear()
        }
        ElMessage.warning('候诊队列加载失败，未切换接诊患者，请刷新重试')
      } else {
        applyPendingSwitch()
      }
    }
  }
}

const loadDepartments = async () => {
  try {
    // 不传 scope → 默认按当前人过滤（医生站只看自己被授权的科室）
    const res = await getDepartmentSelectList({deptType: 1})
    departments.value = res.data || []
  } catch (error) {
    console.error('加载科室失败:', error)
  }
}

const loadUserInfo = () => {
  userInfo.value = {
    userId: localStorage.getItem('userId') || '',
    deptId: localStorage.getItem('deptId') || null,
    deptName: localStorage.getItem('deptName') || '',
    currentRole: localStorage.getItem('currentRole') || '',
  }
  if (userInfo.value.deptId) {
    selectedDeptId.value = Number(userInfo.value.deptId)
  }
}

// ========== 加载今日待办 ==========
const loadTodoList = async () => {
  // 模拟待办数据，实际应从后端获取
  todoList.value = [
    {id: 1, type: 'review', title: '待审核病历', content: '张三的病历待审核', time: '10:30', urgent: false},
    {id: 2, type: 'report', title: '待查看检验报告', content: '李四的血常规报告已出', time: '09:45', urgent: true},
    {id: 3, type: 'report', title: '待查看检查报告', content: '王五的CT报告已出', time: '09:20', urgent: false},
    {id: 4, type: 'followUp', title: '待回访患者', content: '赵六术后3天需电话回访', time: '14:00', urgent: false},
    {id: 5, type: 'consult', title: '会诊申请', content: '内科申请联合会诊', time: '11:00', urgent: true},
  ]
  todoStats.value = {
    pendingReview: 3,
    pendingReport: 5,
    pendingFollowUp: 2,
    pendingConsult: 1,
  }
}

// ========== ICD-10搜索 ==========
const handleIcd10Search = async (query: string) => {
  if (!query) {
    icd10Results.value = [];
    return
  }
  icd10Loading.value = true
  try {
    const res = await searchIcd10(query)
    icd10Results.value = res.data || []
  } catch (error) {
    console.error('搜索ICD-10失败:', error)
  } finally {
    icd10Loading.value = false
  }
}

const handleIcd10Select = (val: string) => {
  const item = icd10Results.value.find((i: any) => i.icdName === val)
  if (item) {
    recordForm.diagnosisCode = item.icdCode
    recordForm.diagnosisName = item.icdName
  }
}

// ========== ICD-10智能预测 ==========
const icdPredictions = ref<any[]>([])
const icdPredictionLoading = ref(false)
const showPrediction = ref(false)

const handlePredictIcd = async () => {
  if (!recordForm.chiefComplaint && !recordForm.presentIllness && !recordForm.specialistExam) {
    ElMessage.warning('请先填写主诉、现病史或专科检查')
    return
  }
  icdPredictionLoading.value = true
  showPrediction.value = true
  try {
    const res = await predictIcd10({
      chiefComplaint: recordForm.chiefComplaint || '',
      presentIllness: recordForm.presentIllness || '',
      specialistExam: recordForm.specialistExam || '',
      diagnosis: recordForm.diagnosis || '',
    })
    icdPredictions.value = res.data || []
  } catch (error) {
    console.error('ICD预测失败:', error)
    icdPredictions.value = []
  } finally {
    icdPredictionLoading.value = false
  }
}

const handleSelectPrediction = (item: any) => {
  recordForm.diagnosisCode = item.icdCode
  recordForm.diagnosisName = item.icdName
  showPrediction.value = false
  ElMessage.success(`已选择: ${item.icdCode} ${item.icdName}`)
}

// ========== 病历文本智能录入 / 草拟（P1-3）==========
// 铁律：两个接口都不写库。抽取结果是候选值、草拟结果是草稿，
// 都必须由医生点「填入」才进表单，保存病历时才由保存流程落库。
const extractDialogVisible = ref(false)
const extractRawText = ref('')
const extractLoading = ref(false)
const extractResult = ref<any>(null)
const extractAppliedFields = ref<string[]>([])

const openExtractDialog = () => {
  extractRawText.value = ''
  extractResult.value = null
  extractAppliedFields.value = []
  extractDialogVisible.value = true
}

const runExtract = async () => {
  if (!extractRawText.value.trim()) {
    ElMessage.warning('请先粘贴或输入要解析的文本')
    return
  }
  extractLoading.value = true
  extractResult.value = null
  extractAppliedFields.value = []
  try {
    const res = await extractEmrText({
      rawText: extractRawText.value,
      recordId: recordForm.id || undefined,
      gender: recordForm.gender || undefined,
      age: recordForm.age || undefined,
    })
    extractResult.value = res.data || null
    if (!extractResult.value?.fields?.length) {
      ElMessage.warning('没有从这段文本中识别出可搬运的内容')
    }
  } catch (error) {
    console.error('病历文本抽取失败:', error)
    extractResult.value = null
  } finally {
    extractLoading.value = false
  }
}

// 只有病历表单里真实存在的字段才允许写入，后端白名单挡一道、前端再挡一道
const canApplyField = (key: string) => !!key && Object.prototype.hasOwnProperty.call(recordForm, key)

const applyExtractField = (row: any) => {
  if (!canApplyField(row?.field)) {
    ElMessage.warning(`「${row?.fieldLabel || row?.field}」无法写入病历表单`)
    return
  }
  ;(recordForm as any)[row.field] = row.value
  if (!extractAppliedFields.value.includes(row.field)) {
    extractAppliedFields.value.push(row.field)
  }
  ElMessage.success(`已填入「${row.fieldLabel}」`)
}

const applyAllExtractFields = () => {
  const rows = extractResult.value?.fields || []
  let count = 0
  rows.forEach((row: any) => {
    if (!canApplyField(row?.field)) return
    ;(recordForm as any)[row.field] = row.value
    if (!extractAppliedFields.value.includes(row.field)) {
      extractAppliedFields.value.push(row.field)
    }
    count++
  })
  if (count === 0) {
    ElMessage.warning('没有可填入的字段')
    return
  }
  ElMessage.success(`已填入 ${count} 个字段，请逐项核对原文后再保存`)
}

const draftDialogVisible = ref(false)
const draftLoading = ref(false)
const draftResult = ref<any>(null)

const runDraft = async () => {
  if (!recordForm.chiefComplaint) {
    ElMessage.warning('请先填写主诉，草拟现病史必须以主诉为依据')
    return
  }
  draftLoading.value = true
  draftResult.value = null
  draftDialogVisible.value = true
  try {
    const res = await draftEmrText({
      recordId: recordForm.id || undefined,
      gender: recordForm.gender || undefined,
      age: recordForm.age || undefined,
      chiefComplaint: recordForm.chiefComplaint || '',
      presentIllness: recordForm.presentIllness || '',
      pastHistory: recordForm.pastHistory || '',
      allergyHistory: recordForm.allergyHistory || '',
      temperature: recordForm.temperature || '',
      pulse: recordForm.pulse || '',
      respiration: recordForm.respiration || '',
      systolicPressure: recordForm.systolicPressure || '',
      diastolicPressure: recordForm.diastolicPressure || '',
      generalCondition: recordForm.generalCondition || '',
      skinMucosa: recordForm.skinMucosa || '',
      headNeck: recordForm.headNeck || '',
      chestLung: recordForm.chestLung || '',
      heart: recordForm.heart || '',
      abdomen: recordForm.abdomen || '',
      spineLimbs: recordForm.spineLimbs || '',
      nervousSystem: recordForm.nervousSystem || '',
      specialistExam: recordForm.specialistExam || '',
      auxiliaryExam: recordForm.auxiliaryExam || '',
    })
    draftResult.value = res.data || null
  } catch (error) {
    console.error('病历草拟失败:', error)
    draftResult.value = null
  } finally {
    draftLoading.value = false
  }
}

const applyDraft = () => {
  const text = draftResult.value?.presentIllness
  if (!text) {
    ElMessage.warning('本次没有生成草稿')
    return
  }
  recordForm.presentIllness = text
  draftDialogVisible.value = false
  ElMessage.success('草稿已填入现病史，请逐字核对后修改')
}

// ========== AI辅助诊疗 ==========
const aiDiagnosisResults = ref<any[]>([])
const aiDiagnosisLoading = ref(false)
// 模型降级标记：degraded=true 表示结果是码表规则匹配出来的，必须如实告知，不得当模型推荐展示
const aiDiagnosisDegraded = ref(false)
const aiDiagnosisDegradeReason = ref('')
const aiGuideDialogVisible = ref(false)
const aiGuideData = ref<any>(null)

const handleAiDiagnosis = async () => {
  if (!recordForm.chiefComplaint && !recordForm.presentIllness && !recordForm.specialistExam) {
    return
  }
  aiDiagnosisLoading.value = true
  try {
    const res = await predictIcd10({
      chiefComplaint: recordForm.chiefComplaint || '',
      presentIllness: recordForm.presentIllness || '',
      specialistExam: recordForm.specialistExam || '',
      diagnosis: recordForm.diagnosis || '',
    })
    // 后端返回 Result<Icd10PredictVO>：候选在 predictions 里，不是顶层数组
    const payload: any = res.data || {}
    // 置信度只认后端返回值（模型或规则给出的分值）。后端没返回就不显示，
    // 严禁前端按数组下标编造百分比 —— 医生会当真，这是安全问题不是展示问题。
    aiDiagnosisResults.value = (payload.predictions || []).slice(0, 5).map((item: any) => ({
      ...item,
      confidence: typeof item.confidence === 'number' ? item.confidence : null
    }))
    aiDiagnosisDegraded.value = payload.degraded === true
    aiDiagnosisDegradeReason.value = payload.degradeReason || ''
  } catch (error) {
    console.error('AI诊断推荐失败:', error)
    aiDiagnosisResults.value = []
    aiDiagnosisDegraded.value = false
    aiDiagnosisDegradeReason.value = ''
  } finally {
    aiDiagnosisLoading.value = false
  }
}

const handleAdoptAiDiagnosis = (item: any) => {
  recordForm.diagnosis = item.icdName
  recordForm.diagnosisCode = item.icdCode
  recordForm.diagnosisName = item.icdName
  ElMessage.success(`已采纳诊断: ${item.icdName}`)
}

const handleViewAiGuide = (item: any) => {
  aiGuideData.value = item
  aiGuideDialogVisible.value = true
}

// 监听主诉/现病史变化，自动触发AI推荐
let aiDebounceTimer: ReturnType<typeof setTimeout> | null = null
const triggerAiDiagnosis = () => {
  if (aiDebounceTimer) clearTimeout(aiDebounceTimer)
  aiDebounceTimer = setTimeout(() => {
    if (recordForm.chiefComplaint || recordForm.presentIllness) {
      handleAiDiagnosis()
    }
  }, 1500)
}

// ========== CDSS用药安全审查 ==========
const cdssAlerts = ref<any[]>([])

const checkDrugSafety = () => {
  cdssAlerts.value = []
  // 上一版在这里写死了三条药物相互作用 + 一条剂量提醒，并在界面上渲染成「用药安全审查 · 警告 N」。
  // 但处方要到结诊提交（saveMedicalRecord）才落库、才有 prescriptionId，
  // 而审核接口 POST /ai/drugAudit/execute 必须按 prescriptionId 由服务端回查明细
  //（这样设计正是为了防止「前端传什么就审什么」），所以开方过程中前端无从审查。
  // 假结论已删除：这里不再产出任何审查结果，只保留调用点占位。
}

// ========== 药品搜索 ==========
const handleDrugSearch = async (query: string) => {
  if (!query) {
    drugResults.value = [];
    return
  }
  drugLoading.value = true
  try {
    const res = await getStockList({drugName: query, pageNum: 1, pageSize: 20})
    drugResults.value = res.data?.records || []
  } catch (error) {
    console.error('搜索药品失败:', error)
  } finally {
    drugLoading.value = false
  }
}

const handleDrugSelect = (val: number) => {
  const drug = drugResults.value.find((d: any) => d.id === val)
  if (drug) {
    newDrug.drugName = drug.drugName
    newDrug.specification = drug.specification || ''
    newDrug.drugId = drug.id
    newDrug.drugCode = drug.drugCode
    newDrug.unit = drug.unit || '盒'
    newDrug.price = drug.retailPrice || 0
  }
}

// ========== 检查项目搜索 ==========
const handleInspectionItemSearch = async (query: string) => {
  if (!query) {
    inspectionItemResults.value = []
    return
  }
  inspectionItemLoading.value = true
  try {
    const res = await searchInspectionItem(query)
    inspectionItemResults.value = res.data || []
  } catch (error) {
    console.error('搜索检查项目失败:', error)
  } finally {
    inspectionItemLoading.value = false
  }
}

/**
 * 检查项目选中 → **立即落库**（批次E/E1）。
 *
 * 原先只是把项目塞进本地数组，等结诊时随病历一起提交：医生开了单让患者去缴费，
 * 只要没点「保存病历」，收费台就查不到这张申请单；而病历每次保存又是"先删后增"申请单，
 * 已缴费的单子会被连根删掉。现在开单即写库，病历只负责回填 recordId。
 */
const handleInspectionItemSelectForRecord = async (record: any, val: number) => {
  const item = inspectionItemResults.value.find((i: any) => i.id === val)
  if (!item) return
  record.inspectionItemId = item.id
  record.inspectionItemName = item.itemName
  record.bodyPart = item.bodyPart || ''
  record.preparation = item.preparation || ''
  record.price = item.price || 0
  await persistInspectionRow(record)
}

/** 把一条检查申请落库（新增或更新）；成功后用后端返回的 VO 覆盖本地行（含执行状态）。 */
const persistInspectionRow = async (record: any) => {
  if (!currentPatient.value || !record.inspectionItemId) return false
  try {
    const res = await saveInspectionApply({
      id: record.id || undefined,
      registId: currentPatient.value.registId,
      recordId: recordForm.id || undefined,
      patientId: currentPatient.value.patientId,
      inspectionItemId: record.inspectionItemId,
      bodyPart: record.bodyPart,
      inspectionPurpose: record.inspectionPurpose,
      clinicalDiagnosis: record.clinicalDiagnosis || recordForm.diagnosis || '',
      specialRequirements: record.preparation,
      isEmergency: record.isEmergency ?? 0,
    })
    const pendingIdx = record._pendingIdx
    Object.assign(record, res.data || {})
    if (pendingIdx) record._pendingIdx = pendingIdx
    loadInsuranceInfo()
    return true
  } catch (error: any) {
    ElMessage.error(error.message || '检查开单失败')
    return false
  }
}

/** 编辑过科目/部位/目的之后手动落库（列表行上的「保存修改」）。 */
const handleSaveInspectionRow = async (record: any) => {
  const isNew = !record.id
  const ok = await persistInspectionRow(record)
  if (ok && isNew) ElMessage.success('检查申请已开单（待缴费）')
  else if (ok) ElMessage.success('检查申请已更新')
}

const handleInspectionItemSelect = (val: number) => {
  const item = inspectionItemResults.value.find((i: any) => i.id === val)
  if (item) {
    inspectionForm.inspectionItemId = item.id
    inspectionForm.inspectionItemName = item.itemName
    inspectionForm.bodyPart = item.bodyPart || ''
    inspectionForm.preparation = item.preparation || ''
  }
}

// ========== 检验项目搜索 ==========
const handleLaboratoryItemSearch = async (query: string) => {
  if (!query) {
    laboratoryItemResults.value = []
    return
  }
  laboratoryItemLoading.value = true
  try {
    const res = await searchLaboratoryItem(query)
    laboratoryItemResults.value = res.data || []
  } catch (error) {
    console.error('搜索检验项目失败:', error)
  } finally {
    laboratoryItemLoading.value = false
  }
}

const handleLaboratoryItemSelect = (val: number) => {
  const item = laboratoryItemResults.value.find((i: any) => i.id === val)
  if (item) {
    laboratoryForm.laboratoryItemId = item.id
    laboratoryForm.laboratoryItemName = item.itemName
    laboratoryForm.specimenType = item.specimenType || '血液'
  }
}

/** 检验项目选中 → 立即落库（批次E/E1），语义同检查。 */
const handleLaboratoryItemSelectForRecord = async (record: any, val: number) => {
  const item = laboratoryItemResults.value.find((i: any) => i.id === val)
  if (!item) return
  record.laboratoryItemId = item.id
  record.laboratoryItemName = item.itemName
  record.specimenType = item.specimenType || '血液'
  record.price = item.price || 0
  await persistLaboratoryRow(record)
}

const persistLaboratoryRow = async (record: any) => {
  if (!currentPatient.value || !record.laboratoryItemId) return false
  try {
    const res = await saveLaboratoryApply({
      id: record.id || undefined,
      registId: currentPatient.value.registId,
      recordId: recordForm.id || undefined,
      patientId: currentPatient.value.patientId,
      laboratoryItemId: record.laboratoryItemId,
      specimenType: record.specimenType,
      laboratoryPurpose: record.laboratoryPurpose,
      clinicalDiagnosis: record.clinicalDiagnosis || recordForm.diagnosis || '',
      isFasting: record.isFasting ?? 0,
      isEmergency: record.isEmergency ?? 0,
    })
    const pendingIdx = record._pendingIdx
    Object.assign(record, res.data || {})
    if (pendingIdx) record._pendingIdx = pendingIdx
    loadInsuranceInfo()
    return true
  } catch (error: any) {
    ElMessage.error(error.message || '检验开单失败')
    return false
  }
}

const handleSaveLaboratoryRow = async (record: any) => {
  const isNew = !record.id
  const ok = await persistLaboratoryRow(record)
  if (ok && isNew) ElMessage.success('检验申请已开单（待缴费）')
  else if (ok) ElMessage.success('检验申请已更新')
}

const handleApplyLaboratoryTemplateToRecord = (record: any, tpl: any) => {
  record.laboratoryItemId = tpl.laboratoryItemId
  record.laboratoryItemName = tpl.laboratoryItemName || ''
  record.specimenType = tpl.specimenType || ''
  record.laboratoryPurpose = tpl.laboratoryPurpose || ''
  record.isEmergency = tpl.isEmergency || 0
  record.price = tpl.price || 0
  ElMessage.success(`已套用模板「${tpl.templateName}」`)
}

// ========== 选中患者 ==========
const selectPatient = async (row: any) => {
  currentPatient.value = row
  // 同步顶部「当前患者」条（映射口径见 toStorePatient：队列行的 id 是队列号，不能当患者 id 用）。
  // 用 syncPatient 而不是 setPatient，避免改 switchedAt 后与本函数的 watch 形成回环。
  // 注意：队列行不带过敏史，条子就不会亮「过敏」标（不假装无过敏），完整信息点条子看档案。
  if (row?.patientId) {
    currentPatientStore.syncPatient(toStorePatient(row))
  }
  activeTab.value = 'record'
  recordSaved.value = false
  // 清空历史数据（批次F：收费/详情/标签也要清，否则新患者加载期间看到的是上一位患者的钱和信息）
  inspectionRecords.value = []
  laboratoryRecords.value = []
  prescriptionList.value = []
  currentPrescriptionIdx.value = 0
  patientChargeItems.value = []
  patientDetail.value = null
  patientTags.value = []
  insuranceInfo.value = null

  // 重置所有表单
  const baseInfo = {
    patientId: row.patientId, patientNo: row.patientNo, patientName: row.patientName,
    registId: row.registId, registNo: row.registNo || '',
    deptId: row.deptId, deptName: row.deptName, doctorId: row.doctorId, doctorName: row.doctorName,
    gender: row.gender, age: row.age,
    visitDate: localDateStr(),
  }
  Object.assign(recordForm, baseInfo, {
    id: null,
    recordNo: '',
    recordStatus: 1,
    reviewStatus: 0,
    chiefComplaint: '',
    presentIllness: '',
    pastHistory: '',
    personalHistory: '',
    familyHistory: '',
    allergyHistory: '',
    temperature: '',
    pulse: '',
    respiration: '',
    systolicPressure: '',
    diastolicPressure: '',
    generalCondition: '',
    skinMucosa: '',
    headNeck: '',
    chestLung: '',
    heart: '',
    abdomen: '',
    spineLimbs: '',
    nervousSystem: '',
    specialistExam: '',
    auxiliaryExam: '',
    diagnosis: '',
    diagnosisCode: '',
    diagnosisName: '',
    treatmentPlan: ''
  })
  Object.assign(prescriptionForm, baseInfo, {prescriptionType: 1, diagnosis: '', usageInstruction: '', details: []})
  Object.assign(inspectionForm, baseInfo, {
    inspectionItemId: null,
    inspectionItemName: '',
    bodyPart: '',
    inspectionPurpose: '',
    clinicalDiagnosis: '',
    isEmergency: 0
  })
  Object.assign(laboratoryForm, baseInfo, {
    laboratoryItemId: null,
    laboratoryItemName: '',
    specimenType: '血液',
    laboratoryPurpose: '',
    clinicalDiagnosis: '',
    isFasting: 0,
    isEmergency: 0
  })

  const params = {patientId: row.patientId, registId: row.registId};

  // 尝试加载已有的病历
  const res = await getByRegistId(params)
  Object.assign(recordForm, res.data)
  examToggled.value = null

  // 四个区块并发加载，统一挂「加载中」标记（各 loader 自己吞异常）
  // （原「既往病历」loader 已随左栏既往页签移除 —— 历史就诊改由患者条上的入口
  //   打开患者详情弹窗，内嵌 CDR 全景时间轴，取数口径也换成患者级的 getPatientCdr）

  // 加载检查申请记录
  trackPatientLoad(loadInspectionRecords(params))

  // 加载检验申请记录
  trackPatientLoad(loadLaboratoryRecords(params))

  // 加载已有处方
  trackPatientLoad(loadPrescriptionRecords(params))

  // 加载患者标签
  loadPatientTags()

  // 加载患者详情
  loadPatientDetail()

  // 加载收费信息
  trackPatientLoad(loadPatientChargeInfo())

  // 加载医保信息
  loadInsuranceInfo()
}

const loadInspectionRecords = async (params: any) => {
  try {
    // 获取申请列表用于显示状态
    const applyRes = await getInspectionApplyList(params)
    inspectionRecords.value = applyRes.data || []
  } catch (error) {
    inspectionRecords.value = []
  }
}

const loadLaboratoryRecords = async (params: any) => {
  try {
    // 获取申请列表用于显示状态
    const applyRes = await getLaboratoryApplyList(params)
    laboratoryRecords.value = applyRes.data || []
  } catch (error) {
    laboratoryRecords.value = []
  }
}

const loadPrescriptionRecords = async (params: any) => {
  try {
    const res = await getPrescriptionList(params)
    prescriptionList.value = res.data || []
    // 保持当前选中索引不越界
    if (currentPrescriptionIdx.value >= prescriptionList.value.length) {
      currentPrescriptionIdx.value = 0
    }
    bindPrescriptionForm()
    loadInsuranceInfo()
    // L7 审方退回重开闭环：被退回的处方要在医生选中患者第一时间可见（原因+次数），
    // 医生改方后重新保存病历即完成重提（后端先删后增继承退回次数并落重提流水）
    const returnedList = (res.data || []).filter((p: any) => p.prescriptionStatus === 7)
    if (returnedList.length) {
      const first = returnedList[0]
      ElMessage.warning({
        message: `有 ${returnedList.length} 张处方被审方退回（${first.returnCount > 1 ? `第 ${first.returnCount} 次` : '首次'}）：${first.returnReason || '未填写原因'}。请修改处方后重新保存病历提交。`,
        duration: 8000,
        showClose: true,
      })
    }
  } catch (error) {
    prescriptionList.value = []
    prescriptionForm.details = []
  }
}

const switchPrescription = () => {
  bindPrescriptionForm()
  loadInsuranceInfo()
}

watch(currentPrescriptionType, (_, oldType) => {
  // 校验旧类型的处方
  const errors = validatePrescription(oldType)
  if (errors.length > 0) {
    ElMessage.warning(`${prescriptionTypeLabel(oldType)}处方：${errors[0]}`)
  }
  // 保存旧类型的处方数据（剂数/煎服方式同在处方头，切走前必须回写，否则切回来就丢了）
  const oldPrescription = prescriptionList.value.find(p => p.prescriptionType === oldType)
  if (oldPrescription) {
    oldPrescription.details = [...prescriptionForm.details]
    oldPrescription.doseCount = prescriptionForm.doseCount
    oldPrescription.decoctFlag = prescriptionForm.decoctFlag
  }
  // 加载新类型的处方数据
  bindPrescriptionForm()
})

const hasPrescriptionType = (type: number) => {
  return prescriptionList.value.some((p: any) => p.prescriptionType === type)
}

const handleAddPrescription = (type: number) => {
  if (hasPrescriptionType(type)) {
    ElMessage.warning(`${prescriptionTypeLabel(type)}处方已存在`)
    return
  }
  const newPrescription = {
    prescriptionType: type,
    diagnosis: '',
    usageInstruction: '',
    details: [],
  }
  prescriptionList.value.push(newPrescription)
  currentPrescriptionIdx.value = prescriptionList.value.length - 1
  prescriptionForm.details = newPrescription.details
  ElMessage.success(`已创建${prescriptionTypeLabel(type)}处方`)
}

// ========== 叫下一位 ==========
const handleCallNext = async () => {
  // 如果有当前就诊患者，先检查是否需要保存
  if (currentCalledPatient.value) {
    // 批次E/E1：检查检验已开单即落库，不再随病历提交；
    // 这里只兜「处方明细」和「还没落库的空行」——后者是本地 UI 状态，一并触发保存不会丢数据。
    const hasUnsavedData = prescriptionForm.details.length > 0
        || inspectionRecords.value.some(r => !r.id)
        || laboratoryRecords.value.some(r => !r.id)
    // 有未保存的数据且病历未提交/审核通过时，自动保存
    if (hasUnsavedData && !recordSaved.value) {
      try {
        await handleSaveRecord()
        ElMessage.success('已自动保存当前病历')
      } catch (e) {
        // 保存失败不阻断呼叫
      }
    }
  }

  // 呼叫下一位
  try {
    await ElMessageBox.confirm('确认接诊下一位候诊患者？', '接诊确认',
        {confirmButtonText: '确认接诊', cancelButtonText: '取消', type: 'info'}
    )
  } catch (error) {
    return
  }

  // 呼叫下一位
  try {
    // 接诊对象一律以服务端回执为准，前端不猜。
    // 原先的写法是「叫完号 → 等 500ms → 重拉列表 → 取 queueStatus=3 的第一条」：
    // 分诊台在这 500ms 里插队/退号/呼叫，猜出来的行与后端实际叫到的就不是同一个人 ——
    // 屏幕显示 A、病历挂到 B 的诊次上。按钮叫「接诊下一位」，接到谁由队列说了算，
    // 但「接到的是谁」必须由服务端原样告知。
    const res: any = await callNextQueue({})
    const called = res?.data
    if (!called || !called.id) {
      ElMessage.warning('已发送叫号，但未取到接诊回执，请刷新队列确认')
      await loadData()
      return
    }
    if (called.previousPatientName) {
      // 同一医生同一时刻只允许一条「就诊中」，叫下一位时服务端会收掉上一条。
      // 如实告知，不静默改状态（医生可能只是想让上一位「挂起」，那就该走暂离/挂起）。
      ElMessage.info(`已自动结束上一位就诊：${called.previousPatientName}`)
    }
    await loadData()
    await selectPatient(called)
    ElMessage.success(`已接诊：${called.patientName}（${shortQueueNo(called.queueNo)}）`)
  } catch (error: any) {
    const msg = error.message || '叫号失败'
    // 后端文案直接透出（含「今日有 N 位候诊患者但未能取号」这类诊断信息），
    // 只有确认是「真没人」时才换成给医生的行动指引 —— 用 includes('候诊患者') 会把
    // 「有候诊患者但取号失败」也吞成「没有候诊患者」，把系统故障说成正常空队列。
    if (msg.includes('没有候诊患者')) {
      ElMessage.warning('当前科室没有候诊患者（患者需先在分诊站签到入队）')
    } else {
      ElMessage.error(msg)
    }
  }
}

// ========== 重呼当前患者 ==========
const handleRecallPatient = async () => {
  if (!currentCalledPatient.value) {
    ElMessage.warning('当前没有就诊中的患者')
    return
  }
  try {
    await recallPatient(currentCalledPatient.value.id)
    ElMessage.success(`已重呼 ${currentCalledPatient.value.patientName}`)
  } catch (error: any) {
    ElMessage.error(error.message || '重呼失败')
  }
}

// ========== 呼叫指定患者（插队）/ 回诊 ==========
const handleCallSpecific = async (row: any) => {
  // 队列 4-已就诊 的行走的是「回诊」：上一位只是被叫号自动收口、病历并未结诊。
  const reconsult = row.queueStatus === 4
  try {
    const msg = reconsult
        ? (currentCalledPatient.value
            ? `当前就诊中：${currentCalledPatient.value.patientName}，确认结束并回诊 ${row.patientName}？`
            : `确认回诊 ${row.patientName}？回诊后继续书写的将是本次就诊的复诊记录。`)
        : currentCalledPatient.value
            ? `当前就诊中：${currentCalledPatient.value.patientName}，确认切换并呼叫 ${row.patientName}？`
            : `确认呼叫 ${row.patientName}？`

    await ElMessageBox.confirm(msg, reconsult ? '回诊确认' : '呼叫确认', {
      confirmButtonText: reconsult ? '确认回诊' : '确认呼叫',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 呼叫指定患者：接诊对象用服务端回执，前端不再「等 500ms 再猜」。
    // 后端会自动收掉本医生的上一位「就诊中」（同一位医生同一时刻只能有一条）。
    const res: any = await callPatient(row.id)
    const called = res?.data || row
    if (called?.previousPatientName) {
      ElMessage.info(`已自动结束上一位就诊：${called.previousPatientName}`)
    }
    await loadData()
    await selectPatient(called)
    ElMessage.success(`${called.reconsult || reconsult ? '已回诊' : '已呼叫'} ${called.patientName || row.patientName}`)
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '呼叫失败')
    }
  }
}

// ========== 暂离诊室 / 恢复接诊 ==========
// 语义是医生自己的接诊状态（Redis DoctorStatusCacheService：0 空闲 / 1 接诊中 / 2 暂离），
// 不是「暂停某个患者」。暂离后叫号与分诊台可据此提示该诊室医生暂离。
const doctorPaused = ref(false)

const loadDoctorStatus = async () => {
  try {
    const res = await getCurrentDoctorStatus()
    doctorPaused.value = (res.data?.status ?? 0) === 2
  } catch (error) {
    console.error('读取接诊状态失败:', error)
  }
}

const handlePause = async () => {
  const next = doctorPaused.value ? 0 : 2
  try {
    await setDoctorStatus(next)
    doctorPaused.value = next === 2
    ElMessage.success(next === 2 ? '已暂离，叫号将跳过本诊室' : '已恢复接诊')
  } catch (error: any) {
    ElMessage.error(error?.message || '接诊状态切换失败')
  }
}

// ========== 结诊 ==========
const handleComplete = async () => {
  if (!currentPatient.value) return

  // 检查病历必填项
  const requiredFields = [
    {field: recordForm.chiefComplaint, label: '主诉', value: 'chiefComplaint'},
    {field: recordForm.presentIllness, label: '现病史', value: 'presentIllness'},
    {field: recordForm.allergyHistory, label: '过敏史', value: 'allergyHistory'},
    {field: recordForm.pastHistory, label: '既往史', value: 'pastHistory'},
    {field: recordForm.personalHistory, label: '个人史', value: 'personalHistory'},
    {field: recordForm.familyHistory, label: '家族史', value: 'familyHistory'},
    {field: recordForm.temperature, label: '体温', value: 'temperature'},
    {field: recordForm.systolicPressure, label: '收缩压', value: 'systolicPressure'},
    {field: recordForm.diagnosis, label: '诊断', value: 'diagnosis'},
  ]

  const missingFields = requiredFields.filter(item => !item.field)

  // 如果有未填写的必填项，提示医生
  if (missingFields.length > 0) {
    const missingListHtml = missingFields.map(item =>
        `<div class="flex items-center justify-between py-1">
        <span class="text-red-600">✗ ${item.label}</span>
      </div>`
    ).join('')

    try {
      await ElMessageBox.confirm(
          `<div class="space-y-2">
          <div class="text-sm text-slate-600">以下必填项尚未填写：</div>
          <div class="rounded-lg bg-red-50 p-3 text-sm">${missingListHtml}</div>
          <div class="text-xs text-slate-400 mt-2">请先填写完整后再结诊，无相关内容请在字段旁点击「填写无」</div>
        </div>`,
          '病历未完成',
          {
            dangerouslyUseHTMLString: true,
            confirmButtonText: '我知道了',
            showCancelButton: false,
            type: 'warning',
          }
      )
    } catch (action) {
      return
    }
    return // 不继续结诊，让医生先填写
  }

  // 校验处方必填项
  const rxErrors = validateAllPrescriptions()
  if (rxErrors.length > 0) {
    const errorListHtml = rxErrors.map(e => `<div class="py-0.5">• ${e}</div>`).join('')
    try {
      await ElMessageBox.confirm(
          `<div class="space-y-2">
          <div class="text-sm text-slate-600">处方必填项尚未填写完整：</div>
          <div class="rounded-lg bg-red-50 p-3 text-sm max-h-40 overflow-y-auto">${errorListHtml}</div>
          <div class="text-xs text-slate-400 mt-2">请先填写完整后再结诊。</div>
        </div>`,
          '处方未完成',
          {
            dangerouslyUseHTMLString: true,
            confirmButtonText: '我知道了',
            showCancelButton: false,
            type: 'warning',
          }
      )
    } catch (action) {
      return
    }
    return
  }

  // 检查各项状态（包括暂存的检查检验）
  const hasPrescriptions = prescriptionForm.details.length > 0
  const hasInspections = inspectionRecords.value.length > 0
  const hasLaboratories = laboratoryRecords.value.length > 0
  const hasAnyCharge = hasPrescriptions || hasInspections || hasLaboratories

  // 构建检查清单
  const checkItems = [
    {label: '已开具处方', checked: hasPrescriptions, critical: false},
    {label: '已开具检查', checked: hasInspections, critical: false},
    {label: '已开具检验', checked: hasLaboratories, critical: false},
  ]

  // 构建确认内容HTML
  const checkListHtml = checkItems.map(item =>
      `<div class="flex items-center gap-2 py-1.5">
      <span class="${item.checked ? 'text-emerald-500' : 'text-amber-500'}">${item.checked ? '✓' : '○'}</span>
      <span class="${item.checked ? 'text-slate-700' : 'text-slate-500'}">${item.label}</span>
    </div>`
  ).join('')

  let warningHtml = ''
  if (!hasAnyCharge) {
    warningHtml = `<div class="mt-3 rounded-lg bg-amber-50 border border-amber-200 p-3 text-sm text-amber-700">
      ⚠ 当前没有开具任何处方/检查/检验，患者无需缴费。
    </div>`
  }

  try {
    await ElMessageBox.confirm(
        `<div class="space-y-2">
        <div class="text-sm text-slate-600">确认完成该患者的就诊？</div>
        <div class="rounded-lg bg-slate-50 p-3 text-sm">${checkListHtml}</div>
        ${warningHtml}
        <div class="text-xs text-slate-400 mt-2">系统将自动保存并提交病历，生成收费单。</div>
      </div>`,
        '结诊确认',
        {
          dangerouslyUseHTMLString: true,
          confirmButtonText: '确认完成',
          cancelButtonText: '返回修改',
          type: 'info',
        }
    )
  } catch (action) {
    return // 用户取消
  }

  try {
    // 获取患者信息
    const patient = currentPatient.value

    // 构建提交数据
    const submitData = {
      // 患者信息
      patientId: patient.patientId,
      patientNo: patient.patientNo,
      patientName: patient.patientName,
      registId: patient.registId,
      queueId: patient.id,
      // 科室医生信息
      deptId: patient.deptId,
      deptName: patient.deptName,
      doctorId: patient.doctorId,
      doctorName: patient.doctorName,
      // 病历信息
      recordId: recordForm.id || null,
      chiefComplaint: recordForm.chiefComplaint,
      presentIllness: recordForm.presentIllness,
      allergyHistory: recordForm.allergyHistory,
      pastHistory: recordForm.pastHistory,
      personalHistory: recordForm.personalHistory,
      familyHistory: recordForm.familyHistory,
      temperature: recordForm.temperature,
      pulse: recordForm.pulse,
      respiration: recordForm.respiration,
      systolicPressure: recordForm.systolicPressure,
      diastolicPressure: recordForm.diastolicPressure,
      generalCondition: recordForm.generalCondition,
      specialistExam: recordForm.specialistExam,
      auxiliaryExam: recordForm.auxiliaryExam,
      diagnosis: recordForm.diagnosis,
      diagnosisCode: recordForm.diagnosisCode,
      diagnosisName: recordForm.diagnosisName,
      treatmentPlan: recordForm.treatmentPlan,
      // 处方信息（所有处方）
      prescriptions: buildPrescriptionPayload(),
      // 检查/检验申请：批次E/E1 起**不再随病历提交** —— 开单那一刻就已经落库了，
      // 病历保存只负责回填 recordId（后端 backfillApplyRecord）。
      // 撤单请走列表上的「删除」（后端带缴费/执行/收费引用三重保护），
      // 而不是"把本地数组里的行去掉"。
    }

    // 调用结诊提交接口
    await submitMedicalRecord(submitData)

    // 显示成功提示
    ElMessage.success('结诊成功')

    // 清空暂存的检查检验数据
    inspectionRecords.value = []
    laboratoryRecords.value = []

    // 刷新队列
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '操作失败')
  }
}

// ========== 建复诊（批次E/E6）==========
// 位置：检查/检验「结果」条目的操作里 —— 报告回来 → 一键建复诊 → 直接接诊。
// 复诊免挂号费（后端 waived：收费单金额 0 且直接置「已收费」，否则签不了到），
// 并带上 **原病历ID** 做关联 —— 只建立引用关系，原病历一律不改。
const canCreateRevisit = (item: any) =>
    !!item?.execRecordId && !!recordForm.id && !!currentPatient.value

/**
 * 该条既往病历是否就是本次复诊号关联的原病历。
 * 雪花ID 前后端都按字符串走，统一 String() 比较，避免 number/string 不等。
 */
const isLinkedRevisitRecord = (record: any) => {
  const linked = currentPatient.value?.revisitRecordId
  return !!linked && !!record?.id && String(record.id) === String(linked)
}

const handleCreateRevisit = async (item?: any) => {
  if (!currentPatient.value) {
    ElMessage.warning('请先选择患者')
    return
  }
  // 复诊号必须挂在一份真实病历上；没保存病历就没有可关联的原病历
  if (!recordForm.id) {
    ElMessage.warning('请先保存病历，再建复诊（复诊号要关联本次病历）')
    return
  }
  const itemName = item?.inspectionItemName || item?.laboratoryItemName || '本次就诊'
  const originLabel = recordForm.recordNo || String(recordForm.id)
  try {
    await ElMessageBox.confirm(
        `患者 ${currentPatient.value.patientName} 的「${itemName}」结果已回，确定为本次就诊创建复诊号？\n` +
        `关联原病历：${originLabel}（原病历不改动），收不收费由「复诊收费策略」判定（铺底策略：当日回诊全免）。`,
        '建复诊',
        {
          confirmButtonText: '确定创建',
          cancelButtonText: '取消',
          type: 'info'
        }
    )
    // 当日回诊（来源 1）：不占号源、不选排班，它是同一次挂号的延续
    await createRevisitRegistration({
      patientId: currentPatient.value.patientId,
      scheduleId: null, // 复诊不需要号源
      settlementType: currentPatient.value.settlementType || 1,
      medicalInsuranceType: currentPatient.value.medicalInsuranceType || '',
      medicalInsuranceNo: currentPatient.value.medicalInsuranceNo || '',
      visitType: 2, // 标记为复诊
      revisitSource: REVISIT_SOURCE.SAME_DAY_RETURN,
      revisitRecordId: recordForm.id, // 关联原病历（后端校验归属）
    })
    ElMessage.success('复诊号已创建（免挂号费），签到后即可接诊')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '创建失败')
    }
  }
}

// ========== 医嘱复诊预约（来源 2，sql/121） ==========
const showRevisitAppoint = ref(false)
const revisitAppointBusy = ref(false)

/**
 * 与上面「建复诊」的区别：建复诊是**当日回诊**（结果回来了，不占号源、按策略全免）；
 * 这里是医生替患者约**未来某一次**就诊（拆线、化疗下一程、复查后再看），
 * 它占号源、按「复诊收费策略」收钱，所以必须能在提交前看到金额。
 */
const openRevisitAppoint = () => {
  if (!recordForm.id) {
    ElMessage.warning('请先保存病历，再预约复诊（复诊号要关联本次病历）')
    return
  }
  showRevisitAppoint.value = true
}

const handleRevisitAppoint = async (payload: {
  revisitRecordId: string | number
  scheduleId: string | number
  slotId?: string | number
  settlementType: number
  medicalInsuranceType: string
  medicalInsuranceNo: string
}) => {
  if (!currentPatient.value) return
  revisitAppointBusy.value = true
  try {
    const res = await createRevisitRegistration({
      patientId: currentPatient.value.patientId,
      // 医生替患者约未来时段 = 预约渠道，扣预约池（不许吃现场余号）
      registSource: 4,
      visitType: 2,
      revisitSource: REVISIT_SOURCE.DOCTOR_ORDERED,
      ...payload,
    })
    showRevisitAppoint.value = false
    ElMessage.success(`复诊号已预约${res.data?.registNo ? `（${res.data.registNo}）` : ''}，请让患者按预约时段来院`)
    loadData()
  } catch (error: any) {
    ElMessage.error(error?.message || '预约复诊失败')
  } finally {
    revisitAppointBusy.value = false
  }
}

// ========== 临时保存 ==========
const handleSaveRecord = async () => {
  if (!currentPatient.value) return
  try {
    // 构建保存数据
    const saveData = {
      // 患者信息
      patientId: currentPatient.value.patientId,
      patientNo: currentPatient.value.patientNo,
      patientName: currentPatient.value.patientName,
      registId: currentPatient.value.registId,
      queueId: currentPatient.value.id,
      // 科室医生信息
      deptId: currentPatient.value.deptId,
      deptName: currentPatient.value.deptName,
      doctorId: currentPatient.value.doctorId,
      doctorName: currentPatient.value.doctorName,
      // 病历信息
      recordId: recordForm.id || null,
      chiefComplaint: recordForm.chiefComplaint,
      presentIllness: recordForm.presentIllness,
      allergyHistory: recordForm.allergyHistory,
      pastHistory: recordForm.pastHistory,
      personalHistory: recordForm.personalHistory,
      familyHistory: recordForm.familyHistory,
      temperature: recordForm.temperature,
      pulse: recordForm.pulse,
      respiration: recordForm.respiration,
      systolicPressure: recordForm.systolicPressure,
      diastolicPressure: recordForm.diastolicPressure,
      generalCondition: recordForm.generalCondition,
      skinMucosa: recordForm.skinMucosa,
      headNeck: recordForm.headNeck,
      chestLung: recordForm.chestLung,
      heart: recordForm.heart,
      abdomen: recordForm.abdomen,
      spineLimbs: recordForm.spineLimbs,
      nervousSystem: recordForm.nervousSystem,
      specialistExam: recordForm.specialistExam,
      auxiliaryExam: recordForm.auxiliaryExam,
      diagnosis: recordForm.diagnosis,
      diagnosisCode: recordForm.diagnosisCode,
      diagnosisName: recordForm.diagnosisName,
      treatmentPlan: recordForm.treatmentPlan,
      // 处方信息（所有处方）
      prescriptions: buildPrescriptionPayload(),
      // 检查/检验申请：批次E/E1 起**不再随病历提交** —— 开单那一刻就已经落库了，
      // 病历保存只负责回填 recordId（后端 backfillApplyRecord）。
      // 撤单请走列表上的「删除」（后端带缴费/执行/收费引用三重保护），
      // 而不是"把本地数组里的行去掉"。
    }

    // 调用保存接口
    const res = await saveMedicalRecord(saveData)
    if (res.data) {
      recordForm.id = res.data
    }
    recordSaved.value = true
    ElMessage.success('病历保存成功')
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  }
}

const handleRemoveDrug = (index: number) => {
  prescriptionForm.details.splice(index, 1)
  // 删除药品后重新预估
  loadInsuranceInfo()
  checkDrugSafety()
}

const handleAddEmptyDrug = () => {
  const type = currentPrescriptionType.value
  // 如果该类型没有处方，先创建一个
  if (!hasPrescriptionType(type)) {
    const newPrescription = {
      prescriptionType: type,
      diagnosis: '',
      usageInstruction: '',
      details: [],
    }
    prescriptionList.value.push(newPrescription)
    prescriptionForm.details = newPrescription.details
  }
  const base = {
    drugId: null, drugCode: '', drugName: '', genericName: '',
    specification: '', dosageForm: '', unit: '盒', quantity: 1,
    price: 0, usageDosage: '', amount: 0,
  }
  if (type === 3) {
    // 中药饮片：总量/金额不手填，选药 + 填每剂克数后按剂数算出来
    prescriptionForm.details.push({
      ...base, unit: 'g', quantity: 0, singleDosage: '', frequency: '每日一剂',
      route: tcmMethodDict.value[0]?.dictValue || '水煎服', duration: 7,
    })
  } else if (type === 2) {
    // 中成药
    prescriptionForm.details.push({
      ...base, singleDosage: '', frequency: '一日三次', route: '口服', duration: 7,
    })
  } else {
    // 西药
    prescriptionForm.details.push({
      ...base, singleDosage: '', frequency: '一日三次', route: '口服', duration: 7,
    })
  }
}

// ========== 处方必填校验 ==========
const validatePrescription = (type: number): string[] => {
  const errors: string[] = []
  const isCurrent = type === currentPrescriptionType.value
  const prescription: any = prescriptionList.value.find(p => p.prescriptionType === type) || {}
  const details = prescription.details || []
  if (details.length === 0) return errors

  // 饮片方的剂数是「一张方一个」的处方头字段，后端强校验 1~30，且必须选代煎/自煎
  if (type === 3) {
    const doseCount = isCurrent ? prescriptionForm.doseCount : prescription.doseCount
    const decoctFlag = isCurrent ? prescriptionForm.decoctFlag : prescription.decoctFlag
    if (!doseCount || doseCount < 1 || doseCount > 30) errors.push('中药饮片处方：请填写剂数（1~30 剂）')
    if (decoctFlag !== 1 && decoctFlag !== 2) errors.push('中药饮片处方：请选择煎服方式（代煎 / 自煎）')
  }

  details.forEach((item: any, idx: number) => {
    const prefix = `${prescriptionTypeLabel(type)}处方第${idx + 1}项`
    if (!item.drugId) errors.push(`${prefix}：请选择药品`)
    if (!item.singleDosage) errors.push(`${prefix}：请填写用量`)
    if (!item.frequency) errors.push(`${prefix}：请填写频次/用法`)
    if (!item.route) errors.push(`${prefix}：请填写给药途径/煎法`)
    if (type === 3 && item.drugId && tcmPerDoseGrams(item) <= 0) {
      errors.push(`${prefix}：每剂克数必须是数字（如 15，不要写「15g」「适量」）`)
    }
    if (!item.quantity || item.quantity <= 0) errors.push(`${prefix}：请填写正确的总量/剂数`)
    if (type !== 3 && (!item.duration || item.duration <= 0)) errors.push(`${prefix}：请填写天数`)
  })
  return errors
}

const validateAllPrescriptions = (): string[] => {
  return [1, 2, 3].flatMap(type => validatePrescription(type))
}

/**
 * 病历提交用的处方载荷。剂数/煎服方式存在处方头，而当前 tab 的编辑态在 prescriptionForm 上，
 * 所以「正在编的那张」取表单值、其余取各自对象上的值（两处提交站点共用，避免口径漂移）。
 */
const buildPrescriptionPayload = () => {
  return prescriptionList.value
      .filter((p: any) => p.details && p.details.length > 0)
      .map((p: any) => {
        const isCurrent = p.prescriptionType === currentPrescriptionType.value
        return {
          prescriptionType: p.prescriptionType || 1,
          doseCount: isCurrent ? prescriptionForm.doseCount : p.doseCount,
          decoctFlag: isCurrent ? prescriptionForm.decoctFlag : p.decoctFlag,
          details: p.details.map((d: any) => ({
            drugId: d.drugId,
            drugName: d.drugName,
            specification: d.specification,
            unit: d.unit,
            singleDosage: d.singleDosage,
            frequency: d.frequency,
            route: d.route,
            quantity: d.quantity,
            duration: d.duration,
            remark: d.remark,
          })),
        }
      })
}

const handleDrugSelectForRecord = (record: any, val: number) => {
  const drug = drugResults.value.find((d: any) => d.id === val)
  if (drug) {
    record.drugId = drug.id
    record.drugCode = drug.drugCode
    record.drugName = drug.drugName
    record.specification = drug.specification || ''
    record.unit = drug.unit || '盒'
    record.price = drug.retailPrice || 0
    // 饮片按克开方：行上存的单价一律是「元/克」，界面预估和后端落库同一个口径
    // （元/kg 留在行上迟早被谁乘一次克数，金额直接放大一千倍）
    if (currentPrescriptionType.value === 3) {
      record.price = tcmPerGramPrice(drug)
      applyTcmGrams(record)
    } else {
      record.amount = record.quantity * (drug.retailPrice || 0)
    }
  }
}

// ========== 检查/检验申请（含知情同意） ==========
const pendingInspectionIdx = ref(0)
const handleAddEmptyInspection = () => {
  inspectionRecords.value.push({
    _pendingIdx: ++pendingInspectionIdx.value,
    inspectionItemId: null,
    inspectionItemName: '',
    // 批次E/F：不再写 applyStatus=0 —— E4 收口后申请单只有 1已提交/2已缴费/6已取消，
    // 未落库的空行没有申请单状态可言（有没有落库看 id）
    bodyPart: '',
    inspectionPurpose: '',
    preparation: '',
    clinicalDiagnosis: '',
    isEmergency: 0,
    price: 0,
  })
}

const pendingLaboratoryIdx = ref(0)
const handleAddEmptyLaboratory = () => {
  laboratoryRecords.value.push({
    _pendingIdx: ++pendingLaboratoryIdx.value,
    laboratoryItemId: null,
    laboratoryItemName: '',
    specimenType: '',
    laboratoryPurpose: '',
    clinicalDiagnosis: '',
    isFasting: 0,
    isEmergency: 0,
    price: 0,
  })
}

// 批次E/F：删掉了 handleSaveInspection / handleSaveLaboratory。
// 它们是 E1 之前的「暂存本地数组、等结诊再提交」路径，两个函数都已没有任何调用点，
// 而它们那句「已添加（结诊后生效）」与现状相反 —— 现在选完项目即刻落库、即刻待缴费。
// 留着只会让下一个人以为还有第二条开单链路。真正的开单在 handleSaveInspectionRow /
// submitInspectionApply（选项目即落库）。

// ========== 删除申请单（批次E/E2：走后端，带保护） ==========
/**
 * 删除检查申请。
 *
 * 原实现只从本地数组 splice —— 申请单从「开单即落库」之后就是真实存在的单据，
 * 只删界面等于"界面上没了、库里还在"，刷新一下又回来。
 * 现在走后端删除接口，能不能删由后端说了算（已缴费 / 已生成检查记录 /
 * 已被收费单引用 一律拒绝并给原因），前端不自己判状态。
 */
const handleDeleteInspectionRecord = async (idx: number) => {
  const row = inspectionRecords.value[idx]
  if (!row) return
  if (!row.id) {
    // 还没落库的空行（选了项目就会立刻落库，所以这只在极端情况下出现）
    inspectionRecords.value.splice(idx, 1)
    loadInsuranceInfo()
    return
  }
  if (row.canDelete === false) {
    ElMessage.warning(row.deleteBlockReason || '当前状态不允许删除')
    return
  }
  try {
    await ElMessageBox.confirm(`确定删除检查申请「${row.inspectionItemName}」？`, '删除检查申请', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteInspectionApply(row.id)
    inspectionRecords.value.splice(idx, 1)
    ElMessage.success('检查申请已删除')
    loadInsuranceInfo()
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败')
  }
}

const handleDeleteLaboratoryRecord = async (idx: number) => {
  const row = laboratoryRecords.value[idx]
  if (!row) return
  if (!row.id) {
    laboratoryRecords.value.splice(idx, 1)
    loadInsuranceInfo()
    return
  }
  if (row.canDelete === false) {
    ElMessage.warning(row.deleteBlockReason || '当前状态不允许删除')
    return
  }
  try {
    await ElMessageBox.confirm(`确定删除检验申请「${row.laboratoryItemName}」？`, '删除检验申请', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteLaboratoryApply(row.id)
    laboratoryRecords.value.splice(idx, 1)
    ElMessage.success('检验申请已删除')
    loadInsuranceInfo()
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败')
  }
}

// ========== 查看报告 ==========
/**
 * 影像随报告一起可见（sql/137 简化 PACS）。
 *
 * 弹框内容是 HTML 字符串，塞不进带缩放/调窗状态的阅片器组件，所以这里只铺缩略图；
 * 放大与窗宽窗位在检查/检验工作站的阅片器里做。地址必须走 /api/ 前缀
 * （后端 context-path=/api，静态资源映射在 /uploads/**），与病历引导单同一口径。
 */
const htmlEsc = (v: any) => String(v ?? '').replace(/[&<>"]/g, (c: string) => (
    {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;'}[c] as string))

const examImagesHtml = (images: any[], label: string) => {
  const list = Array.isArray(images) ? images.filter((i: any) => i?.fileUrl) : []
  if (!list.length) {
    return `<div class="border-t pt-3 text-xs text-slate-400">本次${label}未挂影像帧</div>`
  }
  const cells = list.map((i: any) => `<div>
      <img src="/api/${htmlEsc(i.fileUrl)}" alt="${htmlEsc(i.fileName || '影像')}"
           class="max-h-40 w-full rounded border border-slate-200 bg-black object-contain"/>
      <p class="mt-1 text-[11px] text-slate-500">#${htmlEsc(i.seq)} ${htmlEsc(i.modalityText || '未标模态')}${i.source === 2 ? '（模拟）' : ''}</p>
    </div>`).join('')
  return `<div class="border-t pt-3">
      <p class="font-bold text-slate-700 mb-2">影像（共 ${list.length} 帧，放大请在工作站阅片器打开）</p>
      <div class="grid grid-cols-4 gap-2 rounded bg-slate-900 p-2">${cells}</div>
    </div>`
}

/**
 * 申请单/执行状态 → el-tag 颜色。
 *
 * 颜色只是提示，**文案一律用后端给的 `execStatusText`**：
 * 检查与检验的 record_status 是两套不同码表（检查 2=已签到，检验 2=已采样），
 * 前端按码值自己翻译必然翻错 —— 医生站曾经就把"已挂号"显示成过"未知"。
 */
const applyTagType = (item: any) => {
  if (item?.critical) return 'danger'
  const text = item?.execStatusText || ''
  if (text === '待缴费') return 'warning'
  if (text === '已取消') return 'info'
  if (text === '已出结果' || text === '已审核' || text === '已发布') return 'success'
  if (['检查中', '检测中', '已到检', '已采样', '已接收', '已缴费待执行'].includes(text)) return 'primary'
  return 'info'
}

/**
 * 查看检查报告。
 *
 * 原实现直接读申请单上的 `resultDescription / resultConclusion` —— 这两个字段
 * 从来就不在申请单 VO 里，所以它永远只显示"检查项目/部位/目的"，外加一张写死的
 * `huichuan.png` 假影像。现在按执行记录 ID 真去取报告。
 */
const handleViewInspectionReport = async (item: any) => {
  if (!item?.execRecordId) {
    ElMessage.warning('该检查还没有对应的检查记录，暂无报告可看')
    return
  }
  let data: any = null
  try {
    const res = await getInspectionDetail(item.execRecordId)
    data = res.data || {}
  } catch (error: any) {
    ElMessage.error(error.message || '报告加载失败')
    return
  }
  const record = data.record || {}
  const report = data.report || {}
  const escapeHtml = (v: any) => String(v ?? '-').replace(/[&<>"]/g, (c: string) => (
      {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;'}[c] as string))
  const block = (title: string, body: any, cls = 'bg-slate-50') => body
      ? `<div class="border-t pt-3"><p class="font-bold text-slate-700 mb-2">${title}</p>
         <p class="text-sm ${cls} p-2 rounded whitespace-pre-wrap">${escapeHtml(body)}</p></div>`
      : ''
  ElMessageBox.alert(
      `<div class="space-y-3 w-full">
      <div class="grid grid-cols-2 gap-2 text-sm">
        <p><strong>检查项目：</strong>${escapeHtml(record.inspectionItemName || item.inspectionItemName)}</p>
        <p><strong>检查部位：</strong>${escapeHtml(record.bodyPart || item.bodyPart)}</p>
        <p><strong>检查科室：</strong>${escapeHtml(record.inspectionDeptName)}</p>
        <p><strong>报告医师：</strong>${escapeHtml(report.auditBy || record.executeBy)}</p>
        <p><strong>记录状态：</strong>${escapeHtml(item.execStatusText)}</p>
        <p><strong>报告编号：</strong>${escapeHtml(report.reportNo)}</p>
      </div>
      ${block('检查所见', report.reportContent || record.resultDescription)}
      ${block('影像诊断/印象', report.conclusion || record.resultConclusion, 'bg-emerald-50')}
      ${block('建议', report.suggestions, 'bg-blue-50')}
      ${examImagesHtml(data.images, '检查')}
      ${!report.reportNo && !record.resultConclusion
          ? '<div class="border-t pt-3 text-xs text-slate-400">该检查尚无报告内容（当前状态：'
            + escapeHtml(item.execStatusText) + '）</div>' : ''}
    </div>`,
      '检查报告详情',
      {dangerouslyUseHTMLString: true, confirmButtonText: '关闭',
        customStyle: {'max-width': '70%', 'width': '70%'}}
  )
}

const handleViewLaboratoryReport = async (item: any) => {
  // 加载检验结果明细
  // 优先用后端给的 execRecordId（批次E/E5：申请单 VO 已直接带执行记录 ID），
  // 拿不到才退回"按 applyId 在记录列表里翻"的老办法（多一次全量查询，且依赖 applyId 能对上）。
  let recordId = item?.execRecordId
  try {
    if (!recordId) {
      const recordRes = await getLaboratoryRecordList({
        patientId: currentPatient.value?.patientId
      })
      const records = recordRes.data?.records || []
      const found = records.find((r: any) => r.applyId == item.id)
      if (!found) {
        ElMessage.warning('未找到对应的检验记录')
        return
      }
      recordId = found.id
    }

    const detailRes = await getLaboratoryDetail(recordId)
    const detailRecord = detailRes.data?.record || {}
    const results = detailRes.data?.results || []

    // 统计异常项目：只统计已判定为异常的，未判定项不能算进正常也不能算进异常
    const abnormalCount = results.filter((r: any) => r.abnormalFlag && r.abnormalFlag !== 0).length
    const unjudgedCount = results.filter((r: any) => String(r.judgeNote || '').startsWith('未判定：')).length

    // 构建结果明细表格
    let resultsHtml = ''
    if (results.length > 0) {
      resultsHtml = `
        <div class="border-t pt-3">
          <div class="flex items-center justify-between mb-2">
            <p class="font-bold text-slate-700">检验结果明细（共 ${results.length} 项，异常 ${abnormalCount} 项${
              unjudgedCount > 0 ? `，未判定 ${unjudgedCount} 项` : ''
            }）</p>
          </div>
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
              ${results.map((r: any) => {
                // 一律用后端算好的 abnormalFlagText。
                // 不要用 abnormalFlag 写三目判断：未判定时它也是 0，与「正常」同值，
                // 非 1/2/3 就显示「正常」会把「不知道」当成「正常」给医生看。
                const flagText = r.abnormalFlagText || '—'
                const isAbnormal = flagText === '偏高' || flagText === '偏低' || flagText === '异常'
                const isUnjudged = flagText === '未判定'
                const tagClass = isAbnormal
                    ? 'bg-red-100 text-red-600'
                    : isUnjudged
                        ? 'bg-amber-100 text-amber-700'
                        : 'bg-green-100 text-green-600'
                return `
                <tr class="${isAbnormal ? 'bg-red-50' : isUnjudged ? 'bg-amber-50' : ''}">
                  <td class="border border-slate-300 px-3 py-2">${r.laboratoryItemName}</td>
                  <td class="border border-slate-300 px-3 py-2 ${isAbnormal ? 'text-red-600 font-bold' : ''}">${r.resultValue || '-'}</td>
                  <td class="border border-slate-300 px-3 py-2">${r.resultUnit || '-'}</td>
                  <td class="border border-slate-300 px-3 py-2">${r.referenceRange || '-'}</td>
                  <td class="border border-slate-300 px-3 py-2">
                    <span class="inline-block px-2 py-0.5 rounded text-xs font-medium ${tagClass}">${flagText}</span>
                    ${isUnjudged && r.judgeNote ? `<div class="mt-0.5 text-[11px] text-slate-500">${r.judgeNote}</div>` : ''}
                  </td>
                </tr>
              `}).join('')}
            </tbody>
          </table>
        </div>
      `
    }

    // 检验结论/诊断
    const diagnosisHtml = detailRecord.diagnosis ? `
      <div class="border-t pt-3">
        <p class="font-bold text-slate-700 mb-2">检验结论：</p>
        <div class="rounded-lg bg-blue-50 p-3 text-sm text-slate-700">${detailRecord.diagnosis}</div>
      </div>
    ` : ''

    // 建议
    const suggestionsHtml = detailRecord.suggestions ? `
      <div class="border-t pt-3">
        <p class="font-bold text-slate-700 mb-2">建议：</p>
        <div class="rounded-lg bg-amber-50 p-3 text-sm text-slate-700">${detailRecord.suggestions}</div>
      </div>
    ` : ''

    ElMessageBox.alert(
        `<div class="space-y-3">
        <div class="grid grid-cols-2 gap-2 text-sm">
          <p><strong>检验项目：</strong>${detailRecord.laboratoryItemName || item.laboratoryItemName}</p>
          <p><strong>标本类型：</strong>${detailRecord.specimenType || item.specimenType || '-'}</p>
          <p><strong>检验目的：</strong>${detailRecord.laboratoryPurpose || item.laboratoryPurpose || '-'}</p>
          <p><strong>临床诊断：</strong>${detailRecord.clinicalDiagnosis || item.clinicalDiagnosis || '-'}</p>
        </div>
        ${resultsHtml}
        ${diagnosisHtml}
        ${suggestionsHtml}
        ${examImagesHtml(detailRes.data?.images, '检验')}
      </div>`,
        '检验报告详情',
        {
          dangerouslyUseHTMLString: true, confirmButtonText: '关闭', customStyle: {
            'max-width': '40%'
          }
        }
    )
  } catch (error: any) {
    ElMessage.error(error.message || '获取报告失败')
  }
}

// ========== 打印指引单 ==========
const guidePdfUrl = ref('')
const loadingGuide = ref(false)

const loadGuideContent = async () => {
  if (!recordForm.guidePdfPath) {
    guidePdfUrl.value = ''
    return
  }
  loadingGuide.value = true
  try {
    // 直接使用后端地址访问PDF uploads/guide/20260915/MR202609151809310002.pdf
    guidePdfUrl.value = `/api/${recordForm.guidePdfPath}`
  } catch (e) {
    guidePdfUrl.value = ''
  } finally {
    loadingGuide.value = false
  }
}

const printGuidePdf = () => {
  if (!guidePdfUrl.value) return
  const printWindow = window.open(guidePdfUrl.value, '_blank')
  if (printWindow) {
    printWindow.onload = () => {
      printWindow.print()
    }
  }
}

watch(() => showGuideSheetDialog.value, (val) => {
  if (val) loadGuideContent()
})

// ========== 打印处方 ==========
const printPrescriptions = () => {
  const patient = currentPatient.value
  if (!patient) return

  let html = `
    <html><head><title>处方笺</title>
    <style>
      body { font-family: SimSun, serif; padding: 40px; font-size: 14px; }
      .header { text-align: center; border-bottom: 2px solid #000; padding-bottom: 10px; margin-bottom: 20px; }
      .header h1 { font-size: 24px; margin: 0; }
      .info { display: flex; justify-content: space-between; margin-bottom: 15px; font-size: 13px; }
      .rx-title { font-size: 16px; font-weight: bold; border-bottom: 1px solid #000; padding-bottom: 5px; margin: 20px 0 10px; }
      table { width: 100%; border-collapse: collapse; margin-bottom: 15px; }
      th, td { border: 1px solid #000; padding: 6px 10px; text-align: left; font-size: 13px; }
      th { background: #f5f5f5; }
      .footer { margin-top: 30px; display: flex; justify-content: space-between; font-size: 13px; }
      @media print { body { padding: 20px; } }
    </style></head><body>
    <div class="header"><h1>处 方 笺</h1></div>
    <div class="info">
      <span>患者：${patient.patientName}</span>
      <span>性别：${patientGenderText(patient.gender)}</span>
      <span>年龄：${patient.age}岁</span>
      <span>科室：${patient.deptName}</span>
      <span>医生：${patient.doctorName}</span>
    </div>
  `

  const hasAny = prescriptionList.value.some(p => p.details && p.details.length > 0)
  if (!hasAny) {
    html += '<div style="text-align:center;color:#999;padding:40px;">无处方</div>'
  } else {
    prescriptionList.value.forEach(p => {
      if (!p.details || p.details.length === 0) return
      html += `<div class="rx-title">${prescriptionTypeLabel(p.prescriptionType)}处方</div>`
      html += `<table><thead><tr><th>药品名称</th><th>规格</th><th>数量</th><th>用法</th><th>频次</th><th>天数</th></tr></thead><tbody>`
      p.details.forEach((d: any) => {
        html += `<tr>
          <td>${d.drugName || '-'}</td>
          <td>${d.specification || '-'}</td>
          <td>${d.quantity || '-'} ${d.unit || ''}</td>
          <td>${d.route || '-'}</td>
          <td>${d.frequency || '-'}</td>
          <td>${d.duration || '-'}</td>
        </tr>`
      })
      html += '</tbody></table>'
    })
  }

  html += `
    <div class="footer">
      <span>医师签名：${patient.doctorName || ''}（已电子签名·结诊时自动签署，可验签）</span>
      <span>日期：${new Date().toLocaleDateString('zh-CN')}</span>
    </div>
    </body></html>`

  const printWindow = window.open('', '_blank')
  if (printWindow) {
    printWindow.document.write(html)
    printWindow.document.close()
    printWindow.onload = () => printWindow.print()
  }
}

// 加载全部药品列表
const loadAllDrugs = async () => {
  drugLoading.value = true
  try {
    const types = [1, 2, 3]
    await Promise.all(types.map(async (type) => {
      const res = await getDrugSelectList({drugType: type})
      drugResultsByType[type].value = res.data || []
    }))
  } catch (error) {
    console.error('加载药品列表失败:', error)
  } finally {
    drugLoading.value = false
  }
}

// 加载全部检查项目
const loadAllInspectionItems = async () => {
  inspectionItemLoading.value = true
  try {
    const res = await getInspectionSelectList()
    inspectionItemResults.value = res.data || []
  } catch (error) {
    console.error('加载检查项目失败:', error)
  } finally {
    inspectionItemLoading.value = false
  }
}

// 加载全部检验项目
const loadAllLaboratoryItems = async () => {
  laboratoryItemLoading.value = true
  try {
    const res = await getLaboratorySelectList()
    laboratoryItemResults.value = res.data || []
  } catch (error) {
    console.error('加载检验项目失败:', error)
  } finally {
    laboratoryItemLoading.value = false
  }
}

// ------------------------------------------------------------------
// 开住院证（门诊 → 住院的入口）
//
// 门诊医生判断患者需要住院时，在这里开一张住院证。开完患者拿着证到入院处排床收治，
// 入院记录会记下：来源挂号号、来源住院证号、开证科室与医生、拟诊 —— 这就是"打通"。
//
// 不能替医生猜的事情一律不猜：
//  · 拟收治科室必须医生选（患者住哪个科是临床决策，不是系统能推的）；
//  · 拟诊默认带过来自「诊断」输入框的值，医生可以改，改了就以改的为准。
// ------------------------------------------------------------------
const orderDialogVisible = ref(false)
const orderSubmitting = ref(false)
const orderForm = reactive({
  // 科室 ID 也用字符串承载：后端是 Long，Jackson 能精确解析数字字符串，
  // 而前端一旦 Number() 就会在不经意间改掉大 ID 的末几位。
  applyDeptId: '' as string,
  diagnosisCode: '',
  diagnosisName: '',
  diagnosisNote: '',
  expectAdmitTime: '',
  remark: '',
})

const openOrderDialog = () => {
  if (!currentPatient.value) return ElMessage.warning('请先呼叫患者')
  // 拟诊预填：优先取病历里已录入的诊断名称/编码，没有就留空让医生填（不猜）
  const diagCode = (patientDetail.value?.diagnosisCode || recordForm.diagnosisCode || '').split(',')[0]?.trim() || ''
  const diagName = (patientDetail.value?.diagnosisName || recordForm.diagnosisName || '').split(',')[0]?.trim() || ''
  Object.assign(orderForm, {
    applyDeptId: '',
    diagnosisCode: diagCode,
    diagnosisName: diagName,
    diagnosisNote: '',
    expectAdmitTime: '',
    remark: '',
  })
  orderDialogVisible.value = true
}

const submitOrder = async () => {
  if (!orderForm.applyDeptId) return ElMessage.warning('请选择拟收治科室')
  if (!orderForm.diagnosisName) return ElMessage.warning('请填写拟诊（住院证的诊断是入院处排床的依据，不能空着）')
  if (!currentPatient.value) return
  orderSubmitting.value = true
  try {
    const p = currentPatient.value
    const dept = departments.value.find((d: any) => String(d.id) === String(orderForm.applyDeptId))
    // 注意：所有 ID 一律**原样以字符串**传给后端，绝不做 Number() 转换。
    // 雪花ID 是 19 位（约 2.1e18），远超 JS 的安全整数 2^53（9.007e15），
    // Number() 会静默改掉末几位 —— 实测挂号 ID ...834 被转成 ...800，
    // 于是住院证上记的是一个**不存在的挂号号**，而且一眼看不出来。
    const res = await createAdmissionOrder({
      registId: p.registId || null,
      registNo: p.registNo || null,
      patientId: p.patientId,
      patientNo: p.patientNo || null,
      patientName: p.patientName,
      gender: p.gender ?? null,
      age: p.age ?? null,
      phone: patientDetail.value?.phone || null,
      idCard: patientDetail.value?.idCard || null,
      sourceDeptId: p.deptId || null,
      sourceDeptName: p.deptName || null,
      sourceDoctorId: p.doctorId || null,
      sourceDoctorName: p.doctorName || null,
      applyDeptId: orderForm.applyDeptId,
      applyDeptName: dept?.deptName || null,
      diagnosisCode: orderForm.diagnosisCode || null,
      diagnosisName: orderForm.diagnosisName,
      diagnosisNote: orderForm.diagnosisNote || null,
      insuranceType: patientDetail.value?.medicalInsuranceType || null,
      medicalInsuranceNo: patientDetail.value?.medicalInsuranceNo || null,
      expectAdmitTime: orderForm.expectAdmitTime || null,
      remark: orderForm.remark || null,
    })
    ElMessage.success(`住院证已开具（${res.data}），患者持证到入院处排床即可`)
    orderDialogVisible.value = false
  } catch (e: any) {
    ElMessage.error(e?.message || '开住院证失败')
  } finally {
    orderSubmitting.value = false
  }
}

onMounted(() => {
  // 统一「带着目标患者进入」的入口。此前分成两条路（store 有患者→consumeSwitch()，
  // URL 有 patientId→直接挂起），但两者语义完全相同——这次进来是为了接某个人——
  // 分路的代价是以后每加一个入口都要重新推一遍时序。
  //   ① 顶部搜索跳转：store 在组件挂载前已 setPatient（watch 的注册晚于这次写入，抓不到）
  //   ② 刷新 / 直达 URL：store 从 sessionStorage 恢复，或只剩 URL 上的 patientId
  const restored = currentPatientStore.patient
  const targetId = restored?.id || route.query.patientId
  if (targetId) {
    pendingSwitch = {id: String(targetId), patientName: restored?.patientName || ''}
    // 标记「为某个特定患者而来」：首屏不得自动选中队列里正在就诊的别人
    enteredWithTarget = true
    // 记下这次切换已被本函数消费，避免 watch 之后把同一次再消费一遍
    handledSwitchAt = currentPatientStore.switchedAt
    // 会话里没有当前患者、只有 URL 上的 id → 手输 / 外链进入，算一次显式意图（该有反馈）；
    // 若 store 里有患者，那是切菜单/刷新重建的恢复，不重复提示（见 applyPendingSwitch）
    if (!restored && route.query.patientId) {
      urlIntent = true
    }
  }
  loadUserInfo()
  loadTcmDicts()
  loadDoctorStatus()
  loadDepartments()
  loadData()
  loadTodoList()
  loadDiagTemplates()
  loadRxTemplates()
  loadDrugPackages()
  loadInspectionTemplates()
  loadLaboratoryTemplates()
  loadAllDrugs()
  loadAllInspectionItems()
  loadAllLaboratoryItems()
  refreshTimer = setInterval(loadData, 30000)
})
onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})

// ========== 批次B 三栏同屏：右栏医嘱面板折叠 / 费用抽屉 ==========
const recordCollapsed = ref(false)
const showChargeDrawer = ref(false)

// 病历栏折叠只在**窄屏**才有意义。布局对调后（2026-09-26，用户：「医嘱面板可以做大一点，
// 病历占的太多了」）医嘱栏才是 flex-1 主工作区 —— 真实 HIS 的开药检索表要摊十几列，
// 病历是模板化低频录入，520px 单列文书够用。
// 断点是算出来的：固定占位 = 侧边栏 256 + 页面左右留白 48 + 队列列 300 + 病历列 520 + 两个 12px 缝 24 = 1148，
// 医嘱列 = 视口 - 1148；医嘱栏改造前就是 480px 固定宽，视口 ≥1628 时它已经不比当年窄，
// 不该再有折叠开关。取 1640：此时医嘱列 492px；再窄才给「收起病历」的逃生门（折后病历 36px，医嘱列多出 484px）。
// （队列列 2026-09-26 由 264 加宽到 300：急诊短号+三字姓名+「就诊中」徽章同排，264 下名字被 truncate 吃掉半个字）
const COMPACT_BREAKPOINT = 1640
const compactViewport = ref(false)
const syncCompactViewport = () => {
  compactViewport.value = window.innerWidth <= COMPACT_BREAKPOINT
  // 从窄屏（可能正折着）拖宽到宽屏：病历列本就该常驻，顺手复位。
  if (!compactViewport.value) recordCollapsed.value = false
}
syncCompactViewport()
window.addEventListener('resize', syncCompactViewport)
onUnmounted(() => window.removeEventListener('resize', syncCompactViewport))

// 真正的折叠态 = 窄屏 + 用户点了收起；宽屏下恒为展开
const recordColCollapsed = computed(() => compactViewport.value && recordCollapsed.value)
// 历史就诊：患者级跨就诊次，用弹窗打开（不打断接诊），内嵌 CDR 全景时间轴
const showPatientDetail = ref(false)
// 患者条上的到达时间（队列里有 arriveTime 才有值）
const arriveText = computed(() => {
  const t = currentPatient.value?.arriveTime
  return t ? formatArriveTime(t) + ' 到达' : ''
})

</script>

<template>
  <div class="flex h-[var(--his-page-h)] gap-3">
    <!-- 左栏：候诊队列 / 既往就诊（常驻窄列） -->
    <div class="flex w-[var(--his-queue-col-w)] shrink-0 flex-col gap-3">
      <!--
        当前叫号面板：按真实 HIS「诊室候诊大屏」的缩小版做，不是信息卡片。
        口径：① 视觉主体是「号」——正在就诊的序号用特大号字砸出来，医生隔两米能瞟到；
        ② 诊室灯（接诊中/暂离/空闲）独立成状态点，不混在按钮颜色里；
        ③ 「下一位」预览与后端 callNext 取号同口径（见 nextWaiting），面板预告的人=点按键叫到的人。
      -->
      <div class="call-board shrink-0 rounded-lg p-3.5 shadow-sm">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-1.5">
            <span class="call-light"
                  :class="doctorPaused ? 'is-away' : (currentCalledPatient ? 'is-busy' : 'is-idle')"></span>
            <span class="text-xs font-bold tracking-wide text-sky-100">{{
                doctorPaused ? '暂离中' : (currentCalledPatient ? '接诊中' : '空闲')
              }}</span>
          </div>
          <span class="rounded bg-white/10 px-1.5 py-0.5 text-xs font-medium text-sky-200">候诊 {{
              waitingCount
            }} 人</span>
        </div>

        <!-- 正在就诊：大号号码 + 姓名（窄列必须 truncate，否则名字挤成竖排） -->
        <div v-if="currentCalledPatient" class="mt-2">
          <div class="flex items-end gap-2">
            <div class="call-no leading-none text-white">{{ shortQueueNo(currentCalledPatient.queueNo) || '—' }}</div>
            <div class="min-w-0 flex-1 pb-1">
              <div class="truncate text-lg font-bold leading-tight text-white">
                {{ currentCalledPatient.patientName }}
              </div>
              <div class="mt-0.5 flex items-center gap-1 text-xs text-sky-200">
                <span>{{ patientGenderText(currentCalledPatient.gender) }} {{ currentCalledPatient.age }}岁</span>
                <span v-if="currentCalledPatient.registType === 3"
                      class="rounded bg-red-500/90 px-1 font-bold text-white">急</span>
              </div>
            </div>
          </div>
          <div class="mt-1.5 flex items-center justify-between text-xs text-sky-200">
            <span>已等待 {{ calcWaitMinutes(currentCalledPatient) }} 分钟</span>
            <span class="min-w-0 truncate pl-2 text-sky-200/70">{{ currentCalledPatient.patientNo }}</span>
          </div>
        </div>
        <div v-else class="mt-2 flex h-[74px] items-center">
          <span class="text-sm leading-6 text-sky-200/70">{{
              doctorPaused ? '已暂离，叫号将跳过本诊室' : '尚未叫号，点击下方「接诊下一位」开始'
            }}</span>
        </div>

        <!-- 下一位预览：排序口径 = 后端 callNext（危重级别优先 → 序号） -->
        <div class="mt-2 flex items-center gap-2 rounded-md bg-white/5 px-2 py-1.5 text-xs">
          <span class="shrink-0 font-medium text-sky-300/90">下一位</span>
          <template v-if="nextWaiting">
            <span class="shrink-0 font-bold text-white">{{ shortQueueNo(nextWaiting.queueNo) }}</span>
            <span class="min-w-0 truncate text-sky-100">{{ nextWaiting.patientName }}</span>
            <span v-if="nextWaiting.triageLevel && nextWaiting.triageLevel <= 2"
                  class="ml-auto shrink-0 rounded bg-red-500/80 px-1 text-[10px] font-bold text-white">
              {{ nextWaiting.triageLevel }}级
            </span>
          </template>
          <span v-else class="text-sky-200/50">队列已空</span>
        </div>

        <div class="mt-2.5 flex gap-2">
          <el-button type="primary" class="!flex-1" :icon="VideoPlay"
                     :disabled="doctorPaused || !waitingCount"
                     @click="handleCallNext">接诊下一位
          </el-button>
          <el-button class="!ml-0 !px-4" :icon="RefreshRight" :disabled="!currentCalledPatient || doctorPaused"
                     @click="handleRecallPatient">重呼
          </el-button>
        </div>
        <el-button :type="doctorPaused ? 'success' : 'warning'" plain class="mt-2 !w-full" :icon="VideoPause"
                   @click="handlePause">{{ doctorPaused ? '恢复接诊' : '暂离' }}
        </el-button>
      </div>
      <!--
        左栏只做一件事：今天的候诊队列。
        「历史就诊」一度挂在这里当第二个页签，那是维度错了 ——
        历史就诊是**患者级、跨就诊次**的纵向数据（这个人来过几次、每次做了什么），
        而这一列是**诊次级**的今日队列，两者不同维度。塞进来的后果是：264px 放不下
        （日期断行、科室名竖排），只能靠折叠遮掩；而且它只拉 recordStatus=3（已归档病历），
        未归档的既往诊次根本看不到，本身就是残的。
        已挪到患者条上的「历史就诊」入口：患者详情弹窗内嵌 CDR 全景时间轴（按就诊次组织）。
      -->
      <div class="flex min-h-0 flex-1 flex-col rounded-lg border border-slate-200 bg-white shadow-sm">
        <div class="flex shrink-0 items-center gap-1 border-b border-slate-200 px-2 py-2">
          <span class="pl-1 text-sm font-bold text-slate-700">候诊 {{ waitingCount }}</span>
          <span class="ml-auto text-xs text-slate-400">{{ new Date().toLocaleDateString('zh-CN') }}</span>
        </div>
        <!-- 搜索框 -->
        <div class=" border-slate-100 px-4 py-2">
          <el-input v-model="queueSearch" placeholder="搜索姓名、就诊号..." :prefix-icon="Search" size="small"
                    clearable/>
        </div>
        <!-- 过滤按钮 -->
        <div class="flex items-center gap-1 border-slate-100 px-4 py-2">
          <el-button
              v-for="f in [{ label: '全部', value: 'all' }, { label: '候诊', value: 'waiting' }, { label: '急诊', value: 'emergency' }]"
              :key="f.value" size="small" class="!rounded-md"
              :type="queueFilter === f.value ? 'primary' : ''"
              :plain="queueFilter !== f.value"
              @click="queueFilter = f.value"
          >{{ f.label }}
          </el-button>
        </div>
        <!-- 患者列表 -->
        <div class="flex-1 overflow-y-auto" v-loading="loading">
          <div v-for="row in filteredQueue" :key="row.id"
               class="group cursor-pointer border-y border-slate-100 px-4 py-3 transition-colors hover:bg-slate-50"
               :class="{ 'bg-blue-50 border-l-2 border-l-blue-500': currentPatient?.id === row.id }"
               @click="selectPatient(row)"
          >
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <span class="text-sm font-bold text-slate-900">{{ row.patientName }}</span>
                <span class="rounded bg-blue-50 px-1.5 py-0.5 text-xs font-medium text-blue-600">{{
                    shortQueueNo(row.queueNo)
                  }}</span>
              </div>
              <span
                  :class="['rounded px-1.5 py-0.5 text-xs', queueStatusMap[row.queueStatus]?.color || 'bg-slate-100 text-slate-600']">
                {{ queueStatusMap[row.queueStatus]?.label }}
              </span>
            </div>
            <div class="mt-1.5 flex flex-wrap items-center gap-1.5 text-xs">
              <span
                  :class="['rounded px-1.5 py-0.5 font-medium',
                           row.gender === 1 ? 'bg-blue-50 text-blue-600'
                               : row.gender === 2 ? 'bg-pink-50 text-pink-600' : 'bg-slate-100 text-slate-500']">
                {{ patientGenderText(row.gender) }} {{ row.age }}岁
              </span>
              <span v-if="row.age && row.age >= 60" class="rounded bg-amber-50 px-1.5 py-0.5 text-amber-600">老年</span>
              <span v-else-if="row.age && row.age <= 14"
                    class="rounded bg-purple-50 px-1.5 py-0.5 text-purple-600">儿童</span>
              <!-- 号别：急诊号优先显「急诊号」；其余未知（队列行没挂到挂号 → visitType 为 null）显「未标注」，不许编成初诊/复诊 -->
              <span :class="['rounded px-1.5 py-0.5', queueVisitBadgeOf(row).color]">
                {{ queueVisitBadgeOf(row).label }}
              </span>
              <!-- 批次E/E6：复诊号关联了原病历（挂号表 revisit_record_id），已在既往病历里标出来 -->
              <el-tooltip v-if="row.revisitRecordId" content="本次复诊关联了既往病历（原病历未改动）">
                <span class="rounded bg-amber-50 px-1.5 py-0.5 text-amber-600">关联病历</span>
              </el-tooltip>
              <span v-if="row.settlementType && row.settlementType !== 1"
                    class="rounded bg-blue-50 px-1.5 py-0.5 text-blue-600">医保</span>
              <span v-if="row.hasAllergy" class="rounded bg-red-50 px-1.5 py-0.5 text-red-600">过敏</span>
            </div>
            <div class="mt-1 flex items-center justify-between text-xs text-slate-400">
              <div class="flex items-center gap-2">
                <span v-if="row.arriveTime" class="text-slate-400">
                  <Clock class="inline h-3 w-3"/>
                  {{ formatArriveTime(row.arriveTime) }}到达
                </span>
                <span v-if="row.queueStatus === 3" :class="waitDurationClass(calcWaitMinutes(row))">
                  等待 {{ calcWaitMinutes(row) }}分钟
                </span>
              </div>
              <el-button type="primary" size="small" link v-if="row.queueStatus === 2"
                         class="opacity-0 group-hover:opacity-100 transition-opacity"
                         @click.stop="handleCallSpecific(row)">
                呼叫
              </el-button>
              <!-- 回诊：叫下一位时被自动收口（队列 4）但病历并未结诊提交（挂号单不是 4）的行，
                   允许本人重新叫回就诊中 —— 检查结果回来后看第二眼是门诊高频动作 -->
              <el-button type="warning" size="small" link v-if="row.queueStatus === 4 && row.registStatus !== 4"
                         class="opacity-0 group-hover:opacity-100 transition-opacity"
                         @click.stop="handleCallSpecific(row)">
                回诊
              </el-button>
            </div>
          </div>
          <div v-if="filteredQueue.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">暂无患者
          </div>
        </div>
      </div>

      <!-- 个人模板/套餐：低频入口，收成一个下拉，不再占常驻位 -->
      <el-dropdown trigger="click" placement="top-start" class="shrink-0">
        <el-button size="small" class="w-full">模板 / 套餐
          <el-icon class="ml-1"><ArrowDown/></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="showDiagTemplateDialog = true">常用诊断维护</el-dropdown-item>
            <el-dropdown-item @click="showRxTemplateDialog = true">处方模板管理</el-dropdown-item>
            <el-dropdown-item @click="showPackageDialog = true">药品/耗材套餐</el-dropdown-item>
            <el-dropdown-item @click="showInspectionTemplateDialog = true">检查申请模板</el-dropdown-item>
            <el-dropdown-item @click="showLaboratoryTemplateDialog = true">检验申请模板</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 主区：患者条 + 病历 + 医嘱面板 + 底部动作条 -->
    <div class="flex min-w-0 flex-1 flex-col gap-3">
      <!-- 无患者状态：说清"是没叫号还是今天根本没人"，并给手动刷新入口（队列 30s 自动刷一次） -->
      <div v-if="!currentPatient" class="flex flex-1 flex-col items-center justify-center text-slate-400">
        <Reading class="mb-4 h-16 w-16 opacity-30"/>
        <p class="text-lg font-medium">未开始叫号</p>
        <p class="mt-2 text-sm">
          {{ waitingCount > 0
            ? `今日候诊 ${waitingCount} 人，点击「接诊下一位」开始`
            : '今日没有挂到本诊室的患者；新挂号会自动刷进队列，也可手动刷新' }}
        </p>
        <div class="mt-4 flex items-center gap-2">
          <el-button type="primary" size="large" @click="handleCallNext">
            <el-icon class="mr-1">
              <VideoPlay/>
            </el-icon>
            接诊下一位
          </el-button>
          <el-button size="large" :icon="RefreshRight" @click="loadData">刷新队列</el-button>
        </div>
      </div>

      <template v-else>
        <!-- 患者条：压成 1 行。身份明细与标签都在「患者详情」弹窗里，条上只留动作按钮 -->
        <PatientBriefBar :patient="currentPatient" :detail="patientDetail" :record="recordForm"
                         :arrive-text="arriveText"
                         @open-charge="showChargeDrawer = true" @open-tags="handleOpenTagDialog"
                         @open-detail="showPatientDetail = true">
        </PatientBriefBar>

        <div class="flex min-h-0 flex-1 gap-3">
          <!-- 次栏：病历（对调后的固定 520px 单列文书，真实 HIS 口径：模板化低频录入，不与医嘱抢宽度） -->
          <div :class="recordColCollapsed ? 'w-9' : 'w-[520px]'"
              class="relative flex min-w-0 shrink-0 flex-col overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm transition-all duration-200">
            <!-- 标题行 = 折叠开关（只在 ≤1600px 可点，见脚本里的 COMPACT_BREAKPOINT）；宽屏退化为纯标题 -->
            <button v-if="compactViewport" type="button"
                    class="flex w-full shrink-0 cursor-pointer items-center justify-between gap-1 border-b border-slate-200 bg-slate-50 text-left transition-colors hover:bg-slate-100"
                    :class="recordCollapsed ? 'flex-col px-1 py-2' : 'px-3 py-2'"
                    :title="recordCollapsed ? '展开病历' : '收起病历面板'"
                    @click="recordCollapsed = !recordCollapsed">
              <span class="text-sm font-bold text-slate-700"
                    :class="recordCollapsed ? '[writing-mode:vertical-rl] text-xs tracking-widest' : ''">病历</span>
              <span v-if="!recordCollapsed" class="truncate text-xs text-slate-400">主诉 / 查体 / 诊断</span>
              <el-icon class="shrink-0 text-slate-400">
                <ArrowRight v-if="recordCollapsed"/>
                <ArrowLeft v-else/>
              </el-icon>
            </button>
            <div v-else
                 class="flex w-full shrink-0 items-center justify-between border-b border-slate-200 bg-slate-50 px-3 py-2">
              <span class="text-sm font-bold text-slate-700">病历</span>
              <span class="text-xs text-slate-400">主诉 / 查体 / 诊断</span>
            </div>
            <!-- 病历：单列连续表单（真实 HIS 门诊病历是一张自上而下的文书：主诉→现病史→过敏史→既往/个人/家族史→体检→诊断→处置） -->
            <div v-if="!recordColCollapsed" class="min-h-0 flex-1 overflow-y-auto p-4 custom-scrollbar record-form">
              <div class="space-y-5">

                <!-- 第一节：主诉与病史 -->
                <section>
                  <div class="mb-2 flex items-center justify-between">
                    <h3 class="text-base font-bold text-slate-800">主诉与病史</h3>
                    <el-button type="primary" link @click="openExtractDialog">
                      智能录入
                    </el-button>
                  </div>
                  <div class="space-y-3">
                    <div>
                      <label class="mb-1 block text-sm font-medium text-slate-600">主诉 <span
                          class="text-red-500">*</span></label>
                      <el-input v-model="recordForm.chiefComplaint" type="textarea" :rows="2" placeholder="请输入主诉"/>
                    </div>
                    <div>
                      <div class="mb-1 flex items-center justify-between">
                        <label class="text-sm font-medium text-slate-600">现病史 <span
                            class="text-red-500">*</span></label>
                        <div class="flex items-center gap-1">
                          <el-button type="primary" link :loading="draftLoading" @click="runDraft">
                            AI 草拟
                          </el-button>
                          <el-button v-if="!recordForm.presentIllness" type="primary" link
                                     @click="recordForm.presentIllness = '无'">填写「无」
                          </el-button>
                        </div>
                      </div>
                      <el-input v-model="recordForm.presentIllness" type="textarea" :rows="3"
                                placeholder="请输入现病史"/>
                    </div>
                    <div class="rounded-md border border-red-200 bg-red-50/50 p-3">
                      <div class="mb-1 flex items-center justify-between">
                        <label class="text-sm font-bold text-red-700">⚠ 过敏史 <span
                            class="text-red-500">*</span></label>
                        <el-button v-if="!recordForm.allergyHistory" type="primary" link
                                   @click="recordForm.allergyHistory = '无'">填写「无」
                        </el-button>
                      </div>
                      <el-input v-model="recordForm.allergyHistory" type="textarea" :rows="2"
                                placeholder="无过敏史请填写 无"/>
                    </div>
                    <div class="space-y-3">
                      <div>
                        <div class="mb-1 flex items-center justify-between">
                          <label class="text-sm font-medium text-slate-600">既往史</label>
                          <el-button v-if="!recordForm.pastHistory" type="primary" link
                                     @click="recordForm.pastHistory = '无'">填写「无」
                          </el-button>
                        </div>
                        <el-input v-model="recordForm.pastHistory" type="textarea" :rows="2" placeholder="既往病史"/>
                      </div>
                      <div>
                        <div class="mb-1 flex items-center justify-between">
                          <label class="text-sm font-medium text-slate-600">个人史</label>
                          <el-button v-if="!recordForm.personalHistory" type="primary" link
                                     @click="recordForm.personalHistory = '无'">填写「无」
                          </el-button>
                        </div>
                        <el-input v-model="recordForm.personalHistory" type="textarea" :rows="2" placeholder="个人史"/>
                      </div>
                      <div>
                        <div class="mb-1 flex items-center justify-between">
                          <label class="text-sm font-medium text-slate-600">家族史</label>
                          <el-button v-if="!recordForm.familyHistory" type="primary" link
                                     @click="recordForm.familyHistory = '无'">填写「无」
                          </el-button>
                        </div>
                        <el-input v-model="recordForm.familyHistory" type="textarea" :rows="2" placeholder="家族史"/>
                      </div>
                    </div>
                  </div>
                </section>

                <div class="border-t border-slate-100"></div>

                <!-- 第二节：体格检查 —— 门诊多数患者不逐系统查体，默认折叠；任一项有内容自动展开 -->
                <section>
                  <button type="button"
                          class="flex w-full cursor-pointer items-center justify-between rounded-md px-1 py-1 text-left transition-colors hover:bg-slate-50"
                          @click="examToggled = !examVisible">
                    <span class="text-base font-bold text-slate-800">体格检查
                      <span v-if="!examVisible" class="ml-1 text-xs font-normal text-slate-400">未填 · 点击展开</span>
                    </span>
                    <span class="flex items-center gap-1 text-xs text-slate-400">
                      {{ examVisible ? '收起' : '展开' }}
                      <el-icon class="transition-transform" :class="examVisible ? 'rotate-180' : ''">
                        <ArrowDown/>
                      </el-icon>
                    </span>
                  </button>
                  <div v-show="examVisible" class="mt-2 space-y-3">
                  <div class="flex items-center justify-between">
                    <span class="text-xs text-slate-400">生命体征为当次实测，不提供快捷填充</span>
                    <el-button type="primary" link size="small" :loading="copyExamLoading"
                               @click="handleCopyLastExam">复制上次查体</el-button>
                  </div>
                  <div>
                    <div class="mb-1 flex items-center justify-between">
                      <label class="text-sm font-medium text-slate-600">生命体征 <span
                          class="text-red-500">*</span></label>
                    </div>
                    <div class="grid grid-cols-2 gap-2">
                      <el-input v-model="recordForm.temperature" placeholder="体温 ℃" size="small"/>
                      <el-input v-model="recordForm.pulse" placeholder="脉搏 次/分" size="small"
                      />
                      <el-input v-model="recordForm.respiration" placeholder="呼吸 次/分" size="small"
                      />
                      <el-input v-model="recordForm.systolicPressure" placeholder="收缩压 mmHg" size="small"
                      />
                    </div>
                    <el-input v-model="recordForm.diastolicPressure" placeholder="舒张压 mmHg" size="small"
                              class="mt-2"
                    />
                  </div>
                  <div>
                    <div class="mb-1 flex items-center justify-between">
                      <label class="text-sm font-medium text-slate-600">一般情况</label>
                      <el-button v-if="!recordForm.generalCondition" type="primary" link size="small"
                                 @click="recordForm.generalCondition = '正常'">填写「正常」
                      </el-button>
                    </div>
                    <el-input v-model="recordForm.generalCondition" placeholder="发育、营养、神志等" size="small"
                    />
                  </div>
                  <div>
                    <div class="mb-1 flex items-center justify-between">
                      <label class="text-sm font-medium text-slate-600">皮肤黏膜</label>
                      <el-button v-if="!recordForm.skinMucosa" type="primary" link size="small"
                                 @click="recordForm.skinMucosa = '正常'">填写「正常」
                      </el-button>
                    </div>
                    <el-input v-model="recordForm.skinMucosa" type="textarea" :autosize="{ minRows: 1, maxRows: 6 }"
                              placeholder="皮肤颜色、湿度等"
                    />
                  </div>
                  <div>
                    <div class="mb-1 flex items-center justify-between">
                      <label class="text-sm font-medium text-slate-600">头颈部</label>
                      <el-button v-if="!recordForm.headNeck" type="primary" link size="small"
                                 @click="recordForm.headNeck = '正常'">填写「正常」
                      </el-button>
                    </div>
                    <el-input v-model="recordForm.headNeck" type="textarea" :autosize="{ minRows: 1, maxRows: 6 }"
                              placeholder="头颅、眼、耳等"
                    />
                  </div>
                  <div>
                    <div class="mb-1 flex items-center justify-between">
                      <label class="text-sm font-medium text-slate-600">胸肺</label>
                      <el-button v-if="!recordForm.chestLung" type="primary" link size="small"
                                 @click="recordForm.chestLung = '正常'">填写「正常」
                      </el-button>
                    </div>
                    <el-input v-model="recordForm.chestLung" type="textarea" :autosize="{ minRows: 1, maxRows: 6 }"
                              placeholder="胸廓、叩诊、听诊等"
                    />
                  </div>
                  <div>
                    <div class="mb-1 flex items-center justify-between">
                      <label class="text-sm font-medium text-slate-600">心脏</label>
                      <el-button v-if="!recordForm.heart" type="primary" link size="small"
                                 @click="recordForm.heart = '正常'">填写「正常」
                      </el-button>
                    </div>
                    <el-input v-model="recordForm.heart" type="textarea" :autosize="{ minRows: 1, maxRows: 6 }"
                              placeholder="心率、心律等"
                    />
                  </div>
                  <div>
                    <div class="mb-1 flex items-center justify-between">
                      <label class="text-sm font-medium text-slate-600">腹部</label>
                      <el-button v-if="!recordForm.abdomen" type="primary" link size="small"
                                 @click="recordForm.abdomen = '正常'">填写「正常」
                      </el-button>
                    </div>
                    <el-input v-model="recordForm.abdomen" type="textarea" :autosize="{ minRows: 1, maxRows: 6 }"
                              placeholder="压痛、肝脾等"
                    />
                  </div>
                  <div>
                    <div class="mb-1 flex items-center justify-between">
                      <label class="text-sm font-medium text-slate-600">脊柱四肢</label>
                      <el-button v-if="!recordForm.spineLimbs" type="primary" link size="small"
                                 @click="recordForm.spineLimbs = '正常'">填写「正常」
                      </el-button>
                    </div>
                    <el-input v-model="recordForm.spineLimbs" type="textarea" :autosize="{ minRows: 1, maxRows: 6 }"
                              placeholder="脊柱、关节等"
                    />
                  </div>
                  <div>
                    <div class="mb-1 flex items-center justify-between">
                      <label class="text-sm font-medium text-slate-600">神经系统</label>
                      <el-button v-if="!recordForm.nervousSystem" type="primary" link size="small"
                                 @click="recordForm.nervousSystem = '正常'">填写「正常」
                      </el-button>
                    </div>
                    <el-input v-model="recordForm.nervousSystem" type="textarea"
                              :autosize="{ minRows: 1, maxRows: 6 }"
                              placeholder="生理反射等"
                    />
                  </div>
                  <div>
                    <div class="mb-1 flex items-center justify-between">
                      <label class="text-sm font-medium text-slate-600">专科检查</label>
                      <el-button v-if="!recordForm.specialistExam" type="primary" link size="small"
                                 @click="recordForm.specialistExam = '正常'">填写「正常」
                      </el-button>
                    </div>
                    <el-input v-model="recordForm.specialistExam" type="textarea"
                              :autosize="{ minRows: 1, maxRows: 6 }"
                              placeholder="专科检查所见"
                    />
                  </div>
                  </div>
                </section>

                <div class="border-t border-slate-100"></div>

                <!-- 第三节：诊断与治疗 -->
                <section>
                  <div class="mb-2 text-base font-bold text-slate-800">诊断与治疗</div>
                  <div class="space-y-3">
                  <div><label class="mb-1 block text-sm font-medium text-slate-600">诊断 <span
                      class="text-red-500">*</span></label>
                    <el-select v-model="recordForm.diagnosis" filterable remote reserve-keyword
                               placeholder="输入疾病名称或编码搜索" :remote-method="handleIcd10Search"
                               :loading="icd10Loading" class="w-full"
                               @change="handleIcd10Select">
                      <el-option v-for="item in icd10Results" :key="item.id"
                                 :label="`${item.icdCode} - ${item.icdName}`" :value="item.icdName">
                        <div class="flex items-center justify-between"><span class="font-mono text-sm text-blue-600">{{
                            item.icdCode
                          }}</span><span class="text-sm text-slate-700">{{ item.icdName }}</span></div>
                        <div class="text-xs text-slate-400">{{ item.icdCategory }}</div>
                      </el-option>
                    </el-select>
                    <!-- 常用诊断快捷选择 -->
                    <div v-if="myDiagTemplates.length > 0" class="mt-2 flex flex-wrap gap-1.5">
                      <span class="text-[10px] text-slate-400 leading-5">常用：</span>
                      <span v-for="tpl in myDiagTemplates" :key="tpl.icdCode"
                            class="cursor-pointer rounded bg-blue-50 px-1.5 py-0.5 text-[11px] text-blue-600 transition-colors hover:bg-blue-100"
                            @click="handleQuickSelectDiag(tpl)">
                        {{ tpl.icdName }}
                      </span>
                    </div>
                    <div v-if="recordForm.diagnosisCode" class="mt-1 flex items-center gap-2 text-xs text-slate-400">
                      <span>编码：{{ recordForm.diagnosisCode }}</span>
                      <el-button type="primary" link size="small" @click="handleAddDiagToTemplate">
                        <el-icon class="mr-0.5">
                          <Plus/>
                        </el-icon>
                        添加到常用
                      </el-button>
                    </div>
                    <el-button type="primary" link size="small" class="mt-1" :loading="icdPredictionLoading"
                               @click="handlePredictIcd">
                      <el-icon class="mr-0.5">
                        <MagicStick/>
                      </el-icon>
                      智能预测
                    </el-button>
                  </div>

                  <!-- ICD-10预测结果面板 -->
                  <div v-if="showPrediction" class="rounded-lg border border-blue-200 bg-blue-50 p-2.5">
                    <div class="mb-1.5 flex items-center justify-between">
                      <span class="text-xs font-bold text-blue-700">
                        <el-icon class="mr-0.5"><MagicStick/></el-icon>预测结果
                      </span>
                      <el-button type="primary" link size="small" @click="showPrediction = false">收起</el-button>
                    </div>
                    <div v-if="icdPredictionLoading" class="py-3 text-center text-xs text-blue-500">分析中...</div>
                    <div v-else-if="icdPredictions.length === 0" class="py-3 text-center text-xs text-slate-400">
                      未找到匹配编码
                    </div>
                    <div v-else class="max-h-48 space-y-1.5 overflow-y-auto">
                      <div v-for="(item, idx) in icdPredictions" :key="item.icdCode"
                           class="cursor-pointer rounded border border-slate-200 bg-white p-2 transition-colors hover:border-blue-300"
                           @click="handleSelectPrediction(item)">
                        <div class="flex items-center justify-between">
                          <div class="flex items-center gap-1.5">
                            <span class="rounded bg-blue-100 px-1 py-0.5 text-[10px] font-bold text-blue-700">#{{
                                idx + 1
                              }}</span>
                            <span class="font-mono text-xs font-medium text-blue-600">{{ item.icdCode }}</span>
                            <span class="text-xs font-medium text-slate-900">{{ item.icdName }}</span>
                          </div>
                        </div>
                        <div class="mt-1 flex items-center gap-3 text-[10px] text-slate-500">
                          <span class="text-emerald-600">DRG {{ item.drgWeight }}</span>
                          <span class="font-medium text-amber-600">¥{{ item.estimatedCost }}</span>
                        </div>
                      </div>
                    </div>
                    <div v-if="icdPredictions.length > 0"
                         class="mt-1.5 rounded bg-amber-50 px-2 py-1 text-[10px] text-amber-700">
                      ⚠ 低编结算不足，高编触发审核
                    </div>
                  </div>

                  <div>
                    <div class="mb-1 flex items-center justify-between">
                      <label class="text-sm font-medium text-slate-600">辅助检查</label>
                      <el-button v-if="!recordForm.auxiliaryExam" type="primary" link size="small"
                                 @click="recordForm.auxiliaryExam = '暂无'">填写「暂无」
                      </el-button>
                    </div>
                    <el-input v-model="recordForm.auxiliaryExam" type="textarea"
                              :autosize="{ minRows: 2, maxRows: 6 }"
                              placeholder="实验室检查、影像学检查结果等"/>
                  </div>
                  <div>
                    <div class="mb-1 flex items-center justify-between">
                      <label class="text-sm font-medium text-slate-600">治疗方案</label>
                      <el-button v-if="!recordForm.treatmentPlan" type="primary" link size="small"
                                 @click="recordForm.treatmentPlan = '遵医嘱'">填写「遵医嘱」
                      </el-button>
                    </div>
                    <el-input v-model="recordForm.treatmentPlan" type="textarea"
                              :autosize="{ minRows: 3, maxRows: 8 }"
                              placeholder="治疗方案、用药建议等"/>
                  </div>
                  </div>
                </section>
              </div>
            </div>
            <!-- 病历已提交水印（折叠态不渲染：36px 竖条上 giant 旋转文字会被裁成色块） -->
            <div v-if="recordForm.recordStatus === 2 && !recordColCollapsed"
                 class="pointer-events-none absolute inset-0 z-50 flex items-center justify-center">
              <div class="rotate-[-30deg] text-4xl font-black text-red-500/20 select-none">
                病历已提交
              </div>
            </div>
            <!-- 已审核水印 -->
            <div v-if="recordForm.reviewStatus === 2 && !recordColCollapsed"
                 class="pointer-events-none absolute inset-0 z-50 flex items-center justify-center">
              <div class="rotate-[-30deg] text-4xl font-black text-emerald-500/20 select-none">
                已审核
              </div>
            </div>
          </div>

          <!--
            主栏：**一整张卡**，flex-1 吃剩余宽度 —— 医嘱是医生的主工作区（真实 HIS 口径：
            开药检索表要摊药名/规格/单位/单价/库存/用法用量等十几列，病历只是窄条文书）。
            下面的处方/检查申请/检验申请/辅助诊疗/用药安全审查全是它标题下的分节，不是并列小卡。
            （老王 2026-09-21：原来标题单独一张卡、下面每块又各是一张卡，中间还留 12px 缝，
              看着像五件互不相干的东西；医生要的是「一栏医嘱、里面分节」。）
            边界只由这张卡给：内部各节只用一条分隔线（见 OrderPanel.vue），不再各自带边框。
            本栏常驻不可折叠；窄屏逃生门在病历栏上（见 COMPACT_BREAKPOINT）。
          -->
          <div class="flex min-w-0 flex-1 flex-col overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
            <!-- 标题栏与左栏「病历」同构：bg-slate-50 + border-b + px-3 py-2 + 右侧一句话说明 -->
            <div
                 class="flex w-full shrink-0 items-center justify-between border-b border-slate-200 bg-slate-50 px-3 py-2">
              <span class="text-sm font-bold text-slate-700">医嘱</span>
              <span class="truncate text-xs text-slate-400">处方 / 检查申请 / 检验申请</span>
            </div>
            <!-- 内容区与左栏「病历」同构：p-3 + 分节之间 12px（那里是 gap-3，这里是 space-y-3） -->
            <div class="min-h-0 flex-1 space-y-3 overflow-y-auto p-3 custom-scrollbar">
              <OrderPanel title="处方" :count-text="prescriptionList.length + ' 张'">
                <div>
              <!-- 三个处方类型 tab -->
              <el-tabs v-model="currentPrescriptionType" class="!mb-0">
                <el-tab-pane label="西药" :name="1" />
                <el-tab-pane label="中成药" :name="2" />
                <el-tab-pane label="中药饮片" :name="3" />
              </el-tabs>
              <!-- 当前处方操作栏 -->
              <div v-if="currentPatient.queueStatus === 3"
                   class="mb-2 mt-2 flex items-center justify-between rounded-md bg-slate-50 px-3 py-2">
                <span class="text-xs text-slate-500">
                  {{ prescriptionTypeLabel(currentPrescriptionType) }}处方 · {{ prescriptionList.filter(p => p.prescriptionType === currentPrescriptionType).length > 0 ? prescriptionList.filter(p => p.prescriptionType === currentPrescriptionType)[0].details?.length || 0 : 0 }}种药品
                </span>
                <div class="flex items-center gap-2">
                  <!-- 饮片方的剂数/煎服方式属于整张方（后端存 biz_prescription.dose_count/decoct_flag），
                       放在处方级操作栏而不是每味药一行 —— 一剂药里 12 味不可能各开各的剂数 -->
                  <template v-if="currentPrescriptionType === 3">
                    <label class="text-xs text-slate-600" data-testid="ws-dose-label">剂数<span class="text-red-500">*</span></label>
                    <el-input-number v-model="prescriptionForm.doseCount" :min="1" :max="30"
                                     size="small" controls-position="right" style="width: 100px"
                                     data-testid="ws-dose-count"/>
                    <label class="text-xs text-slate-600">煎服<span class="text-red-500">*</span></label>
                    <el-radio-group v-model="prescriptionForm.decoctFlag" size="small">
                      <el-radio v-for="d in tcmDecoctFlagDict" :key="d.dictValue"
                                :value="Number(d.dictValue)" :data-testid="`ws-decoct-${d.dictValue}`">
                        {{ d.dictLabel }}
                      </el-radio>
                    </el-radio-group>
                  </template>
                  <el-button size="small" @click="showRxTemplateDialog = true">使用模板</el-button>
                  <el-button v-perm="'opd:doctorWorkstation:add'" type="primary" size="small" :icon="Plus" @click="handleAddEmptyDrug">新增药品</el-button>
                </div>
              </div>
              <div class="space-y-3 py-2">
                <!-- 处方明细 -->
                <div class="space-y-2">
                  <!-- 可编辑列表 (queueStatus=3 结诊中) -->
                  <template v-if="currentPatient.queueStatus === 3">
                    <div v-for="(item, idx) in prescriptionForm.details" :key="idx"
                         class="rounded-lg border border-slate-200 p-2">
                      <div class="mb-1.5 flex items-center justify-between">
                          <span class="text-xs font-medium text-slate-700">
                            {{ item.drugName || '药品 #' + (idx + 1) }}
                            <template v-if="item.specification">({{ item.specification }})</template>
                          </span>
                        <div class="flex items-center gap-2">
                          <!-- 医嘱栏变宽后价格上卡头：饮片 price 已是每克价（选药时换算过），乘实发克数即小计 -->
                          <span v-if="item.drugName" class="text-xs text-slate-500">
                            ¥{{ Number(item.price || 0).toFixed(currentPrescriptionType === 3 ? 3 : 2) }}<span
                              class="text-slate-400">/{{ currentPrescriptionType === 3 ? 'g' : (item.unit || '盒') }}</span>
                            · 小计 <span class="font-medium text-slate-800">¥{{ (Number(item.price || 0) * Number(item.quantity || 0)).toFixed(2) }}</span>
                          </span>
                          <el-button v-perm="'opd:doctorWorkstation:delete'" type="danger" link size="small" @click="handleRemoveDrug(idx)">删除</el-button>
                        </div>
                      </div>

                      <!-- 西药处方 -->
                      <template v-if="currentPrescriptionType === 1">
                        <div class="flex flex-wrap items-center gap-x-3 gap-y-1.5">
                          <div class="flex items-center gap-1.5">
                            <label class="text-xs text-slate-600 whitespace-nowrap">药品<span class="text-red-500">*</span></label>
                            <el-select v-model="item.drugId" filterable style="width: 220px"
                                       placeholder="搜索药品" :loading="drugLoading"
                                       @change="(val: any) => handleDrugSelectForRecord(item, val)">
                              <el-option v-for="drug in drugResults" :key="drug.id"
                                         :label="`${drug.drugName} (${drug.specification})`" :value="drug.id">
                                <div class="flex items-center justify-between">
                                  <span class="text-sm font-medium text-slate-900">{{ drug.drugName }}</span>
                                  <span class="text-xs text-slate-400">{{ drug.dosageForm || drug.specification }}</span>
                                </div>
                                <div class="text-xs text-slate-400">{{ drug.specification }} | ¥{{ drug.retailPrice }}/{{ drug.unit }}</div>
                              </el-option>
                            </el-select>
                          </div>
                          <div class="flex items-center gap-1.5">
                            <label class="text-xs text-slate-600 whitespace-nowrap">单次用量<span class="text-red-500">*</span></label>
                            <el-input v-model="item.singleDosage" style="width: 70px" placeholder="用量"/>
                          </div>
                          <div class="flex items-center gap-1.5">
                            <label class="text-xs text-slate-600 whitespace-nowrap">频次<span class="text-red-500">*</span></label>
                            <el-select v-model="item.frequency" style="width: 100px" placeholder="频次">
                              <el-option label="一日一次" value="一日一次"/>
                              <el-option label="一日两次" value="一日两次"/>
                              <el-option label="一日三次" value="一日三次"/>
                              <el-option label="需要时" value="需要时"/>
                            </el-select>
                          </div>
                          <div class="flex items-center gap-1.5">
                            <label class="text-xs text-slate-600 whitespace-nowrap">给药途径<span class="text-red-500">*</span></label>
                            <el-select v-model="item.route" style="width: 100px" placeholder="途径">
                              <el-option label="口服" value="口服"/>
                              <el-option label="静脉注射" value="静脉注射"/>
                              <el-option label="肌肉注射" value="肌肉注射"/>
                              <el-option label="外用" value="外用"/>
                            </el-select>
                          </div>
                          <div class="flex items-center gap-1.5">
                            <label class="text-xs text-slate-600 whitespace-nowrap">总量<span class="text-red-500">*</span></label>
                            <el-input-number v-model="item.quantity" :min="1" controls-position="right" style="width: 80px"/>
                          </div>
                          <div class="flex items-center gap-1.5">
                            <label class="text-xs text-slate-600 whitespace-nowrap">天数<span class="text-red-500">*</span></label>
                            <el-input-number v-model="item.duration" :min="1" :max="90" controls-position="right" style="width: 70px"/>
                          </div>
                        </div>
                      </template>

                      <!-- 中成药处方 -->
                      <template v-else-if="currentPrescriptionType === 2">
                        <div class="flex flex-wrap items-center gap-x-3 gap-y-1.5">
                          <div class="flex items-center gap-1.5">
                            <label class="text-xs text-slate-600 whitespace-nowrap">药品<span class="text-red-500">*</span></label>
                            <el-select v-model="item.drugId" filterable style="width: 220px"
                                       placeholder="搜索中成药" :loading="drugLoading"
                                       @change="(val: any) => handleDrugSelectForRecord(item, val)">
                              <el-option v-for="drug in drugResults" :key="drug.id"
                                         :label="`${drug.drugName} (${drug.specification})`" :value="drug.id">
                                <div class="flex items-center justify-between">
                                  <span class="text-sm font-medium text-slate-900">{{ drug.drugName }}</span>
                                  <span class="text-xs text-slate-400">{{ drug.dosageForm || drug.specification }}</span>
                                </div>
                                <div class="text-xs text-slate-400">{{ drug.specification }} | ¥{{ drug.retailPrice }}/{{ drug.unit }}</div>
                              </el-option>
                            </el-select>
                          </div>
                          <div class="flex items-center gap-1.5">
                            <label class="text-xs text-slate-600 whitespace-nowrap">用法<span class="text-red-500">*</span></label>
                            <el-select v-model="item.route" style="width: 180px" placeholder="用法">
                              <el-option label="口服" value="口服"/>
                              <el-option label="含服" value="含服"/>
                              <el-option label="外用" value="外用"/>
                              <el-option label="嚼服" value="嚼服"/>
                            </el-select>
                          </div>
                          <div class="flex items-center gap-1.5">
                            <label class="text-xs text-slate-600 whitespace-nowrap">一次用量<span class="text-red-500">*</span></label>
                            <el-input v-model="item.singleDosage" style="width: 80px" placeholder="如: 2粒"/>
                          </div>
                          <div class="flex items-center gap-1.5">
                            <label class="text-xs text-slate-600 whitespace-nowrap">频次<span class="text-red-500">*</span></label>
                            <el-select v-model="item.frequency" style="width: 100px" placeholder="频次">
                              <el-option label="一日一次" value="一日一次"/>
                              <el-option label="一日两次" value="一日两次"/>
                              <el-option label="一日三次" value="一日三次"/>
                            </el-select>
                          </div>
                          <div class="flex items-center gap-1.5">
                            <label class="text-xs text-slate-600 whitespace-nowrap">总量<span class="text-red-500">*</span></label>
                            <el-input-number v-model="item.quantity" :min="1" controls-position="right" style="width: 80px"/>
                          </div>
                          <div class="flex items-center gap-1.5">
                            <label class="text-xs text-slate-600 whitespace-nowrap">天数<span class="text-red-500">*</span></label>
                            <el-input-number v-model="item.duration" :min="1" :max="90" controls-position="right" style="width: 70px"/>
                          </div>
                        </div>
                      </template>

                      <!-- 中药饮片处方 -->
                      <template v-else-if="currentPrescriptionType === 3">
                        <div class="flex flex-wrap items-center gap-x-3 gap-y-1.5">
                          <div class="flex items-center gap-1.5">
                            <label class="text-xs text-slate-600 whitespace-nowrap">药名<span class="text-red-500">*</span></label>
                            <el-select v-model="item.drugId" filterable style="width: 200px"
                                       data-testid="ws-herb-drug"
                                       placeholder="搜索中药饮片" :loading="drugLoading"
                                       @change="(val: any) => handleDrugSelectForRecord(item, val)">
                              <el-option v-for="drug in drugResults" :key="drug.id"
                                         :label="drug.drugName" :value="drug.id">
                                <div class="flex items-center justify-between">
                                  <span class="text-sm font-medium text-slate-900">{{ drug.drugName }}</span>
                                  <span class="text-xs text-slate-400">¥{{ drug.retailPrice }}/{{ drug.unit }}</span>
                                </div>
                                <div class="text-xs text-slate-400">{{ drug.specification }} | ¥{{ drug.retailPrice }}/{{ drug.unit }}<template v-if="drug.gramPerUnit"> ≈ ¥{{ tcmPerGramPrice(drug).toFixed(3) }}/g</template></div>
                              </el-option>
                            </el-select>
                          </div>
                          <div class="flex items-center gap-1.5">
                            <label class="text-xs text-slate-600 whitespace-nowrap">每剂克数<span class="text-red-500">*</span></label>
                            <el-input v-model="item.singleDosage" style="width: 70px" placeholder="如: 15"
                                      data-testid="ws-herb-grams" @change="applyTcmGrams(item)"/>
                            <span class="text-xs text-slate-400">g</span>
                          </div>
                          <div class="flex items-center gap-1.5">
                            <label class="text-xs text-slate-600 whitespace-nowrap">实发</label>
                            <!-- 总克数 = 每剂克数 × 剂数，只读；后端按同一口径重算并扣库 -->
                            <span class="w-[70px] text-xs font-medium text-slate-800" data-testid="ws-herb-total">
                              {{ item.quantity || 0 }} g
                            </span>
                            <span class="text-xs text-slate-400" data-testid="ws-herb-amount">¥{{ Number(item.amount || 0).toFixed(2) }}</span>
                          </div>
                          <div class="flex items-center gap-1.5">
                            <label class="text-xs text-slate-600 whitespace-nowrap">煎法<span class="text-red-500">*</span></label>
                            <el-select v-model="item.route" style="width: 100px" data-testid="ws-herb-route" placeholder="煎法">
                              <el-option v-for="d in tcmMethodDict" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue"/>
                            </el-select>
                          </div>
                          <div class="flex items-center gap-1.5">
                            <label class="text-xs text-slate-600 whitespace-nowrap">用法<span class="text-red-500">*</span></label>
                            <el-select v-model="item.frequency" style="width: 180px" placeholder="用法">
                              <el-option label="每日一剂" value="每日一剂"/>
                              <el-option label="每日两剂" value="每日两剂"/>
                              <el-option label="隔日一剂" value="隔日一剂"/>
                            </el-select>
                          </div>
                        </div>
                      </template>

                      <!-- 常用药品快捷选择 -->
                      <div v-if="myRxTemplates.length > 0" class="mt-2 border-t border-slate-100 pt-2">
                        <div class="mb-1 text-[10px] font-medium text-slate-500">常用药品</div>
                        <div class="flex flex-wrap gap-1">
                          <span v-for="tpl in myRxTemplates.slice(0, 5)" :key="tpl.id"
                                class="cursor-pointer rounded bg-blue-50 px-1.5 py-0.5 text-[11px] text-blue-600 transition-colors hover:bg-blue-100"
                                @click="handleApplyRxTemplateToRecord(item, tpl)">
                            {{ tpl.templateName }}
                          </span>
                        </div>
                      </div>
                    </div>
                  </template>
                  <!-- 纯列表 (非结诊中) -->
                  <template v-else>
                    <div v-for="p in prescriptionList.filter(p => p.prescriptionType === currentPrescriptionType)" :key="p.id || p.prescriptionType">
                      <div v-if="p.details && p.details.length > 0" class="mb-3">
                        <div class="mb-2 text-xs text-slate-500">
                          {{ prescriptionTypeLabel(p.prescriptionType) }}处方 · {{ p.details.length }}种药品
                          <template v-if="p.prescriptionType === 3 && p.doseCount"> · {{ p.doseCount }}剂</template>
                        </div>
                        <!-- 医嘱栏对调成 flex-1 主工作区后（2026-09-26 方案A），表格按真实 HIS 处方全列铺开：
                             药名/规格/厂家/单价/金额是医生开完方核对与患者费用解释要看的东西，
                             原先 480px 装不下才砍掉。min-width 让列随栏宽弹性摊开，不留大片空白 -->
                        <el-table :data="p.details" size="small" border>
                          <el-table-column prop="drugName" label="药品名称" min-width="150" show-overflow-tooltip/>
                          <el-table-column prop="specification" label="规格" min-width="110" show-overflow-tooltip/>
                          <el-table-column prop="manufacturer" label="生产厂家" min-width="150" show-overflow-tooltip/>
                          <el-table-column prop="quantity" label="数量" width="70" align="center"/>
                          <el-table-column prop="unit" label="单位" width="56" align="center"/>
                          <el-table-column label="单价" width="80" align="right">
                            <template #default="{ row }">{{ Number(row.price || 0).toFixed(2) }}</template>
                          </el-table-column>
                          <el-table-column label="金额" width="86" align="right">
                            <template #default="{ row }">
                              <span class="font-medium text-slate-800">{{ Number(row.amount || 0).toFixed(2) }}</span>
                            </template>
                          </el-table-column>
                          <el-table-column prop="singleDosage" label="单次用量" width="86"/>
                          <el-table-column prop="frequency" label="频次" width="86"/>
                          <el-table-column prop="route" label="途径" width="70"/>
                          <el-table-column prop="duration" label="天数" width="56" align="center"/>
                        </el-table>
                      </div>
                    </div>
                    <div v-if="prescriptionList.filter(p => p.prescriptionType === currentPrescriptionType).length === 0 || prescriptionList.filter(p => p.prescriptionType === currentPrescriptionType).every(p => !p.details || p.details.length === 0)"
                         class="py-8 text-center text-sm text-slate-400">
                      {{ patientDataLoading ? '处方加载中…' : `暂无${prescriptionTypeLabel(currentPrescriptionType)}处方` }}
                    </div>
                  </template>
                </div>
              </div>
                </div>
              </OrderPanel>
              <OrderPanel title="检查申请" :count-text="inspectionRecords.length + ' 项'">
                <div class="relative">
              <div v-if="currentPatient.queueStatus === 3"
                   class="sticky top-0 z-10 flex justify-end py-2">
                <el-button v-perm="'opd:doctorWorkstation:add'" type="primary" size="small" :icon="Plus" @click="handleAddEmptyInspection">新增检查
                </el-button>
              </div>
              <div class="space-y-3 py-2">
                <!-- 检查申请列表 -->
                <div v-if="inspectionRecords.length > 0">
                  <div class="space-y-2">
                    <!-- 可编辑列表 (queueStatus=3 就诊中) -->
                    <template v-if="currentPatient.queueStatus === 3">
                      <div v-for="(item, idx) in inspectionRecords"
                           :key="item.id || ('pending-inspection-' + item._pendingIdx)"
                           class="rounded-lg border border-slate-200 p-2">
                        <div class="mb-1.5 flex items-center justify-between">
                          <div class="flex items-center gap-2">
                            <span class="text-xs font-medium text-slate-700">检查申请 #{{ idx + 1 }}</span>
                            <el-tag v-if="item.id" :type="applyTagType(item)" size="small">
                              {{ item.execStatusText }}
                            </el-tag>
                            <el-tag v-else type="info" size="small">选项目后即时开单</el-tag>
                            <el-tag v-if="item.critical" type="danger" size="small" effect="dark">危急值</el-tag>
                            <el-tooltip v-if="item.signStatus === 1" :content="`开单医师 ${item.doctorName} · ${item.signedTime || ''}，签名即锁定`">
                              <el-tag type="success" size="small" effect="plain" data-testid="p5-ins-sign-tag">已电子签名</el-tag>
                            </el-tooltip>
                            <el-tag v-else-if="item.signStatus === 2" type="warning" size="small" effect="plain">签名已作废</el-tag>
                          </div>
                          <div class="flex items-center gap-1">
                            <el-button v-perm="'opd:doctorWorkstation:edit'" type="primary" link size="small" :disabled="!item.id"
                                       @click="handleSaveInspectionRow(item)">
                              保存修改
                            </el-button>
                            <el-button v-perm="'opd:doctorWorkstation:delete'" type="danger" link size="small"
                                       @click="handleDeleteInspectionRecord(idx)">
                              删除
                            </el-button>
                          </div>
                        </div>
                        <div class="flex flex-wrap items-center gap-x-3 gap-y-1.5">
                          <div class="flex items-center gap-1.5">
                            <label class="text-xs text-slate-600 whitespace-nowrap">检查项目</label>
                            <el-select v-model="item.inspectionItemName" filterable
                                       placeholder="搜索检查项目"
                                       :loading="inspectionItemLoading" style="width: 180px"
                                       @change="(val: any) => handleInspectionItemSelectForRecord(item, val)">
                              <el-option v-for="opt in inspectionItemResults" :key="opt.id"
                                         :label="`${opt.itemCode} - ${opt.itemName}`" :value="opt.id">
                                <div class="flex items-center justify-between">
                                  <span class="text-sm font-medium text-slate-900">{{ opt.itemName }}</span>
                                  <span class="text-xs text-slate-400">¥{{ opt.price }}</span>
                                </div>
                                <div class="text-xs text-slate-400">{{ opt.itemCode }} · {{ opt.bodyPart }}</div>
                              </el-option>
                            </el-select>
                          </div>
                          <div class="flex items-center gap-1.5">
                            <label class="text-xs text-slate-600 whitespace-nowrap">部位</label>
                            <el-input v-model="item.bodyPart" style="width: 150px" placeholder="检查部位"/>
                          </div>
                          <div class="flex items-center gap-1.5">
                            <el-switch v-model="item.isEmergency" :active-value="1" :inactive-value="0"
                                       size="small" active-text="急诊"/>
                          </div>
                        </div>
                        <div class="mt-1.5 flex items-start gap-1.5">
                          <label class="mt-2 text-xs text-slate-600 whitespace-nowrap">注意事项</label>
                          <el-input v-model="item.preparation" type="textarea" :rows="2" style="width: 400px"
                                    placeholder="注意事项（可选）"/>
                        </div>
                        <div class="mt-1.5 flex items-center gap-1.5">
                          <label class="text-xs text-slate-600 whitespace-nowrap">检查目的</label>
                          <el-input v-model="item.inspectionPurpose" type="textarea" :rows="2" style="width: 400px"
                                    placeholder="检查目的"/>
                        </div>

                        <!-- 常用检查申请快捷选择 -->
                        <div v-if="myInspectionTemplates.length > 0" class="mt-2 border-t border-slate-100 pt-2">
                          <div class="mb-1 text-[10px] font-medium text-slate-500">常用检查</div>
                          <div class="flex flex-wrap gap-1">
                            <el-button v-for="tpl in myInspectionTemplates" :key="tpl.id" size="small" link
                                       type="primary" @click="handleApplyInspectionTemplateToRecord(item, tpl)">
                              {{ tpl.templateName }}
                            </el-button>
                          </div>
                        </div>
                      </div>
                    </template>
                    <!-- 纯列表 (非就诊中) -->
                    <template v-else>
                      <div v-for="(item, idx) in inspectionRecords"
                           :key="item.id || ('pending-inspection-' + item._pendingIdx)"
                           :class="[
                             'rounded-lg border p-3',
                             item.critical
                                 ? 'border-red-300 bg-red-50'
                                 : (item.execStatusText === '待缴费' ? 'border-amber-200 bg-amber-50' : 'border-slate-200')
                           ]">
                        <div class="flex items-center justify-between">
                          <div class="flex items-center gap-2">
                            <span class="text-sm font-medium text-slate-900">{{ item.inspectionItemName }}</span>
                            <el-tag :type="applyTagType(item)" size="small">{{ item.execStatusText }}</el-tag>
                            <el-tag v-if="item.critical" type="danger" size="small" effect="dark">危急值</el-tag>
                            <el-tag v-if="item.isEmergency === 1" type="warning" size="small">急诊</el-tag>
                            <el-tag v-if="item.signStatus === 1" type="success" size="small" effect="plain">已电子签名·{{ item.doctorName }}</el-tag>
                            <el-tag v-else-if="item.signStatus === 2" type="warning" size="small" effect="plain">签名已作废</el-tag>
                          </div>
                          <div class="flex items-center gap-2">
                            <span class="text-xs text-slate-400">{{ item.createTime }}</span>
                            <el-button v-if="item.execRecordId" type="primary" link size="small"
                                       @click="handleViewInspectionReport(item)">
                              查看报告
                            </el-button>
                            <!-- 批次E/E6：结果已回 → 一键建复诊（关联本次病历，免挂号费） -->
                            <el-button v-if="canCreateRevisit(item)" type="warning" link size="small"
                                       @click="handleCreateRevisit(item)">
                              建复诊
                            </el-button>
                            <el-button v-if="item.canDelete" v-perm="'opd:doctorWorkstation:delete'" type="danger" link size="small"
                                       @click="handleDeleteInspectionRecord(idx)">
                              删除
                            </el-button>
                            <el-tooltip v-else-if="item.deleteBlockReason" :content="item.deleteBlockReason">
                              <span class="cursor-help text-xs text-slate-300">删除</span>
                            </el-tooltip>
                          </div>
                        </div>
                        <div v-if="item.bodyPart" class="mt-1 text-xs text-slate-500">部位：{{ item.bodyPart }}</div>
                        <div v-if="item.inspectionPurpose" class="mt-1 text-xs text-slate-500">
                          目的：{{ item.inspectionPurpose }}
                        </div>
                        <div v-if="item.specialRequirements" class="mt-1 text-xs text-slate-500">
                          注意事项：{{ item.specialRequirements }}
                        </div>
                        <div v-if="item.critical" class="mt-2 rounded bg-red-100 p-2 text-xs text-red-700">
                          该检查/相关检验出现危急值，请优先处理并及时通知患者。
                        </div>
                      </div>
                    </template>
                  </div>
                </div>
                <!-- 批次E/F：检查面板原来空着一整块，医生分不清"没开"还是"没加载出来" -->
                <div v-else class="py-8 text-center text-sm text-slate-400">
                  {{ patientDataLoading ? '检查申请加载中…' : '本次就诊还没有检查申请' }}
                </div>
              </div>
                </div>
              </OrderPanel>
              <OrderPanel title="检验申请" :count-text="laboratoryRecords.length + ' 项'">
                <div class="relative">
              <div v-if="currentPatient.queueStatus === 3"
                   class="sticky top-0 z-10 flex justify-end py-2">
                <el-button v-perm="'opd:doctorWorkstation:add'" type="primary" size="small" :icon="Plus" @click="handleAddEmptyLaboratory">新增检验
                </el-button>
              </div>
              <div class="space-y-3 py-2">
                <!-- 检验申请列表 -->
                <div v-if="laboratoryRecords.length > 0">
                  <div class="space-y-2">
                    <!-- 可编辑列表 (queueStatus=3 结诊中) -->
                    <template v-if="currentPatient.queueStatus === 3">
                      <div v-for="(item, idx) in laboratoryRecords"
                           :key="item.id || ('pending-laboratory-' + item._pendingIdx)"
                           class="rounded-lg border border-slate-200 p-2">
                        <div class="mb-1.5 flex items-center justify-between">
                          <div class="flex items-center gap-2">
                            <span class="text-xs font-medium text-slate-700">检验申请 #{{ idx + 1 }}</span>
                            <el-tag v-if="item.id" :type="applyTagType(item)" size="small">
                              {{ item.execStatusText }}
                            </el-tag>
                            <el-tag v-else type="info" size="small">选项目后即时开单</el-tag>
                            <el-tag v-if="item.critical" type="danger" size="small" effect="dark">危急值</el-tag>
                            <el-tooltip v-if="item.signStatus === 1" :content="`开单医师 ${item.doctorName} · ${item.signedTime || ''}，签名即锁定`">
                              <el-tag type="success" size="small" effect="plain" data-testid="p5-lab-sign-tag">已电子签名</el-tag>
                            </el-tooltip>
                            <el-tag v-else-if="item.signStatus === 2" type="warning" size="small" effect="plain">签名已作废</el-tag>
                          </div>
                          <div class="flex items-center gap-1">
                            <el-button v-perm="'opd:doctorWorkstation:edit'" type="primary" link size="small" :disabled="!item.id"
                                       @click="handleSaveLaboratoryRow(item)">
                              保存修改
                            </el-button>
                            <el-button v-perm="'opd:doctorWorkstation:delete'" type="danger" link size="small"
                                       @click="handleDeleteLaboratoryRecord(idx)">
                              删除
                            </el-button>
                          </div>
                        </div>
                        <div class="flex flex-wrap items-center gap-x-3 gap-y-1.5">
                          <div class="flex items-center gap-4">
                            <label class="text-xs text-slate-600 whitespace-nowrap">检验项目</label>
                            <el-select v-model="item.laboratoryItemName" filterable
                                       placeholder="搜索检验项目"
                                       :loading="laboratoryItemLoading" style="width: 180px"
                                       @change="(val: any) => handleLaboratoryItemSelectForRecord(item, val)">
                              <el-option v-for="opt in laboratoryItemResults" :key="opt.id"
                                         :label="`${opt.itemCode} - ${opt.itemName}`" :value="opt.id">
                                <div class="flex items-center justify-between">
                                  <span class="text-sm font-medium text-slate-900">{{ opt.itemName }}</span>
                                  <span class="text-xs text-slate-400">¥{{ opt.price }}</span>
                                </div>
                                <div class="text-xs text-slate-400">{{ opt.itemCode }} · {{ opt.specimenType }}</div>
                              </el-option>
                            </el-select>
                          </div>
                          <div class="flex items-center gap-4">
                            <label class="text-xs text-slate-600 whitespace-nowrap">标本</label>
                            <el-select v-model="item.specimenType" style="width: 90px" placeholder="标本">
                              <el-option label="血液" value="血液"/>
                              <el-option label="尿液" value="尿液"/>
                              <el-option label="粪便" value="粪便"/>
                              <el-option label="体液" value="体液"/>
                            </el-select>
                          </div>
                          <div class="mt-1.5 flex items-center gap-4">
                            <el-switch v-model="item.isFasting" :active-value="1" :inactive-value="0"
                                       size="small" active-text="空腹"/>
                            <el-switch v-model="item.isEmergency" :active-value="1" :inactive-value="0"
                                       size="small" active-text="急诊"/>
                          </div>
                        </div>
                        <div class="mt-1.5 flex items-start gap-4">
                          <label class="mt-2 text-xs text-slate-600 whitespace-nowrap">检验目的</label>
                          <el-input v-model="item.laboratoryPurpose" type="textarea" :rows="2" style="width: 500px"
                                    placeholder="检验目的（可选）"/>
                        </div>

                        <!-- 常用检验申请快捷选择 -->
                        <div class="mt-2 border-t border-slate-100 pt-2">
                          <div class="mb-1 text-[10px] font-medium text-slate-500">常用检验</div>
                          <div class="flex flex-wrap gap-1">
                            <el-button v-for="tpl in myLaboratoryTemplates" :key="tpl.id" size="small" link
                                       type="primary" @click="handleApplyLaboratoryTemplateToRecord(item, tpl)">
                              {{ tpl.templateName }}
                            </el-button>
                          </div>
                        </div>
                      </div>
                    </template>
                    <!-- 纯列表 (非就诊中) -->
                    <template v-else>
                      <div v-for="(item, idx) in laboratoryRecords"
                           :key="item.id || ('pending-laboratory-' + item._pendingIdx)"
                           :class="[
                             'rounded-lg border p-3',
                             item.critical
                                 ? 'border-red-300 bg-red-50'
                                 : (item.execStatusText === '待缴费' ? 'border-amber-200 bg-amber-50' : 'border-slate-200')
                           ]">
                        <div class="flex items-center justify-between">
                          <div class="flex items-center gap-2">
                            <span class="text-sm font-medium text-slate-900">{{ item.laboratoryItemName }}</span>
                            <el-tag :type="applyTagType(item)" size="small">{{ item.execStatusText }}</el-tag>
                            <el-tag v-if="item.critical" type="danger" size="small" effect="dark">危急值</el-tag>
                            <el-tag v-if="item.isEmergency === 1" type="warning" size="small">急诊</el-tag>
                            <el-tag v-if="item.isFasting === 1" type="info" size="small">空腹</el-tag>
                            <el-tag v-if="item.signStatus === 1" type="success" size="small" effect="plain">已电子签名·{{ item.doctorName }}</el-tag>
                            <el-tag v-else-if="item.signStatus === 2" type="warning" size="small" effect="plain">签名已作废</el-tag>
                          </div>
                          <div class="flex items-center gap-2">
                            <span class="text-xs text-slate-400">{{ item.createTime }}</span>
                            <el-button v-if="item.execRecordId" type="primary" link size="small"
                                       @click="handleViewLaboratoryReport(item)">
                              查看报告
                            </el-button>
                            <!-- 批次E/E6：结果已回 → 一键建复诊（关联本次病历，免挂号费） -->
                            <el-button v-if="canCreateRevisit(item)" type="warning" link size="small"
                                       @click="handleCreateRevisit(item)">
                              建复诊
                            </el-button>
                            <el-button v-if="item.canDelete" v-perm="'opd:doctorWorkstation:delete'" type="danger" link size="small"
                                       @click="handleDeleteLaboratoryRecord(idx)">
                              删除
                            </el-button>
                            <el-tooltip v-else-if="item.deleteBlockReason" :content="item.deleteBlockReason">
                              <span class="cursor-help text-xs text-slate-300">删除</span>
                            </el-tooltip>
                          </div>
                        </div>
                        <div v-if="item.specimenType" class="mt-1 text-xs text-slate-500">标本：{{
                            item.specimenType
                          }}
                        </div>
                        <div v-if="item.laboratoryPurpose" class="mt-1 text-xs text-slate-500">
                          目的：{{ item.laboratoryPurpose }}
                        </div>
                        <div v-if="item.critical" class="mt-2 rounded bg-red-100 p-2 text-xs text-red-700">
                          该检验出现危急值，请优先处理并及时通知患者。
                        </div>
                      </div>
                    </template>
                  </div>
                </div>
                <!-- 批次E/F：同检查面板，空态要区分「没开」和「还在加载」 -->
                <div v-else class="py-8 text-center text-sm text-slate-400">
                  {{ patientDataLoading ? '检验申请加载中…' : '本次就诊还没有检验申请' }}
                </div>
              </div>
                </div>
              </OrderPanel>
      <!-- AI辅助诊疗：与检查/检验申请同级的子卡（形态对齐左栏病历里的三个子卡） -->
      <div class="overflow-hidden rounded-lg border border-slate-200">
        <div class="border-b border-slate-200 bg-slate-50 px-3 py-2">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <div
                  class="flex h-5 w-5 items-center justify-center rounded bg-gradient-to-br from-blue-500 to-purple-600">
                <span class="text-[10px] font-bold text-white">AI</span>
              </div>
              <span class="text-sm font-bold text-slate-700">辅助诊疗</span>
            </div>
            <el-button type="primary" link size="small" @click="handleAiDiagnosis" :loading="aiDiagnosisLoading">
              刷新推荐
            </el-button>
          </div>
        </div>
        <div class="p-3">
          <!-- AI诊断推荐 -->
          <div v-if="aiDiagnosisResults.length > 0">
            <div v-if="aiDiagnosisDegraded"
                 class="mb-2 rounded bg-amber-50 px-2 py-1 text-xs text-amber-700">
              模型未参与，以下为码表规则匹配结果<span v-if="aiDiagnosisDegradeReason">（{{ aiDiagnosisDegradeReason }}）</span>
            </div>
            <div class="mb-2 text-xs text-slate-500">根据病历信息，系统推荐以下诊断：</div>
            <div class="space-y-2">
              <div v-for="(item, index) in aiDiagnosisResults" :key="index"
                   class="rounded-lg border border-slate-100 p-2.5 transition-colors hover:border-blue-200 hover:bg-blue-50">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <span
                        class="flex h-5 w-5 items-center justify-center rounded-full bg-blue-100 text-[10px] font-bold text-blue-600">
                      {{ index + 1 }}
                    </span>
                    <span class="text-sm font-medium text-slate-900">{{ item.icdName }}</span>
                  </div>
                  <el-tag v-if="item.confidence !== null"
                          :type="item.confidence >= 80 ? 'success' : item.confidence >= 60 ? 'warning' : 'info'"
                          size="small">
                    {{ item.confidence }}%
                  </el-tag>
                  <el-tag v-else type="info" size="small" effect="plain">
                    {{ item.source === 'rule' ? '规则匹配' : '未给分值' }}
                  </el-tag>
                </div>
                <div class="mt-1.5 flex items-center gap-2">
                  <span class="text-[10px] text-slate-400">{{ item.icdCode }}</span>
                  <el-button type="primary" link size="small" @click="handleAdoptAiDiagnosis(item)">
                    采纳
                  </el-button>
                  <el-button type="info" link size="small" @click="handleViewAiGuide(item)">
                    查看指南
                  </el-button>
                </div>
              </div>
            </div>
          </div>
          <div v-else-if="aiDiagnosisLoading" class="py-6 text-center">
            <el-icon class="mb-2 h-6 w-6 animate-spin text-blue-500">
              <Loading/>
            </el-icon>
            <div class="text-xs text-slate-500">AI正在分析病历信息...</div>
          </div>
          <div v-else class="py-4 text-center">
            <div class="text-xs text-slate-400">填写主诉或现病史后</div>
            <div class="text-xs text-slate-400">系统将自动推荐诊断</div>
          </div>
        </div>
      </div>
      <!-- 用药安全审查（处方提交后由药师审方 + 用药安全规则共同完成，此处只显示状态、不产结论） -->
      <div class="overflow-hidden rounded-lg border border-slate-200">
        <div class="border-b border-slate-200 bg-slate-50 px-3 py-2">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <el-icon class="text-slate-400">
                <InfoFilled/>
              </el-icon>
              <span class="text-sm font-bold text-slate-700">用药安全审查</span>
            </div>
            <el-tag type="info" size="small" effect="plain">待处方提交</el-tag>
          </div>
        </div>
        <div class="p-3">
          <div v-if="prescriptionForm.details.length === 0" class="py-4 text-center">
            <div class="text-xs text-slate-400">添加药品后，处方随病历提交进入审查</div>
          </div>
          <div v-else class="space-y-1.5">
            <div class="text-xs text-slate-500">
              已开 {{ prescriptionForm.details.length }} 项药品。处方审核按处方号由服务端回查明细，
              提交病历（结诊）生成处方后才能出审查结论。
            </div>
            <div class="text-xs text-slate-400">
              审查由药师审方与用药安全规则共同完成，结论见「处方」页签与审方工作台。
            </div>
          </div>
        </div>
      </div>
            </div>
          </div>
        </div>

        <!-- 底部固定动作条：靠左，与上方病历正文左对齐（医生视线从病历下来就落在第一个按钮上） -->
        <PageActionBar align="start">
          <!-- 结诊前：核心操作 -->
          <div v-if="!isVisitCompleted" class="flex items-center gap-3">
            <el-button v-if="currentPatient.queueStatus == 3" v-perm="['opd:doctorWorkstation:add', 'opd:doctorWorkstation:edit']" type="primary" @click="handleSaveRecord">
              临时保存
            </el-button>
            <el-button v-if="currentPatient.queueStatus == 3" v-perm="'opd:doctorWorkstation:add'" type="success" plain :icon="Tickets" @click="openOrderDialog">
              开住院证
            </el-button>
            <el-button v-if="currentPatient.queueStatus == 3" v-perm="'opd:doctorWorkstation:add'" type="primary" plain @click="openRevisitAppoint">
              预约复诊
            </el-button>
            <el-button v-if="currentPatient.queueStatus == 3" v-perm="'opd:doctorWorkstation:edit'" type="warning" @click="handleComplete">
              结诊
            </el-button>
          </div>
          <!-- 结诊后：打印指引 -->
          <div v-else class="flex items-center gap-3">
            <el-button type="primary" @click="showGuideSheetDialog = true">
              <el-icon class="mr-1">
                <Document/>
              </el-icon>
              打印指引单
            </el-button>
            <el-button type="success" @click="printPrescriptions">
              <el-icon class="mr-1">
                <Printer/>
              </el-icon>
              打印处方
            </el-button>
            <!-- 结诊提交是原子动作：病历/处方签名失败会整体回滚，所以走到这里必然已签 -->
            <el-tag type="success" effect="plain" data-testid="p5-done-sign-hint">
              病历 · 处方 · 申请单均已电子签名（签名中心可验签）
            </el-tag>
          </div>
        </PageActionBar>
      </template>
    </div>

  </div>

  <!-- 历史就诊（患者级）：弹窗不打断接诊，内含 CDR 全景时间轴与「打开完整时间轴」 -->
  <PatientDetailDialog v-model="showPatientDetail" :patient-id="currentPatient?.patientId"
                       :patient="currentPatient"/>

  <!-- 费用与医保抽屉（原来常驻右栏 + 藏在一个 Tab 里，改为患者条「费用」按需展开） -->
  <el-drawer v-model="showChargeDrawer" title="费用与医保" size="560px">
    <div class="space-y-3">
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="mb-3 flex items-center gap-2">
          <el-icon class="text-blue-500">
            <Document/>
          </el-icon>
          <span class="text-sm font-bold text-slate-700">医保与结算</span>
        </div>
        <div class="space-y-3">
          <div class="rounded-lg bg-blue-50 p-3">
            <div class="text-xs text-slate-500">医保类型</div>
            <div class="text-sm font-medium text-slate-900">{{ insuranceInfo?.medicalInsuranceType || '-' }}</div>
          </div>
          <div class="grid grid-cols-2 gap-2">
            <div class="rounded-lg bg-emerald-50 p-3">
              <div class="text-xs text-slate-500">统筹比例</div>
              <div class="text-lg font-bold text-emerald-600">{{ insuranceInfo?.coverageRatio || 0 }}%</div>
            </div>
            <div class="rounded-lg bg-amber-50 p-3">
              <div class="text-xs text-slate-500">结算方式</div>
              <div class="text-xs font-medium text-amber-700">{{
                  insuranceInfo?.settlementType == 1 ? '自费' :
                      insuranceInfo?.settlementType == 4 ? '公费医疗' : '医保统筹'
                }}
              </div>
            </div>
          </div>
          <div class="space-y-2 border-t border-slate-100 pt-3">
            <div class="flex items-center justify-between text-sm">
              <span class="text-slate-500">本单预估</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-xs text-slate-500">药品费用</span>
              <span class="text-sm font-medium text-slate-900">¥{{
                  (insuranceInfo?.drugTotal || 0).toFixed(2)
                }}</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-xs text-slate-500">统筹支付</span>
              <span class="text-sm font-medium text-emerald-600">¥{{
                  (insuranceInfo?.insurancePay || 0).toFixed(2)
                }}</span>
            </div>
            <div class="flex items-center justify-between border-t border-slate-100 pt-2">
              <span class="text-xs font-medium text-slate-700">个人自付</span>
              <span class="text-base font-bold text-red-600">¥{{ (insuranceInfo?.selfPay || 0).toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 费用概览 -->
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="mb-3 flex items-center gap-2">
          <el-icon class="text-emerald-500">
            <Coin/>
          </el-icon>
          <span class="text-sm font-bold text-slate-700">费用概览</span>
        </div>
        <div class="space-y-2.5">
          <!-- 药品费用 -->
          <div>
            <div class="mb-1 flex items-center justify-between text-sm">
              <span class="text-slate-500">药品费用</span>
              <span class="font-medium text-slate-900">¥{{ drugTotalAmount.toFixed(2) }}</span>
            </div>
            <el-progress :percentage="drugPercentage" :stroke-width="6" :color="'#3b82f6'"
                         :show-text="false"/>
          </div>
          <!-- 检查费用 -->
          <div>
            <div class="mb-1 flex items-center justify-between text-sm">
              <span class="text-slate-500">检查费用</span>
              <span class="font-medium text-slate-900">¥{{ inspectionTotalAmount.toFixed(2) }}</span>
            </div>
            <el-progress :percentage="inspectionPercentage" :stroke-width="6" :color="'#8b5cf6'"
                         :show-text="false"/>
          </div>
          <!-- 检验费用 -->
          <div>
            <div class="mb-1 flex items-center justify-between text-sm">
              <span class="text-slate-500">检验费用</span>
              <span class="font-medium text-slate-900">¥{{ laboratoryTotalAmount.toFixed(2) }}</span>
            </div>
            <el-progress :percentage="laboratoryPercentage" :stroke-width="6" :color="'#f59e0b'"
                         :show-text="false"/>
          </div>
          <!-- 合计 -->
          <div class="border-t border-slate-100 pt-2.5">
            <div class="flex items-center justify-between">
              <span class="text-sm font-medium text-slate-700">本单合计</span>
              <span class="text-lg font-bold text-red-600">¥{{ totalBillAmount.toFixed(2) }}</span>
            </div>
            <div class="mt-1 flex items-center justify-between text-xs">
              <span class="text-slate-400">预估个人自付</span>
              <span class="font-medium text-amber-600">¥{{ (insuranceInfo?.selfPay || 0).toFixed(2) }}</span>
            </div>
          </div>
          <!-- 药品明细 -->
          <div v-if="prescriptionForm.details.length > 0" class="border-t border-slate-100 pt-2.5">
            <div class="mb-1.5 text-xs font-medium text-slate-500">处方明细 ({{
                prescriptionForm.details.length
              }}种)
            </div>
            <div class="max-h-32 space-y-1 overflow-y-auto custom-scrollbar">
              <div v-for="(drug, idx) in prescriptionForm.details" :key="idx"
                   class="flex items-center justify-between rounded bg-slate-50 px-2 py-1 text-xs">
                <span class="min-w-0 flex-1 truncate text-slate-700">{{ drug.drugName }}</span>
                <span class="ml-2 shrink-0 text-slate-500">x{{ drug.quantity }}</span>
                <span class="ml-2 shrink-0 font-medium text-slate-900">¥{{ (drug.amount || 0).toFixed(2) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="mb-3 text-sm font-bold text-slate-700">收费明细</div>
        <div>
              <div v-if="patientChargeItems.length" class="py-2">
                <!-- 收费汇总（可折叠） -->
                <el-collapse v-model="chargeCollapseActive">
                  <el-collapse-item name="chargeInfo">
                    <template #title>
                      <div class="flex flex-1 items-center justify-between pr-4">
                        <span class="font-bold text-slate-700">收费汇总</span>
                        <el-tag
                            :type="chargeSummary.total > 0 ? 'success' : 'info'"
                            size="small">
                          {{ chargeSummary.total > 0 ? '已收费' : '暂无收费' }}
                        </el-tag>
                      </div>
                    </template>
                    <div class="grid grid-cols-2 gap-3 text-sm">
                      <div>
                        <span class="text-slate-500">总费用：</span>
                        <span class="font-bold text-red-600">¥{{ chargeSummary.total.toFixed(2) }}</span>
                      </div>
                      <div>
                        <span class="text-slate-500">统筹支付：</span>
                        <span class="font-medium text-blue-600">¥{{ chargeSummary.insurance.toFixed(2) }}</span>
                      </div>
                      <div>
                        <span class="text-slate-500">账户支付：</span>
                        <span class="font-medium text-emerald-600">¥{{ chargeSummary.account.toFixed(2) }}</span>
                      </div>
                      <div>
                        <span class="text-slate-500">个人自费：</span>
                        <span class="font-medium text-orange-600">¥{{ chargeSummary.self.toFixed(2) }}</span>
                      </div>
                    </div>
                  </el-collapse-item>

                  <!-- 收费明细（可折叠） -->
                  <el-collapse-item
                      v-if="getChargeDetailsByType([2,3,4]).length || getChargeDetailsByType(5).length || getChargeDetailsByType(6).length"
                      name="chargeDetails">
                    <template #title>
                      <span class="font-bold text-slate-700">收费明细</span>
                    </template>

                    <!-- 按类型分组显示 -->
                    <div v-if="getChargeDetailsByType([2,3,4]).length > 0" class="mb-3">
                      <div class="mb-1 flex items-center gap-2">
                        <el-tag size="small" type="primary">药品</el-tag>
                        <span class="text-xs text-slate-500">（{{ getChargeDetailsByType([2,3,4]).length }}项）</span>
                      </div>
                      <div class="space-y-1 pl-2">
                        <div v-for="(item, index) in getChargeDetailsByType([2,3,4])" :key="index"
                             class="flex items-center justify-between rounded bg-blue-50 px-2 py-1 text-xs">
                          <div class="flex items-center gap-2">
                            <span class="font-medium text-slate-700">{{ item.itemName }}</span>
                            <span class="text-slate-400">{{ item.specification }}</span>
                          </div>
                          <div class="flex items-center gap-3">
                            <span>{{ item.quantity }}{{ item.unit }}</span>
                            <span>× ¥{{ (item.price || 0).toFixed(2) }}</span>
                            <span class="font-medium text-blue-600">= ¥{{ (item.amount || 0).toFixed(2) }}</span>
                          </div>
                        </div>
                      </div>
                    </div>

                    <div v-if="getChargeDetailsByType(5).length > 0" class="mb-3">
                      <div class="mb-1 flex items-center gap-2">
                        <el-tag size="small" type="warning">检查</el-tag>
                        <span class="text-xs text-slate-500">（{{ getChargeDetailsByType(5).length }}项）</span>
                      </div>
                      <div class="space-y-1 pl-2">
                        <div v-for="(item, index) in getChargeDetailsByType(5)" :key="index"
                             class="flex items-center justify-between rounded bg-amber-50 px-2 py-1 text-xs">
                          <div class="flex items-center gap-2">
                            <span class="text-slate-700">{{ item.itemName }}</span>
                            <span class="text-slate-400">{{ item.specification }}</span>
                          </div>
                          <span class="font-medium">¥{{ (item.amount || 0).toFixed(2) }}</span>
                        </div>
                      </div>
                    </div>

                    <div v-if="getChargeDetailsByType(6).length > 0" class="mb-3">
                      <div class="mb-1 flex items-center gap-2">
                        <el-tag size="small" type="success">检验</el-tag>
                        <span class="text-xs text-slate-500">（{{ getChargeDetailsByType(6).length }}项）</span>
                      </div>
                      <div class="space-y-1 pl-2">
                        <div v-for="(item, index) in getChargeDetailsByType(6)" :key="index"
                             class="flex items-center justify-between rounded bg-emerald-50 px-2 py-1 text-xs">
                          <div class="flex items-center gap-2">
                            <span class="text-slate-700">{{ item.itemName }}</span>
                            <span class="text-slate-400">{{ item.specification }}</span>
                          </div>
                          <span class="font-medium">¥{{ (item.amount || 0).toFixed(2) }}</span>
                        </div>
                      </div>
                    </div>

                    <!-- 费用汇总 -->
                    <div class="border-t border-slate-200 pt-2 space-y-1">
                      <div v-if="getChargeDetailsByType([2,3,4]).length > 0" class="flex justify-between text-xs">
                        <span class="text-slate-500">药品小计</span>
                        <span class="font-medium">¥{{ getTypeTotal([2,3,4]).toFixed(2) }}</span>
                      </div>
                      <div v-if="getChargeDetailsByType(5).length > 0" class="flex justify-between text-xs">
                        <span class="text-slate-500">检查小计</span>
                        <span class="font-medium">¥{{ getTypeTotal(5).toFixed(2) }}</span>
                      </div>
                      <div v-if="getChargeDetailsByType(6).length > 0" class="flex justify-between text-xs">
                        <span class="text-slate-500">检验小计</span>
                        <span class="font-medium">¥{{ getTypeTotal(6).toFixed(2) }}</span>
                      </div>
                      <div class="flex justify-between border-t border-slate-200 pt-1 text-sm font-bold">
                        <span class="text-slate-700">合计</span>
                        <span class="text-red-600">¥{{ chargeSummary.total.toFixed(2) }}</span>
                      </div>
                    </div>
                  </el-collapse-item>
                </el-collapse>
              </div>
              <div v-else-if="patientDataLoading" class="py-12 text-center text-sm text-slate-400">
                <p>收费信息加载中…</p>
              </div>
              <div v-else class="py-12 text-center text-sm text-slate-400">
                <Coin class="mx-auto mb-2 h-12 w-12 opacity-30"/>
                <p>暂无收费信息</p>
              </div>
        </div>
      </div>
    </div>
  </el-drawer>

  <!-- AI诊疗指南弹窗 -->
  <el-dialog
      v-model="aiGuideDialogVisible"
      title="诊疗指南"
      width="700px"
      :close-on-click-modal="false"
  >
    <div v-if="aiGuideData" class="space-y-4">
      <div class="rounded-lg bg-gradient-to-r from-blue-50 to-purple-50 p-4">
        <div class="flex items-center gap-3">
          <div class="flex h-10 w-10 items-center justify-center rounded-full bg-blue-100">
            <el-icon class="h-5 w-5 text-blue-600">
              <Document/>
            </el-icon>
          </div>
          <div>
            <div class="text-lg font-bold text-slate-900">{{ aiGuideData.icdName }}</div>
            <div class="text-sm text-slate-500">ICD编码: {{ aiGuideData.icdCode }}</div>
          </div>
        </div>
      </div>

      <!-- 治疗原则 -->
      <div class="rounded-lg border border-slate-200 p-4">
        <h4 class="mb-3 flex items-center gap-2 text-sm font-bold text-slate-700">
          <el-icon class="text-blue-500">
            <CircleCheck/>
          </el-icon>
          治疗原则
        </h4>
        <div class="space-y-2 text-sm text-slate-600">
          <div class="flex items-start gap-2">
            <span class="mt-1 h-1.5 w-1.5 shrink-0 rounded-full bg-blue-500"></span>
            <span>明确诊断后针对性治疗</span>
          </div>
          <div class="flex items-start gap-2">
            <span class="mt-1 h-1.5 w-1.5 shrink-0 rounded-full bg-blue-500"></span>
            <span>根据病情严重程度选择门诊或住院治疗</span>
          </div>
          <div class="flex items-start gap-2">
            <span class="mt-1 h-1.5 w-1.5 shrink-0 rounded-full bg-blue-500"></span>
            <span>定期复查，评估治疗效果</span>
          </div>
        </div>
      </div>

      <!-- 推荐用药 -->
      <div class="rounded-lg border border-slate-200 p-4">
        <h4 class="mb-3 flex items-center gap-2 text-sm font-bold text-slate-700">
          <el-icon class="text-emerald-500">
            <Coin/>
          </el-icon>
          推荐用药方案
        </h4>
        <div class="space-y-3">
          <div class="rounded-lg bg-slate-50 p-3">
            <div class="mb-1 text-sm font-medium text-slate-900">一线用药</div>
            <div class="text-xs text-slate-600">根据临床指南推荐，首选以下药物</div>
            <div class="mt-2 space-y-1">
              <div class="flex items-center justify-between rounded bg-white p-2">
                <span class="text-sm text-slate-700">阿莫西林克拉维酸钾 0.375g</span>
                <span class="text-xs text-slate-500">口服，一日三次</span>
              </div>
              <div class="flex items-center justify-between rounded bg-white p-2">
                <span class="text-sm text-slate-700">头孢呋辛 0.75g</span>
                <span class="text-xs text-slate-500">静脉注射，每8小时一次</span>
              </div>
            </div>
          </div>
          <div class="rounded-lg bg-slate-50 p-3">
            <div class="mb-1 text-sm font-medium text-slate-900">替代用药</div>
            <div class="text-xs text-slate-600">对一线药物过敏或耐药时考虑</div>
            <div class="mt-2 space-y-1">
              <div class="flex items-center justify-between rounded bg-white p-2">
                <span class="text-sm text-slate-700">阿奇霉素 0.5g</span>
                <span class="text-xs text-slate-500">口服，一日一次</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 注意事项 -->
      <div class="rounded-lg border border-amber-200 bg-amber-50 p-4">
        <h4 class="mb-2 flex items-center gap-2 text-sm font-bold text-amber-700">
          <el-icon>
            <Warning/>
          </el-icon>
          注意事项
        </h4>
        <ul class="space-y-1 text-sm text-amber-800">
          <li>• 用药前询问过敏史</li>
          <li>• 注意肝肾功能调整剂量</li>
          <li>• 疗程结束后评估疗效</li>
          <li>• 如症状未缓解及时复诊</li>
        </ul>
      </div>
    </div>
    <template #footer>
      <el-button @click="aiGuideDialogVisible = false">关闭</el-button>
      <el-button type="primary" @click="handleAdoptAiDiagnosis(aiGuideData)">
        采纳该诊断
      </el-button>
    </template>
  </el-dialog>

  <!-- 就诊指引单弹窗 -->
  <el-dialog
      v-model="showGuideSheetDialog"
      title="就诊指引单"
      width="960px"
      :close-on-click-modal="false"
  >
    <div class="min-h-[800px]">
      <div v-if="loadingGuide" class="flex items-center justify-center py-10 text-sm text-slate-500">加载中...</div>
      <iframe v-else-if="guidePdfUrl" :src="guidePdfUrl"
              class="h-[800px] w-full border-0"></iframe>
      <div v-else class="flex items-center justify-center py-10 text-sm text-slate-400">指引单尚未生成，请先提交病历
      </div>
    </div>
    <template #footer>
      <el-button @click="showGuideSheetDialog = false">关闭</el-button>
      <el-button type="primary" @click="printGuidePdf">打印</el-button>
    </template>
  </el-dialog>

  <!-- 标签管理弹窗 -->
  <el-dialog
      v-model="showTagDialog"
      title="患者标签管理"
      width="500px"
      :close-on-click-modal="false"
  >
    <div v-if="currentPatient" class="space-y-4">
      <!-- 当前患者标签 -->
      <div>
        <h4 class="mb-2 text-sm font-medium text-slate-700">当前标签</h4>
        <div v-if="patientTags.length > 0" class="flex flex-wrap gap-2">
          <span v-for="tag in patientTags" :key="tag.tagId"
                class="inline-flex items-center gap-1 rounded px-2 py-1 text-xs font-medium text-white"
                :style="{ backgroundColor: tag.tagColor || '#409EFF' }">
            {{ tag.tagName }}
            <el-icon class="cursor-pointer hover:text-red-200" @click="handleRemoveTag(tag.tagId)">
              <Delete class="h-3 w-3"/>
            </el-icon>
          </span>
        </div>
        <div v-else class="text-sm text-slate-400">暂无标签</div>
      </div>

      <!-- 添加标签 -->
      <div>
        <h4 class="mb-2 text-sm font-medium text-slate-700">添加标签</h4>
        <el-input v-model="tagSearchKeyword" placeholder="搜索标签..." :prefix-icon="Search" clearable class="mb-3"/>
        <div class="max-h-60 overflow-y-auto">
          <div class="flex flex-wrap gap-2">
            <span v-for="tag in filteredAllTags" :key="tag.tagId"
                  class="inline-flex cursor-pointer items-center gap-1 rounded px-2 py-1 text-xs font-medium transition-all hover:scale-105"
                  :class="isTagAdded(tag.tagId) ? 'opacity-50 cursor-not-allowed' : 'text-white cursor-pointer'"
                  :style="{ backgroundColor: isTagAdded(tag.tagId) ? '#ccc' : (tag.tagColor || '#409EFF') }"
                  @click="!isTagAdded(tag.tagId) && handleAddTag(tag.tagId)">
              {{ tag.tagName }}
              <el-icon v-if="!isTagAdded(tag.tagId)" class="h-3 w-3"><Plus/></el-icon>
            </span>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>

  <!-- 常用诊断维护 -->
  <el-dialog v-model="showDiagTemplateDialog" title="常用诊断维护" width="600px" destroy-on-close
             @open="loadDiagTemplates">
    <div class="space-y-3">
      <div class="text-xs text-slate-500">管理您常用的ICD-10诊断编码，开方时可快速选择。</div>
      <!-- 搜索添加 -->
      <div>
        <el-input v-model="diagSearchKeyword" placeholder="输入诊断名称或编码搜索并添加" :prefix-icon="Search"
                  clearable @input="handleDiagSearchInDialog" @clear="diagSearchResults = []"/>
        <div v-if="diagSearchResults.length > 0"
             class="mt-2 max-h-40 space-y-1 overflow-y-auto rounded border border-slate-200 p-2">
          <div v-for="item in diagSearchResults" :key="item.icdCode"
               class="flex cursor-pointer items-center justify-between rounded px-2 py-1.5 hover:bg-blue-50"
               @click="handleAddDiagFromDialog(item)">
            <div class="flex items-center gap-2">
              <span class="font-mono text-xs font-medium text-blue-600">{{ item.icdCode }}</span>
              <span class="text-sm text-slate-900">{{ item.icdName }}</span>
            </div>
            <el-icon class="text-blue-500">
              <Plus/>
            </el-icon>
          </div>
        </div>
      </div>
      <!-- 已有列表 -->
      <div v-if="myDiagTemplates.length === 0" class="py-4 text-center text-sm text-slate-400">
        暂无常用诊断
      </div>
      <div v-else class="space-y-2">
        <div v-for="(item, idx) in myDiagTemplates" :key="item.id || idx"
             class="flex items-center justify-between rounded border border-slate-200 p-2.5 hover:bg-slate-50">
          <div class="flex items-center gap-2">
            <span class="font-mono text-xs font-medium text-blue-600">{{ item.icdCode }}</span>
            <span class="text-sm text-slate-900">{{ item.icdName }}</span>
          </div>
          <el-button type="danger" link size="small" @click="handleDeleteDiagTemplate(idx)">删除</el-button>
        </div>
      </div>
    </div>
    <template #footer>
      <el-button @click="showDiagTemplateDialog = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 处方模板管理 -->
  <el-dialog v-model="showRxTemplateDialog" title="处方模板管理" width="700px" destroy-on-close
             @open="loadRxTemplates">
    <div class="space-y-3">
      <div class="text-xs text-slate-500">按病种保存常用处方组合，开方时一键套用。</div>
      <div class="flex items-center gap-2">
        <el-input v-model="newTemplateName" placeholder="模板名称，如：上呼吸道感染" class="flex-1"/>
        <el-button type="primary" size="small" @click="handleAddRxTemplate">保存当前处方为模板</el-button>
      </div>
      <div v-if="myRxTemplates.length === 0" class="py-8 text-center text-sm text-slate-400">暂无处方模板</div>
      <div v-else class="space-y-2">
        <div v-for="(tpl, idx) in myRxTemplates" :key="tpl.id || idx"
             class="rounded border border-slate-200 p-3 hover:bg-slate-50">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-slate-900">{{ tpl.templateName }}</span>
            <div class="flex gap-2">
              <el-button type="primary" link size="small" @click="handleApplyRxTemplate(tpl)">套用</el-button>
              <el-button type="danger" link size="small" @click="handleDeleteRxTemplate(idx)">删除</el-button>
            </div>
          </div>
          <div class="mt-1 text-xs text-slate-500">{{ tpl.drugCount }}种药品，合计 ¥{{ tpl.totalAmount }}</div>
        </div>
      </div>
    </div>
    <template #footer>
      <el-button @click="showRxTemplateDialog = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 药品/耗材套餐 -->
  <el-dialog v-model="showPackageDialog" title="药品/耗材套餐" width="700px" destroy-on-close
             @open="loadDrugPackages">
    <div class="space-y-3">
      <div class="text-xs text-slate-500">将药品+检查+检验打包成治疗方案套餐，一键开立全套医嘱。</div>
      <el-button type="primary" size="small" @click="showAddPackageDialog = true">
        <el-icon class="mr-0.5">
          <Plus/>
        </el-icon>
        将当前处方/检查/检验保存为套餐
      </el-button>
      <div v-if="myPackages.length === 0" class="py-8 text-center text-sm text-slate-400">暂无套餐</div>
      <div v-else class="space-y-2">
        <div v-for="(pkg, idx) in myPackages" :key="pkg.id || idx"
             class="rounded border border-slate-200 p-3 hover:bg-slate-50">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-slate-900">{{ pkg.packageName }}</span>
            <div class="flex gap-2">
              <el-button type="primary" link size="small" @click="handleApplyPackage(pkg)">套用</el-button>
              <el-button type="danger" link size="small" @click="handleDeletePackage(idx)">删除</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <template #footer>
      <el-button @click="showPackageDialog = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 新增套餐弹窗 -->
  <el-dialog v-model="showAddPackageDialog" title="保存为套餐" width="400px" destroy-on-close>
    <div class="space-y-3">
      <el-input v-model="newPackageName" placeholder="套餐名称，如：术前检查套餐"/>
      <div class="text-xs text-slate-500">
        将当前处方（{{ prescriptionForm.details.length }}种药品）保存为套餐。
      </div>
    </div>
    <template #footer>
      <el-button @click="showAddPackageDialog = false">取消</el-button>
      <el-button type="primary" @click="handleSaveNewPackage">保存</el-button>
    </template>
  </el-dialog>

  <!-- 检查申请模板弹窗 -->
  <el-dialog v-model="showInspectionTemplateDialog" title="检查申请模板" width="700px" destroy-on-close
             @open="loadInspectionTemplates">
    <div class="space-y-3">
      <div class="text-xs text-slate-500">将常用检查项目保存为模板，一键套用。</div>
      <el-button type="primary" size="small" @click="showAddInspectionTemplateDialog = true">
        <el-icon class="mr-0.5">
          <Plus/>
        </el-icon>
        新增模板
      </el-button>
      <div class="max-h-[300px] space-y-2 overflow-y-auto">
        <div v-for="(tpl, idx) in myInspectionTemplates" :key="tpl.id"
             class="flex items-center justify-between rounded-lg border border-slate-100 p-3 transition-colors hover:bg-slate-50">
          <div class="flex-1">
            <div class="text-sm font-medium text-slate-800">{{ tpl.templateName }}</div>
            <div class="text-xs text-slate-400">{{ tpl.inspectionItemName }} · {{ tpl.bodyPart || '未指定部位' }}</div>
          </div>
          <div class="flex items-center gap-2">
            <el-button type="primary" link size="small" @click="handleApplyInspectionTemplate(tpl)">套用</el-button>
            <el-button type="danger" link size="small" @click="handleDeleteInspectionTemplate(idx)">删除</el-button>
          </div>
        </div>
        <div v-if="myInspectionTemplates.length === 0" class="py-8 text-center text-xs text-slate-400">
          暂无模板，可在开立检查申请时保存为模板
        </div>
      </div>
    </div>
    <template #footer>
      <el-button @click="showInspectionTemplateDialog = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 新增检查模板弹窗 -->
  <el-dialog v-model="showAddInspectionTemplateDialog" title="保存检查申请模板" width="500px" destroy-on-close>
    <div class="space-y-3">
      <el-input v-model="inspectionTemplateName" placeholder="模板名称，如：术前胸部检查"/>
      <el-select v-model="inspectionTemplateForm.inspectionItemId" filterable remote reserve-keyword
                 placeholder="搜索检查项目" :remote-method="handleInspectionTemplateItemSearch"
                 :loading="inspectionTemplateItemLoading" class="w-full"
                 @change="handleInspectionTemplateItemSelect">
        <el-option v-for="item in inspectionTemplateItemResults" :key="item.id"
                   :label="`${item.itemCode} - ${item.itemName}`" :value="item.id">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-slate-900">{{ item.itemName }}</span>
            <span class="text-xs text-slate-400">¥{{ item.price }}</span>
          </div>
          <div class="text-xs text-slate-400">{{ item.itemCode }} · {{ item.bodyPart }}</div>
        </el-option>
      </el-select>
      <el-input v-model="inspectionTemplateForm.bodyPart" placeholder="检查部位（可选）"/>
      <el-input v-model="inspectionTemplateForm.inspectionPurpose" placeholder="检查目的（可选）"/>
      <el-switch v-model="inspectionTemplateForm.isEmergency" :active-value="1" :inactive-value="0" active-text="急诊"/>
    </div>
    <template #footer>
      <el-button @click="showAddInspectionTemplateDialog = false">取消</el-button>
      <el-button type="primary" @click="handleSaveInspectionTemplate">保存</el-button>
    </template>
  </el-dialog>

  <!-- 检验申请模板弹窗 -->
  <el-dialog v-model="showLaboratoryTemplateDialog" title="检验申请模板" width="700px" destroy-on-close
             @open="loadLaboratoryTemplates">
    <div class="space-y-3">
      <div class="text-xs text-slate-500">将常用检验项目保存为模板，一键套用。</div>
      <el-button type="primary" size="small" @click="showAddLaboratoryTemplateDialog = true">
        <el-icon class="mr-0.5">
          <Plus/>
        </el-icon>
        新增模板
      </el-button>
      <div class="max-h-[300px] space-y-2 overflow-y-auto">
        <div v-for="(tpl, idx) in myLaboratoryTemplates" :key="tpl.id"
             class="flex items-center justify-between rounded-lg border border-slate-100 p-3 transition-colors hover:bg-slate-50">
          <div class="flex-1">
            <div class="text-sm font-medium text-slate-800">{{ tpl.templateName }}</div>
            <div class="text-xs text-slate-400">{{ tpl.laboratoryItemName }} · {{
                tpl.sampleType || '未指定标本'
              }}
            </div>
          </div>
          <div class="flex items-center gap-2">
            <el-button type="primary" link size="small" @click="handleApplyLaboratoryTemplate(tpl)">套用</el-button>
            <el-button type="danger" link size="small" @click="handleDeleteLaboratoryTemplate(idx)">删除</el-button>
          </div>
        </div>
        <div v-if="myLaboratoryTemplates.length === 0" class="py-8 text-center text-xs text-slate-400">
          暂无模板，可在开立检验申请时保存为模板
        </div>
      </div>
    </div>
    <template #footer>
      <el-button @click="showLaboratoryTemplateDialog = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 新增检验模板弹窗 -->
  <el-dialog v-model="showAddLaboratoryTemplateDialog" title="保存检验申请模板" width="500px" destroy-on-close>
    <div class="space-y-3">
      <el-input v-model="laboratoryTemplateName" placeholder="模板名称，如：术前血常规"/>
      <el-select v-model="laboratoryTemplateForm.laboratoryItemId" filterable remote reserve-keyword
                 placeholder="搜索检验项目" :remote-method="handleLaboratoryTemplateItemSearch"
                 :loading="laboratoryTemplateItemLoading" class="w-full"
                 @change="handleLaboratoryTemplateItemSelect">
        <el-option v-for="item in laboratoryTemplateItemResults" :key="item.id"
                   :label="`${item.itemCode} - ${item.itemName}`" :value="item.id">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-slate-900">{{ item.itemName }}</span>
            <span class="text-xs text-slate-400">¥{{ item.price }}</span>
          </div>
          <div class="text-xs text-slate-400">{{ item.itemCode }} · {{ item.sampleType }}</div>
        </el-option>
      </el-select>
      <el-input v-model="laboratoryTemplateForm.sampleType" placeholder="标本类型（可选）"/>
      <el-input v-model="laboratoryTemplateForm.inspectionPurpose" placeholder="检验目的（可选）"/>
      <el-switch v-model="laboratoryTemplateForm.isEmergency" :active-value="1" :inactive-value="0" active-text="急诊"/>
    </div>
    <template #footer>
      <el-button @click="showAddLaboratoryTemplateDialog = false">取消</el-button>
      <el-button type="primary" @click="handleSaveLaboratoryTemplate">保存</el-button>
    </template>
  </el-dialog>

  <!-- 病历文本智能录入（P1-3 结构化抽取） -->
  <el-dialog v-model="extractDialogVisible" title="智能录入「病史」" width="900px" destroy-on-close
             :close-on-click-modal="false">
    <div class="space-y-3">
      <div class="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-xs text-slate-600">
        把一段文字（自己手打的、从外院系统或上级医院病历粘贴的）贴进来，系统会拆分到对应字段。
        <span class="text-slate-500">
          结果只是候选值：每条都能看到原文依据，<b>必须由你逐条核对后点「填入」</b>才会进表单，
          系统不会自动改病历。
        </span>
      </div>

      <el-input v-model="extractRawText" type="textarea" :rows="7"
                placeholder="例：主诉：反复咳嗽3天&#10;现病史：3天前受凉后出现咳嗽，夜间为甚，咳白色粘痰，无发热&#10;既往史：高血压5年，规律服药&#10;T36.8℃ P82次/分 R18次/分 BP130/85mmHg"/>

      <div class="flex items-center justify-between">
        <span class="text-xs text-slate-400">
          {{ extractRawText.length }} 字（超过 4000 字会被截断，且会在结果里标注）
        </span>
        <div class="flex items-center gap-2">
          <el-button v-if="extractRawText" size="small" @click="extractRawText = ''">清空</el-button>
          <el-button type="primary" size="small" :loading="extractLoading" @click="runExtract">
            解析
          </el-button>
        </div>
      </div>

      <template v-if="extractResult">
        <div v-if="extractResult.degraded"
             class="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
          <b>本次未经过大模型</b>：{{ extractResult.degradeReason }}
        </div>
        <div v-if="extractResult.truncated"
             class="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
          文本超出长度上限，<b>后半段没有参与解析</b>。
        </div>
        <div v-if="extractResult.rejectedCount > 0"
             class="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-xs text-red-700">
          <b>{{ extractResult.rejectedCount }} 条内容被丢弃</b>（原文里找不到依据，或字段不能写入病历）：
          <ul class="mt-1 list-disc pl-4">
            <li v-for="(note, idx) in extractResult.rejectedNotes" :key="idx">{{ note }}</li>
          </ul>
        </div>

        <div class="flex items-center justify-between">
          <span class="text-xs text-slate-500">
            共识别出 {{ extractResult.fieldCount }} 项（耗时 {{ extractResult.latencyMs }} ms）
          </span>
          <el-button v-if="extractResult.fields?.length" type="primary" link size="small"
                     @click="applyAllExtractFields">全部填入
          </el-button>
        </div>

        <el-table :data="extractResult.fields || []" size="small" max-height="320" border>
          <el-table-column label="字段" width="100">
            <template #default="{ row }">
              <span class="text-xs font-medium text-slate-700">{{ row.fieldLabel }}</span>
            </template>
          </el-table-column>
          <el-table-column label="抽取内容" min-width="240">
            <template #default="{ row }">
              <div class="whitespace-pre-wrap text-sm text-slate-900">{{ row.value }}</div>
            </template>
          </el-table-column>
          <el-table-column label="来源" width="96">
            <template #default="{ row }">
              <el-tag :type="row.source === 'HARD_RULE' ? 'success' : 'info'" size="small" effect="plain">
                {{ row.source === 'HARD_RULE' ? '原文切分' : 'AI 搬运' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="原文依据" min-width="200">
            <template #default="{ row }">
              <div class="whitespace-pre-wrap text-xs text-slate-400">{{ row.evidence || '—' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90" fixed="right">
            <template #default="{ row }">
              <el-button v-if="extractAppliedFields.includes(row.field)" link size="small" disabled>
                已填入
              </el-button>
              <el-button v-else type="primary" link size="small" @click="applyExtractField(row)">
                填入
              </el-button>
            </template>
          </el-table-column>
          <template #empty>
            <div class="py-6 text-center text-xs text-slate-400">
              没有识别出可搬运的内容。可以试试按「主诉：」「现病史：」这样的格式补上字段标签。
            </div>
          </template>
        </el-table>
      </template>
    </div>

    <template #footer>
      <el-button @click="extractDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 病历草拟（P1-3，只草拟现病史） -->
  <el-dialog v-model="draftDialogVisible" title="AI 草拟现病史" width="700px" destroy-on-close
             :close-on-click-modal="false">
    <div v-if="draftLoading" class="py-10 text-center text-sm text-slate-500">
      正在根据主诉与查体整理草稿……
    </div>
    <div v-else-if="draftResult" class="space-y-3">
      <div class="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-xs text-slate-600">
        草稿只依据你已经填写的主诉、查体与体征，<b>不会补写你没提到的内容</b>（尤其是「无发热」这类阴性描述）。
        系统不生成诊断，也不生成处理意见。
      </div>

      <div v-if="draftResult.degraded"
           class="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
        <b>本次未经过大模型</b>：{{ draftResult.degradeReason }}
      </div>

      <div v-if="draftResult.presentIllness" class="rounded-lg border border-slate-200 p-3">
        <div class="mb-1 text-xs font-medium text-slate-500">草稿正文</div>
        <div class="whitespace-pre-wrap text-sm leading-6 text-slate-900">{{ draftResult.presentIllness }}</div>
      </div>
      <div v-else class="rounded-lg border border-slate-200 p-3 text-sm text-slate-500">
        这次没有生成草稿。
      </div>

      <div v-if="draftResult.missingPoints?.length">
        <div class="mb-1 text-xs font-medium text-slate-500">还缺这些信息（补充后可重新草拟）</div>
        <div class="flex flex-wrap gap-2">
          <el-tag v-for="(point, idx) in draftResult.missingPoints" :key="idx" type="warning" size="small"
                  effect="plain">
            {{ point }}
          </el-tag>
        </div>
      </div>

      <div v-if="draftResult.summary" class="text-xs text-slate-400">{{ draftResult.summary }}</div>
    </div>

    <template #footer>
      <el-button @click="draftDialogVisible = false">关闭</el-button>
      <el-button type="primary" :disabled="!draftResult?.presentIllness" @click="applyDraft">
        填入现病史
      </el-button>
    </template>
  </el-dialog>

  <!-- ================= 开住院证（门诊 → 住院的入口） ================= -->
  <el-dialog v-model="orderDialogVisible" title="开住院证（入院通知单）" width="640px" destroy-on-close>
    <div v-if="currentPatient" class="mb-4 rounded-lg border border-emerald-200 bg-emerald-50 p-3 text-xs text-slate-600">
      <div class="font-semibold text-slate-800">患者：{{ currentPatient.patientName }}（{{ currentPatient.patientNo || '—' }}）</div>
      <div class="mt-1">
        本次挂号：{{ currentPatient.registNo || '—' }} · 开证科室：{{ currentPatient.deptName || '—' }}
        · 开证医生：{{ currentPatient.doctorName || '—' }}
      </div>
      <div class="mt-1 text-slate-500">
        开证后患者持证到「入出院管理 → 待收治（住院证）」排床收治；入院记录会记下这张证的号与本次挂号号。
      </div>
    </div>
    <el-form :model="orderForm" label-width="110px">
      <el-form-item label="拟收治科室" required>
        <el-select v-model="orderForm.applyDeptId" filterable placeholder="选择收治科室" class="!w-full">
          <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="String(d.id)" />
        </el-select>
        <p class="mt-1 text-xs text-slate-400">住哪个科是临床决策；入院处收治时可以调科，调科会被系统标记出来</p>
      </el-form-item>
      <div class="grid grid-cols-2 gap-x-4">
        <el-form-item label="拟诊编码">
          <el-input v-model="orderForm.diagnosisCode" placeholder="ICD-10，如 J18.9" />
        </el-form-item>
        <el-form-item label="拟诊名称" required>
          <el-input v-model="orderForm.diagnosisName" placeholder="如 社区获得性肺炎" />
        </el-form-item>
      </div>
      <el-form-item label="预计入院时间">
        <el-date-picker
          v-model="orderForm.expectAdmitTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          placeholder="不填表示由患者自行择期"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="收治说明">
        <el-input v-model="orderForm.diagnosisNote" type="textarea" :rows="3" placeholder="病情摘要、收治理由、需注意的事项" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="orderForm.remark" />
      </el-form-item>
    </el-form>
    <div class="rounded-lg bg-slate-50 p-3 text-xs text-slate-500">
      同一挂号只能有一张有效住院证：重复开证会被拒绝，需要变更请让入院处先作废原证。
      住院证默认 7 天有效（系统参数 <span class="font-mono">admission_order.valid_days</span>），过期后不能再收治。
    </div>
    <template #footer>
      <el-button @click="orderDialogVisible = false">取消</el-button>
      <el-button v-perm="'opd:doctorWorkstation:add'" type="primary" :loading="orderSubmitting" @click="submitOrder">开具住院证</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
/* 诊室叫号面板：深色大屏底（主色 #1269B5 压深），号码是这块的视觉主体 */
.call-board {
  background: linear-gradient(160deg, #0f2b46 0%, #144066 100%);
  border: 1px solid #1e4a75;
}

.call-no {
  font-size: 38px;
  font-weight: 800;
  letter-spacing: 0.5px;
  font-variant-numeric: tabular-nums;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.35);
}

/* 诊室灯：接诊中绿 / 暂离琥珀 / 空闲灰，带辉光模拟实体指示灯 */
.call-light {
  width: 9px;
  height: 9px;
  border-radius: 9999px;
  display: inline-block;
  flex-shrink: 0;
}

.call-light.is-busy {
  background: #34d399;
  box-shadow: 0 0 6px rgba(52, 211, 153, 0.9);
}

.call-light.is-away {
  background: #fbbf24;
  box-shadow: 0 0 6px rgba(251, 191, 36, 0.9);
}

.call-light.is-idle {
  background: #94a3b8;
}

/* 病历表单整体放大：医生盯一天病历，14px 输入太小（2026-09-25 用户口径「字可以大一点」） */
.record-form :deep(.el-textarea__inner),
.record-form :deep(.el-input__inner) {
  font-size: 15px;
  line-height: 1.7;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 2px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>
