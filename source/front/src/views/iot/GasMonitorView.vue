<template>
  <div class="p-4">
    <!-- 顶部：全院气体终端概况 -->
    <div class="bg-white rounded-lg shadow-sm px-4 py-3 mb-4 flex flex-wrap items-center gap-x-6 gap-y-3">
      <div class="text-[20px] font-semibold text-[#1269B5]">
        医用气体监测
        <span class="text-[14px] font-normal text-gray-500 ml-1">中心供氧 · 负压吸引 · 压缩空气</span>
      </div>

      <div class="flex items-center gap-6 ml-auto">
        <div class="text-center">
          <div class="text-[22px] font-bold leading-6">{{ stats.total }}</div>
          <div class="text-[12px] text-gray-500">气体终端</div>
        </div>
        <div class="text-center">
          <div class="text-[22px] font-bold leading-6 text-[#0E9488]">{{ stats.online }}</div>
          <div class="text-[12px] text-gray-500">在线</div>
        </div>
        <div class="text-center">
          <div class="text-[22px] font-bold leading-6" :class="stats.alarm ? 'text-red-600' : 'text-gray-400'">
            {{ stats.alarm }}
          </div>
          <div class="text-[12px] text-gray-500">压力异常</div>
        </div>
        <div class="text-center">
          <div class="text-[22px] font-bold leading-6 text-[#1269B5]">{{ stats.inUse }}</div>
          <div class="text-[12px] text-gray-500">正在用气</div>
        </div>
        <div class="text-center">
          <div class="text-[22px] font-bold leading-6" :class="stats.pending ? 'text-orange-600' : 'text-gray-400'">
            {{ stats.pending }}
          </div>
          <div class="text-[12px] text-gray-500">未闭环告警</div>
        </div>
      </div>

      <div class="flex items-center gap-2 text-[12px] text-gray-400">
        <span class="inline-block w-2 h-2 rounded-full bg-green-500 animate-pulse"></span>
        自动刷新 10s · {{ lastRefresh || '' }}
      </div>
    </div>

    <!-- 按点位分组：手术室/ICU/抢救室各看各的吊塔与治疗带 -->
    <div v-loading="loading" class="space-y-4">
      <div v-for="group in groups" :key="group.deptName" class="bg-white rounded-lg shadow-sm">
        <div class="flex items-center gap-2 px-4 py-2 border-b border-gray-100">
          <span class="text-[15px] font-semibold text-gray-800">{{ group.deptName }}</span>
          <span class="text-[12px] text-gray-400">{{ group.items.length }} 个终端</span>
          <el-tag v-if="group.alarmCount" size="small" type="danger" effect="plain" class="ml-auto">
            {{ group.alarmCount }} 个压力异常
          </el-tag>
          <span v-else class="ml-auto text-[12px] text-green-600">压力正常</span>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-2 2xl:grid-cols-3 gap-3 p-3">
          <div v-for="box in group.items" :key="box.deviceNo"
               class="rounded-lg border overflow-hidden transition hover:shadow-md"
               :class="box.borderClass">
            <div class="flex items-center gap-2 px-3 py-2 border-b" :class="box.headerClass">
              <span class="text-[15px] font-semibold truncate">{{ box.deviceName || box.deviceNo }}</span>
              <span class="text-[12px] text-gray-500">{{ box.deviceNo }}</span>
              <el-tag size="small" class="ml-auto" :type="box.badgeType" :effect="box.alarm ? 'dark' : 'plain'">
                {{ box.statusText }}
              </el-tag>
            </div>

            <div class="px-3 py-3">
              <div class="grid grid-cols-3 gap-2">
                <div v-for="gas in box.gases" :key="gas.key" class="rounded-md px-2 py-2 bg-gray-50">
                  <div class="text-[12px] text-gray-500">{{ gas.label }}</div>
                  <div class="mt-0.5">
                    <span class="text-[24px] font-bold leading-7" :class="gas.abnormal ? 'text-red-600' : 'text-[#1269B5]'">
                      {{ gas.value == null ? '' : gas.value }}
                    </span>
                    <span class="text-[11px] text-gray-500 ml-0.5">kPa</span>
                  </div>
                  <div class="text-[11px]" :class="gas.abnormal ? 'text-red-500' : 'text-gray-400'">
                    {{ gas.rangeText }}
                  </div>
                </div>
              </div>

              <div class="mt-2 text-[13px] text-gray-500 flex items-center gap-3 flex-wrap">
                <span class="truncate">{{ box.location || '未填写安装位置' }}</span>
                <span class="ml-auto text-gray-400">更新 {{ box.lastSeen || '' }}</span>
              </div>

              <div class="mt-1 text-[13px] text-gray-500">
                流量 <b class="text-gray-700">{{ box.flow == null ? '' : box.flow }}</b> L/min
                <span v-if="!box.telemetry" class="text-gray-400 ml-2">未上报</span>
                <span v-else-if="box.flow > 0" class="text-[#0E9488] ml-2">正在用气</span>
                <span v-else class="text-gray-400 ml-2">未使用</span>
              </div>

              <div v-if="box.alarmText" class="mt-2 text-[13px] text-red-600">{{ box.alarmText }}</div>
              <div v-else-if="!box.online" class="mt-2 text-[13px] text-red-500">超过 2 分钟未上报，先查网关/供电</div>

              <div class="mt-2 flex items-center justify-between text-[12px]">
                <span class="text-gray-400">O2/VAC/AIR 三路同终端</span>
                <span v-if="box.telemetry" class="text-[#1269B5] cursor-pointer" @click="openTrend(box)">压力趋势</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="!loading && !boxes.length" class="text-center text-gray-400 py-12">
      暂无医用气体终端{{ loadError ? '（' + loadError + '）' : '' }}
    </div>

    <!-- 告警闭环：减压阀故障、压力低 -->
    <div class="bg-white rounded-lg shadow-sm mt-4">
      <div class="flex flex-wrap items-center gap-4 px-4 py-3 border-b border-gray-100">
        <span class="text-[15px] font-semibold">气体告警</span>
        <el-switch v-model="pendingOnly" size="small" active-text="只看未处理" @change="reloadEvents"/>
        <span class="text-[12px] text-gray-400">
          仅列压力低、减压阀故障等 2 级以上告警；压力恢复后仍需人工确认处置（检查要求「发现—处置—复核」留痕）。
        </span>
        <el-button size="small" text class="ml-auto" @click="loadEvents()">刷新</el-button>
      </div>
      <el-table v-loading="eventLoading" :data="events" size="small" style="width: 100%">
        <el-table-column label="时间" width="165">
          <template #default="{row}"><span class="text-[12px]">{{ row.ts }}</span></template>
        </el-table-column>
        <el-table-column label="终端" min-width="180">
          <template #default="{row}">
            <div class="text-[13px]">{{ eventDeviceName(row) }}</div>
            <div class="text-[12px] text-gray-400">{{ row.deviceNo }}</div>
          </template>
        </el-table-column>
        <el-table-column label="告警" width="130">
          <template #default="{row}">
            <span :class="levelClass(row.level)">{{ row.eventCodeText || row.eventCode }}</span>
          </template>
        </el-table-column>
        <el-table-column label="现场状态" width="130">
          <template #default="{row}">
            <span class="text-[12px]" :class="siteState(row).cls">{{ siteState(row).text }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="描述" min-width="200" show-overflow-tooltip/>
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            <el-tag size="small" :type="row.handledFlag === 0 ? 'warning' : 'info'" effect="plain">
              {{ row.handledFlagText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理" width="150">
          <template #default="{row}">
            <span v-if="row.handledFlag === 0" class="text-[12px]">
              <span class="text-[#1269B5] cursor-pointer" @click="handleEvent(row, 1)">处理</span>
              <span class="text-gray-400 mx-2">|</span>
              <span class="text-gray-500 cursor-pointer" @click="handleEvent(row, 2)">忽略</span>
            </span>
            <span v-else class="text-[12px] text-gray-400">
              {{ row.handleBy || '' }}<template v-if="row.handleRemark"> · {{ row.handleRemark }}</template>
            </span>
          </template>
        </el-table-column>
      </el-table>
      <div class="flex justify-end px-4 py-2">
        <el-pagination v-model:current-page="eventPage.pageNum" :page-size="eventPage.pageSize"
                       :total="eventTotal" layout="total, prev, pager, next" small
                       @current-change="loadEvents"/>
      </div>
    </div>

    <DeviceHistoryDialog v-model="trendVisible" :device="trendDevice"/>
  </div>
</template>

<script setup>
/**
 * 医用气体压力监测（设备科/手术室/病区共用）。
 *
 * 口径对齐 GB 50751《医用气体工程技术规范》与捷锐/Amico 终端报警器：
 *   1) 一个终端三路气（O2/VAC/AIR）同屏，压力是主指标，单位固定 kPa；
 *   2) 判异常看区间：O2/AIR 400~500kPa，VAC -70~-40kPa；越界才算异常，不做"看起来差不多"的模糊判断；
 *   3) 减压阀故障先掉压力、后报故障，两个事件都要留处置记录，不能只看当前值。
 *
 * 数据源：biz_iot_device（点位）+ 时序库最新值（o2Kpa/vacuumKpa/airKpa/flowLpm）+ biz_iot_event。
 */
import {computed, onMounted, onUnmounted, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {iotDeviceSelectList, iotEventHandle, iotEventListPage, iotLatestList} from '@/api/iot';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import DeviceHistoryDialog from './DeviceHistoryDialog.vue';

/** 三路气体正常区间（kPa）。负压是负值：越接近 0 说明吸力越弱 */
const GAS_SPEC = [
  {key: 'o2Kpa', label: '中心供氧 O2', min: 400, max: 500, unit: 'kPa'},
  {key: 'vacuumKpa', label: '负压吸引 VAC', min: -70, max: -40, unit: 'kPa'},
  {key: 'airKpa', label: '压缩空气 AIR', min: 400, max: 500, unit: 'kPa'},
];
const STALE_MS = 120_000;
const MIN_ALARM_LEVEL = 2;

const loading = ref(false);
const loadError = ref('');
const lastRefresh = ref('');
const devices = ref([]);
const latest = ref([]);
const events = ref([]);
const eventLoading = ref(false);
const eventTotal = ref(0);
const pendingTotal = ref(0);
const pendingOnly = ref(true);
const eventPage = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
const trendVisible = ref(false);
const trendDevice = ref(null);

const latestByNo = computed(() => {
  const map = new Map();
  for (const d of latest.value) map.set(d.deviceNo, d);
  return map;
});

const boxes = computed(() => devices.value.map(dev => {
  const telemetry = latestByNo.value.get(dev.deviceNo) || null;
  const online = !!telemetry && !isStale(telemetry);
  const gases = GAS_SPEC.map(spec => {
    const value = num(metricValue(telemetry, spec.key));
    const abnormal = value != null && (value < spec.min || value > spec.max);
    return {
      ...spec,
      value,
      abnormal,
      rangeText: `${spec.min}~${spec.max}${spec.unit}`,
    };
  });
  const abnormalGas = gases.filter(g => g.abnormal);
  const flow = num(metricValue(telemetry, 'flowLpm'));
  const alarm = abnormalGas.length > 0;
  return {
    ...dev,
    telemetry,
    gases,
    flow,
    online,
    alarm,
    statusText: !telemetry ? '未上报' : !online ? '离线' : alarm ? '压力异常' : '正常',
    badgeType: alarm ? 'danger' : !online ? 'info' : 'success',
    headerClass: alarm ? 'bg-red-50 border-red-100' : 'bg-[#F2F7FC] border-gray-100',
    borderClass: alarm ? 'border-red-200' : 'border-gray-100',
    alarmText: abnormalGas.map(g => `${g.label} ${g.value}${g.unit} 超出 ${g.min}~${g.max}`).join('；'),
    lastSeen: telemetry?.ts ? String(telemetry.ts).slice(11, 19) : '',
  };
}).sort((a, b) => (b.alarm - a.alarm) || ((a.online ? 1 : 0) - (b.online ? 1 : 0))
    || String(a.deviceNo).localeCompare(String(b.deviceNo))));

const groups = computed(() => {
  const map = new Map();
  for (const box of boxes.value) {
    const key = box.deptName || '未归属科室';
    if (!map.has(key)) map.set(key, []);
    map.get(key).push(box);
  }
  return [...map.entries()].map(([deptName, items]) => ({
    deptName,
    items,
    alarmCount: items.filter(b => b.alarm).length,
  })).sort((a, b) => (b.alarmCount - a.alarmCount) || (b.items.length - a.items.length)
      || a.deptName.localeCompare(b.deptName));
});

const stats = computed(() => ({
  total: boxes.value.length,
  online: boxes.value.filter(b => b.online).length,
  alarm: boxes.value.filter(b => b.alarm).length,
  inUse: boxes.value.filter(b => b.online && b.flow > 0).length,
  pending: pendingTotal.value,
}));

const boxByNo = computed(() => new Map(boxes.value.map(b => [b.deviceNo, b])));
const siteState = (row) => {
  const box = boxByNo.value.get(row.deviceNo);
  if (!box) return {text: '终端不在档案', cls: 'text-gray-400'};
  if (!box.online) return {text: '离线，无法判断', cls: 'text-red-500'};
  if (row.eventCode === 'PRESSURE_LOW') {
    return box.alarm
        ? {text: box.alarmText || '压力仍偏低', cls: 'text-red-600 font-semibold'}
        : {text: '压力已恢复', cls: 'text-green-600'};
  }
  if (row.eventCode === 'REGULATOR_FAULT') {
    return box.alarm
        ? {text: '压力尚未恢复', cls: 'text-red-600 font-semibold'}
        : {text: '压力已恢复', cls: 'text-green-600'};
  }
  return {text: '', cls: 'text-gray-400'};
};

const deviceNameMap = computed(() => {
  const map = new Map();
  for (const d of devices.value) map.set(d.deviceNo, d.deviceName || d.deviceNo);
  return map;
});
const eventDeviceName = (row) => deviceNameMap.value.get(row.deviceNo) || row.deviceNo;

const metricValue = (device, key) => {
  const m = (device?.metrics || []).find(x => x.key === key);
  return m ? m.value : null;
};
const num = (v) => {
  const n = Number(v);
  return v == null || v === '' || !Number.isFinite(n) ? null : n;
};
const isStale = (d) => {
  if (!d?.ts) return true;
  const t = new Date(String(d.ts).replace(' ', 'T')).getTime();
  return !Number.isFinite(t) || Date.now() - t > STALE_MS;
};
const levelClass = (level) => (level === 3 ? 'text-red-600 font-semibold' : level === 2 ? 'text-orange-600' : 'text-gray-600');

const loadBoxes = async () => {
  const [devRes, latestRes] = await Promise.all([
    iotDeviceSelectList({deviceType: 'gas'}),
    iotLatestList({deviceType: 'gas'}),
  ]);
  devices.value = devRes.data || [];
  latest.value = latestRes.data || [];
};

const loadEvents = async ({silent = false} = {}) => {
  if (!silent) eventLoading.value = true;
  try {
    const [res, pendingRes] = await Promise.all([
      iotEventListPage({
        deviceType: 'gas',
        minLevel: MIN_ALARM_LEVEL,
        handledFlag: pendingOnly.value ? 0 : null,
        pageNum: eventPage.value.pageNum,
        pageSize: eventPage.value.pageSize,
      }),
      iotEventListPage({
        deviceType: 'gas',
        minLevel: MIN_ALARM_LEVEL,
        handledFlag: 0,
        pageNum: 1,
        pageSize: 1,
      }),
    ]);
    events.value = res.data?.records || [];
    eventTotal.value = Number(res.data?.total || 0);
    pendingTotal.value = Number(pendingRes.data?.total || 0);
  } catch (e) {
    console.error('加载气体告警失败', e);
  } finally {
    eventLoading.value = false;
  }
};

const reloadEvents = () => {
  eventPage.value.pageNum = 1;
  loadEvents();
};

/**
 * silent = true（10 秒轮询）：不加遮罩、失败不弹窗（弹窗会盖住大屏），保留上一帧数据；
 * silent = false（首屏/手动刷新）：照旧显示遮罩与错误。
 */
const loadAll = async ({silent = false} = {}) => {
  if (!silent) {
    loading.value = true;
    loadError.value = '';
  }
  try {
    await loadBoxes();
    await loadEvents({silent});
    lastRefresh.value = new Date().toLocaleTimeString('zh-CN', {hour12: false});
  } catch (e) {
    console.error('加载医用气体监测失败', e);
    loadError.value = e?.response?.data?.message || e?.message || '接口调用失败';
    if (!silent) ElMessage.error(loadError.value);
  } finally {
    if (!silent) loading.value = false;
  }
};

const openTrend = (box) => {
  trendDevice.value = {...box.telemetry, deviceNo: box.deviceNo, deviceType: 'gas', deviceTypeLabel: '医用气体终端'};
  trendVisible.value = true;
};

const handleEvent = async (row, flag) => {
  let remark;
  try {
    const r = await ElMessageBox.prompt(
        `处理告警「${row.eventCodeText || row.eventCode}」（${eventDeviceName(row)}），填写处理说明：`,
        flag === 1 ? '处理气体告警' : '忽略气体告警',
        {inputValue: flag === 1 ? '已通知气体班查看减压阀/管路' : '误报，无需处理', confirmButtonText: '确认', cancelButtonText: '取消'},
    );
    remark = r.value;
  } catch (e) {
    return;
  }
  try {
    await iotEventHandle({id: row.id, handledFlag: flag, remark});
    ElMessage.success(flag === 1 ? '已标记处理' : '已忽略');
    await loadEvents();
  } catch (e) {
    console.error('处理气体告警失败', e);
    ElMessage.error(e?.response?.data?.message || '处理失败');
  }
};

let timer = null;
onMounted(() => {
  loadAll();
  timer = setInterval(() => {
    // 轮询静默：不挂加载遮罩，避免每 10 秒整屏闪一次
    if (!document.hidden) loadAll({silent: true});
  }, 10000);
});
onUnmounted(() => {
  if (timer) clearInterval(timer);
});
</script>
