<template>
  <el-dialog
    :model-value="modelValue"
    title="追回单详情"
    width="600px"
    append-to-body
    @update:model-value="(v) => emit('update:modelValue', v)"
  >
    <div v-if="detail" class="space-y-3">
      <div class="grid grid-cols-2 gap-x-6 gap-y-2 text-[13px]">
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">追回单号</span><span>{{ detail.recoverNo }}</span></div>
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">来源</span><span>{{ detail.sourceType === 2 ? '飞检' : '疑点稽核' }}</span></div>
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">来源单号</span><span>{{ detail.sourceNo }}</span></div>
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">所属账期</span><span>{{ detail.month || '-' }}</span></div>
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">定点机构</span><span>{{ detail.targetOrgName }}</span></div>
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">状态</span><el-tag size="small" :type="statusType(detail.status)">{{ detail.status }}</el-tag></div>
      </div>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="追回金额">{{ fmt(detail.recoverAmount) }} 元</el-descriptions-item>
        <el-descriptions-item label="追回原因">{{ detail.reason }}</el-descriptions-item>
        <el-descriptions-item label="追回信息">{{ detail.handler || '-' }} {{ detail.recoverTime || '' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </div>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { getRecover } from '@/api/supervise'

const props = defineProps({
  modelValue: Boolean,
  id: [String, Number],
})
const emit = defineEmits(['update:modelValue', 'saved'])

const detail = ref(null)

function fmt(n) {
  return (n ?? 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
function statusType(s) {
  return { 待追回: 'danger', 已追回: 'success', 已核销: 'info' }[s] || 'info'
}

watch(
  () => [props.modelValue, props.id],
  async () => {
    if (!props.modelValue || !props.id) return
    const res = await getRecover(props.id)
    detail.value = res.data
  }
)
</script>
