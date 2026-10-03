<script setup lang="ts">
/**
 * 就诊总览（sql/131 正名；原名「门诊日志」→ 更早叫「今日就诊」）
 *
 * 正名原因：法规口径的「门诊日志」是**病历基表**的临床事件台账（新页 /outpatient-log），
 * 本页基表是挂号 —— 连没签到的挂号都在，是运营全景，不是日志。
 *
 * 与旧页面的三点本质差异：
 *  1) 基表是**挂号**（一次就诊=一条挂号），没签到的挂号也在日志里 —— 旧页面只查队列表，漏掉绝大多数未入队挂号；
 *  2) 筛选条件**全部下推后端**（旧页面把 keyword 拿当前页 list.filter，翻页后结果静默变少）；
 *  3) 状态口径用后端推导的 logStatus（挂号+队列两套口径合推），旧页面只看队列状态 → 未入队挂号显示「未知」。
 */
import {ref, onMounted} from 'vue'
import {ElMessage} from 'element-plus'
import {Search, Refresh, View, Document} from '@element-plus/icons-vue'
import {getOpdLogListPage, getOpdLogStats, runDayEndSettle} from '@/api/appoint'
import {getByRegistId, getRecordDetail} from '@/api/emr'
import {listItemsByPatient} from '@/api/settlementBill'
import {getDepartmentSelectList, getEmployeeList} from '@/api/system'
import {
  statusOf,
  unknownOf,
  OPD_LOG_STATUS,
  OPD_LOG_STATUS_OPTIONS,
  REGIST_TYPE,
  REGIST_SOURCE,
  SETTLEMENT_TYPE,
  REVISIT_TYPE,
  APPLY_STATUS,
} from '@/lib/statusColor'
import {patientGenderText} from '@/lib/patientGender'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'
import {shortQueueNo} from '@/lib/utils'

// ========== 列表 ==========
const loading = ref(false)
const rows = ref<any[]>([])
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

/** 统计条默认值：字段与 OpdLogStatsVO 一一对应，别少写（少写的字段会显示成 0 骗人） */
const emptyStats = () => ({
  total: 0, unpaid: 0, waitCheckIn: 0, waiting: 0, consulting: 0,
  completed: 0, cancelled: 0, overdue: 0, unvisited: 0, noShow: 0, unrecognized: 0,
  avgWaitMinutes: 0, avgVisitMinutes: 0,
})
const stats = ref<any>(emptyStats())
/** 批次F：第一次拿到统计之前不能显示 0 —— "0 例就诊"和"还没查出来"是两句不同的话 */
const statsLoaded = ref(false)

