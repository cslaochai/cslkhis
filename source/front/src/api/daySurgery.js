import request from './request'

/**
 * 日间手术 —— 链路：准入目录 → 预约登记（待评估）→ 术前评估（评估通过）
 * → 手术安排（已安排）→ 完成手术（术后观察）→ 出院 / 转住院（终态）→ 24h 随访。
 *
 * 五条必须记住的口径：
 * 1. 所有 ID 都是**字符串**（雪花ID），不要 Number() 转换。
 * 2. **按钮可用性由后端给**（canEvaluate / canArrange / canFinish / canDischarge /
 *    canTransfer / canFollow / canCancel / canDelete），前端不按 status 码值 switch。
 * 3. **准入是闸门**：预约只能选启用中的目录术式（后端校验，前端下拉也只给启用的）。
 * 4. **超期 / 随访时限是后端算的**（overdue / followDue / followOverdue），
 *    前端不要自己拿时间差算一遍 —— 两套算法必然对不上。
 * 5. 转住院必须填住院号，那是医保与病案口径的分界点。
 */

// ---------------- 准入目录 ----------------

export function listDaySurgeryItemPage(data) {
  return request({ url: '/patient/daySurgery/itemListPage', method: 'post', data })
}

export function getDaySurgeryItemList(deptId) {
  return request({ url: '/patient/daySurgery/itemSelectList', method: 'get', params: { deptId } })
}

export function daySurgeryItemUpsert(data) {
  return request({ url: '/patient/daySurgery/itemUpsert', method: 'post', data })
}

export function updateDaySurgeryItemStatus(id, status) {
  return request({ url: '/patient/daySurgery/itemUpdateStatus', method: 'post', params: { id, status } })
}

// ---------------- 登记单 ----------------

export function listDaySurgeryPage(data) {
  return request({ url: '/patient/daySurgery/listPage', method: 'post', data })
}

export function getDaySurgeryDetail(id) {
  return request({ url: '/patient/daySurgery/getDetailById', method: 'get', params: { id } })
}

export function daySurgeryApplyUpsert(data) {
  return request({ url: '/patient/daySurgery/applyUpsert', method: 'post', data })
}

export function evaluateDaySurgery(data) {
  return request({ url: '/patient/daySurgery/evaluate', method: 'post', data })
}

export function arrangeDaySurgery(data) {
  return request({ url: '/patient/daySurgery/arrange', method: 'post', data })
}

export function finishDaySurgery(data) {
  return request({ url: '/patient/daySurgery/finishSurgery', method: 'post', data })
}

export function dischargeDaySurgery(data) {
  return request({ url: '/patient/daySurgery/discharge', method: 'post', data })
}

export function transferDaySurgeryToIpd(data) {
  return request({ url: '/patient/daySurgery/transferToIpd', method: 'post', data })
}

export function cancelDaySurgery(data) {
  return request({ url: '/patient/daySurgery/cancel', method: 'post', data })
}

export function followDaySurgery(data) {
  return request({ url: '/patient/daySurgery/follow', method: 'post', data })
}

export function deleteDaySurgery(id) {
  return request({ url: '/patient/daySurgery/deleteById', method: 'delete', params: { id } })
}

export function getDaySurgeryStat() {
  return request({ url: '/patient/daySurgery/stat', method: 'get' })
}
