<template>
  <el-dialog v-model="visible"
             :title="`导管日明细 — ${monitor?.patientName || ''}（${monitor?.monitorTypeText || ''}）`"
             width="480px">
    <el-table :data="dailyRows" border data-testid="daily-table" max-height="400" size="small">
      <el-table-column label="监测日期" width="120">
        <template #default="{ row }">{{ fmtDate(row.monitorDate) }}</template>
      </el-table-column>
      <el-table-column label="记录人" prop="recorderName" width="100"/>
      <el-table-column label="打卡时间" width="160">
        <template #default="{ row }">{{ fmtTime(row.recordTime) }}</template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" show-overflow-tooltip/>
    </el-table>
    <div style="margin-top:8px">合计导管日：<b data-testid="daily-total">{{ dailyRows.length }}</b> 天</div>
  </el-dialog>
</template>

<script setup>
/**
 * 导管日明细弹窗（自 InfectionMonitorView 拆出）
 *
 * 打开时按监测ID自取 getMonitorDailyList；monitor 行快照只用于标题（患者/类型文案）。
 */
import {computed, ref, watch} from 'vue'
import {getMonitorDailyList} from '@/api/infectionMonitor'

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 监测行快照（取 patientName/monitorTypeText 拼标题，id 用于自取明细）
  monitor: {type: Object, default: null},
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '—')
const fmtDate = (d) => (d ? String(d).slice(0, 10) : '—')

const dailyRows = ref([])

// 打开时按 id 自取明细（与拆分前 onShowDaily 的取数口径一致）
watch(() => props.modelValue, async (v) => {
  if (!v) return
  dailyRows.value = []
  if (!props.monitor?.id) return
  try {
    const res = await getMonitorDailyList(props.monitor.id)
    if (res.code === 200) dailyRows.value = res.data || []
  } catch (e) {
    console.error(e)
  }
})
</script>
