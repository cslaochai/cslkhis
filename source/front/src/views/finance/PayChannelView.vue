<template>
  <div data-testid="payc-page">
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="账单日期">
            <el-date-picker v-model="query.billDate" type="date" value-format="YYYY-MM-DD"
                            placeholder="全部日期" clearable style="width: 150px" />
          </el-form-item>
          <el-form-item label="渠道">
            <el-select v-model="query.channel" placeholder="全部渠道" clearable style="width: 120px">
              <el-option v-for="c in CHANNELS" :key="c.value" :label="c.label" :value="c.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="勾对状态">
            <el-select v-model="query.matchStatus" placeholder="全部状态" clearable style="width: 140px">
              <el-option v-for="s in MATCH_STATUS" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" data-testid="payc-search" @click="reload">查询</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'finance:payChannel:import'" type="primary" data-testid="payc-import"
                     @click="openImport">拉取渠道账单</el-button>
          <el-button v-perm="'finance:payChannel:import'" data-testid="payc-manual"
                     @click="openManual">手工登记流水</el-button>
        </div>
      </div>
    </el-card>

    <!-- 渠道对账汇总：本地口径 vs 渠道口径 -->
    <el-card shadow="never" class="mb-3">
      <el-table :data="summaryRows" border data-testid="payc-summary">
        <el-table-column label="对账日" prop="billDate" width="110" />
        <el-table-column label="渠道" width="90">
          <template #default="{ row }">{{ chText(row.channel) }}</template>
        </el-table-column>
        <el-table-column label="本地实收（净）" align="right">
          <template #default="{ row }">{{ row.localCount }} 笔 / ¥{{ money(row.localAmount) }}</template>
        </el-table-column>
        <el-table-column label="渠道流水" align="right">
          <template #default="{ row }">{{ row.flowTotal }} 笔 / ¥{{ money(sumFlow(row)) }}</template>
        </el-table-column>
        <el-table-column label="已勾对" align="right">
          <template #default="{ row }">{{ row.matchedCount }} 笔 / ¥{{ money(row.matchedAmount) }}</template>
        </el-table-column>
        <el-table-column label="待处理" align="right">
          <template #default="{ row }">{{ row.unmatchedCount }} 笔 / ¥{{ money(row.unmatchedAmount) }}</template>
        </el-table-column>
        <el-table-column label="差额（渠道-本地）" align="right" width="150">
          <template #default="{ row }">
            <span :class="{ 'diff-warn': Number(row.diffAmount) !== 0 }">¥{{ money(row.diffAmount) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="rows" v-loading="loading" stripe :max-height="tableMaxHeight" data-testid="payc-table">
        <el-table-column label="账单日期" prop="billDate" width="110" />
        <el-table-column label="渠道" width="90">
          <template #default="{ row }">{{ chText(row.channel) }}</template>
        </el-table-column>
        <el-table-column label="渠道流水号" prop="channelTradeNo" min-width="220" show-overflow-tooltip />
        <el-table-column label="交易时间" width="160">
          <template #default="{ row }">{{ fmtTime(row.tradeTime) }}</template>
        </el-table-column>
        <el-table-column label="渠道金额" align="right" width="110">
          <template #default="{ row }">¥{{ money(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="来源" width="90">
          <template #default="{ row }">{{ row.importWay === 1 ? '渠道拉取' : row.importWay === 2 ? '手工登记' : unknown(row.importWay) }}</template>
        </el-table-column>
        <el-table-column label="勾对状态" width="150">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.matchStatus)" size="small">{{ statusText(row.matchStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="本地支付流水号" prop="localTxnNo" width="200">
          <template #default="{ row }">
            {{ row.localTxnNo || '—' }}
            <el-tag v-if="row.txnDirection === 2" type="warning" size="small" effect="plain">退款</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="勾对人" width="110">
          <template #default="{ row }">{{ row.matchedByName || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <template v-if="row.matchStatus === 0">
              <el-button v-perm="'finance:payChannel:match'" link type="primary"
                         :data-testid="'payc-match-' + row.channelTradeNo" @click.stop="openMatch(row)">勾对</el-button>
              <el-button v-perm="'finance:payChannel:diff'" link type="warning"
                         :data-testid="'payc-diff-' + row.channelTradeNo" @click.stop="openDiff(row)">差额处理</el-button>
            </template>
            <span v-else class="muted">{{ row.handleRemark || '—' }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination layout="total, sizes, prev, pager, next" :total="total"
                       :page-sizes="PAGE_SIZES" :page-size="query.pageSize" :current-page="query.pageNum"
                       @current-change="p => { query.pageNum = p; load() }"
                       @size-change="s => { query.pageSize = s; query.pageNum = 1; load() }" />
      </div>
    </el-card>

    <PayChannelImportAdd v-model="importVisible" :bill-date="query.billDate" @saved="reload" />
    <PayChannelManualAdd v-model="manualVisible" :bill-date="query.billDate" @saved="reload" />
    <PayChannelMatchEdit v-model="matchVisible" :row="matchRow" @saved="reload" />
    <PayChannelDiffEdit v-model="diffVisible" :row="diffRow" @saved="reload" />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'
import { CHANNELS, MATCH_STATUS, money } from '@/lib/payChannel'
import PayChannelImportAdd from './PayChannelImportAdd.vue'
import PayChannelManualAdd from './PayChannelManualAdd.vue'
import PayChannelMatchEdit from './PayChannelMatchEdit.vue'
import PayChannelDiffEdit from './PayChannelDiffEdit.vue'
import * as api from '@/api/payChannel'

// 渠道/状态枚举单点定义在 @/lib/payChannel（页面与拆出的弹框组件共用，命中不了渲染 未知(n) 不回落）
const chText = (v) => CHANNELS.find(c => c.value === v)?.label ?? `未知(${v})`
const statusText = (v) => MATCH_STATUS.find(s => s.value === v)?.label ?? `未知(${v})`
const statusTag = (v) => ({ 0: 'info', 1: 'success', 2: 'danger', 3: 'warning' }[v] ?? 'info')

const sumFlow = (row) => Number(row.matchedAmount ?? 0) + Number(row.unmatchedAmount ?? 0)
const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '—')
const unknown = (v) => `未知(${v})`

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const summaryRows = ref([])
const query = reactive({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, billDate: null, channel: null, matchStatus: null })
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

async function load() {
  loading.value = true
  try {
    const { data } = await api.listPage(query)
    rows.value = data.records || []
    total.value = Number(data.total || 0)
  } finally {
    loading.value = false
  }
}

async function loadSummary() {
  const { data } = await api.summary(query.billDate || undefined)
  summaryRows.value = (data.rows || []).map(r => ({ ...r, billDate: data.billDate }))
}

function reload() {
  query.pageNum = 1
  load()
  loadSummary()
}

onMounted(reload)

// ---- 弹框拆到同目录兄弟组件，页面只管开关与把行/查询日期递进去 ----
const importVisible = ref(false)
const manualVisible = ref(false)
const matchVisible = ref(false)
const matchRow = ref(null)
const diffVisible = ref(false)
const diffRow = ref(null)

function openImport() {
  importVisible.value = true
}
function openManual() {
  manualVisible.value = true
}
function openMatch(row) {
  matchRow.value = row
  matchVisible.value = true
}
function openDiff(row) {
  diffRow.value = row
  diffVisible.value = true
}
</script>

<style scoped>
.diff-warn {
  color: #e6a23c;
  font-weight: 600;
}
.muted {
  color: #909399;
}
</style>
