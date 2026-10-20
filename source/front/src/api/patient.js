import request from './request'

// 查询患者列表
export function getPatientList(params) {
    return request.post('/patient/listPage', params)
}

// 获取患者详情
export function getPatientDetail(id) {
    return request.get('/patient/getById', {params: {patientId: id}})
}

// 获取患者完整信息（同时带回过敏史/既往疾病史/手术外伤史/家族史）
export function getPatientFullDetail(id) {
    return request.get('/patient/getDetailById', {params: {patientId: id}})
}

// 新增患者
export function createPatient(data) {
    return request.post('/patient/patientUpsert', data)
}

// 修改患者
export function updatePatient(data) {
    return request.post('/patient/patientUpsert', data)
}

// 删除患者
export function deletePatient(id) {
    return request.delete('/patient/deleteById', {params: {patientId: id}})
}

// ==================== 患者标签关联 ====================
// 标签本身的定义（增删改查）走 /system/patientTag，见 api/system.js 的 getPatientTagList 等

// 获取患者标签列表
export function getPatientTags(params) {
    return request.get('/patient/tag/getByPatientId', {params})
}

// 添加患者标签
export function addPatientTag(params) {
    return request.post('/patient/tag/add', params)
}

// 删除患者标签
export function removePatientTag(params) {
    return request.post('/patient/tag/delete', params)
}

// ==================== 健康档案（六组） ====================
//
// 统一形状：查询一律 POST {patientId} → 明细数组；写一律 POST xxxUpsert（id 空=新增）；
// 删除一律 DELETE ?id=。六组用同一套形状，页面才能用一份通用逻辑维护六张表 ——
// 改造前这六组里：过敏/手术/家族走 POST，既往病走 GET 且**没有删除接口**，
// 用药史**连后端都没有**，联系人 relationship 还是字符串。结果就是
// api 层这半边一行都没人调用（全仓搜不到消费方），六组表长期空着也无人察觉。

// 一次带回六组 + 主档文本投影 + 分叉标记（健康档案页的唯一数据源）
export function getPatientHealthProfile(patientId) {
    return request.get('/patient/profile/getDetail', {params: {patientId}})
}

// 六组的**读**统一走 getPatientHealthProfile（一次带回六组 + 主档投影 + 分叉标记）。
// 不再为每组单独导出 list 函数：那样页面上迟早出现「有的组读聚合、有的组读单组」，
// 两条读法算出的条数一旦不一致就没人说得清哪个对。
export function saveAllergy(data) {
    return request.post('/patient/allergy/allergyUpsert', data)
}

export function deleteAllergy(id) {
    return request.delete('/patient/allergy/deleteById', {params: {id}})
}

export function savePastDisease(data) {
    return request.post('/patient/pastDisease/pastDiseaseUpsert', data)
}

export function deletePastDisease(id) {
    return request.delete('/patient/pastDisease/deleteById', {params: {id}})
}

export function saveSurgeryHistory(data) {
    return request.post('/patient/surgeryHistory/surgeryHistoryUpsert', data)
}

export function deleteSurgeryHistory(id) {
    return request.delete('/patient/surgeryHistory/deleteById', {params: {id}})
}

export function saveFamilyHistory(data) {
    return request.post('/patient/familyHistory/familyHistoryUpsert', data)
}

export function deleteFamilyHistory(id) {
    return request.delete('/patient/familyHistory/deleteById', {params: {id}})
}

export function saveMedicationHistory(data) {
    return request.post('/patient/medication/medicationUpsert', data)
}

export function deleteMedicationHistory(id) {
    return request.delete('/patient/medication/deleteById', {params: {id}})
}

// 新增或修改联系人。relationship 传的是**码值**（字典 sys_patient_relation），
// 不是「配偶」这样的文案 —— 传文案会被 MySQL 隐式转成 0 静默落库（历史踩过）。
export function savePatientContact(data) {
    return request.post('/patient/contact/contactUpsert', data)
}

export function deletePatientContact(contactId) {
    return request.delete('/patient/contact/deleteById', {params: {contactId}})
}
