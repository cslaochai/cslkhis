import request from './request'

/**
 * 不良事件上报（G12）—— 后端 /emr/adverseEvent
 * 状态机：1 已上报待处理 → 2 处理中 → 3 已整改 → 4 已结案（不可逆）
 */

/** 分页查询 */
export function getAdverseEventList(params) {
  return request.post('/emr/adverseEvent/listPage', params)
}

/** 事件详情 */
export function getAdverseEventDetail(id) {
  return request.get('/emr/adverseEvent/getDetailById', { params: { id } })
}

/** 上报事件 / 修改待处理事件 */
export function upsertAdverseEvent(data) {
  return request.post('/emr/adverseEvent/upsert', data)
}

/** 处理（1→2） */
export function handleAdverseEvent(id, remark) {
  return request.post('/emr/adverseEvent/handle', { id, remark })
}

/** 整改（2→3） */
export function rectifyAdverseEvent(id, remark) {
  return request.post('/emr/adverseEvent/rectify', { id, remark })
}

/** 结案（3→4，不可逆） */
export function closeAdverseEvent(id, remark) {
  return request.post('/emr/adverseEvent/close', { id, remark })
}

/** 删除待处理事件（仅上报人本人） */
export function deleteAdverseEvent(id) {
  return request.delete('/emr/adverseEvent/deleteById', { params: { id } })
}

/** 工作台统计（本月上报/待处理/警讯/已结案） */
export function getAdverseEventStats() {
  return request.get('/emr/adverseEvent/stats')
}
