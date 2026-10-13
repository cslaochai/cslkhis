<template>
  <div class="space-y-6">
    <!-- 页头 -->
    <div class="flex items-start justify-between">
      <div class="flex items-center gap-2">
        <el-button v-perm="'finance:settlement:edit'" :icon="Wallet" data-testid="fin-handover-btn"
                   @click="openHandover">办理交班
        </el-button>
        <el-button v-perm="'finance:settlement:edit'" :icon="Tickets" data-testid="fin-run-day-btn" type="primary"
                   @click="openRun">
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
            :class="Number(counts.dayWithDiff || 0) > 0 ? 'text-rose-600' : 'text-emerald-600'"
            class="mt-1 text-2xl font-bold"
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
              class="!w-36"
              clearable
              data-testid="fin-cashier-status"
              placeholder="状态"
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
              class="!w-72"
              end-placeholder="止"
              start-placeholder="交班日期起"
              type="daterange"
              value-format="YYYY-MM-DD"
          />
          <el-button :icon="Search" type="primary" @click="searchCashier">查询</el-button>
          <el-button :icon="Refresh" @click="resetCashier">重置</el-button>
          <span class="ml-auto text-sm text-slate-500">
            共 <span class="font-semibold text-slate-700">{{ cashierLoading ? '—' : cashierPage.total }}</span> 张交班单
          </span>
        </div>

        <el-table v-loading="cashierLoading" :data="cashierRows" data-testid="fin-cashier-table" style="width: 100%">
          <el-table-column class-name="font-mono text-xs" label="交班单号" min-width="150" prop="settlementNo"/>
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
          <el-table-column align="right" label="收费" width="130">
            <template #default="{ row }">
              <div>¥{{ formatMoney(row.chargeAmount) }}</div>
              <div class="text-xs text-slate-400">{{ row.chargeCount ?? 0 }} 笔</div>
            </template>
          </el-table-column>
          <el-table-column align="right" label="退费" width="130">
            <template #default="{ row }">
              <div>¥{{ formatMoney(row.refundAmount) }}</div>
              <div class="text-xs text-slate-400">{{ row.refundCount ?? 0 }} 笔</div>
            </template>
          </el-table-column>
          <el-table-column align="right" label="净额" width="120">
            <template #default="{ row }">
              <span class="font-semibold text-slate-800">¥{{ formatMoney(row.netAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column align="right" label="现金（系统/实交）" width="170">
            <template #default="{ row }">
              <div>¥{{ formatMoney(row.cashAmount) }} / ¥{{ formatMoney(row.handinCash) }}</div>
              <div
                  :class="Number(row.cashDiff || 0) === 0 ? 'text-emerald-600' : 'text-rose-600'"
                  class="text-xs"
              >
                差异 {{ Number(row.cashDiff || 0) > 0 ? '+' : '' }}{{ formatMoney(row.cashDiff) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column align="right" label="票据(开/废)" width="110">
            <template #default="{ row }">
              {{ row.invoiceCount ?? 0 }} / {{ row.invoiceVoidCount ?? 0 }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="96">
            <template #default="{ row }">
              <el-tag :class="cashierStatusTagClass(row.settleStatus)" class="border" effect="plain" size="small">
                {{ cashierStatusText(row.settleStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="90">
            <template #default="{ row }">
              <el-button :icon="View" link size="small" type="primary" @click="openCashierDetail(row)">
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
              class="!w-32"
              clearable
              data-testid="fin-day-status"
              placeholder="状态"
          >
            <el-option v-for="o in DAY_SETTLE_STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
          <el-select
              v-model="dayQuery.reconcileStatus"
              class="!w-32"
              clearable
              data-testid="fin-day-reconcile"
              placeholder="对账结论"
          >
            <el-option v-for="o in RECONCILE_STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
          <el-date-picker
              v-model="dayQuery.dateRange"
              class="!w-72"
              end-placeholder="止"
              start-placeholder="日期起"
              type="daterange"
              value-format="YYYY-MM-DD"
          />
          <el-button :icon="Search" type="primary" @click="searchDay">查询</el-button>
          <el-button :icon="Refresh" @click="resetDay">重置</el-button>
          <span class="ml-auto text-sm text-slate-500">
            共 <span class="font-semibold text-slate-700">{{ dayLoading ? '—' : dayPage.total }}</span> 张日结单
          </span>
        </div>

        <el-table v-loading="dayLoading" :data="dayRows" data-testid="fin-day-table" style="width: 100%">
          <el-table-column class-name="font-mono text-xs" label="日结单号" min-width="140" prop="settlementNo"/>
          <el-table-column label="日期" prop="settleDate" width="110">
            <template #default="{ row }">{{ row.settleDate || '—' }}</template>
          </el-table-column>
          <el-table-column align="right" label="班结单" width="84">
            <template #default="{ row }">{{ row.shiftCount ?? 0 }} 张</template>
          </el-table-column>
          <el-table-column align="right" label="当日实收" width="140">
            <template #default="{ row }">
              <div>¥{{ formatMoney(row.chargeAmount) }}</div>
              <div class="text-xs text-slate-400">{{ row.chargeCount ?? 0 }} 笔 / {{ row.billCount ?? 0 }} 张账单</div>
            </template>
          </el-table-column>
          <el-table-column align="right" label="Σ交班单" width="130">
            <template #default="{ row }">
              <span
                  :class="Number(row.reconcileStatus || 0) === 1 ? 'text-emerald-600' : 'text-rose-600'"
                  class="font-mono"
              >
                ¥{{ formatMoney(row.detailAmount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column align="right" label="统筹记账" width="120">
            <template #default="{ row }">¥{{ formatMoney(row.poolAmount) }}</template>
          </el-table-column>
          <el-table-column align="right" label="退费" width="120">
            <template #default="{ row }">¥{{ formatMoney(row.refundAmount) }}</template>
          </el-table-column>
          <el-table-column align="right" label="净额" width="130">
            <template #default="{ row }">
              <span class="font-semibold text-slate-800">¥{{ formatMoney(row.netAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column align="right" label="最大差异" width="120">
            <template #default="{ row }">
              <span :class="Number(row.diffAmount || 0) === 0 ? 'text-emerald-600' : 'text-rose-600'">
                ¥{{ formatMoney(row.diffAmount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="对账" width="86">
            <template #default="{ row }">
              <el-tag :class="reconcileTagClass(row.reconcileStatus)" class="border" effect="plain" size="small">
                {{ reconcileText(row.reconcileStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="86">
            <template #default="{ row }">
              <el-tag :class="dayStatusTagClass(row.settleStatus)" class="border" effect="plain" size="small">
                {{ dayStatusText(row.settleStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="130">
            <template #default="{ row }">
              <el-button :icon="View" link size="small" type="primary" @click="openDayDetail(row)">详情</el-button>
              <el-button
                  v-if="row.settleStatus === 1"
                  :icon="Select"
                  link
                  size="small"
                  type="success"
                  @click="openDayDetail(row)"
              >
                审核
              </el-button>
            </template>
          </el-table-column>
        </el-table>
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

    <!-- 交班 / 执行日结 / 交班单详情 / 日结单详情已拆到同目录兄弟组件，页面只管开关与递参 -->
    <FinanceHandoverAdd v-model="handoverVisible" @saved="onHandoverSaved"/>
    <FinanceDayRunAdd v-model="runVisible" @saved="onRunSaved"/>
    <FinanceCashierDetail v-model="cashierDetailVisible" :id="cashierDetailRow?.id" :row="cashierDetailRow"/>
    <FinanceDayDetail v-model="dayDetailVisible" :row="dayDetailRow" @saved="onDayAudited"/>
  </div>
</template>

<script setup>
// 财务班结 / 日结 / 三级对账（G8）
//
// 口径要点（写在页面里是因为这里最容易看错）：
//   1) 班结单（JS 开头）是「凭证」——金额是交班那一刻的定格值，之后补录的收费不会改它。
//      要看实时的去查收费单，别拿交班单当实时数。
//   2) 统计区间是**滚动的**：上次交班时刻 → 本次交班时刻，不是固定 08:00-16:00。
//   3) 页面上的筛选一律下推后端，禁止对当前页 list.filter（翻页后结果不全）。
import {onMounted, reactive, ref} from 'vue';
import {Refresh, Search, Select, Tickets, View, Wallet} from '@element-plus/icons-vue';
import {ElMessage} from 'element-plus';
import {cashierListPage, dayListPage, settlementStatusCount} from '@/api/financeSettlement';
import {
  CASHIER_SETTLE_STATUS_OPTIONS,
  cashierStatusTagClass,
  cashierStatusText,
  DAY_SETTLE_STATUS_OPTIONS,
  dayStatusTagClass,
  dayStatusText,
  RECONCILE_STATUS_OPTIONS,
  reconcileTagClass,
  reconcileText,
  shiftTypeText,
} from '@/lib/financeSettlement';
import {formatMoney} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import FinanceHandoverAdd from './FinanceHandoverAdd.vue';
import FinanceDayRunAdd from './FinanceDayRunAdd.vue';
import FinanceCashierDetail from './FinanceCashierDetail.vue';
import FinanceDayDetail from './FinanceDayDetail.vue';

const activeTab = ref('cashier');
// ---------------------------------------------------------------------------
// 状态卡
// ---------------------------------------------------------------------------
const countLoading = ref(true);
const counts = ref({});

async function loadCounts() {
  countLoading.value = true;
  try {
    const res = await settlementStatusCount();
    counts.value = res.data || {};
  } catch (e) {
    counts.value = {};
    
  } finally {
    countLoading.value = false;
  }
}

// ---------------------------------------------------------------------------
// 交班单列表
// ---------------------------------------------------------------------------
const cashierLoading = ref(true); // 「加载中 ≠ 没有」：初值 true，未加载完不显示「暂无」
const cashierRows = ref([]);
const cashierQuery = reactive({settleStatus: null, dateRange: []});
const cashierPage = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});

async function loadCashierList() {
  cashierLoading.value = true;
  try {
    const res = await cashierListPage({
      settleStatus: cashierQuery.settleStatus === null ? undefined : cashierQuery.settleStatus,
      dateStart: cashierQuery.dateRange?.[0] || undefined,
      dateEnd: cashierQuery.dateRange?.[1] || undefined,
      pageNum: cashierPage.pageNum,
      pageSize: cashierPage.pageSize,
    });
    const data = res.data || {};
    cashierRows.value = data.records || [];
    cashierPage.total = Number(data.total || 0);
  } catch (e) {
    cashierRows.value = [];
    cashierPage.total = 0;
    
  } finally {
    cashierLoading.value = false;
  }
}

// ---------------------------------------------------------------------------
// 日结单列表
// ---------------------------------------------------------------------------
const dayLoading = ref(true);
const dayRows = ref([]);
const dayQuery = reactive({
  settleStatus: null,
  reconcileStatus: null,
  dateRange: [],
});
const dayPage = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});

async function loadDayList() {
  dayLoading.value = true;
  try {
    const res = await dayListPage({
      settleStatus: dayQuery.settleStatus === null ? undefined : dayQuery.settleStatus,
      reconcileStatus: dayQuery.reconcileStatus === null ? undefined : dayQuery.reconcileStatus,
      dateStart: dayQuery.dateRange?.[0] || undefined,
      dateEnd: dayQuery.dateRange?.[1] || undefined,
      pageNum: dayPage.pageNum,
      pageSize: dayPage.pageSize,
    });
    const data = res.data || {};
    dayRows.value = data.records || [];
    dayPage.total = Number(data.total || 0);
  } catch (e) {
    dayRows.value = [];
    dayPage.total = 0;
    
  } finally {
    dayLoading.value = false;
  }
}

function searchCashier() {
  cashierPage.pageNum = 1;
  loadCashierList();
}

function resetCashier() {
  cashierQuery.settleStatus = null;
  cashierQuery.dateRange = [];
  searchCashier();
}

function searchDay() {
  dayPage.pageNum = 1;
  loadDayList();
}

function resetDay() {
  dayQuery.settleStatus = null;
  dayQuery.reconcileStatus = null;
  dayQuery.dateRange = [];
  searchDay();
}

// ---------------------------------------------------------------------------
// 交班（班结）：表单与提交逻辑在 FinanceHandoverAdd，页面只管开关和保存后刷新台账
// ---------------------------------------------------------------------------
const handoverVisible = ref(false);

function openHandover() {
  handoverVisible.value = true;
}

async function onHandoverSaved() {
  cashierPage.pageNum = 1;
  await Promise.all([loadCashierList(), loadCounts()]);
}

// 交班单详情：FinanceCashierDetail 自取数据，页面只递打开来源行
const cashierDetailVisible = ref(false);
const cashierDetailRow = ref(null);

function openCashierDetail(row) {
  cashierDetailRow.value = row;
  cashierDetailVisible.value = true;
}

// ---------------------------------------------------------------------------
// 执行日结（含三级对账预览）：逻辑在 FinanceDayRunAdd
// ---------------------------------------------------------------------------
const runVisible = ref(false);

function openRun() {
  runVisible.value = true;
}

async function onRunSaved(vo) {
  activeTab.value = 'day';
  dayPage.pageNum = 1;
  await Promise.all([loadDayList(), loadCounts()]);
  if (vo?.id)
    openDayDetail({id: vo.id});
}

// ---------------------------------------------------------------------------
// 日结单详情：逻辑在 FinanceDayDetail（含审核），页面递打开来源行、审核后刷新
// ---------------------------------------------------------------------------
const dayDetailVisible = ref(false);
const dayDetailRow = ref(null);

function openDayDetail(row) {
  dayDetailRow.value = row;
  dayDetailVisible.value = true;
}

async function onDayAudited() {
  await Promise.all([loadDayList(), loadCounts(), loadCashierList()]);
}

onMounted(async () => {
  await loadCounts();
  await Promise.all([loadCashierList(), loadDayList()]);
});
</script>
