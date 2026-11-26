<template>
  <el-dialog v-model="visible" title="新增科室科目对应" width="560px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="科室">
        <div class="flex items-center gap-2">
          <span>{{ dept?.deptName || '—' }}</span>
          <el-tag v-if="dept?.deptCode" size="small" type="info">{{ dept.deptCode }}</el-tag>
          <el-tag v-if="dept?.parentName" size="small">{{ dept.parentName }}</el-tag>
        </div>
      </el-form-item>
      <el-form-item label="诊疗科目" prop="subjectCode">
        <el-select
            v-model="form.subjectCode"
            data-testid="dept-subject-select"
            filterable
            placeholder="选择要添加的诊疗科目"
            style="width: 100%"
        >
          <el-option-group
              v-for="group in groups"
              :key="group.code"
              :label="`${group.code} ${group.name}`"
          >
            <el-option
                v-for="s in group.children"
                :key="s.subjectCode"
                :label="`${s.subjectCode} ${s.subjectName}`"
                :value="s.subjectCode"
            />
            <el-option
                v-if="!group.children.length"
                :key="group.code"
                :label="`${group.code} ${group.name}（一级科目）`"
                :value="group.code"
            />
          </el-option-group>
        </el-select>
      </el-form-item>
      <el-form-item label="主科目">
        <el-checkbox v-model="form.isPrimary">设为主科目（病案首页取这个）</el-checkbox>
        <div class="text-[12px] text-slate-500">
          勾选后会把该科室原来的主科目替换掉；一个科室只能有一个主科目。
        </div>
      </el-form-item>
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
import {saveDeptSubjectMap} from '@/api/system'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 当前科室（含 deptId/deptName/deptCode/parentName）
  dept: {type: Object, default: null},
  // 科目树按一级分组后的下拉数据
  groups: {type: Array, default: () => []},
  // 该科室已有映射，提交时整单带上
  subjects: {type: Array, default: () => []},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const formRef = ref(null)
const submitLoading = ref(false)
const form = reactive({subjectCode: '', isPrimary: false})

const rules = {
  subjectCode: [{required: true, message: '请选择诊疗科目', trigger: 'change'}],
}

watch(() => props.modelValue, (open) => {
  if (!open) return
  form.subjectCode = ''
  // 科室还没有主科目时默认勾上，避免上报端取不到值
  form.isPrimary = !props.subjects.some(s => s.isPrimary === 1)
  formRef.value?.clearValidate()
})

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!props.dept) {
    ElMessage.warning('请先选择科室')
    return
  }
  const code = form.subjectCode
  if (props.subjects.some(s => s.subjectCode === code)) {
    ElMessage.warning('该科目已在这个科室的清单里')
    return
  }
  const items = props.subjects.map(s => ({subjectCode: s.subjectCode, isPrimary: form.isPrimary ? 0 : s.isPrimary}))
  items.push({subjectCode: code, isPrimary: form.isPrimary ? 1 : 0})

  submitLoading.value = true
  try {
    const res = await saveDeptSubjectMap(props.dept.id ?? props.dept.deptId, items)
    if (res.code === 200) {
      ElMessage.success(res.msg || '保存成功')
      visible.value = false
      emit('saved')
    }
  } catch (error) /* 后端业务异常已由全局拦截器提示，这里不重复弹窗 */ {
  } finally {
    submitLoading.value = false
  }
}
</script>
