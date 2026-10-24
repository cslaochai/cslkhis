import request from './request'

// ==================== 慢病建档/认定（菜单 2051，sql/165） ====================
// 建档即认定（confirmStatus=1）；作废是单向的 1→2，作废后长处方开方资格立即失效。
// 服务端才是唯一口径：同患者同慢病只允许一条有效档案、超 dumped 次数、who 认定的 —— 都不由前端判。

// 慢病档案分页
export function getChronicRecordList(params) {
    return request.post('/chronic/listPage', params)
}

// 患者有效慢病档案（已认定且未作废；长处方资格判定用，后端 /chronic/activeByPatient）
export function getChronicActiveByPatient(patientId) {
    return request.get('/chronic/activeByPatient', { params: { patientId } })
}

// 慢病建档（建档即认定）
export function upsertChronicRecord(data) {
    return request.post('/chronic/upsert', data)
}

// 慢病档案作废（单向：已认定 → 已取消）
export function cancelChronicRecord(recordId) {
    return request.post('/chronic/cancel', { recordId })
}
