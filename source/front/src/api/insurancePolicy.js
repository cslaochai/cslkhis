import request from './request'

// 分页查询医保政策
export function getInsurancePolicyList(params) {
  return request.post('/system/insurancePolicy/listPage', params)
}

// 获取医保政策详情
export function getInsurancePolicyDetail(id) {
  return request.get('/system/insurancePolicy/getById', { params: { id } })
}

// 新增或修改医保政策
export function insurancePolicyUpsert(data) {
  return request.post('/system/insurancePolicy/insurancePolicyUpsert', data)
}

// 删除医保政策
export function deleteInsurancePolicy(id) {
  return request.delete('/system/insurancePolicy/deleteById', { params: { id } })
}
