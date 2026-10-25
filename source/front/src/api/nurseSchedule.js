import request from './request'

// 病区护理排班（sql/166，菜单 330 护理管理 / 331 病区护理排班）
// 后端控制器：/nursing/schedule（his-appoint，2026-10-11 自 his-patient 迁入）
//
// 与门诊医生排班（/schedule + /shift，权限码 org.schedule.*）是两套班次册：
// biz_shift.use_scope 1-门诊/急诊 2-病区护理。本页所有班次都来自矩阵接口回传的 shifts
// （后端已按 use_scope=2 + status=1 收口），前端不自己拉全院班次。

// ========== 参照数据（跨岗位下拉，后端只要求登录） ==========

// 病区下拉：只含当前岗位可见科室下的启用病区，附在册护士数（旧接口，只列病区）
export function getNurseWardSelectList(params) {
  return request.get('/nursing/schedule/wardSelectList', { params })
}

// 护理排班单元下拉（sql/209）：病区 + 有护理编制的门诊科室，附在册护士数
// unitType 1-病区 2-门诊科室；两种单元的 id 不在同一个空间，请求必须带着类型一起走
export function getNurseUnitSelectList(params) {
  return request.get('/nursing/schedule/unitSelectList', { params })
}

// 该排班单元所属科室的在册护士（护士/护师）候选下拉，供排班模板明细行选护士用
// 后端 /nursing/schedule/nurseSelectList：unitType(可选) / unitId(必填) / keyword(可选)
export function getNurseSelectList(unitType, unitId, keyword) {
  return request.get('/nursing/schedule/nurseSelectList', {
    params: { unitType, unitId, keyword }
  })
}

// ========== 周矩阵排班 ==========

// 周矩阵：行=在册护士 列=周一至周日，一次带回 cells + 本周告警 + 每日每班次在岗人数
export function getNurseWeekMatrix(data) {
  return request.post('/nursing/schedule/weekMatrix', data)
}

// 点格排班/改格（一人一天一条；非上班状态由服务端清空班次与工时）
export function upsertNurseScheduleCell(data) {
  return request.post('/nursing/schedule/upsert', data)
}

// 删除一格（后端物理删：唯一键不含 del_flag，软删会让「同一人同一天重排」撞键）
export function deleteNurseScheduleCell(id) {
  return request.delete('/nursing/schedule/deleteById', { params: { id } })
}

// 复制上周：只填目标周空缺格，已排的不覆盖
export function copyNurseScheduleWeek(data) {
  return request.post('/nursing/schedule/copyWeek', data)
}

// 批量铺班：共用「状态+班次」× 选中日期 × 多选护士，只填空缺格、已排的跳过不覆盖
export function batchUpsertNurseSchedule(data) {
  return request.post('/nursing/schedule/batchUpsert', data)
}

// 审批与变更留痕分页（审计：谁在何时对哪位护士哪天做了提交/发布/撤回/调班/改格）
export function listNurseScheduleLogPage(data) {
  return request.post('/nursing/schedule/logPage', data)
}

// ========== 发布态流转（G-15：排班保存只到草稿，审核发布后才计在岗） ==========
// 入参 id = 一行排班的主键（矩阵 Cell.id / 台账 Row.id，后端已 ToStringSerializer 字符串化）

// 提交审核：草稿 → 待审核（排班人自己提交，仍要审核发布才生效）
export function submitNurseSchedule(id) {
  return request.post('/nursing/schedule/submit', null, { params: { id } })
}

// 审核发布：待审核 → 已发布（此后才计入在岗 / 需求层 / 全院视图）
export function auditPublishNurseSchedule(id) {
  return request.post('/nursing/schedule/auditPublish', null, { params: { id } })
}

// 撤回：任意态 → 草稿（撤回后立刻不计在岗，二次确认由调用方把关）
export function recallNurseSchedule(id) {
  return request.post('/nursing/schedule/recall', null, { params: { id } })
}

// 进入调班：已发布/已变更 → 调班中（锁定，改完保存自动转「已变更」）
export function adjustStartNurseSchedule(id) {
  return request.post('/nursing/schedule/adjustStart', null, { params: { id } })
}

// 整周提交审核：本周草稿 → 待审核（入参 { unitType, wardId, weekStart }）
export function submitWeekNurseSchedule(data) {
  return request.post('/nursing/schedule/submitWeek', data)
}

// 整周审核发布：本周待审核 → 已发布（此后计在岗；入参 { unitType, wardId, weekStart }）
export function auditWeekNurseSchedule(data) {
  return request.post('/nursing/schedule/auditWeek', data)
}

// 整周备案：本周备案态 待备案 → 已备案（护理部留痕，不改变在岗口径；入参 { unitType, wardId, weekStart }）
export function fileWeekNurseSchedule(data) {
  return request.post('/nursing/schedule/fileWeek', data)
}

// 整周备案退回：待备案/已备案 → 已退回，本周已发布行整批打回草稿（入参同备案，另带 opinion 退回原因）
export function rejectFileWeekNurseSchedule(data) {
  return request.post('/nursing/schedule/fileRejectWeek', data)
}

// ========== 规则校验 / 工时 / 台账 ==========

// 区间规则校验（告警明细，不阻断保存）
export function checkNurseSchedule(data) {
  return request.post('/nursing/schedule/check', data)
}

// 月度工时统计（含整段未排班的人）
export function getNurseMonthWorkload(data) {
  return request.post('/nursing/schedule/monthWorkload', data)
}

// 排班台账分页（跨病区回看）
export function listNurseSchedulePage(data) {
  return request.post('/nursing/schedule/listPage', data)
}

// ========== 人力配置标准 ==========

// 标准列表（含停用行，看得见某条规则为什么不生效）
export function getNurseScheduleRuleList(unitType, unitId) {
  return request.get('/nursing/schedule/ruleList', { params: { unitType, unitId } })
}

// 保存标准（shiftId=0 是病区级行，周工时/连班上限只在这一行有效）
export function upsertNurseScheduleRule(data) {
  return request.post('/nursing/schedule/ruleUpsert', data)
}

// 删除标准（物理删）
export function deleteNurseScheduleRule(id) {
  return request.delete('/nursing/schedule/ruleDeleteById', { params: { id } })
}
