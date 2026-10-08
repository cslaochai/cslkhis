import request from './request'

// ---------------- 术前访视 ----------------
// 术前访视分页（admissionId / applyId / conclusion / unfinishedOnly / keyword）
export function getAnesthesiaVisitListPage(params) {
    return request.get('/patient/inpatient/anesthesia/visitListPage', {params})
}

// 术前访视详情
export function getAnesthesiaVisitDetail(visitId) {
    return request.get('/patient/inpatient/anesthesia/visitGetDetailById', {params: {visitId}})
}

// 某台手术的术前访视（没有则 data 为 null）
export function getAnesthesiaVisitByApply(applyId) {
    return request.get('/patient/inpatient/anesthesia/visitGetByApply', {params: {applyId}})
}

// 保存术前访视（新增/修改草稿），返回访视单号
export function saveAnesthesiaVisit(data) {
    return request.post('/patient/inpatient/anesthesia/visitSave', data)
}

// 完成术前访视（结论出账）
export function finishAnesthesiaVisit(data) {
    return request.post('/patient/inpatient/anesthesia/visitFinish', data)
}

// 已完成但没有合格术前访视的手术台数
export function getFinishedWithoutVisitCount() {
    return request.get('/patient/inpatient/anesthesia/countFinishedWithoutVisit')
}

// ---------------- 麻醉记录单 ----------------

// 麻醉记录单分页（admissionId / applyId / anesthetistId / recordStatus / keyword / unchargedOnly）
export function getAnesthesiaRecordListPage(params) {
    return request.get('/patient/inpatient/anesthesia/recordListPage', {params})
}

// 麻醉记录单详情（含生命体征与用药）
export function getAnesthesiaRecordDetail(recordId) {
    return request.get('/patient/inpatient/anesthesia/recordGetDetailById', {params: {recordId}})
}

// 某台手术的麻醉记录单（没有则 data 为 null）
export function getAnesthesiaRecordByApply(applyId) {
    return request.get('/patient/inpatient/anesthesia/recordGetByApply', {params: {applyId}})
}

// 开立麻醉记录单（返回麻醉记录单号）
export function createAnesthesiaRecord(data) {
    return request.post('/patient/inpatient/anesthesia/recordCreate', data)
}

// 更新麻醉记录单（仅「记录中」可改）
export function updateAnesthesiaRecord(data) {
    return request.post('/patient/inpatient/anesthesia/recordUpdate', data)
}

// 追加一条生命体征（仅「记录中」可加）
export function addAnesthesiaVital(data) {
    return request.post('/patient/inpatient/anesthesia/addVital', data)
}

// 生命体征列表
export function listAnesthesiaVitals(recordId) {
    return request.get('/patient/inpatient/anesthesia/listVitals', {params: {recordId}})
}

// 追加一条麻醉用药（仅「记录中」可加）
export function addAnesthesiaMed(data) {
    return request.post('/patient/inpatient/anesthesia/addMed', data)
}

// 麻醉用药列表
export function listAnesthesiaMeds(recordId) {
    return request.get('/patient/inpatient/anesthesia/listMeds', {params: {recordId}})
}

// 提交麻醉记录（自动联动计费，返回 OperationChargeSummary）
export function submitAnesthesiaRecord(data) {
    return request.post('/patient/inpatient/anesthesia/recordSubmit', data)
}

// 审核麻醉记录
export function auditAnesthesiaRecord(data) {
    return request.post('/patient/inpatient/anesthesia/recordAudit', data)
}

// 麻醉计费（失败项重试）
export function chargeAnesthesiaRecord(data) {
    return request.post('/patient/inpatient/anesthesia/recordCharge', data)
}

// 尚未计费的麻醉记录单数
export function getAnesthesiaUnchargedCount() {
    return request.get('/patient/inpatient/anesthesia/countUncharged')
}

// ---------------- PACU ----------------

// PACU 复苏记录分页
export function getPacuListPage(params) {
    return request.get('/patient/inpatient/pacu/listPage', {params})
}

// PACU 详情
export function getPacuDetail(pacuId) {
    return request.get('/patient/inpatient/pacu/getDetailById', {params: {pacuId}})
}

// 某条麻醉记录的 PACU 复苏单（没有则 data 为 null）
export function getPacuByRecord(recordId) {
    return request.get('/patient/inpatient/pacu/getByRecord', {params: {recordId}})
}

// 入 PACU 登记（返回复苏单号）
export function enterPacu(data) {
    return request.post('/patient/inpatient/pacu/enter', data)
}

// Aldrete 评分（只传五项，总分服务端算）
export function scorePacu(data) {
    return request.post('/patient/inpatient/pacu/score', data)
}

// 出 PACU（自动联动计费）
export function leavePacu(data) {
    return request.post('/patient/inpatient/pacu/leave', data)
}

// PACU 计费（失败项重试）
export function chargePacu(data) {
    return request.post('/patient/inpatient/pacu/charge', data)
}

// 在室人数
export function getPacuInRoomCount() {
    return request.get('/patient/inpatient/pacu/countInRoom')
}

// ---------------- 麻醉术后随访（P134.3） ----------------
// 挂在麻醉记录上（一台麻醉可有多轮：术后即刻/24h/48h/追加）。
// 轮次、患者快照、随访人一律服务端定；草稿可改可删，**完成即锁死**。

// 随访分页（recordId / admissionId / patientId / followupStatus / keyword）
export function getFollowupListPage(params) {
    return request.get('/patient/inpatient/anesthesiaFollowup/listPage', {params})
}

// 随访详情（含并发症字典 adverseItemOptions）
export function getFollowupDetail(id) {
    return request.get('/patient/inpatient/anesthesiaFollowup/getDetailById', {params: {id}})
}

// 某条麻醉记录的全部随访（按轮次升序）
export function getFollowupListByRecord(recordId) {
    return request.get('/patient/inpatient/anesthesiaFollowup/listByRecord', {params: {recordId}})
}

// 新增/修改随访草稿（返回随访单号）
export function saveFollowup(data) {
    return request.post('/patient/inpatient/anesthesiaFollowup/save', data)
}

// 完成随访（疼痛/恢复必填；勾了并发症必须写经过+处理；完成即锁死）
export function finishFollowup(id) {
    return request.post('/patient/inpatient/anesthesiaFollowup/finish', null, {params: {id}})
}

// 删除随访（仅草稿）
export function deleteFollowup(id) {
    return request.delete('/patient/inpatient/anesthesiaFollowup/deleteById', {params: {id}})
}

// 随访欠账数（已提交麻醉结束超24h且无已完成随访）
export function getFollowupOverdueCount() {
    return request.get('/patient/inpatient/anesthesiaFollowup/countOverduePending')
}
