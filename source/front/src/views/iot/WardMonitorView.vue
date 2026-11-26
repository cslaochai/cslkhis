<template>
  <div class="p-4">
    <!-- 顶部：科室/病区选择 + 病区概况 -->
    <div class="bg-white rounded-lg shadow-sm px-4 py-3 mb-4 flex flex-wrap items-center gap-x-6 gap-y-3">
      <div class="flex items-center gap-2">
        <span class="text-[13px] text-gray-500">科室</span>
        <el-select v-model="deptId" :fit-input-width="false" style="width: 200px" filterable @change="onDeptChange">
          <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="d.id"/>
        </el-select>
        <span class="text-[13px] text-gray-500 ml-2">病区</span>
        <el-select v-model="wardId" :fit-input-width="false" style="width: 180px">
          <el-option label="全部病区" value="ALL"/>
          <el-option v-for="w in wardOptions" :key="w.wardId" :label="w.wardName" :value="w.wardId"/>
        </el-select>
      </div>

      <div class="text-[20px] font-semibold text-[#1269B5]">
        {{ currentDeptName }}<template v-if="currentWardName"> · {{ currentWardName }}</template>
        <span class="text-[14px] font-normal text-gray-500 ml-1">护士站患者监护</span>
      </div>

      <div class="flex items-center gap-6 ml-auto">
        <div class="text-center">
          <div class="text-[22px] font-bold leading-6">{{ stats.occupied }}</div>
          <div class="text-[12px] text-gray-500">在院患者</div>
        </div>
        <div class="text-center">
          <div class="text-[22px] font-bold leading-6 text-[#0E9488]">{{ stats.devices }}</div>
          <div class="text-[12px] text-gray-500">床旁设备</div>
        </div>
        <div class="text-center">
          <div class="text-[22px] font-bold leading-6" :class="stats.alarm ? 'text-red-600' : 'text-gray-400'">
            {{ stats.alarm }}
          </div>
          <div class="text-[12px] text-gray-500">设备告警</div>
        </div>
        <div class="text-center">
          <div class="text-[22px] font-bold leading-6" :class="stats.uncovered ? 'text-orange-600' : 'text-gray-500'">
            {{ stats.uncovered }}
          </div>
          <div class="text-[12px] text-gray-500">未接设备</div>
        </div>
        <div class="text-center">
          <div class="text-[22px] font-bold leading-6 text-gray-500">{{ stats.free }}</div>
          <div class="text-[12px] text-gray-500">空床</div>
        </div>
      </div>

      <div class="flex items-center gap-2 text-[12px] text-gray-400">
        <span class="inline-block w-2 h-2 rounded-full bg-green-500 animate-pulse"></span>
        自动刷新 10s · {{ lastRefresh || '-' }}
        <el-switch v-model="alertMuted" size="small" active-text="告警声音"/>
        <!-- 浏览器要求页面有过用户交互才允许出声：没解锁时把原因显式写出来，避免「以为坏了」 -->
        <span v-if="!alertMuted && !alertAudioReady" class="text-orange-500" title="浏览器要求页面有过点击/按键才允许出声">
          点击页面任意处开启声音
        </span>
        <el-switch v-model="showEmpty" active-text="显示空床" size="small"/><el-button size="small" text @click="loadBeds()">刷新</el-button>
      </div>
    </div>

    <!-- 床位卡：一床一卡，床旁设备指标挂在患者卡上 -->
    <div v-loading="loading" class="grid grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4 gap-4">
      <div v-for="bed in visibleBeds" :key="bed.bedId"
           class="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden transition hover:shadow-md"
           :class="[bed.patientName ? '' : 'opacity-60', (pumpsOf(bed).length || bedSensorOf(bed)) ? 'cursor-pointer' : '']"
           @click="openTrend(bed)">
        <!-- 卡头：床号 + 护理级别 + 告警 -->
        <div class="flex items-center gap-2 px-3 py-2 border-b"
             :class="cardHeaderClass(bed)">
          <span class="text-[17px] font-bold">{{ bed.bedNo }} 床</span>
          <el-tag v-if="pumpsOf(bed).length" size="small" type="success" effect="plain">
            输液泵{{ pumpsOf(bed).length > 1 ? ' ×' + pumpsOf(bed).length : '' }}
          </el-tag>
          <el-tag v-if="bed.patientName && bed.nursingLevelText" size="small" effect="plain">
            护理 {{ bed.nursingLevelText }}
          </el-tag>
          <el-tag v-if="bed.critical" size="small" type="danger" effect="dark">危重</el-tag>
          <el-tag v-if="pumpsOf(bed).some(p => p.alarm) || bedSensorOf(bed)?.vitalBad" size="small" type="danger" effect="dark" class="ml-auto">
            设备告警
          </el-tag>
        </div>

        <div class="px-3 py-3" :class="bed.patientName ? '' : 'text-gray-400'">
          <template v-if="bed.patientName">
            <div class="flex items-baseline gap-2">
              <span class="text-[20px] font-semibold truncate">{{ bed.patientName }}</span>
              <span class="text-[13px] text-gray-500">{{ genderText(bed.gender) }} · {{ bed.age ? bed.age + '岁' : '年龄未录' }}</span>
            </div>
            <div class="mt-1 text-[13px] text-gray-500 flex items-center gap-3">
              <span>住院 <b class="text-gray-700">{{ bed.admitDays ?? '-' }}</b> 天</span>
              <span class="truncate">医生 {{ bed.doctorName || '-' }}</span>
            </div>
            <div class="mt-1 text-[12px] text-orange-600 truncate" v-if="bed.allergyHistory">
              过敏：{{ bed.allergyHistory }}
            </div>

            <!-- 床旁设备实时指标：一台患者可挂多台泵（多通道输液），按绑定关系逐台列 -->
            <div class="mt-3 rounded-md border border-gray-100 bg-gray-50 px-3 py-2" v-if="pumpsOf(bed).length">
              <div class="flex items-center justify-between mb-1">
                <span class="text-[12px] text-gray-500">床旁输液泵</span>
                <span class="text-[11px] text-gray-400">住院申领 · 设备科台账</span>
              </div>
              <div v-for="p in pumpsOf(bed)" :key="p.id"
                   class="py-1.5 border-b last:border-b-0 border-gray-200/70">
                <div class="flex items-center justify-between text-[12px] text-gray-500">
                  <span class="truncate">输液泵 <b class="text-gray-700">{{ p.deviceNo }}</b>
                    <span class="ml-1 text-gray-400">{{ p.bindSourceText }} · {{ shortTime(p.bindTime) }}</span>
                  </span>
                  <span class="flex items-center gap-1" :class="p.online ? 'text-green-600' : 'text-gray-400'">
                    <span class="inline-block w-1.5 h-1.5 rounded-full"
                          :class="p.online ? 'bg-green-500' : 'bg-gray-400'"></span>
                    {{ p.telemetry ? (p.online ? '在线' : '离线') : '未上报' }}
                  </span>
                </div>
                <div class="mt-1 flex items-end justify-between">
                  <div class="flex items-end gap-4">
                    <div>
                      <span class="text-[19px] font-bold text-[#1269B5] leading-6">{{ metric(p.telemetry, 'rate') }}</span>
                      <span class="text-[11px] text-gray-500 ml-0.5">滴/分</span>
                    </div>
                    <div>
                      <span class="text-[19px] font-bold text-gray-700 leading-6">{{ metric(p.telemetry, 'remainingMl') }}</span>
                      <span class="text-[11px] text-gray-500 ml-0.5">ml</span>
                    </div>
                    <div>
                      <span class="text-[19px] font-bold leading-6"
                            :class="lowBattery(p.telemetry) ? 'text-red-600' : 'text-gray-700'">
                        {{ metric(p.telemetry, 'battery') }}</span>
                      <span class="text-[11px] text-gray-500 ml-0.5">%</span>
                    </div>
                  </div>
                  <div class="flex items-center gap-3 text-[12px]">
                    <span v-if="p.telemetry" class="text-[#1269B5] cursor-pointer" @click.stop="openPumpTrend(p)">趋势</span>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="mt-3 rounded-md border border-dashed border-gray-200 px-3 py-2 text-center text-[12px] text-gray-400">
              未绑定床旁设备
              <span class="ml-1 text-gray-400">（在「物资设备 · 设备共享调配」申请，设备科发放后绑定）</span>
            </div>

            <!-- 床垫体征：与输液泵分开显示，护士一眼区分「输注」和「在床/生命体征」 -->
            <div v-if="bedSensorOf(bed)" class="mt-2 rounded-md border border-gray-100 bg-gray-50 px-3 py-2">
              <div class="flex items-center justify-between text-[12px] text-gray-500">
                <span>智能床垫 <b class="text-gray-700">{{ bedSensorOf(bed).deviceNo }}</b></span>
                <span class="flex items-center gap-1" :class="bedSensorOf(bed).online ? 'text-green-600' : 'text-gray-400'">
                  <span class="inline-block w-1.5 h-1.5 rounded-full"
                        :class="bedSensorOf(bed).online ? 'bg-green-500' : 'bg-gray-400'"></span>
                  {{ bedSensorOf(bed).online ? '在线' : '离线' }}
                </span>
              </div>
              <div class="mt-1 grid grid-cols-3 gap-2 text-center">
                <div>
                  <div class="text-[11px] text-gray-400">心率</div>
                  <div class="text-[18px] font-bold leading-6"
                       :class="bedSensorOf(bed).hrBad ? 'text-red-600' : 'text-gray-800'">
                    {{ bedSensorOf(bed).inBed && bedSensorOf(bed).hr ? bedSensorOf(bed).hr : '—' }}<span class="text-[10px] text-gray-400 ml-0.5">bpm</span>
                  </div>
                </div>
                <div>
                  <div class="text-[11px] text-gray-400">呼吸</div>
                  <div class="text-[18px] font-bold leading-6"
                       :class="bedSensorOf(bed).rrBad ? 'text-red-600' : 'text-gray-800'">
                    {{ bedSensorOf(bed).inBed && bedSensorOf(bed).rr ? bedSensorOf(bed).rr : '—' }}<span class="text-[10px] text-gray-400 ml-0.5">次/分</span>
                  </div>
                </div>
                <div>
                  <div class="text-[11px] text-gray-400">在床状态</div>
                  <div class="text-[18px] font-bold leading-6"
                       :class="!bedSensorOf(bed).online ? 'text-gray-400' : bedSensorOf(bed).inBed ? 'text-[#0E9488]' : 'text-orange-600'">
                    {{ !bedSensorOf(bed).online ? '离线' : bedSensorOf(bed).inBed ? '在床' : '离床' }}
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="mt-2 rounded-md border border-dashed border-gray-200 px-3 py-2 text-center text-[12px] text-gray-400">
              未安装智能床垫
            </div>
            <div class="mt-3 flex items-center justify-between text-[12px] text-gray-400">
              <span>{{ lastDeviceTime(bed) ? '更新 ' + lastDeviceTime(bed) : '未接入监测设备' }}</span>
              <span v-if="pumpsOf(bed).length || bedSensorOf(bed)" class="text-[#1269B5] cursor-pointer" @click.stop="openTrend(bed)">
                查看患者趋势 ›
              </span>
            </div>
          </template>
          <template v-else>
            <div class="text-[15px] font-medium">{{ bed.bedNo }} 床 · 空床</div>
            <div class="mt-1 text-[12px]">可收治</div>
          </template>
        </div>
      </div>
    </div>

    <div v-if="!loading && !visibleBeds.length" class="text-center text-gray-400 py-16">
      该科室暂无床位数据{{ loadError ? '：' + loadError : '' }}
    </div>

    <DeviceHistoryDialog v-model="trendVisible" :device="trendDevice"/>
  </div>
