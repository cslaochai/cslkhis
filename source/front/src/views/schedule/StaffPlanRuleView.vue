<template>
  <div data-testid="staff-plan-rule-view">
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="单元类型">
            <el-select v-model="searchForm.orgType" clearable placeholder="全部" style="width: 110px">
              <el-option v-for="o in ORG_UNIT_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="岗位类别">
            <el-select v-model="searchForm.staffType" clearable placeholder="全部" style="width: 130px">
              <el-option v-for="o in STAFF_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" clearable placeholder="全部" style="width: 100px">
              <el-option :value="1" label="启用"/>
              <el-option :value="0" label="停用"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start">
          <el-button v-perm="'staff:planRule:add'" :icon="Plus" data-testid="plan-rule-add" type="primary"
                     @click="handleAdd">
            新增标准
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div data-testid="plan-rule-table">
        <el-table v-loading="loading" :data="tableData" :max-height="tableMaxHeight" stripe>
          <el-table-column label="排班单元" min-width="170">
            <template #default="{ row }">{{ row.orgName || '—' }}（{{ row.orgTypeText }}）</template>
          </el-table-column>
          <el-table-column label="班次" min-width="140">
            <template #default="{ row }">
              <el-tag v-if="!row.shiftId || Number(row.shiftId) === 0" size="small" type="info">全部班次（合计）</el-tag>
              <span v-else>{{ row.shiftName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="岗位类别" prop="staffTypeName" width="110"/>
          <el-table-column label="最低在岗" prop="minStaff" width="100"/>
          <el-table-column label="最高在岗" width="100">
            <template #default="{ row }">{{ row.maxStaff || '不限' }}</template>
          </el-table-column>
          <el-table-column label="周工时上限" width="110">
            <template #default="{ row }">{{ row.maxWeekHours ?? '—' }}</template>
          </el-table-column>
          <el-table-column label="连夜上限" width="100">
            <template #default="{ row }">{{ row.maxConsecutiveNightDays ?? '—' }}</template>
          </el-table-column>
          <el-table-column label="连班上限" width="100">
            <template #default="{ row }">{{ row.maxConsecutiveWorkDays ?? '—' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="160" prop="remark" show-overflow-tooltip/>
          <el-table-column fixed="right" label="操作" width="160">
            <template #default="{ row }">
              <el-button v-perm="'staff:planRule:add'" :icon="Edit" link type="primary" @click="handleEdit(row)">修改
              </el-button>
              <el-button v-perm="'staff:planRule:delete'" :icon="Delete" link type="danger" @click="handleDelete(row)">
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

    <StaffPlanRuleEdit v-model="dialogVisible" :row="editRow" @saved="loadData"/>
  </div>
</template>

<script lang="js" setup>
import {onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue'
import {deleteStaffPlanRule, getStaffPlanRuleListPage} from '@/api/staffPlanRule'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {ORG_UNIT_TYPE_OPTIONS} from '@/lib/staffSchedule'
import {STAFF_TYPE_OPTIONS} from '@/lib/scheduleShift'
import StaffPlanRuleEdit from './StaffPlanRuleEdit.vue'

const loading = ref(false)
const tableData = ref([])
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const searchForm = ref({orgType: null, staffType: null, status: null})

const dialogVisible = ref(false)
const editRow = ref(null)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getStaffPlanRuleListPage({
      ...searchForm.value,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.value.total = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error(e.message || '加载人力标准失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.pageNum = 1;
  loadData()
}
const handleReset = () => {
  searchForm.value = {orgType: null, staffType: null, status: null}
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

const handleAdd = () => {
  editRow.value = null
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editRow.value = row
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
        `删掉「${row.orgName} · ${row.shiftName || '全部班次'} · ${row.staffTypeName}」这条标准后，` +
        '该单元这个班不再做人数校验，确定吗？',
        '删除人力标准', {type: 'warning'})
    const res = await deleteStaffPlanRule(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel' && e.message) ElMessage.error(e.message)
  }
}

onMounted(() => {
  loadData()
})
</script>
