<template>
  <el-dialog v-model="visible" :title="isEdit ? '编辑检查项目' : '新增检查项目'" destroy-on-close width="650px">
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
      <el-button v-perm="'medtech:inspectionItems:add'" :loading="submitLoading" type="primary" @click="handleSubmit">
        确定
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {createInspectionItem, getInspectionItemDetail, updateInspectionItem} from '@/api/system'

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
  id: null, itemCode: '', itemName: '', itemType: 1, bodyPart: '', price: 0,
  duration: 0, preparation: '', contraindication: '', isEmergency: 0, isAppointment: 1, status: 1,
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
    bodyPart: '',
    price: 0,
    duration: 0,
    preparation: '',
    contraindication: '',
    isEmergency: 0,
    isAppointment: 1,
    status: 1
  })
}

watch(visible, async (open) => {
  if (!open) return
  resetForm()
  if (!props.row?.id) return
  try {
    const res = await getInspectionItemDetail(props.row.id)
    if (res.code === 200) Object.assign(formData, res.data)
  } catch (e) {
    ElMessage.error('获取详情失败')
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
    const res = isEdit.value ? await updateInspectionItem(data) : await createInspectionItem(data)
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功');
      visible.value = false;
      emit('saved')
    } else ElMessage.error(res.message || '操作失败')
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}
</script>
