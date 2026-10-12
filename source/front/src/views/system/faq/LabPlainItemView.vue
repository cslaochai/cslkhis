<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="项目名">
            <el-input
                v-model="searchForm.keyword"
                clearable
                placeholder="检验项目名或白话名"
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="分组">
            <el-select v-model="searchForm.groupName" clearable placeholder="全部分组" style="width: 140px">
              <el-option v-for="g in groupOptions" :key="g" :label="g" :value="g"/>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" clearable placeholder="全部" style="width: 110px">
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
          <el-button v-perm="'lab:plain:upsert'" :icon="Plus" type="primary" @click="handleAdd">新增词条</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="tableData" :max-height="tableMaxHeight" stripe>
        <el-table-column label="分组" prop="groupName" width="100"/>
        <el-table-column label="检验项目名" prop="itemName" show-overflow-tooltip width="140"/>
        <el-table-column label="白话名" prop="plainName" show-overflow-tooltip width="130"/>
        <el-table-column label="这项查什么" min-width="220" prop="whatIsIt" show-overflow-tooltip/>
        <el-table-column label="偏高说明" min-width="220" prop="highText" show-overflow-tooltip/>
        <el-table-column label="偏低说明" min-width="220" prop="lowText" show-overflow-tooltip/>
        <el-table-column label="排序" prop="sortOrder" width="80"/>
        <el-table-column label="状态" prop="status" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="{ row }">
            <el-button :icon="Edit" link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-perm="'lab:plain:delete'" :icon="Delete" link type="danger" @click="handleDelete(row)">删除
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

    <!-- 新增/编辑对话框 -->
    <LabPlainItemEdit v-model="dialogVisible" :row="dialogRow" @saved="handleEditSaved"/>
  </div>
</template>

<script lang="js" setup>
import {onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue'
import {getLabPlainGroupNames, getLabPlainList, labPlainDelete} from '@/api/labPlain'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import LabPlainItemEdit from '@/views/system/faq/LabPlainItemEdit.vue'

const loading = ref(false)
const searchForm = ref({
  keyword: '',
  groupName: '',
  status: null,
})
const tableData = ref([])
const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const groupOptions = ref([])

// 新增/编辑：row 为空走新增，有值走修改（表单逻辑全在 LabPlainItemEdit 内）
const dialogVisible = ref(false)
const dialogRow = ref(null)

onMounted(() => {
  loadGroupNames()
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getLabPlainList({
      ...searchForm.value,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.value.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadGroupNames = async () => {
  try {
    const res = await getLabPlainGroupNames()
    if (res.code === 200) {
      groupOptions.value = res.data || []
    }
  } catch (error) {
    // 下拉拉不到分组不影响增删改查，静默即可（仍能按项目名/状态筛）
    console.warn('分组清单加载失败', error)
  }
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.value.keyword = ''
  searchForm.value.groupName = ''
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

const handleEditSaved = () => {
  loadData()
  loadGroupNames()
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除词条「${row.itemName}」吗？删除后该项目的报告解读只剩数值，没有白话。`, '删除确认', {
      type: 'warning',
    })
    const res = await labPlainDelete(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
      loadGroupNames()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel' && error.message) {
      ElMessage.error(error.message)
    }
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
</script>
