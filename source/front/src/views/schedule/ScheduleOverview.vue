<template>
  <div class="w-full" data-testid="schedule-overview">
    <!-- 周切换工具行 -->
    <div class="mb-3 flex flex-wrap items-center gap-3">
      <el-button-group>
        <el-button data-testid="btn-prev-week" @click="shiftWeek(-7)">上一周</el-button>
        <el-button data-testid="btn-this-week" @click="shiftWeek(0)">本周</el-button>
        <el-button data-testid="btn-next-week" @click="shiftWeek(7)">下一周</el-button>
      </el-button-group>
      <span class="text-sm font-medium text-slate-600" data-testid="week-label">{{ weekLabel }}</span>
      <span class="text-xs text-slate-400">只读总览：编辑请到门诊排班 / 全院岗位排班 / 总值班排班等各自工作台</span>
    </div>

    <!-- 岗位卡片行：本周出勤人次（事实层 biz_staff_schedule 聚合） -->
    <div class="mb-3 grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6" data-testid="staff-type-cards">
      <div v-for="card in staffTypeCards" :key="card.staffType"
           class="rounded-md border border-slate-200 bg-white px-4 py-2 text-center">
        <p :data-testid="`card-staff-type-${card.staffType}`" class="text-xl font-semibold text-slate-800">{{
            card.total
          }}</p>
        <p class="text-xs text-slate-400">{{ card.label }} · 本周出勤人次</p>
      </div>
    </div>

    <!-- 单元 × 日期 矩阵：出勤人次 + 缺口红角标 + 门诊号源行 -->
    <div class="mb-4 overflow-x-auto rounded-md border border-slate-200 bg-white" data-testid="unit-matrix">
      <table class="w-full min-w-[880px] border-collapse text-sm">
        <thead>
        <tr class="bg-slate-50 text-slate-500">
          <th class="px-3 py-2 text-left font-medium">单元</th>
          <th v-for="d in days" :key="d.date" :class="{ 'bg-blue-50 text-blue-600': d.date === todayStr }"
              class="px-2 py-2 text-center font-medium">
            {{ d.label }}<br/><span class="text-xs font-normal">{{ d.date.slice(5) }}</span>
          </th>
          <th class="px-3 py-2 text-center font-medium">周合计</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="row in matrixRows" :key="row.key" class="border-t border-slate-100 hover:bg-slate-50/60">
          <td class="px-3 py-2 text-slate-700">
            {{ row.orgName }}
            <span class="ml-1 text-xs text-slate-400">{{ orgTypeLabel(row.orgType) }}</span>
          </td>
          <td v-for="d in days" :key="d.date" :class="{ 'bg-blue-50/50': d.date === todayStr }"
              class="px-2 py-2 text-center">
            <template v-if="row.cells[d.date] !== undefined">
                <span class="relative inline-block">
                  <span :class="row.cells[d.date] > 0 ? 'text-slate-700' : 'text-slate-300'">{{
                      row.cells[d.date]
                    }}</span>
                  <span v-if="row.shortfalls[d.date]"
                        :title="row.shortfallTitles[d.date]"
                        class="absolute -right-3 -top-1.5 rounded-full bg-red-500 px-1 text-[10px] leading-3 text-white">{{ row.shortfalls[d.date] }}</span>
                </span>
            </template>
            <span v-else class="text-slate-200">—</span>
          </td>
          <td class="px-3 py-2 text-center font-medium text-slate-700">{{ row.weekTotal }}</td>
        </tr>
        <!-- 门诊号源行：已挂/总号源，停诊班次红点 -->
        <tr class="border-t border-slate-100 bg-emerald-50/30 hover:bg-emerald-50/50" data-testid="clinic-row">
          <td class="px-3 py-2 text-emerald-700">门诊号源 <span class="ml-1 text-xs text-emerald-500/70">出诊计划</span>
          </td>
          <td v-for="d in days" :key="d.date" :class="{ 'bg-emerald-50/60': d.date === todayStr }"
              class="px-2 py-2 text-center">
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
          <td class="px-3 py-2 text-center font-medium text-emerald-700">
            {{ clinicWeekTotal.used }}/{{ clinicWeekTotal.total }}
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <div class="grid grid-cols-1 gap-4 xl:grid-cols-2">
      <!-- 人力缺口明细 -->
      <div class="rounded-md border border-slate-200 bg-white p-3" data-testid="shortfall-panel">
        <div class="mb-2 flex items-center justify-between">
          <h3 class="text-sm font-semibold text-slate-700">人力缺口（实际在岗低于配置标准）</h3>
          <span class="text-xs text-slate-400" data-testid="shortfall-count">共 {{ shortfalls.length }} 条</span>
        </div>
        <el-table :data="shortfalls" empty-text="本周无缺口" max-height="360" size="small">
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
          <h3 class="text-sm font-semibold text-slate-700">本周总值班（主班优先 · 副班顶上）</h3>
          <span class="text-xs text-slate-400">只解析全院行政位；科室医师值班见总值班排班页</span>
        </div>
        <el-table :data="dutyRows" max-height="360" size="small">
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
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {getScheduleOverviewWeek} from '@/api/staffSchedule'

