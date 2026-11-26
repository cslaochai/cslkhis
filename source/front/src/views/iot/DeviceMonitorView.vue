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
        <div class="kpi-label"><i class="dot dot-red"></i>告警</div>
        <div class="kpi-value text-red-500">{{ kpi.alarm }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">在线率</div>
        <div class="kpi-value text-[#0E9488]">{{ kpi.onlineRate }}</div>
      </div>
    </div>

    <el-card class="table-card" shadow="never">
      <div class="monitor-toolbar">
        <el-tabs v-model="activeType" class="type-tabs">
          <el-tab-pane name="">
            <template #label>
              全部 <span class="tab-count">{{ kpi.total }}</span>
              <i v-if="kpi.alarm" class="tab-alarm"></i>
            </template>
          </el-tab-pane>
          <el-tab-pane v-for="t in typeTabs" :key="t.type" :name="t.type">
            <template #label>
              {{ t.label }} <span class="tab-count">{{ t.count }}</span>
              <i v-if="t.alarmCount" class="tab-alarm"></i>
            </template>
          </el-tab-pane>
        </el-tabs>
        <div class="monitor-controls">
          <el-input v-model="query.deviceNo" clearable placeholder="设备编号" size="small"
                    style="width:150px" @keyup.enter="loadLatest()"/>
          <el-switch v-model="alarmOnly" size="small" active-text="只看告警"/>
          <el-switch v-model="query.autoRefresh" size="small" @change="resetTimer"/>
          <span class="text-[12px] text-gray-500">自动刷新</span>
          <el-select v-model="query.interval" :disabled="!query.autoRefresh" size="small"
                     :fit-input-width="false" style="width:92px" @change="resetTimer">
            <el-option label="3 秒" :value="3"/>
            <el-option label="5 秒" :value="5"/>
            <el-option label="10 秒" :value="10"/>
          </el-select>
          <el-button size="small" :icon="Refresh" @click="loadLatest()">刷新</el-button>
          <span class="text-[12px] text-gray-400" title="数据窗口：TDengine 时序库">刷新于 {{ lastRefreshAt || '—' }}</span>
        </div>
      </div>

      <el-table v-loading="loading" :data="filteredRows" :max-height="tableMaxHeight" stripe
                data-testid="iot-device-table" row-key="deviceNo" @row-click="openHistory">
        <el-table-column label="设备" min-width="180">
          <template #default="{ row }">
            <div class="text-[13px] font-medium text-gray-800 truncate">{{ row.deviceName || row.deviceNo }}</div>
            <div class="text-[12px] text-gray-400">{{ row.deviceNo }}</div>
          </template>
        </el-table-column>
        <el-table-column label="最新读数" min-width="150">
          <template #default="{ row }">
            <span v-if="primaryMetric(row)" class="text-[13px]" :class="isAlarmRow(row) ? 'text-red-600' : 'text-gray-700'">
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
import {computed, onMounted, onUnmounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Refresh} from '@element-plus/icons-vue';
import {iotActiveBinds, iotDeviceSelectList, iotLatestList, iotSelectDeviceTypes} from '@/api/iot';
import DeviceHistoryDialog from './DeviceHistoryDialog.vue';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';

const {queryCardRef, tableMaxHeight} = useTableMaxHeight();

const deviceTypes = ref([]);
const rows = ref([]);
const loading = ref(false);
const lastRefreshAt = ref('');
const alarmOnly = ref(false);
const activeType = ref('');
const query = reactive({deviceNo: '', autoRefresh: true, interval: 5});

const ONLINE_WINDOW_MS = 120 * 1000;
// 30 秒一跳的心跳，让「在线/离线」和「x 分钟前」随时间自然变化，而不是只在拉数据时才重算
const clock = ref(Date.now());

const isOnline = (row) => {
  if (!row.ts) return false;
  const t = new Date(String(row.ts).replace(' ', 'T')).getTime();
  return Number.isFinite(t) && clock.value - t <= ONLINE_WINDOW_MS;
};

// ============ 告警分级 ============
// 明确异常才标色：冷链温度越界、门磁打开、断电、管路阻塞/防拆。
// 温度只对冷链设备判定，环境/资产类温度不做 2~8℃ 推断。
// 返回 '' 表示正常；'warning' / 'danger' 用于告警计数与筛选。
const metricSeverity = (m, row) => {
  const v = String(m.value ?? '').trim();
  if (['occluded', 'tamper'].includes(m.key) && v === '是') return 'danger';
  if (m.key === 'power' && v === '否') return 'danger';
  if (m.key === 'door' && v === '是') return 'warning';
  if (row?.deviceType === 'coldchain') {
    const n = parseFloat(v);
    if (m.key === 'temp' && Number.isFinite(n) && (n < 2 || n > 8)) return 'danger';
    if (m.key === 'humidity' && Number.isFinite(n) && (n < 35 || n > 75)) return 'warning';
  }
  return '';
};

// 一台设备任一指标命中 warning/danger 即计为告警
const isAlarmRow = (row) => (row.metrics || []).some(m => metricSeverity(m, row));

/**
 * 主指标展示映射：每类设备取最能反映运行状态的一个读数在列表直出。
 * 这是前端展示口径（选哪个指标给运维先看），不是码值字典；指标键以后端 DeviceTypeSchema 为准。
 */
const PRIMARY_METRIC = {
  coldchain: {key: 'temp'},
  infusion: {key: 'remainingMl'},
  asset: {key: 'area'},
  env: {key: 'temp'},
  gas: {key: 'o2Kpa'},
  bed: {key: 'heartRate'},
  dialysis: {key: 'tmpMmHg'},
  cabinet: {key: 'stockPct'},
  uv: {key: 'running'},
  robot: {key: 'state'},
};
const primaryMetric = (row) => {
  const cfg = PRIMARY_METRIC[row.deviceType];
  if (!cfg) return null;
  const m = (row.metrics || []).find(x => x.key === cfg.key);
  if (!m || m.value == null || m.value === '') return null;
  return {label: m.label, value: m.value, unit: m.unit || ''};
};

const statusOf = (row) => {
  if (isAlarmRow(row)) return {type: 'danger', text: '告警'};
  return isOnline(row) ? {type: 'success', text: '在线'} : {type: 'info', text: '离线'};
};

// ============ 概览统计（基于全量设备，不受分组/搜索/只看告警影响） ============
const kpi = computed(() => {
  const total = rows.value.length;
  const online = rows.value.filter(isOnline).length;
  const alarm = rows.value.filter(isAlarmRow).length;
  const offline = total - online;
  const onlineRate = total ? `${Math.round(online / total * 100)}%` : '—';
  return {total, online, offline, alarm, onlineRate};
});

// 类型分组 Tab：全量统计每类台数与告警数，告警类带红点角标
const typeTabs = computed(() => deviceTypes.value.map(t => {
  const list = rows.value.filter(r => r.deviceType === t.type);
  return {type: t.type, label: t.label, count: list.length, alarmCount: list.filter(isAlarmRow).length};
}));

const filteredRows = computed(() => {
  const kw = query.deviceNo.trim().toUpperCase();
  let list = rows.value;
  if (activeType.value) list = list.filter(r => r.deviceType === activeType.value);
  if (kw) list = list.filter(r => String(r.deviceNo || '').toUpperCase().includes(kw));
  if (alarmOnly.value) list = list.filter(isAlarmRow);
  // 告警置顶 → 离线次之 → 在线按设备编号
  return [...list].sort((a, b) => {
    const rank = (r) => (isAlarmRow(r) ? 0 : isOnline(r) ? 1 : 2);
    const d = rank(a) - rank(b);
    return d !== 0 ? d : String(a.deviceNo).localeCompare(String(b.deviceNo));
  });
});

const fmtTime = (ts) => (ts ? String(ts).replace('T', ' ').slice(11, 19) : '—');
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
          // 绑定主文案只留床位；绑定方式（扫码/手工/申领）收进 title 提示，不与床号并列重复
          bindText: b ? (b.bedNo ? `${b.bedNo} 床` : '已绑定') : '',
          bindSourceText: b?.bindSourceText || '',
        };
      }));
      lastRefreshAt.value = new Date().toLocaleTimeString('zh-CN', {hour12: false});
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
const resetTimer = () => {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
  if (query.autoRefresh) {
    // 自动刷新走静默：不盖遮罩、不重排表格
    timer = setInterval(() => loadLatest({silent: true}), Math.max(3, query.interval) * 1000);
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
.dot-gray { background: #C0C4CC; }

.monitor-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  padding: 8px 20px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

/* 类型分组 Tab 紧凑化：去掉默认大间距与长下划线 */
.type-tabs {
  flex: 1;
  min-width: 0;
}

.type-tabs :deep(.el-tabs__header) {
  margin: 0;
}

.type-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}

.type-tabs :deep(.el-tabs__item) {
  height: 40px;
  padding: 0 14px;
}

.tab-count {
  display: inline-block;
  margin-left: 2px;
  font-size: 12px;
  color: #909399;
}

.tab-alarm {
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #F56C6C;
  margin-left: 5px;
}

.monitor-controls {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 8px;
}
</style>
