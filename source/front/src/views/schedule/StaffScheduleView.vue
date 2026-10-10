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
          <el-button v-perm="'org:schedule:add'" :icon="CopyDocument" @click="copyDialogVisible = true">整周复制
          </el-button>
          <el-button v-perm="'org:schedule:add'" :icon="Plus" data-testid="staff-schedule-add" type="primary"
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
              <el-button v-perm="'org:schedule:edit'" :icon="Edit" link type="primary" @click="handleEdit(row)">修改
              </el-button>
              <el-button
                  v-if="Number(row.dutyStatus) === 1"
                  v-perm="'org:schedule:edit'"
                  :icon="Switch"
                  data-testid="staff-schedule-swap"
                  link
                  type="warning"
                  @click="openSwap(row)"
              >
                换班
              </el-button>
              <el-button v-perm="'org:schedule:list'" :icon="Clock" link @click="openChangeLog(row)">留痕</el-button>
              <el-button v-perm="'org:schedule:delete'" :icon="Delete" link type="danger" @click="handleDelete(row)">
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="120px">
        <el-form-item label="排班日期" prop="scheduleDate">
          <div data-testid="ss-date">
            <el-date-picker
                v-model="formData.scheduleDate"
                placeholder="夜班请填开始日"
                style="width: 100%"
                type="date"
                value-format="YYYY-MM-DD"
            />
          </div>
        </el-form-item>
        <el-form-item label="排班单元类型" prop="orgType">
          <el-radio-group v-model="formData.orgType" @change="formData.orgId = null">
            <el-radio v-for="o in ORG_UNIT_TYPE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="orgIdRequired" :label="formData.orgType === 2 ? '病区' : '科室'" prop="orgId">
          <el-select
              v-model="formData.orgId"
              :placeholder="formData.orgType === 2 ? '选择病区' : '选择科室'"
              filterable
              style="width: 100%"
          >
            <el-option v-for="o in orgIdOptions" :key="o.id" :label="o.name" :value="o.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="排班对象" prop="employeeId">
          <div data-testid="ss-employee">
            <el-select
                v-model="formData.employeeId"
                :loading="empLoading"
                :remote-method="searchEmployee"
                filterable
                placeholder="输入姓名搜索"
                remote
                reserve-keyword
                style="width: 100%"
            >
              <el-option
                  v-for="e in empOptions"
                  :key="e.id"
                  :label="e.deptName ? `${e.empName}（${e.deptName}）` : e.empName"
                  :value="e.id"
              />
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="出勤状态" prop="dutyStatus">
          <el-select v-model="formData.dutyStatus" style="width: 100%">
            <el-option v-for="o in DUTY_STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
        <el-form-item v-if="needShift" label="班次" prop="shiftId">
          <el-select v-model="formData.shiftId" placeholder="选择班次（时间与工时由班次带出）" style="width: 100%">
            <el-option v-for="s in shiftOptions" :key="s.id" :label="s.shiftName" :value="s.id"/>
          </el-select>
        </el-form-item>
        <el-form-item v-else label="班次">
          <el-alert :closable="false" title="休息/请假/培训/停班没有班次：这类行也要落库，否则看不出「今天没排他」是有意还是漏排"
                    type="info"/>
        </el-form-item>
        <el-form-item v-if="needShift" label="响应形态">
          <el-radio-group v-model="formData.attendMode">
            <el-radio v-for="o in ATTEND_MODE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="needShift" label="是否出诊">
          <el-switch
              v-model="formData.clinicFlag"
              :active-value="1"
              :disabled="!clinicFlagAllowed"
              :inactive-value="0"
          />
          <span class="ml-2 text-xs text-gray-400">
            {{ clinicFlagAllowed ? '只有医生岗的出诊班会生成门诊号源（非医生岗后端会裁掉）' : '听班不放号：人在院外待命' }}
          </span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" :rows="2" placeholder="如：本周三下午参加院感培训" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button :loading="submitLoading" data-testid="ss-submit" type="primary" @click="handleSubmit">确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 换班 / 代班 -->
    <el-dialog v-model="swapDialogVisible" title="换班 / 代班" width="560px">
      <el-alert
          :closable="false"
          show-icon
          style="margin-bottom: 12px"
          title="选了「对方排班」就是两人对调（换班）；不选、直接选人就是这一班改由他承接（代班）。两种都会留痕。"
          type="info"
      />
      <el-form ref="swapFormRef" :model="swapForm" :rules="swapRules" label-width="120px">
        <el-form-item label="对方排班">
          <el-select
              v-model="swapForm.toScheduleId"
              clearable
              filterable
              placeholder="同日同单元的另一条排班（留空=代班）"
              style="width: 100%"
          >
            <el-option
                v-for="r in tableData.filter((x) => Number(x.dutyStatus) === 1)"
                :key="r.id"
                :label="`${r.employeeName} · ${r.shiftName} ${r.startTime}~${r.endTime}`"
                :value="r.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!swapForm.toScheduleId" label="承接人" prop="substituteEmployeeId">
          <div data-testid="ss-substitute">
            <el-select
                v-model="swapForm.substituteEmployeeId"
                :loading="empLoading"
                :remote-method="searchEmployee"
                filterable
                placeholder="输入姓名搜索"
                remote
                reserve-keyword
                style="width: 100%"
            >
              <el-option
                  v-for="e in empOptions"
                  :key="e.id"
                  :label="e.deptName ? `${e.empName}（${e.deptName}）` : e.empName"
                  :value="e.id"
              />
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="变更原因" prop="reason">
          <div data-testid="ss-swap-reason">
            <el-input v-model="swapForm.reason" :rows="2" placeholder="必填：如张三参加抢救无法脱身" type="textarea"/>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="swapDialogVisible = false">取消</el-button>
        <el-button :loading="submitLoading" data-testid="ss-swap-submit" type="primary" @click="handleSwap">确定变更
        </el-button>
      </template>
    </el-dialog>

    <!-- 整周复制 -->
    <el-dialog v-model="copyDialogVisible" title="整周复制排班" width="560px">
      <el-alert
          :closable="false"
          show-icon
          style="margin-bottom: 12px"
          title="只按星期对齐：来源区间与目标区间长度必须相同（整周=7 天），否则周一的班会错位到别的星期。"
          type="info"
      />
      <el-form ref="copyFormRef" :model="copyForm" :rules="copyRules" label-width="150px">
        <el-form-item label="排班单元类型">
          <el-radio-group v-model="copyForm.orgType" @change="copyForm.orgId = null">
            <el-radio v-for="o in ORG_UNIT_TYPE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="来源区间开始日" prop="fromStartDate">
          <el-date-picker v-model="copyForm.fromStartDate" style="width: 100%" type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="来源区间结束日">
          <el-date-picker v-model="copyForm.fromEndDate" style="width: 100%" type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="目标区间开始日" prop="toStartDate">
          <el-date-picker v-model="copyForm.toStartDate" style="width: 100%" type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item>
          <el-button size="small" @click="fillLastWeekToNext">填入「上上周 → 上周」</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="copyDialogVisible = false">取消</el-button>
        <el-button :loading="submitLoading" data-testid="ss-copy-submit" type="primary" @click="handleCopy">开始复制
        </el-button>
      </template>
    </el-dialog>

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
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Clock, CopyDocument, Delete, Edit, Plus, Refresh, Search, Switch} from '@element-plus/icons-vue'
import {
  deleteStaffSchedule,
  getScheduleChangeLogList,
  getStaffOnDuty,
  getStaffScheduleListPage,
  staffScheduleCopyRange,
  staffScheduleSwap,
  staffScheduleUpsert,
} from '@/api/staffSchedule'
import {getDepartmentSelectList, getEmployeeList} from '@/api/system'
import {getShiftSelectList} from '@/api/appoint'
import {getNurseWardSelectList} from '@/api/nurseSchedule'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {
  ATTEND_MODE_OPTIONS,
  DUTY_STATUS_OPTIONS,
  DUTY_STATUS_WORK,
  dutyStatusNeedShift,
  dutyStatusText,
  ORG_UNIT_TYPE_HOSPITAL,
  ORG_UNIT_TYPE_OPTIONS,
  orgUnitTypeText,
  scheduleChangeTypeText,
  scheduleSourceText,
} from '@/lib/staffSchedule'
import {STAFF_TYPE_OPTIONS} from '@/lib/scheduleShift'

