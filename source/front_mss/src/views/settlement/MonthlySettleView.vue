<template>
  <div class="space-y-3">
    <!-- 月度结果卡片 -->
    <div class="his-card p-4">
      <div class="flex items-center justify-between mb-3">
        <div class="font-semibold text-[15px] text-[#0B3B66]">月度结算结果（{{ result.month || selectedMonth }}）</div>
        <div class="flex items-center gap-2">
          <el-date-picker
            v-model="selectedMonth"
            type="month"
            value-format="YYYY-MM"
            placeholder="选择账期"
            style="width: 150px"
          />
          <el-button type="primary" @click="loadResult">查询</el-button>
          <el-button @click="resetMonth">本月</el-button>
        </div>
      </div>

      <el-empty v-if="!result.month" description="请选择账期查询月度结算结果" :image-size="80" />

      <template v-else>
        <div class="flex flex-wrap gap-3 mb-4">
          <div class="stat-box">
            <div class="stat-label">申报统筹（应收）</div>
            <div class="stat-value">{{ fmt(result.declareAmount) }}</div>
            <div class="stat-sub">共 {{ result.declareCount }} 笔</div>
          </div>
          <div class="stat-box">
            <div class="stat-label">审核扣款/拒付</div>
            <div class="stat-value text-danger">{{ fmt(result.deductAmount) }}</div>
          </div>
          <div class="stat-box">
            <div class="stat-label">质量保证金预留</div>
            <div class="stat-value text-warn">{{ fmt(result.reserveAmount) }}</div>
          </div>
          <div class="stat-box">
            <div class="stat-label">本月应拨付</div>
            <div class="stat-value">{{ fmt(result.payableAmount) }}</div>
          </div>
          <div class="stat-box">
            <div class="stat-label">实际已拨付</div>
            <div class="stat-value text-success">{{ fmt(result.allocatedAmount) }}</div>
          </div>
          <div class="stat-box">
            <div class="stat-label">流程状态</div>
            <div class="stat-value text-[16px]">{{ result.statusDesc || '未申报' }}</div>
          </div>
        </div>

        <div class="flex flex-wrap gap-2 mb-3">
          <el-button
            type="primary"
            :disabled="!can('declare')"
            :loading="acting === 'declare'"
            @click="op('declare')"
          >① 月度申报</el-button>
          <el-button
            type="warning"
            :disabled="!can('audit')"
            :loading="acting === 'audit'"
            @click="op('audit')"
          >② 智能审核</el-button>
          <el-button
            type="success"
            :disabled="!can('allocate')"
            :loading="acting === 'allocate'"
            @click="op('allocate')"
          >③ 基金拨付</el-button>
          <el-button
            type="info"
            :disabled="!can('yearSettle')"
            :loading="acting === 'yearSettle'"
            @click="op('yearSettle')"
          >④ 年终清算</el-button>
        </div>

        <el-row :gutter="12">
          <el-col :span="12">
            <div class="font-medium text-[13px] text-gray-600 mb-2">审核扣款明细（{{ (result.deducts || []).length }}）</div>
            <el-table :data="result.deducts || []" size="small" border>
              <el-table-column prop="settlementNo" label="结算清单号" min-width="150" show-overflow-tooltip />
              <el-table-column label="扣款类型" width="100">
                <template #default="{ row }">{{ row.deductType === 1 ? '拒付' : '审核扣款' }}</template>
              </el-table-column>
              <el-table-column label="金额" width="110" align="right">
                <template #default="{ row }">{{ fmt(row.deductAmount) }}</template>
              </el-table-column>
              <el-table-column prop="reason" label="原因" min-width="140" show-overflow-tooltip />
            </el-table>
          </el-col>
          <el-col :span="12">
            <div class="font-medium text-[13px] text-gray-600 mb-2">基金拨付明细（{{ (result.allocations || []).length }}）</div>
            <el-table :data="result.allocations || []" size="small" border>
              <el-table-column prop="allocNo" label="拨付单号" min-width="150" />
              <el-table-column label="类型" width="110">
                <template #default="{ row }">{{ row.allocType === 1 ? '月度拨付' : '质保金返还' }}</template>
              </el-table-column>
              <el-table-column label="金额" width="110" align="right">
                <template #default="{ row }">{{ fmt(row.amount) }}</template>
              </el-table-column>
              <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
            </el-table>
          </el-col>
        </el-row>
      </template>
    </div>

    <!-- 月度结算单列表 -->
    <div class="his-card p-3">
      <div class="font-semibold text-[15px] text-[#0B3B66] mb-2">月度结算单</div>
      <el-table :data="list" size="small" v-loading="loading" highlight-current-row @row-click="onRowClick">
        <el-table-column prop="month" label="账期" width="110" />
        <el-table-column prop="fixMedinsCode" label="定点机构" width="140" />
        <el-table-column label="申报笔数" width="90" align="right">
          <template #default="{ row }">{{ row.declareCount ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="申报统筹" width="120" align="right">
          <template #default="{ row }">{{ fmt(row.declareAmount) }}</template>
        </el-table-column>
        <el-table-column label="审核扣款" width="110" align="right">
          <template #default="{ row }">{{ fmt(row.deductAmount) }}</template>
        </el-table-column>
        <el-table-column label="应拨付" width="120" align="right">
          <template #default="{ row }">{{ fmt(row.payableAmount) }}</template>
        </el-table-column>
        <el-table-column label="已拨付" width="120" align="right">
          <template #default="{ row }">{{ fmt(row.allocatedAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申报时间" width="160">
          <template #default="{ row }">{{ row.declareTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="viewMonth(row.month)">明细</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="flex justify-end mt-3">
        <el-pagination
          :page-sizes="PAGE_SIZES" :page-size="q.size" :current-page="q.page" :total="total"
          layout="total, sizes, prev, pager, next"
          @current-change="(p) => { q.page = p; load() }"
          @size-change="(s) => { q.size = s; q.page = 1; load() }"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  monthlyPage, monthlyResult, monthlyDeclare, monthlyAudit, monthlyAllocate, monthlyYearSettle,
} from '@/api/insurance'
import { PAGE_SIZES } from '@/lib/pagination'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const q = reactive({ page: 1, size: 10 })

