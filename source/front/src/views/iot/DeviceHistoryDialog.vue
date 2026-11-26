<template>
  <el-dialog v-model="visible" :title="title" destroy-on-close width="920px" @closed="onClosed" @opened="renderChart">
    <div class="flex flex-wrap items-center gap-3 mb-3">
      <el-radio-group v-model="rangeMinutes" size="small" @change="load">
        <el-radio-button :value="15">15 分钟</el-radio-button>
        <el-radio-button :value="60">1 小时</el-radio-button>
        <el-radio-button :value="360">6 小时</el-radio-button>
        <el-radio-button :value="1440">24 小时</el-radio-button>
      </el-radio-group>
      <el-select v-model="metricKey" :fit-input-width="false" placeholder="指标" size="small" style="width: 180px"
                 @change="renderChart">
        <el-option v-for="m in metricOptions" :key="m.key" :label="m.label + (m.unit ? `(${m.unit})` : '')" :value="m.key"/>
      </el-select>
      <span class="text-[13px] text-gray-500">
        共 <b>{{ points.length }}</b> 点<template v-if="metricKey && latestValue != null">
        · 最新 <b>{{ latestValue }}{{ currentUnit }}</b></template>
      </span>
      <span v-if="truncated" class="text-[13px] text-red-500">已截断：仅显示前 5000 点，请缩小时间范围</span>
    </div>

    <div ref="chartRef" class="w-full" style="height: 340px"></div>

    <div v-if="!points.length" class="text-center text-gray-400 py-6">该时间段没有上报数据</div>
  </el-dialog>
</template>

<script setup>
/**
 * 单台设备趋势弹框（只读）。
 *
 * 独立组件而不是塞在主页面：主页面只留「查询卡 + 表格卡 + 打开弹框」，
 * 图表的初始化/销毁、时间范围切换都在这里，主页面不为它持有 echarts 实例。
 */
import {computed, nextTick, onUnmounted, ref, watch} from 'vue';
import * as echarts from 'echarts';
import {iotHistoryList} from '@/api/iot';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  device: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const title = computed(() => {
  if (!props.device) return '设备趋势';
  return `${props.device.deviceTypeLabel || props.device.deviceType} ${props.device.deviceNo} · 趋势`;
});

const rangeMinutes = ref(30);
const points = ref([]);
const metricKey = ref('');
const truncated = ref(false);
const chartRef = ref(null);
let chart = null;

const metricOptions = computed(() => {
  const first = points.value[0];
  if (!first) return [];
  return (first.metrics || []).filter(m => m.numeric != null)
      .map(m => ({key: m.key, label: m.label, unit: m.unit}));
});
const currentUnit = computed(() => metricOptions.value.find(m => m.key === metricKey.value)?.unit || '');
const latestValue = computed(() => {
  const last = points.value[points.value.length - 1];
  const m = last?.metrics?.find(x => x.key === metricKey.value);
  if (!m || m.value == null) return null;
  const n = Number(m.value);
  return Number.isFinite(n) ? Math.round(n * 10) / 10 : m.value;
});
const LIMIT = 5000;

const pad = (n) => String(n).padStart(2, '0');
const fmt = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;

const load = async () => {
  if (!props.device?.deviceType || !props.device?.deviceNo) return;
  const end = new Date();
  const start = new Date(end.getTime() - rangeMinutes.value * 60 * 1000);
  try {
    const res = await iotHistoryList({
      deviceType: props.device.deviceType,
      deviceNo: props.device.deviceNo,
      startTime: fmt(start),
      endTime: fmt(end),
      limit: LIMIT,
      // 超过 1 小时改用 5 分钟聚合：原始点 5 秒一条，24 小时 1.7 万点会被 limit 截掉一大半
      intervalMinutes: rangeMinutes.value > 60 ? 5 : null,
    });
    if (res.code === 200) {
      points.value = res.data || [];
      truncated.value = points.value.length >= LIMIT;
      if (!metricOptions.value.some(m => m.key === metricKey.value)) {
        metricKey.value = metricOptions.value[0]?.key || '';
      }
      await nextTick();
      renderChart();
    }
  } catch (e) {
    console.error('加载设备历史失败', e);
  }
};

const renderChart = () => {
  if (!chartRef.value) return;
  if (!chart) chart = echarts.init(chartRef.value);
  if (!metricKey.value || !points.value.length) {
    chart.clear();
    return;
  }
  const opt = metricOptions.value.find(m => m.key === metricKey.value);
  const series = points.value.map(p => {
    const m = (p.metrics || []).find(x => x.key === metricKey.value);
    return [String(p.ts || '').replace(' ', 'T'), m?.numeric ?? null];
  });
  chart.setOption({
    animation: false,
    grid: {left: 56, right: 20, top: 30, bottom: 44},
    tooltip: {trigger: 'axis'},
    xAxis: {type: 'time'},
    yAxis: {type: 'value', name: opt?.unit || '', scale: true},
    dataZoom: [{type: 'inside'}, {type: 'slider', height: 18, bottom: 8}],
    series: [{
      name: `${opt?.label || metricKey.value}${opt?.unit ? `(${opt.unit})` : ''}`,
      type: 'line',
      showSymbol: false,
      data: series,
    }],
  }, true);
};

const onClosed = () => {
  points.value = [];
  metricKey.value = '';
  if (chart) {
    chart.dispose();
    chart = null;
  }
};

watch(visible, (v) => {
  if (v) {
    rangeMinutes.value = 30;
    load();
  }
});

const onResize = () => chart?.resize();
window.addEventListener('resize', onResize);
onUnmounted(() => {
  window.removeEventListener('resize', onResize);
  chart?.dispose();
  chart = null;
});
</script>
