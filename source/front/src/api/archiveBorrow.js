import request from './request'

/**
 * 病案借阅/复印（G16 收口）—— 后端 /charge/archiveBorrow
 * 状态机：1 待审核 → 2 已借出 → 3 已归还；拒绝 → 4；复印审核通过 → 5 已复印
 */

/** 分页查询 */
export function getBorrowList(params) {
  return request.post('/charge/archiveBorrow/listPage', params)
}

/** 详情 */
export function getBorrowDetail(id) {
  return request.get('/charge/archiveBorrow/getDetailById', { params: { id } })
}

/** 工作台统计（待审核/已借出/超期未还/已归还） */
export function getBorrowStats() {
  return request.get('/charge/archiveBorrow/stats')
}

/** 申请借阅/复印 */
export function applyBorrow(data) {
  return request.post('/charge/archiveBorrow/apply', data)
}

/** 审核（approve: true 通过 / false 拒绝） */
export function auditBorrow(id, approve, remark) {
  return request.post('/charge/archiveBorrow/audit', { id, approve, remark })
}

/** 归还（借阅单 2→3） */
export function returnBorrow(id) {
  return request.post('/charge/archiveBorrow/giveBack', null, { params: { id } })
}

/** 删除待审核单（仅申请人本人） */
export function deleteBorrow(id) {
  return request.delete('/charge/archiveBorrow/deleteById', { params: { id } })
}

/** 手动补跑借阅超期提醒 */
export function notifyBorrowOverdue() {
  return request.post('/charge/archiveBorrow/notifyOverdue')
}