// 出勤排班走 4-全院通用班次册：按岗位类别选本册班次，别把门诊出诊册的班次混进来
const SHIFT_USE_SCOPE_GENERAL = 4

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const onDutyList = ref([])
const onDutyLoading = ref(false)

const searchForm = ref({
  startDate: null, endDate: null, orgType: null, staffType: null, dutyStatus: null,
  clinicFlag: null, keyword: '',
})

// ---------------- 下拉 ----------------
const empOptions = ref([])
const empLoading = ref(false)
const deptOptions = ref([])
const wardOptions = ref([])
const shiftOptions = ref([])

const searchEmployee = async (keyword) => {
  empLoading.value = true
  try {
    const res = await getEmployeeList({empName: keyword || '', pageSize: 50})
    empOptions.value = (res?.data || []).map((e) => ({
      id: e.id, empName: e.empName, deptName: e.deptName,
    }))
  } catch {
    empOptions.value = []
  } finally {
    empLoading.value = false
  }
}

/** 单元候选随「排班单元类型」切换；全院级没有单元，后端要求 org_id=0 */
const orgIdOptions = computed(() => {
  if (formData.orgType === 2) return wardOptions.value
  return deptOptions.value
})
const orgIdRequired = computed(() => Number(formData.orgType) !== ORG_UNIT_TYPE_HOSPITAL)

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
const dialogTitle = ref('登记排班')
const formRef = ref(null)
const formData = reactive({
  id: null, scheduleDate: null, orgType: 1, orgId: null, employeeId: null, employeePostId: null,
  shiftId: null, dutyStatus: DUTY_STATUS_WORK, attendMode: 1, clinicFlag: 0, remark: '',
})
const rules = {
  scheduleDate: [{required: true, message: '请选择排班日期', trigger: 'change'}],
  orgType: [{required: true, message: '请选择排班单元类型', trigger: 'change'}],
  employeeId: [{required: true, message: '请选择排班对象', trigger: 'change'}],
  dutyStatus: [{required: true, message: '请选择出勤状态', trigger: 'change'}],
}

