/**
 * 药品调拨（sql/154 ②级：药库 ↔ 药房）的状态机与语义判定单点定义
 *
 * 状态：1待发出 →（确认发出）2待接收 →（确认接收）3已完成；1 可作废成 4。
 * ⚠ 单据文案（transferTypeText / fromRoomText / toRoomText / statusText）由后端算好返回，
 *   这里只管**行为判定与标签色**，不抄第二份翻译（抄了就会和后端漂移）。
 *
 * 「在途不许作废」是这张单的硬约束：药已经离开发出库位、又还没落到接收库位，
 * 此时作废等于把两行流水凑不平（只有 7 没有 8），账实当场对不上 —— 只能再开一张反向调拨搬回去。
 */
export const TRANSFER_STATUS = {
  PENDING_OUT: 1,
  PENDING_IN: 2,
  DONE: 3,
  CANCELLED: 4,
}

export const TRANSFER_TYPE = {
  /** 1-药库下拨药房 */
  TO_PHARMACY: 1,
  /** 2-药房退回药库 */
  BACK_TO_STORE: 2,
}

export const STOCK_ROOM = {
  /** 1-药库 */
  STORE: 1,
  /** 2-药房 */
  PHARMACY: 2,
}

/**
 * 方向 → 发出库位（接收库位取另一端）
 * <p>服务端按同一套规则算 fromRoom/toRoom，前端只用它筛「可选批次」——
 * 药房的批次不会出现在下拨单的候选里，选了也会被后端按库位校验当场拒掉。
 */
export function fromRoomOf(transferType) {
  return Number(transferType) === TRANSFER_TYPE.BACK_TO_STORE ? STOCK_ROOM.PHARMACY : STOCK_ROOM.STORE
}

/** 可改明细 / 作废 / 删除：只有待发出（还没动库存） */
export function canEditTransfer(transfer) {
  return !!transfer && Number(transfer.status) === TRANSFER_STATUS.PENDING_OUT
}

/** 可确认发出：待发出 */
export function canTransferOut(transfer) {
  return canEditTransfer(transfer)
}

/** 可确认接收：待接收（药已发出，必须有人签收，否则库存挂在两行流水中间） */
export function canTransferIn(transfer) {
  return !!transfer && Number(transfer.status) === TRANSFER_STATUS.PENDING_IN
}

/** 可作废：待发出（在途一律拒绝） */
export function canCancelTransfer(transfer) {
  return canEditTransfer(transfer)
}

/** 可删除：待发出 / 已作废（动过库存的单必须留档） */
export function canDeleteTransfer(transfer) {
  const n = Number(transfer?.status)
  return n === TRANSFER_STATUS.PENDING_OUT || n === TRANSFER_STATUS.CANCELLED
}

/** 状态 → 标签色 */
export function transferStatusTagType(status) {
  const n = Number(status)
  if (n === TRANSFER_STATUS.DONE) return 'success'
  if (n === TRANSFER_STATUS.PENDING_IN) return 'warning'
  if (n === TRANSFER_STATUS.PENDING_OUT) return 'primary'
  return 'info'
}

/**
 * 本单流水是否已经凑平（7 与 8 各一条）
 * <p>详情抽屉用它把「账实相符」这句话变成一个能一眼看到的事实，而不是让人去数行数。
 */
export function isTransferBalanced(logs) {
  const list = logs || []
  return (
    list.some((l) => Number(l.changeType) === 7) &&
    list.some((l) => Number(l.changeType) === 8)
  )
}
