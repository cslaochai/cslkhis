<template>
  <div>
    <!-- 全局健康概览：进页面先看全院设备在线/告警，口径基于全量设备，不受分组/搜索影响 -->
    <div ref="queryCardRef" class="kpi-row">
      <div class="kpi-card">
        <div class="kpi-label">设备总数</div>
        <div class="kpi-value text-gray-800">{{ kpi.total }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label"><i class="dot dot-green"></i>在线</div>
        <div class="kpi-value text-green-600">{{ kpi.online }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label"><i class="dot" :class="kpi.offline ? 'dot-red' : 'dot-gray'"></i>离线</div>
        <div class="kpi-value" :class="kpi.offline ? 'text-red-500' : 'text-gray-400'">{{ kpi.offline }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label"><i class="dot dot-red"></i>严重</div>
        <div class="kpi-value text-red-500">{{ kpi.critical }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label"><i class="dot dot-warning"></i>警告</div>
        <div class="kpi-value text-[#E6A23C]">{{ kpi.warning }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">在线率</div>
        <div class="kpi-value text-[#0E9488]">{{ kpi.onlineRate }}</div>
      </div>
    </div>

    <el-card class="table-card" shadow="never">
      <el-tabs v-model="activeType" class="type-tabs">
        <el-tab-pane v-for="t in deviceTypes" :key="t.type" :name="t.type" :label="t.label"/>
      </el-tabs>
      <div class="monitor-controls">
        <el-input v-model="query.deviceNo" clearable placeholder="设备编号" size="small"
                  style="width:150px" @keyup.enter="loadLatest()"/>
        <el-switch v-model="alarmOnly" size="small" active-text="只看告警"/>
        <el-switch v-model="query.autoRefresh" size="small" @change="resetTimer"/>
        <span class="text-[12px] text-gray-500">自动刷新</span>
        <el-select v-model="query.interval" :disabled="!query.autoRefresh" size="small"
                   :fit-input-width="false" style="width:92px" @change="resetTimer">
          <el-option label="10 秒" :value="10"/>
          <el-option label="30 秒" :value="30"/>
          <el-option label="60 秒" :value="60"/>
        </el-select>
        <el-button size="small" :icon="Refresh" @click="loadLatest()">刷新</el-button>
        <span class="text-[12px] text-gray-400" title="数据窗口：TDengine 时序库">刷新于 {{ lastRefreshAt || '' }}</span>
      </div>

      <el-table v-loading="loading" :data="filteredRows" :max-height="tableMaxHeight" stripe
                data-testid="iot-device-table" row-key="deviceNo" @row-click="openHistory">
        <el-table-column label="设备" min-width="180">
          <template #default="{ row }">
            <div class="text-[13px] font-medium text-gray-800 truncate">{{ row.deviceName || row.deviceNo }}</div>
            <div class="text-[12px] text-gray-400">{{ row.deviceNo }}</div>
          </template>
        </el-table-column>
        <el-table-column label="供电" width="110">
          <template #default="{ row }">
            <el-tooltip v-if="row.powerType" :content="powerTypeHint(row.powerType)" placement="top">
              <el-tag size="small" :type="powerTypeTag(row.powerType)">{{ powerTypeText(row.powerType) }}</el-tag>
            </el-tooltip>
            <span v-else class="text-[12px] text-gray-400">—</span>
          </template>
        </el-table-column>
        <el-table-column label="最新读数" min-width="190">
          <template #default="{ row }">
            <!-- 物流机器人：设备侧只报英文任务码，运维看不懂，这里译成中文并与电量/载重排成两行 -->
            <template v-if="row.deviceType === 'robot'">
              <div class="flex items-center gap-1.5">
                <el-tooltip placement="top" :content="`设备原始上报码 ${robotState(row).code}`">
                  <el-tag size="small" :type="robotState(row).tag">{{ robotState(row).text }}</el-tag>
                </el-tooltip>
                <span v-if="metricVal(row, 'location')" class="text-[12px] text-gray-500 truncate">
                  {{ metricVal(row, 'location') }}
                </span>
              </div>
              <div class="text-[12px] text-gray-500">
                电量 {{ metricText(row, 'battery', '%') }} · 载重 {{ robotLoadText(row) }}
              </div>
            </template>
            <span v-else-if="primaryMetric(row)" class="text-[13px]" :class="isAlarmRow(row) ? 'text-red-600' : 'text-gray-700'">
              {{ primaryMetric(row).label }} {{ primaryMetric(row).value }}{{ primaryMetric(row).unit }}
            </span>
            <span v-else class="text-[12px] text-gray-400">—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="85">
          <template #default="{ row }">
            <el-tag :type="statusOf(row).type" size="small">{{ statusOf(row).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="归属 / 安装位置" min-width="190">
          <template #default="{ row }">
            <div class="text-[12px] text-gray-700">{{ row.deptName || '未归属科室' }}</div>
            <div class="text-[12px] text-gray-400 truncate">{{ row.location || '位置未登记' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="绑定对象" width="110">
          <template #default="{ row }">
            <span v-if="row.bindText" class="text-[12px] text-[#1269B5]"
                  :title="row.bindSourceText ? `绑定方式：${row.bindSourceText}` : ''">{{ row.bindText }}</span>
            <span v-else class="text-[12px] text-gray-400">—</span>
          </template>
        </el-table-column>
        <el-table-column label="最近上报" width="150">
          <template #default="{ row }">
            <span :class="isOnline(row) ? '' : 'text-red-500'">{{ fmtTime(row.ts) }}</span>
            <span class="text-gray-400 ml-1">{{ ago(row.ts) }}</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="85">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click.stop="openHistory(row)">趋势</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty :description="loading ? '加载中…' : '暂无设备'"/>
        </template>
      </el-table>
    </el-card>

    <DeviceHistoryDialog v-model="historyVisible" :device="historyDevice"/>
  </div>
</template>

<script setup>
/**
 * 物联网设备监控（设备科/运维视角的台账总览，菜单见 sql/357）
 *
 * 数据链路：设备 → MQTT(EMQX) → his-ms-iot 订阅落库 → TDengine → 本页。
 * 页面只读，不提供启停设备/改参数的操作：远程控制属另一条链路（指令下行 + 审计），还没做。
 *
 * 定位口径（对标专业物联后台）：本页是运维台账总览，按「全局健康概览 → 设备类型分组 → 单台读数」
 * 三层组织视线。类型专属的完整指标与趋势进「趋势」弹框；临床看护侧的床位卡片大屏归 WardMonitorView，
 * 不在本页重复。告警事件的处理闭环见告警中心页（IotEventView）。
 *
 * 为什么没有分页：最新值按「设备类型 → 超级表 PARTITION BY device_no」一条 SQL 取回，
 * 一台设备一行，量级是几十台（上限受设备台账约束），不需要分页。
 */
import {computed, onMounted, onUnmounted, reactive, ref, watch} from 'vue';
import {useRoute} from 'vue-router';
import {ElMessage} from 'element-plus';
import {Refresh} from '@element-plus/icons-vue';
import {iotActiveBinds, iotDeviceSelectList, iotLatestList, iotSelectDeviceTypes} from '@/api/iot';
import DeviceHistoryDialog from './DeviceHistoryDialog.vue';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';

const {queryCardRef, tableMaxHeight} = useTableMaxHeight();
const route = useRoute();

const deviceTypes = ref([]);
const rows = ref([]);
const loading = ref(false);
const lastRefreshAt = ref('');
const alarmOnly = ref(false);
const activeType = ref('');
// 自动刷新间隔：物联网监控全站统一 10 秒一档（其余 5 个监控页同为 10 秒），
// 设备台账几十台、TDengine 一条 SQL 取最新值，10 秒已经够及时；3/5 秒只多耗库不带来信息增量。
const REFRESH_SEC = 10;
const query = reactive({deviceNo: '', autoRefresh: true, interval: REFRESH_SEC});

const ONLINE_WINDOW_MS = 120 * 1000;
// 30 秒一跳的心跳，让「在线/离线」和「x 分钟前」随时间自然变化，而不是只在拉数据时才重算
const clock = ref(Date.now());

const isOnline = (row) => {
  if (!row.ts) return false;
  const t = new Date(String(row.ts).replace(' ', 'T')).getTime();
  return Number.isFinite(t) && clock.value - t <= ONLINE_WINDOW_MS;
};

// ============ 告警状态 ============
// 告警以事件中心为准：后端在 latestList 上回传 alarmLevel（2-警告 3-严重，null=无未处理告警）。
// 不在前端拿最新遥测快照二次推断——瞬时开门/取物等状态流水不该被置顶标红；
// 告警与告警中心闭环联动：事件被处理/忽略后，下次刷新对应设备即不再告警。
const isAlarmRow = (row) => row.alarmLevel != null;

/**
 * 主指标展示映射：每类设备取最能反映运行状态的一个读数在列表直出。
 * 这是前端展示口径（选哪个指标给运维先看），不是码值字典；指标键以后端 DeviceTypeSchema 为准。
 * 物流机器人不走这条：它有状态/位置/电量/载重四项，单独渲染（见模板 robot 分支）。
 */
const PRIMARY_METRIC = {
  coldchain: {key: 'temp'},
  infusion: {key: 'remainingMl'},
  asset: {key: 'area'},
  env: {key: 'temp'},
  gas: {key: 'o2Kpa'},
  bed: {key: 'heartRate'},
  monitor: {key: 'heartRate'},
  dialysis: {key: 'tmpMmHg'},
  cabinet: {key: 'stockPct'},
  uv: {key: 'running'},
};
const primaryMetric = (row) => {
  const cfg = PRIMARY_METRIC[row.deviceType];
  if (!cfg) return null;
  const m = (row.metrics || []).find(x => x.key === cfg.key);
  if (!m || m.value == null || m.value === '') return null;
  return {label: m.label, value: m.value, unit: m.unit || ''};
};

/** 按指标键取原始值（机器人读数和趋势都用它，避免各处自己 find）。 */
const metricVal = (row, key) => {
  const m = (row.metrics || []).find(x => x.key === key);
  return m && m.value != null && m.value !== '' ? m.value : null;
};
const metricText = (row, key, unit) => {
  const v = metricVal(row, key);
  return v == null ? '—' : `${v}${unit}`;
};

/**
 * 物流机器人任务状态码 → 中文。设备侧报的是 TASK_STATE 英文枚举（IDLE/DELIVERING…），
 * 直接把原码丢给运维没人看得懂；没登记的新码原样显示，tooltip 里带原始码方便对报文排查。
 */
const ROBOT_STATE = {
  IDLE: {text: '待命', tag: 'info'},
  LOADING: {text: '装货中', tag: 'warning'},
  DELIVERING: {text: '配送中', tag: 'success'},
  WAITING_ELEVATOR: {text: '等电梯', tag: 'warning'},
  SIGNING: {text: '签收中', tag: 'success'},
  ARRIVED: {text: '已到达', tag: 'success'},
  RETURNING: {text: '返程中', tag: 'primary'},
  CHARGING: {text: '回桩充电', tag: 'primary'},
  PAUSED: {text: '已暂停', tag: 'info'},
};
const robotState = (row) => {
  const code = metricVal(row, 'state');
  if (code == null) return {code: '—', text: '无读数', tag: 'info'};
  const hit = ROBOT_STATE[String(code).toUpperCase()];
  return {code: String(code), text: hit?.text || String(code), tag: hit?.tag || 'info'};
};

/**
 * 机器人载重：只报一个 kg 看不出"货装到哪一步了"。车上一箱一箱装、一箱一箱卸，
 * 所以显示成「4.4/9.0kg（2/4 箱）」——装货中能看到数字往上爬，签收中能看到往下落。
 */
const robotLoadText = (row) => {
  const load = metricVal(row, 'loadKg');
  if (load == null) return '—';
  const trip = metricVal(row, 'tripLoadKg');
  const on = metricVal(row, 'boxesOnboard');
  const cnt = metricVal(row, 'boxCount');
  if (trip != null && Number(cnt) > 0) {
    return `${Number(load).toFixed(1)}/${Number(trip).toFixed(1)}kg（${on ?? 0}/${cnt} 箱）`;
  }
  return `${load}kg`;
};

const statusOf = (row) => {
  if (isAlarmRow(row)) {
    return row.alarmLevel === 3
      ? {type: 'danger', text: '严重'}
      : {type: 'warning', text: '警告'};
  }
  return isOnline(row) ? {type: 'success', text: '在线'} : {type: 'info', text: '离线'};
};

/**
 * 供电方式（411 台账列）：只区分"怎么供电"，不推断当前有没有电。
 * 插电款的"没电"是插座/停电问题，派人接电；电池款才是派人换电池/回桩。
 */
const POWER_TYPE = {
  mains: {text: '插电', tag: 'info', hint: '插电供电，断电即停机/离线，不涉及换电池'},
  mains_backup: {text: '插电+后备电池', tag: 'success', hint: '常态插电满电，拔电才用后备电池，接回电源即回充'},
  battery: {text: '电池·换电', tag: 'warning', hint: '电池供电，电量低需现场更换电池/腕带，绑定关系不变'},
  battery_dock: {text: '电池·回桩', tag: 'warning', hint: '电池供电，电量低自动回充电桩充电'},
};
const powerTypeText = (t) => POWER_TYPE[t]?.text || t;
const powerTypeTag = (t) => POWER_TYPE[t]?.tag || 'info';
const powerTypeHint = (t) => POWER_TYPE[t]?.hint || '';

// ============ 概览统计（基于全量设备，不受分组/搜索/只看告警影响） ============
const kpi = computed(() => {
  const total = rows.value.length;
  const online = rows.value.filter(isOnline).length;
  const critical = rows.value.filter(r => r.alarmLevel === 3).length;
  const warning = rows.value.filter(r => r.alarmLevel === 2).length;
  const offline = total - online;
  const onlineRate = total ? `${Math.round(online / total * 100)}%` : '';
  return {total, online, offline, critical, warning, onlineRate};
});

const filteredRows = computed(() => {
  const kw = query.deviceNo.trim().toUpperCase();
  let list = rows.value;
  if (activeType.value) list = list.filter(r => r.deviceType === activeType.value);
  if (kw) list = list.filter(r => String(r.deviceNo || '').toUpperCase().includes(kw));
  if (alarmOnly.value) list = list.filter(isAlarmRow);
  // 严重 → 警告 → 在线 → 离线，同档按设备编号
  return [...list].sort((a, b) => {
    const rank = (r) => (r.alarmLevel === 3 ? 0 : r.alarmLevel === 2 ? 1 : isOnline(r) ? 2 : 3);
    const d = rank(a) - rank(b);
    return d !== 0 ? d : String(a.deviceNo).localeCompare(String(b.deviceNo));
  });
});

const fmtTime = (ts) => (ts ? String(ts).replace('T', ' ').slice(11, 19) : '');
const ago = (ts) => {
  if (!ts) return '';
  const t = new Date(String(ts).replace(' ', 'T')).getTime();
  if (!Number.isFinite(t)) return '';
  const sec = Math.max(0, Math.round((clock.value - t) / 1000));
  if (sec < 60) return `${sec}s前`;
  if (sec < 3600) return `${Math.floor(sec / 60)}m前`;
  return `${Math.floor(sec / 3600)}h前`;
};

const loadTypes = async () => {
  try {
    const res = await iotSelectDeviceTypes();
    if (res.code === 200) deviceTypes.value = res.data || [];
  } catch (e) {
    console.error('加载设备类型失败', e);
  }
};

/**
 * 按设备编号原地合并：自动刷新拿回的是全新行对象，直接换 rows 数组会让 el-table 整表重排
 * （行高、滚动位置、选中态都可能跳），这就是「闪一下」。同 key 复用行对象，只改属性。
 */
const mergeRows = (list) => {
  const old = new Map(rows.value.map(r => [r.deviceNo, r]));
  rows.value = list.map(n => {
    const o = old.get(n.deviceNo);
    if (!o) return n;
    old.delete(n.deviceNo);
    Object.assign(o, n);
    return o;
  });
};

/**
 * 从告警弹框跳进来：`?deviceNo=PUMP-01&trend=1`。
 * 先把列表筛到这台设备，再自动打开它的趋势弹框——电量曲线就在趋势里，护士不用自己再找一遍。
 */
let routeTrendOpened = false;
const applyRouteFocus = () => {
  const no = String(route.query.deviceNo || '').trim();
  if (!no) return;
  if (!query.deviceNo) query.deviceNo = no;
  if (route.query.trend !== '1' || routeTrendOpened) return;
  const row = rows.value.find(r => r.deviceNo === no);
  if (!row) return;
  routeTrendOpened = true;
  openHistory(row);
};

/**
 * silent = true（自动刷新）：不置 loading、不弹失败提示，失败保留上一帧数据；
 * silent = false（首屏/点刷新）：显示遮罩与错误提示。
 */
const loadLatest = async ({silent = false} = {}) => {
  if (!silent) loading.value = true;
  try {
    const [res, devRes, bindRes] = await Promise.all([
      iotLatestList({}),
      iotDeviceSelectList({}),
      iotActiveBinds({}).catch((e) => {
        console.error('加载设备绑定关系失败，设备清单仍可查看', e);
        return {data: []};
      }),
    ]);
    if (res.code === 200) {
      const devices = new Map((devRes.data || []).map(d => [d.deviceNo, d]));
      const binds = new Map((bindRes.data || []).map(b => [b.deviceNo, b]));
      mergeRows((res.data || []).map(r => {
        const d = devices.get(r.deviceNo) || {};
        const b = binds.get(r.deviceNo);
        return {
          ...r,
          deviceName: d.deviceName || '',
          deptName: d.deptName || '',
          location: d.location || '',
          powerType: d.powerType || '',
          // 绑定主文案只留床位；绑定方式（扫码/手工/申领）收进 title 提示，不与床号并列重复
          bindText: b ? (b.bedNo ? `${b.bedNo} 床` : '已绑定') : '',
          bindSourceText: b?.bindSourceText || '',
        };
      }));
      lastRefreshAt.value = new Date().toLocaleTimeString('zh-CN', {hour12: false});
      applyRouteFocus();
    } else if (!silent) {
      ElMessage.error(res.message || '查询失败');
    }
  } catch (e) {
    console.error(e);
  } finally {
    if (!silent) loading.value = false;
  }
};

let timer = null;
let clockTimer = null;

// 同样停在设备监控页时又从告警弹框点进来：查询参数变了，得把趋势重新打开一次
watch(() => route.query.deviceNo, () => {
  if (!route.query.deviceNo) return;
  routeTrendOpened = false;
  applyRouteFocus();
});
const resetTimer = () => {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
  if (query.autoRefresh) {
    // 自动刷新走静默：不盖遮罩、不重排表格
    timer = setInterval(() => loadLatest({silent: true}), Math.max(REFRESH_SEC, query.interval) * 1000);
  }
};

const historyVisible = ref(false);
const historyDevice = ref(null);
const openHistory = (row) => {
  if (!row?.deviceNo) return;
  historyDevice.value = row;
  historyVisible.value = true;
};

onMounted(() => {
  loadTypes();
  loadLatest();
  resetTimer();
  clockTimer = setInterval(() => { clock.value = Date.now(); }, 30 * 1000);
});
onUnmounted(() => {
  if (timer) clearInterval(timer);
  if (clockTimer) clearInterval(clockTimer);
});
</script>

<style scoped>
/* 顶部全局健康概览卡 */
.kpi-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}

.kpi-card {
  flex: 1;
  min-width: 128px;
  background: #fff;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  padding: 14px 16px;
}

.kpi-label {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #6B85A0;
}

.kpi-value {
  font-size: 22px;
  font-weight: 700;
  margin-top: 4px;
}

.dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 4px;
}

.dot-green { background: #0E9488; }
.dot-red { background: #F56C6C; }
.dot-warning { background: #E6A23C; }
.dot-gray { background: #C0C4CC; }

/* 卡片内边距对齐日志审计：body 20px，页签/工具栏/表格统一内缩（不套 table-card 的 0 内边距） */
.table-card :deep(.el-card__body) {
  padding: 20px;
}

/* 类型页签：沿用项目默认 line 型 el-tabs（同日志审计），不覆盖页签高度/间距 */
.type-tabs {
  margin: 0;
}

.type-tabs :deep(.el-tabs__header) {
  margin: 0 0 15px;
}

.monitor-controls {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
</style>
