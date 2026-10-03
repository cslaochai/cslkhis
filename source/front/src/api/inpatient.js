import request from './request'

// ==================== 入出院管理（入出院闭环 + 病案首页） ====================
// 约定：查询一律 GET（与后端 @GetMapping 对齐），写操作一律 POST。
// 注意 admit 返回的入院ID是字符串：雪花ID 超过 JS 的 Number 安全整数范围，
// 后端已按字符串出参，前端不要再 Number() 转换，否则会静默丢精度。

// 住院列表分页（admitStatus：0-已出院 1-在院）
export function getInpatientListPage(params) {
    return request.get('/patient/inpatient/listPage', {params})
}

// 本科室住院列表分页（医生站/护士站左栏）：科室由后端按登录态强制过滤，前端不传 deptId
export function getMyDeptInpatientListPage(params) {
    return request.get('/patient/inpatient/listMyDeptPage', {params})
}

// 住院详情（入院信息 + 病案首页 + 诊断明细 + 手术明细）
export function getInpatientDetail(admissionId) {
    return request.get('/patient/inpatient/detail', {params: {admissionId}})
}

// 住院统计卡片
export function getInpatientStats() {
    return request.get('/patient/inpatient/stats')
}

// 病区列表（床位数为 sys_bed 实时统计值）
export function getInpatientWardList() {
    return request.get('/patient/inpatient/ward/list')
}

// 床位列表（bedStatus：0-维修 1-空闲 2-占用 3-锁定）
export function getInpatientBedList(params) {
    return request.get('/patient/inpatient/bed/list', {params})
}

// 病区床位图：一床一卡（含空床），科室边界由后端按登录岗位收口，前端传的 deptId 越权会被拒
export function getBedMap(params) {
    return request.get('/patient/inpatient/bedMap', {params})
}

// 入院登记（分床并占用床位），返回入院ID字符串
export function admitInpatient(data) {
    return request.post('/patient/inpatient/admit', data)
}

// 换床（限同一科室内部）
export function transferInpatientBed(data) {
    return request.post('/patient/inpatient/transfer', data)
}

// 出院办理（释放床位并回写病案首页）
export function dischargeInpatient(data) {
    return request.post('/patient/inpatient/discharge', data)
}

// 保存病案首页（诊断/手术明细整表替换）
export function saveInpatientSummary(data) {
    return request.post('/patient/inpatient/summary/save', data)
}
