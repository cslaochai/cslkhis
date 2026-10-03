import request from './request'

// 查询公卫上报列表
export function getPublicHealthList(params) {
  return request.post('/charge/publicHealth/listPage', params)
}

// 获取上报详情
export function getPublicHealthDetail(id) {
  return request.get('/charge/publicHealth/getById', { params: { id } })
}

// 提交上报
export function submitPublicHealth(data) {
  return request.post('/charge/publicHealth/submitReport', data)
}

// 审核上报
export function auditPublicHealth(id, params) {
  return request.post('/charge/publicHealth/auditReport', { id, ...params })
}
