import request from './request'

/**
 * 死亡证明与死亡登记（菜单 318 / 路由 /death-certificate，后端 his-patient /patient/death）
 *
 * 口径（视图层不再各写一套）：
 * 1. 证明状态机：1 草稿 → 2 已审核 → 3 已开具；4 已作废。已开具是法定凭证，禁改，
 *    错证只能「作废 → 按原证重开」（新证 orig_cert_id 指回原证，原证内容永不改）。
 * 2. 签发前置由后端把闸：该住院必须已办「离院方式=死亡」的出院，且死亡时间与出院时间同一时点。
 *    待开证榜（pendingListPage）是欠账榜 —— 没开证照样能出院，但出院报表会一直挂着。
 * 3. 上报时限、逾期、剩余小时全由后端算（overdue/remainHours），前端只渲染不自己比时间。
 * 4. 死因链Ⅰ部分按行序 (a)(b)(c)(d)，链尾即根本死因；提交时整体替换，服务端按链尾回写根本死因。
 * 5. 上报＝后端组装标准报文落库留痕（reportPayload），详情里看报文预览；真实对接死因监测系统时前端无感。
 * 6. ID 全是雪花 ID 字符串，不要 Number()。
 */

/* ---------------- 死亡证明 ---------------- */

export function getCertListPage(params) {
  return request.post('/patient/death/cert/listPage', params)
}

/** 待开证榜（已办死亡离院但无有效证明） */
export function getCertPendingListPage(params) {
  return request.post('/patient/death/cert/pendingListPage', params)
}

export function getCertDetail(id) {
  return request.get('/patient/death/cert/getDetailById', { params: { id } })
}

/** 开证底稿：死者一般项目快照 + 死亡离院时间（表单默认值服务端算，不采信前端） */
export function getCertAdmissionBase(admissionId) {
  return request.get('/patient/death/cert/admissionBase', { params: { admissionId } })
}

export function getDeathStats() {
  return request.get('/patient/death/cert/stats')
}

export function certUpsert(data) {
  return request.post('/patient/death/cert/upsert', data)
}

export function certAudit(data) {
  return request.post('/patient/death/cert/audit', data)
}

export function certIssue(data) {
  return request.post('/patient/death/cert/issue', data)
}

export function certVoid(data) {
  return request.post('/patient/death/cert/voidById', data)
}

export function certReissue(origCertId) {
  return request.post('/patient/death/cert/reissue', null, { params: { origCertId } })
}

/** 四联打印回执：先记数再出纸，只有已开具的证能打印 */
export function certPrint(data) {
  return request.post('/patient/death/cert/print', data)
}

export function certReport(id) {
  return request.post('/patient/death/cert/report', null, { params: { id } })
}

export function certNotifyOverdue() {
  return request.post('/patient/death/cert/notifyOverdue')
}

/* ---------------- 死亡登记 ---------------- */

export function getRegisterListPage(params) {
  return request.post('/patient/death/register/listPage', params)
}

export function getRegisterDetail(id) {
  return request.get('/patient/death/register/getDetailById', { params: { id } })
}

export function getRegisterBase(admissionId) {
  return request.get('/patient/death/register/base', { params: { admissionId } })
}

/** 可登记候选：已办死亡离院的住院（一次性抓候选，不是分页查询） */
export function getRegisterAdmissions(keyword, limit) {
  return request.get('/patient/death/register/admissions', { params: { keyword, limit } })
}

export function registerUpsert(data) {
  return request.post('/patient/death/register/upsert', data)
}

export function registerConfirm(data) {
  return request.post('/patient/death/register/confirm', data)
}

export function registerVoid(data) {
  return request.post('/patient/death/register/voidById', data)
}
