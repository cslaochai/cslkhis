<template>
  <div class="space-y-3">
    <el-row :gutter="12">
      <!-- 左侧：字典类型 -->
      <el-col :span="10">
        <div class="his-card p-3">
          <div class="flex items-center justify-between mb-2">
            <div class="font-semibold text-[15px] text-[#0B3B66]">字典类型</div>
            <el-button type="primary" size="small" @click="openTypeEdit(null)">新增类型</el-button>
          </div>
          <el-input v-model="typeQuery.keyword" placeholder="类型/名称" clearable size="small" class="mb-2" @keyup.enter="loadTypes" @clear="loadTypes" />
          <el-table
            :data="types" size="small" v-loading="typeLoading" highlight-current-row
            :row-class-name="({ row }) => (row.id === currentType?.id ? 'current-type-row' : '')"
            @row-click="selectType"
          >
            <el-table-column prop="dictName" label="名称" min-width="110" />
            <el-table-column prop="dictType" label="类型编码" min-width="120" show-overflow-tooltip />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="openTypeEdit(row)">编辑</el-button>
                <el-button link type="danger" @click.stop="onDeleteType(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="flex justify-end mt-2">
            <el-pagination
              small background layout="total, prev, pager, next" :total="typeTotal"
              :page-size="typeQuery.size" :current-page="typeQuery.page"
              @current-change="(p) => { typeQuery.page = p; loadTypes() }"
            />
          </div>
        </div>
      </el-col>

      <!-- 右侧：字典数据 -->
      <el-col :span="14">
        <div class="his-card p-3">
          <div class="flex items-center justify-between mb-2">
            <div class="font-semibold text-[15px] text-[#0B3B66]">
              字典数据{{ currentType ? `（${currentType.dictName}）` : '' }}
            </div>
            <div class="flex items-center gap-2">
              <el-input v-model="dataQuery.keyword" placeholder="标签/键值" clearable size="small" style="width:160px" @keyup.enter="loadData" @clear="loadData" />
              <el-button type="primary" size="small" :disabled="!currentType" @click="openDataEdit(null)">新增数据</el-button>
            </div>
          </div>
          <el-table :data="dictDatas" size="small" v-loading="dataLoading">
            <el-table-column prop="dictLabel" label="标签" min-width="120" />
            <el-table-column prop="dictValue" label="键值" width="110" />
            <el-table-column label="排序" width="70" align="center" prop="dictSort" />
            <el-table-column label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">
                  {{ row.status === 1 ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openDataEdit(row)">编辑</el-button>
                <el-button link type="danger" @click="onDeleteData(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="flex justify-end mt-2">
            <el-pagination
              small background layout="total, prev, pager, next" :total="dataTotal"
              :page-size="dataQuery.size" :current-page="dataQuery.page"
              @current-change="(p) => { dataQuery.page = p; loadData() }"
            />
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 类型编辑 -->
    <el-dialog v-model="typeVisible" :title="typeForm.id ? '编辑字典类型' : '新增字典类型'" width="440px" append-to-body>
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="90px">
        <el-form-item label="类型编码" prop="dictType">
          <el-input v-model="typeForm.dictType" :disabled="!!typeForm.id" placeholder="如 mss_pay_class" />
        </el-form-item>
        <el-form-item label="类型名称" prop="dictName">
          <el-input v-model="typeForm.dictName" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="typeForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="typeForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSaveType">保存</el-button>
      </template>
    </el-dialog>

    <!-- 数据编辑 -->
    <el-dialog v-model="dataVisible" :title="dataForm.id ? '编辑字典数据' : '新增字典数据'" width="440px" append-to-body>
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="90px">
        <el-form-item label="字典类型">
          <el-input :model-value="currentType?.dictType" disabled />
        </el-form-item>
        <el-form-item label="标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" />
        </el-form-item>
        <el-form-item label="键值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dataForm.dictSort" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="dataForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dataForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSaveData">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  dictTypePage, dictTypeUpsert, dictTypeDelete, dictDataPage, dictDataUpsert, dictDataDelete,
} from '@/api/system'

// ---------- 类型 ----------
const types = ref([])
const typeTotal = ref(0)
const typeLoading = ref(false)
const typeQuery = reactive({ keyword: '', page: 1, size: 10 })
const currentType = ref(null)

async function loadTypes() {
  typeLoading.value = true
  const res = await dictTypePage({ pageNum: typeQuery.page, pageSize: typeQuery.size, keyword: typeQuery.keyword || undefined })
  typeLoading.value = false
  types.value = res.data.records || []
  typeTotal.value = res.data.total || 0
  if (!currentType.value && types.value.length) {
    selectType(types.value[0])
  }
}

function selectType(row) {
  currentType.value = row
  dataQuery.page = 1
  loadData()
}

const typeVisible = ref(false)
const saving = ref(false)
const typeFormRef = ref()
const typeForm = reactive({ id: null, dictType: '', dictName: '', status: 1, remark: '' })
const typeRules = {
  dictType: [{ required: true, message: '请输入类型编码', trigger: 'blur' }],
  dictName: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
}
function openTypeEdit(row) {
  Object.assign(typeForm, {
    id: row?.id || null, dictType: row?.dictType || '', dictName: row?.dictName || '',
    status: row?.status ?? 1, remark: row?.remark || '',
  })
  typeVisible.value = true
}
async function onSaveType() {
  await typeFormRef.value.validate()
  saving.value = true
  try {
    await dictTypeUpsert({ ...typeForm })
    ElMessage.success('保存成功')
    typeVisible.value = false
    loadTypes()
  } catch (e) {
  } finally {
    saving.value = false
  }
}
async function onDeleteType(row) {
  await ElMessageBox.confirm(`确定删除字典类型「${row.dictName}」吗？其下数据将一并删除`, '提示', { type: 'warning' })
  await dictTypeDelete(row.id)
  ElMessage.success('删除成功')
  if (currentType.value?.id === row.id) currentType.value = null
  loadTypes()
}

// ---------- 数据 ----------
const dictDatas = ref([])
const dataTotal = ref(0)
const dataLoading = ref(false)
const dataQuery = reactive({ keyword: '', page: 1, size: 10 })

async function loadData() {
  if (!currentType.value) { dictDatas.value = []; dataTotal.value = 0; return }
  dataLoading.value = true
  const res = await dictDataPage({
    pageNum: dataQuery.page, pageSize: dataQuery.size, dictType: currentType.value.dictType,
    keyword: dataQuery.keyword || undefined,
  })
  dataLoading.value = false
  dictDatas.value = res.data.records || []
  dataTotal.value = res.data.total || 0
}

const dataVisible = ref(false)
const dataFormRef = ref()
const dataForm = reactive({ id: null, dictLabel: '', dictValue: '', dictSort: 0, status: 1, remark: '' })
const dataRules = {
  dictLabel: [{ required: true, message: '请输入标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入键值', trigger: 'blur' }],
}
function openDataEdit(row) {
  Object.assign(dataForm, {
    id: row?.id || null, dictLabel: row?.dictLabel || '', dictValue: row?.dictValue || '',
    dictSort: row?.dictSort ?? 0, status: row?.status ?? 1, remark: row?.remark || '',
  })
  dataVisible.value = true
}
async function onSaveData() {
  await dataFormRef.value.validate()
  saving.value = true
  try {
    await dictDataUpsert({ ...dataForm, dictType: currentType.value.dictType })
    ElMessage.success('保存成功')
    dataVisible.value = false
    loadData()
  } catch (e) {
  } finally {
    saving.value = false
  }
}
async function onDeleteData(row) {
  await ElMessageBox.confirm(`确定删除字典数据「${row.dictLabel}」吗？`, '提示', { type: 'warning' })
  await dictDataDelete(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadTypes)
</script>

<style scoped>
:deep(.current-type-row) {
  --el-table-tr-bg-color: #e8f1fb;
}
</style>
