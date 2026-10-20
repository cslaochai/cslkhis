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

// ==================== 扫码收款（主扫：医院出码，患者扫） ====================
//
// 出的是「聚合支付码」：一张码微信、支付宝、云闪付都能扫，渠道在患者支付那一刻
// 由医院聚合网关识别运行环境后确定，所以出码接口只传账单、不传渠道。
// 收费台因此只有「现金 / 扫码收款」两档，不再有支付宝/微信切换。
// 收款事实不靠前端点按钮：页面轮询 queryResult，后端查单兜底入账，回调只是加速。

// 出码：按账单当前尚需缴建支付单，返回 data:image/png;base64 的码图（data 只需 billId）
export function createQrPay(data) {
    return request.post('/charge/qrPay/create', data)
}

// 查询支付结果：待支付时后端会向渠道查单并兜底入账，返回账单最新尚需缴
export function queryQrPayResult(data) {
    return request.post('/charge/qrPay/queryResult', data)
}

// 关掉这张账单的待支付码：切到现金或关掉弹层时调。
// 现金已经进抽屉还留着能扫的码，患者扫完就是同一笔账收两次
export function closeQrPayByBill(data) {
    return request.post('/charge/qrPay/closeByBill', data)
}
