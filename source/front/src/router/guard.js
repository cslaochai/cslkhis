/**
 * 全局路由守卫 —— 「当前角色能进哪些页面」的唯一执行官
 *
 * ## 为什么必须有这个
 *
 * 菜单（Sidebar）只决定**看得见什么**，不决定**进得去什么**。在它之前：
 *   ① 切角色只换 token + `window.location.reload()`，重载回到**同一个 URL** ——
 *      新角色没有权限的旧页面被原样恢复。现象：药剂师登在「角色管理」页面上刷数据；
 *   ② 全仓没有任何 `router.beforeEach` —— 手工敲 URL、翻浏览器历史、点旧收藏
 *      都能直达任意页面，哪怕这个角色根本没配那条菜单。
 *
 * 前端守卫是**可用性收口**，不是安全边界（真正的边界是服务端 `@PreAuthorize`）。
 * 它要做的是：不让用户停留在「本角色没有的页面」上，而不是拦住恶意请求。
 *
 * ## 判定口径
 *
 * 唯一来源是当前角色**可见的菜单**（`loadMenuTree()` → `/system/menu/userMenus`，
 * 后端已按 currentRole 过滤）。**角色配菜单即配权限** —— 不维护「角色码 → 可访问路径」
 * 的硬编码映射（角色会增删、映射必然过期；Dashboard/Login/Report 三处硬编码已经吃过亏）。
 * 与 `lib/permission.js` / `lib/role-workspace.js` 同一思路。
 *
 * ## 为什么不把「无菜单的页面」一并拦掉
 *
 * `sys_menu` 里挂的是**业务菜单**，而路由表里还有一类页面**本来就不该有菜单**：
 * 已下线的假壳页（`meta.title` 兜底面包屑的那些）、兼容旧链接的 `records` / `hand-hygiene`、
 * 以及首页 `/`。它们的共同点是「不靠菜单分发」。硬拦它们等于把
 * 「这个页面没上菜单」误判成「这个角色没权限」，会误伤。
 *
 * 所以判定分三层，**只拦最明确的一层**：
 *  1. 无 token → 放行（登录页、401 由 request.js 处理，守卫不掺和认证）
 *  2. 菜单还没拉到 / 拉失败 → 放行（fail-open，理由见 `lib/permission.js` 文件头）
 *  3. 目标路径在菜单树里 → 放行；不在 → 跳到当前角色的工作台
 *
 * 第 3 层刻意用「在不在菜单里」而不是「有没有 `meta.permission`」：菜单树是按角色
 * 过滤过的，所以「不在树里」本身就等于「这个角色没配这条菜单」，不需要第二套码。
 */
import {loadMenuTree} from '@/lib/menu-cache'
import {loadWorkbenchConfig} from '@/lib/workbench-config'
import {resolveLandingPath} from '@/lib/role-workspace'

/** 不需要登录、也不需要判权的路径前缀 */
const PUBLIC_PATHS = ['/login']

/** 扁平化：取菜单树里所有页面级（menuType=2）的 path */
function collectMenuPaths(menuTree) {
    const paths = new Set()
    const walk = (nodes) => {
        for (const node of nodes || []) {
            if (node.menuType === 2 && node.path) paths.add(node.path)
            walk(node.children)
        }
    }
    walk(menuTree)
    return paths
}

/**
 * 路径归属判定：整段相等，或以 `/` 为边界后接更长路径
 *
 * 直接用 `startsWith` 会让 `/pharmacy-window` 误命中 `/pharmacy`
 * （Sidebar 的 `isPathMatch` 同一口径，那边也有这个坑的注释）。
 */
function pathMatches(target, candidate) {
    if (candidate === '/') return target === '/'
    return target === candidate || target.startsWith(candidate + '/')
}

/**
 * 安装守卫
 *
 * @param {import('vue-router').Router} router 路由实例（需已注册全部 routes）
 * @param {(msg: string) => void} [notify] 无权时的轻提示回调（由 main.js 注入 ElMessage，
 *        避免 lib 层直接依赖 UI 库）
 */
export function installRouteGuard(router, notify) {
    router.beforeEach(async (to) => {
        // ① 登录页永远放行
        if (PUBLIC_PATHS.some(p => pathMatches(to.path, p))) return true

        // ② 没有 token：交给 request.js 的 401 处理，守卫不掺和认证
        //   （已有行为：401 → 清 token → 跳 /login。这里再拦一次会变成两套跳转逻辑）
        const token = localStorage.getItem('token')
        if (!token) return true

        // ③ 拿当前角色可见菜单。拉不到就**放行**：
        //   菜单接口挂了是基础设施问题，此时把医生挡在医生站外面 = 拿一次网络抖动误伤业务。
        //   服务端该拦的照样拦得住（403 有明确文案）。
        let menuTree
        try {
            menuTree = await loadMenuTree()
        } catch (e) {
            console.warn('[route-guard] 菜单不可用，本次不做入口收敛', e)
            return true
        }

        // 空菜单树 = 还没配好 / 拿到空结果，同样不收敛
        if (!Array.isArray(menuTree) || !menuTree.length) return true

        const menuPaths = collectMenuPaths(menuTree)
        // 命中菜单 → 放行
        for (const p of menuPaths) {
            if (pathMatches(to.path, p)) return true
        }

        // ④ 不在菜单里 —— 分两种情况，不能一律当成「无权」：
        //    a) 首页 `/`：所有角色都有，且它不一定出现在每个角色的菜单里（如纯业务角色），
        //       拦掉会导致「落地页自己把自己拦出去」，死循环。必须放行。
        //    b) 路由表里**本来就没挂菜单**的页面（假壳页 / 兼容旧链接 / 详情页）：
        //       它们的存在意义就是「不靠菜单分发」，拦掉是误伤。
        //       判据：该路由的 meta 里没有菜单对应的 permission 锚点 —— 这里用
        //       「菜单树是否包含该 path 的**任一祖先业务目录**」无法可靠表达，
        //       所以改用显式白名单：路由 meta.standalone = true 的页面放行。
        if (to.path === '/') return true
        if (to.meta?.standalone) return true

        // ⑤ 真·无权：跳到当前角色该待的地方
        // 落点策略 landingScope 来自 /workbench/config（角色级配置，见 lib/workbench-config）；
        // 拉不到就按 0（= 现状行为：有患者工作站就进工作站），绝不因为配置接口抖动把人踢错地方。
        let landingScope = 0
        try {
            landingScope = (await loadWorkbenchConfig()).landingScope
        } catch (e) {
            console.warn('[route-guard] 工作台配置不可用，落点按默认处理', e)
        }
        const landing = resolveLandingPath(router, menuTree, localStorage.getItem('currentRole') || '', landingScope)
        // 落地页就是当前目标 → 放行，否则会自己拦自己形成死循环
        if (pathMatches(to.path, landing) || landing === to.path) return true

        notify?.(`「${to.meta?.title || to.path}」不属于当前角色，已返回工作台`)
        return {path: landing, replace: true}
    })
}
