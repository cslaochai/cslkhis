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

// 审核病历
export function reviewRecord(recordId, approved, remark, reviewerName) {
    return request.post('/emr/recordReview', null, {
        params: {recordId, approved, remark, reviewerName}
    })
}
