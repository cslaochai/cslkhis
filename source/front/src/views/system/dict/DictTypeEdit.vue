<template>
  <!-- 新增与编辑共用一张表单（后端同一个 upsert，row 为空走新增） -->
  <el-dialog v-model="visible" :title="formData.id ? '编辑字典类型' : '新增字典类型'" width="500px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <el-form-item label="字典名称" prop="dictName">
        <el-input v-model="formData.dictName" placeholder="请输入字典名称"/>
      </el-form-item>
      <el-form-item label="字典类型" prop="dictType">
        <el-input v-model="formData.dictType" :disabled="!!formData.id" placeholder="如：sys_user_sex"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" placeholder="请输入备注" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="['system:dict:add','system:dict:edit']" :loading="submitLoading" type="primary"
                 @click="handleSubmit">确定
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {createDictType, updateDictType} from '@/api/system'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 编辑传入当前行；为空表示新增
  row: {type: Object, default: null},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const submitLoading = ref(false)
const formRef = ref(null)
const formData = reactive({
  id: null,
  dictName: '',
  dictType: '',
  dictSource: 2,  // 1-系统级 2-自定义
  status: 1,
  remark: '',
})
const rules = {
  dictName: [{required: true, message: '请输入字典名称', trigger: 'blur'}],
  dictType: [{required: true, message: '请输入字典类型', trigger: 'blur'}],
}

const resetForm = () => {
  formData.id = null
  formData.dictName = ''
  formData.dictType = ''
  formData.dictSource = 2
  formData.status = 1
  formData.remark = ''
}

watch(visible, (open) => {
  if (!open) return
  resetForm()
  if (props.row) Object.assign(formData, props.row)
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
    let res
    if (formData.id) {
      res = await updateDictType(formData)
    } else {
      res = await createDictType(formData)
    }

    if (res.code === 200) {
      ElMessage.success(formData.id ? '修改成功' : '新增成功')
      visible.value = false
      emit('saved')
    } 
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    submitLoading.value = false
  }
}
</script>
