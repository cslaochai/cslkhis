import request from './request'

// ========== 药品管理 ==========
export function getDrugListPage(params) {
    return request.post('/system/drug/listPage', params)
}

export function getDrugSelectList(params) {
    return request.get('/system/drug/selectList', {params})
}

export function getInspectionSelectList() {
    return request.get('/system/medical-item/inspection/selectList')
}

export function getLaboratorySelectList() {
    return request.get('/system/medical-item/laboratory/selectList')
}

// ========== 科室管理 ==========

// 查询科室列表（分页）
export function getDepartmentList(params) {
    return request.post('/system/department/listPage', params)
}

// 查询科室列表（不分页）
export function getDepartmentListAll(params) {
    return request.post('/system/department/list', params)
}

// 查询科室树
export function getDepartmentTree() {
    return request.get('/system/department/tree')
}

/**
 * 科室下拉统一入口 —— 页面**只应该**用这一个。
 *
 * @param {Object} params
 * @param {string} [params.scope]    数据范围。**不传 / 'CURRENT' = 按当前人过滤（默认）**；
 *                                   'ALL' = 全部科室（字典维护、排班模板这类要全量范围的场景）。
 *                                   默认按当前人，是为了让漏传参数的页面拿到收窄结果而不是全院号源。
 * @param {number} [params.deptType] 科室类型：1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他
 * @param {string} [params.deptName] 科室名称模糊匹配
 * @returns 科室下拉项数组（含 id / deptCode / deptName / deptType / parentId）
 */
export function getDepartmentSelectList(params = {}) {
    return request.get('/system/department/selectList', {params})
}

// 获取科室详情
export function getDepartmentDetail(id) {
    return request.get('/system/department/getById', {params: {deptId: id}})
}

// 新增科室
export function createDepartment(data) {
    return request.post('/system/department/departmentUpsert', data)
}

// 修改科室
export function updateDepartment(data) {
    return request.post('/system/department/departmentUpsert', data)
}

// 删除科室
export function deleteDepartment(id) {
    return request.delete('/system/department/deleteById', {params: {deptId: id}})
}

// ========== 诊室管理 ==========

// 查询诊室列表（分页）
export function getClinicRoomList(params) {
    return request.post('/system/clinicRoom/listPage', params)
}

// 查询诊室列表（不分页）
export function getClinicRoomListAll(params) {
    return request.post('/system/clinicRoom/list', params)
}

// 获取诊室详情
export function getClinicRoomDetail(id) {
    return request.get('/system/clinicRoom/getById', {params: {roomId: id}})
}

// 新增诊室
export function createClinicRoom(data) {
    return request.post('/system/clinicRoom/clinicRoomUpsert', data)
}

// 修改诊室
export function updateClinicRoom(data) {
    return request.post('/system/clinicRoom/clinicRoomUpsert', data)
}

// 删除诊室
export function deleteClinicRoom(id) {
    return request.delete('/system/clinicRoom/deleteById', {params: {roomId: id}})
}

// ========== 员工管理 ==========

// 查询员工列表
export function getEmployeeList(params) {
    return request.get('/system/employee/selectList', {params})
}

// 查询员工列表
export function getEmployeeListPage(params) {
    return request.get('/system/employee/listPage', {params})
}

// 获取员工详情
export function getEmployeeDetail(id) {
    return request.get('/system/employee/getById', {params: {id}})
}

// 新增员工
export function createEmployee(data) {
    return request.post('/system/employee/employeeUpsert', data)
}

// 修改员工
export function updateEmployee(data) {
    return request.post('/system/employee/employeeUpsert', data)
}

// 删除员工
export function deleteEmployee(id) {
    return request.delete('/system/employee/deleteById', {params: {id}})
}

// ========== 员工资格证书 ==========

// 查询某员工的资格证书列表
export function getEmployeeQualificationList(params) {
    return request.get('/system/employee/qualification/listByEmployee', {params})
}

// 新增/修改资格证书
export function upsertEmployeeQualification(data) {
    return request.post('/system/employee/qualification/qualificationUpsert', data)
}

// 删除资格证书
export function deleteEmployeeQualification(id) {
    return request.delete('/system/employee/qualification/deleteById', {params: {id}})
}

// ========== 字典管理 ==========

// 查询字典类型列表
export function getDictTypeList() {
    return request.get('/system/dict/type/selectList')
}

// 获取字典类型详情
export function getDictTypeDetail(id) {
    return request.get('/system/dict/type/getById', {params: {typeId: id}})
}

// 新增字典类型
export function createDictType(data) {
    return request.post('/system/dict/typeUpsert', data)
}

// 修改字典类型
export function updateDictType(data) {
    return request.post('/system/dict/typeUpsert', data)
}

// 删除字典类型
export function deleteDictType(id) {
    return request.delete('/system/dict/type/deleteById', {params: {typeId: id}})
}

