import request from './request'

// ==================== 门诊慢特病病种目录（菜单 1011，sql/163） ====================

// 病种目录分页
export function getChronicCatalogList(params) {
    return request.get('/charge/ybChronic/catalogListPage', {params})
}

// 启用中的病种目录（备案表单下拉）
export function getChronicCatalogSelectList() {
    return request.get('/charge/ybChronic/catalogSelectList')
}

// 病种目录新增/修改（编码唯一，只启停不删）
export function upsertChronicCatalog(data) {
    return request.post('/charge/ybChronic/catalogUpsert', data)
}

// 病种目录启停
export function changeChronicCatalogStatus(id, status) {
    return request.post('/charge/ybChronic/changeCatalogStatus', null, {params: {id, status}})
}

// ==================== 慢特病医保备案（菜单 1011，sql/188 更名） ====================

// 备案台账分页（展示态含「已过期」）
export function getChronicRegList(params) {
    return request.get('/charge/ybChronic/regListPage', {params})
}

// 备案详情
export function getChronicRegDetail(id) {
    return request.get('/charge/ybChronic/regGetById', {params: {id}})
}

// 患者在用门特资格（医生站/收费判断能否走门特）
export function getActiveChronicRegOfPatient(patientId) {
    return request.get('/charge/ybChronic/regActiveOfPatient', {params: {patientId}})
}

// 备案汇总
export function getChronicRegSummary() {
    return request.get('/charge/ybChronic/regSummary')
}

// 备案新增/修改（经办人不传则服务端落当前登录人；仅有效可改）
export function upsertChronicReg(data) {
    return request.post('/charge/ybChronic/regUpsert', data)
}

// 备案注销（终态不可逆）
export function cancelChronicReg(data) {
    return request.post('/charge/ybChronic/regCancel', data)
}

// 备案驳回（终态不可逆）
export function rejectChronicReg(data) {
    return request.post('/charge/ybChronic/regReject', data)
}
