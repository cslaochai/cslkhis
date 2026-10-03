import request from './request'

// VTE 防控（sql/167，菜单 332 风险防控 / 333 院内监测）
// 后端控制器：/patient/inpatient/vte
// 口径：风险等级一律取每次住院最新一条 Caprini 评估（服务端算，前端不传等级）

// ========== 风险防控（332） ==========

export function getVteOverview() {
  return request.get('/patient/inpatient/vte/overview')
}

export function listVteRiskPage(params) {
  return request.post('/patient/inpatient/vte/riskListPage', params)
}

export function listVtePreventPage(params) {
  return request.post('/patient/inpatient/vte/preventListPage', params)
}

export function listVtePreventByAdmission(admissionId) {
  return request.get('/patient/inpatient/vte/preventListByAdmission', { params: { admissionId } })
}

export function getVteMeasureOptions(riskLevel) {
  return request.get('/patient/inpatient/vte/measureOptions', { params: { riskLevel } })
}

export function upsertVtePrevent(data) {
  return request.post('/patient/inpatient/vte/preventUpsert', data)
}

export function deleteVtePrevent(id) {
  return request.delete('/patient/inpatient/vte/preventDeleteById', { params: { id } })
}

// ========== 院内监测（333） ==========

export function listVteEventPage(params) {
  return request.post('/patient/inpatient/vte/eventListPage', params)
}

export function listVteEventByAdmission(admissionId) {
  return request.get('/patient/inpatient/vte/eventListByAdmission', { params: { admissionId } })
}

export function upsertVteEvent(data) {
  return request.post('/patient/inpatient/vte/eventUpsert', data)
}

export function deleteVteEvent(id) {
  return request.delete('/patient/inpatient/vte/eventDeleteById', { params: { id } })
}

export function previewVteStats(statMonth) {
  return request.get('/patient/inpatient/vte/previewStats', { params: { statMonth } })
}

export function generateVteStats(data) {
  return request.post('/patient/inpatient/vte/generateStats', data)
}

export function listVteStatsPage(params) {
  return request.post('/patient/inpatient/vte/statsListPage', params)
}

export function exportVteStatsCsv(params) {
  return request.post('/patient/inpatient/vte/statsExportCsv', params)
}
