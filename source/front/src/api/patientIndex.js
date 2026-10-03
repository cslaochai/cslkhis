import request from './request'

/**
 * 患者主索引（EMPI / P5.1）
 *
 * 口径（写在这里以免视图层各写一套）：
 * 1. EMPI 只维护「影子档案 → 主档」的指向，**不搬业务数据**；
 *    合并后按患者聚合的地方一律走主档归并（后端 resolvePatientIds 是唯一口径）。
 * 2. **没有自动合并**。合并必须由人在重复检测结果里指定"保留哪个、并掉哪个"并填理由。
 * 3. 匹配置信级别由**服务端重算**，前端传的 matchType 只用于对账，不生效。
 * 4. 级别越弱，要求的合并理由越长（L1 ≥2 字，其余 ≥10 字）。
 * 5. 默认列表**不含影子档案**；要看影子必须显式 includeShadow。
 * 6. 合并可撤销（按审计快照还原），撤销会让被并档案重新在册 —— 所以撤销理由必填。
 * 7. ID 是字符串，不要 Number()。
 */

// 患者主索引分页（含档案完整度与业务数据量）
export function getPatientIndexList(params) {
  return request.post('/patient/patientIndex/listPage', params)
}

// 疑似重复档案检测（分级成组）
export function getDuplicateGroups(params) {
  return request.post('/patient/patientIndex/duplicateList', params)
}

// 单份档案的主索引详情（含同主档下的其他档案）
export function getPatientIndexDetail(patientId) {
  return request.get('/patient/patientIndex/getDetailById', { params: { patientId } })
}

// 合并档案（一次并一份；服务端判定匹配级别）
export function mergePatientIndex(data) {
  return request.post('/patient/patientIndex/merge', data)
}

// 撤销合并（按审计快照还原）
export function revertPatientMerge(data) {
  return request.post('/patient/patientIndex/revert', data)
}

// 合并历史分页
export function getMergeLogList(params) {
  return request.post('/patient/patientIndex/mergeLogListPage', params)
}

// EMPI 概览指标（唯一性 / 完整性）
export function getPatientIndexStats() {
  return request.get('/patient/patientIndex/stats')
}

// 字典：匹配级别 + 档案关键字段清单（与后端规则同源，前端不硬编码）
export function getPatientIndexDict() {
  return request.get('/patient/patientIndex/dict')
}
