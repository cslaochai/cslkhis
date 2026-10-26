<template>
  <el-dialog v-model="visible" title="听班呼叫详情" width="680px">
    <div v-loading="loading" class="min-h-[200px]">
      <template v-if="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="呼叫单号">{{ detail.callNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detail.status)" size="small">{{ detail.statusText }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="值班">{{ detail.dutyDate }} {{ detail.shiftTypeText }}</el-descriptions-item>
          <el-descriptions-item label="呼叫科室">{{ detail.deptName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="被叫层级">{{ detail.targetLevelText }}</el-descriptions-item>
          <el-descriptions-item label="被叫值班人">
            {{ detail.targetEmpName || '—' }}
            <span v-if="detail.targetPhone" class="ml-2 text-xs text-gray-500">{{ detail.targetPhone }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="患者">{{ detail.patientName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="患者位置">{{ detail.patientLocation || '—' }}</el-descriptions-item>
          <el-descriptions-item label="呼叫时间">{{ detail.callTime }}</el-descriptions-item>
          <el-descriptions-item label="到岗时间">{{ detail.arriveTime || '未到岗' }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.escalateTime" label="升级时间">
            {{ detail.escalateTime }}
            <span class="ml-2 text-xs text-gray-500">
              （原 {{ detail.escalateFromLevelText }} 未到岗）
            </span>
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.cancelReason" label="取消事由">
            {{ detail.cancelReason }}
          </el-descriptions-item>
          <el-descriptions-item label="呼叫事由" :span="2">{{ detail.callReason }}</el-descriptions-item>
        </el-descriptions>

        <div class="mt-4">
          <div class="block-title">呼叫流水</div>
          <el-timeline v-if="(detail.logs || []).length">
            <el-timeline-item
                v-for="log in detail.logs"
                :key="log.id"
                :timestamp="log.actionTime"
                :type="logTypeOf(log.actionType)"
                placement="top"
            >
              <div class="text-sm font-medium">{{ log.actionTypeText }}</div>
              <div class="text-xs text-gray-500">
                {{ log.operatorName || '系统' }}
                <span v-if="log.fromStatusText && log.toStatusText">
                  ：{{ log.fromStatusText }} → {{ log.toStatusText }}
                </span>
              </div>
              <div v-if="log.actionNote" class="mt-1 text-xs text-gray-600">{{ log.actionNote }}</div>
            </el-timeline-item>
          </el-timeline>
          <div v-else class="py-6 text-center text-sm text-gray-400">暂无流水</div>
        </div>
      </template>
    </div>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue'
import {getDutyCallDetail} from '@/api/dutyCall'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: null},
})
const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const loading = ref(false)
const detail = ref(null)

/** 状态标签配色：待到岗橙、已到岗绿、已超时升级红、已取消灰 */
const statusTagType = (status) => {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  if (status === 3) return 'info'
  return 'warning'
}

/** 流水节点配色：升级红、到岗绿、取消灰、呼叫蓝 */
const logTypeOf = (actionType) => {
  if (actionType === 2) return 'success'
  if (actionType === 3) return 'danger'
  if (actionType === 4) return 'info'
  if (actionType === 5) return 'warning'
  return 'primary'
}

const loadDetail = async () => {
  if (!props.id) return
  loading.value = true
  try {
    const res = await getDutyCallDetail(props.id)
    detail.value = res?.data || null
  } catch (e) /* 错误已由全局拦截器统一提示 */{
    detail.value = null
  } finally {
    loading.value = false
  }
}

watch(visible, (open) => {
  if (open) loadDetail()
})
</script>
