import request from './request'

// ==================== 飞检/专项审核批次（菜单 1010，sql/163） ====================

// 批次分页（附名下扣款单数与金额合计）
export function getYbInspectionList(params) {
    return request.get('/charge/ybInspection/listPage', {params})
}

// 批次详情
export function getYbInspectionDetail(id) {
    return request.get('/charge/ybInspection/getById', {params: {id}})
}

// 进行中批次下拉（新建扣款通知时挂批次）
export function getYbInspectionSelectList() {
    return request.get('/charge/ybInspection/selectList')
}

// 批次新增/修改（仅进行中可改；单号服务端生成）
export function upsertYbInspection(data) {
    return request.post('/charge/ybInspection/upsert', data)
}

// 批次结项（结论必填）
export function concludeYbInspection(data) {
    return request.post('/charge/ybInspection/conclude', data)
}

// 批次作废（名下有扣款通知时后端拒绝）
export function cancelYbInspection(data) {
    return request.post('/charge/ybInspection/cancel', data)
}

// ==================== 扣款通知单（申诉→确认追责→缴回闭环） ====================

// 扣款通知分页（附超期展示态）
export function getDeductList(params) {
    return request.get('/charge/ybDeduct/listPage', {params})
}

// 扣款通知详情（含全过程留痕）
export function getDeductDetail(id) {
    return request.get('/charge/ybDeduct/getDetailById', {params: {id}})
}

// 台账汇总（卡片数字来自后端 SQL 聚合）
export function getDeductSummary() {
    return request.get('/charge/ybDeduct/summary')
}

// 通知单新增/修改（仅待确认可改）
export function upsertDeduct(data) {
    return request.post('/charge/ybDeduct/upsert', data)
}

// 发起申诉：待确认 → 申诉中
export function appealDeduct(data) {
    return request.post('/charge/ybDeduct/appeal', data)
}

// 录入申诉结果：申诉中 → 申诉成功 / 维持扣款待缴
export function appealResultDeduct(data) {
    return request.post('/charge/ybDeduct/appealResult', data)
}

// 确认扣款并追责：→ 维持扣款待缴
export function confirmDeduct(data) {
    return request.post('/charge/ybDeduct/confirm', data)
}

// 录入缴回：待缴 → 已缴回
export function paybackDeduct(data) {
    return request.post('/charge/ybDeduct/payback', data)
}

// 作废：仅待确认
export function cancelDeduct(data) {
    return request.post('/charge/ybDeduct/cancel', data)
}
