<template>
  <!-- 新增与编辑共用一张表单（后端同一个 faqUpsert，row 为空走新增） -->
  <el-dialog
      v-model="visible"
      :title="formData.id ? '编辑常见问题' : '新增常见问题'"
      width="720px"
  >
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <el-form-item label="分类编码" prop="categoryCode">
        <el-select
            v-model="formData.categoryCode"
            clearable
            filterable
            placeholder="请选择分类"
            style="width: 100%"
            @change="onCategoryChange"
        >
          <el-option
              v-for="d in props.categoryOptions"
              :key="d.dictValue"
              :label="`${d.dictLabel}（${d.dictValue}）`"
              :value="d.dictValue"
          />
        </el-select>
      </el-form-item>
      <!-- 分类名称随编码从字典自动带出，禁用防止手填导致与字典不一致 -->
      <el-form-item label="分类名称" prop="categoryName">
        <el-input v-model="formData.categoryName" disabled placeholder="随分类自动带出"/>
      </el-form-item>
      <el-form-item label="问题" prop="question">
        <el-input v-model="formData.question" placeholder="患者会怎么问，就怎么写"/>
      </el-form-item>
      <el-form-item label="答案" prop="answer">
        <el-input
            v-model="formData.answer"
            :rows="5"
            placeholder="涉及时间/价格/报销比例的一律写「以现场公示为准 / 请咨询窗口」，不要写死数字"
            type="textarea"
        />
      </el-form-item>
      <el-form-item label="关键词" prop="keywords">
        <el-input
            v-model="formData.keywords"
            placeholder="顿号分隔，务必带口语同义词（如 化验单、约号）"
        />
      </el-form-item>
      <el-form-item label="热门">
        <el-radio-group v-model="formData.hotFlag">
          <el-radio :value="1">热门</el-radio>
          <el-radio :value="0">普通</el-radio>
        </el-radio-group>
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
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="'patient:faq:upsert'" :loading="submitLoading" type="primary" @click="handleSubmit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {faqUpsert, getFaqAdminDetail} from '@/api/patientFaq'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 编辑传入当前行；为空表示新增
  row: {type: Object, default: null},
  // FAQ 分类字典（his_faq_category）：列表页筛选也要用，由页面加载一次下传
  categoryOptions: {type: Array, default: () => []},
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
  categoryCode: '',
  categoryName: '',
  question: '',
  answer: '',
  keywords: '',
  hotFlag: 0,
  status: 1,
  sortOrder: 999,
})

const rules = {
  categoryCode: [{required: true, message: '请输入分类编码', trigger: 'blur'}],
  categoryName: [{required: true, message: '请输入分类名称', trigger: 'blur'}],
  question: [{required: true, message: '请输入问题', trigger: 'blur'}],
  answer: [{required: true, message: '请输入答案', trigger: 'blur'}],
}

// 选中分类后自动带出分类名称（保持与字典一致，避免手填分裂）
const onCategoryChange = (val) => {
  const item = props.categoryOptions.find(d => d.dictValue === val)
  formData.categoryName = item ? item.dictLabel : ''
}

const resetForm = () => {
  formData.id = null
  formData.categoryCode = ''
  formData.categoryName = ''
  formData.question = ''
  formData.answer = ''
  formData.keywords = ''
  formData.hotFlag = 0
  formData.status = 1
  formData.sortOrder = 999
}

watch(visible, async (open) => {
  if (!open) return
  resetForm()
  if (!props.row) return
  try {
    const res = await getFaqAdminDetail(props.row.id)
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
    const res = await faqUpsert({...formData})
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
