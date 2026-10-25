import request from './request'

// 查询病历列表
export function getEmrRecordList(params) {
    return request.get('/emr/getByPatientId', {params})
}
// 查询病历列表
export function getByRegistId(params) {
    return request.get('/emr/getByRegistId', {params})
}

// 分页查询病历列表
export function getRecordListPage(params) {
    return request.get('/emr/listPage', {params})
}

// 获取病历详情
export function getRecordDetail(recordId) {
    return request.get('/emr/getRecordDetailById', {params: {recordId}})
}

// ========== 门诊日志（法规台账，sql/131；只读，报卡走 /emr/infectious） ==========
export function getOutpatientLogPage(params) {
    return request.get('/emr/outpatientLog/listPage', {params})
}

export function getOutpatientLogStats(params) {
    return request.get('/emr/outpatientLog/stats', {params})
}

export function getMyPendingPage(params) {
    return request.get('/emr/outpatientLog/myPending', {params})
}

export function getMyPendingStats(params) {
    return request.get('/emr/outpatientLog/myPendingStats', {params})
}

// 审核病历
export function reviewRecord(recordId, approved, remark, reviewerName) {
    return request.post('/emr/recordReview', null, {
        params: {recordId, approved, remark, reviewerName}
    })
}

// 打印就诊文书：现场生成引导单 + 普通处方签 + 饮片处方签三份 PDF
export function printVisitDocs(recordId) {
    return request.get('/emr/printVisitDocs', {params: {recordId}})
}
