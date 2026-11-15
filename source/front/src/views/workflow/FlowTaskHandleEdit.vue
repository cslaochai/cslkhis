<template>
  <el-dialog
      :model-value="modelValue"
      :title="isPass ? '审批通过' : '审批驳回'"
      width="520px"
      @update:model-value="emit('update:modelValue', $event)"
  >
    <el-form :model="form" label-width="80px">
      <el-form-item label="流程">
        <span class="text-sm text-gray-700">{{ task?.flowName }} / {{ task?.nodeName }}</span>
      </el-form-item>
      <el-form-item label="业务单据">
        <span class="text-sm text-gray-700">{{ task?.businessId || '无' }}</span>
      </el-form-item>
      <el-form-item label="意见">
        <el-input
            v-model="form.message"
            :rows="3"
            maxlength="200"
            :placeholder="isPass ? '通过意见（可不填）' : '驳回原因（建议写清楚，便于整改）'"
            show-word-limit
            type="textarea"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button :loading="saving" :type="isPass ? 'primary' : 'danger'" @click="handleSubmit">
        {{ isPass ? '确认通过' : '确认驳回' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {flowApprove, flowReject} from '@/api/workflow'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  mode: {type: String, default: 'PASS'},
  task: {type: Object, default: null},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const form = reactive({message: ''})
const saving = ref(false)
const isPass = computed(() => props.mode === 'PASS')

watch(() => props.modelValue, (visible) => {
  if (visible) form.message = ''
})

const handleSubmit = async () => {
  if (!props.task?.taskId) {
    ElMessage.warning('待办不存在')
    return
  }
  saving.value = true
  try {
    const payload = {taskId: props.task.taskId, message: form.message}
    const res = isPass.value ? await flowApprove(payload) : await flowReject(payload)
    if (res.code === 200) {
      ElMessage.success(isPass.value ? '已通过' : '已驳回，流程终止')
      emit('update:modelValue', false)
      emit('saved')
    }
  } finally {
    saving.value = false
  }
}
</script>
