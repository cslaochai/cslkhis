<script setup lang="ts">
/**
 * 住院会诊（P4.1：申请 → 应答 → 会诊记录 → 完成 → 回写病历）
 *
 * 这个页面替代了原来的硬编码演示壳（写死的 4 行假数据）—— 按项目规范，所有页面数据必须来自后端接口。
 *
 * 五条口径：
 * 1. **按钮可用性由后端给**（canAccept / canFinish / canCancel / canEdit），不按 consultStatus 码值 switch。
 * 2. **未应答不可完成、已应答不可取消**是后端铁律，前端只按 canFinish / canCancel 渲染，
 *    不做"看起来能点"的按钮，也不在本地拦截（本地拦截会掩盖后端规则的失效）。
 * 3. **完成会诊会自动回写住院病历**：完成后提示里带出返回的病历ID，列表的「病历号」列即证据链。
 * 4. **急会诊超时是后端算的**（overdue / overdueText），前端不重复算时间差。
 * 5. 所有 ID 都是字符串（雪花ID），不要 Number()。
 */
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Plus, Search, User, CircleCheck, Warning, Clock, Document } from '@element-plus/icons-vue'
import {
  getConsultationListPage,
  getConsultationDetail,
  saveConsultation,
  acceptConsultation,
  finishConsultation,
  cancelConsultation,
  getConsultationUnfinishedCount,
} from '@/api/inpatientConsultation'
import { getInpatientListPage } from '@/api/inpatient'
import { getDepartmentSelectList } from '@/api/system'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

interface AdmissionOption {
  admissionId: string
  admissionNo?: string
  patientId?: string
  patientName?: string
  patientNo?: string
  bedNo?: string
  wardName?: string
  deptName?: string
  deptId?: string
}

interface DeptOption {
  id: string
  deptName?: string
}

interface ConsultationRow {
  consultationId: string
  consultationNo?: string
  admissionId?: string
  admissionNo?: string
  patientId?: string
  patientName?: string
  patientNo?: string
  bedNo?: string
  fromDeptId?: string
  fromDeptName?: string
  toDeptId?: string
  toDeptName?: string
  consultType?: number
  consultTypeText?: string
  isUrgent?: number
  isUrgentText?: string
  reason?: string
  applyDoctorName?: string
  applyTime?: string
  consultStatus?: number
  consultStatusText?: string
  acceptTime?: string
  acceptDoctorName?: string
  finishTime?: string
  consultTime?: string
  recordId?: string
  recordNo?: string
  conclusion?: string
  cancelReason?: string
  remark?: string
  responseMinutes?: number
  overdue?: boolean
  overdueText?: string
  canAccept?: boolean
  canFinish?: boolean
  canCancel?: boolean
  canEdit?: boolean
}

const fmt = (v?: string) => (v ? String(v).replace('T', ' ') : '—')
const text = (v?: string | number) => (v === null || v === undefined || v === '' ? '—' : String(v))

// ---------------- 基础数据 ----------------

const admissions = ref<AdmissionOption[]>([])
const depts = ref<DeptOption[]>([])
const admissionId = ref<string>('')

const admissionLabel = (a: AdmissionOption) =>
  `${a.bedNo || '—'} ${a.patientName || '—'}（${a.wardName || a.deptName || '—'}）`

const currentAdmission = computed(() => admissions.value.find((a) => String(a.admissionId) === String(admissionId.value)))

const loadAdmissions = async () => {
  try {
    const res = await getInpatientListPage({ admitStatus: 1, pageNum: 1, pageSize: 200 })
    admissions.value = (res.data?.records || []) as AdmissionOption[]
  } catch (error: any) {
    console.error('加载在院患者失败:', error)
  }
}

const loadDepts = async () => {
  try {
    // 不传 scope → 默认按当前人过滤（医生只看到自己被授权的科室）
    const res = await getDepartmentSelectList({})
    depts.value = (res.data || []) as DeptOption[]
  } catch (error: any) {
    console.error('加载科室失败:', error)
  }
}

// ---------------- 列表 ----------------

