import request from './request'

// ==================== 住院会诊（P4.1：申请 → 应答 → 完成 → 回写病历） ====================
// 约定：查询一律 GET，写操作一律 POST。
//
// 四条必须记住的口径：
// 1. 所有 ID 都是**字符串**：雪花ID 超过 JS 的 Number 安全整数范围，后端统一按字符串出参，
//    前端不要 Number() 转换，否则会静默丢精度（回头查详情必然「会诊记录不存在」）。
// 2. 「能否接诊 / 完成 / 取消 / 修改」由后端给的 canAccept / canFinish / canCancel / canEdit 决定，
//    前端不要自己按 consultStatus 码值 switch —— 后端加一个状态，switch 会静默渲染成"看着正常"的错按钮。
// 3. **完成会诊会自动回写住院病历**（record_type=9 会诊记录，状态=已提交），并把病历ID 回填到
//    record_id。所以前端不许手工建"会诊记录"型文书（后端也会拒绝），病历号从 recordNo 读。
// 4. 急会诊「超时」是后端**查询时算**的（overdue / overdueText），库里没有这个状态列，
//    前端不要自己拿时间差算一遍（两套算法必然对不上）。

// 会诊分页（admissionId / patientId / fromDeptId / toDeptId / consultStatus / consultType / isUrgent / keyword / unfinishedOnly）
export function getConsultationListPage(params) {
    return request.get('/patient/inpatient/consultation/listPage', {params})
}

// 会诊详情
export function getConsultationDetail(consultationId) {
    return request.get('/patient/inpatient/consultation/getDetailById', {params: {consultationId}})
}

// 申请 / 修改会诊（返回会诊号）
export function saveConsultation(data) {
    return request.post('/patient/inpatient/consultation/save', data)
}

// 会诊科室应答（接诊人 = 当前登录用户，不允许替别人接诊）
export function acceptConsultation(data) {
    return request.post('/patient/inpatient/consultation/accept', data)
}

// 完成会诊（结论必填；完成即回写病历，返回回写的病历ID）
export function finishConsultation(data) {
    return request.post('/patient/inpatient/consultation/finish', data)
}

// 取消会诊申请（仅「待应答」）
export function cancelConsultation(data) {
    return request.post('/patient/inpatient/consultation/cancel', data)
}

// 未完成会诊数（待应答 + 已应答）
export function getConsultationUnfinishedCount(params) {
    return request.get('/patient/inpatient/consultation/countUnfinished', {params})
}
