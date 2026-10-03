<script setup>
/**
 * 手写签名板（canvas 手指/鼠标落笔成图，toDataURL 出 PNG base64）。
 *
 * 病危重通知的家属签收用得上；后续手术/麻醉知情同意书签收是同一形态，直接复用。
 * 故意不引第三方签名库：一个 canvas + pointer 事件足够，多一个依赖多一次漂移。
 */
import { onMounted, ref } from 'vue'

const props = defineProps({
  width: { type: Number, default: 420 },
  height: { type: Number, default: 150 },
})
const emit = defineEmits(['change'])

const canvas = ref(null)
const empty = ref(true)
let ctx = null
let drawing = false
let last = null

const pos = (e) => {
  const r = canvas.value.getBoundingClientRect()
  return { x: e.clientX - r.left, y: e.clientY - r.top }
}
const down = (e) => {
  e.preventDefault()
  canvas.value.setPointerCapture?.(e.pointerId)
  drawing = true
  last = pos(e)
}
const move = (e) => {
  if (!drawing) return
  e.preventDefault()
  const p = pos(e)
  ctx.beginPath()
  ctx.moveTo(last.x, last.y)
  ctx.lineTo(p.x, p.y)
  ctx.stroke()
  last = p
  if (empty.value) { empty.value = false; emit('change', exportPng()) }
}
const up = (e) => {
  if (!drawing) return
  e.preventDefault()
  drawing = false
  emit('change', exportPng())
}
const exportPng = () => (empty.value ? '' : canvas.value.toDataURL('image/png'))

const clear = () => {
  ctx.clearRect(0, 0, props.width, props.height)
  guide()
  empty.value = true
  emit('change', '')
}
const confirm = () => exportPng()
const isEmpty = () => empty.value

const guide = () => {
  ctx.save()
  ctx.strokeStyle = '#d8dee9'
  ctx.setLineDash([6, 4])
  ctx.beginPath()
  ctx.moveTo(16, props.height - 34)
  ctx.lineTo(props.width - 16, props.height - 34)
  ctx.stroke()
  ctx.restore()
}

onMounted(() => {
  const cv = canvas.value
  cv.width = props.width
  cv.height = props.height
  ctx = cv.getContext('2d')
  ctx.lineWidth = 2.4
  ctx.lineCap = 'round'
  ctx.lineJoin = 'round'
  ctx.strokeStyle = '#1f2937'
  guide()
})

defineExpose({ confirm, clear, isEmpty })
</script>

<template>
  <div class="inline-block">
    <canvas
      ref="canvas"
      data-testid="sign-pad"
      class="sign-canvas touch-none cursor-crosshair bg-white"
      :style="{ width: width + 'px', height: height + 'px' }"
      @pointerdown="down"
      @pointermove="move"
      @pointerup="up"
      @pointerleave="up"
    />
    <div class="mt-1 flex items-center justify-between text-xs text-slate-500">
      <span data-testid="sign-pad-hint">{{ empty ? '请在框内手写签名' : '签名已落板' }}</span>
      <el-button link type="primary" data-testid="sign-pad-clear" @click="clear">清空重签</el-button>
    </div>
  </div>
</template>

<style scoped>
.sign-canvas {
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  display: block;
}
</style>
