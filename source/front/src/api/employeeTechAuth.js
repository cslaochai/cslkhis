import request from './request'

// ========== G21 医疗技术临床应用授权（手术分级授权台账，sql/155） ==========

// 授权台账分页（employeeName/employeeId/authCategory/techLevel/authStatus/authType/onlyEffective）
export function getTechAuthListPage(data) {
  return request.post('/system/techAuth/listPage', data)
}

// 授权详情
export function getTechAuthById(id) {
  return request.get('/system/techAuth/getById', { params: { id } })
}

// 按人查授权（员工档案、开单提示共用，只要登录）
export function getTechAuthByEmployee(employeeId) {
  return request.get('/system/techAuth/listByEmployee', { params: { employeeId } })
}

// 当前登录人的三类授权
export function getMyTechAuth() {
  return request.get('/system/techAuth/mine')
}

// 登记 / 修改授权（id 为空即新增；已生效的记录后端拒改字段）
export function techAuthUpsert(data) {
  return request.post('/system/techAuth/techAuthUpsert', data)
}

// 审批（approved=false 即驳回）
export function approveTechAuth(data) {
  return request.post('/system/techAuth/approve', data)
}

// 收回（原因必填）
export function revokeTechAuth(data) {
  return request.post('/system/techAuth/revoke', data)
}

// 删除（仅待审批/已驳回，物理删）
export function deleteTechAuth(id) {
  return request.delete('/system/techAuth/deleteById', { params: { id } })
}

// ========== 急诊越权登记 ==========

export function getTechAuthOverrideListPage(data) {
  return request.post('/system/techAuth/overrideListPage', data)
}

// 上级确认（自己那条后端会拒）
export function confirmTechAuthOverride(data) {
  return request.post('/system/techAuth/overrideConfirm', data)
}