</template>

<script setup>
/**
 * 病区患者监护大屏（护士站视角）。
 *
 * 设计口径（对齐 IHE PCD DEC / Philips IntelliBridge / 迈瑞 eGateway 的做法）：
 * 界面以「患者/床位」为主键，设备只是挂在患者身上的数据源——护士关心的是「3 床这个人的输液/体征现在怎么样」，
 * 不是「PUMP-03 这台泵的原始指标」。所以这里不铺设备清单，而是：
 *   1) 病区床位图（HIS 住院域，权威患者信息）
 *   2) 设备—患者绑定关系（biz_iot_device_bind，绑定对象是住院就诊 admissionId）
 *   3) 输液泵最新值 + 床垫体征（时序库，按 device_no / bed_no 对齐）
 * 三者按 admissionId/床号合并：泵跟着患者走，转床不用重绑；床垫固定在床位。
 * <p>
 * 本页严格只读：绑定/解绑统一由「物资设备 · 设备共享调配」的发放/验收触发，监控页不出现任何设备操作按钮。
 * 设备清单页 /iot-devices 保留为设备科/运维视角。
 *
 * 入口不依赖「病区列表」接口（那个在非住院岗位可能取不到），直接给科室下拉：
 * 管理员（data_scope=1）拿全院科室，护士拿自己被授权的科室，选哪个科室就看哪个科室的床。
 */
