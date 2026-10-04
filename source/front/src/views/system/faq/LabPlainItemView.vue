<script setup lang="js">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import {
  getLabPlainList,
  getLabPlainDetail,
  getLabPlainGroupNames,
  labPlainUpsert,
  labPlainDelete,
} from '@/api/labPlain'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

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
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

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
  itemName: [{ required: true, message: '请输入检验项目名称', trigger: 'blur' }],
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
    const res = await labPlainUpsert({ ...formData })
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
              placeholder="检验项目名或白话名"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="分组">
            <el-select v-model="searchForm.groupName" placeholder="全部分组" clearable style="width: 140px">
              <el-option v-for="g in groupOptions" :key="g" :label="g" :value="g" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 110px">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'lab:plain:upsert'" type="primary" :icon="Plus" @click="handleAdd">新增词条</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" stripe :max-height="tableMaxHeight">
        <el-table-column prop="groupName" label="分组" width="100" />
        <el-table-column prop="itemName" label="检验项目名" width="140" show-overflow-tooltip />
        <el-table-column prop="plainName" label="白话名" width="130" show-overflow-tooltip />
        <el-table-column prop="whatIsIt" label="这项查什么" min-width="220" show-overflow-tooltip />
        <el-table-column prop="highText" label="偏高说明" min-width="220" show-overflow-tooltip />
        <el-table-column prop="lowText" label="偏低说明" min-width="220" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" />
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
            <el-button v-perm="'lab:plain:delete'" type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
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
      <el-alert
        type="warning"
        :closable="false"
        class="mb-3"
        title="白话词典只解释指标含义，不给诊断和用药建议"
        description="不要写「确诊」「建议服用」「每日 X 次」这类措辞 —— 患者会把这句话当医嘱读，而这里没有医生复核环节。写了会被后端拦下。"
      />
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="110px">
        <el-form-item label="检验项目名" prop="itemName">
          <el-input
            v-model="formData.itemName"
            placeholder="必须与 biz_lab_result 里的项目名完全一致，否则匹配不上"
          />
        </el-form-item>
        <el-form-item label="白话名" prop="plainName">
          <el-input v-model="formData.plainName" placeholder="如 血色素、坏胆固醇、心肌损伤指标" />
        </el-form-item>
        <el-form-item label="分组" prop="groupName">
          <el-input v-model="formData.groupName" placeholder="如 血常规、肝功能、血脂" />
        </el-form-item>
        <el-form-item label="这项查什么" prop="whatIsIt">
          <el-input v-model="formData.whatIsIt" type="textarea" :rows="2" placeholder="给患者看的一句话" />
        </el-form-item>
        <el-form-item label="偏高说明" prop="highText">
          <el-input v-model="formData.highText" type="textarea" :rows="2" placeholder="结果偏高时怎么解释" />
        </el-form-item>
        <el-form-item label="偏低说明" prop="lowText">
          <el-input v-model="formData.lowText" type="textarea" :rows="2" placeholder="结果偏低时怎么解释" />
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
        <el-form-item label="备注">
          <el-input v-model="formData.remark" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-perm="'lab:plain:upsert'" type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
