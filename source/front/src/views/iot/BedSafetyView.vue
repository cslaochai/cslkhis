<template>
  <div class="p-4 space-y-4">
    <!-- 顶部：护士站第一眼要看的是"本病区几床有人、谁离床、谁体征不对" -->
    <div class="bg-white rounded-lg shadow-sm px-5 py-4 flex flex-wrap items-center gap-6">
      <div class="min-w-[220px]">
        <div class="flex items-center gap-2">
          <h2 class="text-[18px] font-semibold text-gray-800">病区床位安全</h2>
          <el-tag size="small" effect="plain">智能床垫</el-tag>
        </div>
        <div class="text-[12px] text-gray-500 mt-1">离床 / 心率呼吸异常 / 翻身活动监测，按病区床位归集</div>
      </div>

      <div class="flex items-center gap-3">
        <el-select v-model="deptId" placeholder="选择科室" style="width: 200px" filterable @change="onDeptChange">
          <el-option v-for="d in deptOptions" :key="d.id" :label="d.name" :value="String(d.id)"/>
        </el-select>
        <el-select v-model="wardId" placeholder="全部病区" style="width: 190px" @change="onWardChange">
          <el-option label="全部病区" value="ALL"/>
          <el-option v-for="w in wardOptions" :key="w.wardId" :label="w.wardName" :value="String(w.wardId)"/>
        </el-select>
      </div>

      <div class="ml-auto flex items-center gap-6 text-center">
        <div>
          <div class="text-[22px] font-bold leading-6 text-[#1269B5]">{{ stats.monitored }}</div>
          <div class="text-[12px] text-gray-500">监测床位</div>
        </div>
        <div>
          <div class="text-[22px] font-bold leading-6 text-gray-800">{{ stats.patients }}</div>
          <div class="text-[12px] text-gray-500">在院患者</div>
        </div>
        <div>
          <div class="text-[22px] font-bold leading-6 text-[#0E9488]">{{ stats.inBed }}</div>
          <div class="text-[12px] text-gray-500">在床</div>
        </div>
        <div>
          <div class="text-[22px] font-bold leading-6" :class="stats.out ? 'text-orange-600' : 'text-gray-800'">{{ stats.out }}</div>
          <div class="text-[12px] text-gray-500">离床</div>
        </div>
        <div>
          <div class="text-[22px] font-bold leading-6" :class="stats.vital ? 'text-red-600' : 'text-gray-800'">{{ stats.vital }}</div>
          <div class="text-[12px] text-gray-500">体征异常</div>
        </div>
        <div>
          <div class="text-[22px] font-bold leading-6" :class="stats.pending ? 'text-red-600' : 'text-gray-800'">{{ stats.pending }}</div>
          <div class="text-[12px] text-gray-500">未闭环告警</div>
        </div>
      </div>
      <div class="flex items-center gap-2 text-[12px] text-gray-400">
        <span class="inline-block w-2 h-2 rounded-full bg-green-500"></span>
        自动刷新 10s · {{ lastRefresh || '—' }}
      </div>
    </div>

    <el-alert v-if="loadError" :title="loadError" type="error" show-icon :closable="false"/>

    <div v-if="coverageText" class="text-[13px] text-gray-500 px-1">
      {{ coverageText }}
    </div>

    <div v-loading="loading" class="space-y-4">
      <div v-for="ward in groups" :key="ward.wardName" class="bg-white rounded-lg shadow-sm p-4">
        <div class="flex items-center gap-3 mb-3">
          <span class="text-[15px] font-semibold text-gray-800">{{ ward.wardName }}</span>
          <span class="text-[12px] text-gray-500">{{ ward.items.length }} 张监测床</span>
          <span v-if="ward.outCount" class="text-[12px] text-orange-600">{{ ward.outCount }} 床离床</span>
          <span v-if="ward.vitalCount" class="text-[12px] text-red-600">{{ ward.vitalCount }} 床体征异常</span>
          <span class="ml-auto text-[12px]" :class="ward.alarmCount ? 'text-red-600' : 'text-green-600'">
            {{ ward.alarmCount ? `${ward.alarmCount} 床需关注` : '全部平稳' }}
          </span>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4 gap-3">
          <div v-for="box in ward.items" :key="box.deviceNo"
               class="rounded-lg border overflow-hidden transition hover:shadow-md"
               :class="box.borderClass">
            <div class="px-3 py-2 flex items-center gap-2" :class="box.headerClass">
              <span class="text-[17px] font-bold text-gray-800">{{ box.bedNo }} 床</span>
              <span class="text-[14px] font-medium truncate" :class="box.patientName ? 'text-gray-800' : 'text-gray-400'">
                {{ box.patientName || '空床 / 无在院患者' }}
              </span>
              <el-tag v-if="box.patientName && box.nursingLevelText" size="small" effect="plain">{{ box.nursingLevelText }}</el-tag>
              <el-tag class="ml-auto" size="small" :type="box.badgeType" effect="dark">{{ box.statusText }}</el-tag>
            </div>

            <div class="px-3 py-3">
              <div class="text-[12px] text-gray-500 flex items-center gap-2">
                <span v-if="box.patientName">{{ box.genderText }} · {{ box.age == null ? '年龄未知' : box.age + '岁' }}</span>
                <span v-if="box.admitDays != null" class="text-gray-400">住院 {{ box.admitDays }} 天</span>
                <span class="ml-auto text-gray-400">更新 {{ box.lastSeen || '—' }}</span>
              </div>
              <div v-if="box.doctorName || box.allergyHistory" class="mt-1 text-[12px] flex items-center gap-3">
                <span class="text-gray-500 truncate" v-if="box.doctorName">医生 {{ box.doctorName }}</span>
                <span class="text-orange-600 truncate" v-if="box.allergyHistory">过敏：{{ box.allergyHistory }}</span>
              </div>

              <div class="grid grid-cols-2 gap-2 mt-3">
                <div class="rounded bg-gray-50 px-2 py-2">
                  <div class="text-[11px] text-gray-400">心率</div>
                  <div class="text-[20px] font-semibold leading-6" :class="box.hrBad ? 'text-red-600' : 'text-gray-800'">
                    {{ box.hr == null || box.hr === 0 ? '—' : box.hr }}<span v-if="box.hr" class="text-[11px] text-gray-400 ml-1">bpm</span>
                  </div>
                </div>
                <div class="rounded bg-gray-50 px-2 py-2">
                  <div class="text-[11px] text-gray-400">呼吸</div>
                  <div class="text-[20px] font-semibold leading-6" :class="box.rrBad ? 'text-red-600' : 'text-gray-800'">
                    {{ box.rr == null || box.rr === 0 ? '—' : box.rr }}<span v-if="box.rr" class="text-[11px] text-gray-400 ml-1">次/分</span>
                  </div>
                </div>
                <div class="rounded bg-gray-50 px-2 py-2">
                  <div class="text-[11px] text-gray-400">体动</div>
                  <div class="text-[20px] font-semibold leading-6 text-gray-800">{{ box.movement == null ? '—' : box.movement }}</div>
                </div>
                <div class="rounded bg-gray-50 px-2 py-2">
                  <div class="text-[11px] text-gray-400">累计翻身</div>
                  <div class="text-[20px] font-semibold leading-6 text-gray-800">
                    {{ box.turns == null ? '—' : box.turns }}<span class="text-[11px] text-gray-400 ml-1">次</span>
                  </div>
                </div>
              </div>

              <div v-if="box.alarmText" class="mt-2 text-[13px] text-red-600">{{ box.alarmText }}</div>
              <div v-else-if="!box.online" class="mt-2 text-[13px] text-red-500">超过 2 分钟未上报，先查网关/床垫供电</div>
              <div v-else-if="!box.patientName" class="mt-2 text-[13px] text-gray-400">床垫已装，床位当前无人</div>

              <div class="mt-2 flex items-center justify-between text-[12px]">
                <span class="text-gray-400">床垫 {{ box.deviceNo }}</span>
                <el-button link type="primary" :disabled="!box.online" @click="openTrend(box)">趋势</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && !groups.length" description="本病区暂无智能床垫；设备科装床后自动出现在这里"/>
    </div>

    <div class="bg-white rounded-lg shadow-sm p-4">
      <div class="flex items-center gap-3 mb-2">
        <span class="text-[15px] font-semibold text-gray-800">床位告警</span>
        <span class="text-[12px] text-gray-500">只看 2 级及以上（离床超时 / 体征异常）；进出床记录属日常作业，不进告警表</span>
        <div class="ml-auto flex items-center gap-3">
          <el-checkbox v-model="pendingOnly" @change="reloadEvents">只看未处理</el-checkbox>
          <el-button size="small" @click="loadEvents()">刷新</el-button>
        </div>
      </div>

      <el-table :data="events" v-loading="eventLoading" size="small" height="300">
        <el-table-column prop="ts" label="时间" width="150">
          <template #default="{row}">{{ String(row.ts || '').replace('T', ' ').slice(5, 16) }}</template>
        </el-table-column>
        <el-table-column label="床位" width="110">
          <template #default="{row}">{{ boxByNo.get(row.deviceNo)?.bedNo || row.deviceNo }}</template>
        </el-table-column>
        <el-table-column label="患者" width="110">
          <template #default="{row}">{{ boxByNo.get(row.deviceNo)?.patientName || '—' }}</template>
        </el-table-column>
        <el-table-column label="事件" width="150">
          <template #default="{row}">
            <span :class="levelClass(row.level)">{{ row.eventCodeText || row.eventCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="描述" min-width="200" show-overflow-tooltip/>
        <el-table-column label="现场状态" width="140">
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
 * 病区床位安全（住院护士站视角）。
 *
 * 为什么不是"设备清单"：床垫是固定安装设备，护士关心的是"13 床周立群现在在不在床上、心率呼吸对不对"，
 * 不是"BED-01 在线吗"。所以卡片以床位为主键，患者信息来自 ADT 床位图（/patient/inpatient/bedMap），
 * 床垫档案只负责 bedNo → 设备号、时序库给在床/体征。患者转床只换床位图的人，床垫绑定不动。
 *
 * 告警口径：1 级（BED_EXIT / BED_RETURN）是上下床作业记录，护士站看板只取 2 级（离床超 2 分钟、体征异常）。
 */
import {computed, onMounted, onUnmounted, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {getBedMap} from '@/api/inpatient';
import {iotDeviceSelectList, iotEventHandle, iotEventListPage, iotLatestList} from '@/api/iot';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import {loadDeptOptions} from '@/lib/deptOptions';
import DeviceHistoryDialog from './DeviceHistoryDialog.vue';

/** 床垫 5 秒一条，超过 2 分钟未上报按离线 */
const STALE_MS = 120_000;
const MIN_ALARM_LEVEL = 2;
const HR_MAX = 110;
const HR_MIN = 50;
const RR_MAX = 26;
const RR_MIN = 8;

const loading = ref(false);
const loadError = ref('');
const lastRefresh = ref('');
const deptOptions = ref([]);
const deptId = ref('');
const wardId = ref('ALL');
const wardOptions = ref([]);
const beds = ref([]);
const sensors = ref([]);
const latest = ref([]);
const coverage = ref({totalBeds: 0, monitored: 0});

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

const normBed = (no) => String(no == null ? '' : no).trim().replace(/^0+/, '');

const boxes = computed(() => sensors.value.map(dev => {
  const bed = beds.value.find(b => normBed(b.bedNo) === normBed(dev.bedNo)) || null;
  const telemetry = latestByNo.value.get(dev.deviceNo) || null;
  const inBed = truthy(metricValue(telemetry, 'inBed'));
  const hr = num(metricValue(telemetry, 'heartRate'));
  const rr = num(metricValue(telemetry, 'respRate'));
  const turns = num(metricValue(telemetry, 'turns'));
  const movement = num(metricValue(telemetry, 'bodyMovement'));
  const online = !!telemetry && !isStale(telemetry);
  const hrBad = inBed && hr != null && (hr > HR_MAX || hr < HR_MIN);
  const rrBad = inBed && rr != null && (rr > RR_MAX || rr < RR_MIN);
  const vitalBad = hrBad || rrBad;
  const out = online && telemetry != null && !inBed;
  const alarm = vitalBad || (out && online);
  const statusText = !telemetry ? '未上报' : !online ? '离线' : vitalBad ? '体征异常' : out ? '离床' : '在床';
  const badgeType = !online ? 'info' : vitalBad ? 'danger' : out ? 'warning' : 'success';
  return {
    ...dev,
    bed,
    telemetry,
    bedNo: dev.bedNo || bed?.bedNo || '—',
    wardName: bed?.wardName || dev.location || '未分病区',
    patientName: bed?.patientName || '',
    genderText: genderText(bed?.gender),
    age: bed?.age,
    admitDays: bed?.admitDays,
    nursingLevelText: bed?.nursingLevelText,
    doctorName: bed?.doctorName || '',
    allergyHistory: bed?.allergyHistory || '',
    inBed,
    hr,
    rr,
    turns,
    movement,
    hrBad,
    rrBad,
    vitalBad,
    out,
    online,
    alarm,
    statusText,
    badgeType,
    headerClass: alarm ? 'bg-red-50 border-red-100' : 'bg-[#F2F7FC] border-gray-100',
    borderClass: alarm ? 'border-red-200' : 'border-gray-100',
    alarmText: !online ? '床垫离线，当前读数不可信'
        : vitalBad ? `体征异常：心率 ${hr ?? '—'} bpm / 呼吸 ${rr ?? '—'} 次/分（正常 ${HR_MIN}~${HR_MAX} / ${RR_MIN}~${RR_MAX}）`
            : out ? '患者当前不在床上（离床超 2 分钟会进告警表）' : '',
    lastSeen: telemetry?.ts ? String(telemetry.ts).replace('T', ' ').slice(11, 19) : '',
  };
}).sort((a, b) => (b.alarm - a.alarm) || (a.online ? 1 : 0) - (b.online ? 1 : 0)
    || String(a.bedNo).localeCompare(String(b.bedNo), 'zh-CN', {numeric: true})));

const groups = computed(() => {
  const map = new Map();
  for (const box of boxes.value) {
    const key = box.wardName || '未分病区';
    if (!map.has(key)) map.set(key, []);
    map.get(key).push(box);
  }
  return [...map.entries()].map(([wardName, items]) => ({
    wardName,
    items,
    alarmCount: items.filter(b => b.alarm).length,
    outCount: items.filter(b => b.out).length,
    vitalCount: items.filter(b => b.vitalBad).length,
  }));
});

const boxByNo = computed(() => new Map(boxes.value.map(b => [b.deviceNo, b])));

const stats = computed(() => {
  const list = boxes.value;
  return {
    monitored: list.length,
    patients: list.filter(b => b.patientName).length,
    inBed: list.filter(b => b.online && b.inBed).length,
    out: list.filter(b => b.out).length,
    vital: list.filter(b => b.vitalBad).length,
    pending: pendingTotal.value,
  };
});

const coverageText = computed(() => {
  if (!coverage.value.totalBeds) return '';
  const gap = coverage.value.totalBeds - coverage.value.monitored;
  return `本病区共 ${coverage.value.totalBeds} 张床，已装智能床垫 ${coverage.value.monitored} 张`
      + (gap > 0 ? `；其余 ${gap} 张未装，不在本页监测范围` : '（全覆盖）');
});

const siteState = (row) => {
  const box = boxByNo.value.get(row.deviceNo);
  if (!box) return {text: '设备不在本病区', cls: 'text-gray-400'};
  if (!box.online) return {text: '离线，无法判断', cls: 'text-red-500'};
  if (row.eventCode === 'BED_EXIT_LONG') {
    return box.inBed ? {text: '已回床', cls: 'text-green-600'} : {text: '仍离床', cls: 'text-orange-600 font-semibold'};
  }
  if (row.eventCode === 'VITAL_ABNORMAL') {
    return box.vitalBad ? {text: `仍异常 ${box.hr ?? '—'}/${box.rr ?? '—'}`, cls: 'text-red-600 font-semibold'}
        : {text: '体征已恢复', cls: 'text-green-600'};
  }
  return {text: '—', cls: 'text-gray-400'};
};

const metricValue = (device, key) => {
  const m = (device?.metrics || []).find(x => x.key === key);
  return m ? m.value : null;
};
const num = (v) => {
  const n = Number(v);
  return v == null || v === '' || !Number.isFinite(n) ? null : n;
};
const truthy = (v) => v === true || v === 'true' || v === '是' || v === 1 || v === '1';
const genderText = (g) => (g === 1 ? '男' : g === 2 ? '女' : '性别未知');
const isStale = (d) => {
  if (!d?.ts) return true;
  const t = new Date(String(d.ts).replace(' ', 'T')).getTime();
  return !Number.isFinite(t) || Date.now() - t > STALE_MS;
};
const levelClass = (level) => (level === 3 ? 'text-red-600 font-semibold' : level === 2 ? 'text-orange-600' : 'text-gray-600');

const loadDepts = async () => {
  try {
    const list = await loadDeptOptions();
    deptOptions.value = (list || []).map(d => ({id: d.id, name: d.deptName || d.name, deptType: d.deptType}));
    // 默认当前岗位所在科室；没有岗位科室时退第一个住院类科室，不再硬编码演示科室
    const own = deptOptions.value.find(d => String(d.id) === String(localStorage.getItem('deptId')));
    const firstIpd = deptOptions.value.find(d => String(d.deptType || '').includes('4'));
    deptId.value = String((own || firstIpd || deptOptions.value[0])?.id || '');
  } catch (e) {
    console.error('加载科室下拉失败', e);
    loadError.value = '科室下拉加载失败，请检查权限或网络';
  }
};

const loadData = async ({silent = false} = {}) => {
  if (!deptId.value) return;
  if (!silent) {
    loading.value = true;
    loadError.value = '';
  }
  try {
    const [bedRes, devRes, latestRes] = await Promise.all([
      getBedMap({deptId: deptId.value, wardId: wardId.value === 'ALL' ? undefined : wardId.value}),
      iotDeviceSelectList({deviceType: 'bed'}),
      iotLatestList({deviceType: 'bed'}),
    ]);
    const map = bedRes.data || {};
    beds.value = (map.beds || []).filter(b => b.bedNo);
    wardOptions.value = map.wardOptions || [];
    const deptName = map.deptName || '';
    sensors.value = (devRes.data || []).filter(d => {
      const matchDept = !deptName || !d.deptName || d.deptName === deptName
          || deptName.includes(d.deptName) || d.deptName.includes(deptName);
      const matchWard = wardId.value === 'ALL'
          || (beds.value.some(b => normBed(b.bedNo) === normBed(d.bedNo) && String(b.wardId) === String(wardId.value)));
      return d.bedNo && matchDept && matchWard;
    });
    latest.value = latestRes.data || [];
    coverage.value = {totalBeds: (map.beds || []).filter(b => b.bedNo).length, monitored: sensors.value.length};
    lastRefresh.value = new Date().toLocaleTimeString('zh-CN', {hour12: false});
    if (!deptOptions.value.length && map.deptOptions) {
      // bedMap 自带授权科室清单，首屏 deptOptions 拉取失败时兜底
      deptOptions.value = (map.deptOptions || []).map(d => ({id: d.deptId, name: d.deptName}));
    }
  } catch (e) {
    console.error('加载床位安全数据失败', e);
    loadError.value = e?.response?.data?.message || e?.message || '接口调用失败';
    // 10 秒轮询失败不弹窗：弹窗会盖住大屏，静默失败保留上一帧
    if (!silent) ElMessage.error(loadError.value);
  } finally {
    if (!silent) loading.value = false;
  }
};

const loadEvents = async ({silent = false} = {}) => {
  if (!silent) eventLoading.value = true;
  try {
    const base = {deviceType: 'bed', deptId: deptId.value, minLevel: MIN_ALARM_LEVEL};
    const [res, pendingRes] = await Promise.all([
      iotEventListPage({...base, handledFlag: pendingOnly.value ? 0 : null,
        pageNum: eventPage.value.pageNum, pageSize: eventPage.value.pageSize}),
      iotEventListPage({...base, handledFlag: 0, pageNum: 1, pageSize: 1}),
    ]);
    events.value = res.data?.records || [];
    eventTotal.value = Number(res.data?.total || 0);
    pendingTotal.value = Number(pendingRes.data?.total || 0);
  } catch (e) {
    console.error('加载床位告警失败', e);
  } finally {
    eventLoading.value = false;
  }
};

const reloadEvents = () => {
  eventPage.value.pageNum = 1;
  loadEvents();
};

const onDeptChange = () => {
  wardId.value = 'ALL';
  reloadEvents();
  loadData();
};

const onWardChange = () => {
  reloadEvents();
  loadData();
};

const openTrend = (box) => {
  trendDevice.value = {...(box.telemetry || {}), deviceNo: box.deviceNo, deviceType: 'bed', deviceTypeLabel: `${box.bedNo} 床床垫`};
  trendVisible.value = true;
};

const handleEvent = async (row, flag) => {
  let remark;
  try {
    const r = await ElMessageBox.prompt(
        `处理告警「${row.eventCodeText || row.eventCode}」（${boxByNo.value.get(row.deviceNo)?.bedNo || row.deviceNo} 床），填写处理说明：`,
        flag === 1 ? '处理床位告警' : '忽略床位告警',
        {inputValue: flag === 1 ? '已到床边查看，患者安全，已协助回床' : '误报（患者临时离床），无需处理',
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
    console.error('处理床位告警失败', e);
    ElMessage.error(e?.response?.data?.message || '处理失败');
  }
};

let timer = null;
onMounted(async () => {
  await loadDepts();
  await Promise.all([loadData(), loadEvents()]);
  timer = setInterval(() => {
    // 轮询静默：不挂加载遮罩，避免每 10 秒整屏闪一次
    loadData({silent: true});
    loadEvents({silent: true});
  }, 10_000);
});
onUnmounted(() => timer && clearInterval(timer));
</script>
