<script setup lang="ts">
import {computed, onMounted, reactive, ref} from 'vue'
import {patientGenderText} from '@/lib/patientGender'
import {
  Check,
  CircleCheck,
  CircleClose,
  Clock,
  Delete,
  Document,
  Plus,
  Refresh,
  Search,
  Suitcase,
  Tickets,
  User,
  View,
  Warning
} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  admitInpatient,
  dischargeInpatient,
  getInpatientBedList,
  getInpatientDetail,
  getInpatientListPage,
  getInpatientStats,
  getInpatientWardList,
  saveInpatientSummary,
  transferInpatientBed,
} from '@/api/inpatient'
import {cancelAdmissionOrder, getAdmissionOrderListPage,} from '@/api/admissionOrder'
import PatientSelect from '@/components/his/PatientSelect.vue'
import {getEmployeeList} from '@/api/system'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'

interface InpatientRow {
  admissionId: string
  admissionNo: string
  patientId: string
  patientNo?: string
  patientName?: string
  gender?: number
  age?: number
  deptId?: string
  deptName?: string
  wardId?: string
  wardName?: string
  bedId?: string
  bedNo?: string
  doctorName?: string
  admitTime?: string
  dischargeTime?: string
  admitWay?: number
  admitStatus?: number
  diagnosis?: string
  summaryId?: string
  summaryStatus?: number
  inpatientDays?: number
  mainDiagnosisName?: string
  totalAmount?: number
  registNo?: string
  admissionOrderNo?: string
}

/**
 * 住院证（门诊开出的入院通知单）
 * 注意 expired 是后端**查询时算出来的**，不是库里的状态：
 * 库里那条记录仍然是「待收治」，所以界面上要同时显示"待收治"和"已过期"两件事，
 * 否则入院处会拿着一本过期证的记录去找患者收治。
 */
interface AdmissionOrderRow {
  id: string
  orderNo: string
  patientId: string
  patientNo?: string
  patientName: string
  gender?: number
  genderText?: string
  age?: number
  phone?: string
  registId?: string
  registNo?: string
  sourceDeptName?: string
  sourceDoctorName?: string
  applyDeptId?: string
  applyDeptName?: string
  diagnosisCode?: string
  diagnosisName?: string
  diagnosisNote?: string
  insuranceType?: string
  orderStatus: number
  orderStatusText?: string
  expired?: boolean
  orderTime?: string
  validUntil?: string
  admissionId?: string
  admissionNo?: string
  admitTime?: string
  admitDeptId?: string
  admitDeptName?: string
  deptAdjusted?: boolean
  cancelReason?: string
}

interface WardOption {
  wardId: string
  wardCode?: string
  wardName: string
  deptId?: string
  deptName?: string
  totalBeds: number
  freeBeds: number
  occupiedBeds: number
  usageRate: number
}

interface BedOption {
  bedId: string
  bedNo: string
  wardId?: string
  bedStatus?: number
  bedStatusText?: string
  patientId?: string
  patientName?: string
}

// ------------------------------------------------------------------
// 文案映射：未知码值必须显式说「未知」，绝不当成合法值渲染（后端 InpatientLabels 同规则）
// ------------------------------------------------------------------
const admitWayMap: Record<number, string> = {1: '门诊', 2: '急诊', 3: '转院', 4: '其他'}
const dischargeWayMap: Record<number, string> = {
  1: '医嘱离院', 2: '医嘱转院', 3: '医嘱转社区', 4: '非医嘱离院', 5: '死亡', 9: '其他',
}
const summaryStatusMap: Record<number, { label: string, type: 'info' | 'warning' | 'success' }> = {
  1: {label: '草稿', type: 'info'},
  2: {label: '已提交', type: 'warning'},
  3: {label: '已归档', type: 'success'},
}
const admitConditionMap: Record<number, string> = {1: '有', 2: '临床未确定', 3: '情况不明', 4: '无'}
const orderStatusMap: Record<number, { label: string, type: 'info' | 'warning' | 'success' | 'danger' }> = {
  1: {label: '待收治', type: 'warning'},
  2: {label: '已收治', type: 'success'},
  3: {label: '已作废', type: 'info'},
  4: {label: '已过期', type: 'danger'},
}
const operationLevelMap: Record<number, string> = {1: '一级', 2: '二级', 3: '三级', 4: '四级'}
const incisionLevelMap: Record<number, string> = {0: '0类', 1: 'Ⅰ类', 2: 'Ⅱ类', 3: 'Ⅲ类'}
const anesthesiaMap: Record<number, string> = {1: '全麻', 2: '椎管内', 3: '神经阻滞', 4: '局麻', 5: '其他'}

const textOf = (map: Record<number, string>, code?: number | null, empty = '—') => {
  if (code === null || code === undefined) return empty
  return map[code] ?? `未知(${code})`
}
// 性别文案统一走 lib/patientGender：0 是历史脏码值，渲染成「未知(0)」而不是"女"
const genderText = (g?: number) => patientGenderText(g)
const money = (v?: number | string | null) => (v === null || v === undefined ? '—' : `¥${Number(v).toFixed(2)}`)

// ------------------------------------------------------------------
// 列表
// ------------------------------------------------------------------
const loading = ref(false)
const rows = ref<InpatientRow[]>([])
const query = reactive({
  patientName: '',
  wardId: null as string | null,
  admitStatus: 1 as number | null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
})
const total = ref(0)

const stats = reactive({
  inHospitalCount: 0, todayAdmitted: 0, todayDischarged: 0,
  totalBeds: 0, freeBeds: 0, occupiedBeds: 0, bedUsageRate: 0,
})
const wards = ref<WardOption[]>([])
const employees = ref<{ id: string, empName: string }[]>([])

const loadList = async () => {
  loading.value = true
  try {
    const res = await getInpatientListPage({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      patientName: query.patientName || undefined,
      wardId: query.wardId || undefined,
      admitStatus: query.admitStatus === null ? undefined : query.admitStatus,
    })
    rows.value = res.data?.records || []
    total.value = Number(res.data?.total || 0)
  } catch (e: any) {
    ElMessage.error(e?.message || '加载住院列表失败')
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const res = await getInpatientStats()
    Object.assign(stats, res.data || {})
  } catch { /* 统计失败不阻塞列表 */
  }
}

const loadWards = async () => {
  try {
    const res = await getInpatientWardList()
    wards.value = res.data || []
  } catch { /* 忽略 */
  }
}

const loadEmployees = async () => {
  try {
    const res = await getEmployeeList({})
    employees.value = (res.data || []).map((e: any) => ({id: String(e.id), empName: e.empName}))
  } catch { /* 忽略 */
  }
}

const search = () => {
  query.pageNum = 1;
  loadList()
}
const resetQuery = () => {
  query.patientName = ''
  query.wardId = null
  query.admitStatus = 1
  query.pageNum = 1
  loadList()
}

const onStatusChange = () => {
  query.pageNum = 1;
  loadList()
}

// ------------------------------------------------------------------
// 详情
// ------------------------------------------------------------------
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<any>(null)

