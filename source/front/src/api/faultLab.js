/**
 * 故障演练进程 API（his-web-fault，默认 127.0.0.1:18090）
 *
 * 与业务后端不同：演练进程不返回 {code,message,data} 信封，也不带鉴权，
 * 因此这里用原生 fetch，不走 src/api/request.js 的 axios 实例。
 *
 * 地址可用 VITE_FAULT_LAB_BASE 覆盖（默认本机 18090）；跨域由演练进程的
 * FaultLabCorsConfig 放行 /fault/**。
 */
const BASE = (import.meta.env.VITE_FAULT_LAB_BASE || 'http://127.0.0.1:18090').replace(/\/+$/, '')

async function request(path, {method = 'GET', params} = {}) {
  const url = new URL(BASE + path)
  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        url.searchParams.set(key, String(value))
      }
    })
  }
  let res
  try {
    res = await fetch(url, {method, headers: {Accept: 'application/json'}})
  } catch (e) {
    throw new Error(`连不上演练进程 ${BASE}，请先运行 his-web-fault 的 run-web-fault.cmd`)
  }
  const text = await res.text()
  if (!res.ok) {
    throw new Error(`演练进程返回 ${res.status}：${text.slice(0, 300)}`)
  }
  try {
    return JSON.parse(text)
  } catch (e) {
    throw new Error(`演练进程返回非 JSON：${text.slice(0, 200)}`)
  }
}

/** 场景目录（code/name/symptom/trigger/observe） */
export function getFaultCatalog() {
  return request('/fault/catalog')
}

/** 演练进程实时水位 */
export function getFaultStatus() {
  return request('/fault/status')
}

/** 通用触发：path 为 /fault/... 相对路径，params 走查询串（后端 @RequestParam 接收） */
export function triggerFault(path, params) {
  return request(path, {method: 'POST', params})
}

