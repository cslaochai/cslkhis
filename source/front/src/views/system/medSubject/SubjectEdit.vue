<template>
  <!-- 新增与编辑共用一张表单（后端同一个 upsert，id 为空走新增） -->
  <el-dialog v-model="visible" :title="title" width="620px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="110px">
      <el-form-item label="科目代码" prop="subjectCode">
        <div class="flex w-full items-center gap-2">
          <span v-if="formData.subjectLevel === 2" class="text-slate-500">{{ formData.parentCode }}.</span>
          <el-input v-model="codeTail" data-testid="subject-code" class="!w-40" placeholder="两位数字"/>
        </div>
        <div class="mt-1 text-[12px] text-slate-500">
          一级科目两位数字（如 03 内科）；二级科目两位序号（如 01 → 03.01 呼吸内科专业）。
          代码要与国家卫健委《医疗机构诊疗科目名录》一致，上报端按这个码对账。
        </div>
      </el-form-item>
      <el-form-item label="科目名称" prop="subjectName">
        <el-input v-model="formData.subjectName" data-testid="subject-name" placeholder="如：呼吸内科专业"/>
      </el-form-item>
      <el-form-item label="科目层级">
        <el-radio-group v-model="formData.subjectLevel" :disabled="levelLocked">
          <el-radio :value="1">一级科目</el-radio>
          <el-radio :value="2">二级科目</el-radio>
        </el-radio-group>
        <div v-if="levelLocked" class="mt-1 text-[12px] text-slate-500">
          该科目下还有二级科目，层级不可改（改了那批二级科目会变成孤儿）。
        </div>
      </el-form-item>
      <el-form-item label="是否临床">
        <el-radio-group v-model="formData.isClinical">
          <el-radio :value="1">临床科目</el-radio>
          <el-radio :value="0">医技/平台科目</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="排序号">
        <el-input-number v-model="formData.sortOrder" :min="0" class="!w-32" controls-position="right"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" :rows="2" placeholder="如：按本院执业许可证副本范围设置" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'org:medSubject:add'" :loading="submitLoading" type="primary" @click="handleSubmit">确定</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {saveMedSubject} from '@/api/system'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 编辑传当前科目节点；为空表示新增
  row: {type: Object, default: null},
  // 新增下级时传父科目（一级科目节点）
  parentSubject: {type: Object, default: null},
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
  subjectCode: '',
  subjectName: '',
  parentCode: null,
  subjectLevel: 1,
  isClinical: 1,
  status: 1,
  sortOrder: 0,
  remark: '',
})

// 二级科目代码只让用户填「点后面两位」，前缀由上级科目锁定，避免手抖把 03.01 打成 04.01
const codeTail = ref('')
const levelLocked = ref(false)

const title = computed(() => {
  if (formData.id) return '编辑诊疗科目'
  return formData.subjectLevel === 2 ? '新增二级科目' : '新增一级科目'
})

const rules = {
  subjectName: [{required: true, message: '请输入科目名称', trigger: 'blur'}],
}

const resetForm = () => {
  formData.id = null
  formData.subjectCode = ''
  formData.subjectName = ''
  formData.parentCode = null
  formData.subjectLevel = 1
  formData.isClinical = 1
  formData.status = 1
  formData.sortOrder = 0
  formData.remark = ''
  codeTail.value = ''
  levelLocked.value = false
}

watch(visible, (open) => {
  if (!open) return
  resetForm()
  if (props.row) {
    const r = props.row
    formData.id = r.id
    formData.subjectCode = r.subjectCode
    formData.subjectName = r.subjectName
    formData.parentCode = r.parentCode || null
    formData.subjectLevel = r.subjectLevel || 1
    formData.isClinical = r.isClinical ?? 1
    formData.status = r.status ?? 1
    formData.sortOrder = r.sortOrder ?? 0
    formData.remark = r.remark || ''
    codeTail.value = formData.subjectLevel === 2 && r.parentCode
        ? r.subjectCode.slice(r.parentCode.length + 1)
        : r.subjectCode
    // 有下级的科目不许改成二级，否则那批二级会失去归属
    levelLocked.value = Array.isArray(r.children) && r.children.length > 0
    return
  }
  if (props.parentSubject) {
    formData.subjectLevel = 2
    formData.parentCode = props.parentSubject.subjectCode
    formData.isClinical = props.parentSubject.isClinical ?? 1
    formData.sortOrder = 0
  }
})

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  const tail = (codeTail.value || '').trim()
  if (!/^\d{2}$/.test(tail)) {
    ElMessage.error('科目代码要两位数字，如 03 或 01')
    return
  }
  const subjectCode = formData.subjectLevel === 2
      ? `${formData.parentCode}.${tail}`
      : tail

  submitLoading.value = true
  try {
    const res = await saveMedSubject({
      id: formData.id,
      subjectCode,
      subjectName: formData.subjectName,
      parentCode: formData.subjectLevel === 2 ? formData.parentCode : null,
      subjectLevel: formData.subjectLevel,
      isClinical: formData.isClinical,
      status: formData.status,
      sortOrder: formData.sortOrder,
      remark: formData.remark,
    })
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