const openDetail = async (row: InpatientRow) => {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    const res = await getInpatientDetail(row.admissionId)
    detail.value = res.data
  } catch (e: any) {
    ElMessage.error(e?.message || '加载详情失败')
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

// ------------------------------------------------------------------
// 待收治（住院证）—— 门诊 → 住院这条桥的落点
//
// 这里是三甲真实链路上最容易断的一环：门诊医生开完住院证，患者拿着证到入院处，
// 入院处按证排床收治。这个页签就是"入院处的那张待办清单"。
//
// 两个口径不可混：
// 1. 「待收治」是库里的状态；「已过期」是后端按 valid_until 实时算的展示态。
//    一条证可以同时是"待收治 + 已过期"，此时它不能再被收治（后端会拒），
//    所以界面必须把两件事都摆出来，不能让入院处以为还能用。
// 2. 收治时入院处可以**调科**（真实医院允许），但调了就不能装作是按原计划收的，
//    列表里会用「调科」标记把差异显出来（后端 deptAdjusted）。
// ------------------------------------------------------------------
const pendingLoading = ref(false)
const pendingRows = ref<AdmissionOrderRow[]>([])
const pendingTotal = ref(0)
const pendingOnly = ref<number>(1)
const pendingQuery = reactive({keyword: '', pageNum: 1, pageSize: 5})

const loadPending = async () => {
  pendingLoading.value = true
  try {
    const res = await getAdmissionOrderListPage({
      pageNum: pendingQuery.pageNum,
      pageSize: pendingQuery.pageSize,
      keyword: pendingQuery.keyword || undefined,
      onlyPending: pendingOnly.value,
    })
    pendingRows.value = res.data?.records || []
    pendingTotal.value = Number(res.data?.total || 0)
  } catch (e: any) {
    ElMessage.error(e?.message || '待收治列表加载失败')
  } finally {
    pendingLoading.value = false
  }
}

const searchPending = () => {
  pendingQuery.pageNum = 1;
  loadPending()
}

const cancelOrder = async (row: AdmissionOrderRow) => {
  try {
    const {value} = await ElMessageBox.prompt(
        `作废住院证 ${row.orderNo}（${row.patientName}）。作废必须写原因，这条记录会留痕。`,
        '作废住院证',
        {
          inputPlaceholder: '如：患者放弃住院 / 电话联系不上 / 门诊诊断已修正',
          inputValidator: (v: string) => (!!v && v.trim().length > 0) || '作废原因不能为空'
        },
    )
    await cancelAdmissionOrder({id: row.id, cancelReason: value})
    ElMessage.success('住院证已作废')
    await Promise.all([loadPending(), loadStats()])
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message || '作废失败')
  }
}

// ------------------------------------------------------------------
// 入院登记
// ------------------------------------------------------------------
const admitVisible = ref(false)
const admitLoading = ref(false)
const admitBeds = ref<BedOption[]>([])
const admitForm = reactive({
  patientId: null as string | null,
  admissionOrderId: null as string | null,
  wardId: null as string | null,
  bedId: null as string | null,
  admitDoctorId: null as string | null,
  admitTime: '',
  admitWay: 1,
  diagnosis: '',
  admitDiagnosisCode: '',
  admitDiagnosisName: '',
  remark: '',
})

/** 非空表示本次入院是「按住院证收治」，表单会锁掉患者与入院途径（由证决定） */
const admitFromOrder = ref<AdmissionOrderRow | null>(null)

const openAdmit = () => {
  Object.assign(admitForm, {
    patientId: null, admissionOrderId: null, wardId: null, bedId: null, admitDoctorId: null, admitTime: '',
    admitWay: 1, diagnosis: '', admitDiagnosisCode: '', admitDiagnosisName: '', remark: '',
  })
  admitFromOrder.value = null
  admitBeds.value = []
  admitVisible.value = true
}

/**
 * 按住院证收治：患者、入院途径、拟诊都由证决定，入院处只需要选病区/床位/入院医生。
 * 病区默认按证的「拟收治科室」自动带出——只有一个匹配病区时才自动选，
 * 多个病区时留空让入院处自己选，避免替人做决定后收错病区。
 */
const openAdmitFromOrder = async (row: AdmissionOrderRow) => {
  Object.assign(admitForm, {
    patientId: row.patientId,
    admissionOrderId: row.id,
    wardId: null, bedId: null, admitDoctorId: null, admitTime: '',
    admitWay: 1,
    diagnosis: row.diagnosisName || '',
    admitDiagnosisCode: row.diagnosisCode || '',
    admitDiagnosisName: row.diagnosisName || '',
    remark: `由住院证 ${row.orderNo} 收治`,
  })
  admitFromOrder.value = row
  admitBeds.value = []

  const matched = wards.value.filter((w) => String(w.deptId) === String(row.applyDeptId))
  if (matched.length === 1) {
    admitForm.wardId = matched[0].wardId
    await onAdmitWardChange(matched[0].wardId)
  }
  admitVisible.value = true
}

// 患者远程搜索已抽成 PatientSelect 组件
const handlePatientSelectForAdmit = (patient: any) => {
  if (!patient) return
  // PatientSelect @select 回调已给出完整患者对象，直接用
}

const onAdmitWardChange = async (wardId: string) => {
  admitForm.bedId = null
  admitBeds.value = []
  if (!wardId) return
  try {
    // 只列空闲床位：占用/维修/锁定的床选了也会被后端拒，不如一开始就不给选
    const res = await getInpatientBedList({wardId, bedStatus: 1})
    admitBeds.value = res.data || []
  } catch {
    admitBeds.value = []
  }
}

const submitAdmit = async () => {
  if (!admitForm.patientId) return ElMessage.warning('请选择患者')
  if (!admitForm.wardId) return ElMessage.warning('请选择病区')
  if (!admitForm.bedId) return ElMessage.warning('请选择床位')
  if (!admitForm.admitDoctorId) return ElMessage.warning('请选择入院医生')
  admitLoading.value = true
  try {
    const ordered = !!admitForm.admissionOrderId
    const res = await admitInpatient({...admitForm})
    ElMessage.success(ordered
        ? `按住院证收治成功（入院ID ${res.data}）；住院证已置为「已收治」并回填入院ID`
        : `入院登记成功，住院号已生成（入院ID ${res.data}）`)
    admitVisible.value = false
    await Promise.all([loadList(), loadStats(), loadWards(), loadPending()])
  } catch (e: any) {
    ElMessage.error(e?.message || '入院登记失败')
  } finally {
    admitLoading.value = false
  }
}

// ------------------------------------------------------------------
// 换床
// ------------------------------------------------------------------
const transferVisible = ref(false)
const transferLoading = ref(false)
const transferBeds = ref<BedOption[]>([])
const transferForm = reactive({admissionId: '', newBedId: null as string | null, reason: '', wardName: ''})

const openTransfer = async (row: InpatientRow) => {
  transferForm.admissionId = row.admissionId
  transferForm.newBedId = null
  transferForm.reason = ''
  transferForm.wardName = row.wardName || ''
  transferBeds.value = []
  transferVisible.value = true
  try {
    const res = await getInpatientBedList({wardId: row.wardId, bedStatus: 1})
    // 后端规则：只能同科室换床，且不能换到原床位，这里先把原床位剔掉
    transferBeds.value = (res.data || []).filter((b: BedOption) => String(b.bedId) !== String(row.bedId))
  } catch { /* 忽略 */
  }
}

const submitTransfer = async () => {
  if (!transferForm.newBedId) return ElMessage.warning('请选择目标床位')
  transferLoading.value = true
  try {
    await transferInpatientBed({...transferForm})
    ElMessage.success('换床成功')
    transferVisible.value = false
    await Promise.all([loadList(), loadWards()])
  } catch (e: any) {
    ElMessage.error(e?.message || '换床失败')
  } finally {
    transferLoading.value = false
  }
}

// ------------------------------------------------------------------
// 出院办理
// ------------------------------------------------------------------
const dischargeVisible = ref(false)
const dischargeLoading = ref(false)
const dischargeForm = reactive({
  admissionId: '',
  dischargeTime: '',
  dischargeDoctorId: null as string | null,
  dischargeWay: 1,
  deathFlag: 0,
  dischargeDiagnosis: '',
  dischargeDiagnosisCode: '',
  dischargeSummary: '',
  patientName: '',
  admissionNo: '',
})

