<template>
  <canvas ref="canvasRef" class="mon-ecg-canvas"></canvas>
</template>

<script setup>
/**
 * 床旁监护 ECG 小波形（II 导联）。
 *
 * <p>数据来自 /iot/telemetry/waveformList 的最近 4 秒窗（100Hz），随大屏 10 秒轮询整体重绘，
 * 不做前端滚动拼接——大屏本身是轮询口径，窗口整体重绘即可看出心律形态（窦性/房颤不齐/早搏/室颤乱颤/停搏直线）。
 * 呼吸波、脉搏波在设备趋势弹窗里看，床位卡只放最能判读心律的一条 ECG。
 */
import {onMounted, ref, watch} from 'vue'

const props = defineProps({
  /** ECG 采样点数组（-1.5~1.5 mV 量级），空数组画平直基线 */
  data: {type: Array, default: () => []}
})

const canvasRef = ref(null)

const draw = () => {
  const canvas = canvasRef.value
  if (!canvas) return
  const dpr = window.devicePixelRatio || 1
  const W = canvas.clientWidth * dpr
  const H = canvas.clientHeight * dpr
  if (!W || !H) return
  canvas.width = W
  canvas.height = H
  const ctx = canvas.getContext('2d')
  ctx.clearRect(0, 0, W, H)

  const data = props.data || []
  const base = H / 2
  const amp = H * 0.32
  ctx.strokeStyle = '#16a34a'
  ctx.lineWidth = 1.2 * dpr
  ctx.beginPath()
  if (!data.length) {
    ctx.moveTo(0, base)
    ctx.lineTo(W, base)
  }
  data.forEach((v, i) => {
    const x = W * (data.length > 1 ? i / (data.length - 1) : 0)
    const y = base - (Number(v) || 0) * amp
    i ? ctx.lineTo(x, y) : ctx.moveTo(x, y)
  })
  ctx.stroke()
}

onMounted(draw)
watch(() => props.data, draw)
</script>

<style scoped>
.mon-ecg-canvas {
  width: 100%;
  height: 44px;
  display: block;
  background: #0c1116;
  border-radius: 6px;
}
</style>
