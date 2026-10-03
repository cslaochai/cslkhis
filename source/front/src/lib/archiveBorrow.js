/**
 * 病案借阅/复印口径单点（G16 收口）
 * —— 类型/状态文案走字典（his_archive_borrow_*），这里只放「非字典语义」的
 *    展示映射：tag 色、各状态下一步动作、超期判定。页面禁止另写映射副本。
 */

/** 状态 → el-tag type（1待审核 2已借出 3已归还 4已拒绝 5已复印） */
export const BORROW_STATUS_TAG = {
  1: 'warning',
  2: 'primary',
  3: 'success',
  4: 'danger',
  5: 'info',
}

/** 类型 → el-tag type（1借阅 2复印） */
export const BORROW_TYPE_TAG = {
  1: 'primary',
  2: 'info',
}

/** 是否超期未还（已借出且应还日期早于今天） */
export function isOverdue(row) {
  if (Number(row.status) !== 2 || !row.expectReturnDate) return false
  const d = new Date(String(row.expectReturnDate).slice(0, 10).replace(/-/g, '/'))
  const today = new Date(new Date().getFullYear(), new Date().getMonth(), new Date().getDate())
  return d < today
}

/** 状态文案兜底时的 tag 色 */
export function statusTagType(status) {
  return BORROW_STATUS_TAG[Number(status)] || 'info'
}

export function typeTagType(type) {
  return BORROW_TYPE_TAG[Number(type)] || 'info'
}