const openDischarge = (row: InpatientRow) => {
  Object.assign(dischargeForm, {
    admissionId: row.admissionId,
    dischargeTime: '',
    dischargeDoctorId: null,
    dischargeWay: 1,
    deathFlag: 0,
    dischargeDiagnosis: row.mainDiagnosisName || row.diagnosis || '',
    dischargeDiagnosisCode: '',
    dischargeSummary: '',
    patientName: row.patientName || '',
    admissionNo: row.admissionNo,
  })
  dischargeVisible.value = true
}

// 死亡标志与离院方式必须一致：这是病案首页的硬约束，后端会拒，前端先联动起来
const onDischargeWayChange = (v: number) => {
  if (v === 5) dischargeForm.deathFlag = 1
  else dischargeForm.deathFlag = 0
}
const onDeathFlagChange = (v: number) => {
  if (v === 1) dischargeForm.dischargeWay = 5
  else if (dischargeForm.dischargeWay === 5) dischargeForm.dischargeWay = 1
}

const submitDischarge = async () => {
  dischargeLoading.value = true
  try {
    await dischargeInpatient({
      ...dischargeForm,
      dischargeTime: dischargeForm.dischargeTime || undefined,
    })
    ElMessage.success('出院办理成功：床位已释放、病案首页已回写住院天数与离院方式')
    dischargeVisible.value = false
    await Promise.all([loadList(), loadStats(), loadWards()])
  } catch (e: any) {
    ElMessage.error(e?.message || '出院办理失败')
  } finally {
    dischargeLoading.value = false
  }
}

// ------------------------------------------------------------------
// 病案首页
// ------------------------------------------------------------------
const summaryVisible = ref(false)
const summaryLoading = ref(false)
const summaryForm = reactive({
  admissionId: '',
  ageUnit: 1 as number | null,
  admitWay: 1 as number | null,
  isRescue: 0 as number | null,
  isCritical: 0 as number | null,
  totalAmount: null as number | null,
  westernDrugAmount: null as number | null,
  chineseDrugAmount: null as number | null,
  herbalAmount: null as number | null,
  examAmount: null as number | null,
  labAmount: null as number | null,
  treatmentAmount: null as number | null,
  operationAmount: null as number | null,
  materialAmount: null as number | null,
  bedAmount: null as number | null,
  nursingAmount: null as number | null,
  otherAmount: null as number | null,
  remark: '',
  diagnoses: [] as any[],
  operations: [] as any[],
})
const summaryMeta = reactive({
  patientName: '',
  admissionNo: '',
  status: 0,
  days: 0,
  dischargeWay: null as number | null,
  readmit31d: null as number | null
})

const openSummary = async (row: InpatientRow) => {
  summaryLoading.value = true
  summaryVisible.value = true
  try {
    const res = await getInpatientDetail(row.admissionId)
    const d = res.data || {}
    const s = d.summary || {}
    Object.assign(summaryForm, {
      admissionId: row.admissionId,
      ageUnit: s.ageUnit ?? 1,
      admitWay: s.admitWay ?? d.admission?.admitWay ?? 1,
      isRescue: s.isRescue ?? 0,
      isCritical: s.isCritical ?? 0,
      totalAmount: s.totalAmount ?? null,
      westernDrugAmount: s.westernDrugAmount ?? null,
      chineseDrugAmount: s.chineseDrugAmount ?? null,
      herbalAmount: s.herbalAmount ?? null,
      examAmount: s.examAmount ?? null,
      labAmount: s.labAmount ?? null,
      treatmentAmount: s.treatmentAmount ?? null,
      operationAmount: s.operationAmount ?? null,
      materialAmount: s.materialAmount ?? null,
      bedAmount: s.bedAmount ?? null,
      nursingAmount: s.nursingAmount ?? null,
      otherAmount: s.otherAmount ?? null,
      remark: s.remark ?? '',
      diagnoses: (d.diagnoses || []).map((x: any) => ({...x})),
      operations: (d.operations || []).map((x: any) => ({...x})),
    })
    Object.assign(summaryMeta, {
      patientName: d.admission?.patientName || '',
      admissionNo: d.admission?.admissionNo || '',
      status: s.summaryStatus ?? 1,
      days: d.admission?.inpatientDays ?? 0,
      dischargeWay: s.dischargeWay ?? null,
      readmit31d: s.readmit31d ?? null,
    })
    // 诊断条数的主诊断约束由后端把关，但主要诊断必须排第一条，前端保存前顺手排一下
    normalizeOrder()
  } catch (e: any) {
    ElMessage.error(e?.message || '加载病案首页失败')
    summaryVisible.value = false
  } finally {
    summaryLoading.value = false
  }
}

// 主诊断 / 主手术恒排第一条，前端展示顺序与后端落库顺序保持一致
const normalizeOrder = () => {
  summaryForm.diagnoses.sort((a, b) => (a.diagType === 1 ? 0 : 1) - (b.diagType === 1 ? 0 : 1))
  summaryForm.operations.sort((a, b) => (a.isMain === 1 ? 0 : 1) - (b.isMain === 1 ? 0 : 1))
}

const addDiagnosis = () => {
  summaryForm.diagnoses.push({
    diagType: summaryForm.diagnoses.length === 0 ? 1 : 2,
    icdCode: '', icdName: '', admitCondition: 1, ccLevel: 'NONE', diagnosisBasis: '',
  })
}
const addOperation = () => {
  summaryForm.operations.push({
    isMain: summaryForm.operations.length === 0 ? 1 : 0,
    operationCode: '', operationName: '', operationDate: '', operationLevel: 2,
    incisionLevel: 1, anesthesiaType: 1, surgeonName: '', operationBasis: '',
  })
}
const removeDiagnosis = (i: number) => summaryForm.diagnoses.splice(i, 1)
const removeOperation = (i: number) => summaryForm.operations.splice(i, 1)

// 主诊断唯一性：勾了新的主诊断，就把旧的降为「其他诊断」，不让用户提交一个必然被拒的请求
const onDiagTypeChange = (row: any) => {
  if (row.diagType === 1) {
    summaryForm.diagnoses.forEach(d => {
      if (d !== row) d.diagType = 2
    })
  }
  normalizeOrder()
}
const onMainOpChange = (row: any) => {
  if (row.isMain === 1) {
    summaryForm.operations.forEach(o => {
      if (o !== row) o.isMain = 0
    })
  }
  normalizeOrder()
}

const submitSummary = async () => {
  summaryLoading.value = true
  try {
    await saveInpatientSummary({...summaryForm})
    ElMessage.success('病案首页已保存')
    summaryVisible.value = false
    await loadList()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存病案首页失败')
  } finally {
    summaryLoading.value = false
  }
}

const confirmDischargeFromList = (row: InpatientRow) => {
  ElMessageBox.confirm(
      `确认为「${row.patientName}」办理出院？出院会释放床位并把住院天数、离院方式回写到病案首页。`,
      '出院确认',
      {type: 'warning', confirmButtonText: '去填出院信息', cancelButtonText: '取消'}
  ).then(() => openDischarge(row)).catch(() => { /* 取消 */
  })
}

// 按病案首页存在与否给出「可编辑」判断：归档后只读
const summaryEditable = computed(() => summaryMeta.status !== 3)

onMounted(async () => {
  await Promise.all([loadWards(), loadEmployees()])
  await Promise.all([loadList(), loadStats(), loadPending()])
})
</script>

