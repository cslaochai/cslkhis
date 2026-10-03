import request from './request'

/**
 * 合理用药知识库（sql/130）
 *
 * 两张表两套接口，权限码 system:drugKnowledge:list / :add / :delete。
 * 新增与修改在后端是同一个 xxxUpsert，所以前端只有一个 upsert 函数（id 为空即新增）。
 */

export function interactionListPage(params) {
    return request.post('/system/drugKnowledge/interactionListPage', params)
}

export function interactionUpsert(data) {
    return request.post('/system/drugKnowledge/interactionUpsert', data)
}

export function interactionDeleteById(id) {
    return request.delete('/system/drugKnowledge/interactionDeleteById', {params: {id}})
}

export function doseLimitListPage(params) {
    return request.post('/system/drugKnowledge/doseListPage', params)
}

export function doseLimitUpsert(data) {
    return request.post('/system/drugKnowledge/doseUpsert', data)
}

export function doseLimitDeleteById(id) {
    return request.delete('/system/drugKnowledge/doseDeleteById', {params: {id}})
}
