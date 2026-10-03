/**
 * 住院摆药（G13）口径单点 —— 状态标签/颜色映射，页面禁写本地副本。
 * 文案走字典 his_ward_dispense_status / his_ward_dispense_item_status（dict-cache），
 * 这里只负责「码值 → tag 类型/语义」的渲染口径。
 */

export const DISPENSE_STATUS = {
  PENDING: 1,
  DISPENSED: 2,
  CHECKED: 3,
  RETURNED: 4,
}

/** 主单聚合状态（派生值）：1待配药 2配药中 3已配药 4已核对 5已退药 */
export const DISPENSE_ORDER_STATUS = {
  ALL_PENDING: 1,
  PARTIAL: 2,
  ALL_DISPENSED: 3,
  ALL_CHECKED: 4,
  ALL_RETURNED: 5,
}

/** 明细状态 → tag 类型（EP tag type） */
export const itemStatusTag = (status) => {
  switch (Number(status)) {
    case DISPENSE_STATUS.PENDING: return 'warning'
    case DISPENSE_STATUS.DISPENSED: return 'primary'
    case DISPENSE_STATUS.CHECKED: return 'success'
    case DISPENSE_STATUS.RETURNED: return 'info'
    default: return 'info'
  }
}

/** 主单状态 → tag 类型 */
export const orderStatusTag = (status) => {
  switch (Number(status)) {
    case DISPENSE_ORDER_STATUS.ALL_PENDING: return 'warning'
    case DISPENSE_ORDER_STATUS.PARTIAL: return 'warning'
    case DISPENSE_ORDER_STATUS.ALL_DISPENSED: return 'primary'
    case DISPENSE_ORDER_STATUS.ALL_CHECKED: return 'success'
    case DISPENSE_ORDER_STATUS.ALL_RETURNED: return 'info'
    default: return 'info'
  }
}

/** 明细状态是否允许配药 */
export const canDispense = (status) => Number(status) === DISPENSE_STATUS.PENDING

/** 明细状态是否允许核对 */
export const canCheck = (status) => Number(status) === DISPENSE_STATUS.DISPENSED

/** 明细状态是否允许退药（2/3 均可，4 终态） */
export const canReturn = (status) =>
  [DISPENSE_STATUS.DISPENSED, DISPENSE_STATUS.CHECKED].includes(Number(status))

/** 本地兜底文案（字典命中不了时显示"未知(n)"，不由这里回落成看似合法的值） */
export const ITEM_STATUS_FALLBACK = { 1: '待配药', 2: '已配药', 3: '已核对', 4: '已退药' }
export const ORDER_STATUS_FALLBACK = { 1: '待配药', 2: '配药中', 3: '已配药', 4: '已核对', 5: '已退药' }
