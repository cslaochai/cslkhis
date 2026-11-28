/**
 * 科室下拉的统一取数与缓存 —— 页面不要再各写一份 loading / try-catch。
 *
 * <p><b>为什么要缓存</b>：一个页面里「搜索筛选」和「新增表单」常常各要一次科室下拉，
 * 而 AppointmentsView 首屏就是因为同一个接口被拆成多处调用才打出 7 个请求。
 * 这里按 {@code scope + deptType} 缓存 Promise（不是结果），并发调用只会发一次请求。
 *
 * <p><b>scope 默认按当前人过滤</b>，与后端 `/system/department/selectTree` 一致：
 * <ul>
 *   <li>`CURRENT`（默认）→ 只返回当前用户被授权的科室；不限权用户（管理员/院领导）天然拿到全部</li>
 *   <li>`ALL` → 全部科室，仅用于「必须看到全量才能维护」的场景（字典维护 / 排班模板）</li>
 * </ul>
 * 默认值选 CURRENT 而不是 ALL：漏传参数时要"看不到"，不要"看到太多"。
 */
import { getDepartmentSelectTree } from '@/api/system'

/** 树形下拉缓存：key = `${scope}|${deptType}|${excludeDeptType}|${nodeType}|${openOnly}|${mode}` → Promise<TreeNode[]> */
const treeCache = new Map()

/**
 * 取科室树形下拉（带缓存）。节点可选性由服务端按 mode 标注 disabled，前端不自行判定。
 *
 * @param {string} [params.scope]           CURRENT（默认，按当前人授权）/ ALL（全量）
 * @param {string} [params.deptType]        服务范围逗号串：1 门诊 2 医技 3 药房 4 住院 5 其他
 * @param {string} [params.excludeDeptType] 排除类型逗号串，命中即剔除（如普通排班排除急诊 6）
 * @param {string} [params.nodeType]        节点类型逗号串：1 片区 2 临床 3 医技 4 药剂 5 行政 6 辅助 7 其他
 * @param {boolean}[params.openOnly]        true 只出对外开放挂号科室
 * @param {string} [params.mode]            LEAF-仅末级业务科室 / TREE-父子均可（默认）/ ORG-组织归属维护
 */
export function loadDeptTreeOptions({ scope = 'CURRENT', deptType = null, excludeDeptType = null, nodeType = null, openOnly = null, mode = 'TREE' } = {}) {
  const key = `${scope}|${deptType ?? ''}|${excludeDeptType ?? ''}|${nodeType ?? ''}|${openOnly ?? ''}|${mode}`
  if (!treeCache.has(key)) {
    const p = getDepartmentSelectTree({
      scope,
      mode,
      ...(deptType != null ? { deptType } : {}),
      ...(excludeDeptType != null ? { excludeDeptType } : {}),
      ...(nodeType != null ? { nodeType } : {}),
      ...(openOnly != null ? { openOnly } : {}),
    })
      .then((res) => res?.data || [])
      .catch((err) => {
        treeCache.delete(key)
        throw err
      })
    treeCache.set(key, p)
  }
  return treeCache.get(key)
}

/** 把树形下拉结果拍平成 [{id, deptName, deptType, nodeType, parentId, parentDeptName}]，供默认值计算与行内回显查找。
 *  服务端标注 disabled 的分组/片区/行政/停用节点不进扁平候选（旧 el-select 不消费 disabled 字段，需在拍平时剔除），
 *  但其下可选后代仍递归保留。 */
export function flattenDeptTree(tree) {
  const out = []
  const walk = (list, parentName) => {
    for (const d of list || []) {
      if (!d.disabled) {
        out.push({ id: d.id, deptName: d.deptName, fullPath: d.fullPath || d.deptName, deptType: d.deptType, nodeType: d.nodeType, parentId: d.parentId, parentDeptName: parentName || '' })
      }
      if (d.children && d.children.length) walk(d.children, d.deptName)
    }
  }
  walk(tree, '')
  return out
}

/** 显式刷新（科室管理页增删改后使用） */
export function invalidateDeptOptions() {
  treeCache.clear()
}
