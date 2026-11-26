<template>
  <div>
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
        <el-form-item label="自动刷新">
          <el-switch v-model="query.autoRefresh" @change="resetTimer"/>
        </el-form-item>
        <el-form-item label="间隔">
          <el-select v-model="query.interval" :disabled="!query.autoRefresh" :fit-input-width="false"
                     style="width: 100px" @change="resetTimer">
            <el-option label="3 秒" :value="3"/>
            <el-option label="5 秒" :value="5"/>
            <el-option label="10 秒" :value="10"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Refresh" type="primary" @click="loadLatest()">刷新</el-button>
          <el-button :icon="RefreshLeft" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="text-[13px] text-gray-500">
        共 <b>{{ rows.length }}</b> 台设备 ·
        <b class="text-green-600">{{ onlineCount }}</b> 在线 ·
        <b :class="offlineCount ? 'text-red-500' : ''">{{ offlineCount }}</b> 离线（2 分钟无上报）·
        数据窗口：TDengine 时序库 · 刷新于 {{ lastRefreshAt || '—' }}
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
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
        <el-table-column label="归属 / 安装位置" min-width="210">
          <template #default="{ row }">
            <div class="text-[12px] text-gray-700">{{ row.deptName || '未归属科室' }}</div>
            <div class="text-[12px] text-gray-400 truncate">{{ row.location || '位置未登记' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="绑定对象" width="135">
          <template #default="{ row }">
            <span v-if="row.bindText" class="text-[12px] text-[#1269B5]">{{ row.bindText }}</span>
            <span v-else class="text-[12px] text-gray-400">—</span>
          </template>
        </el-table-column>
        <el-table-column label="最近上报" width="160">
          <template #default="{ row }">
            <span :class="isOnline(row) ? '' : 'text-red-500'">{{ fmtTime(row.ts) }}</span>
            <span class="text-gray-400 ml-1">{{ ago(row.ts) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="当前指标" min-width="390">
          <template #default="{ row }">
            <div class="flex flex-wrap gap-1">
              <el-tag v-for="m in row.metrics" :key="m.key" :type="metricTagType(m, row)" disable-transitions
                      effect="plain" size="small">
                {{ m.label }} {{ m.value }}{{ m.unit }}
              </el-tag>
            </div>
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
 * 物联网设备监控（菜单见 sql/357）
 *
 * 数据链路：设备 → MQTT(EMQX) → his-ms-iot 订阅落库 → TDengine → 本页。
 * 页面只读，不提供启停设备/改参数的操作：远程控制属另一条链路（指令下行 + 审计），还没做。
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
const query = reactive({deviceType: '', deviceNo: '', autoRefresh: true, interval: 5});

const filteredRows = computed(() => {
  const kw = query.deviceNo.trim().toUpperCase();
  if (!kw) return rows.value;
  return rows.value.filter(r => String(r.deviceNo || '').toUpperCase().includes(kw));
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

// 明确异常才标色：冷链温度越界、门磁打开、断电、管路阻塞/防拆。
// 温度只对冷链设备判定，环境/资产类温度不做 2~8℃ 推断。
const metricTagType = (m, row) => {
  const v = String(m.value ?? '').trim();
  if (['occluded', 'tamper'].includes(m.key) && v === '是') return 'danger';
  if (m.key === 'power' && v === '否') return 'danger';
  if (m.key === 'door' && v === '是') return 'warning';
  if (row?.deviceType === 'coldchain') {
    const n = parseFloat(v);
    if (m.key === 'temp' && Number.isFinite(n) && (n < 2 || n > 8)) return 'danger';
    if (m.key === 'humidity' && Number.isFinite(n) && (n < 35 || n > 75)) return 'warning';
  }
  return 'info';
};

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
          bindText: b ? `${b.bedNo ? b.bedNo + ' 床' : '床位未知'} · ${b.bindSourceText || ''}` : '',
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
