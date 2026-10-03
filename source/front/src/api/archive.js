import request from './request'

// 查询病历归档列表
export function getArchiveList(params) {
  return request.post('/charge/archive/listPage', params)
}

// 获取归档详情
export function getArchiveDetail(id) {
  return request.get('/charge/archive/getById', { params: { id } })
}

// 归档病历
export function archiveRecord(id) {
  return request.post('/charge/archive/archive', null, { params: { id } })
}

// 封存病历
export function sealRecord(id) {
  return request.post('/charge/archive/seal', null, { params: { id } })
}

// 归档三态计数（待归档 / 已归档 / 已封存 + 合计）
export function getArchiveStatusCount() {
  return request.get('/charge/archive/statusCount')
}

// 手动补跑归档超期提醒（日常由定时任务跑）
export function notifyArchiveOverdue() {
  return request.post('/charge/archive/notifyOverdue')
}
