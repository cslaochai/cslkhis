import { getToken, removeToken } from './auth'

// 后端地址（与 HIS Web 端一致，统一走 /api 前缀）
// 导出给「不走 wx.request」的场景复用（如静态影像 <image src>），避免页面里再抄一份 host
export const BASE_URL = 'http://localhost:8080/api'

function buildUrl(url) {
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return BASE_URL + url
}

/**
 * 封装 wx.request，自动注入 token，统一解析后端 Result{code,message,data}
 * resolve 的是 Result 整体（页面自行判断 code === 200），便于区分业务失败与网络错误。
 */
export function request({ url, method = 'GET', data, header = {} }) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    wx.request({
      url: buildUrl(url),
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
        ...header
      },
      success(res) {
        if (res.statusCode === 401) {
          removeToken()
          wx.redirectTo({ url: '/pages/login/login' })
          reject(new Error('登录已过期'))
          return
        }
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data)
        } else {
          reject(new Error(res.data && res.data.message ? res.data.message : `请求失败: ${res.statusCode}`))
        }
      },
      fail(err) {
        reject(new Error(err.errMsg || '网络错误'))
      }
    })
  })
}

/** GET 请求，params 作为 query 参数 */
export function get(url, params, header) {
  return request({ url, method: 'GET', data: params, header })
}

/** POST 请求，data 作为请求体 */
export function post(url, data, header) {
  return request({ url, method: 'POST', data, header })
}

/** DELETE 请求 */
export function del(url, data, header) {
  return request({ url, method: 'DELETE', data, header })
}

/**
 * SSE 流式请求（用于 AI 助手 agent-stream）
 * 小程序用 wx.request + enableChunked 接收流式数据
 */
export function sseRequest({ url, data, onMessage, onDone, onError }) {
  const token = getToken()
  let buffer = ''
  let aborted = false

  const task = wx.request({
    url: buildUrl(url),
    method: 'POST',
    data,
    header: {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : '',
      'Accept': 'text/event-stream'
    },
    enableChunked: true,
    responseType: 'text',
    success() {
      if (buffer.trim()) {
        parseSSEBuffer(buffer, onMessage)
      }
      onDone && onDone()
    },
    fail(err) {
      if (!aborted) {
        onError && onError(new Error(err.errMsg || '网络错误'))
      }
    }
  })

  task.onChunkReceived(function (res) {
    if (aborted) return
    const chunk = arrayBufferToString(res.data)
    buffer += chunk
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''
    for (const line of lines) {
      if (line.startsWith('data:')) {
        const jsonStr = line.substring(5).trim()
        if (jsonStr) {
          try {
            onMessage && onMessage(JSON.parse(jsonStr))
          } catch (e) { /* 忽略解析错误 */ }
        }
      }
    }
  })

  return {
    abort() {
      aborted = true
      task.abort()
    }
  }
}

function arrayBufferToString(buffer) {
  const bytes = new Uint8Array(buffer)
  let str = ''
  for (let i = 0; i < bytes.length; i++) {
    str += String.fromCharCode(bytes[i])
  }
  try {
    return decodeURIComponent(escape(str))
  } catch (e) {
    return str
  }
}

function parseSSEBuffer(buffer, onMessage) {
  const lines = buffer.split('\n')
  for (const line of lines) {
    if (line.startsWith('data:')) {
      const jsonStr = line.substring(5).trim()
      if (jsonStr) {
        try {
          onMessage && onMessage(JSON.parse(jsonStr))
        } catch (e) { /* 忽略 */ }
      }
    }
  }
}

export default request
