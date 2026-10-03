import request from './request'

/**
 * 全院总值班排班（sql/169，菜单 806）。
 *
 * 「今日总值班」是全院当天负责的那个人的唯一口径：急诊候诊/留观升级、床位跨科调配、
 * 双向转诊协调三条链路的兜底收口人都是他。current/todayList 只要求登录 ——
 * 这是贴在急诊墙上的公共信息，按菜单权限收口等于让人半夜到处打电话问。
 */

/** 当前总值班（此刻全院谁负责；found=0 表示今天没排班，页面必须显红告警） */
export function getCurrentDutyOfficer() {
  return request.get('/system/dutyRoster/current')
}

/** 今日排班（白班/夜班 × 主班/副班） */
export function getTodayDutyList() {
  return request.get('/system/dutyRoster/todayList')
}

/** 分页查询排班 */
export function getDutyRosterListPage(params) {
  return request.post('/system/dutyRoster/listPage', params)
}

/** 登记/修改排班（同一天+班次+角色重复提交 = 改） */
export function dutyRosterUpsert(data) {
  return request.post('/system/dutyRoster/dutyRosterUpsert', data)
}

/** 临时换班（保留原排班人） */
export function dutySubstitute(data) {
  return request.post('/system/dutyRoster/dutySubstitute', data)
}

/** 撤回换班 */
export function dutySubstituteCancel(id) {
  return request.post('/system/dutyRoster/dutySubstituteCancel', null, { params: { id } })
}

/** 删除排班（唯一键不含 del_flag → 物理删） */
export function deleteDutyRoster(id) {
  return request.delete('/system/dutyRoster/deleteById', { params: { id } })
}

/* ==================== 值班日志 / 交班本（sql/170） ==================== */

/** 分页查询值班日志 */
export function getDutyLogListPage(params) {
  return request.post('/system/dutyLog/listPage', params)
}

/** 待我签收的遗留事项（交班对象 = 当前登录人） */
export function getDutyLogPendingMine() {
  return request.get('/system/dutyLog/pendingMine')
}

/** 登记/修改值班日志（值班人留空 = 当前总值班） */
export function dutyLogUpsert(data) {
  return request.post('/system/dutyLog/dutyLogUpsert', data)
}

/** 交班（接班人留空 = 下一班总值班） */
export function dutyLogHandover(data) {
  return request.post('/system/dutyLog/dutyLogHandover', data)
}

/** 签收（只有接班人本人能签收） */
export function dutyLogAck(id) {
  return request.post('/system/dutyLog/dutyLogAck', null, { params: { id } })
}

/** 删除值班日志（已签收的不能删） */
export function deleteDutyLog(id) {
  return request.delete('/system/dutyLog/deleteById', { params: { id } })
}
