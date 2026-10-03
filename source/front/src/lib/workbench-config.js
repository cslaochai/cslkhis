/**
 * 工作台配置的前端缓存（与 lib/menu-cache.js 同一套时机与失效口径）
 *
 * 为什么要有这份缓存：`/workbench/config` 有两个消费方 ——
 *   ① 路由守卫与切角色动作（决定登录后落在工作台还是患者工作站）；
 *   ② 工作台首页（决定画哪几张卡）。
 * 两边各拉一次就是首屏两次相同请求，而且「守卫认为该落 A、页面按 B 渲染」会自相矛盾。
 *
 * `landingScope` 只从这个接口出，**不在 /auth/login 或 /auth/info 里带**：
 * 登录接口保持职责单一，配置类数据登录后由独立接口带回（既有约束）。
 *
 * 失效：切换角色/科室时与菜单缓存一起 `clearWorkbenchConfigCache()`，见 Header.vue。
 */
import {ref} from 'vue'
import {getWorkbenchConfig} from '@/api/workbench'

/** 进程内缓存 */
let cache = null
/** 进行中的请求：并发调用共享同一个 Promise */
let pending = null

/** 配置代次：常驻组件 watch 它，缓存一失效就重取（同 menuCacheEpoch 的理由） */
export const workbenchConfigEpoch = ref(0)

/** 空配置常量：拿不到配置时按 landingScope=0（现状行为）走，不能把守卫卡住 */
const EMPTY = {roleCode: '', landingScope: 0, widgets: []}

/**
 * @param {boolean} force 强制刷新，忽略缓存
 * @returns {Promise<{roleCode: string, landingScope: number, widgets: Array}>}
 */
export function loadWorkbenchConfig(force = false) {
    if (!force && cache) return Promise.resolve(cache)
    if (!force && pending) return pending
    pending = getWorkbenchConfig()
        .then(res => {
            const data = res?.code === 200 && res.data ? res.data : null
            // 只在拿到数据时落缓存：空结果当成「还没配好」，下次仍可重试
            if (data) {
                cache = {roleCode: data.roleCode || '', landingScope: data.landingScope ?? 0, widgets: data.widgets || []}
            }
            return cache || EMPTY
        })
        .finally(() => {
            pending = null
        })
    return pending
}

export function clearWorkbenchConfigCache() {
    cache = null
    pending = null
    workbenchConfigEpoch.value += 1
}
