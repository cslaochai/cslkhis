<script setup lang="ts">
// 财务班结 / 日结 / 三级对账（G8）
//
// 口径要点（写在页面里是因为这里最容易看错）：
//   1) 班结单（JS 开头）是「凭证」——金额是交班那一刻的定格值，之后补录的收费不会改它。
//      要看实时的去查收费单，别拿交班单当实时数。
//   2) 统计区间是**滚动的**：上次交班时刻 → 本次交班时刻，不是固定 08:00-16:00。
//   3) 页面上的筛选一律下推后端，禁止对当前页 list.filter（翻页后结果不全）。
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh, View, Plus, Select, Wallet, Tickets } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  handover,
  cashierListPage,
  getCashierById,
  settlementStatusCount,
  runDaySettlement,
  dayListPage,
  getDayDetailById,
  getDayDetailByDate,
  dayAudit,
} from '@/api/financeSettlement'
import {
  SHIFT_TYPE_OPTIONS,
  CASHIER_SETTLE_STATUS_OPTIONS,
  DAY_SETTLE_STATUS_OPTIONS,
  RECONCILE_STATUS_OPTIONS,
  shiftTypeText,
  cashierStatusText,
  cashierStatusTagClass,
  dayStatusText,
  dayStatusTagClass,
  reconcileText,
  reconcileTagClass,
} from '@/lib/financeSettlement'
import { formatMoney } from '@/lib/utils'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'

/** 本地日期 yyyy-MM-dd。不能用 toISOString()：UTC+8 早上 8 点前会取到昨天。 */
function localDateStr(d = new Date()) {
  const m = `${d.getMonth() + 1}`.padStart(2, '0')
  const day = `${d.getDate()}`.padStart(2, '0')
  return `${d.getFullYear()}-${m}-${day}`
}

const activeTab = ref<'cashier' | 'day'>('cashier')

// ---------------------------------------------------------------------------
// 状态卡
// ---------------------------------------------------------------------------
const countLoading = ref(true)
const counts = ref<any>({})

async function loadCounts() {
  countLoading.value = true
  try {
    const res = await settlementStatusCount()
    counts.value = res.data || {}
  } catch (e: any) {
    counts.value = {}
    ElMessage.error(e?.message || '加载财务状态统计失败')
  } finally {
    countLoading.value = false
  }
}

// ---------------------------------------------------------------------------
// 交班单列表
// ---------------------------------------------------------------------------
const cashierLoading = ref(true) // 「加载中 ≠ 没有」：初值 true，未加载完不显示「暂无」
const cashierRows = ref<any[]>([])
const cashierQuery = reactive({ settleStatus: null as number | null, dateRange: [] as string[] })
const cashierPage = reactive({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0 })

async function loadCashierList() {
  cashierLoading.value = true
  try {
    const res = await cashierListPage({
      settleStatus: cashierQuery.settleStatus === null ? undefined : cashierQuery.settleStatus,
      dateStart: cashierQuery.dateRange?.[0] || undefined,
      dateEnd: cashierQuery.dateRange?.[1] || undefined,
      pageNum: cashierPage.pageNum,
      pageSize: cashierPage.pageSize,
    })
    const data = res.data || {}
    cashierRows.value = data.records || []
    cashierPage.total = Number(data.total || 0)
  } catch (e: any) {
    cashierRows.value = []
    cashierPage.total = 0
    ElMessage.error(e?.message || '加载交班单失败')
  } finally {
    cashierLoading.value = false
  }
}

// ---------------------------------------------------------------------------
// 日结单列表
// ---------------------------------------------------------------------------
const dayLoading = ref(true)
const dayRows = ref<any[]>([])
const dayQuery = reactive({
  settleStatus: null as number | null,
  reconcileStatus: null as number | null,
  dateRange: [] as string[],
})
const dayPage = reactive({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0 })

async function loadDayList() {
  dayLoading.value = true
  try {
    const res = await dayListPage({
      settleStatus: dayQuery.settleStatus === null ? undefined : dayQuery.settleStatus,
      reconcileStatus: dayQuery.reconcileStatus === null ? undefined : dayQuery.reconcileStatus,
      dateStart: dayQuery.dateRange?.[0] || undefined,
      dateEnd: dayQuery.dateRange?.[1] || undefined,
      pageNum: dayPage.pageNum,
      pageSize: dayPage.pageSize,
    })
    const data = res.data || {}
    dayRows.value = data.records || []
    dayPage.total = Number(data.total || 0)
  } catch (e: any) {
    dayRows.value = []
    dayPage.total = 0
    ElMessage.error(e?.message || '加载日结单失败')
  } finally {
    dayLoading.value = false
  }
}

function searchCashier() {
  cashierPage.pageNum = 1
  loadCashierList()
}
function resetCashier() {
  cashierQuery.settleStatus = null
  cashierQuery.dateRange = []
  searchCashier()
}
function searchDay() {
  dayPage.pageNum = 1
  loadDayList()
}
function resetDay() {
  dayQuery.settleStatus = null
  dayQuery.reconcileStatus = null
  dayQuery.dateRange = []
  searchDay()
}

// ---------------------------------------------------------------------------
// 交班（班结）
// ---------------------------------------------------------------------------
const handoverVisible = ref(false)
const handoverSubmitting = ref(false)
const handoverForm = reactive({
  shiftType: 1 as number | null,
  handinCash: null as number | null,
  diffReason: '',
  remark: '',
})

function openHandover() {
  handoverForm.shiftType = 1
  handoverForm.handinCash = null
  handoverForm.diffReason = ''
  handoverForm.remark = ''
  handoverVisible.value = true
}

