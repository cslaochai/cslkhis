import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 401 整页跳转去登录页的单次闸门（见响应拦截器内注释）
let redirectingToLogin = false

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      console.error('请求失败:', res.message)
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    if (error.response) {
      const { status, data } = error.response
      // 401 = 身份失效：清 token 回登录页。
      // 403 = 身份有效但**当前角色碰不得这个接口**：只提示，绝不清 token —— 否则一个越权
      // 请求（如侧边栏菜单接口被鉴权注解误伤）会把人整个踢出登录，现象就是
      // 「点切换角色直接跳回登录页」，而 token 明明是好的。
      if (status === 401) {
        localStorage.removeItem('token')
        // 并发请求会同时收到多个 401，只处理第一次（页面马上整体销毁，标志无需复位）
        if (!redirectingToLogin) {
          redirectingToLogin = true
          // 先发起整页跳转，再补一次会话缓存清理：
          // 跳转真正落地前的空窗里，清理推高菜单代次会让仍挂载的组件去重拉接口，
          // 而 token 已删 → 又是一轮 401（登出场景实测过刷屏）。先导航可把影响压到零，
          // 清缓存只是兜底（硬刷新本就重建模块态，防的是将来改成 SPA 内跳转时漏清）。
          // 用动态 import 是因为静态引会成环：session-cache → menu-cache → api/system → request.js
          window.location.href = '/login'
          import('@/lib/session-cache')
            .then(({ clearSessionCaches }) => { clearSessionCaches() })
        }
        return Promise.reject(error)
      }
      if (status === 403) {
        ElMessage.error(data?.message || '没有权限访问')
        return Promise.reject(error)
      }
    }
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

export default request
