/**
 * 「当前角色能做什么」——前端可见性口径
 *
 * 唯一来源：当前登录角色**可见的菜单**（`/system/menu/userMenus`，后端已按 currentRole 过滤），
 * 取菜单上的 `permission` 字段。**角色配菜单即配权限** —— 不在前端维护「角色码 → 能力」的
 * 硬编码映射（角色会增删、映射必然过期；Dashboard/Login/Report 三处硬编码角色码已经吃过亏）。
 * 与 `lib/role-workspace.js` 同一思路：能力完全由「角色管理 → 菜单权限」决定。
 *
 * ⚠ 这是**可见性**，不是权限边界。真正的边界在服务端：
 * `@PreAuthorize` + 按当前角色收敛的权限集（见 JwtAuthenticationFilter#applyRolePermissions）。
 * 前端隐藏只负责"不给用户看到进不去的入口"，知道 URL 的人照样能发请求 —— 别把它当安全机制。
 *
 * ⚠ 判定不了时**放行**（fail-open）：菜单接口挂了或还没加载完 → 不隐藏任何入口。
 * 理由：菜单查询失败是基础设施问题，此时把医生的「就诊脉络」也藏掉，等于拿一次网络抖动
 * 误伤业务；而服务端该拦的照样拦得住（403 有明确文案，前端会提示）。
 * 需要"先判定再渲染、避免闪一下"的场景，先 `await loadPermissions()`。
 */
import {ref} from 'vue'
import {loadMenuTree} from '@/lib/menu-cache'

/** 当前角色的权限码集合 */
const permissionSet = ref(new Set())
/** 是否已成功判定过（false = 还不知道，一律放行） */
const resolved = ref(false)
let pending = null

/**
 * 加载当前角色权限（幂等，并发调用共享同一个请求）
 * @param {boolean} force 强制重取
 * @returns {Promise<Set<string>>}
 */
export function loadPermissions(force = false) {
    if (resolved.value && !force) return Promise.resolve(permissionSet.value)
    if (pending && !force) return pending
    pending = loadMenuTree(force)
        .then(tree => {
            const set = new Set()
            const walk = (nodes) => {
                for (const node of nodes || []) {
                    if (node?.permission) set.add(node.permission)
                    walk(node?.children)
                }
            }
            walk(tree)
            permissionSet.value = set
            resolved.value = true
            return set
        })
        .catch(e => {
            // 不置 resolved：下次进页面还能再试；本次不收敛（见文件头 fail-open 说明）
            console.warn('取当前角色权限失败，本次不做入口收敛（服务端仍会拦截）', e)
            return permissionSet.value
        })
        .finally(() => {
            pending = null
        })
    return pending
}

/**
 * 是否拥有某个权限码
 * @param {string} code 权限码（如 `patient:cdr:list`）；空值视为不需要权限
 * @returns {boolean} 方案未就绪时返回 true（放行）
 */
export function hasPermission(code) {
    if (!code) return true
    if (!resolved.value) return true
    return permissionSet.value.has(code)
}

/**
 * 是否拥有其中**任意一个**权限码（用于"多个岗位共用同一入口"的场景，如检查/检验报告详情
 * 既是医生站要看、也是技师站要看的，两边的菜单码不同）
 * @param {string[]} codes
 * @returns {boolean}
 */
export function hasAnyPermission(codes) {
    if (!codes || !codes.length) return true
    if (!resolved.value) return true
    return codes.some(code => permissionSet.value.has(code))
}

/** 清空（切换角色后调用；当前实现里切角色会整页刷新，模块状态自然重建） */
export function resetPermissions() {
    permissionSet.value = new Set()
    resolved.value = false
    pending = null
}
