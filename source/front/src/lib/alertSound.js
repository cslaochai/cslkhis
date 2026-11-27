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

/**
 * 最近一次真的发出声音的时间（毫秒）。页面上显示「上次发声 xx:xx:xx」，
 * 用来回答「到底有没有调用浏览器声音」——没有值就说明一次都没播成。
 */
export const lastAlertAt = ref(0)

/** AudioContext 当前状态（suspended/running），排查「点了还是没声」时直接看 */
export const alertCtxState = ref('')

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
 * 音色表：按 IEC 60601-1-8 的分级思路做，波形与节奏对齐真实监护设备的听感。
 * - critical：1000Hz 急促六连（≈0.83s）≈ 输液泵阻塞/体征越界的「急」；
 * - warning：660Hz 三连（≈0.69s），设备离线等非致命告警；
 * - info：780→620 柔和下行双音，站内消息/会诊邀请，刻意做得不像设备报警。
 * 每个脉冲是「正弦 + 二次谐波」，比纯正弦更接近压电蜂鸣器，护士站里一听就知道是机器在叫。
 * 持续告警的「成组重复」不在这里做，由调用方按级别重发（见 ALARM_REPEAT_MS）。
 */
const pulses = (freq, dur, count) => Array.from({length: count}, () => [freq, dur])
const TONES = {
  info: {pulses: [[780, 0.20], [620, 0.22]], gap: 0.10, gain: 0.32},
  warning: {pulses: pulses(660, 0.15, 3), gap: 0.12, gain: 0.40},
  critical: {pulses: pulses(1000, 0.09, 6), gap: 0.055, gain: 0.50},
}

/** 二次谐波占比：加一点泛音，听感从「电子音」变成「设备蜂鸣」 */
const HARMONIC = 0.25

let ctx = null

const ensureCtx = () => {
  if (ctx) return ctx
  const AC = window.AudioContext || window.webkitAudioContext
  if (!AC) return null
  ctx = new AC()
  alertCtxState.value = ctx.state
  // 状态变化（解锁/被系统挂起）同步到页面，别让「没声」只能靠猜
  ctx.onstatechange = () => {
    alertCtxState.value = ctx.state
    if (ctx.state === 'running') alertAudioReady.value = true
  }
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

/**
 * 单个脉冲：基波 + 二次谐波，8ms 起振、指数衰减，避免起停爆音。
 * 两个振荡器汇到同一个 gain，gain 值按谐波总量归一，不同音色的响度才可比。
 */
const beep = (c, freq, dur, gainValue, offset) => {
  const t0 = c.currentTime + offset
  const osc = c.createOscillator()
  const harm = c.createOscillator()
  const gain = c.createGain()
  const harmGain = c.createGain()
  osc.type = 'sine'
  harm.type = 'sine'
  osc.frequency.setValueAtTime(freq, t0)
  harm.frequency.setValueAtTime(freq * 2, t0)
  harmGain.gain.setValueAtTime(HARMONIC, t0)
  gain.gain.setValueAtTime(0.0001, t0)
  gain.gain.exponentialRampToValueAtTime(gainValue / (1 + HARMONIC), t0 + 0.008)
  gain.gain.exponentialRampToValueAtTime(0.0001, t0 + dur)
  osc.connect(gain)
  harm.connect(harmGain)
  harmGain.connect(gain)
  gain.connect(c.destination)
  osc.start(t0)
  harm.start(t0)
  osc.stop(t0 + dur + 0.02)
  harm.stop(t0 + dur + 0.02)
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
  let offset = 0
  for (const [freq, dur] of tone.pulses) {
    beep(c, freq, dur, tone.gain, offset)
    offset += dur + tone.gap
  }
  lastAlertAt.value = Date.now()
  return true
}

/**
 * 持续告警的重响间隔：严重 15 秒（护士没到场就继续叫，与真实设备一致），警告 60 秒。
 * 设备大屏按告警键各自的级别取这个表，别再用统一常量。
 */
export const ALARM_REPEAT_MS = {critical: 15_000, warning: 60_000}

/** 消息 severity → 音色级别（后端只发 urgent/warning/info 三档） */
export const alertLevelOfSeverity = (severity) =>
    severity === 'urgent' ? 'critical' : severity === 'warning' ? 'warning' : 'info'

/**
 * 主动解锁（必须在点击回调里调用）。
 * 用于页面上的「点此开启声音 / 测试声音」按钮：用户点一下，既解锁又立刻出一声，能直接判断有没有声音。
 * @returns {boolean} 此刻是否已可发声（resume 是异步的，首次调用可能返回 false，紧接着 playAlert 会再试一次）
 */
export function resumeAlertAudio() {
  const c = ensureCtx()
  if (!c) return false
  if (c.state !== 'running') c.resume().catch(() => {})
  if (c.state === 'running') {
    alertAudioReady.value = true
    removeGestureListeners()
    return true
  }
  return false
}
