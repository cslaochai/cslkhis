import request from './request'


// 查询处方列表
export function getPrescriptionList(params) {
    return request.get('/prescription/getByPatientId', {params})
}

// 处方分页查询（审方工作台；unauditedOnly=true 只看未审方）
export function getPrescriptionListPage(params) {
    return request.get('/prescription/listPage', {params})
}

// 处方审核（审方药师签名；审核人由后端从登录态取，前端不传）
// pin 为药师本次输入的 PIN；退回（auditResult=2）不产生签名，无需传 pin
export function auditPrescription(data) {
    return request.post('/prescription/audit', data)
}

// 合理用药批量核查（相互作用 + 剂量上限）；返回 [{prescriptionId, blocked, blockMessage, hits}]
export function rationalCheckPrescriptions(prescriptionIds) {
    return request.post('/prescription/rationalCheck', {prescriptionIds})
}

// 开方实时审方（前置审方）：医生工作台处方行未保存时按行数据即时审查；返回 [{hitType, blocked, drugNameA, drugNameB, message, suggestion}]
export function rationalCheckDraft(items) {
    return request.post('/prescription/rationalCheckDraft', {items})
}

// 查询检查申请列表（按挂号取本次就诊，返回带 execStatusText / critical / canDelete）
export function getInspectionApplyList(params) {
    return request.get('/inspection/getByPatientId', {params})
}

// 检查开单（批次E：开单即落库，不再等"保存病历"）
// 开单即签：pin 为签名人本次输入的 PIN，后端校验通过才解密私钥
export function saveInspectionApply(data) {
    return request.post('/inspection/applyUpsert', data)
}

// 删除检查申请单（仅未缴费且无执行记录/收费引用时允许）
export function deleteInspectionApply(id) {
    return request.delete('/inspection/deleteById', {params: {id}})
}

// 查询检验申请列表（按挂号取本次就诊，返回带 execStatusText / critical / canDelete）
export function getLaboratoryApplyList(params) {
    return request.get('/laboratory/getByPatientId', {params})
}

// 检验开单（开单即落库）
// 开单即签：pin 为签名人本次输入的 PIN，后端校验通过才解密私钥
export function saveLaboratoryApply(data) {
    return request.post('/laboratory/applyUpsert', data)
}

// 删除检验申请单
export function deleteLaboratoryApply(id) {
    return request.delete('/laboratory/deleteById', {params: {id}})
}

// ========== 预问诊（G-05） ==========

// 按挂号取预问诊记录（患者小程序提交的问卷 + AI 凝摘要）；无记录返回 data=null
export function getPrevisitByRegist(registId) {
    return request.get('/previsit/getByRegist', {params: {registId}})
}

// ========== 统一保存接口 ==========

// 保存病历（临时保存）
export function saveMedicalRecord(data) {
    return request.post('/medicalRecord/recordSave', data)
}

// 结诊提交。config 透传给 axios（结诊调用带 silent：否决项拦截要弹框让医生选，不能让全局提示先弹一次红条）
export function submitMedicalRecord(data, config) {
    return request.post('/medicalRecord/recordSubmitDirect', data, config)
}

// 医生撤回结诊（已提交→草稿，本人当日未缴费未执行；已归档拒绝）
export function revokeMedicalRecord(recordId, queueId, reason) {
    return request.post('/medicalRecord/recordRevoke', null, {
        params: {recordId, queueId, reason}
    })
}

// ========== 个人模板 ==========

// 查询常用诊断模板
export function getDiagTemplates() {
    return request.get('/doctor/template/diagList')
}

// 保存常用诊断模板（后端契约是 {templates: [...]} 对象，页面传裸数组在此包一层）
export function saveDiagTemplates(templates) {
    return request.post('/doctor/template/saveDiag', {templates})
}

// 删除常用诊断模板
export function deleteDiagTemplate(id) {
    return request.delete('/doctor/template/deleteDiagById', {params: {id}})
}

// 查询处方模板列表
export function getRxTemplates() {
    return request.get('/doctor/template/rxList')
}

// 查询处方模板明细
export function getRxTemplateDetail(id) {
    return request.get('/doctor/template/getRxById', {params: {id}})
}

// 新增处方模板
export function saveRxTemplate(data) {
    return request.post('/doctor/template/saveRx', data)
}

// 删除处方模板
export function deleteRxTemplate(id) {
    return request.delete('/doctor/template/deleteRxById', {params: {id}})
}

// 查询药品套餐列表
export function getDrugPackages() {
    return request.get('/doctor/template/packageList')
}

// 查询药品套餐明细
export function getDrugPackageDetail(id) {
    return request.get('/doctor/template/getPackageById', {params: {id}})
}

// 新增药品套餐
export function saveDrugPackage(data) {
    return request.post('/doctor/template/savePackage', data)
}

// 删除药品套餐
export function deleteDrugPackage(id) {
    return request.delete('/doctor/template/deletePackageById', {params: {id}})
}

// ========== 检查申请模板 ==========

// 查询检查申请模板列表
export function getInspectionTemplates() {
    return request.get('/doctor/inspection/template/list')
}

// 新增检查申请模板
export function saveInspectionTemplate(data) {
    return request.post('/doctor/inspection/template/templateUpsert', data)
}

// 删除检查申请模板
export function deleteInspectionTemplate(id) {
    return request.delete('/doctor/inspection/template/deleteById', {params: {id}})
}

// ========== 检验申请模板 ==========

// 查询检验申请模板列表
export function getLaboratoryTemplates() {
    return request.get('/doctor/laboratory/template/list')
}

// 新增检验申请模板
export function saveLaboratoryTemplate(data) {
    return request.post('/doctor/laboratory/template/templateUpsert', data)
}

// 删除检验申请模板
export function deleteLaboratoryTemplate(id) {
    return request.delete('/doctor/laboratory/template/deleteById', {params: {id}})
}
