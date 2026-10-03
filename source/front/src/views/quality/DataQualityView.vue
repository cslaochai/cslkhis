<script setup lang="ts">
/**
 * 数据质量报表（P5.3）
 *
 * 这个页面对应「统一数据管理」的体检单：五个维度（完整性 / 一致性 / 及时性 / 唯一性 / 有效性）
 * 各自回答一个不同的问题，每条规则给出"在多少条里命中了多少条"，并且每条问题都能
 * 定位到具体表、具体主键、具体患者。
 *
 * 三条必须写在页面上的口径（否则数字会被误读）：
 *   1. 只有分子没有分母的数字没有意义 —— 所以每条规则都显示「命中 / 检查」；
 *   2. 分母为 0 的规则是**失效**，不是**通过** —— 页面用告警样式单独标出来；
 *   3. 维度合规率是加权口径（该维度各规则的分母、分子分别求和后相除），
 *      只用于横向比较维度、纵向看趋势，不代表"全库数据有 X% 是干净的"。
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { Refresh, Search, WarningFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getQualitySummary, listQualityIssuePage } from '@/api/dataQuality'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

const loading = ref(false)
const issueLoading = ref(false)
const summary = ref<any>(null)
const issues = ref<any[]>([])
const total = ref(0)

const query = reactive({
  dimension: '',
  ruleCode: '',
  severity: undefined as number | undefined,
  keyword: '',
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

const severityOptions = [
  { value: 3, label: '严重' },
  { value: 2, label: '警告' },
  { value: 1, label: '提示' },
]

const dimColor: Record<string, string> = {
  COMPLETENESS: '#1269B5',
  CONSISTENCY: '#0E9488',
  TIMELINESS: '#D97706',
  UNIQUENESS: '#7C3AED',
  VALIDITY: '#DC2626',
}

const loadSummary = async () => {
  loading.value = true
  try {
    const res = await getQualitySummary()
    summary.value = res.data
  } catch (e: any) {
    ElMessage.error(e?.message || '加载数据质量总览失败')
  } finally {
    loading.value = false
  }
}

const loadIssues = async () => {
  issueLoading.value = true
  try {
    const params: any = {
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    }
    if (query.dimension) params.dimension = query.dimension
    if (query.ruleCode) params.ruleCode = query.ruleCode
    if (query.severity) params.severity = query.severity
    if (query.keyword) params.keyword = query.keyword.trim()
    const res = await listQualityIssuePage(params)
    issues.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e: any) {
    ElMessage.error(e?.message || '加载问题清单失败')
  } finally {
    issueLoading.value = false
  }
}

const refreshAll = async () => {
  await loadSummary()
  query.pageNum = 1
  await loadIssues()
}

const pickDimension = (code: string) => {
  query.dimension = query.dimension === code ? '' : code
  query.ruleCode = ''
  query.pageNum = 1
  loadIssues()
}

const pickRule = (code: string) => {
  query.ruleCode = query.ruleCode === code ? '' : code
  query.pageNum = 1
  loadIssues()
}

const clearFilter = () => {
  query.dimension = ''
  query.ruleCode = ''
  query.severity = undefined
  query.keyword = ''
  query.pageNum = 1
  loadIssues()
}

const dimensions = computed(() => summary.value?.dimensions || [])
const emptyRules = computed(() =>
  (summary.value?.dimensions || []).flatMap((d: any) => (d.rules || []).filter((r: any) => r.empty)),
)
const hasFilter = computed(
  () => !!(query.dimension || query.ruleCode || query.severity || query.keyword),
)
const targetText = computed(() => {
  if (query.ruleCode) return `规则 ${query.ruleCode}`
  if (query.dimension) {
    const d = dimensions.value.find((x: any) => x.dimension === query.dimension)
    return `维度 ${d?.dimensionText || query.dimension}`
  }
  return '全部规则'
})

const severityTag = (s: number) => (s === 3 ? 'danger' : s === 2 ? 'warning' : 'info')
const rateText = (v: any) => (v === null || v === undefined ? '未生效' : `${Number(v).toFixed(1)}%`)
const num = (v: any) => Number(v ?? 0)

onMounted(refreshAll)
</script>

<template>
  <div v-loading="loading">
    <!-- 标题 -->
    <div class="mb-3 flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div>
        <h1 class="text-xl font-semibold text-slate-900">数据质量报表</h1>
        <p class="mt-1 max-w-4xl text-sm text-slate-500">
          按
          <el-text class="mx-1" size="small">完整性 / 一致性 / 及时性 / 唯一性 / 有效性</el-text>
          五个维度给全院数据做体检，每条规则都给出「在多少条里命中了多少条」，每条问题都能定位到具体的表、主键和患者。
        </p>
      </div>
      <el-button :icon="Refresh" @click="refreshAll">刷新</el-button>
    </div>

    <!-- 口径说明 -->
    <div class="mb-3 rounded-lg border border-blue-100 bg-blue-50 px-4 py-3 text-xs leading-6 text-slate-700">
      <p><span class="font-medium text-slate-900">怎么读这张表：</span>每条规则都有<b>分母</b>（在多少条里查）和<b>分子</b>（命中多少条）。只有分子没有分母的数字没有意义。</p>
      <p><span class="font-medium text-slate-900">分母为 0 的规则是「失效」不是「通过」</span>——它意味着这条规则这次没查到任何对象，页面会单独告警，不要当成干净。</p>
      <p><span class="font-medium text-slate-900">维度合规率是加权口径</span>（该维度各规则的分母、分子分别求和后相除），只用于横向比较维度、纵向看趋势，不代表「全库数据有某个百分比是干净的」。</p>
    </div>

    <!-- 空规则告警：分母为 0 即失效 -->
    <div
      v-if="emptyRules.length"
      data-testid="p5-dq-empty-alert"
      class="mb-3 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-xs text-red-700"
    >
      <div class="flex items-start gap-2">
        <el-icon class="mt-0.5"><WarningFilled /></el-icon>
        <div>
          <p class="font-medium">
            有 {{ emptyRules.length }} 条规则本次没查到任何检查对象（分母为 0），规则处于失效状态，不能当作「通过」：
          </p>
          <p class="mt-1 leading-6">
            <span v-for="r in emptyRules" :key="r.ruleCode" class="mr-2 inline-block">{{ r.ruleName }}（{{ r.ruleCode }}）</span>
          </p>
        </div>
      </div>
    </div>

    <!-- 总览 -->
    <div v-if="summary" data-testid="p5-dq-summary" class="mb-3 grid grid-cols-2 gap-4 lg:grid-cols-5">
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">检查规则</p>
        <p class="mt-1 text-2xl font-semibold text-slate-900" data-testid="p5-dq-stat-rules">{{ summary.ruleCount }}</p>
        <p class="mt-1 text-xs text-slate-400">干净 {{ summary.cleanRuleCount }} · 有问题 {{ summary.dirtyRuleCount }} · 失效 {{ summary.emptyRuleCount }}</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">检查总数（分母合计）</p>
        <p class="mt-1 text-2xl font-semibold text-slate-900" data-testid="p5-dq-stat-checked">{{ summary.checkedTotal }}</p>
        <p class="mt-1 text-xs text-slate-400">各规则分母求和，非去重记录数</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">问题条数</p>
        <p class="mt-1 text-2xl font-semibold text-red-600" data-testid="p5-dq-stat-issues">{{ summary.issueCount }}</p>
        <p class="mt-1 text-xs text-slate-400">其中严重 {{ summary.highIssueCount }} 条</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">总体合规率（加权）</p>
        <p class="mt-1 text-2xl font-semibold text-slate-900" data-testid="p5-dq-stat-rate">{{ rateText(summary.passRate) }}</p>
        <p class="mt-1 text-xs text-slate-400">(分母合计 - 问题数) / 分母合计</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">统计时间</p>
        <p class="mt-1 text-sm font-medium text-slate-700" data-testid="p5-dq-stat-time">{{ summary.generatedAt }}</p>
        <p class="mt-1 text-xs text-slate-400">每次打开页面实时计算</p>
      </div>
    </div>

    <!-- 五维度 -->
    <div data-testid="p5-dq-dimensions" class="mb-3 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-5">
      <div
        v-for="d in dimensions"
        :key="d.dimension"
        :data-testid="`p5-dq-dim-${d.dimension}`"
        class="cursor-pointer rounded-lg border bg-white p-4 shadow-sm transition"
        :class="query.dimension === d.dimension ? 'border-blue-400 ring-1 ring-blue-200' : 'border-slate-200 hover:border-slate-300'"
        @click="pickDimension(d.dimension)"
      >
        <div class="flex items-center justify-between">
          <span class="text-sm font-medium text-slate-900">{{ d.dimensionText }}</span>
          <span
            class="rounded px-1.5 py-0.5 text-xs"
            :class="num(d.issueCount) > 0 ? 'bg-red-50 text-red-600' : 'bg-emerald-50 text-emerald-600'"
          >{{ num(d.issueCount) }} 条</span>
        </div>
        <p class="mt-2 text-2xl font-semibold" :style="{ color: dimColor[d.dimension] }">{{ rateText(d.passRate) }}</p>
        <p class="mt-1 text-xs text-slate-400">
          {{ d.ruleCount }} 条规则 · 命中 {{ d.dirtyRuleCount }} 条
        </p>
        <p class="mt-2 line-clamp-2 text-xs leading-5 text-slate-500">{{ d.description }}</p>
      </div>
    </div>

    <!-- 规则清单 -->
    <div class="mb-3 rounded-lg border border-slate-200 bg-white shadow-sm">
      <div class="flex items-center justify-between border-b border-slate-100 px-4 py-3">
        <div>
          <h2 class="text-sm font-semibold text-slate-900">规则清单</h2>
          <p class="mt-0.5 text-xs text-slate-500">点维度的「命中」数字可以下钻到具体问题记录</p>
        </div>
      </div>
      <div data-testid="p5-dq-rules" class="divide-y divide-slate-100">
        <div v-for="d in dimensions" :key="`rules-${d.dimension}`" class="px-4 py-3">
          <div class="mb-2 flex items-center gap-2">
            <span class="h-2 w-2 rounded-full" :style="{ background: dimColor[d.dimension] }"></span>
            <span class="text-sm font-medium text-slate-800">{{ d.dimensionText }}</span>
            <span class="text-xs text-slate-400">{{ d.description }}</span>
          </div>
          <div class="overflow-x-auto">
            <table class="w-full text-left text-xs">
              <thead class="text-slate-500">
                <tr>
                  <th class="py-1.5 pr-3 font-medium">规则</th>
                  <th class="py-1.5 pr-3 font-medium">严重度</th>
                  <th class="py-1.5 pr-3 font-medium">检查范围</th>
                  <th class="py-1.5 pr-3 font-medium">命中 / 检查</th>
                  <th class="py-1.5 pr-3 font-medium">合规率</th>
                  <th class="py-1.5 font-medium">依据</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="r in d.rules"
                  :key="r.ruleCode"
                  :data-testid="`p5-dq-rule-${r.ruleCode}`"
                  class="border-t border-slate-50 align-top"
                  :class="query.ruleCode === r.ruleCode ? 'bg-blue-50/60' : ''"
                >
                  <td class="py-1.5 pr-3">
                    <span class="font-medium text-slate-800">{{ r.ruleName }}</span>
                    <span class="ml-1 text-slate-400">{{ r.ruleCode }}</span>
                  </td>
                  <td class="py-1.5 pr-3">
                    <el-tag size="small" :type="severityTag(r.severity)">{{ r.severityText }}</el-tag>
                  </td>
                  <td class="py-1.5 pr-3 text-slate-500">{{ r.checkedDesc }}<span class="ml-1 text-slate-400">({{ r.tableName }})</span></td>
                  <td class="py-1.5 pr-3">
                    <el-button
                      v-if="num(r.issueCount) > 0"
                      link
                      type="primary"
                      size="small"
                      :data-testid="`p5-dq-drill-${r.ruleCode}`"
                      @click="pickRule(r.ruleCode)"
                    >{{ num(r.issueCount) }} / {{ num(r.checkedTotal) }}</el-button>
                    <span v-else class="text-emerald-600">{{ num(r.issueCount) }} / {{ num(r.checkedTotal) }}</span>
                    <el-tag v-if="r.empty" size="small" type="danger" class="ml-1">失效</el-tag>
                  </td>
                  <td class="py-1.5 pr-3" :class="r.empty ? 'text-red-600' : 'text-slate-700'">{{ rateText(r.passRate) }}</td>
                  <td class="py-1.5 text-slate-400">{{ r.basis }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="严重度">
          <el-select
            v-model="query.severity"
            placeholder="严重度"
            clearable
            style="width: 110px"
            data-testid="p5-dq-severity"
            @change="() => { query.pageNum = 1; loadIssues() }"
          >
            <el-option v-for="o in severityOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input
            v-model="query.keyword"
            placeholder="患者号 / 姓名 / 单号 / 描述"
            clearable
            style="width: 220px"
            data-testid="p5-dq-keyword"
            @keyup.enter="() => { query.pageNum = 1; loadIssues() }"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="() => { query.pageNum = 1; loadIssues() }">查询</el-button>
          <el-button v-if="hasFilter" @click="clearFilter">清除筛选</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div v-loading="issueLoading">
        <el-table :data="issues" stripe :max-height="tableMaxHeight" data-testid="p5-dq-issues">
          <el-table-column label="维度" width="80">
            <template #default="{ row }">
              <span :style="{ color: dimColor[row.dimension] }">{{ row.dimensionText }}</span>
            </template>
          </el-table-column>
          <el-table-column label="严重度" width="76">
            <template #default="{ row }">
              <el-tag size="small" :type="severityTag(row.severity)">{{ row.severityText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="规则" width="150">
            <template #default="{ row }">
              <div>{{ row.ruleName }}</div>
              <div class="text-slate-400">{{ row.ruleCode }}</div>
            </template>
          </el-table-column>
          <el-table-column label="定位（表#主键）" width="240">
            <template #default="{ row }">
              <div>
                <span class="text-slate-500">{{ row.tableName }}</span>
                <span class="text-slate-400">#</span>
                <span>{{ row.recordId }}</span>
              </div>
              <div class="text-slate-400">{{ row.recordNo || '—' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="患者" width="130">
            <template #default="{ row }">
              <div>{{ row.patientName || '—' }}</div>
              <div class="text-slate-400">{{ row.patientNo || '—' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="科室 / 医师" width="120">
            <template #default="{ row }">
              <div>{{ row.deptName || '—' }}</div>
              <div class="text-slate-400">{{ row.doctorName || '—' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="问题描述" min-width="280">
            <template #default="{ row }">
              <div class="leading-5 text-slate-700">{{ row.detail }}</div>
            </template>
          </el-table-column>
          <el-table-column label="整改建议" min-width="200">
            <template #default="{ row }">
              <div class="leading-5 text-slate-500">{{ row.suggestion }}</div>
            </template>
          </el-table-column>
          <el-table-column label="时间" width="140">
            <template #default="{ row }">
              <span class="text-slate-500">{{ row.occurredTime || '—' }}</span>
            </template>
          </el-table-column>
          <template #empty>
            <div class="py-6 text-sm text-slate-400">当前范围内没有命中的问题记录</div>
          </template>
        </el-table>

        <div ref="footerRef" class="list-footer flex items-center justify-end">
          <p class="mr-3 text-slate-500" data-testid="p5-dq-issue-total">共 {{ total }} 条</p>
          <el-pagination
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            :total="total"
            :page-sizes="PAGE_SIZES"
            layout="sizes, prev, pager, next"
            @current-change="loadIssues"
            @size-change="() => { query.pageNum = 1; loadIssues() }"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>
