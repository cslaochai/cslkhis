import request from './request'

// ==================== 手术器械/敷料清点（G15：术前 → 关体前 → 关体后 三阶段双人核对） ====================
//
// 四条口径：
// 1. 三阶段**必须顺序推进**，跳过会被后端拒绝（跳到"关体后才数"那就晚了）。
// 2. 每一阶段都要**逐项给全数量**，漏项会被拒（"止血钳没数但纱布数了"等于没数）。
// 3. 一致性的判定基准是**术前基线**，不是"和上一段比" ——
//    连续两段都少一块纱布时，"与上段一致"会显示通过。
// 4. 只要这台手术**建过**清点单，三轮没走完或对不上就会**锁死手术完成登记**
//    （OperationApplyServiceImpl.finish 会拦）。所以"对不上"必须立刻处理，不能放着。

// 某台手术的清点单（含明细；没有则 data 为 null）
export function getOperationCountByApply(applyId) {
    return request.get('/patient/inpatient/operationCount/getByApply', {params: {applyId}})
}

// 清点单详情
export function getOperationCountDetail(countId) {
    return request.get('/patient/inpatient/operationCount/getDetailById', {params: {countId}})
}

// 建立清点单（含清点清单），返回清点单号
export function createOperationCount(data) {
    return request.post('/patient/inpatient/operationCount/create', data)
}

// 追加一行清点明细（countId 走 query）
export function addOperationCountItem(countId, data) {
    return request.post('/patient/inpatient/operationCount/addItem', data, {params: {countId}})
}

// 登记某一阶段的清点数量（phase：1-术前 2-关体前 3-关体后）
export function countOperationPhase(data) {
    return request.post('/patient/inpatient/operationCount/countPhase', data)
}
