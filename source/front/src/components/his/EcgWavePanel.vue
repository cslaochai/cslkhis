<template>
  <div class="rounded-lg border border-slate-200 bg-white p-3">
    <div v-if="title || parsed" class="mb-2 flex flex-wrap items-center justify-between gap-2">
      <span class="text-sm font-medium text-slate-700">{{ title || '12 导联心电图' }}</span>
      <span v-if="parsed" class="text-xs text-slate-400">
        定标 {{ gain }}mm/mV · 走纸 {{ paperSpeed }}mm/s · 采样 {{ sampleRate }}Hz
      </span>
    </div>

    <!-- 有波形：心电图纸（粉红格 1mm 小格 / 5mm 大格）+ 12 导联 + 节律条 -->
    <svg v-if="parsed" :viewBox="`0 0 ${GRID_W} ${GRID_H}`" class="w-full rounded"
         style="background: #fef7f5">
      <defs>
        <pattern id="ecgGrid1" height="1" patternUnits="userSpaceOnUse" width="1">
          <path d="M 1 0 L 0 0 0 1" fill="none" stroke="#f8d0ca" stroke-width="0.1"/>
        </pattern>
        <pattern id="ecgGrid5" height="5" patternUnits="userSpaceOnUse" width="5">
          <rect fill="url(#ecgGrid1)" height="5" width="5"/>
          <path d="M 5 0 L 0 0 0 5" fill="none" stroke="#f0a8a0" stroke-width="0.25"/>
        </pattern>
      </defs>
      <rect :height="GRID_H" :width="GRID_W" fill="url(#ecgGrid5)" x="0" y="0"/>

      <g v-for="cell in cells" :key="cell.label">
        <rect :height="CELL_H" :width="CELL_W" :x="cell.x" :y="cell.y" fill="#fffdf9"
              stroke="#f0a8a0" stroke-width="0.2"/>
        <text :x="cell.x + 1.6" :y="cell.y + 4.2" fill="#0f172a" font-size="3.4" font-weight="600">
          {{ cell.label }}
        </text>
        <polyline v-if="cell.points" :points="cell.points" fill="none" stroke="#0f172a"
                  stroke-linejoin="round" stroke-width="0.28"/>
      </g>

      <!-- 节律条 -->
      <rect :height="STRIP_H" :width="GRID_W - MARGIN_L - 2"
            :x="MARGIN_L" :y="MARGIN_T + ROWS * CELL_H + (ROWS - 1) * GAP + GAP" fill="#fffdf9"
            stroke="#f0a8a0" stroke-width="0.2"/>
      <text :x="MARGIN_L + 1.6" :y="MARGIN_T + ROWS * CELL_H + (ROWS - 1) * GAP + GAP + 4.2"
            fill="#0f172a" font-size="3.4" font-weight="600">
        {{ rhythmLabel }}
      </text>
      <polyline v-if="rhythmPoints" :points="rhythmPoints" fill="none" stroke="#0f172a"
                stroke-linejoin="round" stroke-width="0.28"/>
    </svg>

    <!-- 无波形：采集前 / 老数据没有波形 -->
    <div v-else class="flex h-40 items-center justify-center rounded bg-slate-50 text-sm text-slate-400">
      暂无波形数据：请先完成波形采集
    </div>
  </div>
</template>

<script setup>
import {computed} from 'vue';

const props = defineProps({
  waveData: {type: [String, null], required: false},
  title: {type: String, required: false}
});
const parsed = computed(() => {
  if (!props.waveData)
    return null;
  try {
    const v = JSON.parse(props.waveData);
    return Array.isArray(v?.leads) && v.leads.length ? v : null;
  } catch {
    return null;
  }
});
const gain = computed(() => parsed.value?.gainMmPerMv || 10);
const paperSpeed = computed(() => parsed.value?.paperSpeedMmPerS || 25);
const sampleRate = computed(() => parsed.value?.sampleRate || 250);
// 图纸几何：单元格 50mm 宽（25mm/s 下 = 2.5s）× 32mm 高，4 行 × 3 列 + 节律条
const CELL_W = 50;
const CELL_H = 32;
const GAP = 4;
const MARGIN_L = 9;
const MARGIN_T = 8;
const COLS = 3;
const ROWS = 4;
const GRID_W = MARGIN_L + COLS * CELL_W + (COLS - 1) * GAP + 2;
const STRIP_H = 26;
const GRID_H = MARGIN_T + ROWS * CELL_H + (ROWS - 1) * GAP + GAP + STRIP_H + 6;
// 标准排布（行 × 列）： limb → augmented → 前胸
const ROW_LEADS = [
  ['I', 'II', 'III'],
  ['aVR', 'aVL', 'aVF'],
  ['V1', 'V2', 'V3'],
  ['V4', 'V5', 'V6'],
];
const leadMap = computed(() => {
  const m = {};
  parsed.value?.leads?.forEach(l => {
    m[l.name] = l.samples || [];
  });
  return m;
});
/** 1 采样占多少 mm（走纸速度 / 采样率） */
const mmPerSample = computed(() => paperSpeed.value / Math.max(sampleRate.value, 1));
const cells = computed(() => {
  const out = [];
  const mps = mmPerSample.value;
  ROW_LEADS.forEach((names, r) => {
    names.forEach((name, c) => {
      const x = MARGIN_L + c * (CELL_W + GAP);
      const y = MARGIN_T + r * (CELL_H + GAP);
      const samples = leadMap.value[name];
      // 每列显示 CELL_W mm = CELL_W / mps 个采样点，超出部分本格不画（节律条看长程）
      const maxSamples = Math.min(samples?.length || 0, Math.floor(CELL_W / mps));
      const half = CELL_H / 2 - 2;
      const baseline = y + CELL_H / 2;
      const pts = [];
      for (let i = 0; i < maxSamples; i++) {
        const px = x + i * mps;
        // mV → mm（定标 gain），限幅避免大电压溢出格子
        const dy = Math.max(-half * 1.6, Math.min(half * 1.6, -(samples[i] || 0) * gain.value));
        const py = Math.max(y + 1, Math.min(y + CELL_H - 1, baseline + dy));
        pts.push(`${px.toFixed(1)},${py.toFixed(1)}`);
      }
      out.push({x, y, label: name, points: pts.join(' ')});
    });
  });
  return out;
});
const rhythmPoints = computed(() => {
  const samples = parsed.value?.rhythm?.samples || [];
  const mps = mmPerSample.value;
  const x0 = MARGIN_L;
  const y0 = MARGIN_T + ROWS * CELL_H + (ROWS - 1) * GAP + GAP;
  const w = GRID_W - MARGIN_L - 2;
  const maxSamples = Math.min(samples.length, Math.floor(w / mps));
  const baseline = y0 + STRIP_H / 2;
  const half = STRIP_H / 2 - 2;
  const pts = [];
  for (let i = 0; i < maxSamples; i++) {
    const px = x0 + i * mps;
    const dy = Math.max(-half * 1.6, Math.min(half * 1.6, -(samples[i] || 0) * gain.value));
    const py = Math.max(y0 + 1, Math.min(y0 + STRIP_H - 1, baseline + dy));
    pts.push(`${px.toFixed(1)},${py.toFixed(1)}`);
  }
  return pts.join(' ');
});
const rhythmLabel = computed(() => {
  const r = parsed.value?.rhythm;
  return r?.name ? `${r.name}（节律条）` : '节律条';
});
</script>
