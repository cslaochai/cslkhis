<template>
  <div class="rounded-lg border border-slate-200 bg-slate-50 p-3" data-testid="emg-mews-trend">
    <div class="mb-1 flex items-center justify-between">
      <span class="text-xs font-bold text-slate-500">MEWS 趋势</span>
      <span class="text-[11px] text-slate-400">0–14 分 · ≥9 高危 / 5–8 中危 / &lt;5 低危</span>
    </div>
    <svg :viewBox="`0 0 ${W} ${H}`" class="w-full" preserveAspectRatio="xMidYMid meet">
      <!-- 网格基线 -->
      <line :x1="P" :y1="topY" :x2="W - P" :y2="topY" stroke="#e2e8f0" stroke-width="1"/>
      <line :x1="P" :y1="midY" :x2="W - P" :y2="midY" stroke="#e2e8f0" stroke-width="1" stroke-dasharray="3 3"/>
      <line :x1="P" :y1="botY" :x2="W - P" :y2="botY" stroke="#e2e8f0" stroke-width="1"/>
      <text :x="2" :y="topY + 3" class="fill-slate-400" font-size="9">14</text>
      <text :x="2" :y="midY + 3" class="fill-slate-400" font-size="9">7</text>
      <text :x="2" :y="botY + 3" class="fill-slate-400" font-size="9">0</text>
      <!-- 连线 -->
      <polyline v-if="pts.length > 1" :points="linePoints" fill="none" stroke="#94a3b8" stroke-width="1.5"/>
      <!-- 点 -->
      <g v-for="(p, i) in pts" :key="i">
        <circle :cx="p.x" :cy="p.y" r="4" :fill="p.color" stroke="#fff" stroke-width="1"/>
        <text :x="p.x" :y="p.y - 8" text-anchor="middle" class="fill-slate-600" font-size="9">{{ p.score }}</text>
      </g>
    </svg>
    <div v-if="pts.length === 0" class="py-4 text-center text-xs text-slate-400">暂无评分数据</div>
    <div class="mt-1 flex flex-wrap gap-x-3 text-[11px] text-slate-400">
      <span v-for="(p, i) in pts" :key="'l' + i">{{ p.tip }}</span>
    </div>
  </div>
</template>

<script setup>
import {computed} from 'vue';

const props = defineProps({
  points: {type: Array, default: () => []}, // [{ t:'2026-...', score: 7 }]
});

const W = 320, H = 100, P = 22, topY = 14, botY = 84;
const midY = (topY + botY) / 2;

const scoreColor = (s) => (s >= 9 ? '#dc2626' : s >= 5 ? '#d97706' : '#64748b');

const pts = computed(() => {
  const arr = (props.points || []).filter(x => x && x.score != null);
  const n = arr.length;
  return arr.map((x, i) => {
    const xPos = n === 1 ? W / 2 : P + (i / (n - 1)) * (W - 2 * P);
    const yPos = topY + (1 - Math.min(Math.max(x.score, 0), 14) / 14) * (botY - topY);
    return {x: xPos, y: yPos, score: x.score, color: scoreColor(x.score), tip: `${x.t || ''} ${x.score}`};
  });
});

const linePoints = computed(() => pts.value.map(p => `${p.x},${p.y}`).join(' '));
</script>
