<template>
  <div data-testid="schedule-approve-view">
    <el-tabs v-model="activeTab" class="single-tab">
      <el-tab-pane label="门诊排班" name="clinic">
        <div class="mb-3 flex flex-wrap items-center gap-2">
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
                  <el-button v-perm="'outpatient:schedule:audit'" :icon="CircleCheck" data-testid="clinic-approve-audit"
                             link type="success" @click="handleClinicAudit(row)">
                    审核发布
                  </el-button>
                  <el-button v-perm="'outpatient:schedule:audit'" :icon="CloseBold" data-testid="clinic-approve-reject" link
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
  </div>
</template>

<script setup>
/**
 * 门诊排班审批中心（待我审批）。
 */
import {onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {CircleCheck, CloseBold, Refresh} from '@element-plus/icons-vue'
import {
  scheduleAuditPublish as auditPublishClinicSchedule,
  schedulePendingApproval as getClinicSchedulePendingApproval,
  scheduleReject as rejectClinicSchedule,
} from '@/api/appoint'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'

const activeTab = ref('clinic')
const clinicLoading = ref(false)
const clinicBatchLoading = ref(false)
const clinicTableData = ref([])
const clinicSelectedRows = ref([])
const clinicPagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
const {tableMaxHeight} = useTableMaxHeight()

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
    const res = await auditPublishClinicSchedule([row.id])
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
  try {
    const ids = rows.map(r => r.id)
    await auditPublishClinicSchedule(ids)
    ElMessage.success(`已审核发布 ${rows.length} 条`)
    loadClinicData()
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{

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
  loadClinicData()
})
</script>

<style scoped>
/* 只有一个 tab 时隐藏 tab 头（门诊办进去就是纯门诊排班待审列表，不显示 tab 栏） */
.single-tab :deep(.el-tabs__header) {
  display: none;
}
</style>
