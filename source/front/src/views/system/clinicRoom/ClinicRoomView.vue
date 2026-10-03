<script setup lang="js">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import { getClinicRoomList, getClinicRoomDetail, createClinicRoom, updateClinicRoom, deleteClinicRoom, getDepartmentTree } from '@/api/system'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

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

// 新增/编辑对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增诊室')
const submitLoading = ref(false)
const formRef = ref(null)
const formData = reactive({
  id: null,
  name: '',
  code: '',
  location: '',
  deptId: null,
  status: 1,
  remark: '',
})

const rules = {
  name: [
    { required: true, message: '请输入诊室名称', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入诊室编号', trigger: 'blur' },
  ],
  location: [
    { required: true, message: '请输入地理位置', trigger: 'blur' },
  ],
  deptId: [
    { required: true, message: '请选择所属科室', trigger: 'change' },
  ],
}

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
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
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

const resetForm = () => {
  formData.id = null
  formData.name = ''
  formData.code = ''
  formData.location = ''
  formData.deptId = null
  formData.status = 1
  formData.remark = ''
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增诊室'
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  resetForm()
  dialogTitle.value = '编辑诊室'
  try {
    const res = await getClinicRoomDetail(row.id)
    if (res.code === 200) {
      Object.assign(formData, res.data)
    }
  } catch (error) {
    ElMessage.error(error.message || '获取诊室信息失败')
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
    let res
    if (formData.id) {
      res = await updateClinicRoom(formData)
    } else {
      res = await createClinicRoom(formData)
    }

    if (res.code === 200) {
      ElMessage.success(formData.id ? '修改成功' : '新增成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
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

<template>
  <div>
    <!-- 两卡式列表页：查询卡与表格卡分隔（口径参照 views/system/user/UserView.vue） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="诊室名称">
            <el-input
              v-model="searchForm.name"
              placeholder="请输入诊室名称"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="所属科室">
            <el-tree-select
              v-model="searchForm.deptId"
              :data="deptList"
              :props="{ label: 'deptName', value: 'id', children: 'children' }"
              placeholder="请选择科室"
              check-strictly
              clearable
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'org:clinicRoom:add'" type="primary" :icon="Plus" @click="handleAdd">新增诊室</el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" stripe :max-height="tableMaxHeight">
        <el-table-column prop="code" label="诊室编号" width="100" />
        <el-table-column prop="name" label="诊室名称" min-width="150" />
        <el-table-column prop="location" label="地理位置" min-width="180" />
        <el-table-column prop="deptId" label="所属科室" width="150">
          <template #default="{ row }">
            {{ getDeptName(row.deptId) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button v-perm="'org:clinicRoom:delete'" type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
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
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
      @close="dialogVisible = false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="诊室编号" prop="code">
          <el-input v-model="formData.code" placeholder="请输入诊室编号" />
        </el-form-item>
        <el-form-item label="诊室名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入诊室名称" />
        </el-form-item>
        <el-form-item label="地理位置" prop="location">
          <el-input v-model="formData.location" placeholder="如：门诊楼A座3层" />
        </el-form-item>
        <el-form-item label="所属科室" prop="deptId">
          <el-tree-select
            v-model="formData.deptId"
            :data="deptList"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            placeholder="请选择科室"
            check-strictly
            clearable
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="如：靠窗、配备B超机等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-perm="'org:clinicRoom:add'" type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
