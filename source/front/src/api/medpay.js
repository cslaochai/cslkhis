import request from './request'

// ============ 按病种付费结算（P4，菜单 909）：出院结算账单按 DRG/DIP 计算医保应付与盈亏 ============

// 批量结算（不传 billIds 默认最近 50 张出院结算账单）
export function medpaySettleBatch(data) {
  return request.post('/report/medpay/settleBatch', data || {})
}

// 结算结果分页
export function medpayResultListPage(data) {
  return request.post('/report/medpay/result/listPage', data)
}

// 当前医保付费参数（主方式 / 版本 / 费率 / 点值）
export function medpayMode() {
  return request.get('/report/medpay/mode')
}

// 创建特例单议草稿
export function medpayAppealCreate(data) {
  return request.post('/report/medpay/appeal/create', data)
}

// 提交特例单议（草稿→已提交）
export function medpayAppealSubmit(id) {
  return request.post('/report/medpay/appeal/submit', null, { params: { id } })
}

// 审核特例单议（通过/驳回）
export function medpayAppealReview(data) {
  return request.post('/report/medpay/appeal/review', data)
}

// 撤回特例单议草稿
export function medpayAppealWithdraw(id) {
  return request.post('/report/medpay/appeal/withdraw', null, { params: { id } })
}

// 特例单议分页
export function medpayAppealListPage(data) {
  return request.post('/report/medpay/appeal/listPage', data)
}
