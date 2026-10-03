/**
 * 菜单树前端缓存
 *
 * 侧边栏（Sidebar）与面包屑（Header）共用同一份菜单树，避免各拉一次接口。
 * 菜单的唯一口径是后端 `sys_menu` 表，见 `source/back_end/sql/52-菜单表重建.sql`。
 *
 * 取数走 `/system/menu/userMenus`——**按当前登录角色过滤**（不是员工全部角色的并集），
 * 也就是「角色管理 → 菜单权限」里配了什么，这个角色就只看到什么。
 * 切换角色后必须调 `clearMenuTreeCache()` 让缓存失效，见文件尾部的代次说明。
 */
import {ref} from 'vue'
import {getMenuTree, getUserMenus} from '@/api/system'

/** 进程内缓存（刷新页面或切换角色后重新拉取） */
let cache = null
/** 进行中的请求：并发调用共享同一个 Promise */
let pending = null

/**
 * 菜单**代次** —— 缓存失效的响应式信号
 *
 * 解决的问题：Sidebar 在 `setup()` 里把菜单渲染成自己的 `navGroups`，只读一次缓存。
 * 切角色运行时清掉模块缓存后，Sidebar **不会自己重跑** —— 它是常驻布局的一部分，
 * 路由变化（router.replace）不会让它重建。于是清缓存只对"下次新拉"的调用方生效，
 * 侧边栏仍挂着旧角色那一套菜单，直到用户手工刷新页面。
 *
 * 所以这里额外暴露一个自增代次，Sidebar `watch` 它 → 缓存一失效就重跑。
 * 用自增整数而非布尔：连续切两次角色也要各触发一次。
 */
export const menuCacheEpoch = ref(0)

/**
 * 拉取菜单树（当前角色的可见菜单）
 *
 * @param {boolean} force 强制刷新，忽略缓存
 * @returns {Promise<Array>} 菜单树；失败时 reject，由调用方决定降级方式
 */
export function loadMenuTree(force = false) {
    if (!force && cache) return Promise.resolve(cache)
    if (!force && pending) return pending
    pending = getUserMenus()
        .then(res => {
            const tree = res?.data || []
            // 只在拿到数据时落缓存：空结果当成「还没配好」，下次仍可重试
            if (tree.length) cache = tree
            return tree
        })
        .finally(() => {
            pending = null
        })
    return pending
}

/**
 * 按路由地址反查菜单，用于生成面包屑
 *
 * @param {Array} tree loadMenuTree() 的结果
 * @param {string} path 当前路由地址（如 /system/user）
 * @returns {{directory: Object, menu: Object}|null} 命中的一级目录与二级菜单
 */
export function findMenuByPath(tree, path) {
    for (const directory of tree || []) {
        if (directory.menuType !== 1) continue
        for (const menu of directory.children || []) {
            if (menu.menuType === 2 && menu.path === path) {
                return {directory, menu}
            }
        }
    }
    return null
}

/**
 * 清空缓存并推进代次
 *
 * 调用场景：
 *  1. **切换角色**（Header）——「角色配菜单即配权限」，换了角色就是换了一整份菜单；
 *  2. **切换科室** —— 当前各角色菜单不按科室区分，但切科室同样要重建身份上下文，
 *     一并失效零成本，且避免将来菜单若引入科室维度时漏掉这一处；
 *  3. 菜单管理页改完菜单后想立刻生效。
 *
 * 推进 `menuCacheEpoch` 是必需的：少了它，常驻的 Sidebar 收不到任何信号会继续显示旧菜单
 * （见 `menuCacheEpoch` 的说明）。
 */
export function clearMenuTreeCache() {
    cache = null
    pending = null
    menuCacheEpoch.value += 1
}
