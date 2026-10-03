import request from './request'

// ==================== 住院护理文书（三测单 / 护理记录单 / 生命体征监测） ====================
// 两条必须记住的口径：
// 1. 三测单**同一测量时点只允许一条**（库唯一索引兜底）。重复录入会收到明确报错，
//    不是静默覆盖 —— 同一次测量录两条，体温曲线上就是两个点，护士不知道该信哪个。
//    要改就走「修改」传 id。
// 2. /tempSheet 返回的点是后端**按测量时间升序排好的**，前端直接用，不要再排序；
//    而且曲线不分页（分一次页曲线就断一段）。

export function getNursingRecordListPage(params) {
    return request.get('/patient/inpatient/nursing/listPage', {params})
}

export function getNursingRecordDetail(id) {
    return request.get('/patient/inpatient/nursing/detail', {params: {id}})
}

// 录入/修改护理文书（id 为空 = 新增；因三测单按时点唯一，measureTime 必填）
export function saveNursingRecord(data) {
    return request.post('/patient/inpatient/nursing/save', data)
}

// 三测单数据（admissionId 必填，beginDate/endDate 可选；返回按时间升序的点 + 体温上下限）
export function getTempSheet(params) {
    return request.get('/patient/inpatient/nursing/tempSheet', {params})
}

export function getNursingTypeOptions() {
    return request.get('/patient/inpatient/nursing/type/selectList')
}

// ==================== G14 护理完整体 ====================

// 体温单批量录入（一次测量动作 × 多个在院患者；后端整体事务，任何一行不合法整批拒绝）
export function saveNursingRecordsBatch(data) {
    return request.post('/patient/inpatient/nursing/saveBatch', data)
}

// 护理评估单（1-压疮Braden 2-跌倒Morse 3-疼痛NRS 4-VTE Caprini 5-管路滑脱；总分由后端对明细求和复算）
export function saveNursingAssessment(data) {
    return request.post('/patient/inpatient/nursing/assessment/save', data)
}

// 护理评估单分页（admissionId 与 wardId 至少传一个，后端拒绝全院裸捞）
export function getNursingAssessmentListPage(params) {
    return request.get('/patient/inpatient/nursing/assessment/listPage', {params})
}

// 专项评估透视：每类量表最新一条（没评过的类型不返回）
export function getAssessmentLatestByType(params) {
    return request.get('/patient/inpatient/nursing/assessment/latestByType', {params})
}

// 出入量小结（从护理文书原始测量行按日复算，days 日期升序）
export function getIntakeOutputSummary(params) {
    return request.get('/patient/inpatient/nursing/intakeOutputSummary', {params})
}
