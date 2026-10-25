<template>
  <div class="schedule-history-view" data-testid="schedule-history-view">
    <el-card class="query-card mb-3" shadow="never">
      <div class="query-grid-wrap">
        <el-form class="query-grid" label-width="80px" :model="searchForm">
          <el-form-item label="日期范围">
            <el-date-picker v-model="searchForm.dateRange" type="daterange"
                            value-format="YYYY-MM-DD" start-placeholder="开始" end-placeholder="结束"/>
          </el-form-item>
          <el-form-item label="人员姓名">
            <el-input v-model="searchForm.employeeName" clearable placeholder="护士/医生姓名"/>
          </el-form-item>
          <el-form-item label="操作类型">
            <el-select v-model="searchForm.changeType" clearable placeholder="全部">
              <el-option label="新建" value="CREATE"/>
              <el-option label="修改" value="UPDATE"/>
              <el-option label="删除" value="DELETE"/>
              <el-option label="提交" value="SUBMIT"/>
              <el-option label="发布" value="PUBLISH"/>
              <el-option label="撤回" value="RECALL"/>
              <el-option label="调班" value="ADJUST"/>
            </el-select>
          </el-form-item>
        </el-form>
        <div class="query-grid-ops">
          <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="tableData" stripe border>
        <el-table-column label="排班日期" prop="scheduleDate" width="110"/>
        <el-table-column label="人员" prop="employeeName" width="100"/>
        <el-table-column label="工号" prop="empCode" width="90"/>
        <el-table-column label="单元" prop="orgName" min-width="140" show-overflow-tooltip/>
        <el-table-column label="班次" prop="shiftName" min-width="120" show-overflow-tooltip/>
        <el-table-column label="操作类型" prop="changeTypeText" width="100"/>
        <el-table-column label="变更前" prop="fromText" min-width="150" show-overflow-tooltip/>
        <el-table-column label="变更后" prop="toText" min-width="150" show-overflow-tooltip/>
        <el-table-column label="操作人" prop="operatorName" width="100"/>
        <el-table-column label="操作时间" prop="createTime" width="160"/>
      </el-table>
      <el-empty v-if="!loading && tableData.length === 0" description="暂无排班历史记录"/>
      <div class="mt-3 flex justify-end">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {Refresh, Search} from '@element-plus/icons-vue'
import {getScheduleChangeLogPage} from '@/api/staffSchedule'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'

const loading = ref(false)
const tableData = ref([])
const searchForm = ref({dateRange: null, employeeName: '', changeType: null})
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      startDate: searchForm.value.dateRange?.[0],
      endDate: searchForm.value.dateRange?.[1],
      employeeName: searchForm.value.employeeName || undefined,
      changeType: searchForm.value.changeType || undefined,
    }
    const res = await getScheduleChangeLogPage(params)
    tableData.value = res?.data?.records || []
    pagination.value.total = res?.data?.total || 0
  } catch (e) {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.value = {dateRange: null, employeeName: '', changeType: null}
  handleSearch()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.schedule-history-view {
  padding: 16px;
}
</style>
