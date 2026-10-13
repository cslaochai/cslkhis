<template>
  <div>
    <!-- 两卡式列表页：查询卡与表格卡分隔（口径参照 views/system/user/UserView.vue） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="诊室名称">
            <el-input
                v-model="searchForm.name"
                clearable
                placeholder="请输入诊室名称"
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="所属科室">
            <el-tree-select
                v-model="searchForm.deptId"
                :data="deptList"
                :props="{ label: 'deptName', value: 'id', children: 'children' }"
                check-strictly
                clearable
                placeholder="请选择科室"
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
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'org:clinicRoom:add'" :icon="Plus" type="primary" @click="handleAdd">新增诊室</el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="tableData" :max-height="tableMaxHeight" stripe>
        <el-table-column label="诊室编号" prop="code" width="100"/>
        <el-table-column label="诊室名称" min-width="150" prop="name"/>
        <el-table-column label="地理位置" min-width="180" prop="location"/>
        <el-table-column label="所属科室" prop="deptId" width="150">
          <template #default="{ row }">
            {{ getDeptName(row.deptId) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="150" prop="remark"/>
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="{ row }">
            <el-button :icon="Edit" link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-perm="'org:clinicRoom:delete'" :icon="Delete" link type="danger" @click="handleDelete(row)">
              删除
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
    <ClinicRoomEdit v-model="dialogVisible" :dept-list="deptList" :row="dialogRow" @saved="loadData"/>
  </div>
</template>

<script lang="js" setup>
import {onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue'
import {deleteClinicRoom, getClinicRoomList, getDepartmentTree} from '@/api/system'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import ClinicRoomEdit from '@/views/system/clinicRoom/ClinicRoomEdit.vue'

const loading = ref(false)
const searchForm = ref({
  name: '',
  deptId: null,
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

// 部门列表
const deptList = ref([])

// 新增/编辑：row 为空走新增，有值走修改（表单逻辑全在 ClinicRoomEdit 内）
const dialogVisible = ref(false)
const dialogRow = ref(null)

onMounted(() => {
  loadData()
  loadDeptList()
})

const loadDeptList = async () => {
  try {
    const res = await getDepartmentTree()
    if (res.code === 200) {
      deptList.value = res.data || []
    }
  } catch (error) {
    console.error('加载部门列表失败', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getClinicRoomList({
      ...searchForm.value,
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
  searchForm.value.name = ''
  searchForm.value.deptId = null
  searchForm.value.status = null
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
    await ElMessageBox.confirm(`确定要删除诊室 "${row.name}" 吗？`, '删除确认', {
      type: 'warning',
    })
    const res = await deleteClinicRoom(row.id)
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

// 根据科室ID获取科室名称
const getDeptName = (deptId) => {
  const findDept = (list) => {
    for (const dept of list) {
      if (dept.id === deptId) {
        return dept.deptName
      }
      if (dept.children && dept.children.length > 0) {
        const result = findDept(dept.children)
        if (result) return result
      }
    }
    return null
  }
  return findDept(deptList.value) || '未知科室'
}
</script>
