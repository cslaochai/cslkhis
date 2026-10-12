<template>
  <div data-testid="staff-schedule-view">
    <!-- 此刻在岗：这一页的脸，回答「现在打电话给谁」 -->
    <el-card class="mb-3" data-testid="on-duty-card" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">此刻在岗</span>
          <el-button :icon="Refresh" :loading="onDutyLoading" size="small" @click="loadOnDuty">刷新</el-button>
        </div>
      </template>
      <div v-loading="onDutyLoading">
        <el-empty v-if="onDutyList.length === 0" :image-size="60" description="此刻没有人在班（夜班跨零点归昨天的班）"/>
        <div v-else class="flex flex-wrap gap-2" data-testid="on-duty-list">
          <el-tag
              v-for="p in onDutyList"
              :key="`${p.employeeId}-${p.shiftId}`"
              effect="light"
              size="large"
              type="success"
          >
            {{ p.employeeName }} · {{ p.staffTypeName }} · {{ p.orgName }} · {{ p.shiftName }} {{
              p.startTime
            }}~{{ p.endTime }}
            <span v-if="p.attendMode && p.attendMode !== 1"> · {{ p.attendModeText }}</span>
          </el-tag>
        </div>
      </div>
    </el-card>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="开始日期">
            <el-date-picker v-model="searchForm.startDate" clearable placeholder="开始" type="date"
                            value-format="YYYY-MM-DD"/>
          </el-form-item>
          <el-form-item label="结束日期">
            <el-date-picker v-model="searchForm.endDate" clearable placeholder="结束" type="date"
                            value-format="YYYY-MM-DD"/>
          </el-form-item>
          <el-form-item label="岗位类别">
            <el-select v-model="searchForm.staffType" clearable placeholder="全部" style="width: 130px">
              <el-option v-for="o in STAFF_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="出勤状态">
            <el-select v-model="searchForm.dutyStatus" clearable placeholder="全部" style="width: 110px">
              <el-option v-for="o in DUTY_STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="仅出诊">
            <el-select v-model="searchForm.clinicFlag" clearable placeholder="全部" style="width: 100px">
              <el-option :value="1" label="出诊班"/>
              <el-option :value="0" label="非出诊"/>
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="searchForm.keyword" clearable placeholder="姓名/工号" style="width: 140px"/>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'staff:schedule:add'" :icon="CopyDocument" @click="copyDialogVisible = true">整周复制
          </el-button>
          <el-button v-perm="'staff:schedule:add'" :icon="Plus" data-testid="staff-schedule-add" type="primary"
                     @click="handleAdd">
            登记排班
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div data-testid="staff-schedule-table">
        <el-table v-loading="loading" :data="tableData" :max-height="tableMaxHeight" stripe>
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
          <el-table-column label="出勤" width="110">
            <template #default="{ row }">
              <el-tag :type="Number(row.dutyStatus) === 1 ? 'success' : 'info'" size="small">
                {{ row.dutyStatusText }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="响应形态" prop="attendModeText" width="110"/>
          <el-table-column label="出诊" width="80">
            <template #default="{ row }">
              <el-tag v-if="Number(row.clinicFlag) === 1" size="small" type="warning">出诊</el-tag>
              <span v-else class="text-gray-400">—</span>
            </template>
          </el-table-column>
          <el-table-column label="工时" prop="workHours" width="80"/>
          <el-table-column label="来源" width="100">
            <template #default="{ row }">{{
                row.scheduleSourceText || scheduleSourceText(row.scheduleSource)
              }}
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="140" prop="remark" show-overflow-tooltip/>
          <el-table-column fixed="right" label="操作" width="300">
            <template #default="{ row }">
              <el-button v-perm="'staff:schedule:edit'" :icon="Edit" link type="primary" @click="handleEdit(row)">修改
              </el-button>
              <el-button
                  v-if="Number(row.dutyStatus) === 1"
                  v-perm="'staff:schedule:edit'"
                  :icon="Switch"
                  data-testid="staff-schedule-swap"
                  link
                  type="warning"
                  @click="openSwap(row)"
              >
                换班
              </el-button>
              <el-button v-perm="'staff:schedule:list'" :icon="Clock" link @click="openChangeLog(row)">留痕</el-button>
              <el-button v-perm="'staff:schedule:delete'" :icon="Delete" link type="danger" @click="handleDelete(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

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

    <!-- 登记 / 修改 -->
    <StaffScheduleEdit v-model="dialogVisible" :row="editRow" @saved="handleEditSaved"/>

    <!-- 换班 / 代班 -->
    <StaffScheduleSwapEdit v-model="swapDialogVisible" :row="swapRow" :table-data="tableData"
                           @saved="handleEditSaved"/>

    <!-- 整周复制 -->
    <StaffScheduleCopyEdit v-model="copyDialogVisible" @saved="loadData"/>

    <!-- 变更留痕 -->
    <el-drawer v-model="logVisible" :title="`变更留痕 · ${logTarget}`" size="520px">
      <div v-loading="logLoading" data-testid="ss-change-log">
        <el-empty v-if="logList.length === 0" :image-size="60" description="这条排班还没有发生过变更"/>
        <el-timeline v-else>
          <el-timeline-item
              v-for="log in logList"
              :key="log.id"
              :timestamp="log.occurTime"
              placement="top"
          >
            <div class="font-medium">
              {{ log.actionTypeText || scheduleChangeTypeText(log.actionType) }}
            </div>
            <div class="text-xs text-gray-500">
              {{ log.fromEmployeeName || '—' }}
              <span v-if="log.toEmployeeName"> → {{ log.toEmployeeName }}</span>
              <span v-if="log.amount"> · 号数 {{ log.amount }}</span>
            </div>
            <div v-if="log.reason" class="text-xs text-gray-500">原因：{{ log.reason }}</div>
            <div v-if="log.createBy" class="text-xs text-gray-400">操作人：{{ log.createBy }}</div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-drawer>
  </div>
