/**
 * 待办深链（统一协议）：
 *   /xxx?from=msg&bizType=emg-wait&bizId=2115398977175195649&bizNo=JZ2026102800087
 *
 * 目标页接参后必须「定位到那条单据」，不是只打开模块首页 —— 只跳路径等于让用户自己翻列表。
 * 有 bizNo 优先用 bizNo（用户界面上认得的单号），没有才退回 bizId（雪花 ID，能按 id 匹配的行才用）。
 */

/** 解析深链参数；非待办跳转（普通进页面）返回 null，页面按原逻辑加载 */
export function msgDeeplinkOf(route) {
    const q = route?.query || {}
    if (q.from !== 'msg') return null
    const bizNo = q.bizNo == null ? '' : String(q.bizNo)
    const bizId = q.bizId == null ? '' : String(q.bizId)
    return {
        bizType: String(q.bizType || ''),
        bizId,
        bizNo,
        /** 拿来预填搜索框的值：单号优先 */
        keyword: bizNo || bizId,
    }
}

/** 在列表里按单号/主键匹配行；comparator 返回该行的候选值数组（单号、id 都可能命中） */
export function pickRowByDeeplink(rows, link, valuesOf) {
    if (!link || !Array.isArray(rows)) return null
    const targets = [link.bizNo, link.bizId].filter(Boolean)
    if (targets.length === 0) return null
    return rows.find((row) => {
        const values = (valuesOf?.(row) || []).filter((v) => v != null).map(String)
        return targets.some((t) => values.includes(t))
    }) || null
}
