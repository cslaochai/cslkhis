<template>
  <div>
    <!-- 两卡式列表页：查询卡与表格卡分隔开；页面自身不再加内边距（直接用外层布局的 24px），
         面板高随内容自适应；表格限最大高、超高内部滚动，分页在流内紧跟表格底 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="用户名">
            <el-input
                v-model="searchForm.userName"
                clearable
                placeholder="请输入用户名"
                style="width: 180px"
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" clearable placeholder="全部" style="width: 150px">
              <el-option :value="1" label="启用"/>
              <el-option :value="0" label="禁用"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'system:user:add'" :icon="Plus" type="primary" @click="handleAdd">新增用户</el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格卡：数据列 min-width 摊满卡片宽度；body 置 0 内边距让表格全幅贴边 -->
    <el-card class="table-card" shadow="never">
      <el-table
          v-loading="loading"
          :data="tableData"
          :max-height="tableMaxHeight"
          stripe
      >
        <el-table-column label="用户名" min-width="120" prop="userName"/>
        <el-table-column label="姓名" min-width="100" prop="realName"/>
        <el-table-column label="用户类型" min-width="110" prop="userType">
          <template #default="{ row }">
            <el-tag
                :type="row.userType === 1 ? 'primary' : row.userType === 2 ? 'success' : row.userType === 3 ? 'warning' : 'info'">
              {{
                row.userType === 1 ? '院内用户' : row.userType === 2 ? '院外用户' : row.userType === 3 ? '患者' : '其他'
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后登录时间" min-width="180" prop="lastLoginTime">
          <template #default="{ row }">
            {{ row.lastLoginTime }}
          </template>
        </el-table-column>
        <el-table-column label="最后登录IP" min-width="130" prop="lastLoginIp">
          <template #default="{ row }">
            {{ row.lastLoginIp }}
          </template>
        </el-table-column>
        <el-table-column align="center" label="登录次数" min-width="90" prop="loginCount">
          <template #default="{ row }">
            {{ row.loginCount }}
          </template>
        </el-table-column>
        <el-table-column label="密码更新时间" min-width="180" prop="passwordUpdateTime">
          <template #default="{ row }">
            {{ row.passwordUpdateTime }}
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="80" prop="status">
          <template #default="{ row }">
            <el-tag :type="row.status == 1 ? 'success' : 'danger'" size="small">
              {{ getDictLabel(USER_STATUS, row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="180" prop="createTime"/>
        <el-table-column fixed="right" label="操作" width="340">
          <template #default="{ row }">
            <el-button :icon="View" link type="primary" @click.stop="openUserDialog(row, true)">详情</el-button>
            <el-button v-if="canEdit(row)" v-perm="'system:user:edit'" :icon="Edit" link type="primary"
                       @click.stop="handleEdit(row)">编辑
            </el-button>
            <el-button v-if="canManage(row)" v-perm="'system:user:edit'" link type="warning"
                       @click.stop="handleResetPassword(row)">重置密码
            </el-button>
            <el-button v-if="row.userName !== 'admin' && canManage(row)" v-perm="'system:user:delete'" :icon="Delete"
                       link type="danger" @click.stop="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页：在流内紧跟表格底，表格多高它就贴在哪，不钉面板底 -->
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

    <!-- 新增/编辑/详情走同一个表单组件（患者·其他账号与 admin 只读） -->
    <UserEdit v-model="dialogVisible" :row="dialogRow" :view-only="dialogViewOnly" @saved="loadData"/>
  </div>
</template>

<script lang="js" setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search, View} from '@element-plus/icons-vue'
import {deleteUser, getUserList, resetPassword} from '@/api/system'
import {getDictLabel, USER_STATUS} from '@/lib/dict'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import UserEdit from './UserEdit.vue'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'

const loading = ref(false)
const searchForm = reactive({
  userName: '',
  status: '',
})
const tableData = ref([])
const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
})

// 表单弹框（新增/编辑/详情）状态已整体搬进 UserEdit.vue，这里只控显隐与传行
const dialogVisible = ref(false)
const dialogRow = ref(null)
// 操作列「详情」打开：患者/其他账号不可编辑，弹窗只读
const dialogViewOnly = ref(false)

// 列表页布局（两卡式）：查询卡与表格卡分隔；面板高随内容自适应，不锁死。
// 表格高度只设最大值：不足时表格随内容收缩，超过最大值表格内部滚动（高度计算在 useTableMaxHeight）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

// 患者(3)/其他(4) 账号由自助注册等流程维护，用户管理页不开编辑/改状态/重置密码/删除（后端同步拦截）
const canManage = (row) => row.userType !== 3 && row.userType !== 4

// admin 账号只允许查看详情，编辑入口隐藏（删除本来就有同款拦截）
const canEdit = (row) => canManage(row) && row.userName !== 'admin'

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      ...searchForm,
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
  searchForm.userName = ''
  searchForm.status = ''
  handleSearch()
}

const handleAdd = () => {
  dialogRow.value = null
  dialogViewOnly.value = false
  dialogVisible.value = true
}

// 「详情」= 只看；改资料必须走「编辑」按钮
const handleEdit = (row) => openUserDialog(row, false)

const openUserDialog = (row, asView) => {
  dialogRow.value = row
  dialogViewOnly.value = asView || !canManage(row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  if (row.userName === 'admin') {
    ElMessage.warning('管理员不允许删除')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除用户 "${row.userName}" 吗？`, '删除确认', {
      type: 'warning',
    })
    const res = await deleteUser(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } 
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
}

const handleResetPassword = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要重置用户 "${row.userName}" 的密码吗？`, '重置密码', {
      type: 'warning',
    })
    const res = await resetPassword(row.id)
    if (res.code === 200) {
      ElMessage.success('密码已重置')
    }
  } catch {
    // 用户取消
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