async function submitHandover() {
  if (handoverForm.shiftType === null) {
    ElMessage.warning('请选择班次')
    return
  }
  if (handoverForm.handinCash === null || Number(handoverForm.handinCash) < 0) {
    ElMessage.warning('请填写实际清点并上交的现金金额')
    return
  }
  handoverSubmitting.value = true
  try {
    const res = await handover({
      shiftType: handoverForm.shiftType,
      handinCash: handoverForm.handinCash,
      diffReason: handoverForm.diffReason.trim() || undefined,
      remark: handoverForm.remark.trim() || undefined,
      // cashierId 不传：服务端取登录态，前端传「我是谁」等于让交班单能挂到别人名下
    })
    const vo = res.data || {}
    const diff = Number(vo.cashDiff ?? 0)
    if (diff === 0) {
      ElMessage.success(`交班成功（${vo.settlementNo || ''}）现金账实相符`)
    } else {
      ElMessage.warning(
          `交班成功（${vo.settlementNo || ''}）现金差异 ${diff > 0 ? '+' : ''}${formatMoney(diff)} 元，已留痕`,
      )
    }
    handoverVisible.value = false
    cashierPage.pageNum = 1
    await Promise.all([loadCashierList(), loadCounts()])
  } catch (e: any) {
    ElMessage.error(e?.message || '交班失败')
  } finally {
    handoverSubmitting.value = false
  }
}

// 交班单详情
const cashierDetailVisible = ref(false)
const cashierDetailLoading = ref(false)
const cashierDetail = ref<any>(null)

async function openCashierDetail(row: any) {
  cashierDetailVisible.value = true
  cashierDetailLoading.value = true
  cashierDetail.value = row
  try {
    const res = await getCashierById(row.id)
    if (res.data) cashierDetail.value = res.data
  } catch (e: any) {
    ElMessage.warning(e?.message || '获取交班单详情失败，已展示列表数据')
  } finally {
    cashierDetailLoading.value = false
  }
}

// ---------------------------------------------------------------------------
// 执行日结（含三级对账预览）
// ---------------------------------------------------------------------------
const runVisible = ref(false)
const runSubmitting = ref(false)
const runPreviewLoading = ref(false)
const runDetail = ref<any>(null)
const runForm = reactive({
  settleDate: localDateStr(),
  allowDiff: false,
  remark: '',
})

const runReconcile = computed(() => runDetail.value?.reconcile || null)
const runPassed = computed(() => runReconcile.value?.passed === true)

async function loadRunPreview() {
  if (!runForm.settleDate) return
  runPreviewLoading.value = true
  runDetail.value = null
  try {
    const res = await getDayDetailByDate(runForm.settleDate)
    runDetail.value = res.data || null
    // 有差异时默认不勾放行 —— 逼人先看清楚差在哪，而不是顺手点过去
    if (res.data?.reconcile?.passed !== true) {
      runForm.allowDiff = false
    }
  } catch (e: any) {
    runDetail.value = null
    ElMessage.error(e?.message || '加载对账预览失败')
  } finally {
    runPreviewLoading.value = false
  }
}

function openRun() {
  runForm.settleDate = localDateStr()
  runForm.allowDiff = false
  runForm.remark = ''
  runVisible.value = true
  loadRunPreview()
}

async function submitRun() {
  if (!runForm.settleDate) {
    ElMessage.warning('请选择日结日期')
    return
  }
  if (!runPassed.value && !runForm.allowDiff) {
    ElMessage.warning('对账未平。确认差异无误请勾选「放行差异」并填写理由')
    return
  }
  if (!runPassed.value && !runForm.remark.trim()) {
    ElMessage.warning('放行差异必须填写理由')
    return
  }
  runSubmitting.value = true
  try {
    const res = await runDaySettlement({ ...runForm })
    const vo = res.data || {}
    if (vo.reconcileStatus === 2) {
      ElMessage.warning(`日结完成（${vo.settlementNo}）对账有差异，已记录差异说明`)
    } else {
      ElMessage.success(`日结完成（${vo.settlementNo}）三级对账全部通过`)
    }
    runVisible.value = false
    activeTab.value = 'day'
    dayPage.pageNum = 1
    await Promise.all([loadDayList(), loadCounts()])
    if (vo.id) openDayDetail({ id: vo.id })
  } catch (e: any) {
    ElMessage.error(e?.message || '日结失败')
  } finally {
    runSubmitting.value = false
  }
}

// ---------------------------------------------------------------------------
// 日结单详情（日结单 + 三级对账 + 当日交班单 + 科室收入）
// ---------------------------------------------------------------------------
const dayDetailVisible = ref(false)
const dayDetailLoading = ref(false)
const dayDetail = ref<any>(null)

const detailReconcile = computed(() => dayDetail.value?.reconcile || null)
const detailShifts = computed(() => dayDetail.value?.shifts || [])
const detailDepts = computed(() => dayDetail.value?.deptIncomes || [])
const detailSettlement = computed(() => dayDetail.value?.settlement || null)
/** 尚未日结时 settlement 为 null，此时下面几块是试算结果，必须在界面上说清楚 */
const isTrial = computed(() => !!dayDetail.value && !dayDetail.value.settlement)

async function openDayDetail(row: any) {
  dayDetailVisible.value = true
  dayDetailLoading.value = true
  dayDetail.value = null
  try {
    const res = row?.id ? await getDayDetailById(row.id) : await getDayDetailByDate(row.settleDate)
    dayDetail.value = res.data || null
  } catch (e: any) {
    ElMessage.error(e?.message || '加载日结详情失败')
  } finally {
    dayDetailLoading.value = false
  }
}

const audited = computed(() => detailSettlement.value?.settleStatus === 2)

