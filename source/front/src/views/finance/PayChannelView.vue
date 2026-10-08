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

    <!-- 拉取渠道账单 -->
    <el-dialog v-model="importVisible" title="拉取渠道账单" width="420px">
      <el-form label-width="90px">
        <el-form-item label="渠道">
          <el-select v-model="importForm.channel" style="width: 100%" data-testid="payc-import-channel">
            <el-option v-for="c in CHANNELS" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="账单日期">
          <el-date-picker v-model="importForm.billDate" type="date" value-format="YYYY-MM-DD"
                          style="width: 100%" data-testid="payc-import-date" />
        </el-form-item>
        <el-alert type="info" :closable="false"
                  title="当前未接真实渠道：后端打印会在服务端控制台打印拉取动作，并按本地当日支付流水生成模拟账单（幂等，可重复拉取）" />
      </el-form>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" data-testid="payc-import-ok" @click="doImport">拉取</el-button>
      </template>
    </el-dialog>

    <!-- 手工登记 -->
    <el-dialog v-model="manualVisible" title="手工登记渠道流水" width="460px">
      <el-form label-width="100px">
        <el-form-item label="渠道" required>
          <el-select v-model="manualForm.channel" style="width: 100%" data-testid="payc-manual-channel">
            <el-option v-for="c in CHANNELS" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="账单日期" required>
          <el-date-picker v-model="manualForm.billDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="渠道流水号" required>
          <el-input v-model="manualForm.channelTradeNo" data-testid="payc-manual-trade-no" />
        </el-form-item>
        <el-form-item label="金额" required>
          <!-- 退款账单行是负数，min 不能卡 0.01，否则渠道退的那笔钱根本登记不进去 -->
          <el-input-number v-model="manualForm.amount" :min="-999999" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="交易时间">
          <el-date-picker v-model="manualForm.tradeTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="manualForm.remark" type="textarea" :rows="2" maxlength="490" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="manualVisible = false">取消</el-button>
        <el-button type="primary" data-testid="payc-manual-ok" @click="doManual">登记</el-button>
      </template>
    </el-dialog>

    <!-- 勾对 -->
    <el-dialog v-model="matchVisible" title="人工勾对" width="560px">
      <el-form label-width="100px">
        <el-form-item label="渠道流水">
          <span>{{ matchRow ? matchRow.channelTradeNo : '' }}</span>
        </el-form-item>
        <el-form-item label="渠道金额">
          <span class="strong">¥{{ matchRow ? money(matchRow.amount) : '' }}</span>
        </el-form-item>
        <el-form-item label="本地支付流水" required>
          <el-select v-model="matchForm.localTxnNo" filterable placeholder="选择当日该渠道的成功支付流水"
                     style="width: 100%" data-testid="payc-match-txn" :fit-input-width="false">
            <el-option v-for="c in candidates" :key="c.txnNo" :value="c.txnNo"
                       :label="`${c.txnNo}  ${c.patientName}  ¥${money(c.amount)}  ${c.directionText}  ${c.txnTime}`" />
          </el-select>
        </el-form-item>
        <el-alert type="warning" :closable="false"
                  title="两侧金额（收正退负）不一致会被拒绝；差额需要定性的请改用「差额处理」登记长款/短款" />
      </el-form>
      <template #footer>
        <el-button @click="matchVisible = false">取消</el-button>
        <el-button type="primary" data-testid="payc-match-ok" @click="doMatch">确认勾对</el-button>
      </template>
    </el-dialog>

    <!-- 长短款处理 -->
    <el-dialog v-model="diffVisible" title="长款 / 短款处理" width="480px">
      <el-form label-width="100px">
        <el-form-item label="渠道流水">
          <span>{{ diffRow ? diffRow.channelTradeNo : '' }}（¥{{ diffRow ? money(diffRow.amount) : '' }}）</span>
        </el-form-item>
        <el-form-item label="处理结论" required>
          <el-radio-group v-model="diffForm.handleType" data-testid="payc-diff-type">
            <el-radio :value="2">长款（渠道有、本地无）</el-radio>
            <el-radio :value="3">短款（金额不符待核）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理说明" required>
          <el-input v-model="diffForm.handleRemark" type="textarea" :rows="3" maxlength="490"
                    placeholder="定性依据必填（如：重复推送的渠道流水，已联系渠道冲正）" data-testid="payc-diff-remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="diffVisible = false">取消</el-button>
        <el-button type="primary" data-testid="payc-diff-ok" @click="doDiff">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'
