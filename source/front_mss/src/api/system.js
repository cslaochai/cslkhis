// 系统管理接口（HIS 风格函数名，对接 MSS 后端 HIS 契约接口）
import request, { cleanParams } from './request'

// ========== 认证 ==========

// 获取当前用户信息（含 permissions 权限码集合）
export function getUserInfo() {
  return request.get('/auth/info')
}

// ========== 用户管理 ==========

export function getUserList(params) {
  return request.post('/system/user/listPage', cleanParams(params))
}

export function getUserDetail(id) {
  return request.get('/system/user/getById', { params: { id } })
}

export function createUser(data) {
  return request.post('/system/user/userUpsert', data)
}

export function updateUser(data) {
  return request.post('/system/user/userUpsert', data)
}

export function deleteUser(id) {
  return request.delete('/system/user/deleteById', { params: { userId: id } })
}

export function resetPassword(userId) {
  return request.post('/system/user/resetPassword', { userId })
}

// ========== 角色管理 ==========

export function getRoleListPage(params) {
  return request.post('/system/role/listPage', cleanParams(params))
}

export function getRoleList(params) {
  return request.post('/system/role/selectList', cleanParams(params || {}))
}

export function getRoleDetail(id) {
  return request.get('/system/role/getById', { params: { roleId: id } })
}

export function createRole(data) {
  return request.post('/system/role/roleUpsert', data)
}

export function updateRole(data) {
  return request.post('/system/role/roleUpsert', data)
}

export function deleteRole(id) {
  return request.delete('/system/role/deleteById', { params: { roleId: id } })
}

export function getRoleMenuIds(roleId) {
  return request.get('/system/role/getMenuIds', { params: { roleId } })
}

export function saveRoleMenu(data) {
  return request.post('/system/role/saveRoleMenu', data)
}

// ========== 菜单管理 ==========

export function getMenuTree() {
  return request.get('/system/menu/tree')
}

export function getMenuDetail(id) {
  return request.get('/system/menu/getById', { params: { menuId: id } })
}

export function createMenu(data) {
  return request.post('/system/menu/menuUpsert', data)
}

export function updateMenu(data) {
  return request.post('/system/menu/menuUpsert', data)
}

export function deleteMenu(id) {
  return request.delete('/system/menu/deleteById', { params: { menuId: id } })
}

// ========== 科室管理（HIS DeptView 契约） ==========

export function getDepartmentTree() {
  return request.get('/system/department/tree')
}

export function getDepartmentSelectList(params = {}) {
  return request.get('/system/department/selectList', { params: cleanParams(params) })
}

export function getDepartmentDetail(id) {
  return request.get('/system/department/getById', { params: { deptId: id } })
}

export function createDepartment(data) {
  return request.post('/system/department/departmentUpsert', data)
}

export function updateDepartment(data) {
  return request.post('/system/department/departmentUpsert', data)
}

export function deleteDepartment(id) {
  return request.delete('/system/department/deleteById', { params: { deptId: id } })
}

// ========== 员工管理（HIS EmployeeView/EmployeeEdit 契约） ==========

export function getEmployeeListPage(params) {
  return request.get('/system/employee/listPage', { params: cleanParams(params) })
}

export function getEmployeeDetail(id) {
  return request.get('/system/employee/getById', { params: { id } })
}

export function createEmployee(data) {
  return request.post('/system/employee/employeeUpsert', data)
}

export function updateEmployee(data) {
  return request.post('/system/employee/employeeUpsert', data)
}

export function deleteEmployee(id) {
  return request.delete('/system/employee/deleteById', { params: { id } })
}

export function getEmployeeQualificationList(params) {
  return request.get('/system/employee/qualification/listByEmployee', { params: cleanParams(params) })
}

export function upsertEmployeeQualificationBatch(data) {
  return request.post('/system/employee/qualification/qualificationBatchUpsert', data)
}

export function deleteEmployeeQualification(id) {
  return request.delete('/system/employee/qualification/deleteById', { params: { id } })
}

// ========== 字典管理 ==========

export function getDictTypeList() {
  return request.get('/system/dict/type/selectList')
}

export function getDictTypeDetail(id) {
  return request.get('/system/dict/type/getById', { params: { typeId: id } })
}

export function createDictType(data) {
  return request.post('/system/dict/typeUpsert', data)
}

export function updateDictType(data) {
  return request.post('/system/dict/typeUpsert', data)
}

export function deleteDictType(id) {
  return request.delete('/system/dict/type/deleteById', { params: { typeId: id } })
}

// 业务下拉只展示启用项；字典管理页不受影响
const isDictEnabled = d => d && d.status !== 0 && d.status !== '0'

// 查询字典数据列表（HIS：POST /system/dict/data/selectList，dictTypes 支持单值或逗号分隔）
export function getDictDataList(dictTypes) {
  return request.post('/system/dict/data/selectList', { dictTypes }).then(res => {
    if (res && res.code === 200 && Array.isArray(res.data)) {
      res.data = res.data.filter(isDictEnabled)
    }
    return res
  })
}

// 批量获取字典数据（后端返回按类型分组的数组，这里还原为视图使用的对象结构）
export function getDictDataMapList(dictTypes) {
  return request.post('/system/dict/data/selectGroup', { dictTypes }).then(res => {
    if (res && res.code === 200 && Array.isArray(res.data)) {
      const map = {}
      res.data.forEach(group => {
        map[group.dictType] = (group.dataList || []).filter(isDictEnabled)
      })
      return { ...res, data: map }
    }
    return res
  })
}

export function getDictDataDetail(id) {
  return request.get('/system/dict/data/detail/getById', { params: { dataId: id } })
}

export function createDictData(data) {
  return request.post('/system/dict/dataUpsert', data)
}

export function updateDictData(data) {
  return request.post('/system/dict/dataUpsert', data)
}

export function deleteDictData(id) {
  return request.delete('/system/dict/data/deleteById', { params: { dataId: id } })
}

// ========== 员工岗位管理（MSS 岗位管理页） ==========

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

export function deptSelectList() {
  return request.get('/system/dept/selectList')
}