// 查询字典数据列表
export function getDictDataList(dictTypes) {
    return request.post('/system/dict/data/selectList', {dictTypes})
}

// 批量获取字典数据（后端返回按类型分组的数组，这里还原为视图使用的对象结构）
export function getDictDataMapList(dictTypes) {
    return request.post('/system/dict/data/selectGroup', {dictTypes}).then(res => {
        if (res && res.code === 200 && Array.isArray(res.data)) {
            const map = {}
            res.data.forEach(group => {
                map[group.dictType] = group.dataList || []
            })
            return {...res, data: map}
        }
        return res
    })
}

// 获取字典数据详情
export function getDictDataDetail(id) {
    return request.get('/system/dict/data/detail/getById', {params: {dataId: id}})
}

// 新增字典数据
export function createDictData(data) {
    return request.post('/system/dict/dataUpsert', data)
}

// 修改字典数据
export function updateDictData(data) {
    return request.post('/system/dict/dataUpsert', data)
}

// 删除字典数据
export function deleteDictData(id) {
    return request.delete('/system/dict/data/deleteById', {params: {dataId: id}})
}

// ========== 用户管理 ==========

// 用户登录
export function login(data) {
    return request.post('/auth/login', data)
}

// 获取当前用户信息
export function getUserInfo() {
    return request.get('/auth/info')
}

// 获取当前用户的岗位列表（角色 × 科室），取代旧的 /auth/getDepts
export function getPostList() {
    return request.get('/auth/postList')
}

// 用户登出
export function logout() {
    return request.post('/auth/logout')
}

// 查询用户列表
export function getUserList(params) {
    return request.post('/system/user/listPage', params)
}

// 获取用户详情
export function getUserDetail(id) {
    return request.get('/system/user/getById', {params: {id}})
}

// 本人档案（手机号/身份证/邮箱由后端脱敏，只读展示用；不含编辑回显口径）
export function getSelfProfile() {
    return request.get('/system/user/selfProfile')
}

// 新增用户
export function createUser(data) {
    return request.post('/system/user/userUpsert', data)
}

// 修改用户
export function updateUser(data) {
    return request.post('/system/user/userUpsert', data)
}

// 删除用户
export function deleteUser(id) {
    return request.delete('/system/user/deleteById', {params: {userId: id}})
}

// 重置密码
export function resetPassword(userId) {
    return request.post('/system/user/resetPassword', {userId})
}


// 修改密码
export function changePassword(data) {
    return request.post('/auth/changePassword', data)
}

// 获取当前用户角色列表
export function getUserRoles() {
    return request.get('/auth/roles')
}

// 切换岗位（角色 + 科室成对切换）：后端只认 sys_employee_post 里真实存在的组合，
// 不允许「医生切到骨科、药剂师也切到骨科」这种两边分别拼出来的身份
export function switchPost(data) {
    return request.post('/auth/switchPost', data)
}

// ========== 角色管理 ==========

// 查询角色列表
export function getRoleListPage(params) {
    return request.post('/system/role/listPage', params)
}

// 角色下拉（全岗位通用，只要求登录）
export function getRoleList(params) {
    return request.post('/system/role/selectList', params)
}

// 获取角色详情
export function getRoleDetail(id) {
    return request.get('/system/role/getById', {params: {roleId: id}})
}

// 新增角色
export function createRole(data) {
    return request.post('/system/role/roleUpsert', data)
}

// 修改角色
export function updateRole(data) {
    return request.post('/system/role/roleUpsert', data)
}

// 删除角色
export function deleteRole(id) {
    return request.delete('/system/role/deleteById', {params: {roleId: id}})
}

// 查询角色已配置的菜单ID（权限树回显用）
export function getRoleMenuIds(roleId) {
    return request.get('/system/role/getMenuIds', {params: {roleId}})
}

// 保存角色菜单权限（整表替换：以传入 menuIds 为该角色最终权限）
export function saveRoleMenu(data) {
    return request.post('/system/role/saveRoleMenu', data)
}

// ========== 菜单管理 ==========

// 查询菜单列表（后端以树形返回）
export function getMenuList(params) {
    return request.get('/system/menu/tree', {params})
}

// 查询菜单树
export function getMenuTree() {
    return request.get('/system/menu/tree')
}

// 获取当前用户菜单（按当前角色过滤）
export function getUserMenus() {
    return request.get('/system/menu/userMenus')
}

// 获取菜单详情
export function getMenuDetail(id) {
    return request.get('/system/menu/getById', {params: {menuId: id}})
}

// 新增菜单
export function createMenu(data) {
    return request.post('/system/menu/menuUpsert', data)
}

// 修改菜单
export function updateMenu(data) {
    return request.post('/system/menu/menuUpsert', data)
}

// 删除菜单
export function deleteMenu(id) {
    return request.delete('/system/menu/deleteById', {params: {menuId: id}})
}

