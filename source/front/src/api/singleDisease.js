import request from './request'

/**
 * 单病种质控（M4）—— 病种目录（ICD 前缀纳入）→ 病例纳入（首页快照）→ 质控判级 → 上报打标 → 病种指标。
 */

/** 病种目录（含已纳入例数） */
export function listDiseases() {
  return request({ url: '/qc/singleDisease/diseaseList', method: 'get' })
}

/** 病种目录新增/修改（编码唯一） */
export function upsertDisease(data) {
  return request({ url: '/qc/singleDisease/diseaseUpsert', method: 'post', data })
}

/** 病种目录删除（已纳入病例的病种拒绝删除） */
export function deleteDisease(id) {
  return request({ url: `/qc/singleDisease/diseaseDelete/${id}`, method: 'post' })
}

/** 手工纳入病例 */
export function enrollCase(data) {
  return request({ url: '/qc/singleDisease/enroll', method: 'post', data })
}

/** 自动扫描纳入（按 ICD 前缀扫出院首页） */
export function autoEnroll(data) {
  return request({ url: '/qc/singleDisease/autoEnroll', method: 'post', data })
}

/** 质控判级 */
export function qcCase(data) {
  return request({ url: '/qc/singleDisease/qc', method: 'post', data })
}

/** 上报打标（质控通过才可上报） */
export function reportCase(id) {
  return request({ url: `/qc/singleDisease/report/${id}`, method: 'post' })
}

/** 病例分页 */
export function listCasePage(data) {
  return request({ url: '/qc/singleDisease/caseListPage', method: 'post', data })
}

/** 病种指标（治愈率/死亡率/平均住院日/平均费用） */
export function getMetrics() {
  return request({ url: '/qc/singleDisease/metrics', method: 'get' })
}
