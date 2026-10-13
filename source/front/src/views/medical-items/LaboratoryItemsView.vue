<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline @submit.prevent>
          <el-form-item label="项目名称">
            <el-input v-model="searchForm.keyword" clearable placeholder="搜索项目名称/编码"
                      @keyup.enter="handleSearch"/>
          </el-form-item>
          <el-form-item label="项目类型">
            <el-select v-model="searchForm.itemType" clearable placeholder="全部">
              <el-option v-for="(label, val) in typeMap" :key="val" :label="label" :value="Number(val)"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button :icon="Plus" type="primary" @click="handleAdd">新增</el-button>
        </div>
      </div>
    </el-card>
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="tableData" :max-height="tableMaxHeight" stripe>
        <el-table-column label="编码" prop="itemCode" width="120"/>
        <el-table-column label="项目名称" min-width="150" prop="itemName"/>
        <el-table-column label="类型" prop="itemType" width="100">
          <template #default="{row}">{{ typeMap[row.itemType] || '其他' }}</template>
        </el-table-column>
        <el-table-column label="标本类型" prop="specimenType" width="90">
          <template #default="{row}">{{ row.specimenType || '-' }}</template>
        </el-table-column>
        <el-table-column align="right" label="价格" prop="price" width="90">
          <template #default="{row}">¥{{ row.price || 0 }}</template>
        </el-table-column>
        <el-table-column align="center" label="时长(分)" prop="duration" width="80"/>
        <el-table-column align="center" label="空腹" prop="isFasting" width="60">
          <template #default="{row}">
            <el-tag :type="row.isFasting === 1 ? 'warning' : 'info'" size="small">{{
                row.isFasting === 1 ? '是' : '否'
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="状态" prop="status" width="70">
          <template #default="{row}">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{
                row.status === 1 ? '启用' : '停用'
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="250">
          <template #default="{row}">
            <el-button :icon="Edit" link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button :icon="Setting" link type="success" @click="handleConfigDetail(row)">明细</el-button>
            <el-button :icon="Delete" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize"
                       :page-sizes="PAGE_SIZES" :total="pagination.total" layout="total, sizes, prev, pager, next"
                       @size-change="loadData" @current-change="loadData"/>
      </div>
    </el-card>

    <LaboratoryItemsEdit v-model="showDialog" :row="currentRow" :type-map="typeMap" @saved="loadData"/>

    <LaboratoryItemsDetailConfig v-model="showDetailDialog" :row="currentLaboratoryItem"/>
  </div>
</template>

<script lang="js" setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search, Setting} from '@element-plus/icons-vue'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {deleteLaboratoryItem, getLaboratoryItemList} from '@/api/system'
import LaboratoryItemsDetailConfig from './LaboratoryItemsDetailConfig.vue'
import LaboratoryItemsEdit from './LaboratoryItemsEdit.vue'

const loading = ref(false)
const tableData = ref([])
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()
const searchForm = reactive({keyword: '', itemType: null})

const typeMap = {1: '临床血液', 2: '临床体液', 3: '临床生化', 4: '临床免疫', 5: '微生物', 6: '其他'}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getLaboratoryItemList({
      ...searchForm,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize
    })
    if (res.code === 200) {
      tableData.value = res.data?.records || []
      pagination.value.total = res.data?.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.pageNum = 1;
  loadData()
}
const handleReset = () => {
  searchForm.keyword = '';
  searchForm.itemType = null;
  loadData()
}

const showDialog = ref(false)
const currentRow = ref(null)

const handleAdd = () => {
  currentRow.value = null
  showDialog.value = true
}

const handleEdit = (row) => {
  currentRow.value = row
  showDialog.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除检验项目「${row.itemName}」吗？`, '删除确认', {type: 'warning'})
    const res = await deleteLaboratoryItem(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功');
      loadData()
    } 
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
}

const showDetailDialog = ref(false)
const currentLaboratoryItem = ref(null)

const handleConfigDetail = (row) => {
  currentLaboratoryItem.value = row
  showDetailDialog.value = true
}

onMounted(() => loadData())
</script>
