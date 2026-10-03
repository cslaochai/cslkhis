import request from './request'

/**
 * 病危/病重通知与告知书签收回执（菜单 319 / 路由 /critical-notice，后端 his-patient /patient/criticalNotice）
 *
 * 口径（视图层不再各写一套）：
 * 1. 状态机：1 草稿 → 2 已签发（医师电子签名锁定）→ 3 已签收（家属手写签名+法定关系）；4 已作废。
 *    已签发禁改（要改先在签名中心作废签名）；已签收不许作废（患方签字的告知事实不能事后蒸发）。
 * 2. 签发的落款人 = 当前登录职工，后端强制，前端不传签名人。
 * 3. 签收三要素缺一不可：签收人姓名 + 与患者关系（字典 his_notice_relation，法定必填） + 手写签名图。
 * 4. 回执两联（病历联+患方联），只有已签收可打印，打印一次后端计数一次。
 * 5. 身份证/电话出参已后端脱敏（signerIdCardMasked/signerPhoneMasked），前端不渲染明文、不做遮码。
 * 6. ID 全是雪花 ID 字符串，不要 Number()。
 */

export function getNoticeListPage(params) {
  return request.post('/patient/criticalNotice/listPage', params)
}

export function getNoticeById(id) {
  return request.get('/patient/criticalNotice/getById', { params: { id } })
}

/** 开单底稿：按住院带出患者快照（服务端重查，不采信前端字符串） */
export function getNoticeBase(admissionId) {
  return request.get('/patient/criticalNotice/base', { params: { admissionId } })
}

/** 在院患者候选（医生站横幅数据源，带每人历史通知张数） */
export function getNoticeInpatients(params) {
  return request.get('/patient/criticalNotice/inpatients', { params })
}

export function getNoticeDoctorOptions() {
  return request.get('/patient/criticalNotice/doctorOptions')
}

export function getNoticeStats() {
  return request.get('/patient/criticalNotice/stats')
}

export function noticeUpsert(data) {
  return request.post('/patient/criticalNotice/upsert', data)
}

/** 签发：后端以当前登录医师身份做 RSA 电子签名（biz_type=9），失败整体回滚 */
export function noticeIssue(data) {
  return request.post('/patient/criticalNotice/issue', data)
}

/** 签收：家属手写签名 dataURL + 法定关系 */
export function noticeAcknowledge(data) {
  return request.post('/patient/criticalNotice/acknowledge', data)
}

export function noticeVoid(data) {
  return request.post('/patient/criticalNotice/voidById', data)
}

export function noticePrint(data) {
  return request.post('/patient/criticalNotice/print', data)
}
