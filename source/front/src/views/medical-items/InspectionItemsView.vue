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
          <el-button v-perm="'medtech:inspectionItems:add'" :icon="Plus" type="primary" @click="handleAdd">新增
          </el-button>
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
        <el-table-column label="检查部位" prop="bodyPart" width="120">
          <template #default="{row}">{{ row.bodyPart || '-' }}</template>
        </el-table-column>
        <el-table-column align="right" label="价格" prop="price" width="90">
          <template #default="{row}">¥{{ row.price || 0 }}</template>
        </el-table-column>
        <el-table-column align="center" label="时长(分)" prop="duration" width="80"/>
        <el-table-column align="center" label="急诊" prop="isEmergency" width="60">
          <template #default="{row}">
            <el-tag :type="row.isEmergency === 1 ? 'danger' : 'info'" size="small">{{
                row.isEmergency === 1 ? '是' : '否'
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
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="{row}">
            <el-button v-perm="'medtech:inspectionItems:add'" :icon="Edit" link type="primary" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button v-perm="'medtech:inspectionItems:delete'" :icon="Delete" link type="danger"
                       @click="handleDelete(row)">删除
            </el-button>
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
            <el-input v-model="formData.itemCode" :disabled="isEdit" placeholder="如：XR001"/>
          </el-form-item>
          <el-form-item label="项目名称" prop="itemName">
            <el-input v-model="formData.itemName" placeholder="如：胸部X线"/>
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="项目类型" prop="itemType">
            <el-select v-model="formData.itemType" class="w-full">
              <el-option v-for="(label, val) in typeMap" :key="val" :label="label" :value="Number(val)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="检查部位">
            <el-input v-model="formData.bodyPart" placeholder="如：胸部"/>
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
        <el-form-item label="检查前准备">
          <el-input v-model="formData.preparation" :rows="2" placeholder="如：禁食4小时" type="textarea"/>
        </el-form-item>
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="支持急诊">
            <el-switch v-model="formData.isEmergency" :active-value="1" :inactive-value="0"/>
          </el-form-item>
          <el-form-item label="需要预约">
            <el-switch v-model="formData.isAppointment" :active-value="1" :inactive-value="0"/>
          </el-form-item>
          <el-form-item label="状态">
            <el-switch v-model="formData.status" :active-value="1" :inactive-value="0"/>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button v-perm="'medtech:inspectionItems:add'" :loading="submitLoading" type="primary" @click="handleSubmit">
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
import {
  createInspectionItem,
  deleteInspectionItem,
  getInspectionItemDetail,
  getInspectionItemList,
  updateInspectionItem
} from '@/api/system'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'

const loading = ref(false)
const tableData = ref([])
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()
const searchForm = reactive({keyword: '', itemType: null})

const typeMap = {1: '放射检查', 2: '超声检查', 3: '心电图', 4: '内镜检查', 5: '其他'}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getInspectionItemList({
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
  id: null, itemCode: '', itemName: '', itemType: 1, bodyPart: '', price: 0,
  duration: 0, preparation: '', contraindication: '', isEmergency: 0, isAppointment: 1, status: 1,
})

const formRules = {
  itemCode: [{required: true, message: '请输入项目编码', trigger: 'blur'}],
  itemName: [{required: true, message: '请输入项目名称', trigger: 'blur'}],
  itemType: [{required: true, message: '请选择项目类型', trigger: 'change'}],
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增检查项目'
  Object.assign(formData, {
    id: null,
    itemCode: '',
    itemName: '',
    itemType: 1,
    bodyPart: '',
    price: 0,
    duration: 0,
    preparation: '',
    contraindication: '',
    isEmergency: 0,
    isAppointment: 1,
    status: 1
  })
  showDialog.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑检查项目'
  try {
    const res = await getInspectionItemDetail(row.id)
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
    const res = isEdit.value ? await updateInspectionItem(data) : await createInspectionItem(data)
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
    await ElMessageBox.confirm(`确定要删除检查项目「${row.itemName}」吗？`, '删除确认', {type: 'warning'})
    const res = await deleteInspectionItem(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功');
      loadData()
    } else ElMessage.error(res.message || '删除失败')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

onMounted(() => loadData())
</script>
