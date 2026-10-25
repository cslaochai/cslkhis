<template>
  <div
      class="flex flex-col"
      data-testid="schedule-overview"
      style="height: var(--his-page-h); min-height: 420px;"
  >
    <!-- 查询行：周期切换 + 矩阵切片条件（接口只按周取数，这三个条件切的是本周已返回的矩阵行） -->
    <div class="mb-3 flex shrink-0 flex-wrap items-center gap-3">
      <el-button-group>
        <el-button data-testid="btn-prev-week" @click="shiftWeek(-7)">上一周</el-button>
        <el-button data-testid="btn-this-week" @click="shiftWeek(0)">本周</el-button>
        <el-button data-testid="btn-next-week" @click="shiftWeek(7)">下一周</el-button>
      </el-button-group>
      <span class="text-sm font-medium text-slate-600" data-testid="week-label">{{ weekLabel }}</span>
      <el-select v-model="orgTypeFilter" :fit-input-width="false" class="!w-36" clearable
                 data-testid="filter-org-type" placeholder="全部单元类型">
        <el-option v-for="o in ORG_UNIT_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
      </el-select>
      <el-input v-model="orgKeyword" :prefix-icon="Search" class="!w-52" clearable
                data-testid="filter-org-name" placeholder="搜索单元名称"/>
      <el-switch v-model="onlyShortfall" active-text="只看有缺口" data-testid="filter-only-shortfall"/>
      <span class="ml-auto text-xs text-slate-400" data-testid="matrix-row-count">
        当前显示 {{ matrixRows.length }} / {{ allMatrixRows.length }} 个单元
      </span>
    </div>

    <!-- 指标卡行：本周有出勤的岗位才上图；全 0 的岗位不占位置。右侧三张是全院层面的警示/汇总 -->
    <div class="mb-3 grid shrink-0 grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6" data-testid="staff-type-cards">
      <div v-for="card in activeStaffCards" :key="card.staffType"
           class="rounded-md border border-slate-200 bg-white px-3 py-2 text-center">
        <p :data-testid="`card-staff-type-${card.staffType}`" class="text-xl font-semibold text-slate-800">{{
            card.total
          }}</p>
        <p class="text-xs text-slate-500">{{ card.label }}</p>
        <p class="text-[10px] text-slate-400">本周出勤人次</p>
      </div>

      <!-- 人力缺口：实际在岗低于配置标准的累计缺员 -->
      <div
          :class="['rounded-md border px-3 py-2 text-center', weekShortfallTotal > 0 ? 'border-red-200 bg-red-50' : 'border-slate-200 bg-white']"
          data-testid="card-shortfall">
        <p :class="weekShortfallTotal > 0 ? 'text-xl font-semibold text-red-600' : 'text-xl font-semibold text-slate-800'">
          {{ weekShortfallTotal }}
        </p>
        <p class="text-xs text-slate-500">人力缺口（人）</p>
        <p class="text-[10px] text-slate-400">{{ weekShortfallCells }} 个单元·日缺员</p>
      </div>

      <!-- 门诊出诊：号源是另一套度量，单独成卡，不与在岗人次并列 -->
      <div class="rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-center"
           data-testid="card-clinic">
        <p class="text-xl font-semibold text-emerald-700">
          {{ clinicWeek.used }}<span class="text-sm font-normal text-emerald-600/70"> / {{ clinicWeek.total }}</span>
        </p>
        <p class="text-xs text-emerald-700/80">已挂 / 总号源</p>
        <p class="text-[10px] text-emerald-600/70">
          {{ clinicWeek.shiftCount }} 个出诊班 · {{ clinicWeek.pct == null ? '无号源' : `利用率 ${clinicWeek.pct}%` }}
        </p>
      </div>

      <!-- 停诊班次 -->
      <div
          :class="['rounded-md border px-3 py-2 text-center', clinicWeek.stopped > 0 ? 'border-red-200 bg-red-50' : 'border-slate-200 bg-white']"
          data-testid="card-stopped">
        <p :class="clinicWeek.stopped > 0 ? 'text-xl font-semibold text-red-600' : 'text-xl font-semibold text-slate-800'">
          {{ clinicWeek.stopped }}
        </p>
        <p class="text-xs text-slate-500">停诊班次</p>
        <p class="text-[10px] text-slate-400">点击出诊计划查看明细</p>
      </div>
    </div>

    <!-- 两类事实分视图：科室在岗（人次）与门诊出诊（号源）量纲不同，真实 HIS 也从不混在一张矩阵里 -->
    <el-tabs v-model="activeTab" class="min-h-0 flex-1" data-testid="overview-tabs">
      <!-- 视图一：排班单元 × 日期 在岗人次矩阵，点格子看当日排班名单 -->
      <el-tab-pane label="科室在岗" name="unit" class="min-h-0">
        <div class="flex min-h-0 flex-1 flex-col rounded-md border border-slate-200 bg-white" data-testid="unit-matrix">
          <div class="flex shrink-0 items-center justify-between px-3 pt-2">
            <h3 class="text-sm font-semibold text-slate-700">排班单元 × 日期在岗人次</h3>
            <span class="text-xs text-slate-400">点格子看该单元当日排班名单；红格 = 当日缺员</span>
          </div>
          <div v-loading="loading" class="min-h-0 flex-1 overflow-auto overscroll-contain px-3 pb-2">
            <table class="w-full min-w-[880px] border-collapse text-sm">
              <thead>
              <tr class="text-slate-500">
                <th class="sticky left-0 top-0 z-20 bg-slate-50 px-3 py-2 text-left font-medium">单元</th>
                <th v-for="d in days" :key="d.date"
                    :class="{ 'bg-blue-50 text-blue-600': d.date === todayStr }"
                    class="sticky top-0 z-10 bg-slate-50 px-2 py-2 text-center font-medium">
                  {{ d.label }}<br/><span class="text-xs font-normal">{{ d.date.slice(5) }}</span>
                </th>
                <th class="sticky right-0 top-0 z-20 bg-slate-50 px-3 py-2 text-center font-medium">周合计(人次)</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="row in matrixRows" :key="row.key" class="border-t border-slate-100">
                <td class="sticky left-0 z-10 border-r border-slate-100 bg-white px-3 py-1.5 text-slate-700">
                  {{ row.orgName }}
                  <span class="ml-1 text-xs text-slate-400">{{ orgUnitTypeText(row.orgType) }}</span>
                </td>
                <td v-for="d in days" :key="d.date"
                    :class="['px-1 py-1 text-center', { 'bg-blue-50/50': d.date === todayStr }]">
                  <button :data-testid="`matrix-cell-${row.orgId}-${d.date}`"
                          :class="['relative inline-flex min-w-9 cursor-pointer items-center justify-center rounded px-1.5 py-1 transition hover:bg-blue-100', cellTextClass(row, d.date)]"
                          type="button"
                          @click="openCellDetail(row, d.date)">
                    <span>{{ row.cells[d.date] ?? 0 }}</span>
                    <span v-if="row.shortfalls[d.date]"
                          :title="row.shortfallTitles[d.date]"
                          class="absolute -right-2 -top-1 rounded-full bg-red-500 px-1 text-[10px] leading-3 text-white">{{
                        row.shortfalls[d.date]
                      }}</span>
                  </button>
                </td>
                <td class="sticky right-0 z-10 border-l border-slate-100 bg-white px-3 py-1.5 text-center font-medium text-slate-700">
                  {{ row.weekTotal }}
                </td>
              </tr>
              <tr v-if="!matrixRows.length && !loading">
                <td :colspan="days.length + 2" class="px-3 py-8 text-center text-sm text-slate-400"
                    data-testid="matrix-empty">
                  没有符合条件的排班单元
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </el-tab-pane>

      <!-- 视图二：门诊出诊计划（号源）全院按日聚合；按科室/医生明细请到门诊排班页 -->
      <el-tab-pane label="门诊出诊计划" name="clinic" class="min-h-0">
        <div class="flex min-h-0 flex-1 flex-col rounded-md border border-slate-200 bg-white" data-testid="clinic-panel">
          <div class="flex shrink-0 flex-wrap items-center justify-between gap-2 px-3 pt-2">
            <h3 class="text-sm font-semibold text-slate-700">门诊出诊计划 · 号源（全院按日汇总）</h3>
            <span class="text-xs text-slate-400">按科室 / 医生的出诊与号源明细见「门诊排班」页</span>
          </div>
          <div v-loading="loading" class="min-h-0 flex-1 overflow-auto overscroll-contain px-3 pb-2 pt-2">
            <table class="w-full min-w-[880px] border-collapse text-sm">
              <thead>
              <tr class="text-slate-500">
                <th class="sticky left-0 top-0 z-20 bg-emerald-50 px-3 py-2 text-left font-medium">项目</th>
                <th v-for="d in days" :key="d.date"
                    :class="{ 'bg-blue-50 text-blue-600': d.date === todayStr }"
                    class="sticky top-0 z-10 bg-emerald-50/60 px-2 py-2 text-center font-medium">
                  {{ d.label }}<br/><span class="text-xs font-normal">{{ d.date.slice(5) }}</span>
                </th>
                <th class="sticky right-0 top-0 z-20 bg-emerald-50/60 px-3 py-2 text-center font-medium">周合计</th>
              </tr>
              </thead>
              <tbody>
              <!-- 出诊班次数 -->
              <tr class="border-t border-slate-100">
                <td class="sticky left-0 z-10 border-r border-slate-100 bg-emerald-50/40 px-3 py-1.5 text-slate-700">出诊班次</td>
                <td v-for="d in days" :key="d.date" class="px-2 py-1.5 text-center">
                  <span v-if="clinicByDay[d.date]" class="text-slate-700">{{ clinicByDay[d.date].shiftCount }}</span>
                  <span v-else class="text-slate-200">—</span>
                </td>
                <td class="sticky right-0 z-10 border-l border-slate-100 bg-emerald-50/40 px-3 py-1.5 text-center font-medium text-slate-700">
                  {{ clinicWeek.shiftCount }}
                </td>
              </tr>
              <!-- 已挂 / 总号源 -->
              <tr class="border-t border-slate-100" data-testid="clinic-row">
                <td class="sticky left-0 z-10 border-r border-slate-100 bg-emerald-50/40 px-3 py-1.5 text-emerald-700">已挂 / 总号源</td>
                <td v-for="d in days" :key="d.date" class="px-2 py-1.5 text-center">
                  <template v-if="clinicByDay[d.date]">
                    <span class="relative inline-block">
                      <span class="text-emerald-700">{{ clinicByDay[d.date].used }}/{{ clinicByDay[d.date].total }}</span>
                      <span v-if="clinicByDay[d.date].stoppedCount > 0"
                            :title="`停诊 ${clinicByDay[d.date].stoppedCount} 个班次`"
                            class="absolute -right-3 -top-1.5 rounded-full bg-red-500 px-1 text-[10px] leading-3 text-white">{{
                          clinicByDay[d.date].stoppedCount
                        }}</span>
                    </span>
                  </template>
                  <span v-else class="text-slate-200">—</span>
                </td>
                <td class="sticky right-0 z-10 border-l border-slate-100 bg-emerald-50/40 px-3 py-1.5 text-center font-medium text-emerald-700">
                  {{ clinicWeek.used }}/{{ clinicWeek.total }}
                </td>
              </tr>
              <!-- 号源利用率 -->
              <tr class="border-t border-slate-100">
                <td class="sticky left-0 z-10 border-r border-slate-100 bg-emerald-50/40 px-3 py-1.5 text-slate-700">号源利用率</td>
                <td v-for="d in days" :key="d.date" class="px-2 py-1.5 text-center">
                  <div v-if="clinicByDay[d.date] && clinicByDay[d.date].total > 0" class="mx-auto flex w-20 flex-col items-center gap-0.5">
                    <el-progress :percentage="clinicPct(clinicByDay[d.date])" :stroke-width="5" :show-text="false"
                                 :color="clinicPctColor(clinicPct(clinicByDay[d.date]))" class="!w-full"/>
                    <span class="text-[11px] text-slate-500">{{ clinicPct(clinicByDay[d.date]) }}%</span>
                  </div>
                  <span v-else class="text-slate-200">—</span>
                </td>
                <td class="sticky right-0 z-10 border-l border-slate-100 bg-emerald-50/40 px-3 py-1.5 text-center font-medium text-slate-700">
                  {{ clinicWeek.pct == null ? '—' : clinicWeek.pct + '%' }}
                </td>
              </tr>
              <!-- 停诊班次 -->
              <tr class="border-t border-slate-100">
                <td class="sticky left-0 z-10 border-r border-slate-100 bg-emerald-50/40 px-3 py-1.5 text-slate-700">停诊班次</td>
                <td v-for="d in days" :key="d.date" class="px-2 py-1.5 text-center">
                  <span v-if="clinicByDay[d.date] && clinicByDay[d.date].stoppedCount > 0"
                        class="font-semibold text-red-600">{{ clinicByDay[d.date].stoppedCount }}</span>
                  <span v-else class="text-slate-200">—</span>
                </td>
                <td class="sticky right-0 z-10 border-l border-slate-100 bg-emerald-50/40 px-3 py-1.5 text-center font-medium"
                    :class="clinicWeek.stopped > 0 ? 'text-red-600' : 'text-slate-700'">
                  {{ clinicWeek.stopped }}
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </el-tab-pane>

      <!-- 视图三：全岗位月度工时（医生/护士/医技/行政统一统计） -->
      <el-tab-pane label="全岗位工时" name="workload" class="min-h-0">
        <div class="flex min-h-0 flex-1 flex-col rounded-md border border-slate-200 bg-white">
          <div class="flex shrink-0 items-center justify-between px-3 pt-2">
            <h3 class="text-sm font-semibold text-slate-700">全岗位月度工时统计</h3>
            <div class="flex items-center gap-2">
              <el-date-picker v-model="workloadMonth" type="month"
                              value-format="YYYY-MM" placeholder="选择月份"
                              size="small" style="width: 120px" @change="loadWorkload"/>
            </div>
          </div>
          <div v-loading="workloadLoading" class="min-h-0 flex-1 overflow-auto px-3 pb-2">
            <el-table :data="workloadRows" stripe border size="small">
              <el-table-column label="工号" prop="empCode" width="90"/>
              <el-table-column label="姓名" prop="employeeName" width="100"/>
              <el-table-column label="岗位" prop="staffTypeName" width="90"/>
              <el-table-column label="科室" prop="deptName" min-width="120" show-overflow-tooltip/>
              <el-table-column align="right" label="出勤天数" prop="workDays" width="90"/>
              <el-table-column align="right" label="总工时(h)" prop="totalHours" width="100"/>
              <el-table-column align="right" label="夜班数" prop="nightDays" width="80"/>
              <el-table-column align="right" label="休息天数" prop="restDays" width="90"/>
              <el-table-column align="right" label="加班(h)" prop="overtimeHours" width="90"/>
            </el-table>
            <el-empty v-if="!workloadLoading && workloadRows.length === 0" description="暂无工时数据（后端接口待开发）"/>
          </div>
        </div>
      </el-tab-pane>

      <!-- 视图四：排班变更日志（全岗位留痕查询） -->
      <el-tab-pane label="变更日志" name="logs" class="min-h-0">
        <div class="flex min-h-0 flex-1 flex-col rounded-md border border-slate-200 bg-white">
          <div class="shrink-0 px-3 pt-2">
            <div class="flex flex-wrap items-end gap-3">
              <el-date-picker v-model="logSearchForm.dateRange" type="daterange"
                              value-format="YYYY-MM-DD" start-placeholder="开始" end-placeholder="结束"
                              size="small"/>
              <el-input v-model="logSearchForm.employeeName" clearable size="small"
                        placeholder="人员姓名" class="!w-40"/>
              <el-select v-model="logSearchForm.changeType" clearable size="small"
                         placeholder="操作类型" class="!w-36">
                <el-option label="新建" value="CREATE"/>
                <el-option label="修改" value="UPDATE"/>
                <el-option label="删除" value="DELETE"/>
                <el-option label="提交" value="SUBMIT"/>
                <el-option label="发布" value="PUBLISH"/>
                <el-option label="撤回" value="RECALL"/>
                <el-option label="调班" value="ADJUST"/>
              </el-select>
              <el-button size="small" type="primary" @click="loadLogs">查询</el-button>
            </div>
          </div>
          <div v-loading="logLoading" class="min-h-0 flex-1 overflow-auto px-3 pb-2 pt-2">
            <el-table :data="logRows" stripe border size="small">
              <el-table-column label="日期" prop="scheduleDate" width="110"/>
              <el-table-column label="人员" prop="employeeName" width="100"/>
              <el-table-column label="单元" prop="orgName" min-width="140" show-overflow-tooltip/>
              <el-table-column label="操作类型" prop="changeTypeText" width="100"/>
              <el-table-column label="变更前" prop="fromText" min-width="150" show-overflow-tooltip/>
              <el-table-column label="变更后" prop="toText" min-width="150" show-overflow-tooltip/>
              <el-table-column label="操作人" prop="operatorName" width="100"/>
              <el-table-column label="操作时间" prop="createTime" width="160"/>
            </el-table>
            <el-empty v-if="!logLoading && logRows.length === 0" description="暂无变更记录"/>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <div class="mt-3 grid shrink-0 grid-cols-1 gap-4 xl:grid-cols-2">
      <!-- 人力缺口明细：整行可点，点开看该单元当日排班名单（缺口就缺在名单里的哪个班） -->
      <div class="rounded-md border border-slate-200 bg-white p-3" data-testid="shortfall-panel">
        <div class="mb-2 flex items-center justify-between">
          <h3 class="text-sm font-semibold text-slate-700">
            人力缺口（实际在岗低于配置标准）
            <span v-if="weekShortfallTotal > 0" class="ml-1 text-xs font-normal text-red-600">共缺 {{ weekShortfallTotal }} 人</span>
          </h3>
          <span class="text-xs text-slate-400" data-testid="shortfall-count">共 {{ shortfalls.length }} 条</span>
        </div>
        <el-table :data="shortfalls" :row-class-name="shortfallRowClass" empty-text="本周无缺口" max-height="260"
                  size="small"
                  @row-click="openShortfallDetail">
          <el-table-column label="日期" prop="scheduleDate" width="100"/>
          <el-table-column label="单元" min-width="130" prop="orgName" show-overflow-tooltip/>
          <el-table-column label="班次" min-width="90" prop="shiftName" show-overflow-tooltip/>
          <el-table-column label="岗位" prop="staffTypeName" width="70"/>
          <el-table-column align="center" label="应到/实到" width="90">
            <template #default="{ row }">{{ row.minStaff }}/{{ row.actualCount }}</template>
          </el-table-column>
          <el-table-column align="center" label="缺口" width="70">
            <template #default="{ row }">
              <span class="font-semibold text-red-600">-{{ row.shortfall }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 总值班解析 -->
      <div class="rounded-md border border-slate-200 bg-white p-3" data-testid="duty-panel">
        <div class="mb-2 flex items-center justify-between">
          <h3 class="text-sm font-semibold text-slate-700">全院总值班</h3>
          <span class="text-xs text-slate-400">只解析全院行政位；科室医师值班见全院总值班页</span>
        </div>
        <el-table :data="dutyRows" max-height="260" size="small">
          <el-table-column label="日期" prop="dateLabel" width="110"/>
          <el-table-column label="白班" min-width="140">
            <template #default="{ row }">
              <span v-if="row.day.found === 1">{{ row.day.actualEmpName }}</span>
              <span v-else :title="row.day.emptyReason" class="text-red-600">漏排</span>
            </template>
          </el-table-column>
          <el-table-column label="夜班" min-width="140">
            <template #default="{ row }">
              <span v-if="row.night.found === 1">{{ row.night.actualEmpName }}</span>
              <span v-else :title="row.night.emptyReason" class="text-red-600">漏排</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 点矩阵格子 / 缺口明细行 打开的当日排班名单 -->
    <ScheduleOverviewCellDetail
        v-model="cellVisible"
        :date="cellQuery?.date"
        :org-id="cellQuery?.orgId"
        :org-name="cellQuery?.orgName"
        :org-type="cellQuery?.orgType"
        :shortfalls="shortfalls"
    />
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {Search} from '@element-plus/icons-vue'
import {getScheduleOverviewWeek, getWorkloadMonthly, getScheduleChangeLogPage} from '@/api/staffSchedule'
import {STAFF_TYPE_OPTIONS} from '@/lib/scheduleShift'
import {ORG_UNIT_TYPE_OPTIONS, orgUnitTypeText} from '@/lib/staffSchedule'
import ScheduleOverviewCellDetail from './ScheduleOverviewCellDetail.vue'

const WEEK_LABELS = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const beginDate = ref('')
const overview = ref(null)
const loading = ref(false)

/** 视图切换：科室在岗（人次）与门诊出诊（号源）是两套度量，分开看 */
const activeTab = ref('unit')

// ---------------- 变更日志 Tab ----------------
const logSearchForm = ref({dateRange: null, employeeName: '', changeType: null})
const logLoading = ref(false)
const logRows = ref([])
const loadLogs = async () => {
  logLoading.value = true
  try {
    const res = await getScheduleChangeLogPage({
      pageNum: 1, pageSize: 50,
      startDate: logSearchForm.value.dateRange?.[0],
      endDate: logSearchForm.value.dateRange?.[1],
      employeeName: logSearchForm.value.employeeName || undefined,
    })
    logRows.value = res?.data?.records || []
  } catch (e) {
    logRows.value = []
  } finally {
    logLoading.value = false
  }
}

// ---------------- 全岗位工时 Tab ----------------
const workloadMonth = ref(new Date().toISOString().slice(0, 7))
const workloadLoading = ref(false)
const workloadRows = ref([])
const loadWorkload = async () => {
  workloadLoading.value = true
  try {
    const res = await getWorkloadMonthly(workloadMonth.value)
    workloadRows.value = res?.data || []
  } catch (e) {
    workloadRows.value = []
  } finally {
    workloadLoading.value = false
  }
}

/** 矩阵切片条件：单元类型 / 单元名称关键词 / 只看有缺口 */
const orgTypeFilter = ref('')
const orgKeyword = ref('')
const onlyShortfall = ref(false)

/** 当前点开名单的格子：一个排班单元在某一天的出勤情况 */
const cellVisible = ref(false)
const cellQuery = ref(null)

function fmtISO(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

/** 本周今天落在哪一天：矩阵高亮列用（必须本地时区，UTC 会让早上八点前高亮昨天） */
const todayStr = fmtISO(new Date())

function mondayOf(d) {
  const copy = new Date(d)
  const dow = (copy.getDay() + 6) % 7
  copy.setDate(copy.getDate() - dow)
  return copy
}

const days = computed(() => {
  if (!overview.value) return []
  return Array.from({length: overview.value.days || 7}, (_, i) => {
    const date = new Date(`${overview.value.beginDate}T00:00:00`)
    date.setDate(date.getDate() + i)
    const iso = fmtISO(date)
    return {date: iso, label: WEEK_LABELS[i]}
  })
})

const weekLabel = computed(() => {
  if (!overview.value) return ''
  const begin = overview.value.beginDate
  const end = days.value.length ? days.value[days.value.length - 1].date : begin
  return `${begin} ~ ${end}`
})

const staffTypeCards = computed(() => {
  const totals = {}
  for (const row of overview.value?.staffTypeDays || []) {
    totals[row.staffType] = (totals[row.staffType] || 0) + (row.workingCount || 0)
  }
  return STAFF_TYPE_OPTIONS.map((o) => ({
    staffType: o.value,
    label: o.label,
    total: totals[o.value] || 0,
  }))
})

/** 本周有出勤的岗位才上图：全 0 的岗位卡不占视觉 */
const activeStaffCards = computed(() => staffTypeCards.value.filter((c) => c.total > 0))

/** 缺口明细（人力面板直接渲染，矩阵红角标与单元格弹框都按它取） */
const shortfalls = computed(() => overview.value?.shortfalls || [])

/** 本周累计缺员人数与缺员单元·日数（指标卡用） */
const weekShortfallTotal = computed(() =>
    shortfalls.value.reduce((sum, s) => sum + (s.shortfall || 0), 0))
const weekShortfallCells = computed(() =>
    new Set(shortfalls.value.map((s) => `${s.orgType}|${s.orgId}|${s.scheduleDate}`)).size)

/** 缺口按 (单元, 日期) 分桶：矩阵红角标与「只看有缺口」用 */
const shortfallByOrgDay = computed(() => {
  const map = new Map()
  for (const s of shortfalls.value) {
    const key = `${s.orgType}|${s.orgId}`
    if (!map.has(key)) map.set(key, {cells: {}, titles: {}, total: 0})
    const bucket = map.get(key)
    bucket.cells[s.scheduleDate] = (bucket.cells[s.scheduleDate] || 0) + (s.shortfall || 0)
    bucket.titles[s.scheduleDate] = [
      `${s.scheduleDate} ${s.orgName}`,
      `${s.shiftName} · ${s.staffTypeName}`,
      `应到 ${s.minStaff} 实到 ${s.actualCount}，缺 ${s.shortfall} 人`,
    ].join('；')
    bucket.total += s.shortfall || 0
  }
  return map
})

const allMatrixRows = computed(() => {
  const rows = new Map()
  for (const cell of overview.value?.unitDays || []) {
    const key = `${cell.orgType}|${cell.orgId}`
    if (!rows.has(key)) {
      rows.set(key, {
        key,
        orgType: cell.orgType,
        orgId: cell.orgId,
        orgName: cell.orgName || `单元${cell.orgId}`,
        cells: {},
        weekTotal: 0,
      })
    }
    const row = rows.get(key)
    row.cells[cell.scheduleDate] = (row.cells[cell.scheduleDate] || 0) + (cell.workingCount || 0)
    row.weekTotal += cell.workingCount || 0
  }
  const shortfall = shortfallByOrgDay.value
  return Array.from(rows.values()).map((row) => {
    const bucket = shortfall.get(row.key)
    return {
      ...row,
      shortfalls: bucket?.cells || {},
      shortfallTitles: bucket?.titles || {},
      shortfallTotal: bucket?.total || 0,
    }
  })
})

const matrixRows = computed(() => {
  const kw = orgKeyword.value.trim()
  return allMatrixRows.value.filter((row) => {
    if (orgTypeFilter.value && row.orgType !== orgTypeFilter.value) return false
    if (kw && !String(row.orgName || '').includes(kw)) return false
    if (onlyShortfall.value && row.shortfallTotal <= 0) return false
    return true
  })
})

/** 矩阵格子文字色：当日缺员 → 红；有人次 → 深灰；空班 → 浅灰 */
function cellTextClass(row, date) {
  const count = row.cells[date] ?? 0
  if (row.shortfalls[date]) return 'bg-red-50 font-semibold text-red-700'
  return count > 0 ? 'text-slate-700' : 'text-slate-300'
}

const clinicByDay = computed(() => {
  const map = {}
  for (const c of overview.value?.clinicDays || []) {
    map[c.scheduleDate] = {
      shiftCount: Number(c.shiftCount || 0),
      total: Number(c.totalSource || 0),
      used: Number(c.usedSource || 0),
      stoppedCount: Number(c.stoppedCount || 0),
    }
  }
  return map
})

/** 单日号源利用率（%），无总号源返回 null */
function clinicPct(c) {
  if (!c || !c.total) return null
  return Math.round((c.used / c.total) * 100)
}

/** 利用率配色：高占用绿、过半蓝、偏低琥珀、空灰 */
function clinicPctColor(pct) {
  if (pct == null) return '#94a3b8'
  if (pct >= 80) return '#10b981'
  if (pct >= 50) return '#3b82f6'
  return '#f59e0b'
}

const clinicWeek = computed(() => {
  let shiftCount = 0
  let total = 0
  let used = 0
  let stopped = 0
  for (const c of Object.values(clinicByDay.value)) {
    shiftCount += c.shiftCount
    total += c.total
    used += c.used
    stopped += c.stoppedCount
  }
  const pct = total > 0 ? Math.round((used / total) * 100) : null
  return {shiftCount, total, used, stopped, pct}
})

const dutyRows = computed(() => {
  const byDay = new Map()
  for (const d of overview.value?.dutyDays || []) {
    if (!byDay.has(d.dutyDate)) byDay.set(d.dutyDate, {})
    byDay.get(d.dutyDate)[d.shiftType === 2 ? 'night' : 'day'] = d
  }
  return Array.from(byDay.entries()).map(([date, v], i) => ({
    dateLabel: `${date.slice(5)} ${WEEK_LABELS[i]}`,
    day: v.day || {found: 0, emptyReason: '未排'},
    night: v.night || {found: 0, emptyReason: '未排'},
  }))
})

/**
 * 打开某个单元某天的排班名单。
 * 用于排班总览矩阵格子：值班长核对「今天这个科到底排了谁」时点格子下钻，不跳页。
 */
function openCellDetail(row, date) {
  cellQuery.value = {orgType: row.orgType, orgId: row.orgId, orgName: row.orgName, date}
  cellVisible.value = true
}

/**
 * 从人力缺口明细打开同一份名单。
 * 用于排班总览缺口面板：看到缺口行后直接点进去查是谁没排上。
 */
function openShortfallDetail(row) {
  if (!row?.orgType) return
  openCellDetail(row, row.scheduleDate)
}

function shortfallRowClass() {
  return 'cursor-pointer'
}

function shiftWeek(offset) {
  if (offset === 0) {
    beginDate.value = fmtISO(mondayOf(new Date()))
  } else if (!beginDate.value) {
    beginDate.value = fmtISO(mondayOf(new Date()))
    return
  } else {
    const d = new Date(`${beginDate.value}T00:00:00`)
    d.setDate(d.getDate() + offset)
    beginDate.value = fmtISO(d)
  }
  load()
}

async function load() {
  loading.value = true
  try {
    const res = await getScheduleOverviewWeek(beginDate.value || undefined)
    overview.value = res.data
    beginDate.value = res.data.beginDate
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
/* 缺口明细整行点开名单，光标提示可点；表头不参与（表头没有行数据） */
:deep(.cursor-pointer) {
  cursor: pointer;
}

/* el-tabs 插入后补齐高度链：nav 固定、内容区 flex-1，矩阵才能在面板内部滚动而不是把底部面板挤出 */
:deep(.el-tabs) {
  display: flex;
  flex-direction: column;
}
:deep(.el-tabs__content) {
  flex: 1;
  min-height: 0;
}
:deep(.el-tab-pane) {
  height: 100%;
}
</style>
