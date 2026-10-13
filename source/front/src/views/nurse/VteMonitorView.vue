<template>
  <div data-testid="vte-monitor-view">
    <!-- 实时试算 -->
    <el-card class="stat-card" shadow="never">
      <div class="stat-head">
        <span class="stat-title">实时试算（不落库）</span>
        <div data-testid="preview-month">
          <el-date-picker
              v-model="previewMonth"
              :clearable="false"
              placeholder="选择月份"
              style="width: 140px"
              type="month"
              value-format="YYYY-MM"
              @change="loadPreview"/>
        </div>
        <el-button data-testid="btn-preview" @click="loadPreview">试算</el-button>
      </div>
      <div v-if="preview.statMonth" class="stat-items">
        <div class="stat-item">
          <div class="stat-value" data-testid="pv-assess-rate">{{ preview.assessRate ?? '-' }}%</div>
          <div class="stat-label">VTE 风险评估率（目标 ≥{{ preview.assessRateTarget }}%）</div>
          <div class="stat-sub">{{ preview.assessedCount }} / {{ preview.dischargeCount }} 名出院患者</div>
        </div>
        <div class="stat-item">
          <div class="stat-value" data-testid="pv-high-risk-rate">{{ preview.highRiskRate ?? '-' }}%</div>
          <div class="stat-label">中高危占比（占已评估）</div>
          <div class="stat-sub">{{ preview.highRiskCount }} / {{ preview.assessedCount }} 人</div>
        </div>
        <div class="stat-item">
          <div :class="{ 'stat-value-danger': Number(preview.preventRate) < Number(preview.preventRateTarget) }"
               class="stat-value"
               data-testid="pv-prevent-rate">
            {{ preview.preventRate ?? '-' }}%
          </div>
          <div class="stat-label">预防措施落实率（目标 ≥{{ preview.preventRateTarget }}%）</div>
          <div class="stat-sub">{{ preview.preventDoneCount }} / {{ preview.highRiskCount }} 名中高危已落实</div>
        </div>
        <div class="stat-item">
          <div class="stat-value" data-testid="pv-vte-rate">{{ preview.vteIncidenceRate ?? '-' }}%</div>
          <div class="stat-label">院内 VTE 发生率</div>
          <div class="stat-sub">{{ preview.vteEventCount }} / {{ preview.dischargeCount }} · 出血 {{
              preview.bleedCount
            }} 例
          </div>
        </div>
      </div>
      <div v-else class="muted">选择月份后点「试算」</div>
    </el-card>

    <!-- 快照列表 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="statsQuery" inline @submit.prevent>
          <el-form-item label="统计月份">
            <div data-testid="stats-month">
              <el-date-picker v-model="statsQuery.statMonth" clearable placeholder="统计月份" style="width: 140px"
                              type="month" value-format="YYYY-MM"/>
            </div>
          </el-form-item>
          <el-form-item label="统计范围">
            <div data-testid="stats-scope">
              <el-select v-model="statsQuery.scopeType" clearable placeholder="统计范围" style="width: 130px">
                <el-option :value="1" label="全院"/>
                <el-option :value="2" label="科室"/>
              </el-select>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button data-testid="btn-stats-query" type="primary" @click="loadStats">查询</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：生成/导出统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'nursing:vte:statGenerate'" data-testid="btn-generate" type="primary"
                     @click="openGenerateDialog">生成月度快照
          </el-button>
          <el-button v-perm="'nursing:vte:statExport'" data-testid="btn-export" @click="doExport">导出 CSV</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="statsLoading" :data="statsRows" :max-height="tableMaxHeight" data-testid="stats-table"
                stripe>
        <el-table-column label="统计月份" prop="statMonth" width="100"/>
        <el-table-column label="范围" width="80">
          <template #default="{ row }">{{ row.scopeTypeText }}</template>
        </el-table-column>
        <el-table-column label="科室" prop="deptName" show-overflow-tooltip width="130"/>
        <el-table-column align="right" label="评估率" width="140">
          <template #default="{ row }">
            <span :class="{ 'under-target': Number(row.assessRate) < Number(row.assessRateTarget) }">{{
                row.assessRate
              }}%</span>
            <span class="muted">（{{ row.assessedCount }}/{{ row.dischargeCount }}）</span>
          </template>
        </el-table-column>
        <el-table-column align="right" label="中高危" width="130">
          <template #default="{ row }">
            {{ row.highRiskRate }}%
            <span class="muted">（{{ row.highRiskCount }} 人）</span>
          </template>
        </el-table-column>
        <el-table-column align="right" label="措施落实率" width="150">
          <template #default="{ row }">
            <span :class="{ 'under-target': Number(row.preventRate) < Number(row.preventRateTarget) }"
                  data-testid="cell-prevent-rate">{{ row.preventRate }}%</span>
            <span class="muted">（{{ row.preventDoneCount }}/{{ row.highRiskCount }}）</span>
          </template>
        </el-table-column>
        <el-table-column align="right" label="院内 VTE 发生率" width="150">
          <template #default="{ row }">
            <span data-testid="cell-vte-rate">{{ row.vteIncidenceRate }}%</span>
            <span class="muted">（{{ row.vteEventCount }}/{{ row.dischargeCount }}）</span>
          </template>
        </el-table-column>
        <el-table-column align="right" label="出血例数" prop="bleedCount" width="90"/>
        <el-table-column label="生成人" prop="generateBy" width="100"/>
        <el-table-column label="生成时间" prop="generateTime" width="170"/>
      </el-table>
      <!-- ⚠ layout 含 sizes 时 page-size 必须 v-model + @size-change，否则 EP 2.14 把整条分页渲染成 null -->
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="statsQuery.pageNum"
            v-model:page-size="statsQuery.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="statsTotal"
            layout="total, sizes, prev, pager, next"
            @current-change="loadStats"
            @size-change="onStatsSizeChange"/>
      </div>
    </el-card>

    <!-- VTE 事件登记 -->
    <el-card class="event-card" shadow="never">
      <div class="stat-head">
        <span class="stat-title">VTE 事件登记（发生率的分子）</span>
        <div class="toolbar-right">
          <el-button v-perm="'nursing:vte:eventEdit'" data-testid="btn-add-event" type="primary" @click="openEventForm">
            登记事件
          </el-button>
        </div>
      </div>
      <el-table v-loading="eventLoading" :data="eventRows" border data-testid="event-table" stripe>
        <el-table-column label="事件编号" prop="eventNo" width="150"/>
        <el-table-column label="患者" prop="patientName" width="110"/>
        <el-table-column label="事件类型" width="150">
          <template #default="{ row }">{{ row.eventTypeText }}</template>
        </el-table-column>
        <el-table-column label="发生时机" width="120">
          <template #default="{ row }">
            <el-tag :type="row.counted ? 'danger' : 'info'" disable-transitions>{{ row.onsetTypeText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="确诊日期" prop="diagnoseDate" width="110"/>
        <el-table-column label="诊断依据" width="130">
          <template #default="{ row }">{{ row.diagnosisBasisText || '-' }}</template>
        </el-table-column>
        <el-table-column label="部位" min-width="140" prop="thrombusSite" show-overflow-tooltip/>
        <el-table-column label="转归" width="80">
          <template #default="{ row }">{{ row.outcomeText || '-' }}</template>
        </el-table-column>
        <el-table-column align="center" label="计入发生率" width="100">
          <template #default="{ row }">
            <el-tag :type="row.counted ? 'danger' : 'info'" disable-transitions>{{
                row.counted ? '计入' : '不计入'
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="登记人" prop="reporterName" width="100"/>
      </el-table>
      <el-pagination
          v-model:current-page="eventQuery.pageNum"
          v-model:page-size="eventQuery.pageSize"
          :page-sizes="PAGE_SIZES"
          :total="eventTotal"
          class="pager"
          layout="total, sizes, prev, pager, next"
          @current-change="loadEvents"
          @size-change="onEventSizeChange"/>
    </el-card>

    <!-- 生成快照：表单在 VteMonitorGenerate -->
    <VteMonitorGenerate v-model="genDialogVisible" @saved="onGenerated"/>

    <!-- 事件登记：表单在 VteMonitorEventAdd（只有新增） -->
    <VteMonitorEventAdd v-model="eventVisible" @saved="loadEvents"/>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {currentMonth} from '@/lib/vte'
import {
  exportVteStatsCsv,
  listVteEventPage,
  listVteStatsPage,
  previewVteStats
} from '@/api/vte'
// 拆分出的弹框组件
import VteMonitorGenerate from './VteMonitorGenerate.vue'
import VteMonitorEventAdd from './VteMonitorEventAdd.vue'

const previewMonth = ref(currentMonth())
const preview = ref({})

async function loadPreview() {
  try {
    const res = await previewVteStats(previewMonth.value)
    preview.value = res?.data || {}
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
}

const statsQuery = reactive({statMonth: null, scopeType: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()
const statsRows = ref([])
const statsTotal = ref(0)
const statsLoading = ref(false)

async function loadStats() {
  statsLoading.value = true
  try {
    const res = await listVteStatsPage({...statsQuery})
    statsRows.value = res?.data?.records || []
    statsTotal.value = Number(res?.data?.total || 0)
  } finally {
    statsLoading.value = false
  }
}

function onStatsSizeChange() {
  statsQuery.pageNum = 1
  loadStats()
}

const genDialogVisible = ref(false)

function openGenerateDialog() {
  genDialogVisible.value = true
}

/** 生成成功后：台账筛选月份跟着走，立刻能查到刚生成的行（试算也一起刷） */
function onGenerated(m) {
  statsQuery.statMonth = m
  loadStats()
  loadPreview()
}

async function doExport() {
  try {
    const res = await exportVteStatsCsv({statMonth: statsQuery.statMonth, scopeType: statsQuery.scopeType})
    const text = res?.data || ''
    if (!text) {
      ElMessage.warning('没有可导出的数据');
      return
    }
    const blob = new Blob([text], {type: 'text/csv;charset=utf-8'})
    const a = document.createElement('a')
    a.href = URL.createObjectURL(blob)
    a.download = `VTE防控指标_${statsQuery.statMonth || '全部'}.csv`
    a.click()
    URL.revokeObjectURL(a.href)
    ElMessage.success('已导出')
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
}

// ========== VTE 事件 ==========
const eventQuery = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE})
const eventRows = ref([])
const eventTotal = ref(0)
const eventLoading = ref(false)

async function loadEvents() {
  eventLoading.value = true
  try {
    const res = await listVteEventPage({...eventQuery})
    eventRows.value = res?.data?.records || []
    eventTotal.value = Number(res?.data?.total || 0)
  } finally {
    eventLoading.value = false
  }
}

function onEventSizeChange() {
  eventQuery.pageNum = 1
  loadEvents()
}

// 登记表单在 VteMonitorEventAdd（打开时的重置与在院患者懒加载都在那边）
const eventVisible = ref(false)

function openEventForm() {
  eventVisible.value = true
}

onMounted(() => {
  loadStats()
  loadEvents()
  loadPreview()
})
</script>

<style scoped>
.stat-card {
  margin-bottom: 12px;
}

.event-card {
  margin-top: 12px;
}

.stat-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.stat-title {
  font-weight: 600;
}

.stat-items {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.stat-item {
  flex: 1;
  min-width: 190px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px;
}

.stat-value {
  font-size: 22px;
  font-weight: 600;
}

.stat-value-danger {
  color: #f56c6c;
}

.stat-label {
  color: #606266;
  font-size: 12px;
  margin-top: 4px;
}

.stat-sub {
  color: #909399;
  font-size: 12px;
  margin-top: 2px;
}

.toolbar-right {
  margin-left: auto;
}

.pager {
  margin-top: 10px;
  justify-content: flex-end;
}

.muted {
  color: #909399;
  font-size: 12px;
}

.under-target {
  color: #e6a23c;
  font-weight: 600;
}
</style>
