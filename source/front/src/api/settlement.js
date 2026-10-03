import request from './request'

// 查询医保结算清单列表
export function getSettlementList(params) {
    return request.get('/charge/settlement/listPage', {params})
}

// 获取结算清单详情
export function getSettlementDetail(id) {
    return request.get('/charge/settlement/getById', {params: {id}})
}

// 医保预结算
export function preSettle(id) {
    return request.post('/charge/settlement/preSettle', null, {params: {id}})
}

// 正式结算
export function settle(id) {
    return request.post('/charge/settlement/settle', null, {params: {id}})
}

// G7 报盘上传：后端组 2304 报文 → InsuranceGateway（当前 Mock，正式走医保前置机）→ 回执成功后清单转「已上传」
export function uploadSettlement(id) {
    return request.post('/charge/settlement/upload', null, {params: {id}})
}

// G7 报盘撤销：发 2305 报文，回执成功后清单回到「已结算」
export function cancelUploadSettlement(data) {
    return request.post('/charge/settlement/cancelUpload', {id: data.id, reason: data.reason})
}

// G7 报文台账分页（不含报文全文）
export function getReportListPage(params) {
    return request.get('/charge/settlement/reportListPage', {params})
}

// G7 单条报文全文（payload / replyPayload）
export function getReportById(id) {
    return request.get('/charge/settlement/reportById', {params: {id}})
}

// G7 日对账：本地清单 vs 医保侧账单
export function reconcileSettlement(data) {
    return request.post('/charge/settlement/reconcile', {billDate: data.billDate})
}

// 审核结算清单
export function auditSettlement(data) {
  return request.post('/charge/settlement/audit', {
    id: data.id,
    approved: data.approved,
    remark: data.remark
  })
}

// —— 医保结算清单统计与详情（区别于上面的结算账单接口，走 /charge/settlement）——
// 医保结算工作台统计（今日费用/医保支付/笔数 + 待结算/已结算）
export function getInsuranceSettlementStats() {
  return request.get('/charge/settlement/stats')
}

// 医保结算清单完整详情（聚合患者/挂号/病历/账单行，逐行带医保 split）
export function getInsuranceSettlementDetailVO(settlementId) {
  return request.get('/charge/settlement/getDetailById', {params: {id: settlementId}})
}
