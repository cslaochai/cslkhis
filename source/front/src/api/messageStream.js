/**
 * 站内信 SSE 实时流。
 *
 * <p>为什么用原生 fetch 而不是 EventSource：EventSource 无法携带 Authorization 头，
 * 而本项目 JWT 只认请求头（security 过滤器只从 header 取 token）。
 * request.js 又强耦合 {code,message,data} 响应壳，同样不能复用 ——
 * SSE 响应体是事件流，必须原生 fetch + ReadableStream 逐帧解析。
 *
 * <p>协议：GET /system/message/sse，text/event-stream。
 * 帧格式 `data:{json}`；`:` 开头为注释帧（心跳/connected），直接跳过。
 *
 * <p>断线行为：任何异常（网络断、后端重启、token 过期）都调用 onFallback，
 * 由调用方决定降级方式（Header 会退回 5 秒轮询）。本模块不做自动重连 ——
 * 轮询兜底后，刷新页面/重新登录自然恢复推送，避免断线风暴。
 */

const API_BASE = import.meta.env?.VITE_API_BASE || '/api'

/**
 * 打开消息流。
 * @param {Object} handlers
 * @param {(data: {type:string,title:string,bizType:string,severity:string,unread:number}) => void} handlers.onMessage
 * @param {() => void} [handlers.onOpen]
 * @param {(reason: string) => void} [handlers.onFallback] 断线回调（含 reason），调用后流作废
 * @returns {() => void} close 函数（组件卸载时调用；已断线时为 no-op）
 */
export function openMessageStream({ onMessage, onOpen, onFallback }) {
  const token = localStorage.getItem('token')
  if (!token) {
    onFallback?.('no-token')
    return () => {}
  }

  const controller = new AbortController()
  let closed = false

  ;(async () => {
    try {
      const res = await fetch(`${API_BASE}/system/message/sse`, {
        method: 'GET',
        headers: { Authorization: `Bearer ${token}`, Accept: 'text/event-stream' },
        signal: controller.signal,
      })
      if (!res.ok || !res.body) {
        onFallback?.(`http-${res.status}`)
        return
      }

      const reader = res.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''
      onOpen?.()

      for (;;) {
        const { done, value } = await reader.read()
        if (done) break
        buffer += decoder.decode(value, { stream: true })

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
              onMessage?.(JSON.parse(payload))
            } catch {
              // 单帧坏 JSON 只丢这一帧，不断流
            }
          }
        }
      }
      // 服务端正常关流（重启/超时）也走降级
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
