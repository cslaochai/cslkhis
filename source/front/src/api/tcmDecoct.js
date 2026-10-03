import request from './request'

/**
 * 中药代煎台账（sql/139）
 *
 * 单据由「发药完成」在后端生成，前端没有新增入口；停止流转只有作废（本表不提供删除）。
 * 状态只能逐级推进：1-待煎 → 2-已煎 → 3-已取；9-已作废是终态。
 */

export function tcmDecoctListPage(params) {
    return request.post('/pharmacy/tcmDecoct/listPage', params)
}

export function tcmDecoctGetDetailById(id) {
    return request.get('/pharmacy/tcmDecoct/getDetailById', {params: {id}})
}

export function tcmDecoctStatusCount() {
    return request.get('/pharmacy/tcmDecoct/statusCount')
}

/** targetStatus：2-已煎 3-已取 */
export function tcmDecoctAdvance({id, targetStatus}) {
    return request.post('/pharmacy/tcmDecoct/advance', {id, targetStatus})
}

export function tcmDecoctCancel({id, reason}) {
    return request.post('/pharmacy/tcmDecoct/cancel', {id, reason})
}

export function tcmDecoctPrint(id) {
    return request.post('/pharmacy/tcmDecoct/print', {id})
}
