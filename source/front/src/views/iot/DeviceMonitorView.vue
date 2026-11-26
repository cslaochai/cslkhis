<template>
  <div>
    <!-- 查询卡：只放查询条件；刷新控制与统计摘要归表格工具条，避免条件/控制/摘要挤在一行 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="设备类型">
          <el-select v-model="query.deviceType" :fit-input-width="false" clearable placeholder="全部类型"
                     style="width: 170px" @change="loadLatest()">
            <el-option v-for="t in deviceTypes" :key="t.type" :label="t.label" :value="t.type"/>
          </el-select>
        </el-form-item>
        <el-form-item label="设备编号">
          <el-input v-model="query.deviceNo" clearable placeholder="如 PUMP-01" style="width: 170px"
                    @keyup.enter="loadLatest()"/>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Refresh" type="primary" @click="loadLatest()">查询</el-button>
          <el-button :icon="RefreshLeft" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div class="monitor-toolbar">
        <div class="monitor-stats">
          <span class="stat">共 <b>{{ filteredRows.length }}</b> 台</span>
          <span class="stat"><i class="dot dot-green"></i>在线 <b class="text-green-600">{{ onlineCount }}</b></span>
          <span class="stat">
            <i class="dot" :class="offlineCount ? 'dot-red' : 'dot-gray'"></i>
            离线 <b :class="offlineCount ? 'text-red-500' : 'text-gray-400'">{{ offlineCount }}</b>
          </span>
          <span v-if="alarmCount" class="stat">
            <i class="dot dot-red"></i>告警 <b class="text-red-500">{{ alarmCount }}</b>
          </span>
        </div>
        <div class="monitor-controls">
          <el-switch v-model="alarmOnly" size="small" active-text="只看告警"/>
          <el-switch v-model="query.autoRefresh" size="small" @change="resetTimer"/>
          <span class="text-[12px] text-gray-500">自动刷新</span>
          <el-select v-model="query.interval" :disabled="!query.autoRefresh" size="small"
                     :fit-input-width="false" style="width: 92px" @change="resetTimer">
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
        <el-table-column label="设备" min-width="190">
          <template #default="{ row }">
            <div class="text-[13px] font-medium text-gray-800 truncate">{{ row.deviceName || row.deviceNo }}</div>
            <div class="text-[12px] text-gray-400">{{ row.deviceNo }}</div>
          </template>
        </el-table-column>
        <el-table-column label="设备类型" width="115">
          <template #default="{ row }">
            <el-tag effect="plain" size="small">{{ row.deviceTypeLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="85">
          <template #default="{ row }">
            <el-tag :type="isOnline(row) ? 'success' : 'danger'" size="small">
              {{ isOnline(row) ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="归属 / 安装位置" min-width="200">
          <template #default="{ row }">
            <div class="text-[12px] text-gray-700">{{ row.deptName || '未归属科室' }}</div>
            <div class="text-[12px] text-gray-400 truncate">{{ row.location || '位置未登记' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="绑定对象" width="120">
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
        <el-table-column fixed="right" label="操作" width="90">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click.stop="openHistory(row)">趋势</el-button>
          </template>
        </el-table-column>
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
 * 定位口径（对标专业物联后台）：本页是运维台账总览，只放各类型设备共有的台账字段；
 * 类型专属实时指标不摊在总览表里，单台指标与趋势进「趋势」弹框。临床看护侧的床位卡片大屏
 * 归 WardMonitorView，不在本页重复。告警事件的处理闭环见告警中心页（IotEventView）。
 *
 * 为什么没有分页：最新值按「设备类型 → 超级表 PARTITION BY device_no」一条 SQL 取回，
 * 一台设备一行，量级是几十台（上限受设备台账约束），不需要分页。
 */
import {computed, onMounted, onUnmounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Refresh, RefreshLeft} from '@element-plus/icons-vue';
import {iotActiveBinds, iotDeviceSelectList, iotLatestList, iotSelectDeviceTypes} from '@/api/iot';
import DeviceHistoryDialog from './DeviceHistoryDialog.vue';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';

const {queryCardRef, tableMaxHeight} = useTableMaxHeight();

const deviceTypes = ref([]);
const rows = ref([]);
const loading = ref(false);
const lastRefreshAt = ref('');
const alarmOnly = ref(false);
const query = reactive({deviceType: '', deviceNo: '', autoRefresh: true, interval: 5});

const filteredRows = computed(() => {
  const kw = query.deviceNo.trim().toUpperCase();
  let list = rows.value;
  if (kw) list = list.filter(r => String(r.deviceNo || '').toUpperCase().includes(kw));
  if (alarmOnly.value) list = list.filter(isAlarmRow);
  return list;
});

const ONLINE_WINDOW_MS = 120 * 1000;
// 30 秒一跳的心跳，让「在线/离线」和「x 分钟前」随时间自然变化，而不是只在拉数据时才重算
const clock = ref(Date.now());
const isOnline = (row) => {
  if (!row.ts) return false;
  const t = new Date(String(row.ts).replace(' ', 'T')).getTime();
  return Number.isFinite(t) && clock.value - t <= ONLINE_WINDOW_MS;
};
const onlineCount = computed(() => filteredRows.value.filter(isOnline).length);
const offlineCount = computed(() => filteredRows.value.length - onlineCount.value);

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
const alarmCount = computed(() => filteredRows.value.filter(isAlarmRow).length);

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
 * silent = false（首屏/换类型/点刷新）：显示遮罩与错误提示。
 */
const loadLatest = async ({silent = false} = {}) => {
  if (!silent) loading.value = true;
  try {
    const [res, devRes, bindRes] = await Promise.all([
      iotLatestList({deviceType: query.deviceType || undefined}),
      iotDeviceSelectList({deviceType: query.deviceType || undefined}),
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

const reset = () => {
  Object.assign(query, {deviceType: '', deviceNo: ''});
  alarmOnly.value = false;
  loadLatest();
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
/* 工具条：左侧统计摘要，右侧刷新控制 */
.monitor-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.monitor-stats {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #6B85A0;
}

.stat b {
  font-weight: 700;
  margin-left: 2px;
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

.monitor-controls {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
