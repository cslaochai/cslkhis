<template>
  <el-dialog
    :model-value="modelValue"
    :title="row ? '编辑规则' : '新增规则'"
    width="560px"
    append-to-body
    @update:model-value="(v) => emit('update:modelValue', v)"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="规则编码" prop="ruleCode">
        <el-input v-model="form.ruleCode" placeholder="如 RULE_DUP_CHARGE" :disabled="!!row" />
      </el-form-item>
      <el-form-item label="规则名称" prop="ruleName">
        <el-input v-model="form.ruleName" placeholder="如 重复收费" />
      </el-form-item>
      <el-form-item label="规则类型" prop="ruleType">
        <el-select v-model="form.ruleType" style="width:100%">
          <el-option v-for="(v, k) in RULE_TYPES" :key="k" :label="v" :value="k" />
        </el-select>
      </el-form-item>
      <el-form-item label="稽核对象" prop="target">
        <el-radio-group v-model="form.target">
          <el-radio :value="1">经办结算单</el-radio>
          <el-radio :value="2">机构报盘</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="违规级别" prop="severity">
        <el-radio-group v-model="form.severity">
          <el-radio :value="1">一般</el-radio>
          <el-radio :value="2">较重</el-radio>
          <el-radio :value="3">严重</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="规则参数">
        <el-input v-model="form.params" placeholder='JSON，如 {"minAmount":200}' />
      </el-form-item>
      <el-form-item label="规则说明">
        <el-input v-model="form.description" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
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
import { upsertRule } from '@/api/supervise'
import { RULE_TYPES } from './ruleMeta'

const props = defineProps({
  modelValue: Boolean,
  row: Object,
})
const emit = defineEmits(['update:modelValue', 'saved'])

const formRef = ref()
const saving = ref(false)
const form = reactive({
  id: null, ruleCode: '', ruleName: '', ruleType: '', target: 1, severity: 2,
  params: '', description: '', status: 1,
})
const rules = {
  ruleCode: [{ required: true, message: '请输入规则编码', trigger: 'blur' }],
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  target: [{ required: true, message: '请选择稽核对象', trigger: 'change' }],
  severity: [{ required: true, message: '请选择违规级别', trigger: 'change' }],
}

watch(
  () => [props.modelValue, props.row],
  () => {
    if (!props.modelValue) return
    const r = props.row
    Object.assign(form, {
      id: r?.id || null,
      ruleCode: r?.ruleCode || '',
      ruleName: r?.ruleName || '',
      ruleType: r?.ruleType || '',
      target: r?.target ?? 1,
      severity: r?.severity ?? 2,
      params: r?.params || '',
      description: r?.description || '',
      status: r?.status ?? 1,
    })
    formRef.value?.clearValidate()
  },
  { immediate: true }
)

async function save() {
  await formRef.value.validate()
  saving.value = true
  try {
    await upsertRule({ ...form })
    ElMessage.success('保存成功')
    emit('update:modelValue', false)
    emit('saved')
  } finally {
    saving.value = false
  }
}
</script>
