<template>
  <el-dialog v-model="visible" :title="isEdit ? '编辑检验项目' : '新增检验项目'" destroy-on-close width="650px">
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
      <el-button :loading="submitLoading" type="primary" @click="handleSubmit">确定</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {createLaboratoryItem, getLaboratoryItemDetail, updateLaboratoryItem} from '@/api/system'

// row 为空 = 新增，有值 = 修改（后端是同一份 create/update 表单，两侧字段与校验必须同一份）
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  // 项目类型的唯一口径在页面，列表列与本表单下拉共用同一份
  typeMap: {type: Object, default: () => ({})},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const isEdit = computed(() => !!props.row?.id)
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

const resetForm = () => {
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
}

watch(visible, async (open) => {
  if (!open) return
  resetForm()
  if (!props.row?.id) return
  try {
    const res = await getLaboratoryItemDetail(props.row.id)
    if (res.code === 200) Object.assign(formData, res.data)
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
})

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
      visible.value = false;
      emit('saved')
    } 
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    submitLoading.value = false
  }
}
</script>
