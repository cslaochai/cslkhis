<script setup lang="ts">
/**
 * 住院转科（P4.2：发起 → 转入科室接收 → 停原医嘱 + 换科室换床 + 回写病历）
 *
 * 这个页面替代了原来的硬编码演示壳（写死的 4 行假数据，混着"转科"和"交接班"两件事）——
 * 按项目规范，所有页面数据必须来自后端接口。
 *
 * 六条口径：
 * 1. **转科 ≠ 换床**：同科室挪床位走「入出院管理 → 换床」（`/patient/inpatient/transfer`），
 *    跨科室才走本页；发起同科室转科会被后端直接拒绝。
 * 2. **发起 ≠ 生效**：`save` 只留下一张「待接收」的单，床位不占、科室不改、医嘱不停。
 *    所以列表里"待接收"是正常中间态，不是失败。
 * 3. **按钮可用性由后端给**（canAccept / canCancel），不按 transferStatus 码值 switch，
 *    也不在本地拦截（本地拦截会掩盖后端规则的失效）。
 * 4. **接收会自动回写转科记录病历**（record_type=10），列表的「病历号」列就是证据链。
 * 5. **医嘱处置必须展示**：orderRemark 写清"停了几条、哪几条没停掉"，后端刻意不静默跳过。
 * 6. 所有 ID 都是字符串（雪花ID），不要 Number()。
 *
 * 本页**不做**「医护交接班」（那是另一个闭环：总值班交班本走菜单 806，本页从未实现过，
 * 故 sql/188 把菜单名里的「/ 交接班」去掉，只叫「转科管理」），
 * 也不做转科审批流（系统无审批流引擎，做半套审批比不做更糟）。
 */
import { ref, computed, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Plus, Search, Warning } from '@element-plus/icons-vue'
import {
  getTransferListPage,
  getTransferDetail,
  saveTransfer,
  acceptTransfer,
  cancelTransfer,
  getTransferPendingCount,
} from '@/api/inpatientTransfer'
import { getInpatientListPage, getInpatientWardList, getInpatientBedList } from '@/api/inpatient'
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
  wardId?: string
  wardName?: string
  deptId?: string
  deptName?: string
}

interface DeptOption {
  id: string
  deptName?: string
}

interface WardOption {
  wardId: string
  wardName?: string
  deptId?: string
  deptName?: string
  freeBeds?: number
}

interface BedOption {
  bedId: string
  bedNo?: string
  wardId?: string
  wardName?: string
  deptId?: string
  deptName?: string
  bedStatus?: number
  bedStatusText?: string
}

interface TransferRow {
  id: string
  transferNo?: string
  admissionId?: string
  admissionNo?: string
  patientId?: string
  patientName?: string
  fromDeptId?: string
  fromDeptName?: string
  fromWardId?: string
  fromWardName?: string
  fromBedId?: string
  fromBedNo?: string
  toDeptId?: string
  toDeptName?: string
  toWardId?: string
  toWardName?: string
  toBedId?: string
  toBedNo?: string
  transferType?: number
  transferTypeText?: string
  transferReason?: string
  hospitalDays?: number
  stopOrdersCount?: number
  orderRemark?: string
  applyDoctorName?: string
  receiveDoctorName?: string
  recordId?: string
  recordNo?: string
  applyTime?: string
  receiveTime?: string
  transferStatus?: number
  transferStatusText?: string
  cancelReason?: string
  remark?: string
  waitingMinutes?: number
  waitText?: string
  canAccept?: boolean
  canCancel?: boolean
}

const fmt = (v?: string) => (v ? String(v).replace('T', ' ') : '—')
const text = (v?: string | number) => (v === null || v === undefined || v === '' ? '—' : String(v))

// ---------------- 基础数据 ----------------

const admissions = ref<AdmissionOption[]>([])
const depts = ref<DeptOption[]>([])
const wards = ref<WardOption[]>([])

