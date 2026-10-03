import request from './request'

/**
 * 医疗纠纷 / 投诉登记 —— 链路：登记（待受理）→ 受理（调查中，按需封存病历）
 * → 调查/处理跟踪（处理中）→ 结案（终态）/ 撤销（终态）。
 *
 * 四条必须记住的口径：
 * 1. 所有 ID 都是**字符串**（雪花ID 超过 JS 安全整数范围），不要 Number() 转换。
 * 2. **按钮可用性由后端给**（canEdit / canAccept / canFollow / canClose / canRevoke / canSeal / canDelete），
 *    前端不按 status 码值 switch —— 后端加一档状态，switch 会静默渲染成"看着正常"的错按钮。
 * 3. **受理会联动封存病历**：needSeal=1 且患者有已归档病案 → sealStatus=1 已封存；
 *    暂无已归档病案 → sealStatus=2 待归档后封存（不阻断受理，但主单上必须让用户看得见）。
 * 4. 投诉人电话出参**已脱敏**，前端不要再裁一遍；「受理天数」openDays 由服务端算。
 */

/** 分页（keyword/caseType/status/level/deptId/openOnly/dateFrom/dateTo） */
export function listDisputePage(data) {
  return request({ url: '/emr/dispute/listPage', method: 'post', data })
}

/** 详情（含处理跟踪台账） */
export function getDisputeDetail(id) {
  return request({ url: '/emr/dispute/getDetailById', method: 'get', params: { id } })
}

/** 登记 / 修改（仅待受理可改） */
export function disputeUpsert(data) {
  return request({ url: '/emr/dispute/caseUpsert', method: 'post', data })
}

/** 受理（待受理→调查中，按需封存病历） */
export function acceptDispute(data) {
  return request({ url: '/emr/dispute/accept', method: 'post', data })
}

/** 登记处理跟踪（可推进到调查中/处理中） */
export function followDispute(data) {
  return request({ url: '/emr/dispute/follow', method: 'post', data })
}

/** 补封存病历（受理时无已归档病案的单据） */
export function sealDisputeNow(data) {
  return request({ url: '/emr/dispute/sealNow', method: 'post', data })
}

/** 结案（处理途径+责任认定+赔偿金额+结论 必填） */
export function closeDispute(data) {
  return request({ url: '/emr/dispute/close', method: 'post', data })
}

/** 撤销（原因必填） */
export function revokeDispute(data) {
  return request({ url: '/emr/dispute/revoke', method: 'post', data })
}

/** 删除（软删；仅待受理且无跟踪流水） */
export function deleteDispute(id) {
  return request({ url: '/emr/dispute/deleteById', method: 'delete', params: { id } })
}

/** 统计（状态分布/类型分布/科室TOP/赔偿合计/平均结案天数） */
export function getDisputeStat(params) {
  return request({ url: '/emr/dispute/stat', method: 'get', params })
}
