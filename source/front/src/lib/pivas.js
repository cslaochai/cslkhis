/**
 * 静配中心（PIVAS）口径单点 —— 状态标签/颜色与动作显隐，页面禁写本地副本。
 * 文案走字典 his_pivas_status / his_pivas_item_status（dict-cache），
 * 这里只负责「码值 → tag 类型/语义」的渲染口径。
 */

/** 明细状态：0已拒配 1待审方 2已审方 3已排队 4已调配 5已核对发放 */
export const PIVAS_ITEM_STATUS = {
  REJECTED: 0,
  PENDING_AUDIT: 1,
  AUDITED: 2,
  QUEUED: 3,
  COMPOUNDED: 4,
  VERIFIED: 5,
}

/** 主单聚合状态（派生值）：1待审方 2待排队 3待调配 4待核对 5已完成 6全拒配 */
export const PIVAS_BATCH_STATUS = {
  PENDING_AUDIT: 1,
  PENDING_QUEUE: 2,
  PENDING_COMPOUND: 3,
  PENDING_VERIFY: 4,
  DONE: 5,
  ALL_REJECTED: 6,
}

/** 明细状态 → tag 类型（EP tag type） */
export const pivasItemStatusTag = (status) => {
  switch (Number(status)) {
    case PIVAS_ITEM_STATUS.REJECTED: return 'info'
    case PIVAS_ITEM_STATUS.PENDING_AUDIT: return 'warning'
    case PIVAS_ITEM_STATUS.AUDITED: return 'primary'
    case PIVAS_ITEM_STATUS.QUEUED: return 'primary'
    case PIVAS_ITEM_STATUS.COMPOUNDED: return 'warning'
    case PIVAS_ITEM_STATUS.VERIFIED: return 'success'
    default: return 'info'
  }
}

/** 主单状态 → tag 类型 */
export const pivasBatchStatusTag = (status) => {
  switch (Number(status)) {
    case PIVAS_BATCH_STATUS.PENDING_AUDIT: return 'warning'
    case PIVAS_BATCH_STATUS.PENDING_QUEUE: return 'primary'
    case PIVAS_BATCH_STATUS.PENDING_COMPOUND: return 'primary'
    case PIVAS_BATCH_STATUS.PENDING_VERIFY: return 'warning'
    case PIVAS_BATCH_STATUS.DONE: return 'success'
    case PIVAS_BATCH_STATUS.ALL_REJECTED: return 'info'
    default: return 'info'
  }
}

/** 仅待审方可审方 */
export const canAudit = (status) => Number(status) === PIVAS_ITEM_STATUS.PENDING_AUDIT

/** 仅已排队可调配 */
export const canCompound = (status) => Number(status) === PIVAS_ITEM_STATUS.QUEUED

/** 仅已调配可核对发放 */
export const canVerify = (status) => Number(status) === PIVAS_ITEM_STATUS.COMPOUNDED

/** 主单处于「待排队」（全部有效明细已审方）才允许打标签排队；服务端最终收口 */
export const canLabel = (batchStatus) => Number(batchStatus) === PIVAS_BATCH_STATUS.PENDING_QUEUE
