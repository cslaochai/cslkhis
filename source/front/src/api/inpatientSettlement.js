import request from './request'

// ==================== 住院账务（预交金 / 日清单 / 住院结算） ====================
// 三条必须记住的口径：
// 1. **余额来自资金账户，不是流水表**：预交金充值/退款是 L3 支付流水（收正退负），
//    余额是住院资金账户的净额（已扣掉结算时的余额抵扣与出院退差）。所以
//    余额 ≠ 充值合计 − 退款合计，前端不要自己拿流水算，也不要拿最后一笔的 balanceAfter 当余额。
// 2. **收退金额一律传正数**，方向由 prepayType 决定（1充值 2退款）。传负数退款会被当成充值。
//    支付方式只允许 1现金/2微信/3支付宝/6银行卡/7转账；4医保个账、5院内余额由服务端拒绝
//    （统筹不是支付方式，余额抵扣只能由出院结算自己动）。退款接口可能返回多行：
//    柜面退款按 FIFO 摊到几笔原充值流水上（微信收的退回微信），前端要按数组渲染。
// 3. **日清单的当日小计与合计都由后端算**：前端不要累加一遍（会漏掉红冲负行），
//    也不要自己按日期分组（口径要和记账行一致，后端是唯一口径）。

export function getPrepayListPage(params) {
    return request.get('/charge/inpatient/account/prepay/listPage', {params})
}

export function getPrepayBalance(admissionId) {
    return request.get('/charge/inpatient/account/prepay/balance', {params: {admissionId}})
}

// 收预交金（充值 / 退款）：amount 传正数
export function savePrepay(data) {
    return request.post('/charge/inpatient/account/prepay/save', data)
}

export function getDailyBill(params) {
    return request.get('/charge/inpatient/account/dailyBill', {params})
}

export function getSettlementPreview(params) {
    return request.get('/charge/inpatient/account/settlement/preview', {params})
}

export function settleInpatient(data) {
    return request.post('/charge/inpatient/account/settlement/settle', data)
}

export function getSettlementDetail(admissionId) {
    return request.get('/charge/inpatient/account/settlement/detail', {params: {admissionId}})
}

// 账务概览（预交金余额 / 已发生费用 / 是否欠费）。注意：欠费只提示不阻断，
// 这个接口不参与任何业务拦截，唯一的拦截在办理出院时。
export function getAccountSummary(admissionId) {
    return request.get('/charge/inpatient/account/summary', {params: {admissionId}})
}

// ============ G20：欠费管控 ============

// 读管控策略（单行）
export function getArrearsPolicy() {
    return request.get('/charge/inpatient/arrears/policy')
}

// 更新管控策略
export function upsertArrearsPolicy(data) {
    return request.post('/charge/inpatient/arrears/policyUpsert', data)
}

// 在院欠费患者榜（按欠费额倒序）
export function getArrearsBoard(data) {
    return request.post('/charge/inpatient/arrears/board', data)
}
