/**
 * 「当前角色 → 患者工作台」推导
 *
 * 顶部搜索选中患者后该去哪，取决于**当前角色有没有患者工作台**：
 *   有  → 带患者跳过去，让工作台接着干活（医生站接诊、收费处结算…）
 *   没有 → 弹详情框查档（管理员、病案编码员、统计员这类岗位的工作对象不是患者）
 *
 * 这里刻意**不维护「角色码 → 工作台路径」的硬编码映射**：角色会增删、路径会调整，
 * 映射表必然过期（项目里已有 Dashboard/Login/Report 三处硬编码角色码的教训）。
 *
 * 唯一口径：从当前角色**可见的菜单**（/system/menu/userMenus，已按当前角色过滤）里，
 * 取第一个标记了 `meta.patientWorkspace` 的页面。于是「这个角色能不能切患者」完全由
 * 「系统管理 → 角色管理 → 菜单权限」决定 —— 给角色配上医生工作站，它就会被带过去。
 */

/** 扁平化菜单树，按菜单顺序取出所有页面（menu_type=2）的 path */
function collectMenuPaths(menuTree) {
    const paths = []
    const walk = (nodes) => {
        for (const node of nodes || []) {
            if (node.menuType === 2 && node.path) paths.push(node.path)
            walk(node.children)
        }
    }
    walk(menuTree)
    return paths
}

/**
 * 不参与患者工作区分流的角色：系统管理员。
 *
 * 超级管理员按惯例被授予**全部菜单**（含医生工作站），但它的工作对象是配置不是患者：
 * 把它带进医生站，只会得到一句「XX 不在您的今日候诊队列中」——这句话对管理员是误导
 * （管理员根本没有候诊队列）。所以管理角色搜患者一律走查档。
 *
 * 这是角色语义上的**特例**，不是通用规则。若将来出现「管理员也要能接管诊疗」的场景，
 * 改这一处即可。
 */
const ADMIN_ROLE_CODES = new Set(['10012'])

/**
 * 解析当前角色应当被带往的患者工作台路径
 *
 * @param {import('vue-router').Router} router 路由实例（用 getRoutes() 取 meta）
 * @param {Array} menuTree 当前角色菜单树（loadMenuTree() 的结果，已按 sort_order 排序）
 * @param {string} currentRole 当前角色码（Header 的 userInfo.currentRole）
 * @returns {string} 工作台路径；当前角色没有工作台页面时返回空串
 */
export function resolveWorkspacePath(router, menuTree, currentRole) {
    if (!Array.isArray(menuTree) || !menuTree.length || !router) return ''
    if (ADMIN_ROLE_CODES.has(String(currentRole || ''))) return ''

    const workspacePaths = new Set(
        router.getRoutes()
            .filter(route => route.meta?.patientWorkspace)
            .map(route => route.path)
    )
    if (!workspacePaths.size) return ''

    // collectMenuPaths 保持菜单顺序（= sort_order），第一个命中的即该角色优先级最高的工作台
    return collectMenuPaths(menuTree).find(path => workspacePaths.has(path)) || ''
}

/**
 * 解析「切到当前角色后该落在哪」
 *
 * 切角色 / 切科室后的落点必须是**新角色自己的地方**，不能留在旧岗位上那一页
 * （见 `router/guard.js` 文件头对这两个缺陷的描述）。
 *
 * 默认（`landingScope=0`）落到该角色的**患者工作台**（医生→医生站、药剂师→药房发药这类
 * 能立刻干活的页面）；没有工作台的角色（管理员、统计员等）回首页 `/`。
 *
 * `landingScope` 是**角色级配置**（sys_workbench_role.landing_scope，来自
 * `/workbench/config` → lib/workbench-config.js），让「登录后先看到工作台还是直接进工作站」
 * 由「系统管理 → 工作台配置」决定，而不是在这里写角色码 —— 这是决策 2026-09-25 定的，
 * 同文件头口径：角色会增删，硬编码映射必然过期。
 *
 * 导出在这里而不是 `router/guard.js`，是为了让**守卫与切换动作共用同一套推导** ——
 * 若两边各写一份，会出现「切换时跳到 A、守卫认为该在 B」的来回弹。
 *
 * @param {import('vue-router').Router} router
 * @param {Array} menuTree 当前角色菜单树
 * @param {string} currentRole 当前角色码
 * @param {number} [landingScope] 0-默认（有工作站进工作站）1-一律工作台 2-一律工作站；
 *        取不到（首拉失败/老数据）按 0 走 = 现状行为
 * @returns {string} 落地路径，至少为 '/'
 */
export function resolveLandingPath(router, menuTree, currentRole, landingScope = 0) {
    const workspace = resolveWorkspacePath(router, menuTree, currentRole)
    if (Number(landingScope) === 1) return '/'
    return workspace || '/'
}
