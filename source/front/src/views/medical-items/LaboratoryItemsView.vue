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

    <el-dialog v-model="showDialog" :title="dialogTitle" destroy-on-close width="650px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="项目编码" prop="itemCode">
            <el-input v-model="formData.itemCode" :disabled="isEdit" placeholder="如：BL001"/>
          </el-form-item>
          <el-form-item label="项目名称" prop="itemName">
            <el-input v-model="formData.itemName" placeholder="如：血常规"/>
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="项目类型" prop="itemType">
            <el-select v-model="formData.itemType" class="w-full">
              <el-option v-for="(label, val) in typeMap" :key="val" :label="label" :value="Number(val)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="标本类型">
            <el-select v-model="formData.specimenType" class="w-full">
              <el-option label="血液" value="血液"/>
              <el-option label="尿液" value="尿液"/>
              <el-option label="粪便" value="粪便"/>
              <el-option label="体液" value="体液"/>
              <el-option label="组织" value="组织"/>
            </el-select>
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="价格(元)">
            <el-input-number v-model="formData.price" :min="0" :precision="2" class="w-full"/>
          </el-form-item>
          <el-form-item label="时长(分钟)">
            <el-input-number v-model="formData.duration" :min="0" class="w-full"/>
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="参考值">
            <el-input v-model="formData.referenceValue" placeholder="如：3.5-5.5"/>
          </el-form-item>
          <el-form-item label="单位">
            <el-input v-model="formData.unit" placeholder="如：mmol/L"/>
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="需要空腹">
            <el-switch v-model="formData.isFasting" :active-value="1" :inactive-value="0"/>
          </el-form-item>
          <el-form-item label="状态">
            <el-switch v-model="formData.status" :active-value="1" :inactive-value="0"/>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button :loading="submitLoading" type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 检验项目明细配置对话框 -->
    <el-dialog v-model="showDetailDialog" :title="`明细配置 - ${currentLaboratoryItem?.itemName}`" destroy-on-close
               width="800px">
      <div class="space-y-4">
        <div class="flex items-center justify-between">
          <span class="text-sm text-slate-500">配置该检验项目包含的明细指标，检验科录入结果时会自动加载</span>
          <el-button :icon="Plus" type="primary" @click="handleAddDetail">新增指标</el-button>
        </div>

        <el-table v-loading="detailLoading" :data="detailList" border>
          <el-table-column label="编码" prop="itemCode" width="100"/>
          <el-table-column label="指标名称" min-width="150" prop="itemName"/>
          <el-table-column label="单位" prop="unit" width="100"/>
          <el-table-column label="参考范围" prop="referenceRange" width="150"/>
          <el-table-column align="center" label="排序" prop="sortOrder" width="80"/>
          <el-table-column fixed="right" label="操作" width="140">
            <template #default="{row}">
              <el-button :icon="Edit" link type="primary" @click="handleEditDetail(row)">编辑</el-button>
              <el-button :icon="Delete" link type="danger" @click="handleDeleteDetail(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="detailList.length === 0 && !detailLoading" class="py-8 text-center text-sm text-slate-400">
          暂未配置明细指标，请点击"新增指标"添加
        </div>
      </div>
    </el-dialog>

    <!-- 明细编辑对话框 -->
    <el-dialog v-model="showDetailForm" :title="isEditDetail ? '编辑指标' : '新增指标'" destroy-on-close width="500px">
      <el-form :model="detailFormData" label-width="80px">
        <el-form-item label="指标编码" required>
          <el-input v-model="detailFormData.itemCode" placeholder="如：WBC"/>
        </el-form-item>
        <el-form-item label="指标名称" required>
          <el-input v-model="detailFormData.itemName" placeholder="如：白细胞计数"/>
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="detailFormData.unit" placeholder="如：10^9/L"/>
        </el-form-item>
        <el-form-item label="参考范围">
          <el-input v-model="detailFormData.referenceRange" placeholder="如：4-10"/>
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="detailFormData.sortOrder" :min="0"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDetailForm = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitDetail">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="js" setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search, Setting} from '@element-plus/icons-vue'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {
  createLaboratoryItem,
  createLaboratoryItemDetail,
  deleteLaboratoryItem,
  deleteLaboratoryItemDetail,
  getLaboratoryItemDetail,
  getLaboratoryItemDetailList,
  getLaboratoryItemList,
  updateLaboratoryItem,
  updateLaboratoryItemDetail
} from '@/api/system'

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
const dialogTitle = ref('')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const formData = reactive({
  id: null, itemCode: '', itemName: '', itemType: 1, specimenType: '血液', price: 0,
  duration: 0, referenceValue: '', unit: '', isFasting: 0, status: 1,
})

