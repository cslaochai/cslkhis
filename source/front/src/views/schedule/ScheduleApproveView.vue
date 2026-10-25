<template>
  <div data-testid="schedule-approve-view">
    <el-tabs v-model="activeTab" class="mb-2" @tab-change="handleTabChange">
      <el-tab-pane label="全院岗位排班" name="staff">
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="query-grid-wrap">
        <el-form class="query-grid" label-width="70px" :model="searchForm">
          <el-form-item label="单元类型">
            <el-select v-model="searchForm.orgType" clearable placeholder="全部" @change="handleOrgTypeChange">
              <el-option v-for="o in ORG_UNIT_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="排班单元">
            <el-select v-model="searchForm.orgId" clearable filterable :disabled="!searchForm.orgType"
                       :placeholder="searchForm.orgType ? '全部单元' : '先选单元类型'">
              <el-option v-for="u in unitOptions" :key="u.id" :label="unitLabel(u)" :value="u.id"/>
            </el-select>
          </el-form-item>
        </el-form>
        <div class="query-grid-ops">
          <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
      </div>
    </el-card>

    <div class="mb-3 flex flex-wrap items-center gap-2">
      <el-button v-perm="PERM_SCHEDULE_AUDIT" :icon="CircleCheck" :loading="batchLoading"
                 data-testid="schedule-approve-batch-audit" type="primary" @click="handleBatchAudit">
        批量审核发布
      </el-button>
      <el-button v-perm="PERM_SCHEDULE_AUDIT" :icon="CloseBold" :loading="batchLoading"
                 data-testid="schedule-approve-batch-reject" @click="handleBatchReject">
        批量驳回
      </el-button>
      <el-button :icon="Refresh" @click="loadData">刷新</el-button>
      <span class="text-sm text-slate-500">共 {{ pagination.total }} 条待审 · 审核人＝科室主档指定的「排班审核人」</span>
    </div>

    <el-card class="table-card" shadow="never">
      <div data-testid="schedule-approve-table">
        <el-table ref="tableRef" v-loading="loading" :data="tableData" :max-height="tableMaxHeight" stripe
                  @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="45"/>
          <el-table-column label="日期" prop="scheduleDate" width="115"/>
          <el-table-column label="星期" prop="weekDayText" width="80"/>
          <el-table-column label="排班单元" min-width="150">
            <template #default="{ row }">
              {{ row.orgName }}（{{ orgUnitTypeText(row.orgType) }}）
            </template>
          </el-table-column>
          <el-table-column label="排班对象" min-width="140">
            <template #default="{ row }">
              {{ row.employeeName }}
              <span v-if="row.empCode" class="text-gray-400">（{{ row.empCode }}）</span>
            </template>
          </el-table-column>
          <el-table-column label="岗位" prop="staffTypeName" width="90"/>
          <el-table-column label="班次" min-width="170">
            <template #default="{ row }">
              <span v-if="Number(row.dutyStatus) === 1 && row.shiftId">
                {{ row.shiftName }} {{ row.startTime }}~{{ row.endTime }}
              </span>
              <span v-else class="text-gray-400">—（{{ dutyStatusText(row.dutyStatus) }}无班次）</span>
            </template>
          </el-table-column>
          <el-table-column label="出勤" width="100">
            <template #default="{ row }">
              <el-tag :type="Number(row.dutyStatus) === 1 ? 'success' : 'info'" size="small">
                {{ row.dutyStatusText }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="所属科室" prop="deptName" min-width="120"/>
          <el-table-column fixed="right" label="操作" width="230">
            <template #default="{ row }">
              <el-button v-perm="PERM_SCHEDULE_AUDIT" :icon="CircleCheck" data-testid="schedule-approve-audit"
                         link type="success" @click="handleAudit(row)">
                审核发布
              </el-button>
              <el-button v-perm="PERM_SCHEDULE_AUDIT" :icon="CloseBold" data-testid="schedule-approve-reject" link
                         type="danger" @click="handleReject(row)">
                驳回
              </el-button>
              <el-button v-perm="'staff:schedule:list'" :icon="Clock" link @click="openChangeLog(row)">留痕</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-empty v-if="!loading && tableData.length === 0" :image-size="60" description="没有待你审核的排班"/>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
      </el-tab-pane>
      <el-tab-pane label="门诊排班" name="clinic">
        <div class="mb-3 flex flex-wrap items-center gap-2">
          <el-button v-perm="PERM_SCHEDULE_AUDIT" :icon="CircleCheck" :loading="clinicBatchLoading"
                     data-testid="clinic-approve-batch-audit" type="primary" @click="handleClinicBatchAudit">
            批量审核发布
          </el-button>
          <el-button v-perm="PERM_SCHEDULE_AUDIT" :icon="CloseBold" :loading="clinicBatchLoading"
                     data-testid="clinic-approve-batch-reject" @click="handleClinicBatchReject">
            批量驳回
          </el-button>
          <el-button :icon="Refresh" @click="loadClinicData">刷新</el-button>
          <span class="text-sm text-slate-500">共 {{ clinicPagination.total }} 条待审 · 审核人＝科室主档指定的「门诊审核人」，管理员/门诊办可审全部科室</span>
        </div>

        <el-card class="table-card" shadow="never">
          <div data-testid="clinic-approve-table">
            <el-table v-loading="clinicLoading" :data="clinicTableData" :max-height="tableMaxHeight" stripe
                      @selection-change="handleClinicSelectionChange">
              <el-table-column type="selection" width="45"/>
              <el-table-column label="日期" prop="scheduleDate" width="115"/>
              <el-table-column label="科室" prop="deptName" min-width="120"/>
              <el-table-column label="医生" min-width="120">
                <template #default="{ row }">
                  {{ row.doctorName }}
                </template>
              </el-table-column>
              <el-table-column label="班次" min-width="170">
                <template #default="{ row }">
                  {{ row.shiftName || '—' }} {{ row.startTime }}~{{ row.endTime }}
                </template>
              </el-table-column>
              <el-table-column label="号源" width="110">
                <template #default="{ row }">
                  {{ row.usedSource || 0 }}/{{ row.totalSource || 0 }}
                </template>
              </el-table-column>
              <el-table-column label="发布态" prop="publishStatusText" width="100"/>
              <el-table-column fixed="right" label="操作" width="230">
                <template #default="{ row }">
                  <el-button v-perm="PERM_SCHEDULE_AUDIT" :icon="CircleCheck" data-testid="clinic-approve-audit"
                             link type="success" @click="handleClinicAudit(row)">
                    审核发布
                  </el-button>
                  <el-button v-perm="PERM_SCHEDULE_AUDIT" :icon="CloseBold" data-testid="clinic-approve-reject" link
                             type="danger" @click="handleClinicReject(row)">
                    驳回
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <el-empty v-if="!clinicLoading && clinicTableData.length === 0" :image-size="60"
                    description="没有待你审核的门诊排班"/>
          <div class="list-footer flex items-center justify-end">
            <el-pagination
                v-model:current-page="clinicPagination.pageNum"
                v-model:page-size="clinicPagination.pageSize"
                :page-sizes="PAGE_SIZES"
                :total="clinicPagination.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleClinicSizeChange"
                @current-change="handleClinicCurrentChange"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <ScheduleChangeLogDrawer v-model="logVisible" :staff-schedule-id="logRowId" :title="`变更留痕 · ${logTarget}`"/>
  </div>
</template>

<script setup>
/**
 * 排班审批中心（全院岗位排班的「待我审批」聚合页）。
 *
 * 排班员在「员工排班」页把班提交上来后，停在这一页的人不是排班员本人 ——
 * 是科室主档里为该科室指定的「排班审核人」（system.department.schedule_auditor_id）。
 * 系统管理员（角色码 10012）全局豁免；orgType=3 的全院级排班没有归属科室，只有管理员能审。
 *
 * 页面只做两件事：把待审的排班捞出来、逐条或批量放行/打回。
 */
import {onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {CircleCheck, Clock, CloseBold, Refresh, Search} from '@element-plus/icons-vue'
import {
  auditPublishStaffSchedule,
  getStaffSchedulePendingApproval,
  getStaffScheduleUnitSelectList,
  rejectStaffSchedule,
} from '@/api/staffSchedule'
import {
  scheduleAuditPublish as auditPublishClinicSchedule,
  schedulePendingApproval as getClinicSchedulePendingApproval,
  scheduleReject as rejectClinicSchedule,
} from '@/api/appoint'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {
  dutyStatusText,
  ORG_UNIT_TYPE_OPTIONS,
  orgUnitTypeText,
  PERM_SCHEDULE_AUDIT,
} from '@/lib/staffSchedule'
import ScheduleChangeLogDrawer from './ScheduleChangeLogDrawer.vue'

const loading = ref(false)
const batchLoading = ref(false)
const tableData = ref([])
const tableRef = ref(null)
const selectedRows = ref([])
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

// ---------------- 门诊排班（待我审批）----------------
const activeTab = ref('staff')
const clinicLoading = ref(false)
const clinicBatchLoading = ref(false)
const clinicTableData = ref([])
const clinicSelectedRows = ref([])
const clinicPagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
// 门诊 tab 首次进入才取数（惰性），避免审批中心一打开就多发一组请求
let clinicLoaded = false

const searchForm = ref({orgType: null, orgId: null})

// ---------------- 排班单元候选 ----------------
const unitOptions = ref([])
const loadUnitOptions = async (orgType) => {
  if (!orgType) {
    unitOptions.value = []
    return
  }
  try {
    const res = await getStaffScheduleUnitSelectList({orgType})
    unitOptions.value = res.code === 200 ? (res.data || []) : []
  } catch {
    unitOptions.value = []
  }
}
const handleOrgTypeChange = (orgType) => {
  searchForm.value.orgId = null
  loadUnitOptions(orgType)
}
const unitLabel = (u) => (u.deptName ? `${u.name}（${u.deptName}）` : u.name)

// ---------------- 待审列表 ----------------
const loadData = async () => {
  loading.value = true
  try {
    const res = await getStaffSchedulePendingApproval({
      orgType: searchForm.value.orgType,
      orgId: searchForm.value.orgId,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.value.total = res.data.total || 0
    }
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{

  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  loadData()
}
const handleReset = () => {
  searchForm.value = {orgType: null, orgId: null}
  unitOptions.value = []
  handleSearch()
}
const handleSizeChange = (v) => {
  pagination.value.pageSize = v
  loadData()
}
const handleCurrentChange = (v) => {
  pagination.value.pageNum = v
  loadData()
}
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// ---------------- 审核 / 驳回 ----------------
const handleAudit = async (row) => {
  try {
    const res = await auditPublishStaffSchedule(row.id)
    if (res.code === 200) {
      ElMessage.success('已审核发布')
      loadData()
    }
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{

  }
}

/** 驳回必须写原因：提交人要知道下一步改什么，原因同时进变更留痕 */
const handleReject = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt('请填写驳回原因（提交人会在留痕里看到）', '驳回排班', {
      confirmButtonText: '确定驳回',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：与门诊号源冲突，请调整后再报',
      inputValidator: (v) => (v && v.trim() ? true : '驳回原因不能为空'),
    })
    const res = await rejectStaffSchedule({id: row.id, reason: value})
    if (res.code === 200) {
      ElMessage.success('已驳回')
      loadData()
    }
  } catch (e)  /* 取消或错误：取消无提示，错误已由全局拦截器统一提示 */{

  }
}

const handleBatchAudit = async () => {
  const rows = selectedRows.value
  if (rows.length === 0) {
    ElMessage.warning('请先勾选要审核发布的排班')
    return
  }
  try {
    await ElMessageBox.confirm(
        `确定审核发布选中的 ${rows.length} 条排班吗？发布后计入在岗。`, '批量审核发布', {type: 'warning'})
  } catch {
    return
  }
  batchLoading.value = true
  let ok = 0
  try {
    for (const row of rows) {
      try {
        await auditPublishStaffSchedule(row.id)
        ok++
      } catch (e) {
        // 失败的单条由全局拦截器提示，这里继续跑后面的
      }
    }
    if (ok > 0) {
      ElMessage.success(`已审核发布 ${ok} 条`)
    }
    if (ok < rows.length) {
      ElMessage.warning(`${rows.length - ok} 条未通过（多为「不是本科室指定审核人」），详见提示`)
    }
  } finally {
    batchLoading.value = false
    loadData()
  }
}

const handleBatchReject = async () => {
  const rows = selectedRows.value
  if (rows.length === 0) {
    ElMessage.warning('请先勾选要驳回的排班')
    return
  }
  let reason = ''
  try {
    const {value} = await ElMessageBox.prompt(
        `驳回选中的 ${rows.length} 条排班，请填写驳回原因（提交人会在留痕里看到）`, '批量驳回', {
          confirmButtonText: '确定驳回',
          cancelButtonText: '取消',
          inputPlaceholder: '例如：与门诊号源冲突，请调整后再报',
          inputValidator: (v) => (v && v.trim() ? true : '驳回原因不能为空'),
        })
    reason = value
  } catch {
    return
  }
  batchLoading.value = true
  let ok = 0
  try {
    for (const row of rows) {
      try {
        await rejectStaffSchedule({id: row.id, reason})
        ok++
      } catch (e) {
        // 失败的单条由全局拦截器提示，这里继续跑后面的
      }
    }
    if (ok > 0) {
      ElMessage.success(`已驳回 ${ok} 条`)
    }
    if (ok < rows.length) {
      ElMessage.warning(`${rows.length - ok} 条未通过（多为「不是本科室指定审核人」），详见提示`)
    }
  } finally {
    batchLoading.value = false
    loadData()
  }
}

// ---------------- 门诊排班：待审取数 / 审核发布 / 驳回 ----------------
const loadClinicData = async () => {
  clinicLoading.value = true
  try {
    const res = await getClinicSchedulePendingApproval({
      pageNum: clinicPagination.value.pageNum,
      pageSize: clinicPagination.value.pageSize,
    })
    if (res.code === 200) {
      clinicTableData.value = res.data.records || []
      clinicPagination.value.total = res.data.total || 0
    }
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{

  } finally {
    clinicLoading.value = false
  }
}
const handleClinicSizeChange = (v) => {
  clinicPagination.value.pageSize = v
  loadClinicData()
}
const handleClinicCurrentChange = (v) => {
  clinicPagination.value.pageNum = v
  loadClinicData()
}
const handleClinicSelectionChange = (rows) => {
  clinicSelectedRows.value = rows
}
const handleClinicAudit = async (row) => {
  try {
    const res = await auditPublishClinicSchedule(row.id)
    if (res.code === 200) {
      ElMessage.success('已审核发布')
      loadClinicData()
    }
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{

  }
}
const handleClinicReject = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt('请填写驳回原因（排班人会在留痕里看到）', '驳回门诊排班', {
      confirmButtonText: '确定驳回',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：号源与诊室冲突，请调整后再报',
      inputValidator: (v) => (v && v.trim() ? true : '驳回原因不能为空'),
    })
    const res = await rejectClinicSchedule({scheduleId: row.id, reason: value})
    if (res.code === 200) {
      ElMessage.success('已驳回')
      loadClinicData()
    }
  } catch (e)  /* 取消或错误：取消无提示，错误已由全局拦截器统一提示 */{

  }
}
const handleClinicBatchAudit = async () => {
  const rows = clinicSelectedRows.value
  if (rows.length === 0) {
    ElMessage.warning('请先勾选要审核发布的排班')
    return
  }
  try {
    await ElMessageBox.confirm(
        `确定审核发布选中的 ${rows.length} 条门诊排班吗？发布后正式出号。`, '批量审核发布', {type: 'warning'})
  } catch {
    return
  }
  clinicBatchLoading.value = true
  let ok = 0
  try {
    for (const row of rows) {
      try {
        await auditPublishClinicSchedule(row.id)
        ok++
      } catch (e) {
        // 失败的单条由全局拦截器提示，这里继续跑后面的
      }
    }
    if (ok > 0) {
      ElMessage.success(`已审核发布 ${ok} 条`)
    }
    if (ok < rows.length) {
      ElMessage.warning(`${rows.length - ok} 条未通过（多为「不是本科室指定审核人」），详见提示`)
    }
  } finally {
    clinicBatchLoading.value = false
    loadClinicData()
  }
}
const handleClinicBatchReject = async () => {
  const rows = clinicSelectedRows.value
  if (rows.length === 0) {
    ElMessage.warning('请先勾选要驳回的排班')
    return
  }
  let reason = ''
  try {
    const {value} = await ElMessageBox.prompt(
        `驳回选中的 ${rows.length} 条门诊排班，请填写驳回原因（排班人会在留痕里看到）`, '批量驳回', {
          confirmButtonText: '确定驳回',
          cancelButtonText: '取消',
          inputPlaceholder: '例如：号源与诊室冲突，请调整后再报',
          inputValidator: (v) => (v && v.trim() ? true : '驳回原因不能为空'),
        })
    reason = value
  } catch {
    return
  }
  clinicBatchLoading.value = true
  let ok = 0
  try {
    for (const row of rows) {
      try {
        await rejectClinicSchedule({scheduleId: row.id, reason})
        ok++
      } catch (e) {
        // 失败的单条由全局拦截器提示，这里继续跑后面的
      }
    }
    if (ok > 0) {
      ElMessage.success(`已驳回 ${ok} 条`)
    }
    if (ok < rows.length) {
      ElMessage.warning(`${rows.length - ok} 条未通过（多为「不是本科室指定审核人」），详见提示`)
    }
  } finally {
    clinicBatchLoading.value = false
    loadClinicData()
  }
}
const handleTabChange = (name) => {
  if (name === 'clinic' && !clinicLoaded) {
    clinicLoaded = true
    loadClinicData()
  }
}

// ---------------- 变更留痕 ----------------
const logVisible = ref(false)
const logRowId = ref(null)
const logTarget = ref('')

const openChangeLog = (row) => {
  logTarget.value = `${row.scheduleDate} ${row.employeeName}`
  logRowId.value = row.id
  logVisible.value = true
}

onMounted(() => {
  loadData()
})
</script>
