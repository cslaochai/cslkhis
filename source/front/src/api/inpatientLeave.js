import request from './request'

/**
 * 住院患者请假/离院登记（菜单 320 / 路由 /inpatient-leave，后端 his-patient /patient/inpatient/leave）
 *
 * 口径（视图层不再各写一套）：
 * 1. 状态机：1 待审批 → 2 已批准（医师电子签名锁定，biz_type=10）→ 3 已离院（患方承诺三要素签字+实际离院时间）
 *    → 4 已返回（销假闭环）；1 → 5 已拒绝（必填理由）；1/2 → 6 已取消。已离院的单不许取消（人已出去，事实不能蒸发）。
 * 2. 超期未归是查询时算的展示态（expectedReturnTime 已过且 status=3），后端出 overdue/overdueHours，不落状态列。
 * 3. 离院登记三要素缺一不可：确认人姓名 + 与患者关系（字典 his_notice_relation） + 手写签名 —— 责任界定凭证。
 * 4. 审批的落款人 = 当前登录医师，后端强制，前端不传签名人；批准必须填医师意见（病情评估）。
 * 5. 动作可用性全部读后端 canApprove/canLeave/canBack/canCancel/canContact/canPrint，前端不自判状态。
 * 6. ID 全是雪花 ID 字符串，不要 Number()。
 */

export function getLeaveListPage(params) {
  return request.post('/patient/inpatient/leave/listPage', params)
}

export function getLeaveById(id) {
  return request.get('/patient/inpatient/leave/getById', { params: { id } })
}

/** 开单底稿：按住院带出患者快照（服务端重查，不采信前端字符串） */
export function getLeaveBase(admissionId) {
  return request.get('/patient/inpatient/leave/base', { params: { admissionId } })
}

/** 在院患者候选（横幅数据源，含在途请假单张数与在途状态） */
export function getLeaveInpatients(params) {
  return request.get('/patient/inpatient/leave/inpatients', { params })
}

export function getLeaveStats() {
  return request.get('/patient/inpatient/leave/stats')
}

export function leaveUpsert(data) {
  return request.post('/patient/inpatient/leave/upsert', data)
}

/** 审批：allow=true 批准（医师意见必填，后端电子签名锁定）；allow=false 拒绝（理由必填） */
export function leaveApprove(data) {
  return request.post('/patient/inpatient/leave/approve', data)
}

/** 登记离院：患方承诺三要素（姓名/关系/手写签名 dataURL）+ 实际离院时间 */
export function leaveConfirm(data) {
  return request.post('/patient/inpatient/leave/confirmLeave', data)
}

/** 返回销假 */
export function leaveBack(data) {
  return request.post('/patient/inpatient/leave/confirmBack', data)
}

export function leaveCancel(data) {
  return request.post('/patient/inpatient/leave/cancel', data)
}

/** 超期处置记录（联系结果 + 上报对象） */
export function leaveContact(data) {
  return request.post('/patient/inpatient/leave/recordContact', data)
}

export function leavePrint(data) {
  return request.post('/patient/inpatient/leave/print', data)
}
