<template>
  <div class="space-y-4">
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <!-- 参保类型分布 -->
      <div class="his-card p-4">
        <div class="font-semibold text-[15px] mb-2 text-[#0B3B66]">各参保类型结算情况</div>
        <div ref="insChart" class="h-[300px]"></div>
      </div>
      <!-- 科室分布 -->
      <div class="his-card p-4">
        <div class="font-semibold text-[15px] mb-2 text-[#0B3B66]">各科室结算金额</div>
        <div ref="deptChart" class="h-[300px]"></div>
      </div>
    </div>
    <!-- 趋势 -->
    <div class="his-card p-4">
      <div class="font-semibold text-[15px] mb-2 text-[#0B3B66]">近 10 日结算趋势</div>
      <div ref="trendChart" class="h-[320px]"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { statsData } from '@/api/insurance'

const insChart = ref(null)
const deptChart = ref(null)
const trendChart = ref(null)

onMounted(async () => {
  const res = await statsData()
  const d = res.data
  await nextTick()
  renderIns(d.byIns)
  renderDept(d.byDept)
  renderTrend(d.trend)
})

function renderIns(byIns) {
  const keys = Object.keys(byIns)
  const chart = echarts.init(insChart.value)
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['结算笔数', '结算总额', '统筹支付'] },
    grid: { left: 50, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: keys },
    yAxis: [{ type: 'value', name: '笔数' }, { type: 'value', name: '金额', axisLabel: { formatter: (v) => v / 1000 + 'k' } }],
    series: [
      { name: '结算笔数', type: 'bar', data: keys.map((k) => byIns[k].count), itemStyle: { color: '#1269B5' } },
      { name: '结算总额', type: 'line', yAxisIndex: 1, data: keys.map((k) => Math.round(byIns[k].amount)), itemStyle: { color: '#0E9488' } },
      { name: '统筹支付', type: 'line', yAxisIndex: 1, data: keys.map((k) => Math.round(byIns[k].insurancePay)), itemStyle: { color: '#7C3AED' } },
    ],
  })
}

function renderDept(byDept) {
  const keys = Object.keys(byDept)
  const chart = echarts.init(deptChart.value)
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, valueFormatter: (v) => (v / 1000).toFixed(1) + 'k' },
    grid: { left: 60, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'value', axisLabel: { formatter: (v) => v / 1000 + 'k' } },
    yAxis: { type: 'category', data: keys, inverse: true },
    series: [{ type: 'bar', data: keys.map((k) => Math.round(byDept[k].amount)), itemStyle: { color: '#0E9488' }, label: { show: true, position: 'right', formatter: (p) => (p.value / 1000).toFixed(1) + 'k' } }],
  })
}

function renderTrend(trend) {
  const chart = echarts.init(trendChart.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['结算笔数', '结算金额'] },
    grid: { left: 50, right: 20, top: 40, bottom: 40 },
    xAxis: { type: 'category', data: trend.map((t) => t.day.slice(5)) },
    yAxis: [{ type: 'value', name: '笔数' }, { type: 'value', name: '金额', axisLabel: { formatter: (v) => v / 1000 + 'k' } }],
    series: [
      { name: '结算笔数', type: 'bar', data: trend.map((t) => t.count), itemStyle: { color: '#1269B5' } },
      { name: '结算金额', type: 'line', smooth: true, yAxisIndex: 1, data: trend.map((t) => Math.round(t.amount)), itemStyle: { color: '#D97706' } },
    ],
  })
}
</script>
