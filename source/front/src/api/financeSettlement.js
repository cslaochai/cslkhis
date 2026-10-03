import request from './request'

// ---------------------------------------------------------------------------
// 财务班结 / 日结 / 三级对账（G8）
//
// ⚠ 与 /appoint/dayEndSettle 无关：那是门诊号源状态结转，不是财务日结。
// ---------------------------------------------------------------------------

/** 收费员交班（班结）。操作人由服务端取登录态，前端不传身份。 */
export function handover(data) {
  return request.post('/settlement/handover', data)
}

/** 交班单分页 */
export function cashierListPage(data) {
  return request.post('/settlement/cashierListPage', data)
}

/** 交班单详情 */
export function getCashierById(settlementId) {
  return request.get('/settlement/getCashierById', { params: { settlementId } })
}

/** 各状态计数 */
export function settlementStatusCount() {
  return request.get('/settlement/statusCount')
}

/** 执行日结（生成或重算待审核的草稿） */
export function runDaySettlement(data) {
  return request.post('/settlement/runDaySettlement', data)
}

/** 日结单分页 */
export function dayListPage(data) {
  return request.post('/settlement/dayListPage', data)
}

/** 日结单详情（含交班单、三级对账、科室收入） */
export function getDayDetailById(settlementId) {
  return request.get('/settlement/getDayDetailById', { params: { settlementId } })
}

/** 按日期查看日结详情（未日结时返回试算结果） */
export function getDayDetailByDate(date) {
  return request.get('/settlement/getDayDetailByDate', { params: { date } })
}

/** 审核日结单 */
export function dayAudit(data) {
  return request.post('/settlement/dayAudit', data)
}

/** 三级对账（可独立于日结单随时查） */
export function reconcile(date) {
  return request.get('/settlement/reconcile', { params: { date } })
}

/** 科室收入（末行为无科室归属） */
export function deptIncome(date) {
  return request.get('/settlement/deptIncome', { params: { date } })
}
