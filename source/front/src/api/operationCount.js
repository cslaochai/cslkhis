import request from './request'

// 某台手术的清点单（含明细；没有则 data 为 null）
export function getOperationCountByApply(applyId) {
    return request.get('/patient/inpatient/operationCount/getByApply', {params: {applyId}})
}

// 建立清点单（含清点清单），返回清点单号
export function createOperationCount(data) {
    return request.post('/patient/inpatient/operationCount/create', data)
}

// 登记某一阶段的清点数量（phase：1-术前 2-关体前 3-关体后）
export function countOperationPhase(data) {
    return request.post('/patient/inpatient/operationCount/countPhase', data)
}
