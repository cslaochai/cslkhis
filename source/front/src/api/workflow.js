import request from './request'

// ========== 流程定义 ==========

export function flowDefinitionListPage(params) {
    return request.get('/workflow/definition/listPage', {params})
}

export function flowDefinitionDetail(id) {
    return request.get('/workflow/definition/getDetailById', {params: {id}})
}

export function flowDefinitionUpsert(data) {
    return request.post('/workflow/definition/upsert', data)
}

export function flowDefinitionPublish(id) {
    return request.post('/workflow/definition/publish', null, {params: {id}})
}

export function flowDefinitionDelete(id) {
    return request.post('/workflow/definition/deleteById', null, {params: {id}})
}

export function flowDefinitionSelectList() {
    return request.get('/workflow/definition/selectList')
}

// ========== 流程运行 ==========

export function flowStart(data) {
    return request.post('/workflow/runtime/start', data)
}

/** 统一入口：一段文本先过意图识别，再启动对应流程 */
export function flowDispatch(data) {
    return request.post('/workflow/runtime/dispatch', data)
}

export function flowMyTasks(params) {
    return request.get('/workflow/runtime/listMyTasks', {params})
}

export function flowApprove(data) {
    return request.post('/workflow/runtime/approve', data)
}

export function flowReject(data) {
    return request.post('/workflow/runtime/reject', data)
}

export function flowWithdraw(instanceId) {
    return request.post('/workflow/runtime/withdraw', null, {params: {instanceId}})
}

export function flowInstance(id) {
    return request.get('/workflow/runtime/getInstance', {params: {id}})
}

export function flowHistory(instanceId) {
    return request.get('/workflow/runtime/listHistory', {params: {instanceId}})
}

export function flowByBusinessId(businessId) {
    return request.get('/workflow/runtime/listByBusinessId', {params: {businessId}})
}
