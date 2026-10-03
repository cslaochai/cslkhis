import request from './request'

// ---------------------------------------------------------------------------
// 支付渠道对账（M7 留口子）
//
// 渠道侧"对方账"来自后端 PayChannelGateway（当前控制台打印桩，不真调商户平台）；
// 台账 / 勾对 / 长短款是真实落库流程。勾对键为渠道流水号 ↔ 本地支付流水号
// （biz_payment_txn.txn_no），渠道编码对齐 pay_method 中走商户平台的那几个：
// 2-微信 3-支付宝 6-银行卡（4 是医保个人账户，不出渠道账单）。
// ---------------------------------------------------------------------------

/** 渠道流水分页 */
export function listPage(params) {
  return request.get('/charge/payChannelBill/listPage', { params })
}

/** 拉取渠道账单（打印桩模拟商户平台拉取，幂等） */
export function importBill(data) {
  return request.post('/charge/payChannelBill/importBill', data)
}

/** 手工登记渠道侧流水（退款流水金额为负） */
export function manualRegister(data) {
  return request.post('/charge/payChannelBill/manualRegister', data)
}

/** 勾对候选：当日该渠道可勾对的本地支付流水（金额相等的排前面） */
export function matchCandidates(channelBillId) {
  return request.get('/charge/payChannelBill/matchCandidates', { params: { channelBillId } })
}

/** 人工勾对：渠道流水 → 本地支付流水（localTxnNo） */
export function match(data) {
  return request.post('/charge/payChannelBill/match', data)
}

/** 长款/短款处理 */
export function handleDiff(data) {
  return request.post('/charge/payChannelBill/handleDiff', data)
}

/** 渠道对账汇总（本地口径 vs 渠道口径） */
export function summary(billDate) {
  return request.get('/charge/payChannelBill/summary', { params: billDate ? { billDate } : {} })
}
