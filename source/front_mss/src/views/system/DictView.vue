<template>
  <div class="flex h-[calc(100vh-120px)] gap-4">
    <!-- 左侧：字典类型列表 -->
    <div class="w-72 flex-shrink-0 rounded-lg border border-gray-200 bg-white p-4">
      <div class="mb-3 flex items-center justify-between">
        <span class="font-medium">字典列表</span>
        <div class="dict-toolbar flex items-center">
          <el-button v-perm="'system:dict:add'" :icon="Plus" size="small" type="primary" @click="handleAddType">新增
          </el-button>
          <el-button :icon="Refresh" bg size="small" text @click="loadData">刷新</el-button>
        </div>
      </div>

      <!-- 搜索框 -->
      <div class="mb-3">
        <el-input v-model="searchKeyword" clearable placeholder="搜索字典名称" size="small">
          <template #prefix>
            <el-icon>
              <Search/>
            </el-icon>
          </template>
        </el-input>
      </div>

      <div v-loading="loading" class="h-[calc(100%-100px)] overflow-auto">
        <div
            v-for="item in dictTypeList.filter(t => !searchKeyword || t.dictName.includes(searchKeyword))"
            :key="item.id"
            :class="selectedDictType?.id === item.id ? 'bg-blue-50 text-blue-600' : 'hover:bg-gray-50'"
            class="mb-1 cursor-pointer rounded px-3 py-2 text-sm transition-colors"
            @click="handleSelectType(item)"
        >
          <div class="flex items-center justify-between">
            <span class="truncate">{{ item.dictName }}</span>
            <div class="flex items-center gap-1">
              <el-button v-if="item.dictSource === 2" v-perm="'system:dict:edit'" link size="small" type="primary"
                         @click.stop="handleEditDictType(item)">
                <el-icon>
                  <Edit/>
                </el-icon>
              </el-button>
              <el-button v-if="item.dictSource === 2" v-perm="'system:dict:delete'" link size="small" type="danger"
                         @click.stop="handleDeleteType(item)">
                <el-icon>
                  <Delete/>
                </el-icon>
              </el-button>
            </div>
          </div>
          <div class="mt-0.5 text-xs text-gray-400">{{ item.dictType }}</div>
        </div>
        <div v-if="dictTypeList.length === 0 && !loading" class="py-10 text-center text-gray-400">
          暂无数据
        </div>
      </div>
    </div>

    <!-- 右侧：字典值列表 -->
    <div class="flex-1 flex flex-col rounded-lg border border-gray-200 bg-white">
      <div class="flex items-center border-b border-gray-100 px-4 py-3">
        <span class="font-medium">{{
            selectedDictType ? '数据列表：' + selectedDictType.dictName : '请选择字典类型'
          }}</span>
      </div>

      <div class="flex-1 overflow-auto p-4">
        <div v-if="!selectedDictType" class="flex h-full items-center justify-center text-gray-400">
          请在左侧选择字典
        </div>
        <div v-else>
          <div class="mb-3 flex items-center justify-between">
            <span class="text-sm text-gray-500">共 {{ dictDataList.length }} 条数据</span>
            <el-button v-perm="'system:dict:add'" :icon="Plus" size="small" @click="handleAddData">新增</el-button>
          </div>
          <el-table v-loading="dataLoading" :data="dictDataList" stripe>
            <el-table-column label="字典标签" prop="dictLabel" width="150"/>
            <el-table-column label="字典值" prop="dictValue" width="120"/>
            <el-table-column label="排序" prop="dictSort" width="80"/>
            <el-table-column label="来源" prop="dictSource" width="100">
              <template #default="{ row }">
                <el-tag :type="row.dictSource === 1 ? 'info' : 'success'" size="small">
                  {{ row.dictSource === 1 ? '系统级' : '自定义' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" prop="status" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button v-if="row.dictSource === 2" v-perm="'system:dict:edit'" link size="small" type="primary"
                           @click="handleEditData(row)">
                  编辑
                </el-button>
                <el-button v-if="row.dictSource === 2" v-perm="'system:dict:delete'" link size="small" type="danger"
                           @click="handleDeleteData(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- 字典类型对话框 -->
    <DictTypeEdit v-model="typeDialogVisible" :row="typeEditRow" @saved="loadData"/>

    <!-- 字典数据对话框 -->
    <DictDataEdit v-model="dataDialogVisible" :dict-type="selectedDictType?.dictType" :row="dataEditRow"
                  @saved="handleDataSaved"/>
  </div>
</template>

<script lang="js" setup>
import {onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue'
import {deleteDictData, deleteDictType, getDictDataList, getDictTypeList} from '@/api/system'
import DictDataEdit from './DictDataEdit.vue'
import DictTypeEdit from './DictTypeEdit.vue'

const loading = ref(false)
const dictTypeList = ref([])

// 搜索
const searchKeyword = ref('')

// 选中的字典类型
const selectedDictType = ref(null)

// 字典类型表单（增改共用 DictTypeEdit，row 为空走新增）
const typeDialogVisible = ref(false)
const typeEditRow = ref(null)

// 字典数据列表
const dictDataList = ref([])
const dataLoading = ref(false)

// 字典数据表单（增改共用 DictDataEdit，row 为空走新增）
const dataDialogVisible = ref(false)
const dataEditRow = ref(null)

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDictTypeList()
    if (res.code === 200) {
      dictTypeList.value = res.data || []
    }
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    loading.value = false
  }
}

// 点击字典类型
const handleSelectType = async (type) => {
  selectedDictType.value = type
  await loadDictData(type.dictType)
}

const loadDictData = async (dictType) => {
  dataLoading.value = true
  try {
    const res = await getDictDataList(dictType)
    if (res.code === 200) {
      dictDataList.value = res.data || []
    }
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    dataLoading.value = false
  }
}

// 字典类型操作
const handleAddType = () => {
  typeEditRow.value = null
  typeDialogVisible.value = true
}

const handleEditDictType = (type) => {
  selectedDictType.value = type
  typeEditRow.value = type
  typeDialogVisible.value = true
}

const handleDeleteType = async (type) => {
  if (type.dictSource === 1) {
    ElMessage.warning('系统级字典不允许删除')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除字典类型 "${type.dictName}" 吗？`, '删除确认', {
      type: 'warning',
    })
    const res = await deleteDictType(type.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      if (selectedDictType.value?.id === type.id) {
        selectedDictType.value = null
        dictDataList.value = []
      }
      loadData()
    } 
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
}

// 字典数据操作
const handleAddData = () => {
  if (!selectedDictType.value) {
    ElMessage.warning('请先选择字典类型')
    return
  }
  dataEditRow.value = null
  dataDialogVisible.value = true
}

const handleEditData = (row) => {
  dataEditRow.value = row
  dataDialogVisible.value = true
}

const handleDataSaved = () => {
  if (selectedDictType.value) loadDictData(selectedDictType.value.dictType)
}

const handleDeleteData = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除字典数据 "${row.dictLabel}" 吗？`, '删除确认', {
      type: 'warning',
    })
    const res = await deleteDictData(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadDictData(selectedDictType.value.dictType)
    } 
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
}

</script>

<style scoped>
/* 与 MenuView 工具栏同口径：压掉 EP 相邻按钮默认 12px 边距，紧凑贴右 */
.dict-toolbar :deep(.el-button + .el-button) {
  margin-left: 2px;
}

.dict-toolbar :deep(.el-button) {
  padding: 5px 8px;
}
</style>