const admissionLabel = (a: AdmissionOption) =>
  `${a.bedNo || '—'} ${a.patientName || '—'}（${a.deptName || a.wardName || '—'}）`

const loadAdmissions = async () => {
  try {
    const res = await getInpatientListPage({ admitStatus: 1, pageNum: 1, pageSize: 200 })
    admissions.value = (res.data?.records || []) as AdmissionOption[]
    // 刻意**不默认选中第一位患者**：转科管理是"转入科室的工作台"，默认就该看到全部待接收。
    // admissionId 为空 = 不按住院过滤（后端把 null 当作"不过滤"）。
  } catch (error: any) {
    console.error('加载在院患者失败:', error)
  }
}

const loadDepts = async () => {
  try {
    // 转科申请：目标科室 = 转出科室之外的范围，但**不能越过自己的授权范围**。
    // 不传 scope → 默认按当前人过滤。
    const res = await getDepartmentSelectList({})
    depts.value = (res.data || []) as DeptOption[]
  } catch (error: any) {
    console.error('加载科室失败:', error)
  }
}

const loadWards = async () => {
  try {
    const res = await getInpatientWardList()
    wards.value = (res.data || []) as WardOption[]
  } catch (error: any) {
    console.error('加载病区失败:', error)
  }
}

/** 目标床位：只取空闲（bedStatus=1）—— "能不能用"以 sys_bed 为准，不看病区的演示计数 */
const targetBeds = ref<BedOption[]>([])
const loadTargetBeds = async (wardId: string) => {
  targetBeds.value = []
  if (!wardId) return
  try {
    const res = await getInpatientBedList({ wardId, bedStatus: 1 })
    targetBeds.value = (res.data || []) as BedOption[]
  } catch (error: any) {
    console.error('加载床位失败:', error)
  }
}

// ---------------- 列表 ----------------

