/**
 * 「身份级」进程内缓存的统一清理
 *
 * 菜单树（menu-cache）、按钮权限集（perm）、角色权限码（permission）、工作台配置（workbench-config）
 * 都是模块级 `let cache`，只有整页刷新才会自然重建。而登录/退出走的是 SPA 路由跳转
 * （router.push，不重载页面）—— 不清的结果就是：A 退出 → B 登录，侧边栏/路由守卫/卡片
 * 仍按 A 的角色渲染（menu-cache 命中旧树，连 /system/menu/userMenus 都不会再发）。
 *
 * 调用时机 —— 所有身份变化场景统一走这一处：
 *  1. 登录成功拿到新 token 之后、跳转首页之前（LoginView）——
 *     这是兜底主闸：哪怕用户没退出就直达 /login 换了账号，新身份也必然从登录函数经过；
 *  2. 切岗位（Header 的 landOnRoleWorkspace）——换角色 = 换一整份菜单和权限；
 *  3. 退出登录（Header）——**必须在 router.push('/login') 之后**：提前清会推高菜单代次，
 *     仍挂载的组件在 token 已删后重拉接口 → 401 刷屏（实测过）；
 *  4. 401 拦截（api/request.js）——同样是先发起整页跳转再清，硬刷新本就重建模块态，
 *     这里清只是兜底（防将来改成 SPA 内跳转时漏清）。
 */
import {clearMenuTreeCache} from '@/lib/menu-cache'
import {clearWorkbenchConfigCache} from '@/lib/workbench-config'
import {resetPermissions} from '@/lib/permission'
import {resetPerms} from '@/lib/perm'

export function clearSessionCaches() {
    clearMenuTreeCache()
    clearWorkbenchConfigCache()
    resetPermissions()
    resetPerms()
}