import {computed, onMounted, onUnmounted, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {getBedMap} from '@/api/inpatient';
import {iotActiveBinds, iotDeviceSelectList, iotLatestList} from '@/api/iot';
// 设备告警实时流：设备事件（阻塞/余量/断电）落库即推一帧；断线退回本页 10 秒轮询
import {openIotAlarmStream} from '@/api/iotAlarmStream';
// 提示音：与顶栏站内消息共用同一套静音开关与音色（浏览器解锁策略也在那个模块处理）
import {alertAudioReady, alertMuted, playAlert} from '@/lib/alertSound';
import {loadDeptOptions} from '@/lib/deptOptions';
import DeviceHistoryDialog from './DeviceHistoryDialog.vue';

const depts = ref([]);
const deptId = ref(null);
const wardId = ref('ALL');
const showEmpty = ref(false);
const beds = ref([]);
const devices = ref([]);
const activeBinds = ref([]);
const bedDevices = ref([]);
const bedLatest = ref([]);
const loading = ref(false);
const loadError = ref('');
const lastRefresh = ref('');
const trendVisible = ref(false);
const trendDevice = ref(null);

// ========== 告警提示音：SSE 秒达 + 10 秒轮询兜底，按「告警键」去重 ==========
// 为什么按设备/床位去重而不按事件：一台泵阻塞会连续上报多条事件、体征越界会连续多轮轮询命中，
// 但护士该听到的是「3 床这台泵/这个人开始异常」，不是每 10 秒叫一次。
// 所以：进入告警响一次 → 持续不响 → 恢复后再次异常才重新响。
let closeAlarmStream = null;
let alarmSeen = new Set();          // 上一轮处于告警的键（pump:设备号 / bed:设备号:vital）
let alarmPrimed = false;            // 首轮静默：接班/刷新页面不该把历史告警集体叫一遍
const buzzedEvents = new Set();     // 已发声的事件ID（SSE 重连补发、QoS1 重复帧去重）

const currentDeptName = computed(() => depts.value.find(d => String(d.id) === String(deptId.value))?.deptName || '未选择科室');
const wardOptions = computed(() => {
    const map = new Map();
    for (const bed of beds.value) {
        if (bed.wardId && !map.has(String(bed.wardId))) {
            map.set(String(bed.wardId), {wardId: bed.wardId, wardName: bed.wardName || '未命名病区'});
        }
    }
    return [...map.values()];
});
const currentWardName = computed(() => wardId.value === 'ALL' ? '' : (wardOptions.value.find(w => String(w.wardId) === String(wardId.value))?.wardName || ''));
const wardBeds = computed(() => wardId.value === 'ALL'
    ? beds.value
    : beds.value.filter(b => String(b.wardId) === String(wardId.value)));
/** 默认只显示有患者的床：护士站先看“现在的病人”，空床点开关再看 */
const visibleBeds = computed(() => showEmpty.value
    ? wardBeds.value
    : wardBeds.value.filter(b => b.patientName));

const stats = computed(() => {
    const occupied = wardBeds.value.filter(b => b.patientName).length;
    const devicesCount = wardBeds.value.reduce((n, b) => n + pumpsOf(b).length + (bedSensorOf(b) ? 1 : 0), 0);
    const uncovered = wardBeds.value.filter(b => b.patientName && !pumpsOf(b).length && !bedSensorOf(b)).length;
    const alarm = wardBeds.value.reduce((n, b) => n + pumpsOf(b).filter(p => p.alarm).length
        + (bedSensorOf(b)?.vitalBad ? 1 : 0), 0);
    return {occupied, devices: devicesCount, alarm, uncovered, free: wardBeds.value.length - occupied};
});

/** 最新遥测按设备编号索引：采样在 TDengine，绑定在 MySQL，两边靠 device_no 对齐 */
const telemetryByNo = computed(() => {
    const map = new Map();
    for (const d of devices.value) map.set(d.deviceNo, d);
    return map;
});

/** 床垫固定装在床位，按 bed_no 对齐；不同病区床号会重复，必须同时限定科室 */
const normBed = (no) => String(no == null ? '' : no).trim().replace(/^0+/, '');
const bedTelemetryByNo = computed(() => {
    const map = new Map();
    for (const d of bedLatest.value) map.set(d.deviceNo, d);
    return map;
});
const bedDeviceByBedNo = computed(() => {
    const map = new Map();
    const dept = currentDeptName.value;
    for (const dev of bedDevices.value) {
        if (!dev.bedNo) continue;
        const sameDept = !dept || !dev.deptName || dev.deptName === dept
            || dept.includes(dev.deptName) || dev.deptName.includes(dept);
        if (sameDept) map.set(normBed(dev.bedNo), dev);
    }
    return map;
});

/**
 * 床位对应的床垫体征。床垫是固定设备，患者转床后床垫不跟着走，
 * 因此按 bedNo 找设备、再看该设备最新值，而不是按 admissionId。
 */
const bedSensorOf = (bed) => {
    const dev = bedDeviceByBedNo.value.get(normBed(bed?.bedNo));
    if (!dev) return null;
    const telemetry = bedTelemetryByNo.value.get(dev.deviceNo) || null;
    const online = !!telemetry && !isStale(telemetry);
    const inBed = truthy(metricValue(telemetry, 'inBed'));
    const hr = numberValue(metricValue(telemetry, 'heartRate'));
    const rr = numberValue(metricValue(telemetry, 'respRate'));
    const hrBad = online && inBed && hr != null && (hr > 110 || hr < 50);
    const rrBad = online && inBed && rr != null && (rr > 26 || rr < 8);
    return {
        deviceNo: dev.deviceNo,
        telemetry,
        online,
        inBed,
        hr,
        rr,
        hrBad,
        rrBad,
        vitalBad: hrBad || rrBad,
        lastSeen: telemetry?.ts ? shortTime(telemetry.ts) : '',
    };
};

/** 在用绑定按住院就诊索引：泵跟着患者走，转床后仍挂同一张床位卡 */
const bindsByAdmission = computed(() => {
    const map = new Map();
    for (const b of activeBinds.value) {
        const key = String(b.admissionId);
        if (!map.has(key)) map.set(key, []);
        map.get(key).push(b);
    }
    return map;
});

/**
 * 这张床的输液泵（可能多台：多通道输液）。
 * 每项 = 绑定关系 + 该泵最新遥测，online 以最近一次上报时间判（>60s 视为离线）。
 */
const pumpsOf = (bed) => {
    if (!bed?.admissionId) return [];
    const binds = bindsByAdmission.value.get(String(bed.admissionId)) || [];
    return binds.map(b => {
        const telemetry = telemetryByNo.value.get(b.deviceNo) || null;
        return {
            ...b,
            telemetry,
            online: !!telemetry && !isStale(telemetry),
            alarm: !!telemetry && metricValue(telemetry, 'occluded') === '是',
        };
    });
};

const lastPumpTime = (bed) => {
    const times = pumpsOf(bed).map(p => p.telemetry?.ts).filter(Boolean);
    if (!times.length) return '';
    return shortTime(times.sort().at(-1));
};
const lastDeviceTime = (bed) => {
    const pump = lastPumpTime(bed);
    const sensor = bedSensorOf(bed)?.lastSeen || '';
    if (pump && sensor) return pump > sensor ? pump : sensor;
    return pump || sensor;
};

const metricValue = (device, key) => {
    const m = (device?.metrics || []).find(x => x.key === key);
    return m ? m.value : null;
};
const numberValue = (v) => {
    const n = Number(v);
    return v == null || v === '' || !Number.isFinite(n) ? null : n;
};
const truthy = (v) => v === true || v === 'true' || v === '是' || v === 1 || v === '1';
const metric = (device, key) => {
    const v = metricValue(device, key);
    return v == null || v === '' ? '—' : v;
};
const lowBattery = (d) => {
    const v = Number(metricValue(d, 'battery'));
    return Number.isFinite(v) && v <= 20;
};

/** 超过 60 秒没有新采样视为离线（模拟器 1 秒一条，真机也远快于这个阈值） */
const isStale = (d) => {
    if (!d?.ts) return true;
    const t = new Date(String(d.ts).replace(' ', 'T')).getTime();
    return !Number.isFinite(t) || Date.now() - t > 60000;
};

const shortTime = (ts) => (ts ? String(ts).slice(11, 19) : '—');
const genderText = (g) => (g === 1 ? '男' : g === 2 ? '女' : '—');

const cardHeaderClass = (bed) => {
    if (pumpsOf(bed).some(p => p.alarm) || bedSensorOf(bed)?.vitalBad) return 'bg-red-50 border-red-100';
    if (bed.critical) return 'bg-orange-50 border-orange-100';
    return bed.patientName ? 'bg-[#F2F7FC] border-gray-100' : 'bg-gray-50 border-gray-100';
};

/**
 * 当前所有告警键：输液泵阻塞 + 床垫体征越界（心率/呼吸超阈值）。
 * 只算当前病区视图里的床：切到某病区时不替别的病区叫。
 */
const alarmKeys = () => {
    const keys = new Set();
    for (const bed of wardBeds.value) {
        for (const p of pumpsOf(bed)) {
            if (p.alarm) keys.add(`pump:${p.deviceNo}`);
        }
        const sensor = bedSensorOf(bed);
        if (sensor?.vitalBad) keys.add(`bed:${sensor.deviceNo}:vital`);
    }
    return keys;
};

/**
 * 轮询兜底也要响（SSE 断了不能变成哑巴）：与上一轮比，只对新出现的告警键发声。
 * SSE 正常时事件会先记进 alarmSeen，这里就不会重复响。
 */
const syncAlarmSound = (silent = false) => {
    const now = alarmKeys();
    const fresh = [...now].filter(k => !alarmSeen.has(k));
    if (!silent && fresh.length) playAlert('critical');
    alarmSeen = now;
};

/** 连接科室告警流：设备事件一落库就响，不用等下一轮 10 秒 */
const connectAlarmStream = () => {
    if (closeAlarmStream) {
        closeAlarmStream();
        closeAlarmStream = null;
    }
    if (!deptId.value) return;
    closeAlarmStream = openIotAlarmStream({
        deptId: deptId.value,
        onAlarm: (data) => {
            if (!data || data.type !== 'iot-alarm') return;
            const eventId = String(data.eventId ?? '');
            if (eventId && buzzedEvents.has(eventId)) return;
            if (eventId) buzzedEvents.add(eventId);
            const level = Number(data.level) || 1;
            // 先记账再响：随后 loadBeds 会把这台泵算进 alarmSeen，避免轮询兜底再响一遍
            if (data.deviceNo) alarmSeen.add(`pump:${data.deviceNo}`);
            if (level >= 2) playAlert(level >= 3 ? 'critical' : 'warning');
            loadBeds(); // 卡片立即变红，不等下一轮轮询
        },
        onFallback: (reason) => {
            console.warn('设备告警 SSE 断开，退回 10 秒轮询：', reason);
            closeAlarmStream = null;
        },
    });
};

/** 切科室：告警键与去重集合都得重来，否则会把新科室的历史告警当成「新告警」叫一遍 */
const resetAlarmState = () => {
    alarmSeen = new Set();
    alarmPrimed = false;
    buzzedEvents.clear();
};

const loadDepts = async () => {
    try {
        depts.value = await loadDeptOptions();
        if (!depts.value.length) {
            loadError.value = '当前岗位没有被授权任何科室';
            return;
        }
        // 默认当前岗位所在科室；没有岗位科室时退第一个住院类科室，不再硬编码演示科室
        const own = depts.value.find(d => String(d.id) === String(localStorage.getItem('deptId')));
        const inpatient = depts.value.find(d => String(d.deptType || '').includes('4'));
        deptId.value = (own || inpatient || depts.value[0]).id;
    } catch (e) {
        console.error('加载科室下拉失败', e);
        loadError.value = '科室下拉加载失败，请检查权限或网络';
    }
};

/**
 * 按床位主键原地合并：轮询每次拿回的都是全新对象，直接 beds.value = 新数组会让每张卡整块重建，
 * 护士看到的就是「闪一下」。同 key 的对象复用（只覆盖属性），页面只更新变了的文本。
 */
const mergeByBedId = (list) => {
    const old = new Map(beds.value.map(b => [b.bedId, b]));
    beds.value = list.map(n => {
        const o = old.get(n.bedId);
        if (!o) return n;
        old.delete(n.bedId);
        Object.assign(o, n);
        return o;
    });
};

/**
 * silent = true（10 秒轮询）：不置 loading（否则每轮盖一层遮罩，就是那个「闪」）、
 * 不弹失败提示（10 秒一次的报错弹窗会盖住大屏）；失败时保留上一帧数据。
 * silent = false（首屏/切科室/点刷新）：照旧显示遮罩与错误。
 */
const loadBeds = async ({silent = false} = {}) => {
    if (!deptId.value) return;
    if (!silent) {
        loading.value = true;
        loadError.value = '';
    }
    try {
        const [bedRes, devRes, bindRes, bedDevRes, bedLatestRes] = await Promise.all([
            getBedMap({deptId: deptId.value}),
            iotLatestList({deviceType: 'infusion'}),
            iotActiveBinds({deviceType: 'infusion'}),
            iotDeviceSelectList({deviceType: 'bed'}),
            iotLatestList({deviceType: 'bed'}),
        ]);
        const nextBeds = (bedRes.data?.beds || [])
            .filter(b => b.bedNo)
            // 有患者的床排前面，空床沉底：护士站第一眼只看在院患者
            .sort((a, b) => (b.patientName ? 1 : 0) - (a.patientName ? 1 : 0)
                || String(a.bedNo).localeCompare(String(b.bedNo), 'zh-CN', {numeric: true}));
        mergeByBedId(nextBeds);
        loadError.value = '';
        devices.value = devRes.data || [];
        activeBinds.value = bindRes.data || [];
        bedDevices.value = bedDevRes.data || [];
        bedLatest.value = bedLatestRes.data || [];
        if (!beds.value.length) loadError.value = '该科室暂无床位';
        lastRefresh.value = new Date().toLocaleTimeString('zh-CN', {hour12: false});
        // 轮询兜底发声：首轮静默，之后只叫「新出现」的告警（SSE 已在线的会被它提前记账，不会双响）
        syncAlarmSound(!alarmPrimed);
        alarmPrimed = true;
    } catch (e) {
        console.error('加载病区监护数据失败', e);
        const msg = e?.response?.data?.message || e?.message || '接口调用失败';
        loadError.value = msg;
        if (!silent) ElMessage.error(msg);
    } finally {
        if (!silent) loading.value = false;
    }
};

const onDeptChange = () => {
    wardId.value = 'ALL';
    resetAlarmState();
    loadBeds();
    connectAlarmStream();
};

const openTrend = (bed) => {
    const p = pumpsOf(bed).find(x => x.telemetry);
    if (p) {
        openPumpTrend(p);
        return;
    }
    const sensor = bedSensorOf(bed);
    if (sensor?.telemetry) {
        trendDevice.value = {...sensor.telemetry, deviceNo: sensor.deviceNo, deviceType: 'bed',
            deviceTypeLabel: `${bed.bedNo} 床床垫`};
        trendVisible.value = true;
    }
};

const openPumpTrend = (pump) => {
    if (!pump?.telemetry) return;
    trendDevice.value = {...pump.telemetry, deviceTypeLabel: '输液泵'};
    trendVisible.value = true;
};

let timer = null;
onMounted(async () => {
    await loadDepts();
    await loadBeds();
    connectAlarmStream();
    timer = setInterval(() => {
        // 轮询静默：不挂 loading 遮罩，数据原地合并，护士看到的是数字直接变而不是整屏重绘
        if (!document.hidden) loadBeds({silent: true});
    }, 10000);
});
onUnmounted(() => {
    if (timer) clearInterval(timer);
    if (closeAlarmStream) closeAlarmStream();
});
</script>
