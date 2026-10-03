import request from './request'

// ==================== 住院证（门诊转住院） ====================
// 约定：查询一律 GET（与后端 @GetMapping 对齐），写操作一律 POST。
// 注意 create 返回的住院证ID是字符串：雪花ID 超过 JS 的 Number 安全整数范围，
// 后端已按字符串出参，前端不要再 Number() 转换，否则会静默丢精度。

// 开住院证（门诊医生站调用）
export function createAdmissionOrder(data) {
    return request.post('/patient/admissionOrder/create', data)
}

// 住院证分页（住院处待收治看板 / 按患者或挂号反查）
export function getAdmissionOrderListPage(params) {
    return request.get('/patient/admissionOrder/listPage', {params})
}

// 住院证详情
export function getAdmissionOrderDetail(id) {
    return request.get('/patient/admissionOrder/detail', {params: {id}})
}

// 作废住院证（仅「待收治」可作废）
export function cancelAdmissionOrder(data) {
    return request.post('/patient/admissionOrder/cancel', data)
}

// 待收治且未过期的证数量
export function getAdmissionOrderPendingCount() {
    return request.get('/patient/admissionOrder/pendingCount')
}
