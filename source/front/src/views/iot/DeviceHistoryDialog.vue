<template>
  <el-dialog v-model="visible" :title="title" destroy-on-close width="920px" @closed="onClosed" @opened="renderChart">
    <div class="flex flex-wrap items-center gap-3 mb-3">
      <el-radio-group v-model="rangeMinutes" size="small" @change="load">
        <el-radio-button :value="15">15 分钟</el-radio-button>
        <el-radio-button :value="60">1 小时</el-radio-button>
        <el-radio-button :value="360">6 小时</el-radio-button>
        <el-radio-button :value="1440">24 小时</el-radio-button>
      </el-radio-group>
      <el-select v-model="metricKey" :fit-input-width="false" placeholder="指标" size="small" style="width: 220px"
                 @change="renderChart">
        <el-option v-for="m in metricOptions" :key="m.key" :label="m.label + (m.unit ? `(${m.unit})` : '')" :value="m.key"/>
      </el-select>
      <span class="text-[13px] text-gray-500">
        共 <b>{{ points.length }}</b> 点<template v-if="metricKey && latestText">
        · 最新 <b>{{ latestText }}</b></template>
      </span>
      <span v-if="truncated" class="text-[13px] text-red-500">已截断：仅显示前 5000 点，请缩小时间范围</span>
    </div>

    <div ref="chartRef" class="w-full" style="height: 340px"></div>

    <div v-if="!points.length" class="text-center text-gray-400 py-6">该时间段没有上报数据</div>

    <!-- 物流机器人：任务单与乘梯记录。点开一台机器就能看到"这趟谁下的单、几号箱、谁签收、在梯口等了多久" -->
    <div v-if="isRobot" class="mt-3">
      <div class="flex items-center justify-between mb-1">
        <span class="text-[13px] font-medium text-gray-700">任务单 / 乘梯记录</span>
        <span class="text-[12px] text-gray-400">近 24 小时 · 最近 {{ tasks.length }} 条</span>
      </div>
      <el-table v-loading="tasksLoading" :data="tasks" size="small" stripe max-height="190"
                empty-text="近 24 小时还没有任务单">
        <el-table-column label="时间" width="78">
          <template #default="{ row }">{{ String(row.ts || '').slice(11, 19) }}</template>
        </el-table-column>
        <el-table-column label="事件" width="104">
          <template #default="{ row }">
            <el-tag size="small" :type="taskTag(row.eventCode)">{{ row.eventCodeText || row.eventCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="任务单" width="136">
          <template #default="{ row }">
            <span class="text-[12px] text-gray-700">{{ taskOrder(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="明细" min-width="320">
          <template #default="{ row }">
            <span class="text-[12px] text-gray-500">{{ row.detail }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
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
import {iotEventListPage, iotHistoryList} from '@/api/iot';

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

const isRobot = computed(() => props.device?.deviceType === 'robot');

const metricOptions = computed(() => {
  const first = points.value[0];
  if (!first) return [];
  return (first.metrics || []).filter(m => m.numeric != null)
      .map(m => ({key: m.key, label: m.label, unit: m.unit}));
});
const currentUnit = computed(() => metricOptions.value.find(m => m.key === metricKey.value)?.unit || '');
/** 四舍五入两位小数，去尾零；非有限值返回 null */
const round2 = (v) => {
  const n = Number(v);
  return v == null || !Number.isFinite(n) ? null : Number(n.toFixed(2));
};
const lastNum = (p, key) => {
  const m = (p?.metrics || []).find(x => x.key === key);
  return round2(m?.numeric);
};
/** 趋势 tooltip 统一两位小数：AVG 聚合点仍可能带浮点尾数，展示层再兜一道 */
const tooltipFormatter = (params) => {
  const list = Array.isArray(params) ? params : [params];
  if (!list.length) return '';
  const time = list[0].axisValueLabel || list[0].axisValue || '';
  const rows = list.map(p => {
    const raw = Array.isArray(p.value) ? p.value[1] : p.value;
    const text = round2(raw) == null ? '—' : round2(raw);
    return `${p.marker} ${p.seriesName}：${text}`;
  });
  return `${time}<br/>${rows.join('<br/>')}`;
};
const latestText = computed(() => {
  const last = points.value[points.value.length - 1];
  if (!last) return '';
  const v = lastNum(last, metricKey.value);
  return v == null ? '' : `${v}${currentUnit.value}`;
});
const LIMIT = 5000;

const pad = (n) => String(n).padStart(2, '0');
const fmt = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;

// ===== 任务单 / 乘梯记录：只对物流机器人拉，设备事件接口按 deviceNo 过滤 =====
const tasks = ref([]);
const tasksLoading = ref(false);
const parseData = (row) => {
  try {
    return row?.dataJson ? JSON.parse(row.dataJson) : {};
  } catch (e) {
    return {};
  }
};
const taskOrder = (row) => parseData(row).orderNo || '—';
const taskTag = (code) => ({
  TASK_START: 'primary',
  TASK_COMPLETE: 'success',
  ELEVATOR_WAIT: 'warning',
  BATTERY_LOW: 'danger',
}[code] || 'info');

const loadTasks = async () => {
  tasks.value = [];
  if (!isRobot.value || !props.device?.deviceNo) return;
  tasksLoading.value = true;
  try {
    const end = new Date();
    const start = new Date(end.getTime() - 24 * 3600 * 1000);
    const res = await iotEventListPage({
      deviceType: 'robot',
      deviceNo: props.device.deviceNo,
      pageNum: 1,
      pageSize: 20,
      startTime: fmt(start),
      endTime: fmt(end),
    });
    if (res.code === 200) tasks.value = res.data?.records || [];
  } catch (e) {
    console.error('加载机器人任务单失败', e);
  } finally {
    tasksLoading.value = false;
  }
};

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
  const zoom = [{type: 'inside'}, {type: 'slider', height: 18, bottom: 8}];

  const opt = metricOptions.value.find(m => m.key === metricKey.value);
  const series = points.value.map(p => {
    const m = (p.metrics || []).find(x => x.key === metricKey.value);
    return [String(p.ts || '').replace(' ', 'T'), m?.numeric ?? null];
  });
  chart.setOption({
    animation: false,
    grid: {left: 56, right: 20, top: 30, bottom: 44},
    tooltip: {trigger: 'axis', formatter: tooltipFormatter},
    xAxis: {type: 'time'},
    yAxis: {type: 'value', name: opt?.unit || '', scale: true},
    dataZoom: zoom,
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
  tasks.value = [];
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
    loadTasks();
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
