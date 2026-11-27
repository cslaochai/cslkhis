<template>
  <div class="p-4">
    <!-- 顶部：药房冷链概况 -->
    <div class="bg-white rounded-lg shadow-sm px-4 py-3 mb-4 flex flex-wrap items-center gap-x-6 gap-y-3">
      <div class="text-[20px] font-semibold text-[#1269B5]">
        药房冷链监控
        <span class="text-[14px] font-normal text-gray-500 ml-1">冷藏药品 2~8℃ · 温湿度与告警闭环</span>
      </div>

      <div class="flex items-center gap-6 ml-auto">
        <div class="text-center">
          <div class="text-[22px] font-bold leading-6">{{ stats.total }}</div>
          <div class="text-[12px] text-gray-500">冷藏柜</div>
        </div>
        <div class="text-center">
          <div class="text-[22px] font-bold leading-6 text-[#0E9488]">{{ stats.online }}</div>
          <div class="text-[12px] text-gray-500">在线</div>
        </div>
        <div class="text-center">
          <div class="text-[22px] font-bold leading-6" :class="stats.alarm ? 'text-red-600' : 'text-gray-400'">
            {{ stats.alarm }}
          </div>
          <div class="text-[12px] text-gray-500">温湿度超限</div>
        </div>
        <div class="text-center">
          <div class="text-[22px] font-bold leading-6" :class="stats.doorOpen ? 'text-orange-600' : 'text-gray-400'">
            {{ stats.doorOpen }}
          </div>
          <div class="text-[12px] text-gray-500">开门中</div>
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
      <el-button size="small" plain type="primary" @click="exportVisible = true">导出 GSP 记录</el-button>
    </div>

    <!-- 按药房分组：药房管理者先找"我这一摊"，不是先找设备编号 -->
    <div v-loading="loading" class="space-y-4">
      <div v-for="group in groups" :key="group.deptName" class="bg-white rounded-lg shadow-sm">
        <div class="flex items-center gap-2 px-4 py-2 border-b border-gray-100">
          <span class="text-[15px] font-semibold text-gray-800">{{ group.deptName }}</span>
          <span class="text-[12px] text-gray-400">{{ group.items.length }} 台冷藏柜</span>
          <el-tag v-if="group.alarmCount" size="small" type="danger" effect="plain" class="ml-auto">
            {{ group.alarmCount }} 台超限
          </el-tag>
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
              <div class="flex items-end gap-3">
                <div>
                  <span class="text-[34px] font-bold leading-9"
                        :class="box.alarm ? 'text-red-600' : 'text-[#1269B5]'">{{ box.temp == null ? '' : box.temp }}</span>
                  <span class="text-[13px] text-gray-500 ml-0.5">℃</span>
                </div>
                <div class="text-[12px] text-gray-400 pb-1">冷藏药品 2~8℃</div>
                <div class="ml-auto text-right pb-1">
                  <div class="text-[13px] text-gray-500">湿度
                    <b :class="box.humidityAlarm ? 'text-red-600' : 'text-gray-700'">{{ box.humidity == null ? '' : box.humidity + '%' }}</b>
                  </div>
                  <div class="text-[11px] text-gray-400 mt-0.5">参考 35~75%</div>
                  <div class="text-[13px] mt-0.5"
                       :class="box.doorOpen ? 'text-orange-600' : 'text-gray-500'">
                    柜门 <b>{{ box.doorOpen ? '开启' : (box.door == null ? '未上报' : '关闭') }}</b>
                  </div>
                </div>
              </div>

              <div class="mt-1 text-[12px] text-gray-500 flex items-center gap-3 flex-wrap">
                <span class="truncate">{{ box.location || '未填写安装位置' }}</span>
                <span class="text-gray-400 ml-auto">更新 {{ box.lastSeen || '' }}</span>
              </div>

              <div class="mt-1 text-[12px]" :class="box.summaryAlarm ? 'text-orange-600' : 'text-gray-500'">
                近24h {{ box.summaryText }}<span v-if="box.summaryAlarm">（曾超限）</span>
              </div>

              <div v-if="box.alarmText" class="mt-2 text-[13px] text-red-600">{{ box.alarmText }}</div>
              <div v-else-if="!box.online" class="mt-2 text-[13px] text-red-500">超过 2 分钟未上报，先查网络/网关</div>
              <div v-else-if="box.doorOpen" class="mt-2 text-[13px] text-orange-600">柜门开启中，温度可能短时抬头</div>

              <div class="mt-3 flex items-center justify-between text-[12px]">
                <span class="text-gray-400" :class="box.power === '否' ? 'text-red-500' : ''">电源 {{ powerText(box) }}</span>
                <span v-if="box.telemetry" class="text-[#1269B5] cursor-pointer" @click="openTrend(box)">温度趋势</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="!loading && !boxes.length" class="text-center text-gray-400 py-12">
      暂无冷链设备{{ loadError ? '（' + loadError + '）' : '' }}
    </div>

    <!-- 告警闭环：只列 2 级及以上（超温/压缩机故障/断电），日常开门记录不在这里 -->
    <div class="bg-white rounded-lg shadow-sm mt-4">
      <div class="flex flex-wrap items-center gap-4 px-4 py-3 border-b border-gray-100">
        <span class="text-[15px] font-semibold">冷链告警</span>
        <el-switch v-model="pendingOnly" size="small" active-text="只看未处理" @change="reloadEvents"/>
        <span class="text-[12px] text-gray-400">
          仅列超温、压缩机故障、断电等 2 级以上告警；每次开门/关门属日常作业记录，不进入告警列表。处理需留人、时间、说明。
        </span>
        <el-button size="small" text class="ml-auto" @click="loadEvents()">刷新</el-button>
      </div>
      <el-table v-loading="eventLoading" :data="events" size="small" style="width: 100%">
        <el-table-column label="时间" width="165">
          <template #default="{row}"><span class="text-[12px]">{{ row.ts }}</span></template>
        </el-table-column>
        <el-table-column label="冷藏柜" min-width="170">
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
    <ColdChainExportDialog v-model="exportVisible" :devices="devices"/>
  </div>
