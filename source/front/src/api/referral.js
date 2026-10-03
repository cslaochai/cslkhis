import request from './request'

// ============ 双向转诊（G20）：上转上级医院 / 下转基层，0待确认→1已确认→2已完成/3已取消 ============

// 转诊登记
export function referralCreate(data) {
  return request.post('/patient/referral/create', data)
}

// 分页查询转诊单
export function referralListPage(data) {
  return request.post('/patient/referral/listPage', data)
}

// 转诊单详情
export function getReferralDetail(referralId) {
  return request.get('/patient/referral/getDetailById', { params: { referralId } })
}

// 确认转诊（0→1）
export function referralAudit(referralId, toDeptId, auditRemark) {
  return request.post('/patient/referral/audit', { referralId, toDeptId, auditRemark })
}

// 完成转诊（1→2）
export function referralFinish(referralId, finishRemark) {
  return request.post('/patient/referral/finish', { referralId, finishRemark })
}

// 取消转诊（0/1→3）
export function referralCancel(referralId, cancelReason) {
  return request.post('/patient/referral/cancel', { referralId, cancelReason })
}