const rows = ref<TransferRow[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(DEFAULT_PAGE_SIZE)
const loading = ref(false)
const pendingCount = ref(0)

const filters = reactive({
  admissionId: '',
  toDeptId: '',
  transferStatus: '' as number | '',
  transferType: '' as number | '',
  keyword: '',
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const loadList = async () => {
  loading.value = true
  try {
    const res = await getTransferListPage({
      admissionId: filters.admissionId || undefined,
      toDeptId: filters.toDeptId || undefined,
      transferStatus: filters.transferStatus === '' ? undefined : filters.transferStatus,
      transferType: filters.transferType === '' ? undefined : filters.transferType,
      keyword: filters.keyword || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    })
    rows.value = (res.data?.records || []) as TransferRow[]
    total.value = Number(res.data?.total || 0)
  } catch (error: any) {
    ElMessage.error(error.message || '加载转科记录失败')
  } finally {
    loading.value = false
  }
}

const loadPendingCount = async () => {
  try {
    const res = await getTransferPendingCount({})
    pendingCount.value = Number(res.data || 0)
  } catch (error: any) {
    console.error('加载待接收数失败:', error)
  }
}

/** 本页"医嘱没停干净"的行数：只认后端写出的「仍有 … 未停」，前端不自己拼这套规则 */
const pageUnsettledCount = computed(
  () => rows.value.filter((r) => (r.orderRemark || '').includes('仍有')).length,
)

const handleSearch = () => {
  pageNum.value = 1
  loadList()
}

const resetFilters = () => {
  filters.admissionId = ''
  filters.toDeptId = ''
  filters.transferStatus = ''
  filters.transferType = ''
  filters.keyword = ''
  pageNum.value = 1
  loadList()
}

// ---------------- 发起转科 ----------------

const applyVisible = ref(false)
const applySubmitting = ref(false)
const applyForm = reactive({
  admissionId: '',
  toDeptId: '',
  toWardId: '',
  toBedId: '',
  transferType: 1,
  transferReason: '',
  remark: '',
})

const currentAdmission = computed(() =>
  admissions.value.find((a) => String(a.admissionId) === String(applyForm.admissionId)),
)

/** 目标病区：按目标科室过滤（后端也会校验"病区属于科室"，前端只是少让人点错） */
const applyWards = computed(() =>
  wards.value.filter((w) => String(w.deptId) === String(applyForm.toDeptId)),
)

/** 目标科室：排除患者当前科室（同科室挪床请走换床） */
const applyDepts = computed(() =>
  depts.value.filter((d) => String(d.id) !== String(currentAdmission.value?.deptId || '')),
)

const openApply = (presetAdmissionId?: string) => {
  applyForm.admissionId = presetAdmissionId || filters.admissionId || ''
  applyForm.toDeptId = ''
  applyForm.toWardId = ''
  applyForm.toBedId = ''
  applyForm.transferType = 1
  applyForm.transferReason = ''
  applyForm.remark = ''
  targetBeds.value = []
  applyVisible.value = true
}

const handleApplyAdmissionChange = () => {
  // 换了患者就重选目标：科室 → 病区 → 床位三者联动，留着上一轮的会填到错误科室
  applyForm.toDeptId = ''
  applyForm.toWardId = ''
  applyForm.toBedId = ''
  targetBeds.value = []
}

const handleApplyDeptChange = () => {
  applyForm.toWardId = ''
  applyForm.toBedId = ''
  targetBeds.value = []
}

const handleApplyWardChange = () => {
  applyForm.toBedId = ''
  loadTargetBeds(applyForm.toWardId)
}

const submitApply = async () => {
  if (!applyForm.admissionId) {
    ElMessage.warning('请选择要转科的在院患者')
    return
  }
  if (!applyForm.toDeptId || !applyForm.toWardId || !applyForm.toBedId) {
    ElMessage.warning('请完整选择转入科室、病区与床位')
    return
  }
  if (!applyForm.transferReason.trim()) {
    ElMessage.warning('请填写转科原因（转科是一个医疗决定，必须写清理由）')
    return
  }
  applySubmitting.value = true
  try {
    const res = await saveTransfer({
      admissionId: applyForm.admissionId,
      toDeptId: applyForm.toDeptId,
      toWardId: applyForm.toWardId,
      toBedId: applyForm.toBedId,
      transferType: applyForm.transferType,
      transferReason: applyForm.transferReason.trim(),
      remark: applyForm.remark.trim() || undefined,
    })
    ElMessage.success(`转科申请已提交：${res.data || ''}（等待转入科室接收）`)
    applyVisible.value = false
    await Promise.all([loadList(), loadPendingCount()])
  } catch (error: any) {
    ElMessage.error(error.message || '转科申请提交失败')
  } finally {
    applySubmitting.value = false
  }
}

// ---------------- 接收 / 取消 / 详情 ----------------

const acceptVisible = ref(false)
const acceptSubmitting = ref(false)
const acceptTarget = ref<TransferRow | null>(null)
const acceptRemark = ref('')

const openAccept = (row: TransferRow) => {
  acceptTarget.value = row
  acceptRemark.value = ''
  acceptVisible.value = true
}

const submitAccept = async () => {
  if (!acceptTarget.value) return
  acceptSubmitting.value = true
  try {
    await acceptTransfer({
      transferId: acceptTarget.value.id,
      remark: acceptRemark.value.trim() || undefined,
    })
    ElMessage.success('已接收，转科生效（原科室长期医嘱已按规则处置，并回写转科记录病历）')
    acceptVisible.value = false
    await Promise.all([loadList(), loadPendingCount(), loadAdmissions()])
  } catch (error: any) {
    ElMessage.error(error.message || '接收失败')
  } finally {
    acceptSubmitting.value = false
  }
}

const handleCancel = async (row: TransferRow) => {
  try {
    const { value } = await ElMessageBox.prompt(
      `确认取消转科申请 ${row.transferNo || ''}（${row.patientName || ''}）？已接收的转科不能取消。`,
      '取消转科申请',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '再想想',
        inputPlaceholder: '取消原因（必填）',
        inputValidator: (v: string) => (v && v.trim() ? true : '取消原因不能为空'),
      },
    )
    await cancelTransfer({ transferId: row.id, cancelReason: value.trim() })
    ElMessage.success('转科申请已取消')
    await Promise.all([loadList(), loadPendingCount()])
  } catch (error: any) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error.message || '取消失败')
  }
}

