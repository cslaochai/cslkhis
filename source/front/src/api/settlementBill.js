import request from './request'

// ==================== 收费台（L2 结算账单 + L3 收款/退费） ====================
//
// 四层一条链的三段口子，调用顺序就是业务顺序：
//   pendingListPage（哪次就诊有钱要收）→ listPendingFees（这次就诊的待出账记账行）
//   → settlePreview（试算）→ settle（出账单）→ pay（收款，可多笔多渠道）→ /charge/invoice/issue（出票）
//
// 三条口径别在前端自己算：
// 1. **应收金额永远不由前端传入**：settle 只决定"哪些记账行进这张账单、优惠多少"，
//    合计由服务端按行现算；试算与出账共用同一份算法，两边必然是同一个数。
// 2. **payableAmount 才是患者该掏的钱**：已减优惠、统筹、个账。
//    selfAmount 是"应收减统筹"，还带着优惠和个账那两段，两者并排显示必然不等（有优惠的账单）。
//    统筹不是支付方式，不进现金清点，所以收款明细里不许出现"统筹"这一档。
// 3. **是否付清由流水比出来**：paidAmount/refundAmount 是账单上的冗余镜像，
//    权威在 biz_payment_txn；页面只渲染后端给的 unpaidAmount，不再自己减一遍。

// 收费台首屏：按就诊汇总的待收费榜（L1 待出账 + L2 未收齐，两列各说各的）
export function getPendingEncounterPage(data) {
    return request.post('/charge/settlementBill/pendingListPage', data)
}

// 某次就诊下待结算的记账行 + 净额 + 已出账单还欠多少
export function listPendingFees(encounterType, encounterId) {
    return request.get('/charge/settlementBill/listPendingFees', {params: {encounterType, encounterId}})
}

// 出账试算（不落库）
export function settlePreview(data) {
    return request.post('/charge/settlementBill/settlePreview', data)
}

// 结算出账：把选中的记账行锁成一张账单
export function settleBill(data) {
    return request.post('/charge/settlementBill/settle', data)
}

// 取消结算（账单作废 + 解锁记账行；已有收款的后端会拒绝）
export function voidBill(data) {
    return request.post('/charge/settlementBill/voidBill', data)
}

// 收款：一次可提交多笔、多渠道（items 里 amount 一律正数）
export function payBill(data) {
    return request.post('/charge/settlementBill/pay', data)
}

// 按账单退费：feeIds 为空 = 整单退；金额不由前端填（退的是被红冲记账行的净额）
export function refundBill(data) {
    return request.post('/charge/settlementBill/refund', data)
}

export function getBillListPage(data) {
    return request.post('/charge/settlementBill/listPage', data)
}

// 账单详情：账单头 + 行快照 + 全部收/退流水（一次给全，页面不再拼三个请求）
export function getBillDetailById(id) {
  return request.get('/charge/settlementBill/getDetailById', {params: {id}})
}

// 患者维度账单行快照（医生站 / 今日就诊回显「患者已收费项目」）：该患者全部账单行扁平列表
export function listItemsByPatient(patientId) {
  return request.get('/charge/settlementBill/listItemsByPatient', {params: {patientId}})
}
