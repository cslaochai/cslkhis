import axios from 'axios'
import {ElMessage} from 'element-plus'

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
            const {status, data} = error.response
            if (status === 401) {
                const msg = data?.message || '未登录或登录已过期'
                const isLoginRequest = (error.config?.url || '').includes('/auth/login')
                if (!isLoginRequest) {
                    localStorage.removeItem('token')
                    if (!redirectingToLogin) {
                        redirectingToLogin = true
                        window.location.href = '/login'
                        import('@/lib/session-cache')
                            .then(({clearSessionCaches}) => {
                                clearSessionCaches()
                            })
                    }
                }
                return Promise.reject(new Error(msg))
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
