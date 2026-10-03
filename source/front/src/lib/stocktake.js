/**
 * 药房盘点（sql/127）的状态机与文案单点定义
 *
 * 状态：1盘点中 → 2待复核 → 3已过账；无差异直接 4已关单。
 * ⚠ 单据文案（statusText/diffTypeText）由后端算好返回，这里只管**行为判定与标签色**，
 *   不再抄一份翻译（抄了就会和后端漂移）。
 *
 * 过账标记 posted 无字典（0未过账/1已盘盈亏过账/2无差异免过账），按列注释单点定义。
 */
export const STOCKTAKE_STATUS = {
  COUNTING: 1,
  AUDITING: 2,
  POSTED: 3,
  CLOSED: 4,
}

/** 可录实盘数 / 改范围 / 提交 / 删除：只有盘点中 */
export function canCount(stocktake) {
  return !!stocktake && Number(stocktake.status) === STOCKTAKE_STATUS.COUNTING
}

/** 可复核（通过过账 / 退回重录）：只有待复核 */
export function canAudit(stocktake) {
  return !!stocktake && Number(stocktake.status) === STOCKTAKE_STATUS.AUDITING
}

/** 状态 → 标签色 */
export function stocktakeStatusTagType(status) {
  const n = Number(status)
  if (n === STOCKTAKE_STATUS.POSTED) return 'success'
  if (n === STOCKTAKE_STATUS.AUDITING) return 'warning'
  if (n === STOCKTAKE_STATUS.CLOSED) return 'info'
  if (n === STOCKTAKE_STATUS.COUNTING) return 'primary'
  return 'info'
}

/** 过账标记 → 文案（明细级，无字典） */
export function postedText(posted) {
  const n = Number(posted)
  if (n === 1) return '已过账'
  if (n === 2) return '无差异'
  if (n === 0) return '未过账'
  return `未知(${posted})`
}
