<template>
  <el-drawer v-model="visible" :title="title" size="520px">
    <div v-loading="loading" data-testid="ss-change-log">
      <el-empty v-if="list.length === 0" :image-size="60" description="这条排班还没有发生过变更"/>
      <el-timeline v-else>
        <el-timeline-item
            v-for="log in list"
            :key="log.id"
            :timestamp="log.occurTime"
            placement="top"
        >
          <div class="font-medium">
            {{ log.actionTypeText || scheduleChangeTypeText(log.actionType) }}
          </div>
          <div class="text-xs text-gray-500">
            {{ log.fromEmployeeName || '—' }}
            <span v-if="log.toEmployeeName"> → {{ log.toEmployeeName }}</span>
            <span v-if="log.amount"> · 号数 {{ log.amount }}</span>
          </div>
          <div v-if="log.reason" class="text-xs text-gray-500">原因：{{ log.reason }}</div>
          <div v-if="log.createBy" class="text-xs text-gray-400">操作人：{{ log.createBy }}</div>
        </el-timeline-item>
      </el-timeline>
    </div>
  </el-drawer>
</template>

<script setup>
/**
 * 排班变更留痕抽屉（提交审核 / 审核发布 / 驳回 / 撤回 / 换班…都在这条时间线上）。
 *
 * 「全院岗位排班」页与「排班审批中心」页都要看同一条时间线，抽成组件给两处共用，
 * 免得以后加一个动作类型要改两个抽屉。
 */
import {computed, ref, watch} from 'vue'
import {getScheduleChangeLogList} from '@/api/staffSchedule'
import {scheduleChangeTypeText} from '@/lib/staffSchedule'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 排班行主键 */
  staffScheduleId: {type: [String, Number], default: null},
  /** 抽屉标题 */
  title: {type: String, default: '变更留痕'},
})
const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const loading = ref(false)
const list = ref([])

const load = async () => {
  if (!props.staffScheduleId) {
    list.value = []
    return
  }
  loading.value = true
  try {
    const res = await getScheduleChangeLogList(props.staffScheduleId)
    list.value = res.code === 200 ? (res.data || []) : []
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

// 每次打开都重取：审批动作刚做完，留痕必须是最新的
watch(() => props.modelValue, (open) => {
  if (open) load()
})
</script>
