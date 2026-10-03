import request from './request'

// 查询退费申请列表
export function getRefundApplyList(params) {
  return request.post('/charge/refund/listPage', params)
}

// 获取退费申请详情
export function getRefundApplyDetail(id) {
  return request.get('/charge/refund/getById', { params: { id } })
}

// 提交退费申请
export function submitRefundApply(data) {
  return request.post('/charge/refund/applyRefund', data)
}

// 审核退费申请
export function auditRefundApply(data) {
  return request.post('/charge/refund/auditApply', {
    id: data.id,
    approved: data.approved,
    auditorId: data.auditorId,
    auditorName: data.auditorName,
    remark: data.remark
  })
}

// 执行退费
export function executeRefund(id, refundBy) {
  return request.post('/charge/refund/executeRefund', { id, refundBy })
}

// 作废退费申请（待审核/审核通过都能作废，原因必填）
export function discardRefundApply(data) {
  return request.post('/charge/refund/discardApply', { id: data.id, reason: data.reason })
}
