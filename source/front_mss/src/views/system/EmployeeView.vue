<template>
  <div>
    <!-- 两卡式列表页：查询卡与表格卡分隔（口径参照 views/system/user/UserView.vue） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="员工姓名">
            <el-input
                v-model="searchForm.empName"
                clearable
                placeholder="请输入员工姓名"
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
          <el-button v-perm="'org:employee:add'" :icon="Plus" type="primary" @click="handleAdd">新增员工</el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="tableData" :max-height="tableMaxHeight" stripe>
        <el-table-column label="工号" prop="empCode" width="100"/>
        <el-table-column label="员工姓名" prop="empName" width="120"/>
        <!-- 列表接口已脱敏（前 4 后 4，中间打星），页面直接渲染，不要再自己打码一次 -->
        <el-table-column label="身份证号" prop="idCard" width="180">
          <template #default="{ row }">
            <span :class="row.idCard ? '' : 'text-slate-300'">{{ row.idCard || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="主科室" min-width="120" prop="deptName"/>
        <el-table-column label="员工类型" prop="empType" width="100">
          <template #default="{ row }">
            {{ getDictLabelByValue(empTypeOptions, row.empType) }}
          </template>
        </el-table-column>
        <el-table-column label="执业科室" min-width="500" prop="deptNames">
          <template #default="{ row }">
            {{ (row.deptNames && row.deptNames.length > 0) ? row.deptNames.join('、') : row.deptName }}
          </template>
        </el-table-column>
        <el-table-column label="职位" prop="position" width="120">
          <template #default="{ row }">
            {{ getDictLabelByValue(positionOptions, row.position) }}
          </template>
        </el-table-column>
        <el-table-column label="职称" prop="title" width="120">
          <template #default="{ row }">
            {{ getDictLabelByValue(titleOptions, row.title) }}
          </template>
        </el-table-column>
        <el-table-column label="学历" prop="education" width="80">
          <template #default="{ row }">
            {{ getDictLabelByValue(educationOptions, row.education) }}
          </template>
        </el-table-column>
        <el-table-column label="联系电话" prop="phone" width="140"/>
        <el-table-column label="邮箱" min-width="100" prop="email"/>
        <el-table-column label="入职日期" prop="hireDate" width="120"/>
        <el-table-column label="状态" prop="status" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="{ row }">
            <el-button :icon="Edit" link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-perm="'org:employee:delete'" :icon="Delete" link type="danger" @click="handleDelete(row)">
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

    <!-- 新增/编辑对话框（表单、岗位表、资格证书表的整体逻辑都在 EmployeeEdit.vue，页面只控显隐与传行） -->
    <EmployeeEdit
        v-model="dialogVisible"
        :education-options="educationOptions"
        :emp-type-options="empTypeOptions"
        :position-options="positionOptions"
        :row="dialogRow"
        :title-options="titleOptions"
        @saved="loadData"
    />
  </div>
</template>

<script lang="js" setup>
import {onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import EmployeeEdit from './EmployeeEdit.vue'
import {getDictLabelByValue} from '@/lib/employeeDict'
import {deleteEmployee, getEmployeeListPage} from '@/api/system'
import {DICT_TYPE, loadDictDataMap} from '@/lib/dict-cache'

const loading = ref(false)
const searchForm = ref({
  empName: '',
})
const tableData = ref([])
const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

// 字典数据（列表列要翻译这些码值，由页面加载一次，编辑表单用 prop 接过去，不各拉一遍）
const empTypeOptions = ref([])  // 员工类型
const titleOptions = ref([])  // 职称
const positionOptions = ref([])  // 职位
const educationOptions = ref([]) // 学历

// 新增/编辑弹框状态已整体搬进 EmployeeEdit.vue，这里只控显隐与传行
const dialogVisible = ref(false)
const dialogRow = ref(null)

onMounted(() => {
  loadData()
  loadDictData()
})

const loadDictData = async () => {
  try {
    // 职称/职位的候选值全部来自后端 sys_dict_data，页面只提交 dictValue（编码），
    // 中文文案一个字都不写死在前端 —— 字典改了下拉立刻跟着变，不用发版。
    // 存量中文已由 sql/174 全量迁成码值，所以「存进去的」和「选出来的」是同一套值。
    const map = await loadDictDataMap([
      'mss_emp_type', DICT_TYPE.SYS_HOSPITAL_TITLE, DICT_TYPE.HOSPITAL_POSITION, 'his_education',
    ].join(','))
    empTypeOptions.value = map['mss_emp_type'] || []
    titleOptions.value = map[DICT_TYPE.SYS_HOSPITAL_TITLE] || []
    positionOptions.value = map[DICT_TYPE.HOSPITAL_POSITION] || []
    educationOptions.value = map['his_education'] || []
  } catch (error) {
    console.error('加载字典数据失败', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getEmployeeListPage({
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
  searchForm.value.empName = ''
  loadData()
}

const handleAdd = () => {
  dialogRow.value = null
  dialogVisible.value = true
}

// 详情取数与回填都在 EmployeeEdit.vue（它按 row.id 自己拉详情）
const handleEdit = (row) => {
  dialogRow.value = row
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除员工 "${row.empName}" 吗？`, '删除确认', {
      type: 'warning',
    })
    const res = await deleteEmployee(row.id)
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
</script>