// ========== 参数配置 ==========

// 获取医院基础信息（sys_config: hospital.* 键）
export function getHospitalConfig() {
    return request.get('/system/config/hospitalInfo')
}

// 保存医院基础信息
export function saveHospitalConfig(data) {
    return request.post('/system/config/hospitalUpsert', data)
}

// ========== 消息通知 ==========

// 查询未读消息数量
export function getUnreadCount() {
    return request.get('/system/message/unread/count')
}

// 查询消息列表
export function getMessageList(params) {
    return request.get('/system/message/listPage', { params })
}

// 标记消息已读
export function readMessage(messageId) {
    return request.post('/system/message/read', null, {params: {messageId}})
}

// 标记所有消息已读
export function readAllMessages() {
    return request.post('/system/message/readAll')
}

// 按业务类型分组统计消息数（抽屉 Tab 徽标用）
export function getMessageTypeCounts() {
    return request.get('/system/message/typeCounts')
}

// ========== ICD-10编码 ==========

// 搜索ICD-10编码
export function searchIcd10(keyword) {
    return request.get('/system/icd10/selectList', {params: {keyword, limit: 50}})
}

// 智能预测ICD-10编码
export function predictIcd10(data) {
    return request.post('/system/icd10/predict', data)
}

// ========== 检查检验项目 ==========

// 查询检查项目列表
export function getInspectionItemList(params) {
    return request.post('/system/medical-item/inspectionListPage', params)
}

// 搜索检查项目：与全量下拉同一个 selectList 接口，传 keyword/limit 即关键字检索
export function searchInspectionItem(keyword) {
    return request.get('/system/medical-item/inspection/selectList', {params: {keyword, limit: 50}})
}

// 获取检查项目详情
export function getInspectionItemDetail(id) {
    return request.get('/system/medical-item/inspection/getById', {params: {id}})
}

// 新增检查项目
export function createInspectionItem(data) {
    return request.post('/system/medical-item/inspectionUpsert', data)
}

// 修改检查项目
export function updateInspectionItem(data) {
    return request.post('/system/medical-item/inspectionUpsert', data)
}

// 删除检查项目
export function deleteInspectionItem(id) {
    return request.delete('/system/medical-item/inspection/deleteById', {params: {id}})
}

// 查询检验项目列表
export function getLaboratoryItemList(params) {
    return request.post('/system/medical-item/laboratoryListPage', params)
}

// 搜索检验项目：与全量下拉同一个 selectList 接口，传 keyword/limit 即关键字检索
export function searchLaboratoryItem(keyword) {
    return request.get('/system/medical-item/laboratory/selectList', {params: {keyword, limit: 50}})
}

// 获取检验项目详情
export function getLaboratoryItemDetail(id) {
    return request.get('/system/medical-item/laboratory/getById', {params: {id}})
}

// 新增检验项目
export function createLaboratoryItem(data) {
    return request.post('/system/medical-item/laboratoryUpsert', data)
}

// 修改检验项目
export function updateLaboratoryItem(data) {
    return request.post('/system/medical-item/laboratoryUpsert', data)
}

// 删除检验项目
export function deleteLaboratoryItem(id) {
    return request.delete('/system/medical-item/laboratory/deleteById', {params: {id}})
}

// ==================== 检验项目明细 ====================

// 获取检验项目明细列表
export function getLaboratoryItemDetailList(laboratoryItemId) {
    return request.get('/system/medical-item/laboratory/detail/list', {params: {laboratoryItemId}})
}

// 新增检验项目明细
export function createLaboratoryItemDetail(data) {
    return request.post('/system/medical-item/laboratory/detailUpsert', data)
}

// 修改检验项目明细
export function updateLaboratoryItemDetail(data) {
    return request.post('/system/medical-item/laboratory/detailUpsert', data)
}

// 删除检验项目明细
export function deleteLaboratoryItemDetail(id) {
    return request.delete('/system/medical-item/laboratory/detail/deleteById', {params: {id}})
}

// ========== 患者标签管理 ==========

// 查询患者标签列表
export function getPatientTagList(params) {
    return request.post('/system/patientTag/listPage', params)
}

// 查询患者标签列表（不分页）
export function getPatientTagListAll(params) {
    return request.post('/system/patientTag/list', params)
}

// 获取患者标签详情
export function getPatientTagDetail(id) {
    return request.get('/system/patientTag/getById', {params: {tagId: id}})
}

// 新增患者标签
export function createPatientTag(data) {
    return request.post('/system/patientTag/patientTagUpsert', data)
}

// 修改患者标签
export function updatePatientTag(data) {
    return request.post('/system/patientTag/patientTagUpsert', data)
}

// 删除患者标签
export function deletePatientTag(id) {
    return request.delete('/system/patientTag/deleteById', {params: {tagId: id}})
}
