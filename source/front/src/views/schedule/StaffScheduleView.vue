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
      <div class="query-grid-wrap">
        <el-form class="query-grid" label-width="70px" :model="searchForm">
          <el-form-item label="开始日期">
            <el-date-picker v-model="searchForm.startDate" clearable placeholder="开始" type="date"
                            value-format="YYYY-MM-DD"/>
          </el-form-item>
          <el-form-item label="结束日期">
            <el-date-picker v-model="searchForm.endDate" clearable placeholder="结束" type="date"
                            value-format="YYYY-MM-DD"/>
          </el-form-item>
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
          <el-form-item label="岗位类别">
            <el-select v-model="searchForm.staffType" clearable placeholder="全部">
              <el-option v-for="o in STAFF_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="出勤状态">
            <el-select v-model="searchForm.dutyStatus" clearable placeholder="全部">
              <el-option v-for="o in DUTY_STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="仅出诊">
            <el-select v-model="searchForm.clinicFlag" clearable placeholder="全部">
              <el-option :value="1" label="出诊班"/>
              <el-option :value="0" label="非出诊"/>
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="searchForm.keyword" clearable placeholder="姓名/工号"/>
          </el-form-item>
        </el-form>
        <div class="query-grid-ops">
          <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
      </div>
    </el-card>

    <!-- 登记 / 整周复制 / 整周审批：审批动作作用于「当前选中的排班单元 + 审批周」 -->
    <div class="mb-3 flex flex-wrap items-center gap-2">
      <el-button v-perm="'staff:schedule:add'" :icon="Plus" data-testid="staff-schedule-add" type="primary"
                 @click="handleAdd">
        登记排班
      </el-button>
      <el-button v-perm="'staff:schedule:add'" :icon="CopyDocument" @click="copyDialogVisible = true">整周复制</el-button>
      <el-divider direction="vertical"/>
      <span class="text-sm text-slate-500">审批周</span>
      <el-date-picker v-model="approvalWeek" type="date" value-format="YYYY-MM-DD" placeholder="该周任一天"
                      style="width: 150px"/>
      <el-button v-perm="'staff:schedule:add'" :icon="Promotion" :loading="weekLoading" @click="handleSubmitWeek">
        整周提交审核
      </el-button>
      <el-button v-perm="PERM_SCHEDULE_AUDIT" :icon="CircleCheck" :loading="weekLoading" @click="handleAuditWeek">
        整周审核发布
      </el-button>
    </div>

    <el-tabs v-model="activeTab" class="mb-3">
      <el-tab-pane label="排班列表" name="list">
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
          <el-table-column label="发布态" width="100">
            <template #default="{ row }">
              <el-tag :type="publishStatusTagType(Number(row.publishStatus))" size="small">
                {{ row.publishStatusText || publishStatusText(row.publishStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="响应形态" prop="attendModeText" width="110"/>
          <el-table-column label="出诊" width="170">
            <template #default="{ row }">
              <el-tag v-if="clinicBound(row)" size="small" type="warning">
                门诊出诊 {{ Number(row.clinicSourceCount) }} 条号源
              </el-tag>
              <el-tag v-else-if="Number(row.clinicFlag) === 1" size="small" type="info">出诊</el-tag>
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
          <el-table-column fixed="right" label="操作" width="380">
            <template #default="{ row }">
              <el-button v-perm="'staff:schedule:edit'" :icon="Edit" link type="primary" @click="handleEdit(row)">修改
              </el-button>
              <el-button
                  v-if="Number(row.dutyStatus) === 1"
                  v-perm="'staff:schedule:edit'"
                  :icon="Switch"
                  :title="clinicBound(row) ? '已挂门诊号源，换班归门诊排班管理：请到门诊排班页处理' : ''"
                  data-testid="staff-schedule-swap"
                  link
                  type="warning"
                  @click="openSwap(row)"
              >
                换班
              </el-button>
              <el-button v-perm="'staff:schedule:list'" :icon="Clock" link @click="openChangeLog(row)">留痕</el-button>
              <el-button
                  v-perm="'staff:schedule:delete'"
                  :icon="Delete"
                  :title="clinicBound(row) ? '已挂门诊号源，删除归门诊排班管理：请到门诊排班页处理' : ''"
                  link type="danger" @click="handleDelete(row)"
              >
                删除
              </el-button>
              <el-button
                  v-if="canSubmitPublish(row)"
                  v-perm="'staff:schedule:add'"
                  :icon="Promotion"
                  data-testid="staff-schedule-submit"
                  link
                  type="primary"
                  @click="handleSubmitPublish(row)"
              >
                提交审核
              </el-button>
              <el-button
                  v-if="canAuditPublish(row)"
                  v-perm="PERM_SCHEDULE_AUDIT"
                  :icon="CircleCheck"
                  data-testid="staff-schedule-audit"
                  link
                  type="success"
                  @click="handleAuditPublish(row)"
              >
                审核发布
              </el-button>
              <el-button
                  v-if="canAuditPublish(row)"
                  v-perm="PERM_SCHEDULE_AUDIT"
                  :icon="CloseBold"
                  data-testid="staff-schedule-reject"
                  link
                  type="danger"
                  @click="handleReject(row)"
              >
                驳回
              </el-button>
              <el-button
                  v-if="canRecallPublish(row) && Number(row.publishStatus) !== 0"
                  v-perm="'staff:schedule:edit'"
                  :icon="RefreshLeft"
                  data-testid="staff-schedule-recall"
                  link
                  @click="handleRecall(row)"
              >
                撤回
              </el-button>
              <el-button
                  v-if="canAdjustPublish(row)"
                  v-perm="'staff:schedule:edit'"
                  :icon="Switch"
                  data-testid="staff-schedule-adjust"
                  link
                  type="warning"
                  @click="handleAdjustStart(row)"
              >
                进入调班
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
      </el-tab-pane>

      <!-- ============ 变更记录（台账：跨排班单元回看所有留痕） ============ -->
      <el-tab-pane label="变更记录" name="changeLog">
        <el-card class="table-card" shadow="never">
          <div class="mb-2 flex flex-wrap items-center gap-2">
            <el-date-picker v-model="changeLogForm.occurStart" type="date" value-format="YYYY-MM-DD"
                            placeholder="发生起始" style="width: 140px"/>
            <el-date-picker v-model="changeLogForm.occurEnd" type="date" value-format="YYYY-MM-DD"
                            placeholder="发生结束" style="width: 140px"/>
            <el-select v-model="changeLogForm.actionType" clearable placeholder="动作" style="width: 110px">
              <el-option v-for="o in CHANGE_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
            <el-input v-model="changeLogForm.keyword" clearable placeholder="姓名/工号" style="width: 140px"/>
            <el-button :icon="Search" type="primary" @click="changeLogPage.pageNum = 1; loadChangeLog()">查询</el-button>
          </div>
          <el-table v-loading="changeLogLoading" :data="changeLogRows" :max-height="tableMaxHeight" stripe>
            <el-table-column label="发生时间" prop="occurTime" width="160"/>
            <el-table-column label="排班日期" prop="scheduleDate" width="110"/>
            <el-table-column label="排班单元" prop="orgName" min-width="140"/>
            <el-table-column label="动作" width="100">
              <template #default="{ row }">{{ row.actionTypeText || scheduleChangeTypeText(row.actionType) }}</template>
            </el-table-column>
            <el-table-column label="值班人变化" min-width="150">
              <template #default="{ row }">{{ row.fromEmployeeName || '—' }}<span v-if="row.toEmployeeName"> → {{ row.toEmployeeName }}</span></template>
            </el-table-column>
            <el-table-column label="班次变化" min-width="150">
              <template #default="{ row }">{{ row.fromShiftName || '—' }}<span v-if="row.toShiftName"> → {{ row.toShiftName }}</span></template>
            </el-table-column>
            <el-table-column label="号数" prop="amount" width="70"/>
            <el-table-column label="原因" prop="reason" min-width="160" show-overflow-tooltip/>
            <el-table-column label="操作人" prop="createBy" width="110"/>
          </el-table>
          <div class="list-footer mt-2 flex items-center justify-end">
            <el-pagination
                v-model:current-page="changeLogPage.pageNum"
                v-model:page-size="changeLogPage.pageSize"
                :page-sizes="PAGE_SIZES"
                :total="changeLogPage.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="changeLogPage.pageSize = $event; loadChangeLog()"
                @current-change="loadChangeLog"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- ============ 规则校验（连续夜班/连续上班/单周工时/班次冲突/人力配置） ============ -->
      <el-tab-pane label="规则校验" name="check">
        <el-card class="table-card" shadow="never">
          <div class="mb-2 flex flex-wrap items-center gap-2">
            <el-date-picker v-model="checkForm.startDate" type="date" value-format="YYYY-MM-DD"
                            placeholder="开始日期" style="width: 140px"/>
            <el-date-picker v-model="checkForm.endDate" type="date" value-format="YYYY-MM-DD"
                            placeholder="结束日期" style="width: 140px"/>
            <el-select v-model="checkForm.staffType" clearable placeholder="岗位类别" style="width: 130px">
              <el-option v-for="o in STAFF_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
            <el-button :icon="Search" type="primary" :loading="checkLoading" @click="runCheck">开始校验</el-button>
            <span class="text-sm text-slate-500">
              告警 {{ checkResult.alertCount ?? 0 }} 条 · 提示 {{ checkResult.hintCount ?? 0 }} 条
            </span>
          </div>
          <el-table v-loading="checkLoading" :data="checkResult.warnings || []" :max-height="tableMaxHeight" stripe>
            <el-table-column label="级别" width="80">
              <template #default="{ row }">
                <el-tag :type="Number(row.level) === 1 ? 'danger' : 'warning'" size="small">
                  {{ Number(row.level) === 1 ? '告警' : '提示' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="规则" prop="typeText" width="110"/>
            <el-table-column label="日期" prop="scheduleDate" width="105"/>
            <el-table-column label="班次" min-width="120">
              <template #default="{ row }">{{ row.shiftName || '全部班次' }}</template>
            </el-table-column>
            <el-table-column label="员工" width="130">
              <template #default="{ row }">{{ row.employeeName || '—' }}<span v-if="row.staffTypeName" class="text-gray-400">（{{ row.staffTypeName }}）</span></template>
            </el-table-column>
            <el-table-column label="实际/标准" width="90">
              <template #default="{ row }">
                <span v-if="row.actual != null">{{ row.actual }}<template v-if="row.required != null">/{{ row.required }}</template></span>
                <span v-else>—</span>
              </template>
            </el-table-column>
            <el-table-column label="说明" prop="message" min-width="260" show-overflow-tooltip/>
          </el-table>
          <el-empty v-if="!checkLoading && checkResult.warnings && checkResult.warnings.length === 0"
                    :image-size="60" description="区间内没有违反规则的排班"/>
        </el-card>
      </el-tab-pane>

      <!-- ============ 月度工时（一人一行，含整段未排班的人） ============ -->
      <el-tab-pane label="月度工时" name="workload">
        <el-card class="table-card" shadow="never">
          <div class="mb-2 flex flex-wrap items-center gap-2">
            <el-date-picker v-model="workloadForm.month" type="month" value-format="YYYY-MM"
                            placeholder="选择月份" style="width: 140px"/>
            <el-select v-model="workloadForm.staffType" clearable placeholder="岗位类别" style="width: 130px">
              <el-option v-for="o in STAFF_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
            <el-button :icon="Search" type="primary" :loading="workloadLoading" @click="loadWorkload">统计</el-button>
            <span v-if="workloadResult.rows.length" class="text-sm text-slate-500">
              {{ workloadResult.staffCount ?? 0 }} 人 · 总上班 {{ workloadResult.totalWorkDays ?? 0 }} 天 ·
              总夜班 {{ workloadResult.totalNightDays ?? 0 }} 天 · 总工时 {{ workloadResult.totalWorkHours ?? 0 }} 小时
            </span>
          </div>
          <el-table v-loading="workloadLoading" :data="workloadResult.rows" :max-height="tableMaxHeight" stripe>
            <el-table-column label="工号" prop="empCode" width="100"/>
            <el-table-column label="姓名" prop="employeeName" width="100"/>
            <el-table-column label="岗位" prop="staffTypeName" width="90"/>
            <el-table-column label="上班" prop="workDays" width="70" align="center"/>
            <el-table-column label="休息" prop="restDays" width="70" align="center"/>
            <el-table-column label="请假" prop="leaveDays" width="70" align="center"/>
            <el-table-column label="培训" prop="trainingDays" width="70" align="center"/>
            <el-table-column label="停班" prop="suspendedDays" width="70" align="center"/>
            <el-table-column label="夜班" prop="nightDays" width="70" align="center"/>
            <el-table-column label="工时(分)" prop="workMinutes" width="90" align="right"/>
            <el-table-column label="工时(时)" prop="workHours" width="90" align="right"/>
            <el-table-column label="缺排班天数" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="Number(row.missingDays) > 0" size="small" type="warning">{{ row.missingDays }}</el-tag>
                <span v-else class="text-gray-400">0</span>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!workloadLoading && workloadResult.rows.length === 0"
                    :image-size="60" description="该月该口径下没有排班记录"/>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 登记 / 修改 -->
    <StaffScheduleEdit v-model="dialogVisible" :row="editRow" @saved="handleEditSaved"/>

    <!-- 换班 / 代班 -->
    <StaffScheduleSwapEdit v-model="swapDialogVisible" :row="swapRow" :table-data="tableData"
                           @saved="handleEditSaved"/>

    <!-- 整周复制 -->
    <StaffScheduleCopyEdit v-model="copyDialogVisible" @saved="loadData"/>

    <!-- 变更留痕 -->
    <ScheduleChangeLogDrawer v-model="logVisible" :staff-schedule-id="logRowId" :title="`变更留痕 · ${logTarget}`"/>
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
import {
  CircleCheck,
  Clock,
  CloseBold,
  CopyDocument,
  Delete,
  Edit,
  Plus,
  Promotion,
  Refresh,
  RefreshLeft,
  Search,
  Switch
} from '@element-plus/icons-vue'
import {
  adjustStartStaffSchedule,
  auditPublishStaffSchedule,
  auditWeekStaffSchedule,
  checkStaffSchedule,
  deleteStaffSchedule,
  getScheduleChangeLogPage,
  getStaffOnDuty,
  getStaffScheduleListPage,
  getStaffScheduleUnitSelectList,
  getStaffWorkload,
  recallStaffSchedule,
  rejectStaffSchedule,
  submitStaffSchedule,
  submitWeekStaffSchedule,
} from '@/api/staffSchedule'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {publishStatusTagType, publishStatusText} from '@/lib/publishStatus'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {
  canAdjustPublish,
  canAuditPublish,
  canRecallPublish,
  canSubmitPublish,
  DUTY_STATUS_OPTIONS,
  dutyStatusText,
  ORG_UNIT_TYPE_OPTIONS,
  orgUnitTypeText,
  PERM_SCHEDULE_AUDIT,
  scheduleChangeTypeText,
  scheduleSourceText,
} from '@/lib/staffSchedule'
import {STAFF_TYPE_OPTIONS} from '@/lib/scheduleShift'
import StaffScheduleEdit from './StaffScheduleEdit.vue'
import StaffScheduleSwapEdit from './StaffScheduleSwapEdit.vue'
import StaffScheduleCopyEdit from './StaffScheduleCopyEdit.vue'
import ScheduleChangeLogDrawer from './ScheduleChangeLogDrawer.vue'

const loading = ref(false)
const tableData = ref([])
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const onDutyList = ref([])
const onDutyLoading = ref(false)

const searchForm = ref({
  startDate: null, endDate: null, orgType: null, orgId: null, staffType: null, dutyStatus: null,
  clinicFlag: null, keyword: '',
})

// ---------------- 排班单元候选（科室/病区/全院） ----------------
// 单元 ID 空间不统一（科室 ID 与病区 ID 各自编号），所以必须先定类型再选单元，
// 没定类型时单元下拉直接禁用 —— 只按 id 过滤会把另一个空间的行也捞进来。
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
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
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
    startDate: null, endDate: null, orgType: null, orgId: null, staffType: null, dutyStatus: null,
    clinicFlag: null, keyword: '',
  }
  unitOptions.value = []
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

/**
 * 员工→门诊联动穿透标记：这条行被门诊号源引用（clinicSourceCount>0）时归门诊排班管理，
 * 员工侧的删除/换班/发布态动作会被后端拦截；仅「上班→停班」的修改走联动停诊。
 */
const clinicBound = (row) => Number(row.clinicSourceCount || 0) > 0

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
    } 
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
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
const logRowId = ref(null)
const logTarget = ref('')

const openChangeLog = (row) => {
  logTarget.value = `${row.scheduleDate} ${row.employeeName}`
  logRowId.value = row.id
  logVisible.value = true
}

// ---------------- 发布态流转（提交 → 审核发布 / 驳回 / 撤回 / 调班） ----------------
// 提交/审核/驳回/撤回都会改 publish_status，所以每次动作后列表与「此刻在岗」都要跟着变。
const handleSubmitPublish = async (row) => {
  try {
    const res = await submitStaffSchedule(row.id)
    if (res.code === 200) {
      ElMessage.success('已提交审核')
      loadData()
      loadOnDuty()
    }
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
}

const handleAuditPublish = async (row) => {
  try {
    const res = await auditPublishStaffSchedule(row.id)
    if (res.code === 200) {
      ElMessage.success('已审核发布')
      loadData()
      loadOnDuty()
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
      loadOnDuty()
    }
  } catch (e)  /* 取消或错误：取消无提示，错误已由全局拦截器统一提示 */{
    
  }
}

const handleRecall = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确定撤回 ${row.scheduleDate} ${row.employeeName} 的这条排班吗？撤回后不计在岗。`,
        '撤回排班', {type: 'warning'})
    const res = await recallStaffSchedule(row.id)
    if (res.code === 200) {
      ElMessage.success('已撤回')
      loadData()
      loadOnDuty()
    }
  } catch (e)  /* 取消或错误：取消无提示，错误已由全局拦截器统一提示 */{
    
  }
}

const handleAdjustStart = async (row) => {
  try {
    const res = await adjustStartStaffSchedule(row.id)
    if (res.code === 200) {
      ElMessage.success('已进入调班')
      loadData()
      loadOnDuty()
    }
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
}

// ---------------- 整周提交 / 整周审核发布 ----------------
// 整周动作必须落到某一个排班单元上（单元类型 + 单元），所以复用查询卡里选好的单元。
const approvalWeek = ref('')
const weekLoading = ref(false)

const currentUnit = () => {
  if (!searchForm.value.orgType) {
    ElMessage.warning('请先在查询条件里选择「单元类型」')
    return null
  }
  if (searchForm.value.orgId === null || searchForm.value.orgId === undefined) {
    ElMessage.warning('请先在查询条件里选择「排班单元」')
    return null
  }
  return {orgType: searchForm.value.orgType, orgId: searchForm.value.orgId}
}

const handleSubmitWeek = async () => {
  const unit = currentUnit()
  if (!unit) return
  weekLoading.value = true
  try {
    const res = await submitWeekStaffSchedule({...unit, weekStart: approvalWeek.value || undefined})
    if (res.code === 200) {
      ElMessage.success(res.message || '已提交审核')
      loadData()
      loadOnDuty()
    }
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    weekLoading.value = false
  }
}

const handleAuditWeek = async () => {
  const unit = currentUnit()
  if (!unit) return
  weekLoading.value = true
  try {
    const res = await auditWeekStaffSchedule({...unit, weekStart: approvalWeek.value || undefined})
    if (res.code === 200) {
      ElMessage.success(res.message || '已审核发布')
      loadData()
      loadOnDuty()
    }
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    weekLoading.value = false
  }
}

onMounted(() => {
  loadData()
  loadOnDuty()
})

// ---------------- 页签 ----------------
const activeTab = ref('list')

// ---------------- 变更记录（台账） ----------------
const CHANGE_TYPE_OPTIONS = [
  {value: 1, label: '换班'},
  {value: 2, label: '代班'},
  {value: 3, label: '停班'},
  {value: 4, label: '加号'},
  {value: 5, label: '减号'},
  {value: 6, label: '出诊变更'},
  {value: 7, label: '提交审核'},
  {value: 8, label: '审核发布'},
  {value: 9, label: '驳回'},
  {value: 10, label: '整周提交'},
  {value: 11, label: '整周审核发布'},
  {value: 12, label: '撤回'},
  {value: 13, label: '进入调班'},
]
const changeLogForm = ref({occurStart: null, occurEnd: null, actionType: null, keyword: ''})
const changeLogPage = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
const changeLogRows = ref([])
const changeLogLoading = ref(false)

const loadChangeLog = async () => {
  changeLogLoading.value = true
  try {
    const res = await getScheduleChangeLogPage({
      ...changeLogForm.value,
      pageNum: changeLogPage.value.pageNum,
      pageSize: changeLogPage.value.pageSize,
    })
    if (res.code === 200) {
      changeLogRows.value = res.data.records || []
      changeLogPage.value.total = res.data.total || 0
    }
  } catch {
    changeLogRows.value = []
  } finally {
    changeLogLoading.value = false
  }
}

// ---------------- 规则校验 ----------------
const pad = (n) => String(n).padStart(2, '0')
const checkForm = ref({startDate: null, endDate: null, staffType: null})
const checkResult = ref({alertCount: 0, hintCount: 0, warnings: []})
const checkLoading = ref(false)

const runCheck = async () => {
  if (!checkForm.value.startDate || !checkForm.value.endDate) {
    ElMessage.warning('请选择校验日期区间')
    return
  }
  checkLoading.value = true
  try {
    const res = await checkStaffSchedule(checkForm.value)
    checkResult.value = res.code === 200 ? (res.data || {alertCount: 0, hintCount: 0, warnings: []}) : {alertCount: 0, hintCount: 0, warnings: []}
  } catch {
    checkResult.value = {alertCount: 0, hintCount: 0, warnings: []}
  } finally {
    checkLoading.value = false
  }
}

// ---------------- 月度工时 ----------------
const workloadForm = ref({month: null, staffType: null})
const workloadResult = ref({staffCount: 0, totalWorkDays: 0, totalNightDays: 0, totalWorkHours: 0, rows: []})
const workloadLoading = ref(false)

const loadWorkload = async () => {
  if (!workloadForm.value.month) {
    ElMessage.warning('请选择统计月份')
    return
  }
  workloadLoading.value = true
  try {
    const res = await getStaffWorkload(workloadForm.value)
    workloadResult.value = res.code === 200
        ? (res.data || {staffCount: 0, totalWorkDays: 0, totalNightDays: 0, totalWorkHours: 0, rows: []})
        : {staffCount: 0, totalWorkDays: 0, totalNightDays: 0, totalWorkHours: 0, rows: []}
  } catch {
    workloadResult.value = {staffCount: 0, totalWorkDays: 0, totalNightDays: 0, totalWorkHours: 0, rows: []}
  } finally {
    workloadLoading.value = false
  }
}

// 初始默认值：校验区间取本月 1 号 ~ 今天，工时统计取本月
{
  const now = new Date()
  const ymd = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`
  const ym = `${now.getFullYear()}-${pad(now.getMonth() + 1)}`
  checkForm.value.endDate = ymd
  checkForm.value.startDate = `${ym}-01`
  workloadForm.value.month = ym
}
</script>
