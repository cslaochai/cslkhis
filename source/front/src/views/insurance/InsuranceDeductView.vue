<script setup lang="ts">
import {ref, reactive, computed, onMounted} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Search, Refresh, Plus, View, Edit, Delete, Money} from '@element-plus/icons-vue'
import {PAGE_SIZES, DEFAULT_PAGE_SIZE} from '@/lib/pagination'
import {DICT_TYPE, loadDictDataMap} from '@/lib/dict-cache'
import {dictLabelText} from '@/lib/utils'
import {getDepartmentSelectList} from '@/api/system'
import PatientSelect from '@/components/his/PatientSelect.vue'
import {
  getYbInspectionList, getYbInspectionSelectList, upsertYbInspection, concludeYbInspection, cancelYbInspection,
  getDeductList, getDeductDetail, getDeductSummary, upsertDeduct,
  appealDeduct, appealResultDeduct, confirmDeduct, paybackDeduct, cancelDeduct
} from '@/api/insuranceDeduct'

// 状态码只用来判定按钮显隐与标签配色，文字一律从字典翻译（字典权威在 sql/163）
const ST_PENDING = 1, ST_APPEALING = 2, ST_WAIT_PAY = 4, ST_PAID = 5
const INSPECT_RUNNING = 1

const DEDUCT_DICT_TYPES = [
  DICT_TYPE.YB_INSPECT_TYPE, DICT_TYPE.YB_INSPECT_STATUS, DICT_TYPE.YB_DEDUCT_SOURCE,
  DICT_TYPE.YB_VIOLATION_TYPE, DICT_TYPE.YB_DEDUCT_STATUS, DICT_TYPE.YB_APPEAL_RESULT,
  DICT_TYPE.YB_LOSS_BEAR, DICT_TYPE.YB_DEDUCT_ACTION
]
const dicts = ref<Record<string, any[]>>({})
const dictOptions = (type: string) => dicts.value[type] || []
const dictText = (type: string, value: any) => dictLabelText(dicts.value[type], value)

const activeTab = ref('notice')

// ==================== 科室下拉（跨科室选责任科室，要全院范围） ====================
const deptOptions = ref<any[]>([])
const deptNameOf = (id: any) => deptOptions.value.find((d) => String(d.id) === String(id))?.deptName || ''

// ==================== 汇总（后端 SQL 聚合，禁止前端自算） ====================
const summary = ref<any>({})
const loadSummary = async () => {
  try {
    const res: any = await getDeductSummary()
    summary.value = res.data || {}
  } catch (error: any) {
    ElMessage.error(error?.message || '扣款汇总加载失败')
  }
}

// ==================== 扣款通知列表 ====================
const noticeLoading = ref(false)
const noticeRows = ref<any[]>([])
const noticeQuery = reactive({
  deductStatus: undefined as number | undefined, sourceType: undefined as number | undefined,
  violationType: undefined as number | undefined, inspectionId: undefined as string | undefined,
  onlyOverdue: false, keyword: ''
})
const noticePage = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})

const loadNotices = async () => {
  noticeLoading.value = true
  try {
    const res: any = await getDeductList({
      ...noticeQuery, onlyOverdue: noticeQuery.onlyOverdue || undefined,
      pageNum: noticePage.pageNum, pageSize: noticePage.pageSize
    })
    noticeRows.value = res.data?.records || []
    noticePage.total = res.data?.total || 0
  } catch (error: any) {
    ElMessage.error(error?.message || '扣款通知列表加载失败')
  } finally {
    noticeLoading.value = false
  }
}

const resetNoticeQuery = () => {
  noticeQuery.deductStatus = undefined
  noticeQuery.sourceType = undefined
  noticeQuery.violationType = undefined
  noticeQuery.inspectionId = undefined
  noticeQuery.onlyOverdue = false
  noticeQuery.keyword = ''
  noticePage.pageNum = 1
  loadNotices()
}

const money = (v: any) => `¥${Number(v ?? 0).toFixed(2)}`
const statusTagType = (s: number) =>
  s === ST_PENDING ? 'warning' : s === ST_APPEALING ? 'primary' : s === 3 ? 'success'
    : s === ST_WAIT_PAY ? 'danger' : s === ST_PAID ? 'success' : 'info'

// 动作显隐与后端状态机逐条对齐，后端仍是唯一裁判
const canEditNotice = (row: any) => row.deductStatus === ST_PENDING
const canAppeal = (row: any) => row.deductStatus === ST_PENDING
const canAppealResult = (row: any) => row.deductStatus === ST_APPEALING
const canConfirm = (row: any) => row.deductStatus === ST_PENDING || row.deductStatus === ST_WAIT_PAY
// 状态 4 有两个来路：申诉驳回（还没定责）和已确认追责。后端要求先定责才能缴回，
// 所以只有带 confirmBy 的待缴单才露「缴回」，否则点进去必然被拒。
const canPayback = (row: any) => row.deductStatus === ST_WAIT_PAY && !!row.confirmBy
const canCancelNotice = (row: any) => row.deductStatus === ST_PENDING

// ==================== 飞检批次候选 ====================
const inspectionOptions = ref<any[]>([])
const loadInspectionOptions = async () => {
  try {
    const res: any = await getYbInspectionSelectList()
    inspectionOptions.value = res.data || []
  } catch (error: any) {
    ElMessage.error(error?.message || '飞检批次候选加载失败')
  }
}

// ==================== 通知单 新建/编辑 ====================
const noticeFormVisible = ref(false)
const noticeSubmitting = ref(false)
const emptyNotice = () => ({
  id: null as any, deductNo: '', sourceType: 1, inspectionId: undefined as any, patientId: undefined as any,
  patientName: '', patientNo: '', encounterType: undefined as number | undefined,
  deptId: undefined as any, deptName: '', doctorName: '', violationType: undefined as any,
  violationDesc: '', deductAmount: undefined as any, noticeDate: '', handleDeadline: '', remark: ''
})
const noticeForm = ref<any>(emptyNotice())

