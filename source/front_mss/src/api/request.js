// 经办平台 axios 封装：统一走 vite 代理 /mss -> http://localhost:8090/mss
// 约定后端返回 { code, message, data }，拦截器直接返回 body，页面用 res.data 取业务数据
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/mss',
  timeout: 20000,
})

request.interceptors.response.use(
  (resp) => {
    const body = resp.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code !== 200) {
        ElMessage.error(body.message || '请求失败')
      }
      return body
    }
    return body
  },
  (error) => {
    const msg = error?.response?.data?.message || error.message || '网络异常，请检查后端服务'
    ElMessage.error(msg)
    return Promise.reject(error)
  },
)

/** 去掉 GET 查询里的空值（null / undefined / '' 不下发），避免空串绑定到 Boolean 等字段 */
export function cleanParams(params = {}) {
  const out = {}
  Object.entries(params).forEach(([k, v]) => {
    if (v !== null && v !== undefined && v !== '') out[k] = v
  })
  return out
}

export default request
