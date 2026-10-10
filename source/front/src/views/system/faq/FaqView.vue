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

    <el-dialog
        v-model="dialogVisible"
        :close-on-click-modal="false"
        :title="dialogTitle"
        width="720px"
        @close="dialogVisible = false"
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="分类编码" prop="categoryCode">
          <el-select
              v-model="formData.categoryCode"
              clearable
              filterable
              placeholder="请选择分类"
              style="width: 100%"
              @change="onCategoryChange"
          >
            <el-option
                v-for="d in faqCategoryOptions"
                :key="d.dictValue"
                :label="`${d.dictLabel}（${d.dictValue}）`"
                :value="d.dictValue"
            />
          </el-select>
        </el-form-item>
        <!-- 分类名称随编码从字典自动带出，禁用防止手填导致与字典不一致 -->
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="formData.categoryName" disabled placeholder="随分类自动带出"/>
        </el-form-item>
        <el-form-item label="问题" prop="question">
          <el-input v-model="formData.question" placeholder="患者会怎么问，就怎么写"/>
        </el-form-item>
        <el-form-item label="答案" prop="answer">
          <el-input
              v-model="formData.answer"
              :rows="5"
              placeholder="涉及时间/价格/报销比例的一律写「以现场公示为准 / 请咨询窗口」，不要写死数字"
              type="textarea"
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
          <el-input-number v-model="formData.sortOrder" :max="9999" :min="1"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-perm="'patient:faq:upsert'" :loading="submitLoading" type="primary" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="js" setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue'
import {faqDelete, faqUpsert, getFaqAdminDetail, getFaqAdminList} from '@/api/patientFaq'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {DICT_TYPE, loadDictDataList} from '@/lib/dict-cache'

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
  categoryCode: [{required: true, message: '请输入分类编码', trigger: 'blur'}],
  categoryName: [{required: true, message: '请输入分类名称', trigger: 'blur'}],
  question: [{required: true, message: '请输入问题', trigger: 'blur'}],
  answer: [{required: true, message: '请输入答案', trigger: 'blur'}],
}

onMounted(() => {
  loadData()
  loadFaqCategories()
})

// 加载 FAQ 分类字典（his_faq_category）
const loadFaqCategories = async () => {
  faqCategoryOptions.value = await loadDictDataList(DICT_TYPE.FAQ_CATEGORY)
}

// 选中分类后自动带出分类名称（保持与字典一致，避免手填分裂）
const onCategoryChange = (val) => {
  const item = faqCategoryOptions.value.find(d => d.dictValue === val)
  formData.categoryName = item ? item.dictLabel : ''
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
    const res = await faqUpsert({...formData})
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
