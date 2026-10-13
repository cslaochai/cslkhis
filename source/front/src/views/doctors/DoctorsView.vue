<template>
  <div>
    <!-- 搜索区域（两卡式列表页，口径参照 views/system/user/UserView.vue） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="医生姓名">
          <el-input
              v-model="searchForm.empName"
              clearable
              placeholder="请输入医生姓名"
              @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="所属科室">
          <el-tree-select
              v-model="searchForm.deptId"
              :data="deptList"
              :props="{ label: 'deptName', value: 'id', children: 'children' }"
              check-strictly
              class="!w-48"
              clearable
              placeholder="请选择科室"
          />
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域：body 置 0 内边距让表格全幅贴边，max-height 限高、超高内部滚动 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="tableData" :max-height="tableMaxHeight" stripe>
        <el-table-column label="工号" prop="empCode" width="200"/>
        <el-table-column label="姓名" prop="empName" width="100"/>
        <el-table-column label="性别" prop="gender" width="60">
          <template #default="{ row }">
            {{ patientGenderText(row.gender) }}
          </template>
        </el-table-column>
        <el-table-column label="科室" min-width="120" prop="deptName"/>
        <el-table-column label="职称" prop="title" width="120">
          <template #default="{ row }">
            {{ getDictLabelByValue(titleOptions, row.title) }}
          </template>
        </el-table-column>
        <el-table-column label="擅长" min-width="180" prop="specialty">
          <template #default="{ row }">
            <span class="line-clamp-1">{{ row.specialty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="联系电话" prop="phone" width="140"/>
        <el-table-column align="center" label="专家号" prop="isExpert" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isExpert === 1 ? 'success' : 'info'" size="small">
              {{ row.isExpert === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="状态" prop="status" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '在岗' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="250">
          <template #default="{ row }">
            <el-button :icon="View" link type="primary" @click="handleViewDetail(row)">详情</el-button>
            <el-button :icon="Edit" link type="warning" @click="handleEdit(row)">编辑</el-button>
            <el-button
                :icon="Switch"
                :type="row.status === 1 ? 'danger' : 'success'"
                link
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

    <!-- 详情 / 编辑：拆到同目录兄弟组件，页面只管开关与递参 -->
    <DoctorsDetail v-model="detailVisible" :id="detailId"
                   :title-options="titleOptions" :education-options="educationOptions"
                   :position-options="positionOptions"/>
    <DoctorsEdit v-model="editVisible" :row="editRow" :title-options="titleOptions"
                 :position-options="positionOptions" @saved="loadData"/>
  </div>
</template>

<script lang="js" setup>
import {onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Edit, Refresh, Search, Switch, View} from '@element-plus/icons-vue'
import {getDepartmentTree, getDictDataMapList, getEmployeeList, updateEmployee} from '@/api/system'
import DoctorsDetail from './DoctorsDetail.vue'
import DoctorsEdit from './DoctorsEdit.vue'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {getDictLabelByValue} from '@/lib/doctors'
import {patientGenderText} from '@/lib/patientGender'

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

// 详情对话框（内容已拆至 DoctorsDetail.vue，页面只管开关与递参）
const detailVisible = ref(false)
const detailId = ref('')

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
const handleViewDetail = (row) => {
  detailId.value = row.id
  detailVisible.value = true
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
    } 
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
}

// 编辑对话框（表单已拆至 DoctorsEdit.vue，页面只管开关与递参）
const editVisible = ref(false)
const editRow = ref(null)

// 编辑
const handleEdit = (row) => {
  editRow.value = row
  editVisible.value = true
}
</script>
