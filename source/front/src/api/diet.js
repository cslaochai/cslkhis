import request from './request'

// 膳食医嘱执行与订餐配送（sql/168，菜单 425 膳食医嘱执行 / 426 订餐配送）
// 后端控制器：/patient/inpatient/diet
// 口径：方案主来源是 orderClass=10 医嘱校对派生（同事务），本页只服务营养科手工登记与接收/退回

export function getDietTypeOptions() {
  return request.get('/patient/inpatient/diet/dietTypeOptions')
}

// 病区下拉（参照数据，不猜权限）
export function getDietWardSelectList() {
  return request.get('/patient/inpatient/diet/wardSelectList')
}

// ========== 膳食方案（425） ==========

export function listDietPlanPage(params) {
  return request.post('/patient/inpatient/diet/planListPage', params)
}

export function listDietPlanByAdmission(admissionId) {
  return request.get('/patient/inpatient/diet/planListByAdmission', { params: { admissionId } })
}

export function upsertDietPlan(data) {
  return request.post('/patient/inpatient/diet/planUpsert', data)
}

// accept=true 接收，accept=false 退回（退回必填原因）
export function confirmDietPlan(data) {
  return request.post('/patient/inpatient/diet/planConfirm', data)
}

export function stopDietPlan(data) {
  return request.post('/patient/inpatient/diet/planStop', data)
}

export function deleteDietPlan(id) {
  return request.delete('/patient/inpatient/diet/planDeleteById', { params: { id } })
}

// ========== 订餐配送（426） ==========

export function listMealOrderPage(params) {
  return request.post('/patient/inpatient/diet/mealListPage', params)
}

export function listMealOrderByPlan(dietPlanId) {
  return request.get('/patient/inpatient/diet/mealListByPlan', { params: { dietPlanId } })
}

export function generateMealOrder(data) {
  return request.post('/patient/inpatient/diet/mealGenerate', data)
}

// 推进状态机：0 待配餐 → 1 已配餐 → 2 已配送 → 3 已签收；退订单独走 deliverStatus=4 + 原因
export function updateMealStatus(data) {
  return request.post('/patient/inpatient/diet/mealStatus', data)
}

export function deleteMealOrder(id) {
  return request.delete('/patient/inpatient/diet/mealDeleteById', { params: { id } })
}
