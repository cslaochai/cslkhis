import request from './request'

// 查询随访任务列表
export function getFollowupList(params) {
  return request.post('/charge/followup/listPage', params)
}

// 获取随访任务详情（编辑回显：返回明文手机号）
export function getFollowupDetail(id) {
  return request.get('/charge/followup/getById', { params: { id } })
}

/**
 * 出院随访任务看板（服务端聚合：今日应访 / 逾期 / 完成率 / 科室待办 TOP10）。
 *
 * 逾期按 followup_time 与当前时间现算，不是某个状态列 —— 没有定时任务去翻状态，
 * 数状态列得到的「逾期」永远偏小，看板就成了摆设。
 */
export function getFollowupStat() {
  return request.get('/charge/followup/stat')
}

// 开始随访
export function startFollowup(id, params) {
  return request.post('/charge/followup/startFollowup', { id, ...params })
}

// 完成随访
export function completeFollowup(id, params) {
  return request.post('/charge/followup/completeFollowup', { id, ...params })
}

// 取消随访
export function cancelFollowup(id, params) {
  return request.post('/charge/followup/cancelFollowup', { id, ...params })
}

// ============ G20：新建/修改随访任务、按出院记录一键生成 ============

// 新建 / 修改随访任务（修改仅待随访可改）
export function followupUpsert(data) {
  return request.post('/charge/followup/upsert', data)
}

// 按出院记录一键生成随访计划（幂等，同一出院记录重复调用返回已生成任务）
export function followupCreateFromDischarge(data) {
  return request.post('/charge/followup/createFromDischarge', data)
}

/**
 * 随访任务生成复诊号（复诊来源 4-随访计划复诊）
 *
 * data: { taskId, revisitRecordId, scheduleId, slotId?, settlementType }
 * 走的是后端 AppointService.addAppoint 同一条链路（扣号源、按策略建收费单），
 * 成功后把复诊号回写到任务上（revisitAppointId），一条任务只能挂一个有效复诊号。
 */
export function followupCreateRevisitAppoint(data) {
  return request.post('/charge/followup/createRevisitAppoint', data)
}

// ============ G-15：电话外呼（mock 通道=人工登记待呼，护士拨打后回填结果） ============

// 登记电话外呼（返回任务 VO，含明文手机号供拨号）
export function followupCallRegister(id) {
  return request.post('/charge/followup/callRegister', { id })
}

// 回填外呼结果 data: { id, connected, remark? }；接通且任务待随访时自动转随访中
export function followupCallResult(data) {
  return request.post('/charge/followup/callResult', data)
}
