<template>
  <div>
    <!-- 两卡式列表页：查询卡与表格卡分隔（口径参照 views/system/user/UserView.vue） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="角色名称">
            <el-input
                v-model="searchForm.roleName"
                clearable
                placeholder="请输入角色名称"
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'system:role:add'" :icon="Plus" type="primary" @click="handleAdd">新增角色</el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="tableData" :max-height="tableMaxHeight" stripe>
        <el-table-column label="角色编码" prop="roleCode" width="120"/>
        <el-table-column label="角色名称" prop="roleName" width="150"/>
        <el-table-column label="角色类型" prop="roleType" width="120">
          <template #default="{ row }">
            <el-tag :type="row.roleType === 1 ? 'danger' : 'success'">
              {{ row.roleType === 1 ? '系统角色' : '自定义角色' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色描述" min-width="180" prop="remark"/>
        <el-table-column label="数据范围" prop="dataScope" width="150">
          <template #default="{ row }">
            <el-tag :type="row.dataScope === 1 ? 'warning' : 'info'">{{ dataScopeLabel(row.dataScope) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180"/>
        <el-table-column fixed="right" label="操作" width="260">
          <template #default="{ row }">
            <el-button :icon="Setting" link type="primary" @click="handleConfigMenu(row)">菜单权限</el-button>
            <el-button :icon="Edit" link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.roleType !== 1" v-perm="'system:role:delete'" :icon="Delete" link type="danger"
                       @click="handleDelete(row)">删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            :current-page="pagination.pageNum"
            :page-size="pagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="pagination.total"
            background
            layout="total, sizes, prev, pager, next, jumper"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <RoleEdit v-model="dialogVisible" :row="dialogRow" @saved="loadData"/>

    <!-- 菜单权限配置弹框 -->
    <RoleMenuEdit v-model="menuDialogVisible" :role="menuRole" @saved="loadData"/>
  </div>
</template>

<script lang="js" setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search, Setting} from '@element-plus/icons-vue'
import {deleteRole, getRoleListPage} from '@/api/system'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import RoleEdit from './RoleEdit.vue'
import RoleMenuEdit from './RoleMenuEdit.vue'

const loading = ref(false)
const searchForm = ref({
  roleName: '',
})
const tableData = ref([])
/** 分页：走后端 /system/role/listPage，前端不再全量拉取后自己切 */
const pagination = reactive({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

// 新增/编辑：row 为空走新增，有值走修改（表单逻辑全在 RoleEdit 内）
const dialogVisible = ref(false)
const dialogRow = ref(null)

// 菜单权限弹框：只把当前行交给 RoleMenuEdit
const menuDialogVisible = ref(false)
const menuRole = ref(null)

// 表格展示口径：只有 1 是全院
const dataScopeLabel = (v) => (v === 1 ? '全部数据（全院）' : '按岗位科室')

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRoleListPage({
      roleName: searchForm.value.roleName,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    })
    if (res.code === 200) {
      tableData.value = res.data?.records || res.data?.list || []
      pagination.total = Number(res.data?.total || 0)
      // 删掉本页最后一条后页码可能越界，回退到最后一页重查（避免停在空白页）
      const maxPage = Math.max(1, Math.ceil(pagination.total / pagination.pageSize))
      if (pagination.pageNum > maxPage) {
        pagination.pageNum = maxPage
        return loadData()
      }
    }
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.value.roleName = ''
  pagination.pageNum = 1
  loadData()
}

const handlePageChange = (page) => {
  pagination.pageNum = page
  loadData()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
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

const handleConfigMenu = (row) => {
  menuRole.value = row
  menuDialogVisible.value = true
}

const handleDelete = async (row) => {
  // 系统角色不允许删除
  if (row.roleType === 1) {
    ElMessage.warning('系统角色不允许删除')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除角色 "${row.roleName}" 吗？`, '删除确认', {
      type: 'warning',
    })
    const res = await deleteRole(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } 
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
}
</script>
