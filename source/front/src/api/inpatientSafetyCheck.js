import request from './request'

// ==================== 手术三方安全核查（P134.2：麻醉实施前 → 手术开始前 → 离开手术室前） ====================
// 约定：查询一律 GET，写操作一律 POST。
//
// 口径（错了会签出假核查）：
// 1. 所有 ID 都是**字符串**，前端不要 Number() 转换（雪花ID 丢精度后回查必然「不存在」）。
// 2. 三个时段是法定的、有顺序的：Sign In（麻醉实施前）→ Time Out（手术开始前）→ Sign Out（离开手术室前）。
//    只能按顺序签，不能跳、不能重复；某一轮签了三个人的名字就**不可改不可删**（只增不改不删）。
// 3. 三方（手术医师/麻醉医师/手术室护士）必须**三个不同的人**，后端成对校验，前端只提示。
// 4. 只有「已排期/术前核对完成」的手术能签；待排期没有手术台可核，已完成再签是术后伪造。
// 5. 签过一轮就必须签满三轮才能登记手术完成（后端 finish 闸门）；一轮都不签目前不拦。
// 6. 时间一律 `yyyy-MM-dd HH:mm:ss`（后端 @JsonFormat 只认空格分隔）。

// 某台手术的三时段核查卡（cards：每时段的要点字典 + 已签记录 + canSign/不可签原因）
export function getSafetyCheckCards(applyId) {
    return request.get('/patient/inpatient/safetyCheck/cardsByApply', {params: {applyId}})
}

// 签一个时段（applyId + phase + items 码值逗号串 + surgeonId/anesthetistId/nurseId 三方签名）
export function signSafetyCheck(data) {
    return request.post('/patient/inpatient/safetyCheck/sign', data)
}
