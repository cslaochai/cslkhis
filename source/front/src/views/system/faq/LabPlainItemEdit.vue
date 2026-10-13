<template>
  <!-- 新增与编辑共用一张表单（后端同一个 labPlainUpsert，row 为空走新增） -->
  <el-dialog v-model="visible" :title="formData.id ? '编辑白话词条' : '新增白话词条'" width="720px">
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
      <el-button v-perm="'lab:plain:upsert'" :loading="submitLoading" type="primary" @click="handleSubmit">
        确定
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {getLabPlainDetail, labPlainUpsert} from '@/api/labPlain'

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

watch(visible, async (open) => {
  if (!open) return
  resetForm()
  if (!props.row) return
  try {
    const res = await getLabPlainDetail(props.row.id)
    if (res.code === 200) {
      Object.assign(formData, res.data)
    }
  } catch (error) {
    ElMessage.error(error.message || '获取详情失败')
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
    const res = await labPlainUpsert({...formData})
    if (res.code === 200) {
      ElMessage.success(formData.id ? '修改成功' : '新增成功')
      visible.value = false
      emit('saved')
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}
</script>