const openNoticeCreate = async () => {
  noticeForm.value = emptyNotice()
  await loadInspectionOptions()
  noticeFormVisible.value = true
}

const openNoticeEdit = async (row: any) => {
  try {
    const res: any = await getDeductDetail(row.id)
    // 日期字段后端出参就是 yyyy-MM-dd 文本，直接喂给 el-date-picker 的 value-format
    noticeForm.value = {...emptyNotice(), ...(res.data || {}), id: row.id,
      inspectionId: res.data?.inspectionId ? String(res.data.inspectionId) : undefined,
      deptId: res.data?.deptId ? String(res.data.deptId) : undefined,
      patientId: res.data?.patientId ? String(res.data.patientId) : undefined}
    await loadInspectionOptions()
    noticeFormVisible.value = true
  } catch (error: any) {
    ElMessage.error(error?.message || '扣款单详情加载失败')
  }
}

const onNoticePatient = (p: any) => {
  if (!p) return
  noticeForm.value.patientName = p.patientName
  noticeForm.value.patientNo = p.patientNo
}

const onNoticeDept = (id: any) => {
  noticeForm.value.deptName = id ? deptNameOf(id) : ''
}

const saveNotice = async () => {
  const f = noticeForm.value
  if (!f.sourceType || !f.violationType || !f.deductAmount || !f.noticeDate || !f.handleDeadline || !f.violationDesc) {
    ElMessage.warning('来源、违规类型、扣款金额、通知日期、处理期限、违规描述都要填')
    return
  }
  noticeSubmitting.value = true
  try {
    await upsertDeduct({
      id: f.id || undefined, sourceType: f.sourceType, inspectionId: f.inspectionId || undefined,
      patientId: f.patientId || undefined, patientName: f.patientName || undefined, patientNo: f.patientNo || undefined,
      encounterType: f.encounterType || undefined, deptId: f.deptId || undefined, deptName: f.deptName || undefined,
      doctorName: f.doctorName || undefined, violationType: f.violationType, violationDesc: f.violationDesc,
      deductAmount: f.deductAmount, noticeDate: f.noticeDate, handleDeadline: f.handleDeadline,
      remark: f.remark || undefined
    })
    ElMessage.success(f.id ? '扣款通知已更新' : '扣款通知已录入')
    noticeFormVisible.value = false
    await Promise.all([loadNotices(), loadSummary()])
  } catch (error: any) {
    ElMessage.error(error?.message || '保存失败')
  } finally {
    noticeSubmitting.value = false
  }
}

// ==================== 详情（只读 + 留痕时间线） ====================
const noticeDetailVisible = ref(false)
const noticeDetail = ref<any>(null)

const openNoticeDetail = async (row: any) => {
  try {
    const res: any = await getDeductDetail(row.id)
    noticeDetail.value = res.data || null
    noticeDetailVisible.value = true
  } catch (error: any) {
    ElMessage.error(error?.message || '扣款单详情加载失败')
  }
}

// ==================== 申诉 ====================
const appealVisible = ref(false)
const appealSubmitting = ref(false)
const appealForm = reactive({id: null as any, deductNo: '', appealReason: '', appealMaterial: ''})

const openAppeal = (row: any) => {
  Object.assign(appealForm, {id: row.id, deductNo: row.deductNo, appealReason: '', appealMaterial: ''})
  appealVisible.value = true
}

const submitAppeal = async () => {
  if (!appealForm.appealReason) {
    ElMessage.warning('申诉理由必填，医保局看的就是这一句')
    return
  }
  appealSubmitting.value = true
  try {
    await appealDeduct({id: appealForm.id, appealReason: appealForm.appealReason, appealMaterial: appealForm.appealMaterial || undefined})
    ElMessage.success('已提交申诉，状态转为申诉中')
    appealVisible.value = false
    await Promise.all([loadNotices(), loadSummary()])
  } catch (error: any) {
    ElMessage.error(error?.message || '申诉提交失败')
  } finally {
    appealSubmitting.value = false
  }
}

// ==================== 申诉结果 ====================
const resultVisible = ref(false)
const resultSubmitting = ref(false)
const resultForm = reactive({id: null as any, deductNo: '', appealResult: 1, appealResultRemark: ''})

const openAppealResult = (row: any) => {
  Object.assign(resultForm, {id: row.id, deductNo: row.deductNo, appealResult: 1, appealResultRemark: ''})
  resultVisible.value = true
}

const submitAppealResult = async () => {
  resultSubmitting.value = true
  try {
    await appealResultDeduct({id: resultForm.id, appealResult: resultForm.appealResult, appealResultRemark: resultForm.appealResultRemark || undefined})
    ElMessage.success(resultForm.appealResult === 1 ? '已记申诉成功，扣款撤销' : '已记申诉驳回，转入待缴')
    resultVisible.value = false
    await Promise.all([loadNotices(), loadSummary()])
  } catch (error: any) {
    ElMessage.error(error?.message || '申诉结果录入失败')
  } finally {
    resultSubmitting.value = false
  }
}

// ==================== 确认扣款并追责 ====================
const confirmVisible = ref(false)
const confirmSubmitting = ref(false)
const confirmForm = reactive({
  id: null as any, deductNo: '', deductAmount: 0, liableDeptId: undefined as any,
  liableDeptName: '', liableEmpName: '', lossBearType: 2, bearDeptAmount: undefined as any, bearEmpAmount: undefined as any
})

const openConfirm = (row: any) => {
  Object.assign(confirmForm, {
    id: row.id, deductNo: row.deductNo, deductAmount: Number(row.deductAmount || 0),
    liableDeptId: row.deptId ? String(row.deptId) : undefined, liableDeptName: row.deptName || '',
    liableEmpName: row.doctorName || '', lossBearType: row.lossBearType || 2,
    bearDeptAmount: row.bearDeptAmount ?? undefined, bearEmpAmount: row.bearEmpAmount ?? undefined
  })
  if (!confirmForm.liableDeptId) {
    confirmForm.liableDeptName = ''
  }
  confirmVisible.value = true
}

const onConfirmDept = (id: any) => {
  confirmForm.liableDeptName = id ? deptNameOf(id) : ''
}

