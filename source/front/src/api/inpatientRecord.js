import request from './request'

// ==================== 住院病历文书（P2：结构化率 80% 的载体） ====================
// 约定：查询一律 GET，写操作一律 POST。
//
// 三个必须记住的口径：
// 1. 所有 ID 都是**字符串**：雪花ID 超过 JS 的 Number 安全整数范围（2^53），
//    后端统一按字符串出参，前端不要 Number() 转换（转了就会静默丢精度，
//    回头查详情必然「病历不存在」）。
// 2. /save 的语义是「传什么覆盖什么」—— 不传的字段会被置空，不是"只更新非空字段"。
//    编辑时必须把表单完整回传；这是刻意的：否则"医生把主诉删掉了"这个动作在库里留不下来
//    （服务层会逐字段 diff 后写修改日志）。
// 3. structuredTotal 是**按文书类型分别算**的分母（病程类才有"病程正文"这一项），
//    所以它不等于「份数 × 26」。qualityStat 的 elementTotal 同理。

export function getInpatientRecordListPage(params) {
    return request.get('/patient/inpatient/record/listPage', {params})
}

export function getInpatientRecordDetail(id) {
    return request.get('/patient/inpatient/record/detail', {params: {id}})
}

// 新增/修改病历文书（id 为空 = 新增）
export function saveInpatientRecord(data) {
    return request.post('/patient/inpatient/record/save', data)
}

// 提交（草稿 → 已提交；提交后仍可修改，但每次修改留痕）
export function submitInpatientRecord(data) {
    return request.post('/patient/inpatient/record/submit', data)
}

// 归档（已提交 → 已归档；单向门，归档后禁改，remark 必填）
export function archiveInpatientRecord(data) {
    return request.post('/patient/inpatient/record/archive', data)
}

// 修改日志分页（docType / recordId / recordNo / admissionId）
export function getInpatientRecordLogs(params) {
    return request.get('/patient/inpatient/record/logs', {params})
}

// 某份文书的全部修改日志（docType=1 病历 2 护理）
export function getInpatientRecordLogList(params) {
    return request.get('/patient/inpatient/record/logList', {params})
}

// 结构化率统计（admissionId）
export function getRecordQualityStat(admissionId) {
    return request.get('/patient/inpatient/record/qualityStat', {params: {admissionId}})
}

export function getInpatientRecordTypeOptions() {
    return request.get('/patient/inpatient/record/type/selectList')
}

export function getInpatientRecordStatusOptions() {
    return request.get('/patient/inpatient/record/status/selectList')
}
