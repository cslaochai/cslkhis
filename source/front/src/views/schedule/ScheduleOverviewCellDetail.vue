<template>
  <el-dialog
      v-model="visible"
      destroy-on-close
      title="当日排班名单"
      top="7vh"
      width="1080px"
  >
    <!-- 头部：这一格是「哪个单元、哪一天、在岗多少」，与矩阵格子显示的数字对得上 -->
    <div class="mb-3 flex flex-wrap items-center gap-2 rounded-md border border-slate-200 bg-slate-50 px-3 py-2">
      <span class="text-sm font-semibold text-slate-800" data-testid="cell-org-name">{{ orgName || '—' }}</span>
      <span class="text-xs text-slate-400">{{ orgUnitTypeText(orgType) }}</span>
      <el-tag effect="plain" size="small" type="info">{{ date || '—' }}</el-tag>
      <span class="ml-auto text-sm text-slate-600" data-testid="cell-working-count">当日在岗 {{ workingCount }} 人次</span>
      <span class="text-xs text-slate-400" data-testid="cell-row-count">排班记录 {{ rows.length }} 条</span>
    </div>

    <!-- 缺口排在名单上方：先回答「缺在哪个班」，再回答「这个班都有谁」 -->
    <div v-if="cellShortfalls.length" class="mb-3 rounded-md border border-red-200 bg-red-50 px-3 py-2"
         data-testid="cell-shortfall-block">
      <p class="mb-1 text-xs font-semibold text-red-700">该单元当日人力缺口</p>
      <ul class="space-y-1">
        <li v-for="(s, i) in cellShortfalls" :key="i" class="flex flex-wrap items-center gap-x-3 text-xs text-red-700">
          <span class="font-medium">{{ s.shiftName }}</span>
          <span>{{ s.staffTypeName }}</span>
          <span>应到 {{ s.minStaff }} / 实到 {{ s.actualCount }}</span>
          <span class="font-semibold">缺 {{ s.shortfall }} 人</span>
        </li>
      </ul>
    </div>

    <el-table v-loading="loading" :data="rows" border empty-text="当日该单元没有排班记录" max-height="440" size="small">
      <el-table-column label="工号" prop="empCode" width="100"/>
      <el-table-column label="姓名" min-width="90" prop="employeeName"/>
      <el-table-column label="岗位" prop="staffTypeName" width="90"/>
      <el-table-column label="班次" min-width="150">
        <template #default="{ row }">
          <span>{{ row.shiftName || '未分类' }}</span>
          <span v-if="row.startTime" class="ml-1 text-xs text-slate-400">{{ shiftTimeText(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="出勤" width="80">
        <template #default="{ row }">
          <el-tag :type="row.dutyStatus === DUTY_STATUS_WORK ? 'success' : 'info'" effect="plain" size="small">
            {{ row.dutyStatusText }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="响应形态" prop="attendModeText" width="100"/>
      <el-table-column align="center" label="出诊" width="70">
        <template #default="{ row }">{{ Number(row.clinicFlag) === 1 ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column align="center" label="工时" prop="workHours" width="70"/>
      <el-table-column label="来源" prop="scheduleSourceText" width="90"/>
      <el-table-column label="备注" min-width="120" prop="remark" show-overflow-tooltip/>
    </el-table>

    <template #footer>
      <div class="flex items-center justify-between">
        <span class="text-xs text-slate-400">此处只读；调整当日排班请到岗位排班页</span>
        <el-button data-testid="cell-close-btn" @click="visible = false">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue'
import {getStaffScheduleListPage} from '@/api/staffSchedule'
import {DUTY_STATUS_WORK, orgUnitTypeText} from '@/lib/staffSchedule'

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 排班单元类型（1-科室 2-病区 3-全院）
  orgType: {type: Number, default: null},
  // 排班单元ID（雪花ID，务必传字符串，避免精度丢失）
  orgId: {type: [String, Number], default: null},
  // 单元名称（父页面已持有的矩阵行数据，只为弹框刚打开时不闪空白）
  orgName: {type: String, default: ''},
  // 排班日期 yyyy-MM-dd
  date: {type: String, default: ''},
  // 本周缺口清单（父页面已取到，这里按单元+日期筛出当日那几条）
  shortfalls: {type: Array, default: () => []},
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const rows = ref([])
const loading = ref(false)

const cellShortfalls = computed(() =>
    props.shortfalls.filter((s) => s.scheduleDate === props.date
        && String(s.orgId) === String(props.orgId)
        && Number(s.orgType) === Number(props.orgType)))

const workingCount = computed(() => rows.value.filter((r) => Number(r.dutyStatus) === DUTY_STATUS_WORK).length)

function shiftTimeText(row) {
  if (!row.startTime || !row.endTime) return ''
  return `${row.startTime}~${row.crossDay ? '次日' : ''}${row.endTime}`
}

/**
 * 取该单元该日的全部排班记录。
 * 用于排班总览点格子下钻：只按「单元 + 单日」捞一次，当日名单不会超过一页。
 */
async function load() {
  if (!props.date || props.orgId === null || props.orgId === undefined) return
  loading.value = true
  try {
    const res = await getStaffScheduleListPage({
      orgType: props.orgType,
      orgId: props.orgId,
      startDate: props.date,
      endDate: props.date,
      pageNum: 1,
      // 一次性抓当日名单（一个单元一天不可能有几百条排班），不是分页查询，故不进 lib/pagination 口径
      pageSize: 50,
    })
    rows.value = res.code === 200 ? (res.data?.records || []) : []
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    rows.value = []
  } finally {
    loading.value = false
  }
}

// 连续点不同格子时父页面只换查询条件、不会先关再开，所以身份变了必须重取
watch(
    () => [props.modelValue, props.orgId, props.orgType, props.date],
    ([visibleNow]) => {
      if (visibleNow) load()
      else rows.value = []
    }
)
</script>