const bearSum = computed(() => Number(confirmForm.bearDeptAmount || 0) + Number(confirmForm.bearEmpAmount || 0))

const submitConfirm = async () => {
  if (!confirmForm.liableDeptId) {
    ElMessage.warning('责任科室必选，追责追不到科室等于没追')
    return
  }
  if (confirmForm.lossBearType === 4 && bearSum.value > confirmForm.deductAmount) {
    ElMessage.warning(`分摊合计 ${money(bearSum.value)} 已超过扣款 ${money(confirmForm.deductAmount)}，分摊不能超出实扣`)
    return
  }
  confirmSubmitting.value = true
  try {
    await confirmDeduct({
      id: confirmForm.id, liableDeptId: confirmForm.liableDeptId, liableDeptName: confirmForm.liableDeptName || undefined,
      liableEmpName: confirmForm.liableEmpName || undefined, lossBearType: confirmForm.lossBearType,
      bearDeptAmount: confirmForm.bearDeptAmount ?? undefined, bearEmpAmount: confirmForm.bearEmpAmount ?? undefined
    })
    ElMessage.success('已确认扣款并追责，转入待缴')
    confirmVisible.value = false
    await Promise.all([loadNotices(), loadSummary()])
  } catch (error: any) {
    ElMessage.error(error?.message || '确认追责失败')
  } finally {
    confirmSubmitting.value = false
  }
}

// ==================== 缴回 ====================
const paybackVisible = ref(false)
const paybackSubmitting = ref(false)
const paybackForm = reactive({id: null as any, deductNo: '', deductAmount: 0, paidAmount: undefined as any, paybackDate: '', paybackVoucher: ''})

const openPayback = (row: any) => {
  Object.assign(paybackForm, {
    id: row.id, deductNo: row.deductNo, deductAmount: Number(row.deductAmount || 0),
    paidAmount: Number(row.deductAmount || 0), paybackDate: '', paybackVoucher: ''
  })
  paybackVisible.value = true
}

const submitPayback = async () => {
  if (!paybackForm.paidAmount || !paybackForm.paybackDate || !paybackForm.paybackVoucher) {
    ElMessage.warning('缴回金额、缴回日期、凭证号都要填')
    return
  }
  paybackSubmitting.value = true
  try {
    await paybackDeduct({
      id: paybackForm.id, paidAmount: paybackForm.paidAmount,
      paybackDate: paybackForm.paybackDate, paybackVoucher: paybackForm.paybackVoucher
    })
    ElMessage.success('已录入缴回，这张扣款单闭环')
    paybackVisible.value = false
    await Promise.all([loadNotices(), loadSummary()])
  } catch (error: any) {
    // 缴回金额与实扣不符、未确认追责先缴回这类情况由后端判定，拒的原因必须原样回给用户，
    // 否则点「确定」后弹窗静止、用户以为页面坏了
    ElMessage.error(error?.message || '缴回登记失败')
  } finally {
    paybackSubmitting.value = false
  }
}

const cancelDeductRow = (row: any) => {
  ElMessageBox.prompt(`作废扣款单「${row.deductNo}」？仅误录/重复录入才作废，作废原因会写进留痕。`, '作废确认', {
    inputPlaceholder: '作废原因（必填）',
    inputValidator: (v: string) => (v && v.trim() ? true : '作废原因不能为空'),
    type: 'warning'
  }).then(async ({value}) => {
    try {
      await cancelDeduct({id: row.id, reason: value.trim()})
      ElMessage.success('已作废')
      await Promise.all([loadNotices(), loadSummary()])
    } catch (error: any) {
      ElMessage.error(error?.message || '作废失败')
    }
  }).catch(() => {})
}

// ==================== 飞检批次 ====================
const inspectLoading = ref(false)
const inspectRows = ref<any[]>([])
const inspectQuery = reactive({inspectType: undefined as number | undefined, status: undefined as number | undefined, keyword: ''})
const inspectPage = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})

const loadInspections = async () => {
  inspectLoading.value = true
  try {
    const res: any = await getYbInspectionList({...inspectQuery, pageNum: inspectPage.pageNum, pageSize: inspectPage.pageSize})
    inspectRows.value = res.data?.records || []
    inspectPage.total = res.data?.total || 0
  } catch (error: any) {
    ElMessage.error(error?.message || '飞检批次列表加载失败')
  } finally {
    inspectLoading.value = false
  }
}

const resetInspectQuery = () => {
  inspectQuery.inspectType = undefined
  inspectQuery.status = undefined
  inspectQuery.keyword = ''
  inspectPage.pageNum = 1
  loadInspections()
}

const inspectFormVisible = ref(false)
const inspectSubmitting = ref(false)
const emptyInspect = () => ({
  id: null as any, inspectNo: '', inspectType: 1, fundOrg: '', inspectStartDate: '', inspectEndDate: '',
  inspectDate: '', inspectTeam: '', ourReceiver: '', remark: ''
})
const inspectForm = ref<any>(emptyInspect())

const openInspectCreate = () => {
  inspectForm.value = emptyInspect()
  inspectFormVisible.value = true
}

const openInspectEdit = (row: any) => {
  inspectForm.value = {...emptyInspect(), ...row, id: row.id}
  inspectFormVisible.value = true
}

const saveInspect = async () => {
  const f = inspectForm.value
  if (!f.fundOrg || !f.inspectStartDate || !f.inspectEndDate || !f.inspectDate) {
    ElMessage.warning('统筹区、审核期间、进驻日期都要填')
    return
  }
  inspectSubmitting.value = true
  try {
    await upsertYbInspection({
      id: f.id || undefined, inspectType: f.inspectType, fundOrg: f.fundOrg,
      inspectStartDate: f.inspectStartDate, inspectEndDate: f.inspectEndDate, inspectDate: f.inspectDate,
      inspectTeam: f.inspectTeam || undefined, ourReceiver: f.ourReceiver || undefined, remark: f.remark || undefined
    })
    ElMessage.success(f.id ? '批次已更新' : '批次已新建')
    inspectFormVisible.value = false
    await Promise.all([loadInspections(), loadInspectionOptions()])
  } catch (error: any) {
    ElMessage.error(error?.message || '保存失败')
  } finally {
    inspectSubmitting.value = false
  }
}

