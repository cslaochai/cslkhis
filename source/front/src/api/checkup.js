import request from './request'

// ============ 体检管理（G23）：套餐 / 登记 / 结果 / 总检，1登记→2检查中→3完成→4出报告 ============

// 套餐保存（新增/更新，整单替换明细）
export function checkupPackageSave(data) {
  return request.post('/patient/checkup/package/save', data)
}

// 套餐分页
export function checkupPackageListPage(data) {
  return request.post('/patient/checkup/package/listPage', data)
}

// 套餐详情
export function getCheckupPackage(id) {
  return request.get('/patient/checkup/package/getDetailById', { params: { id } })
}

// 套餐停用
export function checkupPackageDisable(id) {
  return request.post('/patient/checkup/package/disable', null, { params: { id } })
}

// 体检登记（按套餐项目预生成结果空行）
export function checkupRecordCreate(data) {
  return request.post('/patient/checkup/record/create', data)
}

// 体检登记分页
export function checkupRecordListPage(data) {
  return request.post('/patient/checkup/record/listPage', data)
}

// 体检登记详情（含结果明细）
export function getCheckupRecord(recordId) {
  return request.get('/patient/checkup/record/getDetailById', { params: { recordId } })
}

// 开始体检（1→2）
export function checkupRecordStart(recordId) {
  return request.post('/patient/checkup/record/start', null, { params: { recordId } })
}

// 单项结果录入
export function checkupResultSave(data) {
  return request.post('/patient/checkup/result/save', data)
}

// 总检出报告（3→4）
export function checkupConclude(data) {
  return request.post('/patient/checkup/record/conclude', data)
}

// 删除登记（仅已登记可删）
export function checkupRecordDelete(recordId) {
  return request.post('/patient/checkup/record/deleteById', null, { params: { recordId } })
}