async function handleAudit() {
  const s = detailSettlement.value
  if (!s) return
  let remark = ''
  try {
    const r = await ElMessageBox.prompt(
        `确认审核日结单 ${s.settlementNo}（当日实收 ¥${formatMoney(s.chargeAmount)}）？审核后不可重算。`,
        '审核日结单',
        {
          type: 'warning',
          confirmButtonText: '确认审核',
          cancelButtonText: '取消',
          inputPlaceholder: '审核意见（选填）',
          inputValue: '',
        },
    )
    remark = r.value || ''
  } catch {
    return
  }
  try {
    await dayAudit({ id: s.id, auditRemark: remark || undefined })
    ElMessage.success('日结单已审核')
    await openDayDetail({ id: s.id })
    await Promise.all([loadDayList(), loadCounts(), loadCashierList()])
  } catch (e: any) {
    ElMessage.error(e?.message || '审核失败')
  }
}

onMounted(async () => {
  await loadCounts()
  await Promise.all([loadCashierList(), loadDayList()])
})
</script>

<template>
  <div class="space-y-6">
    <!-- 页头 -->
    <div class="flex items-start justify-between">
      <div>
        <h1 class="text-2xl font-bold text-slate-900">财务日结 / 三级对账</h1>
        <p class="mt-1 text-sm text-slate-500">
          收费员交班（班结）→ 院级日结 → 三级对账；班结单是定格凭证，日结一经审核不可重算
        </p>
      </div>
      <div class="flex items-center gap-2">
        <el-button v-perm="'finance:settlement:edit'" :icon="Wallet" data-testid="fin-handover-btn" @click="openHandover">办理交班</el-button>
        <el-button v-perm="'finance:settlement:edit'" type="primary" :icon="Tickets" data-testid="fin-run-day-btn" @click="openRun">
          执行日结
        </el-button>
      </div>
    </div>

    <!-- 状态卡 -->
    <div v-loading="countLoading" class="grid grid-cols-2 gap-4 lg:grid-cols-4">
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm" data-testid="fin-card-pending">
        <div class="text-sm text-slate-500">待日结交班单</div>
        <div class="mt-1 text-2xl font-bold text-amber-600">
          {{ countLoading ? '—' : (counts.cashierPending ?? 0) }}
        </div>
        <div class="mt-1 text-xs text-slate-400">
          涉及金额 ¥{{ formatMoney(counts.cashierPendingAmount) }}
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm" data-testid="fin-card-settled">
        <div class="text-sm text-slate-500">已日结 / 已审核交班单</div>
        <div class="mt-1 text-2xl font-bold text-blue-600">
          {{ countLoading ? '—' : (counts.cashierSettled ?? 0) }}
          <span class="text-lg text-slate-400">
            / {{ countLoading ? '—' : (counts.cashierAudited ?? 0) }}
          </span>
        </div>
        <div class="mt-1 text-xs text-slate-400">按收费员班级统计</div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm" data-testid="fin-card-day-pending">
        <div class="text-sm text-slate-500">待审核日结单</div>
        <div class="mt-1 text-2xl font-bold text-amber-600">
          {{ countLoading ? '—' : (counts.dayPendingAudit ?? 0) }}
        </div>
        <div class="mt-1 text-xs text-slate-400">
          已审核 {{ countLoading ? '—' : (counts.dayAudited ?? 0) }} 张
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm" data-testid="fin-card-diff">
        <div class="text-sm text-slate-500">对账有差异的日结单</div>
        <div
            class="mt-1 text-2xl font-bold"
            :class="Number(counts.dayWithDiff || 0) > 0 ? 'text-rose-600' : 'text-emerald-600'"
        >
          {{ countLoading ? '—' : (counts.dayWithDiff ?? 0) }}
        </div>
        <div class="mt-1 text-xs text-slate-400">
          {{ Number(counts.dayWithDiff || 0) > 0 ? '需人工核查差异原因' : '当前无差异单据' }}
        </div>
      </div>
    </div>

    <!-- 两个台账 -->
    <el-tabs v-model="activeTab" class="rounded-lg border border-slate-200 bg-white px-4 shadow-sm">
      <!-- ============ 交班单 ============ -->
      <el-tab-pane name="cashier">
        <template #label>
          <span data-testid="fin-tab-cashier">交班单（班结）</span>
        </template>
        <div class="flex flex-wrap items-center gap-3 pb-3">
          <el-select
              v-model="cashierQuery.settleStatus"
              placeholder="状态"
              clearable
              class="!w-36"
              data-testid="fin-cashier-status"
          >
            <el-option
                v-for="o in CASHIER_SETTLE_STATUS_OPTIONS"
                :key="o.value"
                :label="o.label"
                :value="o.value"
            />
          </el-select>
          <el-date-picker
              v-model="cashierQuery.dateRange"
              type="daterange"
              value-format="YYYY-MM-DD"
              start-placeholder="交班日期起"
              end-placeholder="止"
              class="!w-72"
          />
          <el-button type="primary" :icon="Search" @click="searchCashier">查询</el-button>
          <el-button :icon="Refresh" @click="resetCashier">重置</el-button>
          <span class="ml-auto text-sm text-slate-500">
            共 <span class="font-semibold text-slate-700">{{ cashierLoading ? '—' : cashierPage.total }}</span> 张交班单
          </span>
        </div>

        <el-table v-loading="cashierLoading" :data="cashierRows" style="width: 100%" data-testid="fin-cashier-table">
          <el-table-column prop="settlementNo" label="交班单号" min-width="150" class-name="font-mono text-xs" />
          <el-table-column label="收费员" width="100">
            <template #default="{ row }">{{ row.cashierName || '—' }}</template>
          </el-table-column>
          <el-table-column label="班次" width="80">
            <template #default="{ row }">{{ shiftTypeText(row.shiftType) }}</template>
          </el-table-column>
          <el-table-column label="统计区间" min-width="290">
            <template #default="{ row }">
              <span class="font-mono text-xs text-slate-600">
                {{ row.periodBegin || '—' }} ~ {{ row.periodEnd || '—' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="收费" width="130" align="right">
            <template #default="{ row }">
              <div>¥{{ formatMoney(row.chargeAmount) }}</div>
              <div class="text-xs text-slate-400">{{ row.chargeCount ?? 0 }} 笔</div>
            </template>
          </el-table-column>
          <el-table-column label="退费" width="130" align="right">
            <template #default="{ row }">
              <div>¥{{ formatMoney(row.refundAmount) }}</div>
              <div class="text-xs text-slate-400">{{ row.refundCount ?? 0 }} 笔</div>
            </template>
          </el-table-column>
          <el-table-column label="净额" width="120" align="right">
            <template #default="{ row }">
              <span class="font-semibold text-slate-800">¥{{ formatMoney(row.netAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="现金（系统/实交）" width="170" align="right">
            <template #default="{ row }">
              <div>¥{{ formatMoney(row.cashAmount) }} / ¥{{ formatMoney(row.handinCash) }}</div>
              <div
                  class="text-xs"
                  :class="Number(row.cashDiff || 0) === 0 ? 'text-emerald-600' : 'text-rose-600'"
              >
                差异 {{ Number(row.cashDiff || 0) > 0 ? '+' : '' }}{{ formatMoney(row.cashDiff) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="票据(开/废)" width="110" align="right">
            <template #default="{ row }">
              {{ row.invoiceCount ?? 0 }} / {{ row.invoiceVoidCount ?? 0 }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="96">
            <template #default="{ row }">
              <el-tag :class="cashierStatusTagClass(row.settleStatus)" effect="plain" size="small" class="border">
                {{ cashierStatusText(row.settleStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" :icon="View" @click="openCashierDetail(row)">
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div
            v-if="!cashierLoading && cashierRows.length === 0"
            class="py-12 text-center text-sm text-slate-400"
        >
          没有符合条件的交班单
        </div>
        <div class="mt-4 flex justify-end">
          <el-pagination
              v-model:current-page="cashierPage.pageNum"
              v-model:page-size="cashierPage.pageSize"
              :page-sizes="PAGE_SIZES"
              :total="cashierPage.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="searchCashier"
              @current-change="loadCashierList"
          />
        </div>
      </el-tab-pane>

      <!-- ============ 日结单 ============ -->
      <el-tab-pane name="day">
        <template #label>
          <span data-testid="fin-tab-day">日结单（院级）</span>
        </template>
        <div class="flex flex-wrap items-center gap-3 pb-3">
          <el-select
              v-model="dayQuery.settleStatus"
              placeholder="状态"
              clearable
              class="!w-32"
              data-testid="fin-day-status"
          >
            <el-option v-for="o in DAY_SETTLE_STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
          <el-select
              v-model="dayQuery.reconcileStatus"
              placeholder="对账结论"
              clearable
              class="!w-32"
              data-testid="fin-day-reconcile"
          >
            <el-option v-for="o in RECONCILE_STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
          <el-date-picker
              v-model="dayQuery.dateRange"
              type="daterange"
              value-format="YYYY-MM-DD"
              start-placeholder="日期起"
              end-placeholder="止"
              class="!w-72"
          />
          <el-button type="primary" :icon="Search" @click="searchDay">查询</el-button>
          <el-button :icon="Refresh" @click="resetDay">重置</el-button>
          <span class="ml-auto text-sm text-slate-500">
            共 <span class="font-semibold text-slate-700">{{ dayLoading ? '—' : dayPage.total }}</span> 张日结单
          </span>
        </div>

        <el-table v-loading="dayLoading" :data="dayRows" style="width: 100%" data-testid="fin-day-table">
          <el-table-column prop="settlementNo" label="日结单号" min-width="140" class-name="font-mono text-xs" />
          <el-table-column prop="settleDate" label="日期" width="110">
            <template #default="{ row }">{{ row.settleDate || '—' }}</template>
          </el-table-column>
          <el-table-column label="班结单" width="84" align="right">
            <template #default="{ row }">{{ row.shiftCount ?? 0 }} 张</template>
          </el-table-column>
          <el-table-column label="当日实收" width="140" align="right">
            <template #default="{ row }">
              <div>¥{{ formatMoney(row.chargeAmount) }}</div>
              <div class="text-xs text-slate-400">{{ row.chargeCount ?? 0 }} 笔 / {{ row.billCount ?? 0 }} 张账单</div>
            </template>
          </el-table-column>
          <el-table-column label="Σ交班单" width="130" align="right">
            <template #default="{ row }">
              <span
                  class="font-mono"
                  :class="Number(row.reconcileStatus || 0) === 1 ? 'text-emerald-600' : 'text-rose-600'"
              >
                ¥{{ formatMoney(row.detailAmount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="统筹记账" width="120" align="right">
            <template #default="{ row }">¥{{ formatMoney(row.poolAmount) }}</template>
          </el-table-column>
          <el-table-column label="退费" width="120" align="right">
            <template #default="{ row }">¥{{ formatMoney(row.refundAmount) }}</template>
          </el-table-column>
          <el-table-column label="净额" width="130" align="right">
            <template #default="{ row }">
              <span class="font-semibold text-slate-800">¥{{ formatMoney(row.netAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="最大差异" width="120" align="right">
            <template #default="{ row }">
              <span :class="Number(row.diffAmount || 0) === 0 ? 'text-emerald-600' : 'text-rose-600'">
                ¥{{ formatMoney(row.diffAmount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="对账" width="86">
            <template #default="{ row }">
              <el-tag :class="reconcileTagClass(row.reconcileStatus)" effect="plain" size="small" class="border">
                {{ reconcileText(row.reconcileStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="86">
            <template #default="{ row }">
              <el-tag :class="dayStatusTagClass(row.settleStatus)" effect="plain" size="small" class="border">
                {{ dayStatusText(row.settleStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="130" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" :icon="View" @click="openDayDetail(row)">详情</el-button>
              <el-button
                  v-if="row.settleStatus === 1"
                  link
                  type="success"
                  size="small"
                  :icon="Select"
                  @click="openDayDetail(row)"
              >
                审核
              </el-button>
            </template>
          </el-table-column>        </el-table>
        <div v-if="!dayLoading && dayRows.length === 0" class="py-12 text-center text-sm text-slate-400">
          没有符合条件的日结单
        </div>
        <div class="mt-4 flex justify-end">
          <el-pagination
              v-model:current-page="dayPage.pageNum"
              v-model:page-size="dayPage.pageSize"
              :page-sizes="PAGE_SIZES"
              :total="dayPage.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="searchDay"
              @current-change="loadDayList"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- ==================== 交班 ==================== -->
    <el-dialog v-model="handoverVisible" title="办理交班（班结）" width="560px" destroy-on-close>
      <div data-testid="fin-handover-dialog" class="space-y-4">
        <div class="rounded-lg border border-amber-200 bg-amber-50 p-3 text-xs text-amber-800">
          交班单是定格凭证：金额按「上次交班时刻 → 现在」这个区间汇总，
          生成后不随后续补录变动。班次只是展示标签，不影响统计区间。
        </div>
        <el-form label-width="96px">
          <el-form-item label="班次" required>
            <el-select v-model="handoverForm.shiftType" class="w-full" data-testid="fin-handover-shift">
              <el-option v-for="o in SHIFT_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="实交现金" required>
            <el-input-number
                v-model="handoverForm.handinCash"
                :min="0"
                :precision="2"
                :step="10"
                class="!w-full"
                data-testid="fin-handover-cash"
            />
          </el-form-item>
          <el-form-item label="差异说明">
            <el-input
                v-model="handoverForm.diffReason"
                type="textarea"
                :rows="2"
                placeholder="实交现金与系统现金不一致时必填（后端强校验）"
                data-testid="fin-handover-reason"
            />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="handoverForm.remark" type="textarea" :rows="2" placeholder="选填" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="handoverVisible = false">取消</el-button>
        <el-button
            v-perm="'finance:settlement:edit'"
            type="primary"
            :loading="handoverSubmitting"
            data-testid="fin-handover-submit"
            @click="submitHandover"
        >
          确认交班
        </el-button>
      </template>
    </el-dialog>

    <!-- ==================== 执行日结 ==================== -->
    <el-dialog v-model="runVisible" title="执行日结（含三级对账预览）" width="760px" destroy-on-close>
      <div data-testid="fin-run-dialog" class="space-y-4">
        <el-form label-width="96px">
          <el-form-item label="日结日期" required>
            <el-date-picker
                v-model="runForm.settleDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择日期"
                class="!w-48"
                data-testid="fin-run-date"
                @change="loadRunPreview"
            />
            <span class="ml-3 text-xs text-slate-400">已审核的日结单不可重算</span>
          </el-form-item>
        </el-form>

        <div v-loading="runPreviewLoading" class="space-y-3">
          <template v-if="runReconcile">
            <div
                class="rounded-lg border p-3 text-sm"
                :class="runPassed ? 'border-emerald-200 bg-emerald-50 text-emerald-800'
                  : 'border-rose-200 bg-rose-50 text-rose-800'"
                data-testid="fin-run-summary"
            >
              <span class="font-semibold">{{ runPassed ? '三级对账已平' : '三级对账未平' }}</span>
              <span class="ml-2">{{ runReconcile.summary }}</span>
            </div>

            <table class="w-full border-collapse text-sm" data-testid="fin-run-recon">
              <thead>
              <tr class="bg-slate-50 text-left text-slate-500">
                <th class="border border-slate-200 px-3 py-2">级次</th>
                <th class="border border-slate-200 px-3 py-2">左口径</th>
                <th class="border border-slate-200 px-3 py-2 text-right">左金额</th>
                <th class="border border-slate-200 px-3 py-2">右口径</th>
                <th class="border border-slate-200 px-3 py-2 text-right">右金额</th>
                <th class="border border-slate-200 px-3 py-2 text-right">差额</th>
                <th class="border border-slate-200 px-3 py-2">结论</th>
              </tr>
              </thead>
              <tbody>
              <tr
                  v-for="it in runReconcile.items || []"
                  :key="it.level"
                  :data-testid="`fin-run-recon-${it.level}`"
              >
                <td class="border border-slate-200 px-3 py-2">{{ it.levelName }}</td>
                <td class="border border-slate-200 px-3 py-2 text-slate-500">{{ it.leftLabel }}</td>
                <td class="border border-slate-200 px-3 py-2 text-right font-mono">
                  ¥{{ formatMoney(it.leftAmount) }}
                </td>
                <td class="border border-slate-200 px-3 py-2 text-slate-500">{{ it.rightLabel }}</td>
                <td class="border border-slate-200 px-3 py-2 text-right font-mono">
                  ¥{{ formatMoney(it.rightAmount) }}
                </td>
                <td
                    class="border border-slate-200 px-3 py-2 text-right font-mono"
                    :class="it.passed ? 'text-emerald-600' : 'text-rose-600'"
                >
                  {{ formatMoney(it.diffAmount) }}
                </td>
                <td class="border border-slate-200 px-3 py-2">
                  <el-tag :class="reconcileTagClass(it.passed ? 1 : 2)" effect="plain" size="small" class="border">
                    {{ it.passed ? '平' : '差异' }}
                  </el-tag>
                </td>
              </tr>
              </tbody>
            </table>

            <div
                v-for="it in runReconcile.items || []"
                v-show="!it.passed"
                :key="`msg-${it.level}`"
                class="rounded border border-rose-200 bg-rose-50 p-2 text-xs text-rose-700"
            >
              {{ it.levelName }}：{{ it.message }}
            </div>
          </template>
          <div v-else-if="!runPreviewLoading" class="py-8 text-center text-sm text-slate-400">
            选择日期后自动试算当日对账结果
          </div>
        </div>

        <div
            v-if="runReconcile && !runPassed"
            class="space-y-3 rounded-lg border border-amber-200 bg-amber-50 p-3"
        >
          <el-checkbox v-model="runForm.allowDiff" data-testid="fin-run-allowdiff">
            差异确认无误，放行出单（放行后差异写进日结单留痕）
          </el-checkbox>
          <el-input
              v-model="runForm.remark"
              type="textarea"
              :rows="2"
              placeholder="放行理由（必填）：如跨日退费、渠道延迟到账"
              data-testid="fin-run-remark"
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="runVisible = false">取消</el-button>
        <el-button
            v-perm="'finance:settlement:edit'"
            type="primary"
            :loading="runSubmitting"
            :disabled="!!runReconcile && !runPassed && !runForm.allowDiff"
            data-testid="fin-run-submit"
            @click="submitRun"
        >
          确认日结
        </el-button>
      </template>
    </el-dialog>

    <!-- ==================== 日结单详情 ==================== -->
    <el-drawer v-model="dayDetailVisible" size="72%" :title="`日结单详情 ${detailSettlement?.settlementNo || ''}`">
      <div v-loading="dayDetailLoading" data-testid="fin-detail-drawer" class="space-y-5 text-sm">
        <div
            v-if="isTrial"
            class="rounded-lg border border-amber-200 bg-amber-50 p-3 text-amber-800"
            data-testid="fin-detail-trial"
        >
          该日尚未日结，以下为「试算结果」（不落库）；确认无误请点右上「执行日结」。
        </div>

        <!-- 单头 -->
        <div v-if="detailSettlement" class="rounded-lg border border-slate-200 p-4">
          <div class="grid grid-cols-2 gap-3 lg:grid-cols-4">
            <div><span class="text-slate-400">日结单号：</span>
              <span class="font-mono text-slate-700">{{ detailSettlement.settlementNo || '—' }}</span>
            </div>
            <div><span class="text-slate-400">日期：</span>
              <span class="text-slate-700">{{ detailSettlement.settleDate || '—' }}</span>
            </div>
            <div><span class="text-slate-400">状态：</span>
              <el-tag :class="dayStatusTagClass(detailSettlement.settleStatus)" effect="plain" size="small" class="border">
                {{ dayStatusText(detailSettlement.settleStatus) }}
              </el-tag>
            </div>
            <div><span class="text-slate-400">对账结论：</span>
              <el-tag
                  :class="reconcileTagClass(detailSettlement.reconcileStatus)"
                  effect="plain"
                  size="small"
                  class="border"
              >
                {{ reconcileText(detailSettlement.reconcileStatus) }}
              </el-tag>
            </div>
            <div><span class="text-slate-400">纳入班结单：</span>
              <span class="text-slate-700">{{ detailSettlement.shiftCount ?? 0 }} 张</span>
            </div>
            <div><span class="text-slate-400">当日实收：</span>
              <span class="text-slate-700">
                ¥{{ formatMoney(detailSettlement.chargeAmount) }}（{{ detailSettlement.chargeCount ?? 0 }} 笔流水
                / {{ detailSettlement.billCount ?? 0 }} 张账单）
              </span>
            </div>
            <div><span class="text-slate-400">Σ交班单：</span>
              <span class="text-slate-700">¥{{ formatMoney(detailSettlement.detailAmount) }}</span>
            </div>
            <div><span class="text-slate-400">统筹记账：</span>
              <span class="text-slate-700">¥{{ formatMoney(detailSettlement.poolAmount) }}</span>
            </div>
            <div><span class="text-slate-400">退费：</span>
              <span class="text-slate-700">
                ¥{{ formatMoney(detailSettlement.refundAmount) }}（{{ detailSettlement.refundCount ?? 0 }} 笔）
              </span>
            </div>
            <div><span class="text-slate-400">净额：</span>
              <span class="font-semibold text-slate-800">¥{{ formatMoney(detailSettlement.netAmount) }}</span>
            </div>
            <div><span class="text-slate-400">日结人：</span>
              <span class="text-slate-700">{{ detailSettlement.settleBy || '—' }}</span>
            </div>
            <div><span class="text-slate-400">日结时间：</span>
              <span class="text-slate-700">{{ detailSettlement.settleTime || '—' }}</span>
            </div>
            <div><span class="text-slate-400">审核人：</span>
              <span class="text-slate-700">{{ detailSettlement.auditBy || '—' }}</span>
            </div>
            <div><span class="text-slate-400">审核时间：</span>
              <span class="text-slate-700">{{ detailSettlement.auditTime || '—' }}</span>
            </div>
          </div>
          <div class="mt-3 grid grid-cols-2 gap-3 lg:grid-cols-5">
            <div><span class="text-slate-400">现金：</span>¥{{ formatMoney(detailSettlement.cashAmount) }}</div>
            <div><span class="text-slate-400">微信：</span>¥{{ formatMoney(detailSettlement.wechatAmount) }}</div>
            <div><span class="text-slate-400">支付宝：</span>¥{{ formatMoney(detailSettlement.alipayAmount) }}</div>
            <div><span class="text-slate-400">医保个账：</span>¥{{ formatMoney(detailSettlement.insuranceAmount) }}</div>
            <div><span class="text-slate-400">余额：</span>¥{{ formatMoney(detailSettlement.balanceAmount) }}</div>
          </div>
          <div
              v-if="Number(detailSettlement.unknownPayAmount || 0) !== 0"
              class="mt-2 text-xs text-rose-600"
          >
            支付方式未记录金额 ¥{{ formatMoney(detailSettlement.unknownPayAmount) }}
            —— 未并入任何渠道，避免点钞/渠道对账出现假数
          </div>
          <div v-if="detailSettlement.diffDetail" class="mt-3 rounded border border-slate-200 p-2 text-xs text-slate-600">
            差异说明：{{ detailSettlement.diffDetail }}
          </div>
        </div>

        <!-- 三级对账 -->
        <div v-if="detailReconcile">
          <div class="mb-2 flex items-center justify-between">
            <h3 class="font-semibold text-slate-800">三级对账</h3>
            <span
                class="text-xs"
                :class="detailReconcile.passed ? 'text-emerald-600' : 'text-rose-600'"
            >{{ detailReconcile.summary }}</span>
          </div>
          <table class="w-full border-collapse text-sm" data-testid="fin-recon-table">
            <thead>
            <tr class="bg-slate-50 text-left text-slate-500">
              <th class="border border-slate-200 px-3 py-2">级次</th>
              <th class="border border-slate-200 px-3 py-2">左口径</th>
              <th class="border border-slate-200 px-3 py-2 text-right">左金额</th>
              <th class="border border-slate-200 px-3 py-2">右口径</th>
              <th class="border border-slate-200 px-3 py-2 text-right">右金额</th>
              <th class="border border-slate-200 px-3 py-2 text-right">差额</th>
              <th class="border border-slate-200 px-3 py-2">结论</th>
            </tr>
            </thead>
            <tbody>
            <tr
                v-for="it in detailReconcile.items || []"
                :key="it.level"
                :data-testid="`fin-recon-${it.level}`"
            >
              <td class="border border-slate-200 px-3 py-2">{{ it.levelName }}</td>
              <td class="border border-slate-200 px-3 py-2 text-slate-500">{{ it.leftLabel }}</td>
              <td class="border border-slate-200 px-3 py-2 text-right font-mono">¥{{ formatMoney(it.leftAmount) }}</td>
              <td class="border border-slate-200 px-3 py-2 text-slate-500">{{ it.rightLabel }}</td>
              <td class="border border-slate-200 px-3 py-2 text-right font-mono">¥{{ formatMoney(it.rightAmount) }}</td>
              <td
                  class="border border-slate-200 px-3 py-2 text-right font-mono"
                  :class="it.passed ? 'text-emerald-600' : 'text-rose-600'"
              >
                {{ formatMoney(it.diffAmount) }}
              </td>
              <td class="border border-slate-200 px-3 py-2">
                <el-tag :class="reconcileTagClass(it.passed ? 1 : 2)" effect="plain" size="small" class="border">
                  {{ it.passed ? '平' : '差异' }}
                </el-tag>
              </td>
            </tr>
            </tbody>
          </table>
          <div
              v-for="it in detailReconcile.items || []"
              v-show="!it.passed"
              :key="`dmsg-${it.level}`"
              class="mt-2 rounded border border-rose-200 bg-rose-50 p-2 text-xs text-rose-700"
          >
            {{ it.levelName }}：{{ it.message }}
          </div>
        </div>

        <!-- 当日交班单 -->
        <div>
          <h3 class="mb-2 font-semibold text-slate-800">
            当日交班单（{{ detailShifts.length }} 张）
          </h3>
          <el-table :data="detailShifts" size="small" style="width: 100%" data-testid="fin-detail-shift-table">
            <el-table-column prop="settlementNo" label="交班单号" min-width="150" class-name="font-mono text-xs" />
            <el-table-column label="收费员" width="90">
              <template #default="{ row }">{{ row.cashierName || '—' }}</template>
            </el-table-column>
            <el-table-column label="班次" width="76">
              <template #default="{ row }">{{ shiftTypeText(row.shiftType) }}</template>
            </el-table-column>
            <el-table-column label="统计区间" min-width="280">
              <template #default="{ row }">
                <span class="font-mono text-xs">{{ row.periodBegin }} ~ {{ row.periodEnd }}</span>
              </template>
            </el-table-column>
            <el-table-column label="收费金额" width="120" align="right">
              <template #default="{ row }">¥{{ formatMoney(row.chargeAmount) }}</template>
            </el-table-column>
            <el-table-column label="退费金额" width="120" align="right">
              <template #default="{ row }">¥{{ formatMoney(row.refundAmount) }}</template>
            </el-table-column>
            <el-table-column label="现金差异" width="110" align="right">
              <template #default="{ row }">
                <span :class="Number(row.cashDiff || 0) === 0 ? 'text-emerald-600' : 'text-rose-600'">
                  {{ formatMoney(row.cashDiff) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="86">
              <template #default="{ row }">
                <el-tag :class="cashierStatusTagClass(row.settleStatus)" effect="plain" size="small" class="border">
                  {{ cashierStatusText(row.settleStatus) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="detailShifts.length === 0" class="py-6 text-center text-xs text-slate-400">
            该日没有交班单 —— 二级对账会把当日全部收费都算成「未纳班结」
          </div>
        </div>

        <!-- 科室收入 -->
        <div>
          <h3 class="mb-2 font-semibold text-slate-800">科室收入</h3>
          <el-table :data="detailDepts" size="small" style="width: 100%" data-testid="fin-dept-table">
            <el-table-column label="科室" min-width="180">
              <template #default="{ row }">
                <span :class="row.unattributed ? 'text-rose-600' : 'text-slate-700'">
                  {{ row.deptName || '—' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="明细笔数" width="110" align="right">
              <template #default="{ row }">{{ row.itemCount ?? 0 }}</template>
            </el-table-column>
            <el-table-column label="金额" width="140" align="right">
              <template #default="{ row }">
                <span class="font-semibold" :class="row.unattributed ? 'text-rose-600' : 'text-slate-800'">
                  ¥{{ formatMoney(row.amount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="占比" width="100" align="right">
              <template #default="{ row }">{{ formatMoney(row.ratio) }}%</template>
            </el-table-column>
          </el-table>
          <div class="mt-2 text-xs text-slate-400">
            占比分母为全部明细金额（含无归属），故各行合计不超过 100%。
            无科室归属行单列、不并入科室统计。
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="dayDetailVisible = false">关闭</el-button>
        <el-button
            v-if="detailSettlement && !audited"
            v-perm="'finance:settlement:edit'"
            type="primary"
            :icon="Select"
            data-testid="fin-detail-audit-btn"
            @click="handleAudit"
        >
          审核日结单
        </el-button>
      </template>
    </el-drawer>

    <!-- ==================== 交班单详情 ==================== -->
    <el-dialog v-model="cashierDetailVisible" title="交班单详情" width="680px" destroy-on-close>
      <div v-loading="cashierDetailLoading" class="space-y-4 text-sm">
        <div class="grid grid-cols-2 gap-3 rounded-lg border border-slate-200 p-4">
          <div><span class="text-slate-400">交班单号：</span>
            <span class="font-mono text-slate-700">{{ cashierDetail?.settlementNo || '—' }}</span>
          </div>
          <div><span class="text-slate-400">收费员：</span>
            <span class="text-slate-700">{{ cashierDetail?.cashierName || '—' }}</span>
          </div>
          <div><span class="text-slate-400">班次：</span>
            <span class="text-slate-700">{{ shiftTypeText(cashierDetail?.shiftType) }}</span>
          </div>
          <div><span class="text-slate-400">状态：</span>
            <span class="text-slate-700">{{ cashierStatusText(cashierDetail?.settleStatus) }}</span>
          </div>
          <div class="col-span-2"><span class="text-slate-400">统计区间：</span>
            <span class="font-mono text-slate-700">
              {{ cashierDetail?.periodBegin || '—' }} ~ {{ cashierDetail?.periodEnd || '—' }}
            </span>
          </div>
          <div><span class="text-slate-400">收费：</span>
            <span class="text-slate-700">
              ¥{{ formatMoney(cashierDetail?.chargeAmount) }}（{{ cashierDetail?.chargeCount ?? 0 }} 笔）
            </span>
          </div>
          <div><span class="text-slate-400">退费：</span>
            <span class="text-slate-700">
              ¥{{ formatMoney(cashierDetail?.refundAmount) }}（{{ cashierDetail?.refundCount ?? 0 }} 笔）
            </span>
          </div>
          <div><span class="text-slate-400">净额：</span>
            <span class="font-semibold text-slate-800">¥{{ formatMoney(cashierDetail?.netAmount) }}</span>
          </div>
          <div><span class="text-slate-400">票据(开/废)：</span>
            <span class="text-slate-700">
              {{ cashierDetail?.invoiceCount ?? 0 }} / {{ cashierDetail?.invoiceVoidCount ?? 0 }}
            </span>
          </div>
        </div>

        <div class="grid grid-cols-3 gap-3 rounded-lg border border-slate-200 p-4">
          <div><span class="text-slate-400">现金：</span>¥{{ formatMoney(cashierDetail?.cashAmount) }}</div>
          <div><span class="text-slate-400">微信：</span>¥{{ formatMoney(cashierDetail?.wechatAmount) }}</div>
          <div><span class="text-slate-400">支付宝：</span>¥{{ formatMoney(cashierDetail?.alipayAmount) }}</div>
          <div><span class="text-slate-400">医保：</span>¥{{ formatMoney(cashierDetail?.insuranceAmount) }}</div>
          <div><span class="text-slate-400">余额：</span>¥{{ formatMoney(cashierDetail?.balanceAmount) }}</div>
          <div>
            <span class="text-slate-400">支付方式未知：</span>
            <span :class="Number(cashierDetail?.unknownPayAmount || 0) === 0 ? 'text-slate-700' : 'text-rose-600'">
              ¥{{ formatMoney(cashierDetail?.unknownPayAmount) }}
            </span>
          </div>
        </div>

        <div class="rounded-lg border border-slate-200 p-4">
          <div class="flex items-center gap-6">
            <div><span class="text-slate-400">实交现金：</span>
              <span class="font-semibold text-slate-800">¥{{ formatMoney(cashierDetail?.handinCash) }}</span>
            </div>
            <div>
              <span class="text-slate-400">现金差异：</span>
              <span
                  class="font-semibold"
                  :class="Number(cashierDetail?.cashDiff || 0) === 0 ? 'text-emerald-600' : 'text-rose-600'"
              >
                {{ Number(cashierDetail?.cashDiff || 0) > 0 ? '+' : '' }}{{ formatMoney(cashierDetail?.cashDiff) }}
              </span>
            </div>
          </div>
          <div v-if="cashierDetail?.diffReason" class="mt-2 text-xs text-slate-600">
            差异说明：{{ cashierDetail.diffReason }}
          </div>
        </div>

        <div v-if="cashierDetail?.remark" class="rounded-lg border border-slate-200 p-3">
          <span class="text-slate-400">备注：</span><span class="text-slate-700">{{ cashierDetail.remark }}</span>
        </div>
        <div class="text-xs text-slate-400">
          制单：{{ cashierDetail?.createBy || '—' }} / {{ cashierDetail?.createTime || '—' }}
          <span v-if="cashierDetail?.auditBy" class="ml-3">审核：{{ cashierDetail.auditBy }} / {{ cashierDetail.auditTime }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>