/** 岗位类别文案（与 StaffTypeEnum 同码：1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他） */
const STAFF_TYPE_LABELS = {1: '医生', 2: '护理', 3: '医技', 4: '药学', 5: '收费', 6: '行政'}
const ORG_TYPE_LABELS = {1: '科室', 2: '病区', 3: '全院'}
const WEEK_LABELS = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const beginDate = ref('')
const overview = ref(null)
const loading = ref(false)

const todayStr = new Date().toISOString().slice(0, 10)

function fmtISO(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

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
  return Object.keys(STAFF_TYPE_LABELS).map((t) => ({
    staffType: Number(t),
    label: STAFF_TYPE_LABELS[t],
    total: totals[t] || 0,
  }))
})

/** 缺口明细（人力面板直接渲染，矩阵红角标按它分桶） */
const shortfalls = computed(() => overview.value?.shortfalls || [])

/** 缺口按 (org, date) 分桶：矩阵红角标用 */
const shortfallByOrgDay = computed(() => {
  const map = new Map()
  for (const s of shortfalls.value) {
    const key = `${s.orgType}|${s.orgId}`
    if (!map.has(key)) map.set(key, {cells: {}, titles: {}})
    const bucket = map.get(key)
    bucket.cells[s.scheduleDate] = (bucket.cells[s.scheduleDate] || 0) + (s.shortfall || 0)
    bucket.titles[s.scheduleDate] = [
      `${s.scheduleDate} ${s.orgName}`,
      `${s.shiftName} · ${s.staffTypeName}`,
      `应到 ${s.minStaff} 实到 ${s.actualCount}，缺 ${s.shortfall} 人`,
    ].join('；')
  }
  return map
})

const matrixRows = computed(() => {
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
  return Array.from(rows.values()).map((row) => ({
    ...row,
    shortfalls: shortfall.get(row.key)?.cells || {},
    shortfallTitles: shortfall.get(row.key)?.titles || {},
  }))
})

const clinicByDay = computed(() => {
  const map = {}
  for (const c of overview.value?.clinicDays || []) {
    map[c.scheduleDate] = {
      total: Number(c.totalSource || 0),
      used: Number(c.usedSource || 0),
      stoppedCount: Number(c.stoppedCount || 0),
    }
  }
  return map
})

const clinicWeekTotal = computed(() => {
  let total = 0
  let used = 0
  for (const c of Object.values(clinicByDay.value)) {
    total += c.total
    used += c.used
  }
  return {total, used}
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

function orgTypeLabel(code) {
  return ORG_TYPE_LABELS[code] || ''
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
