<script lang="js" setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue'
import {
  getLabPlainDetail,
  getLabPlainGroupNames,
  getLabPlainList,
  labPlainDelete,
  labPlainUpsert,
} from '@/api/labPlain'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'

const loading = ref(false)
const searchForm = ref({
  keyword: '',
  groupName: '',
  status: null,
})
const tableData = ref([])
const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const groupOptions = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增白话词条')
const submitLoading = ref(false)
const formRef = ref(null)
const formData = reactive({
  id: null,
  groupName: '',
  itemName: '',
  plainName: '',
  whatIsIt: '',
  highText: '',
  lowText: '',
  status: 1,
  sortOrder: 999,
  remark: '',
})

const rules = {
  itemName: [{required: true, message: '请输入检验项目名称', trigger: 'blur'}],
}

onMounted(() => {
  loadGroupNames()
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getLabPlainList({
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

const loadGroupNames = async () => {
  try {
    const res = await getLabPlainGroupNames()
    if (res.code === 200) {
      groupOptions.value = res.data || []
    }
  } catch (error) {
    // 下拉拉不到分组不影响增删改查，静默即可（仍能按项目名/状态筛）
    console.warn('分组清单加载失败', error)
  }
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.value.keyword = ''
  searchForm.value.groupName = ''
  searchForm.value.status = null
  pagination.value.pageNum = 1
  loadData()
}

const resetForm = () => {
  formData.id = null
  formData.groupName = ''
  formData.itemName = ''
  formData.plainName = ''
  formData.whatIsIt = ''
  formData.highText = ''
  formData.lowText = ''
  formData.status = 1
  formData.sortOrder = 999
  formData.remark = ''
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增白话词条'
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  resetForm()
  dialogTitle.value = '编辑白话词条'
  try {
    const res = await getLabPlainDetail(row.id)
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
    const res = await labPlainUpsert({...formData})
    if (res.code === 200) {
      ElMessage.success(formData.id ? '修改成功' : '新增成功')
      dialogVisible.value = false
      loadData()
      loadGroupNames()
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
    await ElMessageBox.confirm(`确定删除词条「${row.itemName}」吗？删除后该项目的报告解读只剩数值，没有白话。`, '删除确认', {
      type: 'warning',
    })
    const res = await labPlainDelete(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
      loadGroupNames()
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
          <el-form-item label="项目名">
            <el-input
                v-model="searchForm.keyword"
                clearable
                placeholder="检验项目名或白话名"
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="分组">
            <el-select v-model="searchForm.groupName" clearable placeholder="全部分组" style="width: 140px">
              <el-option v-for="g in groupOptions" :key="g" :label="g" :value="g"/>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" clearable placeholder="全部" style="width: 110px">
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
          <el-button v-perm="'lab:plain:upsert'" :icon="Plus" type="primary" @click="handleAdd">新增词条</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="tableData" :max-height="tableMaxHeight" stripe>
        <el-table-column label="分组" prop="groupName" width="100"/>
        <el-table-column label="检验项目名" prop="itemName" show-overflow-tooltip width="140"/>
        <el-table-column label="白话名" prop="plainName" show-overflow-tooltip width="130"/>
        <el-table-column label="这项查什么" min-width="220" prop="whatIsIt" show-overflow-tooltip/>
        <el-table-column label="偏高说明" min-width="220" prop="highText" show-overflow-tooltip/>
        <el-table-column label="偏低说明" min-width="220" prop="lowText" show-overflow-tooltip/>
        <el-table-column label="排序" prop="sortOrder" width="80"/>
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
            <el-button v-perm="'lab:plain:delete'" :icon="Delete" link type="danger" @click="handleDelete(row)">删除
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
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="110px">
        <el-form-item label="检验项目名" prop="itemName">
          <el-input
              v-model="formData.itemName"
              placeholder="必须与 biz_lab_result 里的项目名完全一致，否则匹配不上"
          />
        </el-form-item>
        <el-form-item label="白话名" prop="plainName">
          <el-input v-model="formData.plainName" placeholder="如 血色素、坏胆固醇、心肌损伤指标"/>
        </el-form-item>
        <el-form-item label="分组" prop="groupName">
          <el-input v-model="formData.groupName" placeholder="如 血常规、肝功能、血脂"/>
        </el-form-item>
        <el-form-item label="这项查什么" prop="whatIsIt">
          <el-input v-model="formData.whatIsIt" :rows="2" placeholder="给患者看的一句话" type="textarea"/>
        </el-form-item>
        <el-form-item label="偏高说明" prop="highText">
          <el-input v-model="formData.highText" :rows="2" placeholder="结果偏高时怎么解释" type="textarea"/>
        </el-form-item>
        <el-form-item label="偏低说明" prop="lowText">
          <el-input v-model="formData.lowText" :rows="2" placeholder="结果偏低时怎么解释" type="textarea"/>
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
        <el-form-item label="备注">
          <el-input v-model="formData.remark" placeholder="选填"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-perm="'lab:plain:upsert'" :loading="submitLoading" type="primary" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
