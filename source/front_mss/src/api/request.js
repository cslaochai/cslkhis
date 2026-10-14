// 经办平台 axios 封装：统一走 vite 代理 /mss -> http://localhost:8090/mss
// 约定后端返回 { code, message, data }，拦截器直接返回 body，页面用 res.data 取业务数据
// 登录态：请求头带 Bearer token；401（HTTP 或 body.code）统一清 token 跳登录页
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/mss',
  timeout: 20000,
})

// 401 整页跳转登录的单次闸门（并发多个 401 只跳一次）
let redirectingToLogin = false

function redirectToLogin() {
  if (redirectingToLogin) return
  redirectingToLogin = true
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  window.location.href = '/login'
}

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

request.interceptors.response.use(
  (resp) => {
    const body = resp.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 401) {
        const isLoginRequest = (resp.config?.url || '').includes('/auth/login')
        if (!isLoginRequest) redirectToLogin()
        return Promise.reject(new Error(body.message || '未登录'))
      }
      if (body.code !== 200) {
        const isLoginRequest = (resp.config?.url || '').includes('/auth/login')
        if (!resp.config?.silent && !isLoginRequest) {
          ElMessage.error(body.message || '请求失败')
        }
        return Promise.reject(new Error(body.message || '请求失败'))
      }
      return body
    }
    return body
  },
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      const isLoginRequest = (error.config?.url || '').includes('/auth/login')
      if (!isLoginRequest) redirectToLogin()
      return Promise.reject(error)
    }
    const msg = error?.response?.data?.message || error.message || '网络异常，请检查后端服务'
    if (!error.config?.silent) {
      ElMessage.error(msg)
    }
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
