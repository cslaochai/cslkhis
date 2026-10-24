import request from './request'

// ==================== 床旁结算（护士站） ====================
//
// 病区护士对分管在院患者办出院结算 + 亮聚合码。与收费台（settlementBill.js）是两套权限两道路：
// 收费台是收费员岗位桌面（finance:cashier:*），床旁是病区场景（nur:bedside:settle），
// 服务端对每次写操作都复核「该入院/账单的病区在当前岗位科室范围内」。
//
// 调用顺序就是业务顺序：
//   wardBoard（本病区在院患者 + 钱况）→ settlePreview（试算）
//   → settle（出账，带回 billId）→ qrCreate（亮聚合码）→ qrQuery（轮询到账）→ qrClose（取消收款）

// 病区在院患者榜（按床号；钱况口径与欠费榜一致）
export function getBedsideWardBoard(data) {
    return request.post('/charge/bedside/wardBoard', data)
}

// 出院结算试算（不落库）：费用明细 + 医保拆分 + 抵扣/退差/欠费结论
export function bedsideSettlePreview(data) {
    return request.post('/charge/bedside/settlePreview', data)
}

// 办理出院结算：一次住院一张有效账单，欠费也出账单（留在未付清）
export function bedsideSettle(data) {
    return request.post('/charge/bedside/settle', data)
}

// 该入院当前有效的出院结算单（已出账回显）
export function bedsideSettlementDetail(data) {
    return request.post('/charge/bedside/settlementDetail', data)
}

// 按账单出聚合支付码（微信/支付宝/云闪付一码通扫）
export function bedsideCreateQr(data) {
    return request.post('/charge/bedside/qrCreate', data)
}

// 轮询收款结果：渠道查单兜底入账
export function bedsideQueryQr(data) {
    return request.post('/charge/bedside/qrQuery', data)
}

// 模拟患者已付款（仅聚合码模拟模式的验收入口；真模式后端拒绝）
export function bedsideMockPaid(data, channel) {
    return request.post('/charge/bedside/qrMockPaid', data, {params: channel ? {channel} : undefined})
}

// 关闭该账单下全部待支付码（患者不付了/改去收费台）
export function bedsideCloseQr(data) {
    return request.post('/charge/bedside/qrClose', data)
}