const needShift = computed(() => dutyStatusNeedShift(formData.dutyStatus))
/**
 * 听班一律不放号：AttendModeEnum.releasesSource —— 给一个在家待命的人放号，患者到了没人看。
 * 「是不是医生岗」这里不判：岗位类别由 员工→岗位→角色→sys_role.staff_type 三级派生，
 * 员工接口只给到 posts、没有 staff_type，前端判就是猜。这一层由服务端 applyStaffType 收口，
 * 传了不合规的出诊标志后端会裁成 0 并在返回文案里说明。
 */
const clinicFlagAllowed = computed(() => Number(formData.attendMode) !== 2)

const resetForm = () => {
  formData.id = null
  formData.scheduleDate = null
  formData.orgType = 1
  formData.orgId = null
  formData.employeeId = null
  formData.employeePostId = null
  formData.shiftId = null
  formData.dutyStatus = DUTY_STATUS_WORK
  formData.attendMode = 1
  formData.clinicFlag = 0
  formData.remark = ''
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '登记排班'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  dialogTitle.value = '修改排班'
  formData.id = row.id
  formData.scheduleDate = row.scheduleDate
  formData.orgType = row.orgType
  formData.orgId = row.orgId || null
  formData.employeeId = row.employeeId
  formData.employeePostId = row.employeePostId || null
  formData.shiftId = row.shiftId || null
  formData.dutyStatus = row.dutyStatus
  formData.attendMode = row.attendMode || 1
  formData.clinicFlag = row.clinicFlag ?? 0
  formData.remark = row.remark || ''
  if (row.employeeId && !empOptions.value.some((e) => e.id === row.employeeId)) {
    empOptions.value = [{id: row.employeeId, empName: row.employeeName, deptName: row.deptName}, ...empOptions.value]
  }
  // 班次可能已被停用而从下拉里剔除，补进去保证回显是班次名而不是空
  if (row.shiftId && !shiftOptions.value.some((s) => String(s.id) === String(row.shiftId))) {
    shiftOptions.value = [{id: row.shiftId, shiftName: row.shiftName || '已停用班次'}, ...shiftOptions.value]
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitLoading.value = true
  try {
    const payload = {
      ...formData,
      orgId: orgIdRequired.value ? formData.orgId : null,
      shiftId: needShift.value ? formData.shiftId : null,
      clinicFlag: clinicFlagAllowed.value ? formData.clinicFlag : 0,
    }
    const res = await staffScheduleUpsert(payload)
    if (res.code === 200) {
      // 超出人力上限时后端不拦但会带文案回来，必须把这句话给到排班员
      ElMessage.success(res.message || (formData.id ? '修改成功' : '登记成功'))
      dialogVisible.value = false
      loadData()
      loadOnDuty()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
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
const swapFormRef = ref(null)
const swapForm = reactive({fromScheduleId: null, toScheduleId: null, substituteEmployeeId: null, reason: ''})
const swapRules = {reason: [{required: true, message: '请填写变更原因', trigger: 'blur'}]}

const openSwap = (row) => {
  swapForm.fromScheduleId = row.id
  swapForm.toScheduleId = null
  swapForm.substituteEmployeeId = null
  swapForm.reason = ''
  swapDialogVisible.value = true
}

const handleSwap = async () => {
  if (!swapFormRef.value) return
  try {
    await swapFormRef.value.validate()
  } catch {
    return
  }
  submitLoading.value = true
  try {
    const payload = {
      fromScheduleId: swapForm.fromScheduleId,
      reason: swapForm.reason,
    }
    // 传了 toScheduleId 就是互换，不传就是代班 —— 两种形态别混着传
    if (swapForm.toScheduleId) payload.toScheduleId = swapForm.toScheduleId
    else payload.substituteEmployeeId = swapForm.substituteEmployeeId
    const res = await staffScheduleSwap(payload)
    if (res.code === 200) {
      ElMessage.success(res.message || '变更成功')
      swapDialogVisible.value = false
      loadData();
      loadOnDuty()
    } else {
      ElMessage.error(res.message || '变更失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '变更失败')
  } finally {
    submitLoading.value = false
  }
}

// ---------------- 整周复制 ----------------
const copyDialogVisible = ref(false)
const copyFormRef = ref(null)
const copyForm = reactive({orgType: 1, orgId: null, fromStartDate: null, fromEndDate: null, toStartDate: null})
const copyRules = {
  fromStartDate: [{required: true, message: '请选择来源区间开始日', trigger: 'change'}],
  toStartDate: [{required: true, message: '请选择目标区间开始日', trigger: 'change'}],
}

/** 一键把「上一周」整周搬到「下一周」：源/目标长度必须相同，否则周一的班会错位到别的星期 */
const fillLastWeekToNext = () => {
  const dayOffset = (d, n) => {
    const x = new Date(d)
    x.setDate(x.getDate() + n)
    return x.toISOString().slice(0, 10)
  }
  const today = new Date()
  copyForm.fromStartDate = dayOffset(today, -14)
  copyForm.fromEndDate = dayOffset(today, -8)
  copyForm.toStartDate = dayOffset(today, -7)
}

const handleCopy = async () => {
  if (!copyFormRef.value) return
  try {
    await copyFormRef.value.validate()
  } catch {
    return
  }
  submitLoading.value = true
  try {
    const res = await staffScheduleCopyRange({...copyForm})
    if (res.code === 200) {
      ElMessage.success(res.message || '复制完成')
      copyDialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message || '复制失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '复制失败')
  } finally {
    submitLoading.value = false
  }
}

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

onMounted(async () => {
  searchEmployee('')
  loadData()
  loadOnDuty()
  try {
    const [dRes, wRes, sRes] = await Promise.all([
      getDepartmentSelectList({}),
      getNurseWardSelectList({}),
      getShiftSelectList({status: 1, useScope: SHIFT_USE_SCOPE_GENERAL}),
    ])
    deptOptions.value = (dRes?.data || []).map((d) => ({id: d.id ?? d.deptId, name: d.deptName ?? d.name}))
    wardOptions.value = (wRes?.data || []).map((w) => ({id: w.id ?? w.wardId, name: w.wardName ?? w.name}))
    shiftOptions.value = (sRes?.data || []).map((s) => ({id: s.id, shiftName: s.shiftName}))
  } catch (e) {
    ElMessage.error(e.message || '加载下拉数据失败')
  }
})
</script>
