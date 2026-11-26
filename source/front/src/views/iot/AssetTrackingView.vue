<template>
  <div class="p-4 space-y-4">
    <!-- 设备科/护士站找设备：先看"现在在哪"，再看"该不该保养" -->
    <div class="bg-white rounded-lg shadow-sm px-5 py-4 flex flex-wrap items-center gap-6">
      <div class="min-w-[240px]">
        <div class="flex items-center gap-2">
          <h2 class="text-[18px] font-semibold text-gray-800">资产定位与维保</h2>
          <el-tag size="small" effect="plain">定位信标</el-tag>
        </div>
        <div class="text-[12px] text-gray-500 mt-1">在用设备实时位置、离位告警、信标电量与维保到期提醒</div>
      </div>

      <div class="flex items-center gap-2">
        <el-input v-model="keyword" placeholder="设备名称 / 编码 / 标签号" clearable style="width: 220px"/>
        <el-select v-model="areaFilter" placeholder="全部区域" style="width: 210px" clearable>
          <el-option label="全部区域" value="ALL"/>
          <el-option v-for="a in areaOptions" :key="a" :label="a" :value="a"/>
        </el-select>
        <el-checkbox v-model="outOnly">只看离位</el-checkbox>
      </div>

      <div class="ml-auto flex items-center gap-5 text-center">
        <div>
          <div class="text-[22px] font-bold leading-6 text-[#1269B5]">{{ stats.total }}</div>
          <div class="text-[12px] text-gray-500">定位标签</div>
        </div>
        <div>
          <div class="text-[22px] font-bold leading-6 text-gray-800">{{ stats.online }}</div>
          <div class="text-[12px] text-gray-500">在线</div>
        </div>
        <div>
          <div class="text-[22px] font-bold leading-6" :class="stats.out ? 'text-red-600' : 'text-gray-800'">{{ stats.out }}</div>
          <div class="text-[12px] text-gray-500">离位</div>
        </div>
        <div>
          <div class="text-[22px] font-bold leading-6" :class="stats.lowBattery ? 'text-orange-600' : 'text-gray-800'">{{ stats.lowBattery }}</div>
          <div class="text-[12px] text-gray-500">低电（&lt;20%）</div>
        </div>
        <div>
          <div class="text-[22px] font-bold leading-6" :class="stats.maintainDue ? 'text-red-600' : 'text-gray-800'">{{ stats.maintainDue }}</div>
          <div class="text-[12px] text-gray-500">维保预警</div>
        </div>
        <div>
          <div class="text-[22px] font-bold leading-6" :class="stats.pending ? 'text-red-600' : 'text-gray-800'">{{ stats.pending }}</div>
          <div class="text-[12px] text-gray-500">未闭环告警</div>
        </div>
      </div>
      <div class="flex items-center gap-2 text-[12px] text-gray-400">
        <span class="inline-block w-2 h-2 rounded-full bg-green-500"></span>
        自动刷新 15s · {{ lastRefresh || '—' }}
      </div>
    </div>

    <el-alert v-if="loadError" :title="loadError" type="error" show-icon :closable="false"/>

    <div v-loading="loading" class="space-y-4">
      <div v-for="zone in groups" :key="zone.area" class="bg-white rounded-lg shadow-sm p-4">
        <div class="flex items-center gap-3 mb-3">
          <span class="text-[15px] font-semibold text-gray-800">{{ zone.area }}</span>
          <span class="text-[12px] text-gray-500">{{ zone.items.length }} 台设备</span>
          <span v-if="zone.outCount" class="text-[12px] text-red-600">{{ zone.outCount }} 台离位</span>
          <span class="ml-auto text-[12px]" :class="zone.alarmCount ? 'text-red-600' : 'text-green-600'">
            {{ zone.alarmCount ? `${zone.alarmCount} 台不在授权区` : '位置正常' }}
          </span>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4 gap-3">
          <div v-for="box in zone.items" :key="box.deviceNo"
               class="rounded-lg border overflow-hidden transition hover:shadow-md"
               :class="box.out ? 'border-red-200' : 'border-gray-100'">
            <div class="px-3 py-2 flex items-center gap-2" :class="box.out ? 'bg-red-50' : 'bg-[#F2F7FC]'">
              <span class="text-[14px] font-semibold text-gray-800 truncate">{{ box.equipmentName }}</span>
              <el-tag class="ml-auto shrink-0" size="small" :type="box.badgeType" effect="dark">{{ box.statusText }}</el-tag>
            </div>

            <div class="px-3 py-3">
              <div class="text-[12px] text-gray-500 flex items-center gap-2">
                <span class="font-mono">{{ box.equipmentCode || '台账未建档' }}</span>
                <span class="text-gray-400">标签 {{ box.deviceNo }}</span>
                <span class="ml-auto text-gray-400">更新 {{ box.lastSeen || '—' }}</span>
              </div>
              <div v-if="box.equipmentDept || box.equipmentStatus" class="mt-0.5 text-[12px] text-gray-500">
                <span v-if="box.equipmentDept">使用科室 {{ box.equipmentDept }}</span>
                <span v-if="box.equipmentStatus" class="ml-2" :class="box.equipmentStatus === '在用' ? 'text-green-600' : 'text-orange-600'">
                  台账 {{ box.equipmentStatus }}
                </span>
              </div>

              <div class="grid grid-cols-3 gap-2 mt-3">
                <div class="rounded bg-gray-50 px-2 py-2">
                  <div class="text-[11px] text-gray-400">信标电量</div>
                  <div class="text-[19px] font-semibold leading-6" :class="box.battery != null && box.battery < 20 ? 'text-red-600' : 'text-gray-800'">
                    {{ box.battery == null ? '—' : box.battery + '%' }}
                  </div>
                </div>
                <div class="rounded bg-gray-50 px-2 py-2">
                  <div class="text-[11px] text-gray-400">信号</div>
                  <div class="text-[19px] font-semibold leading-6 text-gray-800">{{ box.signalText }}</div>
                </div>
                <div class="rounded bg-gray-50 px-2 py-2">
                  <div class="text-[11px] text-gray-400">维保</div>
                  <div class="text-[14px] font-semibold leading-6 truncate" :class="box.maintain.cls">{{ box.maintain.text }}</div>
                </div>
              </div>

              <div class="mt-2 text-[12px] text-gray-500">
                授权区 <span class="text-gray-700">{{ box.homeArea || '未登记' }}</span>
              </div>
              <div v-if="box.out" class="mt-1 text-[13px] text-red-600">
                当前位于 {{ box.area }}，已离开授权区
              </div>
              <div v-else-if="!box.online" class="mt-1 text-[13px] text-red-500">超过 2 分钟未上报，先查网关/信标电量</div>

              <div class="mt-1 flex flex-wrap items-center gap-x-3 gap-y-1 text-[12px] text-gray-500">
                <span>上次保养 {{ box.lastMaintainDate || '—' }}</span>
                <span v-if="box.meteringValidUntil" :class="box.meteringExpired ? 'text-red-600' : 'text-gray-500'">
                  计量至 {{ box.meteringValidUntil }}{{ box.meteringExpired ? '（已过期）' : '' }}
                </span>
              </div>

              <div class="mt-2 flex items-center justify-between text-[12px]">
                <span class="text-gray-400">{{ box.equipmentModel || '型号未登记' }}</span>
                <el-button link type="primary" :disabled="!box.online" @click="openTrend(box)">轨迹</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && !groups.length" description="没有匹配的定位设备"/>
    </div>

    <div class="bg-white rounded-lg shadow-sm p-4">
      <div class="flex items-center gap-3 mb-2">
        <span class="text-[15px] font-semibold text-gray-800">离位告警</span>
        <span class="text-[12px] text-gray-500">设备离开授权区域会推送 2 级告警；归还后自动记"已归还"</span>
        <div class="ml-auto flex items-center gap-3">
          <el-checkbox v-model="pendingOnly" @change="reloadEvents">只看未处理</el-checkbox>
          <el-button size="small" @click="loadEvents()">刷新</el-button>
        </div>
      </div>

      <el-table :data="events" v-loading="eventLoading" size="small" height="300">
        <el-table-column prop="ts" label="时间" width="150">
          <template #default="{row}">{{ String(row.ts || '').replace('T', ' ').slice(5, 16) }}</template>
        </el-table-column>
        <el-table-column label="设备" min-width="180">
          <template #default="{row}">
            <span class="font-medium">{{ equipmentOf(row.deviceNo).equipmentName || row.deviceNo }}</span>
            <span class="text-[12px] text-gray-400 ml-2">{{ row.deviceNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="事件" width="120">
          <template #default="{row}">
            <span :class="levelClass(row.level)">{{ row.eventCodeText || row.eventCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="描述" min-width="230" show-overflow-tooltip/>
        <el-table-column label="现场状态" width="190">
          <template #default="{row}">
            <span :class="siteState(row).cls">{{ siteState(row).text }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{row}">
            <el-tag size="small" :type="row.handledFlag === 0 ? 'warning' : 'info'" effect="plain">
              {{ row.handledFlagText || (row.handledFlag === 0 ? '待处理' : '已处理') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{row}">
            <template v-if="row.handledFlag === 0">
              <el-button link type="primary" @click="handleEvent(row, 1)">处理</el-button>
              <el-button link type="info" @click="handleEvent(row, 2)">忽略</el-button>
            </template>
            <span v-else class="text-[12px] text-gray-400">{{ row.handleBy || '—' }}</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="flex justify-end mt-2">
        <el-pagination
            v-model:current-page="eventPage.pageNum"
            :page-size="eventPage.pageSize"
            :total="eventTotal"
            layout="total, prev, pager, next"
            @current-change="loadEvents"/>
      </div>
    </div>

    <DeviceHistoryDialog v-model="trendVisible" :device="trendDevice"/>
  </div>
</template>

<script setup>
/**
 * 资产定位与维保（设备科 + 护士站共用）。
 *
 * 定位：信标随设备移动，biz_iot_device 只存"标签 + 绑定的设备台账ID + 授权区域"，位置以时序库 area 为准。
 * 维保：不在 iot 模块直连 platform 的 sys_equipment（跨模块），页面调设备台账接口按 equipmentId 补
 *       设备编码/型号/最近维保/下次维保，iot 侧只负责"这台设备现在在哪、信标还有没有电"。
 * 告警：OUT_OF_AREA（2 级）进告警表；归还/位置变化属作业记录，不刷护士站。
 */
import {computed, onMounted, onUnmounted, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {equipmentListPage} from '@/api/equipment';
import {iotDeviceSelectList, iotEventHandle, iotEventListPage, iotLatestList} from '@/api/iot';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import DeviceHistoryDialog from './DeviceHistoryDialog.vue';

const STALE_MS = 120_000;
const MIN_ALARM_LEVEL = 2;
const LOW_BATTERY = 20;
const MAINTAIN_WARN_DAYS = 30;

const loading = ref(false);
const loadError = ref('');
const lastRefresh = ref('');
const devices = ref([]);
const latest = ref([]);
const equipment = ref([]);

const keyword = ref('');
const areaFilter = ref('ALL');
const outOnly = ref(false);

const events = ref([]);
const eventLoading = ref(false);
const eventTotal = ref(0);
const pendingTotal = ref(0);
const pendingOnly = ref(true);
const eventPage = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});

const trendVisible = ref(false);
const trendDevice = ref(null);

const equipmentById = computed(() => {
  const map = new Map();
  for (const e of equipment.value) map.set(String(e.id), e);
  return map;
});

/** 设备台账不跟 15 秒定位轮询一起拉：5 分钟刷新一次，维保状态变化足够及时 */
let equipmentLoadedAt = 0;
const latestByNo = computed(() => {
  const map = new Map();
  for (const d of latest.value) map.set(d.deviceNo, d);
  return map;
});

const boxes = computed(() => devices.value.map(dev => {
  const eq = equipmentById.value.get(String(dev.equipmentId)) || null;
  const t = latestByNo.value.get(dev.deviceNo) || null;
  const area = metricValue(t, 'area') || '';
  const battery = num(metricValue(t, 'battery'));
  const rssi = num(metricValue(t, 'rssi'));
  const online = !!t && !isStale(t);
  const homeArea = dev.location || '';
  const out = online && !!area && !!homeArea && area !== homeArea;
  const areaKnown = !!area;
  const maintain = maintainState(eq);
  return {
    ...dev,
    telemetry: t,
    equipmentName: eq?.equipmentName || (dev.deviceName || dev.deviceNo).replace('定位标签', ''),
    equipmentCode: eq?.equipmentCode || '',
    equipmentModel: eq ? [eq.brand, eq.model].filter(Boolean).join(' ') : '',
    equipmentDept: eq?.deptName || '',
    equipmentStatus: eq?.statusText || '',
    lastMaintainDate: eq?.lastMaintainDate || '',
    meteringValidUntil: eq?.meteringValidUntil || '',
    meteringExpired: !!eq?.meteringExpired,
    homeArea,
    area: area || '位置未知',
    battery,
    rssi,
    online,
    out,
    areaKnown,
    statusText: !t ? '未上报' : !online ? '离线' : !areaKnown ? '位置未知' : out ? '离位' : '在区',
    badgeType: !online ? 'info' : !areaKnown ? 'warning' : out ? 'danger' : battery != null && battery < LOW_BATTERY ? 'warning' : 'success',
    signalText: rssi == null ? '—' : (rssi > -60 ? '强' : rssi > -75 ? '中' : '弱'),
    maintain,
    lastSeen: t?.ts ? String(t.ts).replace('T', ' ').slice(11, 19) : '',
  };
}));

const matches = (box) => {
  const k = keyword.value.trim().toLowerCase();
  if (k && ![box.equipmentName, box.equipmentCode, box.deviceNo, box.deviceName].filter(Boolean)
      .some(v => String(v).toLowerCase().includes(k))) return false;
  if (areaFilter.value !== 'ALL' && box.area !== areaFilter.value) return false;
  if (outOnly.value && !box.out) return false;
  return true;
};

const areaOptions = computed(() => [...new Set(boxes.value.map(b => b.area).filter(a => a && a !== '位置未知'))]
    .sort((a, b) => a.localeCompare(b)));

const groups = computed(() => {
  const map = new Map();
  for (const box of boxes.value.filter(matches)) {
    const key = box.area;
    if (!map.has(key)) map.set(key, []);
    map.get(key).push(box);
  }
  return [...map.entries()].map(([area, items]) => ({
    area,
    items: items.sort((a, b) => (b.out - a.out) || a.equipmentName.localeCompare(b.equipmentName)),
    outCount: items.filter(b => b.out).length,
    alarmCount: items.filter(b => b.out).length,
  })).sort((a, b) => (b.alarmCount - a.alarmCount) || a.area.localeCompare(b.area));
});

const stats = computed(() => {
  const list = boxes.value;
  return {
    total: list.length,
    online: list.filter(b => b.online).length,
    out: list.filter(b => b.out).length,
    lowBattery: list.filter(b => b.online && b.battery != null && b.battery < LOW_BATTERY).length,
    maintainDue: list.filter(b => b.maintain.due).length,
    pending: pendingTotal.value,
  };
});

const equipmentOf = (deviceNo) => boxes.value.find(b => b.deviceNo === deviceNo) || {};

const siteState = (row) => {
  const box = equipmentOf(row.deviceNo);
  if (!box?.online) return {text: '离线，无法判断', cls: 'text-red-500'};
  if (row.eventCode === 'OUT_OF_AREA') {
    return box.out ? {text: `仍在区外：${box.area}`, cls: 'text-red-600 font-semibold'}
        : {text: `已归还 ${box.area}`, cls: 'text-green-600'};
  }
  return {text: '—', cls: 'text-gray-400'};
};

/** 到期日优先用台账算出的 nextMaintainDate；没有下次日期时按最近一次+周期兜底 */
function maintainState(eq) {
  if (!eq) return {text: '台账缺失', cls: 'text-red-600', due: true};
  if (!eq.lastMaintainDate && !eq.nextMaintainDate) {
    return {text: '未建档', cls: 'text-orange-600', due: true};
  }
  const next = eq.nextMaintainDate || addDays(eq.lastMaintainDate, eq.maintainCycleDays || 365);
  if (!next) return {text: '未建档', cls: 'text-orange-600', due: true};
  const days = Math.ceil((parseDate(next) - todayStart()) / 86400000);
  if (days < 0) return {text: `逾期 ${-days} 天`, cls: 'text-red-600 font-semibold', due: true};
  if (days <= MAINTAIN_WARN_DAYS) return {text: `还有 ${days} 天`, cls: 'text-orange-600', due: true};
  return {text: '正常', cls: 'text-green-600', due: false};
}

const addDays = (date, days) => {
  if (!date) return '';
  const d = parseDate(date);
  d.setDate(d.getDate() + Number(days || 0));
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};
const parseDate = (v) => new Date(String(v).slice(0, 10) + 'T00:00:00');
const todayStart = () => {
  const d = new Date();
  d.setHours(0, 0, 0, 0);
  return d.getTime();
};

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

const loadEquipment = async () => {
  try {
    const res = await equipmentListPage({pageNum: 1, pageSize: 200});
    equipment.value = res.data?.records || [];
    equipmentLoadedAt = Date.now();
  } catch (e) {
    // 台账接口挂了不阻断定位主视图：卡片退化为显示标签自带的设备名
    console.error('加载设备台账失败', e);
    equipment.value = [];
  }
};

const loadDevices = async () => {
  const [devRes, latestRes] = await Promise.all([
    iotDeviceSelectList({deviceType: 'asset'}),
    iotLatestList({deviceType: 'asset'}),
  ]);
  devices.value = devRes.data || [];
  latest.value = latestRes.data || [];
};

const loadEvents = async ({silent = false} = {}) => {
  if (!silent) eventLoading.value = true;
  try {
    const base = {deviceType: 'asset', minLevel: MIN_ALARM_LEVEL};
    const [res, pendingRes] = await Promise.all([
      iotEventListPage({...base, handledFlag: pendingOnly.value ? 0 : null,
        pageNum: eventPage.value.pageNum, pageSize: eventPage.value.pageSize}),
      iotEventListPage({...base, handledFlag: 0, pageNum: 1, pageSize: 1}),
    ]);
    events.value = res.data?.records || [];
    eventTotal.value = Number(res.data?.total || 0);
    pendingTotal.value = Number(pendingRes.data?.total || 0);
  } catch (e) {
    console.error('加载资产告警失败', e);
  } finally {
    eventLoading.value = false;
  }
};

const reloadEvents = () => {
  eventPage.value.pageNum = 1;
  loadEvents();
};

/**
 * silent = true（15 秒轮询）：不加遮罩、失败不弹窗（弹窗会盖住大屏），保留上一帧数据；
 * silent = false（首屏/手动刷新）：照旧显示遮罩与错误。
 */
const loadAll = async ({silent = false} = {}) => {
  if (!silent) {
    loading.value = true;
    loadError.value = '';
  }
  try {
    if (Date.now() - equipmentLoadedAt > 5 * 60 * 1000) await loadEquipment();
    await loadDevices();
    await loadEvents({silent});
    lastRefresh.value = new Date().toLocaleTimeString('zh-CN', {hour12: false});
  } catch (e) {
    console.error('加载资产定位失败', e);
    loadError.value = e?.response?.data?.message || e?.message || '接口调用失败';
    if (!silent) ElMessage.error(loadError.value);
  } finally {
    if (!silent) loading.value = false;
  }
};

const openTrend = (box) => {
  trendDevice.value = {...(box.telemetry || {}), deviceNo: box.deviceNo, deviceType: 'asset', deviceTypeLabel: box.equipmentName};
  trendVisible.value = true;
};

const handleEvent = async (row, flag) => {
  let remark;
  try {
    const r = await ElMessageBox.prompt(
        `处理离位告警「${equipmentOf(row.deviceNo).equipmentName || row.deviceNo}」，填写处理说明：`,
        flag === 1 ? '处理离位告警' : '忽略离位告警',
        {inputValue: flag === 1 ? '已到现场确认设备位置并推回授权区' : '借出会诊/检查，已知去向，无需处理',
          confirmButtonText: '确认', cancelButtonText: '取消'},
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
    console.error('处理资产告警失败', e);
    ElMessage.error(e?.response?.data?.message || '处理失败');
  }
};

let timer = null;
onMounted(async () => {
  await loadEquipment();
  await loadAll();
  timer = setInterval(() => {
    // 轮询静默：不挂加载遮罩，避免每 15 秒整屏闪一次
    loadAll({silent: true});
  }, 15_000);
});
onUnmounted(() => timer && clearInterval(timer));
</script>