</template>

<script setup>
/**
 * 药房冷链监控（药剂科视角）。
 *
 * 口径对齐《药品经营质量管理规范》（GSP）附录 5 与海尔生物医疗 U-Cool/澳柯玛云眼：
 *   1) 先按药房分组，再列该药房的冷藏柜——药房管理者关心"我这一摊有没有事"，不是设备编号清单；
 *   2) 每台柜只给"当前温度 + 参考区间 + 门状态 + 最近上报"，不把原始指标摊给用户自己算；
 *   3) 告警只看 2 级以上（超温/压缩机故障/断电），开门/关门是日常作业记录，混进告警列表会淹没真问题；
 *   4) 超温/故障处理必须留人、时间、说明；温湿度按 5 分钟一轮导出，检查要的是归档记录。
 *
 * 数据源：biz_iot_device（名称/科室/位置）+ 时序库最新值（temp/humidity/door/power）+ biz_iot_event。
 */
import {computed, onMounted, onUnmounted, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {iotColdChainSummary, iotDeviceSelectList, iotEventHandle, iotEventListPage, iotLatestList} from '@/api/iot';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import DeviceHistoryDialog from './DeviceHistoryDialog.vue';
import ColdChainExportDialog from './ColdChainExportDialog.vue';

const TEMP_MIN = 2;
const TEMP_MAX = 8;
const HUM_MIN = 35;
const HUM_MAX = 75;
/** 冷柜 5 秒一条；超过 2 分钟没上报按离线 */
const STALE_MS = 120_000;
/** 告警列表只看 2 级及以上：1 级是开门/关门/温度恢复等作业记录 */
const MIN_ALARM_LEVEL = 2;

const loading = ref(false);
const loadError = ref('');
const lastRefresh = ref('');
const devices = ref([]);
const latest = ref([]);
const summaries = ref([]);
const events = ref([]);
const eventLoading = ref(false);
const eventTotal = ref(0);
const pendingTotal = ref(0);
const pendingOnly = ref(true);
const eventPage = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
const trendVisible = ref(false);
const trendDevice = ref(null);
const exportVisible = ref(false);

const latestByNo = computed(() => {
  const map = new Map();
  for (const d of latest.value) map.set(d.deviceNo, d);
  return map;
});

const summaryByNo = computed(() => {
  const map = new Map();
  for (const s of summaries.value) map.set(s.deviceNo, s);
  return map;
});

const boxes = computed(() => {
  const all = devices.value.map(dev => {
    const telemetry = latestByNo.value.get(dev.deviceNo) || null;
    const summary = summaryByNo.value.get(dev.deviceNo) || null;
    const temp = num(metricValue(telemetry, 'temp'));
    const humidity = num(metricValue(telemetry, 'humidity'));
    const door = metricValue(telemetry, 'door');
    const power = metricValue(telemetry, 'power');
    const online = !!telemetry && !isStale(telemetry);
    const over = temp != null && temp > TEMP_MAX;
    const under = temp != null && temp < TEMP_MIN;
    const humidityAlarm = humidity != null && (humidity < HUM_MIN || humidity > HUM_MAX);
    // 设备平台 door 上报是「是/否」，历史协议里也有 OPEN/CLOSE，两种都认
    const doorOpen = door === '是' || door === 'OPEN';
    const alarm = over || under || humidityAlarm;
    const summaryText = summary
        ? `${fmtNum(summary.minTemp)}~${fmtNum(summary.maxTemp)}℃ · ${summary.windowCount ?? 0} 个窗口`
        : '暂无归档记录';
    const summaryAlarm = !!summary && ((summary.minTemp != null && summary.minTemp < TEMP_MIN)
        || (summary.maxTemp != null && summary.maxTemp > TEMP_MAX));
    return {
      ...dev,
      telemetry,
      summary,
      temp,
      humidity,
      door,
      power,
      online,
      doorOpen,
      humidityAlarm,
      summaryText,
      summaryAlarm,
      alarm,
      statusText: !telemetry ? '未上报' : !online ? '离线'
          : over ? '超温' : under ? '低温' : humidityAlarm ? '湿度异常' : '正常',
      badgeType: alarm ? 'danger' : !online ? 'info' : doorOpen ? 'warning' : 'success',
      headerClass: alarm ? 'bg-red-50 border-red-100' : 'bg-[#F2F7FC] border-gray-100',
      borderClass: alarm ? 'border-red-200' : 'border-gray-100',
      alarmText: [
        over ? `柜温 ${temp}℃ 超过 ${TEMP_MAX}℃ 上限` : '',
        under ? `柜温 ${temp}℃ 低于 ${TEMP_MIN}℃ 下限` : '',
        humidityAlarm ? `湿度 ${humidity}% 超出 ${HUM_MIN}~${HUM_MAX}%` : '',
      ].filter(Boolean).join('；'),
      lastSeen: telemetry?.ts ? String(telemetry.ts).slice(11, 19) : '',
    };
  });
  // 告警置顶，其次离线，正常沉底
  return all.sort((a, b) => (b.alarm - a.alarm) || ((a.online ? 1 : 0) - (b.online ? 1 : 0))
      || String(a.deviceNo).localeCompare(String(b.deviceNo)));
});

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

/** 告警行要回答"现在是不是还在烧"：只看历史上报会让人以为柜子此刻仍超温 */
const boxByNo = computed(() => new Map(boxes.value.map(b => [b.deviceNo, b])));
const siteState = (row) => {
  const box = boxByNo.value.get(row.deviceNo);
  if (!box) return {text: '设备不在档案', cls: 'text-gray-400'};
  if (!box.online) return {text: '离线，无法判断', cls: 'text-red-500'};
  if (row.eventCode === 'TEMP_HIGH') {
    return box.alarm
        ? {text: `仍超限 ${box.temp}℃`, cls: 'text-red-600 font-semibold'}
        : {text: `已恢复 ${box.temp}℃`, cls: 'text-green-600'};
  }
  if (row.eventCode === 'COMPRESSOR_FAULT') {
    return box.alarm
        ? {text: '温度上冲中', cls: 'text-red-600 font-semibold'}
        : {text: '温度已回落', cls: 'text-green-600'};
  }
  return {text: '', cls: 'text-gray-400'};
};

const stats = computed(() => {
  const list = boxes.value;
  return {
    total: list.length,
    online: list.filter(b => b.online).length,
    alarm: list.filter(b => b.alarm).length,
    doorOpen: list.filter(b => b.online && b.doorOpen).length,
    pending: pendingTotal.value,
  };
});

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
const fmtNum = (v) => {
  const n = num(v);
  return n == null ? '' : n.toFixed(1);
};
/** 时序库布尔列统一翻成"是/否"，电源这个指标直接显示成"正常/断电"更像设备面板 */
const powerText = (box) => (box.power === '是' ? '正常' : box.power === '否' ? '断电' : '未上报');
const isStale = (d) => {
  if (!d?.ts) return true;
  const t = new Date(String(d.ts).replace(' ', 'T')).getTime();
  return !Number.isFinite(t) || Date.now() - t > STALE_MS;
};
const levelClass = (level) => (level === 3 ? 'text-red-600 font-semibold' : level === 2 ? 'text-orange-600' : 'text-gray-600');

const loadBoxes = async () => {
  const [devRes, latestRes, summaryRes] = await Promise.all([
    iotDeviceSelectList({deviceType: 'coldchain'}),
    iotLatestList({deviceType: 'coldchain'}),
    iotColdChainSummary(24),
  ]);
  devices.value = devRes.data || [];
  latest.value = latestRes.data || [];
  summaries.value = summaryRes.data || [];
};

const loadEvents = async ({silent = false} = {}) => {
  if (!silent) eventLoading.value = true;
  try {
    const [res, pendingRes] = await Promise.all([
      iotEventListPage({
        deviceType: 'coldchain',
        minLevel: MIN_ALARM_LEVEL,
        handledFlag: pendingOnly.value ? 0 : null,
        pageNum: eventPage.value.pageNum,
        pageSize: eventPage.value.pageSize,
      }),
      iotEventListPage({
        deviceType: 'coldchain',
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
    console.error('加载冷链告警失败', e);
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
    console.error('加载冷链监控失败', e);
    loadError.value = e?.response?.data?.message || e?.message || '接口调用失败';
    if (!silent) ElMessage.error(loadError.value);
  } finally {
    if (!silent) loading.value = false;
  }
};

const openTrend = (box) => {
  trendDevice.value = {...box.telemetry, deviceNo: box.deviceNo, deviceType: 'coldchain', deviceTypeLabel: '药品冷藏柜'};
  trendVisible.value = true;
};

const handleEvent = async (row, flag) => {
  let remark;
  try {
    const r = await ElMessageBox.prompt(
        `处理告警「${row.eventCodeText || row.eventCode}」（${eventDeviceName(row)}），填写处理说明：`,
        flag === 1 ? '处理冷链告警' : '忽略冷链告警',
        {inputValue: flag === 1 ? '已转移药品并复核柜温' : '误报，无需处理', confirmButtonText: '确认', cancelButtonText: '取消'},
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
    console.error('处理冷链告警失败', e);
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
