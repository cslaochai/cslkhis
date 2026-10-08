<script setup lang="js">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import { getFaqAdminList, getFaqAdminDetail, faqUpsert, faqDelete } from '@/api/patientFaq'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

const loading = ref(false)
const searchForm = ref({
  keyword: '',
  categoryCode: '',
})
const tableData = ref([])
const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

const dialogVisible = ref(false)
const dialogTitle = ref('新增常见问题')
const submitLoading = ref(false)
const formRef = ref(null)
const formData = reactive({
  id: null,
  categoryCode: '',
  categoryName: '',
  question: '',
  answer: '',
  keywords: '',
  hotFlag: 0,
  status: 1,
  sortOrder: 999,
})

const rules = {
  categoryCode: [{ required: true, message: '请输入分类编码', trigger: 'blur' }],
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  question: [{ required: true, message: '请输入问题', trigger: 'blur' }],
  answer: [{ required: true, message: '请输入答案', trigger: 'blur' }],
}

onMounted(() => {
  loadData()
})

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

const resetForm = () => {
  formData.id = null
  formData.categoryCode = ''
  formData.categoryName = ''
  formData.question = ''
  formData.answer = ''
  formData.keywords = ''
  formData.hotFlag = 0
  formData.status = 1
  formData.sortOrder = 999
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增常见问题'
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  resetForm()
  dialogTitle.value = '编辑常见问题'
  try {
    const res = await getFaqAdminDetail(row.id)
    if (res.code === 200) {
      Object.assign(formData, res.data)
    }
  } catch (error) {
    ElMessage.error(error.message || '获取详情失败')
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
    const res = await faqUpsert({ ...formData })
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

<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="问题">
            <el-input
              v-model="searchForm.keyword"
              placeholder="问题或关键词"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="分类编码">
            <el-input
              v-model="searchForm.categoryCode"
              placeholder="如 REPORT"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'patient:faq:upsert'" type="primary" :icon="Plus" @click="handleAdd">新增问题</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" stripe :max-height="tableMaxHeight">
        <el-table-column prop="faqNo" label="编号" width="160" />
        <el-table-column prop="categoryName" label="分类" width="110">
          <template #default="{ row }">
            {{ row.categoryName }}<span class="text-gray-400 text-xs">（{{ row.categoryCode }}）</span>
          </template>
        </el-table-column>
        <el-table-column prop="question" label="问题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="answer" label="答案" min-width="260" show-overflow-tooltip />
        <el-table-column prop="keywords" label="关键词" min-width="180" show-overflow-tooltip />
        <el-table-column label="反馈" width="150">
          <template #default="{ row }">
            <span class="text-xs text-gray-500">
              查看 {{ row.viewCount }} · 有用 {{ row.helpfulCount }} · 无用 {{ row.uselessCount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="hotFlag" label="热门" width="80">
          <template #default="{ row }">
            <el-tag :type="row.hotFlag === 1 ? 'warning' : 'info'" size="small">
              {{ row.hotFlag === 1 ? '热门' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button v-perm="'patient:faq:delete'" type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="720px"
      :close-on-click-modal="false"
      @close="dialogVisible = false"
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="分类编码" prop="categoryCode">
          <el-input v-model="formData.categoryCode" placeholder="如 REPORT、APPOINT" />
        </el-form-item>
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="formData.categoryName" placeholder="如 报告查询" />
        </el-form-item>
        <el-form-item label="问题" prop="question">
          <el-input v-model="formData.question" placeholder="患者会怎么问，就怎么写" />
        </el-form-item>
        <el-form-item label="答案" prop="answer">
          <el-input
            v-model="formData.answer"
            type="textarea"
            :rows="5"
            placeholder="涉及时间/价格/报销比例的一律写「以现场公示为准 / 请咨询窗口」，不要写死数字"
          />
        </el-form-item>
        <el-form-item label="关键词" prop="keywords">
          <el-input
            v-model="formData.keywords"
            placeholder="顿号分隔，务必带口语同义词（如 化验单、约号）"
          />
        </el-form-item>
        <el-form-item label="热门">
          <el-radio-group v-model="formData.hotFlag">
            <el-radio :value="1">热门</el-radio>
            <el-radio :value="0">普通</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="formData.sortOrder" :min="1" :max="9999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-perm="'patient:faq:upsert'" type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
