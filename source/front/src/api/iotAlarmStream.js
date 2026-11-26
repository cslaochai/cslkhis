/**
 * 科室设备告警 SSE 实时流（监护大屏）。
 *
 * <p>协议与服务端 IotEventController#alarmSse 对应：GET /iot/event/alarm/sse?deptId=，
 * text/event-stream，帧格式 `data:{json}`，`:` 开头为注释帧（connected/ping）直接跳过。
 *
 * <p>与 api/messageStream.js 同构（同一个原因：EventSource 带不了 Authorization 头，
 * request.js 又强耦合 {code,message,data} 壳，SSE 只能原生 fetch 逐帧解析）。
 * 不合并成公共模块是刻意的：两条流的业务语义完全不同（一个是自己的收件箱，一个是科室广播），
 * 断线后的降级方式也不同（消息退回未读数轮询，监护退回 10 秒床位轮询），混在一起改一处会误伤另一处。
 *
 * <p>断线：任何异常都调 onFallback，由页面决定降级；本模块不做自动重连，
 * 页面刷新/重新登录自然恢复，避免断线风暴。
 */

const API_BASE = import.meta.env?.VITE_API_BASE || '/api'

/**
 * 打开科室告警流。
 * @param {Object} handlers
 * @param {(data: {type:string,eventId:string|number,deviceNo:string,deviceType:string,eventCode:string,
 *   eventCodeText:string,level:number,levelText:string,detail:string,wardId:number,bedNo:string,ts:string}) => void} handlers.onAlarm
 * @param {(reason: string) => void} [handlers.onFallback] 断线回调（含 reason），调用后流作废
 * @param {number|string} [handlers.deptId] 订阅科室；不传由后端取当前岗位科室
 * @returns {() => void} close 函数（页面卸载/切换科室时调用；已断线时为 no-op）
 */
export function openIotAlarmStream({deptId, onAlarm, onFallback}) {
  const token = localStorage.getItem('token')
  if (!token) {
    onFallback?.('no-token')
    return () => {}
  }

  const controller = new AbortController()
  let closed = false

  ;(async () => {
    try {
      const query = deptId == null || deptId === '' ? '' : `?deptId=${encodeURIComponent(deptId)}`
      const res = await fetch(`${API_BASE}/iot/event/alarm/sse${query}`, {
        method: 'GET',
        headers: {Authorization: `Bearer ${token}`, Accept: 'text/event-stream'},
        signal: controller.signal,
      })
      if (!res.ok || !res.body) {
        onFallback?.(`http-${res.status}`)
        return
      }

      const reader = res.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      for (;;) {
        const {done, value} = await reader.read()
        if (done) break
        buffer += decoder.decode(value, {stream: true})

        // 事件以空行分隔；逐块解析，残帧留在 buffer
        let sep
        while ((sep = buffer.indexOf('\n\n')) >= 0) {
          const rawEvent = buffer.slice(0, sep)
          buffer = buffer.slice(sep + 2)
          for (const line of rawEvent.split('\n')) {
            if (line.startsWith(':') || !line.startsWith('data:')) continue
            const payload = line.slice(5).trim()
            if (!payload) continue
            try {
              onAlarm?.(JSON.parse(payload))
            } catch {
              // 单帧坏 JSON 只丢这一帧，不断流
            }
          }
        }
      }
      if (!closed) onFallback?.('stream-closed')
    } catch (err) {
      if (!closed && err?.name !== 'AbortError') {
        onFallback?.(err?.message || 'fetch-error')
      }
    }
  })()

  return () => {
    closed = true
    controller.abort()
  }
}
