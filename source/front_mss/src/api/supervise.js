// ============================================================
// 医保基金监管 - 接口层（/supervise/*，需登录态 token）
// ============================================================
import request, { cleanParams } from './request'

const idBody = (id) => ({ id: String(id) })

// ---------------- 监管工作台 ----------------
export function superviseStats() {
  return request.get('/supervise/dashboard/stats')
}

// ---------------- 规则配置 ----------------
export function listRules(params) {
  return request.get('/supervise/rule/listPage', { params: cleanParams(params) })
}
export function upsertRule(data) {
  return request.post('/supervise/rule/upsert', data)
}
export function changeRuleStatus(id, status) {
  return request.post('/supervise/rule/status', { id: String(id), status })
}

// ---------------- 疑点稽核 ----------------
export function listSuspicions(params) {
  return request.get('/supervise/suspicion/listPage', { params: cleanParams(params) })
}
export function getSuspicion(id) {
  return request.get('/supervise/suspicion/getDetailById', { params: { id } })
}
export function runAudit() {
  return request.post('/supervise/suspicion/run')
}
export function confirmSuspicion(data) {
  return request.post('/supervise/suspicion/confirm', data)
}
export function excludeSuspicion(data) {
  return request.post('/supervise/suspicion/exclude', data)
}

// ---------------- 飞检任务 ----------------
export function listInspections(params) {
  return request.get('/supervise/inspection/listPage', { params: cleanParams(params) })
}
export function upsertInspection(data) {
  return request.post('/supervise/inspection/upsert', data)
}
export function startInspection(id) {
  return request.post('/supervise/inspection/start', idBody(id))
}
export function finishInspection(data) {
  return request.post('/supervise/inspection/finish', data)
}
export function getInspection(id) {
  return request.get('/supervise/inspection/getDetailById', { params: { id } })
}
export function addInspectionItem(data) {
  return request.post('/supervise/inspection/item/add', data)
}
export function deleteInspectionItem(id) {
  return request.post('/supervise/inspection/item/delete', idBody(id))
}
export function inspectionToRecover(id) {
  return request.post('/supervise/inspection/toRecover', idBody(id))
}

// ---------------- 违规追回 ----------------
export function listRecovers(params) {
  return request.get('/supervise/recover/listPage', { params: cleanParams(params) })
}
export function getRecover(id) {
  return request.get('/supervise/recover/getDetailById', { params: { id } })
}
export function doRecover(data) {
  return request.post('/supervise/recover/recover', data)
}
export function writeOffRecover(data) {
  return request.post('/supervise/recover/writeOff', data)
}
