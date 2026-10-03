import request from './request'

// 分页查询退费流水（只读台账）
export function getRefundFlowList(params) {
  return request.post('/charge/refundFlow/listPage', params)
}

// 退费流水详情（含逐条退费明细）
export function getRefundFlowDetail(id) {
  return request.get('/charge/refundFlow/getDetailById', { params: { id } })
}