const rows = ref<ConsultationRow[]>([])
const total = ref(0)
const loading = ref(false)
const pagination = ref({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

const filters = ref({
  toDeptId: '' as string,
  consultStatus: undefined as number | undefined,
  consultType: undefined as number | undefined,
  isUrgent: undefined as number | undefined,
  keyword: '',
  unfinishedOnly: false,
})

const queryParams = (extra: Record<string, any> = {}) => ({
  pageNum: pagination.value.pageNum,
  pageSize: pagination.value.pageSize,
  ...(admissionId.value ? { admissionId: admissionId.value } : {}),
  ...(filters.value.toDeptId ? { toDeptId: filters.value.toDeptId } : {}),
  ...(filters.value.consultStatus !== undefined ? { consultStatus: filters.value.consultStatus } : {}),
  ...(filters.value.consultType !== undefined ? { consultType: filters.value.consultType } : {}),
  ...(filters.value.isUrgent !== undefined ? { isUrgent: filters.value.isUrgent } : {}),
  ...(filters.value.keyword ? { keyword: filters.value.keyword } : {}),
  ...(filters.value.unfinishedOnly ? { unfinishedOnly: 1 } : {}),
  ...extra,
})

const loadList = async () => {
  loading.value = true
  try {
    const res = await getConsultationListPage(queryParams())
    rows.value = (res.data?.records || []) as ConsultationRow[]
    total.value = Number(res.data?.total || 0)
  } catch (error: any) {
    ElMessage.error(error.message || '加载会诊列表失败')
  } finally {
    loading.value = false
  }
}

// ---------------- 统计（全部来自后端，前端不自己数当前页） ----------------

const unfinishedCount = ref(0)
const urgentPendingCount = ref(0)
const finishedCount = ref(0)

const loadCounts = async () => {
  try {
    const [unfinished, urgent, finished] = await Promise.all([
      getConsultationUnfinishedCount({
        ...(admissionId.value ? { admissionId: admissionId.value } : {}),
        ...(filters.value.toDeptId ? { toDeptId: filters.value.toDeptId } : {}),
      }),
      getConsultationListPage(queryParams({ consultStatus: 0, isUrgent: 1, unfinishedOnly: 0, pageNum: 1, pageSize: 1 })),
      getConsultationListPage(queryParams({ consultStatus: 1, unfinishedOnly: 0, pageNum: 1, pageSize: 1 })),
    ])
    unfinishedCount.value = Number(unfinished.data || 0)
    urgentPendingCount.value = Number(urgent.data?.total || 0)
    finishedCount.value = Number(finished.data?.total || 0)
  } catch (error: any) {
    console.error('加载会诊统计失败:', error)
  }
}

const reloadAll = async () => {
  await Promise.all([loadList(), loadCounts()])
}

const handleAdmissionChange = async () => {
  pagination.value.pageNum = 1
  await reloadAll()
}

const handleSearch = async () => {
  pagination.value.pageNum = 1
  await reloadAll()
}

const resetFilters = async () => {
  filters.value = {
    toDeptId: '',
    consultStatus: undefined,
    consultType: undefined,
    isUrgent: undefined,
    keyword: '',
    unfinishedOnly: false,
  }
  pagination.value.pageNum = 1
  await reloadAll()
}

// ---------------- 申请会诊 ----------------

const applyDialog = ref(false)
const saveLoading = ref(false)
const applyForm = ref<any>({})

const emptyApplyForm = () => ({
  admissionId: admissionId.value,
  toDeptId: '',
  consultType: 2,
  isUrgent: 0,
  reason: '',
  remark: '',
})

const openApply = () => {
  if (!admissionId.value) {
    ElMessage.warning('请先选择住院患者（会诊必须挂在一次住院上）')
    return
  }
  applyForm.value = emptyApplyForm()
  applyDialog.value = true
}

const submitApply = async () => {
  if (!applyForm.value.toDeptId) {
    ElMessage.warning('请选择会诊科室')
    return
  }
  if (!applyForm.value.reason || !String(applyForm.value.reason).trim()) {
    ElMessage.warning('请填写会诊理由（会诊方需要知道要解决什么问题）')
    return
  }
  saveLoading.value = true
  try {
    const res = await saveConsultation({ ...applyForm.value, admissionId: admissionId.value })
    ElMessage.success(`会诊申请已提交：${res.data}`)
    applyDialog.value = false
    await reloadAll()
  } catch (error: any) {
    ElMessage.error(error.message || '会诊申请提交失败')
  } finally {
    saveLoading.value = false
  }
}

// ---------------- 接诊 ----------------

const handleAccept = async (row: ConsultationRow) => {
  try {
    await ElMessageBox.confirm(
      `确认由你接诊会诊 ${row.consultationNo}（${row.toDeptName || '—'}）？接诊人将记为当前登录用户。`,
      '会诊接诊',
      { type: 'warning', confirmButtonText: '确认接诊', cancelButtonText: '取消' },
    )
  } catch {
    return
  }
  try {
    await acceptConsultation({ consultationId: row.consultationId })
    ElMessage.success('已接诊')
    await reloadAll()
  } catch (error: any) {
    ElMessage.error(error.message || '接诊失败')
  }
}

// ---------------- 完成（回写病历） ----------------

const finishDialog = ref(false)
const finishLoading = ref(false)
const finishForm = ref<any>({})

const openFinish = (row: ConsultationRow) => {
  finishForm.value = { consultationId: row.consultationId, consultationNo: row.consultationNo, conclusion: '', consultTime: '' }
  finishDialog.value = true
}

const submitFinish = async () => {
  if (!finishForm.value.conclusion || !String(finishForm.value.conclusion).trim()) {
    ElMessage.warning('请填写会诊结论（「已完成」而没有结论，病历上等于什么都没发生）')
    return
  }
  finishLoading.value = true
  try {
    const res = await finishConsultation({
      consultationId: finishForm.value.consultationId,
      conclusion: finishForm.value.conclusion,
      consultTime: finishForm.value.consultTime || undefined,
    })
    ElMessage.success(`会诊已完成，已回写病历（病历ID ${res.data || '—'}）`)
    finishDialog.value = false
    await reloadAll()
  } catch (error: any) {
    ElMessage.error(error.message || '完成会诊失败')
  } finally {
    finishLoading.value = false
  }
}

// ---------------- 取消 ----------------

const cancelDialog = ref(false)
const cancelLoading = ref(false)
const cancelForm = ref<any>({})

const openCancel = (row: ConsultationRow) => {
  cancelForm.value = { consultationId: row.consultationId, consultationNo: row.consultationNo, cancelReason: '' }
  cancelDialog.value = true
}

const submitCancel = async () => {
  if (!cancelForm.value.cancelReason || !String(cancelForm.value.cancelReason).trim()) {
    ElMessage.warning('请填写取消原因（取消是一个临床决定，必须有人负责）')
    return
  }
  cancelLoading.value = true
  try {
    await cancelConsultation({
      consultationId: cancelForm.value.consultationId,
      cancelReason: cancelForm.value.cancelReason,
    })
    ElMessage.success('会诊申请已取消')
    cancelDialog.value = false
    await reloadAll()
  } catch (error: any) {
    ElMessage.error(error.message || '取消失败')
  } finally {
    cancelLoading.value = false
  }
}

// ---------------- 详情 ----------------

const detailDialog = ref(false)
const detail = ref<ConsultationRow | null>(null)

const openDetail = async (row: ConsultationRow) => {
  try {
    const res = await getConsultationDetail(row.consultationId)
    detail.value = (res.data || null) as ConsultationRow
    detailDialog.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '加载会诊详情失败')
  }
}

// ---------------- 展示辅助 ----------------

const statusTagType = (s?: number) => {
  if (s === 0) return 'warning'
  if (s === 3) return 'primary'
  if (s === 1) return 'success'
  if (s === 2) return 'info'
  return 'info'
}

const waitText = (row: ConsultationRow) => {
  if (row.acceptTime) return `已应答 ${row.responseMinutes ?? '—'} 分钟`
  return `等待 ${row.responseMinutes ?? '—'} 分钟`
}

const handleSizeChange = async (size: number) => {
  pagination.value.pageSize = size
  pagination.value.pageNum = 1
  await loadList()
}

const handlePageChange = async (page: number) => {
  pagination.value.pageNum = page
  await loadList()
}

onMounted(async () => {
  await Promise.all([loadAdmissions(), loadDepts()])
  await reloadAll()
})
</script>

<template>
  <div>
    <!-- 页头 -->
    <div class="mb-3 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div class="flex items-center gap-3">
        <el-select
          v-model="admissionId"
          data-testid="p4-consult-admission"
          placeholder="全部在院患者"
          filterable
          clearable
          class="!w-72"
          @change="handleAdmissionChange"
        >
          <el-option v-for="a in admissions" :key="a.admissionId" :label="admissionLabel(a)" :value="String(a.admissionId)" />
        </el-select>
        <el-button :icon="Refresh" data-testid="p4-consult-refresh" @click="reloadAll">刷新</el-button>
        <el-button v-perm="'ipd:consultation:add'" type="primary" :icon="Plus" data-testid="p4-consult-apply" @click="openApply">申请会诊</el-button>
      </div>
    </div>

    <!-- 统计卡片：数字全部来自后端 -->
    <div class="mb-3 grid grid-cols-2 gap-4 lg:grid-cols-4">
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">未完成会诊</p>
        <p class="text-lg font-bold text-slate-900" data-testid="p4-consult-unfinished">{{ unfinishedCount }}</p>
        <p class="text-[11px] text-slate-400">待应答 + 已应答</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">急会诊待应答</p>
        <p class="text-lg font-bold text-slate-900" data-testid="p4-consult-urgent">{{ urgentPendingCount }}</p>
        <p class="text-[11px] text-slate-400">时限 10 分钟，超时在列表标红</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">已完成</p>
        <p class="text-lg font-bold text-slate-900" data-testid="p4-consult-finished">{{ finishedCount }}</p>
        <p class="text-[11px] text-slate-400">完成即回写病历</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">当前筛选结果</p>
        <p class="text-lg font-bold text-slate-900" data-testid="p4-consult-total">{{ total }}</p>
        <p class="text-[11px] text-slate-400">{{ currentAdmission?.patientName || '全部患者' }}</p>
      </div>
    </div>

    <!-- 筛选 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="filters" inline @submit.prevent>
        <el-form-item label="会诊科室">
          <el-select v-model="filters.toDeptId" data-testid="p4-filter-dept" placeholder="会诊科室" clearable class="!w-44" @change="handleSearch">
            <el-option v-for="d in depts" :key="d.id" :label="d.deptName || d.id" :value="String(d.id)" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.consultStatus" data-testid="p4-filter-status" placeholder="状态" clearable class="!w-32" @change="handleSearch">
            <el-option label="待应答" :value="0" />
            <el-option label="已应答" :value="3" />
            <el-option label="已完成" :value="1" />
            <el-option label="已取消" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="范围">
          <el-select v-model="filters.consultType" data-testid="p4-filter-type" placeholder="范围" clearable class="!w-32" @change="handleSearch">
            <el-option label="科内会诊" :value="1" />
            <el-option label="科间会诊" :value="2" />
            <el-option label="全院会诊" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="紧急">
          <el-select v-model="filters.isUrgent" data-testid="p4-filter-urgent" placeholder="紧急" clearable class="!w-32" @change="handleSearch">
            <el-option label="普通会诊" :value="0" />
            <el-option label="急会诊" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input
            v-model="filters.keyword"
            data-testid="p4-filter-keyword"
            placeholder="会诊号 / 患者姓名 / 患者号 / 理由"
            :prefix-icon="Search"
            class="!w-72"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="filters.unfinishedOnly" data-testid="p4-filter-unfinished" @change="handleSearch">只看未完成</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" stripe :max-height="tableMaxHeight" data-testid="p4-consult-table">
        <el-table-column prop="consultationNo" label="会诊号" width="150" />
        <el-table-column label="患者" min-width="150">
          <template #default="{ row }">
            <div class="text-slate-900">{{ text(row.patientName) }}</div>
            <div class="text-slate-400">{{ text(row.patientNo) }} · 床号 {{ text(row.bedNo) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="申请 → 会诊科室" min-width="180">
          <template #default="{ row }">
            <span class="text-slate-700">{{ text(row.fromDeptName) }}</span>
            <span class="mx-1 text-slate-400">→</span>
            <span class="font-medium text-slate-900">{{ text(row.toDeptName) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="范围" width="100" align="center">
          <template #default="{ row }">
            <el-tag effect="plain" size="small">{{ text(row.consultTypeText) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="紧急" width="110" align="center">
          <template #default="{ row }">
            <el-tooltip v-if="row.overdue" :content="row.overdueText || ''" placement="top">
              <el-tag type="danger" size="small" data-testid="p4-row-overdue">急会诊超时</el-tag>
            </el-tooltip>
            <el-tag v-else :type="row.isUrgent === 1 ? 'warning' : 'info'" effect="plain" size="small">
              {{ text(row.isUrgentText) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="会诊理由" min-width="200" show-overflow-tooltip />
        <el-table-column prop="applyDoctorName" label="申请医生" width="100" />
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">{{ fmt(row.applyTime) }}</template>
        </el-table-column>
        <el-table-column label="等待 / 应答" width="130">
          <template #default="{ row }">{{ waitText(row) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.consultStatus)" size="small" data-testid="p4-row-status">
              {{ text(row.consultStatusText) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="病历号" width="150">
          <template #default="{ row }">{{ text(row.recordNo) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.canAccept" v-perm="'ipd:consultation:edit'" link type="primary" size="small" @click="handleAccept(row)">接诊</el-button>
            <el-button v-if="row.canFinish" v-perm="'ipd:consultation:edit'" link type="primary" size="small" @click="openFinish(row)">完成</el-button>
            <el-button v-if="row.canCancel" v-perm="'ipd:consultation:delete'" link type="danger" size="small" @click="openCancel(row)">取消</el-button>
            <el-button link size="small" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-8 text-slate-400">暂无会诊记录</div>
        </template>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
          :current-page="pagination.pageNum"
          :page-size="pagination.pageSize"
          :total="total"
          :page-sizes="PAGE_SIZES"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 申请会诊 -->
    <el-dialog v-model="applyDialog" title="申请会诊" width="560px" destroy-on-close>
      <el-form label-width="96px">
        <el-form-item label="住院患者">
          <el-input :model-value="currentAdmission ? admissionLabel(currentAdmission) : ''" disabled />
        </el-form-item>
        <el-form-item label="会诊科室" required>
          <el-select v-model="applyForm.toDeptId" data-testid="p4-apply-dept" placeholder="请选择会诊科室" filterable class="w-full">
            <el-option v-for="d in depts" :key="d.id" :label="d.deptName || d.id" :value="String(d.id)" />
          </el-select>
          <p class="mt-1 text-[11px] text-slate-400">
            申请科室 = {{ text(currentAdmission?.deptName) }}（由入院记录决定，不由前端填写）
          </p>
        </el-form-item>
        <el-form-item label="会诊范围" required>
          <el-radio-group v-model="applyForm.consultType" data-testid="p4-apply-type">
            <el-radio :value="1">科内会诊</el-radio>
            <el-radio :value="2">科间会诊</el-radio>
            <el-radio :value="3">全院会诊</el-radio>
          </el-radio-group>
          <p class="mt-1 text-[11px] text-slate-400">科内会诊必须与申请科室相同；科间 / 全院必须请到别的科室（后端强制）</p>
        </el-form-item>
        <el-form-item label="急会诊">
          <el-switch v-model="applyForm.isUrgent" :active-value="1" :inactive-value="0" data-testid="p4-apply-urgent" />
          <span class="ml-2 text-[11px] text-slate-400">急会诊时限 10 分钟，超时在列表标红（不阻断）</span>
        </el-form-item>
        <el-form-item label="会诊理由" required>
          <el-input
            v-model="applyForm.reason"
            data-testid="p4-apply-reason"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请写明要解决的问题（会诊方据此准备）"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="applyForm.remark" type="textarea" :rows="2" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialog = false">取消</el-button>
        <el-button v-perm="'ipd:consultation:add'" type="primary" :loading="saveLoading" data-testid="p4-apply-submit" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 完成会诊 -->
    <el-dialog v-model="finishDialog" title="完成会诊" width="600px" destroy-on-close>
      <el-form label-width="96px">
        <el-form-item label="会诊号">
          <el-input :model-value="finishForm.consultationNo" disabled />
        </el-form-item>
        <el-form-item label="会诊时间">
          <el-date-picker
            v-model="finishForm.consultTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="默认当前时间"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="会诊结论" required>
          <el-input
            v-model="finishForm.conclusion"
            data-testid="p4-finish-conclusion"
            type="textarea"
            :rows="5"
            maxlength="1000"
            show-word-limit
            placeholder="会诊意见与处理建议（完成后自动回写住院病历）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="finishDialog = false">取消</el-button>
        <el-button v-perm="'ipd:consultation:edit'" type="primary" :loading="finishLoading" data-testid="p4-finish-submit" @click="submitFinish">完成并回写病历</el-button>
      </template>
    </el-dialog>

    <!-- 取消会诊 -->
    <el-dialog v-model="cancelDialog" title="取消会诊申请" width="520px" destroy-on-close>
      <el-form label-width="96px">
        <el-form-item label="会诊号">
          <el-input :model-value="cancelForm.consultationNo" disabled />
        </el-form-item>
        <el-form-item label="取消原因" required>
          <el-input v-model="cancelForm.cancelReason" data-testid="p4-cancel-reason" type="textarea" :rows="3" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialog = false">返回</el-button>
        <el-button v-perm="'ipd:consultation:delete'" type="danger" :loading="cancelLoading" data-testid="p4-cancel-submit" @click="submitCancel">确认取消</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailDialog" title="会诊详情" width="680px" destroy-on-close>
      <el-descriptions v-if="detail" :column="2" border size="small">
        <el-descriptions-item label="会诊号">{{ text(detail.consultationNo) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ text(detail.consultStatusText) }}</el-descriptions-item>
        <el-descriptions-item label="患者">{{ text(detail.patientName) }}</el-descriptions-item>
        <el-descriptions-item label="患者号">{{ text(detail.patientNo) }}</el-descriptions-item>
        <el-descriptions-item label="入院号">{{ text(detail.admissionNo) }}</el-descriptions-item>
        <el-descriptions-item label="床号">{{ text(detail.bedNo) }}</el-descriptions-item>
        <el-descriptions-item label="申请科室">{{ text(detail.fromDeptName) }}</el-descriptions-item>
        <el-descriptions-item label="会诊科室">{{ text(detail.toDeptName) }}</el-descriptions-item>
        <el-descriptions-item label="会诊范围">{{ text(detail.consultTypeText) }}</el-descriptions-item>
        <el-descriptions-item label="紧急程度">{{ text(detail.isUrgentText) }}</el-descriptions-item>
        <el-descriptions-item label="申请医生">{{ text(detail.applyDoctorName) }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ fmt(detail.applyTime) }}</el-descriptions-item>
        <el-descriptions-item label="接诊医生">{{ text(detail.acceptDoctorName) }}</el-descriptions-item>
        <el-descriptions-item label="接诊时间">{{ fmt(detail.acceptTime) }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ fmt(detail.finishTime) }}</el-descriptions-item>
        <el-descriptions-item label="回写病历号">{{ text(detail.recordNo) }}</el-descriptions-item>
        <el-descriptions-item label="会诊理由" :span="2">{{ text(detail.reason) }}</el-descriptions-item>
        <el-descriptions-item label="会诊结论" :span="2">{{ text(detail.conclusion) }}</el-descriptions-item>
        <el-descriptions-item label="取消原因" :span="2">{{ text(detail.cancelReason) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ text(detail.remark) }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="detail?.overdue" class="mt-3 rounded border border-red-200 bg-red-50 p-2 text-sm text-red-700">
        <el-icon class="mr-1"><Warning /></el-icon>{{ detail.overdueText }}
      </div>
      <div class="mt-3 flex items-center gap-2 text-[11px] text-slate-400">
        <el-icon><Clock /></el-icon>等待/应答时长由后端按申请与接诊时间计算
        <el-icon class="ml-2"><Document /></el-icon>完成会诊后病历号可见（{{ text(detail?.recordNo) }}）
        <el-icon class="ml-2"><User /></el-icon>留痕为员工ID对应的姓名
        <el-icon class="ml-2"><CircleCheck /></el-icon>状态机由后端强制
      </div>
      <template #footer>
        <el-button @click="detailDialog = false">关闭</el-button>
        <el-button v-if="detail?.canFinish" v-perm="'ipd:consultation:edit'" type="primary" @click="detailDialog = false; detail && openFinish(detail)">完成会诊</el-button>
      </template>
    </el-dialog>
  </div>
</template>
