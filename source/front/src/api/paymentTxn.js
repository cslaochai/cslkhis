import request from './request'

// ==================== L3 支付流水台账（biz_payment_txn） ====================
//
// 只读：收/退同表带符号，一账单多流水，渠道流水号在这里。
// 能改流水就等于能对不上渠道 —— 冲正一笔收款走 /charge/settlementBill/refund。

// 分页查询支付流水
export function getPaymentTxnListPage(data) {
    return request.post('/charge/paymentTxn/listPage', data)
}

// 某张账单的全部收/退流水
export function listTxnsByBill(billId) {
    return request.get('/charge/paymentTxn/listByBill', {params: {billId}})
}
