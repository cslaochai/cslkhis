<template>
  <div class="inline-block">
    <canvas
        ref="canvas"
        :style="{ width: width + 'px', height: height + 'px' }"
        class="sign-canvas touch-none cursor-crosshair bg-white"
        data-testid="sign-pad"
        @pointerdown="down"
        @pointerleave="up"
        @pointermove="move"
        @pointerup="up"
    />
    <div class="mt-1 flex items-center justify-between text-xs text-slate-500">
      <span data-testid="sign-pad-hint">{{ empty ? '请在框内手写签名' : '签名已落板' }}</span>
      <el-button data-testid="sign-pad-clear" link type="primary" @click="clear">清空重签</el-button>
    </div>
  </div>
</template>

<script setup>
/**
 * 手写签名板（canvas 手指/鼠标落笔成图，toDataURL 出 PNG base64）。
 *
 * 病危重通知的家属签收用得上；后续手术/麻醉知情同意书签收是同一形态，直接复用。
 * 故意不引第三方签名库：一个 canvas + pointer 事件足够，多一个依赖多一次漂移。
 */
import {onMounted, ref} from 'vue'

const props = defineProps({
  width: {type: Number, default: 420},
  height: {type: Number, default: 150},
})
const emit = defineEmits(['change'])

const canvas = ref(null)
const empty = ref(true)
let ctx = null
let drawing = false
let last = null

const pos = (e) => {
  const r = canvas.value.getBoundingClientRect()
  return {x: e.clientX - r.left, y: e.clientY - r.top}
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
  if (empty.value) {
    empty.value = false;
    emit('change', exportPng())
  }
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
/**
 * 预填一张已有签名笔迹（base64 PNG dataURL，如员工档案预维护的笔迹）到画板上。
 * 图片按画板比例缩放铺满；铺不下的按短边等比。失败（非法 dataURL）时保持画板为空并返回 false。
 */
const setImage = (dataUrl) => {
  if (!dataUrl || !dataUrl.startsWith('data:image/')) return false
  const img = new Image()
  img.onload = () => {
    const cv = canvas.value
    ctx.clearRect(0, 0, props.width, props.height)
    ctx.save()
    ctx.fillStyle = '#ffffff'
    ctx.fillRect(0, 0, props.width, props.height)
    const scale = Math.min(props.width / img.width, props.height / img.height)
    const w = img.width * scale
    const h = img.height * scale
    ctx.drawImage(img, (props.width - w) / 2, (props.height - h) / 2, w, h)
    ctx.restore()
    empty.value = false
    emit('change', exportPng())
  }
  img.onerror = () => { /* 非法图片保持画板为空 */ }
  img.src = dataUrl
  return true
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

defineExpose({confirm, clear, isEmpty, setImage})
</script>

<style scoped>
.sign-canvas {
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  display: block;
}
</style>
