import request from './request'

/**
 * 传染病报告卡（G11，菜单 613 / 路径 /infectious-report）
 *
 * 口径（视图层不再各写一套）：
 * 1. 状态机：1 待审核 → 2 已审核 → 3 已直报；1/2 可退报（→4，必填原因）；
 *    退报卡修改后重报回 1（report_count 递增）。3 已直报是终态，禁改禁退。
 * 2. 时限、逾期、类别文案全由后端算好（overdue/remainHours/*Text），前端只渲染不自己比时间。
 * 3. 直报 = 后端组装标准报文落库（directPayload），详情里看报文预览；
 *    真实对接疾控后同一段报文外发，前端无感。
 * 4. ID 全是雪花 ID 字符串，不要 Number()。
 */

export function getReportListPage(params) {
  return request.post('/emr/infectious/listPage', params)
}

export function getReportDetail(id) {
  return request.get('/emr/infectious/getDetailById', { params: { id } })
}

export function getDiseaseSelectList(keyword) {
  return request.get('/emr/infectious/disease/selectList', { params: { keyword } })
}

export function getReportStats() {
  return request.get('/emr/infectious/stats')
}

export function reportUpsert(data) {
  return request.post('/emr/infectious/upsert', data)
}

export function reportAudit(data) {
  return request.post('/emr/infectious/audit', data)
}

export function reportReturnCard(data) {
  return request.post('/emr/infectious/returnCard', data)
}

export function reportDirectReport(id) {
  return request.post('/emr/infectious/directReport', null, { params: { id } })
}

export function reportNotifyOverdue() {
  return request.post('/emr/infectious/notifyOverdue')
}