const formRules = {
  itemCode: [{required: true, message: '请输入项目编码', trigger: 'blur'}],
  itemName: [{required: true, message: '请输入项目名称', trigger: 'blur'}],
  itemType: [{required: true, message: '请选择项目类型', trigger: 'change'}],
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增检验项目'
  Object.assign(formData, {
    id: null,
    itemCode: '',
    itemName: '',
    itemType: 1,
    specimenType: '血液',
    price: 0,
    duration: 0,
    referenceValue: '',
    unit: '',
    isFasting: 0,
    status: 1
  })
  showDialog.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑检验项目'
  try {
    const res = await getLaboratoryItemDetail(row.id)
    if (res.code === 200) Object.assign(formData, res.data)
  } catch (e) {
    ElMessage.error('获取详情失败')
  }
  showDialog.value = true
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
    const data = {...formData}
    const res = isEdit.value ? await updateLaboratoryItem(data) : await createLaboratoryItem(data)
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功');
      showDialog.value = false;
      loadData()
    } else ElMessage.error(res.message || '操作失败')
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除检验项目「${row.itemName}」吗？`, '删除确认', {type: 'warning'})
    const res = await deleteLaboratoryItem(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功');
      loadData()
    } else ElMessage.error(res.message || '删除失败')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

// ==================== 检验项目明细配置 ====================
const showDetailDialog = ref(false)
const currentLaboratoryItem = ref(null)
const detailList = ref([])
const detailLoading = ref(false)
const showDetailForm = ref(false)
const isEditDetail = ref(false)
const detailFormData = reactive({
  id: null,
  laboratoryItemId: null,
  itemCode: '',
  itemName: '',
  unit: '',
  referenceRange: '',
  sortOrder: 0,
})

const handleConfigDetail = async (row) => {
  currentLaboratoryItem.value = row
  showDetailDialog.value = true
  await loadDetailList(row.id)
}

const loadDetailList = async (laboratoryItemId) => {
  detailLoading.value = true
  try {
    const res = await getLaboratoryItemDetailList(laboratoryItemId)
    if (res.code === 200) {
      detailList.value = res.data || []
    }
  } catch (e) {
    console.error('加载明细失败', e)
  } finally {
    detailLoading.value = false
  }
}

const handleAddDetail = () => {
  isEditDetail.value = false
  Object.assign(detailFormData, {
    id: null,
    laboratoryItemId: currentLaboratoryItem.value?.id,
    itemCode: '',
    itemName: '',
    unit: '',
    referenceRange: '',
    sortOrder: detailList.value.length + 1,
  })
  showDetailForm.value = true
}

const handleEditDetail = (row) => {
  isEditDetail.value = true
  Object.assign(detailFormData, {
    id: row.id,
    laboratoryItemId: row.laboratoryItemId,
    itemCode: row.itemCode,
    itemName: row.itemName,
    unit: row.unit || '',
    referenceRange: row.referenceRange || '',
    sortOrder: row.sortOrder || 0,
  })
  showDetailForm.value = true
}

const handleSubmitDetail = async () => {
  if (!detailFormData.itemCode || !detailFormData.itemName) {
    ElMessage.warning('请填写项目编码和名称')
    return
  }
  try {
    if (isEditDetail.value) {
      await updateLaboratoryItemDetail(detailFormData)
      ElMessage.success('修改成功')
    } else {
      await createLaboratoryItemDetail(detailFormData)
      ElMessage.success('新增成功')
    }
    showDetailForm.value = false
    await loadDetailList(currentLaboratoryItem.value.id)
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const handleDeleteDetail = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除明细项目「${row.itemName}」吗？`, '删除确认', {type: 'warning'})
    await deleteLaboratoryItemDetail(row.id)
    ElMessage.success('删除成功')
    await loadDetailList(currentLaboratoryItem.value.id)
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

onMounted(() => loadData())
</script>