const concludeVisible = ref(false)
const concludeSubmitting = ref(false)
const concludeForm = reactive({id: null as any, inspectNo: '', conclusion: ''})

const openConclude = (row: any) => {
  Object.assign(concludeForm, {id: row.id, inspectNo: row.inspectNo, conclusion: ''})
  concludeVisible.value = true
}

const submitConclude = async () => {
  if (!concludeForm.conclusion) {
    ElMessage.warning('结项结论必填，飞检收尾要留下结论')
    return
  }
  concludeSubmitting.value = true
  try {
    await concludeYbInspection({id: concludeForm.id, conclusion: concludeForm.conclusion})
    ElMessage.success('批次已结项')
    concludeVisible.value = false
    await Promise.all([loadInspections(), loadInspectionOptions()])
  } catch (error: any) {
    ElMessage.error(error?.message || '结项失败')
  } finally {
    concludeSubmitting.value = false
  }
}

const cancelInspectRow = (row: any) => {
  ElMessageBox.prompt(`作废批次「${row.inspectNo}」？名下有扣款通知时不能作废。`, '作废确认', {
    inputPlaceholder: '作废原因（必填）',
    inputValidator: (v: string) => (v && v.trim() ? true : '作废原因不能为空'),
    type: 'warning'
  }).then(async ({value}) => {
    try {
      await cancelYbInspection({id: row.id, reason: value.trim()})
      ElMessage.success('已作废')
      await Promise.all([loadInspections(), loadInspectionOptions()])
    } catch (error: any) {
      ElMessage.error(error?.message || '批次作废失败')
    }
  }).catch(() => {})
}

// 从批次下钻到它名下的扣款单
const drillNotices = (row: any) => {
  noticeQuery.inspectionId = String(row.id)
  noticeQuery.onlyOverdue = false
  noticePage.pageNum = 1
  activeTab.value = 'notice'
  loadNotices()
}

const inspectTagType = (s: number) => (s === 1 ? 'primary' : s === 2 ? 'success' : 'info')

onMounted(async () => {
  dicts.value = await loadDictDataMap(DEDUCT_DICT_TYPES.join(','))
  await Promise.all([loadDepts(), loadNotices(), loadSummary(), loadInspections(), loadInspectionOptions()])
})

const loadDepts = async () => {
  try {
    const res: any = await getDepartmentSelectList({scope: 'ALL'})
    deptOptions.value = res.data || []
  } catch (error: any) {
    ElMessage.error(error?.message || '科室候选加载失败')
  }
}
</script>

