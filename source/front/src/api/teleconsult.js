import request from './request'

/**
 * 互联网医院 / 远程会诊 —— 两条线：
 *   远程会诊：申请（待安排）→ 安排（已安排）→ 出意见完成（已完成）/ 取消（已取消）
 *   线上问诊：发起（待接诊）→ 接诊（接诊中）→ 回复并结束（已完成）/ 退诊（已退诊）
 *
 * 四条必须记住的口径：
 * 1. 所有 ID 都是**字符串**（雪花ID 超过 JS 安全整数范围），不要 Number() 转换。
 * 2. **按钮可用性由后端给**（canArrange / canComplete / canCancel / canAccept / canReply / canReject），
 *    前端不按 status 码值 switch —— 后端加一档状态，switch 会静默渲染成"看着正常"的错按钮。
 * 3. **已安排才可出意见、接诊中才可回复**是后端铁律，前端只在能点时给按钮，不在本地拦截
 *    （本地拦截会掩盖后端规则的失效）。
 * 4. fee 只是价目快照，**不在此处计费**（计费走 charge 域，避免双计）。
 */

// ---------------- 远程会诊 ----------------

/** 分页 */
export function listTeleConsultPage(data) {
  return request({ url: '/patient/teleconsult/teleListPage', method: 'post', data })
}

/** 详情 */
export function getTeleConsultDetail(id) {
  return request({ url: '/patient/teleconsult/teleGetDetailById', method: 'get', params: { id } })
}

/** 申请 / 修改（仅待安排可改） */
export function teleConsultUpsert(data) {
  return request({ url: '/patient/teleconsult/teleUpsert', method: 'post', data })
}

/** 安排会诊（待安排→已安排） */
export function arrangeTeleConsult(data) {
  return request({ url: '/patient/teleconsult/teleArrange', method: 'post', data })
}

/** 完成会诊（已安排→已完成，会诊意见必填） */
export function completeTeleConsult(data) {
  return request({ url: '/patient/teleconsult/teleComplete', method: 'post', data })
}

/** 取消会诊（原因必填） */
export function cancelTeleConsult(data) {
  return request({ url: '/patient/teleconsult/teleCancel', method: 'post', data })
}

/** 删除（仅待安排） */
export function deleteTeleConsult(id) {
  return request({ url: '/patient/teleconsult/teleDeleteById', method: 'delete', params: { id } })
}

// ---------------- 线上问诊 ----------------

/** 分页 */
export function listOnlineConsultPage(data) {
  return request({ url: '/patient/teleconsult/onlineListPage', method: 'post', data })
}

/** 详情 */
export function getOnlineConsultDetail(id) {
  return request({ url: '/patient/teleconsult/onlineGetDetailById', method: 'get', params: { id } })
}

/** 发起问诊 */
export function applyOnlineConsult(data) {
  return request({ url: '/patient/teleconsult/onlineApply', method: 'post', data })
}

/** 接诊（接诊人 = 当前登录人） */
export function acceptOnlineConsult(id) {
  return request({ url: '/patient/teleconsult/onlineAccept', method: 'post', params: { id } })
}

/** 回复并结束（回复必填） */
export function replyOnlineConsult(data) {
  return request({ url: '/patient/teleconsult/onlineReply', method: 'post', data })
}

/** 退诊（原因必填） */
export function rejectOnlineConsult(data) {
  return request({ url: '/patient/teleconsult/onlineReject', method: 'post', data })
}

/** 删除（仅待接诊） */
export function deleteOnlineConsult(id) {
  return request({ url: '/patient/teleconsult/onlineDeleteById', method: 'delete', params: { id } })
}

// ---------------- 统计 ----------------

export function getTeleConsultStat() {
  return request({ url: '/patient/teleconsult/stat', method: 'get' })
}
