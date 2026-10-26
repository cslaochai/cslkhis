<template>
  <div data-testid="schedule-approve-view">
    <el-tabs v-model="activeTab" class="single-tab">
      <el-tab-pane label="门诊排班" name="clinic">
        <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
          <div class="flex items-start justify-between gap-4">
            <el-form :model="clinicSearchForm" inline @submit.prevent>
              <el-form-item label="科室">
                <el-select
                    v-model="clinicSearchForm.deptId"
                    clearable
                    filterable
                    placeholder="门诊科室"
                    style="width: 180px"
                    @change="handleClinicDeptChange"
                >
                  <el-option v-for="d in departmentOptions" :key="d.id" :label="d.deptName" :value="d.id"/>
                </el-select>
              </el-form-item>
              <el-form-item label="医生">
                <el-select
                    v-model="clinicSearchForm.doctorId"
                    :disabled="!clinicSearchForm.deptId"
                    clearable
                    filterable
                    placeholder="先选门诊科室"
                    style="width: 180px"
                >
                  <el-option v-for="e in doctorOptions" :key="e.id" :label="e.empName" :value="e.id"/>
                </el-select>
              </el-form-item>
              <el-form-item label="排班日期">
                <el-date-picker
                    v-model="clinicSearchForm.dateRange"
                    end-placeholder="结束日期"
                    range-separator="至"
                    start-placeholder="开始日期"
                    style="width: 260px"
                    type="daterange"
                    value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item>
                <el-button :icon="Search" data-testid="clinic-approve-search" type="primary"
                           @click="handleClinicSearch">
                  搜索
                </el-button>
                <el-button :icon="Refresh" @click="handleClinicReset">重置</el-button>
              </el-form-item>
            </el-form>
            <!-- 主操作区：批量审核/驳回/刷新靠右，与查询条件视觉分离 -->
            <div class="flex shrink-0 items-start gap-3">
              <el-button v-perm="'outpatient:schedule:audit'" :icon="CircleCheck" :loading="clinicBatchLoading"
                         data-testid="clinic-approve-batch-audit" type="primary" @click="handleClinicBatchAudit">
                批量审核发布
              </el-button>
              <el-button v-perm="'outpatient:schedule:audit'" :icon="CloseBold" :loading="clinicBatchLoading"
                         data-testid="clinic-approve-batch-reject" @click="handleClinicBatchReject">
                批量驳回
              </el-button>
              <el-button :icon="Refresh" @click="loadClinicData">刷新</el-button>
            </div>
          </div>
        </el-card>

        <el-card class="table-card" shadow="never">
          <div data-testid="clinic-approve-table">
            <el-table v-loading="clinicLoading" :data="clinicTableData" :max-height="tableMaxHeight" stripe
                      @selection-change="handleClinicSelectionChange">
              <el-table-column type="selection" width="45"/>
              <el-table-column label="日期" prop="scheduleDate" width="115"/>
              <el-table-column label="科室" min-width="120" prop="deptName"/>
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
                  <el-button v-perm="'outpatient:schedule:audit'" :icon="CircleCheck" data-testid="clinic-approve-audit"
                             link type="success" @click="handleClinicAudit(row)">
                    审核发布
                  </el-button>
                  <el-button v-perm="'outpatient:schedule:audit'" :icon="CloseBold" data-testid="clinic-approve-reject"
                             link
                             type="danger" @click="handleClinicReject(row)">
                    驳回
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <el-empty v-if="!clinicLoading && clinicTableData.length === 0" :image-size="60"
                    description="没有待你审核的门诊排班"/>
          <div ref="footerRef" class="list-footer flex items-center justify-end">
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
  </div>
</template>

<script setup>
/**
 * 门诊排班审批中心（待我审批）。
 */
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {CircleCheck, CloseBold, Refresh, Search} from '@element-plus/icons-vue'
import {
  scheduleAuditPublish as auditPublishClinicSchedule,
  schedulePendingApproval as getClinicSchedulePendingApproval,
  scheduleReject as rejectClinicSchedule,
} from '@/api/appoint'
import {getDepartmentSelectList, getEmployeeList} from '@/api/system'
import {STAFF_TYPE_DOCTOR} from '@/lib/scheduleShift'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'

const activeTab = ref('clinic')
const clinicLoading = ref(false)
const clinicBatchLoading = ref(false)
const clinicTableData = ref([])
const clinicSelectedRows = ref([])
const clinicPagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
// 查询卡筛选条件：门诊科室 / 医生（下拉联动）、排班日期区间
const clinicSearchForm = reactive({
  deptId: null,
  doctorId: null,
  dateRange: [],
})
// 科室下拉（仅门诊科室 deptType=1）；医生下拉随所选科室联动
const departmentOptions = ref([])
const doctorOptions = ref([])

const loadDepartmentOptions = async () => {
  try {
    const res = await getDepartmentSelectList({deptType: 1})
    departmentOptions.value = res.data || []
  } catch (e) /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
}
const loadDoctorOptions = async (deptId) => {
  doctorOptions.value = []
  if (!deptId) return
  try {
    const res = await getEmployeeList({deptId, staffType: STAFF_TYPE_DOCTOR})
    doctorOptions.value = res.data?.records || res.data || []
  } catch (e) /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
}
// 切换科室：清空已选医生并重新加载该科室医生下拉
const handleClinicDeptChange = (deptId) => {
  clinicSearchForm.doctorId = null
  loadDoctorOptions(deptId)
}
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

// ---------------- 门诊排班：待审取数 / 审核发布 / 驳回 ----------------
const loadClinicData = async () => {
  clinicLoading.value = true
  try {
    const res = await getClinicSchedulePendingApproval({
      deptId: clinicSearchForm.deptId || undefined,
      doctorId: clinicSearchForm.doctorId || undefined,
      scheduleDateStart: clinicSearchForm.dateRange?.[0] || undefined,
      scheduleDateEnd: clinicSearchForm.dateRange?.[1] || undefined,
      pageNum: clinicPagination.value.pageNum,
      pageSize: clinicPagination.value.pageSize,
    })
    if (res.code === 200) {
      clinicTableData.value = res.data.records || []
      clinicPagination.value.total = res.data.total || 0
    }
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  } finally {
    clinicLoading.value = false
  }
}
const handleClinicSearch = () => {
  clinicPagination.value.pageNum = 1
  loadClinicData()
}
const handleClinicReset = () => {
  clinicSearchForm.deptId = null
  clinicSearchForm.doctorId = null
  clinicSearchForm.dateRange = []
  doctorOptions.value = []
  handleClinicSearch()
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
    const res = await auditPublishClinicSchedule([row.id])
    if (res.code === 200) {
      ElMessage.success('已审核发布')
      loadClinicData()
    }
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

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
  } catch (e)  /* 取消或错误：取消无提示，错误已由全局拦截器统一提示 */ {

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
  try {
    const ids = rows.map(r => r.id)
    await auditPublishClinicSchedule(ids)
    ElMessage.success(`已审核发布 ${rows.length} 条`)
    loadClinicData()
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  } finally {
    clinicBatchLoading.value = false
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

onMounted(() => {
  loadDepartmentOptions()
  loadClinicData()
})
</script>

<style scoped>
/* 只有一个 tab 时隐藏 tab 头（门诊办进去就是纯门诊排班待审列表，不显示 tab 栏） */
.single-tab :deep(.el-tabs__header) {
  display: none;
}
</style>
