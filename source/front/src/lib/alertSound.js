import {ref} from 'vue'

/**
 * 站内提示音 / 设备告警音：Web Audio 即时合成，不引入任何音频文件。
 *
 * <p>为什么不用 mp3：音色要按级别区分（提示/警告/严重），合成比维护三份音频资源轻，
 * 也不用管部署路径与版权；延迟也更低（无解码）。
 *
 * <p>浏览器的自动播放策略是这里唯一的硬约束：Chrome/Edge 要求页面有过用户手势，
 * 否则 AudioContext 处于 suspended，play() 静默失败且不报错。所以：
 *   1) 页面首次 pointerdown / keydown 时 resume 一次，解锁后整个标签页可用；
 *   2) 未解锁时 playAlert 直接放弃，绝不抛错、不改任何状态（不能因为没声音把消息流程打断）。
 * 病区大屏这类「无人点击」的页面另有方案（Chrome 启动参数 / kiosk），不在本模块处理。
 */

const MUTE_KEY = 'his:alertSound:muted'
const LAST_BEEP_KEY = 'his:alertSound:lastBeep'
/** 同一用户多标签页都会收到同一条推送，1.5 秒内只让一个标签发声，避免双声道 */
const BEEP_WINDOW_MS = 1500
const TAB_ID = `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`

/** 静音开关：模块级 ref，顶栏按钮读写同一份；localStorage 让刷新/切页/换标签都保持 */
export const alertMuted = ref(localStorage.getItem(MUTE_KEY) === '1')

/** 浏览器是否已解锁发声（页面有过用户手势）。监护大屏用它提示「点击开启声音」 */
export const alertAudioReady = ref(false)

export const setAlertMuted = (muted) => {
  alertMuted.value = !!muted
  try {
    localStorage.setItem(MUTE_KEY, alertMuted.value ? '1' : '0')
  } catch {
    // 隐私模式禁用 storage：本次会话仍然生效，只是不持久
  }
}

export const toggleAlertMuted = () => setAlertMuted(!alertMuted.value)

/**
 * 音色表：info 单短音、warning 双音、critical 三连音（方波更刺耳，用于急会诊/严重告警）。
 * 频率取中低段（660~880Hz），护士站/病房环境里穿透力够，又不至于像消防报警。
 */
const TONES = {
  info: {freq: 880, dur: 0.14, gain: 0.30, times: 1, gap: 0, wave: 'sine'},
  warning: {freq: 740, dur: 0.16, gain: 0.35, times: 2, gap: 0.10, wave: 'sine'},
  critical: {freq: 660, dur: 0.20, gain: 0.45, times: 3, gap: 0.12, wave: 'square'},
}

let ctx = null

const ensureCtx = () => {
  if (ctx) return ctx
  const AC = window.AudioContext || window.webkitAudioContext
  if (!AC) return null
  ctx = new AC()
  return ctx
}

/**
 * 首个用户手势里 resume。必须在手势回调内调用，否则 Chrome 不给解锁。
 */
function unlockAudio() {
  const c = ensureCtx()
  if (!c) {
    removeGestureListeners()
    return
  }
  if (c.state === 'running') {
    alertAudioReady.value = true
    removeGestureListeners()
    return
  }
  c.resume()
      .then(() => {
        if (c.state === 'running') {
          alertAudioReady.value = true
          removeGestureListeners()
        }
      })
      .catch(() => {
        // 部分浏览器在非手势上下文 resume 会拒绝，等下一次手势再试
      })
}

const removeGestureListeners = () => {
  window.removeEventListener('pointerdown', unlockAudio, true)
  window.removeEventListener('keydown', unlockAudio, true)
}

if (typeof window !== 'undefined') {
  window.addEventListener('pointerdown', unlockAudio, true)
  window.addEventListener('keydown', unlockAudio, true)
  // 其他标签页改了静音开关，本标签同步（否则顶栏图标与真实状态会不一致）
  window.addEventListener('storage', (e) => {
    if (e.key === MUTE_KEY) alertMuted.value = e.newValue === '1'
  })
}

/**
 * 抢发声权。多标签页场景下只有拿到「这一拍」的标签发声。
 * storage 不可用（隐私模式）时退化为各标签独立发声，不影响主流程。
 */
const claimedByOtherTab = () => {
  try {
    const prev = JSON.parse(localStorage.getItem(LAST_BEEP_KEY) || 'null')
    if (prev && prev.tab !== TAB_ID && Date.now() - Number(prev.at) < BEEP_WINDOW_MS) {
      return true
    }
    localStorage.setItem(LAST_BEEP_KEY, JSON.stringify({tab: TAB_ID, at: Date.now()}))
  } catch {
    return false
  }
  return false
}

const beep = (c, tone, offset) => {
  const t0 = c.currentTime + offset
  const osc = c.createOscillator()
  const gain = c.createGain()
  osc.type = tone.wave
  osc.frequency.setValueAtTime(tone.freq, t0)
  // 指数包络避免方波起停的爆音
  gain.gain.setValueAtTime(0.0001, t0)
  gain.gain.exponentialRampToValueAtTime(tone.gain, t0 + 0.02)
  gain.gain.exponentialRampToValueAtTime(0.0001, t0 + tone.dur)
  osc.connect(gain)
  gain.connect(c.destination)
  osc.start(t0)
  osc.stop(t0 + tone.dur + 0.02)
}

/**
 * 播放提示音。
 * @param {'info'|'warning'|'critical'} level 级别，未知值按 info
 * @returns {boolean} 是否真的发声（未解锁/静音/被其他标签抢占都是 false，调用方不用区分）
 */
export function playAlert(level = 'info') {
  if (alertMuted.value) return false
  const c = ensureCtx()
  if (!c || c.state !== 'running') {
    // 还没解锁：静默放弃。下一次用户点击后自然恢复
    if (c) c.resume().catch(() => {})
    return false
  }
  if (claimedByOtherTab()) return false
  alertAudioReady.value = true
  const tone = TONES[level] || TONES.info
  for (let i = 0; i < tone.times; i++) {
    beep(c, tone, i * (tone.dur + tone.gap))
  }
  return true
}

/** 消息 severity → 音色级别（后端只发 urgent/warning/info 三档） */
export const alertLevelOfSeverity = (severity) =>
    severity === 'urgent' ? 'critical' : severity === 'warning' ? 'warning' : 'info'
