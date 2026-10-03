/**
 * 科室下拉的统一取数与缓存 —— 页面不要再各写一份 loading / try-catch。
 *
 * <p><b>为什么要缓存</b>：一个页面里「搜索筛选」和「新增表单」常常各要一次科室下拉，
 * 而 AppointmentsView 首屏就是因为同一个接口被拆成多处调用才打出 7 个请求。
 * 这里按 {@code scope + deptType} 缓存 Promise（不是结果），并发调用只会发一次请求。
 *
 * <p><b>scope 默认按当前人过滤</b>，与后端 `/system/department/selectList` 一致：
 * <ul>
 *   <li>`CURRENT`（默认）→ 只返回当前用户被授权的科室；不限权用户（管理员/院领导）天然拿到全部</li>
 *   <li>`ALL` → 全部科室，仅用于「必须看到全量才能维护」的场景（字典维护 / 排班模板）</li>
 * </ul>
 * 默认值选 CURRENT 而不是 ALL：漏传参数时要"看不到"，不要"看到太多"。
 */
import { getDepartmentSelectList } from '@/api/system'

/** 缓存：key = `${scope}|${deptType ?? ''}` → Promise<List> */
const cache = new Map()

/** 取科室下拉列表（带缓存）。失败不写入缓存，下次调用会重试。 */
export function loadDeptOptions({ scope = 'CURRENT', deptType = null } = {}) {
  const key = `${scope}|${deptType ?? ''}`
  if (!cache.has(key)) {
    const p = getDepartmentSelectList({
      scope,
      ...(deptType != null ? { deptType } : {}),
    })
      .then((res) => res?.data || [])
      .catch((err) => {
        // 失败要把缓存清掉，否则一次网络抖动会让整页在整个会话里都拿不到科室
        cache.delete(key)
        throw err
      })
    cache.set(key, p)
  }
  return cache.get(key)
}

/** 显式刷新（科室管理页增删改后使用） */
export function invalidateDeptOptions() {
  cache.clear()
}
