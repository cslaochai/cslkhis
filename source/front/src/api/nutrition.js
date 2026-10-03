import request from './request'

// 营养膳食管理（sql/168）
// 后端控制器：/patient/inpatient/nutrition（菜单 424 筛查 / 427 营养会诊 / 428 指标监测）
// 口径：NRS2002 总分、有无营养风险、BMI、复筛日期全部由服务端算，前端只提交分项

// ========== 筛查（424） ==========

export function getNutritionOverview() {
  return request.get('/patient/inpatient/nutrition/overview')
}

export function listNutritionScreenPage(params) {
  return request.post('/patient/inpatient/nutrition/screenListPage', params)
}

export function listNutritionScreenByAdmission(admissionId) {
  return request.get('/patient/inpatient/nutrition/screenListByAdmission', { params: { admissionId } })
}

export function upsertNutritionScreen(data) {
  return request.post('/patient/inpatient/nutrition/screenUpsert', data)
}

export function deleteNutritionScreen(id) {
  return request.delete('/patient/inpatient/nutrition/screenDeleteById', { params: { id } })
}

// ========== 营养会诊（427，复用住院会诊闭环，类别由服务端钉死为 2） ==========

export function listNutritionConsultPage(params) {
  return request.post('/patient/inpatient/nutrition/consultListPage', params)
}

export function getNutritionConsultDetail(consultationId) {
  return request.get('/patient/inpatient/nutrition/consultGetById', { params: { consultationId } })
}

export function applyNutritionConsult(data) {
  return request.post('/patient/inpatient/nutrition/consultApply', data)
}

export function acceptNutritionConsult(data) {
  return request.post('/patient/inpatient/nutrition/consultAccept', data)
}

export function finishNutritionConsult(data) {
  return request.post('/patient/inpatient/nutrition/consultFinish', data)
}

export function cancelNutritionConsult(data) {
  return request.post('/patient/inpatient/nutrition/consultCancel', data)
}

// ========== 月度指标（428） ==========

export function previewNutritionStats(statMonth) {
  return request.get('/patient/inpatient/nutrition/previewStats', { params: { statMonth } })
}

export function generateNutritionStats(data) {
  return request.post('/patient/inpatient/nutrition/generateStats', data)
}

export function listNutritionStatsPage(params) {
  return request.post('/patient/inpatient/nutrition/statsListPage', params)
}

export function exportNutritionStatsCsv(params) {
  return request.post('/patient/inpatient/nutrition/statsExportCsv', params)
}
