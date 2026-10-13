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

// 全局错误提示的同文案节流：一次操作常并发多个请求，被同一原因挡下时拦截器会对每个
// 失败各弹一次（叠出 2~N 个相同弹框）。同文案在时间窗内只弹一次。
let lastErrorMessage = ''
let lastErrorAt = 0
const ERROR_DEDUP_MS = 2000

function notifyError(message) {
    const text = message || '操作失败，请稍后重试'
    const now = Date.now()
    if (text === lastErrorMessage && now - lastErrorAt < ERROR_DEDUP_MS) {
        return
    }
    lastErrorMessage = text
    lastErrorAt = now
    ElMessage.error(text)
}

// HTTP 状态码兜底文案。后端 GlobalExceptionHandler 已对多数状态码返回中文 message，
// 这里只在拿不到 message 时（如网关层直接返回）兜底。
function httpStatusFallback(status) {
    const map = {
        400: '请求参数不正确',
        403: '没有权限访问',
        404: '请求的资源不存在',
        405: '请求方法不支持',
        415: '请求媒体类型不支持',
        500: '系统繁忙，请稍后重试',
        502: '服务暂时不可用，请稍后重试',
        503: '服务暂时不可用，请稍后重试',
        504: '服务暂时不可用，请稍后重试'
    }
    return map[status] || `请求失败（${status}）`
}

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

// 响应拦截器：所有用户可感知的错误在此统一提示一次，业务层 catch 只做 UI 收尾（关 loading、
// 恢复按钮、清状态），不再 ElMessage.error。不想被全局提示打扰的请求（轮询、字段级错误、
// 自定义交互、登录页）在调用 config 里传 { silent: true }，错误照常 reject 给业务层自行处理。
request.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code !== 200) {
            const message = res.message || '操作失败'
            const isLoginRequest = (response.config?.url || '').includes('/auth/login')
            if (!response.config?.silent && !isLoginRequest) {
                notifyError(message)
            }
            const err = new Error(message)
            err.code = res.code
            return Promise.reject(err)
        }
        return res
    },
    error => {
        const config = error.config || {}
        const silent = !!config.silent
        const isLoginRequest = (config.url || '').includes('/auth/login')

        // 主动取消请求：正常静默，任何情况下都不提示
        if (error.code === 'ERR_CANCELED' || error.name === 'CanceledError' || axios.isCancel?.(error)) {
            return Promise.reject(error)
        }

        const {response} = error

        if (response) {
            const {status, data} = response
            const message = data?.message

            // 401：未登录/登录过期。非登录请求清 token 回登录页（单闸门，不额外弹框）；
            // 登录接口自身的 401（用户名/密码错误）不跳转、不弹，交给登录页内联错误展示。
            if (status === 401) {
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
                return Promise.reject(error)
            }

            // 其余 HTTP 错误（400/403/404/405/415/500/502/503/504…）：统一弹后端 message，
            // 缺失则按状态码兜底；403 等并发同源错误由 notifyError 单例节流。
            if (!silent) {
                notifyError(message || httpStatusFallback(status))
            }
            return Promise.reject(error)
        }

        // 请求未到达后端：超时 / 断网 / 跨域 / 后端未启动
        if (!silent) {
            if (error.code === 'ECONNABORTED') {
                notifyError('请求超时，请稍后重试')
            } else {
                notifyError('网络异常，请检查网络连接')
            }
        }
        return Promise.reject(error)
    }
)

export default request
