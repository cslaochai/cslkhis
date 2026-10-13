<template>
  <!-- 明细编辑对话框 -->
  <el-dialog v-model="visible" :title="isEdit ? '编辑指标' : '新增指标'" destroy-on-close width="500px">
    <el-form :model="formData" label-width="80px">
      <el-form-item label="指标编码" required>
        <el-input v-model="formData.itemCode" placeholder="如：WBC"/>
      </el-form-item>
      <el-form-item label="指标名称" required>
        <el-input v-model="formData.itemName" placeholder="如：白细胞计数"/>
      </el-form-item>
      <el-form-item label="单位">
        <el-input v-model="formData.unit" placeholder="如：10^9/L"/>
      </el-form-item>
      <el-form-item label="参考范围">
        <el-input v-model="formData.referenceRange" placeholder="如：4-10"/>
      </el-form-item>
      <el-form-item label="排序号">
        <el-input-number v-model="formData.sortOrder" :min="0"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="handleSubmitDetail">确定</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, reactive, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {createLaboratoryItemDetail, updateLaboratoryItemDetail} from '@/api/system'

// row 为空 = 新增，有值 = 修改（后端是同一份明细 create/update 表单）
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  // 所属检验项目 id（新增时的归属）
  laboratoryItemId: {type: [String, Number], default: null},
  // 新增时排序号默认接在已有明细之后
  defaultSortOrder: {type: Number, default: 0},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const isEdit = computed(() => !!props.row?.id)

const formData = reactive({
  id: null,
  laboratoryItemId: null,
  itemCode: '',
  itemName: '',
  unit: '',
  referenceRange: '',
  sortOrder: 0,
})

watch(visible, (open) => {
  if (!open) return
  const row = props.row
  if (row?.id) {
    Object.assign(formData, {
      id: row.id,
      laboratoryItemId: row.laboratoryItemId,
      itemCode: row.itemCode,
      itemName: row.itemName,
      unit: row.unit || '',
      referenceRange: row.referenceRange || '',
      sortOrder: row.sortOrder || 0,
    })
    return
  }
  Object.assign(formData, {
    id: null,
    laboratoryItemId: props.laboratoryItemId,
    itemCode: '',
    itemName: '',
    unit: '',
    referenceRange: '',
    sortOrder: props.defaultSortOrder,
  })
})

const handleSubmitDetail = async () => {
  if (!formData.itemCode || !formData.itemName) {
    ElMessage.warning('请填写项目编码和名称')
    return
  }
  try {
    if (isEdit.value) {
      await updateLaboratoryItemDetail(formData)
      ElMessage.success('修改成功')
    } else {
      await createLaboratoryItemDetail(formData)
      ElMessage.success('新增成功')
    }
    visible.value = false
    emit('saved')
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}
</script>
