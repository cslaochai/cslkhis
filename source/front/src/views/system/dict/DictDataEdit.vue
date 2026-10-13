<template>
  <!-- 新增与编辑共用一张表单（后端同一个 upsert，row 为空走新增） -->
  <el-dialog v-model="visible" :title="formData.id ? '编辑字典数据' : '新增字典数据'" width="500px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
      <el-form-item label="字典标签" prop="dictLabel">
        <el-input v-model="formData.dictLabel" placeholder="如：男"/>
      </el-form-item>
      <el-form-item label="字典值" prop="dictValue">
        <el-input v-model="formData.dictValue" placeholder="如：1"/>
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="formData.dictSort" :max="999" :min="0"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
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
import {createDictData, updateDictData} from '@/api/system'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 编辑传入当前行；为空表示新增
  row: {type: Object, default: null},
  // 新增时归属的字典类型（列表页当前选中的类型编码）
  dictType: {type: String, default: ''},
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
  dictType: '',
  dictLabel: '',
  dictValue: '',
  dictSort: 0,
  dictSource: 2,  // 1-系统级 2-自定义
  status: 1,
})
const rules = {
  dictLabel: [{required: true, message: '请输入字典标签', trigger: 'blur'}],
  dictValue: [{required: true, message: '请输入字典值', trigger: 'blur'}],
}

const resetForm = () => {
  formData.id = null
  formData.dictType = props.dictType || ''
  formData.dictLabel = ''
  formData.dictValue = ''
  formData.dictSort = 0
  formData.dictSource = 2
  formData.status = 1
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
      res = await updateDictData(formData)
    } else {
      formData.dictType = props.dictType
      res = await createDictData(formData)
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