import * as api from '@/api/payChannel'

// 渠道/状态枚举单点定义在本文件（无字典码值，命中不了渲染 未知(n) 不回落）
// 渠道只列走商户平台的在线渠道（对齐后端 PaymentMethodEnum.channelBacked）：
// 4 是医保个人账户，刷参保人卡扣的钱不由商户平台出账单，混进来会让"渠道短款"和"医保欠付"看起来是同一件事。
const CHANNELS = [
  { value: 2, label: '微信' },
  { value: 3, label: '支付宝' },
  { value: 6, label: '银行卡' },
]
const MATCH_STATUS = [
  { value: 0, label: '待勾对' },
  { value: 1, label: '已勾对' },
  { value: 2, label: '长款' },
  { value: 3, label: '短款' },
]
const chText = (v) => CHANNELS.find(c => c.value === v)?.label ?? `未知(${v})`
const statusText = (v) => MATCH_STATUS.find(s => s.value === v)?.label ?? `未知(${v})`
const statusTag = (v) => ({ 0: 'info', 1: 'success', 2: 'danger', 3: 'warning' }[v] ?? 'info')

const money = (v) => Number(v ?? 0).toFixed(2)
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

// ---- 拉取 ----
const importVisible = ref(false)
const importForm = reactive({ channel: 2, billDate: null })
function openImport() {
  importForm.billDate = query.billDate || today()
  importVisible.value = true
}
async function doImport() {
  const { data } = await api.importBill({ ...importForm })
  ElMessage.success(`拉取完成，新落台账 ${data ?? 0} 笔（服务端控制台可见 [M7支付渠道口子] 打印）`)
  importVisible.value = false
  reload()
}

// ---- 手工登记 ----
const manualVisible = ref(false)
const manualForm = reactive({ channel: 2, billDate: null, channelTradeNo: '', amount: null, tradeTime: null, remark: '' })
function openManual() {
  manualForm.billDate = query.billDate || today()
  manualVisible.value = true
}
async function doManual() {
  await api.manualRegister({ ...manualForm })
  ElMessage.success('已登记渠道流水')
  manualVisible.value = false
  reload()
}

// ---- 勾对 ----
const matchVisible = ref(false)
const matchRow = ref(null)
const candidates = ref([])
const matchForm = reactive({ localTxnNo: '' })
async function openMatch(row) {
  matchRow.value = row
  matchForm.localTxnNo = ''
  const { data } = await api.matchCandidates(row.id)
  candidates.value = data || []
  matchVisible.value = true
}
async function doMatch() {
  await api.match({ id: matchRow.value.id, localTxnNo: matchForm.localTxnNo })
  ElMessage.success('勾对成功')
  matchVisible.value = false
  reload()
}

// ---- 长短款 ----
const diffVisible = ref(false)
const diffRow = ref(null)
const diffForm = reactive({ handleType: 2, handleRemark: '' })
function openDiff(row) {
  diffRow.value = row
  diffForm.handleType = 2
  diffForm.handleRemark = ''
  diffVisible.value = true
}
async function doDiff() {
  if (!diffForm.handleRemark.trim()) {
    ElMessage.warning('长款/短款定性必须写明处理说明')
    return
  }
  await api.handleDiff({ id: diffRow.value.id, handleType: diffForm.handleType, handleRemark: diffForm.handleRemark })
  ElMessage.success('已登记处理结论')
  diffVisible.value = false
  reload()
}

function today() {
  // 本地日期（UTC+8 早 8 点前 toISOString 会取到昨天）
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
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
.strong {
  font-weight: 600;
}
</style>
