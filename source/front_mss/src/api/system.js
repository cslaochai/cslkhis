// 系统管理接口（用户/角色/菜单/岗位/字典/科室）
// 分页出参 { records, total }；入参 pageNum/pageSize/keyword
import request, { cleanParams } from './request'

const idBody = (id) => ({ id: String(id) })

// ---------------- 用户管理 ----------------
export function userPage(params) {
  return request.get('/system/user/listPage', { params: cleanParams(params) })
}
export function userUpsert(data) {
  return request.post('/system/user/upsert', data)
}
export function userResetPassword(data) {
  return request.post('/system/user/resetPassword', data)
}
export function userChangeStatus(data) {
  return request.post('/system/user/changeStatus', data)
}
export function userDelete(id) {
  return request.post('/system/user/deleteById', idBody(id))
}

// ---------------- 角色管理 ----------------
export function rolePage(params) {
  return request.get('/system/role/listPage', { params: cleanParams(params) })
}
export function roleSelectList() {
  return request.get('/system/role/selectList')
}
export function roleDetail(id) {
  return request.get('/system/role/getDetailById', { params: { id } })
}
export function roleUpsert(data) {
  return request.post('/system/role/upsert', data)
}
export function roleDelete(id) {
  return request.post('/system/role/deleteById', idBody(id))
}

// ---------------- 菜单管理 ----------------
export function menuTree() {
  return request.get('/system/menu/tree')
}
export function menuUpsert(data) {
  return request.post('/system/menu/upsert', data)
}
export function menuDelete(id) {
  return request.post('/system/menu/deleteById', idBody(id))
}

// ---------------- 员工岗位管理 ----------------
export function postPage(params) {
  return request.get('/system/post/listPage', { params: cleanParams(params) })
}
export function postsOfEmployee(employeeId) {
  return request.get('/system/post/ofEmployee', { params: { employeeId } })
}
export function postReplace(data) {
  return request.post('/system/post/replace', data)
}
export function employeeSelect(keyword) {
  return request.get('/system/post/employeeSelect', { params: cleanParams({ keyword }) })
}

// ---------------- 经办科室 ----------------
export function deptSelectList() {
  return request.get('/system/dept/selectList')
}

// ---------------- 字典管理 ----------------
export function dictTypePage(params) {
  return request.get('/system/dict/typePage', { params: cleanParams(params) })
}
export function dictTypeUpsert(data) {
  return request.post('/system/dict/typeUpsert', data)
}
export function dictTypeDelete(id) {
  return request.post('/system/dict/typeDelete', idBody(id))
}
export function dictDataPage(params) {
  return request.get('/system/dict/dataPage', { params: cleanParams(params) })
}
export function dictDataUpsert(data) {
  return request.post('/system/dict/dataUpsert', data)
}
export function dictDataDelete(id) {
  return request.post('/system/dict/dataDelete', idBody(id))
}
export function dictDataList(dictType) {
  return request.get('/system/dict/dataList', { params: { dictType } })
}
