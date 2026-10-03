import request from './request'

/**
 * 病案编码任务池（G16 收口）—— 后端 /charge/codeTask
 * 状态机：1 待编码 →（提交）→ 2 已提交 →（审核）→ 3 已完成 / 4 已退修（退修可再提交）
 */

/** 分页查询 */
export function getCodeTaskList(params) {
  return request.post('/charge/codeTask/listPage', params)
}

/** 详情 */
export function getCodeTaskDetail(id) {
  return request.get('/charge/codeTask/getDetailById', { params: { id } })
}

/** 工作台统计（待编码/已提交/已完成/已退修） */
export function getCodeTaskStats() {
  return request.get('/charge/codeTask/stats')
}

/** 同步任务池（幂等：只给尚无任务的病历建单） */
export function syncCodeTasks() {
  return request.post('/charge/codeTask/sync')
}

/** 分配编码员 */
export function assignCodeTask(id, coderId) {
  return request.post('/charge/codeTask/assign', { id, coderId })
}

/** 提交编码 */
export function submitCodeTask(data) {
  return request.post('/charge/codeTask/submit', data)
}

/** 审核（approve: true 通过 / false 退修） */
export function auditCodeTask(id, approve, remark) {
  return request.post('/charge/codeTask/audit', { id, approve, remark })
}
