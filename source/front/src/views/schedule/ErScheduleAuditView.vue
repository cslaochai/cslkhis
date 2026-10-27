<template>
  <div data-testid="er-audit-view">
    <!-- 待审统计：口径 = 当前登录人作为科室指定审核人的全部待审核排班 -->
    <div class="mb-3 grid grid-cols-2 gap-3 sm:grid-cols-3" data-testid="er-audit-stats">
      <div
          class="rounded-md border border-slate-200 bg-white px-4 py-1.5 text-center transition duration-200 hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md cursor-default"
          data-testid="er-audit-stat-total"
          title="待我审核的排班总行数（急诊三科等由科室主档 schedule_auditor_id 指定到我）">
        <p class="text-xl font-semibold" :class="stats.total > 0 ? 'text-orange-500' : 'text-slate-800'">{{
            stats.total
          }}</p>
        <p class="text-xs text-slate-400">待审总数</p>
      </div>
      <div
          class="rounded-md border border-slate-200 bg-white px-4 py-1.5 text-center transition duration-200 hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md cursor-default"
          data-testid="er-audit-stat-today" title="排班日期为今天的待审行数">
        <p class="text-xl font-semibold text-amber-600">{{ stats.today }}</p>
        <p class="text-xs text-slate-400">今日待审</p>
      </div>
      <div
          class="rounded-md border border-slate-200 bg-white px-4 py-1.5 text-center transition duration-200 hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md cursor-default"
          data-testid="er-audit-stat-week" title="排班日期在本周内的待审行数">
        <p class="text-xl font-semibold text-blue-600">{{ stats.week }}</p>
        <p class="text-xs text-slate-400">本周待审</p>
      </div>
    </div>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline @submit.prevent>
          <el-form-item label="科室">
            <el-select v-model="searchForm.deptId" clearable filterable placeholder="全部科室" style="width: 180px"
                       data-testid="er-audit-dept">
              <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id"/>
            </el-select>
          </el-form-item>
          <el-form-item label="医生">
            <el-input v-model="searchForm.keyword" clearable placeholder="姓名 / 工号" style="width: 160px"
                      data-testid="er-audit-keyword" @keyup.enter="handleSearch"/>
          </el-form-item>
          <el-form-item label="排班日期">
            <el-date-picker v-model="searchForm.dateRange" end-placeholder="结束日期" range-separator="至"
                            start-placeholder="开始日期" style="width: 260px" type="daterange"
                            value-format="YYYY-MM-DD"/>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" data-testid="er-audit-search" type="primary" @click="handleSearch">筛选
            </el-button>
            <el-button :icon="Refresh" data-testid="er-audit-reset" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：批量审核靠右，与筛选条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button :disabled="!selectedRows.length" :icon="CircleCheck" :loading="batchLoading"
                     data-testid="er-audit-batch" type="success" @click="handleBatchAudit">
            批量审核发布{{ selectedRows.length ? `（${selectedRows.length}）` : '' }}
          </el-button>
          <el-button :icon="Refresh" :loading="loading" data-testid="er-audit-refresh" @click="loadData">刷新
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div data-testid="er-audit-table">
        <el-table v-loading="loading" :data="pagedRows" :max-height="tableMaxHeight" stripe
                  @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="45"/>
          <el-table-column label="排班日期" min-width="130">
            <template #default="{ row }">
              <span class="font-medium text-slate-700">{{ row.scheduleDate }}</span>
              <span class="ml-1 text-xs text-slate-400">{{ row.weekDayText }}</span>
            </template>
          </el-table-column>
          <el-table-column label="科室" min-width="120" prop="deptName"/>
          <el-table-column label="医生" min-width="140">
            <template #default="{ row }">
              <span class="font-medium text-slate-700">{{ row.employeeName }}</span>
              <span class="ml-1 text-xs text-slate-400">{{ row.empCode }}</span>
            </template>
          </el-table-column>
          <el-table-column label="班次" min-width="170">
            <template #default="{ row }">
              {{ row.shiftName || '—' }} {{ row.startTime }}~{{ row.endTime }}
            </template>
          </el-table-column>
          <el-table-column label="响应形态" prop="attendModeText" width="100"/>
          <el-table-column label="发布态" width="100">
            <template #default="{ row }">
              <el-tag effect="plain" size="small" type="warning">{{ row.publishStatusText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="120" prop="remark" show-overflow-tooltip/>
          <el-table-column fixed="right" label="操作" width="180">
            <template #default="{ row }">
              <el-button :icon="CircleCheck" data-testid="er-audit-row-audit" link type="success"
                         @click="handleAudit(row)">
                审核发布
              </el-button>
              <el-button :icon="CloseBold" data-testid="er-audit-row-reject" link type="danger"
                         @click="handleReject(row)">
                驳回
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-empty v-if="!loading && filteredRows.length === 0" :image-size="60"
                description="没有待你审核的排班"/>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="filteredRows.length"
            layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {CircleCheck, CloseBold, Refresh, Search} from '@element-plus/icons-vue'
import {auditPublishStaffSchedule, getStaffSchedulePendingApproval, rejectStaffSchedule,} from '@/api/staffSchedule'
import {ORG_UNIT_TYPE_DEPT} from '@/lib/staffSchedule'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'

/**
 * 急诊排班审核（医务科专用，独立于科主任的「急诊排班」编辑页）。
 *
 * 数据源 = /system/staffSchedule/pendingApproval：后端按「科室主档 schedule_auditor_id = 当前登录人」
 * 收口返回待审核行（排除门诊派生行与护理病区行），因此本页只要求 staff:schedule:audit 权限，
 * 不持有 staff:schedule:list/add/edit/delete —— 审核人只审不改，编辑动作全部留在科主任的排班页。
 * 科室/医生/日期筛选与分页都在前端做：一个人的待审队列量级很小，一次拉全再筛最直白。
 */

const loading = ref(false)
const batchLoading = ref(false)
const tableData = ref([])
const selectedRows = ref([])
const pagination = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE})