<template>
  <div class="p-4 space-y-4" data-testid="yb-deduct-page">
    <!-- 台账汇总：数字全部来自后端 SQL 聚合 -->
    <el-row :gutter="12">
      <el-col :span="4">
        <el-card shadow="never" class="!rounded-lg" data-testid="deduct-sum-pending">
          <div class="text-xs text-slate-500">待确认</div>
          <div class="text-2xl font-semibold mt-1">{{ summary.pendingConfirmCount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="never" class="!rounded-lg" data-testid="deduct-sum-appealing">
          <div class="text-xs text-slate-500">申诉中</div>
          <div class="text-2xl font-semibold mt-1">{{ summary.appealingCount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="never" class="!rounded-lg" data-testid="deduct-sum-waitpay">
          <div class="text-xs text-slate-500">维持扣款待缴</div>
          <div class="text-2xl font-semibold mt-1">{{ summary.waitPayCount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="never" class="!rounded-lg" data-testid="deduct-sum-paid">
          <div class="text-xs text-slate-500">已缴回</div>
          <div class="text-2xl font-semibold mt-1">{{ summary.paidCount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="never" class="!rounded-lg" data-testid="deduct-sum-overdue">
          <div class="text-xs text-slate-500">超期未结</div>
          <div class="text-2xl font-semibold mt-1" :class="(summary.overdueCount ?? 0) > 0 ? 'text-red-600' : ''">
            {{ summary.overdueCount ?? 0 }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="never" class="!rounded-lg" data-testid="deduct-sum-amount">
          <div class="text-xs text-slate-500">未结案金额</div>
          <div class="text-2xl font-semibold mt-1 text-[#1269B5]">{{ money(summary.openAmountSum) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="!rounded-lg">
      <el-tabs v-model="activeTab" data-testid="deduct-tabs">
        <!-- ==================== 扣款通知 ==================== -->
        <el-tab-pane label="扣款通知台账" name="notice">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-select v-model="noticeQuery.deductStatus" placeholder="全部状态" clearable style="width: 150px" data-testid="deduct-filter-status">
              <el-option v-for="d in dictOptions(DICT_TYPE.YB_DEDUCT_STATUS)" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
            <el-select v-model="noticeQuery.sourceType" placeholder="全部来源" clearable style="width: 160px">
              <el-option v-for="d in dictOptions(DICT_TYPE.YB_DEDUCT_SOURCE)" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
            <el-select v-model="noticeQuery.violationType" placeholder="全部违规类型" clearable style="width: 170px">
              <el-option v-for="d in dictOptions(DICT_TYPE.YB_VIOLATION_TYPE)" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
            <el-select v-model="noticeQuery.inspectionId" placeholder="不限飞检批次" clearable style="width: 190px" data-testid="deduct-filter-inspection">
              <el-option v-for="i in inspectionOptions" :key="i.id" :label="`${i.inspectNo} ${i.fundOrg}`" :value="String(i.id)"/>
            </el-select>
            <el-checkbox v-model="noticeQuery.onlyOverdue" data-testid="deduct-only-overdue">只看超期</el-checkbox>
            <el-input v-model="noticeQuery.keyword" placeholder="单号/患者/科室/违规描述" clearable style="width: 220px" data-testid="deduct-keyword"
                      @keyup.enter="noticePage.pageNum = 1; loadNotices()"/>
            <el-button :icon="Search" type="primary" data-testid="deduct-search-btn" @click="noticePage.pageNum = 1; loadNotices()">查询</el-button>
            <el-button :icon="Refresh" @click="resetNoticeQuery">重置</el-button>
            <div class="flex-1"></div>
            <el-button type="primary" :icon="Plus" v-perm="'finance:insuranceDeduct:add'" data-testid="deduct-create-btn" @click="openNoticeCreate">录入扣款通知</el-button>
          </div>

          <el-table v-loading="noticeLoading" :data="noticeRows" style="width: 100%" data-testid="deduct-table"
                    size="default" @row-click="openNoticeDetail">
            <el-table-column prop="deductNo" label="扣款单号" width="150" class-name="font-mono text-xs"/>
            <el-table-column label="来源" width="140" show-overflow-tooltip>
              <template #default="{row}">{{ dictText(DICT_TYPE.YB_DEDUCT_SOURCE, row.sourceType) }}</template>
            </el-table-column>
            <el-table-column prop="patientName" label="患者" width="90">
              <template #default="{row}">{{ row.patientName || '—' }}</template>
            </el-table-column>
            <el-table-column prop="deptName" label="被审科室" width="110" show-overflow-tooltip>
              <template #default="{row}">{{ row.deptName || '—' }}</template>
            </el-table-column>
            <el-table-column label="违规类型" width="140" show-overflow-tooltip>
              <template #default="{row}">{{ dictText(DICT_TYPE.YB_VIOLATION_TYPE, row.violationType) }}</template>
            </el-table-column>
            <el-table-column prop="violationDesc" label="违规事实" min-width="220" show-overflow-tooltip/>
            <el-table-column label="扣款金额" width="110" align="right">
              <template #default="{row}">{{ money(row.deductAmount) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{row}">
                <el-tag :type="statusTagType(row.deductStatus)" size="small" :data-testid="`deduct-status-${row.deductNo}`">
                  {{ dictText(DICT_TYPE.YB_DEDUCT_STATUS, row.deductStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="处理期限" width="150">
              <template #default="{row}">
                <div class="text-xs">{{ row.handleDeadline }}</div>
                <el-tag v-if="row.overdue" type="danger" size="small" data-testid="deduct-overdue-tag">超期 {{ Math.abs(row.deadlineDays) }} 天</el-tag>
                <span v-else-if="row.deadlineDays != null" class="text-xs text-slate-400">剩 {{ row.deadlineDays }} 天</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="{row}">
                <el-button v-if="canAppeal(row)" size="small" type="primary" plain v-perm="'finance:insuranceDeduct:edit'"
                           :data-testid="`deduct-appeal-${row.deductNo}`" @click.stop="openAppeal(row)">申诉</el-button>
                <el-button v-if="canAppealResult(row)" size="small" type="primary" plain v-perm="'finance:insuranceDeduct:edit'"
                           :data-testid="`deduct-result-${row.deductNo}`" @click.stop="openAppealResult(row)">录结果</el-button>
                <el-button v-if="canConfirm(row)" size="small" type="warning" plain v-perm="'finance:insuranceDeduct:edit'"
                           :data-testid="`deduct-confirm-${row.deductNo}`" @click.stop="openConfirm(row)">确认追责</el-button>
                <el-button v-if="canPayback(row)" size="small" type="success" plain :icon="Money" v-perm="'finance:insuranceDeduct:edit'"
                           :data-testid="`deduct-payback-${row.deductNo}`" @click.stop="openPayback(row)">缴回</el-button>
                <el-button v-if="canEditNotice(row)" size="small" plain :icon="Edit" v-perm="'finance:insuranceDeduct:add'"
                           :data-testid="`deduct-edit-${row.deductNo}`" @click.stop="openNoticeEdit(row)">编辑</el-button>
                <el-button v-if="canCancelNotice(row)" size="small" type="danger" plain :icon="Delete" v-perm="'finance:insuranceDeduct:edit'"
                           :data-testid="`deduct-cancel-${row.deductNo}`" @click.stop="cancelDeductRow(row)">作废</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="flex justify-end mt-3">
            <el-pagination v-model:current-page="noticePage.pageNum" v-model:page-size="noticePage.pageSize"
                           :total="noticePage.total" :page-sizes="PAGE_SIZES" layout="total, sizes, prev, pager, next, jumper"
                           data-testid="deduct-pagination" @current-change="loadNotices" @size-change="noticePage.pageNum = 1; loadNotices()"/>
          </div>
        </el-tab-pane>

        <!-- ==================== 飞检批次 ==================== -->
        <el-tab-pane label="飞检/专项审核批次" name="inspection">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-select v-model="inspectQuery.inspectType" placeholder="全部检查类型" clearable style="width: 170px">
              <el-option v-for="d in dictOptions(DICT_TYPE.YB_INSPECT_TYPE)" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
            <el-select v-model="inspectQuery.status" placeholder="全部状态" clearable style="width: 140px">
              <el-option v-for="d in dictOptions(DICT_TYPE.YB_INSPECT_STATUS)" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
            <el-input v-model="inspectQuery.keyword" placeholder="批次号/医保局/检查组" clearable style="width: 220px" data-testid="inspect-keyword"
                      @keyup.enter="inspectPage.pageNum = 1; loadInspections()"/>
            <el-button :icon="Search" type="primary" data-testid="inspect-search-btn" @click="inspectPage.pageNum = 1; loadInspections()">查询</el-button>
            <el-button :icon="Refresh" @click="resetInspectQuery">重置</el-button>
            <div class="flex-1"></div>
            <el-button type="primary" :icon="Plus" v-perm="'finance:insuranceDeduct:add'" data-testid="inspect-create-btn" @click="openInspectCreate">新建批次</el-button>
          </div>

          <el-table v-loading="inspectLoading" :data="inspectRows" style="width: 100%" data-testid="inspect-table" size="default">
            <el-table-column prop="inspectNo" label="批次号" width="150" class-name="font-mono text-xs"/>
            <el-table-column label="检查类型" width="130">
              <template #default="{row}">{{ dictText(DICT_TYPE.YB_INSPECT_TYPE, row.inspectType) }}</template>
            </el-table-column>
            <el-table-column prop="fundOrg" label="统筹区/医保局" min-width="150" show-overflow-tooltip/>
            <el-table-column label="审核期间" width="200">
              <template #default="{row}">{{ row.inspectStartDate }} ~ {{ row.inspectEndDate }}</template>
            </el-table-column>
            <el-table-column prop="inspectDate" label="进驻/通知日期" width="120"/>
            <el-table-column prop="inspectTeam" label="检查组" min-width="150" show-overflow-tooltip>
              <template #default="{row}">{{ row.inspectTeam || '—' }}</template>
            </el-table-column>
            <el-table-column prop="ourReceiver" label="院内接待" width="100">
              <template #default="{row}">{{ row.ourReceiver || '—' }}</template>
            </el-table-column>
            <el-table-column label="名下扣款" width="150" align="right">
              <template #default="{row}">
                <span class="text-xs">{{ row.deductCount }} 张 / </span>
                <span class="font-medium">{{ money(row.deductAmountSum) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{row}">
                <el-tag :type="inspectTagType(row.status)" size="small">{{ dictText(DICT_TYPE.YB_INSPECT_STATUS, row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="结论" min-width="200" show-overflow-tooltip>
              <template #default="{row}">{{ row.conclusion || '—' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="{row}">
                <el-button size="small" plain :icon="View" :data-testid="`inspect-drill-${row.inspectNo}`" @click="drillNotices(row)">看扣款单</el-button>
                <el-button v-if="row.status === INSPECT_RUNNING" size="small" type="primary" plain v-perm="'finance:insuranceDeduct:add'"
                           :data-testid="`inspect-edit-${row.inspectNo}`" @click="openInspectEdit(row)">编辑</el-button>
                <el-button v-if="row.status === INSPECT_RUNNING" size="small" type="success" plain v-perm="'finance:insuranceDeduct:edit'"
                           :data-testid="`inspect-conclude-${row.inspectNo}`" @click="openConclude(row)">结项</el-button>
                <el-button v-if="row.status === INSPECT_RUNNING" size="small" type="danger" plain :icon="Delete" v-perm="'finance:insuranceDeduct:edit'"
                           :data-testid="`inspect-cancel-${row.inspectNo}`" @click="cancelInspectRow(row)">作废</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="flex justify-end mt-3">
            <el-pagination v-model:current-page="inspectPage.pageNum" v-model:page-size="inspectPage.pageSize"
                           :total="inspectPage.total" :page-sizes="PAGE_SIZES" layout="total, sizes, prev, pager, next, jumper"
                           data-testid="inspect-pagination" @current-change="loadInspections" @size-change="inspectPage.pageNum = 1; loadInspections()"/>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- ==================== 通知单 新建/编辑 ==================== -->
    <el-dialog v-model="noticeFormVisible" :title="noticeForm.id ? `修改扣款通知 ${noticeForm.deductNo}` : '录入扣款通知'" width="820px" data-testid="deduct-form-dialog">
      <el-form :model="noticeForm" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="扣款来源" required>
              <el-select v-model="noticeForm.sourceType" style="width: 100%" data-testid="deduct-form-source">
                <el-option v-for="d in dictOptions(DICT_TYPE.YB_DEDUCT_SOURCE)" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="飞检批次">
              <el-select v-model="noticeForm.inspectionId" clearable filterable style="width: 100%"
                         :placeholder="noticeForm.sourceType === 1 ? '来源为飞检现场时必须关联' : '可不关联'"
                         data-testid="deduct-form-inspection">
                <el-option v-for="i in inspectionOptions" :key="i.id" :label="`${i.inspectNo} ${i.fundOrg}`" :value="String(i.id)"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="患者">
              <PatientSelect v-model="noticeForm.patientId" @select="onNoticePatient" placeholder="按扣款通知上的患者搜（批次性问题可留空）"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="就诊类型">
              <el-select v-model="noticeForm.encounterType" clearable style="width: 100%">
                <el-option label="门诊" :value="1"/><el-option label="住院" :value="2"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="被审科室">
              <el-select v-model="noticeForm.deptId" filterable clearable style="width: 100%" @change="onNoticeDept" data-testid="deduct-form-dept">
                <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="String(d.id)"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="责任医师">
              <el-input v-model="noticeForm.doctorName" placeholder="通知上写明的医师姓名"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="违规类型" required>
              <el-select v-model="noticeForm.violationType" style="width: 100%" data-testid="deduct-form-violation">
                <el-option v-for="d in dictOptions(DICT_TYPE.YB_VIOLATION_TYPE)" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="扣款金额" required>
              <el-input-number v-model="noticeForm.deductAmount" :min="0.01" :precision="2" :controls="false" style="width: 100%" data-testid="deduct-form-amount"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="通知日期" required data-testid="deduct-form-notice-date">
              <el-date-picker v-model="noticeForm.noticeDate" type="date" value-format="YYYY-MM-DD" style="width: 100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="处理期限" required data-testid="deduct-form-deadline">
              <el-date-picker v-model="noticeForm.handleDeadline" type="date" value-format="YYYY-MM-DD" style="width: 100%"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="违规事实描述" required>
          <el-input v-model="noticeForm.violationDesc" type="textarea" :rows="3"
                    placeholder="飞检问的就是这一句：哪天、哪个项目、怎么个违规法，照通知单原文写" data-testid="deduct-form-desc"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="noticeForm.remark" type="textarea" :rows="2"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="noticeFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="noticeSubmitting" data-testid="deduct-form-save" @click="saveNotice">保存</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 通知单详情（只读 + 留痕） ==================== -->
    <el-dialog v-model="noticeDetailVisible" :title="`扣款通知 ${noticeDetail?.deductNo || ''}`" width="880px" data-testid="deduct-detail-dialog">
      <el-form :model="noticeDetail" label-width="120px" disabled>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="状态">
            <el-tag :type="statusTagType(noticeDetail?.deductStatus)" size="small">{{ dictText(DICT_TYPE.YB_DEDUCT_STATUS, noticeDetail?.deductStatus) }}</el-tag>
            <el-tag v-if="noticeDetail?.overdue" type="danger" size="small" class="ml-2">已超期</el-tag>
          </el-form-item></el-col>
          <el-col :span="8"><el-form-item label="来源">{{ dictText(DICT_TYPE.YB_DEDUCT_SOURCE, noticeDetail?.sourceType) }}</el-form-item></el-col>
          <el-col :span="8"><el-form-item label="飞检批次">{{ noticeDetail?.inspectionNo || '—' }}</el-form-item></el-col>
          <el-col :span="8"><el-form-item label="患者">{{ noticeDetail?.patientName || '—' }}</el-form-item></el-col>
          <el-col :span="8"><el-form-item label="被审科室">{{ noticeDetail?.deptName || '—' }}</el-form-item></el-col>
          <el-col :span="8"><el-form-item label="责任医师">{{ noticeDetail?.doctorName || '—' }}</el-form-item></el-col>
          <el-col :span="8"><el-form-item label="违规类型">{{ dictText(DICT_TYPE.YB_VIOLATION_TYPE, noticeDetail?.violationType) }}</el-form-item></el-col>
          <el-col :span="8"><el-form-item label="扣款金额">{{ money(noticeDetail?.deductAmount) }}</el-form-item></el-col>
          <el-col :span="8"><el-form-item label="处理期限">{{ noticeDetail?.handleDeadline }}</el-form-item></el-col>
          <el-col :span="24"><el-form-item label="违规事实">{{ noticeDetail?.violationDesc }}</el-form-item></el-col>
          <el-col v-if="noticeDetail?.appealReason" :span="24"><el-form-item label="申诉理由">{{ noticeDetail.appealReason }}</el-form-item></el-col>
          <el-col v-if="noticeDetail?.appealMaterial" :span="24"><el-form-item label="申诉材料">{{ noticeDetail.appealMaterial }}</el-form-item></el-col>
          <el-col v-if="noticeDetail?.appealResult" :span="12"><el-form-item label="申诉结果">{{ dictText(DICT_TYPE.YB_APPEAL_RESULT, noticeDetail.appealResult) }}</el-form-item></el-col>
          <el-col v-if="noticeDetail?.liableDeptName" :span="12"><el-form-item label="责任科室">{{ noticeDetail.liableDeptName }}</el-form-item></el-col>
          <el-col v-if="noticeDetail?.liableEmpName" :span="12"><el-form-item label="责任人">{{ noticeDetail.liableEmpName }}</el-form-item></el-col>
          <el-col v-if="noticeDetail?.lossBearType" :span="12"><el-form-item label="承担方式">{{ dictText(DICT_TYPE.YB_LOSS_BEAR, noticeDetail.lossBearType) }}</el-form-item></el-col>
          <el-col v-if="noticeDetail?.bearDeptAmount != null" :span="8"><el-form-item label="科室承担">{{ money(noticeDetail.bearDeptAmount) }}</el-form-item></el-col>
          <el-col v-if="noticeDetail?.bearEmpAmount != null" :span="8"><el-form-item label="个人承担">{{ money(noticeDetail.bearEmpAmount) }}</el-form-item></el-col>
          <el-col v-if="noticeDetail?.paybackVoucher" :span="8"><el-form-item label="缴回凭证">{{ noticeDetail.paybackVoucher }}</el-form-item></el-col>
          <el-col v-if="noticeDetail?.paybackDate" :span="8"><el-form-item label="缴回日期">{{ noticeDetail.paybackDate }}</el-form-item></el-col>
          <el-col v-if="noticeDetail?.cancelReason" :span="24"><el-form-item label="作废原因">{{ noticeDetail.cancelReason }}</el-form-item></el-col>
        </el-row>
      </el-form>

      <div class="font-medium mb-2 text-sm">处理留痕</div>
      <el-timeline data-testid="deduct-log-timeline">
        <el-timeline-item v-for="log in noticeDetail?.logs || []" :key="log.id" :timestamp="log.operateTime" placement="top">
          <div class="text-sm">
            <el-tag size="small" type="info">{{ dictText(DICT_TYPE.YB_DEDUCT_ACTION, log.action) }}</el-tag>
            <span class="ml-2">{{ log.detail }}</span>
            <span v-if="log.amount != null" class="ml-2 text-slate-500">{{ money(log.amount) }}</span>
            <span class="ml-2 text-xs text-slate-400">{{ log.operator }}</span>
          </div>
        </el-timeline-item>
      </el-timeline>
      <template #footer>
        <el-button @click="noticeDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 发起申诉 ==================== -->
    <el-dialog v-model="appealVisible" :title="`发起申诉 ${appealForm.deductNo}`" width="640px" data-testid="deduct-appeal-dialog">
      <el-form :model="appealForm" label-width="100px">
        <el-form-item label="申诉理由" required>
          <el-input v-model="appealForm.appealReason" type="textarea" :rows="3" placeholder="为什么认为这笔扣款不该扣，写事实不写情绪" data-testid="deduct-appeal-reason"/>
        </el-form-item>
        <el-form-item label="申诉材料">
          <el-input v-model="appealForm.appealMaterial" type="textarea" :rows="2" placeholder="附了哪些单据/截图/签字件"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="appealVisible = false">取消</el-button>
        <el-button type="primary" :loading="appealSubmitting" data-testid="deduct-appeal-submit" @click="submitAppeal">提交申诉</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 录入申诉结果 ==================== -->
    <el-dialog v-model="resultVisible" :title="`录入申诉结果 ${resultForm.deductNo}`" width="600px" data-testid="deduct-result-dialog">
      <el-form :model="resultForm" label-width="100px">
        <el-form-item label="申诉结果" required>
          <el-radio-group v-model="resultForm.appealResult" data-testid="deduct-result-radio">
            <el-radio :value="1">申诉成功（扣款撤销）</el-radio>
            <el-radio :value="2">申诉驳回（维持扣款待缴）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="结果说明">
          <el-input v-model="resultForm.appealResultRemark" type="textarea" :rows="3" placeholder="照医保局回复原文写"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resultVisible = false">取消</el-button>
        <el-button type="primary" :loading="resultSubmitting" data-testid="deduct-result-submit" @click="submitAppealResult">保存</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 确认扣款并追责 ==================== -->
    <el-dialog v-model="confirmVisible" :title="`确认扣款并追责 ${confirmForm.deductNo}`" width="680px" data-testid="deduct-confirm-dialog">
      <el-alert type="info" :closable="false" class="mb-3"
                :title="`扣款金额 ${money(confirmForm.deductAmount)}；共担时科室+个人分摊之和不得超过它，差额视为院方承担`"/>
      <el-form :model="confirmForm" label-width="110px">
        <el-form-item label="责任科室" required>
          <el-select v-model="confirmForm.liableDeptId" filterable style="width: 100%" @change="onConfirmDept" data-testid="deduct-confirm-dept">
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="String(d.id)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="责任人">
          <el-input v-model="confirmForm.liableEmpName" placeholder="个人承担或共担时要写到人"/>
        </el-form-item>
        <el-form-item label="损失承担方式" required>
          <el-select v-model="confirmForm.lossBearType" style="width: 100%" data-testid="deduct-confirm-bear">
            <el-option v-for="d in dictOptions(DICT_TYPE.YB_LOSS_BEAR)" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="科室承担">
              <el-input-number v-model="confirmForm.bearDeptAmount" :min="0" :precision="2" :controls="false" style="width: 100%" data-testid="deduct-confirm-dept-amount"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="个人承担">
              <el-input-number v-model="confirmForm.bearEmpAmount" :min="0" :precision="2" :controls="false" style="width: 100%" data-testid="deduct-confirm-emp-amount"/>
            </el-form-item>
          </el-col>
        </el-row>
        <div v-if="confirmForm.lossBearType === 4" class="text-xs text-slate-500 ml-[110px]">
          已分摊 {{ money(bearSum) }} / {{ money(confirmForm.deductAmount) }}
        </div>
      </el-form>
      <template #footer>
        <el-button @click="confirmVisible = false">取消</el-button>
        <el-button type="primary" :loading="confirmSubmitting" data-testid="deduct-confirm-submit" @click="submitConfirm">确认扣款</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 录入缴回 ==================== -->
    <el-dialog v-model="paybackVisible" :title="`录入缴回 ${paybackForm.deductNo}`" width="600px" data-testid="deduct-payback-dialog">
      <el-alert type="warning" :closable="false" class="mb-3" :title="`缴回金额须等于扣款金额 ${money(paybackForm.deductAmount)}，差额走院内财务承担流程`"/>
      <el-form :model="paybackForm" label-width="110px">
        <el-form-item label="缴回金额" required>
          <el-input-number v-model="paybackForm.paidAmount" :min="0.01" :precision="2" :controls="false" style="width: 100%" data-testid="deduct-payback-amount"/>
        </el-form-item>
        <el-form-item label="缴回日期" required data-testid="deduct-payback-date">
          <el-date-picker v-model="paybackForm.paybackDate" type="date" value-format="YYYY-MM-DD" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="凭证号/流水" required>
          <el-input v-model="paybackForm.paybackVoucher" placeholder="如 YB-PAY-20260918-0037" data-testid="deduct-payback-voucher"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="paybackVisible = false">取消</el-button>
        <el-button type="primary" :loading="paybackSubmitting" data-testid="deduct-payback-submit" @click="submitPayback">确认缴回</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 批次 新建/编辑 ==================== -->
    <el-dialog v-model="inspectFormVisible" :title="inspectForm.id ? `修改批次 ${inspectForm.inspectNo}` : '新建飞检/审核批次'" width="700px" data-testid="inspect-form-dialog">
      <el-form :model="inspectForm" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="检查类型" required>
              <el-select v-model="inspectForm.inspectType" style="width: 100%" data-testid="inspect-form-type">
                <el-option v-for="d in dictOptions(DICT_TYPE.YB_INSPECT_TYPE)" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="统筹区/医保局" required>
              <el-input v-model="inspectForm.fundOrg" placeholder="如 长沙市医疗保障局" data-testid="inspect-form-fund-org"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="审核期间起" required data-testid="inspect-form-start">
              <el-date-picker v-model="inspectForm.inspectStartDate" type="date" value-format="YYYY-MM-DD" style="width: 100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="审核期间止" required data-testid="inspect-form-end">
              <el-date-picker v-model="inspectForm.inspectEndDate" type="date" value-format="YYYY-MM-DD" style="width: 100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="进驻/通知日期" required data-testid="inspect-form-date">
              <el-date-picker v-model="inspectForm.inspectDate" type="date" value-format="YYYY-MM-DD" style="width: 100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检查组">
              <el-input v-model="inspectForm.inspectTeam" placeholder="如 国家医保局第12飞检组"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="院内接待人">
              <el-input v-model="inspectForm.ourReceiver" placeholder="谁在现场对接检查组"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="inspectForm.remark" type="textarea" :rows="2"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inspectFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="inspectSubmitting" data-testid="inspect-form-save" @click="saveInspect">保存</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 批次结项 ==================== -->
    <el-dialog v-model="concludeVisible" :title="`批次结项 ${concludeForm.inspectNo}`" width="640px" data-testid="inspect-conclude-dialog">
      <el-alert type="info" :closable="false" class="mb-3" title="结项后批次内容不可再改，结论会永久留档"/>
      <el-form :model="concludeForm" label-width="90px">
        <el-form-item label="结项结论" required>
          <el-input v-model="concludeForm.conclusion" type="textarea" :rows="4"
                    placeholder="抽查多少份、发现几类问题、合计扣款多少、整改要求是什么" data-testid="inspect-conclude-text"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="concludeVisible = false">取消</el-button>
        <el-button type="primary" :loading="concludeSubmitting" data-testid="inspect-conclude-submit" @click="submitConclude">确认结项</el-button>
      </template>
    </el-dialog>
  </div>
</template>
