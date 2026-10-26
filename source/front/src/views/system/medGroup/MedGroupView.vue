<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="医疗组名称">
            <el-input
                v-model="searchForm.groupName"
                clearable
                data-testid="medgroup-search-name"
                placeholder="请输入医疗组名称"
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="所属科室">
            <el-select v-model="searchForm.deptId" clearable filterable placeholder="请选择科室">
              <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id"/>
            </el-select>
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
          <el-button v-perm="'org:medGroup:add'" :icon="Plus" data-testid="medgroup-add" type="primary"
                     @click="handleAdd">新增医疗组
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="tableData" :max-height="tableMaxHeight" stripe>
        <el-table-column label="医疗组编码" prop="groupCode" width="120"/>
        <el-table-column label="医疗组名称" min-width="170" prop="groupName"/>
        <el-table-column label="所属科室" min-width="130">
          <template #default="{ row }">{{ row.deptName || '—' }}</template>
        </el-table-column>
        <el-table-column label="主诊医师" min-width="120">
          <template #default="{ row }">
            <span v-if="row.leaderName">{{ row.leaderName }}</span>
            <span v-else class="text-slate-400">未指定</span>
          </template>
        </el-table-column>
        <el-table-column label="组内医师" width="100">
          <template #default="{ row }">
            <el-tag effect="plain" size="small" type="info">{{ row.memberCount ?? 0 }} 人</el-tag>
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
            <el-button v-perm="'org:medGroup:delete'" :icon="Delete" link type="danger"
                       @click="handleDelete(row)">删除
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

    <MedGroupEdit v-model="dialogVisible" :dept-list="deptList" :row="dialogRow" @saved="loadData"/>
  </div>
</template>

<script lang="js" setup>
import {computed, onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue'
import {deleteMedGroup, getDepartmentTree, getMedGroupListPage} from '@/api/system'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import MedGroupEdit from '@/views/system/medGroup/MedGroupEdit.vue'

const loading = ref(false)
const searchForm = ref({
  groupName: '',
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

const deptList = ref([])
const dialogVisible = ref(false)
const dialogRow = ref(null)

const deptOptions = computed(() => {
  const out = []
  const walk = (list) => {
    for (const d of list || []) {
      out.push({id: d.id, deptName: d.deptName})
      if (d.children && d.children.length) walk(d.children)
    }
  }
  walk(deptList.value)
  return out
})

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
    console.error('加载科室列表失败', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMedGroupListPage({
      groupName: searchForm.value.groupName || undefined,
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
  searchForm.value.groupName = ''
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
        `确定要删除医疗组「${row.groupName}」吗？组内还有医师时不能删除。`,
        '删除确认',
        {type: 'warning'},
    )
    const res = await deleteMedGroup(row.id)
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
