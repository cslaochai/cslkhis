import request from './request'

// ==================== 医保合规审核（防止高编高套 / 低编入组） ====================

// 查询结算清单编码明细（诊断 + 手术操作）
export function getSettlementCoding(settlementId) {
    return request.get('/charge/compliance/coding', {params: {settlementId}})
}

// 保存结算清单编码明细（整单覆盖）
export function saveSettlementCoding(data) {
    return request.post('/charge/compliance/coding', data)
}

// 清空结算清单编码明细
export function clearSettlementCoding(settlementId) {
    return request.delete('/charge/compliance/coding', {params: {settlementId}})
}

// 单张清单合规自查（auditType：1-结算前自查 2-批量筛查 3-医保反馈复核）
export function auditSettlementCompliance(settlementId, auditType = 1) {
    return request.post('/charge/compliance/audit', null, {params: {settlementId, auditType}})
}

// 批量合规筛查
export function batchAuditSettlementCompliance(data) {
    return request.post('/charge/compliance/batchAudit', data)
}

// 分页查询合规审核记录
export function getComplianceAuditList(params) {
    return request.get('/charge/compliance/listPage', {params})
}

// 查询合规审核详情
export function getComplianceAuditDetail(auditId) {
    return request.get('/charge/compliance/detail', {params: {auditId}})
}
