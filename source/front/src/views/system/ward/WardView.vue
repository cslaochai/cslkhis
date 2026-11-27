<template>
  <div>
    <!-- 两卡式列表页：查询卡与表格卡分隔 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="病区名称">
            <el-input
                v-model="searchForm.wardName"
                clearable
                data-testid="ward-search-name"
                placeholder="请输入病区名称"
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="病区编码">
            <el-input
                v-model="searchForm.wardCode"
                clearable
                placeholder="请输入病区编码"
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="收治科室">
            <DeptTreeSelect
                v-model="searchForm.deptId"
                mode="LEAF"
                scope="ALL"
                data-testid="ward-search-dept"
                placeholder="任一收治科室"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" clearable placeholder="请选择状态">
              <el-option :value="1" label="启用"/>
              <el-option :value="0" label="停用"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'org:ward:add'" :icon="Plus" data-testid="ward-add" type="primary" @click="handleAdd">
            新增病区
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="tableData" :max-height="tableMaxHeight" stripe>
        <el-table-column label="病区编码" prop="wardCode" width="110"/>
        <el-table-column label="病区名称" min-width="170" prop="wardName"/>
        <el-table-column label="本位科室" min-width="130">
          <template #default="{ row }">
            <span class="text-slate-700">{{ row.deptName || '' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="收治科室" min-width="260">
          <template #default="{ row }">
            <div v-if="row.depts && row.depts.length" class="flex flex-wrap gap-1">
              <el-tag
                  v-for="d in row.depts"
                  :key="d.deptId"
                  :type="d.isPrimary === 1 ? 'primary' : 'info'"
                  effect="plain"
                  size="small"
              >
                {{ d.deptName }}<template v-if="d.bedQuota != null"> · {{ d.bedQuota }}床</template>
              </el-tag>
            </div>
            <span v-else class="text-slate-400">—</span>
          </template>
        </el-table-column>
        <el-table-column label="床位（占用/总）" width="130">
          <template #default="{ row }">
            <span :class="bedClass(row)">{{ row.occupiedBeds ?? 0 }} / {{ row.totalBeds ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="140" prop="remark"/>
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="{ row }">
            <el-button :icon="Edit" link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button
                v-perm="'org:ward:delete'"
                :icon="Delete"
                link
                type="danger"
                @click="handleDelete(row)"
            >删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <WardEdit v-model="dialogVisible" :row="dialogRow" @saved="loadData"/>
  </div>
</template>

<script lang="js" setup>
import {onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue'
import {deleteWard, getWardListPage} from '@/api/system'
import DeptTreeSelect from '@/components/his/DeptTreeSelect.vue'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import WardEdit from '@/views/system/ward/WardEdit.vue'

const loading = ref(false)
const searchForm = ref({
  wardName: '',
  wardCode: '',
  deptId: null,
  status: null,
})
const tableData = ref([])
const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
})

const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const dialogVisible = ref(false)
const dialogRow = ref(null)

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getWardListPage({
      wardName: searchForm.value.wardName || undefined,
      wardCode: searchForm.value.wardCode || undefined,
      deptId: searchForm.value.deptId || undefined,
      status: searchForm.value.status === null ? undefined : searchForm.value.status,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.value.total = res.data.total || 0
    }
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.value.wardName = ''
  searchForm.value.wardCode = ''
  searchForm.value.deptId = null
  searchForm.value.status = null
  pagination.value.pageNum = 1
  loadData()
}

const handleAdd = () => {
  dialogRow.value = null
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogRow.value = row
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确定要删除病区「${row.wardName}」吗？已开床位或有人员挂靠的病区不能删除，请改为停用。`,
        '删除确认',
        {type: 'warning'},
    )
    const res = await deleteWard(row.wardId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
  }
}

const handleSizeChange = (val) => {
  pagination.value.pageSize = val
  loadData()
}

const handleCurrentChange = (val) => {
  pagination.value.pageNum = val
  loadData()
}

const bedClass = (row) => {
  const total = row.totalBeds ?? 0
  const occupied = row.occupiedBeds ?? 0
  if (total > 0 && occupied >= total) return 'text-red-600 font-medium'
  return 'text-slate-700'
}
</script>
