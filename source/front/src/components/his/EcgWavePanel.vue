<script setup lang="ts">
/**
 * 12 导联心电图波形面板（sql/173 心电工作站）
 *
 * waveData 是设备推送 / 模拟采集落库的 JSON 字符串，契约：
 * {
 *   sampleRate: 250,        // 采样率 Hz
 *   durationSec: 10,        // 采了多少秒
 *   gainMmPerMv: 10,        // 定标：1mV = 10mm（心电图纸标准灵敏度）
 *   paperSpeedMmPerS: 25,   // 走纸速度：25mm/s
 *   leads: [{ name: 'I', samples: [...] }, ×12],
 *   rhythm: { name: 'II', samples: [...] }  // II 导联长节律条
 * }
 *
 * 渲染口径 = 真实心电图纸：SVG viewBox 以 mm 为单位（1 单位 = 1mm），
 * 走纸速度决定每毫秒占多宽、定标电压决定 1mV 占多高 —— 波形天然按设备参数缩放，
 * 改 gain / paperSpeed 时图纸跟着变，而不是写死一版像素。
 */
import {computed} from 'vue'

const props = defineProps<{
  /** 波形 JSON 字符串（biz_ecg_waveform.wave_data 原样透传） */
  waveData?: string | null
  /** 面板标题（如「术前常规心电图」），空则不显示 */
  title?: string
}>()

interface WaveJson {
  sampleRate?: number
  durationSec?: number
  gainMmPerMv?: number
  paperSpeedMmPerS?: number
  rhythmName?: string
  leads?: { name: string; samples: number[] }[]
  rhythm?: { name: string; samples: number[] }
}

const parsed = computed<WaveJson | null>(() => {
  if (!props.waveData) return null
  try {
    const v = JSON.parse(props.waveData)
    return Array.isArray(v?.leads) && v.leads.length ? v : null
  } catch {
    return null
  }
})

const gain = computed(() => parsed.value?.gainMmPerMv || 10)
const paperSpeed = computed(() => parsed.value?.paperSpeedMmPerS || 25)
const sampleRate = computed(() => parsed.value?.sampleRate || 250)

// 图纸几何：单元格 50mm 宽（25mm/s 下 = 2.5s）× 32mm 高，4 行 × 3 列 + 节律条
const CELL_W = 50
const CELL_H = 32
const GAP = 4
const MARGIN_L = 9
const MARGIN_T = 8
const COLS = 3
const ROWS = 4
const GRID_W = MARGIN_L + COLS * CELL_W + (COLS - 1) * GAP + 2
const STRIP_H = 26
const GRID_H = MARGIN_T + ROWS * CELL_H + (ROWS - 1) * GAP + GAP + STRIP_H + 6

// 标准排布（行 × 列）： limb → augmented → 前胸
const ROW_LEADS = [
  ['I', 'II', 'III'],
  ['aVR', 'aVL', 'aVF'],
  ['V1', 'V2', 'V3'],
  ['V4', 'V5', 'V6'],
]

const leadMap = computed<Record<string, number[]>>(() => {
  const m: Record<string, number[]> = {}
  parsed.value?.leads?.forEach(l => {
    m[l.name] = l.samples || []
  })
  return m
})

/** 1 采样占多少 mm（走纸速度 / 采样率） */
const mmPerSample = computed(() => paperSpeed.value / Math.max(sampleRate.value, 1))

interface Cell {
  x: number
  y: number
  label: string
  points: string
}

const cells = computed<Cell[]>(() => {
  const out: Cell[] = []
  const mps = mmPerSample.value
  ROW_LEADS.forEach((names, r) => {
    names.forEach((name, c) => {
      const x = MARGIN_L + c * (CELL_W + GAP)
      const y = MARGIN_T + r * (CELL_H + GAP)
      const samples = leadMap.value[name]
      // 每列显示 CELL_W mm = CELL_W / mps 个采样点，超出部分本格不画（节律条看长程）
      const maxSamples = Math.min(samples?.length || 0, Math.floor(CELL_W / mps))
      const half = CELL_H / 2 - 2
      const baseline = y + CELL_H / 2
      const pts: string[] = []
      for (let i = 0; i < maxSamples; i++) {
        const px = x + i * mps
        // mV → mm（定标 gain），限幅避免大电压溢出格子
        const dy = Math.max(-half * 1.6, Math.min(half * 1.6, -(samples[i] || 0) * gain.value))
        const py = Math.max(y + 1, Math.min(y + CELL_H - 1, baseline + dy))
        pts.push(`${px.toFixed(1)},${py.toFixed(1)}`)
      }
      out.push({x, y, label: name, points: pts.join(' ')})
    })
  })
  return out
})

