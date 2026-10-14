<template>
  <el-dialog
    :model-value="modelValue"
    title="疑点详情"
    width="640px"
    append-to-body
    @update:model-value="(v) => emit('update:modelValue', v)"
  >
    <div v-if="detail" class="space-y-3">
      <div class="grid grid-cols-2 gap-x-6 gap-y-2 text-[13px]">
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">疑点单号</span><span>{{ detail.suspicionNo }}</span></div>
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">命中规则</span><span>{{ detail.ruleName }}</span></div>
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">患者</span><span>{{ detail.patientName }}（{{ detail.idCardMask }}）</span></div>
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">参保类型</span><span>{{ detail.insuranceType }}</span></div>
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">来源</span><span>{{ detail.bizSource === 2 ? '机构报盘' : '经办结算单' }}</span></div>
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">来源单据号</span><span>{{ detail.bizNo }}</span></div>
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">就诊类型</span><span>{{ detail.encounterType }}</span></div>
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">业务日期</span><span>{{ detail.bizDate }}</span></div>
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">科室/医师</span><span>{{ detail.dept }} / {{ detail.doctor }}</span></div>
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">状态</span><el-tag size="small" :type="statusType(detail.status)">{{ detail.status }}</el-tag></div>
      </div>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="疑点涉及金额">{{ fmt(detail.involvedAmount) }} 元</el-descriptions-item>
        <el-descriptions-item label="确认违规金额">{{ fmt(detail.confirmAmount) }} 元</el-descriptions-item>
        <el-descriptions-item label="疑点说明">{{ detail.reason }}</el-descriptions-item>
        <el-descriptions-item label="处置信息">{{ detail.handleUser || '-' }} {{ detail.handleTime || '' }} {{ detail.remark || '' }}</el-descriptions-item>
      </el-descriptions>

      <el-alert
        v-if="detail.status === '待核实'"
        type="warning" :closable="false"
        title="请核实后处置：确认违规将生成追回单，排除则关闭本疑点"
      />
      <el-form v-if="detail.status === '待核实'" :model="form" label-width="90px" class="mt-2">
        <el-form-item label="违规金额">
          <el-input-number v-model="form.amount" :min="0" :precision="2" :controls="false" style="width:100%" />
        </el-form-item>
        <el-form-item label="处置备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="核实结论/排除原因" />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">关闭</el-button>
      <template v-if="detail && detail.status === '待核实'">
        <el-button type="info" :loading="saving" @click="onExclude">排除疑点</el-button>
        <el-button type="danger" :loading="saving" @click="onConfirm">确认违规</el-button>
      </template>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getSuspicion, confirmSuspicion, excludeSuspicion } from '@/api/supervise'

const props = defineProps({
  modelValue: Boolean,
  id: [String, Number],
})
const emit = defineEmits(['update:modelValue', 'saved'])

const detail = ref(null)
const saving = ref(false)
const form = reactive({ amount: 0, remark: '' })

function fmt(n) {
  return (n ?? 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
function statusType(s) {
  return { 待核实: 'warning', 已确认: 'danger', 已排除: 'info' }[s] || 'info'
}

watch(
  () => [props.modelValue, props.id],
  async () => {
    if (!props.modelValue || !props.id) return
    const res = await getSuspicion(props.id)
    detail.value = res.data
    form.amount = res.data?.involvedAmount || 0
    form.remark = ''
  }
)

async function onConfirm() {
  saving.value = true
  try {
    await confirmSuspicion({ id: props.id, amount: form.amount, remark: form.remark })
    ElMessage.success('已确认违规并生成追回单')
    emit('update:modelValue', false)
    emit('saved')
  } finally {
    saving.value = false
  }
}

async function onExclude() {
  saving.value = true
  try {
    await excludeSuspicion({ id: props.id, remark: form.remark })
    ElMessage.info('已排除该疑点')
    emit('update:modelValue', false)
    emit('saved')
  } finally {
    saving.value = false
  }
}
</script>
