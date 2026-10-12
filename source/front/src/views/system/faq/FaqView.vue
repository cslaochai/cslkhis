<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="问题">
            <el-input
                v-model="searchForm.keyword"
                clearable
                placeholder="问题或关键词"
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="分类">
            <el-select
                v-model="searchForm.categoryCode"
                clearable
                filterable
                placeholder="请选择分类"
                style="width: 200px"
            >
              <el-option
                  v-for="d in faqCategoryOptions"
                  :key="d.dictValue"
                  :label="`${d.dictLabel}（${d.dictValue}）`"
                  :value="d.dictValue"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'patient:faq:upsert'" :icon="Plus" type="primary" @click="handleAdd">新增问题</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="tableData" :max-height="tableMaxHeight" stripe>
        <el-table-column label="编号" prop="faqNo" width="160"/>
        <el-table-column label="分类" prop="categoryName" width="120">
          <template #default="{ row }">
            {{ row.categoryName }}<span class="text-gray-400 text-xs">（{{ row.categoryCode }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="问题" min-width="200" prop="question" show-overflow-tooltip/>
        <el-table-column label="答案" min-width="260" prop="answer" show-overflow-tooltip/>
        <el-table-column label="关键词" min-width="180" prop="keywords" show-overflow-tooltip/>
        <el-table-column label="反馈" width="150">
          <template #default="{ row }">
            <span class="text-xs text-gray-500">
              查看 {{ row.viewCount }} · 有用 {{ row.helpfulCount }} · 无用 {{ row.uselessCount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="热门" prop="hotFlag" width="80">
          <template #default="{ row }">
            <el-tag :type="row.hotFlag === 1 ? 'warning' : 'info'" size="small">
              {{ row.hotFlag === 1 ? '热门' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
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
            <el-button v-perm="'patient:faq:delete'" :icon="Delete" link type="danger" @click="handleDelete(row)">删除
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

    <FaqEdit v-model="dialogVisible" :row="dialogRow" :category-options="faqCategoryOptions" @saved="loadData"/>
  </div>
</template>

<script lang="js" setup>
import {onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue'
import {faqDelete, getFaqAdminList} from '@/api/patientFaq'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {DICT_TYPE, loadDictDataList} from '@/lib/dict-cache'
import FaqEdit from '@/views/system/faq/FaqEdit.vue'

const loading = ref(false)
const searchForm = ref({
  keyword: '',
  categoryCode: '',
})
// FAQ 分类字典下拉选项（his_faq_category），新增/编辑/搜索共用
const faqCategoryOptions = ref([])
const tableData = ref([])
const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const dialogVisible = ref(false)
const dialogRow = ref(null)

onMounted(() => {
  loadData()
  loadFaqCategories()
})

// 加载 FAQ 分类字典（his_faq_category）
const loadFaqCategories = async () => {
  faqCategoryOptions.value = await loadDictDataList(DICT_TYPE.FAQ_CATEGORY)
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getFaqAdminList({
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
  searchForm.value.keyword = ''
  searchForm.value.categoryCode = ''
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
    await ElMessageBox.confirm(`确定删除问题「${row.question}」吗？删除后患者端立即搜不到。`, '删除确认', {
      type: 'warning',
    })
    const res = await faqDelete(row.id)
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
</script>
