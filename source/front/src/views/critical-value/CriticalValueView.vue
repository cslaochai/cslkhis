<script setup lang="ts">
/**
 * 危急值管理
 *
 * 数据全部来自后台：检验结果录入时由确定性硬规则（LabCriticalValueRules）自动识别，
 * 上报的同时给开单医生发站内信。这个页面只做「查询 + 闭环流转」，不重新判定。
 *
 * 两个容易误解的口径：
 * 1. 「超时」不是数据库里的一个状态，而是查询时用 deadline_time 与当前时间比出来的
 *    （后端字段 overdue）。所以它会随时间自然变化，不会出现状态腐烂。
 * 2. 「未接收直接处置」是允许的 —— 医生往往先处理后补记录，后端会自动补上接收人。
 *    所以处置按钮不要求先接收。
 */
import { ref, computed, onMounted } from 'vue'
import { Search, Refresh, Bell, Clock, View, Check, Warning, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'
import {
  getCriticalValueListPage,
  getCriticalValueDetail,
  getCriticalValueStats,
  receiveCriticalValue,
  handleCriticalValue,
} from '@/api/medicaltech'

interface CriticalValueRow {
  id: number | string
  criticalNo: string
  recordNo?: string
  patientName?: string
  genderText?: string
  age?: number
  itemName?: string
  resultText?: string
  referenceRange?: string
  criticalType?: number
  criticalTypeText?: string
  thresholdText?: string
  criticalDesc?: string
  reportDeptName?: string
  reportBy?: string
  reportTime?: string
  deadlineTime?: string
  overdue?: boolean
  notifyStatus?: number
  status?: number
  statusText?: string
  receiveBy?: string
  receiveTime?: string
  handleBy?: string
  handleTime?: string
  handleMeasure?: string
  source?: string
}

const loading = ref(false)
const rows = ref<CriticalValueRow[]>([])

const query = ref({
  keyword: '',
  status: null as number | null,
  criticalType: null as number | null,
  overdueOnly: false,
})

const pagination = ref({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0 })
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

const stats = ref({
  monthTotal: 0,
  pending: 0,
  received: 0,
  handled: 0,
  overdue: 0,
  timelyRate: null as number | null,
})

const statusMap: Record<number, { label: string; type: 'danger' | 'warning' | 'success' | 'info' }> = {
  1: { label: '待接收', type: 'danger' },
  2: { label: '已接收', type: 'warning' },
  3: { label: '已处置', type: 'success' },
  4: { label: '已作废', type: 'info' },
}

const statCards = computed(() => [
  { label: '本月危急值', value: stats.value.monthTotal, hint: '次', color: 'text-blue-600', bg: 'bg-blue-50', icon: Bell },
  { label: '待接收', value: stats.value.pending, hint: '次', color: 'text-red-600', bg: 'bg-red-50', icon: Warning },
  { label: '已接收待处置', value: stats.value.received, hint: '次', color: 'text-amber-600', bg: 'bg-amber-50', icon: Clock },
  { label: '超时未处置', value: stats.value.overdue, hint: '次', color: 'text-purple-600', bg: 'bg-purple-50', icon: Clock },
])

const fmtTime = (v?: string) => (v ? String(v).replace('T', ' ').slice(0, 16) : '—')

const loadStats = async () => {
  try {
    const res = await getCriticalValueStats()
    if (res.data) {
      stats.value = { ...stats.value, ...res.data }
    }
  } catch (error: any) {
    console.error('加载危急值统计失败:', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCriticalValueListPage({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      keyword: query.value.keyword || undefined,
      status: query.value.status ?? undefined,
      criticalType: query.value.criticalType ?? undefined,
      overdueOnly: query.value.overdueOnly || undefined,
    })
    rows.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载危急值列表失败')
  } finally {
    loading.value = false
  }
}

const reload = async () => {
  await Promise.all([loadData(), loadStats()])
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  reload()
}

const handleReset = () => {
  query.value = { keyword: '', status: null, criticalType: null, overdueOnly: false }
  pagination.value.pageNum = 1
  reload()
}

const handleSizeChange = (val: number) => {
  pagination.value.pageSize = val
  pagination.value.pageNum = 1
  loadData()
}

const handleCurrentChange = (val: number) => {
  pagination.value.pageNum = val
  loadData()
}

// ---------------- 详情 ----------------

const showDetail = ref(false)
const detailLoading = ref(false)
const detail = ref<CriticalValueRow | null>(null)

const openDetail = async (row: CriticalValueRow) => {
  showDetail.value = true
  detailLoading.value = true
  detail.value = row
  try {
    const res = await getCriticalValueDetail(row.id as any)
    if (res.data) {
      detail.value = res.data
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载危急值详情失败')
  } finally {
    detailLoading.value = false
  }
}

// ---------------- 闭环流转 ----------------

const handleReceive = async (row: CriticalValueRow) => {
  try {
    await ElMessageBox.confirm(
      `确认接收 ${row.patientName || ''} 的危急值「${row.itemName || ''}」？`,
      '确认接收',
      { confirmButtonText: '确认接收', cancelButtonText: '取消', type: 'warning' },
    )
    await receiveCriticalValue(row.id as any)
    ElMessage.success('已确认接收')
    await reload()
    if (showDetail.value && detail.value) {
      const res = await getCriticalValueDetail(row.id as any)
      detail.value = res.data || detail.value
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '接收失败')
    }
  }
}

const showHandleDialog = ref(false)
const handleTarget = ref<CriticalValueRow | null>(null)
const handleMeasure = ref('')
const handling = ref(false)

const openHandle = (row: CriticalValueRow) => {
  handleTarget.value = row
  handleMeasure.value = ''
  showHandleDialog.value = true
}

const submitHandle = async () => {
  if (!handleTarget.value) return
  if (!handleMeasure.value.trim()) {
    ElMessage.warning('请填写处置措施')
    return
  }
  handling.value = true
  try {
    await handleCriticalValue(handleTarget.value.id as any, handleMeasure.value.trim())
    ElMessage.success('处置已记录')
    showHandleDialog.value = false
    if (showDetail.value) {
      showDetail.value = false
    }
    await reload()
  } catch (error: any) {
    ElMessage.error(error.message || '处置失败')
  } finally {
    handling.value = false
  }
}

/** 未处置且未作废才允许操作 */
const canOperate = (row: CriticalValueRow) => row.status === 1 || row.status === 2

onMounted(() => {
  reload()
})
</script>

<template>
  <div>
    <!-- 页头 -->
    <div class="mb-3 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-xl font-semibold text-slate-900">危急值管理</h1>
        <p class="mt-1 text-sm text-slate-500">
          检验结果录入时自动识别并通知开单医生，上报 → 接收 → 处置 → 记录全闭环，超时在列表实时标记
        </p>
      </div>
      <div class="flex items-center gap-3">
        <el-tag v-if="stats.timelyRate != null" effect="plain" type="success">
          及时处置率 {{ stats.timelyRate }}%
        </el-tag>
        <el-button :icon="Refresh" @click="reload">刷新</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="mb-3 grid grid-cols-2 gap-4 lg:grid-cols-4">
      <div
        v-for="s in statCards"
        :key="s.label"
        class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
      >
        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg" :class="s.bg">
          <el-icon class="h-5 w-5" :class="s.color"><component :is="s.icon" /></el-icon>
        </div>
        <div class="min-w-0">
          <p class="truncate text-xs text-slate-500">{{ s.label }}</p>
          <p class="text-lg font-bold text-slate-900">{{ s.value }}</p>
          <p class="truncate text-[11px] text-slate-400">{{ s.hint }}</p>
        </div>
      </div>
    </div>

    <!-- 筛选 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input
            v-model="query.keyword"
            placeholder="搜索危急值号/患者/项目..."
            :prefix-icon="Search"
            class="!w-64"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="闭环状态">
          <el-select v-model="query.status" placeholder="闭环状态" clearable class="!w-32">
            <el-option v-for="(v, k) in statusMap" :key="k" :label="v.label" :value="Number(k)" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.criticalType" placeholder="类型" clearable class="!w-28">
            <el-option label="偏低" :value="1" />
            <el-option label="偏高" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="query.overdueOnly" label="只看超时未处置" border />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card v-loading="loading" class="table-card" shadow="never">
      <el-table :data="rows" stripe :max-height="tableMaxHeight">
        <el-table-column prop="criticalNo" label="危急值号" width="200" />
        <el-table-column label="上报时间" width="150">
          <template #default="{ row }">
            <span class="text-slate-600">{{ fmtTime(row.reportTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="患者" width="150">
          <template #default="{ row }">
            <div class="text-sm font-medium text-slate-800">{{ row.patientName || '—' }}</div>
            <div class="text-slate-400">
              {{ row.genderText || '' }}<span v-if="row.age"> {{ row.age }}岁</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="危急值项目与结果" min-width="230">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-tag :type="row.criticalType === 2 ? 'danger' : 'warning'" size="small" effect="plain">
                {{ row.criticalTypeText || (row.criticalType === 2 ? '偏高' : '偏低') }}
              </el-tag>
              <!-- 项目名必须显示：只给「45 g/L ↓」谁也判断不出这是哪一项 -->
              <span class="text-sm font-medium text-slate-800">{{ row.itemName || '—' }}</span>
            </div>
            <div class="mt-0.5 text-sm font-semibold text-red-600">{{ row.resultText || row.resultValue }}</div>
            <div class="text-slate-400">
              阈值 {{ row.thresholdText || '—' }}
              <span v-if="row.referenceRange"> · 参考 {{ row.referenceRange }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="处置时限" width="170">
          <template #default="{ row }">
            <div class="text-slate-600">{{ fmtTime(row.deadlineTime) }}</div>
            <el-tag v-if="row.overdue" type="danger" size="small" effect="dark" class="mt-0.5">已超时</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="通知" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.notifyStatus === 1" type="success" size="small" effect="plain">已通知</el-tag>
            <el-tag v-else type="info" size="small" effect="plain">未通知</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="闭环状态" width="150">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status || 1]?.type || 'info'" size="small">
              {{ row.statusText || statusMap[row.status || 1]?.label }}
            </el-tag>
            <div v-if="row.receiveBy" class="mt-0.5 text-slate-400">接收 {{ row.receiveBy }}</div>
            <div v-if="row.handleBy" class="text-slate-400">处置 {{ row.handleBy }}</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDetail(row)">
              <el-icon class="mr-0.5"><View /></el-icon>详情
            </el-button>
            <el-button
              v-if="row.status === 1"
              v-perm="'medtech:criticalValue:edit'"
              type="warning" link size="small"
              @click="handleReceive(row)"
            >
              <el-icon class="mr-0.5"><Check /></el-icon>接收
            </el-button>
            <el-button
              v-if="canOperate(row)"
              v-perm="'medtech:criticalValue:edit'"
              type="success" link size="small"
              @click="openHandle(row)"
            >
              处置
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="PAGE_SIZES"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情 -->
    <el-dialog v-model="showDetail" :title="`危急值详情 - ${detail?.criticalNo || ''}`" width="680px" destroy-on-close>
      <div v-if="detail" v-loading="detailLoading" class="space-y-4">
        <div
          class="rounded-lg border p-4"
          :class="detail.overdue ? 'border-red-300 bg-red-50' : 'border-slate-200'"
        >
          <div class="flex items-center gap-2">
            <el-icon class="text-red-600"><Warning /></el-icon>
            <span class="text-sm font-semibold text-slate-800">{{ detail.criticalDesc }}</span>
          </div>
          <div v-if="detail.overdue" class="mt-1 text-xs text-red-600">
            已超过处置时限 {{ fmtTime(detail.deadlineTime) }}，请立即处置
          </div>
        </div>

        <div class="grid grid-cols-2 gap-4 rounded-lg border border-slate-200 p-4">
          <div><p class="text-xs text-slate-400">患者</p>
            <p class="text-sm font-medium">{{ detail.patientName || '—' }} {{ detail.genderText || '' }} {{ detail.age ? detail.age + '岁' : '' }}</p></div>
          <div><p class="text-xs text-slate-400">来源</p>
            <p class="text-sm font-medium">
              {{ detail.source === 'RULE' ? '系统自动识别（硬规则）' : detail.source || '—' }}
            </p></div>
          <div><p class="text-xs text-slate-400">检验项目</p>
            <p class="text-sm font-medium">{{ detail.itemName || '—' }}</p></div>
          <div><p class="text-xs text-slate-400">参考区间</p>
            <p class="text-sm font-medium">{{ detail.referenceRange || '—' }}</p></div>
          <div><p class="text-xs text-slate-400">报告科室</p>
            <p class="text-sm font-medium">{{ detail.reportDeptName || '—' }}</p></div>
          <div><p class="text-xs text-slate-400">报告人</p>
            <p class="text-sm font-medium">{{ detail.reportBy || '—' }}</p></div>
          <div><p class="text-xs text-slate-400">上报时间</p>
            <p class="text-sm font-medium">{{ fmtTime(detail.reportTime) }}</p></div>
          <div><p class="text-xs text-slate-400">处置时限</p>
            <p class="text-sm font-medium">{{ fmtTime(detail.deadlineTime) }}</p></div>
        </div>

        <!-- 闭环轨迹 -->
        <div class="rounded-lg border border-slate-200 p-4">
          <p class="mb-3 text-sm font-medium text-slate-700">闭环轨迹</p>
          <div class="space-y-3">
            <div class="flex items-start gap-3">
              <el-icon class="mt-0.5 text-blue-500"><Bell /></el-icon>
              <div>
                <p class="text-sm text-slate-700">已上报并通知开单医生</p>
                <p class="text-xs text-slate-400">{{ fmtTime(detail.reportTime) }} · {{ detail.reportBy || '—' }}</p>
              </div>
            </div>
            <div class="flex items-start gap-3">
              <el-icon class="mt-0.5" :class="detail.receiveBy ? 'text-emerald-500' : 'text-slate-300'">
                <CircleCheck />
              </el-icon>
              <div>
                <p class="text-sm" :class="detail.receiveBy ? 'text-slate-700' : 'text-slate-400'">
                  {{ detail.receiveBy ? '已确认接收' : '待接收' }}
                </p>
                <p v-if="detail.receiveBy" class="text-xs text-slate-400">
                  {{ fmtTime(detail.receiveTime) }} · {{ detail.receiveBy }}
                </p>
              </div>
            </div>
            <div class="flex items-start gap-3">
              <el-icon class="mt-0.5" :class="detail.handleBy ? 'text-emerald-500' : 'text-slate-300'">
                <CircleCheck />
              </el-icon>
              <div>
                <p class="text-sm" :class="detail.handleBy ? 'text-slate-700' : 'text-slate-400'">
                  {{ detail.handleBy ? '已记录处置' : '待处置' }}
                </p>
                <p v-if="detail.handleBy" class="text-xs text-slate-400">
                  {{ fmtTime(detail.handleTime) }} · {{ detail.handleBy }}
                </p>
                <p v-if="detail.handleMeasure" class="mt-1 rounded bg-slate-50 px-2 py-1 text-xs text-slate-600">
                  {{ detail.handleMeasure }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="showDetail = false">关闭</el-button>
        <el-button
          v-if="detail?.status === 1"
          v-perm="'medtech:criticalValue:edit'"
          type="warning"
          @click="detail && handleReceive(detail)"
        >
          确认接收
        </el-button>
        <el-button
          v-if="detail && canOperate(detail)"
          v-perm="'medtech:criticalValue:edit'"
          type="primary"
          @click="detail && openHandle(detail)"
        >
          记录处置
        </el-button>
      </template>
    </el-dialog>

    <!-- 处置 -->
    <el-dialog v-model="showHandleDialog" title="记录处置措施" width="560px" destroy-on-close>
      <div v-if="handleTarget" class="space-y-3">
        <div class="rounded-lg bg-slate-50 p-3 text-sm">
          <div class="font-medium text-slate-800">{{ handleTarget.criticalDesc }}</div>
          <div class="mt-1 text-xs text-slate-500">
            患者 {{ handleTarget.patientName }} · 上报 {{ fmtTime(handleTarget.reportTime) }}
          </div>
        </div>
        <div>
          <label class="mb-1 block text-sm text-slate-600">处置措施 <span class="text-red-500">*</span></label>
          <el-input
            v-model="handleMeasure"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="例如：立即复查血钾，予降钾处理，持续心电监护，30 分钟后复测"
          />
        </div>
        <p class="text-xs text-slate-400">
          提示：未接收直接处置也是允许的，系统会自动把接收人记为当前操作人。
        </p>
      </div>
      <template #footer>
        <el-button @click="showHandleDialog = false">取消</el-button>
        <el-button v-perm="'medtech:criticalValue:edit'" type="primary" :loading="handling" @click="submitHandle">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>
