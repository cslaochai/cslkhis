import request from './request'

// ==================== 床位服务中心（等床队列 / 全院床位调配） ====================
// 约定：查询一律 GET（与后端 @GetMapping 对齐），写操作一律 POST。
// 两个 ID 口径：
//  1) 排队 waitId / 床位 bedId / 入院 admissionId 一律按**字符串**传 —— 雪花ID 超过 JS 的安全整数范围，
//     后端已按 ToStringSerializer 出参，前端再 Number() 会静默丢精度。
//  2) 后端已经把「能不能点」算好了（canAssign / canRelease / canCancel / canAdmit），
//     视图层只读这两个字段，不自己拼状态机 —— 两边各写一遍判断就一定漂移。

// ---------------- 等床队列 ----------------

// 队列分页（排序由服务端钉死：优先级降序 → 登记时间升序，前端不给排序入参）
export function getBedWaitListPage(params) {
    return request.get('/patient/bedCenter/queue/listPage', {params})
}

export function getBedWaitDetail(waitId) {
    return request.get('/patient/bedCenter/queue/getDetailById', {params: {waitId}})
}

// 登记 / 修改排队（有 id = 修改）
export function upsertBedWait(data) {
    return request.post('/patient/bedCenter/queue/upsert', data)
}

// 安排床位（含跨科调配）：床位置「锁定」并挂上患者
export function assignBed(data) {
    return request.post('/patient/bedCenter/queue/assignBed', data)
}

// 退回队列：释放已锁定的床位
export function releaseBed(data) {
    return request.post('/patient/bedCenter/queue/releaseBed', data)
}

// 取消排队（已安排床位的一并释放）
export function cancelBedWait(data) {
    return request.post('/patient/bedCenter/queue/cancel', data)
}

// 按已安排床位办理入院（返回入院ID字符串）
export function admitBedWait(data) {
    return request.post('/patient/bedCenter/queue/admit', data)
}

export function getBedWaitStats() {
    return request.get('/patient/bedCenter/queue/stats')
}

export function countBedWaiting() {
    return request.get('/patient/bedCenter/queue/countWaiting')
}

// ---------------- 床位池与匹配 ----------------

// 床位智能匹配候选（给候选不给最优解：决定由现场做）
export function matchBeds(waitId) {
    return request.get('/patient/bedCenter/pool/match', {params: {waitId}})
}

// 全院床位池（availableOnly=true 只给「空闲且未被预留」的床，安排床位选床时用）
export function getBedPool(params) {
    return request.get('/patient/bedCenter/pool/listPage', {params})
}

export function getBedOverview() {
    return request.get('/patient/bedCenter/overview')
}

// 床位调配图（一床一卡）。与护士站 /patient/inpatient/bedMap 是同一张图的两种视角：
// 护士看"床上躺着谁、几级护理"，这里看"这张床能不能用、被谁预定了、能不能直接放人"。
// deptId 不传时服务端落到当前账号主岗位科室（全院 1000+ 张床画不开，也不该画）。
export function getBedCenterMap(params) {
    return request.get('/patient/bedCenter/pool/bedMap', {params})
}
