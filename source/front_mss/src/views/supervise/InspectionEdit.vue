<template>
  <el-dialog
    :model-value="modelValue"
    :title="row ? '编辑飞检任务' : '新增飞检任务'"
    width="600px"
    append-to-body
    @update:model-value="(v) => emit('update:modelValue', v)"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="任务名称" prop="title">
        <el-input v-model="form.title" placeholder="如 2026 年第三季度专项飞检" />
      </el-form-item>
      <el-form-item label="检查类型" prop="inspectType">
        <el-select v-model="form.inspectType" style="width:100%">
          <el-option label="专项检查" value="专项检查" />
          <el-option label="日常巡查" value="日常巡查" />
          <el-option label="举报核查" value="举报核查" />
        </el-select>
      </el-form-item>
      <el-form-item label="被查机构" prop="targetOrg">
        <el-input v-model="form.targetOrg" placeholder="定点医疗机构名称" />
      </el-form-item>
      <el-form-item label="计划起止">
        <el-date-picker v-model="range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始日" end-placeholder="结束日" style="width:100%" />
      </el-form-item>
      <el-form-item label="带队人">
        <el-input v-model="form.leader" placeholder="带队人姓名/科室" />
      </el-form-item>
      <el-form-item label="检查组成员">
        <el-input v-model="form.members" placeholder="多成员用逗号分隔" />
      </el-form-item>
      <el-form-item label="检查范围">
        <el-input v-model="form.scopeDesc" type="textarea" :rows="3" placeholder="抽查账期、重点问题类型等" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="saving" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { upsertInspection } from '@/api/supervise'

const props = defineProps({
  modelValue: Boolean,
  row: Object,
})
const emit = defineEmits(['update:modelValue', 'saved'])

const formRef = ref()
const saving = ref(false)
const form = reactive({
  id: null, title: '', inspectType: '专项检查', targetOrg: '', planStart: '', planEnd: '',
  leader: '', members: '', scopeDesc: '',
})
const range = ref([])
const rules = {
  title: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  inspectType: [{ required: true, message: '请选择检查类型', trigger: 'change' }],
  targetOrg: [{ required: true, message: '请输入被查机构', trigger: 'blur' }],
}

watch(
  () => [props.modelValue, props.row],
  () => {
    if (!props.modelValue) return
    const r = props.row
    Object.assign(form, {
      id: r?.id || null,
      title: r?.title || '',
      inspectType: r?.inspectType || '专项检查',
      targetOrg: r?.targetOrg || '',
      planStart: r?.planStart || '',
      planEnd: r?.planEnd || '',
      leader: r?.leader || '',
      members: r?.members || '',
      scopeDesc: r?.scopeDesc || '',
    })
    range.value = form.planStart && form.planEnd ? [form.planStart, form.planEnd] : []
    formRef.value?.clearValidate()
  },
  { immediate: true }
)

async function save() {
  await formRef.value.validate()
  saving.value = true
  try {
    const payload = { ...form }
    payload.planStart = range.value?.[0] || null
    payload.planEnd = range.value?.[1] || null
    await upsertInspection(payload)
    ElMessage.success('保存成功')
    emit('update:modelValue', false)
    emit('saved')
  } finally {
    saving.value = false
  }
}
</script>
