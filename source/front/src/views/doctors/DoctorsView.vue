<script setup lang="js">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, View, Switch, Edit } from '@element-plus/icons-vue'
import { getEmployeeList, getEmployeeDetail, updateEmployee, getDepartmentTree, getDictDataMapList } from '@/api/system'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'
import { patientGenderText } from '@/lib/patientGender'

const loading = ref(false)
const searchForm = ref({
  empName: '',
  deptId: null,
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

// 字典数据
const titleOptions = ref([])
const positionOptions = ref([])
const educationOptions = ref([])

// 详情对话框
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)

onMounted(() => {
  loadData()
  loadDeptList()
  loadDictData()
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

const loadDictData = async () => {
  try {
    const res = await getDictDataMapList('sys_hospital_title,hospital_position,his_education')
    if (res.code === 200 && res.data) {
      titleOptions.value = res.data['sys_hospital_title'] || []
      positionOptions.value = res.data['hospital_position'] || []
      educationOptions.value = res.data['his_education'] || []
    }
  } catch (error) {
    console.error('加载字典数据失败', error)
  }
}

const getDictLabelByValue = (options, value) => {
  if (!value) return '-'
  const item = options.find(item => item.dictValue === value)
  return item ? item.dictLabel : value
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getEmployeeList({
      ...searchForm.value,
      empType: 1,
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
  searchForm.value.empName = ''
  searchForm.value.deptId = null
  loadData()
}

const handleSizeChange = (val) => {
  pagination.value.pageSize = val
  loadData()
}

const handleCurrentChange = (val) => {
  pagination.value.pageNum = val
  loadData()
}

// 查看详情
const handleViewDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    const res = await getEmployeeDetail(row.id)
    if (res.code === 200 && res.data) {
      detailData.value = res.data
    }
  } catch (error) {
    ElMessage.error(error.message || '获取详情失败')
  } finally {
    detailLoading.value = false
  }
}

// 启用/禁用
const handleToggleStatus = async (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}医生 "${row.empName}" 吗？`, `${action}确认`, {
      type: 'warning',
    })
    const res = await updateEmployee({
      id: row.id,
      status: row.status === 1 ? '0' : 1,
    })
    if (res.code === 200) {
      ElMessage.success(`${action}成功`)
      loadData()
    } else {
      ElMessage.error(res.message || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel' && error.message) {
      ElMessage.error(error.message)
    }
  }
}

// 编辑对话框
const editVisible = ref(false)
const editLoading = ref(false)
const editForm = ref({
  id: null,
  empName: '',
  gender: 1,
  phone: '',
  deptName: '',
  title: '',
  position: '',
  isExpert: '0',
})

// 编辑
const handleEdit = async (row) => {
  editVisible.value = true
  editLoading.value = true
  try {
    const res = await getEmployeeDetail(row.id)
    if (res.code === 200 && res.data) {
      const data = res.data
      editForm.value = {
        id: data.id,
        empName: data.empName || '',
        gender: data.gender || 1,
        phone: data.phone || '',
        deptName: data.deptName || '',
        title: data.title || '',
        position: data.position || '',
        isExpert: data.isExpert || '0',
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '获取详情失败')
  } finally {
    editLoading.value = false
  }
}

// 提交编辑
const handleEditSubmit = async () => {
  if (!editForm.value.empName) {
    ElMessage.warning('请输入医生姓名')
    return
  }
  try {
    const res = await updateEmployee(editForm.value)
    if (res.code === 200) {
      ElMessage.success('编辑成功')
      editVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message || '编辑失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '编辑失败')
  }
}
</script>

<template>
  <div>
    <!-- 搜索区域（两卡式列表页，口径参照 views/system/user/UserView.vue） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="医生姓名">
          <el-input
            v-model="searchForm.empName"
            placeholder="请输入医生姓名"
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
            clearable
            check-strictly
            class="!w-48"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域：body 置 0 内边距让表格全幅贴边，max-height 限高、超高内部滚动 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" stripe :max-height="tableMaxHeight">
        <el-table-column prop="empCode" label="工号" width="200" />
        <el-table-column prop="empName" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="60">
          <template #default="{ row }">
            {{ patientGenderText(row.gender) }}
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="科室" min-width="120" />
        <el-table-column prop="title" label="职称" width="120">
          <template #default="{ row }">
            {{ getDictLabelByValue(titleOptions, row.title) }}
          </template>
        </el-table-column>
        <el-table-column prop="specialty" label="擅长" min-width="180">
          <template #default="{ row }">
            <span class="line-clamp-1">{{ row.specialty  }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="isExpert" label="专家号" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isExpert === 1 ? 'success' : 'info'" size="small">
              {{ row.isExpert === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '在岗' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="handleViewDetail(row)">详情</el-button>
            <el-button type="warning" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button
              :type="row.status === 1 ? 'danger' : 'success'"
              link
              :icon="Switch"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页：在流内紧跟表格底 -->
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

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="医生详情"
      width="700px"
      destroy-on-close
    >
      <div v-loading="detailLoading">
        <template v-if="detailData">
          <!-- 基本信息 -->
          <div class="mb-4 rounded-lg border border-slate-200 bg-slate-50 p-4">
            <div class="flex items-center gap-4">
              <div class="flex h-16 w-16 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-blue-500 to-blue-600 text-xl font-bold text-white">
                {{ detailData.empName?.charAt(0) }}
              </div>
              <div class="flex-1">
                <div class="flex items-center gap-3">
                  <h3 class="text-lg font-semibold text-slate-900">{{ detailData.empName }}</h3>
                  <el-tag :type="detailData.status === 1 ? 'success' : 'danger'" size="small">
                    {{ detailData.status === 1 ? '在岗' : '停用' }}
                  </el-tag>
                  <el-tag v-if="detailData.isExpert === 1" type="warning" size="small">专家</el-tag>
                </div>
                <p class="mt-1 text-sm text-slate-500">
                  {{ getDictLabelByValue(titleOptions, detailData.title) }} · {{ detailData.deptName  }}
                </p>
              </div>
            </div>
          </div>

          <!-- 详细信息 -->
          <div class="grid grid-cols-2 gap-4 py-2 text-sm">
            <div><span class="text-slate-400">工号：</span><span class="font-medium text-slate-700">{{ detailData.empCode  }}</span></div>
            <div><span class="text-slate-400">性别：</span><span class="font-medium text-slate-700">{{ patientGenderText(detailData.gender) }}</span></div>
            <div><span class="text-slate-400">联系电话：</span><span class="font-medium text-slate-700">{{ detailData.phone  }}</span></div>
            <div><span class="text-slate-400">邮箱：</span><span class="font-medium text-slate-700">{{ detailData.email  }}</span></div>
            <div><span class="text-slate-400">学历：</span><span class="font-medium text-slate-700">{{ getDictLabelByValue(educationOptions, detailData.education) }}</span></div>
            <div><span class="text-slate-400">入职日期：</span><span class="font-medium text-slate-700">{{ detailData.hireDate  }}</span></div>
            <div><span class="text-slate-400">职位：</span><span class="font-medium text-slate-700">{{ getDictLabelByValue(positionOptions, detailData.position) }}</span></div>
            <div><span class="text-slate-400">专家号费用：</span><span class="font-medium text-slate-700">¥{{ detailData.expertPrice || 0 }}</span></div>
            <div class="col-span-2"><span class="text-slate-400">执业科室：</span><span class="font-medium text-slate-700">{{ (detailData.deptNames && detailData.deptNames.length > 0) ? detailData.deptNames.join('、') : detailData.deptName }}</span></div>
            <div class="col-span-2"><span class="text-slate-400">擅长：</span><span class="font-medium text-slate-700">{{ detailData.specialty  }}</span></div>
          </div>
        </template>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="editVisible"
      title="编辑医生"
      width="500px"
      destroy-on-close
    >
      <el-form :model="editForm" label-width="100px" v-loading="editLoading">
        <el-form-item label="医生姓名" required>
          <el-input v-model="editForm.empName" placeholder="请输入医生姓名" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="editForm.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="editForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="主科室">
          <span class="text-sm font-medium text-slate-700">{{ editForm.deptName || '-' }}</span>
          <!--
            原先这里是科室树选（可改），但后端入参里从来没有 deptId 这个字段，
            选了也悄悄不生效。科室与角色统一在「系统管理 → 员工档案」的岗位表里配，
            这里只读展示，避免两处配同一份授权。
          -->
          <p class="mt-1 text-xs text-slate-400">执业科室与角色请在员工档案的「岗位」中配置</p>
        </el-form-item>
        <el-form-item label="职称">
          <el-select v-model="editForm.title" placeholder="请选择职称" clearable class="w-full">
            <el-option
              v-for="item in titleOptions"
              :key="item.dictValue"
              :label="item.dictLabel"
              :value="item.dictValue"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="职位">
          <el-select v-model="editForm.position" placeholder="请选择职位" clearable class="w-full">
            <el-option
              v-for="item in positionOptions"
              :key="item.dictValue"
              :label="item.dictLabel"
              :value="item.dictValue"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="专家号">
          <el-switch v-model="editForm.isExpert" active-value="1" inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