<template>
  <div class="space-y-6">
    <!-- 头部 -->
    <div class="flex items-start justify-between">
      <div class="flex gap-2">
        <el-button :icon="Refresh" @click="() => { loadList(); loadStats(); loadWards(); loadPending() }">刷新
        </el-button>
        <el-button v-perm="'ipd:inpatient:add'" type="primary" :icon="Plus" @click="openAdmit">入院登记</el-button>
      </div>
    </div>

    <!-- 统计卡片：数值全部来自后端（床位口径为 sys_bed 实时统计） -->
    <div class="grid grid-cols-2 gap-4 lg:grid-cols-5">
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-blue-50 p-2.5">
            <el-icon class="h-5 w-5 text-blue-600">
              <Suitcase/>
            </el-icon>
          </div>
          <div>
            <p class="text-xl font-bold text-blue-600">{{ stats.inHospitalCount }}</p>
            <p class="text-xs text-slate-500">在院患者</p>
          </div>
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-purple-50 p-2.5">
            <el-icon class="h-5 w-5 text-purple-600">
              <CircleCheck/>
            </el-icon>
          </div>
          <div>
            <p class="text-xl font-bold text-purple-600">{{ stats.todayAdmitted }}</p>
            <p class="text-xs text-slate-500">今日入院</p>
          </div>
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-amber-50 p-2.5">
            <el-icon class="h-5 w-5 text-amber-600">
              <Clock/>
            </el-icon>
          </div>
          <div>
            <p class="text-xl font-bold text-amber-600">{{ stats.todayDischarged }}</p>
            <p class="text-xs text-slate-500">今日出院</p>
          </div>
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-emerald-50 p-2.5">
            <el-icon class="h-5 w-5 text-emerald-600">
              <User/>
            </el-icon>
          </div>
          <div>
            <p class="text-xl font-bold text-emerald-600">{{ stats.bedUsageRate }}%</p>
            <p class="text-xs text-slate-500">
              床位使用率（{{ stats.occupiedBeds }}/{{ stats.totalBeds }}，空闲 {{ stats.freeBeds }}）
            </p>
          </div>
        </div>
      </div>
      <div class="rounded-lg border border-amber-200 bg-amber-50/40 p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-amber-100 p-2.5">
            <el-icon class="h-5 w-5 text-amber-700">
              <Tickets/>
            </el-icon>
          </div>
          <div>
            <p class="text-xl font-bold text-amber-700">{{ stats.pendingAdmissionOrderCount }}</p>
            <p class="text-xs text-slate-500">待收治住院证（门诊已开证、未排床）</p>
          </div>
        </div>
      </div>
    </div>

    <!-- ================= 待收治（住院证）：门诊 → 住院这条桥的落点 ================= -->
    <div class="rounded-lg border border-amber-200 bg-white shadow-sm">
      <div class="flex flex-wrap items-center justify-between gap-3 border-b border-amber-100 bg-amber-50/50 px-4 py-3">
        <div class="flex items-center gap-2">
          <el-icon class="text-amber-700">
            <Tickets/>
          </el-icon>
          <span class="font-semibold text-slate-800">待收治（住院证）</span>
          <el-tag v-if="pendingTotal > 0" type="warning" size="small" effect="dark">{{ pendingTotal }}</el-tag>
        </div>
        <div class="flex items-center gap-2">
          <el-select v-model="pendingOnly" class="!w-40" size="small" @change="searchPending">
            <el-option :value="1" label="只看可用（未过期）"/>
            <el-option :value="0" label="全部住院证"/>
          </el-select>
          <el-input
              v-model="pendingQuery.keyword"
              placeholder="患者姓名 / 住院证号 / 挂号号"
              :prefix-icon="Search"
              clearable
              size="small"
              class="!w-60"
              @keyup.enter="searchPending"
              @clear="searchPending"
          />
          <el-button size="small" :icon="Search" @click="searchPending">查询</el-button>
          <el-button size="small" :icon="Refresh" @click="loadPending">刷新</el-button>
        </div>
      </div>

      <el-table v-loading="pendingLoading" :data="pendingRows" style="width: 100%" empty-text="暂无待收治住院证">
        <el-table-column prop="orderNo" label="住院证号" width="140"/>
        <el-table-column label="患者" min-width="150">
          <template #default="{ row }">
            <div class="font-medium text-slate-800">{{ row.patientName }}</div>
            <div class="text-xs text-slate-500">{{ row.genderText }} · {{ row.age ?? '—' }}岁 · {{
                row.phone || '无电话'
              }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="来源门诊" min-width="170">
          <template #default="{ row }">
            <div class="text-slate-700">{{ row.sourceDeptName || '—' }} · {{ row.sourceDoctorName || '—' }}</div>
            <div class="text-xs text-slate-500">挂号号 {{ row.registNo || '—' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="拟收治科室" width="130">
          <template #default="{ row }">{{ row.applyDeptName || '—' }}</template>
        </el-table-column>
        <el-table-column label="拟诊" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <div>{{ row.diagnosisName || '—' }}</div>
            <div v-if="row.diagnosisCode" class="text-xs text-slate-400">{{ row.diagnosisCode }}</div>
          </template>
        </el-table-column>
        <el-table-column label="有效期至" width="150">
          <template #default="{ row }">
            <span :class="row.expired ? 'text-rose-500' : 'text-slate-600'">{{ row.validUntil || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="orderStatusMap[row.orderStatus]?.type || 'info'" size="small">
              {{ row.orderStatusText || orderStatusMap[row.orderStatus]?.label || `未知(${row.orderStatus})` }}
            </el-tag>
            <!-- 过期是算出来的展示态，必须和"待收治"同时可见 -->
            <el-tag v-if="row.expired" type="danger" size="small" effect="plain" class="ml-1">已过期</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="收治结果" min-width="170">
          <template #default="{ row }">
            <template v-if="row.admissionId">
              <div class="text-slate-700">{{ row.admissionNo || '—' }}</div>
              <div class="text-xs text-slate-500">
                收治科室 {{ row.admitDeptName || '—' }}
                <el-tag v-if="row.deptAdjusted" type="warning" size="small" effect="plain" class="ml-1">调科</el-tag>
              </div>
            </template>
            <span v-else class="text-slate-400">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
                v-if="row.orderStatus === 1 && !row.expired"
                v-perm="'ipd:inpatient:add'"
                type="primary"
                size="small"
                :icon="Check"
                @click="openAdmitFromOrder(row)"
            >收治入院
            </el-button>
            <el-button
                v-if="row.orderStatus === 1"
                v-perm="'ipd:inpatient:edit'"
                type="danger"
                size="small"
                link
                :icon="CircleClose"
                @click="cancelOrder(row)"
            >作废
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="flex justify-end px-4 py-3">
        <el-pagination
            v-model:current-page="pendingQuery.pageNum"
            :page-size="pendingQuery.pageSize"
            :total="pendingTotal"
            layout="total, prev, pager, next"
            small
            @current-change="loadPending"
        />
      </div>
    </div>

    <!-- 查询条件 -->
    <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div class="flex flex-wrap items-center gap-3">
        <el-input
            v-model="query.patientName"
            placeholder="患者姓名 / 住院号"
            :prefix-icon="Search"
            clearable
            class="!w-56"
            @keyup.enter="search"
            @clear="search"
        />
        <el-select v-model="query.wardId" placeholder="全部病区" clearable class="!w-48" @change="search">
          <el-option v-for="w in wards" :key="w.wardId" :label="w.wardName" :value="w.wardId">
            <span>{{ w.wardName }}</span>
            <span class="float-right text-xs text-slate-400">{{ w.occupiedBeds }}/{{ w.totalBeds }}</span>
          </el-option>
        </el-select>
        <el-radio-group v-model="query.admitStatus" @change="onStatusChange">
          <el-radio-button :value="1">在院</el-radio-button>
          <el-radio-button :value="0">已出院</el-radio-button>
          <el-radio-button :value="null">全部</el-radio-button>
        </el-radio-group>
        <el-button type="primary" :icon="Search" @click="search">查询</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </div>
    </div>

    <!-- 列表 -->
    <div class="rounded-lg border border-slate-200 bg-white shadow-sm">
      <el-table v-loading="loading" :data="rows" style="width: 100%">
        <el-table-column prop="admissionNo" label="住院号" width="140"/>
        <el-table-column label="患者" min-width="140">
          <template #default="{ row }">
            <div class="font-medium text-slate-800">{{ row.patientName || '—' }}</div>
            <div class="text-xs text-slate-400">{{ genderText(row.gender) }} · {{ row.age ?? '—' }}岁</div>
          </template>
        </el-table-column>
        <el-table-column label="科室 / 病区" min-width="160">
          <template #default="{ row }">
            <div>{{ row.deptName || '—' }}</div>
            <div class="text-xs text-slate-400">{{ row.wardName || '—' }} · {{ row.bedNo || '未分床' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="入院途径" width="90" align="center">
          <template #default="{ row }">{{ textOf(admitWayMap, row.admitWay) }}</template>
        </el-table-column>
        <el-table-column label="门诊来源" width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="text-slate-700">{{ row.registNo || '—' }}</div>
            <div class="text-xs text-slate-400">{{ row.admissionOrderNo || '直接入院' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="入院时间" width="150">
          <template #default="{ row }">{{ row.admitTime || '—' }}</template>
        </el-table-column>
        <el-table-column label="住院天数" width="90" align="center">
          <template #default="{ row }">{{ row.inpatientDays ?? 0 }} 天</template>
        </el-table-column>
        <el-table-column label="主要诊断" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.mainDiagnosisName || row.diagnosis || '—' }}</template>
        </el-table-column>
        <el-table-column label="主管医生" width="100">
          <template #default="{ row }">{{ row.doctorName || '—' }}</template>
        </el-table-column>
        <el-table-column label="病案首页" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.summaryStatus" :type="summaryStatusMap[row.summaryStatus]?.type || 'info'" effect="plain"
                    size="small">
              {{ summaryStatusMap[row.summaryStatus]?.label || `未知(${row.summaryStatus})` }}
            </el-tag>
            <span v-else class="text-xs text-slate-400">未生成</span>
          </template>
        </el-table-column>
        <el-table-column label="总费用" width="110" align="right">
          <template #default="{ row }">{{ money(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button link type="primary" size="small" :icon="View" @click="openDetail(row)">详情</el-button>
            <el-button v-perm="'ipd:inpatient:edit'" link type="primary" size="small" :icon="Document"
                       @click="openSummary(row)">病案首页
            </el-button>
            <el-button
                v-perm="'ipd:inpatient:edit'"
                link type="primary" size="small" :icon="Warning"
                :disabled="row.admitStatus !== 1"
                @click="openTransfer(row)"
            >换床
            </el-button>
            <el-button
                v-perm="'ipd:inpatient:edit'"
                link type="danger" size="small" :icon="Check"
                :disabled="row.admitStatus !== 1"
                @click="confirmDischargeFromList(row)"
            >出院
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="flex items-center justify-between border-t border-slate-100 px-4 py-3">
        <span class="text-xs text-slate-400">共 {{ total }} 条</span>
        <el-pagination
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            :total="total"
            :page-sizes="PAGE_SIZES"
            layout="sizes, prev, pager, next"
            @current-change="loadList"
            @size-change="() => { query.pageNum = 1; loadList() }"
        />
      </div>
    </div>

    <!-- ================= 详情 ================= -->
    <el-drawer v-model="detailVisible" title="住院详情" size="880px" destroy-on-close>
      <div v-loading="detailLoading" class="space-y-4">
        <template v-if="detail">
          <div class="rounded-lg border border-slate-200 p-4">
            <div class="mb-3 flex items-center gap-2">
              <span class="font-semibold text-slate-800">{{ detail.admission?.patientName }}</span>
              <el-tag size="small" effect="plain" :type="detail.admission?.admitStatus === 1 ? 'primary' : 'info'">
                {{ detail.admission?.admitStatus === 1 ? '在院' : '已出院' }}
              </el-tag>
              <el-tag size="small" effect="plain" type="warning">住院号 {{ detail.admission?.admissionNo }}</el-tag>
            </div>
            <div class="grid grid-cols-3 gap-x-4 gap-y-2 text-sm">
              <div><span class="text-slate-400">性别 / 年龄</span>：{{ genderText(detail.admission?.gender) }} ·
                {{ detail.admission?.age ?? '—' }}岁
              </div>
              <div><span class="text-slate-400">科室</span>：{{ detail.admission?.deptName || '—' }}</div>
              <div><span class="text-slate-400">病区 / 床位</span>：{{ detail.admission?.wardName || '—' }} ·
                {{ detail.admission?.bedNo || '—' }}
              </div>
              <div><span class="text-slate-400">入院途径</span>：{{ textOf(admitWayMap, detail.admission?.admitWay) }}
              </div>
              <div><span class="text-slate-400">入院时间</span>：{{ detail.admission?.admitTime || '—' }}</div>
              <div><span class="text-slate-400">出院时间</span>：{{ detail.admission?.dischargeTime || '—' }}</div>
              <div><span class="text-slate-400">住院天数</span>：{{ detail.admission?.inpatientDays ?? 0 }} 天</div>
              <div><span class="text-slate-400">主管医生</span>：{{ detail.admission?.doctorName || '—' }}</div>
              <div><span class="text-slate-400">医保卡号</span>：{{ detail.admission?.medicalInsuranceNo || '—' }}</div>
              <div class="col-span-3"><span class="text-slate-400">入院诊断</span>：{{
                  detail.admission?.diagnosis || '—'
                }}
              </div>
            </div>
          </div>

          <div class="rounded-lg border border-slate-200 p-4">
            <div class="mb-3 flex items-center gap-2">
              <span class="font-semibold text-slate-800">病案首页</span>
              <el-tag size="small" :type="detail.summaryStatusText === '已归档' ? 'success' : 'info'" effect="plain">
                {{ detail.summaryStatusText }}
              </el-tag>
              <el-tag v-if="detail.summary?.readmit31d === 1" size="small" type="danger" effect="plain">31 日内再入院
              </el-tag>
              <el-tag v-if="detail.summary?.isSurgery === 1" size="small" type="warning" effect="plain">有手术</el-tag>
            </div>
            <div class="grid grid-cols-3 gap-x-4 gap-y-2 text-sm">
              <div><span class="text-slate-400">主要诊断</span>：{{
                  detail.summary?.mainDiagnosisName || '—'
                }}（{{ detail.summary?.mainDiagnosisCode || '未编码' }}）
              </div>
              <div><span class="text-slate-400">离院方式</span>：{{
                  textOf(dischargeWayMap, detail.summary?.dischargeWay)
                }}
              </div>
              <div><span class="text-slate-400">是否危重</span>：{{
                  detail.summary?.isCritical === 1 ? '是' : detail.summary?.isCritical === 0 ? '否' : '—'
                }}
              </div>
              <div><span class="text-slate-400">住院总费用</span>：<span
                  class="font-semibold text-blue-600">{{ money(detail.summary?.totalAmount) }}</span></div>
              <div><span class="text-slate-400">西药费</span>：{{ money(detail.summary?.westernDrugAmount) }}</div>
              <div><span class="text-slate-400">检查费</span>：{{ money(detail.summary?.examAmount) }}</div>
              <div><span class="text-slate-400">检验费</span>：{{ money(detail.summary?.labAmount) }}</div>
              <div><span class="text-slate-400">手术费</span>：{{ money(detail.summary?.operationAmount) }}</div>
              <div><span class="text-slate-400">床位费</span>：{{ money(detail.summary?.bedAmount) }}</div>
              <div><span class="text-slate-400">护理费</span>：{{ money(detail.summary?.nursingAmount) }}</div>
              <div><span class="text-slate-400">耗材费</span>：{{ money(detail.summary?.materialAmount) }}</div>
              <div><span class="text-slate-400">治疗费</span>：{{ money(detail.summary?.treatmentAmount) }}</div>
            </div>
          </div>

          <div class="rounded-lg border border-slate-200 p-4">
            <p class="mb-2 font-semibold text-slate-800">诊断明细</p>
            <el-table :data="detail.diagnoses || []" size="small" border>
              <el-table-column prop="seqNo" label="序号" width="60" align="center"/>
              <el-table-column label="类型" width="90">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.diagType === 1 ? 'danger' : 'info'" effect="plain">
                    {{ row.diagType === 1 ? '主要诊断' : '其他诊断' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="icdCode" label="ICD-10" width="100"/>
              <el-table-column prop="icdName" label="诊断名称" min-width="140"/>
              <el-table-column label="入院病情" width="100">
                <template #default="{ row }">{{ textOf(admitConditionMap, row.admitCondition) }}</template>
              </el-table-column>
              <el-table-column prop="ccLevel" label="CC/MCC" width="90"/>
              <el-table-column prop="diagnosisBasis" label="诊断依据" min-width="180" show-overflow-tooltip/>
            </el-table>
            <p v-if="!(detail.diagnoses || []).length" class="py-4 text-center text-xs text-slate-400">
              未录入诊断明细 —— 没有主要诊断，DRG 无法分组，医保结算清单也过不了
            </p>
          </div>

          <div class="rounded-lg border border-slate-200 p-4">
            <p class="mb-2 font-semibold text-slate-800">手术操作明细</p>
            <el-table :data="detail.operations || []" size="small" border>
              <el-table-column prop="seqNo" label="序号" width="60" align="center"/>
              <el-table-column label="主要" width="70" align="center">
                <template #default="{ row }">
                  <el-tag v-if="row.isMain === 1" size="small" type="danger" effect="plain">主</el-tag>
                  <span v-else class="text-xs text-slate-400">—</span>
                </template>
              </el-table-column>
              <el-table-column prop="operationCode" label="ICD-9-CM-3" width="120"/>
              <el-table-column prop="operationName" label="手术操作名称" min-width="140"/>
              <el-table-column prop="operationDate" label="手术日期" width="150"/>
              <el-table-column label="级别" width="80">
                <template #default="{ row }">{{ textOf(operationLevelMap, row.operationLevel) }}</template>
              </el-table-column>
              <el-table-column label="切口" width="80">
                <template #default="{ row }">{{ textOf(incisionLevelMap, row.incisionLevel) }}</template>
              </el-table-column>
              <el-table-column label="麻醉" width="90">
                <template #default="{ row }">{{ textOf(anesthesiaMap, row.anesthesiaType) }}</template>
              </el-table-column>
              <el-table-column prop="surgeonName" label="主刀" width="90"/>
              <el-table-column prop="operationBasis" label="手术依据" min-width="160" show-overflow-tooltip/>
            </el-table>
            <p v-if="!(detail.operations || []).length" class="py-4 text-center text-xs text-slate-400">
              无手术操作记录
            </p>
          </div>
        </template>
      </div>
    </el-drawer>

    <!-- ================= 入院登记 ================= -->
    <el-dialog
        v-model="admitVisible"
        :title="admitFromOrder ? '按住院证收治入院' : '入院登记（分床并占用床位）'"
        width="720px"
        destroy-on-close
    >
      <div v-if="admitFromOrder" class="mb-4 rounded-lg border border-amber-200 bg-amber-50 p-3 text-xs text-slate-600">
        <div class="font-semibold text-slate-800">住院证 {{ admitFromOrder.orderNo }}</div>
        <div class="mt-1">
          来源门诊：{{ admitFromOrder.sourceDeptName || '—' }} · {{ admitFromOrder.sourceDoctorName || '—' }}
          （挂号号 {{ admitFromOrder.registNo || '—' }}）
        </div>
        <div>拟收治：{{ admitFromOrder.applyDeptName || '—' }} · 拟诊：{{ admitFromOrder.diagnosisName || '—' }}</div>
        <div class="mt-1 text-slate-500">
          入院途径由证决定（门诊转住院），患者与拟诊已锁定；入院处只需选病区、床位、入院医生。
          如实际收治科室与拟收治不同，系统会记为「调科」。
        </div>
      </div>
      <el-form :model="admitForm" label-width="110px">
        <el-form-item label="患者" required>
          <PatientSelect
              v-model="admitForm.patientId"
              placeholder="输入姓名 / 患者号 / 手机号 / 身份证号搜索"
              :disabled="!!admitFromOrder"
              @select="handlePatientSelectForAdmit"
          />
        </el-form-item>
        <div class="grid grid-cols-2 gap-x-4">
          <el-form-item label="病区" required>
            <el-select v-model="admitForm.wardId" placeholder="选择病区" class="!w-full" @change="onAdmitWardChange">
              <el-option v-for="w in wards" :key="w.wardId" :label="w.wardName" :value="w.wardId">
                <span>{{ w.wardName }}</span>
                <span class="float-right text-xs" :class="w.freeBeds > 0 ? 'text-emerald-500' : 'text-rose-500'">
                  空闲 {{ w.freeBeds }}
                </span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="床位" required>
            <el-select v-model="admitForm.bedId" placeholder="先选病区" class="!w-full" :disabled="!admitForm.wardId">
              <el-option v-for="b in admitBeds" :key="b.bedId" :label="b.bedNo" :value="b.bedId"/>
            </el-select>
            <p v-if="admitForm.wardId && !admitBeds.length" class="mt-1 text-xs text-rose-500">该病区暂无空闲床位</p>
          </el-form-item>
          <el-form-item label="入院途径" required>
            <el-select v-model="admitForm.admitWay" class="!w-full" :disabled="!!admitFromOrder">
              <el-option v-for="(label, code) in admitWayMap" :key="code" :label="label" :value="Number(code)"/>
            </el-select>
            <p v-if="admitFromOrder" class="mt-1 text-xs text-amber-600">有住院证 =
              门诊转住院，途径固定为「门诊」，不允许手填</p>
          </el-form-item>
          <el-form-item label="入院医生" required>
            <el-select v-model="admitForm.admitDoctorId" class="!w-full" placeholder="选择医生">
              <el-option v-for="e in employees" :key="e.id" :label="e.empName" :value="e.id"/>
            </el-select>
          </el-form-item>
          <el-form-item label="入院时间">
            <el-date-picker
                v-model="admitForm.admitTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="不填取当前时间"
                class="!w-full"
            />
          </el-form-item>
          <el-form-item label="入院诊断编码">
            <el-input v-model="admitForm.admitDiagnosisCode" placeholder="ICD-10，如 J18.9"/>
          </el-form-item>
          <el-form-item label="入院诊断名称">
            <el-input v-model="admitForm.admitDiagnosisName" placeholder="如 肺炎"/>
          </el-form-item>
        </div>
        <el-form-item label="入院诊断(文本)">
          <el-input v-model="admitForm.diagnosis" type="textarea" :rows="2" placeholder="入院时的初步诊断描述"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="admitForm.remark"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="admitVisible = false">取消</el-button>
        <el-button v-perm="'ipd:inpatient:add'" type="primary" :loading="admitLoading" @click="submitAdmit">
          {{ admitFromOrder ? '确认收治' : '确认登记' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ================= 换床 ================= -->
    <el-dialog v-model="transferVisible" title="换床" width="520px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="病区">
          <el-input :model-value="transferForm.wardName" disabled/>
        </el-form-item>
        <el-form-item label="目标床位" required>
          <el-select v-model="transferForm.newBedId" placeholder="本区空闲床位" class="!w-full">
            <el-option v-for="b in transferBeds" :key="b.bedId" :label="b.bedNo" :value="b.bedId"/>
          </el-select>
          <p v-if="!transferBeds.length" class="mt-1 text-xs text-rose-500">该病区没有其他空闲床位</p>
        </el-form-item>
        <el-form-item label="换床原因">
          <el-input v-model="transferForm.reason" placeholder="如 病情变化需调至监护床位"/>
        </el-form-item>
      </el-form>
      <div class="rounded-lg bg-slate-50 p-3 text-xs text-slate-500">
        只能在同一科室内部换床；跨科室属于「转科」，走转科流程并需要重新确定主管医师。
      </div>
      <template #footer>
        <el-button @click="transferVisible = false">取消</el-button>
        <el-button v-perm="'ipd:inpatient:edit'" type="primary" :loading="transferLoading" @click="submitTransfer">
          确认换床
        </el-button>
      </template>
    </el-dialog>

    <!-- ================= 出院办理 ================= -->
    <el-dialog v-model="dischargeVisible" title="出院办理" width="640px" destroy-on-close>
      <div class="mb-3 rounded-lg bg-amber-50 p-3 text-xs text-amber-700">
        {{ dischargeForm.patientName }} · {{ dischargeForm.admissionNo }} —— 提交后：床位释放、入院状态置已出院、
        病案首页回写住院天数与离院方式，并自动判定 31 日内再入院。
      </div>
      <el-form :model="dischargeForm" label-width="110px">
        <div class="grid grid-cols-2 gap-x-4">
          <el-form-item label="离院方式" required>
            <el-select v-model="dischargeForm.dischargeWay" class="!w-full" @change="onDischargeWayChange">
              <el-option v-for="(label, code) in dischargeWayMap" :key="code" :label="label" :value="Number(code)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="是否死亡">
            <el-radio-group v-model="dischargeForm.deathFlag" @change="onDeathFlagChange">
              <el-radio :value="0">否</el-radio>
              <el-radio :value="1">是</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="出院时间">
            <el-date-picker
                v-model="dischargeForm.dischargeTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="不填取当前时间"
                class="!w-full"
            />
          </el-form-item>
          <el-form-item label="出院医生">
            <el-select v-model="dischargeForm.dischargeDoctorId" class="!w-full" clearable placeholder="选择医生">
              <el-option v-for="e in employees" :key="e.id" :label="e.empName" :value="e.id"/>
            </el-select>
          </el-form-item>
          <el-form-item label="出院诊断编码">
            <el-input v-model="dischargeForm.dischargeDiagnosisCode" placeholder="ICD-10"/>
          </el-form-item>
          <el-form-item label="出院诊断">
            <el-input v-model="dischargeForm.dischargeDiagnosis" placeholder="如 肺炎（治愈）"/>
          </el-form-item>
        </div>
        <el-form-item label="出院小结">
          <el-input v-model="dischargeForm.dischargeSummary" type="textarea" :rows="3"
                    placeholder="住院经过、治疗结果、出院带药与随访要求"/>
        </el-form-item>
      </el-form>
      <div class="rounded-lg bg-slate-50 p-3 text-xs text-slate-500">
        死亡病例必须同时满足「是否死亡=是」与「离院方式=死亡」，两者不一致会被拒绝 —— 这两个字段是医保审核死亡病例的第一着眼点。
      </div>
      <template #footer>
        <el-button @click="dischargeVisible = false">取消</el-button>
        <el-button v-perm="'ipd:inpatient:edit'" type="primary" :loading="dischargeLoading" @click="submitDischarge">
          确认出院
        </el-button>
      </template>
    </el-dialog>

    <!-- ================= 病案首页 ================= -->
    <el-dialog v-model="summaryVisible" :title="`病案首页 - ${summaryMeta.patientName}（${summaryMeta.admissionNo}）`"
               width="1080px" top="4vh" destroy-on-close>
      <div v-loading="summaryLoading" class="max-h-[70vh] space-y-5 overflow-y-auto pr-1">
        <div class="flex flex-wrap items-center gap-2 rounded-lg bg-slate-50 p-3 text-xs">
          <el-tag size="small" effect="plain" :type="summaryStatusMap[summaryMeta.status]?.type || 'info'">
            {{ summaryStatusMap[summaryMeta.status]?.label || '未生成' }}
          </el-tag>
          <span class="text-slate-500">住院天数：{{ summaryMeta.days }} 天</span>
          <span class="text-slate-500">离院方式：{{ textOf(dischargeWayMap, summaryMeta.dischargeWay) }}</span>
          <span v-if="summaryMeta.readmit31d === 1" class="font-medium text-rose-600">31 日内再入院</span>
          <span class="ml-auto text-slate-400">归档后本页只读</span>
        </div>

        <fieldset :disabled="!summaryEditable" class="space-y-5">
          <!-- 基本信息 -->
          <div>
            <p class="mb-2 font-semibold text-slate-800">基本信息</p>
            <div class="grid grid-cols-3 gap-x-4">
              <el-form-item label="入院途径" label-width="90px">
                <el-select v-model="summaryForm.admitWay" class="!w-full">
                  <el-option v-for="(label, code) in admitWayMap" :key="code" :label="label" :value="Number(code)"/>
                </el-select>
              </el-form-item>
              <el-form-item label="是否抢救" label-width="90px">
                <el-radio-group v-model="summaryForm.isRescue">
                  <el-radio :value="0">否</el-radio>
                  <el-radio :value="1">是</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="是否危重" label-width="90px">
                <el-radio-group v-model="summaryForm.isCritical">
                  <el-radio :value="0">否</el-radio>
                  <el-radio :value="1">是</el-radio>
                </el-radio-group>
              </el-form-item>
            </div>
          </div>

          <!-- 诊断明细 -->
          <div class="rounded-lg border border-slate-200 p-4">
            <div class="mb-2 flex items-center justify-between">
              <p class="font-semibold text-slate-800">
                诊断明细
                <span class="ml-2 text-xs font-normal text-slate-400">主要诊断必须且只能 1 条</span>
              </p>
              <el-button size="small" type="primary" :icon="Plus" @click="addDiagnosis">添加诊断</el-button>
            </div>
            <el-table :data="summaryForm.diagnoses" size="small" border>
              <el-table-column label="类型" width="120">
                <template #default="{ row }">
                  <el-select v-model="row.diagType" size="small" @change="onDiagTypeChange(row)">
                    <el-option label="主要诊断" :value="1"/>
                    <el-option label="其他诊断" :value="2"/>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="ICD-10" width="130">
                <template #default="{ row }">
                  <el-input v-model="row.icdCode" size="small" placeholder="J18.9"/>
                </template>
              </el-table-column>
              <el-table-column label="诊断名称" width="150">
                <template #default="{ row }">
                  <el-input v-model="row.icdName" size="small" placeholder="肺炎"/>
                </template>
              </el-table-column>
              <el-table-column label="入院病情" width="130">
                <template #default="{ row }">
                  <el-select v-model="row.admitCondition" size="small">
                    <el-option v-for="(label, code) in admitConditionMap" :key="code" :label="label"
                               :value="Number(code)"/>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="CC/MCC" width="105">
                <template #default="{ row }">
                  <el-select v-model="row.ccLevel" size="small">
                    <el-option label="NONE" value="NONE"/>
                    <el-option label="CC" value="CC"/>
                    <el-option label="MCC" value="MCC"/>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="诊断依据（病历中的支持描述）" min-width="220">
                <template #default="{ row }">
                  <el-input v-model="row.diagnosisBasis" size="small" placeholder="如 胸片示右下肺片状影，血象升高"/>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="70" align="center">
                <template #default="{ $index }">
                  <el-button link type="danger" size="small" :icon="Delete" @click="removeDiagnosis($index)"/>
                </template>
              </el-table-column>
            </el-table>
            <p v-if="!summaryForm.diagnoses.length" class="py-3 text-center text-xs text-slate-400">
              暂无诊断。没有主要诊断 DRG 无法分组，结算清单必填项也过不了。
            </p>
            <p class="mt-2 text-xs text-slate-400">
              「诊断依据」是四核对里「病历 vs 编码」的那条边：编码写 J18.9，病历里就得有能支撑肺炎的描述。
            </p>
          </div>

          <!-- 手术操作明细 -->
          <div class="rounded-lg border border-slate-200 p-4">
            <div class="mb-2 flex items-center justify-between">
              <p class="font-semibold text-slate-800">
                手术操作明细
                <span class="ml-2 text-xs font-normal text-slate-400">主要手术最多 1 条</span>
              </p>
              <el-button size="small" type="primary" :icon="Plus" @click="addOperation">添加手术</el-button>
            </div>
            <el-table :data="summaryForm.operations" size="small" border>
              <el-table-column label="主要" width="70" align="center">
                <template #default="{ row }">
                  <el-checkbox :model-value="row.isMain === 1"
                               @change="(v: any) => { row.isMain = v ? 1 : 0; onMainOpChange(row) }"/>
                </template>
              </el-table-column>
              <el-table-column label="ICD-9-CM-3" width="130">
                <template #default="{ row }">
                  <el-input v-model="row.operationCode" size="small" placeholder="33.2400"/>
                </template>
              </el-table-column>
              <el-table-column label="手术操作名称" width="150">
                <template #default="{ row }">
                  <el-input v-model="row.operationName" size="small" placeholder="支气管镜检查"/>
                </template>
              </el-table-column>
              <el-table-column label="手术日期" width="190">
                <template #default="{ row }">
                  <el-date-picker v-model="row.operationDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"
                                  size="small" class="!w-full"/>
                </template>
              </el-table-column>
              <el-table-column label="级别" width="95">
                <template #default="{ row }">
                  <el-select v-model="row.operationLevel" size="small">
                    <el-option v-for="(label, code) in operationLevelMap" :key="code" :label="label"
                               :value="Number(code)"/>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="切口" width="95">
                <template #default="{ row }">
                  <el-select v-model="row.incisionLevel" size="small">
                    <el-option v-for="(label, code) in incisionLevelMap" :key="code" :label="label"
                               :value="Number(code)"/>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="麻醉" width="105">
                <template #default="{ row }">
                  <el-select v-model="row.anesthesiaType" size="small">
                    <el-option v-for="(label, code) in anesthesiaMap" :key="code" :label="label" :value="Number(code)"/>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="主刀" width="110">
                <template #default="{ row }">
                  <el-input v-model="row.surgeonName" size="small"/>
                </template>
              </el-table-column>
              <el-table-column label="手术依据" min-width="180">
                <template #default="{ row }">
                  <el-input v-model="row.operationBasis" size="small" placeholder="如 镜下见右下叶支气管黏膜充血"/>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="70" align="center">
                <template #default="{ $index }">
                  <el-button link type="danger" size="small" :icon="Delete" @click="removeOperation($index)"/>
                </template>
              </el-table-column>
            </el-table>
            <p v-if="!summaryForm.operations.length" class="py-3 text-center text-xs text-slate-400">
              无手术操作。术后不填手术记录，等于把本该拿的手术费留在桌上。
            </p>
          </div>

          <!-- 费用信息 -->
          <div class="rounded-lg border border-slate-200 p-4">
            <p class="mb-2 font-semibold text-slate-800">费用信息</p>
            <div class="grid grid-cols-4 gap-x-4">
              <el-form-item label="总费用" label-width="80px">
                <el-input-number v-model="summaryForm.totalAmount" :min="0" :precision="2" :controls="false"
                                 class="!w-full"/>
              </el-form-item>
              <el-form-item label="西药费" label-width="80px">
                <el-input-number v-model="summaryForm.westernDrugAmount" :min="0" :precision="2" :controls="false"
                                 class="!w-full"/>
              </el-form-item>
              <el-form-item label="中成药费" label-width="80px">
                <el-input-number v-model="summaryForm.chineseDrugAmount" :min="0" :precision="2" :controls="false"
                                 class="!w-full"/>
              </el-form-item>
              <el-form-item label="中药饮片" label-width="80px">
                <el-input-number v-model="summaryForm.herbalAmount" :min="0" :precision="2" :controls="false"
                                 class="!w-full"/>
              </el-form-item>
              <el-form-item label="检查费" label-width="80px">
                <el-input-number v-model="summaryForm.examAmount" :min="0" :precision="2" :controls="false"
                                 class="!w-full"/>
              </el-form-item>
              <el-form-item label="检验费" label-width="80px">
                <el-input-number v-model="summaryForm.labAmount" :min="0" :precision="2" :controls="false"
                                 class="!w-full"/>
              </el-form-item>
              <el-form-item label="治疗费" label-width="80px">
                <el-input-number v-model="summaryForm.treatmentAmount" :min="0" :precision="2" :controls="false"
                                 class="!w-full"/>
              </el-form-item>
              <el-form-item label="手术费" label-width="80px">
                <el-input-number v-model="summaryForm.operationAmount" :min="0" :precision="2" :controls="false"
                                 class="!w-full"/>
              </el-form-item>
              <el-form-item label="耗材费" label-width="80px">
                <el-input-number v-model="summaryForm.materialAmount" :min="0" :precision="2" :controls="false"
                                 class="!w-full"/>
              </el-form-item>
              <el-form-item label="床位费" label-width="80px">
                <el-input-number v-model="summaryForm.bedAmount" :min="0" :precision="2" :controls="false"
                                 class="!w-full"/>
              </el-form-item>
              <el-form-item label="护理费" label-width="80px">
                <el-input-number v-model="summaryForm.nursingAmount" :min="0" :precision="2" :controls="false"
                                 class="!w-full"/>
              </el-form-item>
              <el-form-item label="其他费用" label-width="80px">
                <el-input-number v-model="summaryForm.otherAmount" :min="0" :precision="2" :controls="false"
                                 class="!w-full"/>
              </el-form-item>
            </div>
            <el-form-item label="备注" label-width="80px">
              <el-input v-model="summaryForm.remark"/>
            </el-form-item>
          </div>
        </fieldset>
      </div>
      <template #footer>
        <el-button @click="summaryVisible = false">关闭</el-button>
        <el-button v-if="summaryEditable" v-perm="'ipd:inpatient:edit'" type="primary" :loading="summaryLoading"
                   @click="submitSummary">保存病案首页
        </el-button>
        <el-button v-else disabled>已归档，不可修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>
