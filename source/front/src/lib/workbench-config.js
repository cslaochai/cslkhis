/**
 * 工作台配置的前端缓存
 */
import {ref} from 'vue'
import {getWorkbenchConfig} from '@/api/workbench'

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
                cache = {
                    roleCode: data.roleCode || '',
                    landingScope: data.landingScope ?? 0,
                    widgets: data.widgets || []
                }
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
