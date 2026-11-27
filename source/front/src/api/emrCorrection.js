import request from './request'

// 发起病历更正申请（已提交/归档病历；同一病历在途单唯一）
export function applyCorrection(data) {
    return request.post('/emr/correction/apply', data)
}

// 科室初审（通过转医务审批 / 驳回终态）
export function deptApproveCorrection(data) {
    return request.post('/emr/correction/deptApprove', data)
}

// 医务终审（通过置已批准待修改 / 驳回终态）
export function medicalApproveCorrection(data) {
    return request.post('/emr/correction/medicalApprove', data)
}

// 申请人撤回在途申请（待科室审核 / 待医务审批）
export function revokeCorrection(id) {
    return request.post('/emr/correction/revoke', null, {params: {id}})
}

// 更正申请台账分页（按数据权限收口科室）
export function getCorrectionPage(params) {
    return request.post('/emr/correction/listPage', params)
}

// 更正申请单详情（两级审批意见与回写留痕）
export function getCorrectionDetail(id) {
    return request.get('/emr/correction/getById', {params: {id}})
}
