<template>
  <div class="space-y-4">
    <!-- KPI -->
    <div class="grid grid-cols-2 md:grid-cols-3 xl:grid-cols-6 gap-4">
      <StatCard label="启用监管规则" :value="stats.ruleCount" icon="SetUp" color="#1269B5" />
      <StatCard label="疑点总数" :value="stats.suspicionTotal" icon="Search" color="#0E9488" :sub="'待核实 ' + stats.suspicionPending" />
      <StatCard label="已确认违规金额(元)" :value="fmt(stats.confirmedAmount)" icon="Warning" color="#D97706" />
      <StatCard label="待追回金额(元)" :value="fmt(stats.recoverPendingAmount)" icon="RefreshLeft" color="#DC2626" />
      <StatCard label="已追回金额(元)" :value="fmt(stats.recoveredAmount)" icon="CircleCheck" color="#059669" />
      <StatCard label="检查中飞检任务" :value="stats.inspectionRunning" icon="Van" color="#7C3AED" :sub="'已完成 ' + stats.inspectionDone" />
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <!-- 规则分布 -->
      <div class="his-card p-4">
        <div class="font-semibold text-[15px] mb-2 text-[#0B3B66]">疑点按规则分布</div>
        <div ref="ruleChart" class="h-[300px]"></div>
      </div>
      <!-- 疑点状态 -->
      <div class="his-card p-4">
        <div class="font-semibold text-[15px] mb-2 text-[#0B3B66]">疑点处置状态</div>
        <div ref="statusChart" class="h-[300px]"></div>
      </div>
    </div>

    <!-- 趋势 -->
    <div class="his-card p-4">
      <div class="font-semibold text-[15px] mb-2 text-[#0B3B66]">近 10 日疑点趋势</div>
      <div ref="trendChart" class="h-[300px]"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import StatCard from '@/components/StatCard.vue'
import { superviseStats } from '@/api/supervise'

const stats = reactive({})
const ruleChart = ref(null)
const statusChart = ref(null)
const trendChart = ref(null)

function fmt(n) {
  return (n ?? 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

onMounted(async () => {
  const res = await superviseStats()
  Object.assign(stats, res.data)
  await nextTick()
  renderRule(res.data.ruleDist || [])
  renderStatus(res.data)
  renderTrend(res.data.trend || [])
})

function renderRule(dist) {
  const chart = echarts.init(ruleChart.value)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}<br/>疑点 {c} 条（{d}%）' },
    legend: { bottom: 0, type: 'scroll' },
    series: [{
      type: 'pie',
      radius: ['35%', '62%'],
      itemStyle: { borderRadius: 4 },
      label: { formatter: '{b} {c}条' },
      data: dist.map((d) => ({ name: d.ruleName, value: d.count })),
    }],
  })
}

function renderStatus(d) {
  const chart = echarts.init(statusChart.value)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 条' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: '62%',
      itemStyle: { borderRadius: 4 },
      label: { formatter: '{b} {c}条' },
      data: [
        { name: '待核实', value: d.suspicionPending, itemStyle: { color: '#D97706' } },
        { name: '已确认', value: d.suspicionConfirmed, itemStyle: { color: '#DC2626' } },
        { name: '已排除', value: d.suspicionExcluded, itemStyle: { color: '#0E9488' } },
      ],
    }],
  })
}

function renderTrend(trend) {
  const chart = echarts.init(trendChart.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['疑点数', '涉及金额'] },
    grid: { left: 50, right: 30, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: trend.map((t) => t.day.slice(5)) },
    yAxis: [{ type: 'value', name: '条' }, { type: 'value', name: '金额', axisLabel: { formatter: (v) => v / 1000 + 'k' } }],
    series: [
      { name: '疑点数', type: 'bar', data: trend.map((t) => t.count), itemStyle: { color: '#1269B5' } },
      { name: '涉及金额', type: 'line', smooth: true, yAxisIndex: 1, data: trend.map((t) => Math.round(t.amount)), itemStyle: { color: '#D97706' } },
    ],
  })
}
</script>