const rhythmPoints = computed(() => {
  const samples = parsed.value?.rhythm?.samples || []
  const mps = mmPerSample.value
  const x0 = MARGIN_L
  const y0 = MARGIN_T + ROWS * CELL_H + (ROWS - 1) * GAP + GAP
  const w = GRID_W - MARGIN_L - 2
  const maxSamples = Math.min(samples.length, Math.floor(w / mps))
  const baseline = y0 + STRIP_H / 2
  const half = STRIP_H / 2 - 2
  const pts: string[] = []
  for (let i = 0; i < maxSamples; i++) {
    const px = x0 + i * mps
    const dy = Math.max(-half * 1.6, Math.min(half * 1.6, -(samples[i] || 0) * gain.value))
    const py = Math.max(y0 + 1, Math.min(y0 + STRIP_H - 1, baseline + dy))
    pts.push(`${px.toFixed(1)},${py.toFixed(1)}`)
  }
  return pts.join(' ')
})

const rhythmLabel = computed(() => {
  const r: any = parsed.value?.rhythm
  return r?.name ? `${r.name}（节律条）` : '节律条'
})
</script>

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
        <pattern id="ecgGrid1" width="1" height="1" patternUnits="userSpaceOnUse">
          <path d="M 1 0 L 0 0 0 1" fill="none" stroke="#f8d0ca" stroke-width="0.1"/>
        </pattern>
        <pattern id="ecgGrid5" width="5" height="5" patternUnits="userSpaceOnUse">
          <rect width="5" height="5" fill="url(#ecgGrid1)"/>
          <path d="M 5 0 L 0 0 0 5" fill="none" stroke="#f0a8a0" stroke-width="0.25"/>
        </pattern>
      </defs>
      <rect x="0" y="0" :width="GRID_W" :height="GRID_H" fill="url(#ecgGrid5)"/>

      <g v-for="cell in cells" :key="cell.label">
        <rect :x="cell.x" :y="cell.y" :width="CELL_W" :height="CELL_H" fill="#fffdf9"
              stroke="#f0a8a0" stroke-width="0.2"/>
        <text :x="cell.x + 1.6" :y="cell.y + 4.2" font-size="3.4" font-weight="600" fill="#0f172a">
          {{ cell.label }}
        </text>
        <polyline v-if="cell.points" :points="cell.points" fill="none" stroke="#0f172a"
                  stroke-width="0.28" stroke-linejoin="round"/>
      </g>

      <!-- 节律条 -->
      <rect :x="MARGIN_L" :y="MARGIN_T + ROWS * CELL_H + (ROWS - 1) * GAP + GAP"
            :width="GRID_W - MARGIN_L - 2" :height="STRIP_H" fill="#fffdf9"
            stroke="#f0a8a0" stroke-width="0.2"/>
      <text :x="MARGIN_L + 1.6" :y="MARGIN_T + ROWS * CELL_H + (ROWS - 1) * GAP + GAP + 4.2"
            font-size="3.4" font-weight="600" fill="#0f172a">
        {{ rhythmLabel }}
      </text>
      <polyline v-if="rhythmPoints" :points="rhythmPoints" fill="none" stroke="#0f172a"
                stroke-width="0.28" stroke-linejoin="round"/>
    </svg>

    <!-- 无波形：采集前 / 老数据没有波形 -->
    <div v-else class="flex h-40 items-center justify-center rounded bg-slate-50 text-sm text-slate-400">
      暂无波形数据：请先完成波形采集
    </div>
  </div>
</template>