</template>

<script lang="js" setup>
/**
 * 全院岗位排班（sql/200 核心表，菜单 2935）。
 *
 * 存在的意义：排班事实以前散在三张表（门诊出诊 / 护理排班 / 总值班），同一个护士可以在两张表里
 * 各排一次且时间重叠都不报错。这张页面操作的是收敛后的唯一事实表 ——
 * 「谁 · 哪天 · 在哪个单元 · 什么班 · 出不出勤」。
 * 出诊计划（号源）不是这里的事：只有 clinic_flag=1 的班才会在 biz_schedule 侧生成出诊行。
 *
 * 顶部「此刻在岗」是这一页的脸：它回答现在打电话给谁，排班列表回答这周怎么排。
 */
import {onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Clock, CopyDocument, Delete, Edit, Plus, Refresh, Search, Switch} from '@element-plus/icons-vue'
import {
  deleteStaffSchedule,
  getScheduleChangeLogList,
  getStaffOnDuty,
  getStaffScheduleListPage,
} from '@/api/staffSchedule'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {
  DUTY_STATUS_OPTIONS,
  dutyStatusText,
  orgUnitTypeText,
  scheduleChangeTypeText,
  scheduleSourceText,
} from '@/lib/staffSchedule'
import {STAFF_TYPE_OPTIONS} from '@/lib/scheduleShift'
import StaffScheduleEdit from './StaffScheduleEdit.vue'
import StaffScheduleSwapEdit from './StaffScheduleSwapEdit.vue'
import StaffScheduleCopyEdit from './StaffScheduleCopyEdit.vue'

const loading = ref(false)
const tableData = ref([])
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const onDutyList = ref([])
const onDutyLoading = ref(false)

const searchForm = ref({
  startDate: null, endDate: null, orgType: null, staffType: null, dutyStatus: null,
  clinicFlag: null, keyword: '',
})

// ---------------- 列表 ----------------
const loadData = async () => {
  loading.value = true
  try {
    const res = await getStaffScheduleListPage({
      ...searchForm.value,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.value.total = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error(e.message || '加载排班失败')
  } finally {
    loading.value = false
  }
}

const loadOnDuty = async () => {
  onDutyLoading.value = true
  try {
    const res = await getStaffOnDuty({})
    onDutyList.value = res.code === 200 ? (res.data || []) : []
  } catch {
    onDutyList.value = []
  } finally {
    onDutyLoading.value = false
  }
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  loadData()
}
const handleReset = () => {
  searchForm.value = {
    startDate: null, endDate: null, orgType: null, staffType: null, dutyStatus: null,
    clinicFlag: null, keyword: '',
  }
  handleSearch()
}
const handleSizeChange = (v) => {
  pagination.value.pageSize = v;
  loadData()
}
const handleCurrentChange = (v) => {
  pagination.value.pageNum = v;
  loadData()
}

// ---------------- 新增 / 修改 ----------------
const dialogVisible = ref(false)
const editRow = ref(null)

const handleAdd = () => {
  editRow.value = null
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editRow.value = row
  dialogVisible.value = true
}

/** 排班变更后「排班列表」与「此刻在岗」都要跟着变，两个弹框共用这一份刷新 */
const handleEditSaved = () => {
  loadData()
  loadOnDuty()
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确定删除 ${row.scheduleDate} ${row.employeeName} 的这条排班吗？（低于最低在岗会被拦下）`,
        '删除排班', {type: 'warning'})
    const res = await deleteStaffSchedule(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData();
      loadOnDuty()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel' && e.message) ElMessage.error(e.message)
  }
}

// ---------------- 换班 / 代班 ----------------
const swapDialogVisible = ref(false)
const swapRow = ref(null)

const openSwap = (row) => {
  swapRow.value = row
  swapDialogVisible.value = true
}

// ---------------- 整周复制 ----------------
const copyDialogVisible = ref(false)

// ---------------- 变更留痕 ----------------
const logVisible = ref(false)
const logLoading = ref(false)
const logList = ref([])
const logTarget = ref('')

const openChangeLog = async (row) => {
  logTarget.value = `${row.scheduleDate} ${row.employeeName}`
  logVisible.value = true
  logLoading.value = true
  try {
    const res = await getScheduleChangeLogList(row.id)
    logList.value = res.code === 200 ? (res.data || []) : []
  } catch {
    logList.value = []
  } finally {
    logLoading.value = false
  }
}

onMounted(() => {
  loadData()
  loadOnDuty()
})
</script>
