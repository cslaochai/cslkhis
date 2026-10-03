import request from './request'

/**
 * 临床路径 —— 链路：模板维护（草稿→发布→停用）→ 入径 → 变异登记 → 完成/退径 → 变异分析。
 * 步骤是文书记录，不生成医嘱、不计费；操作人服务端取当前登录人。
 */

/** 模板分页 */
export function listPathwayPage(data) {
  return request({ url: '/emr/pathway/listPage', method: 'post', data })
}

/** 模板详情（含步骤） */
export function getPathwayDetail(id) {
  return request({ url: '/emr/pathway/getDetailById', method: 'get', params: { id } })
}

/** 使用中模板下拉（入径选择用） */
export function getActivePathwayList(params) {
  return request({ url: '/emr/pathway/activeSelectList', method: 'get', params })
}

/** 模板新增/修改（仅草稿可编辑，步骤整组替换） */
export function pathwayUpsert(data) {
  return request({ url: '/emr/pathway/pathwayUpsert', method: 'post', data })
}

/** 发布（草稿→使用中，同码仅一张） */
export function publishPathway(data) {
  return request({ url: '/emr/pathway/publishPathway', method: 'post', data })
}

/** 停用（使用中→已停用） */
export function deprecatePathway(data) {
  return request({ url: '/emr/pathway/deprecatePathway', method: 'post', data })
}

/** 入径台账分页 */
export function listEnrollPage(data) {
  return request({ url: '/emr/pathway/enrollListPage', method: 'post', data })
}

/** 入径详情（快照+当前路径日+模板步骤+变异台账） */
export function getEnrollDetail(id) {
  return request({ url: '/emr/pathway/enrollGetDetailById', method: 'get', params: { id } })
}

/** 可入径候选（在院且无在径记录） */
export function listAdmissionsForEnroll(params) {
  return request({ url: '/emr/pathway/admissionsForEnroll', method: 'get', params })
}

/** 入径登记 */
export function enrollUpsert(data) {
  return request({ url: '/emr/pathway/enrollUpsert', method: 'post', data })
}

/** 登记变异（追加台账，仅在径，原因必填） */
export function varianceUpsert(data) {
  return request({ url: '/emr/pathway/varianceUpsert', method: 'post', data })
}

/** 完成（在径→已完成，有变异不挡完成） */
export function finishEnroll(data) {
  return request({ url: '/emr/pathway/finishEnroll', method: 'post', data })
}

/** 退径（在径→已退径，原因必填，终态） */
export function abortEnroll(data) {
  return request({ url: '/emr/pathway/abortEnroll', method: 'post', data })
}

/** 变异分析（pathwayId 空则全院） */
export function getPathwayAnalysis(params) {
  return request({ url: '/emr/pathway/analysis', method: 'get', params })
}

/** 医生站横幅：按住院号查在径记录（不在径返回 data=null） */
export function getActiveEnrollByAdmission(admissionId) {
  return request({ url: '/emr/pathway/activeEnrollByAdmission', method: 'get', params: { admissionId } })
}

/** 开单偏离预检（软约束：报偏差不拦截） */
export function pathwayOrderCheck(data) {
  return request({ url: '/emr/pathway/orderCheck', method: 'post', data })
}
