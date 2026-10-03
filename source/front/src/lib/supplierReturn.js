/**
 * 药品供应商退货（sql/154 ③级）的状态机与语义判定单点定义
 *
 * 状态：1待退货 →（确认退货）2已退货；1 可作废成 3。
 * ⚠ statusText 由后端算好返回，这里只做行为判定与标签色。
 *
 * 与调拨的关键差异：退货没有「接收方」，货出了库就离开医院，所以只有**一步**动作。
 * 也正因为退出去就拿不回来，已退货的单据一律不许作废/删除 —— 冲销只能再入库一次（走入库单）。
 */
export const RETURN_STATUS = {
  PENDING: 1,
  DONE: 2,
  CANCELLED: 3,
}

/** 可改明细 / 作废 / 删除：只有待退货（还没动库存） */
export function canEditReturn(returnDoc) {
  return !!returnDoc && Number(returnDoc.status) === RETURN_STATUS.PENDING
}

/** 可确认退货：待退货 */
export function canConfirmReturn(returnDoc) {
  return canEditReturn(returnDoc)
}

/** 可作废：待退货 */
export function canCancelReturn(returnDoc) {
  return canEditReturn(returnDoc)
}

/** 可删除：待退货 / 已作废 */
export function canDeleteReturn(returnDoc) {
  const n = Number(returnDoc?.status)
  return n === RETURN_STATUS.PENDING || n === RETURN_STATUS.CANCELLED
}

/** 状态 → 标签色 */
export function returnStatusTagType(status) {
  const n = Number(status)
  if (n === RETURN_STATUS.DONE) return 'success'
  if (n === RETURN_STATUS.PENDING) return 'warning'
  if (n === RETURN_STATUS.CANCELLED) return 'info'
  return 'info'
}
