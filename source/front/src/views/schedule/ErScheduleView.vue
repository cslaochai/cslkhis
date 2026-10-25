<template>
  <div class="w-full">
    <!-- 周统计总览：口径 = 当前科室筛选 × 面板所在周，随上周/下周切换联动（样式同门诊排班面板） -->
    <div class="mb-3 grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-5" data-testid="er-stats">
      <div class="rounded-md border border-slate-200 bg-white px-4 py-1.5 text-center transition duration-200 hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md cursor-default"
           data-testid="stat-doctors" title="本周有已发布值班的医生数（distinct 员工）">
        <p class="text-xl font-semibold text-slate-800">{{ panelStats.doctors }}</p>
        <p class="text-xs text-slate-400">值班医生</p>
      </div>
      <div class="rounded-md border border-slate-200 bg-white px-4 py-1.5 text-center transition duration-200 hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md cursor-default"
           data-testid="stat-shifts" title="本周排班总行数（含草稿/待审核/已发布）">
        <p class="text-xl font-semibold text-blue-600">{{ panelStats.shifts }}</p>
        <p class="text-xs text-slate-400">总排班行</p>
      </div>
      <div class="rounded-md border border-slate-200 bg-white px-4 py-1.5 text-center transition duration-200 hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md cursor-default"
           data-testid="stat-night" title="本周夜班行数（班次名含「夜」；急诊夜班只允许已注册执业医生单独值）">
        <p class="text-xl font-semibold text-amber-600">{{ panelStats.night }}</p>
        <p class="text-xs text-slate-400">夜班行</p>
      </div>
      <div class="rounded-md border border-slate-200 bg-white px-4 py-1.5 text-center transition duration-200 hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md cursor-default"
           data-testid="stat-pending" title="本周待审核行数（提交审核后等审核发布，发布后才计在岗）">
        <p class="text-xl font-semibold" :class="panelStats.pending > 0 ? 'text-orange-500' : 'text-slate-800'">
          {{ panelStats.pending }}
        </p>
        <p class="text-xs text-slate-400">待审核</p>
      </div>
      <div class="rounded-md border border-slate-200 bg-white px-4 py-1.5 text-center transition duration-200 hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md cursor-default"
           data-testid="stat-draft" title="本周草稿行数（还没提交审核，不计在岗、不进公开值班表）">
        <p class="text-xl font-semibold text-slate-500">{{ panelStats.draft }}</p>
        <p class="text-xs text-slate-400">草稿</p>
      </div>
    </div>

    <!-- 今日急诊在岗：公开值班表（/er-duty）同源数据，这里给排班员一个「排的东西生效了没」的即时反馈 -->
    <div class="mb-3 rounded-lg border border-slate-200 bg-white px-4 py-3 shadow-sm" data-testid="on-duty-panel">
      <div class="mb-2 flex flex-wrap items-center gap-3">
        <span class="text-base font-medium text-slate-700">今日在岗（已发布）</span>
        <el-tag effect="plain" size="small" type="info">{{ todayText }}</el-tag>
        <span class="text-xs text-slate-400" data-testid="on-duty-count">共 {{ onDutyList.length }} 人在岗</span>
        <div class="ml-auto flex items-center gap-2">
          <span v-if="onDutyLoading" class="text-xs text-slate-400">加载中…</span>
          <el-button :icon="Refresh" data-testid="on-duty-refresh" size="small" @click="loadOnDuty">刷新</el-button>
        </div>
      </div>
      <div v-if="!onDutyList.length" class="py-2 text-sm text-slate-400" data-testid="on-duty-empty">
        今日暂无已发布的急诊值班（排班发布后这里与「急诊值班」公开表同步可见）
      </div>
      <div v-else class="flex flex-col gap-2" data-testid="on-duty-groups">
        <div v-for="g in onDutyGroups" :key="g.deptName" class="flex flex-wrap items-center gap-2">
          <span class="w-28 shrink-0 text-xs text-slate-500">{{ g.deptName }}</span>
          <el-tag v-for="s in g.list" :key="s.id" class="on-duty-tag" effect="light" size="large" type="success"
                  :title="`${s.startTime || ''}-${s.endTime || ''} · ${s.attendModeText || ''}`">
            {{ s.employeeName }}
            <span class="ml-1 text-xs text-slate-500">{{ s.shiftName || `${s.startTime}-${s.endTime}` }}</span>
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 排班面板 -->
    <div class="flex flex-col rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div class="filter-row flex flex-wrap items-center gap-3">
        <span class="text-base font-medium text-slate-700">科室</span>
        <el-select v-model="deptId" :fit-input-width="false" :clearable="false" class="!w-44" data-testid="er-dept"
                   filterable placeholder="选择急诊科室" @change="loadPanel">
          <el-option v-for="d in deptOptions" :key="d.id" :label="d.name" :value="d.id"/>
        </el-select>
        <div class="flex items-center gap-2">
          <el-button data-testid="week-prev" @click="handleWeekShift(-1)">
            <span class="px-0.5 text-base leading-none">‹</span>上一周
          </el-button>
          <span class="min-w-[190px] text-center text-base font-medium text-slate-600" data-testid="week-label">
            {{ weekLabel }}
          </span>
          <el-button data-testid="week-next" @click="handleWeekShift(1)">
            下一周<span class="px-0.5 text-base leading-none">›</span>
          </el-button>
          <el-button link type="primary" @click="handleBackToThisWeek">回本周</el-button>
        </div>
        <span class="ml-auto flex items-center gap-2">
          <el-button :icon="Refresh" :loading="loading" data-testid="panel-refresh" type="primary" @click="loadPanel">
            查询
          </el-button>
          <el-button v-perm="'staff:schedule:add'" :icon="Plus" data-testid="er-add" type="primary" @click="openAdd">
            登记排班
          </el-button>
          <el-button v-perm="'staff:schedule:add'" :loading="weekLoading" :disabled="!panelStats.draft"
                     data-testid="week-submit" title="把当前科室当前周的草稿一次性提交为待审核（已发布/待审核的不动）"
                     @click="handleSubmitWeek">整周提交审核
          </el-button>
          <el-button v-perm="'staff:schedule:audit'" :loading="weekLoading" :disabled="!panelStats.pending"
                     data-testid="week-audit" title="把当前科室当前周的待审核一次性审核发布并计入在岗"
                     type="success" @click="handleAuditWeek">整周审核发布
          </el-button>
          <el-button v-perm="'staff:schedule:add'" data-testid="er-copy" @click="copyVisible = true">整周复制</el-button>
        </span>
      </div>

      <el-table v-loading="loading" :data="panelRows" border class="mt-3" data-testid="er-panel-table"
                empty-text="本周暂无排班：点「登记排班」开始，或从上周整周复制">
        <el-table-column label="值班医生" min-width="150">
          <template #default="{ row }">
            <span class="font-medium text-slate-700">{{ row.employeeName }}</span>
            <span class="ml-1 text-xs text-slate-400">{{ row.empCode }}</span>
          </template>
        </el-table-column>
        <el-table-column v-for="d in weekDays" :key="d.date" :label="d.label" min-width="150">
          <template #default="{ row }">
            <template v-if="row.cells[d.date]">
              <div class="cursor-pointer rounded border border-slate-200 bg-slate-50 px-2 py-1 text-xs hover:border-blue-300"
                   data-testid="er-cell" @click="openEdit(row.cells[d.date])">
                <div class="flex items-center gap-1">
                  <el-tag size="small" :type="shiftTagType(row.cells[d.date])">{{ row.cells[d.date].shiftName }}</el-tag>
                  <el-tag size="small" :type="publishStatusTagType(row.cells[d.date].publishStatus)" effect="plain">
                    {{ row.cells[d.date].publishStatusText }}
                  </el-tag>
                </div>
                <div class="mt-1 text-slate-500">
                  {{ row.cells[d.date].startTime }}–{{ row.cells[d.date].endTime }} · {{ row.cells[d.date].attendModeText }}
                </div>
              </div>
            </template>
            <span v-else class="text-xs text-slate-300">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <div class="flex flex-wrap items-center gap-1" @click.stop>
              <el-button v-for="b in rowActions(row)" :key="b.text" :type="b.type" link size="small"
                         :data-testid="b.testid" @click="b.onClick">
                {{ b.text }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <p class="mt-2 text-xs text-slate-400">
        排班只落到急诊科室的医生岗位；发布态走审核闭环（登记=草稿 → 提交审核 → 科室审核人发布），
        已发布/已变更才计在岗并出现在全院公开的「急诊值班」表。夜班班次会校验注册执业资质（is_registered）。
      </p>
    </div>

    <!-- 登记 / 修改（复用员工排班弹框，scope=急诊值班域） -->
    <StaffScheduleEdit v-model="editVisible" :row="editRow" :scope="SCHEDULE_SCOPE_ER" @saved="handleSaved"/>

    <!-- 换班 / 代班 -->
    <StaffScheduleSwapEdit v-model="swapVisible" :row="swapRow" :scope="SCHEDULE_SCOPE_ER" :table-data="tableData"
                           @saved="handleSaved"/>

    <!-- 整周复制 -->
    <StaffScheduleCopyEdit v-model="copyVisible" :scope="SCHEDULE_SCOPE_ER" @saved="handleSaved"/>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus, Refresh} from '@element-plus/icons-vue'