const searchForm = reactive({
  deptId: null,
  keyword: '',
  dateRange: [],
})

const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const loadData = async () => {
  loading.value = true
  try {
    const res = await getStaffSchedulePendingApproval({
      orgType: ORG_UNIT_TYPE_DEPT,
      pageNum: 1,
      pageSize: 500,
    })
    if (res.code === 200) {
      tableData.value = res.data?.records || []
    }
  } catch (e) /* 错误已由全局拦截器统一提示 */ {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

// 科室下拉从数据里来（审核人名下有哪些待审科室就列哪些，不额外要字典接口）
const deptOptions = computed(() => {
  const map = new Map()
  for (const r of tableData.value) {
    if (r.deptId && !map.has(r.deptId)) map.set(r.deptId, {id: r.deptId, deptName: r.deptName})
  }
  return Array.from(map.values())
})

const filteredRows = computed(() => {
  const kw = (searchForm.keyword || '').trim().toLowerCase()
  const [start, end] = searchForm.dateRange || []
  return tableData.value.filter((r) => {
    if (searchForm.deptId && r.deptId !== searchForm.deptId) return false
    if (kw && !(`${r.employeeName || ''}${r.empCode || ''}`.toLowerCase().includes(kw))) return false
    const d = String(r.scheduleDate || '').slice(0, 10)
    if (start && d < start) return false
    if (end && d > end) return false
    return true
  })
})

const pagedRows = computed(() => {
  const s = (pagination.pageNum - 1) * pagination.pageSize
  return filteredRows.value.slice(s, s + pagination.pageSize)
})

const stats = computed(() => {
  const today = localToday()
  const monday = localMonday()
  const rows = tableData.value
  return {
    total: rows.length,
    today: rows.filter((r) => String(r.scheduleDate || '').slice(0, 10) === today).length,
    week: rows.filter((r) => {
      const d = String(r.scheduleDate || '').slice(0, 10)
      return d >= monday && d <= localSunday()
    }).length,
  }
})

// 本地日期手拼 y-m-d（禁 toISOString：UTC+8 会漂移一天）
function localToday() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function localMonday() {
  const d = new Date()
  const day = d.getDay() === 0 ? 7 : d.getDay()
  d.setDate(d.getDate() - (day - 1))
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function localSunday() {
  const d = new Date()
  const day = d.getDay() === 0 ? 7 : d.getDay()
  d.setDate(d.getDate() + (7 - day))
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const handleSearch = () => {
  pagination.pageNum = 1
}
const handleReset = () => {
  searchForm.deptId = null
  searchForm.keyword = ''
  searchForm.dateRange = []
  pagination.pageNum = 1
}
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

/** 审核发布：待审核 → 已发布（计入在岗、进公开值班表） */
const handleAudit = async (row) => {
  const res = await auditPublishStaffSchedule(row.id)
  if (res.code === 200) {
    ElMessage.success('已审核发布')
    await loadData()
  }
}

/** 驳回：待审核 → 草稿（原因写进变更留痕，科主任在排班页能看到） */
const handleReject = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt('请填写驳回原因（科主任会在留痕里看到）', `驳回 ${row.employeeName} 的排班`, {
      confirmButtonText: '确定驳回',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：与科室会议冲突，请调整时段后再报',
      inputValidator: (v) => (v && v.trim() ? true : '驳回原因不能为空'),
    })
    const res = await rejectStaffSchedule({id: row.id, reason: value})
    if (res.code === 200) {
      ElMessage.success('已驳回')
      await loadData()
    }
  } catch (e) /* 取消不提示，错误已由全局拦截器统一提示 */ {
  }
}

/** 批量审核发布：逐条调后端（量小，串行比批接口直白，单条失败不影响已成功的） */
const handleBatchAudit = async () => {
  const rows = selectedRows.value
  if (!rows.length) {
    ElMessage.warning('请先勾选要审核发布的排班')
    return
  }
  batchLoading.value = true
  let ok = 0
  try {
    for (const r of rows) {
      const res = await auditPublishStaffSchedule(r.id)
      if (res.code === 200) ok += 1
    }
    ElMessage.success(`已审核发布 ${ok}/${rows.length} 条`)
    await loadData()
  } catch (e) /* 全局拦截器已提示 */ {
  } finally {
    batchLoading.value = false
  }
}

onMounted(loadData)
</script>
