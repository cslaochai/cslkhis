import request from './request'

// 护理排班模板（周模板 + 班次实用排班模式 + 套用到周）
// 后端控制器：/nursing/scheduleTemplate（his-ms-outpatient，2026-10-25 新增）
// 权限域复用 护理排班 nursing:schedule:*（list/add/edit/delete），嵌入「护理排班」页的「排班模板」签。
//
// 套用落库复用现有护理周排班底座（biz_schedule + scheduleSource=TEMPLATE），与「复制上周」同一条事实写入口。
// 班次下拉复用 getNurseShiftOptions（use_scope=2 病区护理班次），护士候选复用 getNurseSelectList。

// ========== 统计卡 ==========
export function getTemplateStats() {
  return request.get('/nursing/scheduleTemplate/stats')
}

// ========== 模板分页（不含已删除） ==========
export function pageTemplates(data) {
  return request.post('/nursing/scheduleTemplate/pageTemplates', data)
}

// ========== 模板详情（含明细行） ==========
export function getTemplateDetail(id) {
  return request.get('/nursing/scheduleTemplate/detail', { params: { id } })
}

// ========== 新增/修改模板（主表+明细同事务替换） ==========
export function upsertTemplate(data) {
  return request.post('/nursing/scheduleTemplate/upsert', data)
}

// ========== 删除模板（逻辑删，联动明细） ==========
export function deleteTemplate(id) {
  return request.delete('/nursing/scheduleTemplate/delete', { params: { id } })
}

// ========== 启停模板（0-停用 1-启用） ==========
export function updateTemplateStatus(id, status) {
  return request.post('/nursing/scheduleTemplate/updateStatus', null, { params: { id, status } })
}

// ========== 班次实用排班模式规则列表（含停用行） ==========
export function listTemplateRules() {
  return request.get('/nursing/scheduleTemplate/listRules')
}

// ========== 新增/修改规则 ==========
export function upsertTemplateRule(data) {
  return request.post('/nursing/scheduleTemplate/upsertRule', data)
}

// ========== 删除规则（逻辑删） ==========
export function deleteTemplateRule(id) {
  return request.delete('/nursing/scheduleTemplate/deleteRule', { params: { id } })
}

// ========== 套用模板到周（生成护士周排班，来源=TEMPLATE） ==========
export function applyTemplateToWeek(data) {
  return request.post('/nursing/scheduleTemplate/applyToWeek', data)
}

// ========== 适用记录分页（按套用时间倒序） ==========
export function pageApplies(data) {
  return request.post('/nursing/scheduleTemplate/pageApplies', data)
}

// ========== 班次实用排班模式下拉（use_scope=2 病区护理班次） ==========
// 后端复用现有护理排班班次收口：BizScheduleMapper.selectNursingShifts()
export function getNurseShiftOptions() {
  return request.get('/nursing/schedule/nursingShifts')
}