const selectedMonth = ref('')
const result = ref({})
const acting = ref('')

function currentMonth() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}

function fmt(n) {
  return (n ?? 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
function statusText(s) {
  return { 1: '已申报', 2: '已审核', 3: '已拨付', 4: '已清算' }[s] || '未申报'
}
function statusType(s) {
  return { 1: 'warning', 2: 'primary', 3: 'success', 4: 'info' }[s] || 'info'
}

function can(step) {
  const s = result.value.status || 0
  return { declare: s === 0, audit: s === 1, allocate: s === 2, yearSettle: s === 3 }[step]
}

async function loadResult() {
  if (!selectedMonth.value) {
    ElMessage.warning('请选择账期')
    return
  }
  const res = await monthlyResult(selectedMonth.value)
  result.value = res.data || {}
}

function resetMonth() {
  selectedMonth.value = currentMonth()
  loadResult()
}

async function op(step) {
  acting.value = step
  try {
    const res = await {
      declare: monthlyDeclare,
      audit: monthlyAudit,
      allocate: monthlyAllocate,
      yearSettle: monthlyYearSettle,
    }[step](selectedMonth.value)
    result.value = res.data || {}
    ElMessage.success('操作成功')
    load()
  } finally {
    acting.value = ''
  }
}

function onRowClick(row) {
  selectedMonth.value = row.month
  loadResult()
}
function viewMonth(month) {
  selectedMonth.value = month
  loadResult()
}

async function load() {
  loading.value = true
  const res = await monthlyPage({ pageNum: q.page, pageSize: q.size })
  loading.value = false
  list.value = res.data.records || []
  total.value = res.data.total || 0
}

onMounted(() => {
  load()
  selectedMonth.value = currentMonth()
  loadResult()
})
</script>

<style scoped>
.stat-box {
  flex: 0 0 180px;
  background: #f7fafd;
  border: 1px solid #e3ecf5;
  border-radius: 10px;
  padding: 10px 14px;
}
.stat-label {
  font-size: 12px;
  color: #7a93ab;
}
.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #0b3b66;
  font-family: var(--font-mono);
  margin-top: 2px;
}
.stat-sub {
  font-size: 11px;
  color: #9db1c4;
}
.text-danger { color: #d64545; }
.text-warn { color: #d4890f; }
.text-success { color: #0e9488; }
</style>
