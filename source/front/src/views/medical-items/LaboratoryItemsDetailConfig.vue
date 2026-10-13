<template>
  <!-- 检验项目明细配置对话框 -->
  <el-dialog v-model="visible" :title="`明细配置 - ${props.row?.itemName}`" destroy-on-close width="800px">
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

    <LaboratoryItemsDetailEdit v-model="showDetailForm" :default-sort-order="detailList.length + 1"
                               :laboratory-item-id="props.row?.id" :row="currentDetailRow"
                               @saved="loadDetailList"/>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, ref, watch} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus} from '@element-plus/icons-vue'
import {deleteLaboratoryItemDetail, getLaboratoryItemDetailList} from '@/api/system'
import LaboratoryItemsDetailEdit from './LaboratoryItemsDetailEdit.vue'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 当前检验项目行（标题与明细归属都用它）
  row: {type: Object, default: null},
})
const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

// 检验项目明细配置
const detailList = ref([])
const detailLoading = ref(false)
const showDetailForm = ref(false)
const currentDetailRow = ref(null)

const loadDetailList = async () => {
  detailLoading.value = true
  try {
    const res = await getLaboratoryItemDetailList(props.row?.id)
    if (res.code === 200) {
      detailList.value = res.data || []
    }
  } catch (e) {
    console.error('加载明细失败', e)
  } finally {
    detailLoading.value = false
  }
}

watch(visible, async (open) => {
  if (!open) return
  await loadDetailList()
})

const handleAddDetail = () => {
  currentDetailRow.value = null
  showDetailForm.value = true
}

const handleEditDetail = (row) => {
  currentDetailRow.value = row
  showDetailForm.value = true
}

const handleDeleteDetail = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除明细项目「${row.itemName}」吗？`, '删除确认', {type: 'warning'})
    await deleteLaboratoryItemDetail(row.id)
    ElMessage.success('删除成功')
    await loadDetailList()
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
}
</script>
