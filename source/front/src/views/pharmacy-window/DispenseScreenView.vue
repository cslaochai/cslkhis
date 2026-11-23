<template>
  <div class="flex min-h-screen flex-col items-center justify-center bg-slate-900 text-white">
    <p class="mb-2 text-xl text-slate-400">{{ windowName || '取药窗口' }}</p>

    <template v-if="current">
      <p class="text-sm text-slate-500">请以下患者到本窗口取药</p>
      <p class="my-6 font-mono text-8xl font-bold tracking-widest text-emerald-400">{{ current.queueNo }}</p>
      <p class="text-4xl font-bold">{{ current.patientName }}</p>
      <p class="mt-4 text-sm text-slate-500">处方号 {{ current.prescriptionNo }}</p>
    </template>
    <template v-else>
      <p class="my-10 text-4xl text-slate-600">请稍候</p>
      <p class="text-sm text-slate-500">本窗口暂未叫号</p>
    </template>

    <p class="absolute bottom-6 text-xs text-slate-600">
      取药请出示处方单或就诊卡 · {{ nowText }}
    </p>
  </div>
</template>

<script setup>
/**
 * 取药叫号屏
 *
 * 一块屏对一个窗口，靠 URL 参数绑定：`/dispense-screen?stationCode=OP-DRUG-2`。
 * 不落库（biz_opd_station 没有设备码字段），改窗口就是改 URL，装屏时配一次即可。
 *
 * 轮询 `/currentCall` 而不是接 WebSocket：叫号屏只要「当前叫的号」这一个值，
 * 5 秒延迟对患者无感，却省掉一路长连接和断线重连。
 */
import {onMounted, onUnmounted, ref} from 'vue';
import {useRoute} from 'vue-router';
import {getOpdStationSelectList} from '@/api/opdStation';
import {getCurrentCall} from '@/api/dispenseQueue';

const route = useRoute();
const current = ref(null);
const windowName = ref('');
const nowText = ref('');
let timer = null;
let clock = null;
let windowId = null;

const pad = (n) => String(n).padStart(2, '0');
const tickClock = () => {
  const d = new Date();
  nowText.value = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

/** 按 stationCode 找窗口；没有该参数就用第一个发药窗口，便于先装屏后配码 */
const resolveWindow = async () => {
  try {
    const res = await getOpdStationSelectList({stationKind: 6});
    const list = (res.data || []).filter((s) => {
      const kind = s.stationKind ?? s.station_kind;
      return kind === undefined || kind === null || kind === 6;
    });
    const code = route.query.stationCode;
    const hit = code ? list.find((s) => (s.stationCode || s.station_code) === code) : list[0];
    if (!hit) {
      windowName.value = code || '';
      return;
    }
    windowId = hit.id;
    windowName.value = hit.stationName || hit.station_name || '';
  } catch (e) {
    console.error('加载发药窗口失败', e);
  }
};

const loadCurrent = async () => {
  if (!windowId) return;
  try {
    const res = await getCurrentCall(windowId);
    current.value = res.data || null;
  } catch (e) {
    // 轮询失败保留上一屏内容，下一轮再试，避免屏幕闪成「请稍候」
  }
};

onMounted(async () => {
  tickClock();
  clock = setInterval(tickClock, 30000);
  await resolveWindow();
  loadCurrent();
  timer = setInterval(loadCurrent, 5000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
  if (clock) clearInterval(clock);
});
</script>