const detailVisible = ref(false)
const detail = ref<TransferRow | null>(null)

const openDetail = async (row: TransferRow) => {
  try {
    const res = await getTransferDetail(row.id)
    detail.value = (res.data || row) as TransferRow
    detailVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '加载转科详情失败')
  }
}

const statusTagType = (status?: number) => {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  return 'info'
}

onMounted(async () => {
  await Promise.all([loadAdmissions(), loadDepts(), loadWards()])
  await Promise.all([loadList(), loadPendingCount()])
})
</script>

<template>
  <div>
    <!-- 标题 + 在院患者过滤 -->
    <div class="mb-3 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-xl font-semibold text-slate-900">转科管理</h1>
        <p class="mt-1 text-sm text-slate-500">
          跨科室转科：发起 → 转入科室接收（停原科室长期医嘱 + 换科室换床 + 回写转科记录病历）。
          同科室挪床位请走「入出院管理 → 换床」。
        </p>
      </div>
      <div class="flex items-center gap-3">
        <el-select
          v-model="filters.admissionId"
          data-testid="p4-transfer-admission"
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
        <el-button v-perm="'ipd:transfer:add'" type="primary" :icon="Plus" data-testid="p4-transfer-apply" @click="openApply()">
          发起转科
        </el-button>
        <el-button :icon="Refresh" @click="handleSearch">刷新</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="mb-3 grid grid-cols-2 gap-4 lg:grid-cols-3">
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">待接收转科</p>
        <p class="text-lg font-bold text-slate-900" data-testid="p4-transfer-pending">{{ pendingCount }}</p>
        <p class="text-[11px] text-slate-400">发起后需转入科室接收才生效</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">当前筛选结果</p>
        <p class="text-lg font-bold text-slate-900" data-testid="p4-transfer-total">{{ total }}</p>
        <p class="text-[11px] text-slate-400">共 {{ total }} 条转科轨迹</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">本页医嘱未停清</p>
        <p
          class="text-lg font-bold"
          :class="pageUnsettledCount > 0 ? 'text-red-600' : 'text-slate-900'"
          data-testid="p4-transfer-unsettled"
        >
          {{ pageUnsettledCount }}
        </p>
        <p class="text-[11px] text-slate-400">待校对医嘱需原科室医生处理</p>
      </div>
    </div>

    <!-- 筛选 + 表格（两卡式列表页，口径参照 views/system/user/UserView.vue） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="filters" inline @submit.prevent>
        <el-form-item label="状态">
          <el-select
            v-model="filters.transferStatus"
            data-testid="p4-transfer-filter-status"
            placeholder="状态"
            clearable
            class="!w-32"
            @change="handleSearch"
          >
            <el-option label="待接收" :value="0" />
            <el-option label="已完成" :value="1" />
            <el-option label="已取消" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="转入科室">
          <el-select
            v-model="filters.toDeptId"
            data-testid="p4-transfer-filter-dept"
            placeholder="转入科室"
            clearable
            filterable
            class="!w-44"
            @change="handleSearch"
          >
            <el-option v-for="d in depts" :key="d.id" :label="d.deptName || d.id" :value="String(d.id)" />
          </el-select>
        </el-form-item>
        <el-form-item label="转科类型">
          <el-select
            v-model="filters.transferType"
            data-testid="p4-transfer-filter-type"
            placeholder="转科类型"
            clearable
            class="!w-32"
            @change="handleSearch"
          >
            <el-option label="普通转科" :value="1" />
            <el-option label="急诊转科" :value="2" />
            <el-option label="转入ICU" :value="3" />
            <el-option label="ICU转出" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input
            v-model="filters.keyword"
            placeholder="转科单号 / 入院号 / 患者姓名 / 转科原因"
            clearable
            class="!w-72"
            :prefix-icon="Search"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" style="width: 100%" stripe :max-height="tableMaxHeight" data-testid="p4-transfer-table">
        <el-table-column prop="transferNo" label="转科单号" width="150" />
        <el-table-column label="患者" min-width="140">
          <template #default="{ row }">
            <div class="text-slate-900">{{ text(row.patientName) }}</div>
            <div class="text-slate-400">{{ text(row.admissionNo) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="转出 → 转入" min-width="220">
          <template #default="{ row }">
            <span class="text-slate-700">{{ text(row.fromDeptName) }}</span>
            <span class="mx-1 text-slate-400">→</span>
            <span class="font-medium text-slate-900">{{ text(row.toDeptName) }}</span>
            <div class="text-slate-400">
              {{ text(row.fromWardName) }} {{ text(row.fromBedNo) }}床 →
              {{ text(row.toWardName) }} {{ text(row.toBedNo) }}床
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="transferTypeText" label="类型" width="90" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.transferStatus)" size="small">
              {{ text(row.transferStatusText) }}
            </el-tag>
            <div class="mt-1 text-slate-400">{{ text(row.waitText) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="发起 / 接收" min-width="170">
          <template #default="{ row }">
            <div class="text-slate-700">{{ text(row.applyDoctorName) }}</div>
            <div class="text-slate-400">{{ fmt(row.applyTime) }}</div>
            <div v-if="row.receiveDoctorName" class="mt-1 text-slate-700">{{ row.receiveDoctorName }}</div>
            <div v-if="row.receiveTime" class="text-slate-400">{{ fmt(row.receiveTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="医嘱处置" min-width="220">
          <template #default="{ row }">
            <div v-if="!row.orderRemark" class="text-slate-400">—（待接收，尚未处置）</div>
            <div
              v-else
              class="leading-5"
              :class="(row.orderRemark || '').includes('仍有') ? 'text-red-600' : 'text-slate-600'"
            >
              {{ row.orderRemark }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="病历号" width="140">
          <template #default="{ row }">
            <span v-if="row.recordNo" class="text-slate-700">{{ row.recordNo }}</span>
            <span v-else class="text-slate-400">未回写</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.canAccept"
              v-perm="'ipd:transfer:edit'"
              type="primary"
              link
              data-testid="p4-transfer-accept"
              @click="openAccept(row)"
            >
              接收
            </el-button>
            <el-button
              v-if="row.canCancel"
              v-perm="'ipd:transfer:delete'"
              type="warning"
              link
              data-testid="p4-transfer-cancel"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
            <el-button type="info" link data-testid="p4-transfer-detail" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-6 text-sm text-slate-400" data-testid="p4-transfer-empty">
            暂无转科记录（点击右上角「发起转科」开始）
          </div>
        </template>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
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
    </el-card>

    <!-- 发起转科 -->
    <el-dialog v-model="applyVisible" title="发起转科" width="640px" data-testid="p4-transfer-apply-dialog">
      <el-form label-width="96px">
        <el-form-item label="在院患者" required>
          <el-select
            v-model="applyForm.admissionId"
            data-testid="p4-apply-admission"
            placeholder="选择在院患者"
            filterable
            class="!w-full"
            @change="handleApplyAdmissionChange"
          >
            <el-option
              v-for="a in admissions"
              :key="a.admissionId"
              :label="admissionLabel(a)"
              :value="String(a.admissionId)"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="currentAdmission" label="当前所在">
          <span class="text-slate-700">
            {{ text(currentAdmission.deptName) }} / {{ text(currentAdmission.wardName) }} /
            {{ text(currentAdmission.bedNo) }}床
          </span>
          <span class="ml-2 text-[11px] text-slate-400">转出科室以此为准，发起时不改动</span>
        </el-form-item>
        <el-form-item label="转入科室" required>
          <el-select
            v-model="applyForm.toDeptId"
            data-testid="p4-apply-dept"
            placeholder="选择转入科室（已排除原科室）"
            filterable
            class="!w-full"
            @change="handleApplyDeptChange"
          >
            <el-option v-for="d in applyDepts" :key="d.id" :label="d.deptName || d.id" :value="String(d.id)" />
          </el-select>
        </el-form-item>
        <el-form-item label="转入病区" required>
          <el-select
            v-model="applyForm.toWardId"
            data-testid="p4-apply-ward"
            placeholder="选择转入病区"
            filterable
            class="!w-full"
            :disabled="!applyForm.toDeptId"
            @change="handleApplyWardChange"
          >
            <el-option
              v-for="w in applyWards"
              :key="w.wardId"
              :label="`${w.wardName || w.wardId}（空闲 ${w.freeBeds ?? 0}）`"
              :value="String(w.wardId)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="转入床位" required>
          <el-select
            v-model="applyForm.toBedId"
            data-testid="p4-apply-bed"
            placeholder="选择空闲床位"
            filterable
            class="!w-full"
            :disabled="!applyForm.toWardId"
          >
            <el-option
              v-for="b in targetBeds"
              :key="b.bedId"
              :label="`${b.bedNo || b.bedId}床`"
              :value="String(b.bedId)"
            />
          </el-select>
          <div v-if="applyForm.toWardId && targetBeds.length === 0" class="mt-1 text-[11px] text-red-500">
            该病区当前没有空闲床位
          </div>
        </el-form-item>
        <el-form-item label="转科类型">
          <el-radio-group v-model="applyForm.transferType" data-testid="p4-apply-type">
            <el-radio :value="1">普通转科</el-radio>
            <el-radio :value="2">急诊转科</el-radio>
            <el-radio :value="3">转入ICU</el-radio>
            <el-radio :value="4">ICU转出</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="转科原因" required>
          <el-input
            v-model="applyForm.transferReason"
            type="textarea"
            :rows="3"
            data-testid="p4-apply-reason"
            placeholder="请写明要解决的问题（如：骨折需手术治疗、病情稳定转回普通病房）"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="applyForm.remark" type="textarea" :rows="2" placeholder="可空" />
        </el-form-item>
      </el-form>
      <div class="rounded border border-amber-200 bg-amber-50 px-3 py-2 text-[12px] leading-5 text-amber-700">
        <el-icon class="mr-1 align-middle"><Warning /></el-icon>
        发起后转科<b>尚未生效</b>：床位不占、科室不改、医嘱不停，需转入科室在列表里点「接收」。
        接收时系统会自动停掉原科室「已校对 / 执行中」的长期医嘱，并回写一份转科记录病历。
      </div>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button v-perm="'ipd:transfer:add'" type="primary" :loading="applySubmitting" data-testid="p4-apply-submit" @click="submitApply">
          提交转科申请
        </el-button>
      </template>
    </el-dialog>

    <!-- 接收转科 -->
    <el-dialog v-model="acceptVisible" title="接收转科" width="560px" data-testid="p4-transfer-accept-dialog">
      <div v-if="acceptTarget" class="space-y-3">
        <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm">
          <div class="text-slate-900">
            {{ text(acceptTarget.patientName) }}（{{ text(acceptTarget.admissionNo) }}）
          </div>
          <div class="mt-1 text-slate-600">
            <span class="text-slate-500">{{ text(acceptTarget.fromDeptName) }}</span>
            <span class="mx-1">→</span>
            <span class="font-medium">{{ text(acceptTarget.toDeptName) }}</span>
          </div>
          <div class="text-[12px] text-slate-500">
            转入 {{ text(acceptTarget.toWardName) }} {{ text(acceptTarget.toBedNo) }}床 ·
            {{ text(acceptTarget.transferTypeText) }}
          </div>
          <div class="mt-1 text-[12px] text-slate-500">转科原因：{{ text(acceptTarget.transferReason) }}</div>
        </div>
        <el-alert type="warning" :closable="false" show-icon>
          接收即生效：停原科室长期医嘱 → 换科室换床 → 回写转科记录病历。
          若该床位在此期间已被占用，后端会拒绝（不会把两个患者塞进同一张床）。
        </el-alert>
        <el-form label-width="80px">
          <el-form-item label="接收备注">
            <el-input
              v-model="acceptRemark"
              type="textarea"
              :rows="2"
              data-testid="p4-accept-remark"
              placeholder="可空"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="acceptVisible = false">取消</el-button>
        <el-button v-perm="'ipd:transfer:edit'" type="primary" :loading="acceptSubmitting" data-testid="p4-accept-submit" @click="submitAccept">
          确认接收
        </el-button>
      </template>
    </el-dialog>

    <!-- 转科详情 -->
    <el-dialog v-model="detailVisible" title="转科详情" width="720px" data-testid="p4-transfer-detail-dialog">
      <div v-if="detail" class="space-y-4">
        <div class="flex items-center gap-3">
          <span class="text-base font-semibold text-slate-900">{{ text(detail.transferNo) }}</span>
          <el-tag :type="statusTagType(detail.transferStatus)" size="small">
            {{ text(detail.transferStatusText) }}
          </el-tag>
          <span v-if="detail.waitText" class="text-[12px] text-slate-400">{{ detail.waitText }}</span>
        </div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="患者">{{ text(detail.patientName) }}</el-descriptions-item>
          <el-descriptions-item label="入院号">{{ text(detail.admissionNo) }}</el-descriptions-item>
          <el-descriptions-item label="转出科室">{{ text(detail.fromDeptName) }}</el-descriptions-item>
          <el-descriptions-item label="转入科室">{{ text(detail.toDeptName) }}</el-descriptions-item>
          <el-descriptions-item label="转出床位">
            {{ text(detail.fromWardName) }} {{ text(detail.fromBedNo) }}床
          </el-descriptions-item>
          <el-descriptions-item label="转入床位">
            {{ text(detail.toWardName) }} {{ text(detail.toBedNo) }}床
          </el-descriptions-item>
          <el-descriptions-item label="转科类型">{{ text(detail.transferTypeText) }}</el-descriptions-item>
          <el-descriptions-item label="已住院天数">
            {{ detail.hospitalDays === undefined || detail.hospitalDays === null ? '—' : detail.hospitalDays + ' 天' }}
          </el-descriptions-item>
          <el-descriptions-item label="发起医生">{{ text(detail.applyDoctorName) }}</el-descriptions-item>
          <el-descriptions-item label="发起时间">{{ fmt(detail.applyTime) }}</el-descriptions-item>
          <el-descriptions-item label="接收医生">{{ text(detail.receiveDoctorName) }}</el-descriptions-item>
          <el-descriptions-item label="接收时间">{{ fmt(detail.receiveTime) }}</el-descriptions-item>
          <el-descriptions-item label="转科原因" :span="2">{{ text(detail.transferReason) }}</el-descriptions-item>
          <el-descriptions-item label="医嘱处置" :span="2">
            <span :class="(detail.orderRemark || '').includes('仍有') ? 'text-red-600' : 'text-slate-700'">
              {{ text(detail.orderRemark) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="回写病历号" :span="2">
            <span v-if="detail.recordNo" class="text-slate-700">{{ detail.recordNo }}</span>
            <span v-else class="text-slate-400">尚未回写（转科未生效）</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.cancelReason" label="取消原因" :span="2">
            {{ detail.cancelReason }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.remark" label="备注" :span="2">{{ detail.remark }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>
