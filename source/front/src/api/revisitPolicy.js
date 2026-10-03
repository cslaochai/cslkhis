import request from './request'

// 分页查询复诊收费策略
export function getRevisitFeePolicyList(params) {
  return request.post('/appoint/revisitFeePolicy/listPage', params)
}

// 获取策略详情
export function getRevisitFeePolicyDetail(id) {
  return request.get('/appoint/revisitFeePolicy/getById', { params: { id } })
}

// 新增或修改策略（后端同一个接口、同一个 :add 权限）
export function revisitFeePolicyUpsert(data) {
  return request.post('/appoint/revisitFeePolicy/revisitFeePolicyUpsert', data)
}

// 删除策略
export function deleteRevisitFeePolicy(id) {
  return request.delete('/appoint/revisitFeePolicy/deleteById', { params: { id } })
}