import {
  auditPublishStaffSchedule,
  auditWeekStaffSchedule,
  deleteStaffSchedule,
  getErDutyPublic,
  getStaffScheduleListPage,
  getStaffScheduleUnitSelectList,
  submitStaffSchedule,
  submitWeekStaffSchedule,
} from '@/api/staffSchedule'
import StaffScheduleEdit from './StaffScheduleEdit.vue'
import StaffScheduleSwapEdit from './StaffScheduleSwapEdit.vue'
import StaffScheduleCopyEdit from './StaffScheduleCopyEdit.vue'
import {
  canAuditPublish,
  canSubmitPublish,
  ORG_UNIT_TYPE_DEPT,
  SCHEDULE_SCOPE_ER,
} from '@/lib/staffSchedule'
import {publishStatusTagType} from '@/lib/publishStatus'

/**
 * 急诊排班（域：急诊科室 + 医生岗，biz_schedule scope=2）。
 *
 * 与员工排班页同一张表、同一套 upsert 接口，差别全在 scope：
 * 列表/候选/保存都由服务端按域收口（员工排班页看不到这些行，直调也排不进来）。
 * 样式对齐门诊排班面板（统计卡 + 在岗面板 + 周面板），数据是值班事实不是号源。
 */

// ---------------- 周与日期 ----------------
const mondayOf = (d) => {
  const x = d ? new Date(d) : new Date()
  const day = x.getDay() === 0 ? 7 : x.getDay()
  x.setHours(0, 0, 0, 0)
  x.setDate(x.getDate() - (day - 1))
  return x
}
const isoOf = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`

const weekStart = ref(mondayOf())
const weekDays = computed(() => {
  const labels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return Array.from({length: 7}, (_, i) => {
    const d = new Date(weekStart.value)
    d.setDate(d.getDate() + i)
    return {date: isoOf(d), label: `${labels[i]} ${d.getMonth() + 1}/${d.getDate()}`}
  })
})
const weekLabel = computed(() => `${weekDays.value[0].date} ~ ${weekDays.value[6].date}`)
const todayText = isoOf(new Date())

const handleWeekShift = (n) => {
  const d = new Date(weekStart.value)
  d.setDate(d.getDate() + n * 7)
  weekStart.value = d
  loadPanel()
}
const handleBackToThisWeek = () => {
  weekStart.value = mondayOf()
  loadPanel()
}

// ---------------- 科室候选（急诊叶子，服务端按 scope=2 收口） ----------------
const deptId = ref(null)
const deptOptions = ref([])
const loadDeptOptions = async () => {
  try {
    const res = await getStaffScheduleUnitSelectList({orgType: ORG_UNIT_TYPE_DEPT, scope: SCHEDULE_SCOPE_ER})
    deptOptions.value = res.code === 200 ? (res.data || []) : []
    if (!deptId.value && deptOptions.value.length) {
      deptId.value = deptOptions.value[0].id
    }
  } catch {
    deptOptions.value = []
  }
}

// ---------------- 周面板数据 ----------------
const loading = ref(false)
const tableData = ref([])
const panelStats = ref({doctors: 0, shifts: 0, night: 0, pending: 0, draft: 0})

const loadPanel = async () => {
  loading.value = true
  try {
    const res = await getStaffScheduleListPage({
      scope: SCHEDULE_SCOPE_ER,
      orgType: ORG_UNIT_TYPE_DEPT,
      orgId: deptId.value,
      startDate: weekDays.value[0].date,
      endDate: weekDays.value[6].date,
      pageNum: 1,
      pageSize: 200,
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      computeStats()
    }
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const computeStats = () => {
  const rows = tableData.value
  panelStats.value = {
    doctors: new Set(rows.map((r) => r.employeeId)).size,
    shifts: rows.length,
    night: rows.filter((r) => (r.shiftName || '').includes('夜')).length,
    pending: rows.filter((r) => Number(r.publishStatus) === 1).length,
    draft: rows.filter((r) => Number(r.publishStatus) === 0).length,
  }
}

/** 行 = 值班人（同人同一行），列 = 日期；格子点开改该行。scheduleDate 归一化成 YYYY-MM-DD 再对列 */
const panelRows = computed(() => {
  const byEmp = new Map()
  for (const row of tableData.value) {
    if (!byEmp.has(row.employeeId)) {
      byEmp.set(row.employeeId, {employeeId: row.employeeId, employeeName: row.employeeName, empCode: row.empCode, cells: {}})
    }
    byEmp.get(row.employeeId).cells[String(row.scheduleDate || '').slice(0, 10)] = row
  }
  return Array.from(byEmp.values())
})

// ---------------- 今日在岗（公开值班表同源） ----------------
const onDutyList = ref([])
const onDutyLoading = ref(false)
const loadOnDuty = async () => {
  onDutyLoading.value = true
  try {
    const res = await getErDutyPublic(todayText)
    onDutyList.value = res.code === 200 ? (res.data || []) : []
  } catch {
    onDutyList.value = []
  } finally {
    onDutyLoading.value = false
  }
}
const onDutyGroups = computed(() => {
  const map = new Map()
  for (const s of onDutyList.value) {
    const key = s.deptName || s.orgName || '急诊'
    if (!map.has(key)) map.set(key, [])
    map.get(key).push(s)
  }
  return Array.from(map, ([deptName, list]) => ({deptName, list}))
})

// ---------------- 发布态 / 标签 ----------------
const shiftTagType = (row) => {
  const name = row.shiftName || ''
  if (name.includes('白班')) return 'success'
  if (name.includes('前夜') || name.includes('小夜')) return 'warning'
  if (name.includes('后夜') || name.includes('大夜') || name.includes('夜')) return 'danger'
  return 'info'
}

// ---------------- 弹框与行内操作 ----------------
const editVisible = ref(false)
const editRow = ref(null)
const openAdd = () => {
  editRow.value = null
  editVisible.value = true
}
const openEdit = (row) => {
  editRow.value = row
  editVisible.value = true
}
const handleSaved = () => {
  loadPanel()
  loadOnDuty()
}

const swapVisible = ref(false)
const swapRow = ref(null)
const copyVisible = ref(false)
const weekLoading = ref(false)

/** 整周提交审核：当前科室 + 当前周的草稿 → 待审核（后端按 scope=ER 校验单元域） */
const handleSubmitWeek = async () => {
  weekLoading.value = true
  try {
    const res = await submitWeekStaffSchedule({
      orgType: ORG_UNIT_TYPE_DEPT,
      orgId: deptId.value,
      weekStart: weekDays.value[0].date,
      scope: SCHEDULE_SCOPE_ER,
    })
    if (res.code === 200) {
      ElMessage.success(res.message || '已提交审核')
      handleSaved()
    }
  } catch { /* 全局拦截器已提示 */ } finally {
    weekLoading.value = false
  }
}

/** 整周审核发布：当前科室 + 当前周的待审核 → 已发布（审核人按科室 schedule_auditor_id 命中） */
const handleAuditWeek = async () => {
  weekLoading.value = true
  try {
    const res = await auditWeekStaffSchedule({
      orgType: ORG_UNIT_TYPE_DEPT,
      orgId: deptId.value,
      weekStart: weekDays.value[0].date,
      scope: SCHEDULE_SCOPE_ER,
    })
    if (res.code === 200) {
      ElMessage.success(res.message || '已审核发布')
      handleSaved()
    }
  } catch { /* 全局拦截器已提示 */ } finally {
    weekLoading.value = false
  }
}

/** 该行的全部班次（按日期排序），行内操作按「行」聚合：提交/审核覆盖整行，编辑/换班取第一条 */
const cellsOf = (row) => Object.keys(row.cells).sort().map((k) => row.cells[k])

const rowActions = (row) => {
  const cells = cellsOf(row)
  const first = cells[0]
  const actions = [
    {text: '编辑', type: 'primary', testid: 'row-edit', onClick: () => openEdit(first)},
    {text: '换班/代班', type: 'warning', testid: 'row-swap', onClick: () => {
      swapRow.value = first
      swapVisible.value = true
    }},
  ]
  if (cells.some((c) => canSubmitPublish(c))) {
    actions.push({text: '提交审核', type: 'success', testid: 'row-submit', onClick: () => handleSubmitRow(cells)})
  }
  if (cells.some((c) => canAuditPublish(c))) {
    actions.push({text: '审核发布', type: 'success', testid: 'row-audit', onClick: () => handleAuditRow(cells)})
  }
  actions.push({text: '删除', type: 'danger', testid: 'row-delete', onClick: () => handleDeleteRow(row, cells)})
  return actions
}

/** 提交审核：该行全部草稿逐条提交（一人一周最多几条，循环比批接口更直白） */
const handleSubmitRow = async (cells) => {
  for (const c of cells.filter((x) => canSubmitPublish(x))) {
    const res = await submitStaffSchedule(c.id)
    if (res.code !== 200) return
  }
  ElMessage.success('已提交审核')
  handleSaved()
}

/** 审核发布：该行全部待审核逐条发布（审核人按 sys_department.schedule_auditor_id 命中） */
const handleAuditRow = async (cells) => {
  for (const c of cells.filter((x) => canAuditPublish(x))) {
    const res = await auditPublishStaffSchedule(c.id)
    if (res.code !== 200) return
  }
  ElMessage.success('已审核发布')
  handleSaved()
}

const handleDeleteRow = async (row, cells) => {
  const detail = cells.map((c) => `${c.scheduleDate} ${c.shiftName}`).join('；')
  try {
    await ElMessageBox.confirm(
        `删除「${row.employeeName}」本周 ${cells.length} 条排班？\n${detail}`,
        '删除确认',
        {type: 'warning'},
    )
  } catch {
    return
  }
  for (const c of cells) {
    const res = await deleteStaffSchedule(c.id)
    if (res.code !== 200) return
  }
  ElMessage.success('已删除')
  handleSaved()
}

onMounted(async () => {
  await loadDeptOptions()
  await loadPanel()
  loadOnDuty()
})
</script>

<style scoped>
.on-duty-tag {
  height: auto;
  padding: 4px 10px;
  white-space: normal;
}
</style>