// ========== 筛选（默认本月 —— 就诊总览是回溯分析页，不是「看今天」） ==========
const fmtDate = (d: Date) => {
  const y = d.getFullYear(), m = String(d.getMonth() + 1).padStart(2, '0'), dd = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${dd}`
}
const thisMonth = (): [string, string] => {
  const now = new Date()
  return [fmtDate(new Date(now.getFullYear(), now.getMonth(), 1)), fmtDate(new Date(now.getFullYear(), now.getMonth() + 1, 0))]
}

const searchForm = ref({
  dateRange: thisMonth() as [string, string] | null,
  deptIds: [] as string[],
  doctorId: null as string | null,
  statusCodes: [] as number[],
  registType: null as number | null,
  settlementType: null as number | null,
  revisitType: null as number | null,
  keyword: '',
})

const dateShortcuts = [
  {text: '今日', value: () => { const t = new Date(); return [fmtDate(t), fmtDate(t)] }},
  {text: '本周', value: () => {
    const now = new Date(); const day = now.getDay() || 7
    const mon = new Date(now); mon.setDate(now.getDate() - day + 1)
    const sun = new Date(mon); sun.setDate(mon.getDate() + 6)
    return [fmtDate(mon), fmtDate(sun)]
  }},
  {text: '本月', value: () => thisMonth()},
  {text: '近三月', value: () => {
    const now = new Date()
    const first = new Date(now.getFullYear(), now.getMonth() - 2, 1)
    return [fmtDate(first), fmtDate(now)]
  }},
]

// ========== 下拉数据源（科室 / 员工数组，全部走真实接口） ==========
const deptOptions = ref<any[]>([])
const doctorOptions = ref<any[]>([])
const loadOptions = async () => {
  try {
    // 不传 scope → 默认按当前人过滤（今日就诊列表本就该只看自己能管的科室）
    const [d, e] = await Promise.all([getDepartmentSelectList({}), getEmployeeList({})])
    deptOptions.value = d.data || []
    doctorOptions.value = e.data || []
  } catch (err) {
    console.error('加载科室/医生下拉失败:', err)
  }
}

const optionOf = (table: Record<number, { label: string }>) =>
    Object.entries(table).map(([v, s]) => ({value: Number(v), label: s.label}))
const registTypeOptions = optionOf(REGIST_TYPE)
const settlementOptions = optionOf(SETTLEMENT_TYPE)
const revisitOptions = optionOf(REVISIT_TYPE)

// ========== 查询参数：筛选一律下推，前端不做任何 slice/filter ==========
const buildParams = () => {
  const p: any = {pageNum: pagination.value.pageNum, pageSize: pagination.value.pageSize}
  const f = searchForm.value
  if (f.dateRange?.[0] && f.dateRange?.[1]) { p.startDate = f.dateRange[0]; p.endDate = f.dateRange[1] }
  if (f.deptIds.length) p.deptIds = f.deptIds
  if (f.doctorId) p.doctorId = f.doctorId
  if (f.statusCodes.length) p.statusCodes = f.statusCodes
  if (f.registType != null) p.registType = f.registType
  if (f.settlementType != null) p.settlementType = f.settlementType
  if (f.revisitType != null) p.revisitType = f.revisitType
  if (f.keyword.trim()) p.keyword = f.keyword.trim()
  return p
}

const loadData = async () => {
  loading.value = true
  try {
    const params = buildParams()
    const [listRes, statsRes] = await Promise.all([getOpdLogListPage(params), getOpdLogStats(params)])
    rows.value = listRes.data?.records || []
    pagination.value.total = listRes.data?.total || 0
    stats.value = statsRes.data || emptyStats()
    statsLoaded.value = true
  } catch (err) {
    console.error('加载就诊总览失败:', err)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.value.pageNum = 1; loadData() }
const handleReset = () => {
  searchForm.value = {
    dateRange: thisMonth(), deptIds: [], doctorId: null, statusCodes: [],
    registType: null, settlementType: null, revisitType: null, keyword: '',
  }
  pagination.value.pageNum = 1
  loadData()
}
const handleSizeChange = (v: number) => { pagination.value.pageSize = v; pagination.value.pageNum = 1; loadData() }
const handleCurrentChange = (v: number) => { pagination.value.pageNum = v; loadData() }

// ========== 视图辅助 ==========
/** 就诊状态：优先后端推导的 logStatus；推导不出来时用 queueStatus 渲染「未知(n)」，绝不回落 */
const logStatusStyle = (row: any) =>
    row.logStatus != null ? statusOf(OPD_LOG_STATUS, row.logStatus) : unknownOf(row.queueStatus)

const fmt = (t?: string) => (t ? String(t).replace('T', ' ').slice(0, 16) : '-')
const duration = (m?: number | null) => (m == null ? '-' : m + ' 分')

/** 统计条定义（unrecognized 只在 >0 时出现，避免常规视图多一个恒为 0 的格子） */
const statCards = () => {
  const list = [
    {label: '总就诊', value: stats.value.total, color: 'text-slate-800'},
    {label: '未缴费', value: stats.value.unpaid, color: 'text-amber-600'},
    {label: '待签到', value: stats.value.waitCheckIn, color: 'text-slate-600'},
    {label: '候诊中', value: stats.value.waiting, color: 'text-blue-600'},
    {label: '就诊中', value: stats.value.consulting, color: 'text-purple-600'},
    {label: '已就诊', value: stats.value.completed, color: 'text-emerald-600'},
    {label: '已退号', value: stats.value.cancelled, color: 'text-red-500'},
    {label: '已过号', value: stats.value.overdue, color: 'text-orange-500'},
    // 未就诊 / 爽约 是日终结转收的两个终态。放在统计条里是**故意**的：
    // 这两个数字长期为 0 才说明「每天的号都有人收尾」，一旦涨起来就是有人没签字/没结诊。
    {label: '未就诊', value: stats.value.unvisited, color: 'text-slate-500'},
    {label: '爽约', value: stats.value.noShow, color: 'text-rose-600'},
  ]
  if (stats.value.unrecognized > 0) {
    list.push({label: `未识别`, value: stats.value.unrecognized, color: 'text-slate-400'})
  }
  return list
}

// ========== 就诊详情抽屉（六段，数据全部来自真实接口） ==========
const drawer = ref(false)
const drawerRow = ref<any>(null)
const detailLoading = ref(false)
/** ③④⑤ 来自 /emr/getRecordDetailById */
const emrDetail = ref<any>(null)
/** ⑥ 收费（按患者取，再按 registId 归属本次就诊） */
const chargeList = ref<any[]>([])

const openDetail = async (row: any) => {
  drawerRow.value = row
  drawer.value = true
  emrDetail.value = null
  chargeList.value = []
  detailLoading.value = true
  try {
    // ① ② 段直接用列表行（本来就已经含挂号 + 队列全过程时间戳）
    // ③ ④ ⑤ 段：先按 registId 取病历，再按 recordId 取「病历 + 处方 + 检查 + 检验」
    const recRes = await getByRegistId({registId: row.registId})
    const rec = Array.isArray(recRes.data) ? recRes.data[0] : recRes.data
    if (rec?.id) {
      const detailRes = await getRecordDetail(String(rec.id))
      emrDetail.value = detailRes.data || null
    }
    // ⑥ 收费段：四层按患者取账单行快照，门诊一次就诊 = encounterId 即挂号ID，过滤后按账单归组成卡片
    if (row.patientId) {
      const chargeRes = await listItemsByPatient(row.patientId)
      const all = chargeRes.data || []
      const items = all.filter((c: any) => String(c.encounterId) === String(row.registId))
      chargeList.value = buildChargeCards(items)
    }
  } catch (err) {
    console.error('加载就诊详情失败:', err)
  } finally {
    detailLoading.value = false
  }
}

const record = () => emrDetail.value?.record || null
const prescriptions = () => emrDetail.value?.prescriptions || []
const inspectionApplies = () => emrDetail.value?.inspectionApplies || []
const laboratoryApplies = () => emrDetail.value?.laboratoryApplies || []

const money = (v: any) => (v == null ? '0.00' : Number(v).toFixed(2))

// 四层账单行扁平列表 → 按账单归组成卡片（一次就诊通常一张账单，仍兼容多账单）
const buildChargeCards = (items: any[]) => {
  const map = new Map<string, any>()
  for (const it of items || []) {
    const key = String(it.billId)
    if (!map.has(key)) {
      map.set(key, { id: it.billId, billNo: it.billNo, items: [], totalAmount: 0 })
    }
    const card = map.get(key)
    card.items.push(it)
    card.totalAmount = (card.totalAmount || 0) + (it.amount || 0)
  }
  return Array.from(map.values())
}

// ========== 日终结转（手工重放） ==========
// 后端每晚 00:10 自动跑一次，进门诊页面也会顺手补跑一次；这个按钮是给
// 「停诊/导数据/改完库之后重放」以及「想当场验证结转有没有落下去」用的。
// 可重入：只认还停在中间态的行，重复点第二次影响 0 条。
const settleLoading = ref(false)
const settleDialog = ref(false)
const settleDate = ref<string | null>(null)

const runSettle = async (dryRun: boolean) => {
  settleLoading.value = true
  try {
    const res = await runDayEndSettle({settleDate: settleDate.value || undefined, dryRun})
    const data = res.data || {}
    ElMessage.success(res.message || '结转完成')
    if (!dryRun) {
      settleDialog.value = false
      loadData()
    }
  } catch (err: any) {
    ElMessage.error(err.message || '日终结转失败')
  } finally {
    settleLoading.value = false
  }
}

onMounted(() => {
  loadOptions()
  loadData()
})
</script>

<template>
  <div v-loading="loading">
    <!-- 统计条（跟随筛选，口径与列表同一套 WHERE） -->
    <div class="mb-3 rounded-lg border border-slate-200 bg-white px-4 py-3 shadow-sm">
      <div class="grid grid-cols-4 gap-3 lg:grid-cols-11">
        <div v-for="c in statCards()" :key="c.label" class="text-center">
          <p :class="['text-xl font-bold leading-tight', c.color]">{{ statsLoaded ? c.value : '—' }}</p>
          <p class="mt-0.5 text-xs text-slate-500">{{ c.label }}</p>
        </div>
      </div>
      <div class="mt-2 flex flex-wrap items-center gap-x-6 gap-y-1 border-t border-slate-100 pt-2 text-xs text-slate-500">
        <span>平均候诊 <b class="text-slate-700">{{ statsLoaded ? stats.avgWaitMinutes : '—' }}</b> 分</span>
        <span>平均就诊 <b class="text-slate-700">{{ statsLoaded ? stats.avgVisitMinutes : '—' }}</b> 分</span>
        <span class="text-slate-400">（时长按「到达→开始 / 开始→结束」现算，跨天异常值已剔除）</span>
        <el-button class="ml-auto" size="small" :icon="Refresh" @click="settleDialog = true">日终结转</el-button>
      </div>
    </div>

    <!-- 日终结转：手工重放（每晚 00:10 会自动跑，这里用于补跑与当场核验） -->
    <el-dialog v-model="settleDialog" title="日终结转（给历史遗留的号收尾）" width="520px">
      <div class="space-y-2 text-sm text-slate-600">
        <p>把还停在中间态的挂号与队列收成终态：</p>
        <ul class="ml-4 list-disc space-y-1 text-xs text-slate-500">
          <li>挂了号没到院（无队列行）→ <b>爽约</b></li>
          <li>到院签到了但没被接诊 → <b>未就诊</b>，队列行 → <b>已失效</b></li>
          <li>挂号已退号/已过号但队列还挂着候诊的 → 队列跟随挂号对齐</li>
        </ul>
        <p class="text-xs text-slate-400">
          不释放号源（过去的号不会变回可卖）；可重复执行，第二次影响 0 条。
        </p>
        <el-form label-width="90px" class="pt-1">
          <el-form-item label="就诊日">
            <el-date-picker v-model="settleDate" type="date" value-format="YYYY-MM-DD"
                            placeholder="留空 = 补跑最早遗留日到昨天" clearable class="!w-64"/>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button :loading="settleLoading" @click="runSettle(true)">先试算</el-button>
        <el-button type="primary" :loading="settleLoading" @click="runSettle(false)">执行结转</el-button>
      </template>
    </el-dialog>

    <!-- 筛选区：条件全部下推后端（两卡式列表页，口径参照 views/system/user/UserView.vue） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :inline="true" label-width="70px">
        <el-form-item label="就诊日期">
          <el-date-picker
              v-model="searchForm.dateRange" type="daterange" range-separator="至"
              start-placeholder="开始日期" end-placeholder="结束日期"
              value-format="YYYY-MM-DD" class="!w-64" :shortcuts="dateShortcuts"/>
        </el-form-item>
        <el-form-item label="科室">
          <el-select
              v-model="searchForm.deptIds" multiple collapse-tags collapse-tags-tooltip
              filterable placeholder="全部科室" class="!w-56" :fit-input-width="false">
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="String(d.id)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="医生">
          <el-select v-model="searchForm.doctorId" filterable clearable placeholder="全部医生"
                     class="!w-44" :fit-input-width="false">
            <el-option v-for="e in doctorOptions" :key="e.id" :label="e.empName" :value="String(e.id)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="姓名 / 就诊号 / 门诊号 / 排队号"
                    :prefix-icon="Search" class="!w-60" clearable @keyup.enter="handleSearch"/>
        </el-form-item>

        <el-form-item label="就诊状态">
          <el-select v-model="searchForm.statusCodes" multiple collapse-tags placeholder="全部状态" class="!w-48">
            <el-option v-for="s in OPD_LOG_STATUS_OPTIONS" :key="s.value" :label="s.label" :value="s.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="号别">
          <el-select v-model="searchForm.registType" clearable placeholder="全部" class="!w-28">
            <el-option v-for="s in registTypeOptions" :key="s.value" :label="s.label" :value="s.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="结算方式">
          <el-select v-model="searchForm.settlementType" clearable placeholder="全部" class="!w-32">
            <el-option v-for="s in settlementOptions" :key="s.value" :label="s.label" :value="s.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="就诊类型">
          <el-select v-model="searchForm.revisitType" clearable placeholder="全部" class="!w-28">
            <el-option v-for="s in revisitOptions" :key="s.value" :label="s.label" :value="s.value"/>
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="rows" v-loading="loading" style="width: 100%" stripe :max-height="tableMaxHeight">
        <el-table-column prop="registNo" label="就诊号" width="160" fixed="left">
          <template #default="{ row }">
            <span class="font-mono font-bold text-blue-700">{{ row.registNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="患者" width="150" fixed="left">
          <template #default="{ row }">
            <div class="flex items-center gap-1.5">
              <span class="font-medium text-slate-800">{{ row.patientName }}</span>
              <span class="text-slate-400">{{ patientGenderText(row.gender) }}{{ row.age ? '·' + row.age : '' }}</span>
            </div>
            <div class="font-mono text-slate-400">{{ row.patientNo }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="科室" width="120" show-overflow-tooltip/>
        <el-table-column prop="doctorName" label="接诊医生" width="130" show-overflow-tooltip/>
        <el-table-column label="号别" width="86" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.registType != null" :type="statusOf(REGIST_TYPE, row.registType).tagType"
                    effect="plain" size="small">{{ statusOf(REGIST_TYPE, row.registType).label }}</el-tag>
            <span v-else class="text-slate-400">-</span>
          </template>
        </el-table-column>
        <el-table-column label="就诊类型" width="86" align="center">
          <template #default="{ row }">
            <span :class="['rounded px-1.5 py-0.5', row.revisitType != null ? 'bg-slate-100 text-slate-600' : 'text-slate-400']">
              {{ row.revisitType != null ? statusOf(REVISIT_TYPE, row.revisitType).label : '未标注' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="来源" width="76" align="center">
          <template #default="{ row }">
            <span class="text-slate-500">{{ row.registSource != null ? statusOf(REGIST_SOURCE, row.registSource).label : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="就诊日期" width="104" align="center">
          <template #default="{ row }"><span class="text-slate-600">{{ row.visitDate || '-' }}</span></template>
        </el-table-column>
        <el-table-column label="排队号" width="84" align="center">
          <template #default="{ row }">
            <span v-if="row.queueNo" class="rounded bg-blue-50 px-1.5 py-0.5 font-mono text-blue-700">{{ shortQueueNo(row.queueNo) }}</span>
            <span v-else class="text-slate-300">未签到</span>
          </template>
        </el-table-column>
        <el-table-column label="签到到达" width="132">
          <template #default="{ row }"><span class="text-slate-600">{{ fmt(row.arriveTime) }}</span></template>
        </el-table-column>
        <el-table-column label="开始就诊" width="132">
          <template #default="{ row }"><span class="text-slate-600">{{ fmt(row.startTime) }}</span></template>
        </el-table-column>
        <el-table-column label="结束" width="132">
          <template #default="{ row }"><span class="text-slate-600">{{ fmt(row.endTime) }}</span></template>
        </el-table-column>
        <el-table-column label="候诊" width="70" align="center">
          <template #default="{ row }"><span class="text-slate-600">{{ duration(row.waitMinutes) }}</span></template>
        </el-table-column>
        <el-table-column label="就诊时长" width="76" align="center">
          <template #default="{ row }"><span class="text-slate-600">{{ duration(row.visitMinutes) }}</span></template>
        </el-table-column>
        <el-table-column label="结算" width="90" align="center">
          <template #default="{ row }">
            <span class="text-slate-600">{{ row.settlementType != null ? statusOf(SETTLEMENT_TYPE, row.settlementType).label : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="96" fixed="right">
          <template #default="{ row }">
            <el-tag :type="logStatusStyle(row).tagType" effect="plain" size="small">
              {{ row.logStatusLabel || logStatusStyle(row).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" :icon="View" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-8 text-sm text-slate-400">该筛选条件下没有就诊记录</div>
        </template>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"/>
      </div>
    </el-card>

    <!-- 就诊详情抽屉：六段 -->
    <el-drawer v-model="drawer" title="就诊详情" size="62%" destroy-on-close>
      <div v-loading="detailLoading" class="space-y-4">
        <template v-if="drawerRow">
          <!-- ① 挂号信息 -->
          <section class="rounded-lg border border-slate-200 p-3">
            <h4 class="mb-2 flex items-center gap-1.5 text-sm font-semibold text-slate-800">
              <el-icon><Document/></el-icon>挂号信息
            </h4>
            <div class="grid grid-cols-4 gap-x-4 gap-y-2 text-xs">
              <div><p class="text-slate-400">就诊号</p><p class="font-mono font-medium text-slate-800">{{ drawerRow.registNo }}</p></div>
              <div><p class="text-slate-400">患者</p><p class="font-medium text-slate-800">{{ drawerRow.patientName }}（{{ drawerRow.patientNo }}）</p></div>
              <div><p class="text-slate-400">性别 / 年龄</p><p class="text-slate-700">{{ patientGenderText(drawerRow.gender) }} / {{ drawerRow.age ?? '-' }} 岁</p></div>
              <div><p class="text-slate-400">号别</p><p class="text-slate-700">{{ drawerRow.registType != null ? statusOf(REGIST_TYPE, drawerRow.registType).label : '-' }}</p></div>
              <div><p class="text-slate-400">就诊日期</p><p class="text-slate-700">{{ drawerRow.visitDate || '-' }}</p></div>
              <div><p class="text-slate-400">挂号时间</p><p class="text-slate-700">{{ fmt(drawerRow.registTime) }}</p></div>
              <div><p class="text-slate-400">科室 / 医生</p><p class="text-slate-700">{{ drawerRow.deptName }} / {{ drawerRow.doctorName }}</p></div>
              <div><p class="text-slate-400">来源 / 结算</p><p class="text-slate-700">
                {{ drawerRow.registSource != null ? statusOf(REGIST_SOURCE, drawerRow.registSource).label : '-' }} /
                {{ drawerRow.settlementType != null ? statusOf(SETTLEMENT_TYPE, drawerRow.settlementType).label : '-' }}
              </p></div>
              <div><p class="text-slate-400">就诊类型</p><p class="text-slate-700">{{ drawerRow.revisitType != null ? statusOf(REVISIT_TYPE, drawerRow.revisitType).label : '未标注' }}</p></div>
              <div><p class="text-slate-400">当前状态</p>
                <el-tag :type="logStatusStyle(drawerRow).tagType" effect="plain" size="small">{{ drawerRow.logStatusLabel || logStatusStyle(drawerRow).label }}</el-tag>
              </div>
            </div>
          </section>

          <!-- ② 队列过程 -->
          <section class="rounded-lg border border-slate-200 p-3">
            <h4 class="mb-2 text-sm font-semibold text-slate-800">队列过程</h4>
            <div v-if="drawerRow.queueId" class="grid grid-cols-4 gap-x-4 gap-y-2 text-xs">
              <div><p class="text-slate-400">排队号 / 序号</p><p class="font-mono text-slate-800">{{ shortQueueNo(drawerRow.queueNo) }} / {{ drawerRow.sequenceNo ?? '-' }}</p></div>
              <div><p class="text-slate-400">签到到达</p><p class="text-slate-700">{{ fmt(drawerRow.arriveTime) }}</p></div>
              <div><p class="text-slate-400">叫号</p><p class="text-slate-700">{{ fmt(drawerRow.callTime) }}（{{ drawerRow.callCount ?? 0 }} 次）</p></div>
              <div><p class="text-slate-400">开始就诊</p><p class="text-slate-700">{{ fmt(drawerRow.startTime) }}</p></div>
              <div><p class="text-slate-400">结束</p><p class="text-slate-700">{{ fmt(drawerRow.endTime) }}</p></div>
              <div><p class="text-slate-400">候诊时长</p><p class="text-slate-700">{{ duration(drawerRow.waitMinutes) }}</p></div>
              <div><p class="text-slate-400">就诊时长</p><p class="text-slate-700">{{ duration(drawerRow.visitMinutes) }}</p></div>
              <div><p class="text-slate-400">过号</p><p class="text-slate-700">
                {{ drawerRow.isOverdue === 1 ? `已过号 · ${fmt(drawerRow.overdueTime)}${drawerRow.overdueReason ? ' · ' + drawerRow.overdueReason : ''}` : '否' }}
              </p></div>
            </div>
            <div v-else class="py-3 text-xs text-slate-400">该挂号还未签到入队（无队列记录）</div>
          </section>

          <!-- ③ 病历摘要 -->
          <section class="rounded-lg border border-slate-200 p-3">
            <h4 class="mb-2 text-sm font-semibold text-slate-800">病历摘要</h4>
            <div v-if="record()" class="space-y-2 text-xs">
              <div class="grid grid-cols-3 gap-x-4 gap-y-2">
                <div><p class="text-slate-400">主诉</p><p class="whitespace-pre-wrap text-slate-700">{{ record().chiefComplaint || '-' }}</p></div>
                <div><p class="text-slate-400">现病史</p><p class="whitespace-pre-wrap text-slate-700">{{ record().presentIllness || '-' }}</p></div>
                <div><p class="text-slate-400">既往史</p><p class="whitespace-pre-wrap text-slate-700">{{ record().pastHistory || '-' }}</p></div>
                <div><p class="text-slate-400">体格检查</p><p class="whitespace-pre-wrap text-slate-700">{{ record().physicalExamination || '-' }}</p></div>
                <div><p class="text-slate-400">初步诊断</p><p class="whitespace-pre-wrap text-slate-700">{{ record().diagnosis || '-' }}</p></div>
                <div><p class="text-slate-400">处理意见</p><p class="whitespace-pre-wrap text-slate-700">{{ record().treatmentPlan || record().advice || '-' }}</p></div>
              </div>
              <div class="flex flex-wrap gap-x-4 gap-y-1 border-t border-slate-100 pt-2 text-slate-500">
                <span>病历号 <b class="font-mono text-slate-700">{{ record().recordNo || '-' }}</b></span>
                <span>状态 <b class="text-slate-700">{{ record().recordStatus ?? '-' }}</b></span>
                <span>书写 <b class="text-slate-700">{{ fmt(record().createTime) }}</b></span>
                <span>提交 <b class="text-slate-700">{{ fmt(record().submitTime) }}</b></span>
              </div>
            </div>
            <div v-else class="py-3 text-xs text-slate-400">本次就诊还没有病历记录</div>
          </section>

          <!-- ④ 处方 -->
          <section class="rounded-lg border border-slate-200 p-3">
            <h4 class="mb-2 text-sm font-semibold text-slate-800">处方（{{ prescriptions().length }}）</h4>
            <div v-if="prescriptions().length" class="space-y-3">
              <div v-for="p in prescriptions()" :key="p.id" class="rounded border border-slate-100 bg-slate-50/60 p-2">
                <div class="mb-1 flex flex-wrap items-center gap-x-3 text-xs text-slate-600">
                  <span class="font-mono font-medium text-slate-800">{{ p.prescriptionNo }}</span>
                  <span>{{ p.diagnosis || '无诊断' }}</span>
                  <span>金额 <b class="text-slate-800">¥{{ money(p.totalAmount) }}</b></span>
                  <el-tag size="small" effect="plain" :type="statusOf(APPLY_STATUS, p.prescriptionStatus).tagType">
                    {{ statusOf(APPLY_STATUS, p.prescriptionStatus).label }}
                  </el-tag>
                </div>
                <el-table :data="p.details || []" size="small" border>
                  <el-table-column prop="drugName" label="药品" min-width="140"/>
                  <el-table-column prop="specification" label="规格" width="110"/>
                  <el-table-column prop="quantity" label="数量" width="70" align="center"/>
                  <el-table-column prop="unit" label="单位" width="60" align="center"/>
                  <el-table-column prop="singleDosage" label="单次剂量" width="90" align="center"/>
                  <el-table-column prop="frequency" label="频次" width="80" align="center"/>
                  <el-table-column prop="route" label="用法" width="80" align="center"/>
                  <el-table-column label="金额" width="80" align="right">
                    <template #default="{ row: d }">¥{{ money(d.amount) }}</template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
            <div v-else class="py-3 text-xs text-slate-400">本次就诊没有处方</div>
          </section>

          <!-- ⑤ 检查检验 -->
          <section class="rounded-lg border border-slate-200 p-3">
            <h4 class="mb-2 text-sm font-semibold text-slate-800">检查 / 检验申请</h4>
            <div class="space-y-3">
              <div>
                <p class="mb-1 text-xs font-medium text-slate-500">检查（{{ inspectionApplies().length }}）</p>
                <el-table v-if="inspectionApplies().length" :data="inspectionApplies()" size="small" border>
                  <el-table-column prop="applyNo" label="申请单号" width="150"/>
                  <el-table-column prop="inspectionItemName" label="检查项目" min-width="140"/>
                  <el-table-column prop="bodyPart" label="检查部位" width="100"/>
                  <el-table-column prop="clinicalDiagnosis" label="临床诊断" min-width="120" show-overflow-tooltip/>
                  <el-table-column label="状态" width="90" align="center">
                    <template #default="{ row: a }">
                      <el-tag size="small" effect="plain" :type="statusOf(APPLY_STATUS, a.applyStatus).tagType">
                        {{ statusOf(APPLY_STATUS, a.applyStatus).label }}
                      </el-tag>
                    </template>
                  </el-table-column>
                </el-table>
                <p v-else class="py-2 text-xs text-slate-400">无检查申请</p>
              </div>
              <div>
                <p class="mb-1 text-xs font-medium text-slate-500">检验（{{ laboratoryApplies().length }}）</p>
                <el-table v-if="laboratoryApplies().length" :data="laboratoryApplies()" size="small" border>
                  <el-table-column prop="applyNo" label="申请单号" width="150"/>
                  <el-table-column prop="laboratoryItemName" label="检验项目" min-width="140"/>
                  <el-table-column prop="specimenType" label="标本" width="90"/>
                  <el-table-column prop="clinicalDiagnosis" label="临床诊断" min-width="120" show-overflow-tooltip/>
                  <el-table-column label="状态" width="90" align="center">
                    <template #default="{ row: a }">
                      <el-tag size="small" effect="plain" :type="statusOf(APPLY_STATUS, a.applyStatus).tagType">
                        {{ statusOf(APPLY_STATUS, a.applyStatus).label }}
                      </el-tag>
                    </template>
                  </el-table-column>
                </el-table>
                <p v-else class="py-2 text-xs text-slate-400">无检验申请</p>
              </div>
            </div>
          </section>

          <!-- ⑥ 收费 -->
          <section class="rounded-lg border border-slate-200 p-3">
            <h4 class="mb-2 text-sm font-semibold text-slate-800">收费记录（{{ chargeList.length }}）</h4>
            <div v-if="chargeList.length" class="space-y-3">
              <div v-for="c in chargeList" :key="c.id" class="rounded border border-slate-100 p-2">
                <div class="mb-1 flex flex-wrap items-center gap-x-3 text-xs text-slate-600">
                  <span class="font-mono font-medium text-slate-800">{{ c.billNo }}</span>
                  <span>总额 <b class="text-slate-800">¥{{ money(c.totalAmount) }}</b></span>
                </div>
                <el-table :data="c.items || []" size="small" border>
                  <el-table-column prop="itemName" label="项目" min-width="140"/>
                  <el-table-column prop="itemCode" label="编码" width="120"/>
                  <el-table-column prop="specification" label="规格" width="110"/>
                  <el-table-column prop="quantity" label="数量" width="70" align="center"/>
                  <el-table-column label="金额" width="90" align="right">
                    <template #default="{ row: d }">¥{{ money(d.amount) }}</template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
            <div v-else class="py-3 text-xs text-slate-400">本次就诊没有收费记录</div>
          </section>
        </template>
      </div>
    </el-drawer>
  </div>
</template>
